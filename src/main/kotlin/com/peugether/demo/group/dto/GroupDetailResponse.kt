package com.peugether.demo.group.dto

import java.time.LocalDateTime
import java.time.OffsetDateTime

data class GroupDetailResponse(
    val id: Long,
    val name: String,
    val status: String,
    val inviteCode: String,
    val inviteLink: String,
    val product: ProductInfo?,
    val leader: LeaderInfo,
    val members: List<MemberInfo>,
    val targetAmount: Int,
    val feeAmount: Int,
    val totalAmount: Int,
    val fundedAmount: Int,
    val deadline: OffsetDateTime?,
    val createdAt: LocalDateTime,
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

    data class MemberInfo(
        val userId: Long,
        val nickname: String,
        val role: String,
        val intendedAmount: Int,
        val paidAmount: Int,
        val paymentStatus: String,
    )
}
