# 아이템 50. 적시에 방어적 복사본을 만들라
## Java의 불변식
Java는 안전한 언어다. 네이티브 메서드를 사용하지 않기에 C/C++ 같이 안전하지 않은 언어에서 흔히 보는 버퍼 오버런, 배열 오버런, 와일드 포인터 같은 메모리 충돌 오류에서 안전하다.  
Java로 작성한 클래스는 시스템의 다른 부분에서 무슨 짓을 하든 그 불변식이 지켜진다. 메모리 전체를 하나의 거대한 배열로 다루는 언어에서는 누릴 수 없는 강점이다.

하지만, 아무리 자바라 해도 다른 클래스로부터의 침범을 아무런 노력 없이 다 막을 수 있는 건 아니다.  
**클라이언트가 우리의 불변식을 깨뜨리려 혈안이 되어 있다고 가정하고 방어적으로 프로그래밍해야 한다.** 적절치 않은 클라이언트로부터 클래스를 보호하는 데 충분한 시간을 투자하는 게 좋다.

**어떤 객체든 그 객체의 허락 없이는 외부에서 내부를 수정하는 일은 불가능하다. 하지만, 주의를 기울이지 않으면 자기도 모르게 내부를 수정하도록 허락하는 경우가 생긴다.**

```java
// 기간을 표현하는 클래스 - 불변식을 지키지 못했다.
public final class Period {
    private final Date start;
    private final Date end;

    /**
     * @param start 시작 시각
     * @param end 종료 시각. 시작 시각보다 뒤여야 한다.
     * @throws IllegalArgumentException 시작 시각이 종료 시각보다 늦을 때 발생한다.
     * @throws NullPointerException start나 end가 null이면 발생한다.
    **/
    public Period(Date start, Date end) {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException(start + " after " + end);
        }
        this.start = start;
        this.end = end;
    }

    public Date start() {
        return start;
    }

    public Date end() {
        return end;
    }

    //...
}
```
위 클래스는 불변식이 무리 없이 지켜질 것 같지만, `Date`가 가변이라는 사실을 이용하면 어렵지 않게 그 불변식을 깨뜨릴 수 있다.

```java
// Period 인스턴스의 내부를 공격해보자.
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
end.setYear(78); // p의 내부를 수정했다.
```

Java 8 이후로는 위 문제를 쉽게 해결할 수 있다.  
`Date` 대신 불변인 `Instant`를 사용하면 된다. (또는 `LocalDateTime`이나 `ZonedDateTime`도 가능함)  
**`Date`는 낡은 API이니 새로운 코드를 작성할 때는 더 이상 사용하면 안된다.** 이에 더해, 레거시 코드에 이 잔재가 많이 남아 있다는 점 역시 고려해야 할 대상이다.

## 방어적 복사(defensive copy)
외부 공격으로부터 `Period` 인스턴스의 내부를 보호하려면 **생성자에서 받은 가변 매개변수 각각을 방어적으로 복사해야 한다.** 그런 다음 `Period` 인스턴스 안에서는 원본이 아닌 복사본을 사용한다.

```java
// 수정한 생성자 - 매개변수의 방어적 복사본을 만든다.
public Period(Date start, Date end) {
    this.start = new Date(start.getTime());
    this.end = new Date(end.getTime());

    if (this.start.compareTo(this.end) > 0) {
        throw new IllegalArgumentException(start + " after " + end);
    }
}
```

생성자를 위와 같이 수정한다면 앞서의 공격은 더 이상 `Period`에 위협이 되지 않는다. **매개변수의 유효성을 검사하기 전에 방어적 복사본을 만들고, 이 복사본으로 유효성을 검사한 점에 주목하자.** 멀티스레딩 환경이라면 원본 객체의 유효성을 검사한 후 복사본을 만드는 그 찰나의 취약한 순간에 다른 스레드가 원본 객체를 수정할 위험이 있기 때문이다. 방어적 복사를 매개변수 유효성 검사 전에 수행하면 이런 위험에서 해방될 수 있다.  
// 검사시점/사용시점 공격 or TOCTOU 공격이라 함.

방어적 복사에 `Date`의 `clone()`을 사용하지 않은 점에도 주목해야 한다.  
`Date`는 final이 아니므로 `clone()`이 `Date`가 정의한 게 아닐 수 있다. `clone()`이 악의를 가진 하위 클래스의 인스턴스를 반환할 수도 있다. 이런 공격을 막기 위해서는 **매개변수가 제3자에 의해 확장될 수 있는 타입이라면 방어적 복사본을 만들 때 `clone()`을 사용해서는 안 된다.**

## 가변 필드의 방어적 복사본을 반환하자
```java
// Period 인스턴스를 향한 두 번째 공격
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
p.end().setYear(78); // p의 내부를 변경했다.
```

앞서 생성자를 수정하면 이전 공격은 막아낼 순 있지만, `Period` 인스턴스는 아직도 변경 가능하다. 접근자 메서드가 내부의 가변 정보를 직접 드러내기 때문이다.

위 두 번째 공격을 막아내려면 단순히 접근자가 **가변 필드의 방어적 복사본을 반환하면 된다.**

```java
// 수정한 접근자 - 필드의 방어적 복사본을 반환한다.
public Date start() {
    return new Date(start.getTime());
}

public Date end() {
    return new Date(end.getTime());
}
```
새로운 접근자까지 갖추면 `Period`는 완벽한 불변으로 거듭난다. `Period` 자신 말고는 가변 필드에 접근할 방법이 없으니 확실하다. 모든 필드가 객체 안에 완벽하게 캡슐화되었다.

## Getter와 Setter 메서드
무분별한 Setter의 남발이 안좋다고 하는 이유 중 한 가지가 이번 아이템과 맥락이 같단 생각이 든다. 개인적인 경험 상, 불가피하게 사용해야 한다면 어쩔 수 없지만, Setter를 제한적으로 제공하고 필요하지 않다면 없애는 것이 좋다.

## 핵심 정리
- 클래스가 클라이언트로부터 받는 혹은 클라이언트로 반환하는 구성요소가 가변이라면 그 요소는 반드시 방어적으로 복사해야 한다.
- 복사 비용이 너무 크거나 클라이언트가 그 요소를 잘못 수정할 일이 없음을 신뢰한다면 방어적 복사를 수행하는 대신 해당 구성요소를 수정했을 때의 책임이 클라이언트에 있음을 문서에 명시하도록 하자.