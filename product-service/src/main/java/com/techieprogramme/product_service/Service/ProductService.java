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

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());

        productRepository.save(product);

        log.info("Product {} is saved", product.getId());

    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductResponse> productResponses = new ArrayList<>();

        for (Product product : products) {
            ProductResponse productResponse = new ProductResponse();
            productResponse.setId(product.getId());
            productResponse.setDescription(product.getDescription());
            productResponse.setName(product.getName());
            productResponse.setPrice(product.getPrice());
            productResponses.add(productResponse);
        }

        return productResponses;
    }
}
