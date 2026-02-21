package com.peugether.demo.domain.group

import org.springframework.data.jpa.repository.JpaRepository

interface GroupRepository : JpaRepository<Group, Long> {
    fun existsByInviteCode(inviteCode: String): Boolean
}
