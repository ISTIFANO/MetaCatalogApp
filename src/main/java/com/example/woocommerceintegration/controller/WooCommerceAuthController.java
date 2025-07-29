package com.example.woocommerceintegration.controller;
import com.example.woocommerceintegration.Service.WooCommerceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wc-auth/v1")
public class WooCommerceAuthController {

    @Autowired
    private WooCommerceService wooCommerceService;

    @GetMapping("/authorize")
    public ResponseEntity<String> authorize(
            @RequestParam String url_site) {

        String authorizationUrl = wooCommerceService.generateAuthUrl(url_site);
        return ResponseEntity.ok("Please visit the following URL to authorize: " + authorizationUrl);
    }
}
