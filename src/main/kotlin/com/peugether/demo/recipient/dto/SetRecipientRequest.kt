package com.peugether.demo.recipient.dto

import jakarta.validation.constraints.NotBlank

data class SetRecipientRequest(
    @field:NotBlank
    val phoneNumber: String,
)
