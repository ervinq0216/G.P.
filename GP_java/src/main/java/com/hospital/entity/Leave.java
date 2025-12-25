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

    /**
     * 请假类型：事假/病假
     */
    private String type;

    /**
     * 时段：上午/下午/全天
     */
    private String period;

    private String reason;

    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * 状态: pending(待审批), approved(通过), rejected(拒绝)
     */
    private String status;

    private LocalDateTime createdTime;

    /**
     * 更新时间 (用于红点提醒)
     * 对应数据库的 update_time 字段
     */
    private LocalDateTime updateTime;

    // 关联字段，不存数据库
    @TableField(exist = false)
    private String doctorName;
}