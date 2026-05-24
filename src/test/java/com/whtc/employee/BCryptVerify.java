package com.whtc.employee;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptVerify {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";

        // 生成新的正确hash
        String correctHash = encoder.encode(password);
        System.out.println("新生成的正确 hash: " + correctHash);
        System.out.println("验证新hash: " + encoder.matches(password, correctHash));

        // 测试数据库中的旧hash
        String oldHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO";
        System.out.println("\n数据库中的 hash: " + oldHash);
        System.out.println("验证旧hash: " + encoder.matches(password, oldHash));

        // 生成一个确定性的hash供SQL使用
        System.out.println("\n=== 请使用以下SQL更新密码 ===");
        System.out.println("UPDATE sys_user SET password = '" + correctHash + "' WHERE username = 'admin';");
    }
}
