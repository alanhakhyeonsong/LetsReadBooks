# 2장. 코드 중복 대 유연성 - 코드 중복이 항상 나쁘지만은 않다
## 코드베이스 사이의 공통 코드와 중복
HTTP API에 인가를 추가하기 위한 새로운 비즈니스 요구사항이 생길 때의 예시.

두 팀에서 처음으로 선택한 방식은 인가 컴포넌트를 양쪽 코드베이스에 구현.

```kotlin
@RestController
@RequestMapping("/payment")
public class PaymentController(
  private val paymentService: PaymentService,
  private val authService: AuthService
) {
  @GetMapping("/{token}")
  public fun getAllPayments(@PathVariable token: String): Response {
    if (authService.isTokenValid(token)) {
      return Response.ok(paymentService.getAllPayments()).build();
    } else {
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }
}

@Component
public class AuthService {
  public fun isTokenValid(token: String): Boolean {
    return token.equals("secret");
  }
}
```

```kotlin
@RestController
@RequestMapping("/person")
public class PersonController(
  private val personService: PersonService,
  private val authService: AuthService
) {
  @GetMapping("/{token}/{id}")
  public fun getAllPersonById(
    @PathVariable("token") token: String,
    @PathVariable("id") id: String): Response {
    if (authService.isTokenValid(token)) {
      return Response.ok(personService.getById(id)).build();
    } else {
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }
}

@Component
public class AuthService {
  public fun isTokenValid(token: String): Boolean {
    return token.equals("secret");
  }
}
```

- **중복은 더 많은 버그와 실수를 유발할 수 있다.**
  - Person을 개발하는 팀이 인가 컴포넌트에서 버그를 수정하더라도 Payment를 개발하는 팀이 동일한 실수를 저지르지 않는다는 보장은 없다.
- **독립적인 코드베이스 사이에서 동일하거나 유사한 코드가 중복될 때 개발자들 사이에 지식 공유가 일어나지 않는다.**
  - Person을 개발하는 팀이 토큰 계산 과정에서 버그를 발견하고 팀 내 코드베이스에서 수정한다. 불행히도, 이런 수정은 Payment 개발 팀의 코드베이스까지 자동으로 전파되지 못한다.
- **조정 없는 작업은 더 빠르게 진도가 나갈 수 있다.** 그렇다 하더라도 두 팀이 유사하게 수행한 작업이 상당히 많을 수도 있다.

현실은 처음부터 맨땅에 로직을 구현하는 대신 OAuth2 또는 JWT와 같이 실전에서 검증된 인증 전략을 사용할 가능성이 높다. 이런 검증된 전략은 마이크로서비스 아키텍처의 컨텍스트에서 훨씬 더 유용하다는 사실이 증명된다.

## 라이브러리, 그리고 코드베이스 사이에서 코드 공유
독립적인 두 코드베이스 사이에 상당향 분량의 코드가 중복되어 두 팀이 공통 코드를 찾아내 독자적인 라이브러리로 추출하기로 결정했다 가정하자. 인가 서비스 코드를 독립적인 저장소로 추출하고 외부 저장소에 해당 라이브러리를 올린다.

공통 코드를 외부 저장소에 올리면 두 서비스는 빌드 시점에 라이브러리를 가져와서 거기에 담긴 클래스를 사용할 수 있다. 이런 접근 방법을 사용해 한 장소에 라이브러리를 저장함으로써 코드 중복을 제거할 수 있다.

