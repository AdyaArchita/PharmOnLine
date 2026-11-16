package edu.pharmacy.exception;

/** Thrown when an expired medicine is selected for sale. */
public class ExpiredMedicineException extends Exception {
    public ExpiredMedicineException(String message) {
        super(message);
    }
}
