# Chapter 5. 스프링 데이터 JPA를 이용한 조회 기능
참고로, 조회 모델을 구현할 때 JPA를 사용할 때도 있고 마이바티스를 사용할 때도 있고 `JdbcTemplate`을 사용할 때도 있으니 모든 DB 연동 코드를 JPA만 사용해서 구현해야 한다 생각하진 말자.

## 검색을 위한 스펙
검색 조건이 고정되어 있고 단순하면 다음과 같이 특정 조건으로 조회하는 기능을 만들면 된다.

```java
public interface OrderDataDao {
  Optional<OrderData> findById(OrderNo id);
  List<OrderData> findByOrderer(String ordererId, Date fromDate, Date toDate);
  // ...
}
```

그런데 목록 조회와 같은 기능은 다양한 검색 조건을 조합해야 할 때가 있다. 필요한 조합마다 `find` 메서드를 정의할 수도 있지만 이것은 좋은 방법은 아니다.

검색 조건을 다양하게 조합해야 할 때 사용할 수 있는 것이 **스펙**이다. 이는 **애그리거트가 특정 조건을 충족하는지를 검사할 때 사용하는 인터페이스다.**

```java
public interface Specification<T> {
  public boolean isSatisfiedBy(T agg);
}
```

`agg` 파라미터는 검사 대상이 되는 객체이며 리포지터리에서 사용하면 애그리거트 루트가 되고, 스펙을 DAO에 사용하면 `agg`는 검색 결과로 리턴할 데이터 객체가 된다.

```java
public class OrdererSpec implements Specification<Order> {
  private String ordererId;

  public OrdererSpec(String ordererId) {
    this.ordererId = ordererId;
  }

  // 특정 고객의 주문인지 확인하는 스펙
  public boolean isSatisfiedBy(Order agg) {
    return agg.getOrdererId().getMemberId().getId().equals(ordererId);
  }
}
```

리포지터리나 DAO는 검색 대상을 걸러내는 용도로 스펙을 사용한다. 만약 리포지터리가 메모리에 모든 애그리거트를 보관하고 있다면 다음과 같이 스펙을 사용할 수 있다.

```java
public class MemoryOrderRepository implements OrderRepository {

  public List<Order> findAll(Specification<Order> spec) {
    List<Order> allOrders = findAll();
    return allOrders.stream()
                    .filter(order -> spec.isSatisfiedBy(order))
                    .toList();
  }

  // ...
}
```

리포지터리가 스펙을 이용해서 검색 대상을 걸러주므로 특정 조건을 충족하는 애그리거트를 찾고 싶으면 원하는 스펙을 생성해서 리포지터리에 전달해주기만 하면 된다.

```java
// 검색 조건을 표현하는 스펙을 생성해서
Specification<Order> ordererSpec = new OrdererSpec("Ramos");
// 리포지터리에 전달
List<Order> orders = orderRepository.findAll(ordererSpec);
```

하지만 실제 스펙은 이렇게 구현하지 않는다. **모든 애그리거트 객체를 메모리에 보관하기도 어렵고 설사 다 보관하더라도 조회 성능에 심각한 문제가 발생하기 때문이다.** 실제 스펙은 사용하는 기술에 맞춰 구현하게 된다.

## 스프링 데이터 JPA를 이용한 스펙 구현
Spring Data JPA는 검색 조건을 표현하기 위한 인터페이스인 `Specification`을 제공하며 다음과 같이 정의되어 있다.

```java
package org.springframework.data.jpa.domain;

import java.io.Serializable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.lang.Nullable;

public interface Specification<T> extends Serializable {
  // not, where, and, or 메서드 생략

  @Nullable
  Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
```

- 스펙 인터페이스에서 지네릭 타입 파라미터 `T`는 JPA 엔티티 타입을 의미
- `toPredicate()`는 JPA Criteria API에서 조건을 표현하는 `Predicate`를 생성

