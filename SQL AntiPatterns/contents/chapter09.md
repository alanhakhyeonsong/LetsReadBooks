# 9장. 메타데이터 트리블
### 목표: 확장 적응성 지원
- 쿼리 성능을 향상시키고 지속적으로 크기가 늘어나는 테이블을 지원하도록 DB를 구성하는 것이 목표다.

### 안티패턴: 테이블 또는 칼럼 복제
다른 모든 조건이 동일하다면, 행이 적은 테이블을 조회하는 것이 행이 많은 테이블을 조회하는 것보다 빠르다. 이는 우리가 어떤 작업을 해야 하든 모든 테이블이 보다 적은 행을 포함하도록 만들어야 한다는 그릇된 생각을 하게 만든다. 그리고 두 가지 형태의 안티패턴으로 나타난다.

- 많은 행을 가진 큰 테이블을 여러 개의 작은 테이블로 분리한다. 작은 테이블의 이름은 테이블의 속성 중 하나의 값을 기준으로 해서 짓는다.
- 하나의 컬럼을 여러 개의 컬럼으로 분리한다. 컬럼 이름은 다른 속성의 값을 기준으로 해서 짓는다.

#### 테이블이 우글우글
```sql
CREATE TABLE Bugs_2008 ( ... );
CREATE TABLE Bugs_2009 ( ... );
CREATE TABLE Bugs_2010 ( ... );
```

DB에 행을 삽입할 때 삽입하는 값에 따라 올바른 테이블을 사용하는 것은 사용자 책임이다.

#### 데이터 정합성 관리
테이블 이름에 따라 데이터를 자동으로 제한하는 방법은 없지만, 각 테이블에 CHECK 제약조건을 선언할 수는 있다.

```sql
CREATE TABLE Bugs_2009 (
  ...
  date_reported DATE CHECK (EXTRACT(YEAR FROM date_reported) = 2009)
);
```

이 경우, 새로운 테이블을 만들 때 CHECK 제약조건의 값을 조정해야 함을 잊으면 안된다. 실수하면, 들어가야 하는 데이터를 모두 거부하는 테이블이 되어버린다.

#### 데이터 동기화
단순 `UPDATE` 문으로 처리하기 어려울 수 있다.

```sql
INSERT INTO Bugs_2009 (bug_id, date_reported, ...)
  SELECT bug_id, date_reported, ...
  FROM Bugs_2010
  WHERE bug_id = 1234;

DELETE FROM Bugs_2010 WHERE bug_id = 1234;
```

#### 유일성 보장
PK 값은 모든 분할된 테이블에 걸쳐 유일함이 보장되어야 한다. 한 테이블에서 다른 테이블로 행을 옮겨야 하면, PK 값이 다른 행과 충돌하지 않는다는 확신이 있어야 한다.

시퀀스 객체를 지원하는 DB를 사용한다면, 키 값 생성을 위해 모든 분리된 테이블에 대해 하나의 시퀀스를 사용할 수 있다. 테이블당 ID 유일성만을 보장하는 DB에선 조금 까다로워진다. PK 값 생성만을 위한 별도 테이블을 하나 정의해야 한다.

```sql
CREATE TABLE BugsIdGenerator (bug_id SERIAL PRIMARY KEY);

INSERT INTO BugsIdGenerator (bug_id) VALUES (DEFAULT);
ROLLBACK;

INSERT INTO Bugs_2010 (bug_id, ...)
  VALUES (LAST_INSERT_ID(), ...);
```

#### 여러 테이블에 걸쳐 조회하기
불가피하게 여러 테이블에 걸쳐 조회할 필요가 생긴다면, 분리된 모든 테이블을 `UNION`으로 묶어 전체 집합으로 재구성한 다음 이에 대해 쿼리를 실행해야 한다.

```sql
SELECT b.status, COUNT(*) AS count_per_status FROM (
  SELECT * FROM Bugs_2008
    UNION ALL
  SELECT * FROM Bugs_2009
    UNION ALL
  SELECT * FROM Bugs_2010 ) AS b
GROUP BY b.status;
```

