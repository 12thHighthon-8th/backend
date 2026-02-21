package com.peugether.demo.funding.dto

data class SetFundingAmountResponse(
    val groupId: Long,
    val userId: Long,
    val amount: Int,
    val message: String,
)
