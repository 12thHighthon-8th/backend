package com.peugether.demo.funding.dto

data class PrepareFundingResponse(
    val orderId: String,
    val amount: Int,
    val orderName: String,
)
