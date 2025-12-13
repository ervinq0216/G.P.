package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员实体
 * 对应数据库表 t_admin
 */
@Data
@TableName("t_admin")
public class Admin {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名 (作为登录账号)
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 管理员姓名
     */
    private String realName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}