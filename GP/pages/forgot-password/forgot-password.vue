<template>
  <view class="container">
    <!-- 1. 顶部导航 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="back-arrow">←</text>
      </view>
      <view class="header-content">
        <text class="title">重置密码</text>
        <text class="subtitle">请输入您的信息以重置登录密码</text>
      </view>
    </view>

    <!-- 2. 表单卡片 -->
    <view class="form-card">
      
      <!-- 动态字段1：账号/手机号/工号 -->
      <view class="input-group">
        <view class="input-label">{{ fieldConfig.accountLabel }}</view>
        <view class="input-wrapper">
          <input 
            class="input" 
            :type="role === 'patient' ? 'number' : 'text'"
            :maxlength="role === 'patient' ? 11 : 20"
            v-model="account" 
            :placeholder="fieldConfig.accountPlaceholder" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <!-- 动态字段2：姓名 -->
      <view class="input-group">
        <view class="input-label">{{ fieldConfig.nameLabel }}</view>
        <view class="input-wrapper">
          <input 
            class="input" 
            type="text"
            v-model="name" 
            :placeholder="fieldConfig.namePlaceholder" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <!-- 新密码 -->
      <view class="input-group">
        <view class="input-label">新密码</view>
        <view class="input-wrapper">
          <input 
            class="input" 
            password="true" 
            v-model="newPassword" 
            placeholder="请输入新密码" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <!-- 确认密码 -->
      <view class="input-group">
        <view class="input-label">确认新密码</view>
        <view class="input-wrapper">
          <input 
            class="input" 
            password="true" 
            v-model="confirmPassword" 
            placeholder="请再次输入新密码" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <button class="submit-btn" hover-class="btn-hover" @click="handleReset">
        重置密码
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      role: 'patient', // 默认为患者，通过 onLoad 接收参数修改
      account: '',     // 统一绑定的账号字段（手机号/工号/管理员账号）
      name: '',        // 统一绑定的姓名字段
      newPassword: '',
      confirmPassword: ''
    };
  },
  onLoad(options) {
    if (options.role) {
      this.role = options.role;
      console.log('当前找回密码角色:', this.role);
    }
  },
  computed: {
    // 根据角色动态配置文案
    fieldConfig() {
      if (this.role === 'doctor') {
        return {
          accountLabel: '医生工号',
          accountPlaceholder: '请输入您的医生工号',
          nameLabel: '医生姓名',
          namePlaceholder: '请输入您的真实姓名'
        };
      } else if (this.role === 'admin') {
        return {
          accountLabel: '管理员账号',
          accountPlaceholder: '请输入管理员账号',
          nameLabel: '管理员姓名',
          namePlaceholder: '请输入真实姓名'
        };
      } else {
        // 默认患者
        return {
          accountLabel: '手机号',
          accountPlaceholder: '请输入注册手机号',
          nameLabel: '患者姓名',
          namePlaceholder: '请输入真实姓名'
        };
      }
    }
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    handleReset() {
      // 1. 基础非空校验
            if (!this.account || !this.name || !this.newPassword) {
              return uni.showToast({ title: '请填写完整信息', icon: 'none' });
            }
      
            // 2. 根据角色进行特定格式校验
            if (this.role === 'patient') {
              if (!/^1[3-9]\d{9}$/.test(this.account)) {
                return uni.showToast({ title: '手机号格式不正确', icon: 'none' });
              }
            }
      
            // 3. 密码一致性校验
            if (this.newPassword !== this.confirmPassword) {
              return uni.showToast({ title: '两次密码不一致', icon: 'none' });
            }
            
            uni.showLoading({ title: '校验中...' });
            
            // === 4. 发起真实网络请求 (修改部分) ===
            uni.request({
              url: 'http://localhost:8080/api/reset-password',
              method: 'POST',
              data: {
                role: this.role,
                account: this.account,
                name: this.name,
                newPassword: this.newPassword
              },
              success: (res) => {
                uni.hideLoading();
                
                if (res.data.code === 200) {
                  uni.showToast({ title: '重置成功，请登录', icon: 'success' });
                  setTimeout(() => {
                    uni.navigateBack();
                  }, 1500);
                } else {
                  uni.showToast({ 
                    title: res.data.msg || '重置失败', 
                    icon: 'none' 
                  });
                }
              },
              fail: (err) => {
                uni.hideLoading();
                console.error('请求失败', err);
                uni.showToast({ title: '服务器连接失败', icon: 'none' });
              }
            });
    }
  }
};
</script>

<style>
/* 复用之前的通用样式 */
.container { min-height: 100vh; background-color: #f8faff; padding: 0 40rpx; overflow: hidden; }
.header { margin-top: 80rpx; margin-bottom: 40rpx; }
.back-btn { width: 60rpx; height: 60rpx; margin-bottom: 20rpx; }
.back-arrow { font-size: 40rpx; color: #333; font-weight: bold; }
.title { font-size: 48rpx; font-weight: bold; color: #1a1a1a; display: block; margin-bottom: 10rpx; }
.subtitle { font-size: 26rpx; color: #889aa4; }
.form-card { width: 100%; background: #fff; border-radius: 30rpx; padding: 40rpx 30rpx; box-shadow: 0 20rpx 60rpx rgba(20, 20, 50, 0.06); }
.input-group { margin-bottom: 30rpx; }
.input-label { font-size: 28rpx; color: #333; font-weight: 600; margin-bottom: 12rpx; margin-left: 10rpx; }
.input-wrapper { background-color: #f7f8fa; border-radius: 16rpx; height: 90rpx; padding: 0 30rpx; display: flex; align-items: center; }
.input { flex: 1; height: 100%; font-size: 30rpx; }
.placeholder-style { color: #bfbfbf; font-size: 28rpx; }
.submit-btn { width: 100%; height: 90rpx; line-height: 90rpx; background: linear-gradient(120deg, #2b86ff, #1ca0ff); color: #fff; font-size: 32rpx; font-weight: 600; border-radius: 45rpx; margin-top: 40rpx; border: none; box-shadow: 0 10rpx 20rpx rgba(43, 134, 255, 0.3); }
.submit-btn::after { border: none; }
.btn-hover { opacity: 0.9; transform: scale(0.98); }
</style>