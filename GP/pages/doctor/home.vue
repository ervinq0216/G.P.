<template>
  <view class="container">
    <view class="nav-tabs">
      <view v-for="(tab, index) in tabs" :key="index" class="tab-item" 
            :class="{ active: currentTab === index }" @click="currentTab = index">
        <text class="tab-text">{{ tab }}</text>
        <!-- 个人中心Tab如果有未读消息，显示小红点 -->
        <view class="tab-line" v-if="currentTab === index"></view>
        <view class="red-dot-small" v-if="index === 2 && unreadCount > 0"></view>
      </view>
    </view>

    <view class="content-area">
      <!-- 模块1: 签到签退 (不变) -->
      <scroll-view scroll-y class="module-checkin" v-if="currentTab === 0">
         <!-- ...省略日历代码... -->
         <view class="calendar-card">
           <view class="calendar-header">
             <text>{{ currentYear }}年{{ currentMonth }}月</text>
             <view class="today-btn" @click="jumpToday">回到今天</view>
           </view>
           <view class="week-row">
             <text v-for="d in ['日','一','二','三','四','五','六']" :key="d">{{d}}</text>
           </view>
           <view class="days-grid">
             <view v-for="(day, idx) in days" :key="idx" class="day-cell" :class="{ 'is-today': day.isToday, 'selected': day.dateStr === selectedDateStr }" @click="selectDate(day)">
               <text class="day-num">{{ day.day }}</text>
               <view class="dot" v-if="day.hasRecord"></view>
             </view>
           </view>
         </view>
         <view class="detail-card">
           <!-- ...省略打卡按钮... -->
           <view class="record-row" v-for="(range, key) in checkRanges" :key="key">
             <view class="time-info"><text class="time-label">{{ range.label }}</text><text class="time-range">{{ range.start }}-{{ range.end }}</text></view>
             <view class="status-box">
               <text v-if="currentRecord[key]" class="done">已打卡 {{ currentRecord[key] }}</text>
               <button v-else-if="isToday" class="check-btn" :class="{ orange: key.includes('Out') }" @click="doCheck(key)">{{ key.includes('In') ? '签到' : '签退' }}</button>
               <text v-else class="miss">未打卡</text>
             </view>
           </view>
         </view>
      </scroll-view>

      <!-- 模块2: 今日病患 (不变) -->
      <view class="module-patient" v-if="currentTab === 1">
        <view class="empty-box">
          <image src="/static/logo.png" class="empty-img" mode="aspectFit" style="opacity: 0.3;"></image>
          <text class="empty-text">暂无今日预约病患</text>
        </view>
      </view>

      <!-- 模块3: 个人中心 (增加红点) -->
      <view class="module-profile" v-if="currentTab === 2">
        <view class="profile-header">
          <image :src="userInfo.avatar || '/static/default_avatar.png'" class="avatar-img" mode="aspectFill"></image>
          <view class="user-info">
            <text class="name">{{ userInfo.realName || '医生' }}</text>
            <text class="job-num">工号：{{ userInfo.jobNumber }}</text>
          </view>
        </view>

        <view class="menu-list">
          <view class="menu-item" @click="goToPage('/pages/doctor/info')">
            <view class="menu-left"><text class="icon">👤</text><text>编辑个人信息</text></view><text class="arrow">></text>
          </view>
          <view class="menu-item" @click="goToPage('/pages/common/change-password')">
            <view class="menu-left"><text class="icon">🔒</text><text>修改密码</text></view><text class="arrow">></text>
          </view>
          
          <!-- 消息通知带红点 -->
          <view class="menu-item" @click="goToPage('/pages/doctor/messages')">
            <view class="menu-left">
              <text class="icon">🔔</text>
              <text>消息通知</text>
              <view class="badge" v-if="unreadCount > 0">{{ unreadCount }}</view>
            </view>
            <text class="arrow">></text>
          </view>

          <picker mode="date" :start="todayStr" @change="onDateSelected">
            <view class="menu-item">
              <view class="menu-left"><text class="icon">📅</text><text>请假申请</text></view><text class="arrow">></text>
            </view>
          </picker>
        </view>

        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </view>
    </view>

    <!-- 弹窗部分 (不变) -->
    <view class="modal-mask" v-if="showLeaveModal">
      <view class="modal-content">
        <view class="modal-title">申请请假</view>
        <view class="modal-date">日期：{{ leaveForm.date }}</view>
        <text class="form-label">请假时段：</text>
        <view class="period-options">
          <view v-for="p in ['上午', '下午', '全天']" :key="p" class="period-btn" :class="{ active: leaveForm.period === p }" @click="leaveForm.period = p">{{ p }}</view>
        </view>
        <text class="form-label">请假理由：</text>
        <textarea class="modal-textarea" v-model="leaveForm.reason" placeholder="请输入请假原因..." />
        <view class="modal-btns"><button @click="showLeaveModal = false">取消</button><button class="primary" @click="submitLeave">提交申请</button></view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      tabs: ['签到签退', '今日病患', '个人中心'],
      currentTab: 0,
      userInfo: {},
      unreadCount: 0, // 未读数
      checkRanges: { morningIn: { label: '早上签到', start: '07:45', end: '08:15' }, morningOut: { label: '早上签退', start: '11:45', end: '12:15' }, afternoonIn: { label: '下午签到', start: '14:15', end: '14:45' }, afternoonOut: { label: '下午签退', start: '17:45', end: '18:15' } },
      currentYear: new Date().getFullYear(),
      currentMonth: new Date().getMonth() + 1,
      selectedDateStr: '', days: [], recordsMap: {}, currentRecord: {},
      showLeaveModal: false, leaveForm: { date: '', period: '全天', reason: '' }, todayStr: ''
    };
  },
  computed: { isToday() { return this.selectedDateStr === this.getLocalTodayStr(); } },
  onShow() {
    const user = uni.getStorageSync('userInfo');
    if (user) {
      this.userInfo = user;
      this.fetchDoctorInfo();
      this.fetchUnreadCount(); // 获取未读数
      
      if (user.password === '123456') {
        uni.showModal({ title: '安全提醒', content: '您的密码为初始密码(123456)，存在安全风险，请立即修改！', showCancel: false, confirmText: '去修改', success: (res) => { if (res.confirm) { uni.navigateTo({ url: '/pages/common/change-password' }); } } });
      }
      this.todayStr = this.getLocalTodayStr();
      this.initCalendar();
      this.fetchMonthRecords();
    } else { uni.reLaunch({ url: '/pages/login/index' }); }
  },
  methods: {
    // --- 新增：获取未读数 ---
    fetchUnreadCount() {
      uni.request({
        url: 'http://localhost:8080/api/doctor/unread-count',
        data: { doctorId: this.userInfo.id },
        success: (res) => {
          if(res.data.code === 200) this.unreadCount = res.data.data;
        }
      });
    },

    // --- 其他方法 (保持不变) ---
    onDateSelected(e) { this.leaveForm.date = e.detail.value; this.leaveForm.period = '全天'; this.leaveForm.reason = ''; this.showLeaveModal = true; },
    submitLeave() { if (!this.leaveForm.reason) return uni.showToast({ title: '请输入理由', icon: 'none' }); uni.showLoading({ title: '提交中' }); uni.request({ url: 'http://localhost:8080/api/leave/apply', method: 'POST', data: { doctorId: this.userInfo.id, startDate: this.leaveForm.date, period: this.leaveForm.period, reason: this.leaveForm.reason, type: '事假' }, success: (res) => { uni.hideLoading(); if (res.data.code === 200) { this.showLeaveModal = false; uni.showToast({ title: '申请已提交' }); setTimeout(() => this.goToPage('/pages/doctor/messages'), 1000); } else { uni.showToast({ title: res.data.msg, icon: 'none' }); } } }); },
    getLocalTodayStr() { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`; },
    fetchDoctorInfo() { uni.request({ url: 'http://localhost:8080/api/doctor/info/' + this.userInfo.id, method: 'GET', success: (res) => { if (res.data.code === 200) this.userInfo = res.data.data; } }); },
    initCalendar() { const year = this.currentYear; const month = this.currentMonth - 1; const todayStr = this.getLocalTodayStr(); if (!this.selectedDateStr) this.selectedDateStr = todayStr; const firstDay = new Date(year, month, 1).getDay(); const daysInMonth = new Date(year, month + 1, 0).getDate(); const tempDays = []; for (let i = 0; i < firstDay; i++) tempDays.push({ day: '', empty: true }); for (let i = 1; i <= daysInMonth; i++) { const dStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`; tempDays.push({ day: i, dateStr: dStr, isToday: dStr === todayStr, hasRecord: false }); } this.days = tempDays; },
    fetchMonthRecords() { uni.request({ url: 'http://localhost:8080/api/attendance/list', data: { doctorId: this.userInfo.id, month: `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}` }, success: (res) => { if (res.data.code === 200) { const map = {}; res.data.data.forEach(item => map[item.date] = item); this.recordsMap = map; this.days.forEach(d => { if (!d.empty && map[d.dateStr]) d.hasRecord = true; }); this.updateDetail(); } } }); },
    selectDate(day) { if (!day.empty) { this.selectedDateStr = day.dateStr; this.updateDetail(); } },
    jumpToday() { this.selectedDateStr = this.getLocalTodayStr(); this.initCalendar(); this.updateDetail(); this.fetchMonthRecords(); },
    updateDetail() { this.currentRecord = this.recordsMap[this.selectedDateStr] || {}; },
    doCheck(type) { uni.showLoading({ title: '打卡中' }); uni.request({ url: 'http://localhost:8080/api/attendance/check', method: 'POST', data: { doctorId: this.userInfo.id, type }, success: (res) => { uni.hideLoading(); if (res.data.code === 200) { uni.showToast({ title: '打卡成功' }); this.fetchMonthRecords(); } else uni.showToast({ title: res.data.msg, icon: 'none' }); } }); },
    goToPage(url) { uni.navigateTo({ url }); },
    handleLogout() { uni.showModal({ title: '提示', content: '确定退出吗？', success: (res) => { if (res.confirm) { uni.clearStorageSync(); uni.reLaunch({ url: '/pages/login/index' }); } }}); }
  }
};
</script>

