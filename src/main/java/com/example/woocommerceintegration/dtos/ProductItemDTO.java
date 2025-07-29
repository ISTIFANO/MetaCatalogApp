package com.example.woocommerceintegration.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductItemDTO {
    private String method;
    private String retailer_id;
    private ProductData data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductData {
        private String name;
        private String description;
        private String price;
        private String currency;
        private String availability;
        private String image_url;
        private String url;
        private String brand;

    }
}


