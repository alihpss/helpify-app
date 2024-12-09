package com.example.helpify.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.helpify.classes.UserData

object SharedPreferencesManager {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_TOKEN = "accessToken"
    private const val KEY_USER_ID = "userId"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_USER_ROLE = "userRole"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        getSharedPreferences(context).edit().putString(KEY_TOKEN, token).apply()
    }

    fun saveUserData(context: Context, userData: UserData) {
        getSharedPreferences(context).edit()
            .putString(KEY_USER_ID, userData.id)
            .putString(KEY_USER_NAME, userData.name)
            .putString(KEY_USER_EMAIL, userData.email)
            .putString(KEY_USER_ROLE, userData.role)
            .apply()
    }

    fun getToken(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_TOKEN, null)
    }

    fun getUserData(context: Context): UserData? {
        val prefs = getSharedPreferences(context)
        return UserData(
            id = prefs.getString(KEY_USER_ID, null),
            name = prefs.getString(KEY_USER_NAME, null),
            email = prefs.getString(KEY_USER_EMAIL, null),
            role = prefs.getString(KEY_USER_ROLE, null)
        )
    }

    fun clearUserData(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }
}