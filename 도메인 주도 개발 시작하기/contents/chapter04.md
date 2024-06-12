# Chapter 4. 리포지터리와 모델 구현
## JPA를 이용한 리포지터리 구현
### 모듈 위치
리포지터리 인터페이스는 애그리거트와 같이 도메인 영역에 속하고, 리포지터리를 구현한 클래스는 인프라스트럭처 영역에 속한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1d40d49e-bb16-4cf8-8a80-aecb72748130)

팀 표준에 따라 리포지터리 구현 클래스를 `domain.impl`과 같은 패미지에 위치시킬 수도 있는데 이는 리포지터리 인터페이스와 구현체를 분리하기 위한 타협안 같은 것이지 좋은 설계 원칙을 따르는 것은 아니다. **가능하면 리포지터리 구현 클래스를 인프라스트럭처 영역에 위치시켜 인프라스트럭처에 대한 의존을 낮춰야 한다.**

### 리포지터리 기본 기능 구현
- ID로 애그리거트 조회
- 애그리거트 저장

```java
public interface OrderRepository {
  Order findById(OrderNo no);
  void save(Order order);
}
```

**인터페이스는 애그리거트 루트 기준으로 작성한다.**
- 주문 애그리거트는 `Order` 루트 엔티티를 비롯해 `OrderLine`, `Orderer`, `ShippingInfo` 등 다양한 객체를 포함
- 루트 엔티티를 기준으로 리포지터리 인터페이스를 작성한다.

이 인터페이스를 구현한 클래스는 JPA의 `EntityManager`를 이용해서 기능을 구현한다.

```java
package shop.order.infra;

import org.springframework.stereotype.Repository;
import shop.order.domain.Order;
import shop.order.domain.OrderNo;
import shop.order.domain.OrderRepository;

// 생략

@Repository
public class JpaOrderRepository implements OrderRepository {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Order findById(OrderNo id) {
      return entityManager.find(Order.class, id);
  }

  @Override
  public void save(Order order) {
      entityManager.persist(order);
  }
}
```

참고로 Spring + JPA 사용 시 Spring Data JPA를 사용하기 때문에, 실제론 위와 같이 리포지터리 인터페이스를 구현한 클래스를 직접 작성할 일은 거의 없다.

애그리거트를 수정한 결과를 저장소에 반영하는 메서드를 추가할 필요는 없다. JPA를 사용하면 **트랜잭션 범위에서 변경한 데이터를 자동으로 DB에 반영하기 때문이다.**

```java
public class ChangeOrderService {
  @Transactional
  public void changeShippingInfo(OrderNo no, ShippingInfo newShippingInfo) {
      Optional<Order> orderOpt = orderRepository.findById(no);
      Order order = orderOpt.orElseThrow(() -> new OrderNotFoundException());
      order.changeShippingInfo(newShippingInfo);
  }

  // ...
}
```

위 메서드는 스프링 프레임워크의 트랜잭션 관리 기능을 통해 트랜잭션 범위에서 실행된다. 메서드 실행이 끝나면 트랜잭션을 커밋하는데 이때 JPA는 트랜잭션 범위에서 변경된 객체의 데이터를 DB에 반영하기 위해 `UPDATE` 쿼리를 실행한다.

애그리거트를 삭제하는 기능이 필요할 수도 있다. 삭제 기능을 위한 메서드는 다음과 같이 삭제할 애그리거트 객체를 파라미터로 전달받는다.

```java
public interface OrderRepository {
  // ...
  public void delete(Order order);
}

public class JpaOrderRepository implements OrderRepository {
  @PersistenceContext
  private EntityManager entityManager;

  // ...

  @Override
  public void delete(Order order) {
      entityManager.remove(order);
  }
}
```

삭제 요구사항이 있더라도 데이터를 실제로 삭제하는 경우는 많지 않다. 관리자 기능에서 삭제한 데이터까지 조회해야 하는 경우도 있고 데이터 원복을 위해 일정 기간 동안 보관해야 할 때도 있기 때문이다. 이런 이유로 사용자가 삭제 기능을 실행할 때 데이터를 바로 삭제하기보단 삭제 플래그를 사용해 데이터를 화면에 보여줄지 여부를 결정하는 방식으로 구현한다.

