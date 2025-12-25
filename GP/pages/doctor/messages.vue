<template>
  <view class="container">
    <view class="header-title">消息通知</view>
    
    <scroll-view scroll-y class="msg-list">
      <view class="msg-card" v-for="(item, index) in list" :key="index">
        <view class="msg-header">
          <text class="title">{{ item.title }}</text>
          <text class="time">{{ formatDate(item.time) }}</text>
        </view>
        <view class="msg-body">
          <text class="content">{{ item.content }}</text>
        </view>
        <!-- 仅请假记录显示状态标签 -->
        <view class="msg-footer" v-if="item.type === 'leave'">
          <text class="status" :class="item.status">{{ getStatusText(item.status) }}</text>
        </view>
      </view>
      
      <view v-if="list.length === 0" class="empty">暂无消息</view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      list: [],
      userInfo: {}
    };
  },
  onLoad() {
    this.userInfo = uni.getStorageSync('userInfo');
    this.fetchMessages();
    this.markRead(); // 进入页面即标记已读
  },
  methods: {
    fetchMessages() {
      uni.showLoading({ title: '加载中' });
      uni.request({
        url: 'http://localhost:8080/api/doctor/messages',
        data: { doctorId: this.userInfo.id },
        success: (res) => {
          uni.hideLoading();
          if (res.data.code === 200) {
            this.list = res.data.data;
          }
        }
      });
    },
    markRead() {
      uni.request({
        url: 'http://localhost:8080/api/doctor/read',
        method: 'POST',
        data: { doctorId: this.userInfo.id },
        success: () => {
          // 可以在这里触发全局事件通知主页更新红点，
          // 但由于返回主页会触发 onShow 刷新，所以这里不需额外操作
        }
      });
    },
    getStatusText(status) {
      const map = { 'pending': '⏳ 审核中', 'approved': '✅ 已通过', 'rejected': '❌ 已拒绝' };
      return map[status] || status;
    },
    formatDate(str) {
      if(!str) return '';
      return str.replace('T', ' ').substring(0, 16);
    }
  }
};
</script>

<style>
.container { min-height: 100vh; background-color: #f5f7fa; padding: 20rpx; }
.header-title { font-size: 36rpx; font-weight: bold; margin-bottom: 30rpx; padding-left: 10rpx; text-align: center; margin-top: 20rpx;}
.msg-card { background: #fff; border-radius: 16rpx; padding: 30rpx; margin-bottom: 20rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03); }
.msg-header { display: flex; justify-content: space-between; margin-bottom: 20rpx; border-bottom: 1rpx solid #f5f5f5; padding-bottom: 15rpx; }
.title { font-size: 30rpx; font-weight: bold; color: #333; }
.time { font-size: 24rpx; color: #999; }
.msg-body { font-size: 28rpx; color: #666; margin-bottom: 15rpx; line-height: 1.5; }
.status { font-weight: bold; font-size: 26rpx; }
.status.pending { color: #ff9800; }
.status.approved { color: #52c41a; }
.status.rejected { color: #ff4d4f; }
.msg-footer { display: flex; justify-content: flex-end; }
.empty { text-align: center; color: #999; margin-top: 100rpx; }
</style>