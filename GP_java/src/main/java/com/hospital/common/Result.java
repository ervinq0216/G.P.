package com.hospital.common;

import lombok.Data;

/**
 * 统一返回结果类
 * 用于后端向前端返回标准格式的数据
 * @param <T> 数据类型
 */
@Data
public class Result<T> {
    private Integer code; // 状态码: 200成功, 500失败
    private String msg;   // 提示信息
    private T data;       // 返回的数据

    // 成功时的静态方法
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "success";
        r.data = data;
        return r;
    }

    // 失败时的静态方法
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.msg = msg;
        return r;
    }
}