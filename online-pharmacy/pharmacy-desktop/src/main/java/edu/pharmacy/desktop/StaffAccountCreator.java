package edu.pharmacy.desktop;

import edu.pharmacy.dao.UserDAO;
import edu.pharmacy.model.Admin;
import edu.pharmacy.model.Person;
import edu.pharmacy.model.Pharmacist;
import java.io.Console;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;

/** Console helper for creating the first pharmacist or administrator account. */
public final class StaffAccountCreator {
    private StaffAccountCreator() {
    }

    public static void main(String[] args) throws SQLException, GeneralSecurityException {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException(
                    "Run this helper from a terminal so the password can be entered privately.");
        }
        String role = console.readLine("Create PHARMACIST or ADMIN: ").trim().toUpperCase();
        String name = console.readLine("Full name: ");
        String email = console.readLine("Email: ");
        Person staff;
        if ("PHARMACIST".equals(role)) {
            String license = console.readLine("License number: ");
            staff = new Pharmacist(0, name, email, license, null, null);
        } else if ("ADMIN".equals(role)) {
            String department = console.readLine("Department: ");
            staff = new Admin(0, name, email, department, null, null);
        } else {
            throw new IllegalArgumentException("Role must be PHARMACIST or ADMIN.");
        }

        char[] password = console.readPassword("Password (8-128 characters): ");
        try {
            long id = new UserDAO().create(staff, password);
            console.printf("Staff account created with id %d.%n", id);
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
