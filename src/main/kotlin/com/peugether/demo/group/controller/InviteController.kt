package com.peugether.demo.group.controller

import com.peugether.demo.common.exception.CustomException
import com.peugether.demo.common.exception.ErrorCode
import com.peugether.demo.group.dto.GroupPreviewResponse
import com.peugether.demo.group.dto.JoinGroupResponse
import com.peugether.demo.group.service.GroupService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/invent")
class InviteController(
    private val groupService: GroupService,
) {
    @GetMapping("/{linkToken}")
    fun getGroupPreview(
        @AuthenticationPrincipal userId: Long?,
        @PathVariable linkToken: UUID,
    ): ResponseEntity<GroupPreviewResponse> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        val response = groupService.getGroupPreviewByLinkToken(linkToken, resolvedUserId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{linkToken}")
    fun joinGroup(
        @AuthenticationPrincipal userId: Long?,
        @PathVariable linkToken: UUID,
    ): ResponseEntity<JoinGroupResponse> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        val response = groupService.joinGroupByLinkToken(linkToken, resolvedUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
