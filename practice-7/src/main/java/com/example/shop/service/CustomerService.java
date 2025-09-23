package com.example.shop.service;

import com.example.shop.domain.Customer;
import com.example.shop.dto.CustomerDTO;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.setFirstName(customerDTO.getFirstName());
        customer.setLastName(customerDTO.getLastName());
        customer.setEmail(customerDTO.getEmail());
        customer.setAddress(customerDTO.getAddress());
        customer.setPhone(customerDTO.getPhone());
        
        Customer savedCustomer = customerRepository.save(customer);
        return convertToDTO(savedCustomer);
    }
    
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return convertToDTO(customer);
    }
    
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        return convertToDTO(customer);
    }
    
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
            .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomersOrderByCreatedAt(Pageable pageable) {
        return customerRepository.findAllOrderByCreatedAtDesc(pageable)
            .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomersOrderByName(Pageable pageable) {
        return customerRepository.findAllOrderByName(pageable)
            .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomersByName(String name) {
        return customerRepository.findByFirstNameOrLastNameContaining(name).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomersByEmail(String email) {
        return customerRepository.findByEmailContaining(email).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return customerRepository.findByCreatedAtBetween(startDate, endDate).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        
        customer.setFirstName(customerDTO.getFirstName());
        customer.setLastName(customerDTO.getLastName());
        customer.setEmail(customerDTO.getEmail());
        customer.setAddress(customerDTO.getAddress());
        customer.setPhone(customerDTO.getPhone());
        
        Customer savedCustomer = customerRepository.save(customer);
        return convertToDTO(savedCustomer);
    }
    
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        
        customerRepository.delete(customer);
    }
    
    @Transactional(readOnly = true)
    public Long getOrderCountByCustomer(Long customerId) {
        return customerRepository.countOrdersByCustomerId(customerId);
    }
    
    @Transactional(readOnly = true)
    public Double getTotalSpentByCustomer(Long customerId) {
        Double totalSpent = customerRepository.getTotalSpentByCustomerId(customerId);
        return totalSpent != null ? totalSpent : 0.0;
    }
    
    private CustomerDTO convertToDTO(Customer customer) {
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