## 스프링 데이터 JPA를 이용한 리포지터리 구현
Spring Data JPA는 지정한 규칙에 맞게 리포지터리 인터페이스를 정의하면 리포지터리를 구현한 객체를 알아서 만들어 스프링 빈으로 등록해준다.

다음 규칙에 따라 작성한 인터페이스를 찾아 인터페이스를 구현한 스프링 빈 객체를 자동으로 등록한다.

- `org.springframework.data.repository.Repository<T, ID>` 인터페이스 상속
- `T`는 엔티티 타입을 지정하고, `ID`는 식별자 타입을 지정

```java
@Entity
@Table(name = "purchase_order")
@Access(AccessType.FIELD)
public class Order {
  @EmbeddedId
  private OrderNo number; // OrderNo가 식별자 타입

  // ...
}

public interface OrderRepository extends Repository<Order, OrderNo> {
  Optional<Order> findById(OrderNo id);
  void save(Order order);
}
```

Spring Data JPA는 `OrderRepository`를 리포지터리로 인식해 알맞게 구현한 객체를 스프링 빈으로 등록한다.

```java
@Service
public class CancelOrderService {
  private OrderRepository orderRepository;

  public CancelOrderService(OrderRepository orderRepository, ...(생략)) {
      this.orderRepository = orderRepository;
      // ...
  }

  @Transactional
  public void cancel(OrderNo orderNo, Canceller canceller) {
      Order order = orderRepository.findById(orderNo)
                      .orElseThrow(() -> new NoOrderException());
      if (!cancelPolicy.hasCancellationPermission(order, canceller)) {
          throw new NoCancellablePermission();
      }
      order.cancel();
  }
}
```

## 매핑 구현
### 엔티티와 밸류 기본 매핑 구현
애그리거트와 JPA 매핑을 위한 기본 규칙은 다음과 같다.

- 애그리거트 루트는 엔티티이므로 `@Entity`로 매핑 설정한다.

한 테이블에 엔티티와 밸류 데이터가 있다면

- 밸류는 `@Embeddable`로 매핑 설정한다.
- 밸류 타입 프로퍼티는 `@Embedded`로 매핑 설정한다.

주문 애그리거트의 루트 엔티티는 `Order`이고 이 애그리거트에 속한 `Orderer`와 `ShippingInfo`는 밸류이다. 이 세 객체와 `ShippingInfo`에 포함된 `Address` 객체와 `Receiver` 객체는 한 테이블에 매핑할 수 있다. 루트 엔티티와 루트 엔티티에 속한 밸류는 한 테이블에 매핑할 때가 많다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/996720ff-6178-433b-9869-bbe214092ff4)

```java
@Entity
@Table(name = "purchase_order")
public class Order {
	// ...

	@Embedded
	private Orderer orderer;

	@Embedded
	private ShippingInfo shippingInfo;

  // ...
}

@Embeddable
public class Orderer {
	// MemberId에 정의된 칼럼 이름을 변경하기 위해
	// @AttributeOverride 애노테이션 사용
	@Embedded
	@AttributeOverrides(
		@AttributeOverride(name = "id", column = @Column(name = "orderer_id"))
	)
	private MemberId memberId;

	@Column(name = "orderer_name")
	private String name;

	// ...
}

@Embaddable
public class MemberId implements Serializable {
	@Column(name = "member_id")
	private String id;

	// ...
}
```

- `Orderer`의 `memberId` 프로퍼티와 매핑되는 칼럼 이름은 `'orderer_id'`이므로 `MemberId`에 설정된 `'member_id'`와 이름이 다르다.
- `@Embeddable` 타입에 설정한 칼럼 이름과 실제 칼럼 이름이 다르므로 `@AttributeOverrides` 애너테이션을 이용해 `Orderer`의 `memberId` 프로퍼티와 매핑할 칼럼 이름을 변경했다.

```java
@Embeddable
public class ShippingInfo {
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "zipCode", column = @Column(name = "shipping_zipcode")),
    @AttributeOverride(name = "address1", column = @Column(name = "shipping_addr1")),
    @AttributeOverride(name = "address2", column = @Column(name = "shipping_addr2"))
  })
  private Address address;

  @Column(name = "shipping_message")
  private String message;

  @Embedded
  private Receiver receiver;
}
```

