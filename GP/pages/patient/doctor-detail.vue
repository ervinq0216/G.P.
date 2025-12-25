<template>
  <view class="container">
    <!-- 医生信息卡片 -->
    <view class="doc-card">
      <image :src="doctor.avatar || '/static/default_avatar.png'" class="avatar" mode="aspectFill"></image>
      <view class="info">
        <text class="name">{{ doctor.realName }}</text>
        <text class="dept">工号: {{ doctor.jobNumber }}</text>
      </view>
    </view>
    
    <!-- 简介 -->
    <view class="section">
      <view class="title">医生简介</view>
      <text class="desc">{{ doctor.introduction || '暂无详细介绍' }}</text>
    </view>

    <!-- 预约按钮 -->
    <button class="book-btn" @click="openBookModal">立即预约</button>

    <!-- 预约弹窗 -->
    <view class="modal-mask" v-if="showModal" @click="showModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">选择预约时间</view>
        
        <!-- 日期选择 (未来5个工作日) -->
        <scroll-view scroll-x class="date-scroll">
          <view class="date-item" v-for="(day, index) in scheduleList" :key="index"
                :class="{ active: selectedDateIndex === index }"
                @click="selectedDateIndex = index">
            <text class="week">{{ getWeekDay(day.workDate) }}</text>
            <text class="date">{{ formatDate(day.workDate) }}</text>
          </view>
        </scroll-view>

        <!-- 上下午时段 -->
        <view class="period-list" v-if="scheduleList.length > 0">
          <!-- 上午 -->
          <view class="period-item" 
                :class="{ disabled: isPeriodDisabled(currentSchedule, '上午') }"
                @click="selectPeriod('上午')">
            <text>上午</text>
            <text class="quota">余 {{ getQuota(currentSchedule, '上午') }}</text>
          </view>
          
          <!-- 下午 -->
          <view class="period-item" 
                :class="{ disabled: isPeriodDisabled(currentSchedule, '下午') }"
                @click="selectPeriod('下午')">
            <text>下午</text>
            <text class="quota">余 {{ getQuota(currentSchedule, '下午') }}</text>
          </view>
        </view>
        <view v-else class="empty-tip">近期暂无排班</view>
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
      showModal: false,
      scheduleList: [], // 后端返回的排班列表
      selectedDateIndex: 0,
      userInfo: {}
    };
  },
  computed: {
    // 获取当前选中的日期的排班数据(包含上午下午两条记录)
    currentSchedule() {
      if (this.scheduleList.length === 0) return [];
      const date = this.scheduleList[this.selectedDateIndex].workDate;
      // 过滤出该日期的所有排班(上午+下午)
      return this.scheduleList.filter(s => s.workDate === date);
    }
  },
  onLoad(options) {
    this.doctorId = options.id;
    this.userInfo = uni.getStorageSync('userInfo');
    this.fetchDoctorInfo();
  },
  methods: {
    fetchDoctorInfo() {
      uni.request({
        url: 'http://localhost:8080/api/doctor/info/' + this.doctorId,
        success: (res) => {
          if (res.data.code === 200) this.doctor = res.data.data;
        }
      });
    },
    
    openBookModal() {
      this.showModal = true;
      this.fetchSchedule();
    },

    fetchSchedule() {
      uni.showLoading({ title: '加载排班' });
      uni.request({
        url: 'http://localhost:8080/api/schedule/list',
        data: { doctorId: this.doctorId },
        success: (res) => {
          uni.hideLoading();
          if (res.data.code === 200) {
            // 后端返回的是平铺的列表，我们需要去重日期用于顶部滚动条
            const rawList = res.data.data;
            // 提取唯一日期
            const dateMap = new Map();
            rawList.forEach(s => {
              if(!dateMap.has(s.workDate)) {
                dateMap.set(s.workDate, s); // 存一个代表即可
              }
            });
            this.scheduleList = Array.from(dateMap.values()); // 这里只存去重后的日期对象
            this.fullScheduleData = rawList; // 保存完整数据用于查找
          }
        }
      });
    },

    // 查找特定日期和时段的排班详情
    findSchedule(period) {
      if (this.scheduleList.length === 0) return null;
      const date = this.scheduleList[this.selectedDateIndex].workDate;
      return this.fullScheduleData.find(s => s.workDate === date && s.period === period);
    },

    isPeriodDisabled(list, period) {
      const s = this.findSchedule(period);
      if (!s) return true; // 无排班
      if (s.status === 'leave') return true; // 医生请假
      if (s.bookedCount >= s.totalQuota) return true; // 满号
      return false;
    },

    getQuota(list, period) {
      const s = this.findSchedule(period);
      if (!s) return 0;
      if (s.status === 'leave') return '请假';
      return s.totalQuota - s.bookedCount;
    },

    selectPeriod(period) {
      if (this.isPeriodDisabled(null, period)) return;
      
      const s = this.findSchedule(period);
      uni.showModal({
        title: '确认预约',
        content: `确定预约 ${s.workDate} ${period} 吗？`,
        success: (res) => {
          if (res.confirm) {
            this.submitBooking(s.id);
          }
        }
      });
    },

    submitBooking(scheduleId) {
      uni.showLoading({ title: '预约中' });
      uni.request({
        url: 'http://localhost:8080/api/appointment/book',
        method: 'POST',
        data: {
          patientId: this.userInfo.id,
          scheduleId: scheduleId
        },
        success: (res) => {
          uni.hideLoading();
          if (res.data.code === 200) {
            uni.showToast({ title: '预约成功', icon: 'success' });
            this.showModal = false;
            // 可跳转到我的挂号单页面
          } else {
            uni.showToast({ title: res.data.msg, icon: 'none' });
          }
        }
      });
    },

    // 工具方法
    getWeekDay(dateStr) {
      const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
      return days[new Date(dateStr).getDay()];
    },
    formatDate(dateStr) {
      // 2025-12-25 -> 12-25
      return dateStr.substring(5);
    }
  }
};
</script>

