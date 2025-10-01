package ua.edu.practice.model;

/**
 * Енумерація пріоритетів замовлень
 */
public enum OrderPriority {
    URGENT(0, "Термінові замовлення"),
    VIP(1, "VIP-клієнти"),
    STANDARD(2, "Стандартні замовлення"),
    BACKGROUND(3, "Фонові задачі");

    private final int level;
    private final String description;

    OrderPriority(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }
}

