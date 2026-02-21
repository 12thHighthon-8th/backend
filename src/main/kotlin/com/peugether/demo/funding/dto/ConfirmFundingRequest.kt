package com.peugether.demo.funding.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class ConfirmFundingRequest(
    @field:NotBlank
    val paymentKey: String,

    @field:NotBlank
    val orderId: String,

    @field:Min(100)
    val amount: Int,
)
