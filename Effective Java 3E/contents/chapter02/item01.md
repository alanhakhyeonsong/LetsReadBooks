# 아이템 1. 생성자 대신 정적 팩터리 메서드를 고려하라

> 정적 팩토리 메서드(Static Factory Method)에 대해 알아보자.

## Static Factory Method

- public 생성자를 사용하여 객체를 생성하는 전통적인 방법 외에도 `static factory method`를 이용하여 객체(인스턴스)를 생성할 수 있다.

```java
public static Item1 of(String name, String email) {
    return Item1.builder()
        .name(name)
        .email(email)
        .build();
}
```

```java
public static Boolean valueOf(boolean b) {
    return b ? Boolean.TRUE : Boolean.FALSE;
}
```

`static factory method`의 장점과 단점에 대해 알아보자.

## 장점 1. 이름을 가질 수 있다.

- 생성자와 다르게 이름을 지을 수 있다. 이름을 통해 파라미터 또는 반환될 객체의 특성을 쉽게 묘사할 수 있다. 이는 더 읽기 좋은 코드를 작성하는데 도움이 된다.
- 생성자의 경우 같은 타입의 파라미터를 받는 생성자를 두 개 만들 수 없는 것과 같이 하나의 시그니처에 생성자 하나만 만들 수 있다는 제약이 있다.
- 정적 팩토리 메서드에는 이런 제약이 없고 각각의 차이가 잘 드러내는 이름을 잘 지어주면 된다.

## 장점 2. 호출될 때마다 인스턴스를 새로 생성하지는 않아도 된다.

- 불변 클래스(immutable class)의 경우 또는 매번 새로운 객체를 만들 필요가 없는 경우는 인스턴스를 미리 만들어 놓고나 새로 생성한 인스턴스를 캐싱하여 재활용하는 식으로 불필요한 객체 생성을 피할 수 있다.
- `Boolean.valueOf(boolean)`이 대표적인 예시로, 같은 객체가 자주 요청되는 상황이라면 비용을 아끼고 성능을 올릴 수 있다.
- 반복되는 요청에 같은 객체를 반환하는 식으로 정적 팩토리 방식의 클래스는 인스턴스의 라이프사이클을 통제할 수 있다.
- 이를 통해 싱글톤(Singleton) 또는 인스턴스화 불가(noninstantiable)로 만들 수 있다.

## 장점 3. 반환 타입의 하위 타입 객체를 반환할 수 있는 능력이 있다.

- 반환할 객체의 클래스를 자유롭게 선택할 수 있는 엄청난 유연성이 있다.
- 이 유연성을 응용하면 구현 클래스를 공개하지 않고도 그 객체를 반환할 수 있어 API를 작게 유지할 수 있다.
- `java.util.Collections`가 대표적인 에시이며, 45개에 달하는 인터페이스의 구현체의 인스턴스를 제공하지만 그 구현체들은 전부 non-public이다.
- 즉 인터페이스 뒤에 감추어져 있고 public으로 제공해야 할 API를 줄이므로 개발자가 API를 사용하기 위해 익혀야 할 개념의 수와 난이도도 낮췄다.

## 장점 4. 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.

- 반환 타입의 하위 타입이기만 하면 어떤 클래스의 객체를 반환하든 상관없다.
- `EnumSet` 클래스는 생성자 없이 public static 메서드, `allOf()`. `of()` 등을 제공한다.
- 그 내부에서는 enum 타입 원소의 갯수에 따라 `RegularEnumSet`또는 `JumboEnumSet`으로 상황에 따라 다른 클래스의 객체를 반환한다.
- 개발자는 `EnumSet`이 넘겨주는 인스턴스가 어느 클래스의 인스턴스인지 알 필요가 없고 사용만 하면 된다.

## 장점 5. 정적 팩터리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.

- 장점 3, 4와 비슷한 개념으로, 이러한 유연함은 `service provide framework`를 만드는 근본이 된다.
- 대표적으로 `JDBC`가 있다.
- 서비스 프로바이더 프레임워크에서 `provider`는 서비스의 구현체다. 그리고 이 구현체들을 클라이언트에 제공하는 역할을 프레임워크가 통제하여, 클라이언트를 구현체로부터 분리해준다.
- 서비스 프로바이더 프레임워크는 3개의 핵심 컴포넌트로 이루어진다.
  - 구현체의 동작을 정의하는 서비스 인터페이스(service interface)
  - 제공자가 구현체를 등록할 때 사용하는 제공자 등록 API(provider registration API)
  - 클라이언트가 서비스의 인스턴스를 얻을 때 사용하는 서비스 접근 API(service access API)
