package edu.pharmacy.model;

/** An administrator with access to pharmacy configuration and reports. */
public class Admin extends Person {
    private final String department;

    public Admin(long id, String fullName, String email, String department,
                 String passwordHash, String passwordSalt) {
        super(id, fullName, email, passwordHash, passwordSalt);
        this.department = department == null ? "" : department.trim();
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
