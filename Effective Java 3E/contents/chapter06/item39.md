# 아이템 39. 명명 패턴보다 애너테이션을 사용하라

## 명명 패턴

전통적으로 도구나 프레임워크가 특별히 다뤄야 할 프로그램 요소에는 딱 구분되는 **명명 패턴**을 적용해왔다.  
명명 패턴은 변수, 메서드의 이름을 일관된 방식으로 작성하는 패턴을 말한다.

JUnit은 버전 3까지 테스트 메서드 이름을 `test`로 시작하게끔 했다.

하지만 다음과 같은 단점들이 있다.

- 오타가 나면 안된다.
- 올바른 프로그램 요소에만 사용되리라 보증할 방법이 없다.
- 프로그램 요소를 매개변수로 전달할 마땅한 방법이 없다. (특정 예외를 던져야 성공하는 테스트 등)

따라서 애너테이션을 사용하도록 하자.

## 애너테이션

애너테이션은 명명 패턴의 모든 문제를 해결해주는 개념으로, JUnit도 버전 4부터 전면 도입하였다.

```java
import java.lang.annotation.*;

/**
 * 테스트 메서드임을 선언하는 애너테이션이다.
 * 매개변수 없는 정적 메서드 전용이다.
**/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
}
```

`@Test` 애너테이션 타입 선언 자체에도 두 가지의 다른 애너테이션이 달려 있다. 바로 `@Retention`, `@Target`이다.  
이처럼 **애너테이션 선언에 다는 애너테이션을 매타 애너테이션(meta-annotation)이라 한다.**

`@Retention(RetentionPolicy.RUNTIME)`은 `@Test`가 런타임에도 유지되어야 한다는 표시이다. 만약 이 애너테이션을 생략하면 테스트 도구는 `@Test`를 인식할 수 없다.

한편, `@Target(ElementType.METHOD)`의 경우 `@Test`가 반드시 메서드 선언에서만 사용돼야 한다고 알려준다. 클래스 선언, 필드 선언 등 다른 프로그램 요소에는 달 수 없다.

## 마커 애너테이션

**이와 같이 애너테이션을 "아무 매개변수 없이 단순히 대상에 마킹(marking)한다"는 뜻에서 마커 애너테이션**이라 한다. `@Test`의 경우 이 애너테이션을 사용하면 프로그래머가 Test이름에 오타를 내거나 메서드 선언 외의 프로그램 요소에 달면 컴파일 오류를 내준다.

```java
public class Sample {
    @Test
    public static void m1() { } // 성공해야 한다.

    public static void m2() { }

    @Test
    public static void m3() {
        throw new RuntimeException("실패"); // 실패해야 한다.
    }

    public static void m4 () { }

    @Test
    public void m5() { } // 잘못 사용한 예: 정적 메서드가 아니다.

    public static void m6 { }

    @Test
    public static void m7 {
        throw new RuntimeException("실패"); // 실패해야 한다.
    }

    public static void m8() { }
}
```

`Sample` 클래스에는 정적 메서드가 7개이고 그 중 4개에 `@Test`를 달았다. m3와 m7 메서드는 예외를 던지고 m1, m5는 그렇지 않다. 그리고 m5는 인스턴스 메서드이므로 `@Test`를 잘못 사용한 경우다. 그리고 `@Test`를 붙이지 않은 나머지 4개의 메서드는 테스트 도구가 무시할 것이다.

## 테스트 코드 예제

`@Test` 애너테이션이 `Sample` 클래스의 의미에 직접적인 영향을 주진 않지만 이 애너테이션에 관심 있는 프로그램에게 추가 정보를 제공할 뿐이다.

```java
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RunTests {
    public static void main(String[] args) throws Exception {
        int tests = 0;
        int passed = 0;
        Class<?> testClass = Class.forName(args[0]);
        for (Method m : testClass.getDeclaredMethods()) {
            tests++;
            try {
                m.invoke(null);
                passed++;
            } catch (InvocationTargetException wrappedExc) {
                Throwable exc = wrappedExc.getCause();
                System.out.println(m + " 실패: " + exc);
            } catch (Exception exc) {
                System.out.println("잘못 사용한 @Test: " + m);
            }
        }
        System.out.printf("성공: %d, 실패: %d%n", passed, tests - passed);
    }
}
```

