# 15장. 데이터 타입
칼럼의 데이터 타입을 선정하는 작업은 물리 모델링에서 빼놓을 수 없는 중요한 작업이다. 데이터 타입과 길이를 선정할 때 가장 주의할 사항은 다음과 같다.

- 저장되는 값의 성격에 맞는 최적의 타입을 선정
- 가변 길이 칼럼은 최적의 길이를 지정
- 조인 조건으로 사용되는 칼럼은 똑같은 데이터 타입으로 선정

무분별하게 칼럼의 길이가 크게 선정되면 디스크의 공간은 물론 메모리나 CPU의 자원도 함께 낭비된다. 또한 그로 인해 SQL의 성능이 저하되는 것은 당연한 결과다.

## 문자열(CHAR와 VARCHAR)
### 저장 공간
- 공통점: 문자열을 저장할 수 있는 데이터 타입
- 차이점: 고정 길이 vs 가변 길이
  - 고정 길이: 실제 입력되는 칼럼값의 길이에 따라 저장 공간의 크기가 변하지 않는다. CHAR 타입은 이미 저장 공간의 크기가 고정적이다. 실제 저장된 값의 유효 크기가 얼마인지 별도로 저장할 필요가 없어 추가로 공간이 필요하지 않다.
  - 가변 길이: 최대로 저장할 수 있는 값의 길이는 제한돼 있지만, 그 이하 크기의 값이 저장되면 그만큼 저장 공간이 줄어든다. 하지만 VARCHAR 타입은 저장된 값의 유효 크기가 얼마인지를 별도로 저장해 둬야 하므로 1~2 바이트의 저장 공간이 추가로 더 필요하다.

CHAR 타입과 VARCHAR 타입을 결정할 때 중요한 판단 기준은 다음과 같다.

- 저장되는 문자열의 길이가 대개 비슷한가?
- 킬럼의 값이 자주 변경되는가?

CHAR(10), VARCHAR(10)으로 타입을 정해뒀을 때, 이 길이보다 작게 데이터를 저장했다 길이를 좀 더 늘리게 된다면 CHAR의 경우, 이미 10바이트의 공간이 준비돼 있으므로 그냥 변경되는 칼럼의 값을 업데이트만 하면 된다. 반면, VARCHAR의 경우 "ABCD"처럼 4바이트를 초기에 저장해두고 "ABCDE"로 값이 변경된다면, **레코드 자체를 다른 공간으로 옮겨 저장해야 한다.**

- 주민등록번호처럼 항상 값의 길이가 고정적일 때는 당연히 CHAR 타입을 사용해야 한다.
- 값이 2~3 바이트씩 차이가 나더라도 자주 변경될 수 있는 값들 역시 CHAR 타입을 사용하는 것이 좋다.
  - 자주 변경되더라도 레코드가 물리적으로 다른 위치로 이동하거나 분리되지 않아도 되기 때문이다.

### 저장 공간과 스키마 변경(Online DDL)
MySQL 서버에선 데이터가 변경되는 도중에도 스키마 변경을 할 수 있도록 Online DDL 기능을 제공한다.

VARCHAR(60)으로 지정된 컬럼을 63, 64로 각각 늘려보면 결과는 다음과 같다.

```bash
mysql> ALTER TABLE test MODIFY value(63), ALGORITHM=INPLACE, LOCK=NONE;
Query OK, 0 rows affected (0.00 sec)

mysql> ALTER TABLE test MODIFY value(64), ALGORITHM=INPLACE, LOCK=NONE;
ERROR 1846 (0A000): ALGORITHM=INPLACE is not supported. Reason: Cannot change column type INPLACE. Try ALGORITHM=COPY.
```

VARCHAR 타입의 칼럼이 가지는 길이 저장 공간의 크기 때문에 위와 같은 차이가 발생한다. `utf8mb4` 문자 집합을 사용하는 VARCHAR(60) 칼럼은 최대 길이가 240(60 * 4) 바이트이기 때문에 문자열 값의 길이를 저장하는 공간은 1바이트면 된다. 하지만 VARCHAR(64) 타입은 저장할 수 있는 문자열의 크기가 최대 256바이트까지 가능하기 때문에 문자열 길이를 저장하는 공간의 크기가 2바이트로 바뀌어야 한다. 이렇게 되면 MySQL 서버는 스키마 변경을 하는 동안 읽기 잠금을 걸어 아무도 데이터를 변경하지 못하도록 막고 테이블의 레코드를 복사하는 방식으로 처리한다.

### 문자 집합(캐릭터 셋)

웹 서비스나 스마트폰 앱은 여러 나라의 언어를 동시에 지원하기 위해 기본적으로 UTF-8 문자 집합(`utf8mb4`)을 사용하는 추세다.

MySQL 서버에서 사용 가능한 문자 집합은 `SHOW CHARACTER SET` 명령으로 확인해 볼 수 있다.

### 콜레이션(Collation)
콜레이션은 문자열 칼럼의 값에 대한 비교나 정렬 순서를 위한 규칙을 의미한다. 즉, 비교나 정렬 작업에서 영문 대소문자를 같은 것으로 처리할지, 아니면 더 크거나 작은 것으로 판단할지에 대한 규칙을 정의하는 것이다.

MySQL의 모든 문자열 타입의 칼럼은 독립적인 문자 집합과 콜레이션을 가진다. 각 칼럼에 대해 독립적으로 문자 집합이나 콜레이션을 지정하든 그렇지 않든 독립적인 문자 집합과 콜레이션을 가지는 것이다.

문자 집합은 2개 이상의 콜레이션을 가지고 있는데, 하나의 문자 집합에 속한 콜레이션은 다른 문자 집합과 공유해서 사용할 수 없다. 또한 테이블이나 칼럼에 문자 집합만 지정하면 해당 문자 집합의 디폴트 콜레이션이 해당 칼럼의 콜레이션으로 지정된다. MySQL 서버에서 사용 가능한 콜레이션의 목록은 `SHOW COLLATION` 명령을 이용해 확인할 수 있다.

콜레이션에 대한 자세한 내용은 책을 통해 확인하자.

### 비교 방식
MySQL에서 문자열 칼럼을 비교하는 방식은 CHAR와 VARCHAR가 거의 같다.

- CHAR 타입의 칼럼에 SELECT를 실행했을 때 다른 DBMS처럼 사용되지 않는 공간에 공백 문자가 채워져서 나오지 않는다.
- MySQL 서버에서 지원하는 대부분의 문자 집합과 콜레이션에서 CHAR 타입이나 VARCHAR 타입을 비교할 때 공백 문자를 뒤에 붙여 두 문자열의 길이를 동일하게 만든 후 비교를 수행한다.

하지만 `utf8mb4` 문자 집합이 UCA 버전 9.0.0을 지원하면서 문자열 뒤에 붙어있는 공백 문자들에 대한 비교 방식이 달라졌다.

문자열 비교의 경우 예외적으로 `LIKE`를 사용한 문자열 패턴 비교에선 공백 문자가 유효 문자로 취급된다. 검색어 앞뒤로 와일드 카드 문자를 사용하는 것도 유의해야 한다. MySQL의 독특한 문자열 비교 방식은 특히 회원의 아이디나 닉네임과 같이 다른 DBMS와 연동해야 하는 서비스에서 문제가 되곤 하므로 주의해야 한다.

### 문자열 이스케이프 처리