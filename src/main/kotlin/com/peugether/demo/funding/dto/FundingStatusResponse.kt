package com.peugether.demo.funding.dto

data class FundingStatusResponse(
    val groupId: Long,
    val totalAmount: Int,
    val fundedAmount: Int,
    val remainingAmount: Int,
    val myCurrentAmount: Int,
    val fundingPercentage: Double,
)
