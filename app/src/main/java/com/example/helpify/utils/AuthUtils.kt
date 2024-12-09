package com.example.helpify.utils

import com.auth0.android.jwt.JWT
import com.example.helpify.classes.UserData

object AuthUtils {

    fun decodeJWT(token: String): UserData? {
        return try {
            val jwt = JWT(token)
            UserData(
                id = jwt.getClaim("sub").asString(),
                name = jwt.getClaim("name").asString(),
                email = jwt.getClaim("email").asString(),
                role = jwt.getClaim("role").asString()
            )
        } catch (e: Exception) {
            null
        }
    }
}