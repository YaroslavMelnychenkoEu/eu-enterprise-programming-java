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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тести авторизації для перевірки ролей та дозволів
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("Authorization Tests")
class AuthorizationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("Should allow ADMIN role to access all order endpoints")
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    void adminRole_AllEndpoints_AccessGranted() throws Exception {
        // Act & Assert - Admin can access all endpoints
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/orders/statistics"))
                .andExpect(status().isOk());
        
        mockMvc.perform(put("/api/orders/1/status")
                        .with(csrf())
                        .param("status", "PROCESSING"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/orders/customer/admin"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should allow USER role to access limited endpoints")
    @WithMockUser(username = "user", password = "user", roles = "USER")
    void userRole_LimitedEndpoints_AccessGranted() throws Exception {
        // Act & Assert - User can access limited endpoints
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/orders/customer/user"))
                .andExpect(status().isOk());
        
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"orderId\":\"ORD-001\",\"customerId\":\"user\",\"productName\":\"Test\",\"amount\":100}"))
                .andExpect(status().isCreated());
    }
    
    @Test
    @DisplayName("Should deny USER role access to admin-only endpoints")
    @WithMockUser(username = "user", password = "user", roles = "USER")
    void userRole_AdminEndpoints_AccessDenied() throws Exception {
        // Act & Assert - User cannot access admin endpoints
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
    @DisplayName("Should deny access without proper role")
    @WithMockUser(username = "guest", password = "guest", roles = "GUEST")
    void guestRole_ProtectedEndpoints_AccessDenied() throws Exception {
        // Act & Assert - Guest role has no access
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(get("/api/orders/statistics"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should allow multiple roles for admin user")
    @WithMockUser(username = "admin", password = "admin", roles = {"ADMIN", "USER"})
    void adminWithMultipleRoles_AllEndpoints_AccessGranted() throws Exception {
        // Act & Assert - Admin with multiple roles can access everything
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/orders/statistics"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/orders/customer/admin"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should enforce method-level security annotations")
    @WithMockUser(username = "user", password = "user", roles = "USER")
    void methodLevelSecurity_UserRole_Enforced() throws Exception {
        // Act & Assert - Method-level security should be enforced
        mockMvc.perform(get("/api/orders/statistics"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(put("/api/orders/1/status")
                        .with(csrf())
                        .param("status", "PROCESSING"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should allow method-level access with proper role")
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    void methodLevelSecurity_AdminRole_AccessGranted() throws Exception {
        // Act & Assert - Admin should have access to method-level secured endpoints
        mockMvc.perform(get("/api/orders/statistics"))
                .andExpect(status().isOk());
        
        mockMvc.perform(put("/api/orders/1/status")
                        .with(csrf())
                        .param("status", "PROCESSING"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should handle role hierarchy correctly")
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    void roleHierarchy_AdminRole_HasUserPermissions() throws Exception {
        // Act & Assert - Admin should have both admin and user permissions
        mockMvc.perform(get("/api/orders")) // Admin only
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/orders/1")) // User permission
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/orders/customer/admin")) // User permission
                .andExpect(status().isOk());
    }
}