### 기본 생성자
밸류 타입의 경우 불변이므로 생성 시점에 필요한 값을 모두 전달받으므로 값을 변경하는 `set` 메서드는 제공하지 않는다. 하지만 JPA의 `@Entity`와 `@Embeddable`로 클래스를 매핑하려면 기본 생성자를 제공해야 한다. 하이버네이트와 같은 JPA 프로바이더는 DB에서 데이터를 읽어와 매핑된 객체를 생성할 때 기본 생성자를 사용해서 객체를 생성한다. 이런 이유로 다른 코드에서 기본 생성자를 사용하지 못하도록 `protected`로 선언한다.

`protected`로 선언한 이유는 하이버네이트는 클래스를 상속한 프록시 객체를 이용해서 지연 로딩을 구현하기 때문이다.

```java
@Embeddable
public class Receiver {
  @Column(name = "receiver_name")
  private String name;
  @Column(name = "receiver_phone")
  private String phone;

  protected Receiver() {} // JPA를 적용하기 위해 기본 생성자 추가

  public Receiver(String name, String phone) {
      this.name = name;
      this.phone = phone;
  }

  // ... getter 생략
}
```

### 필드 접근 방식 사용
엔티티에 프로퍼티를 위한 공개 get/set 메서드를 추가하면 도메인의 의도가 사라지고 객체가 아닌 데이터 기반으로 엔티티를 구현할 가능성이 높아진다. **특히 set 메서드는 내부 데이터를 외부에서 변경할 수 있는 수단이 되기 때문에 캡슐화를 깨는 원인이 될 수 있다. 엔티티가 객체로서 제 역할을 하려면 외부에 set 메서드 대신 의도가 잘 드러나는 기능을 제공해야 한다.**

엔티티를 객체가 제공할 기능 중심으로 구현하도록 유도하려면 JPA 매핑 처리를 프로퍼티 방식이 아닌 필드 방식으로 선택해서 불필요한 get/set 메서드를 구현하지 말아야 한다.

```java
@Entity
@Access(AccessType.FIELD)
public class Order {

  @EmbeddedId
  private OrderNo number;

  @Column(name = "state")
  @Enumerated(EnumType.STRING)
  private OrderState state;

  // ... cancel(), changeShippingInfo() 등 도메인 기능 구현
  // 필요한 getter 제공
}
```

JPA 구현체인 하이버네이트는 `@Access`를 이용해 명시적으로 접근 방식을 지정하지 않으면 `@Id`나 `@EmbeddedId`가 어디에 위치했느냐에 따라 접근 방식을 결정한다. `@Id`나 `@EmbeddedId`가 필드에 위치하면 필드 접근 방식을 선택하고 get 메서드에 위치하면 메서드 접근 방식을 선택한다.

### AttributeConverter를 이용한 밸류 매핑 처리
`int`, `long`, `String`, `LocalDate`와 같은 타입은 DB 테이블의 한 개 컬럼에 매핑된다. 이와 비슷하게 밸류 타입의 프로퍼티를 한 개 컬럼에 매핑해야 할 때도 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0b0d9ad8-1702-49b1-b59f-da3307d23621)

두 개 이상의 프로퍼티를 가진 밸류 타입을 한 개 컬럼에 매핑하려면 `@Embeddable`로는 처리할 수 없다. 이럴 때 사용할 수 있는 것이 `AttributeConverter`이다.

```java
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Money money) {
		return money == null ? null : money.getValue();
	}

	@Override
	public Money convertToEntityAttribute(Integer value) {
		return value == null? null : new Money(value);
	}
}
```

`@Converter` 애노테이션의 `autoApply` 속성값을 `true`로 지정했는데 이 경우 모델에 출현하는 모든 `Money` 타입의 프로퍼티에 대해 `MoneyConverter`를 자동으로 적용한다. `@Converter`의 `autoApply` 속성이 `false`인 경우 프로퍼티값을 변환할 때 사용할 컨버터를 직접 지정할 수 있다.

```java
@Entity
@Table(name = "purchase_order")
public class Order {
  // ...

  @Column(name = "total_amounts")
  private Money totalAmounts; // MoneyConverter를 적용해서 값 변환
}

public class Order {

	@Column(name = "total_amounts")
	@Convert(converter = MoneyConverter.class)
	private Money totalAmounts;
	// ...
}
```

