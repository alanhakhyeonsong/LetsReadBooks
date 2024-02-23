# Chapter 2. 아키텍처 개요
## 네 개의 영역
표현, 응용, 도메인, 인프라스트럭처는 아키텍처를 설계할 때 출현하는 전형적인 네 가지 영역이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c5ac0c2a-c0b6-4e71-b8d4-152780aabf4a)

- 표현 영역: 사용자의 요청을 받아 응용 영역에 전달하고 응용 영역의 처리 결과를 다시 사용자에게 보여주는 역할
- 응용 영역: 시스템이 사용자에게 제공해야 할 기능을 구현한다.
  - 로직을 직접 수행하기 보단 도메인 모델에 로직 수행을 위임한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f5decfd5-3ad7-4414-a2d4-177730e888dd)

- 도메인 영역: 도메인 모델을 구현한다. 도메인 모델은 도메인의 핵심 로직을 구현한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ef1f2a66-b35a-4e07-a8a2-edb4e455d793)

- 인프라스트럭처 영역: 구현 기술에 대한 것을 다룬다.
  - RDBMS 연동 처리
  - MQ에 메시지를 전송하거나 수신하는 기능 구현
  - MongoDB나 Redis와의 데이터 연동 처리
  - SMTP를 이용한 메일 발송 기능 구현
  - HTTP 클라이언트를 이용해 REST API를 호출

도메인 영역, 응용 영역, 표현 영역은 구현 기술을 사용한 코드를 직접 만들지 않는다. 대신 인프라스트럭처 영역에서 제공하는 기능을 사용하여 필요한 기능을 개발한다.

- 응용 영역에서 DB에 보관된 데이터가 필요하면 인프라스트럭처 영역의 DB 모듈을 사용하여 데이터를 읽어온다.
- 외부에 메일을 발송해야 한다면 인프라스트럭처가 제공하는 SMTP 연동 모듈을 이용해 메일을 발송한다.

## 계층 구조 아키텍처
계층 구조는 그 특성상 상위 계층에서 하위 계층으로의 의존만 존재하고 하위 계층은 상위 계층에 의존하지 않는다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3ef8fd79-ee22-4a32-ab5c-b1659e69c6a9)

표현 계층은 응용 계층에 의존하고 응용 계층이 도메인 계층에 의존하지만, 반대로 인프라스트럭처 계층이 도메인에 의존하거나 도메인이 응용 계층에 의존하진 않는다.

계층 구조를 엄격하게 적용한다면 상위 계층은 바로 아래 계층에만 의존을 가져야 하지만 구현의 편리함을 위해 계층 구조를 유연하게 적용하기도 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/dabe4da8-83f9-4442-977e-edc788d0c8c3)

표현, 응용, 도메인 계층이 상세한 구현 기술을 다루는 인프라스트럭처 계층에 종속된다는 점에 유의하자.

도메인 가격 계산 규칙을 룰 엔진을 사용해서 구현한다 해보자.

```java
public class DroolsRuleEngine {
  private KieContainer kContainer;

  public DroolsRuleEngine() {
    KieService ks = KieServices.Factory.get();
    kContainer = ks.getKieClasspathContainer();
  }
  
  public void evaluate(String sessionName, List<?> facts) {
    KieSession kSession = KContainer.newKieSession(sessionName);
    try {
      facts.forEach(x -> kSession.insert(x));
      kSession.fireAllRules();
    } finally {
      kSession.dispose();
    }
  }
}

public class CalculateDiscountService {
  private DroolsRuleEngine ruleEngine;

  public CalculateDiscountService() {
    ruleEngine = new DroolsRuleEngine();
  }

  public Money calculateDiscount(List<OrderLine> orderLines, String customerId) {
    Customer customer = findCustomer(customerId);

    // Drools에 특화된 코드: 연산결과를 받기 위해 추가한 타입
    MutableMoney money = new MutableMoney(0);
    // Drools에 특화된 코드: 룰에 필요한 데이터(지식)
    List<?> facts = Arrays.asList(customer, money);
    facts.addAll(orderLines);
    // Drools에 특화된 코드: Drools의 세션 이름
    ruleEngine.evaluate("discountCalculation", facts);
    return money.toImmutableMoney();
  }

  // ...
}
```

위 코드는 두 가지 문제를 안고 있다.

- `CalculateDiscountService`만 테스트하기 어렵다.
  - `RuleEngine`이 완벽하게 동작해야 테스트가 가능함.
- 구현 방식을 변경하기 어렵다.
  - Drools의 세션 이름을 변경하면 `CalculateDiscountService`의 코드도 함께 변경해야 한다.

이런 상황에서 Drools가 아닌 다른 구현 기술을 사용하려면 코드의 많은 부분을 고쳐야 한다. **인프라스트럭처에 의존하면 '테스트 어려움'과 '기능 확장의 어려움'이라는 두 가지 문제가 발생한다.** 이 두 문제를 해소하는 방법은 DIP에 있다.

## DIP
가격 할인 계산을 하려면 고객 정보를 구해야 하고, 구한 고객 정보와 주문 정보를 이용해 룰을 실행해야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/7863e96e-8781-44ad-880b-e113f29910e3)

