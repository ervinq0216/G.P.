package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Appointment;
import com.hospital.entity.Schedule;
import com.hospital.mapper.AppointmentMapper;
import com.hospital.mapper.ScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/appointment")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @PostMapping("/book")
    @Transactional
    public synchronized Result<String> book(@RequestBody Appointment app) {
        Schedule s = scheduleMapper.selectById(app.getScheduleId());
        if (s == null) return Result.error("排班不存在");

        // --- 核心时间校验 ---
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
        // ------------------

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
}