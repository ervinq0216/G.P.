package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.*;
import com.hospital.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端业务控制器
 * 负责个人信息管理、科室与医生查询、公告同步
 */
@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private DepartmentMapper deptMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    // ==================== 1. 个人中心 ====================

    @GetMapping("/info/{id}")
    public Result<Patient> getInfo(@PathVariable Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient != null) {
            patient.setPassword(null); // 屏蔽密码安全返回
            return Result.success(patient);
        }
        return Result.error("用户不存在");
    }

    @PostMapping("/update")
    public Result<Patient> updateInfo(@RequestBody Patient patient) {
        if (patient.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        try {
            int rows = patientMapper.updateById(patient);
            if (rows > 0) {
                Patient updated = patientMapper.selectById(patient.getId());
                if (updated != null) updated.setPassword(null);
                return Result.success(updated);
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("系统异常: " + e.getMessage());
        }
    }

    // ==================== 2. 科室与医生 (核心修复) ====================

    /**
     * 获取所有真实科室列表
     */
    @GetMapping("/dept/list")
    public Result<List<Department>> listDepts() {
        return Result.success(deptMapper.selectList(null));
    }

    /**
     * 根据科室ID过滤医生
     * 修复了张三显示在所有科室的问题：增加 deptId 过滤条件
     */
    @GetMapping("/doctor/list")
    public Result<List<Doctor>> listDoctorsByDept(@RequestParam Long deptId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        // 关键修复：严格匹配科室ID
        wrapper.eq(Doctor::getDeptId, deptId);

        List<Doctor> list = doctorMapper.selectList(wrapper);
        // 隐藏密码字段
        list.forEach(d -> d.setPassword(null));
        return Result.success(list);
    }

    // ==================== 3. 公告与建议 ====================

    @GetMapping("/health-tips")
    public Result<List<Notice>> getHealthTips() {
        return Result.success(noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getType, "suggestion")
                .orderByDesc(Notice::getCreatedTime)
                .last("LIMIT 5")));
    }

    @GetMapping("/announcements")
    public Result<List<Notice>> getAnnouncements() {
        return Result.success(noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getType, "notice")
                .and(w -> w.eq(Notice::getTargetType, "all")
                        .or()
                        .eq(Notice::getTargetType, "patient"))
                .orderByDesc(Notice::getCreatedTime)
                .last("LIMIT 5")));
    }
}