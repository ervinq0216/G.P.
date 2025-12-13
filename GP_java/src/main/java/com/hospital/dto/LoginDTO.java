package com.hospital.dto;

import lombok.Data;

/**
 * 登录请求参数
 * 对应前端 formData 提交的数据
 */
@Data
public class LoginDTO {
    /**
     * 角色: patient (患者), doctor (医生), admin (管理员)
     */
    private String role;

    /**
     * 账号: 对应前端的 formData.username
     * 可能是手机号、工号或管理员用户名
     */
    private String username;

    /**
     * 密码: 对应前端的 formData.password
     */
    private String password;
}