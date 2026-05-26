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
    val role: String? = null,
    val userId: Long,
    val message: String? = null,
    val requiresEmailVerification: Boolean = false,
    val requiresTwoFactor: Boolean = false
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
    val isRushOrder: Boolean = false,
    val pickupAddressString: String? = null,
    val deliveryAddressString: String? = null,
    val pickupSchedule: String? = null,
    val deliverySchedule: String? = null
)

data class WalletDTO(
    val walletId: Long,
    val userId: Long,
    val availableBalance: Double,
    val currency: String,
    val updatedAt: String?
)

data class SubscriptionDTO(
    val subscriptionId: Long,
    val planType: String,
    val planPrice: Double,
    val discountPercentage: Int,
    val ordersIncluded: Int? = null,
    val description: String? = null
)

data class NotificationDTO(
    val notificationId: Long,
    val title: String,
    val message: String,
    val notificationType: String,
    val referenceType: String? = null,
    val referenceId: Long? = null,
    val isRead: Boolean,
    val createdAt: String
)

data class UserDTO(
    val userId: Long,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val phoneNumber: String?,
    val role: String,
    val emailVerified: Boolean,
    val twoFactorEnabled: Boolean
)

data class UpdateUserRequest(
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class TwoFactorEnableRequest(
    val code: String
)

data class TwoFactorLoginRequest(
    val userId: Long,
    val code: String
)

data class TwoFactorResendRequest(
    val userId: Long
)

data class WalletTransactionDTO(
    val transactionId: Long,
    val walletId: Long,
    val amount: Double,
    val transactionType: String,
    val referenceType: String?,
    val referenceId: Long?,
    val status: String,
    val description: String?,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val createdAt: String
)

data class FeedbackRequest(
    val orderId: Long,
    val starRating: Int,
    val feedbackType: String = "SHOP_REVIEW",
    val commentText: String = ""
)

data class FeedbackDTO(
    val feedbackId: Long,
    val orderId: Long? = null,
    val orderNumber: String? = null,
    val customerId: Long? = null,
    val customerName: String? = null,
    val starRating: Int? = null,
    val feedbackType: String? = null,
    val commentText: String? = null,
    val adminResponse: String? = null,
    val createdAt: String? = null
)

