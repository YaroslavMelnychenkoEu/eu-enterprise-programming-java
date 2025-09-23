package com.example.shop.repository;

import com.example.shop.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    List<Category> findByParentCategoryIsNull();
    
    List<Category> findByParentCategoryId(Long parentId);
    
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name%")
    List<Category> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL")
    Page<Category> findRootCategories(Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countProductsByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.id = :id")
    Optional<Category> findByIdWithProducts(@Param("id") Long id);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories WHERE c.id = :id")
    Optional<Category> findByIdWithSubCategories(@Param("id") Long id);
}
