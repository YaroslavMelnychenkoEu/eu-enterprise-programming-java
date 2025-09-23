package com.example.shop.service;

import com.example.shop.domain.Category;
import com.example.shop.dto.CategoryDTO;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        
        if (categoryDTO.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDTO.getParentCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryDTO.getParentCategoryId()));
            category.setParentCategory(parentCategory);
        }
        
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }
    
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return convertToDTO(category);
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getRootCategories(Pageable pageable) {
        return categoryRepository.findRootCategories(pageable)
            .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDTO> getSubCategories(Long parentId) {
        return categoryRepository.findByParentCategoryId(parentId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        
        if (categoryDTO.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDTO.getParentCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryDTO.getParentCategoryId()));
            category.setParentCategory(parentCategory);
        } else {
            category.setParentCategory(null);
        }
        
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }
    
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Check if category has products
        Long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new RuntimeException("Cannot delete category with products. Please remove all products first.");
        }
        
        categoryRepository.delete(category);
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDTO> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContaining(name).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        
        if (category.getParentCategory() != null) {
            dto.setParentCategoryId(category.getParentCategory().getId());
            dto.setParentCategoryName(category.getParentCategory().getName());
        }
        
        return dto;
    }
}
