<template>
	<view class="container">
		<view class="form-group">
			<view class="form-item">
				<text class="label">头像</text>
				<image :src="doctor.avatar || '/static/default_avatar.png'" class="avatar-preview" mode="aspectFill"></image>
			</view>
			<view class="form-item">
				<text class="label">姓名</text>
				<input class="input" v-model="doctor.realName" placeholder="请输入姓名" />
			</view>
			<view class="form-item">
				<text class="label">工号</text>
				<input class="input disabled" v-model="doctor.jobNumber" disabled />
			</view>
			<view class="form-item">
				<text class="label">性别</text>
				<picker mode="selector" :range="['男', '女']" @change="onGenderChange">
					<view class="picker-value">{{ doctor.gender || '请选择' }}</view>
				</picker>
			</view>
			<view class="form-item column">
				<text class="label">个人简介</text>
				<textarea class="textarea" v-model="doctor.introduction" placeholder="请输入您的专业擅长和简介..." />
			</view>
		</view>
		<button class="save-btn" @click="submit">保存修改</button>
	</view>
</template>

<script>
export default {
	data() {
		return {
			doctor: {
				id: null,
				realName: '',
				jobNumber: '',
				gender: '',
				introduction: '',
				avatar: ''
			}
		};
	},
	onLoad() {
		const user = uni.getStorageSync('userInfo');
		if (user) {
			this.doctor.id = user.id;
			this.fetchInfo();
		}
	},
	methods: {
		fetchInfo() {
			uni.showLoading({ title: '加载中' });
			uni.request({
				// 修复点：确保调用的是 doctor 接口而非 patient 接口
				url: 'http://localhost:8080/api/doctor/info/' + this.doctor.id,
				method: 'GET',
				success: (res) => {
					uni.hideLoading();
					if (res.data.code === 200) {
						this.doctor = res.data.data;
					}
				}
			});
		},
		onGenderChange(e) {
			const genders = ['男', '女'];
			this.doctor.gender = genders[e.detail.value];
		},
		submit() {
			if (!this.doctor.realName) return uni.showToast({ title: '姓名不能为空', icon: 'none' });
			
			uni.showLoading({ title: '保存中' });
			uni.request({
				url: 'http://localhost:8080/api/doctor/update',
				method: 'POST',
				data: this.doctor,
				success: (res) => {
					uni.hideLoading();
					if (res.data.code === 200) {
						uni.setStorageSync('userInfo', res.data.data);
						uni.showToast({ title: '保存成功' });
						setTimeout(() => uni.navigateBack(), 1000);
					}
				}
			});
		}
	}
};
</script>

<style>
.container { padding: 30rpx; background-color: #f8f9fb; min-height: 100vh; }
.form-group { background: #fff; border-radius: 20rpx; padding: 0 30rpx; }
.form-item { display: flex; justify-content: space-between; align-items: center; padding: 30rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.form-item.column { flex-direction: column; align-items: flex-start; }
.label { font-size: 30rpx; color: #333; }
.avatar-preview { width: 100rpx; height: 100rpx; border-radius: 50%; background: #eee; }
.input { text-align: right; font-size: 28rpx; flex: 1; margin-left: 30rpx; }
.disabled { color: #999; }
.picker-value { font-size: 28rpx; color: #333; }
.textarea { width: 100%; height: 200rpx; background: #f9f9f9; padding: 20rpx; border-radius: 12rpx; margin-top: 20rpx; font-size: 28rpx; box-sizing: border-box; }
.save-btn { margin-top: 60rpx; background: #2b86ff; color: #fff; border-radius: 45rpx; }
</style>