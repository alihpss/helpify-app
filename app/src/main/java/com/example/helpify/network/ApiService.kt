package com.example.helpify.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Defina o modelo para enviar o e-mail e a senha
data class LoginRequest(val email: String, val password: String)

// Interface Retrofit
interface ApiService {
    @POST("/auth/signin")
    fun login(@Body loginRequest: LoginRequest): Call<Void>
}