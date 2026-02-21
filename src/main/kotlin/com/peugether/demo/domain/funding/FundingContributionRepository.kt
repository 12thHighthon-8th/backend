package com.peugether.demo.domain.funding

import org.springframework.data.jpa.repository.JpaRepository

interface FundingContributionRepository : JpaRepository<FundingContribution, Long> {
    fun findByGroupIdAndUserIdAndStatus(groupId: Long, userId: Long, status: String): FundingContribution?
    fun findByOrderId(orderId: String): FundingContribution?
    fun findAllByGroupIdAndStatus(groupId: Long, status: String): List<FundingContribution>
}
