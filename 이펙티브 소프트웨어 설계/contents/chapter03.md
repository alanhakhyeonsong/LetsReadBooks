# 3장. 코드에서 신경 써야 할 예외와 오류 처리 패턴
실패 처리는 코드를 생성할 때 처음으로 생각해야 하는 것이다. 코드는 **내결함성**이 있어야 하며, 이는 가능한 범위에서 코드가 문제를 극복해야 함을 의미한다. 예외 처리 방법을 결정하기 전에 문제를 방어해서 명시적인 방식으로 신호를 보내는 API를 설계해야 한다. 하지만 모든 오류의 가능성을 따져 명시적으로 신호를 보낼 경우, 코드를 읽고 유지보수하기가 어려워진다.

## 예외의 계층 구조
<img width="594" height="565" alt="Image" src="https://github.com/user-attachments/assets/2c441ae4-07fb-48ea-b9a2-a0ae7efd96bd" />

- Java에서 모든 예외는 객체다.
- 스택 추적 정보는 예외를 일으킨 구체적인 클래스의 코드에서 특정 행을 식별하는 각 항목의 배열이다.

### 모든 예외를 잡는 방식 대 더 세분화된 오류 처리 방식
```java
public void methodThatThrowsCheckedException() throws FileAlreadyExistsException, InterruptedException

// 확인된 예외 처리
public void shouldCatchAtNormalGranularity() {
  try {
    methodThatThrowsCheckedException();
  } catch (FileAlreadyExistsException e) { // FileAlreadyExistsException을 잡는다.
    logger.error("File already exists: ", e);
  } catch (InterruptedException e) { // 직전 예외와 무관한 다른 예외를 잡는다.
    logger.error("Interrupted", e);
  }
}

// 더 포괄적인 타입으로 확인된 예외 처리
public void shouldCatchAtHigherGranularity() {
  try {
    methodThatThrowsCheckedException();
  } catch (IOException e) { // FileAlreadyExistsException은 IOException을 상속받으므로 여기서 IOException이 해당 예외를 대신 처리한다.
    logger.error("Some IO problem: ", e);
  } catch (InterruptedException e) {
    logger.error("Interrupted", e);
  }
}
```

- 포괄적인 처리의 경우 `FileAlreadyExistsException` 예외가 던져졌다는 정보를 잃어버린다는 한 가지 문제점이 있다.
- 예외 타입을 `Exception`이나 `Throwable`까지 포괄할 수도 있었지만, 초기에 잡을 필요가 없는 예외까지 잠재적으로 잡을 수도 있다. 그 과정에서 현재 처리 내용과 무관하며 더 상위 컴포넌트로 전파돼야하는 잠재적으로 심각한 다른 예외를 잡을 수도 있다.

## 당신이 소유한 코드에서 예외를 처리하기 위한 우수 사례
- 작성한 코드 사이의 통합 지점은 코드의 의도를 명시하는 인터페이스가 돼야 한다.
- API를 설계하고 있다면 호출자가 실패를 어떻게 다룰지 결정하게 예외로 의사소통하는 방식을 명시적으로 고려해야 한다.
- 하지만 내부 로직을 포함하며 외부에 공개되지 않는 컴포넌트와 메서드를 개발할 수도 있다. 이 경우 코드와 관련해 가능한 모든 문제에 대해 명시적으로 밝힐 필요가 없을 수도 있다.

### 공개 API에서 확인된 예외 처리하기
- 확인된 예외에 관해서라면, 우리의 의도를 명백하게 선포하고 던질 수 있는 확인된 예외로 공개 API 메서드를 선언해야 한다.
  - eg. 공개 메서드가 I/O 문제로 실패할 수 있다고 예상하면 공개 API 시그니처에 이 예외를 선언해야 한다.
- API가 예외를 명시적으로 선언하면 코드를 작성할 때 클라이언트가 컴파일 시점에서 예외 처리 전략에 대해 결정해야 하게끔 만들기에 실패 상황은 벌어지지 않는다.
- API가 던지는 몇몇 예외를 선언하는 방식이 지나치게 장황하면 클라이언트 코드 작성이 힘든 경우도 종종 생긴다.

```java
void check() throws IOException, InterruptedException;

public void wrapIntoUnchecked() {
  try {
    check();
  } catch (RuntimeException e) {
    throw e;
  } catch (Exception e) {
    throw new RuntimeException(e);
  }
}
```

- `check()`라는 API를 사용하는 클라이언트가 해당 메서드를 호출할 때는 매번 예외를 처리하기 위해 명시적으로 결정을 내려야 한다.
- 원래 예외를 확인되지 않은 새로운 예외로 감싸는 것이 중요하다.
  - 호출자는 원래 예외를 일으킨 이유에 대한 모든 정보를 얻는다.
