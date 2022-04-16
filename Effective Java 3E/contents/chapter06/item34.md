# 아이템 34. int 상수 대신 열거 타입을 사용하라

## 열거 타입

열거 타입은 일정 개수의 상수 값을 정의한 다음, 그 외의 값은 허용하지 않는 타입이다.  
Java에서 열거 타입을 지원하기 전에는 다음 코드처럼 정수 상수를 한 묶음 선언해서 사용하곤 했다.

```java
public static final int APPLE_FUJI = 0;
public static final int APPLE_PIPPIN = 1;
public static final int APPLE_GRANNY_SMITH = 2;

public static final int ORANGE_NAVEL = 0;
public static final int ORANGE_TEMPLE = 1;
public static final int ORANGE_BLOOD = 2;
```

### 정수 열거 패턴(int enum pattern)

이 기법에는 단점이 많다. 타입 안전을 보장할 방법이 없으며 표현력도 좋지 않다. 오렌지를 건네야 할 메서드에 사과를 보내고 동등 연산자(==)로 비교하더라도 컴파일러는 아무런 경고 메시지를 출력하지 않는다.

```java
// 향긋한 오렌지 향의 사과 소스
int i = (APPLE_FUJI - ORANGE_TEMPLE) / APPLE_PIPIN;
```

Java가 정수 열거 패턴을 위한 별도 이름공간(namespace)을 지원하지 않기 때문에 어쩔 수 없이 접두어를 써서 이름 충돌을 방지하는 것이다.

정수 열거 패턴을 사용한 프로그램은 깨지기 쉽다. 평범한 상수를 나열한 것 뿐이라 컴파일하면 그 값이 클라이언트 파일에 그대로 새겨진다. 따라서 상수의 값이 바뀌면 클라이언트도 반드시 다시 컴파일해야 한다. 다시 컴파일하지 않은 클라이언트는 실행이 되더라도 엉뚱하게 동작할 것이다.

정수 상수는 문자열로 출력하기가 다소 까다롭다. 그 값을 출력하거나 디버거로 살펴보면 (의미가 아닌) 단지 숫자로만 보여서 썩 도움이 되지 않는다. 같은 정수 열거 그룹에 속한 모든 상수를 한 바퀴 순회하는 방법도 마땅치 않다. 심지어 그 안에 상수가 몇 개인지도 알 수 없다.

### 문자열 열거 패턴(string enum pattern)

상수의 의미를 출력할 수 있다는 점은 좋지만, 이 변형 패턴은 더 나쁘다.  
문자열 상수의 이름 대신 문자열 값을 그대로 하드코딩하게 만들기 때문이다. 하드코딩한 문자열에 오타가 있어도 컴파일러는 확인할 길이 없으니 자연스럽게 런타임 버그가 생긴다. 문자열 비교에 따른 성능 저하 역시 당연한 결과다.

## 열거 타입의 출현

열거 패턴의 단점을 말끔히 씻어주는 동시에 여러 장점을 안겨주는 대안이 바로 열거 타입이다.

```java
public enum Apple { FUJI, PIPPIN, GRANNY_SMITH }
public enum Orange { NAVEL, TEMPLE, BLOOD }
```

Java의 열거 타입은 C, C++, C# 같은 다른 언어의 열거 타입과 비슷하지만 보이는 것이 다가 아니다. Java의 열거 타입은 완전한 형태의 클래스라서 (단순한 정수값일 뿐인) 다른 언어의 열거 타입보다 훨씬 강력하다.

열거 타입 자체는 클래스이며, 상수 하나당 자신의 인스턴스를 하나씩 만들어 `public static final` 필드로 공개한다. 열거 타입은 밖에서 접근할 수 있는 생성자를 제공하지 않으므로 사실상 `final`이다.

**따라서 클라이언트가 인스턴스를 직접 생성하거나 확장할 수 없으니 열거 타입 선언으로 만들어진 인스턴스들은 딱 하나씩만 존재함이 보장된다. 다시 말해 열거 타입은 인스턴스 통제된다. 싱긅턴은 원소가 하나뿐인 열거 타입이라 할 수 있고, 거꾸로 열거 타입은 싱글턴을 일반화한 형태라고 볼 수 있다.**

