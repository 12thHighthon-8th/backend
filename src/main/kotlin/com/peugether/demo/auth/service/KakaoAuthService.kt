package com.peugether.demo.auth.service

import com.peugether.demo.infra.kakao.KakaoAuthClient
import com.peugether.demo.infra.kakao.KakaoTokenResponse
import com.peugether.demo.infra.kakao.KakaoUserInfoResponse
import org.springframework.stereotype.Service

@Service
class KakaoAuthService(
    private val kakaoAuthClient: KakaoAuthClient,
) {
    fun exchangeCodeForToken(code: String): KakaoTokenResponse {
        return kakaoAuthClient.getToken(code)
    }

    fun getUserInfo(accessToken: String): KakaoUserInfoResponse {
        return kakaoAuthClient.getUserInfo(accessToken)
    }
}
