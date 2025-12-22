package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.dto.ChangePasswordDTO;
import com.hospital.dto.LoginDTO;
import com.hospital.dto.RegisterDTO;
import com.hospital.dto.ResetPasswordDTO;
import com.hospital.entity.Admin;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.mapper.AdminMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 负责处理登录、注册、重置密码、修改密码等核心安全业务
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 允许前端跨域访问
public class AuthController {

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private AdminMapper adminMapper;

    // ==================== 1. 登录接口 ====================
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        String role = loginDTO.getRole();
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        if (role == null) return Result.error("角色不能为空");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("role", role);

        // 1.1 患者登录
        if ("patient".equals(role)) {
            Patient patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                    .eq(Patient::getPhone, username));

            if (patient == null || !patient.getPassword().equals(password)) {
                return Result.error("手机号或密码错误");
            }
            responseData.put("userInfo", patient);
            responseData.put("token", "token-patient-" + patient.getId());
            return Result.success(responseData);
        }

        // 1.2 医生登录
        else if ("doctor".equals(role)) {
            Doctor doctor = doctorMapper.selectOne(new LambdaQueryWrapper<Doctor>()
                    .eq(Doctor::getJobNumber, username));

            if (doctor == null || !doctor.getPassword().equals(password)) {
                return Result.error("工号或密码错误");
            }
            responseData.put("userInfo", doctor);
            responseData.put("token", "token-doctor-" + doctor.getId());
            return Result.success(responseData);
        }

        // 1.3 管理员登录
        else if ("admin".equals(role)) {
            Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                    .eq(Admin::getUsername, username));

            if (admin == null || !admin.getPassword().equals(password)) {
                return Result.error("账号或密码错误");
            }
            responseData.put("userInfo", admin);
            responseData.put("token", "token-admin-" + admin.getId());
            return Result.success(responseData);
        }

        return Result.error("未知角色类型");
    }

    // ==================== 2. 注册接口 (仅患者) ====================
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO registerDTO) {
        String phone = registerDTO.getPhone();
        String password = registerDTO.getPassword();
        String realName = registerDTO.getRealName();

        if (phone == null || phone.isEmpty()) return Result.error("手机号不能为空");
        if (password == null || password.isEmpty()) return Result.error("密码不能为空");
        if (realName == null || realName.isEmpty()) return Result.error("姓名不能为空");

        Long count = patientMapper.selectCount(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getPhone, phone));
        if (count > 0) {
            return Result.error("该手机号已注册，请直接登录");
        }

        Patient newPatient = new Patient();
        newPatient.setPhone(phone);
        newPatient.setPassword(password);
        newPatient.setRealName(realName);
        newPatient.setCreatedTime(LocalDateTime.now());

        try {
            patientMapper.insert(newPatient);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("注册失败，数据库保存异常");
        }

        return Result.success("注册成功");
    }

    // ==================== 3. 重置密码接口 (忘记密码) ====================
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody ResetPasswordDTO resetDTO) {
        String role = resetDTO.getRole();
        String account = resetDTO.getAccount();
        String name = resetDTO.getName();
        String newPassword = resetDTO.getNewPassword();

        if (role == null || account == null || name == null || newPassword == null) {
            return Result.error("信息填写不完整");
        }

        // 3.1 患者重置
        if ("patient".equals(role)) {
            Patient patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                    .eq(Patient::getPhone, account)
                    .eq(Patient::getRealName, name));

            if (patient == null) {
                return Result.error("身份验证失败：手机号与姓名不匹配");
            }
            patient.setPassword(newPassword);
            patientMapper.updateById(patient);
            return Result.success("密码重置成功");
        }

        // 3.2 医生重置
        else if ("doctor".equals(role)) {
            Doctor doctor = doctorMapper.selectOne(new LambdaQueryWrapper<Doctor>()
                    .eq(Doctor::getJobNumber, account)
                    .eq(Doctor::getRealName, name));

            if (doctor == null) {
                return Result.error("身份验证失败：工号与姓名不匹配");
            }
            doctor.setPassword(newPassword);
            doctorMapper.updateById(doctor);
            return Result.success("密码重置成功");
        }

        // 3.3 管理员重置
        else if ("admin".equals(role)) {
            Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                    .eq(Admin::getUsername, account)
                    .eq(Admin::getRealName, name));

            if (admin == null) {
                return Result.error("身份验证失败：账号与姓名不匹配");
            }
            admin.setPassword(newPassword);
            adminMapper.updateById(admin);
            return Result.success("密码重置成功");
        }

        return Result.error("未知角色类型");
    }

    // ==================== 4. 修改密码接口 (登录后修改) ====================
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody ChangePasswordDTO changeDTO) {
        String role = changeDTO.getRole();
        Long id = changeDTO.getId();
        String oldPassword = changeDTO.getOldPassword();
        String newPassword = changeDTO.getNewPassword();

        if (role == null || id == null || oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }

        // 4.1 患者修改
        if ("patient".equals(role)) {
            Patient patient = patientMapper.selectById(id);
            if (patient == null) return Result.error("用户不存在");

            if (!patient.getPassword().equals(oldPassword)) {
                return Result.error("旧密码错误");
            }
            patient.setPassword(newPassword);
            patientMapper.updateById(patient);
            return Result.success("修改成功");
        }

        // 4.2 医生修改
        else if ("doctor".equals(role)) {
            Doctor doctor = doctorMapper.selectById(id);
            if (doctor == null) return Result.error("用户不存在");

            if (!doctor.getPassword().equals(oldPassword)) {
                return Result.error("旧密码错误");
            }
            doctor.setPassword(newPassword);
            doctorMapper.updateById(doctor);
            return Result.success("修改成功");
        }

        // 4.3 管理员修改
        else if ("admin".equals(role)) {
            Admin admin = adminMapper.selectById(id);
            if (admin == null) return Result.error("用户不存在");

            if (!admin.getPassword().equals(oldPassword)) {
                return Result.error("旧密码错误");
            }
            admin.setPassword(newPassword);
            adminMapper.updateById(admin);
            return Result.success("修改成功");
        }

        return Result.error("未知角色类型");
    }
}