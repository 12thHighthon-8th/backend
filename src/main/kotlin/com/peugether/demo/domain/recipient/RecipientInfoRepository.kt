package com.peugether.demo.domain.recipient

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecipientInfoRepository : JpaRepository<RecipientInfo, Long> {
    fun findByLinkToken(linkToken: UUID): RecipientInfo?
    fun findByGroupId(groupId: Long): RecipientInfo?
}
