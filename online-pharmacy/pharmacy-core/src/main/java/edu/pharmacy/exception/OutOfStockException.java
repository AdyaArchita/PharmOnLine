package edu.pharmacy.exception;

/** Thrown when an order requests more units than are available. */
public class OutOfStockException extends Exception {
    public OutOfStockException(String message) {
        super(message);
    }
}
