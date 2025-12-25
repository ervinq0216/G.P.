package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

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
     * 创建时间
     */
    private LocalDateTime createdTime;
}