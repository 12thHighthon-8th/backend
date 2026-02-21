package com.peugether.demo.infra.kakao

import com.peugether.demo.common.exception.CustomException
import com.peugether.demo.common.exception.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class KakaoAuthClient(
    private val webClient: WebClient,
    @Value("\${kakao.oauth.client-id}") private val clientId: String,
    @Value("\${kakao.oauth.client-secret}") private val clientSecret: String,
    @Value("\${kakao.oauth.redirect-uri}") private val redirectUri: String,
) {
    companion object {
        private const val TOKEN_URL = "https://kauth.kakao.com/oauth/token"
        private const val USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"
    }

    fun getToken(code: String): KakaoTokenResponse {
        val formData = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("redirect_uri", redirectUri)
            add("code", code)
        }

        return try {
            webClient.post()
                .uri(TOKEN_URL)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KakaoTokenResponse::class.java)
                .block()
                ?: throw CustomException(ErrorCode.KAKAO_SERVER_ERROR)
        } catch (e: WebClientResponseException) {
            val errorBody = e.responseBodyAsString
            when {
                errorBody.contains("invalid_code") -> throw CustomException(ErrorCode.KAKAO_INVALID_CODE)
                errorBody.contains("invalid_client") -> throw CustomException(ErrorCode.KAKAO_INVALID_CLIENT)
                errorBody.contains("invalid_grant") -> throw CustomException(ErrorCode.KAKAO_INVALID_GRANT)
                else -> throw CustomException(ErrorCode.KAKAO_SERVER_ERROR, "카카오 인증 실패: ${e.message}")
            }
        }
    }

    fun getUserInfo(accessToken: String): KakaoUserInfoResponse {
        return try {
            webClient.get()
                .uri(USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse::class.java)
                .block()
                ?: throw CustomException(ErrorCode.KAKAO_SERVER_ERROR)
        } catch (e: WebClientResponseException) {
            throw CustomException(ErrorCode.KAKAO_SERVER_ERROR, "카카오 사용자 정보 조회 실패: ${e.message}")
        }
    }
}
