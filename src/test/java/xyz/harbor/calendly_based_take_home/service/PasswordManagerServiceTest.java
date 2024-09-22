package xyz.harbor.calendly_based_take_home.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PasswordManagerServiceTest {
    private final String CORRECT_PASSWORD = "correct_password";

    PasswordManagerService passwordManagerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        String hashAlgorithm = "PBKDF2WithHmacSHA1";
        int iterations = 10000;
        int keyLength = 256;
        passwordManagerService = new PasswordManagerService(iterations, keyLength, hashAlgorithm);
    }

    @Test
    void testGetNextSalt() {
        String salt = passwordManagerService.getNextSalt();
        assertNotNull(salt);
        assertEquals(24, salt.length());
    }

    @Test
    void testHash() {
        String salt = passwordManagerService.getNextSalt();
        String hash = passwordManagerService.hash(CORRECT_PASSWORD, salt);

        assertNotNull(hash);
        assertEquals(44, hash.length());
    }

    @Test
    void testIsExpectedPassword() {
        String SALT = "SALT";
        String correctHash = passwordManagerService.hash(CORRECT_PASSWORD, SALT);

        assertTrue(passwordManagerService.isExpectedPassword(CORRECT_PASSWORD, SALT, correctHash));
        String INCORRECT_PASSWORD = "incorrect_password";
        assertFalse(passwordManagerService.isExpectedPassword(INCORRECT_PASSWORD, SALT, correctHash));
    }
}
