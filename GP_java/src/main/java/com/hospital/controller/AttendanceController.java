package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.Attendance;
import com.hospital.mapper.AttendanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceMapper attendanceMapper;

    /**
     * 获取指定医生、指定月份的所有考勤记录
     */
    @GetMapping("/list")
    public Result<List<Attendance>> list(@RequestParam Long doctorId, @RequestParam String month) {
        // month 格式 "2023-12"
        LocalDate start = LocalDate.parse(month + "-01");
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<Attendance> list = attendanceMapper.selectList(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getDoctorId, doctorId)
                .between(Attendance::getDate, start, end));
        return Result.success(list);
    }

    /**
     * 获取医生当天的考勤详情
     */
    @GetMapping("/today")
    public Result<Attendance> getToday(@RequestParam Long doctorId) {
        Attendance attendance = attendanceMapper.selectOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getDoctorId, doctorId)
                .eq(Attendance::getDate, LocalDate.now()));
        return Result.success(attendance);
    }

    /**
     * 签到/签退
     * type: morningIn, morningOut, afternoonIn, afternoonOut
     */
    @PostMapping("/check")
    public Result<String> check(@RequestBody Map<String, Object> params) {
        Long doctorId = Long.valueOf(params.get("doctorId").toString());
        String type = params.get("type").toString();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 查找当天的记录，没有则创建
        Attendance attendance = attendanceMapper.selectOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getDoctorId, doctorId)
                .eq(Attendance::getDate, today));

        if (attendance == null) {
            attendance = new Attendance();
            attendance.setDoctorId(doctorId);
            attendance.setDate(today);
            attendanceMapper.insert(attendance); // 先插入获取ID
        }

        // 校验时间范围
        // 1. 早上签到 07:45 - 08:15
        if ("morningIn".equals(type)) {
            if (isBetween(now, "07:45", "08:15")) {
                if (attendance.getMorningIn() != null) return Result.error("已签到");
                attendance.setMorningIn(now);
            } else return Result.error("不在早上签到时间范围内");
        }
        // 2. 早上签退 11:45 - 12:15
        else if ("morningOut".equals(type)) {
            if (isBetween(now, "11:45", "12:15")) {
                if (attendance.getMorningOut() != null) return Result.error("已签退");
                attendance.setMorningOut(now);
            } else return Result.error("不在早上签退时间范围内");
        }
        // 3. 下午签到 14:15 - 14:45
        else if ("afternoonIn".equals(type)) {
            if (isBetween(now, "14:15", "14:45")) {
                if (attendance.getAfternoonIn() != null) return Result.error("已签到");
                attendance.setAfternoonIn(now);
            } else return Result.error("不在下午签到时间范围内");
        }
        // 4. 下午签退 17:45 - 18:15
        else if ("afternoonOut".equals(type)) {
            if (isBetween(now, "17:45", "18:15")) {
                if (attendance.getAfternoonOut() != null) return Result.error("已签退");
                attendance.setAfternoonOut(now);
            } else return Result.error("不在下午签退时间范围内");
        } else {
            return Result.error("无效的操作类型");
        }

        attendanceMapper.updateById(attendance);
        return Result.success("打卡成功");
    }

    private boolean isBetween(LocalTime now, String start, String end) {
        return !now.isBefore(LocalTime.parse(start + ":00")) && !now.isAfter(LocalTime.parse(end + ":59"));
    }
}