package com.example.shop.service;

import com.example.shop.domain.Customer;
import com.example.shop.domain.Order;
import com.example.shop.domain.Product;
import com.example.shop.domain.enums.OrderStatus;
import com.example.shop.dto.OrderDTO;
import com.example.shop.exception.BusinessException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class TransactionService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Створення замовлення з перевіркою наявності товарів
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderDTO createOrderWithProductAvailabilityCheck(OrderDTO orderDTO) {
        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + orderDTO.getCustomerId()));
        
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Перевірка наявності товарів та додавання до замовлення
        if (orderDTO.getProductIds() != null && !orderDTO.getProductIds().isEmpty()) {
            for (Long productId : orderDTO.getProductIds()) {
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
                
                // Тут можна додати логіку перевірки наявності товару на складі
                // Наприклад, перевірити кількість товару в інвентарі
                if (!isProductAvailable(product)) {
                    throw new BusinessException("Product " + product.getName() + " is not available");
                }
                
                order.addProduct(product);
                totalAmount = totalAmount.add(product.getPrice());
            }
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        return convertToOrderDTO(savedOrder);
    }
    
    /**
     * Оновлення статусу замовлення з відповідними змінами в інвентарі
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderDTO updateOrderStatusWithInventoryChanges(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        // Логіка зміни інвентарю залежно від статусу
        if (newStatus == OrderStatus.CONFIRMED && oldStatus == OrderStatus.PENDING) {
            // При підтвердженні замовлення - зменшуємо кількість товарів на складі
            updateInventoryForConfirmedOrder(order);
        } else if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            // При скасуванні замовлення - повертаємо товари на склад
            updateInventoryForCancelledOrder(order);
        } else if (newStatus == OrderStatus.DELIVERED && oldStatus == OrderStatus.SHIPPED) {
            // При доставці - оновлюємо статистику
            updateStatisticsForDeliveredOrder(order);
        }
        
        Order savedOrder = orderRepository.save(order);
        return convertToOrderDTO(savedOrder);
    }
    
    /**
     * Масове оновлення цін для категорії продуктів
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int massUpdatePricesForCategory(Long categoryId, BigDecimal priceIncrease, BigDecimal priceMultiplier) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        
        if (products.isEmpty()) {
            throw new BusinessException("No products found for category with id: " + categoryId);
        }
        
        int updatedCount = 0;
        for (Product product : products) {
            BigDecimal newPrice;
            
            if (priceIncrease != null) {
                newPrice = product.getPrice().add(priceIncrease);
            } else if (priceMultiplier != null) {
                newPrice = product.getPrice().multiply(priceMultiplier);
            } else {
                throw new BusinessException("Either price increase or price multiplier must be provided");
            }
            
            if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("New price must be greater than zero for product: " + product.getName());
            }
            
            product.setPrice(newPrice);
            updatedCount++;
        }
        
        productRepository.saveAll(products);
        return updatedCount;
    }
    
    /**
     * Обробка скасування замовлення з поверненням товарів на склад
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderDTO cancelOrderWithInventoryReturn(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled");
        }
        
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel delivered order");
        }
        
        // Повертаємо товари на склад
        returnInventoryForCancelledOrder(order);
        
        // Оновлюємо статус замовлення
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        
        return convertToOrderDTO(savedOrder);
    }
    
    // Приватні допоміжні методи
    
    private boolean isProductAvailable(Product product) {
        // Тут можна реалізувати логіку перевірки наявності товару
        // Наприклад, перевірити кількість в інвентарі
        // Поки що повертаємо true для всіх товарів
        return true;
    }
    
    private void updateInventoryForConfirmedOrder(Order order) {
        // Логіка зменшення кількості товарів на складі
        for (Product product : order.getProducts()) {
            // Тут можна оновити кількість товару в інвентарі
            System.out.println("Reducing inventory for product: " + product.getName());
        }
    }
    
    private void updateInventoryForCancelledOrder(Order order) {
        // Логіка повернення товарів на склад
        for (Product product : order.getProducts()) {
            // Тут можна збільшити кількість товару в інвентарі
            System.out.println("Returning inventory for product: " + product.getName());
        }
    }
    
    private void updateStatisticsForDeliveredOrder(Order order) {
        // Логіка оновлення статистики доставки
        System.out.println("Updating delivery statistics for order: " + order.getId());
    }
    
    private void returnInventoryForCancelledOrder(Order order) {
        // Логіка повернення товарів на склад при скасуванні
        for (Product product : order.getProducts()) {
            // Тут можна збільшити кількість товару в інвентарі
            System.out.println("Returning inventory for cancelled order, product: " + product.getName());
        }
    }
    
    private OrderDTO convertToOrderDTO(Order order) {
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
                .collect(java.util.stream.Collectors.toList()));
            dto.setProductNames(order.getProducts().stream()
                .map(Product::getName)
                .collect(java.util.stream.Collectors.toList()));
        }
        
        return dto;
    }
}
