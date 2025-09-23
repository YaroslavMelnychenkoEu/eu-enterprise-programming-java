package com.example.shop.service;

import com.example.shop.domain.Customer;
import com.example.shop.domain.Order;
import com.example.shop.domain.Product;
import com.example.shop.domain.enums.OrderStatus;
import com.example.shop.dto.OrderDTO;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + orderDTO.getCustomerId()));
        
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        
        // Add products to order
        if (orderDTO.getProductIds() != null && !orderDTO.getProductIds().isEmpty()) {
            for (Long productId : orderDTO.getProductIds()) {
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
                order.addProduct(product);
            }
        }
        
        // Calculate total amount
        order.calculateTotalAmount();
        
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return convertToDTO(order);
    }
    
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
            .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByCustomer(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
            .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomerAndDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCustomerIdAndCreatedAtBetween(customerId, startDate, endDate).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByStatusAndCreatedAtBetween(status, startDate, endDate).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return orderRepository.findByTotalAmountBetween(minAmount, maxAmount).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersOrderedByTotalAmount(Pageable pageable) {
        return orderRepository.findAllOrderByTotalAmountDesc(pageable)
            .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersOrderedByCreatedAt(Pageable pageable) {
        return orderRepository.findAllOrderByCreatedAtDesc(pageable)
            .map(this::convertToDTO);
    }
    
    public OrderDTO updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    
    public OrderDTO addProductToOrder(Long orderId, Long productId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        order.addProduct(product);
        order.calculateTotalAmount();
        
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    
    public OrderDTO removeProductFromOrder(Long orderId, Long productId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        order.removeProduct(product);
        order.calculateTotalAmount();
        
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        orderRepository.delete(order);
    }
    
    @Transactional(readOnly = true)
    public Long getOrderCountByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByStatus(OrderStatus status) {
        return orderRepository.getTotalAmountByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getAverageOrderAmountBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.getAverageOrderAmountBetween(startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer().getFullName());
        dto.setCustomerEmail(order.getCustomer().getEmail());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        if (order.getProducts() != null) {
            dto.setProductIds(order.getProducts().stream()
                .map(Product::getId)
                .collect(Collectors.toList()));
            dto.setProductNames(order.getProducts().stream()
                .map(Product::getName)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
}
