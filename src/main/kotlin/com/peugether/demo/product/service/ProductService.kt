package com.peugether.demo.product.service

import com.peugether.demo.domain.product.ProductRepository
import com.peugether.demo.product.dto.ProductListResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    @Transactional(readOnly = true)
    fun getProducts(): ProductListResponse {
        val products = productRepository.findAllByIsAvailableTrue()
        return ProductListResponse(
            products = products.map { product ->
                ProductListResponse.ProductItem(
                    id = product.id,
                    name = product.name,
                    price = product.price,
                    imageUrl = product.imageUrl,
                    productUrl = product.productUrl,
                )
            },
        )
    }
}
