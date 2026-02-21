package com.peugether.demo.domain.user

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(
    @Column(name = "kakao_id", unique = true, nullable = false)
    val kakaoId: Long,

    @Column(name = "nickname", nullable = false, length = 50)
    var nickname: String,

    @Column(name = "profile_image_url", length = 500)
    var profileImageUrl: String? = null,

    @Column(name = "email", length = 100)
    var email: String? = null,
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

    fun softDelete() {
        this.deletedAt = LocalDateTime.now()
    }

    val isDeleted: Boolean
        get() = deletedAt != null
}
