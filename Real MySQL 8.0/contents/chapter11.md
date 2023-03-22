# 11장. 쿼리 작성 및 최적화
이번 장에선, SELECT query를 위주로 정리해보고자 한다.

웹 서비스와 같이 일반적인 온라인 트랜잭션 처리 환경의 데이터베이스에서는 INSERT나 UPDATE 같은 작업은 거의 레코드 단위로 발생하므로 성능상 문제가 되는 경우는 별로 없다.  
하지만, SELECT는 여러 개의 테이블로부터 데이터를 조합해서 빠르게 가져와야 하기 때문에 여러 개의 테이블을 어떻게 읽을 것인가에 많은 주의를 기울여야 한다.

## SELECT 절의 처리 순서
```sql
SELECT s.emp_no, COUNT(DISTINCT e.first_name) AS cnt
FROM salaries s
  INNER JOIN employees e ON e.emp_no = s.emp_no
WHERE s.emp_no IN (100001, 100002)
GROUP BY s.emp_no
HAVING AVG(s.salary) > 1000
ORDER BY AVG(s.salary)
LIMIT 10;
```

위 예제 쿼리는 SELECT 문장에 지정할 수 있는 대부분의 절이 포함돼 있다. 가끔 이런 쿼리에서 어느 절이 먼저 실행될지 예측하지 못할 때가 있는데, 어느 절이 먼저 실행되는지를 모른다면 처리 내용이나 처리 결과를 예측할 수 없다.

