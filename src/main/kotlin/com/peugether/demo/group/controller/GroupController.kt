package com.peugether.demo.group.controller

import com.peugether.demo.common.exception.CustomException
import com.peugether.demo.common.exception.ErrorCode
import com.peugether.demo.group.dto.CreateGroupRequest
import com.peugether.demo.group.dto.CreateGroupResponse
import com.peugether.demo.group.dto.GroupDetailResponse
import com.peugether.demo.group.service.GroupService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/groups")
class GroupController(
    private val groupService: GroupService,
) {
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
}