```java
public class OrdererIdSpec implements Specification<OrderSummary> {

  private String ordererId;

  public OrdererIdSpec(String ordererId) {
    this.ordererId = ordererId;
  }

  @Override
  public Predicate toPredicate(Root<OrderSummary> root,
                                CriteriaQuery<?> query,
                                CriteriaBuilder cb) {
    // OrderSummary_ 클래스는 JPA 정적 메타 모델을 정의한 코드다.
    return cb.equal(root.get(OrderSummary_.ordererId), ordererId);
  }
}
```

실무에선 QueryDSL을 사용하므로 해당 내용은 생략한다.

## 리포지터리/DAO에서 스펙 사용하기
스펙을 충족하는 엔티티를 검색하고 싶다면 `findAll()` 메서드를 사용하면 된다.

```java
public interface OrderSummaryDao extends Repository<OrderSummary, String> {
  List<OrderSummary> findAll(Specification<OrderSummary> spec);
}
```

```java
// 스펙 객체를 생성하고
Specification<OrderSummary> spec = new OrdererIdSpec("user1");
// findAll()을 이용해 검색
List<OrderSummary> results = orderSummaryDao.findAll(spec);
```

## 스펙 조합
Spring Data JPA가 제공하는 스펙 인터페이스는 스펙을 조합할 수 있는 메서드를 제공한다. 추가로 `not`, `where` 등의 메서드도 제공한다.

```java
public interface Specification<T> extends Serializable {

  static <T> Specification<T> not(@Nullable Specification<T> spec) { ... }
  static <T> Specification<T> where(@Nullable Specification<T> spec) { ... }
  default Specification<T> and(@Nullable Specification<T> other) { ... }
  default Specification<T> or(@Nullable Specification<T> other) { ... }
  
  @Nullable
  Predicate toPredicate(Root<T> root, CriteriaQuery query, CriteriaBuilder cb);
  
}
```

## 정렬 지정하기
Spring Data JPA는 두 가지 방법을 사용해 정렬을 지정할 수 있다.

- 메서드 이름에 `OrderBy`를 사용해서 정렬 기준 지정
- `Sort`를 인자로 전달

```java
public interface OrderSummaryDao extends Repository<OrderSummary, String> {

    List<OrderSummary> findByOrdererId(String ordererId, Sort sort);
    List<OrderSummary> findAll(Specification<OrderSummary> spec, Sort sort);
}
```

```java
Sort sort = Sort.by("number").ascending().and(Sort.by("orderDate").descending());
```

## 페이징 처리하기
Spring Data JPA는 페이징 처리를 위해 `Pageable` 타입을 이용한다. `Sort` 타입과 마찬가지로 `find` 메서드에 타입 파라미터를 사용하면 페이징을 자동으로 처리해준다.

```java
public interface MemberDataDao extends Repository<MemberData, String> {

  List<MemberData> findByNameLike(String name, Pageable pageable);
}
```

```java
Pageable pageable = PageRequest.of(1, 10, Sort);
List<MemberData> user = memberDataDao.findByNameLike("사용자%", pageReq);
```

`Pageable`을 사용하는 메서드의 리턴 타입이 `Page`일 경우 Spring Data JPA는 목록 조회 쿼리와 함께 `COUNT` 쿼리도 실행해서 조건에 해당하는 데이터 개수를 구한다.

```java
page.getContent(); // 조회 결과 목록
page.getTotalElements(); // 조건에 해당하는 전체 개수
page.getTotalPages(); // 전체 페이지 번호
page.getNumber(); // 현재 페이지 번호
page.getNumberOfElements(); // 조회 결과 개수
page.getSize(); // 페이지 크기
```

- 프로퍼티를 비교하는 `findBy` 프로퍼티 형식의 메서드는 `Pageable` 타입을 사용하더라도 리턴 타입이 `List`면 `COUNT` 쿼리를 실행하지 않는다.
  - 따라서 페이징 처리와 관련된 정보가 필요없다면 `Page`가 아닌 `List`로 리턴 타입을 설정하여 불필요한 `COUNT` 쿼리를 실행시키지 않도록 한다.

