package com.hospital;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类
 */
@SpringBootApplication
@MapperScan("com.hospital.mapper") // 扫描 Mapper 接口
public class HospitalBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalBackendApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  医院挂号系统后端启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}