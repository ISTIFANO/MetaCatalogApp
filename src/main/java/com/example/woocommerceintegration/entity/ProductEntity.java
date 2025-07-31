package com.example.woocommerceintegration.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class ProductEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "company_id", length = 36)
    private String companyId;

    @Column(name = "archived", nullable = false)
    private boolean archived = false;

    @Column(name = "retailer_id")
    private String retailerId;

    @Column(name = "currency")
    private String currency;

    @Column(name = "site_web")
    private String siteWeb;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "availability")
    private String availability; // inStock or not

    @Column(name = "retailer_product_group_id")
    private String retailerProductGroupId;

//    @ManyToOne
//    @JoinColumn(name = "merchant_id")
//    private MerchantEntity merchant;

    @Column(name = "category")
    private String category;

    @Column(name = "woo_commerce_id")
    private String wooCommerceId;

    public ProductEntity(String name, String description,
                         BigDecimal price, Integer stockQuantity,
                         String companyId, String retailerId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.companyId = companyId;
        this.retailerId = retailerId;
    }
}