# backend

## 참고 사항

- kakao 선물하기 for BIZ API 사용 불가로 인해 모킹으로 대체하였음
- 토스페이먼츠 결제 API(문서 5-1) 사용 불가로 인해 결제 승인/취소 과정을 모킹으로 대체하였음
  - `POST /api/v1/groups/{groupId}/funding/confirm`: 실제 토스페이먼츠 `/v1/payments/confirm` 호출 없이 DB 직접 업데이트
  - `POST /api/v1/groups/{groupId}/funding/cancel`: 실제 토스페이먼츠 취소 API 호출 없이 DB 직접 업데이트
  - paymentKey는 요청값을 그대로 수용 (프론트에서 `MOCK_PAY_*` 형태로 전달 가능)
  - 모금 100% 달성 시 그룹장 FCM 알림 및 개별 취소 시 그룹장 알림은 TODO 상태 (FCM 미구현)