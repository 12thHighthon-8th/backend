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
import com.peugether.demo.domain.group.GroupStatus
import com.peugether.demo.group.dto.CreateGroupRequest
import com.peugether.demo.group.dto.CreateGroupResponse
import com.peugether.demo.group.dto.GroupDetailResponse
import com.peugether.demo.group.dto.ChangeProductRequest
import com.peugether.demo.group.dto.ChangeProductResponse
import com.peugether.demo.group.dto.GroupPreviewResponse
import com.peugether.demo.group.dto.JoinGroupResponse
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

    @Transactional
    fun changeProduct(groupId: Long, userId: Long, request: ChangeProductRequest): ChangeProductResponse {
        // 1. 그룹 조회
        val group = groupRepository.findById(groupId).orElseThrow {
            CustomException(ErrorCode.GROUP_NOT_FOUND)
        }

        // 2. 그룹장 확인
        if (group.leaderId != userId) {
            throw CustomException(ErrorCode.NOT_GROUP_LEADER)
        }

        // 3. FUNDING 이전(CREATED, FUNDING) 상태에서만 변경 가능
        if (group.status != GroupStatus.CREATED && group.status != GroupStatus.FUNDING) {
            throw CustomException(ErrorCode.INVALID_GROUP_STATUS)
        }

        // 4. 새 상품 조회 (판매 가능한 상품만)
        val product = productRepository.findByIdAndIsAvailableTrue(request.productId)
            ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)

        // 5. 금액 재계산
        val targetAmount = feeCalculationService.roundUpTo100(product.price)
        val feeAmount = feeCalculationService.calculateFee(product.price)
        val totalAmount = targetAmount + feeAmount

        // 6. 그룹 업데이트
        group.productId = product.id
        group.targetAmount = targetAmount
        group.feeAmount = feeAmount
        group.totalAmount = totalAmount

        return ChangeProductResponse(
            groupId = group.id,
            product = ChangeProductResponse.ProductInfo(
                id = product.id,
                name = product.name,
                price = product.price,
                imageUrl = product.imageUrl,
            ),
            targetAmount = targetAmount,
            feeAmount = feeAmount,
            totalAmount = totalAmount,
        )
    }

    @Transactional(readOnly = true)
    fun getGroupPreviewByInviteCode(inviteCode: String, userId: Long): GroupPreviewResponse {
        val group = groupRepository.findByInviteCode(inviteCode)
            ?: throw CustomException(ErrorCode.INVITE_CODE_NOT_FOUND)
        return buildGroupPreview(group, userId)
    }

    @Transactional(readOnly = true)
    fun getGroupPreviewByLinkToken(linkToken: UUID, userId: Long): GroupPreviewResponse {
        val group = groupRepository.findByInviteLinkToken(linkToken)
            ?: throw CustomException(ErrorCode.INVITE_LINK_NOT_FOUND)
        return buildGroupPreview(group, userId)
    }

    @Transactional
    fun joinGroupByLinkToken(linkToken: UUID, userId: Long): JoinGroupResponse {
        // 1. 링크 토큰으로 그룹 조회
        val group = groupRepository.findByInviteLinkToken(linkToken)
            ?: throw CustomException(ErrorCode.INVITE_LINK_NOT_FOUND)

        // 2. 그룹 상태 확인 (CREATED 또는 FUNDING 상태만 참여 가능)
        if (group.status != GroupStatus.CREATED && group.status != GroupStatus.FUNDING) {
            throw CustomException(ErrorCode.GROUP_JOIN_NOT_ALLOWED)
        }

        // 3. 이미 멤버인지 확인
        if (groupMemberRepository.findByGroupIdAndUserId(group.id, userId) != null) {
            throw CustomException(ErrorCode.ALREADY_GROUP_MEMBER)
        }

        // 4. 사용자 존재 확인
        userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        // 5. 멤버로 추가
        val member = groupMemberRepository.save(
            GroupMember(
                groupId = group.id,
                userId = userId,
                role = GroupMemberRole.MEMBER,
            ),
        )

        return JoinGroupResponse(
            groupId = group.id,
            userId = userId,
            role = member.role.name,
            message = "그룹에 참여했습니다. 참여 금액을 설정해주세요.",
        )
    }

    @Transactional
    fun joinGroup(groupId: Long, userId: Long): JoinGroupResponse {
        // 1. 그룹 조회
        val group = groupRepository.findById(groupId).orElseThrow {
            CustomException(ErrorCode.GROUP_NOT_FOUND)
        }

        // 2. 그룹 상태 확인 (CREATED 또는 FUNDING 상태만 참여 가능)
        if (group.status != GroupStatus.CREATED && group.status != GroupStatus.FUNDING) {
            throw CustomException(ErrorCode.GROUP_JOIN_NOT_ALLOWED)
        }

        // 3. 이미 멤버인지 확인
        val existingMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
        if (existingMember != null) {
            throw CustomException(ErrorCode.ALREADY_GROUP_MEMBER)
        }

        // 4. 사용자 존재 확인
        userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        // 5. 멤버로 추가
        val member = groupMemberRepository.save(
            GroupMember(
                groupId = groupId,
                userId = userId,
                role = GroupMemberRole.MEMBER,
            ),
        )

        return JoinGroupResponse(
            groupId = groupId,
            userId = userId,
            role = member.role.name,
            message = "그룹에 참여했습니다. 참여 금액을 설정해주세요.",
        )
    }

    private fun buildGroupPreview(group: Group, userId: Long): GroupPreviewResponse {
        val product = group.productId?.let { productRepository.findById(it).orElse(null) }
        val leaderUser = userRepository.findByIdAndDeletedAtIsNull(group.leaderId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val memberCount = groupMemberRepository.findAllByGroupId(group.id).size
        val fundingPercentage = if (group.totalAmount > 0) {
            (group.fundedAmount.toDouble() / group.totalAmount.toDouble() * 100).let {
                Math.round(it * 10) / 10.0
            }
        } else 0.0

        return GroupPreviewResponse(
            id = group.id,
            name = group.name,
            status = group.status.name,
            product = product?.let {
                GroupPreviewResponse.ProductInfo(
                    id = it.id,
                    name = it.name,
                    price = it.price,
                    imageUrl = it.imageUrl,
                )
            },
            leader = GroupPreviewResponse.LeaderInfo(
                id = leaderUser.id,
                nickname = leaderUser.nickname,
            ),
            memberCount = memberCount,
            targetAmount = group.targetAmount,
            feeAmount = group.feeAmount,
            totalAmount = group.totalAmount,
            fundedAmount = group.fundedAmount,
            fundingPercentage = fundingPercentage,
            deadline = group.deadline,
            inviteCode = group.inviteCode,
            inviteLink = "$inviteLinkBaseUrl/${group.inviteLinkToken}",
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
