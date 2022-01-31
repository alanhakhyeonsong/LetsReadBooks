# 아이템 10. equals는 일반 규약을 지켜 재정의하라

equals는 재정의하기 쉬워보이지만 곳곳에 함정이 도사리고 있어서 자칫하면 끔찍한 결과를 초래한다. 문제를 회피하는 가장 쉬운 길은 **아예 재정의하지 않는 것이다.**

다음 상황에 해당한다면 재정의하지 않는 것이 최선이다.

- **각 인스턴스가 본질적으로 고유하다.**  
  값을 표현하는 게 아니라 동작하는 개체를 표현하는 클래스가 여기에 해당. Bean에 등록해두는 객체 repository, controller, service 등이 이에 해당할 것 같다.
- **인스턴스의 논리적 동치성(logical equality)를 검사할 일이 없을 때**  
  값을 비교해서 동등한지 비교할 일이 없다면 논리적 동치성 검사를 할 일도 없다는 것이고, 기본적인 Object의 equals로 충분하다.
- **상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 들어맞을 때**  
  Set의 구현체는 AbstractSet이 구현한 equals를 상속받아 쓰는 것.
- **클래스가 private이거나 package-private이고 equals 메서드를 호출할 일이 없을 때**  
  만약 실수로라도 equals가 호출되는 것을 막고 싶다면 다음처럼 구현해두자.

  ```java
  @Override
  public boolean equals(Object o) {
      throw new AssertionError(); // 호출 금지!
  }
  ```

## equals를 재정의 해야하는 경우

객체 식별성(object identity): X - 두 객체가 물리적으로 같은가  
논리적 동치성(logical equality): O

객체 식별성이 아니라 논리적 동치성을 확인해야 하는데, 상위 클래스의 equals가 논리적 동치성을 비교하도록 재정의되지 않았을 때이다. (주로 값 클래스)

```java
public class Fruit {
    private String name; // name이 같을 경우 두 객체는 같다.(논리적 동치성)
}
```

equals가 논리적 동치성을 확인하도록 재정의해두면, 값을 비교하길 원하는 프로그래머의 기대에 부응할 수 있다.  
// Map, Set의 원소로 사용 가능함

값 클래스라도, 값이 같은 인스턴스가 둘 이상 만들어지지 않음을 보장하는 인스턴스 통제 클래스라면 equals를 재정의하지 않아도 된다. Enum도 여기 해당된다. 어차피 논리적으로 같은 값이 2개 이상 만들어지지 않으니 논리적 동치성과 객체 식별성이 사실상 똑같은 의미가 된다.

## equals 메서드의 규약 - 동치관계

**동치 클래스(equivalence class)**: 집합을 서로 같은 원소들로 이루어진 부분집합으로 나누는 연산  
equals 메서드가 쓸모 있으려면 모든 원소가 같은 동치류에 속한 어떤 원소와도 서로 교환할 수 있어야 한다.

### 반사성(reflexivity)

- null이 아닌 모든 참조 값 x에 대해, x.equals(x)는 true
- 객체는 자기 자신과 같아야한다.

```java
public class Fruit {
    private String name;

    public Fruit(String name) {
        this.name = name;
    }

    public static void main() {
        List<Fruit> list = new ArrayList<>();
        Fruit f = new Fruit("apple");
        list.add(f);
        list.contains(f); // false일 경우에는 반사성을 만족하지 못하는 경우
    }
}
```

### 대칭성(symmetry)

- null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)가 true면 y.equals(x)도 true
- 두 객체는 서로에 대한 동치 여부에 똑같이 답해야 한다.

```java
//대칭성을 위반한 클래스
public final class CanseInsensitiveString {
    private final String s;

    public CaseInsensitiveString(String s) {
        this.s = Objects.requireNonNull(s);
    }

    // 대칭성 위배!
    @Override
    public boolean equals(Object o) {
        if (o instanceof CaseInsensitiveString)
            return s.equalsIgnoreCase(((CaseInsensitiveString) o).s);
        if (o instanceof String) // 한 방향으로만 작동한다.
            return s.equalsIgnoreCase((String) o);
        return false;
    }
}
```

```java
CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
String s = "polish";

cis.equals(s); // true
s.equals(cis); // false
```

