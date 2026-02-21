package com.peugether.demo.domain.fee

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "fee_policies")
@EntityListeners(AuditingEntityListener::class)
class FeePolicy(
    @Column(name = "min_price", nullable = false)
    val minPrice: Int,

    @Column(name = "max_price")
    val maxPrice: Int? = null,

    @Column(name = "fee_rate", precision = 5, scale = 4)
    val feeRate: BigDecimal? = null,

    @Column(name = "fee_fixed")
    val feeFixed: Int? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
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
