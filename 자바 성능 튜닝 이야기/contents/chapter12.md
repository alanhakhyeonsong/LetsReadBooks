# Story 12. DB를 사용하면서 발생 가능한 문제점들
자바 기반 애플리케이션의 성능을 진단해보면, **애플리케이션의 응답 속도를 지연시키는 대부분의 요인은 DB 쿼리 수행 시간과 결과를 처리하는 시간**이다. 애플리케이션 레벨에서 흔히 문제를 일으키는 사례는 다음과 같다.

- DB connection을 할 경우 공통 유틸리티를 사용하지 않은 경우
- 각 모듈별 `DataSource`를 사용하지 않아 리소스가 부족한 현상이 발생한 경우
- `Connection`, `Statement` 관련 객체, `ResultSet`을 close 하지 않은 경우
- 페이지 처리를 위해 `ResultSet`의 `last()` 메서드를 사용한 경우

## DB Connection과 Connection Pool, DataSource
우리가 사용하는 JDBC 관련 API는 클래스가 아니라 인터페이스다. JDK의 API에 있는 `java.sql` 인터페이스를 각 DB 벤더에서 상황에 맞게 구현하도록 되어 있다. 같은 인터페이스라 해도, 각 DB 벤더에 따라 처리되는 속도나 내부 방식은 상이하다.

예전 기술이긴 하지만, 일반적으로 DB에 연결하여 사용하는 예제는 다음과 같다.

```java
try {
    Class.forName("oracle.jdbc.driver.OracleDriver");
    Connection con = DriverManager.getConnection(
      "jdbc:oracle:thin:@ServerIP:1521:SID", "ID", "Password"
    );

    PreparedStatement ps = con.prepareStatement("SELECT ... where id=?");
    ps.setString(1, id);
    ResultSet rs = ps.executeQuery();
    // ...
} catch (ClassNotFoundException e) {
    // driver load fail
    throw e;
} catch (SQLException e) {
    // connection fail
    throw e;
} finally {
    rs.close();
    ps.close();
    con.close();
}
```

1. 드라이버를 로드한다.
2. DB 서버의 IP와 ID,PW 등을 `DriverManager` 클래스의 `getConnection()`을 사용하여 `Connection` 객체로 만든다.
3. `Connection`으로부터 `PreparedStatement` 객체를 받는다.
4. `executeQuery()`를 수행하여 그 결과로 `ResultSet`을 받아 데이터를 처리한다.
5. 모든 데이터를 처리한 이후에는 `finally` 구믄을 사용하여 `ResultSet`, `PreparedStatement`, `Connection` 객체들을 닫는다. 각 객체를 close 할 때 예외가 발생할 수 있으므로, 해당 메서드에선 예외를 던지도록 처리해 둬야 한다.

만약 위 예제처럼 구성되어 있을 때 쿼리가 0.1초 소요된다면, 가장 느린 부분은 `Connection` 객체를 얻는 부분이다. 같은 장비에 DB가 구성되어 있다 하더라도 DB와 WAS 사이에는 통신을 해야하기 때문이다. 게다가 사용자가 갑자기 증가하면 `Connection`을 얻기 위한 시간 또한 엄청나게 소요될 것이며, 많은 화면이 예외를 발생시킬 것이다.

`Connection` 객체를 생성하는 부분에서 발생하는 대기 시간을 줄이고, 네트워크의 부담을 줄이기 위해 사용하는 것이 DB Connection Pool이다. 요즘은 대부분의 WAS에서 커넥션 풀을 제공하고, `DataSource`를 사용하여 JNDI로 호출해 쓸 수 있기 때문에 가능하면 안정되고 검증된 WAS에서 제공하는 것을 사용하자.

`Statement`와 `PreparedStatement`의 가장 큰 차이점은 캐시 사용 여부다. `Statement`를 사용할 때와 `PreparedStatement`를 처음 사용할 때는 다음과 같은 프로세스를 거친다.

1. 쿼리 문장 분석
2. 컴파일
3. 실행

전자를 사용할 경우, 매번 쿼리를 수행할 때마다 모든 단계를 거치게 되고, `PreparedStatement`는 처음 한 번만 세 단계를 거친 후 캐시에 담아서 재사용을 한다. 동일한 쿼리를 반복적으로 수행한다면 `PreparedStatement`가 DB에 훨씬 적은 부하를 주며, 성능도 좋다. 게다가 쿼리에서의 변수를 `?`로 처리하기 때문에 가독성도 좋아진다.

`Statement` 관련 인터페이스에서 쿼리를 수행하는 메서드의 특징을 살펴보자.