### 공유 라이브러리의 트레이드오프와 단점 평가하기
- 새로운 라이브러리를 추출하면 이는 독자적인 코딩 스타일, 배포 과정, 코딩 관례를 따르는 새로운 엔티티가 된다.
- 팀이나 개인이 새로운 코드 베이스에 대해 책임져야 하고, 누군가는 개발 과정을 설정하고, 프로젝트 코드 품질을 검증하고, 새로운 기능을 개발하는 등의 작업을 수행해야 한다.
- 가장 큰 트레이드오프는 새로운 라이브러리를 만드는 언어가 클라이언트를 만들기 위해 사용되는 언어와 동일할 필요가 있다는 것인데, 현실 세계에선 이런 서비스들은 동일 언어나 유사 언어 집합을 사용해서 만들어지니 크게 문제되지 않는다.
- 종종 C와 같은 다른 언어로 라이브러리를 작성해서 구현을 위해 선택한 언어의 네이티브 인터페이스 (eg. JNI)로 감싸는 방식도 가능하지만, 우리의 코드가 다른 간접 계층을 요구할 것이기 때문에 문제가 될 수 있다.
  - 네이티브 인터페이스에 감싸인 코드는 OS 사이에서 호환성이 떨어지거나 메서드 호출이 느려질 수 있음.
- 저장소 관리자는 공유 라이브러리를 위한 멋진 장소지만, 이 라이브러리를 위한 문서화를 제대로 유지보수할 필요도 있다.

### 공유 라이브러리 생성
- 라이브러리를 생성할 때 단순함을 추구하기 위해 노력해야 한다. 타사 라이브러리에 의존할 필요가 있을 때 가장 중요한 사안이다.

<img width="849" height="407" alt="Image" src="https://github.com/user-attachments/assets/36416b48-fe05-422b-8c12-a8e484d4efef" />

- 기반 라이브러리의 주 버전이 다르다면 문제가 커진다.
- 이진 호환성이 가능하거나 가능하지 않을 수도 있다는 것을 의미한다.
- 이런 충돌은 해소하기가 어렵고 조직 내 다른 팀이 우리가 추출한 라이브러리를 사용하려는 의욕을 꺾어버릴 수 있다. 따라서 라이브러리는 최대한 직접적인 의존성을 적게 가져가려고 노력해야 한다.

## 독립적인 마이크로서비스로 코드 추출
라이브러리 코드를 import 한다는 행위는 의존성 수준에서 우리의 코드와 라이브러리 사이에 강한 결합이 존재한다는 의미다. 그렇다고 마이크로서비스 아키텍처가 강한 결합을 수반하지 않는다는 뜻은 아니며, 마이크로서비스 역시 요청 형식 등과 같은 API 수준에서 결합도가 높아질 수 있다.

만일 중복된 기능이 독립적인 비즈니스 도메인으로 포착될 수 있다면 HTTP API로 이런 기능을 외부에 공개하는 또 다른 마이크로서비스를 생성하는 방식을 고려할 수 있다.

애플리케이션에 고성능 요구사항이 존재하지 않는다면 추가적인 HTTP 호출은 문제가 되지 않아야 한다. 추가적인 호출 요청은 클러스터 내부나 폐쇄망에서 일어난다 가정하며, 지구 반대편에 있을지도 모르는 인터넷상의 임의 서버에 대한 통신은 고려하지 않는다.

### 독립적인 서비스의 트레이드오프와 단점 살펴보기
- 배포 과정
  - 마이크로서비스는 배포되어 프로세스로 동작해야 한다.
  - 프로세스를 감시하고, 문제가 있거나 실패할 경우 팀이 대응할 필요가 있다.
  - 생성, 모니터링, 경보는 독립적인 마이크로서비스를 추출할 때 고려할 필요가 있는 다른 요소들이다.
- 버전 관리
  - 몇몇 측면에서 라이브러리 버전 관리보다 훨씬 더 수월할 수 있다.
  - 엔드포인트의 사용 현황을 감시하고 있다가 더 이상 사용되지 않을 경우 재빠르게 지원을 중단하는 방식이 훨씬 더 쉽다.
