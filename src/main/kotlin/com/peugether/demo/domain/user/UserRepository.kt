package com.peugether.demo.domain.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByKakaoId(kakaoId: Long): User?
    fun findByIdAndDeletedAtIsNull(id: Long): User?
}
