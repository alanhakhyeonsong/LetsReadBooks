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
기획서, 유스케이스, 사용자 스토리와 같은 요구사항과 관련자와의 대화를 통해 도메인을 이해하고 이를 바탕으로 도메인 모델 초안을 만들어야 비로소 코드를 작성할 수 있다. 도메인을 모델링할 때 기본이 되는 작업은 모델을 구성하는 핵심 구성요소, 규칙, 기능을 찾는 것이다. 이 과정은 요구사항에서 출발한다.

주문 도메인의 예시를 살펴보자.

- 최소 한 종류 이상의 상품을 주문해야 한다.
- 한 상품을 한 개 이상 주문할 수 있다.
- 총 주문 금액은 각 상품의 구매 가격 합을 모두 더한 금액이다.
- 각 상품의 구매 가격 합은 상품 가격에 구매 개수를 곱한 값이다.
- 주문할 때 배송지 정보를 반드시 지정해야 한다.
- 배송지 정보는 받는 사람 이름, 전화번호, 주소로 구성된다.
- 출고를 하면 배송지를 변경할 수 없다.
- 출고 전에 주문을 취소할 수 있다.
- 고객이 결제를 완료하기 전에는 상품을 준비하지 않는다.

위 요구사항에서 알 수 있는 것은 주문은 '출고 상태로 변경하기', '배송지 정보 변경하기', '주문 취소하기', '결제 완료하기' 기능을 제공한다는 것이다.

```java
public class Order {
  public void changeShipped() { }
  public void changeShippingInfo(ShippingInfo newShipping) { }
  public void cancel() { }
  public void completePayment() { }
}
```

```java
public class OrderLine {
  private Product product;
  private int price;
  private int quantity;
  private int amounts;

  public OrderLine(Product product, int price, int quantity) {
    this.product = product;
    this.price = price;
    this.quantity = quantity;
    this.amounts = calculateAmounts();
  }

  private int calculateAmounts() {
    return price * quantity;
  }

  public int getAmounts() { }
}
```

다음 요구 사항은 `Order`와 `OrderLine`의 관계를 알려준다.

- 최소 한 종류 이상의 상품을 주문해야 한다.
- 총 주문 금액은 각 상품의 구매 가격 합을 모두 더한 금액이다.

```java
public class Order {
  private List<OrderLine> orderLines;
  private Money totalAmounts;

  public Order(List<OrderLine> orderLines) {
    setOrderLines(orderLines);
  }

  private void setOrderLines(List<OrderLine> orderLines) {
    verifyAtLeastOneOrMoreOrderLines(orderLines);
    this.orderLines = orderLines;
    calculateTotalAmounts();
  }

  private void verifyAtLeastOneOrMoreOrderLines(List<OrderLine> orderLines) {
    if (orderLines == null || orderLines.isEmpty()) {
      throw new IllegalArgumentException("no OrderLine");
    }
  }

  private void calculateTotalAmounts() {
    int sum = orderLines.stream()
                        .mapToInt(x -> x.getAmounts())
                        .sum();
    this.totalAmounts = new Money(sum);
  }

  // ...
}
```

```java
public class ShippingInfo {
  private String receiverName;
  private String receiverPhoneNumber;
  private String shippingAddress1;
  private String shippingAddress2;
  private String shippingZipcode;

  // constructor, getter
}
```

```java
public class Order {
  private List<OrderLine> orderLines;
  private Money totalAmounts;

  // ...

  public Order(List<OrderLine> orderLines, ShippingInfo shippingInfo) {
    setOrderLines(orderLines);
    setShippingInfo(shippingInfo);
  }

  private void setShippingInfo(ShippingInfo shippingInfo) {
    // 주문할 때 배송지 정보를 반드시 지정해야 한다.
    if (shippingInfo == null) {
      throw new IllegalStateException("no ShippingInfo");
    }
    this.shippingInfo = shippingInfo;
  }

  // ...
}
```

