# Chapter 3. 애그리거트
## 애그리거트
온라인 쇼핑몰 시스템을 개발할 때 아래와 같이 상위 수준 개념을 이용해서 전체 모델을 정리하면 전반적인 관계를 이해하는 데 도움이 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9f72a67f-01e5-462d-ad68-70c66faba024)

위의 상위 수준 모델을 개별 단위 객체로 다시 그려보면 아래와 같다. 상위 모델에 대한 이해 없이 개별 객체 단위 수준에서 개념을 파악하려면 더 오랜 시간이 걸린다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/209c8c39-4be4-4266-a0a2-49fa556356ab)

백 개 이상의 테이블을 한 장의 ERD에 표시하면, 개별 테이블 간의 관계를 파악하느라 전반적인 구조나 큰 수준에서의 도멩니 간의 관계를 파악하기 어려워져, 코드를 변경하고 확장하는 것이 어려워진다. 복잡한 도메인을 이해하고 관리하기 쉬운 방법이 바로 **애그리거트**다.

애그리거트는 관련 객체를 하나의 군으로 묶어 준다. 수많은 객체를 애그리거트로 묶어서 바라보면 상위 수준에서 도메인 모델 간의 관계를 파악할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/455a5c99-703c-4de4-bf40-d4d41f2e1329)

위는 모델을 애그리거트 단위로 묶어 다시 표현한 것이다. 동일한 모델이지만 애그리거트를 사용함으로써 모델 간의 관계를 개별 모델 수준과 상위 수준에서 모두 이해할 수 있다.

- 애그리거트는 일관성을 관리하는 기준이 된다.
- 복잡한 도메인을 단순한 구조로 만들어주므로 도메인 기능을 확장하고 변경하는데 효율적이다.
- 한 애그리거트에 속한 객체는 유사하거나 동일한 라이프 사이클을 갖는다.
- 한 애그리거트에 속한 객체는 다른 애그리거트에 속하지 않는다.
- 애그리거트는 독립된 객체 군으로 각 애그리거트는 자기 자신을 관리할 뿐 다른 애그리거트를 관리하지 않는다.

**경계를 설정할 때 기본이 되는 것은 도메인 규칙과 요구사항이다.** 도메인 규칙에 따라 함께 생성되는 구성요소는 한 애그리거트에 속할 가능성이 높다.

흔히 'A가 B를 갖는다'로 설계할 수 있는 요구사항이 있다면 A와 B를 한 애그리거트로 묶어서 생각하기 쉽다. 주문의 경우 `Order`가 `ShippingInfo`와 `Orderer`를 가지므로 이는 어느 정도 타당해 보인다. 하지만 'A가 B를 갖는다'로 해석할 수 있는 요구사항이 있다고 하더라도 이것이 반드시 A와 B가 한 애그리거트에 속한다는 것을 의미하는 것은 아니다.

좋은 예가 상품과 리뷰다. 상품 상세페이지에 들어가면 상품 상세 정보와 함께 리뷰 내용을 보여줘야할 때 상품 엔티티와 리뷰 엔티티가 한 애그리거트에 속한다고 생각할 수 있다. 그러나 상품과 리뷰는 함께 생성되지 않고, 함께 변경되지도 않는다. 게다가 상품을 변경하는 주체가 관리자라면, 리뷰를 생성하고 변경하는 주체는 고객이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/bfcc5de6-9653-48ee-9785-2cb24be5d657)

리뷰의 변경이 상품에 영향을 주지 않고, 반대로 상품의 변경이 리뷰에 영향을 주지 않기 때문에 이 둘은 다른 애그리거트라고 볼 수 있다. 처음에는 큰 애그리거트로 보이는 것들이 많지만, 도메인 규칙을 제대로 이해할 수록 애그리거트의 실제 크기는 줄어들고, 일반적으로 하나의 애그리거트는 하나의 엔티티만을 갖는다.