### 밸류 컬렉션: 별도 테이블 매핑
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/17ddb63f-8b67-4128-b46f-e97dad70959c)

밸류 컬렉션을 별도 테이블로 매핑할 때는 `@ElementCollection`과 `@CollectionTable`을 함께 사용한다.

```java
@Entity
@Table(name = "purchase_order")
public class Order {
	// ...
	@ElementCollection
	@CollectionTable(name = "order_line", joinColumns = @JoinColumn(name = "order_number"))
	@OrderColumn(name = "line_idx") // List 자체가 인덱스를 갖고 있으므로 별도의 인덱스 프로퍼티 x
	private List<OrderLine> orderLines;
}

@Embeddable
public class OrderLine {
	@Embedded
	private ProductId productId;
	// ...
}
```

`@CollectionTable`은 밸류를 저장할 테이블을 지정할 때 사용한다. `name` 속성으로 테이블 이름을 지정하고 `joinColumns` 속성은 외부키로 사용하는 컬럼을 지정한다.

### 밸류 컬렉션: 한 개 칼럼 매핑
밸류 컬렉션을 별도 테이블이 아닌 한 개 칼럼에 저장해야 할 때가 있다. 예를 들어, 도메인 모델에는 이메일 주소 목록을 `Set`으로 보관하고 DB에는 한 개 컬럼에 콤마로 구분해서 저장해야 할 때가 있다. 이때 `AttributeConverter`를 사용하면 밸류 컬렉션을 한 개 칼럼에 쉽게 매핑할 수 있다. 단, `AttributeConverter`를 사용하려면 밸류 컬렉션을 표현하는 새로운 밸류 타입을 추가해야 한다.

```java
public class EmailSet {
	private Set<Email> emails = new HashSet<>();

	private EmailSet() {}
	public EmailSet(Set<Email> emails) {
		this.emails.addAll(emails);
	}

	public Set<Email> getEmails() {
		return Collections.unmodifiableSet(emails);
	}
}
```

밸류 컬렉션을 위한 타입을 추가했다면 `AttributeConverter`를 구현한다.

```java
@Converter
public class EmailSetConverter implements AttributeConverter<EmailSet, String> {
	@Override
	public String convertToDatabaseColumn(EmailSet attribute) {
		if (attribute == null) return null;
		return attribute.getEmails().stream()
						.map(Email::toString)
						.collect(Collectors.joining(","));
	}
	@Override
	public EmailSet convertToEntityAttribute(String dbData) {
		if (dbData == null) return null;
		String[] emails = dbData.split(",");
		Set<Email> emailSet = Arrays.stream(emails)
						.map(value ->  new Email(value))
						.collect(toSet());
		return new EmailSet(emailSet);
	}
}
```

이제 남은 것은 `EmailSet` 타입의 프로퍼티가 `Converter`로 `EmailSetConverter`를 사용하도록 지정하는 것이다.

```java
@Column(name = "emails")
@Convert(converter = EmailSetConverter.class)
private EmailSet emailSet;
```

### 밸류를 이용한 ID 매핑
식별자라는 의미를 부각시키기 위해 식별자 작체를 밸류 타입으로 만들 수도 있다. 밸류 타입을 식별자로 매핑하면 `@Id` 대신 `@EmbeddedId` 애너테이션을 사용한다.

JPA 에서 식별자 타입은 `Serializable` 타입이어야 하므로 식별자로 사용될 밸류 타입은 `Serializable` 인터페이스를 상속받아야 한다.

```java
@Entity
@Table(name = "purchase_order")
public class Order {
  @EmbeddedId
  private OrderNo number;
}

@Embeddable
public class OrderNo implements Serializable {
	@Column(name = "order_number")
	private String number;

	public boolean is2ndGeneration() {
		return number.startsWith("N");
	}
	// ...
}
```

**밸류 타입으로 식별자를 구현할 때 얻을 수 있는 장점은 식별자에 기능을 추가할 수 있다는 점이다.** 예를 들어, 1세대 시스템의 주문번호와 2세대 시스템의 주문번호를 구분할 때 주문번호의 첫 글자를 이용할 경우, 위와 같이 `OrderNo` 클래스에 시스템 세대를 구분할 수 있는 기능을 구현할 수 있다.