- 자원 소비
  - 토큰 유효성 검증 로직을 독립적인 서비스가 공개한 API 뒤로 숨길 경우, 확자오가 자원 소비는 더 이상 클라이언트 관점에서 직접적인 문제는 아니다. 처리 과정은 특정 마이크로서비스 인스턴스에서 수행된다.
  - 모든 검증은 마이크로서비스로 왕복 트래픽을 요구하기 때문에 클라이언트 코드가 외부 HTTP 호출을 요구한다는 사실에 주목하자.
  - 마이크로서비스 API를 통해 숨겨진 로직이 복잡하지 않다면 HTTP 호출로 인한 추가 비용이 클라이언트 쪽의 로직을 수행하는 비용 보다 훨씬 더 높게 나온다고 판명될 수도 있다.
  - 로직이 훨씬 더 복잡하면 마이크로서비스 작업과 비교해 HTTP 비용은 무시해도 괜찮을 수 있다.
- 성능
  - 추가적인 HTTP 요청을 수행할 때 성능에 미치는 영향을 계산할 필요가 있다.
  - 캐시 기능
  - 로직을 독립적인 마이크로서비스로 추출하면 당신의 서비스에 모든 사용자의 요청을 위한 추가적인 HTTP 호출을 일으킬 필요성을 강제하며, 이는 상당히 큰 문제가 될 수 있다.
  - 응답 대기 시간과 서비스의 SLA에 어느 정도 영향을 미칠지 계산해야 한다.
  - SLA에 따라 99번째 백분위 대기 시간이 n 밀리초보다 작아야 한다면 다른 마이크로서비스에 대한 호출을 추가하면 SLA를 깨뜨릴 수도 있다.
  - 연쇄적인 실패와 의존중인 마이크로서비스의 일시적인 가용성 장애가 생기지 않게 주의할 필요가 있다. 연쇄적인 실패라는 문제점은 마이크로서비스에 국한되지 않으며 호출할 필요가 있는 어떤 외부 시스템에서도 발생할 수 있다.
  - 너무 많은 요청으로 서비스를 압도당하지 않으면서 다운스트림 서비스가 온라인 상태로 돌아오게끔 허용하기 위해 지수 백오프 기법으로 재시도를 구현해야 할 수도 있다.

<img width="694" height="497" alt="Image" src="https://github.com/user-attachments/assets/8e8dc864-de33-4063-b9ce-a4e0101c67f7" />

- 유지보수

## 코드 중복으로 느슨한 결합 향상시키기
표준 추적 요청과 그래프 추적 요청에 대한 요청 처리기 코드 예시.

```kotlin
data class Trace(
  val isTraceEnabled: Boolean,
  val data: String,
)

data class GraphTrace(
  val isTraceEnabled: Boolean,
  val data: Int,
)

class TraceRequestHandler(
  private val bufferSize: Int,
) {
  private var processed = false
  val buffer = mutableListOf<String>()

  val isProcessed: Boolean
      get() = processed

  fun processRequest(trace: Trace) {
      if (!processed && !trace.isTraceEnabled) return
      if (buffer.size < bufferSize) {
          buffer.add(createPayload(trace))
      }
      if (buffer.size == bufferSize) {
          processed = true
      }
  }

  private fun createPayload(trace: Trace) = "${trace.data}-content"
}
```

## 중복을 줄이기 위해 상속을 사용하는 API 설계
```kotlin
abstract class TraceRequest(
  private val isTraceEnabled: Boolean,
) {
  fun isTraceEnabled() = isTraceEnabled
}

class GraphTrace(
  isTraceEnabled: Boolean,
  private val data: Int
) : TraceRequest(isTraceEnabled) {
  fun getData() = data
}

class Trace(
  isTraceEnabled: Boolean,
  private val data: String
) : TraceRequest(isTraceEnabled) {
  fun getData() = data
}
```

