package com.example.woocommerceintegration.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatalogRequestDTO {
    private String item_type;
    private boolean allow_upsert;
    private List<ProductItemDTO> requests;
}
