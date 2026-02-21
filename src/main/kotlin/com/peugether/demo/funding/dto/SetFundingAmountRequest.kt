package com.peugether.demo.funding.dto

import jakarta.validation.constraints.Min

data class SetFundingAmountRequest(
    @field:Min(0)
    val amount: Int,
)
