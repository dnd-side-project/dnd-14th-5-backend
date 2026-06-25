---
name: timo-git-committer
description: Timo git diff 분석 후 팀 컨벤션 한국어 커밋 메시지 생성 및 커밋. Co-Authored-By 절대 금지.
---

# Timo Git Committer

Git 변경점을 분석하여 한국어 커밋 메시지를 생성하고 커밋을 수행한다.

## 핵심 제약사항

- rebase, reset, push, merge 등 파괴적 명령 실행 금지
- `git add` 시 현재 커밋 작업에 맞는 파일만 선택 — `git add -A` / `git add .` 금지
- 작업 단위가 큰 경우 반드시 분할 커밋 수행
- Co-Authored-By 추가 절대 금지
- `--no-verify` 사용 금지
- `.env`, 시크릿, 자격증명 파일 감지 시 해당 파일 제외하고 사용자 경고

## 커밋 메시지 구조

```
type: subject

body
```

## Type 종류

| Type | 설명 |
|------|------|
| `feat` | 신규 기능 |
| `fix` | 버그 수정 |
| `docs` | 문서 작업 |
| `style` | 코드 스타일 정리 |
| `refactor` | 리팩토링 |
| `test` | 테스트 코드 |
| `chore` | 빌드, 설정, 의존성 등 기타 작업 |
| `revert` | 이전 커밋 되돌리기 |
| `merge` | 브랜치 병합 |

## Subject 규칙

- 한국어, 50자 이내
- 간결한 개조식 표현
- 마침표/특수문자 금지
- 영어 고유명사(클래스명, 기술명) 그대로 사용 가능

## Body 규칙

- 80자 기준 줄바꿈
- 무엇을/왜 변경했는지 설명
- 파일별 변경사항 bullet 사용 가능

## 워크플로우

1. `git status`로 변경사항 확인
2. `git diff`로 변경 내용 분석
3. 관련 파일들을 논리적 단위로 그룹화
4. 각 그룹별로:
   - `git add <files>` (해당 작업 관련 파일만)
   - 커밋 메시지 작성
   - `git commit -m "..."` 수행
5. 분할 커밋 필요시 반복
6. 커밋할 내용 없으면 보고 후 종료
