<template>
  <view class="container">
    
    <!-- 1. 顶部主导航 -->
    <scroll-view scroll-x class="nav-bar">
      <view class="nav-item" v-for="(nav, index) in navs" :key="index" 
            :class="{ active: currentNav === index }" 
            @click="switchNav(index)">
        {{ nav }}
      </view>
    </scroll-view>

    <!-- 内容区域 -->
    <view class="content-area">

      <!-- Module 1: 科室和医生 -->
      <view v-if="currentNav === 0" class="module-dept-doc">
        <view class="sub-tabs">
          <view :class="{ active: subTab1 === 0 }" @click="subTab1 = 0">科室管理</view>
          <view :class="{ active: subTab1 === 1 }" @click="subTab1 = 1">医生管理</view>
        </view>

        <!-- 科室管理 -->
        <view v-if="subTab1 === 0" class="panel">
          <view class="category-row" v-for="(cat, idx) in ['手术科室', '非手术科室', '诊断相关']" :key="idx">
            <view class="cat-header">
              <text class="cat-title">{{ cat }}</text>
              <button class="mini-btn" @click="openDeptModal('add', cat)">+ 新增</button>
            </view>
            <view class="dept-grid">
              <view class="dept-card" v-for="dept in getDeptsByCategory(cat)" :key="dept.id">
                <text class="dept-name">{{ dept.name }}</text>
                <view class="actions">
                  <text class="edit" @click="openDeptModal('edit', null, dept)">✎</text>
                  <text class="del" @click="deleteDept(dept.id)">×</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 医生管理 -->
        <view v-if="subTab1 === 1" class="panel">
          <view class="search-bar">
            <input class="search-input" v-model="searchKeyword" placeholder="输入工号或姓名" />
            <button class="search-btn" @click="fetchDoctors">搜索</button>
            <button class="add-btn" @click="showAddDoctorModal = true">+ 新增医生</button>
          </view>
          <scroll-view scroll-y class="list-scroll">
            <view class="list-item" v-for="doc in doctorList" :key="doc.id">
              <view class="info">
                <text class="name">{{ doc.realName }}</text>
                <text class="sub">{{ doc.jobNumber }} | {{ getDeptName(doc.deptId) }}</text>
              </view>
              <button class="mini-btn del" @click="deleteDoctor(doc.id)">删除</button>
            </view>
          </scroll-view>
        </view>
      </view>

      <!-- Module 2: 管理员管理 -->
      <view v-if="currentNav === 1" class="module-admin">
        <view class="panel">
          <button class="big-add-btn" @click="showAddAdminModal = true">+ 新增管理员</button>
          <scroll-view scroll-y class="list-scroll">
            <view class="list-item" v-for="adm in adminList" :key="adm.id">
              <view class="info">
                <text class="name">{{ adm.username }}</text>
                <text class="sub">系统管理员</text>
              </view>
              <button class="mini-btn del" @click="deleteAdmin(adm.id)" v-if="adm.id !== userInfo.id">删除</button>
            </view>
          </scroll-view>
        </view>
      </view>

      <!-- Module 3: 考勤和请假 -->
      <view v-if="currentNav === 2" class="module-attendance">
        <view class="sub-tabs">
          <view :class="{ active: subTab3 === 0 }" @click="subTab3 = 0">请假审批</view>
          <view :class="{ active: subTab3 === 1 }" @click="subTab3 = 1">考勤查询</view>
        </view>

        <!-- 请假审批 -->
        <view v-if="subTab3 === 0" class="panel">
          <scroll-view scroll-y class="list-scroll">
            <view class="list-item column" v-for="leave in leaveList" :key="leave.id">
              <view class="row-between">
                <text class="name">{{ leave.doctorName }}</text>
                <text class="tag">{{ leave.type }}</text>
              </view>
              <text class="desc">{{ leave.startDate }} 至 {{ leave.endDate }}</text>
              <text class="reason">理由：{{ leave.reason }}</text>
              <view class="btn-row" v-if="leave.status === 'pending'">
                <button class="audit-btn pass" @click="auditLeave(leave.id, 'approved')">批准</button>
                <button class="audit-btn reject" @click="auditLeave(leave.id, 'rejected')">拒绝</button>
              </view>
              <text class="status-text" v-else>{{ leave.status === 'approved' ? '已批准' : '已拒绝' }}</text>
            </view>
            <view v-if="leaveList.length === 0" class="empty">暂无待审批申请</view>
          </scroll-view>
        </view>

        <!-- 考勤查询 -->
        <view v-if="subTab3 === 1" class="panel">
          <view class="search-bar">
            <input class="search-input" placeholder="输入医生工号(功能开发中)" disabled />
            <button class="search-btn">查询</button>
          </view>
          <view class="empty">请先完善后端多条件考勤查询接口</view>
        </view>
      </view>

      <!-- Module 4: 通知和建议 (更新了通知对象) -->
      <view v-if="currentNav === 3" class="module-notice">
        <view class="sub-tabs">
          <view :class="{ active: subTab4 === 0 }" @click="switchNoticeTab(0)">通知管理</view>
          <view :class="{ active: subTab4 === 1 }" @click="switchNoticeTab(1)">健康建议</view>
        </view>

        <!-- 通知管理 -->
        <view v-if="subTab4 === 0" class="panel">
          <view class="form-box">
            <input class="input" v-model="noticeForm.title" placeholder="通知标题" />
            <textarea class="textarea" v-model="noticeForm.content" placeholder="通知内容..." />
            
            <!-- 发布对象选择 -->
            <view class="target-select">
              <text class="label">发布对象:</text>
              <picker mode="selector" :range="targetOptions" range-key="label" @change="onTargetTypeChange">
                <view class="picker-value">{{ getTargetLabel(noticeForm.targetType) }} <text class="arrow">▼</text></view>
              </picker>
            </view>
            
            <!-- 如果选择了特定科室，显示科室选择器 -->
            <view class="target-select" v-if="noticeForm.targetType === 'dept'">
              <text class="label">选择科室:</text>
              <picker mode="selector" :range="deptList" range-key="name" @change="onNoticeDeptChange">
                <view class="picker-value">{{ noticeForm.targetDeptName || '请选择科室' }} <text class="arrow">▼</text></view>
              </picker>
            </view>

            <button class="big-add-btn" @click="publishNotice('notice')">发布通知</button>
          </view>
          
          <view class="divider">已发布通知</view>
          <scroll-view scroll-y class="list-scroll half">
            <view class="list-item" v-for="n in noticeList" :key="n.id">
              <view class="info">
                <text class="name">{{ n.title }}</text>
                <text class="sub">对象: {{ getTargetLabel(n.targetType) }} | {{ formatDate(n.createdTime) }}</text>
              </view>
              <text class="del" @click="deleteNotice(n.id)">删除</text>
            </view>
          </scroll-view>
        </view>

        <!-- 健康建议 -->
        <view v-if="subTab4 === 1" class="panel">
          <view class="hint-text">发布的建议将同步到患者主页</view>
          <view class="form-box">
            <input class="input" v-model="suggestionForm.title" placeholder="建议标题 (如: 春季流感预防)" />
            <textarea class="textarea" v-model="suggestionForm.content" placeholder="建议详情内容..." />
            <button class="big-add-btn green" @click="publishNotice('suggestion')">发布建议</button>
          </view>
          
          <view class="divider">已发布建议</view>
          <scroll-view scroll-y class="list-scroll half">
            <view class="list-item" v-for="s in suggestionList" :key="s.id">
              <view class="info">
                <text class="name">{{ s.title }}</text>
                <text class="sub">{{ formatDate(s.createdTime) }}</text>
              </view>
              <text class="del" @click="deleteNotice(s.id)">删除</text>
            </view>
          </scroll-view>
        </view>
      </view>

      <!-- Module 5: 个人中心 -->
      <view v-if="currentNav === 4" class="module-profile">
        <view class="profile-card">
          <image src="/static/default_avatar.png" class="avatar"></image>
          <text class="name">{{ userInfo.username || '管理员' }}</text>
          <text class="role">系统管理员</text>
        </view>
        <view class="menu-list">
          <view class="menu-item" @click="goToPage('/pages/common/change-password')">
            <text>修改密码</text> <text>></text>
          </view>
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </view>
      </view>

    </view>

    <!-- 弹窗：新增/编辑科室 -->
    <view class="modal-mask" v-if="showDeptModal">
      <view class="modal-content">
        <view class="modal-title">{{ deptForm.id ? '编辑' : '新增' }}科室</view>
        <input class="modal-input" v-model="deptForm.name" placeholder="科室名称" />
        <textarea class="modal-textarea" v-model="deptForm.intro" placeholder="科室简介" />
        <view class="modal-btns">
          <button @click="showDeptModal = false">取消</button>
          <button class="primary" @click="saveDept">保存</button>
        </view>
      </view>
    </view>

    <!-- 弹窗：新增医生 -->
    <view class="modal-mask" v-if="showAddDoctorModal">
      <view class="modal-content">
        <view class="modal-title">新增医生</view>
        <input class="modal-input" v-model="doctorForm.realName" placeholder="真实姓名" />
        <input class="modal-input" v-model="doctorForm.jobNumber" placeholder="工号 (登录账号)" />
        <picker mode="selector" :range="deptList" range-key="name" @change="onDeptChange">
          <view class="picker-box">{{ selectedDeptName || '选择科室' }}</view>
        </picker>
        <view class="hint">默认密码: 123456</view>
        <view class="modal-btns">
          <button @click="showAddDoctorModal = false">取消</button>
          <button class="primary" @click="saveDoctor">保存</button>
        </view>
      </view>
    </view>

    <!-- 弹窗：新增管理员 -->
    <view class="modal-mask" v-if="showAddAdminModal">
      <view class="modal-content">
        <view class="modal-title">新增管理员</view>
        <input class="modal-input" v-model="adminForm.username" placeholder="用户名 (登录账号)" />
        <view class="hint">默认密码: 123456</view>
        <view class="modal-btns">
          <button @click="showAddAdminModal = false">取消</button>
          <button class="primary" @click="saveAdmin">保存</button>
        </view>
      </view>
    </view>

  </view>
