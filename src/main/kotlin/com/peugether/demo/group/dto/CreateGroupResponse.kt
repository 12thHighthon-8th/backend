package com.peugether.demo.group.dto

import java.time.OffsetDateTime

data class CreateGroupResponse(
    val id: Long,
    val name: String,
    val inviteCode: String,
    val inviteLink: String,
    val product: ProductInfo,
    val targetAmount: Int,
    val feeAmount: Int,
    val totalAmount: Int,
    val status: String,
    val deadline: OffsetDateTime?,
    val leader: LeaderInfo,
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
