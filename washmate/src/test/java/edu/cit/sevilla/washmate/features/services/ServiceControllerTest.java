package edu.cit.sevilla.washmate.features.services;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WashServiceRepository washServiceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private WashService washService;
    private ServiceVariant variant;

    @BeforeEach
    void setUp() {
        variant = ServiceVariant.builder()
                .variantId(1L)
                .variantName("Scented")
                .variantPrice(new BigDecimal("20.00"))
                .isActive(true)
                .displayOrder(1)
                .build();

        washService = WashService.builder()
                .serviceId(1L)
                .serviceName("Full Wash")
                .basePricePerUnit(new BigDecimal("100.00"))
                .unitType("kg")
                .isActive(true)
                .hasVariants(true)
                .variants(Arrays.asList(variant))
                .build();
                
        variant.setService(washService);
    }

    @Test
    @WithMockUser // Simulates an authenticated user
    void getAllActiveServices_Success() throws Exception {
        when(washServiceRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(washService));

        mockMvc.perform(get("/api/services")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceName").value("Full Wash"))
                .andExpect(jsonPath("$[0].variants[0].variantName").value("Scented"));
    }

    @Test
    @WithMockUser
    void updateServicePrice_Success() throws Exception {
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(washServiceRepository.save(any(WashService.class))).thenReturn(washService);

        Map<String, BigDecimal> request = new HashMap<>();
        request.put("price", new BigDecimal("150.00"));

        mockMvc.perform(put("/api/services/1/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("Full Wash"));
                
        assertEquals(new BigDecimal("150.00"), washService.getBasePricePerUnit());
    }

    @Test
    @WithMockUser
    void updateVariantPrice_Success() throws Exception {
        when(washServiceRepository.findById(1L)).thenReturn(Optional.of(washService));
        when(washServiceRepository.save(any(WashService.class))).thenReturn(washService);

        Map<String, BigDecimal> request = new HashMap<>();
        request.put("price", new BigDecimal("30.00"));

        mockMvc.perform(put("/api/services/1/variants/1/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("Full Wash"));
                
        assertEquals(new BigDecimal("30.00"), variant.getVariantPrice());
    }

    @Test
    @WithMockUser
    void getAllActiveServices_NoVariants() throws Exception {
        WashService noVariantService = WashService.builder()
                .serviceId(2L)
                .serviceName("Quick Dry")
                .basePricePerUnit(new BigDecimal("50.00"))
                .unitType("kg")
                .isActive(true)
                .hasVariants(false)
                .variants(null)
                .build();

        when(washServiceRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(noVariantService));

        mockMvc.perform(get("/api/services")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceName").value("Quick Dry"));
    }

    @Test
    @WithMockUser
    void getAllActiveServices_WithInactiveAndNullOrderVariants() throws Exception {
        ServiceVariant activeVariant = ServiceVariant.builder()
                .variantId(1L)
                .variantName("Scented")
                .variantPrice(new BigDecimal("20.00"))
                .isActive(true)
                .displayOrder(null)  // null displayOrder branch
                .build();

        ServiceVariant inactiveVariant = ServiceVariant.builder()
                .variantId(2L)
                .variantName("Extra Rinse")
                .variantPrice(new BigDecimal("15.00"))
                .isActive(false)     // inactive variant - should be filtered out
                .displayOrder(2)
                .build();

        WashService serviceWithMixedVariants = WashService.builder()
                .serviceId(3L)
                .serviceName("Premium Wash")
                .basePricePerUnit(new BigDecimal("200.00"))
                .unitType("kg")
                .isActive(true)
                .hasVariants(true)
                .variants(Arrays.asList(activeVariant, inactiveVariant))
                .build();

        activeVariant.setService(serviceWithMixedVariants);
        inactiveVariant.setService(serviceWithMixedVariants);

        when(washServiceRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(serviceWithMixedVariants));

        mockMvc.perform(get("/api/services")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceName").value("Premium Wash"))
                .andExpect(jsonPath("$[0].variants.length()").value(1))
                .andExpect(jsonPath("$[0].variants[0].variantName").value("Scented"));
    }

    @Test
    @WithMockUser
    void getAllActiveServices_HasVariantsTrueButNullVariantsList() throws Exception {
        WashService serviceNullVariants = WashService.builder()
                .serviceId(4L)
                .serviceName("Null Variants Service")
                .basePricePerUnit(new BigDecimal("80.00"))
                .unitType("kg")
                .isActive(true)
                .hasVariants(true)
                .variants(null)  // hasVariants=true but variants list is null
                .build();

        when(washServiceRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(serviceNullVariants));

        mockMvc.perform(get("/api/services")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceName").value("Null Variants Service"));
    }

    @Test
    @WithMockUser
    void getAllActiveServices_VariantsWithBothNullAndNonNullDisplayOrder() throws Exception {
        ServiceVariant variantWithOrder = ServiceVariant.builder()
                .variantId(10L)
                .variantName("With Order")
                .variantPrice(new BigDecimal("25.00"))
                .isActive(true)
                .displayOrder(5)  // non-null displayOrder
                .build();

        ServiceVariant variantWithoutOrder = ServiceVariant.builder()
                .variantId(11L)
                .variantName("Without Order")
                .variantPrice(new BigDecimal("30.00"))
                .isActive(true)
                .displayOrder(null)  // null displayOrder → fallback to 0
                .build();

        WashService svc = WashService.builder()
                .serviceId(5L)
                .serviceName("Mixed Order Service")
                .basePricePerUnit(new BigDecimal("90.00"))
                .unitType("kg")
                .isActive(true)
                .hasVariants(true)
                .variants(Arrays.asList(variantWithOrder, variantWithoutOrder))
                .build();

        variantWithOrder.setService(svc);
        variantWithoutOrder.setService(svc);

        when(washServiceRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(svc));

        mockMvc.perform(get("/api/services")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].variants.length()").value(2));
    }
}