</template>

<script>
export default {
  data() {
    return {
      navs: ['科室/医生', '管理员', '考勤请假', '通知建议', '个人中心'],
      currentNav: 0,
      subTab1: 0,
      subTab3: 0,
      subTab4: 0, // 通知建议的二级导航
      userInfo: {},

      // 数据列表
      deptList: [],
      doctorList: [],
      adminList: [],
      leaveList: [],
      noticeList: [],
      suggestionList: [],

      // 搜索与表单
      searchKeyword: '',
      
      showDeptModal: false,
      showAddDoctorModal: false,
      showAddAdminModal: false,

      deptForm: { id: null, category: '', name: '', intro: '' },
      doctorForm: { realName: '', jobNumber: '', deptId: null },
      adminForm: { username: '' },
      
      noticeForm: { title: '', content: '', targetType: 'all', targetDeptId: null, targetDeptName: '' },
      suggestionForm: { title: '', content: '' },
      
      selectedDeptName: '',
      
      // 目标类型选项 (已增加 "全部医生")
      targetOptions: [
        { label: '全部医生和患者', value: 'all' },
        { label: '全部医生', value: 'doctor' }, // <--- 新增项
        { label: '全部患者', value: 'patient' },
        { label: '特定科室医生', value: 'dept' }
      ]
    };
  },
  onShow() {
    this.userInfo = uni.getStorageSync('userInfo');
    this.loadData();
  },
  methods: {
    switchNav(index) {
      this.currentNav = index;
      this.loadData();
    },
    switchNoticeTab(index) {
      this.subTab4 = index;
      this.loadData();
    },
    loadData() {
      if (this.currentNav === 0) {
        this.fetchDepts();
        if (this.subTab1 === 1) this.fetchDoctors();
      } else if (this.currentNav === 1) {
        this.fetchAdmins();
      } else if (this.currentNav === 2) {
        this.fetchLeaves();
      } else if (this.currentNav === 3) {
        // 加载通知模块数据
        this.fetchDepts(); // 需要科室列表选择目标
        if (this.subTab4 === 0) this.fetchNoticesByType('notice');
        else this.fetchNoticesByType('suggestion');
      }
    },

    // --- API 调用 ---
    fetchDepts() {
      uni.request({ url: 'http://localhost:8080/api/admin/dept/list', success: r => this.deptList = r.data.data });
    },
    fetchDoctors() {
      uni.request({ 
        url: 'http://localhost:8080/api/admin/doctor/list', 
        data: { keyword: this.searchKeyword },
        success: r => this.doctorList = r.data.data 
      });
    },
    fetchAdmins() {
      uni.request({ url: 'http://localhost:8080/api/admin/list', success: r => this.adminList = r.data.data });
    },
    fetchLeaves() {
      uni.request({ url: 'http://localhost:8080/api/admin/leave/list', success: r => this.leaveList = r.data.data });
    },
    fetchNoticesByType(type) {
      uni.request({
        url: 'http://localhost:8080/api/admin/notice/list',
        data: { type: type },
        success: (res) => {
          if (type === 'notice') this.noticeList = res.data.data;
          else this.suggestionList = res.data.data;
        }
      });
    },

    // --- 业务逻辑 ---
    getTargetLabel(val) {
      const opt = this.targetOptions.find(o => o.value === val);
      return opt ? opt.label : '全部';
    },
    onTargetTypeChange(e) {
      this.noticeForm.targetType = this.targetOptions[e.detail.value].value;
    },
    onNoticeDeptChange(e) {
      const dept = this.deptList[e.detail.value];
      this.noticeForm.targetDeptId = dept.id;
      this.noticeForm.targetDeptName = dept.name;
    },
    publishNotice(type) {
      let data = {};
      if (type === 'notice') {
        data = { ...this.noticeForm, type: 'notice' };
        if (!data.title || !data.content) return uni.showToast({ title: '请填写完整', icon: 'none' });
        if (data.targetType === 'dept' && !data.targetDeptId) return uni.showToast({ title: '请选择科室', icon: 'none' });
      } else {
        data = { ...this.suggestionForm, type: 'suggestion', targetType: 'patient' };
        if (!data.title || !data.content) return uni.showToast({ title: '请填写完整', icon: 'none' });
      }
      uni.request({
        url: 'http://localhost:8080/api/admin/notice/save',
        method: 'POST',
        data: data,
        success: () => {
          if (type === 'notice') {
            this.noticeForm = { title: '', content: '', targetType: 'all', targetDeptId: null, targetDeptName: '' };
            this.fetchNoticesByType('notice');
          } else {
            this.suggestionForm = { title: '', content: '' };
            this.fetchNoticesByType('suggestion');
          }
          uni.showToast({ title: '发布成功' });
        }
      });
    },
    deleteNotice(id) {
      uni.showModal({
        title: '确认删除?',
        success: r => {
          if(r.confirm) {
            uni.request({
              url: `http://localhost:8080/api/admin/notice/delete/${id}`,
              method: 'POST',
              success: () => {
                this.fetchNoticesByType(this.subTab4 === 0 ? 'notice' : 'suggestion');
              }
            });
          }
        }
      });
    },

    getDeptsByCategory(cat) { return this.deptList.filter(d => d.category === cat); },
    openDeptModal(type, category, dept) {
      if (type === 'add') { this.deptForm = { id: null, category: category, name: '', intro: '' }; } 
      else { this.deptForm = { ...dept }; }
      this.showDeptModal = true;
    },
    saveDept() {
      uni.request({ url: 'http://localhost:8080/api/admin/dept/save', method: 'POST', data: this.deptForm, success: (res) => { if(res.data.code === 200) { this.showDeptModal = false; this.fetchDepts(); uni.showToast({ title: '保存成功' }); } } });
    },
    deleteDept(id) { uni.showModal({ title: '确认删除?', success: r => { if(r.confirm) { uni.request({ url: `http://localhost:8080/api/admin/dept/delete/${id}`, method: 'POST', success: () => this.fetchDepts() }); } } }); },

    onDeptChange(e) { const idx = e.detail.value; this.doctorForm.deptId = this.deptList[idx].id; this.selectedDeptName = this.deptList[idx].name; },
    saveDoctor() {
      uni.request({ url: 'http://localhost:8080/api/admin/doctor/add', method: 'POST', data: this.doctorForm, success: (res) => { if (res.data.code === 200) { this.showAddDoctorModal = false; this.doctorForm = { realName: '', jobNumber: '', deptId: null }; this.selectedDeptName = ''; this.fetchDoctors(); uni.showToast({ title: '添加成功' }); } else { uni.showToast({ title: res.data.msg, icon: 'none' }); } } });
    },
    deleteDoctor(id) { uni.showModal({ title: '确认删除?', success: r => { if(r.confirm) { uni.request({ url: `http://localhost:8080/api/admin/doctor/delete/${id}`, method: 'POST', success: () => this.fetchDoctors() }); } } }); },
    getDeptName(id) { const d = this.deptList.find(x => x.id === id); return d ? d.name : '未知科室'; },

    saveAdmin() {
      uni.request({ url: 'http://localhost:8080/api/admin/add', method: 'POST', data: this.adminForm, success: res => { if(res.data.code === 200) { this.showAddAdminModal = false; this.fetchAdmins(); uni.showToast({ title: '添加成功' }); } else { uni.showToast({ title: res.data.msg, icon: 'none' }); } } });
    },
    deleteAdmin(id) { uni.request({ url: `http://localhost:8080/api/admin/delete/${id}`, method: 'POST', success: () => this.fetchAdmins() }); },
    auditLeave(id, status) { uni.request({ url: 'http://localhost:8080/api/admin/leave/audit', method: 'POST', data: { id, status }, success: () => { this.fetchLeaves(); uni.showToast({ title: '已处理' }); } }); },

    formatDate(timeStr) { if(!timeStr) return ''; return timeStr.split('T')[0]; },
    goToPage(url) { uni.navigateTo({ url }); },
    handleLogout() {
      uni.removeStorageSync('userInfo');
      uni.reLaunch({ url: '/pages/login/index' });
    }
  }
};
</script>

