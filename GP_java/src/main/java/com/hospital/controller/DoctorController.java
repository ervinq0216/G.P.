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

/**
 * 医生端控制器 - 严格限定医生业务
 */
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

    /**
     * 获取医生信息 - 强制只查询医生表
     */
    @GetMapping("/info/{id}")
    public Result<Doctor> getInfo(@PathVariable Long id) {
        Doctor d = doctorMapper.selectById(id);
        if (d != null) {
            d.setPassword(null); // 脱敏
            return Result.success(d);
        }
        return Result.error("未找到该医生资料");
    }

    /**
     * 更新医生个人资料
     */
    @PostMapping("/update")
    public Result<Doctor> updateInfo(@RequestBody Doctor doctor) {
        if (doctor.getId() == null) return Result.error("ID不能为空");
        doctorMapper.updateById(doctor);
        Doctor updated = doctorMapper.selectById(doctor.getId());
        if (updated != null) updated.setPassword(null);
        return Result.success(updated);
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@RequestParam Long doctorId) {
        Doctor d = doctorMapper.selectById(doctorId);
        if (d == null) return Result.success(0);
        LocalDateTime lastRead = d.getLastReadTime() == null ? LocalDateTime.of(2000,1,1,0,0) : d.getLastReadTime();
        Long noticeCount = noticeMapper.selectCount(new LambdaQueryWrapper<Notice>().eq(Notice::getType, "notice").gt(Notice::getCreatedTime, lastRead).and(w -> w.eq(Notice::getTargetType, "all").or().eq(Notice::getTargetType, "doctor").or(sub -> sub.eq(Notice::getTargetType, "dept").eq(Notice::getTargetDeptId, d.getDeptId()))));
        Long leaveCount = leaveMapper.selectCount(new LambdaQueryWrapper<Leave>().eq(Leave::getDoctorId, doctorId).gt(Leave::getUpdateTime, lastRead));
        return Result.success((int)(noticeCount + leaveCount));
    }

    @GetMapping("/messages")
    public Result<List<Map<String, Object>>> getMessages(@RequestParam Long doctorId) {
        Doctor d = doctorMapper.selectById(doctorId);
        if (d == null) return Result.error("医生不存在");
        List<Map<String, Object>> resList = new ArrayList<>();
        List<Notice> notices = noticeMapper.selectList(new LambdaQueryWrapper<Notice>().eq(Notice::getType, "notice").and(w -> w.eq(Notice::getTargetType, "all").or().eq(Notice::getTargetType, "doctor").or(sub -> sub.eq(Notice::getTargetType, "dept").eq(Notice::getTargetDeptId, d.getDeptId()))).orderByDesc(Notice::getCreatedTime).last("LIMIT 20"));
        for (Notice n : notices) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", "N" + n.getId()); m.put("type", "notice"); m.put("title", "系统通知：" + n.getTitle()); m.put("content", n.getContent()); m.put("time", n.getCreatedTime());
            resList.add(m);
        }
        List<Leave> leaves = leaveMapper.selectList(new LambdaQueryWrapper<Leave>().eq(Leave::getDoctorId, doctorId).orderByDesc(Leave::getUpdateTime).last("LIMIT 20"));
        for (Leave l : leaves) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", "L" + l.getId()); m.put("type", "leave"); m.put("title", "请假申请反馈"); m.put("content", String.format("您申请的 %s 请假状态更新为：%s", l.getStartDate(), l.getStatus())); m.put("time", l.getUpdateTime() != null ? l.getUpdateTime() : l.getCreatedTime()); m.put("status", l.getStatus());
            resList.add(m);
        }
        return Result.success(resList.stream().sorted((m1, m2) -> ((LocalDateTime) m2.get("time")).compareTo((LocalDateTime) m1.get("time"))).collect(Collectors.toList()));
    }

    @PostMapping("/read")
    public Result<String> markRead(@RequestBody Map<String, Long> params) {
        Doctor doc = new Doctor();
        doc.setId(params.get("doctorId"));
        doc.setLastReadTime(LocalDateTime.now());
        doctorMapper.updateById(doc);
        return Result.success("ok");
    }
}