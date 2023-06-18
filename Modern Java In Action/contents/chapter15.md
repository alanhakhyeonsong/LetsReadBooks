# Chapter 15 - CompletableFuture와 리액티브 프로그래밍 컨셉의 기초
책을 기준으로 최근 소프트웨어 개발 방법을 획기적으로 뒤집는 두 가지 추세가 있다.

- 애플리케이션을 실행하는 하드웨어 관련  
  멀티코어 프로세서가 발전하면서 애플리케이션의 속도는 멀티코어 프로세서를 얼마나 잘 활용할 수 있도록 소프트웨어를 개발하는가에 따라 달라질 수 있다.
- 애플리케이션을 어떻게 구성하는가 관련  
  MSA 선택이 증가하게되며 독립적으로만 동작하는 웹사이트가 아닌 다양한 소스의 콘텐츠를 가져와 합치는 메시업 형태를 띄게된다. 이를 위해 여러 웹 서비스에 접근해야 하는 동시에 서비스의 응답을 기다리는 동안 연산이 블록되거나 귀중한 CPU 클록 사이클 자원을 낭비하지 말아야 한다. 특히 스레드를 블록함으로 연산 자원을 낭비하는 일은 피해야 한다.

Java는 이런 환경에서 사용할 수 있는 두 가지 주요 도구를 제공한다. `Future` 인터페이스로 Java 8의 `CompletableFuture` 구현과 Java 9에 추가된 발행 구독 프로토콜에 기반한 리액티브 프로그래밍 개념을 따르는 플로 API이다.

## 동시성을 구현하는 자바 지원의 진화
- 초기 Java: `Runnable`과 `Thread`를 동기화된 클래스와 메서드를 이용해 잠갔다.
- Java 5: 스레드 실행과 태스크 제출을 분리하는 `ExecutorService` 인터페이스, `Runnable`과 `Thread`의 변형을 반환하는 `Callable<T>`, `Future<T>`, 제네릭 지원
- Java 7: 분할 정복 알고리즘의 포크/조인 구현을 지원하는 `java.util.concurrent.RecursiveTask`
- Java 8: 스트림과 새로 추가된 람다 지원에 기반한 병렬 프로세싱, `Future`를 조합하는 기능을 추가하면서 동시성을 강화한 `CompletableFuture`
- Java 9: 분산 비동기 프로그래밍을 명시적으로 지원. 리액티브 프로그래밍을 위한 `Flow` 인터페이스 추가

### 스레드와 높은 수준의 추상화
병렬 스트림 반복은 명시적으로 스레드를 사용하는 것에 비해 높은 수준의 개념이다. 다시 말해 스트림을 이용해 스레드 사용 패턴을 추상화할 수 있다. 스트림으로 추상화하는 것은 디자인 패턴을 적용하는 것과 비슷하지만 대신 쓸모 없는 코드가 라이브러리 내부로 구현되면서 복잡성도 줄어든다는 장점이 더해진다.

### Executor와 스레드 풀
Java 5는 `Executor` 프레임워크와 스레드 풀을 통해 스레드의 힘을 높은 수준으로 끌어올리는 즉 자바 프로그래머가 태스크 제출과 실행을 분리할 수 있는 기능을 제공했다.

*스레드의 문제*  
Java 스레드는 직접 운영체제 스레드에 접근한다. 운영체제 스레드를 만들고 종료하려면 비싼 비용(페이지 테이블과 관련한 상호작용)을 치러야 하며 더욱이 운영체제 스레드의 숫자는 제한되어 있는 것이 문제다. 운영체제가 지원하는 스레드 수를 초과해 사용하면 Java 애플리케이션이 예상치 못한 방식으로 크래시될 수 있으므로 기존 스레드가 실행되는 상태에서 계속 스레드를 만드는 상황이 일어나지 않도록 주의해야 한다.

