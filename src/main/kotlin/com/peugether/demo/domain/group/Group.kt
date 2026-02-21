package com.peugether.demo.domain.group

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "groups")
@EntityListeners(AuditingEntityListener::class)
class Group(
    @Column(name = "leader_id", nullable = false)
    val leaderId: Long,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "invite_code", unique = true, nullable = false, length = 20)
    val inviteCode: String,

    @Column(name = "invite_link_token", unique = true, nullable = false, columnDefinition = "uuid")
    val inviteLinkToken: UUID,

    @Column(name = "product_id")
    var productId: Long? = null,

    @Column(name = "target_amount", nullable = false)
    var targetAmount: Int = 0,

    @Column(name = "fee_amount", nullable = false)
    var feeAmount: Int = 0,

    @Column(name = "total_amount", nullable = false)
    var totalAmount: Int = 0,

    @Column(name = "funded_amount", nullable = false)
    var fundedAmount: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    var status: GroupStatus = GroupStatus.CREATED,

    @Column(name = "deadline", columnDefinition = "TIMESTAMPTZ")
    var deadline: OffsetDateTime? = null,
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
