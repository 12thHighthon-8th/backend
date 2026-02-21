package com.peugether.demo.group.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

data class CreateGroupRequest(
    @field:NotBlank(message = "그룹 이름은 필수입니다")
    @field:Size(max = 100, message = "그룹 이름은 100자 이하여야 합니다")
    val name: String,

    @field:NotNull(message = "상품 ID는 필수입니다")
    val productId: Long,

    val deadline: OffsetDateTime? = null,
)