<style>
/* 保持原有样式，省略部分以节省空间 */
.container { height: 100vh; display: flex; flex-direction: column; background-color: #f5f7fa; }
.nav-bar { white-space: nowrap; height: 90rpx; background: #fff; border-bottom: 1rpx solid #eee; }
.nav-item { display: inline-block; padding: 0 30rpx; line-height: 90rpx; font-size: 30rpx; color: #666; }
.nav-item.active { color: #2b86ff; font-weight: bold; border-bottom: 4rpx solid #2b86ff; }
.content-area { flex: 1; overflow: hidden; padding: 20rpx; display: flex; flex-direction: column; }
.sub-tabs { display: flex; margin-bottom: 20rpx; background: #fff; border-radius: 10rpx; overflow: hidden; }
.sub-tabs view { flex: 1; text-align: center; padding: 20rpx 0; font-size: 28rpx; background: #eee; }
.sub-tabs view.active { background: #2b86ff; color: #fff; }
.panel { background: #fff; border-radius: 16rpx; padding: 20rpx; flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.list-scroll { flex: 1; overflow-y: auto; height: 0; }
.list-scroll.half { height: 300rpx; flex: none; }
/* 科室管理 */
.category-row { margin-bottom: 30rpx; }
.cat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.cat-title { font-weight: bold; border-left: 6rpx solid #2b86ff; padding-left: 10rpx; }
.dept-grid { display: flex; flex-wrap: wrap; gap: 20rpx; }
.dept-card { width: 30%; background: #f8f9fb; padding: 16rpx; border-radius: 8rpx; display: flex; flex-direction: column; align-items: center; position: relative; }
.dept-name { font-size: 26rpx; text-align: center; }
.actions { position: absolute; top: 4rpx; right: 4rpx; display: flex; gap: 10rpx; }
.edit { color: #2b86ff; font-size: 24rpx; }
.del { color: #ff4d4f; font-size: 24rpx; }
/* 列表通用 */
.search-bar { display: flex; gap: 20rpx; margin-bottom: 20rpx; }
.search-input { flex: 1; background: #f5f5f5; height: 70rpx; padding: 0 20rpx; border-radius: 10rpx; }
.search-btn, .add-btn { height: 70rpx; line-height: 70rpx; font-size: 24rpx; padding: 0 20rpx; }
.add-btn { background: #52c41a; color: #fff; }
.list-item { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0; border-bottom: 1rpx solid #eee; }
.list-item.column { flex-direction: column; align-items: flex-start; }
.info { display: flex; flex-direction: column; }
.name { font-size: 30rpx; font-weight: bold; }
.sub { font-size: 24rpx; color: #999; }
.mini-btn { font-size: 22rpx; padding: 0 20rpx; height: 50rpx; line-height: 50rpx; margin: 0; }
.mini-btn.del { background: #ff4d4f; color: #fff; }
/* 考勤请假 */
.row-between { width: 100%; display: flex; justify-content: space-between; }
.tag { font-size: 24rpx; background: #e6f7ff; color: #2b86ff; padding: 2rpx 10rpx; border-radius: 6rpx; }
.desc { font-size: 26rpx; color: #666; margin: 10rpx 0; }
.btn-row { display: flex; gap: 20rpx; margin-top: 10rpx; }
.audit-btn { font-size: 24rpx; height: 50rpx; line-height: 50rpx; }
.pass { background: #52c41a; color: #fff; }
.reject { background: #ff4d4f; color: #fff; }
/* 通知 */
.form-box { display: flex; flex-direction: column; gap: 20rpx; }
.input { border: 1rpx solid #eee; height: 80rpx; padding: 0 20rpx; border-radius: 8rpx; }
.textarea { border: 1rpx solid #eee; height: 160rpx; padding: 20rpx; border-radius: 8rpx; width: 100%; box-sizing: border-box; }
.target-select { display: flex; align-items: center; font-size: 28rpx; color: #333; }
.target-select .label { margin-right: 20rpx; }
.picker-value { color: #2b86ff; border: 1rpx solid #2b86ff; padding: 4rpx 12rpx; border-radius: 8rpx; }
.big-add-btn { background: #2b86ff; color: #fff; width: 100%; margin-top: 20rpx; }
.big-add-btn.green { background: #52c41a; }
.divider { background: #f0f0f0; padding: 10rpx; text-align: center; color: #999; font-size: 24rpx; margin: 20rpx 0; }
.hint-text { color: #999; font-size: 24rpx; text-align: center; margin-bottom: 20rpx; }
/* 个人中心 */
.profile-card { display: flex; flex-direction: column; align-items: center; padding: 60rpx 0; }
.avatar { width: 140rpx; height: 140rpx; border-radius: 50%; margin-bottom: 20rpx; }
.role { font-size: 24rpx; background: #333; color: #ffd700; padding: 4rpx 16rpx; border-radius: 20rpx; margin-top: 10rpx; }
.menu-list { margin-top: 40rpx; }
.menu-item { display: flex; justify-content: space-between; padding: 30rpx; border-bottom: 1rpx solid #eee; font-size: 30rpx; }
.logout-btn { margin-top: 60rpx; background: #fff; color: #ff4d4f; }
/* 弹窗 */
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 999; display: flex; align-items: center; justify-content: center; }
.modal-content { width: 600rpx; background: #fff; border-radius: 16rpx; padding: 40rpx; }
.modal-title { font-size: 32rpx; font-weight: bold; text-align: center; margin-bottom: 30rpx; }
.modal-input { border: 1rpx solid #ddd; height: 80rpx; padding: 0 20rpx; margin-bottom: 20rpx; border-radius: 8rpx; }
.modal-textarea { border: 1rpx solid #ddd; height: 150rpx; padding: 20rpx; width: 100%; box-sizing: border-box; margin-bottom: 20rpx; }
.picker-box { border: 1rpx solid #ddd; height: 80rpx; line-height: 80rpx; padding: 0 20rpx; margin-bottom: 20rpx; color: #333; }
.hint { font-size: 24rpx; color: #999; margin-bottom: 30rpx; }
.modal-btns { display: flex; gap: 20rpx; }
.modal-btns button { flex: 1; font-size: 28rpx; }
.primary { background: #2b86ff; color: #fff; }
</style>