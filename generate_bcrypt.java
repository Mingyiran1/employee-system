import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class generate_bcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 生成 123456 的 BCrypt 密码
        String password = encoder.encode("123456");
        System.out.println("BCrypt密码: " + password);

        // 验证
        boolean matches = encoder.matches("123456", password);
        System.out.println("验证结果: " + matches);
    }
}
