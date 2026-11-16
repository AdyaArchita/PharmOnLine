package edu.pharmacy.model;

/** A pharmacy staff member who manages inventory and processes orders. */
public class Pharmacist extends Person {
    private final String licenseNumber;

    public Pharmacist(long id, String fullName, String email, String licenseNumber,
                      String passwordHash, String passwordSalt) {
        super(id, fullName, email, passwordHash, passwordSalt);
        this.licenseNumber = licenseNumber == null ? "" : licenseNumber.trim();
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    @Override
    public Role getRole() {
        return Role.PHARMACIST;
    }
}