### 열거 타입은 컴파일타임 타입 안전성을 제공한다.

다른 타입의 값을 넘기려하면 컴파일 오류가 발생한다. 타입이 다른 열거 타입 변수에 할당하려 하거나 다른 열거 타입의 값끼리 == 연산자로 비교하려는 꼴이기 때문이다.

### 열거 타입에는 각자의 이름공간이 있어서 이름이 같은 상수도 평화롭게 공존한다.

열거 타입에 새로운 상수를 추가하거나 순서를 바꿔도 다시 컴파일하지 않아도 된다. 공개되는 것이 오직 필드의 이름뿐이라, 정수 열거 패턴과 달리 상수 값이 클라이언트로 컴파일되어 각인되지 않기 때문이다.

열거 타입의 `toString` 메서드는 출력하기에 적합한 문자열을 내어준다.

열거 타입에는 임의의 메서드나 필드를 추가할 수 있고 임의의 인터페이스를 구현하게 할 수도 있다.

- `Object` 메서드들을 높은 품질로 구현
- `Comparable`, `Serializable` 구현
- 직렬화 형태도 웬만큼 변형을 가해도 문제없이 동작하게끔 구현해놨다.

## 열거 타입에 메서드, 필드 추가

각 상수와 연관된 데이터를 해당 상수 자체에 내재시키고 싶다고 가정하자.  
Apple과 Orange를 예로 들면, 과일의 색을 알려주거나 과일 이미지를 반환하는 메서드를 추가하고 싶을 수 있다.

열거 타입에는 어떤 메서드도 추가할 수 있다. 가장 단순하게는 그저 상수 모음일 뿐인 열거 타입이지만, (실제로는 클래스이므로) 고차원의 추상 개념 하나하나를 완벽히 표현해낼 수도 있는 것이다.

```java
public enum Planet {
    MERCURY(3.302e+23, 2.439e6),
    VENUS(4.869e+24, 6.052e6),
    EARTH(5.975e+24, 6.378e6),
    MARS(6.419e+23, 3.393e6),
    JUPITER(1.899e+27, 7.149e7),
    SATURN(5.685e+26, 6.027e7),
    URANUS(8.683e+25, 2.556e7),
    NEPTUNE(1.024e+26, 2.477e7);

    private final double mass; // 질량(단위: 킬로그램)
    private final double radius; // 반지름(단위: 미터)
    private final double sufaceGravity; // 표면중력(단위: m / s^2)

    // 중력상수(단위: m^3 / kg s^2)
    private static final dobule G = 6.67300E-11;

    // 생성자
    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
        sufaceGravity = G * mass / (radius * radius);
    }

    public double mass() { return mass; }
    public double radius() { return radius; }
    public double surfaceGravity() { return surfaceGravity; }

    public double surfaceWeight(double mass) {
        return mass * surfaceGravity; // F = ma
    }

}
```

**열거 타입 상수 각각을 특정 데이터와 연결지으려면 생성자에서 데이터를 받아 인스턴스 필드에 저장하면 된다.** 열거 타입은 근본적으로 불변이라 모든 필드는 final이어야 한다.(item 17)

필드를 public으로 선언해도 되지만, private으로 두고 별도의 public 접근자 메서드를 두는게 낫다.(item 16)

Planet의 생성자에서 표면 중력을 계산해 저장한 이유는 단순히 최적화를 위해서다.

Planet의 열거 타입은 단순하지만 놀랍도록 강력하다. 어떤 객체의 지구에서의 무게를 입력받아 여덟 행성에서의 무게를 출력하는 일은 다음으로 짧게 코드로 작성이 가능하다.

```java
public class WeightTable {
    public static void main(String[] args) {
        double earthWeight = Double.parseDouble(args[0]);
        double mass = earthWeight / Planet.EARTH.surfaceGravity();
        for (Planet p : Planet.values()) {
            System.out.printf("%s에서의 무게는 %fdlek. %n", p, p.surfaceWeight(mass));
        }
    }
}
```

