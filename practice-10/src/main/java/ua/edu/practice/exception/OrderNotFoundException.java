package ua.edu.practice.exception;

/**
 * Виняток для випадку, коли замовлення не знайдено
 */
public class OrderNotFoundException extends RuntimeException {
    
    public OrderNotFoundException(String message) {
        super(message);
    }
    
    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
