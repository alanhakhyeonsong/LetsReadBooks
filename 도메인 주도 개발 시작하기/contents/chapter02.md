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
- 엔티티(Entity)
  - 고유의 식별자를 갖는 객체로 자신의 라이프 사이클을 갖는다.
  - 주문, 회원, 상품과 같이 도메인의 고유한 개념을 표현한다.
  - 도메인 모델의 데이터를 포함하며 해당 데이터와 관련된 기능을 함께 제공한다.
- 밸류(Value)
  - 고유의 식별자를 갖지 않는 객체로 주로 개념적으로 하나인 값을 표현할 때 사용된다.
  - 배송지 주소를 표현하기 위한 주소나 구매 금액을 위한 금액과 같은 타입을 말한다.
  - 엔티티의 속성 뿐만 아니라 다른 밸류 타입의 속성으로도 사용 가능하다.
- 애그리거트(Aggregate)
  - 연관된 엔티티와 밸류 객체를 개념적으로 하나로 묶은 것이다.
  - 주문과 관련된 `Order` 엔티티, `OrderLine` 밸류, `Orderer` 밸류 객체를 '주문' 애그리거트로 묶을 수 있다.
- 리포지터리(Repository)
  - 도메인 모델의 영속성을 처리
  - DBMS 테이블에서 엔티티 객체를 로딩하거나 저장하는 기능을 제공
- 도메인 서비스(Domain Service)
  - 특정 엔티티에 속하지 않은 도메인 로직을 제공한다.
  - '할인 계산 금액'은 상품, 쿠폰, 회원 등급, 구매 금액 등 다양한 조건을 이용해서 구현하게 되는데, 이렇게 도메인 로직이 여러 엔티티와 밸류를 필요로 하면 도메인 서비스에서 로직을 구현한다.

### 엔티티와 밸류
도메인 모델의 엔티티와 DB 테이블의 엔티티는 같은 것이 아니다.

- 두 모델의 가장 큰 차이점은 도메인 모델 엔티티는 데이터와 도메인 기능을 제공한다는 점이다.
- 도메인 관점에서 기능을 구현하고 기능 구현을 캡슐화해서 데이터가 임의로 변경되는 것을 막는다.
- 또 다른 차이점은 도메인 모델의 엔티티는 두 개 이상의 데이터가 개념적으로 하나인 경우 밸류 타입을 이용해서 표현할 수 있다.
- RDBMS와 같은 관계형 데이터베이스는 밸류 타입을 제대로 표현하기 힘들다.

```java
public class Order {
  // 주문 도메인 모델의 데이터
  private OrderNo number;
  private Orderer orderer;
  private ShippingInfo shippingInfo;
  // ...

  // 도메인 모델 엔티티는 도메인 기능도 함께 제공
  public void changeShippingInfo(ShippingInfo newShippingInfo) {
    // ...
  }
}

public class Orderer {
  private String name;
  private String email;

  // ...
}
```

RDBMS와 같은 관계형 데이터베이스는 밸류 타입을 제대로 표현하기 힘들다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/dfda651c-46b3-4c59-bf75-bba4d2503549)

- 왼쪽 테이블의 경우에는 주문자라는 개념이 드러나지 않고 주문자의 개별 데이터만 드러난다.
- 오른쪽 테이블의 경우 주문자 데이터를 별도로 테이블에 저장했지만 이것은 테이블의 엔티티에 가까우며 밸류 타입이 드러나지 않는다.

### 애그리거트
도메인이 커질수록 개발할 도메인 모델도 커지면서 많은 엔티티와 밸류가 출현한다. 엔티티와 밸류 개수가 많아질수록 모델은 점점 더 복잡해진다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f460846b-d58b-496a-93e2-7e527a1fd7cd)

도메인 모델이 복잡해지면 개발자가 전체 구조가 아닌 한 개의 엔티티와 밸류에 집중하는 상황이 발생해 문제가 생길 수 있다. 이때 상위 수준에서 모델을 관리하지 않고 개별 요소에만 초점을 맞추다 보면, 큰 수준에서 모델을 이해하지 못해 큰 틀에서 모델을 관리할 수 없는 상황에 빠질 수 있다.

