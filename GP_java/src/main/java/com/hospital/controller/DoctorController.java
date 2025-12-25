package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.Doctor;
import com.hospital.entity.Leave;
import com.hospital.entity.Notice;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.LeaveMapper;
import com.hospital.mapper.NoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private LeaveMapper leaveMapper;

    @GetMapping("/info/{id}")
    public Result<Doctor> getInfo(@PathVariable Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor != null) {
            return Result.success(doctor);
        }
        return Result.error("医生不存在");
    }

    @PostMapping("/update")
    public Result<Doctor> updateInfo(@RequestBody Doctor doctor) {
        if (doctor.getId() == null) return Result.error("ID不能为空");
        doctorMapper.updateById(doctor);
        Doctor newInfo = doctorMapper.selectById(doctor.getId());
        return Result.success(newInfo);
    }

    /**
     * 获取未读消息数量 (用于显示红点)
     * 逻辑：(相关通知时间 > 上次阅读时间) + (请假审批更新时间 > 上次阅读时间)
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@RequestParam Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) return Result.success(0);

        LocalDateTime lastRead = doctor.getLastReadTime();
        if (lastRead == null) lastRead = LocalDateTime.of(2000, 1, 1, 0, 0);

        // 1. 统计未读通知
        // 目标包括: all(所有人), doctor(所有医生), dept(本可以室)
        Long noticeCount = noticeMapper.selectCount(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getType, "notice")
                .gt(Notice::getCreatedTime, lastRead)
                .and(w -> w.eq(Notice::getTargetType, "all")
                        .or().eq(Notice::getTargetType, "doctor") // 新增：全部医生
                        .or(sub -> sub.eq(Notice::getTargetType, "dept").eq(Notice::getTargetDeptId, doctor.getDeptId()))
                ));

        // 2. 统计未读请假反馈 (状态变更)
        Long leaveCount = leaveMapper.selectCount(new LambdaQueryWrapper<Leave>()
                .eq(Leave::getDoctorId, doctorId)
                .gt(Leave::getUpdateTime, lastRead));

        return Result.success((int)(noticeCount + leaveCount));
    }

    /**
     * 获取所有消息列表 (通知 + 请假反馈)
     */
    @GetMapping("/messages")
    public Result<List<Map<String, Object>>> getMessages(@RequestParam Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) return Result.error("医生不存在");

        List<Map<String, Object>> resultList = new ArrayList<>();

        // 1. 获取相关通知
        List<Notice> notices = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getType, "notice")
                .and(w -> w.eq(Notice::getTargetType, "all")
                        .or().eq(Notice::getTargetType, "doctor") // 新增：全部医生
                        .or(sub -> sub.eq(Notice::getTargetType, "dept").eq(Notice::getTargetDeptId, doctor.getDeptId()))
                )
                .orderByDesc(Notice::getCreatedTime)
                .last("LIMIT 20"));

        for (Notice n : notices) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", "N" + n.getId());
            map.put("type", "notice");
            map.put("title", "系统通知：" + n.getTitle());
            map.put("content", n.getContent());
            map.put("time", n.getCreatedTime());
            resultList.add(map);
        }

        // 2. 获取请假记录
        List<Leave> leaves = leaveMapper.selectList(new LambdaQueryWrapper<Leave>()
                .eq(Leave::getDoctorId, doctorId)
                .orderByDesc(Leave::getUpdateTime)
                .last("LIMIT 20"));

        for (Leave l : leaves) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", "L" + l.getId());
            map.put("type", "leave");
            String statusText = "待审批";
            if ("approved".equals(l.getStatus())) statusText = "已通过";
            if ("rejected".equals(l.getStatus())) statusText = "已拒绝";

            map.put("title", "请假申请反馈");
            map.put("content", String.format("您申请的 %s (%s) 请假状态更新为：%s", l.getStartDate(), l.getPeriod(), statusText));
            map.put("time", l.getUpdateTime() != null ? l.getUpdateTime() : l.getCreatedTime());
            map.put("status", l.getStatus());
            resultList.add(map);
        }

        // 3. 混合排序 (按时间倒序)
        List<Map<String, Object>> sortedList = resultList.stream()
                .sorted((m1, m2) -> ((LocalDateTime) m2.get("time")).compareTo((LocalDateTime) m1.get("time")))
                .collect(Collectors.toList());

        return Result.success(sortedList);
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/read")
    public Result<String> markRead(@RequestBody Map<String, Long> params) {
        Long doctorId = params.get("doctorId");
        Doctor doctor = new Doctor();
        doctor.setId(doctorId);
        doctor.setLastReadTime(LocalDateTime.now());
        doctorMapper.updateById(doctor);
        return Result.success("已读");
    }
}