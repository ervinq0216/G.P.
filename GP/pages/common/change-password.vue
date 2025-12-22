<template>
  <view class="container">
    <view class="form-card">
      <view class="header-title">修改登录密码</view>
      <view class="subtitle">为了您的账号安全，建议定期更换密码</view>

      <view class="input-group">
        <view class="input-label">旧密码</view>
        <view class="input-wrapper">
          <input 
            class="input" 
            password 
            v-model="oldPassword" 
            placeholder="请输入当前使用的密码" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <view class="input-group">
        <view class="input-label">新密码</view>
        <view class="input-wrapper">
          <input 
            class="input" 
            password 
            v-model="newPassword" 
            placeholder="请输入新密码" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <view class="input-group">
        <view class="input-label">确认新密码</view>
        <view class="input-wrapper">
          <input 
            class="input" 
            password 
            v-model="confirmPassword" 
            placeholder="请再次输入新密码" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <button class="submit-btn" hover-class="btn-hover" @click="handleSubmit">
        确认修改
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
      role: '',
      userId: null
    };
  },
  onLoad() {
    // 从缓存中获取当前登录用户的角色和ID
    this.role = uni.getStorageSync('role');
    const userInfo = uni.getStorageSync('userInfo');
    if (userInfo) {
      this.userId = userInfo.id;
    } else {
      uni.showToast({ title: '未登录', icon: 'none' });
      setTimeout(() => uni.reLaunch({ url: '/pages/login/index' }), 1500);
    }
  },
  methods: {
    handleSubmit() {
      if (!this.oldPassword || !this.newPassword) {
        return uni.showToast({ title: '请输入密码', icon: 'none' });
      }
      if (this.newPassword !== this.confirmPassword) {
        return uni.showToast({ title: '两次新密码不一致', icon: 'none' });
      }
      if (this.oldPassword === this.newPassword) {
        return uni.showToast({ title: '新密码不能与旧密码相同', icon: 'none' });
      }

      uni.showLoading({ title: '提交中...' });

      uni.request({
        url: 'http://localhost:8080/api/change-password',
        method: 'POST',
        data: {
          role: this.role,
          id: this.userId,
          oldPassword: this.oldPassword,
          newPassword: this.newPassword
        },
        success: (res) => {
          uni.hideLoading();
          if (res.data.code === 200) {
            uni.showToast({ title: '修改成功，请重新登录', icon: 'success' });
            // 清除登录状态并跳转回登录页
            setTimeout(() => {
              uni.removeStorageSync('token');
              uni.removeStorageSync('userInfo');
              uni.removeStorageSync('role');
              uni.reLaunch({ url: '/pages/login/index' });
            }, 1500);
          } else {
            uni.showToast({ title: res.data.msg || '修改失败', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '网络连接失败', icon: 'none' });
        }
      });
    }
  }
};
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f8faff;
  padding: 40rpx;
  overflow: hidden;
}

.form-card {
  width: 100%;
  background: #fff;
  border-radius: 30rpx;
  padding: 50rpx 40rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
  text-align: center;
}

.subtitle {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 50rpx;
  text-align: center;
}

.input-group {
  margin-bottom: 40rpx;
}

.input-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 16rpx;
  margin-left: 10rpx;
}

.input-wrapper {
  background-color: #f7f8fa;
  border-radius: 16rpx;
  height: 90rpx;
  padding: 0 30rpx;
  display: flex;
  align-items: center;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.input-wrapper:hover {
  background-color: #fff;
  border-color: #e1eaff;
}

.input {
  flex: 1;
  height: 100%;
  font-size: 30rpx;
  color: #333;
}

.placeholder-style {
  color: #bfbfbf;
  font-size: 28rpx;
}

.submit-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  background: linear-gradient(120deg, #2b86ff, #1ca0ff);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 45rpx;
  margin-top: 60rpx;
  box-shadow: 0 10rpx 20rpx rgba(43, 134, 255, 0.3);
  border: none;
}

.submit-btn::after {
  border: none;
}

.btn-hover {
  opacity: 0.9;
  transform: scale(0.98);
}
</style>