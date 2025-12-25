package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.Leave;
import com.hospital.entity.Schedule;
import com.hospital.mapper.LeaveMapper;
import com.hospital.mapper.ScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private LeaveMapper leaveMapper;

    /**
     * 获取医生预约排班列表
     * 规则：包含今天在内的最近5个工作日
     */
    @GetMapping("/list")
    public Result<List<Schedule>> getDoctorSchedules(@RequestParam Long doctorId) {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        List<LocalDate> workingDays = new ArrayList<>();
        LocalDate checkDate = today; // 从今天开始检查

        // 1. 计算包含今天在内的未来5个工作日
        while (workingDays.size() < 5) {
            if (checkDate.getDayOfWeek() != DayOfWeek.SATURDAY && checkDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays.add(checkDate);
            }
            checkDate = checkDate.plusDays(1);
        }

        // 2. 确保排班记录存在
        for (LocalDate date : workingDays) {
            ensureScheduleExists(doctorId, date, "上午");
            ensureScheduleExists(doctorId, date, "下午");
        }

        // 3. 查询排班
        List<Schedule> list = scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, doctorId)
                .in(Schedule::getWorkDate, workingDays)
                .orderByAsc(Schedule::getWorkDate, Schedule::getId));

        // 4. 处理“当天”的截止逻辑
        for (Schedule s : list) {
            if (s.getWorkDate().equals(today)) {
                // 上午10点后不可预约当天上午
                if ("上午".equals(s.getPeriod()) && nowTime.getHour() >= 10) {
                    s.setStatus("expired"); // 标记为已过期/截止
                }
                // 下午17点后不可预约当天下午
                if ("下午".equals(s.getPeriod()) && nowTime.getHour() >= 17) {
                    s.setStatus("expired");
                }
            }
        }

        return Result.success(list);
    }

    private void ensureScheduleExists(Long doctorId, LocalDate date, String period) {
        Schedule s = scheduleMapper.selectOne(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, doctorId)
                .eq(Schedule::getWorkDate, date)
                .eq(Schedule::getPeriod, period));

        if (s == null) {
            Schedule newS = new Schedule();
            newS.setDoctorId(doctorId);
            newS.setWorkDate(date);
            newS.setPeriod(period);
            newS.setTotalQuota(30);
            newS.setBookedCount(0);

            // 校验医生请假情况
            Long leaveCount = leaveMapper.selectCount(new LambdaQueryWrapper<Leave>()
                    .eq(Leave::getDoctorId, doctorId)
                    .eq(Leave::getStartDate, date)
                    .eq(Leave::getStatus, "approved")
                    .and(w -> w.eq(Leave::getPeriod, "全天").or().eq(Leave::getPeriod, period)));

            newS.setStatus(leaveCount > 0 ? "leave" : "available");
            scheduleMapper.insert(newS);
        }
    }
}