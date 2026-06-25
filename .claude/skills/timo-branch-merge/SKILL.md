---
name: timo-branch-merge
description: 현재 변경사항을 feature 브랜치(worktree)에 커밋·푸시하고 main에 --no-ff 머지까지 수행
---

# Timo Branch Merge

변경사항을 feature 브랜치에 커밋·푸시한 뒤 main에 no-ff 머지한다.

## 핵심 제약사항

- `git worktree`로 브랜치 생성 — `git checkout -b` 금지
- `git add -A` / `git add .` 금지 — 관련 파일만 명시적으로 스테이징
- Co-Authored-By 추가 절대 금지
- `--no-verify` 사용 금지
- force push 금지
- `.env`, 시크릿, 자격증명 파일 감지 시 제외 후 사용자 경고
- 머지 후 worktree 반드시 정리 (`git worktree remove`)

## 브랜치 네이밍

```
feat/<kebab-case-설명>
fix/<kebab-case-설명>
refactor/<kebab-case-설명>
chore/<kebab-case-설명>
```

## 워크플로우

1. `git status` + `git diff`로 변경사항 분석
2. 변경 내용 기반으로 브랜치명 결정
3. `git worktree add ../<repo>-<branch> -b <branch>`로 worktree 생성
4. 변경 파일을 worktree 경로로 `cp`
5. worktree에서 `timo-git-committer` 스킬로 커밋 수행
   - 작업 단위가 크면 논리적 단위로 분할 커밋
6. `git push -u origin <branch>`
7. main 워킹 디렉토리로 돌아와 untracked/modified 파일 처리
   - untracked 신규 파일 → 제거 (worktree에 이미 커밋됨)
   - modified 파일 → `git stash`
8. `git merge --no-ff <branch> -m "merge: <작업 요약>"`
9. `git stash pop` (stash 했을 경우)
10. `git push origin main`
11. `git worktree remove ../<repo>-<branch>`

## 주의사항

- untracked 파일 제거 전 반드시 worktree에 해당 파일이 커밋되어 있는지 확인
- stash pop 후 충돌 발생 시 사용자에게 알리고 중단
- main push 전 `git log --oneline -3`으로 머지 커밋 확인
