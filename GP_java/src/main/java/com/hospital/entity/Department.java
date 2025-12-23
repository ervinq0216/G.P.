package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_department")
public class Department {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String category; // 手术科室/非手术科室/诊断相关
    private String name;
    private String intro;
    private LocalDateTime createdTime;
}