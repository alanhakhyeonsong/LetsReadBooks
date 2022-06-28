# 1장. MySQL과 MariaDB 개요
오픈소스인 MySQL 및 MariaDB와 국내 시장 점유율이 가장 높은 상용DB인 Oracle을 기준으로 구조와 기능, SQL 문을 간단히 비교해보자.

## 구조적 차이
기본적으로 데이터가 저장되는 스토리지의 구조 측면에서 큰 차이가 있다.
- Oracle: 통합된 스토리지 하나를 공유하여 사용함.
- MySQL: 물리적인 DB 서버마다 독립적으로 스토리지를 할당하여 구성함.

![](https://velog.velcdn.com/images/songs4805/post/2e39c68f-bebb-41b2-a688-ea4bf7e0bd02/image.png)

Oracle은 사용자가 어느 DB 서버에 접속하여 SQL 문을 수행하더라도 같은 결과를 출력하거나 동일한 구문(`SELECT`, `INSERT`, `DELETE`, `UPDATE`)을 처리할 수 있다.

**MySQL은 독립적인 스토리지 할당에 기반을 두는 만큼 이중화를 위한 클러스터나 복제 구성으로 운영하더라도 보통은 master-salve 구조(주-종 구조)가 대부분이다.** **마스터 노드는 쓰기/읽기 처리를 모두 수행할 수 있고, 슬레이브 노드는 읽기 처리만 수행할 수 있다.**  
다시 말해, 물리적으로 여러 대의 MySQL DB 서버에 접속하더라도 동일한 구문(`SELECT`, `INSERT`, `DELETE`, `UPDATE`)이 처리되지 않을 수 있으며 DB 서버마다 각자의 역할이 부여될 수 있다.  
위 그림은 마스터-슬레이브 구조로 구축한 두 대의 MySQL 서버를 보여주는데, **쿼리 오프로딩**이 적용되어 있다. 그 결과 마스터 노드에서는 `INSERT`, `DELETE`, `UPDATE` 문을 수행하고, 슬레이브 노드에서는 `SELECT` 문을 수행한다.

쿼리가 수행되는 물리적인 서버의 위치를 인지하고 쿼리 튜닝을 수행해야 할 필요가 있다. 구축된 DB 서버의 구조를 충분히 이해하고 적합한 서버에 접근하여 쿼리 튜닝을 수행해야 한다는 말이다.

> 📌 쿼리 오프로딩(query offloading)?  
> DB 서버의 트랜잭션에서 쓰기 트랜잭션과 읽기 트랜잭션을 분리하여 DB 처리량을 증가시키는 성능 향상 기법이다.
> - 쓰기 트랜잭션: `INSERT`, `DELETE`, `UPDATE`
> - 읽기 트랜잭션: `SELECT`

## 지원 기능 차이
먼저, 조인 알고리즘의 기능에 차이가 있다.
- MySQL: 대부분 중첩 루프 조인 방식으로 조인을 수행
- Oracle: 중첩 루프 조인 방식, 정렬 병합 조인, 해시 조인 방식 사용함

다음으로, **MySQL은 Oracle과 달리 데이터를 저장하는 스토리지 엔진이라는 개념을 포함하므로 오픈소스 DBMS를 바로 꽂아서 사용할 수 있는 확장성이 특징이다.**

마지막으로 메모리 오버헤드에 차이가 있다.
- MySQL: 메모리 사용률이 Oracle에 비해 상대적으로 낮아 사양이 낮은 컴퓨팅 환경에서도 설치하여 서비스할 수 있다. (1MB의 메모리 환경에서도 가능할 만큼 오버헤드가 작음)
- Oracle: 최소 수백 MB의 메모리 환경이 제공되어야 설치할 수 있다.

## SQL 구문 차이
### Null 대체
어떤 열을 조회하고자 할 때, 해당 열이 Null 값도 포함할 수 있으므로 해당 열에 Null이 포함될 때는 다른 값으로 대체하려 한다.
- MySQL/MariaDB
```sql
# IFNULL(열명, '대쳇값')

SELECT IFNULL(col1, 'N/A') col1 FROM tab;
```

- Oracle
```sql
# NVL(열명, '대쳇값')

SELECT NVL(col1, 'N/A') col1 FROM tab;
```

### 페이징 처리
테이블에서 데이터를 불러올 경우 전체가 아닌 일부 분량만 제한적으로 가져올 때가 있다. 해당 키워드는 페이징 처리뿐만 아니라 새 일련번호를 받는 순번을 생성할 때도 응용하여 사용할 수 있다.
- MySQL/MariaDB
```sql
# LIMIT 숫자

SELECT col1 FROM tab LIMIT 5;
```

- Oracle
```sql
# ROWNUM <= 숫자

SELECT col1 FROM tab WHERE ROWNUM <= 5;
```

### 현재 날짜
DBMS에서 현재 시스템 날짜를 조회할 때 사용한다.  
MySQL에선 출력의 가독성을 높이고자 date라는 문자로 별칭을 부여하며, Oracle에선 가상 테이블인 dual을 사용해 `SELECT` 문을 작성한다. MySQL은 `FROM` 절 없이 `SELECT` 문만으로도 현재 날짜를 출력할 수 있지만 Oracle은 이처럼 가상 테이블을 명시해야 현재 날짜를 출력할 수 있다.
- MySQL/MariaDB
```sql
# NOW()

SELECT NOW() AS date;
```

- Oracle
```sql
# SYSDATE

SELECT SYSDATE AS date FROM dual;
```

### 조건문
- MySQL/MariaDB: `IF` 문과 `CASE WHEN~THEN` 문 사용
```sql
# IF (조건식, '참값', '거짓값')

SELECT IF(col1='A', 'apple', '-') AS col1 FROM tab;
```

- Oracle: `DECODE` 키워드, `IF` 문, `CASE WHEN~THEN` 문 사용
```sql
# DECODE(열명, '값', '참값', '거짓값')

SELECT DECODE(col1, 'A', 'apple', '-') AS col1
```

### 날짜 형식
날짜 데이터를 원하는 형태로 변경하는 구문을 작성할 수 있다.
- MySQL/MariaDB
```sql
# DATE_FORMAT(날짜열, '형식')

SELECT DATE_FORMAT(NOW(), '%Y%m%d %H%i%s') AS date;
```

- Oracle
```sql
# TO_CHAR(날짜열, '형식')

SELECT TO_CHAR(SYSDATE, 'YYYYMMDD HH24MISS') AS date FROM DUAL;
```

### 자동 증갓값
신규 데이터가 지속해 생성될 때는 증가하는 순번을 자동으로 매기는 숫자형 값, 즉 자동 증갓값을 저장할 수 있다. 이미 저장된 다른 데이터와 값이 중복되지 않도록 기존에 저장된 순번보다 더 큰 숫자를 생성하여 데이터를 저장하는 방식이다.

- MySQL/MariaDB
```sql
# 테이블마다 특정한 하나의 열에만 해당 속성을 정의하여
# 자동 증가하는 값을 설정하는 auto_increment를 명시한다.
# AUTO_INCREMENT

CREATE TABLE tab
(seq   INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
 title VARCHAR(20) NOT NULL);
```

- MariaDB 10.3 이상
```sql
# 시퀀스 오브젝트를 생성한 뒤 호출하여 활용하는 방법

CREATE SEQUENCE [시퀀스명]
INCREMENT BY [증감숫자]
START WITH [시작숫자]
NOMINVALUE OR MINVALUE [최솟값]
NOMAXVALUE OR MAXVALUE [최댓값]
CYCLE OR NOCYCLE
CACHE OR NOCACHE

# 다음 값 채번 문법
SELECT NEXTVAL(시퀀스명);
```

- Oracle
```sql
CREATE SEQUENCE [시퀀스명]
INCREMENT BY [증감숫자]
START WITH [시작숫자]
NOMINVALUE OR MINVALUE [최솟값]
NOMAXVALUE OR MAXVALUE [최댓값]
CYCLE OR NOCYCLE
CACHE OR NOCACHE

# 다음 값 채번 문법
SELECT 시퀀스명.NEXTVAL FROM dual;
```

### 문자 결합
여러 개의 문자를 하나로 결합하여 조회할 때가 있다.  
A와 B라는 두 개의 문자를 결합하는 예제는 다음과 같다.
- MySQL/MariaDB
```sql
# CONCAT(열값 또는 문자열, 열값 또는 문자열)

SELECT CONCAT('A', 'B') TEXT;
```

- Oracle
```sql
# 1. 열값 또는 문자열 || 열값 또는 문자열
# 2. CONCAT(열값 또는 문자열, 열값 또는 문자열)

SELECT 'A'||'B' TEXT FROM DUAL;
SELECT CONCAT('A', 'B') TEXT;
```

### 문자 추출
문자열에서 특정 구간 및 특정 위치의 문자열을 추출하고 싶을 때 사용한다.  
ABCDE라는 문자열에서 두 번째 위치의 문자부터 시작해서 3개의 문자를 가져오는 예제이다.

- MySQL/MariaDB
```sql
# SUBSTRING(열값 또는 문자열, 시작 위치, 추출하려는 문자 개수)

SELECT SUBSTRING('ABCDE', 2, 3) AS sub_string;
```

- Oracle
```sql
# SUBSTR(열값 또는 문자열, 시작 위치, 추출하려는 문자 개수)

SELECT SUBSTR('ABCDE', 2, 3) AS sub_string FROM DUAL;
```