<style>
.container { min-height: 100vh; background-color: #f5f7fa; padding: 20rpx; }
.doc-card { background: #fff; border-radius: 20rpx; padding: 40rpx; display: flex; align-items: center; margin-bottom: 20rpx; }
.avatar { width: 140rpx; height: 140rpx; border-radius: 50%; margin-right: 30rpx; background: #eee; }
.name { font-size: 36rpx; font-weight: bold; color: #333; margin-bottom: 10rpx; display: block; }
.dept { font-size: 26rpx; color: #666; }

.section { background: #fff; border-radius: 20rpx; padding: 30rpx; margin-bottom: 40rpx; }
.title { font-size: 30rpx; font-weight: bold; border-left: 8rpx solid #2b86ff; padding-left: 16rpx; margin-bottom: 20rpx; }
.desc { font-size: 28rpx; color: #666; line-height: 1.6; }

.book-btn { background: #2b86ff; color: #fff; border-radius: 50rpx; width: 90%; margin: 0 auto; display: block; }

.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 99; }
.modal-content { width: 100%; background: #fff; border-radius: 30rpx 30rpx 0 0; padding: 40rpx; min-height: 500rpx; }
.modal-header { text-align: center; font-size: 32rpx; font-weight: bold; margin-bottom: 40rpx; }

.date-scroll { white-space: nowrap; margin-bottom: 40rpx; }
.date-item { display: inline-block; width: 120rpx; height: 120rpx; background: #f5f7fa; border-radius: 16rpx; margin-right: 20rpx; text-align: center; padding-top: 20rpx; box-sizing: border-box; }
.date-item.active { background: #e6f7ff; border: 2rpx solid #2b86ff; color: #2b86ff; }
.week { font-size: 24rpx; display: block; margin-bottom: 8rpx; }
.date { font-size: 28rpx; font-weight: bold; }

.period-list { display: flex; justify-content: space-between; }
.period-item { width: 48%; height: 100rpx; background: #f0f0f0; border-radius: 16rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.period-item.disabled { opacity: 0.5; background: #eee; color: #999; pointer-events: none; }
.quota { font-size: 22rpx; margin-top: 6rpx; color: #666; }
</style>