<template>
  <view class="container">
    <view class="header-title">我的挂号记录</view>

    <scroll-view scroll-y class="list-scroll">
      <view class="app-card" v-for="(item, index) in list" :key="index">
        <view class="card-header">
          <text class="dept-name">{{ item.deptName }}</text>
          <text class="status" :class="item.status">{{ getStatusText(item.status) }}</text>
        </view>
        
        <view class="card-body">
          <image :src="item.doctorAvatar || '/static/default_avatar.png'" class="doc-avatar" mode="aspectFill"></image>
          <view class="info">
            <text class="doc-name">{{ item.doctorName }} <text class="title">医生</text></text>
            <text class="time-info">就诊时间：{{ item.workDate }} {{ item.period }}</text>
          </view>
        </view>
        
        <view class="card-footer">
          <text class="create-time">下单时间：{{ formatTime(item.createTime) }}</text>
          <!-- 预留按钮位置，例如取消预约 -->
          <!-- <button class="action-btn" v-if="item.status === 'booked'">取消</button> -->
        </view>
      </view>

      <view v-if="list.length === 0" class="empty-box">
        <image src="/static/logo.png" class="empty-img" mode="aspectFit" style="opacity: 0.2;"></image>
        <text class="empty-text">您还没有挂号记录</text>
      </view>
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
    this.fetchData();
  },
  methods: {
    fetchData() {
      uni.showLoading({ title: '加载中' });
      uni.request({
        url: 'http://localhost:8080/api/appointment/patient-list',
        data: { patientId: this.userInfo.id },
        success: (res) => {
          uni.hideLoading();
          if (res.data.code === 200) {
            this.list = res.data.data;
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    },
    getStatusText(status) {
      const map = {
        'booked': '预约成功',
        'completed': '已完成',
        'cancelled': '已取消'
      };
      return map[status] || status;
    },
    formatTime(str) {
      if (!str) return '';
      return str.replace('T', ' ').substring(0, 16);
    }
  }
};
</script>

<style>
.container { min-height: 100vh; background-color: #f5f7fa; padding: 20rpx; }
.header-title { font-size: 34rpx; font-weight: bold; margin-bottom: 20rpx; padding-left: 10rpx; }
.list-scroll { height: calc(100vh - 100rpx); }

.app-card { background: #fff; border-radius: 20rpx; padding: 30rpx; margin-bottom: 20rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03); }
.card-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1rpx solid #f5f5f5; padding-bottom: 20rpx; margin-bottom: 20rpx; }
.dept-name { font-weight: bold; font-size: 30rpx; color: #333; }
.status { font-size: 26rpx; }
.status.booked { color: #2b86ff; }
.status.completed { color: #52c41a; }
.status.cancelled { color: #999; }

.card-body { display: flex; align-items: center; margin-bottom: 20rpx; }
.doc-avatar { width: 100rpx; height: 100rpx; border-radius: 50%; background: #eee; margin-right: 24rpx; }
.info { display: flex; flex-direction: column; }
.doc-name { font-size: 32rpx; font-weight: bold; color: #333; margin-bottom: 8rpx; }
.title { font-size: 24rpx; color: #666; font-weight: normal; margin-left: 8rpx; }
.time-info { font-size: 26rpx; color: #2b86ff; background: #e6f1ff; padding: 4rpx 12rpx; border-radius: 8rpx; display: inline-block; }

.card-footer { display: flex; justify-content: space-between; align-items: center; font-size: 24rpx; color: #999; }
.action-btn { font-size: 24rpx; background: #fff; border: 1rpx solid #ccc; color: #666; padding: 0 20rpx; height: 50rpx; line-height: 48rpx; border-radius: 25rpx; margin: 0; }

.empty-box { display: flex; flex-direction: column; align-items: center; margin-top: 200rpx; }
.empty-img { width: 160rpx; height: 160rpx; margin-bottom: 30rpx; }
.empty-text { color: #999; font-size: 28rpx; }
</style>