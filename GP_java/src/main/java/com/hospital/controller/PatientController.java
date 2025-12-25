package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.Notice;
import com.hospital.entity.Patient;
import com.hospital.mapper.NoticeMapper;
import com.hospital.mapper.PatientMapper;
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

    @PostMapping("/update")
    public Result<Patient> updateInfo(@RequestBody Patient patient) {
        if (patient.getId() == null) {
            return Result.error("用户ID不能为空，请重新登录");
        }
        try {
            int rows = patientMapper.updateById(patient);
            if (rows > 0) {
                Patient updatedPatient = patientMapper.selectById(patient.getId());
                if (updatedPatient != null) updatedPatient.setPassword(null);
                return Result.success(updatedPatient);
            } else {
                return Result.error("更新失败，未找到该用户");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("系统异常: " + e.getMessage());
        }
    }

    @GetMapping("/info/{id}")
    public Result<Patient> getInfo(@PathVariable Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient != null) {
            patient.setPassword(null);
            return Result.success(patient);
        }
        return Result.error("用户不存在");
    }

    /**
     * 获取健康建议 (同步管理员发布的 suggestion)
     */
    @GetMapping("/health-tips")
    public Result<List<Notice>> getHealthTips() {
        // 查询类型为 suggestion 的最新的5条记录
        List<Notice> list = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getType, "suggestion")
                .orderByDesc(Notice::getCreatedTime)
                .last("LIMIT 5"));
        return Result.success(list);
    }

    /**
     * 获取医院公告 (同步管理员发布的 notice 且 targetType 包含患者)
     */
    @GetMapping("/announcements")
    public Result<List<Notice>> getAnnouncements() {
        // 目标类型是 all 或者 patient
        List<Notice> list = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getType, "notice")
                .and(w -> w.eq(Notice::getTargetType, "all").or().eq(Notice::getTargetType, "patient"))
                .orderByDesc(Notice::getCreatedTime)
                .last("LIMIT 5"));
        return Result.success(list);
    }
}