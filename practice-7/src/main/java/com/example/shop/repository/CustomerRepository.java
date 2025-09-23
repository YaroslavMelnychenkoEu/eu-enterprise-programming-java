package com.example.shop.repository;

import com.example.shop.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByEmail(String email);
    
    List<Customer> findByFirstNameContainingIgnoreCase(String firstName);
    
    List<Customer> findByLastNameContainingIgnoreCase(String lastName);
    
    @Query("SELECT c FROM Customer c WHERE c.firstName LIKE %:name% OR c.lastName LIKE %:name%")
    List<Customer> findByFirstNameOrLastNameContaining(@Param("name") String name);
    
    @Query("SELECT c FROM Customer c WHERE c.email LIKE %:email%")
    List<Customer> findByEmailContaining(@Param("email") String email);
    
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.orders WHERE c.id = :id")
    Optional<Customer> findByIdWithOrders(@Param("id") Long id);
    
    @Query("SELECT c FROM Customer c WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate")
    List<Customer> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId")
    Long countOrdersByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.customer.id = :customerId")
    Double getTotalSpentByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT c FROM Customer c ORDER BY c.createdAt DESC")
    Page<Customer> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT c FROM Customer c ORDER BY c.lastName ASC, c.firstName ASC")
    Page<Customer> findAllOrderByName(Pageable pageable);
}
