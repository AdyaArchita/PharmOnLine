package edu.pharmacy.exception;

/** Thrown by a billing workflow when the available balance cannot cover a bill. */
public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
