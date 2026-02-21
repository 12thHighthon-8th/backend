package com.peugether.demo.recipient.dto

import java.time.LocalDateTime

data class RecipientPageResponse(
    val groupName: String,
    val productName: String?,
    val productImageUrl: String?,
    val message: String?,
    val expiresAt: LocalDateTime,
    val status: String,
)
