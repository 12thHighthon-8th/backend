package com.peugether.demo.group.dto

import java.time.OffsetDateTime

data class GroupPreviewResponse(
    val id: Long,
    val name: String,
    val status: String,
    val product: ProductInfo?,
    val leader: LeaderInfo,
    val memberCount: Int,
    val targetAmount: Int,
    val feeAmount: Int,
    val totalAmount: Int,
    val fundedAmount: Int,
    val fundingPercentage: Double,
    val deadline: OffsetDateTime?,
    val inviteCode: String,
    val inviteLink: String,
) {
    data class ProductInfo(
        val id: Long,
        val name: String,
        val price: Int,
        val imageUrl: String?,
    )

    data class LeaderInfo(
        val id: Long,
        val nickname: String,
    )
}
