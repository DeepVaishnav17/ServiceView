package com.techieprogramme.product_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.techieprogramme.product_service.model.Product;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByVendorName(String vendorName);
}
