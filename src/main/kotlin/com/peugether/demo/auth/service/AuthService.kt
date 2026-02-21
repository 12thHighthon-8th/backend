package com.peugether.demo.auth.service

import com.peugether.demo.auth.dto.AuthResponse
import com.peugether.demo.auth.jwt.JwtTokenProvider
import com.peugether.demo.common.exception.CustomException
import com.peugether.demo.common.exception.ErrorCode
import com.peugether.demo.domain.user.User
import com.peugether.demo.domain.user.UserRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class AuthService(
    private val kakaoAuthService: KakaoAuthService,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>,
) {
    companion object {
        private const val REFRESH_TOKEN_PREFIX = "refresh_token:"
    }

    @Transactional
    fun kakaoLogin(code: String): AuthResponse {
        // 1. 카카오 Authorization Code → Access Token 교환
        val kakaoToken = kakaoAuthService.exchangeCodeForToken(code)

        // 2. 카카오 사용자 정보 조회
        val kakaoUserInfo = kakaoAuthService.getUserInfo(kakaoToken.accessToken)

        // 3. 기존 회원 조회 또는 신규 생성
        val (user, isNewUser) = findOrCreateUser(kakaoUserInfo.id, kakaoUserInfo)

        // 4. 서비스 JWT 발급
        val accessToken = jwtTokenProvider.createAccessToken(user.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id)

        // 5. Refresh Token → Redis 저장
        saveRefreshToken(user.id, refreshToken)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtTokenProvider.getAccessTokenExpiry(),
            refreshTokenExpiresIn = jwtTokenProvider.getRefreshTokenExpiry(),
            isNewUser = isNewUser,
        )
    }

    fun refreshToken(refreshToken: String): AuthResponse {
        // 1. Redis에서 Refresh Token 검증
        val userId = findUserIdByRefreshToken(refreshToken)
            ?: throw CustomException(ErrorCode.TOKEN_EXPIRED, "유효하지 않은 Refresh Token입니다. 재인증해주세요.")

        // 2. 사용자 존재 여부 확인
        userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        // 3. 새 토큰 발급 (Refresh Token Rotation)
        val newAccessToken = jwtTokenProvider.createAccessToken(userId)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(userId)

        // 4. 새 Refresh Token을 Redis에 저장
        saveRefreshToken(userId, newRefreshToken)

        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            accessTokenExpiresIn = jwtTokenProvider.getAccessTokenExpiry(),
            refreshTokenExpiresIn = jwtTokenProvider.getRefreshTokenExpiry(),
            isNewUser = false,
        )
    }

    fun logout(userId: Long) {
        deleteRefreshToken(userId)
    }

    @Transactional
    fun withdraw(userId: Long) {
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        user.softDelete()
        deleteRefreshToken(userId)
    }

    private fun findOrCreateUser(
        kakaoId: Long,
        kakaoUserInfo: com.peugether.demo.infra.kakao.KakaoUserInfoResponse,
    ): Pair<User, Boolean> {
        val existingUser = userRepository.findByKakaoId(kakaoId)

        return if (existingUser != null) {
            Pair(existingUser, false)
        } else {
            val nickname = kakaoUserInfo.kakaoAccount?.profile?.nickname ?: "사용자$kakaoId"
            val profileImageUrl = kakaoUserInfo.kakaoAccount?.profile?.profileImageUrl
            val email = kakaoUserInfo.kakaoAccount?.email

            val newUser = userRepository.save(
                User(
                    kakaoId = kakaoId,
                    nickname = nickname,
                    profileImageUrl = profileImageUrl,
                    email = email,
                )
            )
            Pair(newUser, true)
        }
    }

    private fun saveRefreshToken(userId: Long, refreshToken: String) {
        redisTemplate.opsForValue().set(
            "$REFRESH_TOKEN_PREFIX$userId",
            refreshToken,
            Duration.ofSeconds(jwtTokenProvider.getRefreshTokenExpiry()),
        )
    }

    private fun deleteRefreshToken(userId: Long) {
        redisTemplate.delete("$REFRESH_TOKEN_PREFIX$userId")
    }

    private fun findUserIdByRefreshToken(refreshToken: String): Long? {
        // Redis에서 userId 기반으로 직접 조회
        // Refresh Token Rotation을 사용하므로, userId를 JWT에서 추출
        return try {
            val userId = jwtTokenProvider.getUserId(refreshToken)
            val stored = redisTemplate.opsForValue().get("$REFRESH_TOKEN_PREFIX$userId")
            if (stored == refreshToken) userId else null
        } catch (e: Exception) {
            null
        }
    }
}
