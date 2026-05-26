package edu.cit.sevilla.washmate.features.users;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/orders/{orderId}")
    public ResponseEntity<?> submitOrderFeedback(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody FeedbackRequest request) {
        
        Long userId = Long.parseLong(jwt.getSubject());
        FeedbackDTO result = feedbackService.submitOrderFeedback(orderId, userId, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<FeedbackDTO> getOrderFeedback(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt) {
        
        Long userId = Long.parseLong(jwt.getSubject());
        FeedbackDTO result = feedbackService.getOrderFeedback(orderId, userId);
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> response = new HashMap<>();
        response.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        Map<String, String> response = new HashMap<>();
        response.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        e.printStackTrace();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Internal Server Error: " + e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
}
