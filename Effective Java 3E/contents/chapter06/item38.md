# 아이템 38. 확장할 수 있는 열거 타입이 필요하면 인터페이스를 사용하라.
## 열거 타입 확장
열거 타입은 거의 모든 상황에서 타입 안전 열거 패턴보다 우수하다.  
단, 예외가 하나 있다면 타입 안전 열거 패턴은 확장할 수 있으나 열거 타입은 그럴수 없다는 점이다.

타입 안전 열거 패턴은 열거한 값들을 그대로 가져온 다음 값을 더 추가하여 다른 목적으로 쓸 수 있는 반면, 열거 타입은 그렇게 할 수 없다.

사실 대부분의 상황에서 열거 타입을 확장하는 건 좋지 않은 생각이다. 확장한 타입의 원소는 기반 타입의 원소로 취급하지만 그 반대는 성립하지 않는다면 이상할 것이다. 기반 타입과 확장된 타입들의 원소 모두를 순회할 방법도 마땅치 않다. 마지막으로, 확장성을 높이려면 고려할 요소가 늘어나 설계와 구현이 더 복잡해진다.

## 연산 코드 예제
확장할 수 있는 열거 타입이 어울리는 쓰임은 바로 연산 코드(opcode)다. 연산 코드의 각 원소는 특정 기계가 수행하는 연산을 뜻한다. 이따금 API가 제공하는 기본 연산 외에 사용자 확장 연산을 추가할 수 있도록 열어줘야 할 때가 있다.

열거 타입으로 이 효과를 내는 방법이 있다. 기본 아이디어는 열거 타입이 임의의 인터페이스를 구현할 수 있다는 사실을 이용하는 것이다. 연산 코드용 인터페이스를 정의하고 열거 타입이 이 인터페이스를 구현하게 하면 된다. 이때 열거 타입이 그 인터페이스의 표준 구현체 역할을 한다.

```java
// 인터페이스를 이용해 확장 가능 열거 타입을 흉내 냈다.
public interface Operation {
    double apply(double x, double y);
}

public enum BasicOperation implements Operation {
    PLUS("+") {
        public double apply(double x, double y) {
            return x + y;
        }
    },
    MINUS("-") {
        public double apply(double x, double y) {
            return x - y;
        }
    },
    TIMES("*") {
        public double apply(double x, double y) {
            return x * y;
        }
    },
    DIVIDE("/") {
        public double apply(double x, double y) {
            return x / y;
        }
    };

    private final String symbol;

    BasicOperation(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
```

열거 타입인 `BasicOperation`은 확장할 수 없지만 인터페이스인 `Operation`은 확장할 수 있고, 이 인터페이스를 연산의 타입으로 사용하면 된다. 이렇게 하면 `Operation`을 구현한 또 다른 열거 타입을 정의해 기본 타입인 `BasicOperation`을 대체할 수 있다.

### 지수 연산(EXP), 나머지 연산(REMAINDER) 추가하기
```java
public enum ExtendedOperation implements Operation {
    EXP("^") {
        public double apply(double x, double y) {
            return Math.pow(x, y);
        }
    },
    REMAINDER("%") {
        public double apply(double x, double y) {
            return x % y;
        }
    };

    private final String symbol;

    ExtendedOperation(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
```

개별 인스턴스 수준에서뿐 아니라 타입 수준에서도, 기본 열거 타입 대신 확장된 열거 타입을 넘겨 확장된 열거 타입의 원소 모두를 사용하게 할 수도 있다.

```java
public class Main {
    public static void main(String[] args) {
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        test(ExtendedOperation.class, x, y);
    }

    // ExtendedOperation의 모든 원소를 테스트 하는 메서드
    private static <T extends Enum<T> & Operation> void test(Class<T> opEnumType, double x, double y) {
        for (Operation op : opEnumType.getEnumConstants()) {
            System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
        }
    }
}
```

main 메서드는 test 메서드에 `ExtendedOperation`의 class 리터럴을 넘겨 확장된 연산들이 무엇인지 알려준다. 여기서 class 리터럴은 한정적 타입 토큰 역할을 한다.(item 33)

`opEnumType` 매개변수의 선언(`<T extends Enum<T> & Operation>`)은 복잡한데, Class 객체가 열거 타입인 동시에 `Operation`의 하위 타입이어야 한다는 뜻이다. 열거 타입이어야 원소를 순회할 수 있고, `Operation`이어야 원소가 뜻하는 연산을 수행할 수 있기 때문이다.

### Collection<? extends Operation>을 넘기는 방법
```java
public class Main {
    public static void main(String[] args) {
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        test(Arrays.asList(ExtendedOpreation.values()), x, y);
    }

    private static void test(Collection<? extends Operation> opSet, double x, double y) {
        for (Operation op : opSet) {
            System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
        }
    }
}
```

이 코드는 그나마 덜 복잡하고 test 메서드가 살짝 더 유연해졌다. 여러 구현 타입의 연산을 조합해 호출할 수 있게 되었다.  
반면, 특정 연산에서는 `EnumSet`을 사용하지 못한다.

## 인터페이스를 이용해 확장 가능한 열거 타입을 흉내 내는 방식의 문제점
열거 타입끼리 구현을 상속할 수 없다는 점이 문제가 된다.

아무 상태에도 의존하지 않는 경우에는 디폴트 구현(item 20)을 이용해 인터페이스에 추가하는 방법이 있다.  
반면, Operation 예는 연산 기호를 저장하고 찾는 로직이 `BasicOperation`, `ExtendedOperation` 모두에 들어가야만 한다. 이 경우에는 중복량이 적으니 문제되진 않지만, 공유하는 기능이 많다면 그 부분을 별도의 도우미 클래스나 정적 도우미 메서드로 분리하는 방식으로 코드 중복을 없앨 수 있을 것이다.

## 핵심 정리
- 열거 타입 자체는 확장할 수 없지만, **인터페이스와 그 인터페이스를 구현하는 기본 열거 타입을 함께 사용해 같은 효과를 낼 수 있다.**
- 이렇게 하면 클라이언트는 이 인터페이스를 구현해 자신만의 열거 타입(혹은 다을 타입)을 만들 수 있다.
- 그리고 API가 (기본 열거 타입을 직접 명시하지 않고) 인터페이스 기반으로 작성되었다면 기본 열거 타입의 인스턴스가 쓰이는 모든 곳을 새로 확장한 열거 타입의 인스턴스로 대처해 사용할 수 있다.