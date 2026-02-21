package com.peugether.demo.funding.service

import com.peugether.demo.common.exception.CustomException
import com.peugether.demo.common.exception.ErrorCode
import com.peugether.demo.domain.funding.FundingContribution
import com.peugether.demo.domain.funding.FundingContributionRepository
import com.peugether.demo.domain.group.GroupRepository
import com.peugether.demo.domain.group.GroupStatus
import com.peugether.demo.domain.group.member.GroupMemberRepository
import com.peugether.demo.domain.product.ProductRepository
import com.peugether.demo.funding.dto.ConfirmFundingRequest
import com.peugether.demo.funding.dto.ConfirmFundingResponse
import com.peugether.demo.funding.dto.FundingStatusResponse
import com.peugether.demo.funding.dto.PrepareFundingRequest
import com.peugether.demo.funding.dto.PrepareFundingResponse
import com.peugether.demo.funding.dto.SetFundingAmountRequest
import com.peugether.demo.funding.dto.SetFundingAmountResponse
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class FundingService(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val fundingContributionRepository: FundingContributionRepository,
    private val productRepository: ProductRepository,
    private val redisTemplate: RedisTemplate<String, String>,
) {
    private val log = LoggerFactory.getLogger(FundingService::class.java)

    companion object {
        private const val PREPARE_KEY_PREFIX = "payment_prepare:"
        private val PREPARE_TTL = Duration.ofMinutes(5)
    }

    @Transactional(readOnly = true)
    fun getFundingStatus(groupId: Long, userId: Long): FundingStatusResponse {
        val group = groupRepository.findById(groupId).orElseThrow {
            CustomException(ErrorCode.GROUP_NOT_FOUND)
        }
        val member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            ?: throw CustomException(ErrorCode.NOT_GROUP_MEMBER)

        val remaining = group.totalAmount - group.fundedAmount
        val percentage = if (group.totalAmount > 0) {
            Math.round(group.fundedAmount.toDouble() / group.totalAmount.toDouble() * 1000) / 10.0
        } else 0.0

        return FundingStatusResponse(
            groupId = groupId,
            totalAmount = group.totalAmount,
            fundedAmount = group.fundedAmount,
            remainingAmount = remaining,
            myCurrentAmount = member.intendedAmount,
            fundingPercentage = percentage,
        )
    }

    @Transactional
    fun setFundingAmount(groupId: Long, userId: Long, request: SetFundingAmountRequest): SetFundingAmountResponse {
        val group = groupRepository.findById(groupId).orElseThrow {
            CustomException(ErrorCode.GROUP_NOT_FOUND)
        }

        if (group.status != GroupStatus.FUNDING) {
            throw CustomException(ErrorCode.INVALID_GROUP_STATUS)
        }

        val member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            ?: throw CustomException(ErrorCode.NOT_GROUP_MEMBER)

        val amount = request.amount
        if (amount != 0 && amount % 100 != 0) {
            throw CustomException(ErrorCode.INVALID_AMOUNT_UNIT)
        }

        // 현재까지 모금된 금액 + 이번 설정 금액이 총액 초과 불가
        if (amount > 0 && group.fundedAmount + amount > group.totalAmount) {
            throw CustomException(ErrorCode.FUNDING_AMOUNT_EXCEEDED)
        }

        member.intendedAmount = amount

        val message = if (amount == 0) "참여 금액이 취소되었습니다."
                      else "참여 금액이 설정되었습니다. 결제를 진행해 주세요."

        return SetFundingAmountResponse(
            groupId = groupId,
            userId = userId,
            amount = amount,
            message = message,
        )
    }

    @Transactional
    fun prepareFunding(groupId: Long, userId: Long, request: PrepareFundingRequest): PrepareFundingResponse {
        val group = groupRepository.findById(groupId).orElseThrow {
            CustomException(ErrorCode.GROUP_NOT_FOUND)
        }

        if (group.status != GroupStatus.FUNDING) {
            throw CustomException(ErrorCode.INVALID_GROUP_STATUS)
        }

        val member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            ?: throw CustomException(ErrorCode.NOT_GROUP_MEMBER)

        if (member.intendedAmount <= 0) {
            throw CustomException(ErrorCode.FUNDING_AMOUNT_NOT_SET)
        }

        val amount = request.amount
        if (amount != member.intendedAmount) {
            throw CustomException(ErrorCode.FUNDING_PREPARE_FAILED)
        }

        if (amount % 100 != 0) {
            throw CustomException(ErrorCode.INVALID_AMOUNT_UNIT)
        }

        // 초과 모금 방지
        if (group.fundedAmount + amount > group.totalAmount) {
            throw CustomException(ErrorCode.FUNDING_AMOUNT_EXCEEDED)
        }

        val orderId = "GROUP_${groupId}_USER_${userId}_${Instant.now().epochSecond}"

        // Redis에 임시 저장 (5분 TTL)
        redisTemplate.opsForValue().set(
            "$PREPARE_KEY_PREFIX$orderId",
            "$userId:$amount",
            PREPARE_TTL,
        )

        val product = group.productId?.let { productRepository.findById(it).orElse(null) }
        val orderName = "${product?.name ?: group.name} - ${"%,d".format(amount)}원"

        return PrepareFundingResponse(
            orderId = orderId,
            amount = amount,
            orderName = orderName,
        )
    }

    @Transactional
    fun confirmFunding(groupId: Long, userId: Long, request: ConfirmFundingRequest): ConfirmFundingResponse {
        val group = groupRepository.findById(groupId).orElseThrow {
            CustomException(ErrorCode.GROUP_NOT_FOUND)
        }

        if (group.status != GroupStatus.FUNDING) {
            throw CustomException(ErrorCode.INVALID_GROUP_STATUS)
        }

        val member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            ?: throw CustomException(ErrorCode.NOT_GROUP_MEMBER)

        // Redis에서 orderId 검증
        val prepareKey = "$PREPARE_KEY_PREFIX${request.orderId}"
        val prepareValue = redisTemplate.opsForValue().get(prepareKey)
            ?: throw CustomException(ErrorCode.FUNDING_PREPARE_FAILED)

        val parts = prepareValue.split(":")
        if (parts.size != 2 || parts[0].toLongOrNull() != userId || parts[1].toIntOrNull() != request.amount) {
            throw CustomException(ErrorCode.FUNDING_PREPARE_FAILED)
        }

        // [모킹] 토스페이먼츠 결제 승인 API 호출 생략
        // 실제 연동 시: POST https://api.tosspayments.com/v1/payments/confirm
        log.info("[MOCK] 토스페이먼츠 결제 승인 생략 - orderId={}, amount={}", request.orderId, request.amount)

        // DB 업데이트
        fundingContributionRepository.save(
            FundingContribution(
                groupId = groupId,
                userId = userId,
                amount = request.amount,
                paymentKey = request.paymentKey,
                orderId = request.orderId,
                status = "DONE",
            ),
        )

        // 누적 납부 금액 업데이트
        member.paidAmount += request.amount
        member.paymentStatus = "PAID"
        member.paymentKey = request.paymentKey
        // 다음 결제를 위해 intendedAmount 초기화
        member.intendedAmount = 0

        group.fundedAmount += request.amount

        val isFullyFunded = group.fundedAmount >= group.totalAmount
        if (isFullyFunded) {
            group.status = GroupStatus.FUNDED
            // TODO: 그룹장에게 FCM 푸시 알림 발송 예정
            log.info("[TODO] 모금 완료 - groupId={}, 그룹장 FCM 알림 미구현", groupId)
        }

        // Redis orderId 삭제
        redisTemplate.delete(prepareKey)

        return ConfirmFundingResponse(
            groupId = groupId,
            userId = userId,
            amount = request.amount,
            paymentKey = request.paymentKey,
            orderId = request.orderId,
            fundedAmount = group.fundedAmount,
            totalAmount = group.totalAmount,
            isFullyFunded = isFullyFunded,
        )
    }
}
