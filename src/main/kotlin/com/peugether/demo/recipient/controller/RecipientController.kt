package com.peugether.demo.recipient.controller

import com.peugether.demo.recipient.dto.AcceptGiftRequest
import com.peugether.demo.recipient.dto.AcceptGiftResponse
import com.peugether.demo.recipient.dto.RecipientPageResponse
import com.peugether.demo.recipient.dto.RejectGiftResponse
import com.peugether.demo.recipient.service.RecipientService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/recipient")
class RecipientController(
    private val recipientService: RecipientService,
) {
    @GetMapping("/{linkToken}")
    fun getRecipientPage(
        @PathVariable linkToken: UUID,
    ): ResponseEntity<RecipientPageResponse> {
        val response = recipientService.getRecipientPage(linkToken)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{linkToken}/accept")
    fun acceptGift(
        @PathVariable linkToken: UUID,
        @Valid @RequestBody request: AcceptGiftRequest,
    ): ResponseEntity<AcceptGiftResponse> {
        val response = recipientService.acceptGift(linkToken, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{linkToken}/reject")
    fun rejectGift(
        @PathVariable linkToken: UUID,
    ): ResponseEntity<RejectGiftResponse> {
        val response = recipientService.rejectGift(linkToken)
        return ResponseEntity.ok(response)
    }
}
