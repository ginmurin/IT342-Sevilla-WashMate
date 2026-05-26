package edu.cit.sevilla.washmate.features.payments;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.sevilla.washmate.config.PayMongoConfig;

@ExtendWith(MockitoExtension.class)
class PayMongoServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PayMongoConfig payMongoConfig;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PayMongoService payMongoService;

    @BeforeEach
    void setUp() {
        lenient().when(payMongoConfig.getBaseUrl()).thenReturn("https://api.paymongo.com/v1");
        lenient().when(payMongoConfig.getSecretKey()).thenReturn("sk_test_123");
    }

    @Test
    void createPaymentIntent_Success() throws Exception {
        BigDecimal amount = new BigDecimal("100.00");
        String responseJson = "{\"data\":{\"id\":\"pi_123\",\"attributes\":{\"client_key\":\"pi_key_123\"}}}";
        
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson, HttpStatus.OK));
                
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("id", "pi_123");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("client_key", "pi_key_123");
        data.put("attributes", attributes);
        responseMap.put("data", data);
        
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(responseMap);

        Map<String, String> result = payMongoService.createPaymentIntent(amount);

        assertNotNull(result);
        assertEquals("pi_123", result.get("paymentIntentId"));
        assertEquals("pi_key_123", result.get("clientKey"));
    }

    @Test
    void createCardPaymentMethod_Success() throws Exception {
        String responseJson = "{\"data\":{\"id\":\"pm_123\"}}";
        
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson, HttpStatus.OK));
                
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("id", "pm_123");
        responseMap.put("data", data);
        
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(responseMap);

        String result = payMongoService.createCardPaymentMethod("1234123412341234", 12, 25, "123", "John Doe", "john@example.com");

        assertNotNull(result);
        assertEquals("pm_123", result);
    }

    @Test
    void attachPaymentMethod_Success() throws Exception {
        String responseJson = "{\"data\":{\"attributes\":{\"status\":\"succeeded\"}}}";
        
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson, HttpStatus.OK));
                
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("status", "succeeded");
        data.put("attributes", attributes);
        responseMap.put("data", data);
        
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(responseMap);

        Map<String, String> result = payMongoService.attachPaymentMethod("pi_123", "pm_123", "pi_key_123", "http://return.url");

        assertNotNull(result);
        assertEquals("succeeded", result.get("status"));
    }

    @Test
    void createSource_Success() throws Exception {
        String responseJson = "{\"data\":{\"id\":\"src_123\",\"attributes\":{\"redirect\":{\"checkout_url\":\"http://checkout.url\"}}}}";
        
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson, HttpStatus.OK));
                
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("id", "src_123");
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> redirect = new HashMap<>();
        redirect.put("checkout_url", "http://checkout.url");
        attributes.put("redirect", redirect);
        data.put("attributes", attributes);
        responseMap.put("data", data);
        
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(responseMap);

        Map<String, String> result = payMongoService.createSource("gcash", new BigDecimal("100.00"), "http://success.url", "http://fail.url");

        assertNotNull(result);
        assertEquals("src_123", result.get("sourceId"));
        assertEquals("http://checkout.url", result.get("checkoutUrl"));
    }

    @Test
    void createCheckoutSession_Success() throws Exception {
        String responseJson = "{\"data\":{\"id\":\"cs_123\",\"attributes\":{\"checkout_url\":\"http://checkout.url\"}}}";
        
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson, HttpStatus.OK));
                
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("id", "cs_123");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("checkout_url", "http://checkout.url");
        data.put("attributes", attributes);
        responseMap.put("data", data);
        
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(responseMap);

        Map<String, String> result = payMongoService.createCheckoutSession(new BigDecimal("100.00"), "http://success.url", "http://fail.url");

        assertNotNull(result);
        assertEquals("cs_123", result.get("sessionId"));
        assertEquals("http://checkout.url", result.get("checkoutUrl"));
    }

    @Test
    void getPaymentIntentStatus_Success() throws Exception {
        String responseJson = "{\"data\":{\"attributes\":{\"status\":\"succeeded\",\"amount\":10000}}}";
        
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson, HttpStatus.OK));
                
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("status", "succeeded");
        attributes.put("amount", 10000);
        data.put("attributes", attributes);
        responseMap.put("data", data);
        
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(responseMap);

        Map<String, Object> result = payMongoService.getPaymentIntentStatus("pi_123");

        assertNotNull(result);
        assertEquals("succeeded", result.get("status"));
        assertEquals(10000, result.get("amount"));
    }

    @Test
    void verifyWebhookSignature_Failure() {
        when(payMongoConfig.getWebhookSecret()).thenReturn("wh_secret");
        
        boolean result = payMongoService.verifyWebhookSignature("payload", "invalid_signature");

        assertFalse(result);
    }

    @Test
    void verifyWebhookSignature_Success() throws Exception {
        when(payMongoConfig.getWebhookSecret()).thenReturn("wh_secret");
        
        // Mock HmacSHA256 behavior if needed, or just calculate the real signature!
        // The real implementation uses HmacSHA256.generateSignature(payload, secret)
        String realSignature = edu.cit.sevilla.washmate.features.auth.HmacSHA256.generateSignature("payload", "wh_secret");
        
        boolean result = payMongoService.verifyWebhookSignature("payload", realSignature);

        assertTrue(result);
    }

    @Test
    void createPaymentIntent_Failure() throws Exception {
        BigDecimal amount = new BigDecimal("100.00");
        
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST));

        assertThrows(RuntimeException.class, () -> {
            payMongoService.createPaymentIntent(amount);
        });
    }

    @Test
    void createCardPaymentMethod_Failure() throws Exception {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST));

        assertThrows(RuntimeException.class, () -> {
            payMongoService.createCardPaymentMethod("1234123412341234", 12, 25, "123", "John Doe", "john@example.com");
        });
    }

    @Test
    void attachPaymentMethod_Failure() throws Exception {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST));

        assertThrows(RuntimeException.class, () -> {
            payMongoService.attachPaymentMethod("pi_123", "pm_123", "pi_key_123", "http://return.url");
        });
    }

    @Test
    void attachPaymentMethod_WithRedirect() throws Exception {
        String responseJson = "{\"data\":{\"attributes\":{\"status\":\"awaiting_next_action\",\"next_action\":{\"redirect\":{\"url\":\"http://redirect.url\"}}}}}";
        
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson, HttpStatus.OK));
                
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("status", "awaiting_next_action");
        Map<String, Object> nextAction = new HashMap<>();
        Map<String, Object> redirect = new HashMap<>();
        redirect.put("url", "http://redirect.url");
        nextAction.put("redirect", redirect);
        attributes.put("next_action", nextAction);
        data.put("attributes", attributes);
        responseMap.put("data", data);
        
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(responseMap);

        Map<String, String> result = payMongoService.attachPaymentMethod("pi_123", "pm_123", "pi_key_123", "http://return.url");

        assertNotNull(result);
        assertEquals("awaiting_next_action", result.get("status"));
        assertEquals("http://redirect.url", result.get("redirectUrl"));
    }

    @Test
    void createSource_Failure() throws Exception {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST));

        assertThrows(RuntimeException.class, () -> {
            payMongoService.createSource("gcash", new BigDecimal("100.00"), "http://success.url", "http://fail.url");
        });
    }

    @Test
    void createCheckoutSession_Failure() throws Exception {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST));

        assertThrows(RuntimeException.class, () -> {
            payMongoService.createCheckoutSession(new BigDecimal("100.00"), "http://success.url", "http://fail.url");
        });
    }

    @Test
    void getPaymentIntentStatus_Failure() throws Exception {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST));

        assertThrows(RuntimeException.class, () -> {
            payMongoService.getPaymentIntentStatus("pi_123");
        });
    }
}
