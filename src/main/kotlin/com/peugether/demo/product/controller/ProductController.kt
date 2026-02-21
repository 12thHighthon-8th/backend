package com.peugether.demo.product.controller

import com.peugether.demo.product.dto.ProductListResponse
import com.peugether.demo.product.service.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) {
    @GetMapping
    fun getProducts(): ResponseEntity<ProductListResponse> {
        return ResponseEntity.ok(productService.getProducts())
    }
}
