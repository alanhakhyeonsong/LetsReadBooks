# 아이템 14. Comparable을 구현할지 고려하라

Comparable 인터페이스의 유일무이한 메서드인 `compareTo()`는 Object의 메서드가 아니다. Object의 equals와 비슷하지만 두 가지 차이점이 있다.

compareTo는 단순 동치성 비교에 더해 순서까지 비교할 수 있으며, 제네릭 하다.(타입 지정 가능)

Comparable을 구현했다는 것은 그 클래스의 인스턴스들에는 자연적인 순서가 있음을 뜻한다.

```java
Arrays.sort(arr);
```

검색, 극단값 계산, 자동 정렬되는 컬렉션 관리도 역시 쉽게 할 수 있다.  
다음 예시는 명령줄 인수들을 중복은 제거하고 알파벳 순으로 출력한다.

```java
public class WordList {
    public static void main(String[] args) {
        Set<String> s = new TreeSet<>();
        Collections.addAll(s, args);
        System.out.println(s);
    }
}
```

사실상 자바 플랫폼 라이브러리의 모든 값 클래스와 열거타입이 Comparable을 구현했다. 알파벳, 숫자, 연대 같이 순서가 명확한 값 클래스를 작성한다면 반드시 Comparable 인터페이스를 구현하자.

```java
public interface Comparable<T> {
    int compareTo(T t);
}
```

## compareTo의 일반 규약

이 객체와 주어진 객체의 순서를 비교한다. 이 객체가 주어진 객체보다 작으면 음의 정수를, 같으면 0을, 크면 양의 정수를 반환한다. 이 객체와 비교할 수 없는 타입의 객체가 주어지면 `ClassCastException`을 던진다.

- Comparable을 구현한 클래스는 모든 x, y에 대해 `sgn(x.compareTo(y)) == -sgn(y.compareTo(x))`여야 한다. (x.compareTo(y)는 y.compareTo(x)가 예외를 던질때에 한해 예외를 던져야 함.)
- Comparable을 구현한 클래스는 추이성을 보장해야 한다. 즉, `(x.compareTo(y) > 0 && y.compareTo(z) > 0)`이면, `x.compareTo(z) > 0` 이다.
- Comparable을 구현한 클래스는 모든 z에 대해 `x.compareTo(y) == 0이`면 `sgn(x.compareTo(z)) == sgn(y.compareTo(z))`다.
- 권고가 필수는 아니지만 꼭 지키는게 좋은 것은 `(x.compareTo(y) == 0) == (x.equals(y))`여야 한다. Comparable을 구현하고 이 권고를 지키지 않는 모든 클래스는 그 사실을 명시해야 한다.

> 이 클래스의 순서는 equals 메서드와 일관되지 않다. (주의)

모든 객체에 대해 전역 동치관계를 부여하는 equals 메서드와 달리, compareTo는 타입이 다른 객체를 신경쓰지 않아도 된다. 타입이 다른 객체가 주어지면 간단히 `ClassCastException`을 던져도 된다.

hashCode 규약을 지키지 못하면 해시를 사용하는 클래스와 어울리지 못하듯, compareTo 규약을 지키지 못하면 비교를 활용하는 클래스와 어울리지 못한다.

비교를 활용하는 클래스의 예

- TreeSet, TreeMap(정렬된 컬렉션)
- Collections, Arrays(정렬 알고리즘을 활용하는 유틸리티 클래스)

## compareTo 규약 살펴보기

1. 두 객체 참조의 순서를 바꿔 비교해도 예상한 결과가 나와야 한다.
2. 첫 번째가 두 번째보다 크고 두 번째가 세 번째보다 크면, 첫 번째는 세 번째 보다 커야한다.
3. 크기가 같은 객체들끼리는 어떤 객체오 ㅏ비교하더라도 항상 같아야 한다.

이상 세 규약은 compareTo 메서드로 수행하는 동치성 검사도 equals 규약과 똑같이 반사성, 대칭성, 추이성을 충족해야 함을 뜻한다. 그래서 주의사항도 똑같다.

compareTo 우회법은 다음과 같다.

Comparable을 구현한 클래스를 확장해 값 컴포넌트를 추가하고 싶다면, 확장하는 대신 독립된 클래스를 만들고, 이 클래스에 원래 클래스의 인스턴스를 가리키는 필드를 두자.  
이 후, 내부 인스턴스를 반환하는 '뷰' 메서드를 제공하면 된다.  
-> 바깥 클래스에 원하는 compareTo 메서드를 구현해넣을 수 있다.

마지막 규약은 필수는 아니지만 꼭 지키길 권한다. compareTo 메서드로 수행한 동치성 테스트의 결과가 equals와 같아야 한다는 것이다. compareTo로 줄지은 순서와 equals의 결과가 일관되게 한다.

## compareTo 메서드 작성 요령

Comparable은 타입을 인수로 받는 제네릭 인터페이스이므로 compareTo 메서드의 인수타입은 컴파일 타임에 정해진다. 입력 인수 타입을 확인하거나 형변환할 필요가 없다.

compareTo 메서드는 각 필드가 동치인지를 비교하는 게 아니라 그 순서를 비교한다. 객체 참조 필드를 비교하려면 compareTo 메서드를 재귀적으로 호출한다.

