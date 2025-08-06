package com.example.woocommerceintegration.dtos;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WooCommerceProduct {
    private Long id;
    private String name;
    private String slug;
    private String status;
    private String type;
    private String description;

    @JsonProperty("short_description")
    private String shortDescription;

    private String price;

    @JsonProperty("regular_price")
    private String regularPrice;

    @JsonProperty("sale_price")
    private String salePrice;

    @JsonProperty("on_sale")
    private Boolean onSale;

    private String sku;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @JsonProperty("manage_stock")
    private Boolean manageStock;

    @JsonProperty("stock_status")
    private String stockStatus;

    // Champs supplémentaires utiles de WooCommerce
    private String permalink;

    @JsonProperty("date_created")
    private String dateCreated;

    @JsonProperty("date_modified")
    private String dateModified;

    private Boolean featured;

    @JsonProperty("catalog_visibility")
    private String catalogVisibility;

    private Boolean purchasable;
    @JsonProperty("categories")
    private List<Category> categories;

    @JsonProperty("total_sales")
    private Integer totalSales;

    private Boolean virtual;
    private Boolean downloadable;

    @JsonProperty("tax_status")
    private String taxStatus;

    @JsonProperty("tax_class")
    private String taxClass;

    private String weight;

    @JsonProperty("images")
    private List<ProductImage> images;

    @JsonProperty("shipping_required")
    private Boolean shippingRequired;

    @JsonProperty("reviews_allowed")
    private Boolean reviewsAllowed;

    @JsonProperty("average_rating")
    private String averageRating;

    @JsonProperty("rating_count")
    private Integer ratingCount;
}
