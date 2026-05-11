package edu.cit.sevilla.washmate.features.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OAuthRedirectCodeUtilTest {

    private OAuthRedirectCodeUtil oAuthRedirectCodeUtil;

    @BeforeEach
    void setUp() {
        oAuthRedirectCodeUtil = new OAuthRedirectCodeUtil();
    }

    @Test
    void generateState_Returns32CharString() {
        String state = oAuthRedirectCodeUtil.generateState();
        assertNotNull(state);
        assertEquals(32, state.length());
    }

    @Test
    void storeState_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            oAuthRedirectCodeUtil.storeState("some-state");
        });
    }

    @Test
    void validateAndConsumeState_AlwaysReturnsTrue() {
        assertTrue(oAuthRedirectCodeUtil.validateAndConsumeState("some-state"));
    }

    @Test
    void generateRedirectCode_Returns8CharString() {
        String code = oAuthRedirectCodeUtil.generateRedirectCode();
        assertNotNull(code);
        assertEquals(8, code.length());
    }

    @Test
    void storeRedirectCode_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            oAuthRedirectCodeUtil.storeRedirectCode("code", "access", "refresh", 1L, "test@test.com", "CUSTOMER");
        });
    }

    @Test
    void getAndConsumeRedirectCode_AlwaysReturnsNull() {
        assertNull(oAuthRedirectCodeUtil.getAndConsumeRedirectCode("code"));
    }
}
