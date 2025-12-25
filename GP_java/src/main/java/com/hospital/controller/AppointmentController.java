package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Appointment;
import com.hospital.entity.Schedule;
import com.hospital.mapper.AppointmentMapper;
import com.hospital.mapper.ScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/appointment")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * 提交预约
     */
    @PostMapping("/book")
    @Transactional
    public synchronized Result<String> book(@RequestBody Appointment appointment) {
        if (appointment.getPatientId() == null || appointment.getScheduleId() == null) {
            return Result.error("参数不完整");
        }

        // 1. 检查排班是否存在及剩余号源
        Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule == null) return Result.error("排班不存在");

        if ("leave".equals(schedule.getStatus())) {
            return Result.error("医生该时段已请假");
        }

        if (schedule.getBookedCount() >= schedule.getTotalQuota()) {
            return Result.error("号源已满");
        }

        // 2. 更新排班已预约数
        schedule.setBookedCount(schedule.getBookedCount() + 1);
        scheduleMapper.updateById(schedule);

        // 3. 创建预约记录
        appointment.setDoctorId(schedule.getDoctorId());
        appointment.setStatus("booked");
        appointment.setCreatedTime(LocalDateTime.now());
        appointmentMapper.insert(appointment);

        return Result.success("预约成功");
    }
}