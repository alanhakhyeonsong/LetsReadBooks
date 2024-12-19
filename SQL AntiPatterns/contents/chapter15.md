# 15장. 애매한 그룹

### 목표: 그룹당 최댓값을 가진 행 얻기
```sql
SELECT product_id, MAX(date_reported) AS latest, bug_id
FROM Bugs JOIN BugsProducts USING (bug_id)
GROUP BY product_id;
```

보고일자가 가장 최근인 버그의 ID를 얻기 위해 위와 같은 쿼리 형태가 나타날 텐데, 위 쿼리는 에러가 발생하거나 결과를 신뢰할 수 없다.  
목표는 그룹의 최댓값(or 최솟값 or 평균 값)뿐 아니라 해당 값을 찾은 행의 다른 속성도 포함하도록 쿼리를 작성하는 것이다.

### 안티패턴: 그룹되지 않은 컬럼 참조
#### 단일 값 규칙
- 각 그룹의 행은 `GROUP BY` 절 뒤에 쓴 컬럼(또는 컬럼 목록)의 값이 같은 행이다. 앞선 쿼리에서 select 목록에 있는 모든 컬럼은 그룹당 하나의 값을 가져야 한다. → 단일 값 규칙
- `GROUP BY` 절 뒤에 쓴 칼럼들은 얼마나 많은 행이 그룹에 대응되는지 상관 없이 각 그룹당 정확히 하나의 값만 나온다는 것이 보장된다.
- `MAX()` 함수 또한 각 그룹당 하나의 값만 내보낸다는 것이 보장된다.
- 하지만 select 목록에 있는 다른 컬럼에 대해선 DB가 이를 확신할 수 없다.
  - 그룹 안에서 모든 행에 같은 값이 나오는지 보장할 수 없음.

#### 내 뜻대로 동작하는 쿼리
다른 컬럼에 사용된 `MAX()`를 통해 어떤 `bug_id`를 넣어야 할지 SQL이 알아낼 수 있다 잘못 생각하게 된다.  
→ **쿼리가 최댓값을 얻을 때, 자연스레 다른 컬럼 값도 그 최댓값을 얻은 행에서 가져올 것이라 가정한다.**

아래와 같은 경우엔 이런 추론을 할 수 없다.

- 두 버그의 `date_reported` 값이 동일하고 이 값이 그룹 내 최댓값이라면, 쿼리에서 어느 `bug_id` 값을 보여줘야 하는가?
- 쿼리에서 두 가지 다른 집계 함수를 사용한다면 이는 그룹 안에서 두 개의 다른 행에 대응될 것이다.
- 집계 함수가 리턴하는 값과 매치되는 행이 없는 경우엔 `bug_id` 값을 어떻게 해야 하는가?
  - `AVG()`, `COUNT()`, `SUM()`과 같은 함수를 사용할 때 발생한다.

```sql
SELECT product_id, SUM(hours) AS total_project_estimate, bug_id
FROM Bugs JOIN BugsProducts USING (bug_id)
GROUP BY product_id;
```

이런 이유로 단일 값 규칙이 중요하다. 이 규칙을 준수하지 못하는 모든 쿼리가 애매한 결과를 만드는 건 아니지만, 많은 경우는 그렇다.

동일한 쿼리가 데이터 상태에 따라 유효하거나 유효하지 않을 수 있다. 이는 애플리케이션 신뢰성을 위해 좋은 게 아니다.

### 해법: 컬럼을 모호하게 사용하지 않기
#### 함수 종속인 컬럼만 쿼리하기
가장 간단한 방법이다.

```sql
SELECT product_id, MAX(date_reported) AS latest
FROM Bugs JOIN BugsProducts USING (bug_id)
GROUP BY product_id;
```

#### 상호 연관된 서브쿼리 사용하기
바깥쪽 쿼리에 대한 참조를 가지고 있어 바깥쪽 쿼리의 각 행에 대해 다른 결과를 생성할 수 있다. 이를 이용해 서브쿼리가 그룹 내 날짜가 큰 버그를 찾게 해, 각 제품별로 가장 최근에 보고된 버그를 찾을 수 있다. 서브쿼리가 아무것도 못 찾으면, 바깥쪽 쿼리에 있는 버그가 가장 최근의 버그다.