- `CalculateDiscountService`는 고수준의 모듈
- 고수준 모듈: 의미 있는 단일 기능을 제공하는 모듈
- 고수준 모듈의 기능을 구현하려면 여러 하위 기능이 필요하다.
- 고수준 모듈이 제대로 동작하려면 저수준 모듈을 사용해야 한다.
  - 고수준 모듈이 저수준 모듈을 사용하면 구현 변경과 테스트가 어렵다는 문제가 발생한다.
  - 문제를 해결하기 위해 DIP를 통해 저수준 모듈이 고수준 모듈에 의존하도록 바꾼다.

이는 추상화한 인터페이스를 통해 해결할 수 있다.

```java
public interface RuleDiscounter {
  Money applyRules(Customer customer, List<OrderLine> orderLines);
}

public class CalculateDiscountService {
  private RuleDiscounter ruleDiscounter;

  public CalculateDiscountService(RuleDiscounter ruleDiscounter) {
    this.ruleDiscounter = ruleDiscounter;
  }

  public Money calculateDiscount(List<OrderLine> orderLines, String customerId) {
    Customer customer = findCustomer(customerId);
    return ruleDiscounter.applyRules(customer, orderLines);
  }

  // ...
}
```

`CalculateDiscountService`에는 Drools에 의존하는 코드가 없다. 단지 `RuleDiscounter`가 룰을 적용한다는 사실만 알 뿐이다. `RuleDiscounter`의 구현 객체는 생성자를 통해 전달받는다.

```java
public class DroolsRuleDiscounter implements RuleDiscounter {
  // ...
}
```

이렇게 바뀌면 `CalculateDiscountService`는 더 이상 구현 기술에 의존하지 않는다. '룰을 이용한 할인 금액 계산'은 고수준 모듈의 개념이므로 `RuleDiscounter` 인터페이스는 고수준 모듈에 속한다. `DroolsRuleDiscounter`는 고수준의 하위 기능인 `RuleDiscounter`를 구현한 것이므로 저수준 모듈에 속한다.

DIP를 적용하면 저수준 모듈이 고수준 모듈에 의존하게 된다. 이를 DIP, 의존 역전 원칙이라 부른다.

DIP를 적용하면 앞서 언급한 두 가지 문제를 해결할 수 있다.

```java
// 사용할 저수준 객체 생성
RuleDiscounter ruleDiscounter = new DroolsRuleDiscounter();

// 사용할 저수준 구현 객체 변경 시
// RuleDiscounter ruleDiscounter = new SimpleRuleDiscounter();

// 생성자 방식으로 주입
CalculateDiscountService disService = new CalculateDiscountService(ruleDiscounter);
```

테스트 역시 인터페이스를 의존하는 구조라면 대역 객체를 사용해서 테스트를 진행할 수 있다.

```java
public class CalculateDiscountServiceTest {

  @Test
  public void noCustomer_thenExceptionShouldBeThrown() {
    // 테스트 목적의 대역 객체
    CustomerRepository stubRepo = mock(CustomerRepository.class);
    when(stubRepo.findById("noCustId")).thenReturn(null);

    RuleDiscounter stubRule = (cust, lines) -> null;

    // 대용 객체를 주입받아 테스트 진행
    CalculateDiscountService calDisSvc = new CalculateDiscountService(stubRepo, stubRule);
    assertThrows(NoCustomerException.class, () -> calDisSvc.calculateDiscount(someLines, "noCustId"));
  }
}
```

### DIP 주의사항
DIP를 잘못 생각하면 단순히 인터페이스와 구현 클래스를 분리하는 정도로 받아들일 수 있다. DIP의 핵심은 고수준 모듈이 저수준 모듈에 의존하지 않도록 하기 위함인데 이를 적용한 결과 구조만 보고 저수준 모듈에서 인터페이스를 추출하는 경우가 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c1648fe4-3a2d-42c3-801b-ddf296167b0d)

이는 잘못된 구조다. 도메인 영역은 구현 기술을 다루는 인프라스트럭처 영역에 의존하고 있다. 즉, 여전히 고수준 모듈이 저수준 모듈에 의존하고 있는 것이다. DIP를 적용할 때 하위 기능을 추상화한 인터페이스는 고수준 모듈 관점에서 도출한다. 즉, '할인 금액 계산'을 추상화한 인터페이스는 저수준 모듈이 아닌 고수준 모듈에 위치한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e551d09f-7f97-4f5d-9b30-4bf7437e9d76)

### DIP와 아키텍처
인프라스트럭처 영역은 구현 기술을 다루는 저수준 모듈이고 응용 영역과 도메인 영역은 고수준 모듈이기 때문에 DIP를 적용하면 아래와 같이 인프라스트럭처 영역이 응용 영역과 도메인 영역에 의존(상속)하는 구조과 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d7399150-4da2-4367-a275-b33a218ca124)

인프라스트럭처에 위치한 클래스가 도메인이나 응용 영역에 정의된 인터페이스를 상속받아 구현하는 구조가 되므로 도메인과 응용 영역에 대한 영향을 주지 않거나 최소화 하면서 구현 기술을 변경하는 것이 가능하다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d57a8622-9de4-496a-94e8-0b9159b2491b)

## 도메인 영역의 주요 구성요소
