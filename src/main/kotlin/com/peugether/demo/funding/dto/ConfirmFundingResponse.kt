package com.peugether.demo.funding.dto

data class ConfirmFundingResponse(
    val groupId: Long,
    val userId: Long,
    val amount: Int,
    val paymentKey: String,
    val orderId: String,
    val fundedAmount: Int,
    val totalAmount: Int,
    val isFullyFunded: Boolean,
)
