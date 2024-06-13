# Chapter 8. 애그리거트 트랜잭션 관리
## 애그리거트와 트랜잭션
한 주문 애그리거트에 대해 운영자는 배송 상태로 변경할 때 사용자는 배송지 주소를 변경하면 어떻게 될까?

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a511b3af-a0cb-4a54-a632-14a939f38c42)

각 스레드는 개념적으로 동일한 애그리거트지만 물리적으로 서로 다른 애그리거트 객체를 사용한다. 때문에 운영자 스레드가 주문 애그리거트 객체를 배송 상태로 변경하더라도 고객 스레드가 사용하는 주문 애그리거트 객체에는 영향을 주지 않는다. 고객 스레드 입장에서 주문 애그리거트 객체는 아직 배송 상태 전이므로 배송지 정보를 변경할 수 있다.

이 상황에서 두 스레드는 각각 트랜잭션을 커밋할 때 수정한 내용을 DB에 반영한다. 이 시점에 배송 상태로 바뀌고 배송지 정보도 바뀌게 된다. 이 순서의 문제점은 **애그리거트의 일관성이 깨지는 것이다.**

이런 문제가 발생하지 않도록 하려면 다음 두 가지 중 하나를 해야 한다.

- 운영자가 배송지 정보를 조회하고 상태를 변경하는 동안, 고객이 애그리거트를 수정하지 못하게 막는다.
- 운영자가 배송지 정보를 조회한 이후에 고객이 정보를 변경하면, 운영자가 애그리거트를 다시 조회한 뒤 수정하도록 한다.

이 두 가지는 **애그리거트 자체의 트랜잭션과 관련이 있다.** DBMS가 지원하는 트랜잭션과 함께 애그리거트를 위한 추가적인 트랜잭션 처리 기법이 필요하다. 애그리거트에 대해 사용할 수 있는 대표적인 트랜잭션 처리 방식에는 Pessimistic Lock과 Optimistic Lock이 있다.

## 선점 잠금
선점 잠금(Pessimistic Lock)은 **먼저 애그리거트를 구한 스레드가 애그리거트 사용이 끝날 때까지 다른 스레드가 해당 애그리거트를 수정하지 못하게 막는 방식이다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d7bb59cb-02ac-45f2-8349-cb685da0096f)

스레드1이 애그리거트를 수정하고 트랜잭션을 커밋하면 잠금을 해제한다. 이 순간 대기하고 있던 스레드2가 애그리거트에 접근하게 된다. 스레드1이 트랜잭션을 커밋한 뒤에 스레드2가 애그리거트를 구하게 되므로 스레드2는 스레드1이 수정한 애그리거트의 내용을 보게 된다.

한 스레드가 애그리거트를 구하고 수정하는 동안 다른 스레드가 수정할 수 없으므로 동시에 애그리거트를 수정할 때 발생하는 데이터 충돌 문제를 해소할 수 있다.

선점 잠금은 보통 DBMS가 제공하는 행 단위 잠금을 사용해서 구현한다. 오라클을 비롯한 다수 DBMS가 `for update`와 같은 쿼리를 사용해서 특정 레코드에 한 사용자만 접근할 수 있는 잠금 장치를 제공한다.

JPA의 `EntityManager`는 `LockModeType`을 인자로 받는 `find()` 메서드를 제공하는데, `LockModeType.PESSIMISTIC_WRITE`를 값으로 전달하면 해당 엔티티와 매핑된 테이블을 이용해서 선점 잠금 방식을 적용할 수 있다. 하이버네이트의 경우 잠금 모드로 사용하면 `for update`쿼리를 사용해서 선점 잠금을 구현한다.

```java
Order order = entityManager.find(Order.class, orderNo, LockModeType.PESSIMISTIC_WRITE);
```

Spring Data JPA는 `@Lock` 애너테이션을 사용해 잠금 모드를 지정한다.

```java
public interface MemberRepository extends Repository<Member, MemberId> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select m from Member m where m.id = :id")
  Optional<Member> findByIdForUpdate(@Param("id") MemberId memberId);
}
```

### 선점 잠금과 교착 상태
**선점 잠금 기능을 사용할 때는 잠금 순서에 따른 교착 상태가 발생하지 않도록 주의해야 한다.**

