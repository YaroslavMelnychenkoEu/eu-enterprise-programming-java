package com.example.shop.service;

import com.example.shop.domain.Category;
import com.example.shop.domain.Product;
import com.example.shop.dto.ProductDTO;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }
        
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }
    
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDTO(product);
    }
    
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
            .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
            .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryAndPriceRange(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByCategoryAndPriceRange(categoryId, minPrice, maxPrice).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProductsByName(String name) {
        return productRepository.findByNameContaining(name).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProductsByNameOrDescription(String name, String description) {
        return productRepository.findByNameOrDescriptionContaining(name, description).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsOrderedByPrice(Pageable pageable, boolean ascending) {
        if (ascending) {
            return productRepository.findAllOrderByPriceAsc(pageable)
                .map(this::convertToDTO);
        } else {
            return productRepository.findAllOrderByPriceDesc(pageable)
                .map(this::convertToDTO);
        }
    }
    
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsOrderedByCreatedAt(Pageable pageable) {
        return productRepository.findAllOrderByCreatedAtDesc(pageable)
            .map(this::convertToDTO);
    }
    
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }
        
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }
    
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        productRepository.delete(product);
    }
    
    @Transactional(readOnly = true)
    public Long getProductCountByCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getAveragePriceByCategory(Long categoryId) {
        return productRepository.getAveragePriceByCategoryId(categoryId);
    }
    
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        return dto;
    }
}