**Comparable을 구현하지 않은 필드나 표준이 아닌 순서로 비교해야 한다면 비교자(Comparator)를 대신 사용한다.** 비교자는 직접 만들거나 자바가 제공하는 것 중에 골라 쓰면 된다.  
// 참고: [Comparable, Comparator 차이](https://velog.io/@ovan/Comparable-Comparator)

```java
public final class CaseInsensitiveString implements Comparable<CaseInsensitiveString> {
    public int compareTo(CaseInsensitiveString cis) {
        return String.CASE_IMSENSITIVE_ORDER.compare(s, cis.s);
    }
    ...
}
```

CaseInsensitiveString의 참조는 CaseInsensitiveString 참조만 비교할 수 있다는 뜻으로, Comparable을 구현할 때 일반적으로 따르는 패턴이다.  
**compareTo 메서드에서 관계 연산자인 <와 >를 사용하는 이전 방식은 거추장스럽고 오류를 유발하니 추천하지 않는다.**

### 핵심 필드가 여러 개일 때?

어느 것을 먼저 비교하느냐가 중요해진다. 가장 핵심적인 필드부터 비교해나가야 한다. 비교 결과가 0이 아니라면, 즉 순서가 결정되면 거기서 끝이다.  
똑같지 않은 필드를 찾을 때까지 그 다음으로 중요한 필드를 비교해나간다.

```java
public int compareTo(PhoneNumber pn) {
    int result = Short.compare(areaCode, pn.areaCode); // 가장 중요한 필드
    if (result == 0) {
        result = Short.compare(prefix, pn.prefix); // 두 번째로 중요한 필드
        if (result == 0)
            result = Short.compare(lineNum, pn.lineNum); // 세 번째로 중요한 필드
    }
    return result;
}
```

## Java 8에서의 Comparator 인터페이스

비교자 생성 메서드(comparator construction method)와 팀을 꾸려 메서드 연쇄 방식으로 비교자를 생성할 수 있다. 이 비교자들을 Comparable 인터페이스가 원하는 compareTo 메서드를 구현하는데 멋지게 활용할 수 있다.

그러나, 약간의 성능 저하가 뒤따른다.

```java
private static final Comparator<PhoneNumber> COMPARATOR =
    comparingInt((PhoneNumber pn) -> pn.areaCode)
        .thenComparingInt(pn -> pn.prefix)
        .thenComparingInt(pn -> pn.lineNum);

public int compareTo(PhoneNumber pn) {
    return COMPARATOR.compare(this, pn);
}
```

비교자 생성 메서드 2개를 이용해 비교자를 생성한다.

첫 번째인 comparingInt는 객체 참조를 int 타입 키에 매핑하는 키 추출 함수를 인수로 받아, 그 키를 기준으로 순서를 정하는 비교자를 반환하는 정적 메서드다.

그 다음 comparingInt는 람다를 인수로 받으며, 람다는 PhoneNumber에서 추출한 지역 코드를 기준으로 전화번호의 순서를 정하는 Comparator<PhoneNumber>를 반환한다.

두 번째 비교자 생성 메서드인 thenComparingInt를 수행할 때는 타입을 명시하지 않아도 자바의 타입 추론 능력이 있어 추론할 수 있다.

## Comparator

Comparator는 수많은 보조 생성 메서드들을 가지고 있다.

long과 double용으로는 comparingInt, thenComparingInt의 변형 메서드를 준비했다.

short처럼 더 작은 정수 타입에는 int용 버전을 사용하면 된다.

객체 참조용 비교자 생성 메서드도 준비되어 있다. comparing이라는 정적 메서드 2개가 다중정의되어 있다.  
첫 번째는 키 추출자를 받아서 그 키의 자연적 순서를 사용한다.  
두 번째는 키 추출자 하나와 추출된 키를 비교할 비교자까지 총 2개의 인수를 받는다.

또한 thenComparing이란 인스턴스 메서드가 3개 다중정의되어 있다.  
첫 번째는 비교자 하나만 인수로 받아 그 비교자로 부차 순서를 정한다.  
두 번째는 키 추출자를 인수로 받아 그 키의 자연적 순서로 보조 순서를 정한다.  
세 번째는 키 추출자 하나의 추출된 키를 비교할 비교자까지 총 2개의 인수를 받는다.

```java
// 해시코드 값의 차를 기준으로 비교하는 비교자 - 추이성을 위배함
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return o1.hashCode() - o2.hashCode();
    }
};
```

이 방식은 정수 오버플로를 일으키거나 부동소수점 계산 방식에 따른 오류를 낼 수 있다.  
따라서 다음 두 방식 중 하나만을 사용하자.

```java
// 정적 compare 메서드를 활용한 비교자
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return Integer.compare(o1.hashCode(), o2.hashCode());
    }
};

// 비교자 생성 메서드를 활용한 비교자
static Comparator<Object> hashCodeOrder =
    Comparator.comparingInt(o -> o.hashCode());
```

## 핵심 정리

- 순서를 고려해야 하는 값 클래스를 작성한다면 꼭 Comparable 인터페이스를 구현하여, 그 인스턴스들을 쉽게 정렬하고, 검색하고, 비교 기능을 제공하는 컬렉션과 아우러지도록 해야 한다.
- compareTo 메서드에서 필드의 값을 비교할 때 <와 > 연산자는 쓰지 말아야 하낟.
- 대신 박싱된 기본 타입 클래스가 제공하는 정적 compare 메서드나 Comparator 인터페이스가 제공하는 비교자 생성 메서드를 사용하자.