```java
if (order.getNumber().is2ndGeneration()) {
  // ...
}
```

JPA는 내부적으로 엔티티를 비교할 목적으로 `equals()` 메서드와 `hashcode()` 값을 사용하므로 식별자로 사용할 밸류 타입은 이 두 메서드를 알맞게 구현해야 한다.

### 별도 테이블에 저장하는 밸류 매핑
- 애그리거트에서 루트 엔티티를 뺀 나머지 구성요소는 대부분 밸류이다.
  - 루트 엔티티 외에 또다른 엔티티가 있다면 진짜 엔티티인지 의심해봐야 한다.
  - 단지 별도 테이블에 저장된다고 해서 엔티티인 것은 아니다.
- 밸류가 아니라 엔티티가 확실하다면 해당 엔티티가 다른 애그리거트는 아닌지 확인해야 한다.
  - 특히 자신만의 독자적인 라이프 사이클을 갖는다면 구분되는 애그리거트일 가능성이 높다.
- 애그리거트에 속한 객체가 밸류인지 엔티티인지 구분하는 방법은 고유 식별자를 갖는지를 확인하는 것이다.
  - 하지만 식별자를 찾을 때 매핑되는 테이블의 식별자를 애그리거트 구성요소의 식별자와 동일한 것으로 착각하면 안 된다.
  - 별도 테이블로 저장하고 테이블에 PK가 있다고 해서 테이블과 매핑되는 애그리거트 구성요소가 항상 고유 식별자를 갖는 것은 아니기 때문이다.
- 예를 들어 게시글 데이터를 `ARTICLE` 테이블과 `ARTICLE_CONTENT` 테이블로 나눠서 저장한다고 할 때, `Article`과 `ArticleContent` 클래스를 두 테이블에 매핑할 수 있다.
  - `ArticleContent`는 `Article`의 내용을 담고 있는 밸류이다.
  - `ARTICLE_CONTENT`의 `ID`는 식별자이긴 하지만 이 식별자를 사용하는 이유는 `ARTICLE` 테이블의 데이터와 연결하기 위함일 뿐 `ARTICLE_CONTENT`를 위한 별도 식별자가 필요하기 때문은 아니다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6ee94839-53c7-4e7e-a239-79f3549f1ece)

`ArticleContent`를 밸류로 보고 접근하면 모델은 아래와 같이 바뀐다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a4d4ed69-2346-4a9b-a846-90e25697c47a)

`ArticleContent`는 밸류이므로 `@Embeddable`로 매핑한다. `ArticleContent`와 매핑되는 테이블은 `Article`과 매핑되는 테이블과 다른데, 이때 밸류를 매핑한 테이블을 지정하기 위해 `@SecondaryTable`과 `@AttributeOverride`를 사용한다.

```java
@Entity
@Table(name = "article")
@SecondaryTable(
	name = "article_content",
	pkJoinColumns = @PrimaryKeyJoinColumn(name = "id")
)
public class Article {
	@Id
	private Long id;
	...
	@AttributeOverrides({
		@AttributeOverride(name = "content",
			column = @Column(table = "article_content")),
		@AttributeOverride(name = "contentType",
			column = @Column(table = "article_content"))
	})
	private ArticleContent content;
	// ...
}
```

`@SecondaryTable`을 이용하면 아래 코드를 실행할 때 두 테이블을 조인해서 데이터를 조회한다.

```java
// @SecondaryTable로 매핑된 article_content 테이블을 조인
Article article = entityManager.find(Article.class, 1L);
```

한 가지 단점은 `@SecondaryTable`을 사용하면 목록 화면에 보여줄 `Article`을 조회할 때 `article_content` 테이블까지 조인해서 데이터를 읽어오는데 이는 원하는 결과가 아니다. 이는 5장에서 조회 전용 쿼리를 실행하여 해결할 수 있다.

### 밸류 컬렉션을 @Entity로 매핑하기
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fcbf4257-24d1-4035-980e-63e840648094)

