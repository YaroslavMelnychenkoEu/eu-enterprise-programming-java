package ua.edu.practice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.edu.practice.exception.InsufficientFundsException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Модульні тести для PaymentService
 */
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {
    
    private PaymentService paymentService;
    
    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
    }
    
    @Test
    @DisplayName("Should return true when customer has sufficient funds")
    void hasSufficientFunds_SufficientFunds_ReturnsTrue() {
        // Act
        boolean result = paymentService.hasSufficientFunds("customer1", new BigDecimal("500.00"));
        
        // Assert
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("Should return false when customer has insufficient funds")
    void hasSufficientFunds_InsufficientFunds_ReturnsFalse() {
        // Act
        boolean result = paymentService.hasSufficientFunds("customer1", new BigDecimal("1500.00"));
        
        // Assert
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("Should return false for unknown customer")
    void hasSufficientFunds_UnknownCustomer_ReturnsFalse() {
        // Act
        boolean result = paymentService.hasSufficientFunds("unknown", new BigDecimal("100.00"));
        
        // Assert
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("Should process payment successfully")
    void processPayment_ValidData_Success() {
        // Arrange
        String customerId = "customer1";
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal initialBalance = paymentService.getCustomerBalance(customerId);
        
        // Act
        paymentService.processPayment(customerId, amount);
        
        // Assert
        BigDecimal newBalance = paymentService.getCustomerBalance(customerId);
        assertThat(newBalance).isEqualTo(initialBalance.subtract(amount));
    }
    
    @Test
    @DisplayName("Should throw exception when insufficient funds")
    void processPayment_InsufficientFunds_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment("customer1", new BigDecimal("1500.00")))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds for customer: customer1");
    }
    
    @Test
    @DisplayName("Should get customer balance")
    void getCustomerBalance_ValidCustomer_ReturnsBalance() {
        // Act
        BigDecimal balance = paymentService.getCustomerBalance("customer1");
        
        // Assert
        assertThat(balance).isEqualTo(new BigDecimal("1000.00"));
    }
    
    @Test
    @DisplayName("Should return zero balance for unknown customer")
    void getCustomerBalance_UnknownCustomer_ReturnsZero() {
        // Act
        BigDecimal balance = paymentService.getCustomerBalance("unknown");
        
        // Assert
        assertThat(balance).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("Should add funds to customer balance")
    void addFunds_ValidData_Success() {
        // Arrange
        String customerId = "customer1";
        BigDecimal amount = new BigDecimal("200.00");
        BigDecimal initialBalance = paymentService.getCustomerBalance(customerId);
        
        // Act
        paymentService.addFunds(customerId, amount);
        
        // Assert
        BigDecimal newBalance = paymentService.getCustomerBalance(customerId);
        assertThat(newBalance).isEqualTo(initialBalance.add(amount));
    }
}
