package com.peugether.demo.auth.dto

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
    val isNewUser: Boolean,
) {
    fun toClientResponse() = ClientAuthResponse(
        accessToken = accessToken,
        accessTokenExpiresIn = accessTokenExpiresIn,
        isNewUser = isNewUser,
    )
}

data class ClientAuthResponse(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val isNewUser: Boolean,
)
