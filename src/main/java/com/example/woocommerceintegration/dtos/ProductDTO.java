package com.example.woocommerceintegration.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String companyId;
    private boolean archived;
    private String retailerId;
    private String currency;
    private String siteWeb;
    private String imageUrl;
    private String availability;
    private String retailerProductGroupId;
    private String category;
    private String wooCommerceId;
}

