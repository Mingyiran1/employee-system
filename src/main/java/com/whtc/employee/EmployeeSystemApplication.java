package com.whtc.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAspectJAutoProxy
@EnableAsync
public class EmployeeSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeSystemApplication.class, args);
        System.out.println("========================================");
        System.out.println("  员工信息管理系统启动成功！");
        System.out.println("  访问地址: http://localhost:3000");
        System.out.println("========================================");
    }
}
