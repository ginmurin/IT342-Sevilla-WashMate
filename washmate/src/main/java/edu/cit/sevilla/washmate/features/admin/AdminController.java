package edu.cit.sevilla.washmate.features.admin;

import edu.cit.sevilla.washmate.features.users.UserDTO;
import edu.cit.sevilla.washmate.features.orders.Order;
import edu.cit.sevilla.washmate.features.users.User;
import edu.cit.sevilla.washmate.features.orders.OrderRepository;
import edu.cit.sevilla.washmate.features.users.UserRepository;
import edu.cit.sevilla.washmate.features.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import edu.cit.sevilla.washmate.features.wallet.Wallet;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final WalletRepository walletRepository;

    /**
     * Get global platform statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGlobalStats(@AuthenticationPrincipal Jwt jwt) {
        verifyAdmin(jwt);
        
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();
        
        List<Order> allOrders = orderRepository.findAll();
        double totalRevenue = allOrders.stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0.0)
                .sum();

        // In this system, SHOP_OWNERs are effectively the "shops"
        long totalShops = userRepository.findAll().stream()
                .filter(u -> "SHOP_OWNER".equalsIgnoreCase(u.getRole()))
                .count();

        return ResponseEntity.ok(Map.of(
            "totalUsers", totalUsers,
            "totalShops", totalShops,
            "totalOrders", totalOrders,
            "totalRevenue", totalRevenue
        ));
    }

    /**
     * Get all users in the system.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(@AuthenticationPrincipal Jwt jwt) {
        verifyAdmin(jwt);
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Update a user's role.
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Jwt jwt) {
        verifyAdmin(jwt);
        
        String newRole = request.get("role");
        if (newRole == null || newRole.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(newRole.toUpperCase());
        User savedUser = userRepository.save(user);
        
        log.info("Admin updated user {} role to {}", userId, newRole);
        return ResponseEntity.ok(toUserDTO(savedUser));
    }

    /**
     * Update a user's status (ACTIVE, INACTIVE, etc.).
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Jwt jwt) {
        verifyAdmin(jwt);

        String newStatus = request.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(newStatus.toUpperCase());
        User savedUser = userRepository.save(user);

        log.info("Admin updated user {} status to {}", userId, newStatus);
        return ResponseEntity.ok(toUserDTO(savedUser));
    }

    /**
     * Delete a user.
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal Jwt jwt) {
        verifyAdmin(jwt);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Don't allow deleting yourself
        Long adminId = Long.parseLong(jwt.getSubject());
        if (userId.equals(adminId)) {
            throw new RuntimeException("Cannot delete your own account");
        }

        userRepository.delete(user);
        log.info("Admin deleted user {}", userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update a user's wallet balance.
     */
    @PutMapping("/users/{userId}/wallet")
    public ResponseEntity<UserDTO> updateUserWallet(
            @PathVariable Long userId,
            @RequestBody Map<String, Double> request,
            @AuthenticationPrincipal Jwt jwt) {
        verifyAdmin(jwt);

        Double newBalance = request.get("balance");
        if (newBalance == null) {
            throw new IllegalArgumentException("Balance is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find or create wallet
        edu.cit.sevilla.washmate.features.wallet.Wallet wallet = walletRepository.findByUserUserId(userId)
                .orElseGet(() -> {
                    edu.cit.sevilla.washmate.features.wallet.Wallet newWallet = new edu.cit.sevilla.washmate.features.wallet.Wallet();
                    newWallet.setUser(user);
                    newWallet.setAvailableBalance(java.math.BigDecimal.ZERO);
                    return newWallet;
                });

        wallet.setAvailableBalance(java.math.BigDecimal.valueOf(newBalance));
        walletRepository.save(wallet);

        log.info("Admin updated user {} wallet balance to {}", userId, newBalance);
        return ResponseEntity.ok(toUserDTO(user));
    }

    // Helper to verify admin role from JWT
    private void verifyAdmin(Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Access denied: Admin role required");
        }
    }

    private UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setTwoFactorEnabled(user.getTwoFactorEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        
        // Fetch wallet balance if exists
        walletRepository.findByUserUserId(user.getUserId())
            .ifPresent(wallet -> {
                if (wallet.getAvailableBalance() != null) {
                    dto.setWalletBalance(wallet.getAvailableBalance().doubleValue());
                }
            });
            
        return dto;
    }
}

