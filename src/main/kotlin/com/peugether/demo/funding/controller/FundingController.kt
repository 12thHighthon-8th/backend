package com.peugether.demo.funding.controller

import com.peugether.demo.funding.dto.ConfirmFundingRequest
import com.peugether.demo.funding.dto.ConfirmFundingResponse
import com.peugether.demo.funding.dto.FundingStatusResponse
import com.peugether.demo.funding.dto.PrepareFundingRequest
import com.peugether.demo.funding.dto.PrepareFundingResponse
import com.peugether.demo.funding.dto.SetFundingAmountRequest
import com.peugether.demo.funding.dto.SetFundingAmountResponse
import com.peugether.demo.funding.service.FundingService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/groups/{groupId}/funding")
class FundingController(
    private val fundingService: FundingService,
) {
    @GetMapping("/status")
    fun getFundingStatus(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<FundingStatusResponse> {
        return ResponseEntity.ok(fundingService.getFundingStatus(groupId, userId))
    }

    @PutMapping("/amount")
    fun setFundingAmount(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: SetFundingAmountRequest,
    ): ResponseEntity<SetFundingAmountResponse> {
        return ResponseEntity.ok(fundingService.setFundingAmount(groupId, userId, request))
    }

    @PostMapping("/prepare")
    fun prepareFunding(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: PrepareFundingRequest,
    ): ResponseEntity<PrepareFundingResponse> {
        return ResponseEntity.ok(fundingService.prepareFunding(groupId, userId, request))
    }

    @PostMapping("/confirm")
    fun confirmFunding(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: ConfirmFundingRequest,
    ): ResponseEntity<ConfirmFundingResponse> {
        return ResponseEntity.ok(fundingService.confirmFunding(groupId, userId, request))
    }

}
