package ua.edu.practice.model;

/**
 * Статуси замовлення
 */
public enum OrderStatus {
    NEW("Нове замовлення"),
    PROCESSING("В обробці"),
    PAID("Оплачено"),
    SHIPPED("Відправлено"),
    DELIVERED("Доставлено"),
    CANCELLED("Скасовано");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
