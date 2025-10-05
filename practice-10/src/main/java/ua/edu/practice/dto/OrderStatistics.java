package ua.edu.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для статистики замовлень
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatistics {
    private long totalOrders;
    private long newOrders;
    private long paidOrders;
    private long cancelledOrders;
}
