---
name: timo-codegen
description: Timo 프로젝트 코드 생성. DDD 레이어 구조, Java 21 관용 패턴, Timo 네이밍 컨벤션 준수. 완료 후 timo-git-committer → timo-code-reviewer 자동 실행.
---

# Timo Code Generator

## Project Structure

```
src/main/java/com/dnd5/timoapi/
├── domain/{domain}/
│   ├── presentation/          # *Controller, request/, response/
│   ├── application/
│   │   ├── service/           # *Service
│   │   └── scheduler/         # *Scheduler (@Scheduled)
│   ├── domain/
│   │   ├── entity/            # *Entity extends BaseEntity
│   │   ├── model/             # Java record, enums/
│   │   └── repository/        # *Repository extends JpaRepository
│   ├── exception/             # *ErrorCode implements ErrorCode
│   └── infrastructure/        # 외부 연동 구현체
└── global/
    ├── common/entity/         # BaseEntity
    ├── exception/             # ErrorCode, BusinessException, GlobalExceptionHandler
    └── security/context/      # SecurityUtil
```

## Mandatory Patterns

### Controller
- `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`, `@Validated`
- `@ResponseStatus(HttpStatus.CREATED)` for POST, `@ResponseStatus(HttpStatus.NO_CONTENT)` for DELETE/PATCH
- `@Valid @RequestBody` for POST/PATCH, `@Positive` on path variable IDs
- 비즈니스 로직 없음 — Service에 위임만

### Service
- `@Service`, `@RequiredArgsConstructor`, `@Transactional` at class level
- `@Transactional(readOnly = true)` on read methods
- 현재 사용자: `SecurityUtil.getCurrentUserId()`
- 에러: `throw new BusinessException(XxxErrorCode.XXX_CASE)`
- 소유자 검증 후 뮤테이션

### Repository
- `extends JpaRepository<XxxEntity, Long>`
- 소프트 딜리트 조회: `AndDeletedAtIsNull` suffix 또는 `@Query`
- 복잡한 쿼리: `@Query` JPQL + `@Param`

### Entity
- `extends BaseEntity`, `@Entity`, `@Table`, `@Getter`
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@AllArgsConstructor`
- FK는 `Long` ID 필드만 (JPA 연관관계 매핑 지양)
- `static XxxEntity from(Xxx model)` 팩토리
- `Xxx toModel()` 변환 메서드

### Domain Model
- Java `record` (불변 값 객체)

### ErrorCode
- `@Getter @RequiredArgsConstructor enum XxxErrorCode implements ErrorCode`
- 한국어 에러 메시지

### Request/Response DTO
- `record` 또는 Bean Validation 있으면 `class` + Lombok
- Response: `static XxxResponse from(Xxx model)` 팩토리

## Rules

- 주석 절대 금지
- FK: 객체 참조 아닌 `Long` ID 필드
- `SecurityUtil.getCurrentUserId()` 이외 경로로 userId 수신 금지
- 미사용 import 금지

## Task Execution

1. 사용자 요청 파악
2. 수정/생성할 파일과 레이어 경계 파악
3. 의존성 순서로 파일 생성:
   ErrorCode → record Model → Entity → Repository → Service → Controller
4. 레이어 간 import 위반 없는지 확인
5. 주석이 없는지 확인

## After Completion

1. Skill 도구로 `timo-git-committer` 에이전트 실행
2. 커밋 완료 후 Skill 도구로 `timo-code-reviewer` 스킬 실행
