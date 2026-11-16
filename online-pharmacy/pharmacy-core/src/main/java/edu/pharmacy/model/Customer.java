package edu.pharmacy.model;

/** A customer who browses medicines and places orders in the web portal. */
public class Customer extends Person {
    private final String address;

    public Customer(long id, String fullName, String email, String address,
                    String passwordHash, String passwordSalt) {
        super(id, fullName, email, passwordHash, passwordSalt);
        this.address = address == null ? "" : address.trim();
    }

    public String getAddress() {
        return address;
    }

    @Override
    public Role getRole() {
        return Role.CUSTOMER;
    }
}
