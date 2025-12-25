package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_schedule")
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private LocalDate workDate;
    private String period; // 上午/下午
    private Integer totalQuota;
    private Integer bookedCount;
    private String status; // available/leave
    private LocalDateTime createdTime;
}