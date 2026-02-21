package com.peugether.demo.group.controller

import com.peugether.demo.common.exception.CustomException
import com.peugether.demo.common.exception.ErrorCode
import com.peugether.demo.group.dto.ChangeProductRequest
import com.peugether.demo.group.dto.ChangeProductResponse
import com.peugether.demo.group.dto.CreateGroupRequest
import com.peugether.demo.group.dto.CreateGroupResponse
import com.peugether.demo.group.dto.GroupDetailResponse
import com.peugether.demo.group.dto.GroupListResponse
import com.peugether.demo.group.dto.GroupPreviewResponse
import com.peugether.demo.group.dto.JoinGroupRequest
import com.peugether.demo.group.dto.JoinGroupResponse
import com.peugether.demo.group.service.GroupService
import com.peugether.demo.recipient.dto.SetRecipientRequest
import com.peugether.demo.recipient.dto.SetRecipientResponse
import com.peugether.demo.recipient.service.RecipientService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/groups")
class GroupController(
    private val groupService: GroupService,
    private val recipientService: RecipientService,
) {
    @GetMapping
    fun getMyGroups(
        @AuthenticationPrincipal userId: Long?,
    ): ResponseEntity<GroupListResponse> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        return ResponseEntity.ok(groupService.getMyGroups(resolvedUserId))
    }

    @PostMapping
    fun createGroup(
        @AuthenticationPrincipal userId: Long?,
        @Valid @RequestBody request: CreateGroupRequest,
    ): ResponseEntity<CreateGroupResponse> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        val response = groupService.createGroup(resolvedUserId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{groupId}")
    fun getGroupDetail(
        @AuthenticationPrincipal userId: Long?,
        @PathVariable groupId: Long,
    ): ResponseEntity<GroupDetailResponse> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        val response = groupService.getGroupDetail(groupId, resolvedUserId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{groupId}/product")
    fun changeProduct(
        @AuthenticationPrincipal userId: Long?,
        @PathVariable groupId: Long,
        @Valid @RequestBody request: ChangeProductRequest,
    ): ResponseEntity<ChangeProductResponse> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        val response = groupService.changeProduct(groupId, resolvedUserId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/invent/{inviteCode}")
    fun getGroupByInviteCode(
        @AuthenticationPrincipal userId: Long?,
        @PathVariable inviteCode: String,
    ): ResponseEntity<GroupPreviewResponse> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        val response = groupService.getGroupPreviewByInviteCode(inviteCode, resolvedUserId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/invent")
    fun joinGroup(
        @AuthenticationPrincipal userId: Long?,
        @RequestBody request: JoinGroupRequest,
    ): ResponseEntity<JoinGroupResponse> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        val response = groupService.joinGroup(resolvedUserId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/{groupId}/recipient")
    fun setRecipient(
        @AuthenticationPrincipal userId: Long?,
        @PathVariable groupId: Long,
        @Valid @RequestBody request: SetRecipientRequest,
    ): ResponseEntity<SetRecipientResponse> {
        val resolvedUserId = userId ?: throw CustomException(ErrorCode.UNAUTHORIZED)
        val response = recipientService.setRecipient(groupId, resolvedUserId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
