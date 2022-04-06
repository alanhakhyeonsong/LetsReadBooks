# 아이템 26. 로 타입은 사용하지 말라

## 제네릭 클래스, 제네릭 인터페이스

클래스와 인터페이스 선언에 타입 매개변수(type parameter)가 쓰이면 이를 제네릭 클래스 혹은 제네릭 인터페이스라 한다. 이 둘을 통틀어 제네릭 타입(generic type)이라 한다.

각각의 제네릭 타입은 일련의 매개변수화 타입(parameterized type)을 정의한다.

## 로 타입(raw type)

로 타입이란 제네릭 타입에서 타입 매개변수를 전혀 사용하지 않을 때를 말한다. `List<E>`의 로 타입은 `List`이다. 로 타입은 타입 선언에서 제네릭 타입 정보가 전부 지워진 것처럼 동작하는데, 제네릭이 도래하기 전 코드와 호환되도록 하기 위한 궁여지책이라 할 수 있다.

```java
// Stamp 인스턴스만 취급한다.
private final Collection stamps = ...;
// 실수로 동전을 넣는다.
stamps.add(new Coin(...)); // "unchecked call" 경고를 내뱉는다.
```

제네릭을 지원하기 전에는 컬렉션을 위와 같이 선언했다. Java 9에서도 여전히 동작하지만 좋은 에라고 볼 순 없다.  
이 코드를 사용하면 실수로 Stamp 대신 Coin을 넣어도 아무 오류 없이 컴파일되고 실행된다.

```java
for (Iterator i = stamps.iterator(); i.hasNext(); ) {
    Stamp stamp = (Stamp) i.next(); // ClassCastException을 던진다.
    stamp.cancel();
}
```

**오류는 가능한 한 발생 즉시, 이상적으로는 컴파일할 때 발견하는 것이 좋다.** 위 예시는 오류가 발생하고 한참 뒤인 런타임에야 알아챌 수 있는데, 이렇게 되면 런타임에 문제를 겪는 코드와 원인을 제공한 코드가 물리적으로 상당히 떨어져 있을 가능성이 커진다.

제네릭을 활용하면 이 정보가 주석이 아닌 타입 선언 자체에 녹아든다.

```java
private final Collection<Stamp> stamps = ...;
```

위 코드와 같이 매개변수화된 컬렉션 타입의 경우 타입 안전성이 확보된다. 이제 `stamps`에 엉뚱한 타입의 인스턴스를 넣으려 하면 컴파일 오류가 발생하며 무엇이 잘못됐는지를 정확히 알려준다.

**로 타입(타입 매개변수가 없는 제네릭 타입)을 쓰는 걸 언어 차원에서 막아 놓지는 않았지만 절대로 써선 안 된다.** **로 타입을 쓰면 제네릭이 안겨주는 안전성과 표현력을 모두 잃게 된다.**

## 로 타입은 왜 쓰는가?

로 타입을 애초에 왜 만들어 놓은 것인가? 바로 **호환성 때문이다.**  
Java가 제네릭을 받아들이기까지 거의 10년이 걸린 탓에 제네릭 없이 짠 코드가 이미 세상을 뒤덮어 버렸다. 그래서 기존 코드를 모두 수용하면서 제네릭을 사용하는 새로운 코드와도 맞물려 돌아가게 해야만 했다. 이 마이그레이션 호환성을 위해 로 타입을 지원하고 제네릭 구현에는 소거 방식을 사용하기로 한 것이다.

## 로 타입과 매개변수화 타입의 차이?

**`List` 같은 로 타입은 사용해서는 안 되나, `List<Object>` 처럼 임의 객체를 허용하는 매개변수화 타입은 괜찮다.** 둘의 차이를 간단히 이야기하자면, `List`는 제네릭 타입에서 완전히 발을 뺀 것이고, `List<Object>`는 모든 타입을 허용한다는 의사를 컴파일러에 명확히 전달한 것이다.

