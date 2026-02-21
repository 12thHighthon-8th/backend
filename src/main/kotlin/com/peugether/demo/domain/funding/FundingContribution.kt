package com.peugether.demo.domain.funding

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "funding_contributions")
@EntityListeners(AuditingEntityListener::class)
class FundingContribution(
    @Column(name = "group_id", nullable = false)
    val groupId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "amount", nullable = false)
    val amount: Int,

    @Column(name = "payment_key", nullable = false, length = 200)
    val paymentKey: String,

    @Column(name = "order_id", unique = true, nullable = false, length = 100)
    val orderId: String,

    @Column(name = "status", nullable = false, length = 20)
    var status: String = "DONE",

    @Column(name = "cancelled_at")
    var cancelledAt: LocalDateTime? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
}
