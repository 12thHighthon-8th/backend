package com.peugether.demo.common.exception

enum class ErrorCode(val status: Int, val code: String, val message: String) {
    // 인증
    UNAUTHORIZED(401, "AUTH_001", "인증이 필요합니다"),
    TOKEN_EXPIRED(401, "AUTH_002", "토큰이 만료되었습니다"),
    INVALID_TOKEN(401, "AUTH_003", "유효하지 않은 토큰입니다"),

    // 카카오 OAuth
    KAKAO_INVALID_CODE(400, "KAKAO_001", "인증 코드가 만료되었습니다. 다시 시도해주세요."),
    KAKAO_INVALID_GRANT(400, "KAKAO_002", "인증 정보가 일치하지 않습니다."),
    KAKAO_SERVER_ERROR(500, "KAKAO_003", "카카오 서버 오류입니다."),
    KAKAO_INVALID_CLIENT(500, "KAKAO_004", "서버 설정 오류입니다."),

    // 사용자
    USER_NOT_FOUND(404, "USER_001", "사용자를 찾을 수 없습니다"),
    USER_ALREADY_WITHDRAWN(400, "USER_002", "이미 탈퇴한 사용자입니다"),

    // 그룹
    GROUP_NOT_FOUND(404, "GROUP_001", "그룹을 찾을 수 없습니다"),
    NOT_GROUP_LEADER(403, "GROUP_002", "그룹장만 수행할 수 있습니다"),
    GROUP_ALREADY_FUNDED(400, "GROUP_003", "이미 모금이 완료된 그룹입니다"),
    INVALID_GROUP_STATUS(400, "GROUP_004", "현재 그룹 상태에서 수행할 수 없는 작업입니다"),
    NOT_GROUP_MEMBER(403, "GROUP_005", "그룹 멤버만 접근할 수 있습니다"),
    ALREADY_GROUP_MEMBER(400, "GROUP_006", "이미 그룹에 참여되어 있습니다"),
    GROUP_JOIN_NOT_ALLOWED(400, "GROUP_007", "현재 그룹 상태에서는 참여할 수 없습니다"),
    INVITE_CODE_NOT_FOUND(404, "GROUP_008", "유효하지 않은 초대 코드입니다"),
    INVITE_LINK_NOT_FOUND(404, "GROUP_009", "유효하지 않은 초대 링크입니다"),

    // 상품
    PRODUCT_NOT_FOUND(404, "PRODUCT_001", "상품을 찾을 수 없습니다"),

    // 모금
    FUNDING_AMOUNT_EXCEEDED(400, "FUND_001", "모금 목표 금액을 초과할 수 없습니다"),
    FUNDING_AMOUNT_NOT_SET(400, "FUND_002", "참여 금액을 먼저 설정해주세요"),
    FUNDING_ALREADY_PAID(400, "FUND_003", "이미 결제되었습니다. 변경이 불가능합니다"),
    INVALID_AMOUNT_UNIT(400, "FUND_004", "금액은 100원 단위여야 합니다"),
    FUNDING_PREPARE_FAILED(400, "FUND_005", "결제 준비에 실패했습니다"),

    // 수취인
    RECIPIENT_LINK_EXPIRED(400, "RCPT_001", "링크가 만료되었습니다"),
    RECIPIENT_ALREADY_RESPONDED(400, "RCPT_002", "이미 응답한 요청입니다"),

    // 결제
    PAYMENT_CONFIRM_FAILED(500, "PAY_001", "결제 승인에 실패했습니다"),
    PAYMENT_CANCEL_FAILED(500, "PAY_002", "결제 취소에 실패했습니다"),
}

class CustomException(
    val errorCode: ErrorCode,
    message: String = errorCode.message,
) : RuntimeException(message)