- 코드에서 실제 예외를 감추고 확인되지 않은 예외를 전파하는 API 사용은 기대되는 예외를 감추고 API의 내결함성을 떨어뜨린다.
- 클라이언트가 명시적으로 오류를 처리하게 만들고 싶지 않다면 이런 오류를 무시해서 호출자의 스택까지 전파하는 결정을 의식적으로 내릴 필요가 있다. 종종 이는 올바른 해법이 아니다.

### 공개 API에서 확인되지 않은 예외 처리하기
- 오류 처리 지침에 따르면 메서드마다 확인되지 않은 예외를 선언하면 프로그램의 명료성을 떨어뜨린다.
- 하지만 확인되지 않은 예외를 선언하는 방식이 실행 가능한 해결책인 몇몇 상황도 존재한다.
- 메서드의 호출자가 다른 API와 상호작용할 때 예외에 대해 알고 있으면 유용하지만, 이런 예외를 잡을 필요가 없을 때가 있다.
- 코드에서 다른 컴포넌트가 사용하는 메서드를 만들 때 선행 조건과 기대되는 동작 방식을 문서화해야 한다.
- 너무 많은 예외를 선언하면 코드가 장황해지고 불분명해질 수 있다.
- 특정 컴포넌트의 `private` 메서드를 변경해야 한다면 내부 구조를 알아야 한다. 변경 대상 메서드를 검사해 던질 수 있는 예외에 대해 확인해야 한다.
  - 컴포넌트가 공개 API만을 통해 블랙박스 식으로 사용되면 호출자가 컴포넌트의 내부 구조에 대해 알 필요가 없게 만들어야 한다.
  - 이런 `private` 메서드들에 확인되지 않은 예외를 선언하는 방식은 좋은 해법이 될 수 있다.
- API 계약에서 명시적으로 예외를 선언할 때 호출자가 잠재적인 모든 문제에 대응해 방어하고 가능한 예외를 추측하게 강제하지 않아야 한다.

## 예외 처리에서 주의할 안티 패턴
우리가 사용하고 싶은 API가 예외를 선언하면 컴파일 시점에서 이를 처리할 필요가 있다.

종종 기반 코드를 분석해 이런 예외가 어떤 상황에서도 던져질 수 없다는 결론을 내리려는 유혹이 들 때가 있다. 하지만 메서드가 확인되지 않은 예외를 선언하면 이를 메서드 계약으로 취급해야 한다.

```java
// 예외 삼키기
try {
  check();
} catch (Exception e) { // 호출자는 예외가 일어날 수 없다고 확신
  // 예외가 일어나지 않는다고? 이런 가정은 매우 위험하다.
}
```

- 삼켜진 예외는 호출 스택으로 결코 전파되지 않는다.
- 정보를 잃어버려 시스템에서 무언의 실패를 감수해야 하며, 디버깅하기가 무척 까다롭다.

```java
// 스택 추적 출력
try {
  check();
} catch (Exception e) {
  e.printStackTrace();
}
```

- 스택 추적이 기본적으로 표준 출력으로 예외 내용을 출력하기 때문에 위험하다.
  - 이렇게 하는 대신, `FileOutputStream`처럼 표준 출력이 아닌 다른 곳으로 목적지를 정할 수 있다.
  - 표준 출력을 잡거나 전파하지 않는다면 이 정보를 잃어버릴 위험에 직면한다.

예외가 특정 코드 수준에서 처리돼야 하는지를 판단할 필요가 있다. 그럴경우, 예외를 잡을 때 최대한 많은 정보를 추출해야 한다.

```java
try {
  check();
} catch (Exception e) {
  logger.error("Problem when check ", e);
}
```

- 더 높은 수준에서 특정 오류를 처리하기로 결정하면 `check()`를 호출하는 메서드는 이 예외를 잡으려 해서는 안된다.
  - 예외를 잡는 대신, 메서드 시그니처에 선언만 해야 한다.

### 오류가 발생할 경우 자원 닫기
종종 코드는 몇몇 시스템 자원을 소비할 필요가 있는 메서드나 클래스와 상호작용할 필요가 있다.
- 파일 생성 → 파일 시스템 핸들을 열도록 요구
- HTTP 클라이언트 생성 → 가용 포트 풀에서 포트를 할당하는 소켓을 열도록 요구

처리 과정이 문제없이 진행되고 모든 작업이 예상대로 동작하는 경우, 처리 과정이 완료되고 나서 클라이언트를 닫을 필요가 있다.

```java
// HTTP 클라이언트 닫기
CloseableHttpClient client = HttpClients.createDefault(); // 클라이언트를 사용해 처리
try {
  processRequests(client); // 시스템 자원을 할당하는 새로운 클라이언트를 생성
  client.close(); // 처리가 끝난 다음 클라이언트를 닫는다.
} catch (IOException e) {
  logger.error("Problem when closing the client or processing requests", e);
  // close()에 예외가 발생해 실패할 경우 오류를 로그에 기록.
}
```
- `processRequests()` 메소드는 `IOException`을 던질 수도 있다.
- 예외가 코드의 이 지점에서 던져지면 `close()` 메서드는 호출되지 않아 자원 누수 위험에 직면한다.

