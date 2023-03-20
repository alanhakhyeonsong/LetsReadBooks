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