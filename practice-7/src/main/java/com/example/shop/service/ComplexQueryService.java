package com.example.shop.service;

import com.example.shop.domain.Customer;
import com.example.shop.domain.Order;
import com.example.shop.domain.Product;
import com.example.shop.domain.enums.OrderStatus;
import com.example.shop.dto.CustomerDTO;
import com.example.shop.dto.OrderDTO;
import com.example.shop.dto.ProductDTO;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ComplexQueryService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // ========== JPQL QUERIES ==========
    
    /**
     * JPQL запит: Знайти всі продукти з категорії та ціновим діапазоном
     */
    public List<ProductDTO> findProductsByCategoryAndPriceRangeJPQL(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        String jpql = "SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC";
        
        TypedQuery<Product> query = entityManager.createQuery(jpql, Product.class);
        query.setParameter("categoryId", categoryId);
        query.setParameter("minPrice", minPrice);
        query.setParameter("maxPrice", maxPrice);
        
        return query.getResultList().stream()
            .map(this::convertToProductDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * JPQL запит: Знайти замовлення з детальною інформацією про клієнта та продукти
     */
    public List<OrderDTO> findOrdersWithDetailsJPQL() {
        String jpql = "SELECT DISTINCT o FROM Order o " +
                     "LEFT JOIN FETCH o.customer c " +
                     "LEFT JOIN FETCH o.products p " +
                     "WHERE o.status = :status " +
                     "ORDER BY o.createdAt DESC";
        
        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class);
        query.setParameter("status", OrderStatus.CONFIRMED);
        
        return query.getResultList().stream()
            .map(this::convertToOrderDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * JPQL запит: Статистика продажів по категоріях
     */
    public List<Object[]> getSalesStatisticsByCategoryJPQL() {
        String jpql = "SELECT c.name, COUNT(p.id), AVG(p.price), SUM(p.price) " +
                     "FROM Category c " +
                     "LEFT JOIN c.products p " +
                     "LEFT JOIN p.orders o " +
                     "WHERE o.status = :status " +
                     "GROUP BY c.id, c.name " +
                     "ORDER BY SUM(p.price) DESC";
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("status", OrderStatus.DELIVERED);
        
        return query.getResultList();
    }
    
    // ========== CRITERIA API ==========
    
    /**
     * Criteria API: Динамічний пошук продуктів з фільтрами
     */
    public List<ProductDTO> findProductsWithDynamicFilters(String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> product = cq.from(Product.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (name != null && !name.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(product.get("name")), "%" + name.toLowerCase() + "%"));
        }
        
        if (categoryId != null) {
            predicates.add(cb.equal(product.get("category").get("id"), categoryId));
        }
        
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(product.get("price"), minPrice));
        }
        
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(product.get("price"), maxPrice));
        }
        
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(product.get("price")));
        
        TypedQuery<Product> query = entityManager.createQuery(cq);
        return query.getResultList().stream()
            .map(this::convertToProductDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Criteria API: Пошук клієнтів з замовленнями за період
     */
    public List<CustomerDTO> findCustomersWithOrdersInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Customer> cq = cb.createQuery(Customer.class);
        Root<Customer> customer = cq.from(Customer.class);
        Join<Customer, Order> orders = customer.join("orders", JoinType.INNER);
        
        cq.where(cb.between(orders.get("createdAt"), startDate, endDate));
        cq.distinct(true);
        cq.orderBy(cb.asc(customer.get("lastName")), cb.asc(customer.get("firstName")));
        
        TypedQuery<Customer> query = entityManager.createQuery(cq);
        return query.getResultList().stream()
            .map(this::convertToCustomerDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Criteria API: Агрегація даних по замовленнях
     */
    public List<Object[]> getOrderAggregationStatistics() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<Order> order = cq.from(Order.class);
        
        cq.select(cb.array(
            order.get("status"),
            cb.count(order.get("id")),
            cb.sum(order.get("totalAmount")),
            cb.avg(order.get("totalAmount"))
        ));
        
        cq.groupBy(order.get("status"));
        cq.orderBy(cb.asc(order.get("status")));
        
        TypedQuery<Object[]> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
    
    // ========== NATIVE SQL QUERIES ==========
    
    /**
     * Нативний SQL: Складний запит з підзапитами та агрегацією
     */
    public List<Object[]> getTopCustomersByTotalSpent(int limit) {
        String sql = "SELECT c.id, c.first_name, c.last_name, c.email, " +
                    "COUNT(o.id) as order_count, " +
                    "SUM(o.total_amount) as total_spent, " +
                    "AVG(o.total_amount) as avg_order_amount " +
                    "FROM customers c " +
                    "LEFT JOIN orders o ON c.id = o.customer_id " +
                    "WHERE o.status = 'DELIVERED' OR o.status IS NULL " +
                    "GROUP BY c.id, c.first_name, c.last_name, c.email " +
                    "HAVING COUNT(o.id) > 0 " +
                    "ORDER BY total_spent DESC " +
                    "LIMIT :limit";
        
        return entityManager.createNativeQuery(sql)
            .setParameter("limit", limit)
            .getResultList();
    }
    
    /**
     * Нативний SQL: Продукти з найвищими продажами
     */
    public List<Object[]> getTopSellingProducts(int limit) {
        String sql = "SELECT p.id, p.name, p.price, c.name as category_name, " +
                    "COUNT(op.product_id) as times_ordered, " +
                    "SUM(o.total_amount) as total_revenue " +
                    "FROM products p " +
                    "LEFT JOIN order_products op ON p.id = op.product_id " +
                    "LEFT JOIN orders o ON op.order_id = o.id " +
                    "LEFT JOIN categories c ON p.category_id = c.id " +
                    "WHERE o.status = 'DELIVERED' OR o.status IS NULL " +
                    "GROUP BY p.id, p.name, p.price, c.name " +
                    "HAVING COUNT(op.product_id) > 0 " +
                    "ORDER BY times_ordered DESC, total_revenue DESC " +
                    "LIMIT :limit";
        
        return entityManager.createNativeQuery(sql)
            .setParameter("limit", limit)
            .getResultList();
    }
    
    /**
     * Нативний SQL: Місячна статистика продажів
     */
    public List<Object[]> getMonthlySalesStatistics(int year) {
        String sql = "SELECT EXTRACT(MONTH FROM o.created_at) as month, " +
                    "COUNT(o.id) as order_count, " +
                    "SUM(o.total_amount) as total_revenue, " +
                    "AVG(o.total_amount) as avg_order_amount " +
                    "FROM orders o " +
                    "WHERE EXTRACT(YEAR FROM o.created_at) = :year " +
                    "AND o.status = 'DELIVERED' " +
                    "GROUP BY EXTRACT(MONTH FROM o.created_at) " +
                    "ORDER BY month";
        
        return entityManager.createNativeQuery(sql)
            .setParameter("year", year)
            .getResultList();
    }
    
    // ========== SPECIFICATIONS (JPA Specifications) ==========
    
    /**
     * Специфікація для динамічної фільтрації продуктів
     */
    public List<ProductDTO> findProductsWithSpecifications(String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        // Це приклад того, як можна використовувати JPA Specifications
        // Для повної реалізації потрібно створити ProductSpecification клас
        
        // Поки що використовуємо Criteria API як альтернативу
        return findProductsWithDynamicFilters(name, categoryId, minPrice, maxPrice);
    }
    
    // ========== HELPER METHODS ==========
    
    private ProductDTO convertToProductDTO(Product product) {
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
                .collect(Collectors.toList()));
            dto.setProductNames(order.getProducts().stream()
                .map(Product::getName)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    private CustomerDTO convertToCustomerDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setAddress(customer.getAddress());
        dto.setPhone(customer.getPhone());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }
}