보통 운영체제와 자바의 스레드 수가 하드웨어 스레드 개수보다 많으므로 일부 운영체제 스레드가 블록되거나 자고 있는 상황에서 모든 하드웨어 스레드가 코드를 실행하도록 할당된 상황에 놓을 수 있다. 프로그램에서 사용할 최적의 Java 스레드 개수는 사용할 수 있는 하드웨어 코어의 개수에 따라 달라진다.

*스레드 풀 그리고 스레드 풀이 더 좋은 이유*  
스레드 풀은 일정한 수의 워커 스레드를 가지고 있다. 스레드 풀에서 사용하지 않은 스레드로 제출된 태스크를 먼저 온 순서대로 실행한다. 이들 태스크 실행이 종료되면 이들 스레드를 풀로 반환한다. 이 방식의 장점은 하드웨어에 맞는 수의 태스크를 유지함과 동시에 수 천개의 태스크를 스레드 풀에 아무 오버헤드 없이 제출할 수 있다는 점이다.

*스레드 풀 그리고 스레드 풀이 나쁜 이유*  
거의 모든 관점에서 스레드를 직접 사용하는 것보다 스레드 풀을 이용하는 것이 바람직하지만 두 가지 사항을 주의해야 한다.

- k 스레드를 가진 스레드 풀은 오직 k만큼의 스레드를 동시에 실행할 수 있다. 이 때 잠을 자거나 I/O를 기다리거나 네트워크 연결을 기다리는 태스크가 있다면 주의해야 한다. 이런 상황에서 스레드는 블록되며, 블록 상황에서 태스크가 워커 스레드에 할당된 상태를 유지하지만 아무 작업도 하지 않게 된다. 핵심은 **블록할 수 있는 태스크는 스레드 풀에 제출하지 말아야 한다는 것이지만 항상 이를 지킬 수 있는 것은 아니다.**
- 프로그램을 종료하기 전에 모든 스레드 풀을 종료하자. Java는 이런 상황을 위해 `Thread.setDaemon` 메서드를 제공한다.

### 스레드의 다른 추상화 : 중첩되지 않은 메서드 호출
엄격한 포크/조인 방식이 아닌 비동기 메서드로 여유로운 포크/조인을 사용할 수 있다.

- 엄격한 포크/조인: 스레드 생성과 `join()`이 한 쌍처럼 중첩된 메서드 호출 방식
- 여유로운 포크/조인: 시작된 태스크를 내부 호출이 아니라 외부 호출에서 종료하도록 기다리는 방식
  - 스레드 실행은 메서드를 호출한 다음의 코드와 동시에 실행되므로 데이터 경쟁 문제를 일으키지 않도록 주의해야 한다.
  - 기존 실행 중이던 스레드가 종료되지 않은 상황에서 Java의 `main()` 메서드가 반환할 때 스레드의 행동
    - 애플리케이션을 종료하지 못하고 모든 스레드가 실행을 끝날 때까지 기다린다.
    - 애플리케이션 종료를 방해하는 스레드를 강제종료 시키고 애플리케이션을 종료한다.

애플리케이션에서 만든 모든 스레드를 추적하고 애플리케이션을 종료하기 전에 스레드 풀을 포함한 모든 스레드를 종료하는 것이 좋다.

## 동기 API와 비동기 API
```java
// 동기 API
int f(int x);
int g(int x);

int y = f(x);
int z = g(x);
System.out.println(y + z);
```

```java
// 별도의 스레드로 실행
class ThreadExample {
    public static void main(String[] args) throws InterruptedException {
        int x = 1337;
        Result result = new Result();

        Thread t1 = new Thread(() -> { result.left = f(x); });
        Thread t2 = new Thread(() -> { result.right = g(x); });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(result.left + result.right);
    }

    private static class Result {
        private int left;
        private int right;
    }
}
```

```java
// Runnable 대신 Future API로 단순화
public class ExecutorServiceExample {
    public static void main(String[] args) throws ExecutionException, InterrutedException {
        int x = 1337;

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<Integer> y = executorService.submit(() -> f(x));
        Future<Integer> z = executorService.submit(() -> g(x));
        System.out.println(y.get() + z.get());

        executorService.shutdown();
    }
}
```