1. 스레드1: A 애그리거트에 대한 선점 잠금 구함
2. 스레드2: B 애그리거트에 대한 선점 잠금 구함
3. 스레드1: B 애그리거트에 대한 선점 잠금 시도
4. 스레드2: A 애그리거트에 대한 선점 잠금 시도

이 순서에 따르면 스레드1은 영원히 B 애그리거트에 대한 선점 잠금을 구할 수 없다. 왜냐하면 스레드2가 B 애그리거트에 대한 잠금을 이미 선점하고 있기 때문이다. 동일한 이유로 스레드2는 A 애그리거트에 대한 잠금을 구할 수 없다. 두 스레드는 상대방 스레드가 먼저 선점한 잠금을 구할 수 없어 더 이상 다음 단계를 진행하지 못하게 된다.

**선점 잠금에 따른 교착 상태는 상대적으로 사용자 수가 많을 때 발생할 가능성이 높고, 사용자 수가 많아지면 교착 상태에 빠지는 스레드는 더 빠르게 증가한다.** 더 많은 스레드가 교착 상태에 빠질수록 시스템은 아무것도 할 수 없는 상태가 된다.

이런 문제가 발생하지 않도록 하려면 잠금을 구할 때 최대 대시 시간을 지정해야 한다. JPA에서 선점 잠금을 시도할 때 최대 대기 시간을 지정하려면 다음과 같이 힌트를 사용하면 된다.

```java
Map<String, Object> hints = new HashMap<>();
hints.put("javax.persistence.lock.timeout", 2000);
Order order = entityManager.find(Order.class, orderNo, 
		LockModeType.PESSIMISTIC_WRITE, hints);
```

위 힌트는 잠금을 구하는 대기 시간을 밀리초 단위로 지정한다. 지정한 시간 이내에 잠금을 구하지 못하면 예외를 발생시킨다. 이 힌트를 사용할 때 주의할 점은 DBMS에 따라 힌트가 적용되지 않을 수도 있다는 것이다. 힌트를 이용할 때에는 사용 중인 DBMS가 관련 기능을 지원하는지 확인해야 한다.

Spring Data JPA는 `@QueryHints` 애너테이션을 사용해 쿼리 힌트를 지정할 수 있다.

```java
public interface MemberRepository extends Repository<Member, MemberId> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({
    @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")
  })
  @Query("select m from Member m where m.id = :id")
  Optional<Member> findByIdForUpdate(@Param("id") MemberId memberId);
}
```

## 비선점 잠금
선점 잠금이 강력해 보이긴 하지만 선점 잠금으로 모든 트랜잭션 충돌 문제가 해결되는 것은 아니다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e1bc85e2-cdb9-4e07-8a06-f16cc35105f1)

1. 운영자는 배송을 위해 주문 정보를 조회한다. 시스템은 정보를 제공한다.
2. 고객이 배송지 변경을 위해 변경 폼을 요청한다. 시스템은 변경 폼을 제공한다.
3. 고객이 새로운 배송지를 입력하고 폼을 전송하여 배송지를 변경한다.
4. 운영자가 1번에서 조회한 주문 정보를 기준으로 배송지를 정하고 배송 상태 변경을 요청한다.

한 스레드에서 데이터를 조회한 후 변경하기 전에 다른 스레드에서 데이터를 변경하면 문제가 발생할 수 있다. 이때 필요한 것이 비선점 잠금이다.

**비선점 잠금은 동시에 접근하는 것을 막는 대신 변경한 데이터를 실제 DBMS에 반영하는 시점에 변경 가능 여부를 확인하는 방식이다.**

비선점 잠금을 구현하려면 애그리거트에 버전으로 사용할 숫자 타입 프로퍼티를 추가해야 한다. 애그리거트를 수정할 때마다 버전으로 사용할 프로퍼티 값이 1씩 증가하는데 이때 다음과 같은 쿼리를 사용한다.

```sql
UPDATE aggtable SET version = version + 1, colx = ?, coly = ?
WHERE aggid = ? and version = 현재버전
```

