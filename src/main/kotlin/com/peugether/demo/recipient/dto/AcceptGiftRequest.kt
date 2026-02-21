package com.peugether.demo.recipient.dto

import jakarta.validation.constraints.NotBlank

data class AcceptGiftRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val address: String,
    val addressDetail: String? = null,
    @field:NotBlank
    val zipCode: String,
)
