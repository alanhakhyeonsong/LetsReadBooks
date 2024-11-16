# 2장. 무단횡단
### 목표: 다중 값 속성 저장

```sql
CREATE TABLE Products (
  product_id    SERIAL PRIMARY KEY,
  product_name  VARCHAR(1000),
  account_id    BIGINT UNSIGNED,
  FOREIGN KEY (account_id) REFERENCES Accounts (account_id)
);

INSERT INTO Products (product_id, product_name, account_id)
VALUES (DEFAULT, 'Visual TurboBuilder', 12);
```

### 안티패턴: 쉼표로 구분된 목록에 저장

```sql
CREATE TABLE Products (
  product_id    SERIAL PRIMARY KEY,
  product_name  VARCHAR(1000),
  account_id    VARCHAR(100), -- 쉼표로 구분된 목록
);

INSERT INTO Products (product_id, product_name, account_id)
VALUES (DEFAULT, 'Visual TurboBuilder', '12,34');
```

다대다 테이블을 만들지 않기 위해 위와 같은 행동을 하지 말자.

- 모든 FK가 하나의 필드에 결합되어 있으면 쿼리가 어려워진다.
- 정규표현식 및 패턴 매칭으로 인한 이슈 사항이 한둘이 아님.
  - 조인, 집계 쿼리, 유효성 검증, ...
  - 목록 길이 제한

### 안티패턴 인식 방법
- 이 목록이 지원해야 하는 최대 항목 수는 얼마나 될까? - `VARCHAR` 컬럼의 최대 길이 산정 시
- SQL에서 단어의 경계를 어떻게 알아내는지 알아? - 문자열의 일부를 찾아내기 위해 정규 표현식을 사용한다면, 이런 부분을 별도로 저장해야 함을 뜻하는 단서
- 이 목록에서 절대 나오지 않을 문자가 어떤 게 있을까? - 모호하지 않은 문자를 구분자로 사용하고 싶겠지만, 언젠가 그 문자가 목록의 값으로 나타날 것이라 예상해야 함.

### 안티패턴 사용이 합당한 경우
애플리케이션에서 쉼표로 구분된 형식의 데이터를 필요로하고, 목록 안의 개별 항목엔 접근할 필요가 없을 수 있다. 비슷하게 애플리케이션이 다른 출처에서 쉼표로 구분된 형식으로 데이터를 받아 데이터베이스에 그대로 저장하고 나중에 동일한 형식으로 불러내야 하며, 목록 안의 개별 값을 분리할 필요가 없다면 안티패턴을 사용할 수 있다.

### 해법: 교차 테이블 생성
- 컬럼에 FK를 생성하면 내부적으로 해당 컬럼에 대한 인덱스를 생성한다. (대부분의 DB)