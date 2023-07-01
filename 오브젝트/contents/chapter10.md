# 10장. 상속과 코드 재사용
## 상속과 중복 코드
### DRY 원칙
**중복 코드는 변경을 방해한다.** 이것이 중복 코드를 제거해야 하는 가장 큰 이유다. 중복 코드가 가지는 가장 큰 문제는 코드를 수정하는 데 필요한 노력을 몇 배로 증가시킨다는 것이다. **중복 여부를 판단하는 기준은 변경이다.** 요구사항이 변경됐을 때 두 코드를 함께 수정해야 한다면 이 코드는 중복이다. 중복 코드를 결정하는 기준은 코드의 모양이 아니다. 모양이 유사하다는 것은 단지 중복의 징후일 뿐이다. 중복 여부를 결정하는 기준은 코드가 변경에 반응하는 방식이다.

DRY는 '반복하지 마라'라는 뜻의 **Don't Repeat Yourself**의 첫 글자를 모아 만든 용어로 간단히 말해 동일한 지식을 중복하지 말라는 것이다. **한 번, 단 한번 원칙** 또는 **단일 지점 제어(Single-Point Control) 원칙**이라고도 부른다.

> 📌 DRY 원칙  
> 모든 지식은 시스템 내에서 단일하고, 애매하지 않고, 정말로 믿을 만한 표현 양식을 가져야 한다.

### 중복과 변경
중복 코드는 새로운 중복 코드를 부른다. 중복 코드를 제거하지 않은 상태에서 코드를 수정할 수 있는 유일한 방법은 새로운 중복 코드를 추가하는 것뿐이다. 새로운 중복 코드를 추가하는 과정에서 코드의 일관성이 무너질 위험이 항상 도사리고 있다. 더 큰 문제는 중복 코드의 양이 많아질수록 버그의 수는 증가하며 그에 비례해 코드를 변경하는 속도는 점점 더 느려진다.

민첩하게 변경하기 위해서는 중복 코드를 추가하는 대신 제거해야 한다. 기회가 생길 때마다 코드를 DRY 하게 만들기 위해 노력하라.

두 클래스 사이의 중복 코드를 제거하는 한 가지 방법은 클래스를 하나로 합치는 것이다. 타입 코드를 추가하고 값에 따라 로직을 분기시켜 해결할 순 있지만 이 방식은 **타입 코드를 사용하는 클래스는 낮은 응집도와 높은 결합도라는 문제에 시달리게 된다.** 객체지향 프로그래밍 언어는 타입 코드를 사용하지 않고도 중복 코드를 관리할 수 있는 효과적인 방법을 제공한다. **상속**이 바로 그것이다.

상속을 이용해 코드를 재사용하기 위해서는 부모 클래스의 개발자가 세웠던 가정이나 추론 과정을 정확하게 이해해야 한다. 이것은 자식 클래스의 작성자가 부모 클래스의 구현 방법에 대한 정확한 지식을 가져야 한다는 것을 의미한다. 이것은 자식 클래스의 작성자가 부모 클래스의 구현 방법에 대한 정확한 지식을 가져야 한다는 것을 의미한다. 따라서 **상속은 결합도를 높인다.**

### 강하게 결합된 Phone과 NightlyDiscountPhone
부모 클래스와 자식 클래스 사이의 결합이 문제인 이유를 살펴보자. `NightlyDiscountPhone`은 부모 클래스인 `Phone`의 `calculateFee` 메서드를 오버라이딩한다. 또한 메서드 안에서 `super` 참조를 이용해 부모 클래스의 메서드를 호출한다. `NightlyDiscountPhone`의 `calculateFee` 메서드는 자신이 오버라이딩한 `Phone`의 `calculateFee` 메서드가 모든 통화에 대한 요금의 총합을 반환한다는 사실에 기반하고 있다.
하지만 세금을 부과하는 요구사항이 추가된다면 어떻게 될까? `calculateFee` 메서드에서 값을 반환할 때 `taxRate`를 이용해 세금을 부과해야 한다.

```java
public class Phone {

    private double taxRate; // 세금 부과 요구사항으로 인한 변수 추가

    public Phone(Money amount, Duration seconds, double taxRate) {
        // ...
        this.taxTate = taxRate;
    }

    public calculateFee() {
        // ...
        return result.plus(result.times(taxRate));
    }

    public double getTaxRate() {
        return this.taxRate;
    }
}
```

`NightlyDiscountPhone`은 생성자에서 전달받은 `taxRate`를 부모 클래스인 `Phone`의 생성자로 전달해야 한다. 또한 `Phone`과 동일하게 값을 반환할 때 `taxRate`를 이용해 세금을 부과해야 한다.