이 쿼리는 수정할 애그리거트와 매핑되는 테이블의 버전 값이 현재 애그리거트의 버전과 동일한 경우에만 데이터를 수정한다. 그리고 수정에 성공하면 버전 값을 1 증가시킨다. 다른 트랜잭션이 먼저 데이터를 수정해서 버전 값이 바뀌면 데이터 수정에 실패하게 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c76a9adf-8882-4cb1-8c1c-68dcce6c56d9)

JPA는 버전을 이용한 비선점 잠금 기능을 지원한다. 다음과 같이 버전으로 사용할 필드에 `@Version` 애너테이션을 붙이고 매핑되는 테이블에 버전을 저장할 칼럼을 추가하면 된다. 엔티티가 변경되는 시점에 다음과 같은 비선점 잠금 쿼리가 실행된다.

```java
@Entity
@Table(name = "purchase_order")
@Access(AccessType.FIELD)
public class Order {
	@EmbeddedId
	private OrderNo number;

	@Version
	private long version;
	
	// ...
}
```

JPA는 엔티티가 변경되어 UPDATE 쿼리를 실행할 때 `@Version`에 명시한 필드를 이용해서 비선점 잠금 쿼리를 실행한다. 즉, 애그리거트 객체의 버전이 10이면 UPDATE 쿼리를 실행할 때 다음과 같은 쿼리를 사용해서 버전이 일치하는 경우에만 데이터를 수정한다.

```sql
UPDATE purchase_order SET ..., version = version + 1
WHERE number = ? and version = 10
```

응용 서비스는 버전에 대해 알 필요가 없다. 리포지터리에서 필요한 애그리거트를 구하고 알맞은 기능만 실행하면 된다. 기능 실행 과정에서 애그리거트 데이터가 변경되면 JPA는 트랜잭션 종료 시점에 비선점 잠금을 위한 쿼리를 실행한다.

```java
public class ChangeShippingService {

  @Transactional
  public void changeShipping(ChangeShippingRequest changeReq) {
    Order order = orderRepository.findById(new OrderNo(changeReq.getNumber()));
    checkNoOrder(order);
    order.changeShippingInfo(changeReq.getShippingInfo());
  }

  // ...
}
```

**비선점 잠금을 위한 쿼리를 실행할 때 쿼리 실행 결과로 수정된 행의 개수가 0이면 이미 누군가 앞서 데이터를 수정한 것이다. 이는 트랜잭션이 충돌한 것이므로 트랜잭션 종료 시점에 예외가 발생한다.**

위 코드에선 Spring의 `@Transactional`을 이용해 트랜잭션 범위를 정했으므로 `changeShipping()` 메서드가 리턴될 때 트랜잭션이 종료되고, 이 시점에 트랜잭션 충돌이 발생하면 `OptimisticLockingFailureException`이 발생한다.

표현 영역 코드는 이 익셉션이 발생했는지에 따라 트랜잭션 충돌이 일어났는지 확인할 수 있다.

```java
@Controller
public class OrderController {
  private ChangeShippingService changeShippingService;

  @PostMapping("/changeShipping")
  public String changeShipping(ChangeShippingRequest changeReq) {
    try {
      changeShippingService.changeShipping(changeReq);
      return "changeShippingSuccess";
    } catch(optimisticLockingFailureException ex) {
        // 누군가 먼저 같은 주문 애그리거트를 수정했으므로, 
        // 트랜잭션 충돌이 일어났다는 메시지를 보여준다. 
        return "changeShippingExConflict";
    }
}
```

비선점 잠금을 앞선 상황으로 확장해서 적용할 수 있다. 시스템은 사용자에게 수정 폼을 제공할 때 애그리거트 버전을 함께 제공하고, 폼을 서버에 전송할 때 이 버전을 함께 전송한다. 아래 처럼 사용자가 전송한 버전과 애그리거트 버전이 동일한 경우에만 애그리거트 수정 기능을 수행하도록 함으로써 트랜잭션 충돌 문제를 해소할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/319770f3-32d6-4498-850e-d6e1919170c2)

비선점 잠금 방식을 여러 트랜잭션으로 확장하려면 애그리거트 정보를 뷰로 보여줄 때 버전 정보도 함께 사용자 화면에 전달해야 한다. 사용자 요청을 처리하는 응용 서비스를 위한 요청 데이터는 사용자가 전송한 버전값을 포함한다. 예를 들어, 배송 상태 변경을 처리하는 응용 서비스가 전달받는 데이터는 다음과 같이 주문 번호와 함께 해당 주문을 조회한 시점의 버전 값을 포함해야 한다.

