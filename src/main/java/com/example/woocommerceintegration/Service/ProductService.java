package com.example.woocommerceintegration.Service;

import com.example.woocommerceintegration.Repository.ProductRepository;
import com.example.woocommerceintegration.dtos.ProductDTO;
import com.example.woocommerceintegration.entity.ProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final RestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    public ProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductEntity saveOrUpdateProduct(ProductDTO dto) {

        ProductEntity product = productRepository.findByWooCommerceId(dto.getWooCommerceId()).orElse(new ProductEntity());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCompanyId(dto.getCompanyId());
        product.setArchived(dto.isArchived());
        product.setRetailerId(dto.getRetailerId());
        product.setCurrency(dto.getCurrency());
        product.setSiteWeb(dto.getSiteWeb());
        product.setImageUrl(dto.getImageUrl());
        product.setAvailability(dto.getAvailability());
        product.setRetailerProductGroupId(dto.getRetailerProductGroupId());
        product.setCategory(dto.getCategory());
        product.setWooCommerceId(dto.getWooCommerceId());
        return productRepository.save(product);
    }
    public List<ProductDTO> fetchUnarchivedProducts(String requestId, String canal) {
System.out.println("Fetching unarchived products from " + canal);
System.out.println("Fetching unarchived products from " + requestId);
        String sourceUrl = "http://localhost:3000/v1/api/products/unarchived_prodcut";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-requestId", requestId);
        headers.set("x-api-canal", canal);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ProductDTO[]> response = restTemplate.exchange(
                sourceUrl,
                HttpMethod.POST,
                entity,
                ProductDTO[].class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch unarchived products.");
        }

        return Arrays.asList(response.getBody());
    }
}
