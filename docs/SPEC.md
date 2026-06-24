# Timo SPEC (Technical Specification)

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.2 |
| ORM | Spring Data JPA (Hibernate) |
| DB | MySQL 8.0 |
| Cache | Redis 7 |
| Search / Log | Elasticsearch 9.2 + Logstash 9.2 + Kibana 9.2 |
| Push | Firebase Admin SDK 9.4.2 (FCM) |
| AI | Google Gemini API |
| Alert | Discord Webhook |
| Auth | Spring Security + OAuth2 Client + JWT (jjwt 0.13.0) |
| API Docs | SpringDoc OpenAPI 3 (`/swagger-ui.html`) |
| Build | Gradle 8, Docker |

---

## 패키지 구조

```
com.dnd5.timoapi
├── domain/{domain}/
│   ├── presentation/       # Controller, request/, response/
│   ├── application/
│   │   ├── service/        # @Service @Transactional
│   │   └── scheduler/      # @Scheduled
│   ├── domain/
│   │   ├── entity/         # JPA Entity (extends BaseEntity)
│   │   ├── model/          # Java record (불변 도메인 모델)
│   │   └── repository/     # JpaRepository
│   ├── exception/          # *ErrorCode implements ErrorCode
│   └── infrastructure/     # 외부 연동 구현체
└── global/
    ├── common/entity/      # BaseEntity (id, createdAt, updatedAt, deletedAt)
    ├── exception/          # ErrorCode, BusinessException, GlobalExceptionHandler
    ├── security/           # SecurityConfig, JwtTokenProvider, SecurityUtil
    ├── infrastructure/     # GeminiClient, FcmSender, DiscordNotifier, FileStorageService
    └── analytics/          # ApplicationEvent 기반 이벤트
```

---

## 인증 구조

- OAuth2 소셜 로그인 → `OAuth2SuccessHandler` → JWT 발급 → HTTP-only 쿠키 저장
- Access token: 쿠키 `access_token`
- Refresh token: 쿠키 `refresh_token` + Redis(`userId → token`)
- 요청마다 `JwtAuthenticationFilter`에서 쿠키 검증
- 현재 유저 추출: `SecurityUtil.getCurrentUserId()` (서비스 레이어 전용)
- 세션 정책: STATELESS

---

## API 엔드포인트

### Auth

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/test-auth/login` | 테스트 로그인 (`{ email }`) | Public |
| POST | `/auth/reissue` | refresh token으로 재발급 | Public |
| POST | `/auth/logout` | 로그아웃 (쿠키 + Redis 삭제) | 인증 |
| GET | `/auth/login/{provider}` | OAuth2 로그인 시작 | Public |
| GET | `/auth/callback/{provider}` | OAuth2 콜백 | Public |

provider: `google`, `naver`, `kakao`

### Users

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/users/me` | 내 프로필 조회 | 인증 |
| PATCH | `/users/me` | 닉네임 수정 (max 20자) | 인증 |
| DELETE | `/users/me` | 회원 탈퇴 (소프트딜리트 + 연관 데이터 cascade) | 인증 |

### Groups

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/groups` | 그룹 생성 (`{ name, type, image? }`) | 인증 |
| GET | `/groups` | 내 그룹 목록 (`?code=` 선택) | 인증 |
| GET | `/groups/{groupId}` | 그룹 상세 (`?code=` 선택) | 인증 |
| PATCH | `/groups/{groupId}` | 그룹 수정 — OWNER 전용 | 인증 |
| DELETE | `/groups/{groupId}` | 그룹 삭제 — OWNER 전용 | 인증 |
| POST | `/groups/members` | 그룹 참여 (`?type=FRIEND&code=` or `?type=CHARACTER`) | 인증 |
| DELETE | `/groups/{groupId}/members` | 그룹 탈퇴 | 인증 |
| GET | `/groups/{groupId}/reflections/today` | 오늘의 그룹 회고 (`?sort=LATEST\|STREAK\|TOTAL`) | 인증 |
| POST | `/admin/groups` | 그룹 생성 (어드민) | ADMIN |
| POST | `/admin/groups/seed` | CHARACTER 그룹 시드 | ADMIN |
| DELETE | `/admin/groups/{groupId}` | 그룹 강제 삭제 | ADMIN |

### Reflections

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/reflections` | 회고 작성 (`content` max 10,000자) | 인증 |
| GET | `/reflections/today/question` | 오늘의 질문 조회 | 인증 |
| POST | `/reflections/today/question/change` | 오늘의 질문 변경 (하루 5회) | 인증 |
| GET | `/reflections/today` | 오늘 작성한 회고 조회 | 인증 |
| GET | `/reflections/me` | 내 회고 목록 (`?month=YYYY-MM`) | 인증 |
| GET | `/reflections/{reflectionId}` | 회고 상세 | 인증 |
| DELETE | `/admin/reflections/{reflectionId}` | 회고 삭제 (어드민) | ADMIN |