응용 서비스는 전달받은 버전 값을 이용해서 애그리거트의 버전과 일치하는지 확인하고 일치하는 경우에만 요청한 기능을 수행한다. 표현 계층은 버전 충돌 익셉션이 발생하면 버전 충돌을 사용자에게 알려주고 사용자가 알맞은 후속 처리를 할 수 있도록 한다.

```java
public class StartShippingService {

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public void startShipping(StartShippingRequest req) {
      Optional<Order> orderOpt = orderRepository.findById(new OrderNo(req.getOrderNumber()));
      Order order = orderOpt.orElseThrow(() -> new NoOrderException());
      if (order.matchVersion(req.getVersion())) {
          throw new VersionConflictException(); // 이미 누군가가 애그리거트를 수정했다면 익셉션 발생
      }
      order.startShipping();
  }
  // ...
}
```

```java
@Controller
public class OrderAdminController {
	private StartShippingService startShippingService;

	@PostMapping("/startShipping")
	public String startShipping(StartShippingRequest startReq) {
		try {
			startShippingService.startShipping(startReq);
			return "shippingStarted";
		} catch(OptimisticLockingFailureException | VersionConflictException ex) {
			// 트랜잭션 충돌
			return "startShippingTxConflict";
		}
	}
	// ... 
```

- 응용 서비스는 전달받은 버전 값을 이용해서 애그리거트 버전과 일치하는지 확인하고, 일치하는 경우에만 기능을 수행한다.
- `matchVersion` 메서드는 현재 애그리거트의 버전과 인자로 전달받은 버전이 일치하면 true를, 아니라면 false를 리턴하도록 구현한다.
- `VersionConflictException`은 이미 누군가가 애그리거트를 수정했다는 것을 의미하고, `OptimisticLockingFailureException`은 이미 누군가가 거의 동시에 애그리거트를 수정했다는 것을 의미한다.

### 강제 버전 증가
애그리거트에 애그리거트 루트 외에 다른 엔티티가 존재하는데 기능 실행 도중 루트가 아닌 다른 엔티티의 값만 변경된다고 하자. **이 경우 JPA는 루트 엔티티의 버전 값을 증가시키지 않는다.** 연관된 엔티티의 값이 변경된다고 해도 루트 엔티티 자체의 값은 바뀌는 것이 없으므로 루트 엔티티의 버전 값은 갱신하지 않는 것이다.

그런데 이런 JPA 특징은 애그리거트 관점에서 보면 문제가 된다. 비록 루트 엔티티의 값이 바뀌지 않았더라도 애그리거트의 구성요소 중 일부 값이 바뀌면 논리적으로 그 애그리거트는 바뀐 것이다. **따라서 애그리거트 내에 어떤 구성요소의 상태가 바뀌면 루트 애그리거트의 버전 값이 증가해야 비선점 잠금이 올바르게 동작한다.**

JPA는 이런 문제를 처리할 수 있도록 `EntityManager#find()` 메서드로 엔티티를 구할 때 강제로 버전 값을 증가시키는 잠금 모드를 지원한다.

```java
@Repository
public class JpaOrderRepository implements OrderRepository {
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Order findByIdOptimisticLockMode(OrderNo id) {
		return entityManager.find(Order.class, id, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
	}
  // ...
}
```

`LockModeType.OPTIMISTIC_FORCE_INCREMENT`를 사용하면 해당 엔티티의 상태가 변경되었는지에 상관없이 트랜잭션 종료 시점에 버전 값 증가 처리를 한다. 이 잠금 모드를 사용하면 애그리거트 루트 엔티티가 아닌 다른 엔티티나 밸류가 변경되더라도 버전 값을 증가시킬 수 있으므로 비선점 잠금 기능을 안전하게 적용할 수 있다.

Spring Data JPA를 사용하면 `@Lock` 애너테이션을 이용해 지정하면 된다.

## 오프라인 선점 잠금
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/4a232782-690c-45d2-8672-e18d7d29e35c)

