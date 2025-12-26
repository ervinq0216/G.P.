<template>
	<view class="container">
		<!-- 顶部病人历史记录 -->
		<view class="history-section">
			<view class="section-title">历史就诊记录</view>
			<scroll-view scroll-y class="history-list">
				<view class="history-card" v-for="(item, index) in historyList" :key="index" @click="showHistoryDetail(item)">
					<view class="history-top">
						<text class="h-date">{{ formatDate(item.date) }}</text>
						<text class="h-dept">{{ item.deptName }}</text>
					</view>
					<view class="history-doc">接诊医生：{{ item.doctorName }}</view>
				</view>
				<view v-if="historyList.length === 0" class="empty">暂无历史就诊记录</view>
			</scroll-view>
		</view>

		<!-- 底部本次诊断输入 -->
		<view class="diagnosis-section">
			<view class="section-title">本次诊断 / 病例书写</view>
			<textarea class="diag-textarea" v-model="diagnosis" placeholder="请输入病人症状、诊断结果及医嘱..." />
			<button class="submit-btn" @click="submitDiagnosis">提交病例并完成就诊</button>
		</view>

		<!-- 历史病例弹窗 -->
		<view class="modal-mask" v-if="showModal" @click="showModal = false">
			<view class="modal-content" @click.stop>
				<view class="modal-title">历史病例详情</view>
				<view class="record-content">
					<text>{{ currentRecord.record }}</text>
				</view>
				<button class="modal-close" @click="showModal = false">关闭</button>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				appointmentId: null,
				patientId: null,
				historyList: [],
				diagnosis: '',
				showModal: false,
				currentRecord: {}
			};
		},
		onLoad(options) {
			this.appointmentId = options.appointmentId;
			this.patientId = options.patientId;
			this.fetchHistory();
		},
		methods: {
			fetchHistory() {
				uni.request({
					url: 'http://localhost:8080/api/appointment/history',
					data: { patientId: this.patientId },
					success: (res) => {
						if (res.data.code === 200) {
							// 过滤掉当前这次未完成的预约（如果有的话）
							this.historyList = res.data.data.filter(item => item.id != this.appointmentId);
						}
					}
				});
			},
			showHistoryDetail(item) {
				this.currentRecord = item;
				this.showModal = true;
			},
			submitDiagnosis() {
				if (!this.diagnosis.trim()) return uni.showToast({ title: '请填写病例信息', icon: 'none' });

				uni.showLoading({ title: '提交中' });
				uni.request({
					url: 'http://localhost:8080/api/appointment/diagnose',
					method: 'POST',
					data: {
						id: this.appointmentId,
						medicalRecord: this.diagnosis
					},
					success: (res) => {
						uni.hideLoading();
						if (res.data.code === 200) {
							uni.showToast({ title: '提交成功' });
							setTimeout(() => {
								uni.navigateBack(); // 返回主页
							}, 1500);
						} else {
							uni.showToast({ title: '提交失败', icon: 'none' });
						}
					}
				});
			},
			formatDate(str) {
				if (!str) return '';
				return str.split('T')[0];
			}
		}
	};
</script>

<style>
	.container { display: flex; flex-direction: column; height: 100vh; background-color: #f5f7fa; padding: 20rpx; }
	.section-title { font-size: 32rpx; font-weight: bold; border-left: 8rpx solid #2b86ff; padding-left: 15rpx; margin-bottom: 20rpx; color: #333; }
	
	.history-section { flex: 1; display: flex; flex-direction: column; overflow: hidden; margin-bottom: 20rpx; }
	.history-list { flex: 1; overflow-y: auto; height: 0; }
	.history-card { background: #fff; padding: 30rpx; border-radius: 16rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.02); }
	.history-top { display: flex; justify-content: space-between; margin-bottom: 10rpx; }
	.h-date { font-weight: bold; font-size: 30rpx; color: #333; }
	.h-dept { font-size: 26rpx; color: #2b86ff; background: #e6f1ff; padding: 2rpx 10rpx; border-radius: 8rpx; }
	.history-doc { font-size: 26rpx; color: #666; }
	
	.diagnosis-section { background: #fff; padding: 30rpx; border-radius: 20rpx; box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05); }
	.diag-textarea { width: 100%; height: 300rpx; background: #f9f9f9; padding: 20rpx; border-radius: 16rpx; margin-bottom: 30rpx; font-size: 28rpx; box-sizing: border-box; }
	.submit-btn { background: #2b86ff; color: #fff; border-radius: 50rpx; }
	
	.empty { text-align: center; color: #999; margin-top: 100rpx; }
	
	/* 弹窗 */
	.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); z-index: 99; display: flex; align-items: center; justify-content: center; }
	.modal-content { width: 600rpx; background: #fff; border-radius: 24rpx; padding: 40rpx; max-height: 70vh; display: flex; flex-direction: column; }
	.modal-title { font-size: 34rpx; font-weight: bold; text-align: center; margin-bottom: 30rpx; }
	.record-content { flex: 1; overflow-y: auto; font-size: 28rpx; color: #333; line-height: 1.6; margin-bottom: 30rpx; min-height: 200rpx; }
	.modal-close { background: #f5f5f5; color: #666; border-radius: 40rpx; font-size: 28rpx; }
</style>