package com.peugether.demo.recipient.dto

import java.time.LocalDateTime
import java.util.UUID

data class SetRecipientResponse(
    val groupId: Long,
    val linkToken: UUID,
    val recipientLink: String,
    val linkExpiresAt: LocalDateTime,
    val message: String,
)
