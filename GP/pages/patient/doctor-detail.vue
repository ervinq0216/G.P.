<template>
  <view class="container">
    <!-- 医生信息部分 (保持原样) -->
    <view class="doctor-header-card">
      <image :src="doctor.avatar || '/static/default_avatar.png'" class="header-avatar" mode="aspectFill"></image>
      <view class="header-right">
        <text class="doc-name">{{ doctor.realName }}</text>
        <view class="intro-box">
          <text class="intro-title">医生简介</text>
          <text class="intro-content">{{ doctor.introduction || '暂无详细介绍' }}</text>
        </view>
      </view>
    </view>

    <view class="action-section">
      <button class="book-main-btn" @click="showBookPopup = true">点击预约挂号</button>
    </view>

    <!-- 预约选择弹窗 -->
    <view class="modal-mask" v-if="showBookPopup" @click="showBookPopup = false">
      <view class="modal-content book-popup" @click.stop>
        <view class="modal-header">选择预约时段</view>
        
        <scroll-view scroll-x class="date-scroll">
          <view class="date-item" v-for="(day, index) in dateList" :key="index"
                :class="{ active: selectedDateIndex === index }" @click="selectedDateIndex = index">
            <text class="week">{{ day.isToday ? '今天' : day.week }}</text>
            <text class="date">{{ day.formatDate }}</text>
          </view>
        </scroll-view>

        <view class="period-list" v-if="currentDaySchedules.length > 0">
          <view class="period-box" v-for="s in currentDaySchedules" :key="s.id"
                :class="{ disabled: s.status === 'leave' || s.status === 'expired' || s.bookedCount >= s.totalQuota }"
                @click="handleBooking(s)">
            <view class="period-top">
              <text class="p-name">{{ s.period }}</text>
              <!-- 状态判断 -->
              <text class="p-status" v-if="s.status === 'leave'">请假</text>
              <text class="p-status" v-else-if="s.status === 'expired'">已截止</text>
              <text class="p-status" v-else-if="s.bookedCount >= s.totalQuota">已满</text>
              <text class="p-status available" v-else>有号</text>
            </view>
            <text class="p-quota" v-if="s.status !== 'expired'">剩余：{{ s.totalQuota - s.bookedCount }}</text>
            <text class="p-quota" v-else>不可约</text>
          </view>
        </view>

        <view v-if="currentDaySchedules.length === 0" class="empty-msg">当前日期暂无排班</view>

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
      userInfo: {}
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
  },
  methods: {
    fetchDoctor() {
      uni.request({
        url: 'http://localhost:8080/api/doctor/info/' + this.doctorId,
        success: (res) => { if(res.data.code === 200) this.doctor = res.data.data; }
      });
    },
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
            const todayStr = new Date().toISOString().split('T')[0];
            
            this.scheduleList.forEach(s => {
              if(!seen.has(s.workDate)) {
                seen.add(s.workDate);
                const d = new Date(s.workDate);
                this.dateList.push({
                  fullDate: s.workDate,
                  formatDate: s.workDate.substring(5),
                  week: weeks[d.getDay()],
                  isToday: s.workDate === todayStr
                });
              }
            });
          }
        }
      });
    },
    handleBooking(s) {
      // 增加 expired 禁用判断
      if (s.status === 'leave' || s.status === 'expired' || s.bookedCount >= s.totalQuota) return;
      
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
    }
  }
}
</script>

<style>
/* ... (基础样式保持不变) ... */
.container { min-height: 100vh; background: #f8f9fb; padding: 30rpx; }
.doctor-header-card { display: flex; background: #fff; border-radius: 20rpx; padding: 30rpx; box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05); }
.header-avatar { width: 180rpx; height: 240rpx; border-radius: 12rpx; background: #eee; margin-right: 30rpx; }
.header-right { flex: 1; }
.doc-name { font-size: 38rpx; font-weight: bold; color: #333; margin-bottom: 20rpx; display: block; }
.intro-box { background: #fdfdfd; border-top: 1rpx solid #eee; padding-top: 10rpx; }
.intro-title { font-size: 24rpx; color: #999; display: block; margin-bottom: 8rpx; }
.intro-content { font-size: 26rpx; color: #666; line-height: 1.6; }
.book-main-btn { margin-top: 60rpx; background: #2b86ff; color: #fff; border-radius: 50rpx; }

.book-popup { width: 100%; position: absolute; bottom: 0; border-radius: 40rpx 40rpx 0 0; padding: 40rpx; background: #fff; }
.date-scroll { white-space: nowrap; margin-bottom: 40rpx; }
.date-item { display: inline-block; width: 120rpx; height: 120rpx; background: #f5f5f5; margin-right: 20rpx; border-radius: 16rpx; text-align: center; padding-top: 20rpx; }
.date-item.active { background: #e6f1ff; color: #2b86ff; border: 2rpx solid #2b86ff; }
.week { font-size: 22rpx; display: block; }
.date { font-size: 28rpx; font-weight: bold; }
.period-list { display: flex; gap: 20rpx; margin-bottom: 40rpx; }
.period-box { flex: 1; border: 1rpx solid #eee; border-radius: 16rpx; padding: 20rpx; text-align: center; }
.period-box.disabled { background: #f9f9f9 !important; color: #ccc !important; border-color: #eee; }
.p-name { font-weight: bold; font-size: 30rpx; }
.p-status { font-size: 20rpx; margin-left: 10rpx; }
.p-status.available { color: #52c41a; }
.p-quota { font-size: 22rpx; color: #999; display: block; }
.close-popup { text-align: center; color: #999; padding: 20rpx; }
.empty-msg { text-align: center; color: #999; padding: 40rpx; }
</style>