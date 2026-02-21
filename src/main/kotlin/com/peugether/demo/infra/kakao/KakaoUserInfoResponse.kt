package com.peugether.demo.infra.kakao

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoUserInfoResponse(
    val id: Long,

    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount? = null,
) {
    data class KakaoAccount(
        val email: String? = null,

        @JsonProperty("email_needs_agreement")
        val emailNeedsAgreement: Boolean? = null,

        val profile: Profile? = null,
    )

    data class Profile(
        val nickname: String? = null,

        @JsonProperty("profile_image_url")
        val profileImageUrl: String? = null,

        @JsonProperty("thumbnail_image_url")
        val thumbnailImageUrl: String? = null,
    )
}