```java
public class NightlyDiscountPhone extends Phone {

    public NightlyDiscountPhone(Money nightlyAmount, Money regularAmount, Duration seconds, double taxRate) {
        super(regularAmount, seconds, taxRate);
        // ...
    }

    @Override
    public Money calculateFee() {
        // ...
        return result.minus(nightlyFee.plus(nightlyFee.times(getTaxRate()));
    }
}
```

`NightlyDiscountPhone`을 `Phone`의 자식 클래스로 만든 이유는 `Phone`의 코드를 재사용하고 중복 코드를 제거하기 위해서다. 하지만 세금을 부과하는 로직을 추가하기 위해 `Phone`을 수정할 때 유사한 코드를 `NightlyDiscountPhone`에도 추가해야 했다. 다시 말해서 코드 중복을 제거하기 위해 상속을 사용했음에도 세금을 계산하는 로직을 추가하기 위해 새로운 중복 코드를 만들어야 하는 것이다.

이것은 `NightlyDiscountPhone`이 `Phone`의 구현에 너무 강하게 결합돼 있기 때문에 발생하는 문제다. 이처럼 상속 관계로 연결된 자식 클래스가 부모 클래스의 변경에 취약해지는 현상을 가리켜 **취약한 기반 클래스 문제**라고 부른다.

> 📌 상속을 위한 경고 1  
> 자식 클래스의 메서드 안에서 `super` 참조를 이용해 부모 클래스의 메서드를 직접 호출할 경우 두 클래스는 강하게 결합된다. **`super` 호출을 제거할 수 있는 방법을 찾아 결합도를 제거하라.**

## 취약한 기반 클래스 문제
부모 클래스의 작은 변경에도 자식 클래스는 컴파일 오류와 실행 에러라는 고통에 시달려야 할 수도 있다. 이처럼 부모 클래스의 변경에 의해 자식 클래스가 영향을 받는 현상을 **취약한 기반 클래스 문제**라고 부른다. 이 문제는 상속을 사용한다면 피할 수 없는 OOP의 근본적인 취약성이다.

구현을 상속한 경우(`extends`를 사용한 경우) 파생 클래스는 기반 클래스에 강하게 결합되며, 이 둘 사이의 밀접한 연결은 바람직하지 않다.

상속 관계를 추가할수록 전체 시스템의 결합도가 높아진다는 사실을 알고 있어야 한다. 상속은 자식 클래스를 점진적으로 추가해서 기능을 확장하는 데는 용이하지만 높은 결합도로 인해 부모 클래스를 점진적으로 개선하는 것은 어렵게 만든다. 최악의 경우에는 모든 자식 클래스를 동시에 수정하고 테스트해야 할 수도 있다. 취약한 기반 클래스 문제는 캡슐화를 약화시키고 결합도를 높인다.

객체를 사용하는 이유는 구현과 관련된 세부사항을 퍼블릭 인터페이스 뒤로 캡슐화할 수 있기 때문이다. 캡슐화는 변경에 의한 파급효과를 제어할 수 있기 때문에 가치가 있다. 객체는 변경 될지도 모르는 불안정한 요소를 캡슐화함으로써 파급효과를 걱정하지 않고도 자유롭게 내부를 변경할 수 있다.

안타깝게도 상속을 사용하면 부모 클래스의 퍼블릭 인터페이스가 아닌 구현을 변경하더라도 자식 클래스가 영향을 받기 쉬워진다. 상속 계층의 상위에 위치한 클래스에 가해지는 작은 변경만으로도 상속 계층에 속한 모든 자손들이 급격하게 요동칠 수 있다.

객체지향의 기반은 캡슐화를 통한 변경의 통제다. 상속은 코드의 재사용을 위해 캡슐화의 장점을 희석시키고 구현에 대한 결합도를 높임으로써 객체지향이 가진 강력함을 반감시킨다.

### 불필요한 인터페이스 상속 문제
`java.util.Properties`와 `java.util.Stack`은 대표적인 잘못된 상속의 예이다.

<p align="center">
  <img src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/97633edd-41ee-4544-a78b-d30e90454596">
</p>

Java의 초기 컬렉션 프레임워크 개발자들은 요소의 추가, 삭제 오퍼레이션을 제공하는 `Vector`를 재사용하기 위해 `Stack`을 `Vector`의 자식 클래스로 구현했다.

`Vector`는 임의의 위치(index)에서 요소를 조회하고, 추가하고, 삭제할 수 있는 `get`, `add`, `remove` 오퍼레이션을 제공한다. 이에 비해 `Stack`은 맨 마지막 위치 에서만 요소를 추가하거나 제거할 수 있는 `push`, `pop` 오퍼레이션을 제공한다.

```java
Stack<String> stack = new Stack<>();
stack.push("1st");
stack.push("2nd");
stack.push("3rd");

stack.add(0, "4th");

assertEquals("4th", stack.pop()); // 에러!
```

