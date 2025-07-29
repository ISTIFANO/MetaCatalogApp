package com.example.woocommerceintegration.controller;

import com.example.woocommerceintegration.Repository.ApiKeyRepository;
import com.example.woocommerceintegration.Service.WooCommerceService;
import com.example.woocommerceintegration.entity.Apikeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private WooCommerceService wooCommerceService;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    /**
     * Endpoint simplifié pour générer l'URL d'autorisation
     * L'utilisateur ne fournit que l'URL de son site
     */
    @PostMapping("/generate-auth-url")
    public ResponseEntity<Map<String, String>> generateAuthUrl(@RequestBody Map<String, String> request) {
        try {
            String storeUrl = request.get("website");

            if (storeUrl == null || storeUrl.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "website is required");
                error.put("message", "Please provide your WooCommerce website URL");
                return ResponseEntity.badRequest().body(error);
            }

            String authUrl = wooCommerceService.generateAuthUrl(storeUrl);

            Map<String, String> response = new HashMap<>();
            response.put("auth_url", authUrl);
            response.put("website", storeUrl);
            response.put("message", "Click on the auth_url to authorize your WooCommerce store");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error generating auth URL: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Endpoint pour récupérer les produits avec filtres et statistiques
     */
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) String website,
            @RequestParam(defaultValue = "true") boolean useHttps,
            @RequestParam(required = false) String status,           // publish, draft, private
            @RequestParam(required = false) String stock_status,     // instock, outofstock, onbackorder
            @RequestParam(required = false) String on_sale,          // true, false
            @RequestParam(required = false) String featured,         // true, false
            @RequestParam(required = false) String category,         // ID de catégorie
            @RequestParam(required = false) String search,           // Recherche par nom
            @RequestParam(defaultValue = "10") int per_page,         // Nombre par page
            @RequestParam(defaultValue = "1") int page) {            // Page
        try {
            System.out.println("=== GET PRODUCTS WITH FILTERS DEBUG ===");

            Apikeys apiKey;
            if (website != null && !website.trim().isEmpty()) {
                List<Apikeys> apiKeys = apiKeyRepository.findByWebsite(website);
                if (apiKeys.isEmpty()) {
                    return ResponseEntity.badRequest().body("No API keys found for website: " + website);
                }
                apiKey = apiKeys.get(0);
            } else {
                apiKey = apiKeyRepository.findFirstByOrderByIdDesc();
                if (apiKey == null) {
                    return ResponseEntity.badRequest().body("No API keys found. Please authorize first.");
                }
            }

            String storeUrl = apiKey.getWebsite();
            if (storeUrl == null || storeUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("No store URL found for this API key");
            }

            // Construire les filtres
            Map<String, String> filters = new HashMap<>();
            if (status != null) filters.put("status", status);
            if (stock_status != null) filters.put("stock_status", stock_status);
            if (on_sale != null) filters.put("on_sale", on_sale);
            if (featured != null) filters.put("featured", featured);
            if (category != null) filters.put("category", category);
            if (search != null) filters.put("search", search);
            filters.put("per_page", String.valueOf(per_page));
            filters.put("page", String.valueOf(page));

            Map<String, Object> result = wooCommerceService.fetchProductsWithStats(
                    storeUrl,
                    apiKey.getConsumerKey(),
                    apiKey.getConsumerSecret(),
                    useHttps,
                    filters
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println("Controller Error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error fetching products: " + e.getMessage());
        }
    }

    /**
     * Endpoint pour obtenir seulement les statistiques
     */
    @GetMapping("/products/stats")
    public ResponseEntity<?> getProductStats(@RequestParam(required = false) String website) {
        try {
            Apikeys apiKey;
            if (website != null && !website.trim().isEmpty()) {
                List<Apikeys> apiKeys = apiKeyRepository.findByWebsite(website);
                if (apiKeys.isEmpty()) {
                    return ResponseEntity.badRequest().body("No API keys found for website: " + website);
                }
                apiKey = apiKeys.get(0);
            } else {
                apiKey = apiKeyRepository.findFirstByOrderByIdDesc();
                if (apiKey == null) {
                    return ResponseEntity.badRequest().body("No API keys found. Please authorize first.");
                }
            }

            Map<String, Object> result = wooCommerceService.fetchProductsWithStats(
                    apiKey.getWebsite(),
                    apiKey.getConsumerKey(),
                    apiKey.getConsumerSecret(),
                    true,
                    Map.of("per_page", "100") // Récupérer plus de produits pour les stats
            );

            Map<String, Object> response = new HashMap<>();
            response.put("website", apiKey.getWebsite());
            response.put("statistics", result.get("statistics"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching statistics: " + e.getMessage());
        }
    }

    /**
     * Endpoint pour lister tous les sites enregistrés
     */
    @GetMapping("/sites")
    public ResponseEntity<?> getAllSites() {
        try {
            List<Apikeys> allApiKeys = apiKeyRepository.findAllByOrderByCreatedAtDesc();

            Map<String, Object> response = new HashMap<>();
            response.put("total", allApiKeys.size());
            response.put("sites", allApiKeys.stream().map(key -> {
                Map<String, Object> site = new HashMap<>();
                site.put("id", key.getId());
                site.put("website", key.getWebsite());
                site.put("user_id", key.getUserId());
                site.put("permissions", key.getKeyPermissions());
                site.put("created_at", key.getCreatedAt());
                site.put("is_active", key.getIsActive());
                return site;
            }).toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching sites: " + e.getMessage());
        }
    }
}