열거 타입은 자신 안에 정의된 상수들의 값을 배열에 담아 반환하는 정적 메서드인 `values`를 제공한다. 값들은 선언된 순서로 저장된다.  
각 열거 타입 값의 `toString` 메서드는 상수 이름을 문자열로 반환하므로 `println`과 `printf`로 출력하기에 안성맞춤이다.

```log
// args[0] = "185"
MERCURY에서의 무게는 69.912739이다.
VENUS에서의 무게는 167.434436이다.
EARTH에서의 무게는 185.000000이다.
MARS에서의 무게는 70.226739이다.
JUPITER에서의 무게는 467.990696이다.
SATURN에서의 무게는 197.120111이다.
URANUS에서의 무게는 167.398264이다.
NEPTUNE에서의 무게는 210.208751이다.
```

## 열거 타입에서 상수를 제거하면?

제거한 상수를 참조하지 않는 클라이언트에는 아무 영향이 없다.

제거된 상수를 참조하는 클라이언트는 어떻게 될까?  
클라이언트 프로그램을 다시 컴파일하면 제거된 상수를 참조하는 줄에서 디버깅에 유용한 메시지를 담은 컴파일 오류가 발생할 것이다.

## 열거 타입의 유용한 기능

열거 타입을 선언한 클래스 혹은 그 패키지에서만 유용한 기능은 private나 package-private 메서드로 구현한다. 이렇게 구현된 열거 타입 상수는 자신을 선언한 클래스 혹은 패키지에서만 사용할 수 있는 기능을 담게 된다. 일반 클래스와 마찬가지로, 그 기능을 클라이언트에 노출해야 할 합당한 이유가 없다면 private으로, 혹은 (필요하다면) package-private으로 선언하라.(item 15)

## 널리 쓰이는 열거 타입

**널리 쓰이는 열거 타입은 톱레벨 클래스로 만들고, 특정 톱레벨 클래스에서만 쓰인다면 해당 클래스의 멤버 클래스(item 24)로 만든다.** `java.math.RoundingMode`의 사례는 이 개념을 많은 곳에서 사용하여 다양한 API가 더 일관된 모습을 갖출 수 있도록 장려한 것이다.

## 상수마다 동작이 달라지는 열거 타입

ex) 사칙연산 계산기 예제

### 1. 값에 따라 분기하는 열거 타입

```java
public enum Opreation {
    PLUS, MINUS, TIMES, DIVIDE;

    public double apply(double x, double y) {
        switch(this) {
            case PLUS: return x + y;
            case MINUS: return x - y;
            case TIMES: return x * y;
            case DIVIDE: return x / y;
        }
        throw new AssertionError("알 수 없는 연산: " + this);
    }
}
```

위 코드는 동작은 하지만 그리 예쁘지는 않다. 마지막의 throw 문은 실제로는 도달할 일이 없지만 기술적으로는 도달할 수 있기 때문에 생략하면 컴파일조차 되지 않는다.  
더 나쁜 점은 깨지기 쉬운 코드라는 사실이다. 예컨대 새로운 상수를 추가하면 해당 case 문도 추가해야 한다. 혹시라도 추가하지 않는다면, 컴파일은 되지만 새로 추가된 연산을 할 때마다 "알 수 없는 연산" 이라는 런타임 오류를 내며 프로그램이 종료된다.

### 2. 상수별 메서드 구현을 활용한 열거 타입

```java
public enum Operation {
    PLUS { public double apply(double x, double y) { return x + y;}},
    MINUS { public double apply(double x, double y) { return x - y;}},
    TIMES { public double apply(double x, double y) { return x * y;}},
    DIVIDE { public double apply(double x, double y) { return x / y;}},
}
```

apply 메서드가 상수 선언 바로 옆에 붙어 있으니 새로운 상수를 추가할 때 apply도 재정의해야 한다는 사실을 깜빡하기는 어려울 것이다. 그뿐만 아니라 apply가 추상 메서드이므로 재정의하지 않는다면 컴파일 오류로 알려준다.

