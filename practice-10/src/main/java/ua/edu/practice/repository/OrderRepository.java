package ua.edu.practice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.edu.practice.model.Order;
import ua.edu.practice.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторій для роботи з замовленнями
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Знайти замовлення за orderId
     */
    Optional<Order> findByOrderId(String orderId);
    
    /**
     * Знайти замовлення за customerId
     */
    List<Order> findByCustomerId(String customerId);
    
    /**
     * Знайти замовлення за статусом
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Знайти замовлення з сумою більше ніж вказана
     */
    @Query("SELECT o FROM Order o WHERE o.amount > :amount")
    List<Order> findByAmountGreaterThan(@Param("amount") BigDecimal amount);
    
    /**
     * Підрахувати замовлення за статусом
     */
    long countByStatus(OrderStatus status);
    
    /**
     * Знайти замовлення за діапазоном сум
     */
    @Query("SELECT o FROM Order o WHERE o.amount BETWEEN :minAmount AND :maxAmount")
    List<Order> findByAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                   @Param("maxAmount") BigDecimal maxAmount);
}
