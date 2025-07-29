package com.example.woocommerceintegration.Repository;


import com.example.woocommerceintegration.entity.Apikeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<Apikeys, Long> {
    Apikeys findFirstByOrderByIdDesc();
    List<Apikeys> findByWebsite(String website);
    Optional<Apikeys> findByConsumerKey(String consumerKey);
    List<Apikeys> findAllByOrderByCreatedAtDesc();
}
