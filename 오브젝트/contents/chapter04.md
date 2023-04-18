# 4장. 설계 품질과 트레이드오프
객체지향 설계란 올바른 객체에게 올바른 책임을 할당하면서 낮은 결합도와 높은 응집도를 가진 구조를 창조하는 활동이다.  
훌륭한 설계란 합리적인 비용 안에서 변경을 수용할 수 있는 구조를 만드는 것이다. 적절한 비용 안에서 쉽게 변경할 수 있는 설계는 응집도가 높고 서로 느슨하게 결합돼 있는 요소로 구성된다.

이번 장에선, 영화 예매 시스템을 책임이 아닌 상태를 표현하는 데이터 중심의 설계를 살펴보고, 객체지향적으로 설계한 구조와 어떤 차이점이 있는지 살펴본다.

## 데이터 중심의 영화 예매 시스템
- 데이터 중심의 관점에서 객체는 자신이 포함하고 있는 데이터를 조작하는 데 필요한 오퍼레이션을 정의한다. (상태 중심)
- 책임 중심의 관점에서 객체는 다른 객체가 요청할 수 있는 오퍼레이션을 위해 필요한 상태를 보관한다. (행동 중심)
- 객체의 상태는 구현에 속한다. 구현은 불안정하기 때문에 변하기 쉽다.
- 객체의 책임은 인터페이스에 속한다.

객체는 책임을 드러내는 안정적인 인터페이스 뒤로 책임을 수행하는 데 필요한 상태를 캡슐화함으로써 구현 변경에 대한 파장이 외부로 퍼져나가는 것을 방지한다.  
따라서 책임에 초점을 맞추면 상대적으로 변경에 안정적인 설계를 얻을 수 있게 된다.

```java
public class Movie {
    private String title;
    private Duration runningTime;
    private Money fee;
    private List<DiscountCondition> discountConditions;

    private MovieType movieType;
    private Money discountAmount;
    private double discountPercent;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Duration getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(Duration runningTime) {
        this.runningTime = runningTime;
    }

    public Money getFee() {
        return fee;
    }

    public void setFee(Money fee) {
        this.fee = fee;
    }

    public List<DiscountCondition> getDiscountConditions() {
        return Collections.unmodifiableList(discountConditions);
    }

    public void setDiscountConditions(List<DiscountCondition> discountConditions) {
        this.discountConditions = discountConditions;
    }

    public MovieType getMovieType() {
        return movieType;
    }

    public void setMovieType(MovieType movieType) {
        this.movieType = movieType;
    }

    public Money getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Money discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }
}
```

객체 안에 객체의 종류를 저장하는 인스턴스 변수 `movieType`이나 배타적으로 사용되는 인스턴스 변수 `discountAmount`, `discountPercent`를 하나의 클래스 안에 포함시키는 방식은 데이터 중심 설계 안에서 흔히 보인다.

데이터 중심의 설계에서는 객체가 포함해야 하는 데이터에 집중한다. `이 객체가 포함해야 하는 데이터는 무엇인가?`라는 질문을 객체의 책임을 결정하기 전에 반복한다면 데이터 중심의 설계에 매몰돼 있을 확률이 높다.

