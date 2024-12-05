# 7장. 다형성 연관
### 목표: 여러 부모 참조
```sql
...
  FOREIGN KEY (issue_id)
    REFERENCES Bugs(issue_id) OR FeatureRequests(issue_id)
);
```

위와 같은 식으로 여러 개의 부모테이블을 참조하는 FK는 만들 수 없다.

- 특정 댓글은 하나의 버그 또는 하나의 기능요청, 둘 중 하나만 참조해야 한다.

### 안티패턴: 이중 목적의 FK 사용
이런 경우에 대한 해법은 **다형성 연관**이란 이름이 붙을 정도로 널리 알려져 있다.

```sql
CREATE TABLE Comments (
  comment_id SERIAL PRIMARY KEY,
  issue_type VARCHAR(20), -- Bugs 또는 FeatureRequests
  issue_id   BIGINT UNSIGNED NOT NULL,
  author     BIGINT UNSIGNED NOT NULL,
  comment_date DATETIME,
  comment    TEXT,
  FOREIGN KEY (author) REFERENCES Accounts(account_id)
);
```

- FK는 하나의 테이블만 참조할 수 있기 때문에 다형성 연관을 사용할 경우엔 이 연관을 메타데이터에 선언할 수 없다.
- `Comments.issue_id`의 값이 부모테이블에 있는 값과 대응되도록 강제할 수 없고, 데이터 정합성도 보장할 수 없다.
- `Comments.issue_type`에 있는 문자열이 DB에 있는 테이블에 대응되는지 확인하는 메타데이터도 없다.

#### 다형성 연관에서의 조회
- 자식 테이블을 부모 테이블과 조인할 때 `issue_type`을 정확하게 사용하는 것이 중요하다.
  - 둘 중 하나만 참조해야 한다.

```sql
SELECT *
FROM Bugs AS b JOIN Comments AS c
  ON (b.issue_id = c.issue_id AND c.issue_type = 'Bugs')
WHERE b.issue_id = 1234;
```

하지만, 테이블을 바꿔가며 조인하기는 불가능하다. 그렇다고 외부조인을 사용하하는 것은 결과 집합에서 각 행에 매칭되지 않는 부모로부터 온 필드는 `NULL`이 된다.

### 안티패턴 인식 방법
- "이 태깅 스키마는 데이터베이스 내의 어떤 리소스에도 태그 또는 다른 속성을 달 수 있다."
- "우리 데이터베이스 설계에선 FK를 선언할 수 없어"
- "`entity_type` 컬럼의 용도가 뭐지?"

### 안티패턴 사용이 합당한 경우
다형성 연관 안티패턴은 사용을 피하고, FK와 같은 제약조건을 사용해 참조 정합성을 보장해야 한다. 다형성 연관은 메타데이터 대신 애플리케이션 코드에 지나치게 의존하게 만드는 경우가 많다.

Hibernate와 같은 ORM 프레임워크를 사용하는 경우 이 안티패턴 사용이 불가피할 수 있다.

### 해법: 관계 단순화
다형성 연관의 단점을 피하면서 필요한 데이터 모델을 지원하기 위해선 DB를 다시 설계하는게 낫다.

- 역 참조
  - 교차 테이블 생성
  - 신호등 설치
  - 양쪽 다 보기
  - 차선 통합
- 공통 슈퍼테이블 생성

모든 테이블 관계엔 참조하는 테이블 하나, 참조되는 테이블 하나가 있다.