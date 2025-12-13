package com.hospital.dto;

import lombok.Data;

/**
 * 重置密码请求参数
 */
@Data
public class ResetPasswordDTO {
    /**
     * 角色: patient, doctor, admin
     */
    private String role;

    /**
     * 账号: 手机号/工号/用户名
     */
    private String account; // 对应前端的 account 字段

    /**
     * 真实姓名 (用于身份校验)
     */
    private String name;    // 对应前端的 name 字段

    /**
     * 新密码
     */
    private String newPassword;
}