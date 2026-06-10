# 프로젝트 규칙

- 커밋 메시지는 한글로 작성

## 캐릭터 그룹 참여 로직

- 캐릭터 그룹은 모든 유저에게 노출 (`getMyGroups` OK)
- `joinGroup` 이후부터 내 회고가 그룹에 등록(노출)됨
- `leaveGroup` 시 멤버십 제거 → 그룹 뷰에서 자동 숨김 (회고 데이터 삭제 없음)
- `getTodayReflectionsForCharacterGroup`은 groupMember인 유저의 회고만 반환 (구현 완료)
