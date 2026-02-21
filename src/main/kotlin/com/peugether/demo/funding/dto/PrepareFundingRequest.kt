package com.peugether.demo.funding.dto

import jakarta.validation.constraints.Min

data class PrepareFundingRequest(
    @field:Min(100)
    val amount: Int,
)
