package ua.edu.practice.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тести безпеки для перевірки автентифікації та авторизації
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("Security Tests")
class SecurityTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("Should allow access to generate order ID without authentication")
    void accessGenerateOrderId_NoAuth_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/generate-order-id"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should deny access to protected endpoints without authentication")
    void accessProtectedEndpoint_NoAuth_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should allow access to protected endpoints with valid authentication")
    @WithMockUser(username = "user", password = "user", roles = "USER")
    void accessProtectedEndpoint_ValidAuth_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should allow admin access to admin-only endpoints")
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    void accessAdminEndpoint_AdminRole_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/orders/statistics"))
                .andExpect(status().isOk());
        
        mockMvc.perform(put("/api/orders/1/status")
                        .with(csrf())
                        .param("status", "PROCESSING"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should deny user access to admin-only endpoints")
    @WithMockUser(username = "user", password = "user", roles = "USER")
    void accessAdminEndpoint_UserRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(get("/api/orders/statistics"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(put("/api/orders/1/status")
                        .with(csrf())
                        .param("status", "PROCESSING"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should allow user access to their own orders")
    @WithMockUser(username = "user", password = "user", roles = "USER")
    void accessUserOrders_UserRole_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/user"))
                .andExpect(status().isOk());
        
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"orderId\":\"ORD-001\",\"customerId\":\"user\",\"productName\":\"Test\",\"amount\":100}"))
                .andExpect(status().isCreated());
    }
    
    @Test
    @DisplayName("Should require CSRF token for state-changing operations")
    @WithMockUser(username = "user", password = "user", roles = "USER")
    void performStateChangingOperation_NoCsrf_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("{\"orderId\":\"ORD-001\",\"customerId\":\"user\",\"productName\":\"Test\",\"amount\":100}"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(delete("/api/orders/ORD-001"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should allow CSRF-exempt operations")
    @WithMockUser(username = "user", password = "user", roles = "USER")
    void performReadOperations_NoCsrf_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/orders/customer/user"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should allow access to H2 console without authentication")
    void accessH2Console_NoAuth_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should handle authentication with basic auth")
    void authenticateWithBasicAuth_ValidCredentials_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/1")
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should reject authentication with invalid credentials")
    void authenticateWithBasicAuth_InvalidCredentials_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/1")
                        .with(httpBasic("invalid", "invalid")))
                .andExpect(status().isUnauthorized());
    }
}
