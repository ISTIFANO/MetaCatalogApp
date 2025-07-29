package com.example.woocommerceintegration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "apikeys")
public class Apikeys {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String consumerKey;
    private String consumerSecret;
    private String keyPermissions;
    private String userId;
    private Long keyId;

    private String website;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}