- 클라이언트는 서비스 접근 API를 사용할 때 원하는 구현체의 조건을 명시할 수 있고, 명시하지 않으면 기본 구현체를 반환하거나 지원하는 구현체들을 하나씩 돌아가며 반환한다.
- 종종 위 3개의 핵심 컴포넌트와 더불어 서비스 제공자 인터페이스(service provider interface)라는 컴포넌트가 쓰이기도 하는데, 그게 없는 경우에는 리플랙션을 사용해서 구현체를 만들어 준다.
- `JDBC`의 경우, `DriverManager.registerDriver()`가 프로바이더 등록 API, `DriverManager.getConnection()`이 서비스 액세스 API, 그리고 `Driver`가 서비스 프로바이더 인터페이스 역할을 수행 한다.

## 단점 1. 상속을 하려면 public이나 protected 생성자가 필요하니 정적 팩터리 메서드만 제공하면 하위 클래스를 만들 수 없다.

- 클래스가 `package-private`으로 보호되기 때문에 다른 패키지에서 해당 클래스를 상속할 수 없다.
- 이 제약은 상속보다 컴포지션을 사용하도록 유도하고 불변 타입으로 만들려면 이 제약을 지켜야 한다는 점에서 다른 관점에서는 장점이라고도 할 수 있다.

## 단점 2. 정적 팩터리 메서드는 프로그래머가 찾기 어렵다.

- 생성자는 Javadoc 상단에 모아서 보여주지만, static 팩토리 메서드는 API 문서에서 특별히 다뤄주지 않는다.
- 따라서 클래스나 인터페이스 문서 상단에 팩토리 메서드에 대한 문서를 제공하며 메서드 이름도 널리 알려진 규약을 따라 짓는 식으로 문제를 완화해야 한다.

> 📌 참고 - 정적 팩터리 메서드에 흔히 사용하는 명명 방식들
>
> - from: 매개변수를 하나 받아서 해당 타입의 인스턴스를 반환하는 형변환 메서드  
>   `Date d = Date.from(instant);`
> - of: 여러 매개변수를 받아 적합한 타입의 인스턴스를 반환하는 집계 메서드  
>   `Set<Rank> faceCards = EnumSet.of(JACK, QUEEN, KING);`
> - valueOf: from과 of의 더 자세한 버전  
>   `BigInteger prime = BigInteger.valueOf(Integer.MAX_VALUE);`
> - instance 혹은 getInstance: (매개변수를 받는다면) 매개변수로 명시한 인스턴스를 반환하지만, 같은 인스턴스임을 보장하지는 않는다.  
>   `StackWalker luke = StackWalker.getInstance(options);`
> - create 혹은 newInstance: instance 혹은 getInstance와 같지만, 매번 새로운 인스턴스를 생성해 반환함을 보장한다.  
>   `Object newArray = Array.newInstance(classObject, arrayLen);`
> - getType: getInstance와 같으나, 생성할 클래스가 아닌 다른 클래스에 팩터리 메서드를 정의할 때 쓴다. "Type"은 팩터리 메서드가 ㅂㄴ환할 객체의 타입이다.  
>   `FileStore fs = Files.getFileStore(path);`
> - newType: newInstance와 같으나, 생성할 클래스가 아닌 다른 클래스에 팩터리 메서드를 정의할 때 쓴다. "Type"은 팩터리 메서드가 반환할 객체의 타입이다.  
>   `BufferedReader br = Files.newBufferedReader(path);`
> - type: getType과 newType의 간결한 버전  
>   `List<Complaint> litany = Collections.list(legacyLitany);`

## 핵심 정리

- 정적 팩터리 메서드와 public 생성자는 각각의 쓰임새가 있으니 상대적인 장단점을 이해하고 사용하는 것이 좋다.
- 그래도 정적 팩터리를 사용하는 게 유리한 경우가 더 많으므로 무작정 public 생성자를 제공하던 습관을 고치도록 하자.