도메인 모델은 개별 객체뿐만 아니라 상위 수준에서 모델을 볼 수 있어야 전체 모델의 관계와 개별 모델을 이해하는 데 도움이 된다. 도메인 모델에서 전체 구조를 이해하는 데 도움이 되는 것이 바로 애그리거트다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/72e622d2-1986-4375-a0f3-8bccb7795313)

- 애그리거트는 관련 객체를 하나로 묶은 군집이다.
- 대표적인 예가 주문이다.
- 주문 도메인에는 주문, 배송지 정보, 주문자, 주문 목록, 총 결제 금액의 하위 모델로 구성된다.
- 이 하위 개념을 표현한 모델을 하나로 묶어 '주문'이라는 상위 개념으로 표현할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e51d86f7-67c4-4b5c-82f9-ab1ed4cfa648)

- 애그리거트는 군집에 속한 객체를 관리하는 루트 엔티티를 갖는다.
- 루트 앤티티는 애그리거트에 속해있는 엔티티와 밸류 객체를 이용해서 애그리거트가 구현해야 할 기능을 제공한다.
- 애그리거트를 사용하는 코드는 애그리거트 루트가 제공하는 기능을 실행하고 애그리거트 루트를 통해 간접적으로 애그리거트 내의 다른 엔티티나 밸류 객체에 접근한다.

### 리포지터리
도메인 객체를 지속적으로 사용하려면 RDBMS, NoSQL, 로컬 파일과 같은 물리적인 저장소에 도메인 객체를 보관해야 한다. 이를 위한 도메인 모델이 리포지터리다. 엔티티나 밸류가 요구사항에서 도출되는 도메인 모델이라면 리포지터리는 구현을 위한 도메인 모델이다. 리포지터리는 에그리거트 단위로 도메인 객체를 저장하고 조회한다.

```java
public interface OrderRepository {
  Order findByMember(OrderNumber number);
  void save(Order order);
  void delete(Order order);
}

public class CancelOrderService {
  private OrderRepository orderRepository;

  public void cancel(OrderNumber number) {
    Order order = orderRepository.findByNumber(number);
    if (order == null) throw new NoOrderException(number);
    order.cancel();
  }
}
```

도메인 모델 관점에서 리포지터리는 도메인 객체를 영속화하는 데 필요한 기능을 추상화한 것으로 고수준 모듈에 속한다. 기반 기술을 이용해서 리포지터리를 구현한 클래스는 저수준 모듈로 인프라스트럭처 영역에 속한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/28184cc9-57da-4f9b-ae95-5e40c5933fe0)

응용 서비스는 의존 주입과 같은 방식을 사용해서 실제 리포지터리 구현 객체에 접근한다. 응용 서비스와 리포지터리는 밀접한 연관이 있다.

- 응용 서비스는 필요한 도메인 객체를 구하거나 저장할 때 리포지터리를 사용한다.
- 응용 서비스는 트랜잭션을 관리하는데, 트랜잭션 처리는 리포지터리 구현 기술의 영향을 받는다.

리포지터리를 사용하는 주체가 응용 서비스이기 때문에 리포지터리는 응용 서비스가 필요로 하는 메서드를 제공한다. 다음 두 메서드가 기본이 된다.

- 애그리거트를 저장하는 메서드
- 애그리거트 루트 식별자로 애그리거트를 조회하는 메서드

## 요청 처리 흐름
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ce27bbce-dca2-4769-a0cb-08aa8277818c)

- 표현 영역은 사용자가 전송한 데이터 형식이 올바른지 검사하고 문제가 없다면 데이터를 이용해서 응용 서비스에 기능 실행을 위임한다.
- 응용 서비스는 도메인 모델을 이용해서 기능을 구현한다. 기능 구현에 필요한 도메인 객체를 리포지터리에서 가져와 실행하거나 신규 도메인 객체를 생성해서 리포지터리에 저장한다. 필요에 따라 트랜잭션을 관리한다.