매개변수로 `List`를 받는 메서드에 `List<String>`을 넘길 수는 있지만, `List<Object>`를 받는 메서드에는 넘길 수 없다.  
이는 제네릭의 하위 타입 규칙 때문이다. 다시 말해, `List<String>`은 로 타입인 `List`의 하위 타입이지만, `List<Object>`의 하위 타입은 아니다.

**`List<Object>` 같은 매개변수화 타입을 사용할 때와 달리 `List` 같은 로 타입을 사용하면 타입 안전성을 잃게 된다.**

```java
public static void main(String[] args) {
    List<String> strings = new ArrayList<>();
    unsafeAdd(strings, Integer.valueOf(42));
    String s = strings.get(0);
}

private static void unsafeAdd(List list, Object o) {
    list.add(o);
}
```

위 코드는 컴파일은 되지만 로 타입인 `List`를 사용하여 다음과 같은 경고가 발생한다.

```log
Test.java:10: warning: [unchecked] unchecked call to add(E) as a member of the raw type List
    list.add(o);
```

이 프로그램을 이대로 실행하면 `ClassCastException`을 던진다.

```java
private static void unsafeAdd(List<Object> list, Object o) {
    list.add(o);
}
```

로 타입을 매개변수화 타입으로 바꾼 다음 컴파일 해보면 컴파일조차 되지 않는다.

## 원소 타입을 몰라도 되는 방식

```java
static int numElementsInCommon(Set s1, Set s2) {
    int result = 0;
    for (Object o1 : s1) {
        if (s2.contains(o1)) {
            result++;
        }
    }
    return result;
}
```

이 메서드는 동작은 하지만 로 타입을 사용하므로 안전하지 않다. 따라서 **비한정적 와일드카드 타입(unbounded wildcard type)을 대신 사용하는 게 좋다.**  
**제네릭 타입을 쓰고 싶지만 실제 타입 매개변수가 무엇인지 신경 쓰고 싶지 않다면 물음표(?)를 사용하자.**

ex) `Set<E>`의 비한정적 와일드카드 타입: `Set<?>`

## 와일드카드 타입과 로 타입

둘의 차이를 간단히 말하자면, **와일드카드 타입은 안전하고, 로 타입은 안전하지 않다.**

로 타입 컬렉션에는 아무 원소나 넣을 수 있으니 타입 불변식을 훼손하기 쉽다.  
반면, **`Collection<?>`에는 (null 외에는) 어떤 원소도 넣을 수 없다.**

## 로 타입의 소소한 예외

### 1. class 리터럴에는 로 타입을 써야 한다.

자바 명세는 class 리터럴에 매개변수화 타입을 사용하지 못하게 했다. (배열과 기본 타입은 허용)  
`List.class`, `String[].class`, `int.class`는 허용하고 `List<String>.class`와 `List<?>.class`는 허용하지 않는다.

### 2. instanceof 연산자와 관련이 있다.

런타임에는 제네릭 타입정보가 지워지므로 `instanceof` 연산자는 비한정적 왈디카드 타입 이외의 매개변수화 타입에는 적용할 수 없다. 로 타입이든 비한정적 와일드카드 타입이든 `instanceof`는 완전히 똑같이 동작한다.

```java
if (o instanceof Set) { // 로 타입
    Set<?> s = (Set<?>) o; // 와일드카드 타입
    ...
}
```

## 핵심 정리

- 로 타입을 사용하면 런타임에 예외가 일어날 수 있으니 사용하면 안 된다.
- 로 타입은 제네릭이 도입하기 이전 코드와의 호환성을 위해 제공될 뿐이다.
- 빠르게 훑어보자면, `Set<Object>`는 어떤 타입의 객체도 저장할 수 있는 매개변수화 타입이고, `Set<?>`는 모종의 타입 객체만 저장할 수 있는 와일드카드 타입이다.
- 이들의 로 타입인 `Set`은 제네릭 타입 시스템에 속하지 않는다.
- `Set<Object>`와 `Set<?>`는 안전하지만, 로타입인 `Set`은 안전하지 않다.

![](https://imagedelivery.net/v7-TZByhOiJbNM9RaUdzSA/d5092b3a-a94f-47f8-b46f-73a6808b8900/public)
