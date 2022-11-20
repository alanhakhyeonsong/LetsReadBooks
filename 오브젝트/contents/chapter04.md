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
