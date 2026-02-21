package com.peugether.demo.product

import com.peugether.demo.domain.product.Product
import com.peugether.demo.domain.product.ProductRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * kakao 선물하기 for BIZ 사용 불가로 인해 모킹 상품 데이터를 초기화합니다.
 * 앱 시작 시 products 테이블이 비어 있으면 예시 데이터를 삽입합니다.
 */
@Component
class MockProductDataInitializer(
    private val productRepository: ProductRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (productRepository.count() > 0) return

        val mockProducts = listOf(
            Product(
                externalProductId = "MOCK-001",
                name = "스타벅스 아메리카노 10잔 기프티콘",
                price = 47500,
                imageUrl = null,
                productUrl = null,
            ),
            Product(
                externalProductId = "MOCK-002",
                name = "올리브영 스킨케어 기프트 세트",
                price = 89000,
                imageUrl = null,
                productUrl = null,
            ),
            Product(
                externalProductId = "MOCK-003",
                name = "애플 에어팟 프로 (2세대)",
                price = 329000,
                imageUrl = null,
                productUrl = null,
            ),
            Product(
                externalProductId = "MOCK-004",
                name = "다이슨 에어랩 멀티스타일러",
                price = 649000,
                imageUrl = null,
                productUrl = null,
            ),
            Product(
                externalProductId = "MOCK-005",
                name = "나이키 에어포스 1",
                price = 119000,
                imageUrl = null,
                productUrl = null,
            ),
            Product(
                externalProductId = "MOCK-006",
                name = "CGV 영화관람권 5매",
                price = 69000,
                imageUrl = null,
                productUrl = null,
            ),
            Product(
                externalProductId = "MOCK-007",
                name = "배스킨라빈스 패밀리 케이크",
                price = 42900,
                imageUrl = null,
                productUrl = null,
            ),
            Product(
                externalProductId = "MOCK-008",
                name = "신세계 상품권 50,000원권",
                price = 50000,
                imageUrl = null,
                productUrl = null,
            ),
            Product(
                externalProductId = "MOCK-009",
                name = "삼성 갤럭시 버즈3 Pro",
                price = 239000,
                imageUrl = null,
                productUrl = null,
            ),
            Product(
                externalProductId = "MOCK-010",
                name = "무신사 스탠다드 패딩 점퍼",
                price = 159000,
                imageUrl = null,
                productUrl = null,
            ),
        )

        productRepository.saveAll(mockProducts)
    }
}
