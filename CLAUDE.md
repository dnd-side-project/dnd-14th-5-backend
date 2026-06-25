# Timo 프로젝트 컨텍스트

## 프로젝트 개요

Timo — 캐릭터 기반 회고 앱
- Java 21 + Spring Boot 4.0.2
- DDD 아키텍처 (presentation / application / domain / infrastructure / exception)
- JPA (Hibernate) + MySQL(주) + Redis(캐시) + Elasticsearch(검색)
- 외부 연동: Gemini AI, Firebase FCM, Discord(에러 알림)

## 패키지 구조

```
com.dnd5.timoapi
├── domain/
│   └── {domain}/
│       ├── application/
│       │   ├── service/        # @Service @Transactional
│       │   └── scheduler/      # @Scheduled 배치
│       ├── domain/
│       │   ├── entity/         # *Entity extends BaseEntity
│       │   ├── model/          # Java record (불변 도메인 모델)
│       │   │   └── enums/
│       │   └── repository/     # *Repository extends JpaRepository
│       ├── exception/          # *ErrorCode implements ErrorCode
│       ├── infrastructure/     # 외부 연동 구현체
│       └── presentation/
│           ├── request/        # *Request record/class
│           ├── response/       # *Response record/class
│           └── *Controller.java
└── global/
    ├── analytics/              # Spring ApplicationEvent 기반 분석
    ├── common/
    │   ├── entity/             # BaseEntity (id, createdAt, updatedAt, deletedAt)
    │   └── response/           # PageResponse<T>
    ├── exception/              # ErrorCode, BusinessException, GlobalExceptionHandler, ErrorResponse
    ├── infrastructure/
    │   ├── fcm/                # FcmSender, FcmConfig
    │   ├── file/               # FileStorageService
    │   ├── gemini/             # GeminiClient, GeminiConfig
    │   ├── notification/       # Notifier (Discord)
    │   └── redis/              # RedisConfig
    ├── log/                    # ApiLoggingFilter
    └── security/
        ├── config/             # SecurityConfig
        ├── context/            # SecurityUtil (getCurrentUserId)
        ├── jwt/                # JwtTokenProvider, JwtAuthenticationFilter
        └── exception/          # SecurityErrorCode
```

## 도메인 맵

| 도메인 | 역할 |
|--------|------|
| auth | OAuth2 로그인, JWT 발급 (Google/Naver/Kakao) |
| user | 회원 관리, ZTPI 성격 검사 |
| group | 캐릭터 그룹, 멤버십 관리 |
| reflection | 오늘의 회고, 피드백 (Gemini AI) |
| introduction | 자기소개 |
| notification | FCM 푸시, 알림 이력 |
| test | ZTPI 성격 검사 문항 |

## 핵심 코드 패턴

### Entity

```java
@Entity
@Table(name = "xxx")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class XxxEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public static XxxEntity from(Xxx model) {
        return new XxxEntity(model.userId());
    }

    public Xxx toModel() {
        return new Xxx(getId(), userId(), getCreatedAt(), getDeletedAt());
    }

    public void softDelete() {
        super.softDelete();
    }
}
```

### Domain Model (Java record)

```java
public record Xxx(
    Long id,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime deletedAt
) {}
```

### Repository

```java
public interface XxxRepository extends JpaRepository<XxxEntity, Long> {

    Optional<XxxEntity> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT x FROM XxxEntity x WHERE x.userId = :userId AND x.deletedAt IS NULL ORDER BY x.createdAt DESC")
    List<XxxEntity> findAllByUserId(@Param("userId") Long userId);
}
```

### Service

```java
@Service
@RequiredArgsConstructor
@Transactional
public class XxxService {

    private final XxxRepository xxxRepository;

    @Transactional(readOnly = true)
    public Xxx findById(Long id) {
        return xxxRepository.findByIdAndDeletedAtIsNull(id)
            .map(XxxEntity::toModel)
            .orElseThrow(() -> new BusinessException(XxxErrorCode.XXX_NOT_FOUND));
    }

    public Long create(XxxRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        XxxEntity entity = XxxEntity.from(request.toModel(userId));
        return xxxRepository.save(entity).getId();
    }
}
```

### Controller

