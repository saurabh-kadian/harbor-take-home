package xyz.harbor.calendly_based_take_home.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Arrays;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Service
public class PasswordManagerService {
    private final SecureRandom random;
    private final int iterations;
    private final int keyLength;

    private final String hashAlgorithm;

    @Autowired
    public PasswordManagerService(@Value("${hash.iterations}") int iterations,
                                  @Value("${hash.keyLength}") int keyLength,
                                  @Value("${hash.algorithm}") String hashAlgorithm){
        this.random = new SecureRandom();
        this.iterations = iterations;
        this.keyLength = keyLength;
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getNextSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public String hash(String password, String salt) {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        char[] passwordChars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, iterations, keyLength);
        Arrays.fill(passwordChars, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(hashAlgorithm);
            byte[] hashBytes = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    public boolean isExpectedPassword(String password, String salt, String expectedHash) {
        String pwdHash = hash(password, salt);
        return pwdHash.equals(expectedHash);
    }
}
