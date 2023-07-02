# 11장. 합성과 유연한 설계
상속이 부모 클래스와 자식 클래스를 연결해서 부모 클래스의 코드를 재사용하는 데 비해 합성은 전체를 표현하는 객체가 부분을 표현하는 객체를 포함해서 부분 객체의 코드를 재사용한다. 상속에서 부모 클래스와 자식 클래스 사이의 의존성은 컴파일타임에 해결되지만, 합성에서 두 객체 사이의 의존성은 런타임에 해결된다. 상속 관계는 **is-a 관계**라고 부르고 합성 관계는 **has-a 관계**라고 부른다.

상속은 부모 클래스 안에 구현된 **코드 자체**를 재사용 하지만 합성은 포함되는 **객체의 퍼블릭 인터페이스를** 재사용한다. 따라서 상속 대신 합성을 사용하면 구현에 대한 의존성을 인터페이스에 대한 의존성으로 변경할 수 있다. 다시 말해서 클래스 사이의 높은 결합도를 객체 사이의 낮은 결합도로 대체할 수 있는 것이다.

> 코드 재사용을 위해서는 객체 합성이 클래스 상속보다 더 좋은 방법이다. [GOF]

상속을 받으면 부모 클래스의 내부가 자식 클래스에 공개 되기 때문에 화이트박스 재사용으로 부른다. 합성은 객체의 내부는 공개 되지 않고 인터페이스를 통해서만 재사용 되기 때문에 블랙박스 재사용으로 부른다.

## 상속을 합성으로 변경하기
- 불필요한 인터페이스 상속 문제: `java.util.Properties`와 `java.util.Stack`
- 메서드 오버라이딩의 오작용 문제: `java.util.HashSet`을 상속받은 `InstrumentedHashSet`
- 부모 클래스와 자식 클래스의 동시 수정 문제: `Playlist`를 상속받은 `PersonalPlaylist`

## 상속으로 인한 조합의 폭발적인 증가
상속으로 인해 결합도가 높아지면 코드를 수정하는 데 필요한 작업의 양이 과도하게 늘어나는 경향이 있다. 대표적으로 다음 두 가지 문제점이 발생한다.

- 하나의 기능을 추가하거나 수정하기 위해 불필요하게 많은 수의 클래스를 추가하거나 수정해야 한다.
- 단일 상속만 지원하는 언어에선 상속으로 인해 오히려 중복 코드의 양이 늘어날 수 있다.

합성을 사용하면 상속으로 인해 발생하는 클래스의 증가와 중복 코드 문제를 간단하게 해결할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6d8f3d84-bb24-4049-9ada-26e100c16007)

이처럼 상속의 남용으로 하나의 기능을 추가하기 위해 필요 이상으로 많은 수의 클래스를 추가해야 하는 경우를 가리켜 **클래스 폭발(class explosion) 문제 또는 조합의 폭발(combinational explosion) 문제**라고 부른다.

상속 관계는 컴파일 타임에 결정되고 고정되기 때문에 코드를 실행하는 도중에는 변경할 수 없다. 따라서 여러 기능을 조합해야 하는 설계에 상속을 이용하면 모든 조합 가능한 경우 별로 클래스를 추가 해야 한다.

## 합성 관계로 변경하기
클래스 폭발 문제를 해결하기 위해 합성을 사용하는 이유는 런타임에 객체 사이의 의존성을 자유롭게 변경할 수 있기 때문이다. 합성을 사용하면 구현 시점에 정책들의 관계를 고정시킬 필요가 없으며 실행 시점에 정책들의 관계를 유연하게 변경할 수 있게 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d52c9dce-a4db-4c62-8ef7-4262c0d2d04e)

```java
public class Phone {
    private RatePolicy ratepolicy;
    private List<Call> calls = new ArrayList<>();

    public Phone(RatePolicy ratePolicy) {
        this.ratePolicy = ratePolicy;
    }

    public List<Call> getCalls() {
        return Collections.unmodifiableList(calls);
    }

    public Money calculateFee() {
        return ratePolicy.calculateFee(this);
    }
}
```

`Phone` 클래스에서 사용하는 `RatePolicy` 타입을 인터페이스로 정의한다.

```java
public interface RatePolicy {
    Money calculateFee(Phone phone);
}
```

`RatePolicy`를 구현한 추상 클래스는 다음과 같다.

```java
public abstract class BasicRatePolicy implements RatePolicy {
    @Override
    public Money calculateFee(Phone phone) {
        Money result = Money.ZERO;

        for(Call call : phone.getCalls()) {
            result.plus(calculateCallFee(call));
        }

        return result;
    }

    protected abstract Money calculateCallFee(Call call);
}
```

추상 클래스를 상속받은 구현 클래스를 정의한다.

```java
public class RegularPolicy extends BasicRatePolicy {
    private Money amout;
    private Duration seconds;

    public RegularPolicy(Money amount, Duration seconds) {
        this.amount = amount;
        this.seconds = seconds;
    }

    @Override
    protected Money calculateCallFee(Call call) {
        return amount.times(call.getDuration().getSeconds() / seconds.getSeconds());
    }
}
```

부가 연산을 구현할 추상클래스는 다음과 같다.

```java
public abstract class AdditionalRatePolicy implements RatePolicy {
    private RatePolicy next;

    public AdditionalRatePolicy(RatePolicy next) {
        this.next = next;
    }

    @Override
    public Money calculateFee(Phone phone) {
        Money fee = next.calculateFee(phone);

        return afterCalculated(fee);
    }

    protected abstract Money afterCalculated(Call call);
}
```

실제로 클라이언트에선 런타임 시 다양한 조합이 가능해진다.

```java
Phone phone = new Phone(new TaxablePolicy(0.05, 
                          new RateDiscountablePolicy(Money.wons(1000),
                            new RegularPolicy(...))));
```

## 믹스인
**믹스인(mixin)은 객체를 생성할 때 코드 일부를 클래스 안에 섞어 넣어 재사용하는 기법을 가리키는 용어**다. 합성이 실행 시점에 객체를 조합하는 재사용 방법이라면 믹스인은 컴파일 시점에 필요한 코드 조각을 조합하는 재사용 방법이다.

여기까지 설명을 듣고 나면 믹스인과 상속이 유사한 것처럼 보이겠지만 믹스인은 상속과는 다르다. 상속의 진정한 목적은 자식 클래스를 부모 클래스와 동일한 개념적인 범주로 묶어 is-a 관계를 만들기 위한 것이다. 반면 믹스인은 말 그대로 코드를 다른 코드 안에 섞어 넣기 위한 방법이다.

- 믹스인을 **추상 서브클래스(abstract subclass)라고 부르기도 한다.**
- 믹스인을 사용하면 특ㅈ어 클래스에 대한 변경 또는 확장을 독립적으로 구현한 후 필요한 시점에 차례대로 추가할 수 있다. 이를 **쌓을 수 있는 변경(stackable modification)이라 부른다.**