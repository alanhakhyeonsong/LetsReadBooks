# Chapter 10. 이벤트
## 시스템 간 강결합 문제
환불 기능을 실행하는 주체는 주문 도메인 엔티티가 될 수 있다.

```java
public class Order {
  // ...

  // 외부 서비스를 실행하기 위해 도메인 서비스를 파라미터로 전달 받음
  public void cancel(RefundService refundService) {
    verifyNotYetShipped();
    this.state = OrderState.CANCELED;

    this.refundStatus = State.REFUND_STARTED;
    try {
      refundService.refund(getPaymentId());
      this.refundStatus = State.REFUND_COMPLETED;
    } catch (Exception ex) {
      // ???
    }
  }
}
```

응용 서비스에서 환불 기능을 실행할 수도 있다.

```java
public class CancelOrderService {
  private RefundService refundService;

  @Transactional
  public void cancel(OrderNo orderNo) {
    Order order = findOrder(orderNo);
    order.cancel();

    order.refundStarted();
    try {
      refundService.refund(order.getPaymentId());
      order.refundCompleted();
    } catch (Exception ex) {
      // ???
    }
  }

  // ...
}
```

보통 결제 시스템은 외부에 존재하므로 `RefundService`는 외부에 있는 결제 시스템이 제공하는 환불 서비스를 호출한다. 이때 두 가지 문제가 발생할 수 있다.

- 외부 서비스가 정상이 아닐 경우 트랜잭션 처리를 어떻게 해야할 지 애매하다는 것이다.
  - 환불에 실패했으므로 주문 취소 트랜잭션을 롤백?
  - 주문은 취소 상태로 변경하고 환불만 나중에 다시 시도하는 방식으로 처리?
- 성능 문제
  - 외부 서비스 성능에 직접적인 영향을 받게 된다.

이 외에도 설계상 문제가 나타날 수 있다.

- `Order`는 주문을 표현하는 도메인 객체인데 결제 도메인의 환불 관련 로직이 뒤섞이게 된다.
  - 환불 기능이 바뀌면 `Order`도 영향을 받게 된다는 것을 의미함.
- 기능 추가 시 문제가 발생한다.
  - 주문을 취소한 뒤 환불뿐만 아니라 취소했다는 내용을 통지해야 한다면?
  - → 로직이 섞이는 문제가 더 커지고 트랜잭션 처리가 더 복잡해지며 영향을 주는 외부 서비스가 두 개로 증가한다.

```java
public class Order {
  // 기능을 추가할 때마다 파라미터가 함께 추가되면
  // 다른 로직이 더 많이 섞이고, 트랜잭션 처리가 더 복잡해진다.
  public void cancel(RefundService refundService, NotiService notiSvc) {
    verifyNotYetShipped();
    this.state = OrderState.CANCELED;
    // ...
    // 주문 + 결제 + 통지 로직이 섞임
    // refundService는 성공하고, notiSvc는 실패하면?
    // refundService와 notiSvc 중 무엇을 먼저 처리하나?
  }
}
```

위와 같은 문제들이 발생하는 이유는 **주문 바운디드 컨텍스트와 결제 바운디드 컨텍스트간의 강결합 때문이다.** 이런 강한 결합을 없앨 수 있는 방법은 바로 **이벤트를 사용하는 것이다.** 특히 비동기 이벤트를 사용하면 두 시스템 간의 결합을 크게 낮출 수 있다.

## 이벤트 개요
- 여기서 말하는 이벤트는 '과거에 벌어진 어떤 것'을 의미한다.
- 이벤트가 발생한다는 것은 상태가 변경됐다는 것을 의미한다.
- 발생하는 것에서 끝나지 않고 그 이벤트에 반응하여 원하는 동작을 수행하는 기능을 구현한다.

```javascript
// click에 전달한 함수는 이벤트가 발생하면 그 이벤트에 반응하여 경고 창을 출력
$("#myBtn").click(function(evt) {
  alert("경고");
});
```

도메인 모델에서도 UI 컴포넌트와 유사하게 도메인의 상태 변경을 이벤트로 표현할 수 있다.

- '~할 때', '~가 발생하면', '만약 ~하면'과 같은 요구사항은 도메인의 상태 변경과 관련된 경우가 많고 이런 요구사항을 이벤트를 이용해 구현할 수 있다.
  - 주문을 취소할 때 이메일을 보낸다.

