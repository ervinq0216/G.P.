<template>
  <view class="container">
    
    <!-- 顶部 Tab -->
    <view class="nav-tabs">
      <view v-for="(tab, index) in tabs" :key="index" class="tab-item" 
            :class="{ active: currentTab === index }" @click="currentTab = index">
        <text class="tab-text">{{ tab }}</text>
        <view class="tab-line" v-if="currentTab === index"></view>
      </view>
    </view>

    <view class="content-area">
      
      <!-- Module 1: 签到签退 -->
      <scroll-view scroll-y class="module-checkin" v-if="currentTab === 0">
        <view class="calendar-card">
          <view class="calendar-header">
            <text>{{ currentYear }}年{{ currentMonth }}月</text>
            <view class="today-btn" @click="jumpToday">回到今天</view>
          </view>
          <view class="week-row">
            <text v-for="d in ['日','一','二','三','四','五','六']" :key="d">{{d}}</text>
          </view>
          <view class="days-grid">
            <view v-for="(day, idx) in days" :key="idx" 
                  class="day-cell" 
                  :class="{ 'is-today': day.isToday, 'selected': day.dateStr === selectedDateStr }"
                  @click="selectDate(day)">
              <text class="day-num">{{ day.day }}</text>
              <!-- 这里的 dot 是考勤记录标记(绿色)，选中状态是蓝色背景 -->
              <view class="dot" v-if="day.hasRecord"></view>
            </view>
          </view>
        </view>

        <view class="detail-card">
          <view class="detail-header">
            <text class="date-title">{{ selectedDateStr }} 考勤记录</text>
          </view>
          
          <view class="record-row">
            <view class="time-info"><text class="time-label">早上签到 (07:45-08:15)</text></view>
            <view class="status-box">
              <text v-if="currentRecord.morningIn" class="done">已签到 {{ currentRecord.morningIn }}</text>
              <button v-else-if="isToday" class="check-btn" @click="doCheck('morningIn')">签到</button>
              <text v-else class="miss">未签到</text>
            </view>
          </view>
          <view class="record-row">
            <view class="time-info"><text class="time-label">早上签退 (11:45-12:15)</text></view>
            <view class="status-box">
              <text v-if="currentRecord.morningOut" class="done">已签退 {{ currentRecord.morningOut }}</text>
              <button v-else-if="isToday" class="check-btn orange" @click="doCheck('morningOut')">签退</button>
              <text v-else class="miss">未签退</text>
            </view>
          </view>
          <view class="record-row">
            <view class="time-info"><text class="time-label">下午签到 (14:15-14:45)</text></view>
            <view class="status-box">
              <text v-if="currentRecord.afternoonIn" class="done">已签到 {{ currentRecord.afternoonIn }}</text>
              <button v-else-if="isToday" class="check-btn" @click="doCheck('afternoonIn')">签到</button>
              <text v-else class="miss">未签到</text>
            </view>
          </view>
          <view class="record-row">
            <view class="time-info"><text class="time-label">下午签退 (17:45-18:15)</text></view>
            <view class="status-box">
              <text v-if="currentRecord.afternoonOut" class="done">已签退 {{ currentRecord.afternoonOut }}</text>
              <button v-else-if="isToday" class="check-btn orange" @click="doCheck('afternoonOut')">签退</button>
              <text v-else class="miss">未签退</text>
            </view>
          </view>
        </view>
        <view style="height: 40rpx;"></view>
      </scroll-view>

      <!-- Module 2: 今日病患 -->
      <view class="module-patient" v-if="currentTab === 1">
        <view class="empty-box">
          <image src="/static/logo.png" class="empty-img" mode="aspectFit" style="opacity: 0.3;"></image>
          <text class="empty-text">暂无今日预约病患</text>
        </view>
      </view>

      <!-- Module 3: 个人中心 -->
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
            <view class="menu-left">
              <text class="icon">👤</text>
              <text>编辑个人信息</text>
            </view>
            <text class="arrow">></text>
          </view>
          <view class="menu-item" @click="goToPage('/pages/common/change-password')">
            <view class="menu-left">
              <text class="icon">🔒</text>
              <text>修改密码</text>
            </view>
            <text class="arrow">></text>
          </view>
          <view class="menu-item" @click="showToast('请假功能开发中')">
            <view class="menu-left">
              <text class="icon">📅</text>
              <text>请假申请</text>
            </view>
            <text class="arrow">></text>
          </view>
        </view>

        <button class="logout-btn" @click="handleLogout">退出登录</button>
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
      
      // 日历相关
      currentYear: new Date().getFullYear(),
      currentMonth: new Date().getMonth() + 1,
      selectedDateStr: '', 
      days: [],
      recordsMap: {}, 
      currentRecord: {} 
    };
  },
  computed: {
    isToday() {
      // 使用修复后的本地时间获取方法
      return this.selectedDateStr === this.getLocalTodayStr();
    }
  },
  onShow() {
    const user = uni.getStorageSync('userInfo');
    if (user) {
      this.userInfo = user; 
      this.fetchDoctorInfo();

      if (user.password === '123456') {
        uni.showModal({
          title: '安全提醒',
          content: '您的密码为初始密码(123456)，存在安全风险，请立即修改！',
          showCancel: false,
          confirmText: '去修改',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({ url: '/pages/common/change-password' });
            }
          }
        });
      }

      this.initCalendar();
      this.fetchMonthRecords();
    } else {
      uni.reLaunch({ url: '/pages/login/index' });
    }
  },
  methods: {
    // --- 核心修复：获取本地时区的 YYYY-MM-DD ---
    getLocalTodayStr() {
      const d = new Date();
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },

    fetchDoctorInfo() {
      uni.request({
        url: 'http://localhost:8080/api/doctor/info/' + this.userInfo.id,
        method: 'GET',
        success: (res) => {
          if (res.data.code === 200) {
            this.userInfo = res.data.data;
          }
        }
      });
    },

    // --- 日历逻辑 ---
    initCalendar() {
      const date = new Date();
      const year = date.getFullYear();
      const month = date.getMonth(); 
      
      // 修复：使用本地时间字符串，而不是 toISOString()
      const todayStr = this.getLocalTodayStr();
      
      if (!this.selectedDateStr) this.selectedDateStr = todayStr;

      const firstDay = new Date(year, month, 1).getDay();
      const daysInMonth = new Date(year, month + 1, 0).getDate();

      const tempDays = [];
      for (let i = 0; i < firstDay; i++) {
        tempDays.push({ day: '', empty: true });
      }
      for (let i = 1; i <= daysInMonth; i++) {
        const dStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
        tempDays.push({
          day: i,
          dateStr: dStr,
          isToday: dStr === todayStr,
          hasRecord: false
        });
      }
      this.days = tempDays;
    },

    fetchMonthRecords() {
      uni.request({
        url: 'http://localhost:8080/api/attendance/list',
        method: 'GET',
        data: {
          doctorId: this.userInfo.id,
          month: `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}`
        },
        success: (res) => {
          if (res.data.code === 200) {
            const map = {};
            res.data.data.forEach(item => {
              map[item.date] = item;
            });
            this.recordsMap = map;
            this.days.forEach(d => {
              if (!d.empty && map[d.dateStr]) d.hasRecord = true;
            });
            this.updateDetail();
          }
        }
      });
    },

    selectDate(day) {
      if (day.empty) return;
      this.selectedDateStr = day.dateStr;
      this.updateDetail();
    },

    jumpToday() {
      // 修复：使用本地时间
      this.selectedDateStr = this.getLocalTodayStr();
      
      // 确保日历视图也重置到当前年月 (防止翻页后点回到今天没反应)
      this.currentYear = new Date().getFullYear();
      this.currentMonth = new Date().getMonth() + 1;
      
      this.initCalendar(); 
      this.updateDetail();
      this.fetchMonthRecords(); // 刷新数据
    },

    updateDetail() {
      this.currentRecord = this.recordsMap[this.selectedDateStr] || {};
    },

    doCheck(type) {
      uni.showLoading({ title: '打卡中' });
      uni.request({
        url: 'http://localhost:8080/api/attendance/check',
        method: 'POST',
        data: {
          doctorId: this.userInfo.id,
          type: type
        },
        success: (res) => {
          uni.hideLoading();
          if (res.data.code === 200) {
            uni.showToast({ title: '打卡成功' });
            this.fetchMonthRecords();
          } else {
            uni.showToast({ title: res.data.msg, icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    },

    goToPage(url) { uni.navigateTo({ url }); },
    showToast(msg) { uni.showToast({ title: msg, icon: 'none' }); },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出吗？',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('userInfo');
            uni.removeStorageSync('token');
            uni.removeStorageSync('role');
            uni.reLaunch({ url: '/pages/login/index' });
          }
        }
      });
    }
  }
};
</script>

<style>
.container { height: 100vh; display: flex; flex-direction: column; background-color: #f5f7fa; }
.nav-tabs { height: 90rpx; display: flex; background: #fff; box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05); z-index: 10; flex-shrink: 0; }
.tab-item { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative; }
.tab-text { font-size: 28rpx; color: #666; }
.tab-item.active .tab-text { color: #2b86ff; font-weight: bold; font-size: 30rpx; }
.tab-line { position: absolute; bottom: 10rpx; width: 40rpx; height: 6rpx; background: #2b86ff; border-radius: 3rpx; }
.content-area { flex: 1; overflow: hidden; display: flex; flex-direction: column; }

/* 模块1: 考勤 */
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

/* 模块2: 今日病患 */
.module-patient { height: 100%; display: flex; align-items: center; justify-content: center; }
.empty-box { display: flex; flex-direction: column; align-items: center; }
.empty-img { width: 200rpx; height: 200rpx; margin-bottom: 20rpx; }
.empty-text { color: #999; font-size: 30rpx; }

/* 模块3: 个人中心 */
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
.logout-btn { margin-top: 60rpx; background: #fff; color: #ff4d4f; border-radius: 45rpx; height: 90rpx; line-height: 90rpx; font-size: 32rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05); }
</style>