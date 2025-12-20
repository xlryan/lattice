import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        // Match the configuration in SecurityConfig: defaultsForSpringSecurity_v5_8
        Pbkdf2PasswordEncoder encoder = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        String hash = encoder.encode("lattice");
        System.out.println("Hash for 'lattice': " + hash);
    }
}
