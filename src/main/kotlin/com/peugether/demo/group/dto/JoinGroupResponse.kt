package com.peugether.demo.group.dto

data class JoinGroupResponse(
    val groupId: Long,
    val userId: Long,
    val role: String,
    val message: String,
)