<style>
/* 保持原有CSS，增加红点样式 */
.container { height: 100vh; display: flex; flex-direction: column; background-color: #f5f7fa; }
.nav-tabs { height: 90rpx; display: flex; background: #fff; box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05); z-index: 10; flex-shrink: 0; }
.tab-item { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative; }
.tab-text { font-size: 28rpx; color: #666; }
.tab-item.active .tab-text { color: #2b86ff; font-weight: bold; font-size: 30rpx; }
.tab-line { position: absolute; bottom: 10rpx; width: 40rpx; height: 6rpx; background: #2b86ff; border-radius: 3rpx; }
.red-dot-small { width: 12rpx; height: 12rpx; background: #ff4d4f; border-radius: 50%; position: absolute; top: 20rpx; right: 40rpx; }

.content-area { flex: 1; overflow: hidden; display: flex; flex-direction: column; }
.module-checkin { height: 100%; padding: 20rpx; box-sizing: border-box; }
.calendar-card { background: #fff; border-radius: 20rpx; padding: 30rpx; margin-bottom: 20rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03); }
.calendar-header { display: flex; justify-content: space-between; margin-bottom: 30rpx; font-weight: bold; font-size: 32rpx; align-items: center; }
.today-btn { font-size: 24rpx; color: #2b86ff; border: 1rpx solid #2b86ff; padding: 6rpx 16rpx; border-radius: 20rpx; font-weight: normal; }
.week-row { display: flex; justify-content: space-around; margin-bottom: 20rpx; color: #999; font-size: 26rpx; }
.days-grid { display: flex; flex-wrap: wrap; }
.day-cell { width: 14.28%; height: 80rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative; }
.day-num { font-size: 30rpx; color: #333; width: 60rpx; height: 60rpx; line-height: 60rpx; text-align: center; border-radius: 50%; }
.is-today .day-num { background: #e6f7ff; color: #2b86ff; font-weight: bold; }
.selected .day-num { background: #2b86ff !important; color: #fff !important; }
.dot { width: 8rpx; height: 8rpx; background: #52c41a; border-radius: 50%; position: absolute; bottom: 10rpx; }
.detail-card { background: #fff; border-radius: 20rpx; padding: 30rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03); }
.date-title { font-size: 32rpx; font-weight: bold; border-left: 8rpx solid #2b86ff; padding-left: 20rpx; display: block; margin-bottom: 30rpx; }
.record-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; padding: 20rpx; background: #f8f9fb; border-radius: 12rpx; }
.time-info { display: flex; flex-direction: column; }
.time-label { font-size: 30rpx; color: #333; font-weight: 500; }
.time-range { font-size: 24rpx; color: #999; margin-top: 6rpx; }
.done { color: #52c41a; font-size: 28rpx; font-weight: bold; }
.miss { color: #ccc; font-size: 28rpx; }
.check-btn { font-size: 26rpx; background: #2b86ff; color: #fff; padding: 0 30rpx; height: 60rpx; line-height: 60rpx; border-radius: 30rpx; min-width: 140rpx; }
.check-btn.orange { background: #ff9800; }
.module-patient { height: 100%; display: flex; align-items: center; justify-content: center; }
.empty-img { width: 200rpx; height: 200rpx; margin-bottom: 20rpx; }
.empty-text { color: #999; font-size: 30rpx; }
.module-profile { padding: 30rpx; background: #f5f7fa; flex: 1; }
.profile-header { background: #fff; border-radius: 20rpx; padding: 40rpx; display: flex; align-items: center; margin-bottom: 30rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03); }
.avatar-img { width: 120rpx; height: 120rpx; border-radius: 50%; margin-right: 30rpx; background: #eee; }
.name { font-size: 36rpx; font-weight: bold; display: block; margin-bottom: 10rpx; color: #333; }
.job-num { font-size: 26rpx; color: #999; }
.menu-list { background: #fff; border-radius: 20rpx; padding: 0 30rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03); }
.menu-item { display: flex; justify-content: space-between; padding: 35rpx 0; border-bottom: 1rpx solid #f5f5f5; font-size: 30rpx; align-items: center; }
.menu-item:last-child { border-bottom: none; }
.menu-left { display: flex; align-items: center; }
.icon { margin-right: 20rpx; font-size: 34rpx; width: 50rpx; text-align: center; }
.arrow { color: #ccc; font-size: 28rpx; }
.badge { background: #ff4d4f; color: #fff; font-size: 20rpx; padding: 0 10rpx; border-radius: 20rpx; margin-left: 20rpx; }
.logout-btn { margin-top: 60rpx; background: #fff; color: #ff4d4f; border-radius: 45rpx; height: 90rpx; line-height: 90rpx; font-size: 32rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05); }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0,0,0,0.6); z-index: 999; display: flex; align-items: center; justify-content: center; }
.modal-content { width: 600rpx; background-color: #fff; border-radius: 16rpx; padding: 40rpx; }
.modal-title { font-size: 32rpx; font-weight: bold; text-align: center; margin-bottom: 30rpx; }
.modal-date { text-align: center; color: #2b86ff; margin-bottom: 20rpx; font-weight: bold; }
.form-label { font-size: 28rpx; color: #333; margin: 20rpx 0 10rpx; display: block; }
.period-options { display: flex; gap: 20rpx; margin-bottom: 20rpx; }
.period-btn { flex: 1; text-align: center; height: 60rpx; line-height: 60rpx; background: #f5f5f5; border-radius: 30rpx; font-size: 26rpx; color: #666; }
.period-btn.active { background: #e6f7ff; color: #2b86ff; border: 1rpx solid #2b86ff; }
.modal-textarea { border: 1rpx solid #ddd; height: 150rpx; padding: 20rpx; width: 100%; box-sizing: border-box; margin-bottom: 20rpx; background: #f9f9f9; font-size: 28rpx; }
.modal-btns { display: flex; gap: 20rpx; }
.modal-btns button { flex: 1; font-size: 28rpx; }
.primary { background: #2b86ff; color: #fff; }
</style>