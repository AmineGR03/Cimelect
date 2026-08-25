package com.cimelect.mapper;

import com.cimelect.dto.product.ProductRequest;
import com.cimelect.dto.product.ProductResponse;
import com.cimelect.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        return Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .unit(request.unit())
                .active(true)
                .build();
    }

    public void update(Product product, ProductRequest request) {
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setUnit(request.unit());
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getUnit(),
                product.isActive()
        );
    }
}