처음 도메인 모델을 만들기 시작하면 큰 애그리거트로 보이는 것들이 많지만, 도메인에 대한 경험이 생기고 도메인 규칙을 제대로 이해할수록 애그리거트의 실제 크기는 줄어든다. 그동안 경험을 비추어 보면 다수의 애그리거트가 한 개의 엔티티 객체만 갖는 경우가 많았으며 두 개 이상의 엔티티로 구성되는 애그리거트는 드물었다.

## 애그리거트 루트
주문 애그리거트는 다음을 포함한다.

- 총 금액인 `totalAmount`를 갖고 있는 `Order` 엔티티
- 개별 구매 상품의 개수인 `quantity`와 금액인 `price`를 갖고 있는 `OrderLine` 밸류

구매할 상품의 개수를 변경하면 한 `OrderLine`의 `quantity`를 변경하고 더불어 `Order`의 `totalAmount`도 변경해야 한다. 그렇지 않으면 다음 도메인 규칙을 어기고 데이터 일관성이 깨진다.

- 주문 총 금액은 개별 상품의 주문 개수 * 가격의 합

애그리거트는 여러 객체로 구성되기 때문에 한 객체만 상태가 정상이면 안된다. 도메인 규칙을 지키려면 애그리거트에 속한 모든 객체가 정상 상태를 가져야 한다. 애그리거트에 속한 모든 객체가 일관된 상태를 유지하려면 **애그리거트 전체를 관리할 주체**가 필요한데, 이 책임을 지는 것이 바로 **애그리거트 루트 엔티티**이다. 애그리거트 루트 엔티티는 애그리거트의 대표 엔티티로써, 애그리거트에 속한 객체는 애그리거트 루트 엔티티에 직접 또는 간접적으로 속하게 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2d501d01-c8c7-407b-afd1-c7363015e46a)

### 도메인 규칙과 일관성
- 애그리거트 루트의 핵심 역할은 애그리거트의 일관성이 깨지지 않도록 하는 것
- 애그리거트 루트는 애그리거트가 제공해야 할 도메인 기능을 구현한다.
- 애그리거트 외부에서 애그리거트에 속한 객체를 직접 변경하면 안된다.
- 단순히 필드를 변경하는 setter를 공개 범위로 만들지 않는다.
- 밸류 타입은 불변으로 구현한다.

### 애그리거트 루트의 기능 구현
- 애그리거트 루트는 애그리거트 내부의 다른 객체를 조합해서 기능을 완성한다.
- 예를 들어 `Order`는 총 주문 금액을 구하기 위해 `OrderLine` 목록을 사용한다.

### 트랜잭션 범위
트랜잭션 범위는 작을수록 좋다. 한 트랜잭션이 한 개 테이블을 수정하는 것과 세 개의 테이블을 수정하는 것을 비교하면 성능에서 차이가 발생한다. 한 개 테이블을 수정하면 트랜잭션 충돌을 막기 위해 잠그는 대상이 한 개의 테이블의 한 행으로 한정되지만, 세 개의 테이블을 수정하면 잠금 대상이 더 많아진다. 이는 전체적인 성능을 떨어뜨린다.

**한 트랜잭션에서는 한 개의 애그리거트만 수정해야 한다.** 이것은 애그리거트에서 다른 애그리거트를 변경하지 않는다는 것을 의미한다. 애그리거트 내부에서 다른 애그리거트의 상태를 변경하는 기능을 실행하면 안된다. 애그리거트는 최대한 서로 독립적이어야 하는데 한 애그리거트가 다른 애그리거트의 기능에 의존하기 시작하면 애그리거트 간 결합도가 높아진다. 그럴수록 향후 수정 비용이 증가하므로 애그리거트에서 다른 애그리거트의 상태를 변경하지 말아야 한다.

**부득이하게 한 트랜잭션으로 두 개 이상의 애그리거트를 수정해야 한다면 애그리거트에서 다른 애그리거트를 직접 수정하지 말고 응용 서비스에서 두 애그리거트를 수정하도록 구현한다.**

