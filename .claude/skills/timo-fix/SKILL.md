---
name: timo-fix
description: 버그 진단 → 수정 → 검증 → 커밋 자동 워크플로우
---

# Timo 버그 수정

대상: $ARGUMENTS

## Phase 1: 병렬 진단

두 에이전트를 동시에 실행:

**에이전트 A — 원인 분석:**
- 에러 메시지/스택트레이스에서 발생 위치 특정
- 관련 도메인 코드 흐름 추적 (Controller → Service → Repository → Entity)
- `SecurityUtil.getCurrentUserId()` 및 소유자 검증 로직 확인
- 동일 패턴이 다른 도메인에도 존재하는지 확인

**에이전트 B — 테스트 분석:**
- `src/test/`에서 관련 도메인 테스트 파일 탐색
- 버그를 검증할 수 있는 기존 테스트 케이스 존재 여부
- `MockedStatic<SecurityUtil>` 목킹 패턴 적용 여부 확인
- 추가 필요한 테스트 케이스 도출

## Phase 2: 수정

1. 원인 확인된 코드 수정
2. 동일 패턴의 다른 도메인 버그도 함께 수정
3. **Regression 테스트 필수**: 버그를 재현하는 테스트 케이스 추가
   - 테스트명 한국어, 버그 수정 전 실패 / 수정 후 통과

## Phase 3: 검증

Skill 도구로 `validate-before-commit` 실행

## Phase 4: 커밋

검증 통과 시 Skill 도구로 `timo-git-committer` 실행 (`fix:` prefix)

## 결과 리포트

```
### 원인
[근본 원인 1줄]

### 수정 내용
- [파일명:라인]: [변경 사항]

### 추가된 테스트
- [테스트 클래스명#메서드명]

### 상태: ✅ 완료 / ⚠️ 수동 확인 필요
```