### Reflection Feedback

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/reflections/{reflectionId}/feedback` | AI 피드백 요청 — 202 Accepted | 인증 |
| GET | `/reflections/{reflectionId}/feedback` | 피드백 조회 | 인증 |
| DELETE | `/admin/reflection-feedbacks/{feedbackId}` | 피드백 삭제 | ADMIN |

### Questions

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/questions/{questionId}` | 질문 상세 | Public |
| POST | `/admin/questions` | 질문 등록 (`{ sequence, category, content, createdBy }`) | ADMIN |
| GET | `/admin/questions` | 질문 목록 (`?keyword=&category=&page=&size=`) | ADMIN |
| PATCH | `/admin/questions/{questionId}` | 질문 수정 | ADMIN |
| DELETE | `/admin/questions/{questionId}` | 질문 삭제 | ADMIN |

### Tests (ZTPI)

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/tests` | 검사 목록 | 인증 |
| GET | `/tests/{testId}` | 검사 상세 | Public |
| GET | `/tests/type/{testType}` | 타입별 검사 조회 | Public |
| GET | `/tests/{testId}/questions` | 검사 문항 목록 | 인증 |
| GET | `/tests/{testId}/questions/{questionId}` | 문항 상세 | Public |
| POST | `/admin/tests` | 검사 등록 | ADMIN |
| PATCH | `/admin/tests/{testId}` | 검사 수정 | ADMIN |
| DELETE | `/admin/tests/{testId}` | 검사 삭제 | ADMIN |
| POST | `/admin/tests/{testId}/questions` | 문항 등록 (`{ category, content, sequence, isReversed }`) | ADMIN |
| PATCH | `/admin/tests/{testId}/questions/{questionId}` | 문항 수정 | ADMIN |
| DELETE | `/admin/tests/{testId}/questions/{questionId}` | 문항 삭제 | ADMIN |

### Test Records

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/test-records` | 검사 시작 — idempotent (진행 중이면 기존 반환) | 인증 |
| PATCH | `/test-records/{testRecordId}/complete` | 검사 완료 처리 (카테고리 배정) | 인증 |
| GET | `/test-records/me` | 내 검사 이력 | 인증 |
| GET | `/test-records/{testRecordId}` | 검사 상세 | 인증 |
| DELETE | `/test-records/{testRecordId}` | 검사 삭제 | 인증 |
| POST | `/test-records/{testRecordId}/responses` | 답변 제출 (`score` 1–5) | 인증 |
| PATCH | `/test-records/{testRecordId}/responses/{responseId}` | 답변 수정 | 인증 |
| GET | `/test-records/{testRecordId}/responses` | 답변 목록 | 인증 |
| DELETE | `/test-records/{testRecordId}/responses/{responseId}` | 답변 삭제 | 인증 |

### Notifications

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/notifications/schedules` | 알림 설정 생성 (FCM 토큰 함께 등록) | 인증 |
| GET | `/notifications/schedules/me` | 내 알림 설정 조회 | 인증 |
| POST | `/notifications/schedules/test-send` | 테스트 알림 발송 | 인증 |
| PATCH | `/notifications/schedules/{scheduleId}` | 알림 시간 수정 | 인증 |
| DELETE | `/notifications/schedules/{scheduleId}` | 알림 설정 삭제 | 인증 |
| GET | `/notifications/histories/me` | 알림 이력 | 인증 |
| PATCH | `/notifications/histories/{historyId}` | 알림 읽음 처리 | 인증 |

### 기타

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/statistics` | 전체 통계 | Public |
| GET | `/statistics/{category}` | 카테고리별 통계 | Public |
| GET | `/introductions` | 서비스 소개 (`?version=int`) | Public |
| GET | `/introductions/{introductionId}` | 소개 상세 | Public |
| POST | `/service-feedbacks` | 서비스 피드백 제출 | 인증 |
| POST | `/images` | 이미지 업로드 (max 10MB, multipart) | 인증 |
| GET | `/time-perspective-categories` | 시간 관점 카테고리 목록 | Public |

---

## DB 스키마

모든 테이블은 `BaseEntity`를 상속: `id (BIGINT PK AUTO_INCREMENT)`, `created_at`, `updated_at`, `deleted_at`. 소프트딜리트 패턴 전체 적용.

