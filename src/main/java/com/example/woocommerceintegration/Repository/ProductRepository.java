package com.example.woocommerceintegration.Repository;

import com.example.woocommerceintegration.dtos.WooCommerceProduct;
import com.example.woocommerceintegration.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
    Optional<ProductEntity> findByWooCommerceId(String wooCommerceId);

    Optional<ProductEntity> findByCompanyId(String companyId);
}
