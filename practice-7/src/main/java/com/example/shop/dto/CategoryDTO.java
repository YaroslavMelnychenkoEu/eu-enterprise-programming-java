package com.example.shop.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Long parentCategoryId;
    private String parentCategoryName;
    private List<CategoryDTO> subCategories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public CategoryDTO() {}
    
    public CategoryDTO(Long id, String name, String description, Long parentCategoryId, String parentCategoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
        this.parentCategoryName = parentCategoryName;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getParentCategoryId() {
        return parentCategoryId;
    }
    
    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
    
    public String getParentCategoryName() {
        return parentCategoryName;
    }
    
    public void setParentCategoryName(String parentCategoryName) {
        this.parentCategoryName = parentCategoryName;
    }
    
    public List<CategoryDTO> getSubCategories() {
        return subCategories;
    }
    
    public void setSubCategories(List<CategoryDTO> subCategories) {
        this.subCategories = subCategories;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
