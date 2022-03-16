# 아이템 22. 인터페이스는 타입을 정의하는 용도로만 사용하라

인터페이스는 자신을 구현한 클래스의 인스턴스를 참조할 수 있는 타입 역할을 한다. 달리 말해, 클래스가 어떤 인터페이스를 구현한다는 것은 자신의 인스턴스로 무엇을 할 수 있는지를 클라이언트에 얘기해주는 것이다. 인터페이스는 오직 이 용도로만 사용해야 한다.

이 지침에 맞지 않는 예로 상수 인터페이스라는 것이 있다.

```java
// 상수 인터페이스 안티패턴 - 사용 금지!
public interface PhysicalConstants {
    static final double AVOGADROS_NUMBERS = 6.022_140_857e23;
    static final double BOLTZMANN_CONSTANT = 1.380_648_52e-23;
    static final double ELECTRON_MASS = 9.109_383_56e-31;
}
```

상수 인터페이스란 위와 같이 메서드 없이, 상수 필드로만 가득 찬 인터페이스를 말한다.  
**상수 인터페이스 안티패턴은 인터페이스를 잘못 사용한 예다.** 클래스 내부에서 사용하는 상수는 외부 인터페이스가 아니라 내부 구현에 해당한다. 따라서 상수 인터페이스를 구현하는 것은 **이 내부 구현을 클래스의 API로 노출하는 행위다.**

클래스가 어떤 상수 인터페이스를 사용하든 사용자에게는 아무런 의미가 없다. 오히려 사용자에게 혼란을 주기도 하며, 더 심하게는 클라이언트 코드가 내부 구현에 해당하는 이 상수들에 종속되게 한다. 그래서 다음 릴리스에서 이 상수들을 더는 쓰지 않게 되더라도 바이너리 호환성을 위해 여전히 상수 인터페이스를 구현하고 있어야 한다.

상수를 공개할 목적이라면 더 합당한 선택지가 몇 가지 있다.

1. 특정 클래스나 인터페이스와 강하게 연관된 상수라면 그 클래스나 인터페이스 자체에 추가해야 한다.  
   → `Integer.MIN_VALUE`, `Integer.MAX_VALUE` 상수가 대표적인 예시다.
2. 열거 타입으로 나타내기 적합한 상수라면 열거 타입으로 만들어 공개하면 된다.(item 34)
3. 인스턴스화할 수 없는 유틸리티 글래스(item 4)에 담아 공개하자.

```java
//상수 유틸리티 클래스
package effectivejava.ch4.item22.constantutilityclass;

public class PhysicalConstants {
    private PhysicalConstants() {} // 인스턴스화 방지

    public static final double AVOGADROS_NUMBERS = 6.022_140_857e23;
    public static final double BOLTZMANN_CONSTANT = 1.380_648_52e-23;
    public static final double ELECTRON_MASS = 9.109_383_56e-31;
}
```

유틸리티 클래스에 정의된 상수를 클라이언트에서 사용하려면 클래스 이름까지 함께 명시해야 한다. 유틸리티 클래스의 상수를 빈번히 사용한다면 정적 임포트(static import)하여 클래스 이름은 생략할 수 있다.

```java
import static effectivejava.ch4.item22.constantutilityclass.PhysicalConstants.*;

public class Test {
    double atoms(double mols) {
        return AVOGADROS_NUMBERS * mols;
    }
}
```

> 📌 참고  
> 숫자 리터럴에 사용한 밑줄(\_)은 Java 7부터 허용되는 것으로 숫자 리터럴의 값에는 아무런 영향을 주지 않으면서, 읽기는 훨씬 편하게 해준다.

## 핵심 정리

- 인터페이스는 타입을 정의하는 용도로만 사용해야 한다.
- 상수 공개용 수단으로 사용하지 말자.
