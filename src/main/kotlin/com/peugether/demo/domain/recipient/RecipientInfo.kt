package com.peugether.demo.domain.recipient

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "recipient_info")
@EntityListeners(AuditingEntityListener::class)
class RecipientInfo(
    @Column(name = "group_id", nullable = false, unique = true)
    val groupId: Long,

    @Column(name = "phone_number", nullable = false, length = 20)
    val phoneNumber: String,

    @Column(name = "name", length = 50)
    var name: String? = null,

    @Column(name = "address", columnDefinition = "TEXT")
    var address: String? = null,

    @Column(name = "address_detail", columnDefinition = "TEXT")
    var addressDetail: String? = null,

    @Column(name = "zip_code", length = 10)
    var zipCode: String? = null,

    @Column(name = "accept_status", nullable = false, length = 20)
    var acceptStatus: String = "PENDING",

    @Column(name = "link_token", nullable = false, unique = true, columnDefinition = "uuid")
    val linkToken: UUID,

    @Column(name = "link_expires_at", nullable = false)
    val linkExpiresAt: LocalDateTime,

    @Column(name = "notified_at")
    var notifiedAt: LocalDateTime? = null,
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

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
}