```sql
SELECT bp1.product_id, b1.date_reported AS latest, b1.bug_id
FROM Bugs b1 JOIN BugsProducts bp1 USING (bug_id)
WHERE NOT EXISTS
 (SELECT * FROM Bugs b2 JOIN BugsProducts bp2 USING (bug_id)
  WHERE bp1.product_id = bp2.product_id
    AND b1.date_reported < b2.date_reported);
```

하지만, 상호 연관된 서브쿼리는 바깥쪽 쿼리의 각 행에 대해 한 번씩 실행되기 때문에, 성능상 최적의 방법은 아니다.

#### 유도 테이블 사용하기
서브쿼리를 유도 테이블(인라인 뷰)로 사용해, 각 제품에 대한 `product_id`와 버그 보고일자의 최댓값만 포함하는 임시 결과를 만들 수 있다. 그런 다음 이 결과를 테이블과 조인해 쿼리 결과가 각 제품당 가장 최근의 버그만 포함하게 한다.

```sql
SELECT m.product_id, m.latest, b1.bug_id
FROM Bugs b1 JOIN BugsProducts bp1 USING (bug_id)
  JOIN (SELECT bp2.product_id, MAX(b2.date_reported) AS latest
        FROM Bugs b2 JOIN BugsProducts bp2 USING (bug_id)
        GROUP BY bp2.product_id) m
  ON (bp1.product_id = m.product_id AND b1.date_reported = m.latest);
```

|product_id|latest|bug_id|
|--|--|--|
|1|2010-06-01|2248|
|2|2010-02-16|3456|
|2|2010-02-16|5150|
|3|2010-01-01|5678|

서브쿼리가 여러 행의 `latest` 날짜를 리턴하면 하나의 제품에 대해 여러 행이 나올 수 있음에 유의하자.

```sql
-- product_id 당 하나의 행만 나오게 한다면, 바깥 쿼리에 다른 그룹핑 함수를 사용하자.
SELECT m.product_id, m.latest, MAX(b1.bug_id) AS latest_bug_id
FROM Bugs b1 JOIN
  (SELECT product_id, MAX(date_reported) AS latest
   FROM Bugs b2 JOIN BugsProducts USING (bug_id)
   GROUP BY product_id) m
  ON (b1.date_reported = m.latest)
GROUP BY m.product_id, m.latest;
```

|product_id|latest|bug_id|
|--|--|--|
|1|2010-06-01|2248|
|2|2010-02-16|5150|
|3|2010-01-01|5678|

유도 테이블은 상호 연관되지 않아, 대부분의 DB에서 서브쿼리가 한 번만 수행된다. 하지만, 임시 테이블에 중간 결과를 저장해야 하므로 성능상 최적의 방법은 아니다.

#### 조인 사용하기
대응되는 행이 없을 수도 있는 행의 집합에 대해 대응을 시도하는 조인을 할 수 있다. 이를 **외부 조인**이라 한다.

```sql
SELECT bp1.product_id, b1.date_reported AS latest, b1.bug_id
FROM Bugs b1 JOIN BugsProducts bp1 ON (b1.bug_id = bp1.bug_id)
LEFT OUTER JOIN (Bugs AS b2 JOIN BugsProducts AS bp2 ON (b2.bug_id = bp2.bug_id))
  ON (bp1.product_id = bp2.product_id AND (b1.date_reported < b2.date_reported
    OR b1.date_reported = b2.date_reported AND b1.bug_id < b2.bug_id))
WHERE b2.bug_id IS NULL;
```

// 이 쿼리는 분석을 자주 해봐야함...

#### 다른 컬럼에 집계 함수 사용하기
```sql
SELECT product_id, MAX(date_reported) AS latest,
  MAX(bug_id) AS latest_bug_id
FROM Bugs JOIN BugsProducts USING (bug_id)
GROUP BY product_id;
```

#### 각 그룹에 대해 모든 값을 연결하기

```sql
SELECT product_id, MAX(date_reported) AS latest
  GROUP_CONCAT(bug_id) AS bug_id_list
FROM Bugs JOIN BugsProducts USING (bug_id)
GROUP BY product_id;
```

|product_id|latest|bug_id_list|
|--|--|--|
|1|2010-06-01|1234, 2248|
|2|2010-02-16|3456, 4077, 5150|
|3|2010-01-01|5678, 8063|