# 아이템 44. 표준 함수형 인터페이스를 사용하라

Java가 람다를 지원하면서 API를 작성하는 모범 사례도 크게 바뀌었다.  
상위 클래스의 기본 메서드를 재정의해 원하는 동작을 구현하는 템플릿 메서드 패턴의 매력이 크게 줄었다. 이를 대체하는 현대적인 해법은 같은 효과의 함수 객체를 받는 정적 팩터리나 생성자를 제공하는 것이다.  
이 내용을 일반화한다면, 함수 객체를 매개변수로 받는 생성자와 메서드를 더 많이 만들어야 한다. 이때, 함수형 매개변수 타입을 올바르게 선택해야 한다.

```java
protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
    return size() > 100;
}
```

위 코드는 람다를 사용하면 훨씬 더 효율적이다. 위 함수 객체는 `Map.Entry<K,V>`를 받아 `boolean`을 반환해야 할 것 같지만, 꼭 그렇지는 않다. `removeEldestEntry`는 `size()`를 호출해 맵 안의 원소 수를 알아내는데, `removeEldestEntry`가 인스턴스 메서드라서 가능한 방식이다. 하지만 생성자에 넘기는 함수 객체는 이 맵의 인스턴스 메서드가 아니다. 팩터리나 생성자를 호출할 때는 맵의 인스턴스가 존재하지 않기 때문이다. 따라서 맵은 자기 자신도 함수 객체에 건네줘야 한다.

```java
@FunctionalInterface
interface EldestEntryRemovalFunction<K,V> {
    boolean remove(Map<K,V> map, Map.Entry<K,V> eldest);
}
```

위 조건을 만족하는 함수형 인터페이스는 위와 같지만, 굳이 사용할 이유는 없다. **자바 표준 라이브러리에 이미 같은 모양의 인터페이스가 준비되어 있기 때문이다.**

## 표준 함수형 인터페이스

`java.util.function` 패키지를 보면 다양한 용도의 표준 함수형 인터페이스가 담겨 있다. **필요한 용도에 맞는게 있다면, 직접 구현하지 말고 표준 함수형 인터페이스를 활용하라.** API가 다루는 개념의 수가 줄어들어 익히기 더 쉬워진다. 또한 표준 함수형 인터페이스들은 유용한 디폴트 메서드를 많이 제공하므로 다른 코드와의 상호운용성도 크게 좋아질 것이다.

`java.util.function` 패키지에는 총 43개의 인터페이스가 담겨 있다. 이 중 기본 인터페이스 6개만 기억하면 나머지를 충분히 유추해낼 수 있다. 이 기본 인터페이스들은 모두 **참조 타입용**이다.

| 인터페이스          | 함수 시그니처         | 예                    |
| ------------------- | --------------------- | --------------------- |
| `UnaryOperator<T>`  | `T apply(T t)`        | `String::toLowerCase` |
| `BinaryOperator<T>` | `T apply(T t1, T t2)` | `BigInteger::add`     |
| `Predicate<T>`      | `boolean test(T t)`   | `Collection::isEmpty` |
| `Function<T,R>`     | `R apply(T t)`        | `Arrays::asList`      |
| `Supplier<T>`       | `T get()`             | `Instant::now`        |
| `Consumer<T>`       | `void accept(T t)`    | `System.out::println` |

**표준 함수형 인터페이스 대부분은 기본 타입만 지원한다. 그렇다고 기본 함수형 인터페이스에 박싱된 기본 타입을 넣어 사용하지는 말자.**

## 함수형 인터페이스를 직접 작성해야 하는 경우

대부분의 상황에서는 표준 함수형 인터페이스를 사용하는 편이 더 낫다. 표준 인터페이스 중 필요한 용도에 맞는 게 없다면 직접 작성해야 한다.

`Comparator<T>`의 예시를 생각한다면, 다음 세 가지 특성 중 하나 이상을 만족한다면 전용 함수형 인터페이스를 구현해야 하는지 진중히 고민할 필요가 있다.

- 자주 쓰이며, 이름 자체가 용도를 명확히 설명해준다.
- 반드시 따라야 하는 규약이 있다.
- 유용한 디폴트 메서드를 제공할 수 있다.

## `@FunctionalInterface`

`@FunctionalInterface` 애너테이션을 사용하는 이유는 `@Override`를 사용하는 이유와 비슷하다. 프로그래머의 의도를 명시하는 것으로 크게 세 가지 목적이 있다.

1. 해당 클래스의 코드나 설명 문서를 읽을 이에게 그 인터페이스가 람다용으로 설계된 것임을 알려준다.
2. 해당 인터페이스가 추상 메서드를 오직 하나만 가지고 있어야 컴파일되게 해준다.
3. 그 결과 유지보수 과정에서 누군가 실수로 메서드를 추가하지 못하게 막아준다.

따라서 **직접 만든 함수형 인터페이스에는 항상 `@FunctionalInterface` 애너테이션을 사용하라.**

## 함수형 인터페이스 사용 주의점

서로 다른 함수형 인터페이스를 같은 위치의 인수로 받는 메서드들을 다중 정의해서는 안된다. 클라이언트에게 모호함만 안겨줄 뿐이며, 이 모호함으로 인해 실제로 문제가 일어나기도 한다.

## 핵심 정리

- 자바도 람다를 지원한다. 지금부터는 API를 설계할 때 람다도 염두에 두어야 한다는 뜻이다.
- 입력값과 반환값에 함수형 인터페이스 타입을 활용하라.
- 보통은 `java.util.function` 패키지의 표준 함수형 인터페이스를 사용하는 것이 가장 좋은 선택이다.
- 단, 흔치는 않지만 직접 새로운 함수형 인터페이스를 만들어 쓰는 편이 나을 수도 있음을 잊지 말자.