### 3. 상수별 클래스 몸체(class body)와 데이터를 사용한 열거 타입

```java
public enum Operation {
    PLUS("+") {
        public double apply(double x, double y) { return x + y; }
    },
    MINUS("-") {
        public double apply(double x, double y) { return x - y; }
    },
    TIMES("*") {
        public double apply(double x, double y) { return x * y; }
    },
    DIVIDE("/") {
        public double apply(double x, double y) { return x / y; }
    };

    private final String symbol;

    Operation(String symbol) { this.symbol = symbol; }

    @Override
    public String toString() {
        return symbol;
    }

    public abstract double apply(double x, double y);
}

public class OperationTest {
    public static void main(String[] args) {
        double x = Double.parseDouble("2");
        double y = Double.parseDouble("4");
        for (Operation op : Operation.values()) {
            System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
        }
    }
}
```

열거 타입에는 상수 이름을 입력받아 그 이름에 해당하는 상수를 반환해주는 `valueOf(String)` 메서드가 자동 생성된다. 한편, 열거 타입의 `toString()` 메서드를 재정의할 때, `toString`이 반환하는 문자열을 해당 열거 타입 상수로 변환해주는 `fromString` 메서드도 함께 제공하는 걸 고려해보자.

### fromString

```java
public static final Map<String, Operation> stringToEnum =
    Stream.of(values()).collect(
        toMap(Object::toString, e -> e);
    )

// 지정한 문자열에 해당하는 Operation을 (존재한다면) 반환한다.
public static Optional<Operation> fromString(String symbol) {
    return Optional.ofNullable(stringToEnum.get(symbol));
}
```

Opration 상수가 stringToEnum 맵에 추가되는 시점은 열거 타입 상수 생성 후 정적 필드가 초기화될 때다. 앞의 코드는 values 메서드가 반환하는 배열 대신 스트림을 사용했다.  
열거 타입 상수는 생성자에서 자신의 인스턴스를 맵에 추가할 수 없다. 이 방식이 허용되었다면 런타임에 NPE가 발생했을 것이다.  
열거 타입의 정적 필드 중 열거 타입의 생성자에서 접근할 수 있는 것은 상수 변수 뿐이다.(item 24) 열거 타입 생성자가 실행되는 시점에는 정적 필드들이 아직 초기화되기 전이라, 자기 자신을 추가하지 못하게 하는 제약이 꼭 필요하다.  
이 제약의 특수한 예로, 열거 타입 생성자에서 같은 열거 타입의 다른 상수에도 접근할 수 없다.

fromString이 `Optional<Operation>`을 반환하는 점도 주의하자. 이는 주어진 문자열이 가리키는 연산이 존재하지 않을 수 있음을 클라이언트에 알리고, 그 상황을 클라이언트에서 대처하도록 한 것이다.

## 상수별 메서드 구현의 단점

열거 타입 상수끼리 코드를 공유하기 어렵다는 단점이 있다.

### 1. 값에 따라 분기하여 코드를 공유하는 열거 타입

```java
enum PayrollDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    private static final int MINS_PER_SHIFT = 8 * 60;

    int pay(int minutesWorked, int payRate) {
        int basePay = minutesWorked * payRate;

        int overtimePay;
        switch(this) {
            case SATURDAY : case SUNDAY: // 주말
                overtimePay = basePay / 2;
                break;
            default: // 주중
                overtimePay = minutesWorked <= MINS_PER_SHIFT ?
                  0 : (minutesWorked - MINS_PER_SHIFT) * payRate / 2;
        }
        return basePay + overtimePay;
    }
}
```

분명 간결하지만, 관리 관점에서는 위험한 코드다. 휴가와 같은 새로운 값을 열거 타입에 추가하려면 그 값을 처리하는 case 문을 잊지 말고 쌍으로 넣어줘야 하는 것이다. 추가하는 것을 잊는다면 휴가 기간에 열심히 일해도 평일과 똑같은 임금을 받게 된다.

