package com.peugether.demo.auth.dto

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
    val isNewUser: Boolean,
)
