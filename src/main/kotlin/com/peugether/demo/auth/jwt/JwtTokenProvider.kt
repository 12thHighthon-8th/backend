package com.peugether.demo.auth.jwt

import com.peugether.demo.common.exception.CustomException
import com.peugether.demo.common.exception.ErrorCode
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKeyString: String,
    @Value("\${jwt.access-token-expiry}") private val accessTokenExpiry: Long,
    @Value("\${jwt.refresh-token-expiry}") private val refreshTokenExpiry: Long,
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKeyString.toByteArray())
    }

    fun createAccessToken(userId: Long): String {
        return buildToken(userId, accessTokenExpiry * 1000)
    }

    fun createRefreshToken(userId: Long): String {
        return buildToken(userId, refreshTokenExpiry * 1000)
    }

    private fun buildToken(userId: Long, expiryMs: Long): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + expiryMs))
            .signWith(secretKey)
            .compact()
    }

    fun getUserId(token: String): Long {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
                .toLong()
        } catch (e: ExpiredJwtException) {
            throw CustomException(ErrorCode.TOKEN_EXPIRED)
        } catch (e: JwtException) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: ExpiredJwtException) {
            false
        } catch (e: JwtException) {
            false
        }
    }

    fun getAccessTokenExpiry(): Long = accessTokenExpiry
    fun getRefreshTokenExpiry(): Long = refreshTokenExpiry
}
