# 아이템 18. 상속보다는 컴포지션을 사용하라

상속은 코드를 재사용하는 강력한 수단이지만, 항상 최선은 아니다. 잘못 사용하면 오류를 내기 쉬운 소프트웨어를 만들게 된다. 상위 클래스와 하위클래스를 모두 같은 프로그래머가 통제하는 패키지 안에서라면 상속도 안전한 방법이다. 확장할 목적으로 설계되었고 문서화도 잘 된 클래스도 마찬가지로 안전하다.

**일반적인 구체 클래스를 패키지 경계를 넘어, 즉 다른 패키지의 구체 클래스를 상속하는 일은 위험하다.**  
// 여기서 말하는 '상속'은 (클래스가 다른 클래스를 확장하는) 구현 상속을 말한다.

## 구체 클래스 상속의 위험성

참고로, 이 문제는 (클래스가 인터페이스를 구현하거나 인터페이스가 다른 인터페이스를 확장하는) 인터페이스 상속과는 무관하다.

**메서드 호출과 달리 상속은 캡슐화를 깨뜨린다.** 상위 클래스에 따라 하위 클래스의 동작에 문제가 생길 수 있기 때문이다.

HashSet을 사용하는 예시로, 성능을 높이기 위해 HashSet이 처음 생성된 이후 원소가 몇 개 더해졌는지 알고자 다음 코드를 작성했다고 하자.

```java
public class InstrumentedHashSet<E> extends HashSet<E> {
    // 추가된 원소의 수
    private int addCount = 0;

    public InstrumentedHashSet() {}

    public InstrumentedHashSet(int initCap, float loadFactor) {
        super(initCap, loadFactor);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}
```

이 클래스는 잘 구현된 것처럼 보이지만 제대로 작동하지 않는다.

```java
InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
s.addAll(List.of("틱", "탁탁", "펑"));
```

`getAddCount` 메서드를 호출하면 3을 반환할 것이라 기대하지만, 실제로는 6을 반환한다.  
그 원인은 HashSet의 addAll 메서드가 add 메서드를 사용해 구현됐기 때문이다.

InstrumentedHashSet의 addAll은 addCount에 3을 더한 뒤 HashSet의 addAll 구현을 호출했다. HashSet의 addAll은 각 원소를 add 메서드를 호출해 추가하는데, 이때 불리는 add는 InstrumentedHashSet에서 재정의한 메서드이다. 따라서 addCount에 값이 중복해서 더해졌기 때문에 의도한 결과가 나오지 않는다.

이처럼 자신의 다른 부분을 사용하는 **'자기사용(self-use)'** 여부는 해당 클래스의 내부 구현 방식에 해당하며, 하위 클래스가 깨지기 쉬운 이유가 된다.

## 하위 클래스가 깨지기 쉬운 이유들

- **상위 클래스의 메서드를 재정의하여 하위 클래스의 로직을 방어한다.** (HashSet의 addAll() 예제)

  - 상위클래스의 메서드 동작을 다시 구현하는게 어렵다.
  - 시간도 더든다.
  - 오류나 성능을 떨어뜨릴 수 있다.
  - 하위클래스에서 접근 불가한 private 클래스를 써야하면 구현이 불가능하다.

- **상위 클래스 릴리즈에서 새로운 메서드를 추가했을 때를 고려해야한다.**

  - 상위 클래스의 다음 릴리즈에서 나온 새로운 메서드를 이용해 하위 클래스에서 '허용되지 않은' 원소를 추가할 수 있는 사태가 생길 수 있다. (ex. Hashtable, Vector를 컬렉션 프레임워크에 포함시킨 것)

- **하위클래스에 추가한 새 메서드가, 상위 클래스 다음 릴리즈에서 같은 시그니처를 가질 때**
  - 운 없게도 하필 하위 클래스에 추가한 메서드와 시그니처가 같고 반환 타입은 다르다면 컴파일조차 되지 않는다. 또한 상위 클래스의 메서드가 요구하는 규약을 만족하지 못할 가능성이 크다.

## 컴포지션 설계

위 문제들을 모두 피해가는 묘안은 **기존 클래스를 확장하는 대신, 새로운 클래스를 만들고 private 필드로 기존 클래스의 인스턴스를 참조하게 하는 것이다.**

- **컴포지션(composition)**: 기존 클래스가 새로운 클래스의 구성요소로 쓰인다.
- **전달(forwarding)**: 새 클래스의 인스턴스 메서드들은 기존 클래스의 대응하는 메서드를 호출해 그 결과를 반환한다.
- **전달 메서드(forwarding method)**: 새 클래스의 메서드

이를 적용하면, 새로운 클래스는 기존 클래스의 내부 구현 방식의 영향에서 벗어나며, 심지어 기존 클래스에 새로운 메서드가 추가되더라도 전혀 영향받지 않는다.

```java
// 래퍼 클래스 - 상속 대신 컴포지션을 사용했다.
public class InstrumentedSet<E> extends ForwardingSet<E> {
    private int addCount = 0;

    public InstrumentedSet(Set<E> s) {
        super(s);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount();
    }
}
```