### 이벤트 관련 구성 요소
- 이벤트
- 이벤트 생성 주체
  - 도메인 모델에서 이벤트 생성 주체는 엔티티, 밸류, 도메인 서비스와 같은 도메인 객체.
  - 도메인 로직을 실행해 상태가 바뀌면 관련 이벤트를 발생시킨다.
- 이벤트 핸들러(구독자)
  - 이벤트 생성 주체가 발생한 이벤트에 반응.
  - 생성 주체가 발생한 이벤트를 전달받아 이벤트에 담긴 데이터를 이용해 원하는 기능을 실행한다.
- 이벤트 디스패처(퍼블리셔)
  - 이벤트 생성 주체와 이벤트 핸들러를 연결해 주는 것.
  - 이벤트를 전달받은 디스패처는 해당 이벤트를 처리할 수 있는 핸들러에 이벤트를 전파한다.

### 이벤트의 구성
이벤트는 발생한 이벤트에 대한 정보를 담는다.

- 이벤트 종류: 클래스 이름을 이벤트 종류를 표현
- 이벤트 발생 시간
- 추가 데이터: 주문번호, 신규 배송지 정보 등 이벤트와 관련된 정보

```java
public class ShippingInfoChangedEvent {

  private String orderNumber;
  private long timestamp;
  private ShippingInfo newShippingInfo;

  // 생성자, getter
}
```

이벤트는 현재 기준으로 과거에 벌어진 것을 표현하기 때문에 과거 시제(`Changed`)를 사용한다.

```java
public class Order {

  public void changeShippingInfo(ShippingInfo shippingInfo) {
    verifyNotYetShipped();
    setShippingInfo(newShippingInfo);
    Events.raise(new ShippingInfoChangeEvent(number, newShippingInfo));
  }

  // ...
}

public class ShippingInfoChangeHandler {

  @EventListener(ShippingInfoChangeEvent.class)
  public void handle(ShippingInfoChangeEvent evt) {
    shippingInfoSynchronizer.sync(
      evt.getOrderNumber(),
      evt.getNewShippingInfo());
  }
}
```

이벤트는 이벤트 핸들러가 작업을 수행하는 데 필요한 데이터를 담아야 한다. 이 데이터가 부족하면 핸들러는 필요한 데이터를 읽기 위해 관련 API를 호출하거나 DB에서 데이터를 직접 읽어와야 한다.

### 이벤트 용도
- 트리거
  - 도메인의 상태가 바뀔 때 다른 후처리가 필요하면 후처리를 실행하기 위한 트리거로 이벤트를 사용
  - 주문 취소 이벤트
- 서로 다른 시스템 간의 데이터 동기화
  - 배송지 변경 시 외부 배송 서비스에 바뀐 배송지 정보를 전송해야 한다.
  - 주문 도메인은 배송지 변경 이벤트 발생 → 이벤트 핸들러는 외부 배송 서비스와 배송지 정보를 동기화

### 이벤트 장점
서로 다른 도메인 로직이 섞이는 것을 방지할 수 있다. 또한, 기능 확장이 용이하다.

![](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/84868a31-5c77-45f5-a8ef-deafc181d400)

## 이벤트, 핸들러, 디스패처 구현
스프링이 제공하는 이벤트 관련 기능을 사용해서 구현해본다.

- 이벤트 클래스: 이벤트를 구현한다.
- 디스패처: 스플이이 제공하는 `ApplicationEventPublisher`를 이용한다.
- Events: 이벤트를 발행한다. 이벤트 발행을 위해 `ApplicationEventPublisher`를 사용한다.
- 이벤트 핸들러: 이벤트를 수신해서 처리한다. 스프링이 제공하는 기능을 사용한다.

### 이벤트 클래스
이벤트 자체를 위한 상위 타입은 존재하지 않는다. 원하는 클래스를 이벤트로 사용하면 된다.

모든 이벤트가 공통으로 갖는 프로퍼티가 존재한다면 관련 상위 클래스를 만들 수도 있다.

```java
public abstract class Event {
  private long timestamp;

  public Event() {
    this.timestamp = System.currentTimeMillis();
  }

  public long getTimestamp() {
    return this.timestamp;
  }
}
```

### Events 클래스와 ApplicationEventPublisher
이벤트 발생과 출판을 위해 스프링이 제공하는 `ApplicationEventPublisher`를 사용한다. 스프링 컨테이너는 `ApplicationEventPublisher`도 된다.

