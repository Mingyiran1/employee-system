package com.whtc.employee;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 为密码 "123456" 生成 hash
        String password = "123456";
        String hash = encoder.encode(password);

        System.out.println("生成的 BCrypt hash: " + hash);
        System.out.println("Hash 长度: " + hash.length());

        // 验证生成的 hash
        boolean matches = encoder.matches(password, hash);
        System.out.println("验证结果: " + matches);

        // 也验证一下旧的 hash
        String oldHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO";
        boolean oldMatches = encoder.matches(password, oldHash);
        System.out.println("旧hash验证结果: " + oldMatches);
    }
}
