# 아이템 3. private 생성자나 열거 타입으로 싱글턴임을 보증하라

싱글턴(singleton): 인스턴스를 오직 하나만 생성할 수 있는 클래스를 말한다.

보통 함수 같은 Stateless 객체나 설계상 유일해야 하는 시스템 컴포넌트를 싱글턴으로 만든다.  
**클래스를 싱글턴으로 만들면 이를 사용하는 클라이언트를 테스트하기가 어려워질 수 있다.** 타입을 인터페이스로 정의한 다음 그 인터페이스를 구현해서 만든 싱글턴이 아니라면 싱글턴 인스턴스를 가짜(mock) 구현으로 대체할 수 없기 때문이다.

싱글턴을 만드는 방식은 2가지가 있다. 두 방식 모두 생성자는 private로 감춰두고, 유일한 인스턴스에 접근할 수 있는 수단으로 public static 멤버를 하나 마련해둔다.

## 싱글턴을 만드는 방법

### 1. 싱글턴 인스턴스를 public static final 필드로 만들고 생성자를 private로 한다.

```java
public class Elvis {
    public static final Elvis INSTANCE = new Elvis();
    private Elvis() { ... }

    public void leaveTheBuilding() { ... }
}
```

private 생성자는 public static final 필드인 `Elvis.INSTANCE`를 초기화할 때 딱 한 번만 호출된다. public이나 protected 생성자가 없으므로 Elvis 클래스가 초기화될 때 만들어진 인스턴스가 전체 시스템에서 하나뿐임이 보장된다.  
// 리플렉션을 사용해서 private 생성자를 호출하는 방법을 제외하면 생성자는 오직 최초 한번만 호출되고 싱글턴이 된다.

이런 API 사용이 정적 팩터리 메소드를 사용하는 방법에 비해 더 명확하고 더 간단하다.

#### 📌 예외) 리플렉션 API를 사용하면 새로운 인스턴스를 생성할 수 있다.

```java
Constructor<Elvis> constructor =
        (Constructor<Elvis>) elvis.getClass().getDeclaredConstructor();
constructor.setAccessible(true);
Elvis elvis2 = constructor.newInstance();

assertNotSame(elvis, elvis2); // success
```

해결 방법은 생성자에 검증작업을 추가하여 새로운 인스턴스 생성을 막도록 한다.

```java
private Elvis() {
    if(INSTANCE != null) {
        throw new RuntimeException("생성자를 호출할 수 없습니다.");
    }
}
```

### 2. 정적 팩터리 메서드를 public static 멤버로 제공

```java
public class Elvis {
    private static final Elvis INSTANCE = new Elvis();
    private Elvis() { ... }
    public static Elvis getInstance() { return INSTANCE; }

    public void leaveTheBuilding() { ... }
}
```

다음과 같은 장점이 있다.

- API를 변경하지 않고도 싱글턴으로 쓸지 안쓸지 변경이 가능하다.
- 정적 팩터리를 제네릭 싱글턴 팩터리로 만들 수도 있다.
- 정적 팩터리의 메서드 참조를 공급자(supplier)로 사용할 수 있다.

위 두 가지 방식 중 하나의 방식으로 만든 싱글턴 클래스를 직렬화하려면 단순히 `Serializable`을 구현한다고 선언하는 것만으로는 부족하다. 직렬화된 인스턴스를 역직렬화할 때마다 새로운 인스턴스가 만들어지는 문제가 발생하기 때문에, 모든 인스턴스 필드를 일시적(transient)이라고 선언하고 `readResolve()`를 제공해야 한다.

```java
// 싱글턴임을 보장해주는 readResolve 메서드
private Object readResolve() {
    // 진짜 Elvis를 반환하고, 가짜 Elvis는 가비지 컬렉터에 맡긴다.
    return INSTANCE;
}
```

### 3. 원소가 하나인 열거 타입을 선언

```java
public enum Elvis {
    INSTANCE;

    public void leaveTheBuilding() { ... }
}
```

public 필드 방식과 비슷하지만, 더 간결하고, 추가 노력 없이 직렬화할 수 있고, 심지어 아주 복잡한 직렬화 상황이나 리플렉션 공격에서도 제2의 인스턴스가 생기는 일을 완벽히 막아준다.

**코드는 좀 불편하게 느껴지지만 대부분 상황에서는 원소가 하나뿐인 열거 타입이 싱글턴을 만드는 가장 좋은 방법이다.** 하지만, 만드려는 싱글턴이 Enum외의 클래스를 상속해야 한다면 이 방법은 사용할 수 없다.(열거 타입이 다른 인터페이스를 구현하도록 선언할 수는 있다.)

## 참고: 리플렉션(Reflection)?

- 리플렉션은 자바 언어가 가진 특징이다. 리플렉션은 자바 프로그램이 자기 자신(객체)이나 프로그램 내 속성을 조사, 분석하는데 도와주는 기술이다. 예를 들어, 자바 클래스가 클래스 내 멤버 변수에 대한 정보와 해당 멤버 변수를 display 하게 도와준다.
- 실용적인 내용으로는 이미 로딩이 완료된 클래스에서 또 다른 클래스를 동적으로 로딩(Dynamic Loading)하여 생성자, 멤버 필드, 멤버 메서드 등을 사용할 수 있게 도와준다.
- 반사, 투영이라는 사전적인 의미를 가지고 있는데 객체(인스턴스)를 통해 클래스의 정보를 분석해 내는 프로그램 기법을 의미한다.

## References

- [자바봄 Reading Record/이펙티브자바](https://javabom.tistory.com/13?category=833277)
- [자바 리플렉션과 동적 로딩](https://madplay.github.io/post/java-reflection)