```java
public class CancelOrderService {
  private OrderRepository orderRepository;

  @Transactional
  public void cancel(OrderNumber number) {
    Order order = orderRepository.findByNumber(number);
    if (order == null) throw new NoOrderException(number);
    order.cancel();
  }

  // ...
}
```

## 인프라스트럭처 개요
인프라스트럭처는 표현 영역, 응용 영역, 도메인 영역을 지원한다. 도메인 객체의 영속성 처리, 트랜잭션, SMTP 클라이언트, REST 클라이언트 등 다른 영역에서 필요로 하는 프레임워크, 구현 기술, 보조 기능을 지원한다.

도메인 영역과 응용 영역에서 인프라스트럭처의 기능을 직접 사용하는 것보다 이 두 영역에서 정의한 인터페이스를 인프라스트럭처 영역에서 구현하는 것이 시스템을 더 유연하고 테스트하기 쉽게 만들어준다.

하지만 무조건 인프라스트럭처에 대한 의존을 없앨 필요는 없다. 스프링을 사용할 경우 응용 서비스는 트랜잭션 처리를 위해 스프링이 제공하는 `@Transactional`을 사용하는 것이 편리하다. 영속성 처리를 위해 JPA를 사용할 경우 `@Entity`나 `@Table`과 같은 JPA 전용 애너테이션을 도메인 모델 클래스에 사용하는 것이 XML 매핑 설정을 이용하는 것보다 편리하다.

구현의 편리함은 DIP가 주는 다른 장점만큼 중요하기 때문에 DIP의 장점을 해치지 않는 범위에서 응용 영역과 도메인 영역에서 구현 기술에 대한 의존을 가져가는 것이 나쁘지 않다. 응용 영역과 도메인 영역이 인프라스트럭처에 대한 의존을 완전히 갖지 않도록 시도하는 것은 자칫 구현을 더 복잡하고 어렵게 만들 수 있다.

스프링의 `@Transactional` 애너테이션을 사용하면 한 줄로 트랜잭션을 처리할 수 있는데 코드에서 스프링에 대한 의존을 없애려면 복잡한 스프링 설정을 사용해야 한다. 의존은 없앴지만 특별히 테스트를 더 쉽게 할 수 있다거나 유연함을 증가시켜 주지 못한다. 단지 설정만 복잡해지고 개발 시간만 늘어날 뿐이다.

## 모듈 구성
아키텍처의 각 영역은 별도 패키지에 위치한다. 도메인이 크면 하위 도메인으로 나누고 각 하위 도메인마다 별도 패키지를 구성한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/37a30c1d-494d-4555-b5cb-c3059f38ec74)

도메인 모듈은 도메인에 속한 애그리거트를 기준으로 다시 패키지를 구성한다. 예를 들어 카탈로그 하위 도메인이 상품 애그리거트와 카테고리 애그리거트로 구성될 경우 아래와 같이 도메인을 두 개의 하위 패키지로 구성할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f33ab68c-6458-4ac8-a532-f23bd7cb11f6)

애그리거트, 모델, 리포지터리는 같은 패키지에 위치시킨다. 예를 들어 주문과 관련된 `Order`, `OrderLine`, `Orderer`, `OrderRepository` 등은 `com.myshop.order.domain` 패키지에 위치시킨다.

도메인이 복잡하면 도메인 모델과 도메인 서비스를 별도 패키지에 위치시킬 수도 있다.

- `com.myshop.order.domain.order`: 애그리거트 위치
- `com.myshop.order.domain.service`: 도메인 서비스 위치

응용 서비스도 다음과 같이 도메인 별로 패키지를 구분할 수 있다.

- `com.myshop.catalog.application.product`
- `com.myshop.catalog.application.category`

모듈 구조를 얼마나 세분화해야 하는지에 대해 정해진 규칙은 없다. 한 패키지에 너무 많은 타입이 몰려 코드를 찾을 때 불편한 정도만 아니면 된다.