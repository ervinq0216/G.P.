package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.Appointment;
import com.hospital.entity.Patient;
import com.hospital.entity.Schedule;
import com.hospital.mapper.AppointmentMapper;
import com.hospital.mapper.PatientMapper;
import com.hospital.mapper.ScheduleMapper;
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

    /**
     * 患者提交预约
     */
    @PostMapping("/book")
    @Transactional
    public synchronized Result<String> book(@RequestBody Appointment app) {
        Schedule s = scheduleMapper.selectById(app.getScheduleId());
        if (s == null) return Result.error("排班不存在");

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        if (s.getWorkDate().equals(today)) {
            if ("上午".equals(s.getPeriod()) && nowTime.getHour() >= 10) {
                return Result.error("抱歉，今日上午预约已于10:00截止");
            }
            if ("下午".equals(s.getPeriod()) && nowTime.getHour() >= 17) {
                return Result.error("抱歉，今日下午预约已于17:00截止");
            }
        }

        if ("leave".equals(s.getStatus())) return Result.error("医生该时段请假，无法预约");
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
     * 医生查看预约列表
     * @param doctorId 医生ID
     * @param dateStr 日期字符串 (yyyy-MM-dd)
     */
    @GetMapping("/doctor-list")
    public Result<List<Map<String, Object>>> getDoctorAppointments(
            @RequestParam Long doctorId,
            @RequestParam String dateStr) {

        LocalDate date = LocalDate.parse(dateStr);

        // 1. 获取该医生当天的排班计划（上午和下午两条记录）
        List<Schedule> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, doctorId)
                .eq(Schedule::getWorkDate, date));

        if (schedules.isEmpty()) return Result.success(new ArrayList<>());

        List<Long> scheduleIds = schedules.stream().map(Schedule::getId).toList();

        // 2. 查询这些排班对应的所有“已预约”记录
        List<Appointment> apps = appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .in(Appointment::getScheduleId, scheduleIds)
                .eq(Appointment::getStatus, "booked")
                .orderByAsc(Appointment::getCreatedTime));

        // 3. 组装数据并关联患者姓名、性别、电话
        List<Map<String, Object>> result = new ArrayList<>();
        for (Appointment a : apps) {
            Map<String, Object> map = new HashMap<>();
            Patient p = patientMapper.selectById(a.getPatientId());
            // 匹配属于上午还是下午
            Schedule s = schedules.stream().filter(item -> item.getId().equals(a.getScheduleId())).findFirst().orElse(null);

            map.put("id", a.getId());
            map.put("patientName", p != null ? p.getRealName() : "未知患者");
            map.put("patientPhone", p != null ? p.getPhone() : "-");
            map.put("patientGender", p != null ? p.getGender() : "未知");
            map.put("period", s != null ? s.getPeriod() : "未知");
            map.put("bookTime", a.getCreatedTime());
            result.add(map);
        }

        return Result.success(result);
    }
}