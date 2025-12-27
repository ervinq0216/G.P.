package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;
    private Long doctorId;
    private Long scheduleId;

    /**
     * 状态: booked(已预约), completed(已就诊/已完成), cancelled(已取消)
     */
    private String status;

    /**
     * 医生填写的病例信息
     */
    private String medicalRecord;

    /**
     * 患者评价内容
     */
    private String comment;

    /**
     * 评分 (1-5)
     */
    private Integer rating;

    private LocalDateTime createdTime;
}