개념적으로 밸류인데 구현 기술의 한계나 팀 표준 때문에 `@Entity`를 사용해야 할 때도 있다. JPA는 `@Embeddable` 타입의 클래스 상속 매핑을 지원하지 않는다. 대신 `@Entity`를 이용한 상속 매핑으로 처리해야 한다. 엔티티로 관리되므로 식별자 필드가 필요하고 타입 식별 칼럼을 추가해야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e1fec9c1-42c0-47ba-9ad9-cfff1c707682)

`Image`는 밸류이므로 독자적인 라이프 사이클을 가지는 대신 `Product`에 완전히 의존한다.

따라서 `Product`를 저장할 때 함께 저장되고 `Product`를 삭제할 때 함께 삭제되도록 `cascade` 속성을 지정한다. 리스트에서 `Image` 객체를 제거하면 DB에서 함께 삭제되도록 `orphanRemoval`도 `true`로 설정한다.

```java
@Entity
@Table(name = "product")
public class Product {
  @EmbeddedId
  private ProductId id;
  private String name;

  @Convert(converter = MoneyConverter.class)
  private Money price;
  private String detail;

  @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
          orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  @OrderColumn(name = "list_idx")
  private List<Image> images = new ArrayList<>();
  
  // ...

  public void changeImages(List<Image> newImages) {
      images.clear();
      images.addAll(newImages);
  }
}
```

- `changeImages()` 메서드를 보면 이미지 교체를 위해 `clear()` 메서드를 사용하고 있다.
- `@Entity`에 대한 `@OneToMany` 매핑에서 컬렉션의 `clear()` 메서드를 호출하면 삭제 과정이 다소 비효율적이다.
  - 하이버네이트의 경우 `@Entity`를 위한 컬렉션 객체의 `clear()` 메서드를 호출하면 select 쿼리로 대상 엔티티를 로딩하고, 각 개별 엔티티에 대해 delete 쿼리를 실행한다.
  - 이는 변경 빈도가 높은 경우 전체 서비스 성능에 문제를 일으킬 수 있다.
- 하이버네이트는 `@Embeddable` 타입에 대한 컬렉션의 `clear()` 메서드를 호출하면 컬렉션에 속한 객체를 로딩하지 않고 한 번의 delete 쿼리로 삭제 처리를 수행한다.
  - 따라서 애그리거트의 특성을 유지하면서 이 문제를 해소하기 위해선 상속을 포기하고 @Embeddable로 매핑된 단일 클래스로 구현해야 한다.
  - 타입에 따라 다른 기능을 구현하기 위해선 다형성을 포기하고 메서드 내부에서 조건절(if-else)을 통해 분기되는 로직을 수행해야 한다.

### ID 참조와 조인 테이블을 이용한 단방향 M-N 매핑
애그리거트 간 집합 연관은 성능상의 이유로 피해야 한다고 했다. 그럼에도 불구하고 요구사항을 구현하는 데 집합 연관을 사용하는 것이 유리하다면 ID 참조를 이용한 단방향 집합 연관을 적용해 볼 수 있다.

```java
@Entity
@Table(name = "product")
public class Product {
	@EmbeddedId
	private ProductId id;

	@ElementCollection
	@CollectionTable(name ="product_category",
		joinColumns = @JoinColumn(name = "product_id"))
	private Set<CategoryId> categoryIds;
	// ...
}
```

위 코드는 ID 참조를 이용한 애그리거트 간 단방향 M:N 연관은 밸류 컬렉션 매핑과 동일한 방식으로 설정한 것을 알 수 있다. 차이점이 있다면, 집합의 값에 밸류 대신 연관을 맺는 식별자가 온다.

`@ElementCollection`을 이용하기 때문에 `Product`를 삭제할 때 매핑에 사용한 조인 테이블의 데이터도 함께 삭제된다. 애그리거트를 직접 참조하는 방식을 사용했다면 영속성 전파나 로딩 전략을 고민해야 하는 데 ID 참조 방식을 사용함으로써 이런 고민을 없앨 수 있다.

## 애그리거트 로딩 전략
JPA 매핑을 설정할 때 항상 기억해야 할 점은 **애그리거트에 속한 객체가 모두 모여야 완전한 하나가 된다는 것이다.** 즉 다음과 같이 애그리거트 루트를 로딩하면 루트에 속한 모든 객체가 완전한 상태여야 함을 의미한다.

