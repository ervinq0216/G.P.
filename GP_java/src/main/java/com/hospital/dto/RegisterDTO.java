package com.hospital.dto;

import lombok.Data;

/**
 * 注册请求参数
 * 用于接收前端注册页面提交的数据
 */
@Data
public class RegisterDTO {
    /**
     * 手机号 (必填)
     */
    private String phone;

    /**
     * 真实姓名 (必填)
     */
    private String realName;

    /**
     * 密码 (必填)
     */
    private String password;

    // 注意：验证码校验通常在前置步骤或这里一起校验，
    // 但为了简化，暂不传验证码到后端，或者前端校验过了即可。
}