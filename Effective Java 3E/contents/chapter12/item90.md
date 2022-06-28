# 아이템 90. 직렬화된 인스턴스 대신 직렬화 프록시 사용을 검토하라
`Serializable`을 구현하기로 결정한 순간 언어의 정상 메커니즘인 생성자 이외의 방법으로 인스턴스를 생성할 수 있게 된다. 버그와 보안 문제가 일어날 가능성이 커진다는 뜻이다. 하지만 **직렬화 프록시 패턴**을 사용하면 위험을 크게 줄일 수 있다.

## 직렬화 프록시 패턴
바깥 클래스의 논리적 상태를 정밀하게 표현하는 중첩 클래스를 설계해 `private static`으로 선언한다. 여기서 중첩 클래스가 바깥 클래스의 직렬화 프록시다.

중첩 클래스의 생성자는 단 하나여야 하며, 바깥 클래스를 매개변수로 받아야 한다. 이 생성자는 단순히 인수로 넘어온 인스턴스의 데이터를 복사한다. 일관성 검사 또는 방어적 복사도 필요 없다. 바깥 클래스와 직렬화 프록시 모두 `Serializable`을 구현해야 한다.

```java
class Period implements Serializable {
    private final Date start;
    private final Date end;

    public Periond(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    private static class SerializableProxy implements Serializable {
        private static final long serialVersionUID = 234098243823485285L;
        private final Date start;
        private final Date end;

        SerializableProxy(Peroid p) {
            this.start = p.start;
            this.end = p.end;
        }

        // Deserialize 할 때 호출됨. 오브젝트를 생성한다.
        private Object readResolve() {
            return new Period(start, end); // public 생성자를 사용한다.
        }
    }

    // 바깥 클래스의 직렬화된 인스턴스를 생성할 수 없다.
    // 직렬화할 때 호출되는데, 프록시를 반환하게 하고 있다.
    // Serialize 할 때 호출된다.
    private Object writeReplace() {
        return new SerializationProxy(this);
    }


    // 직렬화 프록시 패턴용 readObject 메서드
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("프록시가 필요합니다.");
    }
}
```

## 직렬화 프록시 패턴의 장점
위 예제 코드에서 본 것처럼 멤버 필드를 `final`로 선언할 수 있기 때문에 진정한 불변으로 만들 수 있다. 또한 직렬화 프록시 패턴은 역직렬화한 인스턴스와 원래의 직렬화된 클래스가 달라도 정상적으로 작동한다.

대표적인 예시로, `EnumSet`이 있다. 이는 public 생성자 없이 정적 팩터리만 제공한다. 원소의 개수가 64개 이하라면 `RegularEnumSet`을 사용하고 그보다 크면 `JumboEnumSet`을 사용한다.

그런데 원소 64개를 가진 `EnumSet`을 직렬화한 다음 원소 5개를 추가하고 역직렬화하면 어떤 결과가 나타날까? 간단히 역직렬화할 때 `JumboEnumSet`으로 하면 된다. 이게 가능한 이유는 `EnumSet`에는 직렬화 프록시 패턴이 적용되어 있기 때문이다.

```java
// EnumSet의 직렬화 프록시
private static class SerializationProxy <E extends Enum<E>> implements Serializable {
    // 이 EnumSet의 원소 타입
    private final Class<E> elementType;

    // 이 EnumSet 안의 원소들
    private final Enum<?>[] elements;

    SerializationProxy(EnumSet<E> set) {
        elementType = set.elementType;
        elements = set.toArray(new Enum<?>[0]);
    }

    private Object readResolve() {
        EnumSet<E> result = EnumSet.noneOf(elementType);
        for (Enum<?> e : elements)
            result.add((E)e);
        return result;
    }

    private static final long serialVersionUID = 362491234563181265L;
}
```

## 직렬화 프록시 패턴의 한계
- 클라이언트가 멋대로 확장할 수 있는 클래스에는 적용할 수 없다.
- 객체 그래프에 순환이 잇는 클래스에도 적용할 수 없다.

이런 객체의 메서드를 직렬화 프록시의 `readResolve`안에서 호출하려 하면 `ClassCastException`이 발생할 것이다. 직렬화 프록시만 가졌을 뿐 실제 객체는 만들어진 것이 아니기 때문이다. 또한 방어적 복사보다 상대적으로 속도도 느리다.

## 핵심 정리
- 제3자가 확장할 수 없는 클래스라면 가능한 한 직렬화 프록시 패턴을 사용하자.
- 이 패턴이 아마도 중요한 불변식을 안정적으로 직렬화해주는 가장 쉬운 방법일 것이다.