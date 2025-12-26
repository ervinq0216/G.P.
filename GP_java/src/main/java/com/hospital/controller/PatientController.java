package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.*;
import com.hospital.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 核心修复：获取患者资料，严格限定查询 t_patient 表
     */
    @GetMapping("/info/{id}")
    public Result<Patient> getInfo(@PathVariable Long id) {
        Patient p = patientMapper.selectById(id);
        if (p != null) {
            p.setPassword(null); // 安全脱敏
            return Result.success(p);
        }
        return Result.error("未找到患者资料");
    }

    @PostMapping("/update")
    public Result<Patient> updateInfo(@RequestBody Patient patient) {
        if (patient.getId() == null) return Result.error("ID不能为空");
        patientMapper.updateById(patient);
        Patient updated = patientMapper.selectById(patient.getId());
        if (updated != null) updated.setPassword(null);
        return Result.success(updated);
    }

    @GetMapping("/dept/list")
    public Result<List<Department>> listDepts() {
        return Result.success(deptMapper.selectList(null));
    }

    @GetMapping("/doctor/list")
    public Result<List<Doctor>> listDoctorsByDept(@RequestParam Long deptId) {
        return Result.success(doctorMapper.selectList(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDeptId, deptId)));
    }

    @GetMapping("/health-tips")
    public Result<List<Notice>> getHealthTips() {
        return Result.success(noticeMapper.selectList(new LambdaQueryWrapper<Notice>().eq(Notice::getType, "suggestion").orderByDesc(Notice::getCreatedTime).last("LIMIT 5")));
    }

    @GetMapping("/announcements")
    public Result<List<Notice>> getAnnouncements() {
        return Result.success(noticeMapper.selectList(new LambdaQueryWrapper<Notice>().eq(Notice::getType, "notice").and(w -> w.eq(Notice::getTargetType, "all").or().eq(Notice::getTargetType, "patient")).orderByDesc(Notice::getCreatedTime).last("LIMIT 5")));
    }
}