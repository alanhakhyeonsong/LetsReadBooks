# 아이템 55. 옵셔널 반환은 신중히 하라
Java 8 이전에는 메서드가 특정 조건에서 값을 반환할 수 없을 때 **예외를 던지거나**, (반환 타입이 객체 참조라면) **`null`을 반환**하였다.  
두 방법 모두 허점이 있다.
- 예외는 진짜 예외적인 상황에서만 사용해야 하며(item 69) 예외를 생성할 때 스택 추적 전체를 캡처하므로 비용도 만만치 않다.
- `null`을 반환하면 위 같은 문제는 생기지 않지만, 별도의 `null` 처리 코드를 추가해야 한다. `null` 처리를 무시하고 반환된 `null` 값을 어딘가에 저장해두면 언젠가 근본적인 원인과 전혀 상관없는 코드에서 `NullPointerException`이 발생할 수 있다.

## Java 8의 Optional
`Optional<T>`는 `null`이 아닌 `T` 타입 참조를 하나 담거나, 혹은 아무것도 담지 않을 수 있다. 아무것도 담지 않은 옵셔널은 '비었다'고 하며, 반대로 어떤 값을 담은 옵셔널은 '비지 않았다'고 한다.  
옵셔널은 원소를 최대 1개 가질 수 있는 '불변' 컬렉션이다. `Optional<T>`가 `Collection<T>`를 구현하진 않았지만, 원칙적으로 그렇다는 말이다.

보통은 `T`를 반환해야 하지만, 특정 조건에서는 아무것도 반환하지 않아야 할 때 `T` 대신 `Optional<T>`를 반환하도록 선언하면 된다. 그 결과 유효한 반환값이 없을 때는 빈 결과를 반환하는 메서드가 만들어진다. **옵셔널을 반환하는 메서드는 예외를 던지는 메서드보다 유연하고 사용하기 쉬우며, `null`을 반환하는 메서드보다 오류 가능성이 적다.**

```java
// 컬렉션에서 최댓값을 구한다. (컬렉션이 비었으면 예외를 던진다)
public static <E extends Comparable<E>> E max(Collection<E> c) {
    if (c.isEmpty())
        throw new IllegalArgumentException("빈 컬렉션");
    
    E result = null;
    for (E e : c)
        if (result == null || e.compareTo(result) > 0)
            result = Objects.requireNonNull(e);
    
    return result;
}
```
위 예제 코드를 `Optional<E>`를 반환하도록 수정하면 다음과 같다.

```java
// 컬렉션에서 최댓값을 구해 Optional<E>로 반환한다.
public static <E extends Comparable<E>> Optional<E> max(Collection<E> c) {
    if (c.isEmpty())
        return Optional.empty();
    
    E result = null;
    for (E e : c)
        if (result == null || e.compareTo(result) > 0)
            result = Objects.requireNonNull(e);
    
    return Optional.of(result);
}
```
위 코드에서 두 가지 팩터리를 사용했다는 점에 주목하자.  
빈 옵셔널은 `Optional.empty()`로 만들고, 값이 든 옵셔널은 `Optional.of(value)`로 생성했다. **`Optional.of(value)`에 `null`을 넣으면 `NullPointerException`을 던지니 주의하자.** `null` 값을 허용하는 옵셔널을 만들려면 `Optional.ofNullable(value)`를 사용하면 된다.  
**옵셔널을 반환하는 메서드에서는 절대 `null`을 반환하지 말자. 이는 옵셔널을 도입한 취지를 완전히 무시하는 행위다.**

또한 스트림의 종단 연산 중 상당수가 옵셔널을 반환한다.
```java
// 컬렉션에서 최댓값을 구해 Optional<E>로 반환한다. - 스트림 버전
public static <E extends Comparable<E>> Optional<E> max(Collection<E> c) {
    return c.stream().max(Comparator.naturalOrder());
}
```

## 옵셔널 반환을 선택하는 기준
**옵셔널은 검사 예외와 취지가 비슷하다.(item 71)** 즉, 반환 값이 없을 수도 있음을 API 사용자에게 명확히 알려준다. 비검사 예외를 던지거나 `null`을 반환한다면 API 사용자가 그 사실을 인지하지 못해 끔찍한 결과로 이어질 수 있다. 하지만 검사 예외를 던지면 클라이언트는 반드시 이에 대처하는 코드를 작성해넣어야 한다.

비슷하게, 메서드가 옵셔널을 반환한다면 클라이언트는 값을 받지 못했을 때 취할 행동을 선택해야 한다. 크게 4가지 방법이 있다.

1. 기본값을 설정한다.

```java
// 옵셔널 활용 1 - 기본값을 정해둘 수 있다.
String lastwordInLexicon = max(words).orElse("단어 없음...");
```

