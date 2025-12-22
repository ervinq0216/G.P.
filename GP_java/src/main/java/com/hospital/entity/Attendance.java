package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@TableName("t_attendance")
public class Attendance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private LocalDate date;
    private LocalTime morningIn;
    private LocalTime morningOut;
    private LocalTime afternoonIn;
    private LocalTime afternoonOut;
    private LocalDateTime createdTime;
}