```java
Page<MemberData> findByBlocked(boolean blocked, Pageable pageable);
List<MemberData> findByNameLike(String name, Pageable pageable);
```

- 반면 스펙을 사용하는 `findAll` 메서드에 `Pageable` 타입을 사용하면 리턴 타입이 `Page`가 아니더라도 `COUNT` 쿼리를 사용한다.
  - 스펙을 사용하고 페이지 처리를 하면서 `COUNT` 쿼리를 실행하고 싶지 않다면 스프링 데이터 JPA의 커스텀 레포지토리 기능을 이용해서 직접 구현해야 한다.

```java
List<MemberData> findAll(Specification<MemberData> spec, Pageable pageable);
```

- 처음부터 N개의 데이터가 필요하다면 `Pageable` 대신 `findFirstN` 형식의 메서드를 사용할 수도 있다.

```java
List<MemberData> findFirst3ByNameLikeOrderByName(String name);
```

- `First` 대신 `Top`을 사용해도 된다.
- 뒤에 숫자가 없으면 한 개 결과만 리턴한다.

```java
MemberData findFirstByBlockedOrderById(boolean blocked);
```

## 스펙 조합을 위한 스펙 빌더 클래스
스펙을 사용하다보면 조건에 따라 스펙을 조합해야 할 때가 있다. 이때 스펙 빌더를 사용해 작성할 수 있다.

```java
Specification<MemberData> spec = SpecBuilder.builder(MemberData.class)
    .ifTrue(searchRequest.isOnlyNotBlocked(), () -> MemberDataSpecs.nonBlocked())
    .ifHasText(searchRequest.getName(), name -> MemberDataSpecs.nameLike(searchRequest.getName()))
    .toSpec();

List<MemberData> result = memberDataDao.findAll(spec, PageRequest.of(0, 5));
```

## 동적 인스턴스 생성
JPA는 쿼리 결과에서 임의의 객체를 동적으로 생성할 수 있는 기능을 제공하고 있다.  
(Projection을 의미한다.)

```java
public interface OrderSummaryDao extends Repository<OrderSummary, String> {

  @Query("""
        select new com.myshop.order.query.dto.OrderView(
            o.number, o.state, m.name, m.id, p.name
        )
        from Order o join o.orderLines ol, Member m, Product p
        where o.orderer.memberId.id = :ordererId
        and o.orderer.memberId.id = m.id
        and index(ol) = 0
        and ol.productId.id = p.id
        order by o.number.number desc
        """)
  List<OrderView> findOrderView(String ordererId);
}
```

`new` 키워드 뒤에 생성할 인스턴스의 완전한 클래스 이름을 지정하고 괄호 안에 생성자에 인자로 전달할 값을 지정한다.

```java
public class OrderView {

  private final String number;
  private final OrderState state;
  private final String memberName;
  private final String memberId;
  private final String productName;

  public OrderView(OrderNo number, OrderState state, String memberName, MemberId memberId, String productName) {
      this.number = number.getNumber();
      this.state = state;
      this.memberName = memberName;
      this.memberId = memberId.getId();
      this.productName = productName;
  }
  
  // ...getter 생략
}
```

- 조회 전용 모델을 만드는 이유는 표현 영역을 통해 사용자에게 데이터를 보여주기 위함이다.
- 많은 웹 프레임워크는 새로 추가한 밸류 타입을 알맞은 형식으로 출력하지 못하므로 위 코드처럼 값을 기본 타입으로 변환하면 편리하다.
- 동적 인스턴스의 장점은 JPQL을 그대로 사용하므로 객체 기준으로 쿼리를 작성하면서도 동시에 지연/즉시 로딩과 같은 고민 없이 원하는 모습으로 데이터를 조회할 수 있다는 점이다.

## 하이버네이트 @Subselect 사용
`@Subselect`는 쿼리 결과를 `@Entity`로 매핑할 수 있는 기능이다.

