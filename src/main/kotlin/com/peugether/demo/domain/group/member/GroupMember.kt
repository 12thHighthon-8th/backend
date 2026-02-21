package com.peugether.demo.domain.group.member

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "group_members",
    uniqueConstraints = [UniqueConstraint(columnNames = ["group_id", "user_id"])],
)
@EntityListeners(AuditingEntityListener::class)
class GroupMember(
    @Column(name = "group_id", nullable = false)
    val groupId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    var role: GroupMemberRole,

    @Column(name = "intended_amount", nullable = false)
    var intendedAmount: Int = 0,

    @Column(name = "paid_amount", nullable = false)
    var paidAmount: Int = 0,

    @Column(name = "payment_status", nullable = false, length = 20)
    var paymentStatus: String = "PENDING",

    @Column(name = "payment_key", length = 200)
    var paymentKey: String? = null,
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