![image](https://user-images.githubusercontent.com/60968342/226342321-8651ee06-0e93-49f0-bec7-172356b7f034.png)

각 요소가 없는 경우는 가능하나, 위 순서가 바뀌어서 실행되는 형태의 쿼리는 거의 없다. ORDER BY나 GROUP BY 절이 있더라도 인덱스를 이용해 처리할 때는 그 단계 자체가 불필요하므로 생략된다.

![image](https://user-images.githubusercontent.com/60968342/226342290-9949a6eb-274f-43ee-8054-c5becbb63d6e.png)

위 그림은 ORDER BY가 사용된 쿼리에서 예외적인 순서로 실행되는 경우를 나타낸다. 이 경우는 첫 번째 테이블만 읽어 정렬을 수행한 뒤 나머지 테이블을 읽는데, 주로 GROUP BY 절 없이 ORDER BY만 사용된 쿼리에서 사용될 수 있는 순서다.

위 그림에서 소개한 실행 순서를 벗어나는 쿼리가 필요하다면, 서브쿼리로 작성된 인라인 뷰(Inline View)를 사용해야 한다.

```sql
SELECT s.emp_no, cnt
FROM (
  SELECT s.emp_no, COUNT(DISTINCT e.first_name) AS cnt, MAX(s.salary) AS max_salary
  FROM salaries s
    INNER JOIN employees e ON e.emp_no = s.emp_no
  WHERE s.emp_no IN (100001, 100002)
  GROUP BY s.emp_no
  HAVING MAX(s.salary) > 1000
  LIMIT 10
) temp_view
ORDER BY max_salary;
```

하지만, 이렇게 인라인 뷰가 사용되면 임시 테이블이 사용되기 때문에 주의해야 한다.

## 인덱스를 사용하기 위한 기본 규칙
**기본적으로 인덱스된 칼럼의 값 자체를 변환하지 않고 그대로 사용한다는 조건을 만족해야 한다. 인덱스는 칼럼의 값을 아무런 변환 없이 B-Tree에 정렬해서 저장한다.**  
WHERE 조건이나 GROUP BY 또는 ORDER BY에서도 원본값을 검색하거나 정렬할 때만 B-Tree에 정렬된 인덱스를 이용한다.

## WHERE 절에서 인덱스 사용
- 칼럼 값 자체를 변환하지 않고 그대로 사용해야 한다.
- 비교 조건에서 양쪽의 데이터 타입이 일치해야 한다.
- OR 연산이 있으면 주의해야 한다 : 인덱스 한번 + 풀 테이블 한번 하느니 차라리 풀 테이블 한 번 하는 수 있다.
- 범위 조건이 있으면 그 뒤의 column은 인덱스를 쓰지 못한다.
- MySQL에선 NULL 값이 포함된 레코드도 인덱스로 관리한다.

## GROUP BY 절의 인덱스 사용
- GROUP BY 절에 명시된 칼럼이 인덱스 칼럼의 순서와 위치가 같아야 한다.
- 인덱스를 구성하는 칼럼 중에서 뒷쪽에 있는 칼럼은 GROUP BY 절에 명시되지 않아도 인덱스를 사용할 순 있지만, 인덱스 앞쪽에 있는 칼럼이 GROUP BY 절에 명시되지 않으면 인덱스를 사용할 수 없다.
- WHERE 조건절과는 달리 GROUP BY 절에 명시된 칼럼이 하나라도 인덱스에 없으면 GROUP BY 절은 전혀 인덱스를 이용하지 못한다.

WHERE 조건 없이 인덱스 순서가 col1, col2, col3, col4라 가정할 때, 다음과 같다.
```sql
... GROUP BY col1 # 사용 가능
... GROUP BY col1, col2 # 사용 가능
... GROUP BY col2, col1 # 순서 불일치. 사용 불가
... GROUP BY col1, col3 # 순서는 일치하나, col2가 누락되서 사용 불가
... GROUP BY col1, col3, col2 # 순서 불일치. 사용 불가
... GROUP BY col1, col2, col3, col4, col5 # col5는 인덱스에 들어있지 않아 사용 불가
```

WHERE 조건이 아래와 같이 하나일 때, 인덱스 앞의 칼럼으로 한 번 걸러졌기 때문에 뒤의 칼럼부터 GROUP BY 절에 있어도 인덱스를 사용할 수 있다.

```sql
... WHERE col1 = '상수' GROUP BY col2, col3 # 사용 가능
... WHERE col1 = '상수', col2 = '상수' GROUP BY col3, col4 # 사용 가능
```

## ORDER BY 절의 인덱스 사용
ORDER BY 절의 인덱스 사용 여부는 GROUP BY의 요건과 거의 흡사하다. 하지만, **ORDER BY는 정렬되는 각 컬럼의 오름차순 및 내림차순 옵션이 인덱스와 같거나 정반대인 경우에만 사용할 수 있다.**

인덱스 순서가 col1, col2, col3, col4라 가정할 때, 다음과 같다.

```sql
# 이 예시는 모두 인덱스를 사용할 수 없다.
ORDER BY col2, col3 # col1 누락
ORDER BY col1, col3, col2 # 순서 불일치
ORDER BY col1, col2 DESC, col3 # 중간에 DESC가 껴서 불가
ORDER BY col1, col3 # col2 누락
ORDER BY col1, col2, col3, col4, col5 # col5는 인덱스에 없어서 불가
```

## WHERE 조건과 ORDER BY(or GROUP BY) 절의 인덱스 사용
### WHERE 절과 ORDER BY 절이 동시에 같은 인덱스 사용
- 아래 두 조건보다 훨씬 빠르다. 가능하다면 이 방식으로 처리할 수 있도록 쿼리를 튜닝하거나 인덱스를 생성하자.
- 만약 다른 인덱스를 각각 쓴다면, 아래 두 방법 중 하나로 처리될 것이다. (실행계획 확인 필요)

### WHERE 절만 인덱스 사용
- ORDER BY 절은 인덱스를 쓸 수 없고, WHERE 절은 인덱스를 쓸 수 있을 때
- 인덱스를 통해 검색된 결과 레코드를 별도의 정렬 처리 과정(FileSort)을 거쳐 정렬을 수행한다. 주로 이 방법은 WHERE 절의 조건에 일치하는 레코드의 건수가 많지 않을 때 효과적이다.

### ORDER BY 절만 인덱스 사용
- ORDER BY 절은 인덱스를 쓸 수 있지만, WHERE 절은 인덱스를 쓸 수 없을 때
- ORDER BY 절의 순서대로 인덱스를 읽으며, 레코드 한 건씩을 WHERE 절의 조건에 일치하는지 비교해 일치하지 않을 때 버리는 형태로 처리. 아주 많은 레코드를 조회해서 정렬해야 할 때는 이런 형태로 튜닝하기도 한다.

### GROUP BY + ORDER BY
GROUP BY 절에 명시된 칼럼과 ORDER BY에 명시된 칼럼의 순서와 내용이 모두 같아야 한다. GROUP BY와 ORDER BY가 같이 사용된 쿼리에서는 둘 중 하나라도 인덱스를 이용할 수 없을 때는 둘 다 인덱스를 사용하지 못한다.

MySQL의 GROUP BY는 ORDER BY 칼럼에 대한 정렬까지 함께 수행하는 것이 기본 작동 방식이므로 GROUP BY와 ORDER BY 칼럼이 내용과 순서가 같은 쿼리에서는 ORDER BY 절을 생략해도 같은 결과를 얻게 된다.

### WHERE + GROUP BY + ORDER BY
![image](https://user-images.githubusercontent.com/60968342/226357265-e64bdd5b-f2a9-428c-b43b-e384ab3d18a5.png)

## DISTINCT
집합 함수와 같이 DISTINCT가 사용되는 쿼리의 실행걔획에선 DISTINCT가 인덱스를 사용하지 못할 때는 항상 임시 테이블이 있어야 한다. 하지만, 실행 계획의 Extra 칼럼에는 'Using temporary' 메시지가 출력되지 않는다.

- SELECT DISTINCT : 집합 함수와 함께 사용되지 않는 DISTINCT는 GROUP BY와 거의 같은 방식으로 처리된다. 단지 차이는 SELECT DISTINCT의 경우 정렬이 보장되지 않는다는 것뿐이다.
- 집합 함수와 함께 사용된 DISTINCT : 집합 함수 내에서 사용된 DISTINCT는 그 집합 함수의 인자로 전달된 칼럼 값들 중 중복을 제거하고 남은 값만을 가져온다.

## LIMIT n
- MySQL의 LIMIT는 항상 쿼리의 가장 마지막에 실행된다.
- LIMIT의 중요한 특성은 LIMIT에서 필요한 레코드 건수만 준비되면 바로 쿼리를 종료시킨다는 것이다.
- LIMIT 0이 사용되면 MySQL 옵티마이저는 쿼리를 실행하지 않고 최적화만 실행한 후 종료

## JOIN
### JOIN의 순서와 인덱스
조인이 들어간 쿼리 튜닝 이전에, **인덱스 레인지 스캔**으로 레코드를 읽는 작업을 다시 한번 생각해보자.
- 먼저 **인덱스 탐색**은, 인덱스에서 조건을 만족하는 값이 저장된 위치를 찾는 과정이다.
- **인덱스 스캔**은 인덱스 탐색 이후 탐색된 위치부터 필요한 만큼 인덱스를 쭉 읽는 과정이다.
- 이 과정까지 읽어들인 인덱스 키와 레코드 주소를 이용해 레코드가 저장된 페이지를 가져오고, 최종 레코드를 읽어온다.

일반적으로 인덱스를 이용해서 쿼리하는 작업에서는 가져오는 레코드의 건수가 소량이기 때문에 인덱스 스캔 작업은 부하가 작지만 특정 인덱스 키를 찾는 인덱스 탐색 작업은 상대적으로 부하가 높은 편이다.

조인 작업에서 드라이빙 테이블을 읽을 때는 인덱스 탐색 작업을 단 한 번만 수행하고, 드리븐 테이블에서는 인덱스 탐색 작업과 스캔 작업을 드라이빙 테이블에서 읽은 레코드 건수만큼 반복한다. 드라이빙 테이블과 드리븐 테이블이 1:1로 조인되더라도 드리븐 테이블을 읽는 것이 훨씬 더 큰 부하를 차지한다. 때문에 옵티마이저는 항상 드라이빙 테이블이 아니라 드리븐 테이블을 최적으로 읽을 수 있게 실행 계획을 수립한다.

```sql
SELECT *
FROM employees e, dept_emp de
WHERE e.emp_no = de.emp_no;
```
위 query에서 employees 테이블과 dept_emp 테이블의 emp_no 칼럼에 인덱스가 어떻게 있는지에 따라 쿼리 수행 절차가 달라진다.

- 두 테이블 모두 index 존재 : 어느 테이블을 드라이빙으로 선택하든 인덱스를 이용해 드리븐 테이블의 검색 작업을 빠르게 처리할 수 있다. 옵티마이저가 각 테이블의 통계 정보에 따라 두 테이블 모두 드라이빙 테이블이 될 수 있다.
- employees에만 index 존재 : 인덱스가 존재하는 테이블을 반복적으로 읽는 것이 효과적이므로 인덱스가 없는 dept_emp 테이블을 드라이빙 테이블로 선택하고, employees 테이블을 드리븐 테이블로 선택한다.
- dept_emp에만 index 존재 : employees 테이블의 반복된 풀 스캔을 피하기 위해 앞선 케이스와 반대로 동작한다.
- 모두 인덱스가 없는 경우 : 통계 정보에 따라 두 테이블 모두 드라이빙 테이블이 될 수 있으며, 어느쪽이든 드리븐 테이블을 읽어올 때 풀 테이블 스캔이 걸린다. 따라서 조인 버퍼를 사용할 가능성이 높다.

### JOIN 칼럼의 데이터 타입 관련 주의사항
WHERE 절에 사용되는 조건에서 비교 대상 칼럼과 표현식의 데이터 타입을 반드시 동일하게 사용해야 한다고 정리했다. 이것은 테이블의 조인 조건에서도 동일하다.  
조인 칼럼 간의 비교에서 각 칼럼의 데이터 타입이 일치하지 않으면 인덱스를 효율적으로 이용할 수 없다.

### OUTER JOIN의 성능과 주의사항
INNER JOIN은 조인 데상 테이블에 모두 존재하는 레코드만 결과 집합으로 반환한다. 이 같은 특성 때문에 OUTER JOIN으로만 조인을 실행하는 쿼리들을 사용해야 하는 상황도 자주 있다.

```sql
SELECT *
FROM employees e
  LEFT JOIN dept_emp de ON de.emp_no = e.emp_no
  LEFT JOIN departments d ON d.dept_no = de.dept_no AND d.dept_name = 'Developmnet';
```

위 쿼리의 실행 계획을 보면 제일 먼저 employees 테이블을 풀 스캔 하면서 dept_emp 테이블과 departments 테이블을 드리븐 테이블로 사용한다는 것을 알 수 있다.

![image](https://user-images.githubusercontent.com/60968342/226523814-f6000c5c-8c58-433c-9d89-13dbba8efc9a.png)

employees 테이블에 존재하는 사원 중 dept_emp 테이블에 레코드를 갖지 않는 경우가 있다면 OUTER JOIN이 필요하지만, 대부분은 그런 경우는 없으므로 굳이 사용할 필요는 없다.  
**즉, 테이블의 데이터가 일관되지 않은 경우에만 OUTER JOIN이 필요하다.**

MySQL 옵티마이저는 절대 아우터로 조인되는 테이블을 드라이빙 테이블로 선택하지 못하기 때문에 풀 스캔이 필요한 employees 테이블을 드라이빙 테이블로 선택한다. 그 결과 쿼리의 성능이 떨어지는 실행 계획을 수립하는 것이다.

위 쿼리에 INNER JOIN을 사용했다면 다음과 같이 departments 테이블에서 부서명이 Development인 레코드 1건만 찾아서 조인을 실행하는 실행 계획을 선택했을 것이다.

![image](https://user-images.githubusercontent.com/60968342/226524147-caa11ecf-5876-49d9-b0cc-4ed0c3a22975.png)

필요한 데이터와 조인되는 테이블 간의 관계를 정확히 파악해서 꼭 필요한 경우가 아니라면 INNER JOIN을 사용하는 것이 업무 요건을 정확히 구현함과 동시에 쿼리의 성능도 향상시킬 수 있다.

OUTER JOIN 쿼리를 작성하면서 많이 하는 다른 실수는 다음과 같다. OUTER로 조인되는 테이블에 대한 조건을 WHERE 절에 함께 명시하는 것이다.

```sql
# Bad
SELECT * FROM employees e
  LEFT JOIN dept_manager mgr ON mgr.emp_no = e.emp_no
WHERE mgr.dept_no = 'd001';

# TO-BE (옵티마이저가 변환해서 실행한 결과)
SELECT * FROM employees e
  INNER JOIN dept_manager mgr ON mgr.emp_no = e.emp_no
WHERE mgr.dept_no = 'd001';
```

LEFT JOIN 절이 걸린 다음 WHERE 조건 절을 체크하기 때문에 dept_manager가 없는 경우는 dept_manager 테이블의 dept_no 칼럼이 존재하지 않아 체크할 수 없다. 따라서 mgr.dept_no 가 d001인 dept_manager만 있는 경우 조인하는 것이 아니라, 일단 조인한 뒤 dept_no를 체크하기 때문에 그냥 inner join으로 변환된다.

정상적인 OUTER JOIN이 되게 하려면, 다음 쿼리와 같이 WHERE 절의 조건을 LEFT JOIN의 ON 절로 옮겨야 한다.

```sql
SELECT * FROM employees e
  LEFT JOIN dept_manager mgr ON mgr.emp_no = e.emp_no AND mgr.dept_no = 'd001';
```

### JOIN과 FK
FK는 JOIN과 아무런 연관이 없다. FK를 생성하는 주 목적은 데이터의 무결성을 보장하기 위해서이다. (참조 무결성)

데이터 모델링을 할 때는 각 테이블 간의 관계를 필수적으로 그려 넣어야한다. 하지만, 그 데이터 모델을 데이터베이스에 생성할 때는 그 테이블 간의 관계는 외래키로 생성하지 않을 때가 더 많다.

### Delayed Join
JOIN이 실행되기 이전에 GROUP BY, ORDER BY를 처리하는 방식이다.

조인을 사용해서 데이터를 조회하는 쿼리에 GROUP BY, ORDER BY를 사용할 때 각 처리 방법에서 인덱스를 사용한다면 이미 최적으로 처리되고 있을 가능성이 높다. 하지만, 그렇지 못한다면 MySQL 서버는 조인이 모든 조인을 실행하고 난 다음 GROUP BY나 ORDER BY를 실행하게 된다.

파생 테이블에 저장돼야 할 레코드의 건수가 적으면 적을수록 지연된 조인의 효과가 커진다. 따라서 쿼리에 GROUP BY나 DISTINCT 등과 LIMIT 절이 함께 사용된 쿼리에서 상당히 효과적이다.

## GROUP BY
GROUP BY는 특정 칼럼의 값으로 레코드를 그루핑하고, 그룹별로 집계된 결과를 하나의 레코드로 조회할 때 사용한다.

MySQL의 GROUP BY는 정렬 작업까지 수행한다. 이런 정렬 작업 때문에 GROUP BY가 많이 느려지는데, GROUP BY에서 정렬은 하지 않도록 ORDER BY NULL을 추가할 수 있다.

GROUP BY가 사용된 쿼리에서는 그룹핑된 그룹별로 소계를 가져올 수 있는 롤업 기능을 사용할 수 있다.

## ORDER BY
ORDER BY 절이 없는 SELECT 쿼리 결과의 순서는 처리 절차에 따라 달라질 수 있다. 어떤 DBMS도 ORDER BY 절이 명시되지 않은 쿼리에 대해서는 어떠한 정렬도 보장하지 않는다.

ORDER BY에서 인덱스를 사용하지 못할 때는 추가적인 정렬 작업을 수행하고, 쿼리 실행계획에 있는 Extra 칼럼에 'Using filesort'라는 코멘트가 표시된다. 'Filesort'라는 단어에 포함되는 'File'은 디스크의 파일을 이용해 정렬을 수행한다는 의미가 아니라 쿼리를 수행하는 도중에 MySQL 서버가 퀵 소트 정렬 알고리즘을 수행했다는 의미 정도로 이해하면 된다. 정렬 대상이 많은 경우에는 여러 부분으로 나눠서 처리하는데, 정렬된 결과를 임시로 디스크나 메모리에 저장해 둔다.

- ORDER BY 절은 1개 또는 그 이상 여러 개의 칼럼으로 정렬을 수행할 수 있으며, 정렬 순서는 칼럼별로 다르게 명시할 수 있다.
- ORDER BY 뒤에 숫자 값이 아닌 문자열 상수를 사용하는 경우에는 옵티마이저가 ORDER BY 절 자체를 무시한다.
- ORDER BY RAND()를 이용한 임의 정렬이나 조회는 절대 인덱스를 이용할 수 없다.
- MySQL 정렬에서 NULL은 항상 최소의 값으로 간주하고 정렬을 수행한다. 오름차순 정렬인 경우 NULL은 항상 제일 먼저 반환되며, 내림차순인 경우에는 제일 마지막에 반환된다.

## 서브쿼리
서브쿼리는 두 가지 종류가 있다.
- 상관서브쿼리: 서브 쿼리가 독립적으로 실행되지 못하고 외부 쿼리 결과를 기다려야 하는 경우
- 독립서브쿼리: 서브 쿼리가 독립적으로 실행될 수 있는 경우. MySQL에서는 독립 서브쿼리라 하더라도 효율적으로 처리되지 못할 때가 많다.

서브쿼리를 사용할 때는 다음 제약 사항을 주의하자.
- 서브 쿼리를 IN 연산자와 함께 사용할 때는 효율적으로 처리되지 못한다.
- IN 연산자 안에서 사용되는 서브 쿼리에는 ORDER BY와 LIMIT를 동시에 사용할 수 없다.

### SELECT 절에 사용된 서브쿼리
SELECT 절에 사용된 서브쿼리는 내부적으로 임시 테이블을 만들거나 쿼리를 비효율적으로 실행하게 만들지는 않기 때문에 서브쿼리가 적절히 인덱스를 사용할 수 있다면 크게 주의할 사항은 없다.

일반적으로 SELECT 절에 서브쿼리를 사용하면 그 서브쿼리는 항상 칼럼과 레코드가 하나인 결과를 반환해야 한다.

```sql
# success
SELECT emp_no, (SELECT dept_name FROM departments WHERE dept_name = 'Sales1')
FROM dept_emp LIMIT 10;

# fail
SELECT emp_no, (SELECT dept_name FROM departments)
FROM dept_emp LIMIT 10;

# fail
SELECT emp_no, (SELECT dept_no, dept_name FROM departments WHERE dept_name = 'Sales1')
FROM dept_emp LIMIT 10;
```

- 첫 번째 쿼리에서 사용된 서브쿼리는 항상 결과가 0건이다. 하지만, 첫 번째 쿼리는 에러를 발생하지 않고, 서브쿼리의 결과는 NULL로 채워져서 반환된다.
- 두 번째 쿼리에서 서브쿼리가 2건 이상의 레코드를 반환하는 경우에는 에러가 나면서 쿼리가 종료된다.
- 세 번째 쿼리와 같이 SELECT 절에 사용된 서브쿼리가 2개 이상의 칼럼을 가져오려고 할 때도 에러가 발생한다.

즉, SELECT 절의 서브쿼리에는 로우 서브쿼리를 사용할 수 없고, 오로지 스칼라 서브쿼리만 사용할 수 있다.

가끔 조인으로 처리해도 되는 쿼리를 SELECT 절의 서브쿼리를 사용해서 작성할 때도 있다. **하지만 서브쿼리로 실행될 때 보다 조인으로 처리할 때가 조금 더 빠르기 때문에 가능하다면 조인으로 쿼리를 작성하는 것이 좋다. 처리해야 하는 레코드 건수가 많아지면 많아질수록 성능 차이가 커질 수도 있으므로 가능하면 조인으로 쿼리를 작성하는 방법을 권장한다.**

### FROM 절에 사용된 서브쿼리
이전 버전의 MySQL 서버에서는 FROM 절에 서브쿼리가 사용되면 항상 서브쿼리의 결과를 임시 테이블로 저장하고 필요할 때 다시 임시 테이블을 읽는 방식으로 처리했다. 그래서 가능하면 FROM 절의 서브 외부 쿼리로 병합하는 형태로 쿼리 튜닝을 했다. 하지만, MySQL 5.7 버전부터는 옵티마이저가 FROM 절의 서브쿼리를 외부 쿼리로 병합하는 최적화를 수행하도록 개선됐다.

```sql
EXPLAIN SELECT * FROM (SELECT * FROM employees) y;

# MySQL 서버가 재작성한 쿼리 결과
SELECT * FROM employees;
```

서브쿼리의 외부 쿼리 병합은 꼭 FROM 절의 서브쿼리에 대해서만 적용되는 최적화는 아니다. FROM 절에 사용된 뷰(View)의 경우에도 MySQL 옵티마이저는 뷰 쿼리와 외부 쿼리르 ㄹ병합해서 최적화된 실행 계획을 사용한다.

FROM 절의 모든 서브쿼리를 외부 쿼리로 병합할 수 있는 것은 아니다. 다음과 같은 사례는 불가하다.
- 집합 함수 사용 (SUM, MIN, MAX, COUNT 등)
- DISTINCT
- GROUP BY 또는 HAVING
- LIMIT
- UNION(UNION DISTINCT) 또는 UNION ALL
- SELECT 절에 서브쿼리가 사용된 경우
- 사용자 변수 사용(사용자 변수에 값이 할당되는 경우)

외부 쿼리와 병합되는 FROM 절의 서브쿼리가 ORDER BY 절을 가진 경우에는 외부 쿼리가 GROUP BY나 DISTINCT 같은 기능을 사용하지 않는다면 서브쿼리의 정렬 조건을 외부 쿼리로 같이 병합한다. 외부 쿼리에서 GROUP BY나 DISTINCT와 같은 기능이 사용되고 있다면, 서브쿼리의 정렬 작업은 무의미하기 때문에 서브쿼리의 ORDER BY 절은 무시된다.

// 가능하면 사용하지 말자.

### WHERE 절에 단순 비교를 위해 사용된 서브쿼리
MySQL 5.5 이전 버전까지는 서브쿼리 외부의 조건으로 쿼리를 실행하고, 최종적으로 서브쿼리를 체크 조건으로 사용했다. 하지만, 이러한 처리 방식의 경우 풀 테이블 스캔이 필요한 경우가 많아 성능저하가 심각했다.

```sql
SELECT * FROM dept_emp de
WHERE de.emp_no = (SELECT e.emp_no
                  FROM employees e
                  WHERE e.first_name='Georgi' AND e.last_name='Facello' LIMIT 1);
```

MySQL 5.5 이전 버전까지는 위 쿼리의 경우 dept_emp 테이블을 풀 스캔하면서 서브쿼리의 조건에 일치하는지 여부를 체크했다.

이후 버전부터는 이 쿼리의 실행 계획은 그 이전 버전과는 정반대로 실행되도록 개선됐다. 서브쿼리를 먼저 실행한 뒤 상수로 변환한다. 그리고 상숫값으로 서브쿼리를 대체해서 나머지 쿼리 부분을 처리한다.  
책에 나온 실행 계획에 의하면, dept_emp 테이블을 풀 스캔하지 않고 (emp_no, from_date) 조합의 인덱스를 사용했다.

### IN 비교 (IN (subquery))
실제 조인은 아니지만 다음 예제와 같이 테이블의 레코드가 다른 테이블의 레코드를 이용한 표현식(또는 칼럼 그 자체)과 일치하는지를 체크하는 형태를 세미 조인이라고 한다. 즉 WHERE 절에 사용된 IN (subquery) 형태의 조건을 조인의 한 방식인 세미 조인이라고 보는 것이다.

```sql
SELECT *
FROM employees e
WHERE e.emp_no IN
  (SELECT de.emp_no FROM dept_emp de WHERE de.from_date='1995-01-01');
```

MySQL 5.5 버전까지는 세미 조인의 최적화과 매우 부족해서 대부분 풀 테이블 스캔을 했다. 그래서 이런 형태는 사용하지 않아야 하는 패턴이라 알려졌다.  
하지만 MySQL 5.6 버전부터 8.0 버전까지 세미 조인의 최적화가 많이 개선되면서 이제 더이상은 IN (subquery) 형태를 2개의 쿼리로 쪼개어 실행하거나 다른 우회 방법을 찾을 필요가 없어졌다.

MySQL 서버의 세미 조인 최적화는 쿼리 특성이나 조인 관계에 맞게 다음과 같이 5개의 최적화 전략을 선택적으로 사용한다.
- 테이블 풀-아웃
- 퍼스트 매치
- 루스 스캔
- 구체화
- 중복 제거

### NOT IN 비교 (NOT IN (subquery))
IN (subquery)와 비슷한 형태지만 이 경우를 안티 세미 조인이라 명명한다. 일반적인 RDBMS에서 Not-Equal 비교는 인덱스를 제대로 활용할 수 없듯이 안티 세미 조인 또한 최적화할 수 있는 방법이 많지 않다. MySQL 옵티마이저는 안티 세미 조인 쿼리가 사용되면 다음 두 가지 방법으로 최적화를 수행한다.
- NOT EXISTS
- 구체화

두 가지 최적화 모두 그다지 성능 향상에 도움이 되지 않는 방법이므로 쿼리가 최대한 다른 조건을 활용해서 데이터 검색 범위를 좁힐 수 있게 하는 것이 좋다. WHERE 절에 단독으로 안티 세미 조인 조건만 있다면 풀 테이블 스캔을 피할 수 없으니 주의하자.

## 집합 연산
집합 연산은 여러 테이블의 레코드를 연결하는 방법이다.

- UNION : 두 개의 집합을 하나로 묶는 역할. 중복을 제거한다면 UNION DISTINCT를, 제거하지 않는다면 UNION ALL을 사용하면 된다.
- INTERSECT : MySQL에서 바로 제공되지는 않는다.
- MINUS : MySQL에서 바로 제공되지는 않는다.

집합 연산은 모두 임시 테이블이 필요한 작업이다.  
UNION 키워드 뒤에 아무것도 명시하지 않으면 DISTINCT가 적용된다. 중복인 레코드를 확인하는 방법은 '모든 칼럼'을 비교하는 것이다.

## 잠금을 사용하는 SELECT
**InnoDB 테이블에 대해 레코드를 SELECT할 때 레코드에 아무런 잠금도 걸지 않는데, 이를 잠금 없는 읽기(Non Locking Consistent Read)라고 한다.** 하지만 SELECT 쿼리를 이용해 읽은 레코드의 칼럼 값을 애플리케이션에서 가공해서 다시 업데이트하고자 할 때는 **SELECT가 실행된 후 다른 트랜잭션이 그 칼럼의 값을 변경하지 못하게 해야 한다.** 이럴 때는 레코드를 읽으면서 강제로 잠금을 걸어 둘 필요가 있는데, 이때 사용하는 옵션이 `FOR SHARE`와 `FOR UPDATE` 절이다.

두 명령은 auto-commit이 비활성화(OFF)된 상태 또는 START TRANSACTION 명령으로 트랜잭션이 시작된 상태에서만 잠금이 유지된다.
- FOR SHARE : SELECT 쿼리로 읽은 레코드에 대해 읽기 잠금 설정 후 다른 세션에서 해당 레코드를 변경하지 못하게 한다. 다른 세션에서 잠금이 걸린 레코드를 읽는 것은 가능하다.
- FOR UPDATE : SELECT 쿼리가 읽은 레코드에 대해 쓰기 잠금 설정 후 다른 트랜잭션에서는 그 레코드를 변경하는 것뿐만 아니라 읽기도 수행할 수 없다.

```sql
SELECT * FROM employees WHERE emp_no = 10001 FOR SHARE;
SELECT * FROM employees WHERE emp_no = 10001 FOR UPDATE;
```

한 가지 주의할 사항은 FOR UPDATE나 FOR SHARE 절을 가지지 않는 SELECT 쿼리의 작동 방식이다. InnoDB 스토리지 엔진을 사용하는 테이블에서는 잠금 없는 읽기가 지원되기 때문에 특정 레코드가 `SELECT ... FOR UPDATE` 쿼리에 의해 잠겨진 상태라 하더라도 FOR SHARE나 FOR UPDATE 절을 가지지 않은 단순 SELECT 쿼리는 아무런 대기 없이 실행된다.

### 잠금 테이블 선택
```sql
SELECT *
FROM employees e
  INNER JOIN dept_emp de ON de.emp_no = e.emp_no
  INNER JOIN departments e ON d.dept_no = de.dept_no
FOR UPDATE;
```

위 쿼리는 employees 테이블과 dept_emp 테이블, departments 테이블을 조인해서 읽으면서 FOR UPDATE 절을 사용했다. 그래서 **InnoDB 스토리지 엔진은 3개 테이블에서 읽은 레코드에 대해 모두 쓰기 잠금(Exclusive Lock)을 걸게 된다.**

그런데, dept_emp, departments 테이블은 그냥 참고용으로만 읽고, 실제 쓰기 잠금은 employees 테이블에만 걸고 싶다면?

MySQL 8.0 버전부터는 잠금을 걸 테이블을 선택할 수 있도록 기능이 개선됐다. FOR UPDATE 뒤에 `OF 테이블` 절을 추가하면 해당 테이블에 대해서만 잠금을 걸게 된다.

```sql
SELECT *
FROM employees e
  INNER JOIN dept_emp de ON de.emp_no = e.emp_no
  INNER JOIN departments d ON d.dept_no = de.dept_no
WHERE e.emp_no = 10001
FOR UPDATE OF e;
```

### NOWAIT & SKIP LOCKED
MySQL 8.0 버전부터는 NOWAIT와 SKIP LOCKED 옵션을 사용할 수 있게 기능이 추가됐다. 지금까지의 MySQL 잠금은 누군가가 레코드를 잠그고 있다면 다른 트랜잭션은 그 잠금이 해제될 때까지 기다려야 했다. 때로는 일정 시간이 지나면 잠금 획득 실패 에러 메시지를 받을 수도 있었다. 새로 추가된 해당 옵션은 다음과 같다.

- NOWAIT : 쿼리를 실행 후 읽으려는 row에 lock이 걸려있으면 바로 트랜잭션 실패 처리 (innodb_lock_wait_timeout 만큼 기다리지 않고 바로)
- SKIP LOCKED : 쿼리를 실행 후 읽으려는 row에 lock 이 걸려있으면 해당 row skip 하고 resultset return (이 절을 사용한 SELECT 구문은 확정적이지 않은(NOT-DETERMINISTIC) 쿼리가 됨)

동시성 이슈를 해결하기 위해 select ~ for update , select ~ for shared mode 같은 쿼리를 수행할 때 위 옵션을 줄 수 있는데 언제 사용하면 좋을까? 보통은 다음과 같다.

- batch 작업 등으로 테이블 내 데이터를 일괄 변경할 때
- 티켓 예매 서비스 처럼 사용자들이 동시에 몰리는 데이터를 다룰 때

자세한 예제는 다음 링크를 참고하자.  
// https://kimdubi.github.io/mysql/skip_locked/

MySQL 8.0 이전 버전에서는 동시성 문제를 해결하기 위해 Redis나 Memcached같은 캐시 솔루션을 별도로 구축해서 쿠폰 발급 기능 같은 동시에 요청이 몰리는 기능을 구현했다. 하지만, FOR UPDATE SKIP LOCKED 절을 사용하면 트랜잭션이 수행되는 데 걸리는 시간과 관계없이 다른 트랜잭션에 의해 이미 사용 중인(잠겨진) 레코드를 스킵하는 시간만 지나면 각자의 트랜잭션을 실행할 수 있다. 1000건의 쿠폰을 가정한다면, MySQL 서버에서 1000건의(가장 마지막 트랜잭션이 잠금을 획득하기 위해 스킵해야 할 레코드 건수) 레코드를 스캔하는 데 걸리는 매우 짧다. 이 절을 사용한다면 1000개의 트랜잭션을 동시에 처리하게 되는 효과를 얻을 수도 있다.  
SKIP LOCKED 절 없이 FOR UPDATE만 사용한 경우에는 동시에 유입된 트랜잭션이 모두 잠금 대기를 하고 있다가 첫 번째 레코드를 잠금 트랜잭션이 완료돼야 비로소 두 번째 트랜잭션이 시작될 수 있고, 이후 트랜잭션들도 마찬가지이다. 아무리 MySQL 서버가 많은 CPU와 메모리를 가지고 있다 하더라도 이처럼 처리가 순차적으로 된다면 서버의 남는 자원을 제대로 활용하지 못한다.

참고로, NOWAIT와 SKIP LOCKED 절은 SELECT ... FOR UPDATE 구문에서만 사용할 수 있으며, 당연히 UPDATE, DELETE 쿼리에선 사용할 수 없다. 쿼리 자체를 비확정적으로 만드는 특성 때문에 UPDATE, DELETE 문에서 사용된다면 실행될 때마다 데이터베이스의 상태를 다른 결과로 만들게 되는 문제가 발생하기 때문이다.