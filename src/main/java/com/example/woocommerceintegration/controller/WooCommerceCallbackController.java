package com.example.woocommerceintegration.controller;

import com.example.woocommerceintegration.Repository.ApiKeyRepository;
import com.example.woocommerceintegration.entity.Apikeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class WooCommerceCallbackController {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody Map<String, Object> data,
                                                 @RequestParam(required = false) String website) {
        try {
            System.out.println("=== CALLBACK RECEIVED ===");
            System.out.println("Callback data: " + data);
            System.out.println("Website parameter: " + website);

            if (website == null || website.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Website parameter is missing from callback");
            }

            Apikeys apiKey = new Apikeys();
            apiKey.setConsumerKey((String) data.get("consumer_key"));
            apiKey.setConsumerSecret((String) data.get("consumer_secret"));
            apiKey.setKeyPermissions((String) data.get("key_permissions"));
            apiKey.setUserId((String) data.get("user_id"));
            apiKey.setWebsite(website);
            apiKey.setIsActive(true);

            if (data.get("key_id") != null) {
                apiKey.setKeyId(((Number) data.get("key_id")).longValue());
            }

            apiKeyRepository.save(apiKey);

            System.out.println("API keys saved successfully for website: " + website);
            return ResponseEntity.ok("✅ Authorization successful! Your WooCommerce store (" + website + ") is now connected.");

        } catch (Exception e) {
            System.err.println("Error processing callback: " + e.getMessage());
            return ResponseEntity.badRequest().body("❌ Error processing authorization: " + e.getMessage());
        }
    }

    @GetMapping("/success")
    public ResponseEntity<String> handleSuccess() {
        return ResponseEntity.ok("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Authorization Successful</title>
                <style>
                    body { font-family: Arial, sans-serif; text-align: center; margin-top: 50px; }
                    .success { color: green; font-size: 24px; }
                    .info { color: #666; margin: 20px; }
                </style>
            </head>
            <body>
                <div class="success">✅ Authorization Successful!</div>
                <div class="info">Your WooCommerce store has been successfully connected.</div>
                <div class="info">You can now close this window and return to the application.</div>
            </body>
            </html>
            """);
    }
}
