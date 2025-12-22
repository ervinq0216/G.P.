package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Doctor;
import com.hospital.mapper.DoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorMapper doctorMapper;

    @GetMapping("/info/{id}")
    public Result<Doctor> getInfo(@PathVariable Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor != null) {
            // 返回密码用于前端判断是否为 123456 (仅用于此处逻辑，实际生产应通过标志位)
            // doctor.setPassword(null);
            return Result.success(doctor);
        }
        return Result.error("医生不存在");
    }

    @PostMapping("/update")
    public Result<Doctor> updateInfo(@RequestBody Doctor doctor) {
        if (doctor.getId() == null) return Result.error("ID不能为空");
        doctorMapper.updateById(doctor);
        return Result.success(doctor);
    }
}