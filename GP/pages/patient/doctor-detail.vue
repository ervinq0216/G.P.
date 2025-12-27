<template>
  <view class="container">
    <!-- 1. 医生信息 -->
    <view class="doctor-header-card">
      <image :src="doctor.avatar || '/static/default_avatar.png'" class="header-avatar" mode="aspectFill"></image>
      <view class="header-right">
        <view class="name-row">
          <text class="doc-name">{{ doctor.realName }}</text>
          <!-- 收藏按钮 -->
          <view class="fav-btn" @click="toggleFavorite">
            <text class="fav-icon" :class="{ active: isFavorited }">{{ isFavorited ? '❤️' : '🤍' }}</text>
            <text class="fav-text">{{ isFavorited ? '已收藏' : '收藏' }}</text>
          </view>
        </view>
        <view class="intro-box">
          <text class="intro-title">医生简介</text>
          <text class="intro-content">{{ doctor.introduction || '暂无详细介绍' }}</text>
        </view>
      </view>
    </view>

    <!-- 2. 预约操作 -->
    <view class="action-section">
      <button class="book-main-btn" @click="showBookPopup = true">点击预约挂号</button>
    </view>

    <!-- 3. 评价列表 -->
    <view class="review-section">
      <view class="section-header">
        <text class="title">患者评价 ({{ reviews.length }})</text>
      </view>
      
      <view class="review-list">
        <view class="review-item" v-for="(item, index) in reviews" :key="index">
          <view class="review-top">
            <view class="user-info">
              <image src="/static/default_avatar.png" class="u-avatar"></image>
              <text class="u-name">{{ item.patientName }}</text>
            </view>
            <view class="rating-stars">
              <text v-for="n in 5" :key="n" class="star" :class="{ active: n <= item.rating }">★</text>
            </view>
          </view>
          <view class="review-content">{{ item.comment }}</view>
          <view class="review-time">{{ formatDate(item.time) }}</view>
        </view>
        
        <view v-if="reviews.length === 0" class="empty-reviews">
          <text>暂无评价</text>
        </view>
      </view>
    </view>

    <!-- 4. 预约弹窗 (保持不变) -->
    <view class="modal-mask" v-if="showBookPopup" @click="showBookPopup = false">
      <view class="modal-content book-popup" @click.stop>
        <view class="modal-header">选择预约时段</view>
        
        <scroll-view scroll-x class="date-scroll">
          <view class="date-item" v-for="(day, index) in dateList" :key="index"
                :class="{ active: selectedDateIndex === index }" @click="selectedDateIndex = index">
            <text class="week">{{ day.week }}</text>
            <text class="date">{{ day.formatDate }}</text>
          </view>
        </scroll-view>

        <view class="period-list" v-if="currentDaySchedules.length > 0">
          <view class="period-box" v-for="s in currentDaySchedules" :key="s.id"
                :class="{ disabled: s.status === 'leave' || s.bookedCount >= s.totalQuota }"
                @click="handleBooking(s)">
            <view class="period-top">
              <text class="p-name">{{ s.period }}</text>
              <text class="p-status" v-if="s.status === 'leave'">医生请假</text>
              <text class="p-status" v-else-if="s.bookedCount >= s.totalQuota">已满</text>
              <text class="p-status available" v-else>有号</text>
            </view>
            <text class="p-quota">剩余：{{ s.totalQuota - s.bookedCount }}</text>
          </view>
        </view>
        <view v-else class="empty-msg">该日期暂无排班</view>

        <view class="close-popup" @click="showBookPopup = false">取消</view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      doctorId: null,
      doctor: {},
      showBookPopup: false,
      scheduleList: [], 
      dateList: [],
      selectedDateIndex: 0,
      userInfo: {},
      reviews: [],
      isFavorited: false // 收藏状态
    };
  },
  computed: {
    currentDaySchedules() {
      if (this.dateList.length === 0) return [];
      const targetDate = this.dateList[this.selectedDateIndex].fullDate;
      return this.scheduleList.filter(s => s.workDate === targetDate);
    }
  },
  onLoad(opt) {
    this.doctorId = opt.id;
    this.userInfo = uni.getStorageSync('userInfo');
    this.fetchDoctor();
    this.fetchSchedules();
    this.fetchReviews();
    this.checkFavorite(); // 检查收藏状态
  },
  methods: {
    fetchDoctor() {
      // 优先调用 doctor 接口获取准确信息
      uni.request({
        url: 'http://localhost:8080/api/doctor/info/' + this.doctorId,
        success: (res) => { 
           if (res.data.code === 200) {
               this.doctor = res.data.data;
           }
        }
      });
    },
    // --- 收藏逻辑 ---
    checkFavorite() {
      uni.request({
        url: 'http://localhost:8080/api/favorite/check',
        data: { patientId: this.userInfo.id, doctorId: this.doctorId },
        success: (res) => {
          if(res.data.code === 200) this.isFavorited = res.data.data;
        }
      });
    },
    toggleFavorite() {
      uni.request({
        url: 'http://localhost:8080/api/favorite/toggle',
        method: 'POST',
        data: { patientId: this.userInfo.id, doctorId: this.doctorId },
        success: (res) => {
          if(res.data.code === 200) {
            this.isFavorited = !this.isFavorited;
            uni.showToast({ title: res.data.data, icon: 'none' });
          }
        }
      });
    },
    // ----------------
    
    fetchSchedules() {
      uni.request({
        url: 'http://localhost:8080/api/schedule/list',
        data: { doctorId: this.doctorId },
        success: (res) => {
          if (res.data.code === 200) {
            this.scheduleList = res.data.data;
            const seen = new Set();
            this.dateList = [];
            const weeks = ['周日','周一','周二','周三','周四','周五','周六'];
            this.scheduleList.forEach(s => {
              if(!seen.has(s.workDate)) {
                seen.add(s.workDate);
                const d = new Date(s.workDate);
                this.dateList.push({
                  fullDate: s.workDate,
                  formatDate: s.workDate.substring(5),
                  week: weeks[d.getDay()]
                });
              }
            });
          }
        }
      });
    },
    fetchReviews() {
        uni.request({
            url: 'http://localhost:8080/api/appointment/reviews',
            data: { doctorId: this.doctorId },
            success: (res) => {
                if (res.data.code === 200) {
                    this.reviews = res.data.data;
                }
            }
        });
    },
    handleBooking(s) {
      if (s.status === 'leave' || s.bookedCount >= s.totalQuota) return;
      uni.showModal({
        title: '确认预约',
        content: `确定预约 ${s.workDate} ${s.period} 吗？`,
        success: (m) => {
          if (m.confirm) {
            uni.request({
              url: 'http://localhost:8080/api/appointment/book',
              method: 'POST',
              data: { patientId: this.userInfo.id, scheduleId: s.id },
              success: (res) => {
                if(res.data.code === 200) {
                  uni.showToast({ title: '预约成功', icon: 'success' });
                  this.showBookPopup = false;
                  this.fetchSchedules(); 
                } else {
                  uni.showToast({ title: res.data.msg, icon: 'none' });
                }
              }
            });
          }
        }
      });
    },
    formatDate(str) {
        if (!str) return '';
        return str.split('T')[0];
    }
  }
}
</script>