이 테스트 러너는 명령줄로부터 완전 정규화된 클래스 이름을 받아, 그 클래스에서 `@Test` 애너테이션이 달린 메서드를 차례로 호출한다. `isAnnotationPresent`가 실행할 메서드를 찾아주는 메서드다.  
테스트 메서드가 예외를 던지면 리플렉션 메커니즘이 `InvocationTargetException`으로 감싸서 다시 던진다. 그래서 이 프로그램은 `InvocationTargetException`을 잡아 원래 예외에 담긴 실패 정보를 추출해(`getCause`) 출력한다.

`InvocationTargetException` 외의 예외가 발생했다면 `@Test` 애너테이션을 잘못 사용했다는 뜻이다. 두 번째 catch 블록은 이처럼 잘못 사용해서 발생한 예외를 붙잡아 적절한 오류 메시지를 출력한다. (인스턴스 메서드, 매개변수가 있는 메서드, 호출할 수 없는 메서드 등에 단 경우)

### 특정 예외를 던져야만 성공하는 테스트를 지원하자.

```java
// 매개변수 하나를 받는 애너테이션 타입
import java.lang.annotation.*;

/**
 * 명시한 예외를 던져야만 성공하는 테스트 메서드용 애너테이션
**/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
    Class<? extends Throwable> value();
}
```

이 애너테이션의 매개변수 타입은 `Class<? extends Throwable>`이다. 와일드카드의 타입이 의미하는 바는 "Throwable을 확장한 클래스의 Class 객체"라는 뜻이며, 따라서 모든 예외와 오류 타입을 다 수용한다. 이는 한정적 타입 토큰(item 33)의 또 하나의 활용 사례다.

이 애너테이션을 실제 활용하는 모습은 다음과 같다.

```java
public class Sample2 {
    @ExceptionTest(ArithmeticException.class)
    public static void m1() { // 성공해야 한다.
        int i = 0;
        i = i / i;
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m2() { // 실패해야 한다. (다른 예외 발생)
        int[] a = new int[0];
        int i = a[1];
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m3() { } // 실패해야 한다. (예외가 발생하지 않음)
}
```

### 예외를 여러 개 명시하고 그중 하나만 발생하면 성공하게 만들자.

```java
// 배열 매개변수를 받는 애너테이션 타입
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
    Class<? extends Throwable>[] value();
}
```

원소가 여럿인 배열을 지정할 때는 다음과 같이 원소들을 중괄호로 감싸고 쉼표로 구분해주기만 하면 된다.

```java
@ExceptionTest({ IndexOutOfBoundsException.class, NullPointerException.class })
public static void doublyBad() {
    List<String> list = new ArrayList<>();

    // 자바 API 명세에 따르면 다음 메서드는 IndexOutOfBoundsException이나
    // NullPointerException을 던질 수 있다.
    list.addAll(5, null);
}
```

### 반복 가능 애너테이션

**Java 8에서는 여러 개의 값을 받는 애너테이션을 다른 방식으로도 만들 수 있다.**

배열 매개변수를 사용하는 대신 \*\*애너테이션에 `@Repeatable` 메타 애너테이션을 다는 방식이다. `@Repeatable`을 단 애너테이션은 하나의 프로그램 요소에 여러 번 달 수 있다.

단, 주의할 점은 다음과 같다.

- `@Repeatable`을 단 애너테이션을 반환하는 '컨테이너 애너테이션'을 하나 더 정의하고, `@Repeatable`에 이 컨테이너 애너테이션의 class 객체를 매개변수로 전달해야 한다.
- 컨테이너 애너테이션은 내부 애너테이션 타입의 배열을 반환하는 value 메서드를 정의해야 한다.
- 컨테이너 애너테이션 타입에는 적절한 보존 정책(`@Retention`)과 적용 대상(`@Target`)을 명시해야 한다. 그렇지 않으면 컴파일 되지 않을 것이다.

```java
// 반복 가능한 애너테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ExceptionTestContainer.class)
public @interface ExceptionTest {
    Class<? extends Throwable> value();
}

// 컨테이너 애너테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTestContainer {
    ExceptionTest[] value();
}
```

배열 방식 대신 반복 가능 애너테이션을 적용하면 다음과 같다.

```java
@ExceptionTest(IndexOutOfBoundsException.class)
@ExceptionTest(NullPointerException.class)
public static void doublyBad() { ... }
```

정리하자면, **애너테이션으로 할 수 있는 일을 명명 패턴으로 처리할 이유는 없다.**  
도구 제작자를 제외하곤, 일반 프로그래머가 애너테이션 타입을 직접 정의할 일은 거의 없다. **하지만 자바 프로그래머라면 예외 없이 자바가 제공하는 애너테이션 타입들은 사용해야 한다.**