```java
public class Events {
  private static ApplicationEventPublisher publisher;

  static void setPublisher(ApplicationEventPublisher publisher) {
    Events.publisher = publisher;
  }

  public static void raise(Object event) {
    if (publisher != null) {
      publisher.publishEvent(event);
    }
  }
}

@Configuration
@RequiredArgsConstructor
public class EventsConfiguration {
  
  private final ApplicationContext applicationContext;

  @Bean
  public InitializingBean eventsInitializer() {
    return () -> Events.setPublisher(applicationContext);
  }
}
```

### 이벤트 발생과 이벤트 핸들러
이벤트를 처리할 핸들러는 스프링이 제공하는 `@EventListener` 애너테이션을 사용해 구현한다.

```java
@Component
@RequiredArgsConstructor
public class OrderCanceledEventHandler {
  private final RefundService refundService;

  @EventListener(OrderCanceledEvent.class)
  public void handle(OrderCanceledEvent event) {
    refundService.refund(event.getOrderNumber());
  }
}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/738bf043-a200-46b4-8955-309430315dbb)

1. 도메인 기능을 실행한다.
2. 도메인 기능은 `Events.raise()`를 이용해 이벤트를 발생시킨다.
3. `Events.raise()`는 스프링이 제공하는 `ApplicationEventPublisher`를 이용해 이벤트를 출판한다.
4. `ApplicationEventPublisher`는 `@EventListener(이벤트타입.class)` 애너테이션이 붙은 메서드를 찾아 실행한다.

코드 흐름을 보면 응용 서비스와 동일한 트랜잭션 범위에서 이벤트 핸들러를 실행하고 있다. 즉, 도메인 상태 변경과 이벤트 핸들러는 같은 트랜잭션 범위에서 발생한다.

## 동기 이벤트 처리 문제
이벤트를 사용해 강결합 문제는 해소했지만 아직 남아 있는 문제가 하나 있다. 바로 **외부 서비스에 영향을 받는 문제이다.**

```java
// 1. 응용 서비스 코드
@Transactional
public void cancel(OrderNo orderNo) {
  Order order = findOrder(orderNo);
  order.cancel(); // order.cancel()에서 ORderCanceledEvent 발생
}

