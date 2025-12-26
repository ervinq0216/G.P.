<template>
	<view class="container">
		<!-- 顶部 Tab 导航 -->
		<view class="nav-tabs">
			<view v-for="(tab, index) in tabs" :key="index" class="tab-item" :class="{ active: currentTab === index }"
				@click="handleTabSwitch(index)">
				<text class="tab-text">{{ tab }}</text>
				<view class="tab-line" v-if="currentTab === index"></view>
			</view>
		</view>

		<view class="content-area">
			<!-- Module A: 医院简介 -->
			<scroll-view scroll-y class="module-intro" v-if="currentTab === 0">
				<image src="/static/hospital_banner.png" mode="aspectFill" class="banner-img" @error="imageError"></image>
				
				<!-- 医院概况 -->
				<view class="card info-card">
					<view class="card-header">
						<view class="header-line"></view>
						<text class="header-title">医院概况</text>
					</view>
					<text class="intro-text">
						XX市第一人民医院始建于1950年，是一所集医疗、教学、科研、预防、保健、康复为一体的三级甲等综合性医院。医院拥有先进的医疗设备和技术精湛的专家团队。
					</text>
				</view>

				<!-- 医院公告 -->
				<view class="card list-card">
					<view class="card-header">
						<view class="header-line"></view>
						<text class="header-title">医院公告</text>
						<text class="more-link">查看更多</text>
					</view>
					<view class="list-container">
						<view v-for="(item, index) in announcements" :key="index" class="list-item" @click="showDetail(item)">
							<view class="dot"></view>
							<text class="item-title">{{ item.title }}</text>
							<text class="item-date">{{ formatDate(item.createdTime) }}</text>
						</view>
					</view>
				</view>

				<!-- 健康建议 -->
				<view class="card list-card">
					<view class="card-header">
						<view class="header-line green"></view>
						<text class="header-title">健康建议</text>
						<text class="more-link">查看更多</text>
					</view>
					<view class="list-container">
						<view v-for="(item, index) in healthTips" :key="index" class="list-item" @click="showDetail(item)">
							<view class="dot green-dot"></view>
							<text class="item-title">{{ item.title }}</text>
						</view>
					</view>
				</view>
				<view style="height: 40rpx;"></view>
			</scroll-view>

			<!-- Module B: 科室导航 (修复张三显示问题) -->
			<view class="module-dept" v-if="currentTab === 1">
				<!-- 左侧分类：按要求排序 -->
				<scroll-view scroll-y class="dept-sidebar">
					<view v-for="(cat, index) in categoryOrder" :key="index" class="sidebar-item" 
						:class="{ 'sidebar-active': currentCategoryIndex === index }" 
						@click="currentCategoryIndex = index">
						{{ cat }}
					</view>
				</scroll-view>

				<!-- 右侧科室列表 -->
				<scroll-view scroll-y class="dept-content">
					<view class="dept-grid-title">{{ categoryOrder[currentCategoryIndex] }}</view>
					<view class="dept-grid">
						<!-- 动态过滤当前分类下的科室 -->
						<view v-for="dept in filteredDepts" :key="dept.id" class="dept-box" @click="openDeptModal(dept)">
							<text class="dept-name">{{ dept.name }}</text>
						</view>
					</view>
					<view v-if="filteredDepts.length === 0" class="empty-tip">暂无科室数据</view>
				</scroll-view>
			</view>

			<!-- Module C: AI 咨询 (已有的AI逻辑) -->
			<view class="module-ai-chat" v-if="currentTab === 2">
				<scroll-view scroll-y class="chat-history" :scroll-top="scrollTop" :scroll-with-animation="true">
					<view class="chat-item ai">
						<view class="avatar ai-avatar">AI</view>
						<view class="bubble ai-bubble"><text>您好！我是您的智能健康助手。请描述您的症状，我将为您提供建议。</text></view>
					</view>
					<view v-for="(msg, index) in chatList" :key="index" class="chat-item" :class="msg.role">
						<view class="avatar" :class="msg.role + '-avatar'">{{ msg.role === 'user' ? '我' : 'AI' }}</view>
						<view class="bubble" :class="msg.role + '-bubble'"><text selectable>{{ msg.content }}</text></view>
					</view>
					<view class="chat-item ai" v-if="isAiLoading">
						<view class="avatar ai-avatar">AI</view>
						<view class="bubble ai-bubble loading-bubble"><view class="dot-loading">...</view></view>
					</view>
					<view style="height: 120rpx;"></view>
				</scroll-view>
				<view class="chat-input-area">
					<input class="chat-input" v-model="inputMessage" placeholder="请输入您的症状..." confirm-type="send" @confirm="sendMessage" />
					<button class="send-btn" :disabled="isAiLoading || !inputMessage.trim()" @click="sendMessage">发送</button>
				</view>
			</view>

			<!-- Module D: 个人中心 -->
			<view class="module-profile" v-if="currentTab === 3">
							<view class="profile-header">
								<view class="profile-bg-circle"></view>
								<view class="user-info-box" @click="goToInfo">
									<image :src="userInfo.avatar || '/static/default_avatar.png'" class="user-avatar-img" mode="aspectFill"></image>
									<view class="user-text">
										<text class="user-name">{{ userInfo.realName || '患者用户' }}</text>
										<text class="user-phone">{{ userInfo.phone || '账号未绑定' }}</text>
									</view>
									<text class="edit-hint">编辑 ></text>
								</view>
							</view>
							
							<view class="menu-list">
								<view class="menu-item" @click="goToInfo">
									<view class="menu-left"><text class="menu-icon">👤</text><text class="menu-title">个人信息</text></view>
									<text class="menu-arrow">></text>
								</view>
								
								<!-- 补齐修改密码按钮 -->
								<view class="menu-item" @click="goToPage('/pages/common/change-password')">
									<view class="menu-left"><text class="menu-icon">🔒</text><text class="menu-title">修改密码</text></view>
									<text class="menu-arrow">></text>
								</view>
			
								<view class="menu-item" @click="showToast('挂号历史正在开发中')">
									<view class="menu-left"><text class="menu-icon">📋</text><text class="menu-title">我的挂号单</text></view>
									<text class="menu-arrow">></text>
								</view>
							</view>
							
							<button class="logout-btn-large" @click="handleLogout">退出登录</button>
						</view>
		</view>

		<!-- 弹窗：科室详情 & 医生列表 -->
		<view class="modal-mask" v-if="showDeptModal" @click="showDeptModal = false">
			<view class="modal-content dept-modal" @click.stop>
				<view class="modal-header">
					<text class="modal-title">{{ selectedDept.name }}</text>
					<text class="close-btn" @click="showDeptModal = false">×</text>
				</view>
				<view class="dept-intro-section">
					<text class="label">科室简介</text>
					<text class="intro-text">{{ selectedDept.intro || '该科室暂无简介。' }}</text>
				</view>
				<view class="doc-list-title">科室医生</view>
				<scroll-view scroll-y class="doc-scroll">
					<view class="doctor-item" v-for="doc in deptDoctors" :key="doc.id" @click="goToDoctorDetail(doc.id)">
						<image :src="doc.avatar || '/static/default_avatar.png'" class="doc-avatar" mode="aspectFill"></image>
						<view class="doc-right">
							<text class="doc-name">{{ doc.realName }}</text>
							<text class="doc-hint">点击预约挂号</text>
						</view>
						<text class="arrow">></text>
					</view>
					<view v-if="deptDoctors.length === 0" class="empty-doc">该科室暂无在职医生</view>
				</scroll-view>
			</view>
		</view>

		<!-- AI 咨询免责声明弹窗 -->
		<view class="modal-mask" v-if="showAIModal">
			<view class="modal-content">
				<view class="modal-header"><text class="modal-title">⚠️ 免责声明</text></view>
				<view class="modal-body">
					<text>AI 生成的内容基于大模型算法，仅供参考，不作为最终医疗诊断结果。</text>
					<text class="highlight">危重病人请立即拨打120或前往医院急诊就诊！</text>
				</view>
				<button class="modal-btn" @click="confirmAI">确认并继续</button>
			</view>
		</view>

	</view>
