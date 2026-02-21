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
                name = "두바이 쫀득쿠키 100알",
                price = 800000,
                imageUrl = "https://i.namu.wiki/i/o1x_d4wlydLg3Cs8OYPVl5m0hLUATpAjr7QW3X-zPZhqTf8byt63WCbtx1PeZyX-j3REDhQyjh2aQI2zU8cWj2ulstGCXqwOPruWX-6Lj25BzjmHFnjV8t5vB2rbDy9MBtBPh8LlI1hOnHARwdEZww.webp",
                productUrl = null,
            ),
            Product(
                externalProductId = "MOCK-002",
                name = "Apple 2023 맥미니 M2M2 Pro 10코어 · 16코어 · 2TB · 16GB",
                price = 2455410,
                imageUrl = "https://thumbnail9.coupangcdn.com/thumbnails/remote/230x230ex/image/retail/images/2023/02/08/17/2/2bbd243f-a1ae-449d-9a0d-27baa0070560.jpg",
                productUrl = null,
            ),
            Product(
                externalProductId = "MOCK-003",
                name = "Apple 2025 에어팟 프로 3 USB-C 블루투스 이어폰",
                price = 340030,
                imageUrl = "https://thumbnail.coupangcdn.com/thumbnails/remote/492x492ex/image/retail/images/13880464300917-2f4f4e1b-9bb7-4b73-ba97-0ea444b9e2d9.jpg",
                productUrl = null,
            ),
        )

        productRepository.saveAll(mockProducts)
    }
}
