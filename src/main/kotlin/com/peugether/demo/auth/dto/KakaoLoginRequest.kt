package com.peugether.demo.auth.dto

import jakarta.validation.constraints.NotBlank

data class KakaoLoginRequest(
    @field:NotBlank(message = "Authorization code는 필수입니다")
    val code: String,
)
