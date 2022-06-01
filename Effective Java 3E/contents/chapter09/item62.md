# 아이템 62. 다른 타입이 적절하다면 문자열 사용을 피하라
문자열은 흔하고 Java가 잘 지원해주기에 원래 의도하지 않은 용도로도 쓰이는 경향이 있다. 이번 아이템은 **문자열을 쓰지 않아야 할 사례**를 다룬다.

1. **문자열은 다른 값 타입을 대신하기에 적합하지 않다.**  
입력받을 데이터가 진짜 문자열일 때만 문자열로 받고 수치형이라면 int, float, BigInteger 등 적당한 수치 타입으로 변환해야 한다.
2. **문자열은 열거 타입을 대신하기에 적합하지 않다.**
3. **문자열은 혼합 타입을 대신하기에 적합하지 않다.**

```java
// 혼합 타입을 문자열로 처리한 부적절한 예
String compoundKey = className + "#" + i.next();
```
위 예제는 각 요소를 개별로 접근하려면 문자열을 파싱해야 해서 느리고, 귀찮고, 오류 가능성도 커진다. 적절한 `equals`, `toString`, `compareTo` 메서드를 제공할 수 없으며, `String`이 제공하는 기능에만 의존해야 한다. 차라리 전용 클래스를 새로 만드는 편이 낫고 보통 private 정적 멤버 클래스로 선언한다.

4. **문자열은 권한을 표현하기에 적합하지 않다.**

```java
// 잘못된 예시 - 문자열을 사용해 권한을 구분하였다.
public class ThreadLocal {
    private ThreadLocal() { } // 객체 생성 불가

    // 현 스레드의 값을 키로 구분해 저장한다.
    public static void set(String key, Object value);

    // (키가 가리키는) 현 스레드의 값을 반환한다.
    public static Object get(String key);
}
```
위 방식의 문제는 스레드 구분용 문자열 키가 전역 이름공간에서 공유된다는 점이다. "A"라는 단어와 "A"라는 단어는 하나의 주소값을 가진다. 서로 다른 개체가 아니다. `new String("A")`와 같은 방식을 하지 않는다면 동일한 단어는 같은 개체이다. 따라서 위 예제는 전역 이름 공간에서 공유되므로 문제가 있다.

```java
// Key 클래스로 권한을 구분했다.
public class ThreadLocal {
    private ThreadLocal() { } // 객체 생성 불가

    public static class Key { // (권한)
        Key() { }
    }

    // 위조 불가능한 고유 키를 생성한다.
    public static Key getKey() {
        return new Key();
    }

    public static void set(Key key, Object value);
    public static Object get(Key key);
}
```
위 방식은 문자열 기반의 예제에서 나타나는 문제를 모두 해결해주지만 개선의 여지가 있다.

```java
// Key를 ThreadLocal로 변경
public final class ThreadLocal {
    public ThreadLocal();
    public void set(Object value);
    public Object get();
}
```

```java
// 매개변수화하여 타입안전성 확보
public final class ThreadLocal<T> {
    public ThreadLocal();
    public void set(T value);
    public T get();
}
```
이는 타입안전하고 Java의 `java.lang.ThreadLocal`과 흡사해졌다. 문자열 기반 API의 문제를 해결해주며, 키 기반 API보다 빠르고 우아하다.

## 핵심 정리
- 더 적합한 데이터 타입이 있거나 새로 작성할 수 있다면, 문자열을 쓰고 싶은 유혹을 뿌리쳐라.
- 문자열은 잘못 사용하면 번거롭고, 덜 유연하고, 느리고, 오류 가능성도 크다.
- 문자열을 잘못 사용하는 흔한 예로는 기본 타입, 열거 타입, 혼합 타입이 있다.