// 2. 이벤트를 처리하는 코드
@EventListener(OrderCanceledEvent.class)
public void handle(OrderCanceledEvent event) {
  // refundService.refund()가 느려지거나 익셉션이 발생하면?
  refundService.refund(event.getOrderNumber());
}
```

외부 환불 서비스와 연동한다 가정할 때, 만약 외부 환불 기능이 갑자기 느려지면 `cancel()` 메서드도 함께 느려진다. 이는 외부 서비스의 성능 저하가 바로 시스템 내 성능 저하로 연결된다는 것을 의미한다.

트랜잭션 역시 문제가 된다. 익셉션 발생 시 `cancel()` 메서드의 트랜잭션을 롤백 해야 할까? 이렇게 되면 구매 취소 기능을 롤백 하는 것이므로 구매 취소가 실패하는 것과 같다.

- 외부 환불 서비스 실행에 실패했다 해서 반드시 트랜잭션을 롤백 해야하는지에 대한 문제
  - 구매 취소 자체는 일단 처리하고 환불만 재처리하거나 수동으로 처리할 수도 있다.

외부 시스템과의 연동을 동기로 처리할 때 발생하는 성능과 트랜잭션 범위 문제를 해소하는 방법은 이벤트를 비동기로 처리하거나 이벤트와 트랜잭션을 연계하는 것이다.

## 비동기 이벤트 처리
회원 가입 신청을 하면 검증을 위해 이메일을 보내는 서비스가 많다. 회원 가입 신청을 하자마자 바로 내 메일함에 검증 이메일이 도착할 필요는 없다. 비슷하게 주문을 취소하자마자 바로 결제를 취소하지 않아도 된다. 즉, 일정 시간 안에만 후속 조치를 처리하면 되는 경우가 적지 않다.

'A 하면 이어서 B 하라'는 요구사항 중에서 'A 하면 최대 언제까지 B 하라'로 바꿀 수 있는 요구사항은 이벤트를 비동기로 처리하는 방식으로 구현할 수 있다. A 이벤트가 발생하면 별도 스레드로 B를 수행하는 핸들러를 실행하는 방식으로 요구사항을 구현할 수 있다.

이벤트를 비동기로 구현할 수 있는 방법은 다양하다.

- 로컬 핸들러를 비동기로 실행하기
- 메시지 큐를 사용하기
- 이벤트 저장소와 이벤트 포워더 사용하기
- 이벤트 저장소와 이벤트 제공 API 사용하기

### 로컬 핸들러 비동기 실행
이벤트 핸들러를 비동기로 실행하는 방법은 이벤트 핸들러를 별도 스레드로 실행하는 것이다. 스프링이 제공하는 `@Async` 애너테이션을 사용하면 손쉽게 비동기로 이벤트 핸들러를 실행할 수 있다.

- `@EnableAsync` 애너테이션을 사용해 비동기 기능을 활성화한다.
- 이벤트 핸들러 메서드에 `@Async` 애너테이션을 붙인다.

### 메시징 시스템을 이용한 비동기 구현
비동기로 이벤트를 처리해야 할 때 사용하는 또 다른 방법은 Kafka나 RabbitMQ와 같은 메시징 시스템을 사용하는 것이다.

이벤트가 발생하면 이벤트 디스패처는 이벤트를 메시지 큐에 보낸다. 메시지 큐는 이벤트를 메시지 리스너에 전달하고, 메시지 리스너는 알맞은 이벤트 핸들러를 이용해서 이벤트를 처리한다. 이때 이벤트를 메시지 큐에 저장하는 과정과 메시지 큐에서 이벤트를 읽어와 처리하는 과정은 별도 스레드나 프로세스로 처리된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f244c325-80ef-43e7-a2fe-af14a4f89ef4)

필요하다면 이벤트를 발생시키는 도메인 기능과 메시지 큐에 이벤트를 저장하는 절차를 한 트랜잭션으로 묶어야 한다. 도메인 기능을 실행한 결과를 DB에 반영하고 이 과정에서 발생한 이벤트를 메시지 큐에 저장하는 것을 같은 트랜잭션 범위에서 실행하려면 글로벌 트랜잭션이 필요하다.

글로벌 트랜잭션을 사용하면 안전하게 이벤트를 메시지 큐에 전달할 수 있는 장점이 있지만 전체 성능이 떨어지는 단점도 있다. 글로벌 트랜잭션을 지원하지 않는 메시징 시스템도 있다.

메시지 큐를 사용하면 보통 이벤트를 발생시키는 주체와 이벤트 핸들러가 별도 프로세스에서 동작한다. 이것은 이벤트 발생 JVM과 이벤트 처리 JVM이 다르다는 것을 의미한다. 물론 한 JVM에서 이벤트 발생 주체와 이벤트 핸들러가 메시지 큐를 이용해 이벤트를 주고받을 수 있지만, 동일 JVM에서 비동기 처리를 위해 메시지 큐를 사용하는 것은 시스템을 복잡하게 만들 뿐이다.

RabbitMQ 처럼 많이 사용되는 메시징 시스템은 글로벌 트랜잭션 지원과 함께 클러스터와 고가용성을 지원하기 때문에 안정적으로 멧지ㅣ를 전달할 수 있는 장점이 있다. 또한 다양한 개발 언어와 통신 프로토콜을 지원하고 있다.

Kafka는 글로벌 트랜잭션을 지원하진 않지만 다른 메시징 시스템에 비해 높은 성능을 보여준다.

### 이벤트 저장소를 이용한 비동기 처리
이벤트를 일단 DB에 저장한 뒤 별도 프로그램을 이용해 이벤트 핸들러에 전달하는 방식이 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/cf328e82-7dcc-4504-8c07-9c93b1d7255a)

- 이벤트가 발생하면 핸드러는 스토리지에 이벤트를 저장한다.
- 포워더는 주기적으로 이벤트 저장소에서 이벤트를 가져와 이벤트 핸들러를 실행한다.
- 포워더는 별도 스레드를 이용하기 때문에 이벤트 발행과 처리가 비동기로 처리된다.

이 방식은 도메인의 상태와 이벤트 저장소로 동일한 DB를 사용한다. 즉, 도메인의 상태 변화와 이벤트 저장이 로컬 트랜잭션으로 처리된다. 이벤트를 물리적 저장소에 보관하기 때문에 핸들러가 이벤트 처리에 실패할 경우 포워더는 다시 이벤트 저장소에서 이벤트를 읽어와 핸들러를 실행하면 된다.

이벤트 저장소를 이용한 두 번째 방법은 이벤트를 외부에 제공하는 API를 사용하는 것이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/40312efc-c7fe-4751-8f71-6d9c2d0e0c7c)

API 방식과 포워더 방식의 차이점은 이벤트를 전달하는 방식에 있다. 포워더 방식이 포워더를 통해 이벤트를 외부에 전달한다면, API 방식은 외부 핸들러가 API 서버를 통해 이벤트 목록을 가져간다. 포워더 방식은 이벤트를 어디까지 처리했는지 추적하는 역할이 포워더에 있다면 API 방식은 이벤트 목록을 요구하는 외부 핸들러가 자신이 어디까지 이벤트를 처리했는지 기억해야 한다.

### 이벤트 저장소 구현
- `EventEntry`: 이벤트 저장소에 보관할 데이터다. 이벤트를 식별하기 위한 `id`, 이벤트 타입인 `type`, 직렬화한 데이터 형식인 `contentType`, 이벤트 데이터 자체인 `payload`, 이벤트 시간인 `timestamp`를 갖는다.
- `EventStore`: 이벤트를 저장하고 조회하는 인터페이스를 제공한다.
- `JdbcEventStore`: JDBC를 이용한 `EventStore` 구현 클래스
- `EventApi`: REST API를 이용해 이벤트 목록을 제공하는 컨트롤러

이벤트는 과거에 벌어진 사건이므로 데이터가 변경되지 않는다. 이런 이유로 `EventStore` 인터페이스는 새로운 이벤트를 추가하는 기능과 조회하는 기능만 제공하고 기존 이벤트 데이터를 수정하는 기능은 제공하지 않는다.

API를 사용하는 클라이언트는 일정 간격으로 다음 과정을 실행한다.

1. 가장 마지막에 처리한 데이터의 offset인 `lastOffset`을 구한다. 저장한 `lastOffset`이 없으면 0을 사용한다.
2. 마지막에 처리한 `lastOffset`을 offset으로 사용해 API를 실행한다.
3. API 결과로 받은 데이터를 처리한다.
4. offset + 데이터 개수를 `lastOffset`으로 저장한다.

마지막에 처리한 `lastOffset`을 저장하는 이유는 같은 이벤트를 중복으로 처리하지 않기 위해서다.

### 포워더 구현
```java
@Component
@RequiredArgsConstructor
public class EventForwarder {
  private static final int DEFAULT_LIMIT_SIZE = 10;