도메인을 구현하다 보면 특정 조건이나 상태에 따라 제약이나 규칙이 달리 적용되는 경우가 많다. 주문 요구사항에선 다음 내용이 제약과 규칙에 해당된다.

- 출고를 하면 배송지 정보를 변경할 수 없다.
- 출고 전에 주문을 취소할 수 있다.

이 요구사항은 출고 상태가 되기 전과 후의 제약사항을 기술하고 있다. 출고 상태에 따라 배송지 정보 변경 기능과 주문 취소 기능은 다른 제약을 갖는다. 이 요구사항을 충족하려면 주문은 최소한 출고 상태를 표현할 수 있어야 한다.

다음 요구사항도 상태와 관련이 있다.

- 고객이 결제를 완료하기 전에는 상품을 준비하지 않는다.

이 요구사항은 결제 완료 전을 의미하는 상태와 결제 완료 내지 상품 준비 중이라는 상태가 필요함을 알려준다.

```java
public enum OrderState {
  PAYMENT_WAITING,
  PREPARING,
  SHIPPED,
  DELIVERING,
  DELIVERY_COMPLETED,
  CANCELED;
}

public class Order {
  private OrderState state;

  public void changeShippingInfo(ShippingInfo newShippingInfo) {
    verifyNotYetShipped();
    setShippingInfo(newShippingInfo);
  }

  public void cancel() {
    verifyNotYetShipped();
    this.state = OrderState.CANCELED;
  }

  private void verifyNotYetShipped() {
    if (state != OrderState.PAYMENT_WAITING && state != OrderState.PREPARING) {
      throw new IllegalStateException("already shipped");
    }
  }

  // ...
}
```

## 엔티티와 밸류
### 엔티티
**엔티티의 가장 큰 특징은 식별자를 가진다는 것이다.** 엔티티 객체마다 고유해서 각 엔티티는 서로 다른 식별자를 갖는다.

- 주문 도메인에서 각 주문은 주문번호를 가지며 이는 각 주문마다 다르다.

주문에서 배송지 주소가 바뀌거나 상태가 바뀌더라도 주문번호가 바뀌지 않는 것처럼 엔티티의 식별자는 바뀌지 않는다. **엔티티를 생성하고 속성을 바꾸고 삭제할 때까지 식별자는 유지한다.**

엔티티의 식별자는 바뀌지 않고 고유하기 때문에 두 엔티티 객체의 식별자가 같으면 두 엔티티는 같다고 판단할 수 있다.

```java
public class Order {
  private String orderName;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (obj.getClass() != Order.class) return false;
    Order other = (Order) obj;
    if (this.orderNumber == null) return false;
    return this.orderNumber.equals(other.orderNumber);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((orderNumber == null) ? 0 : orderNumber.hashCode());
    return result;
  }
}
```

### 엔티티의 식별자 생성
엔티티의 식별자를 생성하는 시점은 도메인의 특징과 사용하는 기술에 따라 달라진다.

- 특정 규칙에 따라 생성
- UUID나 Nano ID와 같은 고유 식별자 생성기 사용
- 값을 직접 입력
- 일련번호 사용(시퀀스나 DB의 자동 증가 칼럼 사용)

자동 증가 칼럼을 제외한 다른 방식은 식별자를 먼저 만들고 엔티티 객체를 생성할 때 식별자를 전달한다.  
자동 증가 칼럼은 DB 테이블에 데이터를 삽입해야 비로소 값을 알 수 있기 때문에 테이블에 데이터를 추가하기 전에는 식별자를 알 수 없다. 이는 엔티티 객체를 생성할 때 식별자를 전달할 수 없음을 의미한다.

```java
Article article = new Article(author, title);
articleRepository.save(article);
Long savedArticleId = article.getId(); // DB 저장 후 식별자 참조 가능
```

### 밸류 타입
