package com.peugether.demo.group.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

@Schema(description = "그룹 생성 요청")
data class CreateGroupRequest(
    @Schema(description = "그룹 이름", example = "지수 생일 선물 모금")
    @field:NotBlank(message = "그룹 이름은 필수입니다")
    @field:Size(max = 100, message = "그룹 이름은 100자 이하여야 합니다")
    val name: String,

    @Schema(description = "상품 ID (GET /api/v1/products 로 조회)", example = "1")
    @field:NotNull(message = "상품 ID는 필수입니다")
    val productId: Long,

    @Schema(description = "모금 마감일 (생략 가능)", example = "2026-03-01T23:59:59+09:00", nullable = true)
    val deadline: OffsetDateTime? = null,
)
