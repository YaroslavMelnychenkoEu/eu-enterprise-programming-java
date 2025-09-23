package com.example.shop.repository;

import com.example.shop.domain.Order;
import com.example.shop.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByCustomerId(Long customerId);
    
    List<Order> findByStatus(OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    List<Order> findByCustomerIdAndCreatedAtBetween(@Param("customerId") Long customerId, 
                                                  @Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    List<Order> findByStatusAndCreatedAtBetween(@Param("status") OrderStatus status, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount AND o.totalAmount <= :maxAmount")
    List<Order> findByTotalAmountBetween(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.products WHERE o.id = :id")
    Optional<Order> findByIdWithCustomerAndProducts(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.products WHERE o.id = :id")
    Optional<Order> findByIdWithProducts(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId, Pageable pageable);
    
    @Query("SELECT o FROM Order o ORDER BY o.totalAmount DESC")
    Page<Order> findAllOrderByTotalAmountDesc(Pageable pageable);
    
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    Page<Order> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal getAverageOrderAmountBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