```java
// 재사용할 수 있는 전달 클래스
public class ForwardingSet<E> implements Set<E> {
    private final Set<E> s;
    public ForwardingSet(Set<E> s) { this.s = s;}

    public void clear() { s.clear(); }
    public boolean contains(Object o) {return s.contains(o); }
    public boolean isEmpty() { return s.isEmpty(); }
    public int size() { return s.size(); }
    public Iterator<E> iterator() { return s. iterator(); }
    public boolean add(E e) { return s.add(e); }
    public boolean remove(Object o) {return s.remove(o); }
    public boolean containsAll(Collection<?> c) { return s.containsAll(c); }
    public boolean addAll(Collection<? extends E> c) { return s.addAll(c); }
    public boolean removeAll(Collection<?> c) { return s.removeAll(c); }
    public boolean retainAll(Collection<?> c) { return s.retainAll(c); }
    public Object[] toArray() { return s.toArray(); }
    public <T> T[] toArray(T[] a) { return s.toArray(a); }
    @Override public boolean equals(Object o) { return s.equals(o); }
    @Override public int hashCode() {return s.hashCode(); }
    @Override public String toString() { return s.toString(); }
}
```

ForwardingSet: 전달 메서드만으로 이뤄진 재사용 가능한 전달 클래스

InstrumentedSet은 HashSet의 모든 기능을 정의한 Set 인터페이스를 활용해 설계되어 견고하고 아주 유연하다. 구체적으로는 Set 인터페이스를 구현했고, Set의 인스턴스를 인수로 받는 생성자를 하나 제공한다. 임의의 Set에 계측 기능을 덧씌워 새로운 Set으로 만드는 것이 이 클래스의 핵심이다.

상속 방식은 구체 클래스 각각을 따로 확장해야 하며, 지원하고 싶은 상위 클래스의 생성자 각각에 대응하는 생성자를 별도로 정의해줘야 한다.  
하지만, 컴포지션 방식은 한 번만 구현해두면 어떠한 Set 구현체라도 계측할 수 있으며, 기존 생성자들과도 함께 사용할 수 있다.

```java
Set<Instance> times = new InstrumentedSet<>(new TreeSet<>(cmp));
Set<E> s = new InstrumentedSet<>(new HashSet<>(INIT_CAPACITY));
```

다른 Set 인스턴스를 감싸고(wrap) 있다는 뜻에서 InstrumentedSet 같은 클래스를 **래퍼 클래스**라 하며, **다른 Set에 계측 기능을 덧씌운다는 뜻에서 데코레이터 패턴(Decorator pattern) 이라고 한다.**

컴포지션과 전달의 조합은 넓은 의미로 위임(delegation)이라고 부른다. 엄밀히 따지자면, 래퍼 객체가 내부 객체에 자기 자신의 참조를 넘기는 경우에만 해당한다.

// [참고: 데코레이터 패턴이란](https://gmlwjd9405.github.io/2018/07/09/decorator-pattern.html)

래퍼 클래스는 단점이 거의 없다. **래퍼 클래스가 콜백(callback) 프레임워크와는 어울리지 않는다는 점만 주의하면 된다.**  
// SELF 문제

## 상속의 원칙

- 하위 클래스가 상위 클래스의 '진짜' 하위 타입인 상황에서만 사용한다.
- 클래스 B가 클래스 A와 is-a 관계일 때만 클래스 A를 상속해야 한다.

자바 플랫폼 라이브러리에서도 이 원칙을 명백히 위반한 클래스들이 있다.  
ex) Vector을 확장한 Stack, Hashtable을 확장한 Properties

컴포지션을 써야 할 상황에서 상속을 사용하는 건 내부 구현을 불필요하게 노출하는 꼴이다.

- 그 결과 API가 내부 구현에 묶이고 그 클래스의 성능도 영원히 제한된다.
- 더 큰 문제는 클라이언트가 노출된 내부에 직접 접근할 수 있다는 점이다.
- 가장 심각한 문제는 클라이언트에서 상위 클래스를 직접 수정하여 하위 클래스의 불변식을 해칠 수 있다는 사실이다.

## 핵심 정리

- 상속은 강력하지만 캡슐화를 해친다는 문제가 있다.
- 상속은 상위 클래스와 하위 클래스가 순수한 is-a 관계일 때만 써야 한다.
- is-a 관계일 때도 안심할 수만은 없는 것은, 하위 클래스의 패키지가 상위 클래스와 다르고, 상위 클래스가 확장을 고려해 설계되지 않았다면 여전히 문제가 될 수 있다.
- 상속의 취약점을 피하려면 상속 대신 컴포지션과 전달을 사용하자.
- 특히 래퍼 클래스로 구현할 적당한 인터페이스가 있다면 더욱 그렇다.
- 래퍼 클래스는 하위 클래스보다 견고하고 강력하다.
