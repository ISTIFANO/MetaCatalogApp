package com.example.woocommerceintegration.Service;

import com.example.woocommerceintegration.dtos.ProductItemDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Récupère tous les noms de produits existants dans le catalogue Facebook
     */
    public Set<String> fetchExistingProductNames(String catalogId, String token) {
        String url = "https://graph.facebook.com/v22.0/" + catalogId + "/products?fields=name&limit=1000";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        Set<String> existingProductNames = new HashSet<>();
        String nextPageUrl = url;

        try {
            while (nextPageUrl != null) {
                ResponseEntity<String> response = restTemplate.exchange(nextPageUrl, HttpMethod.GET, entity, String.class);
                JsonNode root = objectMapper.readTree(response.getBody());

                JsonNode dataNode = root.get("data");
                if (dataNode != null && dataNode.isArray()) {
                    for (JsonNode productNode : dataNode) {
                        JsonNode nameNode = productNode.get("name");
                        if (nameNode != null && !nameNode.isNull()) {
                            String productName = normalizeProductName(nameNode.asText());
                            existingProductNames.add(productName);
                        }
                    }
                }

                JsonNode pagingNode = root.get("paging");
                if (pagingNode != null && pagingNode.get("next") != null) {
                    nextPageUrl = pagingNode.get("next").asText();
                } else {
                    nextPageUrl = null;
                }
            }

            System.out.println("✅ Récupéré " + existingProductNames.size() + " noms de produits existants");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des produits existants: " + e.getMessage());
            e.printStackTrace();
        }

        return existingProductNames;
    }

    /**
     * Méthode principale pour synchroniser une liste de produits avec le catalogue Facebook
     */
    public void synchronizeProductsWithCatalog(List<ProductItemDTO> products, String catalogId, String token) {
        System.out.println("🔄 Début de la synchronisation de " + products.size() + " produits...");

        // 1. Récupérer tous les produits existants du catalogue
        Set<String> existingProductNames = fetchExistingProductNames(catalogId, token);

        // 2. Identifier les produits manquants (comparaison par nom)
        List<ProductItemDTO> missingProducts = products.stream()
                .filter(product -> {
                    String productName = normalizeProductName(product.getData().getName());
                    return !existingProductNames.contains(productName);
                })
                .collect(Collectors.toList());

        System.out.println(" " + existingProductNames.size() + " produits existants dans le catalogue");
        System.out.println(" " + missingProducts.size() + " produits manquants à ajouter");

        // 3. Ajouter automatiquement les produits manquants
        if (!missingProducts.isEmpty()) {
            addMissingProductsToCatalog(missingProducts, catalogId, token);
        } else {
            System.out.println("✅ Tous les produits sont déjà présents dans le catalogue");
        }
    }

    /**
     * Ajoute automatiquement tous les produits manquants au catalogue
     */
    private void addMissingProductsToCatalog(List<ProductItemDTO> missingProducts, String catalogId, String token) {
        System.out.println(" Ajout de " + missingProducts.size() + " produits manquants...");

        int successCount = 0;
        int errorCount = 0;

        for (ProductItemDTO product : missingProducts) {
            try {
                addProductToCatalog(product, catalogId, token);
                successCount++;

                // Pause pour éviter de surcharger l'API
                Thread.sleep(200);

            } catch (Exception e) {
                errorCount++;
                System.err.println(" Erreur lors de l'ajout du produit " + product.getData().getName() + ": " + e.getMessage());
            }
        }

        System.out.println(" Synchronisation terminée: " + successCount + " ajouts réussis, " + errorCount + " erreurs");
    }

    /**
     * Normalise le nom du produit pour la comparaison (supprime espaces, met en minuscules)
     */
    private String normalizeProductName(String productName) {
        if (productName == null) {
            return "";
        }
        return productName.trim().toLowerCase()
                .replaceAll("\\s+", " ") // Remplace plusieurs espaces par un seul
                .replaceAll("[^a-zA-Z0-9\\s]", ""); // Supprime caractères spéciaux pour comparaison plus flexible
    }

    /**
     * Ajoute un produit individuel au catalogue Facebook
     */
    public void addProductToCatalog(ProductItemDTO item, String catalogId, String token) {
        String url = "https://graph.facebook.com/v22.0/" + catalogId + "/products";

        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();

        payload.add("retailer_id", Optional.ofNullable(item.getRetailer_id()).orElse("default-id-" + System.currentTimeMillis()));
        payload.add("name", Optional.ofNullable(item.getData().getName()).orElse("Unnamed Product"));

        String rawDescription = Optional.ofNullable(item.getData().getDescription()).orElse("No description");
        String plainDescription = rawDescription.replaceAll("<[^>]*>", "");
        payload.add("description", plainDescription);

        payload.add("image_url", Optional.ofNullable(item.getData().getImage_url()).orElse("https://example.com/default.jpg"));
        payload.add("url", Optional.ofNullable(item.getData().getUrl()).orElse("https://example.com/product"));

        String price = Optional.ofNullable(item.getData().getPrice()).orElse("0");
        payload.add("price", price);

        String currency = Optional.ofNullable(item.getData().getCurrency()).orElse("MAD");
        payload.add("currency", currency);

        // Validation de la disponibilité
        String availability = Optional.ofNullable(item.getData().getAvailability()).orElse("").trim().toLowerCase();
        List<String> validAvailabilities = Arrays.asList(
                "in stock", "out of stock", "preorder",
                "available for order", "discontinued", "pending", "mark_as_sold"
        );
        if (!validAvailabilities.contains(availability)) {
            availability = "in stock";
        }
        payload.add("availability", availability);

        payload.add("condition", "new");

        // Extraction du domaine comme marque
        try {
            URL urlObj = new URL(item.getData().getUrl());
            String brand = urlObj.getHost().replace("www.", "");
            payload.add("brand", brand);
        } catch (MalformedURLException e) {
            System.err.println("URL invalide pour l'extraction de la marque: " + item.getData().getUrl());
            payload.add("brand", "default-brand");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println(" Produit ajouté: " + item.getData().getName() + " (ID: " + item.getRetailer_id() + ")");
        } catch (HttpClientErrorException e) {
            System.err.println(" Erreur HTTP lors de l'ajout du produit " + item.getData().getName() + ": " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            System.err.println(" Erreur inattendue: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Méthode de compatibilité - récupère les retailer_ids existants (conservée pour compatibilité)
     */
    @Deprecated
    public Set<String> fetchExistingRetailerIds(String catalogId, String token) {
        String url = "https://graph.facebook.com/v22.0/" + catalogId + "/products?fields=retailer_id&limit=1000";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        Set<String> existingRetailerIds = new HashSet<>();

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get("data");
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode productNode : dataNode) {
                    JsonNode retailerIdNode = productNode.get("retailer_id");
                    if (retailerIdNode != null && !retailerIdNode.isNull()) {
                        existingRetailerIds.add(retailerIdNode.asText());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return existingRetailerIds;
    }
}