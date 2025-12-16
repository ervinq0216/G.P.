package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Patient;
import com.hospital.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientMapper patientMapper;

    /**
     * 获取最新的患者信息 (用于刷新个人中心)
     */
    @GetMapping("/info/{id}")
    public Result<Patient> getInfo(@PathVariable Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient != null) {
            // 安全起见，不返回密码
            patient.setPassword(null);
            return Result.success(patient);
        }
        return Result.error("用户不存在");
    }

    /**
     * 更新患者个人信息
     */
    @PostMapping("/update")
    public Result<Patient> updateInfo(@RequestBody Patient patient) {
        if (patient.getId() == null) {
            return Result.error("用户ID不能为空");
        }

        // 执行更新 (只更新非空字段)
        int rows = patientMapper.updateById(patient);

        if (rows > 0) {
            // 更新成功后，查询最新数据返回给前端更新缓存
            Patient updatedPatient = patientMapper.selectById(patient.getId());
            updatedPatient.setPassword(null);
            return Result.success(updatedPatient);
        } else {
            return Result.error("更新失败");
        }
    }
}