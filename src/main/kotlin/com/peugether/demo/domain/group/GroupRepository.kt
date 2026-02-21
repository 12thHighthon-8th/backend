package com.peugether.demo.domain.group

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GroupRepository : JpaRepository<Group, Long> {
    fun existsByInviteCode(inviteCode: String): Boolean
    fun findByInviteCode(inviteCode: String): Group?
    fun findByInviteLinkToken(inviteLinkToken: UUID): Group?
    fun findAllByIdIn(ids: Collection<Long>): List<Group>
}