### 상수별 메서드 구현으로 급여를 정확히 계산하는 방법

1. 잔업수당을 계산하는 코드를 모든 상수에 중복해서 넣으면 된다.
2. 계산 코드를 평일용과 주말용으로 나눠 각각을 도우미 메서드로 작성한 다음 각 상수가 자신에게 필요한 메서드를 적절히 호출하면 된다.

두 방식 모두 코드가 장황해져 가독성이 크게 떨어지고 오류 발생 가능성이 높아진다.

가장 깔끔한 방법은 새로운 상수를 추가할 때 잔업수당 '전략'을 선택하도록 하는 것이다.

- 잔업수당 계산을 private 중첩 열거 타입으로 옮기고(PayType 으로)
- PayrollDay 열거 타입의 생성자에서 이 중 적당한 것을 선택한다.

이 패턴은 switch 문보다 복잡하지만 더 안전하고 유연하다.

### 2. 전략 열거 패턴

```java
enum PayrollDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY(PayType.WEEKEND), SUNDAY(PayType.WEEKEND);

    private final PayType payType;

    PayrollDay(PayType payType) { this.payType = payType; }

    int pay(int minutesWorked, int payRate) {
        return payType.pay(minutesWorked, payRate);
    }

    // 전략 열거 타입
    enum PayType {
        WEEKDAY {
            int overtimePay(int minsWorked, int payRate) {
                return minsWorked <= MINS_PER_SHIFT ?
                  0 : (minutesWorked - MINS_PER_SHIFT) * payRate / 2;
            }
        },
        WEEKEND {
            int overtimePay(int minsWorked, int payRate) {
                return minsWorked * payRate / 2;
            }
        };

        abstract int overtimePay(int mins, int payRate);
        private static final int MINS_PER_SHIFT = 8 * 60;

        int pay(int minsWorked, int payRate) {
            int basePay = minsWorked * payRate;
            return basePay + overtimePay(minsWorked, payRate);
        }
    }
}
```

switch 문은 열거 타입의 상수별 동작을 구현하는 데 적합하지 않다. 하지만 **기존 열거 타입에 상수별 동작을 혼합해 넣을 때는 switch 문이 좋은 선택이 될 수 있다.**

### switch문과 열거 타입

```java
public static Operation inverse(Operation op) {
    switch(op) {
        case PLUS: return Operation.MINUS;
        case MINUS: return Operation.PLUS;
        case TIMES: return Operation.DIVDE;
        case DIVIDE: return Operation.TIMES;

        default: throw new AssertionError("알 수 없는 연산: " + op);
    }
}
```

추가하려는 메서드가 의미상 열거 타입에 속하지 않는다면 직접 만든 열거 타입이라도 이 방식을 적용하는 게 좋다.

## 언제 열거 타입을 쓰는가?

대부분의 경우 열거 타입의 성능은 정수 상수와 별반 다르지 않다. 열거 타입을 메모리에 올리는 공간과 초기화하는 시간이 들긴 하지만 체감될 정도는 아니다.

- 필요한 원소를 컴파일타임에 다 알 수 있는 상수 집합이라면 항상 열거 타입을 사용하자.
- 열거 타입에 정의된 상수 개수가 영원히 고정 불변일 필요는 없다. 열거 타입은 나중에 상수가 추가돼도 바이너리 수준에서 호환되도록 설계되었다.

## 핵심 정리

- 열거 타입은 확실히 정수 상수보다 뛰어나다.
- 더 읽기 쉽고 안전하고 강력하다.
- 대다수 열거 타입이 명시적 생성자나 메서드 없이 쓰이지만, 각 상수를 특정 데이터와 연결짓거나 상수마다 다르게 동작하게 할 때는 필요하다.
- 드물게는 하나의 메서드가 상수별로 다르게 동작해야 할 때도 있다.
- 이런 열거 타입에서는 switch 문 대신 상수별 메서드 구현을 사용하자.
- 열거 타입 상수 일부가 같은 동작을 공유한다면 전략 열거 타입 패턴을 사용하자.
