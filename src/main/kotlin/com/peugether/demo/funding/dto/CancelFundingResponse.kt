package com.peugether.demo.funding.dto

data class CancelFundingResponse(
    val groupId: Long,
    val userId: Long,
    val cancelledAmount: Int,
    val fundedAmount: Int,
    val message: String,
)
