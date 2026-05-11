package edu.cit.sevilla.washmate.features.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class HmacSHA256Test {

    @Test
    void testConstructor() throws Exception {
        Constructor<HmacSHA256> constructor = HmacSHA256.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void generateSignature_Success() throws Exception {
        String message = "hello world";
        String secret = "secret";
        
        // Expected HMAC-SHA256 for "hello world" with "secret"
        // You can calculate this online or use a known value
        String result = HmacSHA256.generateSignature(message, secret);
        
        assertNotNull(result);
        assertEquals(64, result.length()); // SHA-256 hex string is 64 characters
    }

    @Test
    void generateSignature_Consistency() throws Exception {
        String message = "test message";
        String secret = "my-secret-key";
        
        String result1 = HmacSHA256.generateSignature(message, secret);
        String result2 = HmacSHA256.generateSignature(message, secret);
        
        assertEquals(result1, result2); // Must be deterministic
    }
}
