import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String storedHash = "$2a$10$7JB720yubVS1vai/5EjiueVwDkB.qD6kz.EicKnx.v/aK.vB5tEJq";
        String inputPassword = "123456";
        boolean matches = encoder.matches(inputPassword, storedHash);
        System.out.println("Password match result: " + matches);
        if (!matches) {
            String newHash = encoder.encode(inputPassword);
            System.out.println("New hash for '123456': " + newHash);
        }
    }
}
