package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者实体
 * 对应数据库表 t_patient
 */
@Data
@TableName("t_patient")
public class Patient {
    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 手机号 (作为登录账号)
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像 (Base64字符串或URL)
     */
    private String avatar;

    /**
     * 性别 (男/女)
     */
    private String gender;

    /**
     * 出生日期 (用于计算年龄)
     */
    private LocalDate birthday;

    /**
     * 身高 (cm)
     */
    private Double height;

    /**
     * 体重 (kg)
     */
    private Double weight;

    /**
     * 血型 (A/B/AB/O/Rh+)
     */
    private String bloodType;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}