package edu.pharmacy.model;

/** Supplier contact and address data. */
public final class Supplier {
    private final long id;
    private final String name;
    private final String contactName;
    private final String phone;
    private final String email;
    private final String address;

    public Supplier(long id, String name, String contactName, String phone,
                    String email, String address) {
        if (id < 0) {
            throw new IllegalArgumentException("Supplier id cannot be negative.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Supplier name is required.");
        }
        this.id = id;
        this.name = name.trim();
        this.contactName = clean(contactName);
        this.phone = clean(phone);
        this.email = clean(email);
        this.address = clean(address);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContactName() {
        return contactName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }
}
