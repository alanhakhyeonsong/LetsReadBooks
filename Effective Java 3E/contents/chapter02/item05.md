# 아이템 5. 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

많은 클래스가 하나 이상의 자원에 의존한다. (간단하게 Spring framework를 생각해도 이에 동의할 것이다.)  
이 책에선 `SpellChecker`와 `dictionary`를 예로 들고 있다. `SpellChecker`는 `dictionary`를 사용하고, 이를 의존하는 리소스 또는 의존성이라 부른다. 부적절한 구현 예시를 보자.

## 부적절한 구현 예시

### 1. 정적 유틸리티를 잘못 사용한 예([item 4](./item04.md))

```java
// 정적 유틸리티를 잘못 사용한 예 - 유연하지 않고 테스트하기 어렵다.
public class SpellChecker {
    private static final Lexicon dictionary = ...;

    private SpellChecker() {} // 객체 생성 방지

    public static boolean isValid(String word) { ... }
    public static List<String> suggestions(String typo) { ... }
}
```

### 2. 싱글턴을 잘못 사용한 예([item 3](./item03.md))

```java
// 싱글턴을 잘못 사용한 예 - 유연하지 않고 테스트하기 어렵다.
public class SpellChecker {
    private final Lexicon dictionary = ...;

    private SpellChecker() {}
    public static SpellChecker INSTANCE = new SpellChecker(...);

    public static boolean isValid(String word) { ... }
    public static List<String> suggestions(String typo) { ... }
}
```

두 방식 모두 사전을 단 하나만 사용한다고 가정한다는 점에서 그리 훌륭해 보이지 않다. 실제로는 사전이 언어별로 따로 있고 특수 어휘용 사전을 별도로 두기도 한다. 심지어 테스트용 사전도 필요할 수 있다. 사전 하나로 이 모든 쓰임에 대응할 수 있기 바라는 건 너무 순진한 생각이다.  
**사용하는 자우너에 따라 동작이 달라지는 클래스에는 정적 유틸리티 클래스나 싱글턴 방식이 적합하지 않다.**  
// `김영한님의 스프링 핵심원리 - 기본편` 강의에서도 이와 비슷한 맥락으로 설명하신 적이 있다.

이런 요구 사항을 만족할 수 있는 간단한 패턴으로 생성자를 사용해서 **새 인스턴스를 생성할 때 사용할 리소스를 넘겨주는 방법이 있다.** 이는 의존 객체 주입의 한 형태이다.

### 적절한 구현

```java
// 의존 객체 주입은 유연성과 테스트 용이성을 높여준다.
public class SpellChecker {
    private final Lexicon dictionary;

    public SpellChecker(Lexicon dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    public static boolean isValid(String word) { ... }
    public static List<String> suggestions(String typo) { ... }
}
```

위 예시에선 `dictionary`라는 딱 하나의 자원만 사용하지만, 자원이 몇 개든 의존 관계가 어떻든 상관없이 잘 동작한다. 또한 불변을 보장하여 (같은 자원을 사용하려는) 여러 클라이언트가 의존 객체들을 안심하고 공유할 수 있기도 하다.

위와 같은 의존성 주입은 생성자, 정적 팩터리([item 1](./item01.md)), 빌더([item 2](./item02.md))에도 적용할 수 있다.

이 패턴의 변종으로 생성자에 자원 팩터리를 넘겨주는 방식이 있다. **팩터리란 호출할 때마다 특정 타입의 인스턴스를 반복해서 만들어주는 객체를 말한다.** 즉, 팩터리 메서드 패턴(Factory Method Pattern)을 구현한 것이다.

Java 8에 추가된 `Supplier<T>` 인터페이스가 그런 팩터리를 표현한 완벽한 에시라고 한다.  
`Supplier<T>`를 입력으로 받는 메서드는 보통 한정적 와일드카드 타입(bounded wildcard type, item 31)을 사용해 팩터리의 타입 매개변수를 제한해야 한다.

```java
// 클라이언트가 제공한 팩터리가 생성한 타일(Tile)들로 구성된 모자이크(Mosaic)를 만드는 메서드
Mosaic create(Supplier<? extends Tile> tileFactory) { ... }
```

의존 객체 주입이 유연성과 테스트 용이성을 개선해주지만, 의존성이 수 천 개나 되는 큰 프로젝트에서는 코드를 어지럽게 만들기도 한다. 그 점은 대거, 쥬스, 스프링같은 의존 객체 주입 프레임워크를 사용하면 해소할 수 있다.

## 핵심 정리

- 클래스가 내부적으로 하나 이상의 자원에 의존하고, 그 자원이 클래스 동작에 영향을 준다면 싱글턴과 정적 유틸리티 클래스는 사용하지 않는 것이 좋다. 이 자원들을 클래스가 직접 만들게 해서도 안 된다.
- 필요한 자원을(혹은 그 자원을 만들어주는 팩터리를) 생성자에 (혹은 정적 팩터리나 빌더에) 넘겨주자.
- 의존 객체 주입이라 하는 이 기법은 클래스의 유연성, 재사용성, 테스트 용이성을 기막히게 개선해준다.
