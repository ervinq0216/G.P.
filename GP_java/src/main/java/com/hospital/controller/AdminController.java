package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.*;
import com.hospital.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private DepartmentMapper deptMapper;
    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private LeaveMapper leaveMapper;
    @Autowired
    private NoticeMapper noticeMapper;

    // ================= 科室管理 =================
    @GetMapping("/dept/list")
    public Result<List<Department>> listDept() {
        return Result.success(deptMapper.selectList(null));
    }

    @PostMapping("/dept/save")
    public Result<String> saveDept(@RequestBody Department dept) {
        if (dept.getId() == null) {
            dept.setCreatedTime(LocalDateTime.now());
            deptMapper.insert(dept);
        } else {
            deptMapper.updateById(dept);
        }
        return Result.success("保存成功");
    }

    @PostMapping("/dept/delete/{id}")
    public Result<String> deleteDept(@PathVariable Long id) {
        deptMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // ================= 医生管理 =================
    @GetMapping("/doctor/list")
    public Result<List<Doctor>> listDoctors(@RequestParam(required = false) Long deptId,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        if (deptId != null) wrapper.eq(Doctor::getDeptId, deptId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Doctor::getRealName, keyword)
                    .or().like(Doctor::getJobNumber, keyword));
        }
        List<Doctor> list = doctorMapper.selectList(wrapper);
        return Result.success(list);
    }

    @PostMapping("/doctor/add")
    public Result<String> addDoctor(@RequestBody Doctor doctor) {
        Long count = doctorMapper.selectCount(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getJobNumber, doctor.getJobNumber()));
        if (count > 0) return Result.error("工号已存在");

        doctor.setPassword("123456");
        doctor.setCreatedTime(LocalDateTime.now());
        doctorMapper.insert(doctor);
        return Result.success("添加成功");
    }

    @PostMapping("/doctor/delete/{id}")
    public Result<String> deleteDoctor(@PathVariable Long id) {
        doctorMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // ================= 管理员管理 =================
    @GetMapping("/list")
    public Result<List<Admin>> listAdmins() {
        return Result.success(adminMapper.selectList(null));
    }

    @PostMapping("/add")
    public Result<String> addAdmin(@RequestBody Admin admin) {
        Long count = adminMapper.selectCount(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, admin.getUsername()));
        if (count > 0) return Result.error("用户名已存在");

        admin.setPassword("123456");
        admin.setCreatedTime(LocalDateTime.now());
        adminMapper.insert(admin);
        return Result.success("添加成功");
    }

    @PostMapping("/delete/{id}")
    public Result<String> deleteAdmin(@PathVariable Long id) {
        adminMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // ================= 考勤与请假 =================
    @GetMapping("/leave/list")
    public Result<List<Leave>> listLeaves(@RequestParam(defaultValue = "pending") String status) {
        List<Leave> leaves = leaveMapper.selectList(new LambdaQueryWrapper<Leave>()
                .eq(Leave::getStatus, status)
                .orderByDesc(Leave::getCreatedTime));

        leaves.forEach(l -> {
            Doctor d = doctorMapper.selectById(l.getDoctorId());
            if(d != null) l.setDoctorName(d.getRealName());
        });
        return Result.success(leaves);
    }

    @PostMapping("/leave/audit")
    public Result<String> auditLeave(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String status = params.get("status").toString();

        Leave leave = new Leave();
        leave.setId(id);
        leave.setStatus(status);
        leaveMapper.updateById(leave);
        return Result.success("审批完成");
    }

    // ================= 通知与建议 (更新部分) =================

    /**
     * 获取通知列表 (支持按类型筛选)
     * type: notice (通知管理) / suggestion (健康建议)
     */
    @GetMapping("/notice/list")
    public Result<List<Notice>> listNotices(@RequestParam(required = false) String type) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notice::getType, type);
        }
        wrapper.orderByDesc(Notice::getCreatedTime);
        return Result.success(noticeMapper.selectList(wrapper));
    }

    @PostMapping("/notice/save")
    public Result<String> saveNotice(@RequestBody Notice notice) {
        if (notice.getId() == null) {
            notice.setCreatedTime(LocalDateTime.now());
            noticeMapper.insert(notice);
        } else {
            noticeMapper.updateById(notice);
        }
        return Result.success("发布成功");
    }

    @PostMapping("/notice/delete/{id}")
    public Result<String> deleteNotice(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.success("删除成功");
    }
}