```java
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Immutable
@Subselect(
      """
      select o.order_number as number,
      o.version,
      o.orderer_id,
      o.orderer_name,
      o.total_amounts,
      o.receiver_name,
      o.state,
      o.order_date,
      p.product_id,
      p.name as product_name
      from purchase_order o inner join order_line ol
          on o.order_number = ol.order_number
          cross join product p
      where
      ol.line_idx = 0
      and ol.product_id = p.product_id"""
)
@Synchronize({"purchase_order", "order_line", "product"})
public class OrderSummary {
  // 필드 생략..

  protected OrderSummary() {
  }
}
```

- `@Immutable`, `@Subselect`, `@Synchronize`는 하이버네이트 전용 애너테이션인데 이 태그를 사용하면 테이블이 아닌 쿼리 결과를 `@Entity`로 매핑할 수 있다.
- `@Subselect`는 조회 쿼리를 값으로 갖는다.
  - 하이버네이트는 이 쿼리의 결과를 매핑할 테이블처럼 사용한다.
  - DBMS가 여러 테이블을 조인해서 조회한 결과를 한 테이블처럼 보여주기 위한 용도로 뷰를 사용하는 것처럼 `@Subselect`를 사용하면 쿼리 실행 결과를 매핑할 테이블처럼 사용한다.
- 뷰를 수정할 수 없듯이 `@Subselect`로 조회한 `@Entity` 역시 수정할 수 없다.
  - 실수로 `@Subselect`를 이용한 `@Entity`의 매핑 필드를 수정하면 하이버네이트는 `update` 쿼리를 실행시키지만, 매핑한 테이블이 실제 DB에는 없으므로 에러가 발생한다.
  - 이 문제를 발생하기 위해 `@Immutable`을 사용한다.
  - `@Immutable`을 사용하면 하이버네이트는 해당 엔티티의 매핑 필드/프로퍼티가 변경되도 DB에 반영하지 않고 무시한다.
- 특별한 이유가 없으면 하이버네이트는 트랜잭션을 커밋하는 시점에 변경사항을 DB에 반영한다.
  - 아래 코드의 `OrderSummary`에는 최신 값이 아닌 이전 값이 담기게 된다.

```java
// purchase_order 테이블에서 조회
Order order = orderRepository.findById(orderNumber);
order.changeShippingInfo(newInfo); // 상태 변경

// 변경 내역이 DB에 반영되지 않았는데 purchase_order 테이블에서 조회
List<OrderSummary> summaries = orderSummaryRepository.findByOrderId(userId);
```

- 이런 문제를 해소하기 위한 용도로 사용한 것이 `@Synchronize`다.
  - `@Synchronize`는 해당 엔티티와 관련된 테이블 목록을 명시한다.
- 하이버네이트는 엔티티를 로딩하기 전에 지정한 테이블과 관련된 변경이 발생하면 플러시(flush)를 먼저 한다.
  - `OrderSummary`를 로딩하기 전에 `purchase_order` 테이블에 변경이 발생하면 관련 내역을 먼저 플러시한다.
  - 따라서 `OrderSummary`를 로딩하는 시점에서는 변경 내역이 반영된다.
- `@Subselect`를 사용해도 일반 `@Entity`와 같기 때문에 `EntityManager#find()`, JPQL, Criteria를 사용해서 조회할 수 있다는 것이 `@Subselect`의 장점이다.

```java
// @Subselect를 적용한 @Entity는 일반 @Entity와 동일한 방법으로 조회할 수 있다
// 스펙도 사용 가능하다
Specification<OrderSummary> spec = OrderSummarySpecs.orderDateBetween(from, to);
Pageable pageable = PageRequest.of(1, 1);
List<OrderSummary> results = orderSummaryDao.findAll(spec, pageable);
```

- `@Subselect`는 이름처럼 `@Subselect`의 값으로 지정한 쿼리를 `from` 절의 서브 쿼리로 사용한다.
- `@Subselect`를 사용할 때는 쿼리가 이러한 형태를 갖는다는 점을 유념해야 한다.
- 서브 쿼리를 사용하고 싶지 않다면 네이티브 SQL 쿼리를 사용하거나 마이바티스와 같은 별도 매퍼를 사용해서 조회 기능을 구현해야 한다.