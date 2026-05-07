package edu.cit.sevilla.washmate.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * PayMongo Configuration
 * Loads PayMongo API credentials from environment variables
 * and provides RestTemplate bean for HTTP calls to PayMongo API.
 */
@Configuration
@Getter
public class PayMongoConfig {

    @Value("${paymongo.public-key}")
    private String publicKey;

    @Value("${paymongo.secret-key}")
    private String secretKey;

    @Value("${paymongo.base-url}")
    private String baseUrl;

    @Value("${paymongo.webhook-secret}")
    private String webhookSecret;

    /**
     * Provides RestTemplate bean for making HTTP requests to PayMongo API.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
