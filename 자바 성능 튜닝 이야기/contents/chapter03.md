# Story 3. 왜 자꾸 String을 쓰지 말라는 거야?
`String` 클래스는 잘 사용하면 상관이 없지만, 잘못 사용하면 메모리에 많은 영향을 준다.

## String 클래스를 잘못 사용한 사례
Java 기반의 프로그래밍을 할 때 `java.lang.Object`를 제외하고 가장 많이 사용하는 객체는 Primitive Type을 제외하면 `String` 클래스, `Collection` 관련 클래스다. 대부분의 웹 기반 시스템은 DB에서 데이터를 가져와 그 데이터를 화면으로 출력하는 시스템이기 대문에, 쿼리 문장을 만들기 위한 `String` 클래스와 결과를 처리하기 위한 `Collection` 클래스를 가장 많이 사용하게 된다.

MyBatis, Hibernate와 같은 데이터 매핑 프레임워크를 사용하기 이전엔 보통 쿼리를 다음과 같이 작성했다고 한다.

```java
String strSQL = "";
strSQL += "select * ";
strSQL += "from ( ";
strSQL += "select A_column, ";
strSQL += "B_column ,";
// ... (약 400 라인)
```

이렇게 쿼리를 작성하면, 개발 시에는 좀 편할지 몰라도 메모리를 많이 사용하게 된다는 문제가 있다. 위와 같은 패턴을 100회 수행한다는 가정하에 이 메서드를 한 번 수행하면, 몇 MB 메모리를 사용하는지 확인해본 결과 다음과 같이 나왔다고 한다.

- 메모리 사용량: 10회 평균 약 5MB
- 응답 시간: 10회 평균 약 5ms

이번엔 `StringBuilder`로 변경한 뒤 테스트 해보면 결과가 다음과 같이 바뀌었다고 한다.

```java
StringBuilder strSQL = new StringBuilder();
strSQL.append(" select * ");
strSQL.append(" from ( ");
strSQL.append(" select A_column, ");
strSQL.append(" B_column ,");
// ... (약 400 라인)
```

- 메모리 사용량: 10회 평균 약 371KB
- 응답 시간: 10회 평균 약 0.3ms

## StringBuffer 클래스와 StringBuilder 클래스
JDK 5.0을 기준으로 문자열을 만드는 클래스는 `String`, `StringBuffer`, `StringBuilder`가 가장 많이 사용된다. 여기서 `StringBuilder`는 JDK 5.0에서 새로 추가되었다.

- `StringBuffer`: ThreadSafe하게 설계되어 여러 스레드에서 하나의 `StringBuffer` 객체를 처리해도 전혀 문제가 되지 않는다.
- `StringBuilder`: 단일 스레드에서의 안전성만을 보장한다.

주로 사용하는 두 개의 메서드만 비교해보자.

- `append()`: 말 그대로 기존 값의 맨 끝 자리에 넘어온 값을 덧붙이는 작업을 수행
- `insert()`: 지정된 위치 이후에 넘어온 값을 덧붙이는 작업을 수행. 만약 지정한 위치까지 값이 할당되어 있지 않으면 `StringIndexOutObBoundsException`이 발생한다.

문자열을 더할 땐, 되도록이면 `append()`를 사용하도록 하자.

## String vs. StringBuffer vs. StringBuilder
왜 `append()` 메서드를 이용하여 문자열을 더해야 할까?

```jsp
<%
final String aValue = "abcde";
for (int i = 0; i < 10; i++) {
    String a = new String();
    StringBuffer b = new StringBuffer();
    StringBuilder c = new StringBuilder();
    for (int j = 0; j < 10000; j++) a += aValue;

    for (int j = 0; j < 10000; j++) b.append(aValue);

    String temp = b.toString();

    for (int j = 0; j < 10000; j++) c.append(aValue);

    String temp2 = c.toString();
}
%>
OK
<%= System.currentTimeMillis() %>
```

소스를 JSP로 만든 이유는 이 코드를 java 파일로 만들어 반복 작업을 수행할 경우, 클래스를 메모리로 로딩하는 데 소요되는 시간이 발생하기 때문이다. 그래서 JSP로 만들어 최초에 이 화면을 호출했을 때의 응답 시간 및 메모리 사용량은 측정에서 제외하고, 두 번째 호출한 내용부터 10회 반복 수행한 결과의 누적 값을 구한다.

셋 중 어느 것이 가장 빠르고 메모리를 적게 사용할까?