- `executeQuery()`: select 관련 쿼리를 수행하며, 결과로 요청한 데이터 값이 `ResultSet` 형태로 전달된다.
- `executeUpdate()`: select 관련 쿼리를 제외한 DML 및 DDL 쿼리를 수행한다. 결과는 `int` 형태로 리턴된다.
- `execute()`: 쿼리의 종류와 상관 없이 쿼리를 수행한다. `execute()` 메서드의 수행 결과는 `boolean` 형태의 데이터를 리턴하는데, 만약 데이터가 있을 경우 `true`를 리턴하여 `getResultSet()`을 사용하여 결과 값을 받을 수 있다. `false`일 경우 변경된 행의 개수를 확인하기 위해선 `getUpdateCount()`를 사용하여 값을 확인하면 된다.

쿼리를 수행한 결과는 `ResultSet` 인터페이스에 담기는데, 여러 건의 데이터가 넘어오기 때문에 `next()`를 사용해 데이터의 커서를 다음으로 옮기면서 처리한다. 데이터를 읽어오기 위해서는 `get`으로 시작하는 네이밍의 메서드를 사용하면 된다.

## DB를 사용할 때 당아야 하는 것들
각 객체를 얻는 순서는 `Connection` → `Statement` → `ResultSet` 순이며, 닫는 순서는 역순이다.

`ResultSet` 객체가 닫히는 경우는 다음과 같다.

- `close()` 메서드를 호출
- GC의 대상이 되어 GC되는 경우
- 관련된 `Statement` 객체의 `close()`가 호출되는 경우

`ResultSet`의 `close()`를 호출하는 이유는 자동으로 호출되기 전에 관련된 DB와 JDBC 리소스를 해제하기 위함인데, 찰나의 순간이라도 빨리 닫으면 그만큼 해당 DB 서버의 부담이 적어지게 된다.

`Statement` 역시 비슷하다. 반드시 `close()`를 호출하자.

- `close()`를 호출
- GC의 대상이 되어 GC되는 경우

`Connection` 인터페이스는 다음 세 가지 경우에 닫힌다.

- `close()` 호출
- GC대상이 되어 GC되는 경우
- 치명적인 에러가 발생하는 경우

`Connection`은 대부분 커넥션 풀을 사용하여 관리된다. 시스템이 기동되면 지정된 개수만큼 연결하고, 필요할 때 증가시키도록 되어 있다. 증가되는 최대 값 또한 지정하도록 되어 있다. 사용자가 증가해 더 이상 사용할 수 잇는 연결이 없으면, 여우가 생길 때까지 대기한다. 그러다 어느 정도 시간이 지나면 오류가 발생한다. 따라서 `close()`를 통해 닫아야 한다. **GC가 될 때까지 기다리면 커넥션 풀이 부족해지는 것은 시간문제다.**

JDK 7에서 `AutoClosable` 인터페이스가 등장했는데, 이를 구현한 것인지 잘 확인해보고 그렇다면 `close()`를 일일이 호출하지 말고 `try-with-resource`를 활용하여 간단하고 확실하게 처리하자.

## ResultSet.last() 메서드
이 메서드를 사용하는 경우가 많이 있는데, 수행 시간은 데이터의 건수 및 DB와의 통신 속도에 따라서 굉장히 달라진다. 따라서 이 메서드의 사용은 자제해야 한다. 전체 데이터 개수를 확인하고 싶다면 이를 사용하지 말고 차라리 count 쿼리를 한 번 더 사용하자.

## JDBC를 사용하면서 유의할 만한 몇 가지 팁
- `setAutoCommit()`은 필요할 때만 사용하자.  
  이 메서드를 통해 자동 커밋 여부를 지정하는데, 단순한 select 작업만을 수행할 때도 사용하는 경우가 있다. 여러 개의 쿼리를 동시에 작업할 때 성능에 영향을 주게 되므로 되도록 자제하자.
- 배치성 작업은 `executeBatch()`를 사용하자.  
  `Statement` 인터페이스에 정의되어 있는 `addBatch()` 메서드를 사용하여 쿼리를 지정하고, `executeBatch()` 메서드를 사용하여 쿼리를 수행하자. 여러 개의 쿼리를 한 번에 수행할 수 있기 때문에 JDBC 호출 횟수가 감소되어 성능이 좋아진다.
- `setFetchSize()`를 사용하여 데이터를 더 빠르게 가져오자.  
  가져오는 데이터의 수가 정해져 있을 경우, 이 메서드를 사용하여 원하는 개수를 정의하자. 하지만 너무 많은 건수를 지정하면 서버에 많은 부하가 올 수 있으니 적절하게 사용해야 한다.
- 한 건만 필요할 때는 한 건만 가져오자.  
  실제 쿼리에선 100건 정도를 갖고 오는데, ,`ResultSet.next()`를 `while` 블록을 사용해서 수행하지 않고, 단 한 번만 메서드를 수행해 결과를 처리하는 경우가 있다. 이 경우 단 한 건만을 가져오도록 쿼리를 수정해야 한다.