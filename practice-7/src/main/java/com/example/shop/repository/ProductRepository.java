package com.example.shop.repository;

import com.example.shop.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByCategoryId(Long categoryId);
    
    List<Product> findByCategoryName(String categoryName);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByCategoryAndPriceRange(@Param("categoryId") Long categoryId, 
                                            @Param("minPrice") BigDecimal minPrice, 
                                            @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    List<Product> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% OR p.description LIKE %:description%")
    List<Product> findByNameOrDescriptionContaining(@Param("name") String name, @Param("description") String description);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p ORDER BY p.price ASC")
    Page<Product> findAllOrderByPriceAsc(Pageable pageable);
    
    @Query("SELECT p FROM Product p ORDER BY p.price DESC")
    Page<Product> findAllOrderByPriceDesc(Pageable pageable);
    
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    Page<Product> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.orders WHERE p.id = :id")
    Optional<Product> findByIdWithOrders(@Param("id") Long id);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT AVG(p.price) FROM Product p WHERE p.category.id = :categoryId")
    BigDecimal getAveragePriceByCategoryId(@Param("categoryId") Long categoryId);
}
