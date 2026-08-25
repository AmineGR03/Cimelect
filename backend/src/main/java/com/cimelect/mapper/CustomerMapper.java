package com.cimelect.mapper;

import com.cimelect.dto.customer.CustomerRequest;
import com.cimelect.dto.customer.CustomerResponse;
import com.cimelect.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {
        return Customer.builder()
                .companyName(request.companyName())
                .country(request.country())
                .contactName(request.contactName())
                .email(request.email())
                .phone(request.phone())
                .archived(false)
                .build();
    }

    public void update(Customer customer, CustomerRequest request) {
        customer.setCompanyName(request.companyName());
        customer.setCountry(request.country());
        customer.setContactName(request.contactName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCompanyName(),
                customer.getCountry(),
                customer.getContactName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.isArchived(),
                customer.getCreatedAt()
        );
    }
}