```java
// product는 완전한 하나여야 한다.
Product product = productRepository.findById(id);
```

애그리거트 루트에서 연관 매핑의 조회 방식을 즉시 로딩으로 설정하면 완전한 상태가 되도록 만들 수 있다. 하지만 이것이 항상 좋은 것은 아니다. 특히 컬렉션에 대해 로딩 전략을 `FetchType.EAGER`로 설정하면 오히려 즉시 로딩 방식이 문제가 될 수 있다.

- 예컨대 즉시 로딩 방식으로 설정된 엔티티 필드나 밸류 필드에 대해서 쿼리 실행 시 `lefter outer join`을 수행하기 때문에 성능적인 문제를 일으킬 수 있다.
  - 하이버네이트가 중복된 데이터를 알맞게 제거해서 실제 메모리 상에 알맞은 개수의 객체로 변환해주지만 애그리거트가 커지면 문제가 될 수 있다.
- 애그리거트는 개념적으로 하나여야 하지만 **루트 엔티티를 로딩하는 시점에 애그리거트에 속한 객체를 모두 로딩해야 하는 것은 아니다.**
- 애그리거트가 완전해야 하는 이유는 두 가지 정도이다.
  - 첫 번째 이유는 상태를 변경하는 기능을 실행할 때 애그리거트 상태가 완전해야 하기 때문이다.
  - 두 번째 이유는 표현 영역에서 애그리거트의 상태 정보를 보여줄 때 필요하기 때문이다.
- 이 중 두 번째는 별도의 조회 전용 기능과 모델을 구현하는 방식을 사용하는 것이 더 유리하기 때문에 **애그리거트의 완전한 로딩과 관련된 문제는 상태 변경과 더 관련이 있다.**
  - **JPA는 트랜잭션 범위 내에서 지연 로딩을 허용하기 때문에 실제로 상태를 변경하는 시점에 필요한 구성요소만 로딩해도 문제가 되지 않는다.**
  - 또한 일반적인 애플리케이션은 상태 변경을 실행하는 빈도보다 조회 기능을 실행하는 빈도가 훨씬 높으므로 상태 변경을 위해 지연 로딩을 사용할 때 발생하는 추가 쿼리로 인한 실행 속도 저하는 보통 문제가 되지 않는다.
- 위의 이유로 애그리거트 내의 모든 연관을 즉시 로딩으로 설정할 필요는 없다.
  - 지연 로딩은 동작 방식이 항상 동일하므로 즉시 로딩처럼 경우의 수를 따질 필요가 없는 장점이 있다.
  - 즉시 로딩 설정은 `@Entity`나 `@Embeddable`에 대해 다르게 동작하고, JPA 프로바이더에 따라 구현 방식이 다를 수 있다.

## 애그리거트의 영속성 전파
애그리거트가 완전한 상태여야 한다는 것은 애그리거트 루트를 조회할 때뿐만 아니라 저장하고 삭제할 때도 하나로 처리해야 함을 의미한다.

- 저장 메서드는 애그리거트 루트만 저장하면 안 되고 애그리거트에 속한 모든 객체를 저장해야 한다.
- 삭제 메서드는 애그리거트 루트뿐만 아니라 애그리거트에 속한 모든 객체를 삭제해야 한다.

`@Embeddable` 매핑 타입의 경우 함께 저장되고 삭제되므로 `cascade` 속성을 추가로 설정하지 않아도 된다. 반면에 애그리거트에 속한 `@Entity` 타입에 대한 매핑은 `cascade` 속성을 사용해서 저장과 삭제 시에 함께 처리되도록 설정해야 한다.
- `@OneToOne`, `@OneToMany`는 `cascade` 속성의 기본값이 없으므로 `CascadeType.PERSIST`, `CascadeType.REMOVE`를 설정한다.

```java
@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
        orphanRemoval = true, fetch = FetchType.LAZY)
@JoinColumn(name = "product_id")
@OrderColumn(name = "list_idx")
private List<Image> images = new ArrayList<>();
```

## 식별자 생성 기능
- 식별자는 크게 세 가지 방식 중 하나로 생성한다.
  - 사용자가 직접 생성
  - 도메인 로직으로 생성
  - DB를 이용한 일련번호 사용
