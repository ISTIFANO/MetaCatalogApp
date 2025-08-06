package com.example.woocommerceintegration.Service;

import com.example.woocommerceintegration.dtos.WooCommerceProduct;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@Service
public class WooCommerceService {

    @Value("${woocommerce.app.name}")
    private String appName;

    @Value("${woocommerce.scope}")
    private String scope;

    @Value("${woocommerce.return.url}")
    private String returnUrl;

    @Value("${woocommerce.callback.url}")
    private String callbackUrl;

    @Value("${woocommerce.user.id.prefix}")
    private String userIdPrefix;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WooCommerceService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Génère un ID utilisateur unique
     */
    private String generateUserId() {
        return userIdPrefix + System.currentTimeMillis();
    }

    /**
     * Génère l'URL d'autorisation avec seulement l'URL du site fournie par l'utilisateur
     */
    public String generateAuthUrl(String storeUrl) {
        try {
            // Nettoyer l'URL
            if (storeUrl.endsWith("/")) {
                storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
            }

            // Générer un ID utilisateur unique
            String userId = generateUserId();

            // Ajouter l'URL du site au callback pour le traçage
            String enhancedCallbackUrl = callbackUrl + "?website=" + URLEncoder.encode(storeUrl, StandardCharsets.UTF_8);

            String params = String.format(
                    "app_name=%s&scope=%s&user_id=%s&return_url=%s&callback_url=%s",
                    URLEncoder.encode(appName, StandardCharsets.UTF_8),
                    URLEncoder.encode(scope, StandardCharsets.UTF_8),
                    URLEncoder.encode(userId, StandardCharsets.UTF_8),
                    URLEncoder.encode(returnUrl, StandardCharsets.UTF_8),
                    URLEncoder.encode(enhancedCallbackUrl, StandardCharsets.UTF_8)
            );

            return storeUrl + "/wc-auth/v1/authorize?" + params;
        } catch (Exception e) {
            throw new RuntimeException("Error generating auth URL", e);
        }
    }