- 10,000회 반복하여 문자열을 더하고, 이러한 작업을 10회 반복한다.
- 이 화면을 10회 반복 호출한다.

그러므로 각 문자열을 더하는 라인은 총 100만 번씩 수행된다. 프로파일링 툴을 사용하여 실행한 결과는 다음과 같다.

|주요 소스 부분|응답 시간(ms)|비고|
|--|--|--|
|`a += aValue;`|95,801.41ms|95초|
|`b.append(aValue);`|247.48ms|0.24초|
|`String temp = b.toString();`|14.21ms||
|`c.append(aValue);`|174.17ms|0.17초|
|`String temp2 = c.toString();`|13.38ms||

메모리 사용량을 보자.

|주요 소스 부분|메모리 사용량(bytes)|생성된 임시 객체수|비고|
|--|--|--|--|
|`a += aValue;`|100,102,000,000|4,000,000|약 95GB|
|`b.append(aValue);`|29,493,600|1,200|약 28MB|
|`String temp = b.toString();`|10,004,000|200|약 9.5MB|
|`c.append(aValue);`|29,493,600|1,200|약 28MB|
|`String temp2 = c.toString();`|10,004,000|200|약 9.5MB|

이러한 결과가 왜 발생하는지 알아보자.

`a += aValue;`

이 소스 라인이 수행되면 어떻게 될까? `a`에 `aValue`를 더하면 새로운 `String` 클래스의 객체가 만들어지고, 이전에 있던 `a` 객체는 필요 없는 쓰레기 값이 되어 GC 대상이 되어 버린다.

이러한 작업이 반복 수행되면서 메모리를 많이 사용하게 되고, 응답 속도에도 많은 영향을 미치게 된다. GC를 하면 할수록 시스템의 CPU를 사용하게 되고 시간도 많이 소요된다. 그래서 프로그래밍을 할 때, 메모리 사용을 최소화하는 것은 당연한 일이다.

`StringBuffer`나 `StringBuilder`는 `String`과는 다르게 새로운 객체를 생성하지 않고, 기존에 있는 객체의 크기를 증가시키면서 값을 더한다.

- `String`은 짧은 문자열을 더할 경우 사용한다.
- `StringBuffer`는 스레드 안전한 프로그램이 필요할 때나, 개발 중인 시스템의 부분이 스레드에 안전한지 모를 경우 사용하면 좋다. 만약 클래스에 `static`으로 선언한 문자열을 변경하거나, singleton으로 선언된 클래스에 선언된 문자열일 경우에는 이 클래스를 사용해야만 한다.
- `StringBuilder`는 스레드에 안전한지의 여부와 전혀 관계 없는 프로그램을 개발할 때 사용하면 좋다. 만약 메서드 내에 변수를 선언했다면, 해당 변수는 그 메서드 내에서만 살아있으므로 `StringBuilder`를 사용하면 된다.

## 버전에 따른 차이
만약 JDK 5.0 이상을 사용한다면 결과가 약간 달라진다. 아래와 같은 예제가 있다 해보자.

```java
public class VersionTest {
    String str = "Here " + "is " + "a " + "sample.";
    public VersionTest() {
        int i = 1;
        String str2 = "Here " + "is " + i + "smaples.";
    }
}
```

JDK 1.4를 사용해서 컴파일해 보자. 일반적으로 많이 쓰이는 JAD를 사용하여 역 컴파일한 소스는 다음과 같다.

```java
public class VersionTest {
    public VersionTest() {
        str = "Here is a sample.";
        int i = 1;
        String s = "Here " + "is " + i + "smaples.";
    }
    String str;
}
```

역 컴파일한 소스를 보면 Java 컴파일러가 문자열을 더한 것을 컴파일할 때 알아서 더해 놓고 있다. 중간에 int나 다른 객체가 들어가게 되면 위와 같이 그대로 더하도록 되어 있다. 어차피 필요 없는 객체는 생성이 된다는 의미이다.

JDK 5.0에선 어떻게 달라졌을까?

```java
public class VersionTest {
    public VersionTest() {
        str = "Here is a sample.";
        int i = 1;
        String str2 = (new StringBuilder("Here is "))
        .append(i).append(" samples.").toString();
    }
    String str;
}
```

만약 우리가 문자열을 그냥 더하도록 프로그래밍 했다면, 컴파일 할 때 위와 같이 변환된다. 개발자의 실수를 어느 정도는 피할 수 있게 된다는 것이다.