```java
// 대칭성을 만족하게 수정
@Override
public boolean equals(Object o) {
    return o instanceof CaseInsensitiveString &&
        ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
        // String에 대한 instanceof 부분을 빼고 구현한다.
}
```

### 추이성(transitivity)

- null이 아닌 모든 참조 값 x, y, z에 대해, x.equals(y)가 true이고 y.equals(z)도 true이면 x.equals(z)도 true
- 첫 번째 객체와 두 번째 객체가 같고, 두 번째 객체와 세 번째 객체가 같다면, 첫 번재 객체와 세 번째 객체도 같아야 한다.(삼단논법)

**이 추이성 조건 때문에, equals를 재정의하면 안되는 경우에 superclass에서 equals를 정의한 경우를 언급함**

다음 문제점이 있다.

1. 대칭성 위배 문제에 빠질 수 있음

```java
// ColorPoint.java의 equals
@Override
public boolean equals(Object o) {
    if (!(o instanceof ColorPoint)) return false;
    return super.equals(o) && ((ColorPoint) o).color == color;
}

public static void main() {
    Point p = new Point(1, 2);
    ColorPoint cp = new ColorPoint(1, 2, Color.RED);
    p.equals(cp); // true (Point의 equals로 계산)
    cp.equals(p); // false (ColorPoint의 equals로 계산)
}
```

2. 추이성 위배 문제에 빠질 수 있음

```java
// ColorPoint.java의 equals
@Override
public boolean equals(Object o) {
    if (!(o instanceof Point)) return false;
    if (!(o instanceof ColorPoint)) return o.equals(this);
    return super.equals(o) && ((ColorPoint) o).color == color;
}

public static void main() {
    ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
    Point p2 = new Point(1, 2);
    ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);
    p1.equals(p2); // true (ColorPoint의 equals로 계산)
    p2.equals(p3); // true (Point의 equals로 계산)
    p1.equals(p1)l // false (ColorPoint의 equals로 계산)
}
```

3. 무한 재귀에 빠질 수 있다.

```java
// SmallPoint.java의 equals
@Override
public boolean equals(Object o) {
    if (!(o instanceof Point)) return false;
    if (!(o instanceof SmallPoint)) return o.equals(this);
    return super.equals(o) && ((SmallPoint) o).color == color;
}

public static void main() {
    ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
    SmallPoint p2 = new SmallPoint(1, 2);
    p1.equals(p2);
    // 처음에 ColorPoint의 equals로 비교: 2번째 if문 때문에 SmallPoint의 equals로 비교
    // 이후 SmallPoint의 equals로 비교: 2번째 if문 때문에 ColorPoint의 equals로 비교
    // 무한 재귀의 상태
}
```

**구체 클래스를 확장해 새로운 값을 추가하면서 equals 규약을 만족시킬 방법은 존재하지 않는다.**

그렇다고 instanceof 검사 대신 getClass 검사를 하라는 것이 아니다.

```java
@Override
public boolean equals(Object o) {
    if (o == null || o.getClass() != getClass()) return false;
    Point p = (Point) o;
    return p.x == x && p.y == y;
}
```

**리스코프 치환원칙을 위배한다**: Point의 하위 클래스는 정의상 여전히 Point이므로 어디서든 Point로 활용될 수 있어야 한다.

**리스코프 치환원칙(Liskov substitution principle)**: 어떤 타입에 있어 중요한 속성이라면 그 하위 타입에서도 마찬가지로 중요하다. 따라서 그 타입의 모든 메서드가 하위 타입에서도 똑같이 잘 작동해야 한다.

구체 클래스의 하위 클래스에서 값을 추가할 방법은 없지만 "상속 대신 컴포지션을 사용하라"는 괜찮은 우회 방법이 있다.

```java
public class ColorPoint {
    private final Point point;
    private final Color color;

    public ColorPoint(int x, int y, Color color) {
        point = new Point(x, y);
        this.color = Objects.requireNonNull(color);
    }

    // 이 ColorPoint의 Point 뷰를 반환한다.
    public Point asPoint() {
        return point;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColorPoint)) return false;
        ColorPoint cp = (ColorPoint) o;
        return cp.point.equals(point) && cp.color.equals(color);
    }
}
```

### 일관성(consistency)

