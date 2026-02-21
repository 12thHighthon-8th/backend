package com.peugether.demo.group.dto

data class ChangeProductResponse(
    val groupId: Long,
    val product: ProductInfo,
    val targetAmount: Int,
    val feeAmount: Int,
    val totalAmount: Int,
) {
    data class ProductInfo(
        val id: Long,
        val name: String,
        val price: Int,
        val imageUrl: String?,
    )
}
