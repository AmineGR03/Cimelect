package com.cimelect.service;

import com.cimelect.dto.product.ProductRequest;
import com.cimelect.dto.product.ProductResponse;
import com.cimelect.entity.Product;
import com.cimelect.exception.ResourceNotFoundException;
import com.cimelect.mapper.ProductMapper;
import com.cimelect.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(productMapper::toResponse).toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        return productMapper.toResponse(productRepository.save(productMapper.toEntity(request)));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));
        productMapper.update(product, request);
        return productMapper.toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));
        product.setActive(false);
    }
}