- null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)를 반복해서 호출하면 항상 true를 반환하거나 항상 false를 반환
- 두 객체가 같다면(어느 하나 혹은 두 객체 모두가 수정되지 않는 한) 앞으로도 영원히 같아야 한다.

가변 객체는 비교 시점에 따라 서로 다를 수도 혹은 같을 수도 있지만, 불변 객체는 한번 다르면 끝까지 달라야 한다.

**클래스가 불변이든 가변이든 equals의 판단에 신뢰할 수 없는 자원이 끼어들면 안된다.** (ex. URL과 매핑된 호스트의 IP 주소) 이런 문제를 피하려면 equals는 항시 메모리에 존재하는 객체만을 사용한 결정적 계산만 수행해야 한다.

### null-아님

- null이 아닌 모든 참조 값 x에 대해, x.equals(null)은 false
- 모든 객체가 null과 같지 않아야 한다.

```java
// 명시적 null 검사
@Override
public boolean equals(Object o) {
    if (o == null) return false;
}
```

```java
// 묵시적 null 검사
@Override
public boolean equals(Object o) {
    if (!(o instanceof MyType)) // instanceof 자체가 타입과 무관하게 null이면 false 반환
        return false;
    MyType mt = (MyType) o;
}
```

## 양질의 equals 메서드를 구현하는 4단계

1. **== 연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.**  
   자기 자신이면 true를 반환. 이는 단순한 성능 최적화용으로, 비교 작업이 복잡한 상황일 때 값어치를 할 것이다.
2. **instanceof 연산자로 입력이 올바른 타입인지 확인한다.**  
   올바른 타입의 경우 equals가 정의된 클래스로 리턴이 되는 것이 일반적이지만, 아닌 경우(구현한 특정 인터페이스) 구현한 (서로 다른) 클래스끼리고 비교할 수 있어야 한다. 이런 구현체들은 인터페이스의 equals를 이용해 비교해야 한다.
3. **입력을 올바른 타입으로 형변환한다.**  
   2번에서 instanceof를 이용했기 때문에 100% 성공한다.
4. **입력 객체와 자기 자신에 대응되는 '핵심' 필드들이 모두 일치하는지 하나씩 검사한다.**  
   2번에서 인터페이스를 사용했다면 입력의 필드 값을 가져올 때도 그 인터페이스의 메서드를 사용해야 한다.

## equals 구현 시 주의할 추가적인 사항들

### 기본타입과 참조타입

기본타입은 == 연산자로 비교하고 참조타입은 equals로 비교한다.

float, double 필드의 경우 `Float.compare(float, float)`, `Double.compare(double, double)`로 비교한다. 이는 특수한 부동소수값 등을 다뤄야 하기 때문이다.  
// `Float.equals(float)`, `Double.equals(double)`은 오토 박싱을 수반할 수 있으니 성능상 좋지 않다.

배열 필드는 원소 각각을 지침대로 비교한다. 모두가 핵심 필드라면, `Arrays.equals()`를 사용한다.

### null 정상값 취급 방지

`Object.equals(object, object)`로 비교하여 `NullPointException` 발생을 예방하자.

### 필드의 표준형을 저장하자.

비교하기 복잡한 필드는 필드의 표준형을 저장한 후 비교하자. 불변 클래스에 제격이다.

### 필드 비교 순서는 equals 성능을 좌우한다.

다를 가능성이 더 크거나 비교 비용이 싼 필드를 우선으로 하자. 핵심필드와 파생필드를 구분해야한다.

### equals 재정의할 땐 hashCode도 반드시 재정의하자.

### 너무 복잡하게 해결하려 들지 말자.

### Object 외의 타입을 매개변수로 받는 equals 메서드는 선언하지 말자.

```java
public boolean equals(MyClass o) // @Override가 아니다. 재정의가 아니라 다중정의 상태.
```

### AutoValue 프레임워크

## 핵심 정리

- 꼭 필요한 경우가 아니면 equals를 재정의하지 말자.
- 많은 경우에 Object의 equals가 우리가 원하는 비교를 정확히 수행해준다.
- 재정의해야 할 때는 그 클래스의 핵심 필드 모두를 빠짐없이, 다섯 가지 규약을 확실히 지켜가며 비교해야 한다.