![image](https://user-images.githubusercontent.com/60968342/202902718-21450ae5-8dae-449a-b1ce-8a24555deea9.png)

`ReservationAgency`는 데이터 클래스를 조합해서 영화 예매 절차를 구현한다.

```java
package chapter03.before;

import static chapter03.before.DiscountConditionType.PERIOD;

public class ReservationAgency {
    public Reservation reserve(Screening screening, Customer customer, int audienceCount) {
        Movie movie = screening.getMovie();

        boolean discountable = false;
        for (DiscountCondition condition : movie.getDiscountConditions()) {
            if (condition.getType() == PERIOD) {
                discountable = screening.getWhenScreened().getDayOfWeek().equals(condition.getDayOfWeek()) &&
                        condition.getStartTime().compareTo(screening.getWhenScreened().toLocalTime()) <= 0 &&
                        condition.getEndTime().compareTo(screening.getWhenScreened().toLocalTime()) >= 0;
            } else {
                discountable = condition.getSequence() == screening.getSequence();
            }

            if (discountable) {
                break;
            }
        }

        Money fee;
        if (discountable) {
            Money discountAmount = Money.ZERO;
            switch (movie.getMovieType()) {
                case AMOUNT_DISCOUNT:
                    discountAmount = movie.getDiscountAmount();
                    break;
                case PERCENT_DISCOUNT:
                    discountAmount = movie.getFee().times(movie.getDiscountPercent());
                    break;
                case NONE_DISCOUNT:
                    discountAmount = Money.ZERO;
                    break;
            }

            fee = movie.getFee().minus(discountAmount);
        } else {
            fee = movie.getFee();
        }

        return new Reservation(customer, screening, fee, audienceCount);
    }
}
```

`reserve()`는 `DiscountCondition`에 대해 루프를 돌며 할인 가능 여부를 확인하고, `discountable` 변수의 값을 체크하여 적절한 할인 정책에 따라 예매 요금을 계산한다.

## 설계 트레이드오프
### 캡슐화
상태와 행동을 하나의 객체 안에 모으는 이유는 객체의 내부 구현을 외부로부터 감추기 위해서다. 오직 인터페이스만 밖으로 드러난다. 변경될 수 있는 어떤 것이라도 캡슐화해야 한다.

객체지향 설계의 가장 중요한 원리는 불안정한 구현 세부사항을 안정적인 인터페이스 뒤로 캡슐화하는 것이다.

> 유지보수성이 목표다. 여기서 유지보수성이란 두려움 없이, 주저함 없이, 저항감 없이 코드를 변경할 수 있는 능력을 말한다. ... 가장 중요한 동료는 캡슐화다. 캡슐화란 어떤 것을 숨긴다는 것을 의미한다. 우리는 시스템의 한 부분을 다른 부분으로부터 감춤으로써 뜻밖의 피해가 발생할 수 있는 가능성을 사전에 방지할 수 있다. 만약 시스템이 완전히 캡슐화된다면 우리는 변경으로부터 완전히 자유로워질 것이다. 만약 시스템의 캡슐화가 크게 부족하다면 우리는 변경으로부터 자유로울 수 없고, 결과적으로 시스템은 진화할 수 없을 것이다. 응집도, 결합도, 중복 역시 훌륭한(변경 가능한) 코드를 규정하는 데 핵심적인 품질인 것이 사실이지만 캡슐화는 우리를 좋은 코드로 안내하기 때문에 가장 중요한 제1원리다.

### 응집도와 결합도
구조적 설계 방법이 주도하던 시대에 소프트웨어의 품질을 측정하기 위해 소개된 기준이지만 객체지향의 시대에서도 여전히 유효하다.

응집도
- 객체지향 관점에서 응집도는 객체 또는 클래스에 얼마나 관련 높은 책임들을 할당했는지를 나타낸다.
  - 모듈에 포함된 내부 요소들이 연관돼 있는 정도
  - 모듈 내의 요소들이 하나의 목적을 위해 긴밀하게 협력한다면 그 모듈은 높은 응집도를 가진다.
  - 모듈 내의 요소들이 서로 다른 목적을 추구한다면 그 모듈은 낮은 응집도를 가진다.

결합도
- 객체지향 관점에서 결합도는 객체 또는 클래스가 협력에 필요한 적절한 수준의 관계만을 유지하고 있는지를 나타낸다.
  - 의존성의 정도를 나타내며 다른 모듈에 대해 얼마나 많은 지식을 갖고 있는지를 나타내는 척도다.
  - 즉 독립적 변경이 유연함의 정도다.

**변경의 관점에서 응집도란 변경이 발생할 때 모듈 내부에서 발생하는 변경의 정도다.** 하나의 변경을 수용하기 위해 모듈 전체가 함께 변경된다면 응집도가 높은 것이고, 일부만 변경된다면 응집도가 낮은 것이다. 또한 하나의 변경에 대해 하나의 모듈만 변경되면 응집도가 높지만, 다수의 모듈이 변경되면 응집도가 낮은 것이다.

![image](https://user-images.githubusercontent.com/60968342/202903362-98004501-4e60-4b42-9ecf-a5571b01187f.png)

응집도가 높으면 오직 하나만 수정하면 된다. 여러 모듈을 수정해야 한다면 응집도가 낮다.

![image](https://user-images.githubusercontent.com/60968342/202903384-6b049fdc-60e7-49a2-8fb2-4a730d1f5056.png)

결합도가 낮으면 하나의 모듈만 수정하면 된다. 한 모듈의 내부 구현의 변경이 다른 모듈에 영향을 미치면 결합도가 높다고 표현한다. 표준 라이브러리, 프레임워크에 의존하는 것은 상관 없다. 변경 확률이 매우 적기 때문이다.  
직접 작성한 코드는 항상 불안정하며 변경될 가능성이 크다. 따라서 낮은 결합도를 유지하려고 노력해야 한다.

## 데이터 중심의 영화 예매 시스템의 문제점
데이터 중심의 설계가 가진 대표적인 문제점을 요약하면 다음과 같다.
- 캡슐화 위반
- 높은 결합도
- 낮은 응집도

### 캡슐화 위반
```java
public class Movie {
    private Money fee;

    public Money getFee() {
        return fee;
    }

    public void setFee(Money fee) {
        this.fee = fee;
    }
}
```
객체의 내부에 직접 접근할 수 없기 때문에 캡슐화의 원칙을 지키는 것처럼 보인다. 하지만 getter/setter를 통해 인스턴스 변수를 노골적으로 드러내기 때문에 어떤 정보도 캡슐화하지 못한다.

설계할 때 협력에 관해 고민하지 않으면 캡슐화를 위반하는 과도한 접근자와 수정자를 가지게 되는 경향이 있다. 객체가 사용될 문맥을 추측할 수 밖에 없는 경우 개발자는 어떤 상황에서도 해당 객체가 사용될 수 있게 최대한 많은 접근자 메서드를 추가하게 되는 것이다. 이와 같은 경우를 추측에 의한 설계 전략이라 한다.

### 높은 결합도
객체 내부의 구현이 객체의 인스턴스에 드러난다는 것은 클라이언트가 구현에 강하게 결합된다는 것을 의미한다. 더 나쁜 소식은 단지 객체의 내부 구현을 변경했음에도 이 인터페이스에 의존하는 모든 클라이언트들도 함께 변경해야 한다는 것이다.

![image](https://user-images.githubusercontent.com/60968342/203056066-ee15f8b3-70bc-465f-a285-ef0ee2be41da.png)

데이터 중심의 설계는 전체 시스템을 하나의 거대한 의존성 덩어리로 만들어 버리기 때문에 어떤 변경이라도 일단 발생하고 나면 시스템 전체가 요동칠 수 밖에 없다.

### 낮은 응집도
서로 다른 이유로 변경되는 코드가 하나의 모듈 안에 공존할 때 모듈의 응집도가 낮다고 말한다.

다음과 같은 수정 사항이 발생하는 경우 `ReservationAgency`의 코드를 수정해야 할 것이다.
- 할인 정책 추가
- 할인 정책별 할인 요금 계산 방법 변경
- 할인 조건 추가
- 할인 조건별 할인 여부 판단 방법 변경
- 예매 요금 계산 방법 변경

낮은 응집도는 두 가지 측면에서 설계에 문제를 일으킨다.
- 변경의 이유가 서로 다른 코드들을 하나의 모듈 안에 뭉쳐놓았기 때문에 변경과 아무 상관이 없는 코드들이 영향을 받게 된다. 예를 들어 `ReservationAgency` 안에 할인 정책을 선택하는 코드와 할인 조건을 판단하는 코드가 함께 존재하기 때문에 새로운 할인 정책을 추가하는 작업이 할인 조건에도 영향을 미칠 수 있다. 어떤 코드를 수정한 후에 아무런 상관도 없던 코드에 문제가 발생하는 것은 모듈의 응집도가 낮을 때 발생하는 대표적인 증상이다.
- 하나의 요구사항 변경을 반영하기 위해 동시에 여러 모듈을 수정해야 한다. 응집도가 낮을 경우 다른 모듈에 위치해야 할 책임의 일부가 엉뚱한 곳에 위치하게 되기 때문이다. 새로운 할인 정책을 추가해야 한다면, `MovieType`에 새로운 할인 정책을 표현하는 열거형 값을 추가하고 `ReservationAgency`의 `reserve` 메서드의 `switch` 구문에 새로운 case 절을 추가해야 한다. 또한 새로운 할인 정책에 따라 할인 요금을 계산하기 위해 필요한 데이터도 `Movie`에 추가해야 한다. 새로운 할인 조건을 추가하는 경우도 마찬가지다.

> 📌 단일 책임 원칙(Single Responsibility Principle, SRP)
>
> 로버트 마틴은 모듈의 응집도가 변경과 연관이 있다는 사실을 강조하기 위해 단일 책임 원칙이라는 설계 원칙을 제시했다. 단일 책임 원칙을 한마디로 요약하면 클래스는 단 한가지의 변경 이유만 가져야 한다는 것이다. 한 가지 주의할 점은 단일 책임 원칙이라는 맥락에서 '책임'이라는 말이 '변경의 이유'라는 의미로 사용된다는 점이다. 단일 책임 원칙에서의 책임은 지금까지 살펴본 역할, 책임, 협력에서 이야기하는 책임과는 다르며 변경과 관련된 더 큰 개념을 가리킨다.

## 자율적인 객체를 향해
- 캡슐화를 지켜라
- getter/setter 등으로 자신의 필드 타입을 노출시키는 행위를 최소화 하자
- 객체는 스스로의 상태를 책임져야 하며 외부에서는 인터페이스에 정의된 메서드를 통해서만 상태에 접근할 수 있어야 한다.

## 스스로 자신의 데이터를 책임지는 객체
- 객체 내부에 저장되는 데이터보다 객체가 협력에 참여하며 수행할 책임을 정의하는 오퍼레이션이 더 중요하다.
- 다음 두 질문을 조합하면 새로운 데이터 타입을 만들 수 있다.
  - 이 객체가 어떤 데이터를 포함해야 하는가?
  - 이 객체가 데이터에 대해 수행해야 하는 오퍼레이션은 무엇인가?

![image](https://user-images.githubusercontent.com/60968342/203060537-2914e125-4caf-4462-ac84-5f047059e761.png)

이처럼 변경한 결과 각 데이터의 수정에 대한 책임을 각 객체에 할당하게 되었다.

## 여전히 부족하다.
`DiscountCondition`은 여전히 내부 인스턴스 변수의 타입을 외부 인터페이스로 노출시킨다. 또한 `Movie`는 할인 정책의 종류를 인터페이스에 노출시키고 있다. 새로운 할인 정책이 추가되거나 제거되면 `screnning`을 변경해야 한다.

내부 구현의 변경이 외부로 퍼져나가는 파급 효과는 캡슐화가 부족하다는 명백한 증거이다.

### 높은 결합도
`Movie`와 `DiscountCondition` 사이의 결합도를 보면 다음과 같은 영향을 살펴볼 수 있다.
- `DiscountCondition`의 기간 할인 조건의 명칭이 `PERIOD`에서 다른 값으로 변경된다면 `Movie`를 수정해야 한다.
- `DiscountConditon`의 종류가 추가되거나 삭제된다면 `Movie` 안의 if ~ else 구문을 수정해야 한다.
- 각 `DiscountCondition`의 만족 여부를 판단하는 데 필요한 정보가 변경된다면 `Movie`의 `isDiscountable` 메서드 시그니처도 함께 변경될 것이고 결과적으로 이 메서드에 의존하는 `Screening`에 대한 변경을 초래할 것이다.

### 낮은 응집도
위 내용과 같은 이야기이다.

## 데이터 중심 설계의 문제점
데이터 중심의 설계가 변경에 취약한 이유는 다음 두 가지다.
- 본질적으로 너무 이른 시기에 데이터에 관해 결정하도록 강요한다.
- 협력이라는 문맥을 고려하지 않고 객체를 고립시킨 채 오퍼레이션을 결정한다.

### 데이터 중심 설계는 객체의 행동보다는 상태에 초점을 맞춘다
데이터는 구현의 일부다. 데이터 주도 설계는 설계를 시작하는 처음부터 데이터에 관해 결정하도록 강요하기 때문에 너무 이른 시기에 내부 구현에 초점을 맞추게 한다.  
데이터 중심의 관점에서 객체는 그저 단순한 데이터의 집합체일 뿐이다. 이로 인해 접근자와 수정자를 과도하게 추가하게 되고 이 데이터 객체를 사용하는 절차를 분리된 별도의 객체 안에 구현하게 된다. 접근자와 수정자는 public 속성과 큰 차이가 없기 때문에 객체의 캡슐화는 완전히 무너질 수 밖에 없다.

결론적으로 객체 내부 구현이 객체의 인터페이스를 어지럽히고 객체의 응집도와 결합도에 나쁜 영향을 미치기 때문에 변경에 취약한 코드를 낳게 된다.

### 데이터 중심 설계는 객체를 고립시킨 채 오퍼레이션을 정의하도록 만든다
올바른 객체지향 설계의 무게 중심은 항상 객체의 내부가 아니라 외부에 맞춰져 있어야 한다. 객체가 내부에 어떤 상태를 가지고 그 상태를 어떻게 관리하는가는 부가적인 문제다. 중요한 것은 객체가 다른 객체와 협력하는 방법이다.

데이터 중심 설계에서 초점은 객체의 내부로 향한다. 실행 문맥에 대한 깊이있는 고민 없이 객체가 관리할 데이터의 세부 정보를 먼저 결정한다. 객체의 구현이 이미 결정된 상태에서 다른 객체와의 협력 방법을 고민하기 때문에 이미 구현된 객체의 인터페이스를 억지로 끼워맞출 수밖에 없다.