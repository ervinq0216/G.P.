<template>
  <view class="container">
    <view class="content">
      <text class="title">这是医生主页</text>
      <text class="subtitle">功能开发中...</text>
    </view>
    <button class="logout-btn" @click="handleLogout">退出登录</button>
  </view>
</template>

<script>
export default {
  methods: {
    handleLogout() {
      uni.reLaunch({
        url: '/pages/login/index'
      });
    }
  }
};
</script>

<style>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f8faff;
}
.content {
  margin-bottom: 50rpx;
  text-align: center;
}
.title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
}
.subtitle {
  font-size: 28rpx;
  color: #999;
  margin-top: 20rpx;
  display: block;
}
.logout-btn {
  width: 200rpx;
  font-size: 28rpx;
  background-color: #fff;
  color: #666;
  border: 1px solid #ddd;
}
</style>