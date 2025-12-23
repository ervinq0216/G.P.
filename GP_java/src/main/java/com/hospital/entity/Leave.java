package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_leave")
public class Leave {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private String type;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // pending, approved, rejected
    private LocalDateTime createdTime;

    // 关联字段，不存数据库
    @TableField(exist = false)
    private String doctorName;
}