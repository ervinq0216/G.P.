<template>
  <view class="container">
    <view class="form-list">
      <view class="form-item avatar-item" @click="chooseAvatar">
        <text class="label">头像</text>
        <view class="right-content">
          <image :src="doctorInfo.avatar || '/static/default_avatar.png'" class="avatar-img" mode="aspectFill"></image>
          <text class="arrow">></text>
        </view>
      </view>

      <view class="form-item">
        <text class="label">姓名</text>
        <input class="input" v-model="doctorInfo.realName" placeholder="请输入姓名" />
      </view>

      <view class="form-item">
        <text class="label">性别</text>
        <picker :range="genders" @change="e => doctorInfo.gender = genders[e.detail.value]">
          <view class="picker-value">
            {{ doctorInfo.gender || '请选择' }} <text class="arrow">></text>
          </view>
        </picker>
      </view>

      <view class="form-item bio-item">
        <text class="label">个人简介</text>
        <textarea class="textarea" v-model="doctorInfo.introduction" placeholder="请输入个人及擅长领域简介..." />
      </view>
    </view>

    <button class="save-btn" @click="saveInfo">保存修改</button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      doctorInfo: {},
      genders: ['男', '女']
    };
  },
  onLoad() {
    const user = uni.getStorageSync('userInfo');
    if(user) this.doctorInfo = { ...user };
  },
  methods: {
    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: (res) => {
          const filePath = res.tempFilePaths[0];
          // #ifdef MP-WEIXIN
          uni.getFileSystemManager().readFile({
            filePath: filePath, encoding: 'base64',
            success: r => this.doctorInfo.avatar = 'data:image/png;base64,' + r.data
          });
          // #endif
          // #ifndef MP-WEIXIN
          uni.request({
            url: filePath, responseType: 'arraybuffer',
            success: r => this.doctorInfo.avatar = 'data:image/png;base64,' + uni.arrayBufferToBase64(r.data)
          });
          // #endif
        }
      });
    },
    saveInfo() {
      uni.showLoading({ title: '保存中' });
      uni.request({
        url: 'http://localhost:8080/api/doctor/update',
        method: 'POST',
        data: this.doctorInfo,
        success: (res) => {
          uni.hideLoading();
          if (res.data.code === 200) {
            uni.setStorageSync('userInfo', res.data.data); // 更新缓存
            uni.showToast({ title: '保存成功' });
            setTimeout(() => uni.navigateBack(), 1000);
          } else {
            uni.showToast({ title: '保存失败', icon: 'none' });
          }
        }
      });
    }
  }
};
</script>

<style>
.container { min-height: 100vh; background-color: #f5f7fa; padding-top: 20rpx; }
.form-list { background-color: #fff; padding: 0 30rpx; }
.form-item { display: flex; align-items: center; justify-content: space-between; border-bottom: 1rpx solid #eee; padding: 30rpx 0; }
.bio-item { flex-direction: column; align-items: flex-start; }
.label { font-size: 30rpx; color: #333; }
.right-content, .picker-value { display: flex; align-items: center; font-size: 30rpx; color: #666; }
.avatar-img { width: 100rpx; height: 100rpx; border-radius: 50%; background-color: #eee; margin-right: 16rpx; }
.input { text-align: right; font-size: 30rpx; flex: 1; }
.textarea { width: 100%; height: 200rpx; background: #f8f8f8; margin-top: 20rpx; padding: 20rpx; border-radius: 10rpx; font-size: 28rpx; box-sizing: border-box; }
.save-btn { width: 90%; background: #2b86ff; color: #fff; border-radius: 45rpx; margin: 60rpx auto; }
</style>