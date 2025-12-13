<template>
  <view class="container">
    <!-- 背景装饰圆 -->
    <view class="bg-circle-1"></view>
    <view class="bg-circle-2"></view>

    <!-- 1. 顶部导航/标题 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="back-arrow">←</text>
      </view>
      <view class="header-content">
        <text class="title">创建账号</text>
        <text class="subtitle">填写信息以开启智能导诊服务</text>
      </view>
    </view>

    <!-- 2. 注册表单卡片 -->
    <view class="form-card">
      
      <!-- 手机号 -->
      <view class="input-group">
        <view class="input-label">手机号</view>
        <view class="input-wrapper">
          <input 
            class="input" 
            type="number" 
            maxlength="11"
            v-model="formData.phone" 
            placeholder="请输入手机号" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <!-- 真实姓名 -->
      <view class="input-group">
        <view class="input-label">真实姓名</view>
        <view class="input-wrapper">
          <input 
            class="input" 
            v-model="formData.realName" 
            placeholder="请输入真实姓名" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <!-- 密码 -->
      <view class="input-group">
        <view class="input-label">设置密码</view>
        <view class="input-wrapper">
          <input 
            class="input" 
            password="true"
            v-model="formData.password" 
            placeholder="6-20位数字或字母" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <!-- 确认密码 -->
      <view class="input-group">
        <view class="input-label">确认密码</view>
        <view class="input-wrapper">
          <input 
            class="input" 
            password="true"
            v-model="formData.confirmPassword" 
            placeholder="请再次输入密码" 
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <!-- 图形验证码 -->
      <view class="input-group">
        <view class="input-label">验证码</view>
        <view class="captcha-row">
          <view class="input-wrapper captcha-input">
            <input 
              class="input" 
              v-model="formData.captcha" 
              placeholder="请输入验证码" 
              placeholder-class="placeholder-style"
            />
          </view>
          <!-- 模拟图形验证码区域 -->
          <view class="captcha-img-box" @click="refreshCaptcha">
            <text class="captcha-text">{{ simulatedCaptcha }}</text>
          </view>
        </view>
      </view>

      <!-- 注册按钮 -->
      <button class="submit-btn" hover-class="btn-hover" @click="handleRegister">
        立即注册
      </button>

      <!-- 底部协议 -->
      <view class="agreement-box">
        <text class="agreement-text">点击注册即代表同意</text>
        <text class="agreement-link" @click="openProtocol('service')">《用户服务协议》</text>
        <text class="agreement-text">和</text>
        <text class="agreement-link" @click="openProtocol('privacy')">《隐私政策》</text>
      </view>

    </view>
    
    <!-- 已有账号提示 -->
    <view class="login-link-box" @click="goBack">
      <text class="link-text">已有账号？</text>
      <text class="link-highlight">去登录</text>
    </view>

  </view>
</template>

<script>
export default {
  data() {
    return {
      formData: {
        phone: '',
        realName: '',
        password: '',
        confirmPassword: '',
        captcha: ''
      },
      simulatedCaptcha: '' // 模拟的后端验证码
    };
  },
  onLoad() {
    this.refreshCaptcha();
  },
  methods: {
    // 生成4位随机验证码
    refreshCaptcha() {
      const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
      let result = '';
      for (let i = 0; i < 4; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
      }
      this.simulatedCaptcha = result;
    },
    
    // 返回登录页
    goBack() {
      // 尝试使用 navigateBack，如果当前是栈底（比如直接打开的注册页），则重定向到登录页
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        uni.redirectTo({
          url: '/pages/login/index' // 注意：如果你的登录页路径是 index，请改为 /pages/login/index
        });
      }
    },
    
    // 打开协议页面
    openProtocol(type) {
      // 这里的路径假设你会在 pages/agreement/ 目录下创建对应文件
      // 实际开发中，你可能需要创建这些页面
      const url = type === 'service' ? '/pages/agreement/service' : '/pages/agreement/privacy';
      
      console.log('尝试打开协议:', url);
      
      uni.navigateTo({
        url: url,
        fail: () => {
          // 如果页面还没做，先弹个提示
          uni.showToast({
            title: '协议页面开发中...',
            icon: 'none'
          });
        }
      });
    },

    handleRegister() {
      // 1. 基础非空验证
      if (!this.formData.phone || !this.formData.realName || !this.formData.password) {
        return uni.showToast({ title: '请填写完整信息', icon: 'none' });
      }

      // 2. 手机号格式验证
      if (!/^1[3-9]\d{9}$/.test(this.formData.phone)) {
        return uni.showToast({ title: '手机号格式不正确', icon: 'none' });
      }

      // 3. 密码一致性验证
      if (this.formData.password !== this.formData.confirmPassword) {
        return uni.showToast({ title: '两次输入的密码不一致', icon: 'none' });
      }

      // 4. 验证码验证
      if (this.formData.captcha.toUpperCase() !== this.simulatedCaptcha) {
        this.refreshCaptcha();
        this.formData.captcha = '';
        return uni.showToast({ title: '验证码错误', icon: 'none' });
      }

      uni.showLoading({ title: '注册中...' });

      // === 5. 发起真实网络请求 ===
      uni.request({
        url: 'http://localhost:8080/api/register', // 如果是真机调试，请改 IP
        method: 'POST',
        data: {
          phone: this.formData.phone,
          realName: this.formData.realName,
          password: this.formData.password
        },
        success: (res) => {
          uni.hideLoading();
          
          if (res.data.code === 200) {
            uni.showToast({ title: '注册成功', icon: 'success' });
            
            // 延迟跳转回登录页
            setTimeout(() => {
              this.goBack();
            }, 1500);
          } else {
            // 显示后端返回的错误信息（如：该手机号已注册）
            uni.showToast({ 
              title: res.data.msg || '注册失败', 
              icon: 'none' 
            });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('注册请求失败', err);
          uni.showToast({ title: '服务器连接失败', icon: 'none' });
        }
      });
    }
  }
};
</script>

