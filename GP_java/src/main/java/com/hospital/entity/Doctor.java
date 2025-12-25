package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_doctor")
public class Doctor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String jobNumber;
    private String password;
    private String realName;
    private Long deptId;

    private String avatar;
    private String gender;
    private String introduction;

    private LocalDateTime lastReadTime; // 新增：最后阅读时间
    private LocalDateTime createdTime;
}