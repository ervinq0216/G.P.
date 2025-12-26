package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.*;
import com.hospital.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointment")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private DepartmentMapper departmentMapper;

    /**
     * 预约挂号 (患者提交)
     */
    @PostMapping("/book")
    @Transactional
    public synchronized Result<String> book(@RequestBody Appointment app) {
        Schedule s = scheduleMapper.selectById(app.getScheduleId());
        if (s == null) return Result.error("排班不存在");

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        if (s.getWorkDate().equals(today)) {
            if ("上午".equals(s.getPeriod()) && nowTime.getHour() >= 10) return Result.error("今日上午预约已截止");
            if ("下午".equals(s.getPeriod()) && nowTime.getHour() >= 17) return Result.error("今日下午预约已截止");
        }

        if ("leave".equals(s.getStatus())) return Result.error("医生请假");
        if (s.getBookedCount() >= s.getTotalQuota()) return Result.error("预约已满");

        s.setBookedCount(s.getBookedCount() + 1);
        scheduleMapper.updateById(s);

        app.setDoctorId(s.getDoctorId());
        app.setStatus("booked");
        app.setCreatedTime(LocalDateTime.now());
        appointmentMapper.insert(app);

        return Result.success("预约成功");
    }

    /**
     * 医生查看某天的预约列表
     */
    @GetMapping("/doctor-list")
    public Result<List<Map<String, Object>>> getDoctorAppointments(@RequestParam Long doctorId, @RequestParam String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            List<Schedule> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                    .eq(Schedule::getDoctorId, doctorId).eq(Schedule::getWorkDate, date));

            if (schedules.isEmpty()) return Result.success(new ArrayList<>());

            List<Long> scheduleIds = schedules.stream().map(Schedule::getId).toList();
            List<Appointment> apps = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                    .in(Appointment::getScheduleId, scheduleIds).eq(Appointment::getStatus, "booked").orderByAsc(Appointment::getCreatedTime));

            List<Map<String, Object>> result = new ArrayList<>();
            for (Appointment a : apps) {
                Map<String, Object> map = new HashMap<>();
                Patient p = patientMapper.selectById(a.getPatientId());
                Schedule s = schedules.stream().filter(item -> item.getId().equals(a.getScheduleId())).findFirst().orElse(null);

                map.put("id", a.getId());
                map.put("patientId", a.getPatientId()); // 关键：新增patientId供跳转使用
                map.put("patientName", p != null ? p.getRealName() : "未知");
                map.put("patientPhone", p != null ? p.getPhone() : "-");
                map.put("patientGender", p != null ? p.getGender() : "");
                map.put("period", s != null ? s.getPeriod() : "");
                map.put("bookTime", a.getCreatedTime());
                result.add(map);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询异常");
        }
    }

    /**
     * 患者查看自己的挂号单
     */
    @GetMapping("/patient-list")
    public Result<List<Map<String, Object>>> getPatientAppointments(@RequestParam Long patientId) {
        List<Appointment> apps = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getPatientId, patientId).orderByDesc(Appointment::getCreatedTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Appointment a : apps) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("status", a.getStatus());
            map.put("medicalRecord", a.getMedicalRecord()); // 返回病例信息
            map.put("createTime", a.getCreatedTime());
            Schedule s = scheduleMapper.selectById(a.getScheduleId());
            if (s != null) {
                map.put("workDate", s.getWorkDate());
                map.put("period", s.getPeriod());
                Doctor d = doctorMapper.selectById(s.getDoctorId());
                if (d != null) {
                    map.put("doctorName", d.getRealName());
                    map.put("doctorAvatar", d.getAvatar());
                    Department dept = departmentMapper.selectById(d.getDeptId());
                    map.put("deptName", dept != null ? dept.getName() : "未知科室");
                }
            }
            result.add(map);
        }
        return Result.success(result);
    }

    /**
     * 获取患者的历史病例 (不包含当前未完成的预约)
     */
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getPatientHistory(@RequestParam Long patientId) {
        // 查询该患者所有已完成(completed)的预约
        List<Appointment> apps = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getPatientId, patientId)
                .isNotNull(Appointment::getMedicalRecord) // 必须有病例
                .orderByDesc(Appointment::getCreatedTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Appointment a : apps) {
            Map<String, Object> map = new HashMap<>();
            Schedule s = scheduleMapper.selectById(a.getScheduleId());
            Doctor d = doctorMapper.selectById(a.getDoctorId());
            Department dept = null;
            if (d != null) dept = departmentMapper.selectById(d.getDeptId());

            map.put("id", a.getId());
            map.put("date", s != null ? s.getWorkDate() : "");
            map.put("deptName", dept != null ? dept.getName() : "未知");
            map.put("doctorName", d != null ? d.getRealName() : "未知");
            map.put("record", a.getMedicalRecord());
            result.add(map);
        }
        return Result.success(result);
    }

    /**
     * 医生提交病例 (诊断)
     */
    @PostMapping("/diagnose")
    public Result<String> submitDiagnosis(@RequestBody Appointment app) {
        if (app.getId() == null || app.getMedicalRecord() == null) {
            return Result.error("信息不完整");
        }

        Appointment existing = appointmentMapper.selectById(app.getId());
        if (existing == null) return Result.error("预约不存在");

        existing.setMedicalRecord(app.getMedicalRecord());
        existing.setStatus("completed"); // 标记为已完成/已就诊
        appointmentMapper.updateById(existing);

        return Result.success("病例提交成功");
    }
}