```kotlin
abstract class BaseTraceRequestHandler<T : TraceRequest>(
    private val bufferSize: Int,
) {
    private var processed = false
    private val _buffer = mutableListOf<String>()

    val buffer: List<String>
        get() = _buffer
    val isProcessed: Boolean
        get() = processed

    fun processRequest(trace: T) {
        if (!processed && !trace.isTraceEnabled()) return
        if (_buffer.size < bufferSize) {
            _buffer.add(createPayload(trace))
        }
        if (_buffer.size == bufferSize) {
            processed = true
        }
    }

    protected abstract fun createPayload(trace: T): String
}

class TraceRequestHandler(
    bufferSize: Int,
): BaseTraceRequestHandler<Trace>(bufferSize) {
    override fun createPayload(trace: Trace) = "${trace.getData()}-content"
}
```

상속을 사용해 처리기를 상당히 단순화시키고 DRY 원칙을 사용해 중복 코드를 제거했다. 유지보수성이 더 높아졌지만, 그만큼 더 강하게 결합되어 있다.

표준 처리기를 위한 로직은 버퍼를 추적해야 하지만 그래프 추적 처리기는 아니라면 `instanceof` 처리가 필요한데, 이는 무너지기 쉽고 상속 도입의 취지에 반한다. 이런 해법은 부모와 자식 클래스 사이에 강한 결합을 초래한다.

이런 문제를 완화하기 위해 중복된 코드를 다루는 해법으로 돌아갈 수 있다. 하지만 현실 세계에서 이런 결정이 문제가 되는 이유는 우리가 리팩터링해야 하는 컴포넌트들은 훨씬 더 복잡하고 상당히 많은 작업을 수반하기 때문이다.

### 상속과 합성 사이의 트레이드오프 살펴보기
- 각 하위 클래스가 항상 다른 클래스와 다소 구분되는 형태의 잘 정의된 요구사항의 집합을 따른다면 전략 패턴이 잘 맞아 떨어진다.
- 하지만 요구사항이 커지면 상속 대신 합성을 고려하고 싶을 수 있다.

<img width="871" height="309" alt="Image" src="https://github.com/user-attachments/assets/21728095-5016-415c-8772-87f12c425448" />

- 의존성 주입 단계에서 구성하는 방법처럼 처리기 자체의 추상화가 코드의 나머지로부터 충분히 격리된 상태에서 사용된다면 나머지 코드 베이스를 건들지 않고서도 상속 기반의 접근 방법을 합성 기반의 접근 방법으로 전환할 수 있다. (반대로도 가능)

### 내재된 중복과 우연한 중복 살펴보기
공유된 추상화를 만든 다음 이 추상화를 공유하기 위해 여러 곳에 위치한 코드를 손보는 것.
- 일단 추상화를 만들고 나서 여러 차례 사용하면 컴포넌트 사이의 결합도는 높아질 수 있다. 이는 공유된 코드를 독립된 클래스로 분리하는 작업이 만만치 않을 수도 있음을 의미한다.

때때로 중복처럼 보이는 내용이 현재 요구사항에선 동일하게 취급되는 다른 사안이지만 나중에 달라질지도 모르므로 동일하게 취급해선 안 될 수도 있다. 시스템 설계 초기에 이런 두 상황을 구분하기란 어려울 수 있다.

## 요약
- 독자적인 라이브러리를 추출하는 방식으로 코드베이스 사이에서 공통 코드를 공유할 수 있다. 반대로 라이브러리를 통한 코드 재사용은 강한 결합과 부족한 유연성과 같은 다양한 문제를 일으킨다.
- 공통 비즈니스 로직을 독립적인 서비스로 추출하는 작업은 더 복잡한 문제를 위한 올바른 선택일 수 있지만, 높은 유지보수 비용이 들어간다.
- 상속은 코드 중복을 제거하고 자식 클래스 사이에서 공통 코드를 공유할 수 있게 돕는다. 불행하게도 상속을 사용하면 코드의 유연성을 제한하는 많은 트레이드오프가 생긴다.
- 유연성을 제공하고 팀 사이에 조정을 줄이기 때문에 때때로 중복된 코드를 유지하는 방식도 가치가 있다.