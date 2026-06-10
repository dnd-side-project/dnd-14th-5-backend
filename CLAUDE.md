# 프로젝트 규칙

- 커밋 메시지는 한글로 작성

## 논의 필요: 캐릭터 그룹 참여 로직

현재 `getTodayReflectionsForCharacterGroup`은 멤버 여부 무관하게 해당 category의 모든 회고를 보여줌.

의도된 스펙:
- 캐릭터 그룹은 모든 유저에게 노출 (`getMyGroups` 현재 구현 OK)
- 단, 내 회고가 해당 그룹에 등록되려면 `joinGroup` 해야 함
- 즉 `getTodayReflectionsForCharacterGroup`은 groupMember인 유저의 회고만 반환해야 함

→ 팀원과 스펙 확정 후 수정 필요
