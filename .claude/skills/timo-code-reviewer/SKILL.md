---
name: timo-code-reviewer
description: Timo 변경 코드 리뷰. DDD 레이어 준수, Java 관용 패턴, 보안(JWT/소유자 검증), 네이밍, 주석 금지 기준 검토.
---

# Timo Code Reviewer

## Review Scope

`git diff HEAD~1` (또는 관련 범위)로 변경된 코드만 리뷰.

## Checklist

### DDD Layer Compliance
- [ ] Controller가 domain/infrastructure에서 Service·DTO 외 import 없음
- [ ] Service가 Controller 레이어 클래스 import 없음
- [ ] Domain model(record)에 Spring 어노테이션 없음
- [ ] 외부 서비스(Gemini, FCM)를 Controller에서 직접 호출 없음
- [ ] 크로스 도메인 접근 시 타 도메인 Service 경유

### Java Idioms
- [ ] DTO·도메인 모델은 `record` (Bean Validation 필요시 class 허용)
- [ ] `Optional` 체이닝 사용 (`ifPresent`, `map`, `orElseThrow`)
- [ ] Lombok 어노테이션 최소 필요한 것만 사용
- [ ] Response에 `static XxxResponse from(Xxx model)` 팩토리

### Security
- [ ] 현재 사용자: `SecurityUtil.getCurrentUserId()` (요청 파라미터 수신 금지)
- [ ] 리소스 뮤테이션 전 소유자 검증
- [ ] JWT 시크릿: `JwtProperties`에서만 참조 (하드코딩 금지)
- [ ] FCM 토큰을 응답에 노출하지 않음
- [ ] PII 로깅 없음

### Naming Conventions
- [ ] `*Controller`, `*Service`, `*Repository`, `*Entity`, `*ErrorCode`, `*Scheduler`
- [ ] Request DTO: `*Request`, Response DTO: `*Response`
- [ ] 소프트 딜리트 조회: `AndDeletedAtIsNull` suffix

### Code Quality
- [ ] 주석 없음 (인라인, 블록, Javadoc 모두)
- [ ] 미사용 import 없음
- [ ] 읽기 전용 서비스 메서드: `@Transactional(readOnly = true)`
- [ ] `@ResponseStatus` 비-200 응답에 선언
- [ ] 모든 도메인 에러: `BusinessException(ErrorCode)` 사용
- [ ] Entity FK: `Long` ID 필드만

### Performance
- [ ] N+1 없음: 루프 안 단건 조회 → `findAllByIdIn()` 배치 조회로 교체
- [ ] Gemini AI 호출이 동기 흐름에서 응답 블로킹 없음

### Error Handling
- [ ] `*ErrorCode`가 `ErrorCode` 인터페이스 구현
- [ ] Controller에 ad-hoc try/catch 없음

## Output Format

```
[CATEGORY] Severity: HIGH/MEDIUM/LOW
File: path/to/File.java (line N)
Issue: <문제>
Fix: <수정 방법>
```

```
총 N개 이슈 발견: HIGH N, MEDIUM N, LOW N
```

이슈 없으면: `리뷰 완료 — 이슈 없음`
