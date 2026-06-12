package com.techieprogramme.product_service.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.techieprogramme.product_service.dto.ProductRequest;
import com.techieprogramme.product_service.dto.ProductResponse;
import com.techieprogramme.product_service.model.Product;
import com.techieprogramme.product_service.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(ProductRequest productRequest) {
        Product product = new Product();
        product.setSkuCode(productRequest.getSkuCode());

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategory(productRequest.getCategory());
        product.setImageUrl(productRequest.getImageUrl());
        product.setVendorName(productRequest.getVendorName());

        productRepository.save(product);

        log.info("Product {} is saved", product.getId());

    }

    public List<ProductResponse> getAllProducts(String vendorName) {
        List<Product> products;
        if (vendorName != null && !vendorName.isEmpty()) {
            products = productRepository.findByVendorName(vendorName);
        } else {
            products = productRepository.findAll();
        }
        List<ProductResponse> productResponses = new ArrayList<>();

        for (Product product : products) {
            ProductResponse productResponse = new ProductResponse();
            productResponse.setId(product.getId());
            productResponse.setSkuCode(product.getSkuCode());
            productResponse.setDescription(product.getDescription());
            productResponse.setName(product.getName());
            productResponse.setPrice(product.getPrice());
            productResponse.setCategory(product.getCategory());
            productResponse.setImageUrl(product.getImageUrl());
            productResponse.setVendorName(product.getVendorName());
            productResponses.add(productResponse);
        }

        return productResponses;
    }

    public void updateProduct(String id, ProductRequest productRequest) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setSkuCode(productRequest.getSkuCode());
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategory(productRequest.getCategory());
        product.setImageUrl(productRequest.getImageUrl());
        product.setVendorName(productRequest.getVendorName());
        productRepository.save(product);
        log.info("Product {} is updated", product.getId());
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
        log.info("Product {} is deleted", id);
    }
}
