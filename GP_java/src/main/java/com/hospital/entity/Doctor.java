package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 医生实体
 * 对应数据库表 t_doctor
 */
@Data
@TableName("t_doctor")
public class Doctor {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工号 (作为登录账号)
     */
    private String jobNumber;

    /**
     * 密码
     */
    private String password;

    /**
     * 医生姓名
     */
    private String realName;

    /**
     * 所属科室ID
     */
    private Long deptId;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}