- 식별자 생성 규칙이 있다면 **엔티티를 생성할 때 식별자를 엔티티가 별도 서비스로 식별자 생성 기능을 분리**해야 한다.
  - **식별자 생성 규칙은 도메인 규칙이므로 도메인 영역에 식별자 생성 기능을 위치**시켜야 한다.
  - 도메인 서비스에서 식별자를 생성하고, 응용 서비스가 이 도메인 서비스를 이용해서 식별자를 구하고 엔티티를 생성하게 구현할 수 있다.
  - **리포지토리 또한 식별자 생성 규칙을 구현하기에 적합**한데, 레포지토리 인터페이스에 식별자를 생성하는 메서드를 추가하고 리포지토리 구현 클래스에서 알맞게 구현하면 된다.
- DB 자동 증가 컬럼을 식별자로 생성하면 식별자 매핑에서 `@GeneratedValue`를 사용한다.
  - 자동 증가 컬럼은 DB의 insert 쿼리를 실행해야 식별자가 생성되므로 도메인 객체를 리포지토리에 저장할 때 생성된다.
  - **즉, 도메인 객체를 생성하는 시점에는 식별자를 알 수 없고 도메인 객체를 저장한 뒤에 식별자를 구할 수 있음을 의미**한다.
- JPA는 저장 시점에 생성한 식별자를 `@Id`로 매핑한 프로퍼티/필드에 할당하기 때문에 저장 이후엔 엔티티의 식별자를 이용할 수 있다.
  - 자동 증가 컬럼 외에 JPA의 식별자 생성 기능을 사용하는 경우에도 마찬가지로 저장 시점에 식별자를 생성한다.

## 도메인 구현과 DIP
이 장에서 구현한 리포지터리는 DIP 원칙을 어기고 있다.

```java
@Entity
@Table(name = "article")
@SecondaryTable(
  name = "article_content",
  pkJoinColumns = @PrimaryKeyJoinColumn(name = "id")
)
public class Article {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
}
```

DIP에 따르면 `@Entity`, `@Table`은 구현 기술에 속하므로 `Article`과 같은 도메인 모델은 구현 기술인 JPA에 의존하지 말아야 하는데 이 코드는 도메인 모델인 `Article`이 영속성 구현 기술인 JPA에 의존하고 있다.

리포지터리 인터페이스도 마찬가지다. `ArticleRepository` 인터페이스는 도메인 패키지에 위치하는데 구현 기술인 Spring Data JPA의 `Repository` 인터페이스를 상속하고 있다. 즉 도메인이 인프라에 의존하는 것이다.

```java
public interface ArticleRepository extends Repository<Article, Long> {
  void save(Article article);

  Optional<Article> findById(Long id);
}
```

구현 기술에 대한 의존 없이 도메인을 순수하게 유지하려면 Spring Data JPA의 `Repository` 인터페이스를 상속받지 않도록 수정하고 아래와 같이 `ArticleRepository` 인터페이스를 구현한 클래스를 인프라에 위치시켜야 한다.

또한 `Article` 클래스에서 `@Entity`나 `@Table`과 같이 JPA에 특화된 애너테이션을 모두 지우고 인프라에 JPA를 연동하기 위한 클래스를 추가해야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fdacadd3-92f3-481b-8928-daadef7a696d)


특정 기술에 의존하지 않는 순수한 도메인 모델을 추구하는 개발자는 위와 같은 구조로 구현한다. 이 구조를 가지면 구현 기술을 변경하더라도 도메인이 받는 영향을 최소화할 수 있다.

DIP를 적용하는 주된 이유는 저수준 구현이 변경되더라도 고수준이 영향을 받지 않도록 하기 위함이다. **하지만 리포지터리와 도메인 모델의 구현 기술은 거의 바뀌지 않는다.** 이렇게 변경이 거의 없는 상황에서 변경을 미리 대비하는 것은 과하다. DIP를 완벽하게 지키면 좋겠지만 개발 편의성과 실용성을 가지면서 구조적인 유연함은 어느 정도 유지한다. 복잡도를 높이지 않으면서 기술에 따른 구현 제약이 낮다면 합리적인 선택이다.