</template>

<script>
export default {
	data() {
		return {
			tabs: ['医院简介', '科室导航', 'AI 咨询', '个人中心'],
			currentTab: 0,
			userInfo: {},
			allDepts: [],
			categoryOrder: ['手术科室', '非手术科室', '诊断相关'],
			currentCategoryIndex: 0,
			showDeptModal: false,
			selectedDept: {},
			deptDoctors: [],
			announcements: [],
			healthTips: [],
			showAIModal: false,
			aiConfirmed: false,
			inputMessage: '',
			chatList: [],
			isAiLoading: false,
			scrollTop: 0
		};
	},
	computed: {
		filteredDepts() {
			const currentCatName = this.categoryOrder[this.currentCategoryIndex];
			return this.allDepts.filter(d => d.category === currentCatName);
		}
	},
	onShow() {
		const cachedUser = uni.getStorageSync('userInfo');
		const role = uni.getStorageSync('role');
		
		// === 核心修复：防止串号 ===
		if (cachedUser && role === 'patient') {
			this.userInfo = cachedUser;
			// 只有角色正确，才去请求数据
			this.fetchPatientInfo(); // 重新拉取患者表中的信息，覆盖缓存
			this.fetchAnnouncements();
			this.fetchHealthTips();
			this.fetchDepts();
		} else {
			// 如果没有缓存，或者角色不对（例如上一次是医生登录），立即清理并踢出
			uni.clearStorageSync();
			uni.reLaunch({ url: '/pages/login/index' });
		}
	},
	methods: {
		fetchPatientInfo() {
			uni.request({
				// 强制指定患者 API
				url: 'http://localhost:8080/api/patient/info/' + this.userInfo.id,
				success: (res) => {
					if (res.data.code === 200) {
						this.userInfo = res.data.data;
						// 安全更新缓存
						const safeUser = { ...res.data.data };
						delete safeUser.avatar; // 不存大头像到缓存
						uni.setStorageSync('userInfo', safeUser);
					}
				}
			});
		},
		handleTabSwitch(index) {
			if (index === 2 && !this.aiConfirmed) { this.showAIModal = true; } 
			else { this.currentTab = index; if (index === 2) this.scrollToBottom(); }
		},
		goToPage(url) { uni.navigateTo({ url }); },
		goToInfo() { uni.navigateTo({ url: '/pages/patient/info' }); },
		handleLogout() {
			uni.showModal({
				title: '提示',
				content: '确定要退出登录吗？',
				success: (res) => {
					if (res.confirm) {
						uni.clearStorageSync(); // 彻底清理缓存
						uni.reLaunch({ url: '/pages/login/index' });
					}
				}
			});
		},
			// 获取真实科室列表
			fetchDepts() {
				uni.request({
					url: 'http://localhost:8080/api/patient/dept/list',
					success: (res) => {
						if (res.data.code === 200) {
							this.allDepts = res.data.data;
						}
					}
				});
			},

			// 点击具体科室，获取该科室下属医生并弹窗
			openDeptModal(dept) {
				this.selectedDept = dept;
				this.deptDoctors = [];
				this.showDeptModal = true;
				
				uni.showLoading({ title: '加载中' });
				uni.request({
					url: 'http://localhost:8080/api/patient/doctor/list',
					data: { deptId: dept.id }, // 传递科室ID进行精确匹配
					success: (res) => {
						uni.hideLoading();
						if (res.data.code === 200) {
							this.deptDoctors = res.data.data;
						}
					},
					fail: () => uni.hideLoading()
				});
			},

			// 跳转医生预约详情
			goToDoctorDetail(docId) {
				this.showDeptModal = false;
				uni.navigateTo({
					url: `/pages/patient/doctor-detail?id=${docId}`
				});
			},

			// --- 基础公共方法 ---
			handleTabSwitch(index) {
				if (index === 2 && !this.aiConfirmed) {
					this.showAIModal = true;
				} else {
					this.currentTab = index;
					if (index === 2) this.scrollToBottom();
				}
			},
			confirmAI() {
				this.showAIModal = false;
				this.aiConfirmed = true;
				this.currentTab = 2;
				this.scrollToBottom();
			},
			fetchAnnouncements() {
				uni.request({
					url: 'http://localhost:8080/api/patient/announcements',
					success: (res) => { if (res.data.code === 200) this.announcements = res.data.data; }
				});
			},
			fetchHealthTips() {
				uni.request({
					url: 'http://localhost:8080/api/patient/health-tips',
					success: (res) => { if (res.data.code === 200) this.healthTips = res.data.data; }
				});
			},
			sendMessage() {
				const msg = this.inputMessage.trim();
				if (!msg) return;
				this.chatList.push({ role: 'user', content: msg });
				this.inputMessage = '';
				this.isAiLoading = true;
				this.scrollToBottom();

				uni.request({
					url: 'http://localhost:8080/api/ai/chat',
					method: 'POST',
					data: { message: msg },
					success: (res) => {
						if (res.data.code === 200) {
							let aiContent = '';
							try {
								const deepSeekRes = JSON.parse(res.data.data);
								aiContent = deepSeekRes.choices?.[0]?.message?.content || 'AI 暂时无法回答。';
							} catch (e) {
								aiContent = res.data.data || '解析错误。';
							}
							this.chatList.push({ role: 'ai', content: aiContent });
						} else {
							this.chatList.push({ role: 'ai', content: '服务出错：' + res.data.msg });
						}
					},
					complete: () => {
						this.isAiLoading = false;
						this.scrollToBottom();
					}
				});
			},
			scrollToBottom() {
				setTimeout(() => { this.scrollTop = 99999; this.$nextTick(() => { this.scrollTop += 1; }); }, 100);
			},
			formatDate(str) { return str ? str.split('T')[0] : ''; },
			showDetail(item) {
				uni.showModal({ title: item.title, content: item.content, showCancel: false, confirmText: '知道了' });
			},
			goToInfo() { uni.navigateTo({ url: '/pages/patient/info' }); },
			handleLogout() {
				uni.showModal({
					title: '提示',
					content: '确定要退出登录吗？',
					success: (res) => {
						if (res.confirm) {
							uni.clearStorageSync();
							uni.reLaunch({ url: '/pages/login/index' });
						}
					}
				});
			},
			showToast(msg) { uni.showToast({ title: msg, icon: 'none' }); },
			imageError() { console.log('图片加载失败'); }
		}
	};