- `Future` 형식 API: Java 5에서 소개된 `Future`를 이용한다. 일회성 값을 처리하는데 적합하다.
- 리액티브 형식 API: 콜백 형식으로 일련의 값을 처리하는데 적합하다.

```java
void f(int x, IntConsumer dealWithResult);

public class CallbackStyleExample {
    public static void main(String[] args) {
        int x = 1337;
        Result result = new Result();

        f(x, (int y) -> {
            result.left = y;
            System.out.println((result.left + result.right));
        });

        g(x, (int z) -> {
            result.right = z;
            System.out.println((result.left + result.right));
        });
    }
}
```

위 코드는 호출 합계를 정확하게 출력하지 않고 상황에 따라 먼저 계산된 결과를 출력한다. 락을 사용하지 않으므로 값을 두 번 출력할 수 있을 뿐더러 때로는 +에 제공된 두 피연산자가 `println`이 호출되기 전에 업데이트 될 수도 있다. 다음 두 가지 방법으로 이 문제를 보완할 수 있다.
- `if-then-else`를 이용해 적절한 락을 이용해 두 콜백이 모두 호출되었는지 확인한 다음 `println`을 호출해 원하는 기능을 수행할 수 있다.
- 리액티브 형식의 API는 보통 한 결과가 아니라 일련의 이벤트에 반응하도록 설계되었으므로 `Future`를 이용하는 것이 더 적절하다.

### 잠자기(그리고 기타 블로킹 동작)는 해로운 것으로 간주
스레드는 잠들어도 여전히 시스템 자원을 점유한다. 스레드 풀에서 잠을 자는 태스크는 다른 태스크가 시작되지 못하게 막으므로 자원을 소비한다는 사실을 기억하자. (OS가 이들 태스크를 관리하므로 일단 스레드로 할당된 태스크는 중지시키지 못한다.)  
모든 블록 동작도 마찬가지다. 이상적으로는 절대 태스크에서 기다리는 일을 만들지 말거나 아니면 코드에서 예외를 일으키는 방법으로 이런 상황을 방지할 수 있다.

### 비동기 API에서 예외는 어떻게 처리하는가?
`Future`나 리액티브 형식의 비동기 API에서 호출된 메서드의 실제 바디는 별도의 스레드에서 호출되며 이때 발생하는 어떤 에러는 이미 호출자의 실행 범위와는 관계가 없는 상황이 된다.

Java 9 플로 API에선 여러 콜백을 한 객체로 감싼다. `Subscriber` 클래스를 이용하며, 그렇지 않는 경우 예외가 발생했을 때 실행될 추가 콜백을 만들어 인터페이스를 구현해야 한다.

## 박스와 채널 모델
박스와 채널 모델은 동시성 모델을 설계하고 개념화하기 위한 모델을 말한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c9051379-50c4-4194-b0a9-333f2d6c712d)

```java
int t = p(x);
System.out.println( r(q1(t), q2(t)) );
```

박스와 채널 모델을 이용해 생각과 코드를 구조화할 수 있다. 대규모 시스템 구현의 추상화 수준을 높일 수 있다. 박스로 원하는 연산을 표현하면 계산을 손으로 코딩한 결과보다 더 효율적일 것이다. 박스와 채널 모델은 병렬성을 직접 프로그래밍하는 관점을 콤비네이터를 이용해 내부적으로 작업을 처리하는 관점으로 바꿔준다.

## CompletableFuture와 콤비네이터를 이용한 동시성
동시 코딩 작업을 `Future` 인터페이스로 생각하도록 유도한다는 점이 `Future` 인터페이스의 문제다. Java 8에선 `Future` 인터페이스의 구현인 `CompletableFuture`를 이용해 `Future`를 조합할 수 있는 기능을 추가했다.

일반적으로 `Future`는 실행해서 `get()`으로 결과를 얻을 수 있는 `Callable`로 만들어진다. 하지만 `CompletableFuture`는 실행할 코드 없이 `Future`를 만들 수 있도록 허용하며 `complete()` 메서드를 이용해 나중에 어떤 값을 이용해 다른 스레드가 이를 완료할 수 있고 `get()`으로 값을 얻을 수 있도록 허용한다.