```java
// 처리 요청에 문제가 생길 경우 HTTP 클라이언트 닫기
CloseableHttpClient client = HttpClients.createDefault();
try {
  processRequests(client); // 처리 요청 문제를 잡는다.
} catch (IOException e) {
  logger.error("Problem when processing requests", e);
}

try {
  client.close(); // processRequests()가 완료된 다음에야 close() 호출
} catch (IOException e) {
  logger.error("Problem when closing the client", e);
}
```
- 이런 코드는 장황하고 오류가 발생하기 쉽다.
  - 동일 `IOException`을 두 번 다룰 필요가 있다는 사실에서 기인
- 처리 과정에서 문제가 있는 경우 `close()`메서드를 호출하는 대비책도 마련해야 함.
- API가 확인되지 않은 예외를 던질 경우 이를 잊어버리기 쉽고 `close()` 메서드를 호출하지 않을 것이므로 자원 누수라는 위험에 직면한다.

```java
// try-with-resource 사용
try (CloseableHttpClient client = HttpClients.createDefault()) { // try-with-resource 문 내에서 HttpClient를 생성
  processRequests(client);
} catch (IOException e) {
  logger.error("Problem when processing requests", e);
  // processRequests()가 던진 예외를 처리
}
```
- 언어가 지원하지 않을 수 있다. `Closable`을 구현한 객체의 생명주기라면 가능함.

```java
// finally 블록을 사용해 자원 닫기
CloseableHttpClient client = HttpClients.createDefault();
try {
  processRequests(client); // 시스템 자원을 할당하는 새로운 클라이언트를 생성
} finally {
  System.out.println("closing");
  client.close();
}
```
- `processRequests()`가 예외를 던지더라도 `finally` 블록의 닫는 로직이 실행될 것.

### 애플리케이션 흐름을 제어하기 위해 예외를 사용하는 안티 패턴
- 객체지향적인 예외 처리를 구현할 때 흔히 `goto` 문처럼 애플리케이션의 흐름을 제어하기 위해 예외를 사용하는 경우가 있다. 이렇게 예외를 남용하는 애플리케이션에서는 호출자에게 로직이 다른 코드 경로를 따라야 한다는 예외를 던지게 된다.
- 메서드마다 타입을 하나만 반환한다는 제약을 극복하기 위해 예외를 사용하려는 경우
  - 호출자가 메서드의 결과에 따라 다른 코드 경로를 수행하기 위해 분기 로직을 구축할 때 문제.

## 타사 라이브러리에서 오는 예외
## 멀티스레드 환경에서 주의할 예외
## Try로 오류를 처리하는 함수형 접근 방식
## 예외 처리 코드의 성능 비교
## 요약
- 여러 객체지향 언어에는 예외와 오류의 계층이 존재한다. 진단 목적으로 예외 계층을 확실하게 이해해야 한다.
- 오류 처리 API를 설계하기 위해 확인된 예외와 확인되지 않은 예외를 선택할 수 있다. 확인된 예외는 이런 API에서 명시적인 부분이며 반드시 처리돼야 하며, 확인되지 않은 예외는 오류 처리 코드에서 암시적인 부분이며 처리될 필요가 없다.
- 공개 API를 위한 예외 처리 로직을 설계할 때 우리가 소유한 코드의 예외 처리 과정에서 확인된 예외와 확인되지 않은 예외를 비교하고 양자의 장단점을 분석해야 한다.
- 오류 처리 API에서 문제에 적절히 대응해야 한다. 종종 기반 코드를 분석해서 어떤 상황에서도 예외가 던져질 수 없다고 결론을 내리고자 하는 유혹이 든다. 오류 처리 로직에서 공통적인 안티패턴을 이해하면 이런 결정을 내릴 때 도움이 된다.
- 타사 라이브러리와 상호작용할 때 오류 처리 전략을 개발해야 한다. 타사 라이브러리와 통합할 때 예외 타입을 외부에 노출하면 결합도를 높이므로 외부 예외를 감쌀 필요성을 이해하는 것이 중요하다.
- 멀티스레드가 수반되는 비동기식 처리 과정에서 오류 처리는 주의 깊게 다뤄야 하며, 그렇지 않을 경우에는 조용한 실패가 발생할 위험이 있다.
- 예외를 던지는 행위는 코드에서 실패를 처리하기 위한 유일한 방법이 아니다. Try 모나드 구성 요소는 성공은 물론이고 실패도 캡슐화한다.
- 다양한 오류 처리 전략을 위한 성능 벤치마크를 사용해 어떤 연산이 가장 비싼지를 판단할 수 있다.