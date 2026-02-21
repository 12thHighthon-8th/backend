package com.peugether.demo.domain.group.member

import org.springframework.data.jpa.repository.JpaRepository

interface GroupMemberRepository : JpaRepository<GroupMember, Long> {
    fun findByGroupIdAndUserId(groupId: Long, userId: Long): GroupMember?
    fun findAllByGroupId(groupId: Long): List<GroupMember>
}
