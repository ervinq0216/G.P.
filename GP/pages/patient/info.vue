<template>
  <view class="container">
    <view class="form-list">
      
      <!-- 头像 -->
      <view class="form-item avatar-item" @click="chooseAvatar">
        <text class="label">头像</text>
        <view class="right-content">
          <image 
            :src="userInfo.avatar || '/static/default_avatar.png'" 
            class="avatar-img"
            mode="aspectFill"
          ></image>
          <text class="arrow">></text>
        </view>
      </view>

      <!-- 姓名 (只读) -->
      <view class="form-item">
        <text class="label">姓名</text>
        <text class="value disabled">{{ userInfo.realName }}</text>
      </view>

      <!-- 性别 -->
      <view class="form-item">
        <text class="label">性别</text>
        <picker 
          :range="genders" 
          @change="onGenderChange"
        >
          <view class="picker-value">
            {{ userInfo.gender || '请选择' }} <text class="arrow">></text>
          </view>
        </picker>
      </view>

      <!-- 出生日期 (选择后自动计算年龄) -->
      <view class="form-item">
        <text class="label">出生日期</text>
        <picker 
          mode="date" 
          :value="userInfo.birthday" 
          start="1900-01-01" 
          :end="today" 
          @change="onDateChange"
        >
          <view class="picker-value">
            {{ userInfo.birthday || '请选择' }} <text class="arrow">></text>
          </view>
        </picker>
      </view>

      <!-- 年龄 (自动计算) -->
      <view class="form-item" v-if="age !== null">
        <text class="label">年龄</text>
        <text class="value">{{ age }} 岁</text>
      </view>

      <!-- 身高 -->
      <view class="form-item">
        <text class="label">身高 (cm)</text>
        <input 
          class="input" 
          type="number" 
          v-model="userInfo.height" 
          placeholder="请输入身高" 
          style="text-align: right;"
        />
      </view>

      <!-- 体重 -->
      <view class="form-item">
        <text class="label">体重 (kg)</text>
        <input 
          class="input" 
          type="number" 
          v-model="userInfo.weight" 
          placeholder="请输入体重" 
          style="text-align: right;"
        />
      </view>

      <!-- 血型 -->
      <view class="form-item">
        <text class="label">血型</text>
        <picker 
          :range="bloodTypes" 
          @change="onBloodTypeChange"
        >
          <view class="picker-value">
            {{ userInfo.bloodType || '请选择' }} <text class="arrow">></text>
          </view>
        </picker>
      </view>

    </view>

    <button class="save-btn" @click="saveInfo">保存修改</button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      userInfo: {
        id: null,
        realName: '',
        avatar: '',
        gender: '',
        birthday: '',
        height: '',
        weight: '',
        bloodType: ''
      },
      genders: ['男', '女'],
      bloodTypes: ['A', 'B', 'AB', 'O', 'Rh+'],
      today: new Date().toISOString().split('T')[0]
    };
  },
  computed: {
    age() {
      if (!this.userInfo.birthday) return null;
      const birth = new Date(this.userInfo.birthday);
      const now = new Date();
      let age = now.getFullYear() - birth.getFullYear();
      const m = now.getMonth() - birth.getMonth();
      if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) {
        age--;
      }
      return age;
    }
  },
  onLoad() {
    // 从缓存加载当前用户信息
    const cache = uni.getStorageSync('userInfo');
    if (cache) {
      // 复制数据，防止直接修改缓存
      this.userInfo = { ...this.userInfo, ...cache };
    }
  },
  methods: {
    // 切换性别
    onGenderChange(e) {
      this.userInfo.gender = this.genders[e.detail.value];
    },
    // 切换生日
    onDateChange(e) {
      this.userInfo.birthday = e.detail.value;
    },
    // 切换血型
    onBloodTypeChange(e) {
      this.userInfo.bloodType = this.bloodTypes[e.detail.value];
    },
    
    // 选择头像 (兼容 H5 和 小程序)
    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'], // 压缩图
        success: (res) => {
          const filePath = res.tempFilePaths[0];
          
          // --- 兼容性处理开始 ---
          
          // #ifdef MP-WEIXIN
          // 微信小程序端：使用 FileSystemManager
          uni.getFileSystemManager().readFile({
            filePath: filePath,
            encoding: 'base64',
            success: (readRes) => {
              this.userInfo.avatar = 'data:image/png;base64,' + readRes.data;
            },
            fail: (err) => {
              console.error('小程序读取失败', err);
              uni.showToast({ title: '图片读取失败', icon: 'none' });
            }
          });
          // #endif

          // #ifndef MP-WEIXIN
          // H5 或 App 端：使用 uni.request 获取 ArrayBuffer 再转换
          uni.request({
            url: filePath,
            method: 'GET',
            responseType: 'arraybuffer',
            success: (fileRes) => {
              const base64 = uni.arrayBufferToBase64(fileRes.data);
              // 注意：为了简单起见，这里统一假设前缀为 png，实际大多情况通用
              this.userInfo.avatar = 'data:image/png;base64,' + base64;
            },
            fail: (err) => {
              console.error('H5读取失败', err);
              uni.showToast({ title: '图片转换失败', icon: 'none' });
            }
          });
          // #endif
          
          // --- 兼容性处理结束 ---
        }
      });
    },

    // 保存信息
    saveInfo() {
      uni.showLoading({ title: '保存中...' });
      
      uni.request({
        url: 'http://localhost:8080/api/patient/update',
        method: 'POST',
        data: this.userInfo,
        success: (res) => {
          uni.hideLoading();
          if (res.data.code === 200) {
            uni.showToast({ title: '保存成功' });
            
            // 更新本地缓存
            uni.setStorageSync('userInfo', res.data.data);
            
            // 延迟返回上一页
            setTimeout(() => {
              uni.navigateBack();
            }, 1000);
          } else {
            uni.showToast({ title: res.data.msg || '保存失败', icon: 'none' });
          }
        },
        fail: (err) => {
          uni.hideLoading();
          console.error(err);
          uni.showToast({ title: '网络错误', icon: 'none' });
        }
      });
    }
  }
};
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-top: 20rpx;
}

.form-list {
  background-color: #fff;
  padding: 0 30rpx;
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 110rpx;
  border-bottom: 1rpx solid #eee;
}

.form-item:last-child {
  border-bottom: none;
}

.avatar-item {
  height: 160rpx;
}

.label {
  font-size: 30rpx;
  color: #333;
}

.right-content, .picker-value {
  display: flex;
  align-items: center;
  font-size: 30rpx;
  color: #666;
}

.avatar-img {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: #eee;
  margin-right: 16rpx;
}

.value {
  font-size: 30rpx;
  color: #333;
  text-align: right;
}

.value.disabled {
  color: #999;
}

.input {
  text-align: right;
  font-size: 30rpx;
  color: #333;
}

.arrow {
  color: #ccc;
  margin-left: 10rpx;
  font-size: 28rpx;
}

.save-btn {
  width: 90%;
  height: 90rpx;
  line-height: 90rpx;
  background: linear-gradient(120deg, #2b86ff, #1ca0ff);
  color: #fff;
  border-radius: 45rpx;
  margin: 60rpx auto;
  font-size: 32rpx;
}
</style>