- 오프라인 선점 잠금(Offline Pessimistic Lock)은 여러 트랜잭션에 걸쳐 동시 변경을 막는다.
  - 이는 단일 트랜잭션에서 동시 변경을 막는 선점 잠금 방식과 차이가 있다.
- **첫 번째 트랜잭션을 시작할 때 오프라인 잠금을 선점하고, 마지막 트랜잭션에서 잠금을 해제**한다.
- 오프라인 선점 잠금은 **잠금 유효 시간**을 가져야 한다.
  - 잠금을 선점한 사용자가 마지막 트랜잭션을 수행하기 전에 프로그램이 종료되면 다른 사용자가 영원히 잠금을 구할 수 없게 되기 때문이다.

### 오프라인 선점 잠금을 위한 LockManager 인터페이스와 관련 클래스
오프라인 선점 잠금은 크게 잠금 선점 시도, 잠금 확인, 잠금 해제, 잠금 유효시간 연장의 네 가지 기능이 필요하다.

```java
package com.myshop.lock;

public interface LockManager {
  LockId tryLock(String type, String id) throws LockException;

  void checkLock(LockId lockId) throws LockException;

  void releaseLock(LockId lockId) throws LockException;

  void extendLockExpiration(LockId lockId, long inc) throws LockException;
}
```

```java
public class LockId {
  private String value;

  public LockId(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
```

- 오프라인 선점 잠금이 필요한 코드는 `LockManager#tryLock()`을 이용해 잠금을 시도한다.
- 잠금에 성공하면 `tryLock()`은 `LockId`를 리턴한다.
- `LockId`는 다음에 잠금을 해제할 때 사용하며, 어딘가에 보관해야 한다.

```java
// 서비스: 서비스는 잠금 ID를 리턴한다
public DataAndLockId getDataWithLock(Long id) {
  // 1. 오프라인 선점 시도
  LockId lockId = lockManager.tryLock("data", id);
  // 2. 기능 실행
  Data data = someDao.select(id);
  return new DataAndLockId(data, lockId);
}
```

- 서비스는 DAO를 통해 조회한 데이터와 `LockId`를 함께 반환한다.
- 컨트롤러는 서비스가 리턴한 `LockId`를 모델로 뷰에 전달한다.
- 잠금을 선점하는 데 실패하면 `LockException`이 발생한다.

```java
// 서비스: 잠금을 해제한다.
public void edit(EditRequest editReq, LockId lockId) {
  // 1. 잠금 선점 확인
  lockManager.checkLock(lockId);
  // 2. 기능 실행
  // ...
  // 3. 잠금 해제
  lockManager.releaseLock(lockId);
}
```

- `LockId`는 뷰와 컨트롤러를 거쳐 잠금을 해제하는 서비스 코드에 전달된다.
- 잠금을 선점한 이후에 실행하는 기능은 다음과 같은 상황을 고려하여 반드시 주어진 `LockId`를 갖는 잠금이 유효한지 확인해야 한다.
  - 잠금 유효 시간이 지났으면 이미 다른 사용자가 잠금을 선점한다.
  - 잠금을 선점하지 않은 사용자가 기능을 실행했다면 기능 실행을 막아야 한다.

### DB를 이용한 LockManager 구현
잠금 정보를 저장할 테이블과 인덱스를 아래와 같이 생성한다.

```sql
create table locks (
  `type` varchar(255),
  id varchar(255),
  lockid varchar(255),
  expiration_time datetime,
  primary key (`type`, id)
) character set utf8;

create unique index locks_idx ON locks (lockid);
```

`Order` 타입의 1번 식별자를 갖는 애그리거트에 대한 잠금을 구하고 싶다면 다음의 `insert` 쿼리를 이용해 `locks` 테이블에 데이터를 삽입하면 된다.

```sql
insert into locks values ('Order', '1', '생성한lockid', '2024-06-13 22:10:00');
```

```java
public class LockData {
  private String type;
  private String id;
  private String lockId;
  private long timestamp;

  public LockData(String type, String id, String lockId, long timestamp) {
    this.type = type;
    this.id = id;
    this.lockId = lockId;
    this.timestamp = timestamp;
  }

  public String getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getLockId() {
    return lockId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public boolean isExpired() {
    return timestamp < System.currentTimeMillis();
  }
}
```

