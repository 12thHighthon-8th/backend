package com.peugether.demo.group.dto

import java.time.LocalDateTime
import java.time.OffsetDateTime

data class GroupListResponse(
    val groups: List<GroupItem>,
) {
    data class GroupItem(
        val id: Long,
        val name: String,
        val status: String,
        val myRole: String,
        val product: ProductInfo?,
        val leader: LeaderInfo,
        val memberCount: Int,
        val targetAmount: Int,
        val feeAmount: Int,
        val totalAmount: Int,
        val fundedAmount: Int,
        val fundingPercentage: Double,
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
    }
}