```java
public class ChangeOrderService {

  @Transactional
  public void changeShippingInfo(OrderId id, ShippingInfo newShippingInfo, boolean useNewShippingAddrAsMemberAddr) {
    Order order = orderRepository.findById(id);
    if (order == null) throw new OrderNotFoundException();
    order.shipTo(newShippingInfo);
    if (useNewShippingAddrAsMemberAddr) {
      Member member = findMember(order.getOrderer());
      member.changeAddress(newShippingInfo.getAddress());
    }
  }

  // ...
}
```

도메인 이벤트를 사용하면 한 트랜잭션에서 한 개의 애그리거트를 수정하면서도 동기나 비동기로 다른 애그리거트의 상태를 변경하는 코드를 작성할 수 있다.

다음 경우엔 한 트랜잭션에서 두 개 이상의 애그리거트를 변경하는 것을 고려할 수 있다.

- 팀 표준
- 기술 제약: 기술적으로 이벤트 방식을 도입할 수 없는 경우 한 트랜잭션에서 다수의 애그리거트를 수정해서 일관성을 처리해야 한다.
- UI 구현의 편리

## 리포지터리와 애그리거트
애그리거트는 개념상 완전한 한 개의 도메인 모델을 표현하므로 객체의 영속성을 처리하는 리포지터리는 애그리거트 단위로 존재한다. `Order`와 `OrderLine`을 물리적으로 각각 별도의 DB 테이블에 저장한다 해서 `Order`와 `OrderLine`을 위한 리포지터리를 각각 만들지 않는다. `Order`가 애그리거트 루트고 `OrderLine`은 애그리거트에 속하는 구성요소이므로 `Order`를 위한 리포지터리만 존재한다.

애그리거트는 개념적으로 하나이므로 리포지터리는 애그리거트 전체를 저장소에 영속화해야 한다. 동일하게 애그리거트를 구하는 리포지터리 메서드는 완전한 애그리거트를 제공해야 한다.

리포지터리가 완전한 애그리거트를 제공하지 않으면 필드나 값이 올바르지 않아 애그리거트의 기능을 실행하는 도중에 `NullPointerException`과 같은 문제가 발생할 수 있다.

저장소로 MariaDB나 Oracle과 같은 RDBMS 뿐만 아니라 MongoDB와 같은 NoSQL도 함께 사용하는 곳이 증가하고 있다. 애그리거트를 영속화할 저장소로 무엇을 사용하든지 간에 애그리거트의 상태가 변경되면 모든 변경을 원자적으로 저장소에 반영해야 한다. 애그리거트에서 두 개의 객체를 변경했는데 저장소에는 한 객체에 대한 변경만 반영되면 데이터 일관성이 깨지므로 문제가 된다.

## ID를 이용한 애그리거트 참조
한 객체가 다른 객체를 참조하는 것처럼 애그리거트도 다른 애그리거트를 참조한다. 애그리거트 관리 주체는 애그리거트 루트이므로 애그리거트에서 다른 애그리거트를 참조한다는 것은 다른 애그리거트의 루트를 참조한다는 것과 같다.

애그리거트 간의 참조는 필드를 통해 쉽게 구현할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9ceacabb-6bf2-4bb5-ac9b-33d3d2f80ad7)

JPA는 `@ManyToOne`, `@OneToOne`과 같은 애너테이션을 이용해서 연관된 객체를 로딩하는 기능을 제공하고 있으므로 필드를 이용해 다른 애그리거트를 쉽게 참조할 수 있다.

ORM 기술 덕에 애그리거트 루트에 대한 참조를 쉽게 구현할 수 있고 필드(또는 getter)를 이용한 애그리거트 참조를 사용하면 다른 애그리거트의 데이터를 쉽게 조회할 수 있다. 하지만 필드를 이용한 애그리거트 참조는 다음 문제를 야기할 수 있다.

- 편한 탐색 오용
- 성능에 대한 고민
- 확장 어려움

한 애그리거트 내부에서 다른 애그리거트 객체에 접근할 수 있으면 다른 애그리거트의 상태를 쉽게 변경할 수 있게 된다. 트랜잭션 범위에서 언급한 것처럼 한 애그리거트가 관리하는 범위는 자기 자신으로 한정해야 한다. 그런데 애그리거트 내부에서 다른 애그리거트 객체에 접근할 수 있으면 구현의 편리함 때문에 다른 애그리거트를 수정하고자 하는 유혹에 빠지기 쉽다.

