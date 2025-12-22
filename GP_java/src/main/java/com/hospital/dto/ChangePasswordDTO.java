package com.hospital.dto;

import lombok.Data;

/**
 * 修改密码请求参数
 */
@Data
public class ChangePasswordDTO {
    /**
     * 角色: patient, doctor, admin
     */
    private String role;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}