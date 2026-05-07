package edu.cit.sevilla.washmate.controller;

import edu.cit.sevilla.washmate.dto.WashServiceDTO;
import edu.cit.sevilla.washmate.dto.ServiceVariantDTO;
import edu.cit.sevilla.washmate.entity.WashService;
import edu.cit.sevilla.washmate.repository.WashServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * REST Controller for wash service management in single-shop system.
 * Provides public endpoints for service information.
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final WashServiceRepository washServiceRepository;

    /**
     * Get all active wash services.
     */
    @GetMapping
    public ResponseEntity<List<WashServiceDTO>> getAllActiveServices() {
        List<WashService> services = washServiceRepository.findByIsActiveTrue();
        List<WashServiceDTO> serviceDTOs = services.stream()
            .map(this::convertToDTO)
            .toList();
        return ResponseEntity.ok(serviceDTOs);
    }

    /**
     * Convert WashService entity to DTO.
     */
    private WashServiceDTO convertToDTO(WashService service) {
        WashServiceDTO dto = WashServiceDTO.builder()
            .serviceId(service.getServiceId())
            .serviceName(service.getServiceName())
            .basePricePerUnit(service.getBasePricePerUnit())
            .unitType(service.getUnitType())
            .description(service.getDescription())
            .isActive(service.getIsActive())
            .hasVariants(service.getHasVariants())
            .isAutoSelected(service.getIsAutoSelected())
            .build();

        // Add variants if service has them
        if (Boolean.TRUE.equals(service.getHasVariants()) && service.getVariants() != null) {
            dto.setVariants(service.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                .sorted(Comparator.comparingInt(v -> v.getDisplayOrder() != null ? v.getDisplayOrder() : 0))
                .map(v -> ServiceVariantDTO.builder()
                    .variantId(v.getVariantId())
                    .variantName(v.getVariantName())
                    .variantPrice(v.getVariantPrice())
                    .displayOrder(v.getDisplayOrder())
                    .isActive(v.getIsActive())
                    .build())
                .toList());
        }

        return dto;
    }

    /**
     * Update the base price of a service.
     */
    @org.springframework.web.bind.annotation.PutMapping("/{serviceId}/price")
    public ResponseEntity<WashServiceDTO> updateServicePrice(
            @org.springframework.web.bind.annotation.PathVariable Long serviceId,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, java.math.BigDecimal> request) {
        WashService service = washServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        
        service.setBasePricePerUnit(request.get("price"));
        WashService updatedService = washServiceRepository.save(service);
        return ResponseEntity.ok(convertToDTO(updatedService));
    }

    /**
     * Update the price of a service variant.
     */
    @org.springframework.web.bind.annotation.PutMapping("/{serviceId}/variants/{variantId}/price")
    public ResponseEntity<WashServiceDTO> updateVariantPrice(
            @org.springframework.web.bind.annotation.PathVariable Long serviceId,
            @org.springframework.web.bind.annotation.PathVariable Long variantId,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, java.math.BigDecimal> request) {
        WashService service = washServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        
        service.getVariants().stream()
                .filter(v -> v.getVariantId().equals(variantId))
                .findFirst()
                .ifPresent(v -> v.setVariantPrice(request.get("price")));
                
        WashService updatedService = washServiceRepository.save(service);
        return ResponseEntity.ok(convertToDTO(updatedService));
    }
}