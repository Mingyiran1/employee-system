package com.whtc.employee;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String encoded = encoder.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("加密密码: " + encoded);

        // 验证
        boolean matches = encoder.matches(password, encoded);
        System.out.println("验证结果: " + matches);
    }
}
