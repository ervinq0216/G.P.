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

    @GetMapping("/info/{id}")
    public Result<Patient> getInfo(@PathVariable Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient != null) {
            patient.setPassword(null);
            return Result.success(patient);
        }
        return Result.error("用户不存在");
    }

    // --- 获取真实科室列表 ---
    @GetMapping("/dept/list")
    public Result<List<Department>> listDepts() {
        return Result.success(deptMapper.selectList(null));
    }

    // --- 核心修复：按科室ID查询医生 ---
    @GetMapping("/doctor/list")
    public Result<List<Doctor>> listDoctorsByDept(@RequestParam Long deptId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getDeptId, deptId); // 严格过滤科室ID
        List<Doctor> list = doctorMapper.selectList(wrapper);
        // 隐藏敏感信息
        list.forEach(d -> d.setPassword(null));
        return Result.success(list);
    }

    @GetMapping("/health-tips")
    public Result<List<Notice>> getHealthTips() {
        return Result.success(noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getType, "suggestion").orderByDesc(Notice::getCreatedTime).last("LIMIT 5")));
    }

    @GetMapping("/announcements")
    public Result<List<Notice>> getAnnouncements() {
        return Result.success(noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getType, "notice")
                .and(w -> w.eq(Notice::getTargetType, "all").or().eq(Notice::getTargetType, "patient"))
                .orderByDesc(Notice::getCreatedTime).last("LIMIT 5")));
    }
}