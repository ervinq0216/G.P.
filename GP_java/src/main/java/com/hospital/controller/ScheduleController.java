package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.Schedule;
import com.hospital.mapper.ScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * 获取医生未来5个工作日的排班
     */
    @GetMapping("/list")
    public Result<List<Schedule>> listDoctorSchedules(@RequestParam Long doctorId) {
        LocalDate today = LocalDate.now();
        List<Schedule> result = new ArrayList<>();

        // 循环查找未来5个工作日（跳过周六周日）
        int count = 0;
        LocalDate date = today;

        while (count < 5) {
            date = date.plusDays(1); // 从明天开始
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue; // 跳过周末
            }

            // 检查数据库是否已有排班，没有则自动创建默认排班
            checkAndCreateSchedule(doctorId, date, "上午");
            checkAndCreateSchedule(doctorId, date, "下午");
            count++;
        }

        // 查询刚才生成或已存在的排班
        // 这里的逻辑稍微简化，直接查询未来一段日期的
        LocalDate endDate = date;
        result = scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, doctorId)
                .gt(Schedule::getWorkDate, today)
                .le(Schedule::getWorkDate, endDate)
                .orderByAsc(Schedule::getWorkDate));

        return Result.success(result);
    }

    private void checkAndCreateSchedule(Long doctorId, LocalDate date, String period) {
        Schedule s = scheduleMapper.selectOne(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, doctorId)
                .eq(Schedule::getWorkDate, date)
                .eq(Schedule::getPeriod, period));

        if (s == null) {
            Schedule newSchedule = new Schedule();
            newSchedule.setDoctorId(doctorId);
            newSchedule.setWorkDate(date);
            newSchedule.setPeriod(period);
            newSchedule.setTotalQuota(30);
            newSchedule.setBookedCount(0);
            newSchedule.setStatus("available");
            scheduleMapper.insert(newSchedule);
        }
    }
}