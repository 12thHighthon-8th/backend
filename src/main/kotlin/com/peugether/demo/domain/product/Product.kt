package com.peugether.demo.domain.product

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener::class)
class Product(
    @Column(name = "external_product_id", nullable = false, length = 100)
    val externalProductId: String,

    @Column(name = "name", nullable = false, length = 300)
    val name: String,

    @Column(name = "price", nullable = false)
    val price: Int,

    @Column(name = "image_url", length = 500)
    val imageUrl: String? = null,

    @Column(name = "product_url", length = 500)
    val productUrl: String? = null,

    @Column(name = "is_available", nullable = false)
    var isAvailable: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime
}