```java
public class CFComplete {
    public static void main(String[] args)
        throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int x = 1337;

        CompletableFuture<Integer> a = new CompletableFuture<>();
        executorService.submit(() -> a.complete(f(x)));
        int b = g(x);
        System.out.println(a.get() + b);

        executorService.shutdown();
    }
}
```

콤비네이터를 이용해 `get()`에서 블록하지 않을 수 있고 그렇게 함으로 병렬 실행의 효율성은 높이고 데드락은 피하는 최상의 해결책을 구현할 수 있다.

```java
CompletableFuture<V> thenCombine(CompletableFuture<U> other, BiFunction<T, U, V> fn);

public class CFCombine {
    public static void main(String[] args)
        throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int x = 1337;

        CompletableFuture<Integer> a = new CompletableFuture<>();
        CompletableFuture<Integer> b = new CompletableFuture<>();
        CompletableFuture<Integer> c = a.thenCombine(b, (y, z) -> y + z);
        executorService.submit(() -> a.complete(f(x)));
        executorService.submit(() -> b.complete(g(x)));
        
        System.out.println(c.get());
        executorService.shutdown();
    }
}
```

## 발행-구독 그리고 리액티브 프로그래밍
리액티브 프로그래밍은 `Future` 같은 객체를 통해 한 번의 결과가 아니라 여러 번의 결과를 제공하는 모델이다. 또한 가장 최근의 결과에 대해 반응(react)하는 부분이 존재한다.

Java 9에선 `java.util.concurrent.Flow`의 인터페이스에 발행-구독 모델을 적용해 리액티브 프로그래밍을 제공한다.

Java 9 플로 API는 다음처럼 세 가지로 간단하게 정리할 수 있다.

- **구독자**가 구독할 수 있는 **발행자**
- 이 연결을 **구독**(subscription)이라 한다.
- 이 연결을 이용해 **메시지**(또는 **이벤트**로 알려짐)를 전송한다.

### 발행-구독 모델에서의 컨테이너
- 여러 컴포넌트가 한 구독자로 구독할 수 있다.
- 한 컴포넌트는 여러 개별 스트림을 발행할 수 있다.
- 한 컴포넌트는 여러 구독자에 가입할 수 있다.

### Publisher
`Publisher`는 발행자이며 `subscribe`를 통해 구독자를 등록한다.

```java
interface Publisher<T> {
    void subscribe(Subscriber<? super T> subscriber);
}
```

### Subscriber
`Subscriber`는 구독자이며, `onNext()`라는 정보를 전달할 단순 메서드를 포함하며, 구현자가 필요한대로 이 메서드를 구현할 수 있다.

```java
interface Subscriber<T> {
    void onNext(T t);
    void onError(Throwable t);
    void onComplete();
    void onSubscribe(Subscription s);
}
```

## 역압력
'발행자가 매우 빠른 속도로 이벤트를 발행한다면 구독자가 아무 문제 없이 처리할 수 있을까?'에서 역압력의 개념이 출발된다. 발행자가 구독자의 `onNext`를 호출하여 이벤트를 전달하는 것을 **압력**이라 부른다. 이런 상황에선 수신할 이벤트의 숫자를 제한하는 역압력기법이 필요하다. Java 9 플로 API에선 발행자가 무한의 속도로 아이템을 방출하는 대신 요청했을 때만 다음 아이템을 보내도록 하는 `request()` 메서드를 제공한다.

`Publisher`와 `Subscriber` 사이에 채널이 연결되면 첫 이벤트로 `Subscriber.onSubscribe(Subscription subscription)` 메서드가 호출된다. `Subscription` 객체는 다음처럼 `Subscriber`와 `Publisher`와 통신할 수 있는 메서드를 포함한다.

```java
interface Subscription {
    void cancel();
    void request(long n);
}
```