위 코드에서 `Stack`에 마지막으로 추가한 값은 "4th"지만 `pop` 메서드의 반환값은 "3rd"다. 그 이유는 `Vector`의 `add` 메서드를 이용해서 스택의 맨 앞에 "4th"를 추가했기 때문이다.

> 📌 상속을 위한 경고 2  
> 상속받은 부모 클래스의 메서드가 자식 클래스의 내부 구조에 대한 규칙을 깨트릴 수 있다.

### 메서드 오버라이딩의 오작용 문제
> 📌 상속을 위한 경고 3  
> 자식 클래스가 부모 클래스의 메서드를 오버라이딩할 경우 부모 클래스가 자신의 메서드를 사용하는 방법에 자식 클래스가 결합될 수 있다.

조슈아 블로치는 클래스가 상속되기를 원한다면 상속을 위해 클래스를 설계하고 문서화해야 하며, 그렇지 않은 경우에는 상속을 금지시켜야 한다고 주장한다.

> 그러나 잘된 API 문서는 메서드가 무슨 일(what)을 하는지를 기술해야 하고, 어떻게 하는지(how)를 설명해서는 안 된다는 통념을 어기는 것은 아닐까? 그렇다. 어기는 것이다! 이것은 결국 상속이 캡슐화를 위반함으로써 초래된 불행인 것이다.

설계는 트레이드오프 활동이라는 사실을 기억하라. 상속은 코드 재사용을 위해 캡슐화를 희생한다. 완벽한 캡슐화를 원한다면 코드 재사용을 포기하거나 상속 이외의 다른 방법을 사용해야 한다.

### 부모 클래스와 자식 클래스의 동시 수정 문제
책의 예제 코드는 자식 클래스가 부모 클래스의 메서드를 오버라이딩하거나 불필요한 인터페이스를 상속받지 않았음에도 부모 클래스를 수정할 때 자식 클래스를 함께 수정해야 할 수도 있다는 사실을 잘 보여준다. 상속을 사용하면 자식 클래스가 부모 클래스의 구현에 강하게 결합되기 때문에 이 문제를 피하기는 어렵다.

**결합도란 다른 대상에 대해 알고 있는 지식의 양이다ㅣ.** 상속은 기본적으로 부모 클래스의 구현을 재사용한다는 기본 전제를 따르기 때문에 자식 클래스가 부모 클래스의 내부에 대해 속속들이 알도록 강요한다. 따라서 코드 재사용을 위한 상속은 부모 클래스와 자식 클래스를 강하게 결합시키기 때문에 함께 수정해야 하는 상황 역시 빈번하게 발생할 수밖에 없는 것이다.

조슈아 블로치는 이 문제에 대해 다음과 같이 조언한다.

> 다시 말해, 서브클래스는 올바른 기능을 위해 슈퍼클래스의 세부적인 구현에 의존한다. 슈퍼클래스의 구현은 릴리스를 거치면서 변경될 수 있고, 그에 따라 서브클래스의 코드를 변경하지 않더라도 깨질 수 있다. 결과적으로, 슈퍼클래스의 작성자가 확장될 목적으로 특별히 그 클래스를 설계하지 않았다면 서브클래스는 슈퍼클래스와 보조를 맞춰서 진화해야 한다.

상속과 관련된 마지막 주의사항은 다음과 같다.

> 📌 상속을 위한 경고 4  
> 클래스를 상속하면 결합도로 인해 자식 클래스의 부모 클래스의 구현을 영원히 변경하지 않거나, 자식 클래스와 부모 클래스를 동시에 변경하거나 둘 중 하나를 선택할 수밖에 없다.

## Phone 다시 살펴보기
### 추상화에 의존하자
**이 문제를 해결하는 가장 일반적인 방법은 자식 클래스가 부모 클래스의 구현이 아닌 추상화에 의존하도록 만드는 것이다.** 정확하게 말하면 부모 클래스와 자식 클래스 모두 추상화에 의존하도록 수정해야 한다. 저자가 생각하는 코드 중복을 제거하기 위해 상속을 도입할 때 따르는 두 가지 원칙이 있다.

- 두 메서드가 유사하게 보인다면 차이점을 메서드로 추출하라. 메서드 추출을 통해 두 메서드를 동일한 형태로 보이도록 만들 수 있다.
- 부모 클래스의 코드를 하위로 내리지 말고 자식 클래스의 코드를 상위로 올려라. 부모 클래스의 구체적인 메서드를 자식 클래스로 내리는 것보다 자식 클래스의 추상적인 메서드를 부모 클래스로 올리는 것이 재사용성과 응집도 측면에서 더 뛰어난 결과를 얻을 수 있다.

