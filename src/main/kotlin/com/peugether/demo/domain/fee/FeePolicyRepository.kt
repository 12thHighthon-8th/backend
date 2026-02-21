package com.peugether.demo.domain.fee

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FeePolicyRepository : JpaRepository<FeePolicy, Long> {

    @Query(
        """
        SELECT fp FROM FeePolicy fp
        WHERE fp.isActive = true
          AND fp.minPrice <= :price
          AND (fp.maxPrice IS NULL OR fp.maxPrice > :price)
        ORDER BY fp.minPrice DESC
        LIMIT 1
        """,
    )
    fun findActiveByPrice(price: Int): FeePolicy?
}
