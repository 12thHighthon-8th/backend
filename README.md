# backend

## 참고 사항

- kakao 선물하기 for BIZ API 사용 불가로 인해 모킹으로 대체하였음
- 토스페이먼츠 결제 API(문서 5-1) 사용 불가로 인해 결제 승인/취소 과정을 모킹으로 대체하였음
  - `POST /api/v1/groups/{groupId}/funding/confirm`: 실제 토스페이먼츠 `/v1/payments/confirm` 호출 없이 DB 직접 업데이트
  - `POST /api/v1/groups/{groupId}/funding/cancel`: 실제 토스페이먼츠 취소 API 호출 없이 DB 직접 업데이트
  - paymentKey는 요청값을 그대로 수용 (프론트에서 `MOCK_PAY_*` 형태로 전달 가능)
  - 모금 100% 달성 시 그룹장 FCM 알림 및 개별 취소 시 그룹장 알림은 TODO 상태 (FCM 미구현)
- 카카오 알림톡(문서 5-2) 사용 불가로 인해 수취인 링크 발송을 모킹으로 대체하였음
  - 원래 기획: 모금 완료 후 그룹장이 수취인 전화번호를 입력하면 카카오 알림톡 또는 SMS로 배송지 입력 링크를 수취인에게 자동 발송
  - `POST /api/v1/groups/{groupId}/recipient`: 알림톡/SMS 발송 없이 생성된 링크를 API 응답으로 직접 반환
    - 응답의 `recipientLink` 필드에 수취인이 접속할 수 있는 링크가 포함됨
    - 그룹장이 해당 링크를 수취인에게 직접 전달해야 함