</script>

<style>
	/* 布局与容器 */
	.container { height: 100vh; display: flex; flex-direction: column; background-color: #f5f7fa; }
	.nav-tabs { height: 90rpx; display: flex; background: #fff; box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05); z-index: 10; flex-shrink: 0; }
	.tab-item { flex: 1; display: flex; align-items: center; justify-content: center; position: relative; }
	.tab-text { font-size: 28rpx; color: #666; }
	.active .tab-text { color: #2b86ff; font-weight: bold; font-size: 30rpx; }
	.tab-line { position: absolute; bottom: 10rpx; width: 40rpx; height: 6rpx; background: #2b86ff; border-radius: 3rpx; }
	.content-area { flex: 1; overflow: hidden; position: relative; }

	/* 模块 A: 医院简介 */
	.module-intro { height: 100%; }
	.banner-img { width: 100%; height: 300rpx; background: #eee; }
	.card { background: #fff; margin: 20rpx; border-radius: 16rpx; padding: 30rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02); }
	.card-header { display: flex; align-items: center; margin-bottom: 20rpx; justify-content: space-between; }
	.header-line { width: 8rpx; height: 32rpx; background: #2b86ff; border-radius: 4rpx; margin-right: 16rpx; }
	.header-line.green { background: #42b983; }
	.header-title { font-size: 32rpx; font-weight: bold; color: #333; flex: 1; }
	.more-link { font-size: 24rpx; color: #999; }
	.intro-text { font-size: 28rpx; color: #666; line-height: 1.6; }
	.list-item { display: flex; align-items: center; padding: 20rpx 0; border-bottom: 1rpx dashed #f0f0f0; }
	.dot { width: 12rpx; height: 12rpx; background: #2b86ff; border-radius: 50%; margin-right: 16rpx; }
	.green-dot { background: #42b983; }
	.item-title { flex: 1; font-size: 28rpx; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

	/* 模块 B: 科室导航 (左右联动风格) */
	.module-dept { height: 100%; display: flex; }
	.dept-sidebar { width: 220rpx; background: #f8f9fb; height: 100%; }
	.sidebar-item { height: 110rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #777; }
	.sidebar-active { background: #fff; color: #2b86ff; font-weight: bold; border-left: 8rpx solid #2b86ff; }
	.dept-content { flex: 1; background: #fff; padding: 30rpx; }
	.dept-grid-title { font-size: 32rpx; font-weight: bold; margin-bottom: 30rpx; color: #333; }
	.dept-grid { display: flex; flex-wrap: wrap; gap: 20rpx; }
	.dept-box { width: 47%; height: 90rpx; background: #f0f7ff; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; }
	.dept-name { font-size: 26rpx; color: #333; }

	/* 弹窗样式 */
	.dept-modal { width: 660rpx; max-height: 85vh; padding: 40rpx; border-radius: 30rpx; display: flex; flex-direction: column; }
	.dept-info-section { background: #f9f9f9; padding: 20rpx; border-radius: 16rpx; margin-bottom: 30rpx; }
	.label { font-size: 24rpx; color: #999; display: block; margin-bottom: 8rpx; }
	.intro-text { font-size: 26rpx; color: #666; line-height: 1.6; }
	.doc-list-title { font-size: 30rpx; font-weight: bold; margin-bottom: 20rpx; border-left: 8rpx solid #2b86ff; padding-left: 15rpx; }
	.doc-scroll { flex: 1; height: 500rpx; }
	.doctor-item { display: flex; align-items: center; padding: 24rpx; border-bottom: 1rpx solid #f0f0f0; }
	.doc-avatar { width: 100rpx; height: 100rpx; border-radius: 50%; margin-right: 24rpx; background: #eee; }
	.doc-right { flex: 1; }
	.doc-name { font-size: 32rpx; font-weight: bold; color: #333; margin-bottom: 6rpx; display: block; }
	.doc-hint { font-size: 22rpx; color: #2b86ff; }
	.empty-doc { text-align: center; color: #ccc; padding: 60rpx; font-size: 26rpx; }

	/* 模块 C: AI 咨询 */
	.module-ai-chat { flex: 1; display: flex; flex-direction: column; background-color: #f5f7fa; height: 100%; }
	.chat-history { flex: 1; padding: 20rpx; box-sizing: border-box; }
	.chat-item { display: flex; margin-bottom: 30rpx; }
	.chat-item.user { flex-direction: row-reverse; }
	.avatar { width: 80rpx; height: 80rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #fff; flex-shrink: 0; }
	.ai-avatar { background: linear-gradient(135deg, #2b86ff, #2bdfff); margin-right: 20rpx; }
	.user-avatar { background-color: #ff9800; margin-left: 20rpx; }
	.bubble { max-width: 70%; padding: 20rpx 24rpx; border-radius: 16rpx; font-size: 30rpx; line-height: 1.5; }
	.ai-bubble { background: #fff; color: #333; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05); }
	.user-bubble { background: #2b86ff; color: #fff; box-shadow: 0 2rpx 8rpx rgba(43, 134, 255, 0.3); }
	.chat-input-area { height: 110rpx; background: #fff; border-top: 1rpx solid #eee; display: flex; align-items: center; padding: 0 20rpx; }
	.chat-input { flex: 1; height: 75rpx; background: #f5f7fa; border-radius: 40rpx; padding: 0 30rpx; font-size: 28rpx; margin-right: 20rpx; }
	.send-btn { width: 130rpx; height: 75rpx; line-height: 75rpx; background: #2b86ff; color: #fff; font-size: 28rpx; border-radius: 40rpx; }

	/* 模块 D: 个人中心 */
	.module-profile { padding: 30rpx; background: #f5f7fa; height: 100%; }
	.profile-header { background: #fff; border-radius: 20rpx; padding: 50rpx 30rpx; margin-bottom: 30rpx; display: flex; align-items: center; position: relative; overflow: hidden; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03); }
	.user-avatar-img { width: 130rpx; height: 130rpx; border-radius: 50%; background: #f0f0f0; margin-right: 30rpx; border: 4rpx solid #fff; }
	.user-name { font-size: 38rpx; font-weight: bold; color: #333; margin-bottom: 8rpx; display: block; }
	.user-phone { font-size: 26rpx; color: #999; }
	.menu-list { background: #fff; border-radius: 20rpx; padding: 0 30rpx; margin-bottom: 60rpx; }
	.menu-item { display: flex; justify-content: space-between; align-items: center; height: 110rpx; border-bottom: 1rpx solid #f5f5f5; }
	.menu-icon { font-size: 40rpx; margin-right: 20rpx; }
	.logout-btn-large { background: #fff; color: #ff4d4f; height: 95rpx; line-height: 95rpx; border-radius: 50rpx; font-size: 32rpx; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05); }

	/* 通用弹窗 */
	.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); z-index: 999; display: flex; align-items: center; justify-content: center; }
	.modal-content { width: 620rpx; background: #fff; border-radius: 30rpx; padding: 50rpx 40rpx; }
	.modal-title { font-size: 36rpx; font-weight: bold; color: #333; flex: 1; text-align: center; }
	.close-btn { font-size: 44rpx; color: #ccc; }
	.modal-body { margin-top: 30rpx; font-size: 28rpx; color: #666; line-height: 1.6; }
	.highlight { color: #ff4d4f; font-weight: bold; display: block; margin-top: 20rpx; }
	.modal-btn { margin-top: 40rpx; background: #2b86ff; color: #fff; border-radius: 50rpx; }
</style>