`locks` 테이블을 이용해 `LockManager`를 구현한 코드는 다음과 같다. DB 연동은 스프링이 제공하는 `JdbcTemplate`을 이용해 처리했다.

```java
@Component
public class SpringLockManager implements LockManager {
  private int lockTimeout = 5 * 60 * 1000;
  private JdbcTemplate jdbcTemplate;

  private RowMapper<LockData> lockDataRowMapper = (rs, rowNum) ->
          new LockData(rs.getString(1), rs.getString(2),
                  rs.getString(3), rs.getTimestamp(4).getTime());

  public SpringLockManager(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * type과 id에 대한 잠금을 시도한다
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public LockId tryLock(String type, String id) throws LockException {
    checkAlreadyLocked(type, id);
    LockId lockId = new LockId(UUID.randomUUID().toString());
    locking(type, id, lockId);
    return lockId;
  }

  /**
   * 잠금이 존재하는지 검사한다
   */
  private void checkAlreadyLocked(String type, String id) {
    List<LockData> locks = jdbcTemplate.query(
            "select * from locks where type = ? and id = ?",
            lockDataRowMapper, type, id);
    Optional<LockData> lockData = handleExpiration(locks);
    if (lockData.isPresent()) throw new AlreadyLockedException();
  }

  /**
   * 잠금 유효 시간이 지나면 해당 데이터를 삭제하고, 값이 없는 Optional을 리턴한다
   * 유효 시간이 지나지 않았으면 해당 LockData를 가진 Optional을 리턴한다
   */
  private Optional<LockData> handleExpiration(List<LockData> locks) {
    if (locks.isEmpty()) return Optional.empty();
    LockData lockData = locks.get(0);
    if (lockData.isExpired()) {
      jdbcTemplate.update(
              "delete from locks where type = ? and id = ?",
              lockData.getType(), lockData.getId());
      return Optional.empty();
    } else {
      return Optional.of(lockData);
    }
  }

  /**
   * 잠금을 위해 locks 테이블에 데이터를 삽입한다
   * 데이터 삽입 결과가 없으면 익셉션을 발생시킨다
   * DuplicateKeyException이 발생하면 LockingFailException을 발생시킨다
   */
  private void locking(String type, String id, LockId lockId) {
    try {
      int updatedCount = jdbcTemplate.update(
              "insert into locks values (?, ?, ?, ?)",
              type, id, lockId.getValue(), new Timestamp(getExpirationTime()));
      if (updatedCount == 0) throw new LockingFailException();
    } catch (DuplicateKeyException e) {
      throw new LockingFailException(e);
    }
  }

  /**
   * 현재 시간 기준으로 lockTimeout 이후 시간을 유효 시간으로 생성한다
   */
  private long getExpirationTime() {
    return System.currentTimeMillis() + lockTimeout;
  }

  /**
   * 잠금이 유효한지 검사한다
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void checkLock(LockId lockId) throws LockException {
    Optional<LockData> lockData = getLockData(lockId);
    if (!lockData.isPresent()) throw new NoLockException();
  }

  /**
   * lockId에 해당하는 LockData를 구한다
   */
  private Optional<LockData> getLockData(LockId lockId) {
      List<LockData> locks = jdbcTemplate.query(
              "select * from locks where lockid = ?",
              lockDataRowMapper, lockId.getValue());
      return handleExpiration(locks);
  }

  /**
   * lockId에 해당하는 잠금 유효 시간을 inc 만큼 늘린다
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void extendLockExpiration(LockId lockId, long inc) throws LockException {
    Optional<LockData> lockDataOpt = getLockData(lockId);
    LockData lockData =
            lockDataOpt.orElseThrow(() -> new NoLockException());
    jdbcTemplate.update(
            "update locks set expiration_time = ? where type = ? AND id = ?",
            new Timestamp(lockData.getTimestamp() + inc),
            lockData.getType(), lockData.getId());
  }

  /**
   * lockId에 해당하는 잠금 데이터를 locks 테이블에서 삭제한다
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void releaseLock(LockId lockId) throws LockException {
    jdbcTemplate.update("delete from locks where lockid = ?", lockId.getValue());
  }

  public void setLockTimeout(int lockTimeout) {
    this.lockTimeout = lockTimeout;
  }
}
```