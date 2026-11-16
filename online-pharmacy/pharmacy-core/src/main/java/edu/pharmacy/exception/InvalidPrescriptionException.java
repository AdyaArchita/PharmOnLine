package edu.pharmacy.exception;

/** Thrown when a prescription-only medicine lacks an approved prescription. */
public class InvalidPrescriptionException extends Exception {
    public InvalidPrescriptionException(String message) {
        super(message);
    }
}
