package com.example.washmate.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<AuthResponse>

    @POST("/api/auth/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): Response<AuthResponse>

    @POST("/api/auth/google/mobile")
    suspend fun googleMobile(@Body request: GoogleIdTokenRequest): Response<AuthResponse>

    @GET("/api/auth/me")
    suspend fun getMe(): Response<UserDTO>

    @PUT("/api/auth/me")
    suspend fun updateMe(@Body request: UpdateUserRequest): Response<UserDTO>

    @POST("/api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Map<String, String>>

    @POST("/api/auth/2fa/send-code")
    suspend fun sendTwoFactorCode(): Response<Map<String, String>>

    @POST("/api/auth/2fa/enable")
    suspend fun enableTwoFactor(@Body request: TwoFactorEnableRequest): Response<Map<String, Any>>

    @POST("/api/auth/2fa/disable")
    suspend fun disableTwoFactor(): Response<Map<String, Any>>

    @POST("/api/auth/2fa/login")
    suspend fun verifyTwoFactorLogin(@Body request: TwoFactorLoginRequest): Response<AuthResponse>

    @POST("/api/auth/2fa/resend-login")
    suspend fun resendTwoFactorLogin(@Body request: TwoFactorResendRequest): Response<Map<String, String>>

    @GET("/api/subscriptions/me")
    suspend fun getMySubscription(): Response<UserSubscriptionDTO>

    @GET("/api/orders/my-orders")
    suspend fun getMyOrders(): Response<List<OrderDTO>>

    @GET("/api/services")
    suspend fun getServices(): Response<List<WashServiceDTO>>

    @POST("/api/orders/create")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderDTO>

    @POST("/api/orders/{orderId}/payment/process")
    suspend fun processOrderPayment(@Path("orderId") orderId: Long, @Body request: Map<String, String>): Response<Map<String, Any>>
    @POST("/api/orders/{orderId}/payment/confirm/{paymentId}")
    suspend fun confirmOrderPayment(
        @Path("orderId") orderId: Long,
        @Path("paymentId") paymentId: String,
        @Query("paymentMethod") paymentMethod: String
    ): Response<OrderDTO>

    @GET("/api/wallet/balance")
    suspend fun getWalletBalance(): Response<WalletDTO>

    @POST("/api/wallet/topup/process")
    suspend fun processWalletTopup(@Body request: Map<String, @JvmSuppressWildcards Any>): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("/api/wallet/topup/confirm/{paymentId}")
    suspend fun confirmWalletTopup(
        @Path("paymentId") paymentId: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>?
    ): Response<WalletDTO>

    @GET("/api/wallet/transactions")
    suspend fun getWalletTransactions(): Response<List<com.example.washmate.api.WalletTransactionDTO>>

    @GET("/api/notifications")
    suspend fun getNotifications(): Response<List<NotificationDTO>>

    @GET("/api/notifications/unread-count")
    suspend fun getUnreadNotificationsCount(): Response<Map<String, Long>>

    @POST("/api/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(@Path("notificationId") notificationId: Long): Response<NotificationDTO>

    @POST("/api/notifications/mark-all-read")
    suspend fun markAllNotificationsAsRead(): Response<Map<String, String>>

    @POST("/api/feedbacks/orders/{orderId}")
    suspend fun submitOrderFeedback(
        @Path("orderId") orderId: Long,
        @Body request: FeedbackRequest
    ): Response<FeedbackDTO>

    @GET("/api/feedbacks/orders/{orderId}")
    suspend fun getOrderFeedback(@Path("orderId") orderId: Long): Response<FeedbackDTO>

    @GET("/api/subscriptions/plans")
    suspend fun getSubscriptionPlans(): Response<List<SubscriptionDTO>>

    @POST("/api/subscriptions/upgrade/{planType}/process")
    suspend fun processSubscriptionUpgrade(
        @Path("planType") planType: String,
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>

    @POST("/api/subscriptions/confirm-upgrade/{userSubscriptionId}/{paymentId}")
    suspend fun confirmSubscriptionUpgrade(
        @Path("userSubscriptionId") userSubscriptionId: Long,
        @Path("paymentId") paymentId: String,
        @Query("paymentMethod") paymentMethod: String
    ): Response<UserSubscriptionDTO>
}

