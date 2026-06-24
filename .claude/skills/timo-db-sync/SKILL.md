---
name: timo-db-sync
description: JPA Entity 스캔으로 도메인 모델과 DB 스키마 갭 감지 및 동기화 제안
---

# Timo DB 동기화

대상 도메인: $ARGUMENTS (없으면 전체)

## Step 1: Entity 코드 스캔

`src/main/java/com/dnd5/timoapi/domain/` 하위 `*Entity.java` 파일 탐색.
각 Entity의 `@Column`, `@Table`, 필드 정의 수집.

## Step 2: BaseEntity 확인

`global/common/entity/BaseEntity.java` → 공통 컬럼(`id`, `createdAt`, `updatedAt`, `deletedAt`) 파악.

## Step 3: 갭 분석

- `@Column(nullable = false)`인데 DB default 없는 컬럼 → NPE 위험
- `@Column(unique = true)` 선언 여부 vs 실제 유니크 제약 필요성
- FK 필드(`Long xxxId`)에 대응하는 다른 Entity 존재 여부
- `deletedAt` 없는 Entity에서 소프트 딜리트 패턴 사용 여부

## Step 4: 결과

**갭 없음**: `✅ Entity 정의 일관성 확인 완료`

**갭 있음**: 수정할 Entity 코드 및 조치 제안
- `ddl-auto: update` 환경 → 재시작 후 자동 반영
- 컬럼 삭제/이름 변경은 직접 DDL 제공

**신규 Entity 생성 요청 시**:

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
        return new Xxx(getId(), userId, getCreatedAt(), getDeletedAt());
    }
}
```

BaseEntity 상속 → `id`, `createdAt`, `updatedAt`, `deletedAt` 자동 포함.
