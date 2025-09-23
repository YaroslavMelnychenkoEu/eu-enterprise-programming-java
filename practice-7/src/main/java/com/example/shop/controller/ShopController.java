package com.example.shop.controller;

import com.example.shop.domain.enums.OrderStatus;
import com.example.shop.dto.*;
import com.example.shop.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/shop")
@CrossOrigin(origins = "*")
public class ShopController {
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private ComplexQueryService complexQueryService;
    
    // ========== CATEGORY ENDPOINTS ==========
    
    @PostMapping("/categories")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO created = categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    // ========== PRODUCT ENDPOINTS ==========
    
    @PostMapping("/products")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO created = productService.createProduct(productDTO);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/products")
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<ProductDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<ProductDTO> products = complexQueryService.findProductsWithDynamicFilters(name, categoryId, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }
    
    // ========== CUSTOMER ENDPOINTS ==========
    
    @PostMapping("/customers")
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customerDTO) {
        CustomerDTO created = customerService.createCustomer(customerDTO);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/customers")
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerDTO> customers = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(customers);
    }
    
    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        CustomerDTO customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }
    
    // ========== ORDER ENDPOINTS ==========
    
    @PostMapping("/orders")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        OrderDTO created = orderService.createOrder(orderDTO);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/orders/customer/{customerId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<OrderDTO> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        OrderDTO updated = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updated);
    }
    
    // ========== TRANSACTION ENDPOINTS ==========
    
    @PostMapping("/orders/with-availability-check")
    public ResponseEntity<OrderDTO> createOrderWithAvailabilityCheck(@RequestBody OrderDTO orderDTO) {
        OrderDTO created = transactionService.createOrderWithProductAvailabilityCheck(orderDTO);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/orders/{id}/status-with-inventory")
    public ResponseEntity<OrderDTO> updateOrderStatusWithInventory(@PathVariable Long id, @RequestParam OrderStatus status) {
        OrderDTO updated = transactionService.updateOrderStatusWithInventoryChanges(id, status);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
        OrderDTO cancelled = transactionService.cancelOrderWithInventoryReturn(id);
        return ResponseEntity.ok(cancelled);
    }
    
    @PutMapping("/products/category/{categoryId}/mass-update-prices")
    public ResponseEntity<String> massUpdatePrices(
            @PathVariable Long categoryId,
            @RequestParam(required = false) BigDecimal priceIncrease,
            @RequestParam(required = false) BigDecimal priceMultiplier) {
        int updatedCount = transactionService.massUpdatePricesForCategory(categoryId, priceIncrease, priceMultiplier);
        return ResponseEntity.ok("Updated " + updatedCount + " products");
    }
    
    // ========== COMPLEX QUERY ENDPOINTS ==========
    
    @GetMapping("/analytics/top-customers")
    public ResponseEntity<List<Object[]>> getTopCustomers(@RequestParam(defaultValue = "10") int limit) {
        List<Object[]> topCustomers = complexQueryService.getTopCustomersByTotalSpent(limit);
        return ResponseEntity.ok(topCustomers);
    }
    
    @GetMapping("/analytics/top-products")
    public ResponseEntity<List<Object[]>> getTopSellingProducts(@RequestParam(defaultValue = "10") int limit) {
        List<Object[]> topProducts = complexQueryService.getTopSellingProducts(limit);
        return ResponseEntity.ok(topProducts);
    }
    
    @GetMapping("/analytics/monthly-sales")
    public ResponseEntity<List<Object[]>> getMonthlySales(@RequestParam(defaultValue = "2024") int year) {
        List<Object[]> monthlySales = complexQueryService.getMonthlySalesStatistics(year);
        return ResponseEntity.ok(monthlySales);
    }
    
    @GetMapping("/analytics/sales-by-category")
    public ResponseEntity<List<Object[]>> getSalesByCategory() {
        List<Object[]> salesByCategory = complexQueryService.getSalesStatisticsByCategoryJPQL();
        return ResponseEntity.ok(salesByCategory);
    }
    
    @GetMapping("/analytics/order-statistics")
    public ResponseEntity<List<Object[]>> getOrderStatistics() {
        List<Object[]> orderStats = complexQueryService.getOrderAggregationStatistics();
        return ResponseEntity.ok(orderStats);
    }
    
    @GetMapping("/analytics/customers-with-orders")
    public ResponseEntity<List<CustomerDTO>> getCustomersWithOrdersInPeriod(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        List<CustomerDTO> customers = complexQueryService.findCustomersWithOrdersInPeriod(start, end);
        return ResponseEntity.ok(customers);
    }
}
