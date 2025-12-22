<template>
  <view class="container">
    <view class="form-list">
      
      <!-- 头像 -->
      <view class="form-item avatar-item" @click="chooseAvatar">
        <text class="label">头像</text>
        <view class="right-content">
          <image 
            :src="doctorInfo.avatar || '/static/default_avatar.png'" 
            class="avatar-img" 
            mode="aspectFill"
          ></image>
          <text class="arrow">></text>
        </view>
      </view>

      <!-- 姓名 -->
      <view class="form-item">
        <text class="label">姓名</text>
        <input 
          class="input" 
          v-model="doctorInfo.realName" 
          placeholder="请输入姓名" 
        />
      </view>

      <!-- 性别 -->
      <view class="form-item">
        <text class="label">性别</text>
        <picker 
          :range="genders" 
          @change="onGenderChange"
        >
          <view class="picker-value">
            {{ doctorInfo.gender || '请选择' }} <text class="arrow">></text>
          </view>
        </picker>
      </view>

      <!-- 个人简介 -->
      <view class="form-item bio-item">
        <text class="label">个人简介</text>
        <textarea 
          class="textarea" 
          v-model="doctorInfo.introduction" 
          placeholder="请输入个人及擅长领域简介..." 
          maxlength="200"
        />
      </view>

    </view>

    <button class="save-btn" @click="saveInfo">保存修改</button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      doctorInfo: {
        id: null,
        avatar: '',
        realName: '',
        gender: '',
        introduction: ''
      },
      genders: ['男', '女']
    };
  },
  onLoad() {
    // 从缓存加载基础信息
    const user = uni.getStorageSync('userInfo');
    if (user) {
      this.doctorInfo = { ...this.doctorInfo, ...user };
      
      // 如果本地缓存没有头像(因为我们不存了)，且有ID，尝试从后端拉取一次用于显示
      // 注意：这只为了本次编辑显示，不更新回缓存
      if (!this.doctorInfo.avatar && this.doctorInfo.id) {
        this.fetchDoctorInfo();
      }
    }
  },
  methods: {
    // 主动获取完整信息（含头像）
    fetchDoctorInfo() {
      uni.request({
        url: 'http://localhost:8080/api/doctor/info/' + this.doctorInfo.id,
        method: 'GET',
        success: (res) => {
          if (res.data.code === 200) {
            this.doctorInfo = res.data.data;
          }
        }
      });
    },

    onGenderChange(e) {
      this.doctorInfo.gender = this.genders[e.detail.value];
    },
    
    // 选择头像 (兼容H5/小程序)
    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: (res) => {
          const filePath = res.tempFilePaths[0];
          
          // #ifdef MP-WEIXIN
          uni.getFileSystemManager().readFile({
            filePath: filePath,
            encoding: 'base64',
            success: r => {
              this.doctorInfo.avatar = 'data:image/png;base64,' + r.data;
            }
          });
          // #endif

          // #ifndef MP-WEIXIN
          uni.request({
            url: filePath,
            responseType: 'arraybuffer',
            success: r => {
              const base64 = uni.arrayBufferToBase64(r.data);
              this.doctorInfo.avatar = 'data:image/png;base64,' + base64;
            }
          });
          // #endif
        }
      });
    },

    saveInfo() {
      uni.showLoading({ title: '保存中...' });
      
      uni.request({
        url: 'http://localhost:8080/api/doctor/update',
        method: 'POST',
        data: this.doctorInfo,
        success: (res) => {
          uni.hideLoading();
          if (res.data.code === 200) {
            
            // === 关键修复点 ===
            // 后端返回了包含 Base64 头像的完整数据
            // 我们存入 LocalStorage 前，必须把 avatar 删掉
            // 否则会报 QuotaExceededError
            
            const safeUserInfo = JSON.parse(JSON.stringify(res.data.data));
            delete safeUserInfo.avatar; // 删除头像字段
            
            try {
              uni.setStorageSync('userInfo', safeUserInfo);
            } catch (e) {
              console.error('缓存写入失败', e);
            }

            uni.showToast({ title: '保存成功' });
            setTimeout(() => uni.navigateBack(), 1000);
          } else {
            uni.showToast({ title: '保存失败', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    }
  }
};
</script>

<style>
.container { min-height: 100vh; background-color: #f5f7fa; padding-top: 20rpx; }
.form-list { background-color: #fff; padding: 0 30rpx; }
.form-item { display: flex; align-items: center; justify-content: space-between; min-height: 110rpx; border-bottom: 1rpx solid #eee; }
.form-item:last-child { border-bottom: none; }
.bio-item { flex-direction: column; align-items: flex-start; padding: 30rpx 0; }
.label { font-size: 30rpx; color: #333; margin-bottom: 10rpx; }
.right-content, .picker-value { display: flex; align-items: center; font-size: 30rpx; color: #666; }
.avatar-img { width: 100rpx; height: 100rpx; border-radius: 50%; background-color: #eee; margin-right: 16rpx; }
.arrow { color: #ccc; margin-left: 10rpx; }
.input { text-align: right; font-size: 30rpx; color: #333; flex: 1; }
.textarea { width: 100%; height: 200rpx; background-color: #f8f9fb; border-radius: 10rpx; padding: 20rpx; margin-top: 20rpx; font-size: 28rpx; box-sizing: border-box; }
.save-btn { width: 90%; background: linear-gradient(120deg, #2b86ff, #1ca0ff); color: #fff; border-radius: 45rpx; margin: 60rpx auto; }
</style>