`Publiser`는 `Subscription` 객체를 만들어 `Subscriber`로 전달하면 `Subscriber`는 이를 이용해 `Publisher`로 정보를 보낼 수 있다.

### 실제 역압력의 간단한 형태
한 번에 한 개의 이벤트를 처리하도록 발행-구독 연결을 구성하려면 다음과 같은 작업이 필요하다.

- `Subscriber`가 `OnSubscribe`로 전달된 `Subscription` 객체를 필드로 저장
- `Subscriber`가 수많은 이벤트를 받지 않도록 `onSubscribe`, `onNext`, `onError`의 마지막 동작에 `channel.request(1)`을 추가해 오직 한 이벤트만 요청한다.
- 요청을 보낸 채널에만 `onNext`, `onError` 이벤트를 보내도록 `Publisher`의 `notifyAllSubscribers` 코드를 바꾼다.
- 보통 여러 `Subscriber`가 자신만의 속도를 유지할 수 있도록 `Publisher`는 새 `Subscription`을 만들어 각 `Subscriber`와 연결한다.

역압력을 구현하려면 여러가지 장단점을 생각해야 한다.

- 여러 `Subscriber`가 있을 때 이벤트를 가장 느린 속도로 보낼 것인가? 아니면 각 `Subscriber`에게 보내지 않은 데이터를 저장할 별도의 큐를 가질 것인가?
- 큐가 너무 커지면 어떻게 해야할까?
- `Subscriber`가 준비가 안 되었다면 큐의 데이터를 폐기할 것인가?

이런 결정은 데이터의 성격에 따라 달라진다.

## 리액티브 시스템 vs 리액티브 프로그래밍
### 리액티브 시스템
- 런타임 환경이 변화에 대응하도록 전체 아키텍처가 설계된 프로그램.
- 반응성(responsive), 회복성(resilient), 탄력성(elastic)으로 세 가지 속성을 가진다.
- 반응성은 리액티브 시스템이 큰 작업을 처리하느라 간단한 질의의 응답을 지연하지 않고 실시간으로 입력에 반응하는 것이다.
- 회복성은 한 컴포넌트의 실패로 전체 시스템이 실패하지 않음을 의미한다.
- 탄력성은 시스템이 자신의 작업 부하에 맞게 적응하며 작업을 효율적으로 처리함을 의미한다.

### 리액티브 프로그래밍
- 리액티브 시스템이 가지는 속성을 구현하기 위한 프로그래밍 형식을 의미한다.
- `java.util.concurrent.Flow` 관련된 자바 인터페이스에서 제공하는 리액티브 프로그래밍 형식.
- 이들 인터페이스 설계는 메시지 주도(message-driven) 속성을 반영한다.

정리하자면 리액티브 시스템은 전체적인 리액티브 환경 아키텍처를 의미하며 리액티브 프로그래밍은 리액티브 환경을 위해 사용하는 프로그래밍 기법이다. 리액티브 프로그래밍을 이용해 리액티브 시스템을 구현할 수 있다.

## 📌 정리
- Java의 동시성 지원은 계속 진화해 왔으며 앞으로도 그럴 것이다. 스레드 풀은 보통 유용하지만 블록되는 태스크가 많아지면 문제가 발생한다.
- 메서드를 비동기로 만들면 병렬성을 추가할 수 있으며 부수적으로 루프를 최적화한다.
- 박스와 채널 모델을 이용해 비동기 시스템을 시각화할 수 있다.
- Java 8 `CompletableFuture` 클래스와 Java 9 플로 API 모두 박스와 채널 다이어그램으로 표현할 수 있다.
- `CompletableFuture` 클래슨느 한 번의 비동기 연산을 표현한다. 콤비네이터로 비동기 연산을 조합함으로 `Future`를 이용할 때 발생했던 기존의 블로킹 문제를 해결할 수 있다.
- 플로 API는 발행-구독 프로토콜, 역압력을 이용하면 Java의 리액티브 프로그래밍의 기초를 제공한다.
- 리액티브 프로그래밍을 이용해 리액티브 시스템을 구현할 수 있다.