    /**
     * Récupère les produits avec statistiques
     */
    public Map<String, Object> fetchProductsWithStats(String storeUrl, String consumerKey, String consumerSecret, boolean useHttps, Map<String, String> filters) {
        try {
            String endpoint = "/wp-json/wc/v3/products";
            String url = storeUrl + endpoint;

            // Ajouter les filtres à l'URL
            if (filters != null && !filters.isEmpty()) {
                StringBuilder queryParams = new StringBuilder("?");
                for (Map.Entry<String, String> filter : filters.entrySet()) {
                    if (queryParams.length() > 1) {
                        queryParams.append("&");
                    }
                    queryParams.append(filter.getKey()).append("=").append(URLEncoder.encode(filter.getValue(), StandardCharsets.UTF_8));
                }
                url += queryParams.toString();
            }

            System.out.println("=== FETCH PRODUCTS WITH STATS DEBUG ===");
            System.out.println("Store URL: " + storeUrl);
            System.out.println("Full URL: " + url);
            System.out.println("Filters: " + filters);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (useHttps) {
                String auth = consumerKey + ":" + consumerSecret;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                headers.set("Authorization", "Basic " + encodedAuth);
            } else {
                Map<String, String> oauthParams = generateOAuthParams(consumerKey, consumerSecret, "GET", url);
                StringBuilder authHeader = new StringBuilder("OAuth ");
                for (Map.Entry<String, String> entry : oauthParams.entrySet()) {
                    authHeader.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\", ");
                }
                String authHeaderString = authHeader.toString();
                if (authHeaderString.endsWith(", ")) {
                    authHeaderString = authHeaderString.substring(0, authHeaderString.length() - 2);
                }
                headers.set("Authorization", authHeaderString);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<WooCommerceProduct> wooCommerceProducts = objectMapper.readValue(response.getBody(), new TypeReference<List<WooCommerceProduct>>() {});

                // Calculer les statistiques
                Map<String, Object> stats = calculateProductStats(wooCommerceProducts);

                Map<String, Object> result = new HashMap<>();
                result.put("products", wooCommerceProducts);
                result.put("statistics", stats);
                result.put("total", wooCommerceProducts.size());
                result.put("store_url", storeUrl);
                result.put("applied_filters", filters);
System.out.println(result.get("statistics"));
                return result;
            } else {
                throw new RuntimeException("Failed to fetch products. Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.out.println("Error fetching products: " + e.getMessage());
            throw new RuntimeException("Error fetching products: " + e.getMessage(), e);
        }
    }

    /**
     * Calcule les statistiques des produits
     */
    private Map<String, Object> calculateProductStats(List<WooCommerceProduct> wooCommerceProducts) {
        Map<String, Object> stats = new HashMap<>();

        if (wooCommerceProducts.isEmpty()) {
            return stats;
        }

        // Statistiques générales
        int totalProducts = wooCommerceProducts.size();
        int publishedProducts = 0;
        int draftProducts = 0;
        int inStockProducts = 0;
        int outOfStockProducts = 0;
        int onSaleProducts = 0;
        int featuredProducts = 0;

        double totalValue = 0.0;
        double averagePrice = 0.0;
        int totalStockQuantity = 0;

        Map<String, Integer> categoryCount = new HashMap<>();
        Map<String, Integer> statusCount = new HashMap<>();

        for (WooCommerceProduct wooCommerceProduct : wooCommerceProducts) {
            // Statut
            if ("publish".equals(wooCommerceProduct.getStatus())) {
                publishedProducts++;
            } else if ("draft".equals(wooCommerceProduct.getStatus())) {
                draftProducts++;
            }
            statusCount.put(wooCommerceProduct.getStatus(), statusCount.getOrDefault(wooCommerceProduct.getStatus(), 0) + 1);

            // Stock
            if ("instock".equals(wooCommerceProduct.getStockStatus())) {
                inStockProducts++;
            } else if ("outofstock".equals(wooCommerceProduct.getStockStatus())) {
                outOfStockProducts++;
            }

            // Prix et vente
            if (wooCommerceProduct.getOnSale() != null && wooCommerceProduct.getOnSale()) {
                onSaleProducts++;
            }

            if (wooCommerceProduct.getFeatured() != null && wooCommerceProduct.getFeatured()) {
                featuredProducts++;
            }

            // Calculs numériques
            if (wooCommerceProduct.getPrice() != null && !wooCommerceProduct.getPrice().isEmpty()) {
                try {
                    totalValue += Double.parseDouble(wooCommerceProduct.getPrice());
                } catch (NumberFormatException e) {
                    // Ignorer les prix non numériques
                }
            }

            if (wooCommerceProduct.getStockQuantity() != null) {
                totalStockQuantity += wooCommerceProduct.getStockQuantity();
            }
        }

        if (totalProducts > 0) {
            averagePrice = totalValue / totalProducts;
        }

        // Construire les statistiques
        stats.put("total_products", totalProducts);
        stats.put("published_products", publishedProducts);
        stats.put("draft_products", draftProducts);
        stats.put("in_stock_products", inStockProducts);
        stats.put("out_of_stock_products", outOfStockProducts);
        stats.put("on_sale_products", onSaleProducts);
        stats.put("featured_products", featuredProducts);
        stats.put("total_value", Math.round(totalValue * 100.0) / 100.0);
        stats.put("average_price", Math.round(averagePrice * 100.0) / 100.0);
        stats.put("total_stock_quantity", totalStockQuantity);
        stats.put("status_breakdown", statusCount);

        return stats;
    }

    // Méthodes OAuth existantes...
    private Map<String, String> generateOAuthParams(String consumerKey, String consumerSecret, String method, String url) {
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("oauth_consumer_key", consumerKey);
            params.put("oauth_signature_method", "HMAC-SHA1");
            params.put("oauth_timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("oauth_nonce", generateNonce());

            StringBuilder paramString = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (paramString.length() > 0) {
                    paramString.append("&");
                }
                paramString.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            String baseString = method.toUpperCase() + "&" +
                    URLEncoder.encode(url, StandardCharsets.UTF_8) + "&" +
                    URLEncoder.encode(paramString.toString(), StandardCharsets.UTF_8);

            String signingKey = URLEncoder.encode(consumerSecret, StandardCharsets.UTF_8) + "&";
            String signature = generateHmacSha1Signature(baseString, signingKey);
            params.put("oauth_signature", signature);

            return params;
        } catch (Exception e) {
            throw new RuntimeException("Error generating OAuth parameters", e);
        }
    }

    private String generateNonce() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).replaceAll("[^a-zA-Z0-9]", "");
    }

    private String generateHmacSha1Signature(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
        mac.init(secretKey);
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature);
    }
}