```java
@RestController
@RequestMapping("/xxx")
@RequiredArgsConstructor
@Validated
public class XxxController {

    private final XxxService xxxService;

    @GetMapping("/{id}")
    public XxxResponse getXxx(@PathVariable @Positive Long id) {
        return XxxResponse.from(xxxService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createXxx(@RequestBody @Valid XxxRequest request) {
        xxxService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteXxx(@PathVariable @Positive Long id) {
        xxxService.delete(id);
    }
}
```

### ErrorCode

```java
@Getter
@RequiredArgsConstructor
public enum XxxErrorCode implements ErrorCode {
    XXX_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 xxx를 찾을 수 없습니다."),
    XXX_NOT_OWNER(HttpStatus.FORBIDDEN, "xxx 소유권이 없습니다. (xxxId: %s, userId: %s)");

    private final HttpStatus status;
    private final String message;
}

// 사용
throw new BusinessException(XxxErrorCode.XXX_NOT_FOUND);
throw new BusinessException(XxxErrorCode.XXX_NOT_OWNER, id, userId);

// 응답: { "name": "XXX_NOT_FOUND", "message": "해당 xxx를 찾을 수 없습니다." }
```

### 현재 유저 추출

```java
Long userId = SecurityUtil.getCurrentUserId();
```

### 소프트 딜리트

```java
// 조회 시 필터 필수
xxxRepository.findByIdAndDeletedAtIsNull(id)

// 삭제
entity.softDelete();
```

## API 응답 표준

| 상황 | HTTP Status | Body |
|------|-------------|------|
| 생성 | 201 Created | 없음 또는 생성된 ID |
| 조회 | 200 OK | 데이터 |
| 수정/삭제 | 204 No Content | 없음 |
| 비즈니스 오류 | 4xx | `{ name, message }` |
| 인증 오류 | 401 | `{ name, message }` |
| 권한 오류 | 403 | `{ name, message }` |

## 보안 규칙

불변 조건:
- 현재 사용자: `SecurityUtil.getCurrentUserId()` (요청 파라미터로 userId 수신 금지)
- 리소스 뮤테이션 전 소유자 검증 필수
- ADMIN 전용 엔드포인트 → `SecurityConfig`에 `.hasRole("ADMIN")` 등록 필수
- JWT 시크릿: `JwtProperties`에서만 참조 (하드코딩 금지)
- PII 로깅 없음

## 테스트 패턴

```java
@ExtendWith(MockitoExtension.class)
class XxxServiceTest {

    @InjectMocks private XxxService xxxService;
    @Mock private XxxRepository xxxRepository;

    @Test
    void findById_존재하지_않으면_404_예외() {
        when(xxxRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> xxxService.findById(1L))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                .isEqualTo(XxxErrorCode.XXX_NOT_FOUND));
    }
}

// SecurityUtil 목킹 (정적 메서드)
try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
    mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
    // ...
}
```

테스트 파일 네이밍: `XxxServiceTest.java`, 메서드명은 한국어 (`findById_다른_유저면_403_예외`)

## 프로젝트 규칙

- 커밋 메시지는 한글로 작성

## 캐릭터 그룹 참여 로직

- 캐릭터 그룹은 모든 유저에게 노출 (`getMyGroups` OK)
- `joinGroup` 이후부터 내 회고가 그룹에 등록(노출)됨
- `leaveGroup` 시 멤버십 제거 → 그룹 뷰에서 자동 숨김 (회고 데이터 삭제 없음)
- `getTodayReflectionsForCharacterGroup`은 groupMember인 유저의 회고만 반환 (구현 완료)

## 커맨드 워크플로우

| 상황 | 커맨드 |
|------|--------|
| 새 기능 구현 | `/timo-codegen` |
| 버그 수정 | `/timo-fix` |
| 리팩토링 | `/timo-refactor` |
| DB 갭 감지 | `/timo-db-sync` |
| 보안 감사 | `/timo-pentest` |
| 코드 리뷰 | `/timo-code-reviewer` |
| 커밋 전 검증 | `/validate-before-commit` |
| PR 전 검증 | `/validate-before-pr` |
| 커밋 | `/timo-git-committer` |
| 브랜치 커밋·푸시·머지 | `/timo-branch-merge` |
