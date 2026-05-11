package com.example.washmate.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
}
