# 5장. 책임 할당하기
데이터 중심 설계로 인해 발생하는 문제점을 해결할 수 있는 가장 기본적인 방법은 **데이터가 아닌 책임에 초점을 맞추는 것이다.** 이번 장에서 살펴볼 GRASP 패턴은 책임 할당의 어려움을 해결하기 위한 답을 제시해 줄 것이다.

## 책임 주도 설계를 향해
데이터 중심 설계에서 책임 중심 설계로 전환하기 위해서는 다음의 두 가지 원칙을 따라야 한다.
- 데이터보다 행동을 먼저 결정하라
- 협력이라는 문맥 안에서 책임을 결정하라

### 데이터보다 행동을 먼저 결정하라
객체에게 중요한 것은 데이터가 아니라 외부에 제공하는 행동이다. 클라이언트의 관점에서 객체가 수행하는 행동이란 곧 객체의 책임을 의미한다. 객체는 협력에 참여하기 위해 존재하며 협력 안에서 수행하는 책임이 객체의 존재가치를 증명한다. 데이터는 객체가 책임을 수행하는 데 필요한 재료를 제공할 뿐이다.

책임 중심의 설계에선 **"이 객체가 수행하는 책임은 무엇인가"를 결정한 후에 "이 책임을 수행하는 데 필요한 데이터는 무엇인가"를 결정한다.** 다시 말해 책임 중심의 설계에서는 **객체의 행동, 즉 책임을 먼저 결정한 후에 객체의 상태를 결정한다는 것이다.**

### 협력이라는 문맥 안에서 책임을 결정하라
객체에게 할당된 책임의 품질은 협력에 적합한 정도로 결정된다. 책임은 객체의 입장이 아니라 객체가 참여하는 협력에 적합해야 한다.

협력을 시작하는 주체는 메시지 전송자이기 때문에 협력에 적합한 책임이란 메시지 수신자가 아니라 메시지 전송자에게 적합한 책임을 의미한다. 다시 말해 **메시지를 전송하는 클라이언트의 의도에 적합한 책임을 할당해야 한다**는 것이다.

협력에 적합한 책임을 수확하기 위해서는 객체를 결정한 후에 메시지를 선택하는 것이 아니라 **메시지를 결정한 후에 객체를 선택해야 한다.** 메시지가 존재하기 때문에 그 메시지를 처리할 객체가 필요한 것이다. **객체가 메시지를 선택하는 것이 아니라 메시지가 객체를 선택하게 해야 한다.**

메시지가 클라이언트의 의도를 표현한다는 사실에 주목하자. 객체를 결정하기 전에 객체가 수신할 메시지를 먼저 결정한다는 점 역시 주목하라. 클라이언트는 어떤 객체가 메시지를 수신할지 알지 못한다. 클라이언트는 단지 임의의 객체가 메시지를 수신할 것이라는 사실을 믿고 자신의 의도를 표현한 메시지를 전송할 뿐이다. 그리고 메시지를 수신하기로 결정된 객체는 메시지를 처리할 '책임'을 할당받게 된다.

협력이라는 문맥 안에서 메시지에 집중하는 책임 중심의 설계는 캡슐화의 원리를 지키기가 훨씬 쉬워진다.

### 책임 주도 설계
3장에서 설명한 책임 주도 설계의 흐름을 다시 나열한 것은 다음과 같다.
- 시스템이 사용자에게 제공해야 하는 기능인 시스템 책임을 파악한다.
- 시스템 책임을 더 작은 책임으로 분할한다.
- 분할된 책임을 수행할 수 있는 적절한 객체 또는 역할을 찾아 책임을 할당한다.
- 객체가 책임을 수행하는 도중 다른 객체의 도움이 필요한 경우 이를 책임질 적절한 객체 또는 역할을 찾는다.
- 해당 객체 또는 역할에게 책임을 할당함으로써 두 객체가 협력하게 한다.

