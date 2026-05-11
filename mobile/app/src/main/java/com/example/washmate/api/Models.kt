package com.example.washmate.api

data class LoginRequest(
    val emailOrUsername: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phoneNumber: String? = null,
    val role: String = "CUSTOMER"
)

data class AuthResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: String,
    val userId: Long,
    val message: String? = null,
    val requiresEmailVerification: Boolean = false
)

data class VerifyEmailRequest(
    val userId: Long,
    val code: String
)

data class ResendOtpRequest(
    val email: String
)

data class GoogleIdTokenRequest(
    val idToken: String
)

data class OrderDTO(
    val orderId: Long,
    val orderNumber: String,
    val totalAmount: Double,
    val status: String,
    val createdAt: String,
    val services: List<OrderServiceDTO>? = null
)

data class OrderServiceDTO(
    val serviceName: String,
    val subtotal: Double
)

data class UserSubscriptionDTO(
    val planType: String,
    val discountPercentage: Int? = null,
    val status: String,
    val expiryDate: String? = null
)

data class ServiceVariantDTO(
    val variantId: Long,
    val variantName: String,
    val variantPrice: Double,
    val displayOrder: Int? = null,
    val isActive: Boolean = true
)

data class WashServiceDTO(
    val serviceId: Long,
    val serviceName: String,
    val basePricePerUnit: Double,
    val unitType: String,
    val description: String? = null,
    val hasVariants: Boolean = false,
    val variants: List<ServiceVariantDTO>? = null
)

data class OrderServiceInput(
    val serviceId: Long,
    val quantity: Double,
    val selectedVariantId: Long? = null
)

data class OrderRequest(
    val services: List<OrderServiceInput>,
    val totalWeight: Double? = null,
    val specialInstructions: String? = null,
    val isRushOrder: Boolean = false
)

data class WalletDTO(
    val walletId: Long,
    val userId: Long,
    val availableBalance: Double,
    val currency: String,
    val updatedAt: String?
)
