package edu.pharmacy.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Salted PBKDF2 password hashing using only Java's standard security APIs. */
public final class PasswordHasher {
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int ITERATIONS = 210_000;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static Hash create(char[] password) throws GeneralSecurityException {
        validate(password);
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password, salt);
        return new Hash(Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(derived));
    }

    public static boolean verify(char[] password, String encodedSalt, String encodedHash)
            throws GeneralSecurityException {
        validate(password);
        byte[] salt = Base64.getDecoder().decode(encodedSalt);
        byte[] expected = Base64.getDecoder().decode(encodedHash);
        byte[] actual = derive(password, salt);
        return MessageDigest.isEqual(expected, actual);
    }

    private static byte[] derive(char[] password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec specification = new PBEKeySpec(password, salt, ITERATIONS, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(specification).getEncoded();
        } finally {
            specification.clearPassword();
        }
    }

    private static void validate(char[] password) {
        if (password == null || password.length < 8 || password.length > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must contain between 8 and 128 characters.");
        }
    }

    public record Hash(String salt, String value) {
    }
}
