package com.peugether.demo.domain.product

import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
    fun findByIdAndIsAvailableTrue(id: Long): Product?
    fun findAllByIsAvailableTrue(): List<Product>
    fun findByNameContainingIgnoreCaseAndIsAvailableTrue(keyword: String): List<Product>
}
