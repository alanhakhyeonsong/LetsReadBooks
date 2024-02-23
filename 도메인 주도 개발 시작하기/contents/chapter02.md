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