<style>
/* 容器与背景复用登录页风格 */
.container {
  min-height: 100vh;
  background-color: #f8faff;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 0 40rpx;
}

.bg-circle-1 {
  position: absolute;
  top: -100rpx;
  right: -100rpx;
  width: 400rpx;
  height: 400rpx;
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.2) 0%, rgba(0, 242, 254, 0.1) 100%);
  border-radius: 50%;
  z-index: 0;
}

.bg-circle-2 {
  position: absolute;
  top: 10%;
  left: -150rpx;
  width: 300rpx;
  height: 300rpx;
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.1) 0%, rgba(79, 172, 254, 0.05) 100%);
  border-radius: 50%;
  z-index: 0;
}

/* 1. 头部区域 */
.header {
  margin-top: 80rpx;
  margin-bottom: 40rpx;
  position: relative;
  z-index: 1;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  margin-bottom: 20rpx;
}

.back-arrow {
  font-size: 40rpx;
  color: #333;
  font-weight: bold;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #1a1a1a;
  display: block;
  margin-bottom: 10rpx;
}

.subtitle {
  font-size: 26rpx;
  color: #889aa4;
}

/* 2. 表单卡片 */
.form-card {
  width: 100%;
  background: #fff;
  border-radius: 30rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 20rpx 60rpx rgba(20, 20, 50, 0.06);
  z-index: 1;
  box-sizing: border-box;
}

.input-group {
  margin-bottom: 30rpx;
}

.input-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 12rpx;
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

/* 简单的聚焦效果 */
.input-wrapper:active {
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

/* 验证码特殊布局 */
.captcha-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.captcha-input {
  flex: 1;
  margin-right: 20rpx;
}

.captcha-img-box {
  width: 200rpx;
  height: 90rpx;
  background: linear-gradient(90deg, #eef2f5, #e4e9f0);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* 模拟验证码图片的干扰线效果 */
.captcha-img-box::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: repeating-linear-gradient(45deg, rgba(0,0,0,0.05) 0, rgba(0,0,0,0.05) 2px, transparent 0, transparent 4px);
  pointer-events: none;
}

.captcha-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #2b86ff;
  letter-spacing: 10rpx;
  font-style: italic; /* 斜体增加验证码感觉 */
}

/* 按钮样式 */
.submit-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  background: linear-gradient(120deg, #2b86ff, #1ca0ff);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 45rpx;
  margin-top: 50rpx;
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

.agreement-box {
  margin-top: 30rpx;
  /* 使用 Flex 布局让文字自然流式排列并居中 */
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}

.agreement-text {
  font-size: 22rpx;
  color: #999;
}

.agreement-link {
  font-size: 22rpx;
  color: #2b86ff; /* 设置为蓝色，表示可点击 */
  padding: 0 4rpx; /* 增加一点点击区域 */
}

/* 底部链接 */
.login-link-box {
  margin-top: 40rpx;
  text-align: center;
  padding: 20rpx;
  z-index: 1;
}

.link-text {
  font-size: 28rpx;
  color: #666;
}

.link-highlight {
  font-size: 28rpx;
  color: #2b86ff;
  font-weight: bold;
  margin-left: 10rpx;
}
</style>