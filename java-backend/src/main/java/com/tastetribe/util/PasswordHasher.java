package com.tastetribe.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Password hashing helper. Passwords are NEVER stored as plain text — BCrypt
 * (salted, adaptive) hashes are persisted and verified on login.
 */
@Component
public class PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String plain) {
        return encoder.encode(plain);
    }

    public boolean verify(String plain, String hash) {
        try {
            return encoder.matches(plain, hash);
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