### 차이를 메서드로 추출하라
가장 먼저 할 일은 중복 코드 안에서 차이점을 별도의 메서드로 추출하는 것이다. 이것은 흔히 말하는 "변하는 것으로부터 변하지 않는 것을 분리하라", 또는 "변하는 부분을 찾고 이를 캡슐화하라"라는 조언을 메서드 수준에서 적용한 것이다.

먼저 할 일은 두 클래스의 메서드에서 다른 부분을 별도의 메서드로 추출하는 것이다. `calculateFee`의 `for`문 안에 구현된 요금 계산 로직이 서로 다르다. 이 부분을 동일한 이름을 가진 메서드로 추출하자.

```java
public class Phone {
    private Money amount;
    private Duration seconds;
    private List<Call> calls = new ArrayList<>();

    public Phone(Money amount, Duration seconds) {
        this.amount = amount;
        this.seconds = seconds;
    }

    public Money calculateFee() {
        Money result = Money.ZERO;

        for (Call call : calls) {
            result = result.plus(calculateCallFee(call));
        }

        return result;
    }

    private Money calculateCallFee(Call call) {
        return amount.times(call.getDuration().getSeconds() / seconds.getSeconds());
    }
}
```

```java
public class NightlyDiscountPhone {
    private static final int LATE_NIGHT_HOUR = 22;

    private Money nightlyAmount;
    private Money regularAmount;
    private Duration seconds;
    private List<Call> calls = new ArrayList<>();

    public NightlyDiscountPhone(Money nightlyAmount, Money regularAmount, Duration seconds) {
        this.nightlyAmount = nightlyAmount;
        this.regularAmount = regularAmount;
        this.seconds = seconds;
    }

    public Money calculateFee() {
        Money result = Money.ZERO;

        for (Call call : calls) {
            result = result.plus(calculateCallFee(call));
        }

        return result;
    }

    private Money calculateCallFee(Call call) {
        if (call.getFrom().getHour() >= LATE_NIGHT_HOUR) {
            return nightlyAmount.times(call.getDuration().getSeconds() / seconds.getSeconds());
        } else {
            return regularAmount.times(call.getDuration().getSeconds() / seconds.getSeconds());
        }
    }
}
```

두 클래스의 `calculateFee` 메서드는 완전히 동일해졌고 추출한 `calculateCallFee` 메서드 안에 서로 다른 부분을 격리시켜 놓았다.

### 중복 코드를 부모 클래스로 올려라
부모 클래스를 추가하자. 목표는 모든 클래스들이 추상화에 의존하도록 만드는 것이기 때문에 이 클래스는 추상 클래스로 구현하는 것이 적합할 것이다.

<p align="center">
  <img src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/4df1ef6d-4f59-42d0-af41-7a74fed653a9">
</p>

> 모든 하위 클래스가 이 행동을 할 수 있게 만들려면 여러 개의 중복 코드를 양산하거나 이 행동을 상위 클래스로 올리는 수밖에 없다.

### 추상화가 핵심이다
공통 코드를 이동시킨 후에 각 클래스는 서로 다른 변경의 이유를 가진다는 것에 주목하라. 세 클래스는 각각 하나의 변경 이유만을 가진다. 이 클래스들은 단일 책임 원칙을 준수하기 때문에 응집도가 높다.

사실 부모 클래스 역시 자신의 내부에 구현된 추상 메서드를 호출하기 때문에 추상화에 의존한다고 말할 수 있다. 의존성 역전 원칙도 준수하는데, 요금 계산과 관련된 상위 수준의 정책을 구현하는 `AbstractPhone`이 세부적인 요금 계산 로직을 구현하는 `Phone`과 `NightlyDiscountPhone`에 의존하지 않고 그 반대로 `Phone`과 `NightlyDiscountPhone`이 추상화인 `AbstractPhone`에 의존하기 때문이다.

새로운 요금제를 추가하기도 쉽다는 사실 역시 주목하라. 새로운 요금제가 필요하다면 `AbstractPhone`을 상속받는 새로운 클래스를 추가한 후 `calculateCallFee` 메서드만 오버라이딩하면 된다. 다른 클래스를 수정할 필요가 없다. 현재의 설계에는 확장에는 열려 있고 수정에는 닫혀 있기 때문에 개방-폐쇄 원칙 역시 준수한다.

### 차이에 의한 프로그래밍
시간이 흐르고 객체지향에 대한 이해가 깊어지면서 사람들은 코드를 재사용하기 위해 맹목적으로 상속을 사용하는 것이 위험하다는 사실을 깨닫기 시작했다. 상속이 코드 재사용이라는 측면에서 매우 강력한 도구인 것은 사실이지만 강력한 만큼 잘못 사용할 경우에 돌아오는 피해 역시 크다는 사실을 뼈저리게 경험한 것이다. 상속의 오용과 남용은 애플리케이션을 이해하고 확장하기 어렵게 만든다. 