2. 상황에 맞는 예외를 던진다.
```java
// 옵셔널 활용 2 - 원하는 예외를 던질 수 있다.
Toy myToy = max(toys).orElseThrow(TemperTantrumException::new);
```
위 코드에서 실제 예외가 아니라 예외 팩터리를 건넨 것은 예외가 실제로 발생하지 않는 한 예외 생성 비용은 들지 않기 때문이다.

3. 항상 값이 채워져 있다고 가정한다.
```java
// 옵셔널 활용 3 - 항상 값이 채워져 있다고 가정한다.
Element lastNobleGas = max(Elements.NOBLE_GASES).get();
```
옵셔널에 항상 값이 채워져 있다고 확신한다면 그냥 곧바로 값을 꺼내 사용하는 선택지도 있다. 다만, 잘못 판단한 것이라면 `NoSuchElementException`이 발생할 것이다.

4. `isPresent()`
```java
Optional<ProcessHandle> parentProcess = ph.parent();
System.out.println("부모 PID: " + (parentProcess.isPresent() ?
    String.valueOf(parentProcess.get().pid()) : "N/A"));
```
앞서의 기본 메서드로 처리하기 어려워 보인다면 `filter`, `map`, `flatMap`, `isPresent` 들이 문제를 해결해 줄 수 있다. `isPresent`의 경우 안전 밸브 역할의 메서드로, 옵셔널이 채워져 있으면 `true`를, 비어 있으면 `false`를 반환한다. 이 메서드론 원하는 모든 작업을 수행할 수 있지만 신중히 사용해야 한다.

## Java 9의 stream
Java 9에선 `Optional`에 `stream()` 메서드가 추가되었다. 이는 `Optional`을 `Stream`으로 변환해주는 어댑터다.  
옵셔널에 값이 있으면 그 값을 원소로 담은 스트림으로, 값이 없다면 빈 스트림으로 변환한다. 이를 `Stream.flatMap()`과 조합하면 편리하다.

```java
streamOfOptionals
    .flatMap(Optional::stream);
```

## 옵셔널의 주의점
반환값으로 옵셔널을 사용한다고 해서 무조건 득이 되는 것은 아니다.  
**컬렉션, 스트림, 배열, 옵셔널 같은 컨테이너 타입은 옵셔널로 감싸면 안 된다.** 빈 `Optional<List<T>>`를 반환하기 보다는 빈 `List<T>`를 반환하는 게 좋다.(item 54)  
빈 컨테이너를 그대로 반환하면 클라이언트에 옵셔널 처리 코드를 넣지 않아도 된다.

### 메서드 반환 타입의 선언 기준
메서드 반환 타입을 `T` 대신 `Optional<T>`로 선언하는 기본 규칙은 다음과 같다.  
**결과가 없을 수 있으며, 클라이언트가 이 상황을 특별하게 처리해야 한다면 `Optional<T>`를 반환한다.

하지만 이렇게 하더라도 `Optional<T>`를 반환하는 데는 대가가 따른다. `Optional`도 엄연히 새로 할당하고 초기화해야 하는 객체이고, 그 안에서 값을 꺼내려면 메서드를 호출해야 하니 한 단계를 더 거치는 셈이다.  
**따라서 성능이 중요한 상황에는 옵셔널이 맞지 않을 수 있다.** 어떤 메서드가 이 상황에 처하는지 알아내려면 세심히 측정해보는 수밖에 없다.

### 전용 옵셔널 클래스
박싱된 기본 타입을 담는 옵셔널은 기본 타입 자체보다 무거울 수밖에 없다. 값을 두 겹이나 감싸기 때문이다. 그래서 Java API 설계자는 int, long, double 전용 옵셔널 클래스들을 준비해놨다.

`OptionalInt`, `OptionalLong`, `OptionalDouble`이다. 이 옵셔널들도 `Optional<T>`가 제공하는 메서드를 거의 다 제공한다. 따라서 **박싱된 기본 타입을 담은 옵셔널을 반환하는 일은 없도록 하자.**

이 외에 옵셔널을 반환하고 처리하는 쓰임은 대부분 적절치 않기 때문에 잊자. 예를 들어, **옵셔널을 맵의 값으로 사용하면 절대 안된다.** 일반화해 이야기하면 **옵셔널을 컬렉션의 키, 값, 원소나 배열의 원소로 사용하는 게 적절한 상황은 거의 없다.**

## 핵심 정리
- 값을 반환하지 못할 가능성이 있고, 호출할 때마다 반환값이 없을 가능성을 염두에 둬야 하는 메서드라면 옵셔널을 반환해야 할 상황일 수 있다.
- 하지만 옵셔널 반환에는 성능 저하가 뒤따르니, 성능에 민감한 메서드라면 `null`을 반환하거나 예외를 던지는 편이 나을 수 있다.
- 그리고 옵셔널을 반환값 이외의 용도로 쓰는 경우는 매우 드물다.