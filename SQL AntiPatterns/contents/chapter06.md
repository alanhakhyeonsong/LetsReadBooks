# 6장. 엔터티-속성-값
### 목표: 가변 속성 지원
OOP는 동일한 데이터 타입을 확장하는 것과 같은 방법으로 객체의 타입도 관계를 가질 수 있다. 보통 여러 객체의 계산이나 비교를 간단히 하기 위해 객체를 하나의 DB 테이블에 행으로 저장하고자 한다. 게다가 객체의 각 서브타입이 베이스 타입이나 다른 서브타입에는 적용되지 않는 속성 칼럼을 저장하는 것도 허용해야 한다.

### 안티패턴: 범용 속성 테이블 사용
가변 속성을 지원해야 할 때 일부 개발자가 흥미를 갖는 방법은 **별도 테이블을 생성해 속성을 행으로 저장하는 것이다.**

- 엔티티: 이 컬럼은 하나의 엔티티에 대해 하나의 행을 가지는 부모 테이블에 대한 FK
- 속성: 일반적인 테이블에서의 컬럼 이름을 나타내지만, 이 새로운 설계에선 각 행마다 속성이 하나씩 들어간다.
- 값: 모든 엔티티는 각 속성에 대한 값을 가진다.
  - PK 값이 1234인 버그가 주어졌을 때, `status`란 속성을 가지고, 그 속성 값은 `NEW`다.

이 설계는 EAV, 오픈 스키마, 스키마리스 또는 이름-값 쌍으로 불리기도 한다.

```sql
CREATE TABLE Issues (
  issue_id SERIAL PRIMARY KEY
);

INSERT INTO Issues (issue_id) VALUES (1234);

CREATE TABLE IssueAttributes(
  issue_id BIGINT UNSIGNED NOT NULL,
  attr_name VARCHAR(100) NOT NULL,
  attr_value VARCHAR(100),
  PRIMARY KEY (issue_id, attr_name),
  FOREIGN KEY (issue_id) REFERENCES Issues(issue_id)
);

INSERT INTO IssueAttributes (issue_id, attr_name, attr_value)
  VALUES
    (1234, 'product', '1'),
    (1234, 'date_reported', '2009-06-01'),
    (1234, 'status', 'NEW'),
    (1234, 'description', 'Saving does not work'),
    (1234, 'reported_by', 'Bill'),
    (1234, 'version_affected', '1.0'),
    (1234, 'severity', 'loss of functionality'),
    (1234, 'priority', 'high'),
```

별도 테이블을 추가해 아래와 같은 이득을 얻은 것 같아 보인다.

- 두 테이블 모두 적은 컬럼을 갖고 있다.
- 새로운 속성을 지원하기 위해 컬럼 수를 늘릴 필요가 없다.
- 특정 속성이 해당 행에 적용되지 않을 때 NULL을 채워야 하는 컬럼이 지저분하게 생기는 것을 피할 수 있다.

#### 속성 조회
간단한 쿼리에 대해 복잡하게 작성해야 한다.

```sql
-- 일반적 테이블 설계 시
select issue_id, date_reported from Issues;

-- EAV 설계 시
select issue_id, attr_value AS "date_reported"
FROM IssueAttributes
WHERE attr_name = 'date_reported';
```

#### 데이터 정합성 지원
- 필수 속성 사용 불가
  - 각 속성이 행으로 대응되므로 각 `issue_id` 값에 대해 행이 존재하는지, 그 행의 `attr_name` 컬럼이 `date_reported`를 가지고 있는지 확인해야 하는데 SQL에선 이런 제약조건을 지원하지 않는다.
- SQL 데이터 타입 사용 불가
  - 날짜 데이터가 아닌 문자열을 입력 시 버그
- 참조 정합성 강제 불가
  - EAV 설계에선 `attr_value` 컬럼에 제약조건을 일반적으로 걸 수 없다. 참조 정합성 제약조건은 테이블의 모든 행에 적용된다.
- 속성 이름 강제 불가

#### 행을 재구성하기

|issue_id|date_reported|status|priority|description|
|--|--|--|--|--|
|1234|2009-06-01|신규|높음|저장 기능 동작 안함|

각 속성이 별도의 행으로 저장되어 있으므로, 행 하나의 일부로 속성을 꺼내기 위해선 각 속성에 대한 조인이 필요하다.

```sql
SELECT i.issue_id,
  i1.attr_value AS "date_reported",
  i2.attr_value AS "status",
  i3.attr_value AS "priority",
  i4.attr_value AS "description",
FROM Issues AS i
  LEFT OUTER JOIN IssueAttributes AS i1
    ON i.issue_id = i1.issue_id AND i1.attr_name = 'date_reported'
  LEFT OUTER JOIN IssueAttributes AS i2
    ON i.issue_id = i2.issue_id AND i2.attr_name = 'status'
  LEFT OUTER JOIN IssueAttributes AS i3
    ON i.issue_id = i3.issue_id AND i3.attr_name = 'priority'
  LEFT OUTER JOIN IssueAttributes AS i4
    ON i.issue_id = i4.issue_id AND i4.attr_name = 'description'
WHERE i.issue_id = 1234;
```

내부 조인을 사용하면 `IssueAttributes`에 속성이 하나라도 없는 경우 아무 것도 리턴되지 않을 수 있으므로 외부 조인을 사용해야 한다. 속성 개수가 늘어나면 조인 회수도 늘어나야 하고, 쿼리의 비용은 지수적으로 증가한다.

### 안티패턴 인식 방법
- "이 데이터베이스는 메타데이터 변경 없이 확장이 가능하지. 런타임에 새로운 속성을 정의할 수 있어"
  - RDBMS에선 이런 수준의 유연성을 제공하지 않는다.
- "하나의 쿼리에서 조인을 최대 몇 번이나 할 수 있지?"
  - DB 한계를 넘어서는 것을 걱정할 정도로 조인 회수가 많은 쿼리가 필요하다면 설계에 문제가 있다.
- "우리 전자상거래 플랫폼에선 리포트를 어떻게 생성해야 할지 이해할 수가 없어. 아무래도 컨설턴트를 고용해야 할 것 같아."

### 안티패턴 사용이 합당한 경우
- 비관계형 데이터 관리가 필요하다면 아래와 같은 비관계형 기술을 사용하는 것이다.
  - Cassandra
  - MongoDB
  - Redis
  - Hadoop, HBase

### 해법: 서브타입 모델링
- 단일 테이블 상속
- 구체 테이블 상속
- 클래스 테이블 상속
- 반구조적 데이터
  - TEXT 타입의 컬럼으로 XML 혹은 JSON 형태를 저장

메타데이터를 위해선 메타데이터를 사용하자.