<style>
.container { min-height: 100vh; background: #f8f9fb; padding: 30rpx; padding-bottom: 100rpx; }
.doctor-header-card { display: flex; background: #fff; border-radius: 20rpx; padding: 30rpx; box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05); margin-bottom: 20rpx; }
.header-avatar { width: 140rpx; height: 140rpx; border-radius: 12rpx; background: #eee; margin-right: 30rpx; }
.header-right { flex: 1; }

/* 名字和收藏按钮行 */
.name-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.doc-name { font-size: 36rpx; font-weight: bold; color: #333; }
.fav-btn { display: flex; align-items: center; padding: 6rpx 16rpx; border-radius: 30rpx; background-color: #f5f5f5; transition: all 0.3s; }
.fav-btn:active { opacity: 0.7; }
.fav-icon { font-size: 32rpx; margin-right: 6rpx; }
.fav-text { font-size: 24rpx; color: #666; }

.intro-box { background: #fdfdfd; border-top: 1rpx solid #eee; padding-top: 10rpx; }
.intro-title { font-size: 24rpx; color: #999; display: block; margin-bottom: 8rpx; }
.intro-content { font-size: 26rpx; color: #666; line-height: 1.5; }
.book-main-btn { margin-top: 40rpx; margin-bottom: 40rpx; background: #2b86ff; color: #fff; border-radius: 50rpx; font-size: 32rpx; }

/* 评价部分样式 */
.review-section { background: #fff; border-radius: 20rpx; padding: 30rpx; margin-top: 20rpx; }
.section-header { border-bottom: 1rpx solid #f0f0f0; padding-bottom: 20rpx; margin-bottom: 20rpx; }
.section-header .title { font-size: 30rpx; font-weight: bold; border-left: 8rpx solid #2b86ff; padding-left: 16rpx; }
.review-item { padding-bottom: 30rpx; margin-bottom: 30rpx; border-bottom: 1rpx solid #f9f9f9; }
.review-item:last-child { border-bottom: none; margin-bottom: 0; padding-bottom: 0; }
.review-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.user-info { display: flex; align-items: center; }
.u-avatar { width: 60rpx; height: 60rpx; border-radius: 50%; background: #eee; margin-right: 16rpx; }
.u-name { font-size: 26rpx; color: #333; font-weight: bold; }
.rating-stars .star { color: #eee; font-size: 24rpx; margin-left: 2rpx; }
.rating-stars .star.active { color: #ffca28; }
.review-content { font-size: 28rpx; color: #666; line-height: 1.6; margin-bottom: 12rpx; }
.review-time { font-size: 22rpx; color: #999; text-align: right; }
.empty-reviews { text-align: center; color: #ccc; font-size: 24rpx; padding: 40rpx 0; }

/* 弹窗样式 */
.book-popup { width: 100%; position: absolute; bottom: 0; border-radius: 40rpx 40rpx 0 0; padding: 40rpx; background: #fff; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 99; }
.modal-content { width: 100%; background: #fff; border-radius: 30rpx 30rpx 0 0; padding: 40rpx; min-height: 500rpx; }
.modal-header { text-align: center; font-size: 32rpx; font-weight: bold; margin-bottom: 40rpx; }
.date-scroll { white-space: nowrap; margin-bottom: 40rpx; }
.date-item { display: inline-block; width: 120rpx; height: 120rpx; background: #f5f5f5; margin-right: 20rpx; border-radius: 16rpx; text-align: center; padding-top: 20rpx; }
.date-item.active { background: #e6f1ff; color: #2b86ff; border: 2rpx solid #2b86ff; }
.week { font-size: 22rpx; display: block; }
.date { font-size: 28rpx; font-weight: bold; }
.period-list { display: flex; gap: 20rpx; margin-bottom: 40rpx; }
.period-box { flex: 1; border: 1rpx solid #eee; border-radius: 16rpx; padding: 20rpx; text-align: center; }
.period-box.disabled { background: #f9f9f9; color: #ccc; pointer-events: none; }
.p-name { font-weight: bold; font-size: 30rpx; }
.p-status { font-size: 20rpx; margin-left: 10rpx; }
.p-status.available { color: #52c41a; }
.p-quota { font-size: 22rpx; color: #999; display: block; }
.close-popup { text-align: center; color: #999; padding: 20rpx; }
.empty-msg { text-align: center; color: #999; padding: 40rpx; }
</style>