### users
| 컬럼 | 타입 | 제약 |
|---|---|---|
| email | VARCHAR | NOT NULL UNIQUE |
| nickname | VARCHAR | NOT NULL |
| timezone | VARCHAR | NOT NULL DEFAULT 'Asia/Seoul' |
| provider | ENUM(GOOGLE, NAVER, KAKAO) | NOT NULL |
| role | ENUM(USER, ADMIN) | NOT NULL |
| category | ENUM(ZtpiCategory) | NULLABLE |
| is_onboarded | BOOLEAN | NOT NULL DEFAULT false |
| streak_days | INT | NOT NULL DEFAULT 0 |
| total_days | INT | NOT NULL DEFAULT 0 |

### user_groups
| 컬럼 | 타입 | 제약 |
|---|---|---|
| code | VARCHAR(8) | NOT NULL UNIQUE |
| name | VARCHAR | NOT NULL |
| type | ENUM(FRIEND, CHARACTER) | NOT NULL |
| image | TEXT | NULLABLE |
| category | ENUM(ZtpiCategory) | NULLABLE (CHARACTER 그룹용) |

### user_group_members
| 컬럼 | 타입 | 제약 |
|---|---|---|
| group_id | BIGINT | NOT NULL |
| user_id | BIGINT | NOT NULL |
| role | ENUM(OWNER, MEMBER) | NOT NULL |
| — | UNIQUE(group_id, user_id) | |

### reflections
| 컬럼 | 타입 | 제약 |
|---|---|---|
| user_id | BIGINT | NOT NULL |
| question_id | BIGINT | NOT NULL |
| date | DATE | NOT NULL |
| answer_text | TEXT | NOT NULL |
| — | UNIQUE(user_id, date) | |

### reflection_feedbacks
| 컬럼 | 타입 | 제약 |
|---|---|---|
| reflection_id | BIGINT | NOT NULL UNIQUE |
| score | INT | 0–5, NULLABLE |
| content | TEXT | NULLABLE |
| status | ENUM(PROCESSING, COMPLETED, FAILED) | NOT NULL |
| category | ENUM(ZtpiCategory) | NULLABLE |
| is_increased | BOOLEAN | NULLABLE |
| changed_score | DOUBLE | NULLABLE |
| before_score | DOUBLE | NULLABLE |
| after_score | DOUBLE | NULLABLE |

### reflection_questions
| 컬럼 | 타입 | 제약 |
|---|---|---|
| sequence | BIGINT | NOT NULL |
| category | ENUM(ZtpiCategory) | NOT NULL |
| content | TEXT | NOT NULL |
| created_by | VARCHAR | NOT NULL |
| — | UNIQUE(sequence, category) | |

### user_reflection_question_orders
| 컬럼 | 타입 | 제약 |
|---|---|---|
| user_id | BIGINT | NOT NULL |
| category | ENUM(ZtpiCategory) | NOT NULL |
| sequence | BIGINT | NOT NULL |
| — | UNIQUE(user_id, category) | |

### tests
| 컬럼 | 타입 | 제약 |
|---|---|---|
| type | ENUM | NOT NULL UNIQUE |
| name | VARCHAR | NOT NULL |
| description | TEXT | |

### test_questions
| 컬럼 | 타입 | 제약 |
|---|---|---|
| test_id | BIGINT | NOT NULL |
| category | ENUM(ZtpiCategory) | NOT NULL |
| content | TEXT | NOT NULL |
| sequence | INT | NOT NULL |
| is_reversed | BOOLEAN | NOT NULL |

### user_test_records
| 컬럼 | 타입 | 제약 |
|---|---|---|
| user_id | BIGINT | NOT NULL |
| test_id | BIGINT | NOT NULL |
| status | ENUM(IN_PROGRESS, COMPLETED) | NOT NULL |

### user_test_record_responses
| 컬럼 | 타입 | 제약 |
|---|---|---|
| test_record_id | BIGINT | NOT NULL |
| question_id | BIGINT | NOT NULL |
| answer_score | INT | NOT NULL (1–5) |
| — | UNIQUE(test_record_id, question_id) | |

### user_test_record_results
| 컬럼 | 타입 | 제약 |
|---|---|---|
| test_record_id | BIGINT | NOT NULL |
| category | ENUM(ZtpiCategory) | NOT NULL |
| score | DOUBLE | NOT NULL |

### users_reflection_alarm_settings
| 컬럼 | 타입 | 제약 |
|---|---|---|
| user_id | BIGINT | NOT NULL UNIQUE |
| alarm_time | TIME | NOT NULL |

### fcm_tokens
| 컬럼 | 타입 | 제약 |
|---|---|---|
| user_id | BIGINT | NOT NULL |
| token | VARCHAR(500) | NOT NULL |

