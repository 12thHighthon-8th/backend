package com.peugether.demo.recipient.service

import com.peugether.demo.common.exception.CustomException
import com.peugether.demo.common.exception.ErrorCode
import com.peugether.demo.domain.group.GroupRepository
import com.peugether.demo.domain.group.GroupStatus
import com.peugether.demo.domain.group.member.GroupMemberRepository
import com.peugether.demo.domain.product.ProductRepository
import com.peugether.demo.domain.recipient.RecipientInfo
import com.peugether.demo.domain.recipient.RecipientInfoRepository
import com.peugether.demo.recipient.dto.AcceptGiftRequest
import com.peugether.demo.recipient.dto.AcceptGiftResponse
import com.peugether.demo.recipient.dto.RecipientPageResponse
import com.peugether.demo.recipient.dto.RejectGiftResponse
import com.peugether.demo.recipient.dto.SetRecipientRequest
import com.peugether.demo.recipient.dto.SetRecipientResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class RecipientService(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val productRepository: ProductRepository,
    private val recipientInfoRepository: RecipientInfoRepository,
    @Value("\${app.recipient-link-base-url:https://peugether.co/recipient}") private val recipientLinkBaseUrl: String,
) {
    private val log = LoggerFactory.getLogger(RecipientService::class.java)

    @Transactional
    fun setRecipient(groupId: Long, leaderId: Long, request: SetRecipientRequest): SetRecipientResponse {
        val group = groupRepository.findById(groupId).orElseThrow {
            CustomException(ErrorCode.GROUP_NOT_FOUND)
        }

        // 그룹장 확인
        if (group.leaderId != leaderId) {
            throw CustomException(ErrorCode.NOT_GROUP_LEADER)
        }

        // FUNDED 상태에서만 수취인 등록 가능
        if (group.status != GroupStatus.FUNDED) {
            throw CustomException(ErrorCode.GROUP_NOT_FUNDED)
        }

        // 이미 수취인 정보가 존재하는 경우
        if (recipientInfoRepository.findByGroupId(groupId) != null) {
            throw CustomException(ErrorCode.RECIPIENT_ALREADY_EXISTS)
        }

        val linkToken = UUID.randomUUID()
        val linkExpiresAt = LocalDateTime.now().plusDays(7)

        val recipientInfo = recipientInfoRepository.save(
            RecipientInfo(
                groupId = groupId,
                phoneNumber = request.phoneNumber,
                linkToken = linkToken,
                linkExpiresAt = linkExpiresAt,
                notifiedAt = LocalDateTime.now(),
            ),
        )

        // 그룹 상태를 PENDING_DELIVERY로 전이
        group.status = GroupStatus.PENDING_DELIVERY

        val recipientLink = "$recipientLinkBaseUrl/$linkToken"

        // [모킹] 카카오 알림톡 / SMS 발송 생략
        // 원래 기획: 수취인 전화번호로 알림톡/SMS를 통해 배송지 입력 링크 전달
        // 알림톡 서비스 미사용으로 인해 링크를 API 응답으로 직접 반환하는 방식으로 대체
        log.info(
            "[MOCK] 카카오 알림톡/SMS 발송 생략 - groupId={}, phoneNumber={}, recipientLink={}",
            groupId,
            request.phoneNumber.replace(Regex("(\\d{3})\\d{4}(\\d{4})"), "$1****$2"),
            recipientLink,
        )

        return SetRecipientResponse(
            groupId = groupId,
            linkToken = recipientInfo.linkToken,
            recipientLink = recipientLink,
            linkExpiresAt = linkExpiresAt,
            message = "[모킹] 알림톡 발송 대신 링크를 직접 반환합니다. 수취인에게 아래 링크를 전달해주세요.",
        )
    }

    @Transactional(readOnly = true)
    fun getRecipientPage(linkToken: UUID): RecipientPageResponse {
        val recipientInfo = recipientInfoRepository.findByLinkToken(linkToken)
            ?: throw CustomException(ErrorCode.RECIPIENT_NOT_FOUND)

        if (LocalDateTime.now().isAfter(recipientInfo.linkExpiresAt)) {
            throw CustomException(ErrorCode.RECIPIENT_LINK_EXPIRED)
        }

        val group = groupRepository.findById(recipientInfo.groupId).orElseThrow {
            CustomException(ErrorCode.GROUP_NOT_FOUND)
        }

        val product = group.productId?.let { productRepository.findById(it).orElse(null) }

        return RecipientPageResponse(
            groupName = group.name,
            productName = product?.name,
            productImageUrl = product?.imageUrl,
            message = null, // GroupMessage 미구현, 추후 연동
            expiresAt = recipientInfo.linkExpiresAt,
            status = recipientInfo.acceptStatus,
        )
    }

    @Transactional
    fun acceptGift(linkToken: UUID, request: AcceptGiftRequest): AcceptGiftResponse {
        val recipientInfo = recipientInfoRepository.findByLinkToken(linkToken)
            ?: throw CustomException(ErrorCode.RECIPIENT_NOT_FOUND)

        if (LocalDateTime.now().isAfter(recipientInfo.linkExpiresAt)) {
            throw CustomException(ErrorCode.RECIPIENT_LINK_EXPIRED)
        }

        if (recipientInfo.acceptStatus != "PENDING") {
            throw CustomException(ErrorCode.RECIPIENT_ALREADY_RESPONDED)
        }

        recipientInfo.name = request.name
        recipientInfo.address = request.address
        recipientInfo.addressDetail = request.addressDetail
        recipientInfo.zipCode = request.zipCode
        recipientInfo.acceptStatus = "ACCEPTED"

        // [모킹] 파트너 서비스 주문 생성 생략
        log.info("[MOCK] 파트너 서비스 주문 생성 생략 - groupId={}", recipientInfo.groupId)

        // TODO: 그룹장에게 FCM 푸시 알림 발송 예정
        log.info("[TODO] 배송지 입력 완료 - groupId={}, 그룹장 FCM 알림 미구현", recipientInfo.groupId)

        return AcceptGiftResponse(message = "배송지가 입력되었습니다. 선물이 곧 배송됩니다.")
    }

    @Transactional
    fun rejectGift(linkToken: UUID): RejectGiftResponse {
        val recipientInfo = recipientInfoRepository.findByLinkToken(linkToken)
            ?: throw CustomException(ErrorCode.RECIPIENT_NOT_FOUND)

        if (LocalDateTime.now().isAfter(recipientInfo.linkExpiresAt)) {
            throw CustomException(ErrorCode.RECIPIENT_LINK_EXPIRED)
        }

        if (recipientInfo.acceptStatus != "PENDING") {
            throw CustomException(ErrorCode.RECIPIENT_ALREADY_RESPONDED)
        }

        recipientInfo.acceptStatus = "REJECTED"

        val group = groupRepository.findById(recipientInfo.groupId).orElseThrow {
            CustomException(ErrorCode.GROUP_NOT_FOUND)
        }
        group.status = GroupStatus.CANCELLED

        // TODO: 전원 전액 환불 처리 예정
        log.info("[TODO] 수취인 거절 - groupId={}, 전원 환불 미구현", recipientInfo.groupId)

        return RejectGiftResponse(message = "선물을 거절했습니다.")
    }
}
