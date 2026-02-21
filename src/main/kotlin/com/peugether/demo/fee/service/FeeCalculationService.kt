package com.peugether.demo.fee.service

import com.peugether.demo.domain.fee.FeePolicyRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FeeCalculationService(
    private val feePolicyRepository: FeePolicyRepository,
    @Value("\${fee.default-rate:0.05}") private val defaultRate: Double,
    @Value("\${fee.default-fixed:#{null}}") private val defaultFixed: Int?,
) {
    fun calculateFee(productPrice: Int): Int {
        val policy = feePolicyRepository.findActiveByPrice(productPrice)

        val rawFee = when {
            policy?.feeRate != null -> (productPrice * policy.feeRate.toDouble()).toInt()
            policy?.feeFixed != null -> policy.feeFixed
            defaultFixed != null -> defaultFixed
            else -> (productPrice * defaultRate).toInt()
        }

        return roundUpTo100(rawFee)
    }

    fun roundUpTo100(amount: Int): Int = ((amount + 99) / 100) * 100
}
