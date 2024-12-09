package com.example.helpify.network

import com.example.helpify.classes.AcceptScheduledService
import com.example.helpify.classes.ScheduleServiceRequest
import com.example.helpify.classes.ScheduleServiceResponse
import com.example.helpify.classes.ScheduledService
import com.example.helpify.classes.Service
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Defina o modelo para enviar o e-mail e a senha
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val name: String, val phone: String, val role: String)

data class LoginResponse(
    val accessToken: String
)

// Interface Retrofit
interface ApiService {
    @POST("/auth/signin")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("/auth/signup")
    fun register(@Body registerRequest: RegisterRequest): Call<Void>

    @GET("scheduled-services/next/{userId}")
    fun getNextScheduledServices(@Path("userId") userId: String): Call<List<ScheduledService>>

    @GET("services/available")
    fun getAvailableServices(): Call<List<Service>>

    @GET("services/{serviceId}")
    fun getServiceDetails(@Path("serviceId") serviceId: String): Call<Service>

    @POST("scheduled-services")
    fun scheduleService(@Body request: ScheduleServiceRequest): Call<Void>

    @GET("scheduled-services/available")
    fun getScheduledServices(@Query("date") date: String): Call<List<ScheduledService>>

    @PATCH("scheduled-services/{id}")
    fun acceptScheduleService(@Path("id") id: String, @Body request: AcceptScheduledService): Call<Void>
}