# 11장. 31가지 맛
### 목표: 컬럼을 특정 값으로 제한하기
컬럼의 값을 고정된 집합의 값으로 제한하는 것은 매우 유용하다. 해당 컬럼이 유효하지 않은 항목을 절대로 포함하지 않는다고 보장할 수 있으면, 컬럼을 사용하는 것이 단순해진다.

```sql
INSERT INTO Bugs (status) VALUES ('NEW'); -- OK
INSERT INTO Bugs (status) VALUES ('BANANA'); -- 에러
```

유효하지 않은 값은 데이터베이스가 거부하는 것이 이상적이다.

### 안티패턴: 컬럼 정의에 값 지정
- 많은 사람들이 컬럼을 정의할 때 유효한 데이터 값을 지정한다. 컬럼 정의는 메타데이터, 즉 테이블 구조 정의의 일부다.

```sql
CREATE TABLE Bugs (
  ...
  status VARCHAR(20) CHECK (status IN ('NEW', 'IN PROGRESS', 'FIXED'))
);

CREATE TABLE Bugs (
  ...
  status ENUM('NEW', 'IN PROGRESS', 'FIXED'),
);
```

- 다른 방법은 도메인이나 사용자 정의 타입을 사용하는 것이다. 이를 이용해 컬럼에 미리 지정한 값만 허용하도록 제한하고, 동일한 도메인이나 데이터 타입을 데이터베이스 내 여러 컬럼에 적용할 수 있다. 그러나 이는 많은 RDBMS 제품에서 지원되지 않는 경우가 있다.
- 미리 허용된 값을 확인하고 다른 값이 들어오면 에러를 발생시키는 트리거를 사용할 수 있다.

#### 중간에 있는게 뭐지?
`status`에 허용된 값의 완전한 목록을 얻으려면, 컬럼의 메타데이터를 쿼리해야 한다.

```sql
SELECT column_type
FROM information_schema.columns
WHERE table_schema = 'bugtracker_schema'
  AND table_name = 'bugs'
  AND column_name = 'status';
```

결과적으로 쿼리가 점점 더 복잡해지고, 애플리케이션 데이터와 데이터베이스 메타데이터가 서로 맞지 않게 되면 문제가 발생할 것이다.

#### 새로운 맛 추가하기
가장 흔한 변경은 허용된 값을 추가하거나 삭제하는 것이다. `ENUM`이나 체크 제약조건에 값을 추가하거나 삭제하는 문법은 없다.

```sql
ALTER TABLE Bugs MODIFY COLUMN status
  ENUM('NEW', 'IN PROGRESS', 'FIXED', 'DUPLICATE');
```

어떤 DB는 테이블이 비어있지 않으면 컬럼 정의를 변경할 수 없어 테이블 내용을 모두 덤프한 다음, 테이블을 재정의하고, 저장했던 데이터를 다시 넣어줘야 한다. 이 작업을 하는 동안 테이블에 접근할 수 없게 된다.

#### 예전 맛은 절대 없어지지 않는다
값을 더 이상 사용되지 않게 만들면, 과거 데이터가 망가질 수 있다.  
없어질 값이라도 과거 행이 참조하는 한 그대로 유지해야 할 수도 있다.
- 더 이상 사용되지 않는 값을 어떻게 식별할 수 있을까?
- 어떻게 사용자 인터페이스에서 제외해 더 이상 사용되지 않는 값을 입력하지 못하게 할 수 있을까?

#### 포팅이 어렵다
`ENUM` 데이터 타입은 MySQL의 고유 기능이다. 모든 SQL 데이터베이스 제품에서 호환성을 유지하기엔 어렵다.

### 안티패턴 인식 방법
- "데이터베이스를 내려야 애플리케이션 메뉴의 선택항목을 추가할 수 있어. 길어야 30분이면 충분할 거야. 모든게 잘 되면 말이지."
- "status 컬럼은 다음 값 중 하나만 가질 수 있어. 이 목록을 바꿀 일이 생기면 안 돼."
- "애플리케이션 코드에 있는 목록 값이 데이터베이스에 있는 비즈니스 규칙과 또 틀어졌어."

### 안티패턴 사용이 합당한 경우


### 해법: 데이터로 값을 지정하기
`Bugs.status` 컬럼에 들어갈 수 있는 각 값을 행으로 하는 색인 테이블을 만들고, `Bugs.status`가 새로 만든 테이블을 참조하도록 FK 제약조건을 선언한다.

```sql
CREATE TABLE BugStatus (
  status VARCHAR(20) PRIMARY KEY
);

INSERT INTO BugStatus (status)
  VALUES ('NEW'), ('IN PROGRESS'), ('FIXED');

CREATE TABLE Bugs (
  ...
  status VARCHAR(20),
  FOREIGN KEY (status) REFERENCES BugStatus(status)
    ON UPDATE CASCADE
);
```

#### 값의 집합 쿼리하기
```sql
SELECT status FROM BugStatus ORDER BY status;
```

#### 색인 테이블의 값 갱신하기
- 평범한 `INSERT` 문으로 값을 추가할 수 있다.
- 테이블에 대한 접근을 제한하지 않고도 변경이 가능하다.
- 컬럼을 재정의할 필요도, 다운타임 일정을 세울 필요도, ETL 작업을 수행할 필요도 없다.
- 색인 테이블에 값을 추가하거나 삭제하기 위해 현재의 값을 알아야 할 필요도 없다.

#### 더 이상 사용하지 않는 값 지원하기
```sql
ALTER TABLE BugStatus ADD COLUMN active
  ENUM('INACTIVE', 'ACTIVE') NOT NULL DEFAULT 'ACTIVE';

UPDATE BugStatus SET active = 'INACTIVE' WHERE status = 'DUPLICATE';
```

고정된 값의 집합에 대한 유효성 확인을 할 때는 메타데이터를 사용하라. 유동적인 값의 집합에 대한 유효성 확인을 할 때는 데이터를 사용하라.