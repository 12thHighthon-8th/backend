package com.peugether.demo.auth.controller

import com.peugether.demo.auth.dto.AuthResponse
import com.peugether.demo.auth.dto.ClientAuthResponse
import com.peugether.demo.auth.dto.KakaoLoginRequest
import com.peugether.demo.auth.service.AuthService
import com.peugether.demo.common.exception.CustomException
import com.peugether.demo.common.exception.ErrorCode
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    @Value("\${app.frontend-url:http://localhost:3000}") private val frontendUrl: String,
) {
    @PostMapping("/kakao")
    fun kakaoLogin(
        @Valid @RequestBody request: KakaoLoginRequest,
        httpResponse: HttpServletResponse,
    ): ResponseEntity<ClientAuthResponse> {
        val auth = authService.kakaoLogin(request.code)
        setRefreshTokenCookie(httpResponse, auth.refreshToken, auth.refreshTokenExpiresIn)
        return ResponseEntity.ok(auth.toClientResponse())
    }

    /**
     * 카카오 OAuth 백엔드 콜백 엔드포인트.
     * 카카오 redirect URI를 백엔드로 설정한 경우 사용.
     * code 처리 후 프론트엔드로 accessToken만 redirect. refresh token은 HttpOnly 쿠키로 설정.
     */
    @GetMapping("/kakao/callback")
    fun kakaoCallback(
        @RequestParam code: String,
        httpResponse: HttpServletResponse,
    ) {
        val auth = authService.kakaoLogin(code)
        setRefreshTokenCookie(httpResponse, auth.refreshToken, auth.refreshTokenExpiresIn)
        val redirectUrl = "$frontendUrl/oauth/kakao/callback" +
            "?accessToken=${auth.accessToken}" +
            "&isNewUser=${auth.isNewUser}"
        httpResponse.sendRedirect(redirectUrl)
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue("refresh_token") refreshToken: String,
        httpResponse: HttpServletResponse,
    ): ResponseEntity<ClientAuthResponse> {
        val auth = authService.refreshToken(refreshToken)
        setRefreshTokenCookie(httpResponse, auth.refreshToken, auth.refreshTokenExpiresIn)
        return ResponseEntity.ok(auth.toClientResponse())
    }

    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userId: Long?,
        httpResponse: HttpServletResponse,
    ): ResponseEntity<Map<String, Boolean>> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        authService.logout(resolvedUserId)
        clearRefreshTokenCookie(httpResponse)
        return ResponseEntity.ok(mapOf("success" to true))
    }

    @DeleteMapping("/withdraw")
    fun withdraw(
        @AuthenticationPrincipal userId: Long?,
        httpResponse: HttpServletResponse,
    ): ResponseEntity<Map<String, Boolean>> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        authService.withdraw(resolvedUserId)
        clearRefreshTokenCookie(httpResponse)
        return ResponseEntity.ok(mapOf("success" to true))
    }

    private fun setRefreshTokenCookie(response: HttpServletResponse, token: String, maxAge: Long) {
        val cookie = ResponseCookie.from("refresh_token", token)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/api/v1/auth")
            .maxAge(maxAge)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    private fun clearRefreshTokenCookie(response: HttpServletResponse) {
        val cookie = ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/api/v1/auth")
            .maxAge(0)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }
}
