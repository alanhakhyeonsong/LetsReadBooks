# 아이템 42. 익명 클래스보다는 람다를 사용하라

## 익명 클래스

Java에서 함수 타입을 표현할 때 추상 메서드를 하나만 담은 인터페이스(드물게는 추상 클래스)를 사용했다. 이런 인터페이스의 인스턴스를 함수 객체(function object)라 하여, 특정 함수나 동작을 나타내는 데 썼다.

```java
Collections.sort(words, new Comparator<String>() {
    public int compare(String s1, String s2) {
        return Integer.compare(s1.length(), s2.length());
    }
});
```

전략 패턴처럼, 함수 객체를 사용하는 과거 객체 지향 디자인패턴에는 익명 클래스면 충분했다. 위 코드에서 `Comparator` 인터페이스가 정렬을 담당하는 추상 전략을 뜻하며, 문자열을 정렬하는 구체적인 전략을 익명 클래스로 구현했다.

**하지만 익명 클래스 방식은 코드가 너무 길기 때문에 Java는 함수형 프로그래밍에 적합하지 않았다.**

## 람다식

Java 8에 와서 추상 메서드 하나짜리 인터페이스는 특별한 의미를 인정받았다.

**함수형 인터페이스라 불리는 이 인터페이스들의 인스턴스를 람다식(lambda expression, 혹은 람다)을 사용해 만들 수 있게 되었다. 람다는 함수나 익명 클래스와 개념은 비슷하지만 코드는 훨씬 간결하다.**

```java
Collections.sort(words, (s1, s2) -> Integer.compare(s1.length(), s2.length()));
```

람다의 타입은 컴파일러가 문맥을 살펴 추론한다. **타입을 명시해야 코드가 더 명확할 때만 제외하곤, 람다의 모든 매개변수 타입은 생략하자**

```java
Collections.sort(words, comparingInt(String::length));
```

람다 자리에 비교자 생성 메서드를 사용하면 코드를 더욱 간결하게 만들 수 있다.

```java
words.sort(comparingInt(String::length));
```

더 나아가 Java 8에서 List 인터페이스에 추가된 `sort` 메서드를 이용하면 더욱 짧아진다.

## 람다 사용 시 유의사항

- **람다는 이름이 없고 문서화도 못한다. 따라서 코드 자체로 동작이 명확히 설명되지 않거나 코드 줄 수가 많아지면 람다를 쓰지 말아야한다.**
- **람다는 한 줄일 때 가장 좋고 길어야 세 줄 안에 끝내는게 좋다. 세 줄을 넘어가면 가독성이 심하게 나빠진다.**
- 람다가 길거나 읽기 어렵다면 더 간단히 줄여보거나 람다를 쓰지 않는 쪽으로 리팩터링하길 바란다.
- **람다는 함수형 인터페이스에서만 쓰인다.** 추상 클래스의 인스턴스를 만들 때 람다를 쓸 수 없으니, 익명 클래스를 써야 한다.
- **람다는 자신을 참조할 수 없다.** 람다에서의 `this` 키워드는 바깥 인스턴스를 가리킨다. (익명 클래스에서의 `this`는 익명 클래스의 인스턴스 자신을 가리킴)
- 함수 객체가 자신을 참조해야 한다면 반드시 익명 클래스를 써야한다.
- **람다를 직렬화하는 일은 극히 삼가야 한다.(익명 클래스의 인스턴스도 마찬가지)**

## 핵심 정리

- Java 8 부터 작은 함수 객체를 구현하는 데 적합한 람다가 도입되었다.
- **익명 클래스는 (함수형 인터페이스가 아닌) 타입의 인스턴스를 만들 때만 사용하라.**
- 람다는 작은 함수 객체를 아주 쉽게 표현할 수 있어 (이전 자바에서는 실용적이지 않던) 함수형 프로그래밍의 지평을 열었다.
