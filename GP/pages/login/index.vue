<template>
  <view class="container">
    <!-- 背景装饰圆 -->
    <view class="bg-circle-1"></view>
    <view class="bg-circle-2"></view>

    <!-- 1. 顶部 Logo/标题区域 -->
    <view class="header">
      <view class="logo-box">
        <view class="logo-circle">
          <text class="logo-text">✚</text>
        </view>
        <view class="logo-shadow"></view>
      </view>
      <text class="title">AI 辅助医院挂号系统</text>
      <text class="subtitle">智能导诊 · 精准匹配 · 便捷医疗</text>
    </view>

    <!-- 2. 角色选择区域 (Tabs) -->
    <view class="role-switch-container">
      <view class="role-switch">
        <!-- 动态滑块背景 -->
        <view 
          class="role-indicator"
          :style="{ transform: 'translateX(' + indicatorOffset + '%)' }"
        ></view>
        <!-- 角色选项 -->
        <view 
          v-for="(item, index) in roles" 
          :key="index"
          class="role-item"
          :class="{ 'role-active': currentRole === item.key }"
          @click="switchRole(item.key, index)"
        >
          <text class="role-label">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <!-- 3. 表单区域 (卡片式) -->
    <view class="form-card">
      <view class="welcome-text">欢迎登录</view>
      
      <view class="input-group">
        <view class="input-icon">👤</view>
        <input 
          class="input" 
          v-model="formData.username" 
          :placeholder="placeholderText" 
          placeholder-class="placeholder-style"
        />
      </view>

      <view class="input-group">
        <view class="input-icon">🔒</view>
        <input 
          class="input" 
          v-model="formData.password" 
          password 
          placeholder="请输入登录密码" 
          placeholder-class="placeholder-style"
        />
      </view>

      <!-- 登录按钮 -->
      <button class="login-btn" hover-class="login-btn-hover" @click="handleLogin">
        立即登录
      </button>

      <!-- 4. 底部功能区 -->
      <view class="footer-actions">
        <!-- 注册按钮 (仅患者可见) -->
        <view 
          class="action-link register" 
          :style="{ opacity: currentRole === 'patient' ? 1 : 0, pointerEvents: currentRole === 'patient' ? 'auto' : 'none' }"
          @click="goToRegister"
        >
          注册新账号
        </view>
        
        <!-- 忘记密码按钮 -->
        <view class="action-link forgot" @click="goToForgotPassword">忘记密码?</view>
      </view>
    </view>
    
    <!-- 底部版权 -->
    <view class="copyright">
      <text>智慧医疗 · 服务健康</text>
    </view>
	<view class="copyright">
	  <text>© 202208170238 邱博文</text>
	</view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      roles: [
        { key: 'patient', label: '患者' },
        { key: 'doctor', label: '医生' },
        { key: 'admin', label: '管理' }
      ],
      currentRole: 'patient',
      roleIndex: 0,
      formData: {
        username: '',
        password: ''
      }
    };
  },
  computed: {
    placeholderText() {
      if (this.currentRole === 'patient') return '请输入手机号';
      if (this.currentRole === 'doctor') return '请输入医生工号';
      return '请输入管理员账号';
    },
    indicatorOffset() {
      return this.roleIndex * 100;
    }
  },
  methods: {
    switchRole(roleKey, index) {
      this.currentRole = roleKey;
      this.roleIndex = index;
      this.formData.password = ''; // 切换角色清空密码
    },
    
    // --- 核心：真实登录逻辑 ---
    handleLogin() {
      // 1. 本地校验
      if (!this.formData.username || !this.formData.password) {
        uni.showToast({ title: '请输入账号和密码', icon: 'none' });
        return;
      }

      uni.showLoading({ title: '登录中...' });

      // 2. 发起网络请求
      // 注意：如果是真机调试，请将 localhost 换成你电脑的 IP 地址 (如 192.168.1.5)
      uni.request({
        url: 'http://localhost:8080/api/login', 
        method: 'POST',
        data: {
          role: this.currentRole,
          username: this.formData.username,
          password: this.formData.password
        },
        success: (res) => {
          uni.hideLoading();
          
          // 3. 处理后端返回结果
          if (res.data.code === 200) {
            // 登录成功
            uni.showToast({ title: '登录成功', icon: 'success' });
            
            const userInfo = res.data.data.userInfo;
            const token = res.data.data.token;
            
            // 建议：将 token 存入 Storage
            uni.setStorageSync('token', token);
            uni.setStorageSync('userInfo', userInfo);
            uni.setStorageSync('role', this.currentRole);

            // 4. 根据角色跳转不同主页
            let targetUrl = '';
            switch(this.currentRole) {
              case 'patient':
                targetUrl = '/pages/patient/home';
                break;
              case 'doctor':
                targetUrl = '/pages/doctor/home';
                break;
              case 'admin':
                targetUrl = '/pages/admin/home';
                break;
            }

            setTimeout(() => {
              uni.reLaunch({
                url: targetUrl,
                fail: (err) => {
                  console.error('跳转失败', err);
                  uni.showToast({ title: '页面未找到: ' + targetUrl, icon: 'none' });
                }
              });
            }, 1000);

          } else {
            // 登录失败 (后端返回 500 或其他错误码)
            uni.showToast({ 
              title: res.data.msg || '登录失败', 
              icon: 'none' 
            });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('网络请求失败', err);
          uni.showToast({ 
            title: '连接服务器失败，请检查后端是否启动', 
            icon: 'none',
            duration: 3000
          });
        }
      });
    },

    goToRegister() {
      if (this.currentRole !== 'patient') return;
      uni.navigateTo({
        url: '/pages/register/register',
        fail: () => {
          uni.showToast({ title: '请检查注册页路径', icon: 'none' });
        }
      });
    },
    
    goToForgotPassword() {
      console.log('点击忘记密码，当前角色:', this.currentRole);
      // 传递当前角色参数
      uni.navigateTo({
        url: `/pages/forgot-password/forgot-password?role=${this.currentRole}`,
        fail: (err) => {
          console.error(err);
          uni.showToast({ title: '跳转失败', icon: 'none' });
        }
      });
    }
  }
};
</script>

