package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.Leave;
import com.hospital.mapper.LeaveMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin(origins = "*")
public class LeaveController {

    @Autowired
    private LeaveMapper leaveMapper;

    /**
     * 提交请假申请
     */
    @PostMapping("/apply")
    public Result<String> apply(@RequestBody Leave leave) {
        if (leave.getDoctorId() == null || leave.getStartDate() == null || leave.getPeriod() == null) {
            return Result.error("请填写完整信息");
        }

        // 默认设置
        leave.setEndDate(leave.getStartDate()); // 单日请假
        leave.setStatus("pending");
        leave.setCreatedTime(LocalDateTime.now());
        if (leave.getType() == null) leave.setType("事假");

        leaveMapper.insert(leave);
        return Result.success("申请提交成功，请等待审批");
    }

    /**
     * 获取我的请假记录 (用于消息通知)
     */
    @GetMapping("/my-list")
    public Result<List<Leave>> myList(@RequestParam Long doctorId) {
        List<Leave> list = leaveMapper.selectList(new LambdaQueryWrapper<Leave>()
                .eq(Leave::getDoctorId, doctorId)
                .orderByDesc(Leave::getCreatedTime));
        return Result.success(list);
    }
}