### notification_histories
| 컬럼 | 타입 | 제약 |
|---|---|---|
| user_id | BIGINT | NOT NULL |
| title | VARCHAR | NOT NULL |
| body | VARCHAR | NOT NULL |
| is_read | BOOLEAN | NOT NULL DEFAULT false |

### analytics_events
| 컬럼 | 타입 | 제약 |
|---|---|---|
| user_id | BIGINT | NULLABLE |
| event_name | VARCHAR | NOT NULL |
| properties | TEXT (JSON) | |
| created_at | DATETIME | NOT NULL |

---

## 외부 연동

### Google Gemini API
- **엔드포인트**: `POST /models/{model}:generateContent?key={apiKey}`
- **요청**: 시스템 프롬프트(`reflection_feedback_prompts` 최신 버전) + 유저 메시지 `{category, question, response}`
- **응답**: `candidates[0].content.parts[0].text` → `{score: int, content: string}` JSON으로 파싱
- **설정**: `GEMINI_API_KEY` 환경변수, 모델명 `application.properties`

### Firebase Cloud Messaging
- **구현**: `FcmSender` — Firebase Admin SDK
- **단건**: `Message` / 다건: `MulticastMessage`
- **발송**: `@Async` 비동기
- **비활성화**: `FCM_ENABLED=false` 환경변수

### Discord Webhook
- **구현**: `DiscordNotifier` — `WebClient` POST, `@Async`
- **트리거**: `GlobalExceptionHandler`에서 미처리 예외 발생 시
- **비활성화**: `NOTIFICATION_ENABLED=false` 환경변수

### Redis 키 패턴

| Key | 값 | TTL |
|---|---|---|
| `refresh_token:{userId}` | refreshToken | 고정 TTL |
| `reflection:question:today:{userId}` | questionId | 자정까지 |
| `reflection:question:skip:count:{userId}` | 변경 횟수 (max 5) | 자정까지 |

---

## 스케줄러

### NotificationScheduler
- **Cron**: `0 * * * * *` (매분 실행)
- 현재 분(`HH:mm`)과 `alarm_time`이 일치하는 유저 조회
- FCM 발송 → `NotificationHistory` 저장
- 유저별 예외 독립 처리 (한 명 실패해도 계속 진행)

### ReflectionScheduler
- **Cron**: `0 0 0 * * *` (자정 실행)
1. 어제 회고 작성 유저의 `UserReflectionQuestionOrder.sequence` +1 (다음 질문으로 전진)
2. 다음 질문을 Redis에 미리 캐싱
3. 어제 회고 미작성 유저의 `streak_days` → 0 리셋

---

## Analytics 이벤트

Spring `ApplicationEventPublisher`로 발행, `@Async` 핸들러가 `analytics_events` 테이블에 저장.

| 이벤트 | event_name | properties |
|---|---|---|
| TestStartedEvent | `test_started` | testRecordId |
| TestCompletedEvent | `test_completed` | testRecordId, scores (카테고리별 점수 map) |
| ReflectionCreatedEvent | `reflection_created` | reflectionId, questionId, category, answerLength |
| FeedbackReceivedEvent | `feedback_received` | reflectionId, score |
| NotificationSentEvent | `notification_sent` | tokenCount |

---

## 보안

### SecurityConfig 경로 규칙

| 경로 | 정책 |
|---|---|
| `/error`, `/favicon.ico`, `/swagger-ui/**`, `/v3/api-docs/**` | Security 필터 제외 |
| `/auth/**` (logout 제외), `/test-auth/**` | `permitAll` |
| `/auth/logout` | `authenticated` |
| `/admin/**` | `hasRole("ADMIN")` |
| `/users/**`, `/reflections/**`, `/test-records/**`, `/notifications/**`, `/groups/**` | `authenticated` |
| `POST /images`, `GET /tests`, `GET /tests/*/questions` | `authenticated` |
| 그 외 (`/statistics`, `/questions`, `/introductions`, `/time-perspective-categories` 등) | `permitAll` |

### 서비스 레이어 소유자 검증 (필터 외부)

- 회고: `reflection.userId == currentUserId`
- 회고 피드백: 부모 회고의 `userId == currentUserId`
- 검사 기록: `userTestRecord.userId == currentUserId`
- 알림 설정: `alarmSetting.userId == currentUserId`
- 그룹 수정/삭제: `GroupMemberRole == OWNER`

---

## 에러 응답 형식

```json
{ "name": "ERROR_CODE_NAME", "message": "한국어 설명" }
```

미처리 예외 → HTTP 500 + Discord 알림 발송.