<style>
/* 全局容器：使用相对定位以便放置背景元素 */
.container {
  min-height: 100vh;
  background-color: #f8faff;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 0 40rpx;
}

/* 背景装饰圆：增加视觉层次感 */
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
  top: 20%;
  left: -150rpx;
  width: 300rpx;
  height: 300rpx;
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.1) 0%, rgba(79, 172, 254, 0.05) 100%);
  border-radius: 50%;
  z-index: 0;
}

/* 1. 头部区域 */
.header {
  margin-top: 120rpx;
  margin-bottom: 50rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 1;
}

.logo-box {
  position: relative;
  margin-bottom: 30rpx;
}

.logo-circle {
  width: 140rpx;
  height: 140rpx;
  background: linear-gradient(135deg, #2b86ff 0%, #2bdfff 100%);
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 30rpx rgba(43, 134, 255, 0.3);
  transform: rotate(-5deg);
}

.logo-text {
  font-size: 70rpx;
  color: #fff;
  font-weight: bold;
}

.title {
  font-size: 44rpx;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16rpx;
  letter-spacing: 2rpx;
}

.subtitle {
  font-size: 26rpx;
  color: #889aa4;
}

/* 2. 角色切换区域 */
.role-switch-container {
  width: 100%;
  padding: 0 20rpx;
  margin-bottom: 40rpx;
  z-index: 1;
  box-sizing: border-box;
}

.role-switch {
  position: relative;
  width: 100%;
  height: 90rpx;
  background-color: #eef2f6;
  border-radius: 24rpx;
  display: flex;
  padding: 8rpx;
  box-sizing: border-box;
}

/* 滑块动画指示器 */
.role-indicator {
  position: absolute;
  top: 8rpx;
  left: 8rpx;
  width: calc((100% - 16rpx) / 3);
  height: calc(100% - 16rpx);
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: transform 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
  z-index: 1;
}

.role-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  position: relative;
}

.role-label {
  font-size: 28rpx;
  color: #667;
  font-weight: 500;
  transition: color 0.3s;
}

.role-active .role-label {
  color: #2b86ff;
  font-weight: bold;
}

/* 3. 表单卡片区域 */
.form-card {
  width: 100%;
  background: #fff;
  border-radius: 40rpx;
  padding: 50rpx 40rpx;
  box-shadow: 0 20rpx 60rpx rgba(20, 20, 50, 0.06);
  z-index: 1;
  box-sizing: border-box;
}

.welcome-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 40rpx;
}

.input-group {
  display: flex;
  align-items: center;
  background-color: #f7f8fa;
  border-radius: 20rpx;
  height: 100rpx;
  padding: 0 30rpx;
  margin-bottom: 30rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.input-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
  color: #999;
}

.input {
  flex: 1;
  height: 100%;
  font-size: 30rpx;
  color: #333;
}

.placeholder-style {
  color: #bfbfbf;
}

.login-btn {
  width: 100%;
  height: 100rpx;
  line-height: 100rpx;
  background: linear-gradient(120deg, #2b86ff, #1ca0ff);
  color: #fff;
  font-size: 34rpx;
  font-weight: 600;
  border-radius: 50rpx;
  margin-top: 50rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 12rpx 24rpx rgba(43, 134, 255, 0.3);
  border: none;
}

.login-btn::after {
  border: none;
}

.login-btn-hover {
  transform: scale(0.98);
  opacity: 0.9;
}

/* 4. 底部链接 */
.footer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10rpx;
}

.action-link {
  font-size: 26rpx;
  padding: 10rpx;
  transition: opacity 0.3s;
}

.register {
  color: #2b86ff;
  font-weight: 500;
}

.forgot {
  color: #999;
}

/* 底部版权 */
.copyright {
  margin-top: auto;
  padding-bottom: 40rpx;
  text-align: center;
}

.copyright text {
  font-size: 22rpx;
  color: #ccc;
  letter-spacing: 4rpx;
}
</style>