package com.peugether.demo.group.dto

import jakarta.validation.constraints.NotNull

data class ChangeProductRequest(
    @field:NotNull(message = "상품 ID는 필수입니다")
    val productId: Long,
)