## 책임 할당을 위한 GRASP 패턴
GRASP 패턴은 "General Responsibility Assignment Software Pattern(일반적인 책임 할당을 위한 소프트웨어 패턴)"의 약자로 **객체에게 책임을 할당할 때 지침으로 삼을 수 있는 원칙들의 집합을 패턴 형식으로 정리한 것이다.**

### 도메인 개념에서 출발하기
설계를 시작하기 전에 도메인에 대한 개략적인 모습을 그려 보는 것이 유용하다. 도메인 안에는 무수히 많은 개념들이 존재하며 이 도메인 개념들을 책임 할당의 대상으로 사용하면 코드에 도메인의 모습을 투영하기가 좀 더 수월해진다. 따라서 어떤 책임을 할당해야 할 때 가장 먼저 고민해야 하는 유력한 후보는 바로 도메인 개념이다.

![image](https://user-images.githubusercontent.com/60968342/233024917-39f0822d-bf6a-4029-a969-9c9fc8e7eb0d.png)

위의 그림은 영화 예매 시스템을 구성하는 도메인 개념과 개념 사이의 관계를 대략적으로 표현한 것이다. 설계를 시작하는 단계에서는 개념들의 의미와 관계가 정확하거나 완벽할 필요가 없다. 이 단계에서는 책임을 할당받을 객체들의 종류와 관계에 대한 유용한 정보를 제공할 수 있다면 충분하다. **중요한 것은 설계를 시작하는 것이지 도메인 개념들을 완벽하게 정리하는 것이 아니다.**

### 정보 전문가에게 책임을 할당하라
책임 주도 설계 방식의 첫 단계는 애플리케이션이 제공해야 하는 기능을 애플리케이션의 책임으로 생각하는 것이다. 이 책임을 애플리케이션에 대해 전송된 메시지로 간주하고 이 메시지를 책임질 첫 번째 객체를 선택하는 것으로 설계를 시작한다. 메시지는 메시지를 수신할 객체가 아니라 메시지를 전송할 객체의 의도를 반영해서 결정해야 한다.
- 메시지를 전송할 객체는 무엇을 원하는가?
- 메시지를 수신할 적합한 객체는 누구인가?

객체는 상태와 행동을 통합한 캡슐화의 단위이다. 객체는 자신의 상태를 스스로 처리하는 자율적인 존재여야 한다. 객체의 책임과 책임을 수행하는 데 필요한 상태는 동일한 객체 안에 존재해야 한다.

따라서 **객체에게 책임을 할당하는 첫 번째 원칙은 책임을 수행할 정보를 알고 있는 객체에게 책임을 할당하는 것이다.** GRASP에서는 이를 **INFORMATION EXPERT(정보 전문가) 패턴**이라 부른다. 이 패턴은 **객체가 자신이 소유하고 있는 정보와 관련된 작업을 수행한다는 일반적인 직관을 표현한 것이다.** 여기서 정보는 데이터와 다르다는 사실에 주의하라. 책임을 수행하는 객체가 정보를 '알고' 있다고 해서 그 정보를 '저장'하고 있을 필요는 없다. 객체는 해당 정보를 제공할 수 있는 다른 객체를 알고 있거나 필요한 정보를 계산해서 제공할 수도 있다.

만약 스스로 처리할 수 없는 작업이 있다면 외부에 도움을 요청해야 한다. 이 요청이 외부로 전송해야하는 새로운 메시지가 되고, 최종적으로 이 메시지가 새로운 객체의 책임으로 할당된다. 이 같은 연쇄적인 메시지 전송과 수신을 통해 협력 공동체가 구성되는 것이다.

![image](https://user-images.githubusercontent.com/60968342/233258704-239f16c5-ce3f-4d47-981c-e9d8644d324f.png)

위 그림에서 Screening은 영화 예매를 위한 정보 전문가다. 예매에 관한 책임을 할당받았지만, 가격을 계산하는 것은 외부의 Movie에게 도움을 요청하여 Movie가 가격 계산의 책임을 지게 된다. 할인 여부도 마찬가지로 외부의 DiscountCondition에게 책임을 지게 하면 된다.

예제와 같이 정보 전문가 패턴은 객체에게 책임을 할당할 때 가장 기본이 되는 책임 할당 원칙이다. 객체란 상태와 행동을 함께 가지는 단위라는 객체지향의 가장 기본적인 원리를 책임 할당의 관점에서 표현한다. 이 패턴을 따르는 것만으로도 자율성이 높은 객체들로 구성된 협력 공동체를 구축할 가능성이 높아지는 것이다.

### 높은 응집도와 낮은 결합도
앞선 설계와는 달리 Movie 대신 Screening이 직접 DiscountCondition과 협력하게 하면 다음과 같이 변경된다.

![image](https://user-images.githubusercontent.com/60968342/233259770-e0f965da-1801-4556-81fc-099b3ebd474a.png)

위 설계를 선택하지 않고 앞선 설계를 선택한 이유는 응집도와 결합도에 있다. 높은 응집도와 낮은 결합도는 객체에 책임을 할당할 때 항상 고려해야 하는 기본 원리다. **책임을 할당할 수 있는 다양한 대안들이 존재한다면 응집도와 결합도의 측면에서 더 나은 대안을 선택하는 것이 좋다.**

GRASP에선 이를 **LOW COUPLING(낮은 결합도) 패턴**과 **HIGH COHESION(높은 응집도) 패턴**이라 부른다.

> 📌 LOW COUPLING 패턴
> 
> 어떻게 하면 의존성을 낮추고 변화의 영향을 줄이며 재사용성을 증가시킬 수 있을까? 설계의 전체적인 결합도가 낮게 유지되도록 책임을 할당하라.  
> 낮은 결합도는 모든 설계 결정에서 염두에 둬야 하는 원리다. 다시 말해 설계 결정을 평가할 때 적용할 수 있는 평가원리다. 현재의 책임 할당을 검토하거나 여러 설계 대안들이 있을 때 낮은 결합도를 유지할 수 있는 설계를 선택하라.

앞서 그림 5.1의 도메인 개념을 다시 살펴보면, Movie는 DiscountCondition의 목록을 속성으로 포함하고 있다. Movie와 DiscountCondition은 이미 결합돼 있기 때문에 Movie를 DiscountCondition과 협력하게 하면 설계 전체적으로 결합도를 추가하지 않고도 협력을 완성할 수 있다. 하지만, Screening이 DiscountCondition과 협력할 경우에는 Screening과 DiscountCondition 사이에 새로운 결합도가 추가된다.

따라서 LOW COUPLING 패턴의 관점에서는 첫 번째 설계가 더 나은 설계 대안인 것이다.

> 📌 HIGH COHESION 패턴
> 
> 어떻게 복잡성을 관리할 수 있는 수준으로 유지할 것인가? 높은 응집도를 유지할 수 있게 책임을 할당하라.  
> 낮은 결합도처럼 높은 응집도 역시 모든 설계 결정에서 염두에 둬야 할 원리다. 다시 말해 설계 결정을 평가할 때 적용할 수 있는 평가원리다. 현재의 책임 할당을 검토하고 있거나 여러 설계 대안 중 하나를 선택해야 한다면 높은 응집도를 유지할 수 있는 설계를 선택하라.

Screening의 가장 중요한 책임은 예매를 생성하는 것이다. 만약 Screening이 DiscountCondition과 협력해야 한다면 Screening은 영화 요금 계산과 관련된 책임 일부를 떠안아야 할 것이다. 이 경우 Screening은 DiscountCondition이 할인 여부를 판단할 수 있고 Movie가 이 할인 여부를 필요로 한다는 사실 역시 알고 있어야 한다.

다시 말해 예매 요금을 계산하는 방식이 변경될 경우 Screening도 함께 변경해야 하는 것이다. 결과적으로 서로 다른 이유로 변경되는 책임을 짊어지게 되므로 응집도가 낮아질 수밖에 없다.

### 창조자에게 객체 생성 책임을 할당하라
영화 예매 협력의 최종 결과물은 Reservation 인스턴스를 생성하는 것이다. 이것은 협력에 참여하는 어떤 객체에게는 Reservation 인스턴스를 생성할 책임을 할당해야 한다는 것을 의미한다.

GRASP의 **CREATOR(창조자) 패턴**은 이 같은 경우에 사용할 수 있는 책임 할당 패턴으로서 객체를 생성할 책임을 어떤 객체에게 할당할지에 대한 지침을 제공한다.

> 📌 CREATOR 패턴
> 
> 객체 A를 생성해야 할 때 어떤 객체에게 객체 생성 책임을 할당해야 하는가? 아래 조건을 최대한 많이 만족하는 B에게 객체 생성 책임을 할당하라.
> - B가 A 객체를 포함하거나 참조한다.
> - B가 A 객체를 기록한다.
> - B가 A 객체를 긴밀하게 사용한다.
> - B가 A 객체를 초기화하는 데 필요한 데이터를 가지고 있다. (이 경우 B는 A에 대한 정보전문가다)
> 
> CREATOR 패턴의 의도는 어떤 방식으로든 생성되는 객체와 연결되거나 관련될 필요가 있는 객체에 해당 객체를 생성할 책임을 맡기는 것이다. 생성될 객체에 대해 잘 알고 있어야 하거나 그 객체를 사용해야 하는 객체는 어떤 방식으로든 생성될 객체와 연결될 것이다. 다시 말해 두 객체는 서로 결합된다.
> 
> 이미 결합돼 있는 객체에게 생성 책임을 할당하는 것은 설계의 전체적인 결합도에 영향을 미치지 않는다. 결과적으로 CREATOR 패턴은 이미 존재하는 객체 사이의 관계를 이용하기 때문에 설계가 낮은 결합도를 유지할 수 있게 된다.

![image](https://user-images.githubusercontent.com/60968342/233262640-35b8474c-9c92-47c3-ad68-2bdc576c8bab.png)

## 구현을 통한 검증
```java
public class Screening {

    private Movie movie;
    private int sequence;
    private LocalDateTime whenScreened;

    public Reservation reserve(Customer customer, int audienceCount) {
        return new Reservation(customer, this, calculateFee(audienceCount), audienceCount);
    }

    public Money calculateFee(int audienceCount) {
        return movie.calculateMovieFee(this).times(audienceCount);
    }
}
```

Movie에 전송하는 메시지의 시그니처를 `calculateMovieFee(Screening screening)`로 선언했다는 사실에 주목하자. 이 메시지는 수신자인 Movie가 아닌 송신자인 Screening의 의도를 표현한다. **여기서 중요한 것은 Screening이 Movie의 내부 구현에 대한 어떤 지식도 없이 전송할 메시지를 결정했다는 것이다.** 이처럼 Movie의 구현을 고려하지 않고 필요한 메시지를 결정하면 Movie의 내부 구현을 깔끔하게 캡슐화할 수 있다.

메시지가 변경되지 않는 한 Movie에 어떤 수정을 가하더라도 Screening에는 영향을 미치지 않는다. 이처럼 메시지가 객체를 선택하도록 책임 주도 설계 방식을 따르면 캡슐화와 낮은 결합도라는 목표를 비교적 손쉽게 달성할 수 있다.

### DiscountCondition 개선하기

```java
public class DiscountCondition {

    private DiscountConditionType type;
    private int sequence;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public boolean isSatisfiedBy(Screening screening) {
        if (type == DiscountConditionType.PERIOD) {
            return isSatisfiedByPeriod(screening);
        }
        return isSatisfiedBySequence(screening);
    }

    private boolean isSatisfiedByPeriod(Screening screening) {
        return this.dayOfWeek.equals(screening.getWhenScreened().getDayOfWeek())
                && !this.startTime.isAfter(screening.getWhenScreened().toLocalTime())
                && !this.endTime.isBefore(screening.getWhenScreened().toLocalTime());
    }

    private boolean isSatisfiedBySequence(Screening screening) {
        return sequence == screening.getSequence();
    }
}
```

위 클래스의 가장 큰 문제점은 변경에 취약하다는 점이다. 다음 세 가지 변경 이유를 살펴보자.
- 새로운 할인조건 추가: `isSatisfiedBy` 메서드 안의 `if ~ else` 구문을 수정해야 한다. 물론 새로운 할인 조건이 새로운 데이터를 요구한다면 `DiscountCondition`에 속성을 추가하는 작업도 필요하다.
- 순번 조건을 판단하는 로직 변경: `isSatisfiedBySequence` 메서드의 내부 구현을 수정해야 한다. 물론 순번 조건을 판단하는 데 필요한 데이터가 변경된다면 `DiscountCondition`의 `sequence` 속성 역시 변경해야 할 것이다.
- 기간 조건을 판단하는 로직이 변경되는 경우: `isSatisfiedByPeriod` 메서드의 내부 구현을 수정해야 한다. 물론 기간 조건을 판단하는 데 필요한 데이터가 변경된다면 `DiscountCondition`의 `dayOfWeek`, `startTime`, `endTime` 속성 역시 변경해야 할 것이다.

하나 이상의 변경 이류를 가지기 때문에 응집도가 낮다는 것을 확인할 수 있었다. **응집도가 낮다는 것은 서로 연관성이 없는 기능이나 데이터가 하나의 클래스 안에 뭉쳐져 있다는 것을 의미한다.**

코드를 통해 변경의 이유를 파악할 수 있는 첫 번째 방법은 **인스턴스 변수가 초기화 되는 시점**을 살펴보는 것이다. 응집도가 높은 클래스는 인스턴스를 생성할 때 모든 속성을 함께 초기화한다. 반면 응집도가 낮은 클래스는 객체의 속성 중 일부만 초기화하고 일부는 초기화되지 않은 상태로 남겨진다. 따라서 **함께 초기화 되는 속성을 기준으로 코드를 분리해야 한다.**

두 번째 방법은 **메서드들이 인스턴스 변수를 사용하는 방식**을 살펴보는 것이다. 모든 메서드가 객체의 모든 속성을 사용한다면 클래스의 응집도는 높다고 볼 수 있다. 반면 메서드들이 사용하는 속성에 따라 그룹이 나뉜다면 클래스의 응집도가 낮다고 볼 수 있다. 클래스의 응집도를 높이기 위해서는 **속성 그룹과 해당 그룹에 접근하는 메서드 그룹을 기준으로 코드를 분리해야 한다.**

> 📌 클래스 응집도 판단하기
> 
> - 하나 이상의 이유로 변경돼야 한다면 응집도가 낮은 것이다. 변경의 이유를 기준으로 클래스를 분리하라.
> - 클래스의 인스턴스를 초기화하는 시점에 경우에 따라 서로 다른 속성들을 초기화하고 있다면 응집도가 낮은 것이다. 초기화되는 속성의 그룹을 기준으로 클래스를 분리하라.
> - 메서드 그룹이 속성 그룹을 사용하는지 여부로 나뉜다면 응집도가 낮은 것이다. 이들 그룹을 기준으로 클래스를 분리하라.

### 타입 분리하기
DiscountCondition의 가장 큰 문제는 순번 조건과 기간 조건이라는 두 개의 독립적인 타입이 하나의 클래스 안에 공존하고 있다는 점이다. 두 타입을 분리해보자.

```java
public class PeriodCondition {

    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public PeriodCondition(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isSatisfiedBy(Screening screening) {
        return this.dayOfWeek.equals(screening.getWhenScreened().getDayOfWeek())
                && !this.startTime.isAfter(screening.getWhenScreened().toLocalTime())
                && !this.endTime.isBefore(screening.getWhenScreened().toLocalTime());
    }
}

public class SequenceCondition {
    
    private int sequence;

    public SequenceCondition(int sequence) {
        this.sequence = sequence;
    }

    public boolean isSatisfiedBy(Screening screening) {
        return sequence == screening.getSequence();
    }
}
```
클래스를 분리하면 앞에서 언급했던 문제점들이 모두 해결된다. SequenceCondition과 PeriodCondition은 자신의 모든 인스턴스 변수를 함께 초기화할 수 있다. 결과적으로 모든 메서드는 동일한 인스턴스 변수 그룹을 사용한다. 그 결과 개별 클래스들의 응집도가 향상됐다.

하지만, Movie의 인스턴스는 SequenceCondition과 PeriodCondition 이라는 두 개의 서로 다른 클래스의 인스턴스 모두와 협력할 수 있어야 한다. 이 문제는 다형성을 통해 해결할 수 있다.

### 다형성을 통해 분리하기
![image](https://user-images.githubusercontent.com/60968342/233274650-2f046358-0152-441b-aa1e-ec9a6636278b.png)

Movie의 입장에서 보면 SequenceCondition과 PeriodCondition은 아무 차이도 없다. 할인 가능 여부를 반환해 주기만 하면 Movie는 객체가 어떤 인스턴스인지는 상관하지 않는다.

이 시점이 되면 자연스럽게 **역할**의 개념이 등장한다. 역할을 사용한다면 객체의 구체적인 타입을 추상화할 수 있다. Java에선 일반적으로 이를 구현하기 위해 추상 클래스나 인터페이스를 사용한다. 역할을 대체할 클래스들 사이에서 구현을 공유해야 할 필요가 있다면 추상 클래스를 사용하면 된다. 구현을 공유할 필요 없이 역할을 대체하는 객체들의 책임만 정의하고 싶다면 인터페이스를 사용하면 된다.

DiscountCondition을 암시적인 타입으로 다시 만들어주고 변화하는 행동에 따라 분리한 타입에 책임을 할당해주면 된다. 객체의 암시적인 타입에 따라 행동을 분기해야 한다면 암시적인 타입을 명시적인 클래스로 정의하고 행동을 나눔으로써 응집도 문제를 해결할 수 있다. 다시 말해 객체의 타입에 따라 변하는 행동이 있다면 타입을 분리하고 변화하는 행동을 각 타입의 책임으로 할당하라는 것이다. GRASP에선 이를 **POLYMORPHISM(다형성) 패턴**이라 한다.

> 📌 POLYMORPHISM 패턴
> 
> 객체의 타입에 따라 변하는 로직이 있을 때 변하는 로직을 담당할 책임을 어떻게 할당해야 하는가? 타입을 명시적으로 정의하고 각 타입에 다형적으로 행동하는 책임을 할당하라.
> 
> 조건에 따른 변화는 프로그램의 기본 논리다. 프로그램을 `if ~ else` 또는 `switch ~ case` 등의 조건 논리를 사용해서 설계한다면 새로운 변화가 일어난 경우 조건 논리를 수정해야 한다. 이것은 프로그램을 수정하기 어렵고 변경에 취약하게 만든다.
> 
> POLYMORPHISM 패턴은 객체의 타입을 검사해서 타입에 따라 여러 대안들을 수행하는 조건적인 논리를 사용하지 말라고 경고한다. 대신 다형성을 이용해 새로운 변화를 다루기 쉽게 확장하라고 권고한다.

### 변경으로부터 보호하기


## 책임 주도 설계의 대안