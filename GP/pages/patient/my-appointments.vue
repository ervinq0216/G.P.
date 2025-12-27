<template>
  <view class="container">
    <view class="header-title">我的挂号记录</view>

    <scroll-view scroll-y class="list-scroll">
      <view class="app-card" v-for="(item, index) in list" :key="index" @click="openDetail(item)">
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
          <text class="view-btn" v-if="item.status === 'completed'">
              {{ item.comment ? '已评价' : '去评价' }}
          </text>
        </view>
      </view>

      <view v-if="list.length === 0" class="empty-box">
        <image src="/static/logo.png" class="empty-img" mode="aspectFit" style="opacity: 0.2;"></image>
        <text class="empty-text">您还没有挂号记录</text>
      </view>
    </scroll-view>

    <!-- 评价弹窗 -->
    <view class="modal-mask" v-if="showModal" @click="showModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">就诊详情</view>
        
        <!-- 上部分：病例信息 -->
        <view class="record-box">
          <view class="box-title">📋 医生诊断/病例</view>
          <view class="box-content">
            <text>{{ currentItem.medicalRecord || '医生暂未填写详细病例' }}</text>
          </view>
        </view>

        <!-- 下部分：患者评价 -->
        <view class="comment-box">
          <view class="box-title">✍️ 就医评价</view>
          
          <!-- 如果已经评价过，显示只读信息 -->
          <view v-if="currentItem.comment || currentItem.rating" class="read-only-comment">
            <view class="rating-display">
                <text class="rating-label">评分：</text>
                <text v-for="n in 5" :key="n" class="star-icon" :class="{ active: n <= (currentItem.rating || 0) }">★</text>
            </view>
            <text class="comment-text">评价：{{ currentItem.comment || '无' }}</text>
            <text class="tag">已完成</text>
          </view>

          <!-- 如果未评价，显示输入表单 -->
          <view v-else>
            <!-- 评分选择 -->
            <view class="rating-input">
                <text class="rating-label">请评分：</text>
                <view class="stars-row">
                    <text v-for="n in 5" :key="n" 
                          class="star-btn" 
                          :class="{ active: n <= tempRating }" 
                          @click="tempRating = n">★</text>
                </view>
            </view>

            <textarea class="comment-input" v-model="commentText" placeholder="请输入您对本次就医的评价（可选）..." />
            
            <view class="modal-btns">
              <button class="cancel-btn" @click="showModal = false">取消</button>
              <button class="confirm-btn" @click="submitComment">提交评价</button>
            </view>
          </view>
        </view>

        <!-- 关闭按钮 -->
        <button class="close-btn-full" v-if="currentItem.comment || currentItem.rating" @click="showModal = false">关闭</button>
      </view>
    </view>

  </view>
</template>

<script>
export default {
  data() {
    return {
      list: [],
      userInfo: {},
      showModal: false,
      currentItem: {},
      commentText: '',
      tempRating: 5 // 默认5星
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
    openDetail(item) {
      if (item.status === 'completed') {
        this.currentItem = item;
        // 如果未评价，重置表单状态
        if (!item.comment && !item.rating) {
            this.commentText = '';
            this.tempRating = 5;
        }
        this.showModal = true;
      } else {
        uni.showToast({ title: '未就诊订单无法查看详情', icon: 'none' });
      }
    },
    submitComment() {
      uni.showLoading({ title: '提交中' });
      uni.request({
        url: 'http://localhost:8080/api/appointment/comment',
        method: 'POST',
        data: {
          id: this.currentItem.id,
          comment: this.commentText,
          rating: this.tempRating // 提交评分
        },
        success: (res) => {
          uni.hideLoading();
          if (res.data.code === 200) {
            uni.showToast({ title: '评价成功' });
            this.showModal = false;
            this.fetchData(); // 刷新列表状态
          } else {
            uni.showToast({ title: res.data.msg, icon: 'none' });
          }
        }
      });
    },
    getStatusText(status) {
      const map = { 'booked': '待就诊', 'completed': '已完成', 'cancelled': '已取消' };
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
.view-btn { color: #fff; background-color: #2b86ff; padding: 8rpx 24rpx; border-radius: 30rpx; font-size: 24rpx; }

.empty-box { display: flex; flex-direction: column; align-items: center; margin-top: 200rpx; }
.empty-img { width: 160rpx; height: 160rpx; margin-bottom: 30rpx; }
.empty-text { color: #999; font-size: 28rpx; }

/* 弹窗样式 */
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); z-index: 999; display: flex; align-items: center; justify-content: center; }
.modal-content { width: 620rpx; background: #fff; border-radius: 30rpx; padding: 40rpx; max-height: 80vh; display: flex; flex-direction: column; }
.modal-header { text-align: center; font-size: 34rpx; font-weight: bold; margin-bottom: 30rpx; }

.record-box, .comment-box { margin-bottom: 30rpx; }
.box-title { font-size: 30rpx; font-weight: bold; color: #333; margin-bottom: 16rpx; }
.box-content { background: #f9f9f9; padding: 20rpx; border-radius: 12rpx; font-size: 28rpx; color: #666; line-height: 1.6; min-height: 100rpx; }

/* 评分样式 */
.rating-input { display: flex; align-items: center; margin-bottom: 20rpx; }
.rating-label { font-size: 28rpx; color: #333; margin-right: 20rpx; }
.stars-row { display: flex; gap: 10rpx; }
.star-btn { font-size: 50rpx; color: #ddd; transition: color 0.2s; }
.star-btn.active { color: #ffca28; }

.rating-display { margin-bottom: 10rpx; }
.star-icon { font-size: 36rpx; color: #ddd; margin-right: 4rpx; }
.star-icon.active { color: #ffca28; }

.comment-input { width: 100%; height: 200rpx; background: #f9f9f9; padding: 20rpx; border-radius: 12rpx; font-size: 28rpx; box-sizing: border-box; }
.read-only-comment { background: #e8f5e9; padding: 20rpx; border-radius: 12rpx; font-size: 28rpx; position: relative; }
.comment-text { color: #333; line-height: 1.5; display: block; margin-top: 10rpx; }
.tag { position: absolute; right: 10rpx; top: 10rpx; font-size: 20rpx; border: 1rpx solid #2e7d32; padding: 2rpx 8rpx; border-radius: 6rpx; color: #2e7d32; }

.modal-btns { display: flex; gap: 20rpx; margin-top: 20rpx; }
.cancel-btn, .confirm-btn { flex: 1; height: 80rpx; line-height: 80rpx; font-size: 28rpx; border-radius: 40rpx; }
.cancel-btn { background: #f5f5f5; color: #666; }
.confirm-btn { background: #2b86ff; color: #fff; }
.close-btn-full { width: 100%; background: #f5f5f5; color: #666; margin-top: 20rpx; border-radius: 40rpx; }
</style>