테이블이 새롭게 추가된다면, 애플리케이션 코드에서도 새로 추가된 테이블을 참조하도록 위 쿼리 역시 수정해야 한다.

#### 메타데이터 동기화
만약 특정 컬럼을 추가하려면, 각 테이블에 똑같이 추가해줘야 한다.

#### 참조 정합성 관리
- `Comments`와 같은 종속 테이블이 `Bugs`를 참조한다면, 종속 테이블에서 FK를 선언할 수 없게 된다. 부모 테이블이 여러 개로 분리되어 있기 때문이다.
- 분리된 테이블은 자식이 될 때도 문제가 생긴다.

```sql
SELECT * FROM Accounts a
JOIN (
  SELECT * FROM Bugs_2008
  UNION ALL
  SELECT * FROM Bugs_2009
  UNION ALL
  SELECT * FROM Bugs_2010
) t ON (a.account_id = t.reported_by);
```

#### 메타데이터 트리블 컬럼 식별하기
컬럼 또한 메타데이터 트리블이 될 수 있다.

```sql
CREATE TABLE ProjectHistory (
  ...
  bugs_fixed_2008 INT,
  bugs_fixed_2009 INT,
  bugs_fixed_2010 INT
);
```

### 안티패턴 인식 방법
- "그러면 우리는 ~당 테이블(또는 컬럼)을 생성해야 해"
- "이 데이터베이스에 테이블을 최대 몇 개까지 만들 수 있을까?"
- "오늘 아침에 애플리케이션이 새로운 데이터를 추가하는 데 실패한 이유를 알아냈어. 새해에 대한 테이블을 만드는 걸 까먹었지 뭐야."
- "어떻게 하면 여러 테이블을 한꺼번에 검색하는 쿼리를 작성할 수 있을까?"
- "어떻게 하면 테이블 이름을 파라미터로 넘길 수 있을까? 테이블 이름 뒤에 연도를 동적으로 붙여서 쿼리를 해야 해"

### 안티패턴 사용이 합당한 경우
매일 사용하는 데이터와 오래된 데이터를 분리해 별도 보관하는 방식으로 테이블 수동 분할을 사용할 수 있다. 일정 시간이 지난 오래된 데이터를 조회할 필요가 크게 줄어드는 것은 종종 있는 일이다. 현재 데이터와 오래된 데이터를 함께 조회할 필요가 없다면, 오래된 데이터를 다른 위치로 옮기고 해당 테이블에서 삭제하는 것이 적절하다. 가끔 필요한 분석을 위해 오래된 데이터를 동일한 테이블 구조로 별도 보관하면 현재 데이터에 대한 쿼리 성능도 훨씬 좋아질 수 있다.

### 해법: 파티션과 정규화
- 수평 분할 사용
  - 파티셔닝
- 수직 분할 사용
  - 컬럼으로 테이블을 나누는 방법은 크기가 큰 컬럼이나 거의 사용되지 않는 컬럼이 있을 때 유리하다.
  - 종속된 다른 테이블을 만들고, 이 테이블의 `BLOB` 컬럼에 큰 데이터를 저장한다. 한 베이스테이블의 항목 당 하나의 행만 존재하는 것이 보장되도록 종속 테이블의 PK를 베이스 테이블의 FK로 만든다.
- 종속 테이블 사용

```sql
CREATE TABLE ProjectHistory (
  project_id BIGINT,
  year       SMALLINT,
  bug_fixed  INT,
  PRIMARY KEY (project_id, year),
  FOREIGN KEY (project_id) REFERENCES Projects(project_id),
);
```

프로젝트 하나를 한 행으로 하고 연도별 컬럼을 사용하는 대신, 수정된 버그 개수를 한 컬럼에 여러 개의 행으로 저장하는 것이 좋다. 이러면 해가 바뀌더라도 새로운 컬럼을 추가할 필요가 없다.

**데이터가 메타데이터를 낳도록 하지 말자.**