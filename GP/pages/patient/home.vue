<template>
  <view class="container">
    
    <!-- 1. 顶部自定义导航栏 -->
    <view class="nav-tabs">
      <view 
        v-for="(tab, index) in tabs" 
        :key="index" 
        class="tab-item"
        :class="{ active: currentTab === index }"
        @click="handleTabSwitch(index)"
      >
        <text class="tab-text">{{ tab }}</text>
        <view class="tab-line" v-if="currentTab === index"></view>
      </view>
    </view>

    <!-- 内容区域：根据 currentTab 显示不同模块 -->
    <view class="content-area">

      <!-- Module A: 医院简介 -->
      <scroll-view scroll-y class="module-intro" v-if="currentTab === 0">
        <!-- 医院形象图 -->
        <image src="/static/hospital_banner.png" mode="aspectFill" class="banner-img" @error="imageError"></image>
        
        <!-- 发展简述 -->
        <view class="card info-card">
          <view class="card-header">
            <view class="header-line"></view>
            <text class="header-title">医院概况</text>
          </view>
          <text class="intro-text">
            XX市第一人民医院始建于1950年，是一所集医疗、教学、科研、预防保健为一体的三级甲等综合医院。医院始终坚持“以患者为中心”的服务理念，拥有先进的医疗设备和资深的专家团队，致力于为广大市民提供优质、高效的医疗健康服务。
          </text>
        </view>

        <!-- 联系方式 -->
        <view class="card contact-card">
          <view class="contact-row" @click="openLocation">
            <text class="icon">📍</text>
            <text class="row-text">地址：XX省XX市XX区健康大道888号</text>
            <text class="arrow">></text>
          </view>
          <view class="divider"></view>
          <view class="contact-row" @click="makePhoneCall">
            <text class="icon">📞</text>
            <text class="row-text">电话：010-12345678</text>
            <text class="arrow">></text>
          </view>
        </view>

        <!-- 医院公告 -->
        <view class="card list-card">
          <view class="card-header">
            <view class="header-line"></view>
            <text class="header-title">医院公告</text>
            <text class="more-link">查看更多</text>
          </view>
          <view class="list-container">
            <view 
              v-for="(item, index) in announcements" 
              :key="index" 
              class="list-item"
              @click="goToDetail('announcement', item.id)"
            >
              <view class="dot"></view>
              <text class="item-title">{{ item.title }}</text>
              <text class="item-date">{{ item.date }}</text>
            </view>
          </view>
        </view>

        <!-- 健康建议 -->
        <view class="card list-card">
          <view class="card-header">
            <view class="header-line green"></view>
            <text class="header-title">健康建议</text>
            <text class="more-link">查看更多</text>
          </view>
          <view class="list-container">
            <view 
              v-for="(item, index) in healthTips" 
              :key="index" 
              class="list-item"
              @click="goToDetail('health', item.id)"
            >
              <view class="dot green-dot"></view>
              <text class="item-title">{{ item.title }}</text>
            </view>
          </view>
        </view>
        
        <!-- 底部占位，防止被Tabbar遮挡 -->
        <view style="height: 40rpx;"></view>
      </scroll-view>

      <!-- Module B: 科室导航 (左右联动布局) -->
      <view class="module-dept" v-if="currentTab === 1">
        <!-- 左侧：一级分类 -->
        <scroll-view scroll-y class="dept-sidebar">
          <view 
            v-for="(category, index) in deptData" 
            :key="index"
            class="sidebar-item"
            :class="{ 'sidebar-active': currentCategoryIndex === index }"
            @click="currentCategoryIndex = index"
          >
            {{ category.name }}
          </view>
        </scroll-view>

        <!-- 右侧：二级网格 -->
        <scroll-view scroll-y class="dept-content">
          <view class="dept-grid-title">{{ deptData[currentCategoryIndex].name }}</view>
          <view class="dept-grid">
            <view 
              v-for="(dept, idx) in deptData[currentCategoryIndex].children" 
              :key="idx"
              class="dept-box"
              @click="selectDept(dept)"
            >
              <text class="dept-name">{{ dept }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- Module C: AI 咨询 (WebView) -->
      <view class="module-ai" v-if="currentTab === 2">
        <!-- WebView 容器 -->
        <!-- 注意：web-view 在小程序中会自动铺满全屏，但在H5中可以控制 -->
        <web-view 
          v-if="aiConfirmed" 
          src="https://chat.deepseek.com"
          :update-title="false"
        ></web-view>
        
        <!-- 如果未确认，显示占位背景 -->
        <view v-else class="ai-placeholder">
          <text>正在初始化智能助手...</text>
        </view>
      </view>

    </view>

    <!-- AI 风险提示弹窗 -->
    <view class="modal-mask" v-if="showAIModal">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">⚠️ 免责声明</text>
        </view>
        <view class="modal-body">
          <text>AI 生成的内容基于大模型算法，可能存在不准确或过时的信息。</text>
          <text class="highlight">AI 建议仅供参考，不能替代专业医生的诊断和治疗方案。</text>
          <text>如有身体不适，请务必咨询线下医师。</text>
        </view>
        <button class="modal-btn" @click="confirmAI">确认关闭弹窗并继续</button>
      </view>
    </view>

  </view>
</template>

<script>
export default {
  data() {
    return {
      tabs: ['医院简介', '科室导航', 'AI 咨询'],
      currentTab: 0,
      
      // AI 模块状态
      showAIModal: false,
      aiConfirmed: false,

      // 模拟公告数据
      announcements: [
        { id: 1, title: '关于2024年端午节门诊安排的通知', date: '06-01' },
        { id: 2, title: '我院新增专家门诊的公告', date: '05-28' },
        { id: 3, title: '住院部探视制度调整说明', date: '05-20' }
      ],

      // 模拟健康建议数据
      healthTips: [
        { id: 1, title: '春季流感高发，如何科学预防？' },
        { id: 2, title: '高血压患者的饮食注意事项' },
        { id: 3, title: '定期体检的重要性' }
      ],

      // 科室导航数据 (左右联动结构)
      currentCategoryIndex: 0,
      deptData: [
        {
          name: '非手术科室',
          children: ['内科学系', '心内科', '呼吸与危重症', '消化内科', '肾内科', '血液内科', '风湿免疫科', '感染内科', '儿科', '神经科', '皮肤科', '中医科']
        },
        {
          name: '手术科室',
          children: ['外科学系', '基本外科', '骨科', '心外科', '胸外科', '泌尿外科', '神经外科', '整形美容外科', '眼科', '耳鼻喉科', '口腔科', '妇产科']
        },
        {
          name: '诊断相关',
          children: ['超声医学科', '病理科', '检验科', '放射科', '药剂科', '核医学科', '输血科']
        }
      ]
    };
  },
  methods: {
    // 图片加载失败处理
    imageError(e) {
      console.log('图片加载失败，实际开发请替换 static/hospital_banner.png');
    },

    // 切换 Tab
    handleTabSwitch(index) {
      // 如果点击的是 AI 咨询 (index 2)
      if (index === 2) {
        if (!this.aiConfirmed) {
          // 没确认过，显示弹窗
          this.showAIModal = true;
          // 暂时不切换 tab，等确认后再切换，或者先切换背景
          this.currentTab = index; 
        } else {
          // 已确认过，直接进
          this.currentTab = index;
        }
      } else {
        // 其他 Tab 正常切换
        this.currentTab = index;
      }
    },

    // 确认 AI 提示
    confirmAI() {
      this.showAIModal = false;
      this.aiConfirmed = true;
      // 强制刷新 WebView 状态
      this.$forceUpdate();
    },

    // 模拟功能
    openLocation() {
      uni.openLocation({
        latitude: 39.909,
        longitude: 116.397,
        name: 'XX市第一人民医院',
        address: 'XX省XX市XX区健康大道888号'
      });
    },
    makePhoneCall() {
      uni.makePhoneCall({ phoneNumber: '01012345678' });
    },
    goToDetail(type, id) {
      uni.showToast({ title: `查看${type}详情 ID:${id}`, icon: 'none' });
      // uni.navigateTo({ url: `/pages/article/detail?id=${id}` });
    },
    selectDept(deptName) {
      uni.showToast({ title: `选择了: ${deptName}`, icon: 'none' });
      // 可以在这里跳转到挂号页面，并携带科室参数
      // uni.navigateTo({ url: `/pages/registration/select-doctor?dept=${deptName}` });
    }
  }
};
</script>

<style>
/* 基础容器 */
.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

/* --- 1. 顶部导航栏 --- */
.nav-tabs {
  height: 90rpx;
  display: flex;
  background-color: #fff;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
  z-index: 10;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.tab-text {
  font-size: 30rpx;
  color: #666;
}

.tab-item.active .tab-text {
  color: #2b86ff;
  font-weight: bold;
  font-size: 32rpx;
}

.tab-line {
  position: absolute;
  bottom: 10rpx;
  width: 40rpx;
  height: 6rpx;
  background-color: #2b86ff;
  border-radius: 3rpx;
}

/* --- 内容区域 --- */
.content-area {
  flex: 1;
  overflow: hidden; /* 防止内容溢出 */
  position: relative;
}

/* --- Module A: 医院简介 --- */
.module-intro {
  height: 100%;
}

.banner-img {
  width: 100%;
  height: 300rpx;
  background-color: #e0e0e0; /* 占位色 */
}

.card {
  background-color: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  justify-content: space-between;
}

.header-line {
  width: 8rpx;
  height: 32rpx;
  background-color: #2b86ff;
  border-radius: 4rpx;
  margin-right: 16rpx;
}

.header-line.green { background-color: #42b983; }

.header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  flex: 1;
}

.more-link {
  font-size: 24rpx;
  color: #999;
}

.intro-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  text-align: justify;
}

/* 联系卡片 */
.contact-row {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
}

.contact-row:active {
  background-color: #fafafa;
}

.icon { margin-right: 20rpx; font-size: 32rpx; }
.row-text { flex: 1; font-size: 28rpx; color: #333; }
.arrow { color: #ccc; font-size: 28rpx; }
.divider { height: 1rpx; background-color: #eee; margin: 0 10rpx; }

/* 列表样式 */
.list-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx dashed #f0f0f0;
}

.list-item:last-child { border-bottom: none; }

.dot {
  width: 12rpx;
  height: 12rpx;
  background-color: #2b86ff;
  border-radius: 50%;
  margin-right: 16rpx;
}

.green-dot { background-color: #42b983; }

.item-title {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-date {
  font-size: 24rpx;
  color: #999;
  margin-left: 20rpx;
}

/* --- Module B: 科室导航 --- */
.module-dept {
  height: 100%;
  display: flex;
  background-color: #fff;
}

/* 左侧边栏 */
.dept-sidebar {
  width: 200rpx;
  background-color: #f5f7fa;
  height: 100%;
}

.sidebar-item {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #666;
  position: relative;
}

.sidebar-active {
  background-color: #fff;
  color: #2b86ff;
  font-weight: bold;
}

.sidebar-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 30rpx;
  bottom: 30rpx;
  width: 8rpx;
  background-color: #2b86ff;
  border-radius: 0 4rpx 4rpx 0;
}

/* 右侧内容 */
.dept-content {
  flex: 1;
  background-color: #fff;
  padding: 20rpx;
  height: 100%;
  box-sizing: border-box;
}

.dept-grid-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
  padding-left: 10rpx;
}

.dept-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.dept-box {
  width: 48%; /* 两列布局 */
  height: 80rpx;
  background-color: #f8faff;
  border: 1rpx solid #e1eaff;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dept-box:active {
  background-color: #2b86ff;
}

.dept-box:active .dept-name {
  color: #fff;
}

.dept-name {
  font-size: 26rpx;
  color: #333;
  text-align: center;
}

/* --- Module C: AI 咨询 --- */
.module-ai {
  height: 100%;
  width: 100%;
}

.ai-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 28rpx;
}

/* --- 弹窗样式 --- */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.6);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  width: 600rpx;
  background-color: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.modal-header {
  text-align: center;
  margin-bottom: 30rpx;
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff9800; /* 警告色 */
}

.modal-body {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.highlight {
  color: #d32f2f;
  font-weight: bold;
}

.modal-btn {
  background-color: #2b86ff;
  color: #fff;
  border-radius: 40rpx;
  font-size: 30rpx;
}
</style>