```java
public class Order {
  private Orderer orderer;

  public void changeShippingInfo(ShippingInfo newShippingInfo,
          boolean useNewShippingAddrAsMemberAddr) {
    // ...
    if (useNewShippingAddrAsMemberAddr) {
      // 한 애그리거트 내부에서 다른 애그리거트에 접근할 수 있으면,
      // 구현이 쉬워진다는 것 때문에 다른 애그리거트의 상태를 변경하는
      // 유혹에 빠지기 쉽다.
      orderer.getMember().changeAddress(newShippingInfo.getAddress());
    }
  }
  // ...
}
```

애그리거트를 직접 참조하면 성능과 관련된 여러가지 고민을 해야 한다. JPA를 사용하면 참조한 객체를 지연 로딩과 즉시 로딩의 두 가지 방식에 대한 고민이 필요하다. 또한 서비스가 커지면 도메인을 분리하기 위해 시스템을 분리하기 시작하면서 더 이상 다른 애그리거트 루트를 참조하기 위해 JPA와 같은 단일 기술을 사용할 수 없다.

이런 세 가지 문제점을 완화시키기 위해 ID를 이용해 다른 애그리거트를 참조한다. DB 테이블에서 외래키로 참조하는 것과 비슷하게 ID를 이용한 참조는 다른 애그리거트를 참조할 때 ID를 사용한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f97fdbda-7f8e-48e2-b0cf-d5951a008216)

ID 참조를 사용하면 모든 객체가 참조로 연결되지 않고 한 애그리거트에 속한 객체들만 참조로 연결된다. 이는 애그리거트의 경계를 명확히 하고 애그리거트 간 물리적인 연결을 제거하기 때문에 모델의 복잡도를 낮춰준다. 또한 애그리거트 간의 의존을 제거하므로 응집도를 높여주는 효과도 있다.

구현 복잡도도 낮아진다. 다른 애그리거트를 직접 참조하지 않으므로 애그리거트 간 참조를 지연 로딩으로 할지 즉시 로딩으로 할지 고민하지 않아도 된다. 참조하는 애그리거트가 필요하면 응용 서비스에서 ID를 이용해서 로딩하면 된다.

```java
public class ChangeOrderService {

  @Transactional
  public void changeShippingInfo(OrderId id, ShippingInfo newShippingInfo, boolean useNewShippingAddrAsMemberAddr) {
    Order order = orderRepository.findById(id);
    if (order == null) throw new OrderNotFoundException();
    order.changeShippingInfo(newShippingInfo);
    if (useNewShippingAsMemberAddr) {
      // ID를 이용해서 참조하는 애그리거트를 구한다.
      Member member = memberRepository.findById(
        order.getOrderer().getMemberId();
      );
      member.changeAddress(newShippingInfo.getAddress());
    }
  }

  // ...
}
```

응용 서비스에서 필요한 애그리거트를 로딩하므로 애그리거트 수준에서 지연 로딩을 하는 것과 동일한 결과를 만든다.

ID를 이용한 참조 방식을 사용하면 복잡도를 낮추는 것과 함께 한 애그리거트에서 다른 에그리거트를 수정하는 문제를 근원적으로 방지할 수 있다. 외부 애그리거트를 직접 참조하지 않기 때문에 애초에 한 애그리거트에서 다른 애그리거트의 상태를 변경할 수 없는 것이다.

애그리거트별로 다른 구현 기술을 사용하는 것도 가능해진다. 중요한 데이터인 주문 애그리거트는 RDBMS에 저장하고 조회 성능이 중요한 상품 애그리거트는 NoSQL에 저장할 수 있다. 또한 각 도메인을 별도 프로세스로 서비스하도록 구현할 수도 있다.

### ID를 이용한 참조와 조회 성능
다른 애그리거트를 ID로 참조하면 참조하는 여러 애그리거트를 읽을 때 조회 속도가 문제될 수 있다.

