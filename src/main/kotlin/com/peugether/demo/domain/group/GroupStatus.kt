package com.peugether.demo.domain.group

enum class GroupStatus {
    CREATED,            // 그룹 생성, 상품 선택 완료, 모금 시작 전
    FUNDING,            // 모금 진행 중
    FUNDED,             // 모금 100% 달성, 수취인 전화번호 입력 대기
    PENDING_DELIVERY,   // 수취인 정보 입력 완료, 주문/배송 처리 중
    DELIVERED,          // 수령 완료
    CANCELLED,          // 중도 취소
    ARCHIVED_COMPLETE,  // 정상 완료 아카이빙
    ARCHIVED_CANCELLED, // 취소 아카이빙
}
