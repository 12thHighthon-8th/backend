package com.peugether.demo.group.service

import com.peugether.demo.common.exception.CustomException
import com.peugether.demo.common.exception.ErrorCode
import com.peugether.demo.domain.group.Group
import com.peugether.demo.domain.group.GroupRepository
import com.peugether.demo.domain.group.member.GroupMember
import com.peugether.demo.domain.group.member.GroupMemberRepository
import com.peugether.demo.domain.group.member.GroupMemberRole
import com.peugether.demo.domain.product.ProductRepository
import com.peugether.demo.domain.user.UserRepository
import com.peugether.demo.fee.service.FeeCalculationService
import com.peugether.demo.group.dto.CreateGroupRequest
import com.peugether.demo.group.dto.CreateGroupResponse
import com.peugether.demo.group.dto.GroupDetailResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val feeCalculationService: FeeCalculationService,
    @Value("\${app.invite-link-base-url:https://peugether.co/join}") private val inviteLinkBaseUrl: String,
) {
    companion object {
        private val INVITE_CODE_CHARS = ('A'..'Z') + ('0'..'9')
        private const val INVITE_CODE_LENGTH = 8
    }

    @Transactional
    fun createGroup(leaderId: Long, request: CreateGroupRequest): CreateGroupResponse {
        // 1. 사용자 조회
        val leader = userRepository.findByIdAndDeletedAtIsNull(leaderId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        // 2. 상품 조회 (판매 가능한 상품만)
        val product = productRepository.findByIdAndIsAvailableTrue(request.productId)
            ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)

        // 3. 금액 계산
        val targetAmount = feeCalculationService.roundUpTo100(product.price)
        val feeAmount = feeCalculationService.calculateFee(product.price)
        val totalAmount = targetAmount + feeAmount

        // 4. 초대 코드 / 링크 토큰 생성
        val inviteCode = generateUniqueInviteCode()
        val inviteLinkToken = UUID.randomUUID()

        // 5. 그룹 저장
        val group = groupRepository.save(
            Group(
                leaderId = leaderId,
                name = request.name,
                inviteCode = inviteCode,
                inviteLinkToken = inviteLinkToken,
                productId = product.id,
                targetAmount = targetAmount,
                feeAmount = feeAmount,
                totalAmount = totalAmount,
                deadline = request.deadline,
            ),
        )

        // 6. 그룹장 멤버 저장
        groupMemberRepository.save(
            GroupMember(
                groupId = group.id,
                userId = leaderId,
                role = GroupMemberRole.LEADER,
            ),
        )

        return CreateGroupResponse(
            id = group.id,
            name = group.name,
            inviteCode = group.inviteCode,
            inviteLink = "$inviteLinkBaseUrl/$inviteLinkToken",
            product = CreateGroupResponse.ProductInfo(
                id = product.id,
                name = product.name,
                price = product.price,
                imageUrl = product.imageUrl,
            ),
            targetAmount = group.targetAmount,
            feeAmount = group.feeAmount,
            totalAmount = group.totalAmount,
            status = group.status.name,
            deadline = group.deadline,
            leader = CreateGroupResponse.LeaderInfo(
                id = leader.id,
                nickname = leader.nickname,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun getGroupDetail(groupId: Long, requesterId: Long): GroupDetailResponse {
        // 1. 그룹 조회
        val group = groupRepository.findById(groupId).orElseThrow {
            CustomException(ErrorCode.GROUP_NOT_FOUND)
        }

        // 2. 멤버 여부 확인
        groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
            ?: throw CustomException(ErrorCode.NOT_GROUP_MEMBER)

        // 3. 상품 정보 조회
        val product = group.productId?.let { productRepository.findById(it).orElse(null) }

        // 4. 멤버 목록 조회
        val members = groupMemberRepository.findAllByGroupId(groupId)

        // 5. 멤버 닉네임 조회
        val userIds = members.map { it.userId }.toSet()
        val users = userRepository.findAllById(userIds).associateBy { it.id }

        // 6. 리더 정보
        val leaderUser = users[group.leaderId]
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        return GroupDetailResponse(
            id = group.id,
            name = group.name,
            status = group.status.name,
            inviteCode = group.inviteCode,
            inviteLink = "$inviteLinkBaseUrl/${group.inviteLinkToken}",
            product = product?.let {
                GroupDetailResponse.ProductInfo(
                    id = it.id,
                    name = it.name,
                    price = it.price,
                    imageUrl = it.imageUrl,
                )
            },
            leader = GroupDetailResponse.LeaderInfo(
                id = leaderUser.id,
                nickname = leaderUser.nickname,
            ),
            members = members.map { member ->
                val user = users[member.userId]
                GroupDetailResponse.MemberInfo(
                    userId = member.userId,
                    nickname = user?.nickname ?: "알 수 없음",
                    role = member.role.name,
                    intendedAmount = member.intendedAmount,
                    paidAmount = member.paidAmount,
                    paymentStatus = member.paymentStatus,
                )
            },
            targetAmount = group.targetAmount,
            feeAmount = group.feeAmount,
            totalAmount = group.totalAmount,
            fundedAmount = group.fundedAmount,
            deadline = group.deadline,
            createdAt = group.createdAt,
        )
    }

    private fun generateUniqueInviteCode(): String {
        repeat(10) {
            val code = (1..INVITE_CODE_LENGTH).map { INVITE_CODE_CHARS.random() }.joinToString("")
            if (!groupRepository.existsByInviteCode(code)) return code
        }
        throw IllegalStateException("초대 코드 생성에 실패했습니다")
    }
}
