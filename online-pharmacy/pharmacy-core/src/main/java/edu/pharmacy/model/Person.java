package edu.pharmacy.model;

import java.util.Objects;
import java.util.Locale;

/** Shared identity and authentication data for all system users. */
public abstract class Person {
    private final long id;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final String passwordSalt;

    protected Person(long id, String fullName, String email,
                     String passwordHash, String passwordSalt) {
        if (id < 0) {
            throw new IllegalArgumentException("User id cannot be negative.");
        }
        this.id = id;
        this.fullName = requireText(fullName, "Full name");
        this.email = requireText(email, "Email").toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    private static String requireText(String value, String label) {
        Objects.requireNonNull(value, label + " is required.");
        String cleaned = value.trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        return cleaned;
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public abstract Role getRole();
}