  private final EventStore eventStore;
  private final OffsetStore offsetStore;
  private final EventSender eventSender;
  private int limitSize = DEFAULT_LIMIT_SIZE

  @Scheduled(initialDelay = 1000L, fixedDelay = 1000L)
  public void getAndSend() {
    long nextOffset = getNextOffset();
    List<EventEntry> events = eventStore.get(nextOffset, limitSize);
    if (!events.isEmpty()) {
      int processedCount = sendEvent(events);
      if (processedCount > 0) {
        saveNextOffset(nextOffset + processedCount);
      }
    }
  }

  private long getNextOffset() {
    return offsetStore.get();
  }

  private int sendEvent(List<EventEntry> events) {
    int processedCount = 0;
    try {
      for (EventEntry entry : events) {
        eventSender.send(entry);
        processedCount++;
      }
    } catch (Exception ex) {
      // 로깅 처리
    }
    return processedCount;
  }

  private void saveNextOffset(long nextOffset) {
    offsetStore.update(nextOffset);
  }
}

public interface OffsetStore {
  long get();
  void update(long nextOffset);
}

public interface EventSender {
  void send(EventEntry entry);
}
```

`OffsetStore`를 구현한 클래스는 `offset` 값을 DB에 보관하거나 로컬 파일에 보관해서 마지막 `offset` 값을 물리적 저장소에 보관하면 된다.

`EventSender` 인터페이스를 구현한 클래스는 `send()` 메서드에서 외부 메시징 시스템에 이벤트를 전송하거나 원하는 핸들러에 이벤트를 전달하면 된다. 이벤트 처리 중에 익셉션이 발생하면 그대로 전파해서 다음 주기에 `getAndSend()` 메서드를 실행할 때 재처리할 수 있도록 한다.

## 이벤트 적용 시 추가 고려 사항
이벤트를 구현할 때 추가로 고려할 점이 있다.

- 이벤트 소스를 `EntryEvent`에 추가할지 여부이다.
  - 앞서 구현한 방식은 이벤트 발생 주체에 대한 정보를 갖지 않도록 구현했는데, 특정 주체가 발생시킨 이벤트만 조회하는 기능을 구현할 수 없다. 이를 위해선 이벤트에 발생 주체 정보를 추가해야 한다.
- 포워더에서 전송 실패를 얼마나 허용할 것이냐에 대한 것이다.
  - 포워더는 이벤트 전송에 실패하면 실패한 이벤트로부터 다시 읽어와 전송을 시도한다. 그런데 특정 이벤트에서 계속 전송에 실패하면 그 이벤트 때문에 나머지 이벤트를 전송할 수 없게 된다. 따라서 포워더를 구현할 때는 실패한 이벤트의 재전송 횟수 제한을 두어야 한다.
- 이벤트 손실
  - 이벤트 저장소를 사용하는 방식은 이벤트 발생과 이벤트 저장을 한 트랜잭션으로 처리하기 때문에 트랜잭션에 성공하면 이벤트가 저장소에 보관된다는 것을 보장할 수 있다. 반면에 로컬 핸들러를 이용해서 이벤트를 비동기로 처리할 경우 이벤트 처리에 실패하면 이벤트를 유실하게 된다.
- 이벤트 순서
  - 이벤트 발생 순서대로 외부 시스템에 전달해야 할 경우, 이벤트 저장소를 사용하는 것이 좋다. 이벤트 저장소는 저장소에 이벤트를 발생 순서대로 저장하고 그 순서대로 이벤트 목록을 제공하기 때문이다. 반면에 메시징 시스템은 사용 기술에 따라 이벤트 발생 순서와 메시지 전달 순서가 다를 수도 있다.
- 이벤트 재처리
  - 동일한 이벤트를 다시 처리해야 할 때 이벤트를 어떻게 할지 결정해야 한다. 가장 쉬운 방법은 마지막으로 처리한 이벤트의 순번을 기억해 두었다가 이미 처리한 순번의 이벤트가 도착하면 해당 이벤트를 처리하지 않고 무시하는 것이다.

### 이벤트 처리와 DB 트랜잭션 고려
이벤트를 처리할 때는 DB 트랜잭션을 함께 고려해야 한다. 예를 들어 주문 취소와 환불 기능을 다음과 같이 이벤트를 이용해 구현했다고 치자.

- 주문 취소 기능은 주문 취소 이벤트를 발생시킨다.
- 주문 취소 이벤트 핸들러는 환불 서비스에 환불 처리를 요청한다.
- 환불 서비스는 외부 API를 호출해서 결제를 취소한다.

이벤트 발생과 처리를 모두 동기로 처리하면 복잡하다. 비동기로 이벤트를 처리할 때도 DB 트랜잭션을 고려해야 한다.

다만 경우의 수를 줄이면 도움이 된다. 경우의 수를 줄이는 방법은 트랜잭션이 성공할 때만 이벤트 핸들러를 실행하는 것이다.

스프링은 `@TransactionalEventListener` 애너테이션을 지원한다. 이 애너테이션은 트랜잭션 상태에 따라 이벤트 핸들러를 실행할 수 있게 한다.

```java
@TransactionalEventListener(
  classes = OrderCanceledEvent.class,
  phase = Transactional.AFTER_COMMIT;
)
public void handle(OrderCanceledEvent event) {
  refundService.refund(event.getOrderNumber());
}
```

중간에 에러가 발생해서 트랜잭션이 롤백되면 핸들러 메서드를 실행하지 않는다. 이 기능을 사용하면 이벤트 핸들러를 실행했는데 트랜잭션이 롤백되는 상황은 발생하지 않는다.

이벤트 저장소로 DB를 사용해도 동일한 효과를 볼 수 있다. 이벤트 발생 코드와 이벤트 저장 처리를 한 트랜잭션으로 처리하면 된다. 이렇게 하면 트랜잭션이 성공할 때만 이벤트가 DB에 저장되므로, 트랜잭션은 실패했는데 이벤트 핸들러가 실행되는 상황은 발생하지 않는다.

트랜잭션이 성공할 때만 이벤트 핸들러를 실행하게 되면 트랜잭션 실패에 대한 경우의 수가 줄어 이제 이벤트 처리 실패만 고민하면 된다. 이벤트 특성에 따라 재처리 방식을 결정하면 된다.

