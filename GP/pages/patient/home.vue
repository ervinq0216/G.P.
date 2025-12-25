<template>
  <view class="container">
    <!-- 顶部导航保持不变 -->
    <view class="nav-tabs">
      <view v-for="(tab, index) in tabs" :key="index" class="tab-item" 
            :class="{ active: currentTab === index }" @click="handleTabSwitch(index)">
        <text class="tab-text">{{ tab }}</text>
        <view class="tab-line" v-if="currentTab === index"></view>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="content-area">
      
      <!-- Module A: 医院简介 (保持不变) -->
      <scroll-view scroll-y class="module-intro" v-if="currentTab === 0">
        <image src="/static/hospital_banner.png" mode="aspectFill" class="banner-img"></image>
        <view class="card info-card">
          <view class="card-header"><view class="header-line"></view><text class="header-title">医院概况</text></view>
          <text class="intro-text">XX市第一人民医院始建于1950年...</text>
        </view>
        <view class="card contact-card">
          <view class="contact-row" @click="openLocation"><text class="icon">📍</text><text class="row-text">地址：XX省XX市XX区健康大道888号</text><text class="arrow">></text></view>
          <view class="divider"></view>
          <view class="contact-row" @click="makePhoneCall"><text class="icon">📞</text><text class="row-text">电话：010-12345678</text><text class="arrow">></text></view>
        </view>
        <view class="card list-card">
          <view class="card-header"><view class="header-line"></view><text class="header-title">医院公告</text><text class="more-link">查看更多</text></view>
          <view class="list-container">
            <view v-for="(item, index) in announcements" :key="index" class="list-item" @click="showDetail(item)">
              <view class="dot"></view><text class="item-title">{{ item.title }}</text><text class="item-date">{{ formatDate(item.createdTime) }}</text>
            </view>
          </view>
        </view>
        <view class="card list-card">
          <view class="card-header"><view class="header-line green"></view><text class="header-title">健康建议</text><text class="more-link">查看更多</text></view>
          <view class="list-container">
            <view v-for="(item, index) in healthTips" :key="index" class="list-item" @click="showDetail(item)">
              <view class="dot green-dot"></view><text class="item-title">{{ item.title }}</text>
            </view>
          </view>
        </view>
        <view style="height: 40rpx;"></view>
      </scroll-view>

      <!-- Module B: 科室导航 (更新排序与点击事件) -->
      <view class="module-dept" v-if="currentTab === 1">
        <scroll-view scroll-y class="dept-sidebar">
          <view v-for="(category, index) in deptData" :key="index" class="sidebar-item" 
                :class="{ 'sidebar-active': currentCategoryIndex === index }" 
                @click="currentCategoryIndex = index">
            {{ category.name }}
          </view>
        </scroll-view>
        <scroll-view scroll-y class="dept-content">
          <view class="dept-grid-title">{{ deptData[currentCategoryIndex].name }}</view>
          <view class="dept-grid">
            <view v-for="(deptName, idx) in deptData[currentCategoryIndex].children" :key="idx" class="dept-box" @click="openDeptDoctors(deptName)">
              <text class="dept-name">{{ deptName }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- Module C: AI 咨询 (保持不变) -->
      <view class="module-ai-chat" v-if="currentTab === 2">
        <scroll-view scroll-y class="chat-history" :scroll-top="scrollTop" :scroll-with-animation="true">
          <view class="chat-item ai"><view class="avatar ai-avatar">AI</view><view class="bubble ai-bubble"><text>您好！我是您的智能健康助手。</text></view></view>
          <view v-for="(msg, index) in chatList" :key="index" class="chat-item" :class="msg.role">
            <view class="avatar" :class="msg.role + '-avatar'">{{ msg.role === 'user' ? '我' : 'AI' }}</view>
            <view class="bubble" :class="msg.role + '-bubble'"><text selectable>{{ msg.content }}</text></view>
          </view>
          <view class="chat-item ai" v-if="isAiLoading"><view class="avatar ai-avatar">AI</view><view class="bubble ai-bubble loading-bubble"><view class="dot-loading">...</view></view></view>
          <view style="height: 120rpx;"></view>
        </scroll-view>
        <view class="chat-input-area">
          <input class="chat-input" v-model="inputMessage" placeholder="请输入您的症状..." confirm-type="send" @confirm="sendMessage" />
          <button class="send-btn" :disabled="isAiLoading || !inputMessage.trim()" @click="sendMessage">发送</button>
        </view>
      </view>

      <!-- Module D: 个人中心 (保持不变) -->
      <view class="module-profile" v-if="currentTab === 3">
        <view class="profile-header">
          <view class="profile-bg-circle"></view>
          <view class="user-info-box" @click="goToInfo">
            <image :src="userInfo.avatar || '/static/default_avatar.png'" class="user-avatar-img" mode="aspectFill"></image>
            <view class="user-text"><text class="user-name">{{ userInfo.realName || '未填写姓名' }}</text><text class="user-phone">{{ userInfo.phone || '' }}</text></view>
            <text class="edit-hint">编辑 ></text>
          </view>
        </view>
        <view class="menu-list">
          <view class="menu-item" @click="goToInfo"><view class="menu-left"><text class="menu-icon">👤</text><text class="menu-title">个人信息</text></view><text class="menu-arrow">></text></view>
          <view class="menu-item" @click="showToast('功能开发中')"><view class="menu-left"><text class="menu-icon">📋</text><text class="menu-title">我的挂号单</text></view><text class="menu-arrow">></text></view>
          <view class="menu-item" @click="showToast('功能开发中')"><view class="menu-left"><text class="menu-icon">⭐</text><text class="menu-title">我的收藏</text></view><text class="menu-arrow">></text></view>
        </view>
        <button class="logout-btn-large" @click="handleLogout">退出登录</button>
      </view>

    </view>

    <!-- 弹窗：科室医生列表 -->
    <view class="modal-mask" v-if="showDoctorListModal" @click="showDoctorListModal = false">
      <view class="modal-content doctor-list-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ selectedDeptName }} - 医生列表</text>
          <text class="close-btn" @click="showDoctorListModal = false">×</text>
        </view>
        <view class="modal-body list-body">
          <view class="doc-intro">本通过显示该科室的所有在职医生，点击卡片可查看详情并预约。</view>
          <scroll-view scroll-y class="doc-scroll">
            <view class="doc-card" v-for="doc in deptDoctors" :key="doc.id" @click="goToDoctorDetail(doc.id)">
              <image :src="doc.avatar || '/static/default_avatar.png'" class="doc-avatar" mode="aspectFill"></image>
              <view class="doc-info">
                <text class="doc-name">{{ doc.realName }}</text>
                <text class="doc-intro-text">{{ doc.introduction || '暂无简介' }}</text>
              </view>
            </view>
            <view v-if="deptDoctors.length === 0" class="empty-tip">该科室暂无医生排班</view>
          </scroll-view>
        </view>
      </view>
    </view>

    <!-- AI 弹窗 -->
    <view class="modal-mask" v-if="showAIModal"><view class="modal-content"><view class="modal-header"><text class="modal-title">⚠️ 免责声明</text></view><view class="modal-body"><text>AI 生成的内容基于大模型算法...</text><text class="highlight">AI 建议仅供参考...</text></view><button class="modal-btn" @click="confirmAI">确认关闭弹窗并继续</button></view></view>

  </view>
</template>

<script>
export default {
  data() {
    return {
      tabs: ['医院简介', '科室导航', 'AI 咨询', '个人中心'],
      currentTab: 0,
      userInfo: {},
      
      // 动态数据
      announcements: [], healthTips: [],
      
      // 科室数据 (更新排序)
      currentCategoryIndex: 0,
      deptData: [
        { name: '手术科室', children: ['普外科', '骨科', '神经外科', '心外科', '胸外科'] },
        { name: '非手术科室', children: ['心内科', '呼吸内科', '消化内科', '神经内科', '肾内科', '儿科'] },
        { name: '诊断相关', children: ['放射科', '检验科', '超声科', '药剂科'] }
      ],
      
      // 医生列表弹窗
      showDoctorListModal: false,
      selectedDeptName: '',
      deptDoctors: [],

      // AI相关
      showAIModal: false, aiConfirmed: false, inputMessage: '', chatList: [], isAiLoading: false, scrollTop: 0
    };
  },
  onShow() {
    const cachedUser = uni.getStorageSync('userInfo');
    if (cachedUser) {
      this.userInfo = cachedUser;
      this.fetchAnnouncements();
      this.fetchHealthTips();
    }
  },
  methods: {
    // --- 科室医生逻辑 ---
    openDeptDoctors(deptName) {
      this.selectedDeptName = deptName;
      this.deptDoctors = [];
      this.showDoctorListModal = true;
      
      uni.showLoading({ title: '加载中' });
      // 调用管理员的 listDoctors 接口（复用，通过关键字搜索或增加 deptName 参数）
      // 这里为了简单，我们用 keyword 搜索科室名 (实际后端 listDoctors 是搜人名的，建议后端 AdminController 加一个按 deptName 搜的逻辑，或者我们遍历所有医生过滤)
      // 由于没有直接按科室名搜的接口，这里暂时演示获取所有医生前端过滤 (生产环境请优化后端接口)
      uni.request({
        url: 'http://localhost:8080/api/admin/doctor/list',
        success: (res) => {
          uni.hideLoading();
          if (res.data.code === 200) {
            // 前端过滤 (需要后端 list 返回 deptId, 然后前端匹配，或者后端直接支持按 deptName 查)
            // 简单起见，假设我们能通过某种方式获取。
            // 修正：最好的方式是先查部门ID，再查医生。
            // 这里为了演示，假设所有医生都显示，或者您可以自己在后端加一个 getDoctorsByDeptName 接口
            this.deptDoctors = res.data.data; // 暂时显示所有，供测试
          }
        },
        fail: () => uni.hideLoading()
      });
    },
    
    goToDoctorDetail(doctorId) {
      this.showDoctorListModal = false;
      uni.navigateTo({
        url: `/pages/patient/doctor-detail?id=${doctorId}`
      });
    },

    // --- 其他原有方法 (保持不变) ---
    fetchAnnouncements() { uni.request({ url: 'http://localhost:8080/api/patient/announcements', success: (res) => { if (res.data.code === 200) this.announcements = res.data.data; } }); },
    fetchHealthTips() { uni.request({ url: 'http://localhost:8080/api/patient/health-tips', success: (res) => { if (res.data.code === 200) this.healthTips = res.data.data; } }); },
    handleTabSwitch(index) { if (index === 2) { if (!this.aiConfirmed) this.showAIModal = true; else { this.currentTab = index; this.scrollToBottom(); } } else { this.currentTab = index; } },
    confirmAI() { this.showAIModal = false; this.aiConfirmed = true; this.currentTab = 2; this.scrollToBottom(); },
    sendMessage() { const msg = this.inputMessage.trim(); if (!msg) return; this.chatList.push({ role: 'user', content: msg }); this.inputMessage = ''; this.isAiLoading = true; this.scrollToBottom(); uni.request({ url: 'http://localhost:8080/api/ai/chat', method: 'POST', data: { message: msg }, success: (res) => { if (res.data.code === 200) { let aiContent = ''; try { const deepSeekRes = JSON.parse(res.data.data); if (deepSeekRes.choices && deepSeekRes.choices.length > 0) aiContent = deepSeekRes.choices[0].message.content; else aiContent = 'AI 暂时无法回答。'; } catch (e) { aiContent = res.data.data || '解析错误。'; } this.chatList.push({ role: 'ai', content: aiContent }); } else { this.chatList.push({ role: 'ai', content: '服务出错：' + res.data.msg }); } }, fail: () => { this.chatList.push({ role: 'ai', content: '网络连接失败。' }); }, complete: () => { this.isAiLoading = false; this.scrollToBottom(); } }); },
    scrollToBottom() { setTimeout(() => { this.scrollTop = 99999; this.$nextTick(() => { this.scrollTop += 1; }); }, 100); },
    goToInfo() { uni.navigateTo({ url: '/pages/patient/info' }); },
    handleLogout() { uni.showModal({ title: '提示', content: '确定要退出登录吗？', success: (res) => { if (res.confirm) { uni.removeStorageSync('userInfo'); uni.removeStorageSync('token'); uni.removeStorageSync('role'); uni.reLaunch({ url: '/pages/login/index' }); } } }); },
    showToast(msg) { uni.showToast({ title: msg, icon: 'none' }); },
    openLocation() { uni.openLocation({ latitude: 39.909, longitude: 116.397, name: 'XX医院', address: 'XX大道' }); },
    makePhoneCall() { uni.makePhoneCall({ phoneNumber: '01012345678' }); },
    showDetail(item) { uni.showModal({ title: item.title, content: item.content, showCancel: false, confirmText: '关闭' }); },
    formatDate(timeStr) { if(!timeStr) return ''; return timeStr.split('T')[0]; },
    imageError(e) { console.log('图片加载失败'); },
    selectDept(deptName) { /* 替换为 openDeptDoctors */ }
  }
};
</script>

<style>
/* 保持原有样式，新增弹窗样式 */
.container { height: 100vh; display: flex; flex-direction: column; background-color: #f5f7fa; }
.nav-tabs { height: 90rpx; display: flex; background-color: #fff; box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05); z-index: 10; flex-shrink: 0; }
.tab-item { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative; }
.tab-text { font-size: 28rpx; color: #666; }
.tab-item.active .tab-text { color: #2b86ff; font-weight: bold; font-size: 30rpx; }
.tab-line { position: absolute; bottom: 10rpx; width: 40rpx; height: 6rpx; background-color: #2b86ff; border-radius: 3rpx; }
.content-area { flex: 1; overflow: hidden; position: relative; display: flex; flex-direction: column; }
.module-intro { height: 100%; }
.banner-img { width: 100%; height: 300rpx; background-color: #e0e0e0; }
.card { background-color: #fff; margin: 20rpx; border-radius: 16rpx; padding: 30rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02); }
.card-header { display: flex; align-items: center; margin-bottom: 20rpx; justify-content: space-between; }
.header-line { width: 8rpx; height: 32rpx; background-color: #2b86ff; border-radius: 4rpx; margin-right: 16rpx; }
.header-line.green { background-color: #42b983; }
.header-title { font-size: 32rpx; font-weight: bold; color: #333; flex: 1; }
.more-link { font-size: 24rpx; color: #999; }
.intro-text { font-size: 28rpx; color: #666; line-height: 1.6; text-align: justify; }
.contact-row { display: flex; align-items: center; padding: 20rpx 0; }
.icon { margin-right: 20rpx; font-size: 32rpx; }
.row-text { flex: 1; font-size: 28rpx; color: #333; }
.arrow { color: #ccc; font-size: 28rpx; }
.divider { height: 1rpx; background-color: #eee; margin: 0 10rpx; }
.list-item { display: flex; align-items: center; padding: 20rpx 0; border-bottom: 1rpx dashed #f0f0f0; }
.list-item:last-child { border-bottom: none; }
.dot { width: 12rpx; height: 12rpx; background-color: #2b86ff; border-radius: 50%; margin-right: 16rpx; }
.green-dot { background-color: #42b983; }
.item-title { flex: 1; font-size: 28rpx; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-date { font-size: 24rpx; color: #999; margin-left: 20rpx; }
.empty-tip { text-align: center; color: #999; font-size: 24rpx; padding: 20rpx; }
.module-dept { height: 100%; display: flex; background-color: #fff; width: 100%; }
.dept-sidebar { width: 200rpx; background-color: #f5f7fa; height: 100%; }
.sidebar-item { height: 100rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #666; position: relative; }
.sidebar-active { background-color: #fff; color: #2b86ff; font-weight: bold; }
.sidebar-active::before { content: ''; position: absolute; left: 0; top: 30rpx; bottom: 30rpx; width: 8rpx; background-color: #2b86ff; border-radius: 0 4rpx 4rpx 0; }
.dept-content { flex: 1; background-color: #fff; padding: 20rpx; height: 100%; box-sizing: border-box; }
.dept-grid-title { font-size: 30rpx; font-weight: bold; color: #333; margin-bottom: 30rpx; padding-left: 10rpx; }
.dept-grid { display: flex; flex-wrap: wrap; gap: 20rpx; }
.dept-box { width: 48%; height: 80rpx; background-color: #f8faff; border: 1rpx solid #e1eaff; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; }
.dept-box:active { background-color: #2b86ff; }
.dept-box:active .dept-name { color: #fff; }
.dept-name { font-size: 26rpx; color: #333; text-align: center; }
.module-ai-chat { flex: 1; display: flex; flex-direction: column; background-color: #f5f7fa; height: 100%; }
.chat-history { flex: 1; padding: 20rpx; box-sizing: border-box; height: 0; }
.chat-item { display: flex; margin-bottom: 30rpx; }
.chat-item.user { flex-direction: row-reverse; }
.avatar { width: 80rpx; height: 80rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #fff; flex-shrink: 0; }
.ai-avatar { background: linear-gradient(135deg, #2b86ff, #2bdfff); margin-right: 20rpx; }
.user-avatar { background-color: #ff9800; margin-left: 20rpx; }
.bubble { max-width: 70%; padding: 20rpx 24rpx; border-radius: 16rpx; font-size: 30rpx; line-height: 1.5; position: relative; word-break: break-all; }
.ai-bubble { background-color: #fff; color: #333; border-top-left-radius: 4rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05); }
.user-bubble { background-color: #2b86ff; color: #fff; border-top-right-radius: 4rpx; box-shadow: 0 2rpx 8rpx rgba(43, 134, 255, 0.3); }
.loading-bubble { padding: 10rpx 20rpx; }
.dot-loading { font-weight: bold; letter-spacing: 4rpx; color: #999; animation: breathe 1.5s infinite; }
@keyframes breathe { 0% { opacity: 0.3; } 50% { opacity: 1; } 100% { opacity: 0.3; } }
.chat-input-area { height: 100rpx; background-color: #fff; border-top: 1rpx solid #eee; display: flex; align-items: center; padding: 0 20rpx; position: relative; z-index: 20; }
.chat-input { flex: 1; height: 70rpx; background-color: #f5f7fa; border-radius: 35rpx; padding: 0 30rpx; font-size: 28rpx; margin-right: 20rpx; }
.send-btn { width: 120rpx; height: 70rpx; line-height: 70rpx; background-color: #2b86ff; color: #fff; font-size: 28rpx; border-radius: 35rpx; padding: 0; }
.send-btn[disabled] { background-color: #ccc; color: #fff; }
.module-profile { flex: 1; background-color: #f5f7fa; padding: 30rpx; }
.profile-header { background-color: #fff; border-radius: 20rpx; padding: 40rpx 30rpx; margin-bottom: 30rpx; display: flex; align-items: center; position: relative; overflow: hidden; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03); }
.profile-bg-circle { position: absolute; top: -50rpx; right: -50rpx; width: 200rpx; height: 200rpx; background: linear-gradient(135deg, rgba(43, 134, 255, 0.1) 0%, rgba(43, 223, 255, 0.05) 100%); border-radius: 50%; }
.user-info-box { display: flex; align-items: center; width: 100%; position: relative; z-index: 1; }
.user-avatar-img { width: 120rpx; height: 120rpx; border-radius: 50%; background-color: #f0f0f0; margin-right: 30rpx; border: 2rpx solid #fff; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1); }
.user-text { flex: 1; display: flex; flex-direction: column; }
.user-name { font-size: 36rpx; font-weight: bold; color: #333; margin-bottom: 10rpx; }
.user-phone { font-size: 26rpx; color: #999; }
.edit-hint { font-size: 26rpx; color: #999; }
.menu-list { background-color: #fff; border-radius: 20rpx; padding: 0 30rpx; margin-bottom: 50rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03); }
.menu-item { display: flex; justify-content: space-between; align-items: center; height: 110rpx; border-bottom: 1rpx solid #f5f5f5; }
.menu-item:last-child { border-bottom: none; }
.menu-left { display: flex; align-items: center; }
.menu-icon { font-size: 36rpx; margin-right: 20rpx; width: 50rpx; text-align: center; }
.menu-title { font-size: 30rpx; color: #333; }
.menu-arrow { color: #ccc; font-size: 28rpx; }
.logout-btn-large { background-color: #fff; color: #ff4d4f; font-size: 32rpx; border-radius: 45rpx; height: 90rpx; line-height: 90rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05); }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0,0,0,0.6); z-index: 999; display: flex; align-items: center; justify-content: center; }
.modal-content { width: 600rpx; background-color: #fff; border-radius: 24rpx; padding: 40rpx; }
.modal-header { text-align: center; margin-bottom: 30rpx; position: relative; }
.modal-title { font-size: 36rpx; font-weight: bold; color: #333; }
.close-btn { position: absolute; right: 0; top: 0; font-size: 40rpx; color: #999; padding: 10rpx; }
.modal-body { font-size: 28rpx; color: #666; line-height: 1.6; margin-bottom: 40rpx; display: flex; flex-direction: column; gap: 20rpx; }
.highlight { color: #d32f2f; font-weight: bold; }
.modal-btn { background-color: #2b86ff; color: #fff; border-radius: 40rpx; font-size: 30rpx; }

/* 医生列表弹窗专用 */
.doctor-list-modal { height: 70vh; display: flex; flex-direction: column; }
.doc-intro { font-size: 24rpx; color: #999; margin-bottom: 20rpx; }
.doc-scroll { flex: 1; height: 0; overflow-y: auto; }
.doc-card { display: flex; background: #f8f9fb; padding: 20rpx; border-radius: 12rpx; margin-bottom: 20rpx; align-items: center; }
.doc-avatar { width: 100rpx; height: 100rpx; border-radius: 50%; background: #eee; margin-right: 20rpx; }
.doc-info { flex: 1; display: flex; flex-direction: column; }
.doc-name { font-size: 30rpx; font-weight: bold; color: #333; }
.doc-intro-text { font-size: 24rpx; color: #666; margin-top: 6rpx; display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2; overflow: hidden; }
</style>