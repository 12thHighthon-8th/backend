package com.peugether.demo.product.dto

data class ProductListResponse(
    val products: List<ProductItem>,
) {
    data class ProductItem(
        val id: Long,
        val name: String,
        val price: Int,
        val imageUrl: String?,
        val productUrl: String?,
    )
}
