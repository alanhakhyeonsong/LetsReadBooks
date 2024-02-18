# Chapter 1. 도메인 모델 시작하기
## 도메인이란?
소프트웨어로 해결하고자 하는 문제 영역은 도메인에 해당한다. 한 도메인은 다시 하위 도메인으로 나눌 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/7c312f02-547a-470b-8ae4-d8326536959b)

특정 도메인을 위한 소프트웨어라 해서 도메인이 제공해야 할 모든 기능을 직접 구현하는 것은 아니다. 결제 시스템을 직접 구현하기보단 결제 대행업체를 이용해서 처리할 때가 많다.

도메인마다 고정된 하위 도메인이 존재하는 것은 아니다.

## 도메인 모델
**기본적으로 도메인 모델은 특정 도메인을 개념적으로 표현한 것이다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d824ba59-8d69-4d5c-bcc1-560cea4093ec)

위 모델을 보면 주문은 주문번호와 지불할 총금액이 있고, 배송정보를 변경할 수 있음을 알 수 있다. 또한 주문을 취소할 수 있다는 것도 알 수 있다. 도메인 모델을 사용하면 여러 관계자들이 동일한 모습으로 도메인을 이해하고 도메인 지식을 공유하는 데 도움이 된다.

도메인을 이해하려면 도메인이 제공하는 기능과 도메인의 주요 데이터 구성을 파악해야 하는데, 이런 면에서 기능과 데이터를 함께 보여주는 객체 모델은 도메인을 모델링하기 적합하다.

도메인 모델은 기본적으로 도메인 자체를 이해하기 위한 개념 모델이다.

## 도메인 모델 패턴
일반적인 애플리케이션의 아키텍처는 네 개의 영역으로 구분된다.

- Presentation: 사용자의 요청을 처리하고 사용자에게 정보를 보여준다. 여기서 사용자는 소프트웨어를 사용하는 사람뿐만 아니라 외부 시스템일 수도 있다.
- Application: 사용자가 요청한 기능을 실행한다. 업무 로직을 직접 구현하지 않으며 도메인 계층을 조합해서 기능을 실행한다.
- Domain: 시스템이 제공할 도메인 규칙을 구현한다.
- Infrastructure: 데이터베이스나 메시징 시스템과 같은 외부 시스템과의 연동을 처리한다.

도메인 계층은 도메인의 핵심 규칙을 구현한다.

- 출고 전에 배송지를 변경할 수 있다.
- 주문 취소는 배송 전에만 할 수 있다.

이런 도메인 규칙을 객체 지향 기법으로 구현하는 패턴이 도메인 모델 패턴이다.

```java
public class Order {
  private OrderState state;
  private ShippingInfo shippingInfo;

  public void changeShippingInfo(ShippingInfo newShippingInfo) {
    if (!state.isShippingChangeable()) {
      throw new IllegalStateException("can't change shipping in " + state);
    }
    this.shippingInfo = newShippingInfo;
  }
  // ...
}

public enum OrderState {
  PAYMENT_WAITING {
    public boolean isShippingChangeable() {
      return true;
    }
  },
  PREPARING {
    public boolean isShippingChangeable() {
      return true;
    }
  },
  SHIPPED, DELIVERING, DELIVERY_COMPLETED;

  public boolean isShippingChangeable() {
    return false;
  }
}
```

위 코드는 주문 도메인의 일부 기능을 도메인 모델 패턴으로 구현한 것이다. 주문 상태를 표현하는 `OrderState`는 배송지를 변경할 수 있는지를 검사할 수 있는 `isShippingChangeable()` 메서드를 제공하고 있다.

큰 틀에서 보면 `OrderState`는 `Order`에 속한 데이터이므로 배송지 정보 변경 가능 여부를 판단하는 코드를 `Order`로 이동할 수도 있다.

```java
public class Order {
  private OrderState state;
  private ShippingInfo shippingInfo;

  public void changeShippingInfo(ShippingInfo newShippingInfo) {
    if (!isShippingChangeable()) {
      throw new IllegalStateException("can't change shipping in " + state);
    }
    this.shippingInfo = newShippingInfo;
  }

  private boolean isShippingChangeable() {
    return state == OrderState.PAYMENT_WAITING || state == OrderState.PREPARING;
  }
  // ...
}

public enum OrderState {
  PAYMENT_WAITING, PREPARING, SHIPPED, DELIVERING, DELIVERY_COMPLETED;
}
```

배송지 변경이 가능한지를 판단할 규칙이 주문 상태와 다른 정보를 사용한다면 `OrderState`만으론 배송지 변경 가능 여부를 판단할 수 없으므로 `Order`에서 로직을 구현해야 한다.

중요한 점은 주문과 관련된 중요 업무 규칙을 주문 도메인 모델인 `Order`나 `OrderState`에서 구현한다는 점이다. 핵심 규칙을 구현한 코드는 도메인 모델에만 위치하기 때문에 규칙이 바뀌거나 규칙을 확장해야 할 때 다른 코드에 영향을 덜 주고 변경 내역을 모델에 반영할 수 있게 된다.

개념 모델은 순수하게 문제를 분석한 결과물이다. 이는 데이터베이스, 트랜잭션 처리, 성능, 구현 기술과 같은 것을 고려하고 있지 않기 때문에 실제 코드를 작성할 때 개념 모델을 있는 그대로 사용할 수 없다. 그래서 개념 모델을 구현 가능한 형태의 모델로 전환하는 과정을 거치게 된다.

## 도메인 모델 도출