주문 목록을 보여주기위해 상품 애그리거트와 회원 애그리거트를 함께 읽어야 하는데, 이를 처리할 때 각 주문마다 상품과 회원 애그리거트를 읽어온다면, 한 DBMS에 데이터가 있다면 조인을 이용해서 한 번에 모든 데이터를 가져올 수 있음에도 불구하고 주문마다 상품 정보를 읽어오는 쿼리를 실행하게 된다.

```java
Member member = memberRepository.findById(ordererId);
List<Order> orders = orderRepository.findByOrderer(ordererId);
List<OrderView> dtos = orders.stream()
            .map(order -> {
              ProductId prodId = order.getOrderLines().get(0).getProductId();
              // 각 주문마다 첫 번째 주문 상품 정보 로딩을 위한 쿼리 실행
              Product product = productRepository.findById(prodId);
              return new OrderView(order, member, product);
            }).collect(toList());
```

주문 개수가 10개면 주문을 읽어오기 위한 1번의 쿼리와 주문별로 각 상품을 읽어오기 위한 10번의 쿼리를 실행한다. '조회 대상이 N개일 때 N개를 읽어오는 한 번의 쿼리와 연관된 데이터를 읽어오는 쿼리를 N번 실행한다'해서 이를 **N+1 조회 문제**라 부른다. ID를 이용한 애그리거트 참조는 지연 로딩과 같은 효과를 만드는 데 지연 로딩과 관련된 대표적인 문제가 N+1 조회 문제다.

이는 더 많은 쿼리를 실행하기에 전체 조회 속도가 느려지는 원인이 된다. **이 문제가 발생하지 않도록 하려면 조인을 사용해야 한다.** ID 참조 방식을 객체 참조 방식으로 바꾸고 즉시 로딩을 사용하도록 매핑 설정을 바꾸는게 가장 쉬운 방법인데, 이는 애그리거트 간 참조를 ID 참조 방식에서 객체 참조 방식으로 다시 되돌리는 것이다.

조회 전용 쿼리를 만들면 이 문제를 해결할 수 있다. 데이터 조회를 위한 별도 DAO를 만들고 DAO의 조회 메서드에서 조인을 이용해 한 번의 쿼리로 필요한 데이터를 로딩하면 된다.

```java
@Repository
public class JpaOrderViewDao implements OrderViewDao {
  @PersistenceContext
  private EntityManager em;

  @Override
  public List<OrderView> selectByOrderer(String ordererId) {
    String selectQuery = 
            "select new com.myshop.order.application.dto.OrderView(o, m, p) " +
            "from Order o join o.orderLines ol, Member m, Product p " +
            "where o.orderer.memberId.id = :ordererId " +
            "and o.orderer.memberId = m.id " +
            "and index(ol) = 0 " +
            "and ol.productId = p.id " +
            "order by o.number.number desc";
    TypedQuery<OrderView> query = 
        em.createQuery(selectQuery, OrderView.class);
    query.setParameter("ordererId", ordererId);
    return query.getResultList();
  }
}
```

쿼리가 복잡하거나 SQL에 특화된 기능을 사용해야 한다면 조회를 위한 부분만 마이바티스와 같은 기술을 이용해서 구현할 수도 있다.

애그리거트마다 서로 다른 저장소를 사용하면 한 번의 쿼리로 관련 애그리거트를 조회할 수 없다. 이때는 조회 성능을 높이기 위해 캐시를 적용하거나 조회 전용 저장소를 따로 구성한다. 이 방법은 코드가 복잡해지는 단점이 있지만 시스템의 처리량을 높일 수 있다는 장점이 있다. 특히 한 대의 DB 장비로 대응할 수 없는 수준의 트래픽이 발생하는 경우 캐시나 조회 전용 저장소는 필수로 선택해야 하는 기법이다.

## 애그리거트 간 집합 연관
애그리거트 간 1-N과 M-N 연관에 대해 살펴보자. 이 두 연관은 컬렉션을 이용한 연관이다. 카테고리와 상품 간의 연관이 대표적이다.