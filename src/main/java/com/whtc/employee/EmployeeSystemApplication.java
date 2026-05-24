package com.whtc.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableCaching
@EnableAspectJAutoProxy
@EnableAsync
public class EmployeeSystemApplication {
    public static void main(String[] args) {
        // 生成正确的BCrypt哈希
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String correctHash = encoder.encode("123456");

        // 立即验证生成的hash是否正确
        boolean verify = encoder.matches("123456", correctHash);

        System.out.println("========================================");
        System.out.println("  新生成的BCrypt hash（验证结果: " + verify + "）:");
        System.out.println("  " + correctHash);
        System.out.println("========================================");
        System.out.println("  请复制上面的hash到DataGrip执行:");
        System.out.println("  UPDATE sys_user SET password = '" + correctHash + "' WHERE username = 'admin';");
        System.out.println("========================================");

        SpringApplication.run(EmployeeSystemApplication.class, args);
        System.out.println("========================================");
        System.out.println("  员工信息管理系统启动成功！");
        System.out.println("  访问地址: http://localhost:3000");
        System.out.println("========================================");
    }
}
