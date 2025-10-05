package ua.edu.practice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.edu.practice.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Сервіс для обробки платежів
 */
@Service
@Slf4j
public class PaymentService {
    
    // Імітація балансу клієнтів
    private final ConcurrentMap<String, BigDecimal> customerBalances = new ConcurrentHashMap<>();
    
    public PaymentService() {
        // Ініціалізація тестових балансів
        customerBalances.put("customer1", new BigDecimal("1000.00"));
        customerBalances.put("customer2", new BigDecimal("500.00"));
        customerBalances.put("customer3", new BigDecimal("2000.00"));
        customerBalances.put("admin", new BigDecimal("10000.00"));
    }
    
    /**
     * Перевірити достатність коштів
     */
    public boolean hasSufficientFunds(String customerId, BigDecimal amount) {
        BigDecimal balance = customerBalances.getOrDefault(customerId, BigDecimal.ZERO);
        boolean sufficient = balance.compareTo(amount) >= 0;
        
        log.debug("Customer {} balance: {}, required: {}, sufficient: {}", 
                customerId, balance, amount, sufficient);
        
        return sufficient;
    }
    
    /**
     * Обробити платіж
     */
    public void processPayment(String customerId, BigDecimal amount) {
        log.info("Processing payment for customer: {} amount: {}", customerId, amount);
        
        if (!hasSufficientFunds(customerId, amount)) {
            throw new InsufficientFundsException("Insufficient funds for customer: " + customerId);
        }
        
        // Імітація обробки платежу
        try {
            Thread.sleep(100); // Імітація затримки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Списання коштів
        BigDecimal currentBalance = customerBalances.getOrDefault(customerId, BigDecimal.ZERO);
        BigDecimal newBalance = currentBalance.subtract(amount);
        customerBalances.put(customerId, newBalance);
        
        log.info("Payment processed. Customer {} new balance: {}", customerId, newBalance);
    }
    
    /**
     * Отримати баланс клієнта
     */
    public BigDecimal getCustomerBalance(String customerId) {
        return customerBalances.getOrDefault(customerId, BigDecimal.ZERO);
    }
    
    /**
     * Поповнити баланс клієнта
     */
    public void addFunds(String customerId, BigDecimal amount) {
        BigDecimal currentBalance = customerBalances.getOrDefault(customerId, BigDecimal.ZERO);
        BigDecimal newBalance = currentBalance.add(amount);
        customerBalances.put(customerId, newBalance);
        
        log.info("Funds added. Customer {} new balance: {}", customerId, newBalance);
    }
}
