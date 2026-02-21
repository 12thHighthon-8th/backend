package com.peugether.demo.auth.controller

import com.peugether.demo.auth.dto.AuthResponse
import com.peugether.demo.auth.dto.KakaoLoginRequest
import com.peugether.demo.auth.dto.RefreshTokenRequest
import com.peugether.demo.auth.service.AuthService
import com.peugether.demo.common.exception.CustomException
import com.peugether.demo.common.exception.ErrorCode
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/kakao")
    fun kakaoLogin(
        @Valid @RequestBody request: KakaoLoginRequest,
    ): ResponseEntity<AuthResponse> {
        val response = authService.kakaoLogin(request.code)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): ResponseEntity<AuthResponse> {
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userId: Long?,
    ): ResponseEntity<Map<String, Boolean>> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        authService.logout(resolvedUserId)
        return ResponseEntity.ok(mapOf("success" to true))
    }

    @DeleteMapping("/withdraw")
    fun withdraw(
        @AuthenticationPrincipal userId: Long?,
    ): ResponseEntity<Map<String, Boolean>> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        authService.withdraw(resolvedUserId)
        return ResponseEntity.ok(mapOf("success" to true))
    }
}
