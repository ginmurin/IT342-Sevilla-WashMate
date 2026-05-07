package edu.cit.sevilla.washmate.service;

import edu.cit.sevilla.washmate.config.PayMongoConfig;
import edu.cit.sevilla.washmate.util.HmacSHA256;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * PayMongo Service
 * Handles all PayMongo API interactions for payment processing.
 * Supports card payments (with 3DS), e-wallet sources (GCash, Maya, GrabPay), and webhook verification.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayMongoService {

    private final RestTemplate restTemplate;
    private final PayMongoConfig payMongoConfig;
    private final ObjectMapper objectMapper;

    // ===== PAYMENT INTENT (CARD PAYMENTS) =====

    /**
     * Create a PayMongo PaymentIntent for card payments.
     * @param amountPHP Amount in Philippine Pesos (will be converted to cents)
     * @return Map with intent ID and client key
     */
    public Map<String, String> createPaymentIntent(BigDecimal amountPHP) {
        try {
            String url = payMongoConfig.getBaseUrl() + "/payment_intents";

            // Convert PHP to cents (multiply by 100)
            long amountInCents = amountPHP.multiply(new BigDecimal("100")).longValue();

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> attributes = new HashMap<>();

            attributes.put("amount", amountInCents);
            attributes.put("currency", "PHP");
            attributes.put("payment_method_allowed", List.of("card"));

            // Enable 3DS secure for card payments
            Map<String, Object> paymentMethodOptions = new HashMap<>();
            Map<String, String> cardOptions = new HashMap<>();
            cardOptions.put("request_three_d_secure", "any");
            paymentMethodOptions.put("card", cardOptions);
            attributes.put("payment_method_options", paymentMethodOptions);
            attributes.put("capture_type", "automatic");

            data.put("attributes", attributes);
            requestBody.put("data", data);

            // Make request with Basic Auth
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
                log.error("PayMongo payment intent creation failed: {}", response.getBody());
                throw new RuntimeException("Failed to create payment intent: " + response.getStatusCode());
            }

            // Parse response
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            Map<String, Object> responseData = (Map<String, Object>) responseBody.get("data");
            String intentId = (String) responseData.get("id");

            Map<String, Object> responseAttributes = (Map<String, Object>) responseData.get("attributes");
            String clientKey = (String) responseAttributes.get("client_key");

            log.info("✅ Created PayMongo PaymentIntent: {}", intentId);
            return Map.of("paymentIntentId", intentId, "clientKey", clientKey);

        } catch (Exception e) {
            log.error("❌ Error creating payment intent", e);
            throw new RuntimeException("PayMongo payment intent creation failed", e);
        }
    }

    // ===== PAYMENT METHOD (CARD) =====

    /**
     * Create a PayMongo PaymentMethod from card details.
     * @param cardNumber Card number (16 digits)
     * @param expMonth Expiration month (1-12)
     * @param expYear Expiration year (2-digit, e.g., 25 for 2025)
     * @param cvc CVC/CVV (3-4 digits)
     * @param cardholderName Cardholder name
     * @param email Billing email
     * @return PaymentMethod ID
     */
    public String createCardPaymentMethod(String cardNumber, int expMonth, int expYear, String cvc, String cardholderName, String email) {
        try {
            String url = payMongoConfig.getBaseUrl() + "/payment_methods";

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> attributes = new HashMap<>();

            attributes.put("type", "card");

            // Card details
            Map<String, Object> details = new HashMap<>();
            details.put("card_number", cardNumber.replaceAll("\\s", ""));
            details.put("exp_month", expMonth);
            details.put("exp_year", expYear);
            details.put("cvc", cvc);
            attributes.put("details", details);

            // Billing information
            Map<String, String> billing = new HashMap<>();
            billing.put("name", cardholderName);
            billing.put("email", email);
            attributes.put("billing", billing);

            data.put("attributes", attributes);
            requestBody.put("data", data);

            // Make request with Basic Auth
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
                log.error("PayMongo payment method creation failed: {}", response.getBody());
                throw new RuntimeException("Failed to create payment method: " + response.getStatusCode());
            }

            // Parse response
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            Map<String, Object> responseData = (Map<String, Object>) responseBody.get("data");
            String methodId = (String) responseData.get("id");

            log.info("✅ Created PayMongo PaymentMethod: {}", methodId);
            return methodId;

        } catch (Exception e) {
            log.error("❌ Error creating payment method", e);
            throw new RuntimeException("PayMongo payment method creation failed", e);
        }
    }

    // ===== ATTACH PAYMENT METHOD TO INTENT =====

    /**
     * Attach PaymentMethod to PaymentIntent (triggers payment processing, may redirect to 3DS).
     * @param paymentIntentId Payment intent ID
     * @param paymentMethodId Payment method ID
     * @param clientKey Client key from payment intent
     * @param returnUrl URL to return to after 3DS (if needed)
     * @return Map with status and optional redirect URL (for 3DS)
     */
    public Map<String, String> attachPaymentMethod(String paymentIntentId, String paymentMethodId, String clientKey, String returnUrl) {
        try {
            String url = payMongoConfig.getBaseUrl() + "/payment_intents/" + paymentIntentId + "/attach";

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> attributes = new HashMap<>();

            attributes.put("payment_method", paymentMethodId);
            attributes.put("client_key", clientKey);
            attributes.put("return_url", returnUrl);

            data.put("attributes", attributes);
            requestBody.put("data", data);

            // Make request with Basic Auth
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
                log.error("PayMongo attach payment method failed: {}", response.getBody());
                throw new RuntimeException("Failed to attach payment method: " + response.getStatusCode());
            }

            // Parse response
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            Map<String, Object> responseData = (Map<String, Object>) responseBody.get("data");
            Map<String, Object> responseAttributes = (Map<String, Object>) responseData.get("attributes");

            String status = (String) responseAttributes.get("status");
            String redirectUrl = null;

            // Check if 3DS redirect is needed
            Map<String, Object> nextAction = (Map<String, Object>) responseAttributes.get("next_action");
            if (nextAction != null) {
                Map<String, Object> redirect = (Map<String, Object>) nextAction.get("redirect");
                if (redirect != null) {
                    redirectUrl = (String) redirect.get("url");
                }
            }

            log.info("✅ Attached payment method to intent. Status: {}", status);

            Map<String, String> result = new HashMap<>();
            result.put("status", status);
            result.put("paymentIntentId", paymentIntentId);
            if (redirectUrl != null) {
                result.put("redirectUrl", redirectUrl);
            }
            return result;

        } catch (Exception e) {
            log.error("❌ Error attaching payment method", e);
            throw new RuntimeException("PayMongo attach payment method failed", e);
        }
    }

    // ===== SOURCE (E-WALLET) =====

    /**
     * Create a PayMongo Source for e-wallet payments (GCash, Maya, GrabPay).
     * @param sourceType "gcash", "paymaya", or "grab_pay"
     * @param amountPHP Amount in Philippine Pesos
     * @param successUrl URL to redirect to on success
     * @param failureUrl URL to redirect to on failure
     * @return Map with source ID and checkout URL
     */
    public Map<String, String> createSource(String sourceType, BigDecimal amountPHP, String successUrl, String failureUrl) {
        try {
            String url = payMongoConfig.getBaseUrl() + "/sources";

            // Convert PHP to cents
            long amountInCents = amountPHP.multiply(new BigDecimal("100")).longValue();

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> attributes = new HashMap<>();

            attributes.put("type", sourceType);
            attributes.put("amount", amountInCents);
            attributes.put("currency", "PHP");

            // Redirect URLs
            Map<String, String> redirect = new HashMap<>();
            redirect.put("success", successUrl);
            redirect.put("failed", failureUrl);
            attributes.put("redirect", redirect);

            data.put("attributes", attributes);
            requestBody.put("data", data);

            // Make request with Basic Auth
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
                log.error("PayMongo source creation failed: {}", response.getBody());
                throw new RuntimeException("Failed to create source: " + response.getStatusCode());
            }

            // Parse response
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            Map<String, Object> responseData = (Map<String, Object>) responseBody.get("data");
            String sourceId = (String) responseData.get("id");

            Map<String, Object> responseAttributes = (Map<String, Object>) responseData.get("attributes");
            Map<String, Object> redirectData = (Map<String, Object>) responseAttributes.get("redirect");
            String checkoutUrl = (String) redirectData.get("checkout_url");

            log.info("✅ Created PayMongo {} Source: {}", sourceType.toUpperCase(), sourceId);
            return Map.of("sourceId", sourceId, "checkoutUrl", checkoutUrl);

        } catch (Exception e) {
            log.error("❌ Error creating source", e);
            throw new RuntimeException("PayMongo source creation failed", e);
        }
    }

    // ===== GET PAYMENT INTENT STATUS =====

    /**
     * Get payment intent status (to verify if payment succeeded).
     * @param paymentIntentId Payment intent ID
     * @return Map with status and amount
     */
    public Map<String, Object> getPaymentIntentStatus(String paymentIntentId) {
        try {
            String url = payMongoConfig.getBaseUrl() + "/payment_intents/" + paymentIntentId;

            // Make request with Basic Auth
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("PayMongo get payment intent failed: {}", response.getBody());
                throw new RuntimeException("Failed to get payment intent: " + response.getStatusCode());
            }

            // Parse response
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            Map<String, Object> responseData = (Map<String, Object>) responseBody.get("data");
            Map<String, Object> attributes = (Map<String, Object>) responseData.get("attributes");

            String status = (String) attributes.get("status");
            Object amount = attributes.get("amount");

            log.info("✅ Got PaymentIntent status: {} (amount: {})", status, amount);

            Map<String, Object> result = new HashMap<>();
            result.put("status", status);
            result.put("amount", amount);
            return result;

        } catch (Exception e) {
            log.error("❌ Error getting payment intent status", e);
            throw new RuntimeException("PayMongo get payment intent failed", e);
        }
    }

    // ===== WEBHOOK VERIFICATION =====

    /**
     * Verify PayMongo webhook signature using HMAC.
     * @param payload Raw webhook payload
     * @param paymongoSignature Signature from X-Paymongo-Signature header
     * @return true if signature is valid
     */
    public boolean verifyWebhookSignature(String payload, String paymongoSignature) {
        try {
            // Create HMAC-SHA256 hash of payload with webhook secret
            String computedSignature = HmacSHA256.generateSignature(payload, payMongoConfig.getWebhookSecret());

            // Compare computed signature with provided signature
            boolean isValid = computedSignature.equals(paymongoSignature);

            if (isValid) {
                log.info("✅ Webhook signature verified");
            } else {
                log.warn("❌ Webhook signature verification failed");
            }

            return isValid;
        } catch (Exception e) {
            log.error("❌ Error verifying webhook signature", e);
            return false;
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Create HTTP headers with Basic Auth for PayMongo API.
     * Uses secret key + empty password (PayMongo Basic Auth format: secret_key:)
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Basic Auth: base64(secret_key:)
        String auth = payMongoConfig.getSecretKey() + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.add("Authorization", "Basic " + encodedAuth);

        return headers;
    }
}
