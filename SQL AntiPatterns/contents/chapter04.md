# 4장. 아이디가 필요해
### 목표: PK 관례 확립
모든 테이블이 PK를 갖도록 하는 것이 목표지만, PK의 본질을 혼동하면 안티패턴을 초래할 수 있다.  
까다로운 부분은 PK로 사용할 칼럼을 선정하는 일이다. 대부분의 테이블에서 어느 속성의 값이든 하나 이상의 행에서 나타날 잠재적 가능성이 있다.

다음과 같은 것이 필요하다면 PK 제약 조건은 중요하다.

- 테이블에 중복 행이 저장되는 것을 방지
- 쿼리에서 각 행을 참조
- FK 참조 지원

PK 제약 조건을 사용하지 않으면, 중복 행을 확인해야 하는 잡일이 생기게 된다.

### 안티패턴: 만능키
DB 내 모든 테이블이 다음과 같은 특성을 갖는 PK 컬럼을 가지도록 하는 문화적 관례로 PK가 항상 아래와 같이 정의되는 컬럼이라고 생각하면 안된다.

- PK 컬럼 이름은 `id`
- PK 컬럼의 데이터 타입은 32비트 또는 64비트 정수
- 유일한 값은 자동 생성된다.

모든 테이블에 `id` 컬럼을 추가하는 것은, 그 사용을 이상하게 만드는 몇 가지 효과를 초래한다.

- 중복 키 생성

```sql
CREATE TABLE Bugs (
  id          SERIAL PRIMARY KEY,
  bug_id      VARCHAR(10) UNIQUE,
  description VARCHAR(1000),
  ...
);

INSERT INTO Bugs (bug_id, description, ...)
  VALUES ('VIS-078', 'crashes on save', ...);
```

사용 목적이 동일한데 unique 컬럼을 왜?

- 중복 행 허용

정션 테이블에서 아래와 같이 중복이 일어날 수 있다.

```sql
CREATE TABLE BugsProducts(
  id          SERIAL PRIMARY KEY,
  bug_id      BIGINT UNSIGNED NOT NULL,
  product_id  BIGINT UNSIGNED NOT NULL,
  FOREIGN KEY (bug_id) REFERENCES Bugs(bug_id),
  FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

INSERT INTO BugsProducts (bug_id, product_id)
  VALUES (1234, 1), (1234, 1), (1234, 1); -- 중복이 허용됨
```

- 모호한 키의 의미
  - PK 컬럼 이름이 동일한 두 테이블을 조인할 때 특히 문제가 된다. PK의 컬럼 이름이 테이블의 엔티티 타입에 대한 실마리를 줘야 한다.
- USING 사용

```sql
SELECT * FROM Bugs AS b
  JOIN BugsProducts AS bp ON (b.bug_id = bp.bug_id);

-- 위와 같은 쿼리
SELECT * FROM Bugs JOIN BugsProducts USING (bug_id);

-- 모든 테이블이 id란 이름으로 PK를 정의했다면,
SELECT * FROM Bugs AS b
  JOIN BugsProducts AS bp ON (b.id = bp.bug_id);
```

- 어려운 복합키

### 안티패턴 인식 방법
- 이 테이블엔 PK가 없어도 될 것 같은데
  - 이런 말이 나오면 PK와 가상키 용어의 의미를 혼동하는 것이다.
- 다대다 연결에서 왜 중복이 발생했지?
  - FK 컬럼을 묶어 PK 제약조건을 걸거나 최소한 unique 제약조건이라도 걸어둬야 함.
- 값은 색인 테이블로 옮기고 ID로 참조해야 한다지만 그렇게 하고 싶지 않다. 이는 원하는 실제 값을 얻기 위해 매번 조인을 해야 하기 때문이다.
  - DB 설계 이론에서 말하는 정규화에 대한 흔한 오해다. 정규화는 가상키와 아무런 상관이 없다.

### 안티패턴 사용이 합당한 경우
- 가상키는 지나치게 긴 자연키를 대체하기 위해 사용한다면 적절하다.

### 해법: 상황에 맞추기
PK는 제약조건이지 데이터 타입이 아니다. 데이터 타입이 인덱스를 지원하기만 하면, 어느 컬럼 또는 컬럼의 묶음에 대해서도 PK를 선언할 수 있다. 또한 테이블의 특정 컬럼을 PK로 잡지 않고도 자동 증가하는 정수값을 가지도록 정의할 수 있다. 이 두 개념은 서로 독립적인 것이다.

- PK에 의미 있는 이름을 선택해야 한다.
  - ex) `Bugs` 테이블 → `bug_id`
- FK에서도 가능하다면 같은 컬럼 이름을 사용해야 한다.
- 관례에서 벗어나기
- 자연키와 복합키 포용