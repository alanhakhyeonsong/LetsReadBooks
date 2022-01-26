# 아이템 9. try-finally보다는 try-with-resources를 사용하라

자바 라이브러리에는 `close()` 메서드를 호출해 직접 닫아줘야 하는 자원이 많다.  
`InputStream`, `OutputStream`, `java.sql.Connection` 등이 좋은 예시이다.

자원 닫기는 클라이언트가 놓치기 쉬워서 예측할 수 없는 성능 문제로 이어지기도 한다.  
상당수가 `finalizer`를 활용하고 있지만 언제 닫힐지 모르기 때문에 믿을만하지 못하다.

전통적으로 자원이 제대로 닫힘을 보장하는 수단으로 `try-finally`가 쓰였다.

```java
static String firstLineOfFile(String path) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(path));
    try {
        return br.readLine();
    } finally {
        br.close();
    }
}
```

`readLine()`에서 예외가 발생한다면, `br.close()`가 실패한다.

이 경우, 첫 번째에서 발생한 에러가 두번째(`br.close()`)에서 발생한 에러에 의해 잡아 먹히므로, 궁극적으로 어디서 발생한 에러인지 알기 어렵다. 스택 추적 내역에 첫 번째 예외에 관한 정보는 남지 않게 되어, 실제 시스템에서의 디버깅을 몹시 어렵게 한다.

이러한 문제를 해결하고자 `try-with-resouces`가 Java 7에서 등장하였다. 이 구조를 사용하려면 해당 자원이 `AutoCloseable` 인터페이스를 구현해야 한다.  
// 단순히 void를 반환하는 `close()`메서드 하나만 덩그러니 정의한 인터페이스임

자바 라이브러리와 서드파티 라이브러리들의 수많은 클래스와 인터페이스가 이미 이를 구현하거나 확장해뒀다. 만약 반드시 닫혀야하는 자원이라면 위와 같이 해당 인터페이스를 반드시 구현/확장해야 한다.

```java
static String firstLineOfFile(String path, String defaultval) {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine();
    } catch (IOException e) {
        return defaultVal;
    }
}
```

위 코드 처럼 `try-with-resources`에서 `catch`도 사용할 수 있다. 이렇게 처리하면 try 문을 더 중첩하지 않고도 다수의 예외를 처리할 수 있다는 장점이 있다.

// [try-with-resources 참고 자료(Baeldung)](https://www.baeldung.com/java-try-with-resources)

## 핵심 정리

- 꼭 회수해야 하는 자원을 다룰 때는 `try-finally` 말고, `try-with-resources`를 사용하자.
- 코드는 더 짧고 분명해지며, 만들어지는 예외 정보도 훨씬 유용하게 된다.
- `try-finally`로 작성하면 실용적이지 못할 만큼 코드가 지저분해지는 경우라도, `try-with-resources`로는 정확하고 쉽게 자원을 회수할 수 있다.
