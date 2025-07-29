package com.example.woocommerceintegration.controller;

import com.example.woocommerceintegration.Service.CatalogService;
import com.example.woocommerceintegration.dtos.CatalogRequestDTO;
import com.example.woocommerceintegration.dtos.ProductItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/catalog")
public class CatalogController {

    @Autowired
    private CatalogService catalogService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitCatalog(
            @RequestHeader("Authorization") String token,
            @RequestHeader("CatalogId") String catalogId,
            @RequestBody CatalogRequestDTO catalogRequest) {

        System.out.println("🔐 Token reçu: " + token);
        System.out.println("📂 Catalog ID: " + catalogId);
        System.out.println("📦 Nombre de produits reçus: " + catalogRequest.getRequests().size());

        // Nettoyer le token
        token = token.replace("Bearer ", "");

        try {
            // ✨ Nouvelle approche : synchronisation automatique par nom de produit
            catalogService.synchronizeProductsWithCatalog(catalogRequest.getRequests(), catalogId, token);

            return ResponseEntity.ok(" Catalogue synchronisé avec succès. Tous les produits manquants ont été ajoutés automatiquement.");

        } catch (Exception e) {
            System.err.println(" Erreur lors de la synchronisation du catalogue: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(" Erreur lors de la synchronisation: " + e.getMessage());
        }
    }
}

