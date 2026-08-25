package com.cimelect.mapper;

import com.cimelect.dto.supplier.SupplierRequest;
import com.cimelect.dto.supplier.SupplierResponse;
import com.cimelect.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public Supplier toEntity(SupplierRequest request) {
        return Supplier.builder()
                .companyName(request.companyName())
                .country(request.country())
                .contactName(request.contactName())
                .email(request.email())
                .phone(request.phone())
                .archived(false)
                .build();
    }

    public void update(Supplier supplier, SupplierRequest request) {
        supplier.setCompanyName(request.companyName());
        supplier.setCountry(request.country());
        supplier.setContactName(request.contactName());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
    }

    public SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getCompanyName(),
                supplier.getCountry(),
                supplier.getContactName(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.isArchived(),
                supplier.getCreatedAt()
        );
    }
}
