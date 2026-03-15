# 2장. 프로그램이 실행되었지만, 뭐가 뭔지 하나도 모르겠다
- 범위: 2.1 ~ 2.9 (프로세스, 스레드, 코루틴, 동기/비동기, 이벤트 루프)
- 공통: 스레드/프로세스 차이, 동기/비동기/논블로킹 핵심
- FE: JS 이벤트 루프, 콜백 지옥, async/await → 책의 코루틴/비동기 부분 집중 (2.4~2.8)
- BE: 스레드 풀, 동시성 제어, 스레드 안전 패턴 (2.3, 2.7, 2.8)
- DevOps: 서버 아키텍처 (다중 스레드, 이벤트 루프), 고성능 서버 모델 (2.8)

---

## 2.1 운영 체제, 프로세스, 스레드의 근본 이해하기

### CPU와 프로그램 실행의 기초
- CPU는 스레드, 프로세스, 운영 체제와 같은 개념을 전혀 알지 못하며, 단지 메모리에서 명령어를 가져와 실행하는 행위만 반복한다.
- 다음에 실행할 명령어 주소는 PC(프로그램 카운터) 레지스터에 저장된다.
- 프로그램의 시작 지점인 main 함수에 해당하는 첫 번째 기계 명령어 주소가 PC 레지스터에 기록됨으로써 프로그램 실행이 시작된다.

### 프로세스의 탄생과 멀티태스킹
- 운영 체제가 없다면 프로그램 실행은 매우 복잡하고 번거로우며, 한 번에 하나의 프로그램만 실행할 수 있다.
- 여러 프로그램이 동시에 실행되는 것처럼 보이게 하려면(멀티태스킹) CPU가 프로그램 실행을 빠르게 전환해야 한다. 이때 프로그램 실행 상태를 저장하고 복구하는 것이 중요하며, 이 상태를 **상황 정보(context)** 라고 한다.
- 이 상황 정보를 관리하는 구조체에 **프로세스(process)** 라는 이름이 붙었으며, 프로세스는 실행을 일시 중지하고 재개할 수 있도록 한다.
- 운영 체제는 이 프로세스를 관리하고 스케줄링함으로써 프로그래머에게 CPU와 표준 크기의 메모리를 독점하고 있다는 착각을 제공한다.
- 프로세스 실행 시 메모리 내 구조를 **프로세스 주소 공간(process address space)** 이라고 하며, 이는 코드 영역, 데이터 영역, 힙 영역, 스택 영역으로 구성된다.

### 스레드의 탄생과 필요성
- 다중 프로세스는 병렬 실행을 가능하게 하지만, 프로세스 생성 부담이 크고 프로세스 간 통신(IPC)이 복잡하다는 단점이 있다.
- 이 문제를 해결하기 위해 **스레드(thread)** 가 등장했다.
- 스레드는 동일한 프로세스 주소 공간을 공유하는 여러 실행 흐름을 의미하며, 이는 하나의 프로세스에 속한 명령어를 여러 CPU에서 동시에 실행할 수 있게 한다. 스레드 덕분에 프로세스 간 통신이 필요 없어졌다.
- 모든 스레드는 함수 실행 시 정보를 저장하는 자신만의 스택 영역을 가지며, 스레드를 생성하면 이 스택 영역을 위해 프로세스의 메모리 공간이 소모된다.
- 스레드 풀(thread pool)은 스레드를 재사용하여 짧은 작업(short task)을 처리하는 데 사용되며, 작업은 처리할 데이터와 함수로 구성된다.

## 2.2 스레드 간 공유되는 프로세스 리소스
운영 체제에서 프로세스는 리소스를 할당하는 기본 단위이고, 스레드는 스케줄링의 기본 단위이며, 프로세스 리소스는 스레드 간에 공유된다.

### 스레드 전용 리소스 (Thread-Private Resources)
스레드 전용 리소스는 다른 스레드가 직접 접근할 수 없는 자원을 의미한다.

1. **스택 영역:** 각 스레드는 독점적으로 사용하는 스택 영역을 가진다. 이 스택 영역에는 함수의 매개변수, 지역 변수, 반환 주소 등 실행 시간 정보가 저장된다.
2. **CPU 레지스터:** PC 레지스터, 스택 포인터 등 CPU 내부 레지스터 값도 스레드 전용이며, 이 모든 정보를 **스레드 상황 정보(thread context)** 라고 한다.
3. **스레드 전용 저장소 (TLS):** `__thread` 같은 수식어가 붙은 변수는 모든 스레드에서 접근할 수 있지만, 각 스레드가 변수의 인스턴스(복사본)를 가지므로 한 스레드의 변경이 다른 스레드에 영향을 미치지 않는다.

### 스레드 공유 리소스 (Thread-Shared Resources)
스레드 전용 리소스를 제외한 프로세스 주소 공간의 나머지 영역은 스레드 간에 공유된다.

1. **코드 영역:** 기계 명령어가 저장되며, 읽기 전용이므로 스레드 안전 문제가 발생하지 않는다.
2. **데이터 영역:** 전역 변수가 저장되는 곳이며, 모든 스레드가 이 전역 변수에 접근 가능하다.
3. **힙 영역:** 동적으로 할당된 메모리(malloc, new)가 위치하며, 포인터만 있다면 모든 스레드가 접근할 수 있다.
4. **스택 영역의 예외:** 비록 스택 영역은 개념상 전용이지만, 실제 구현상 격리되어 있지 않아 한 스레드가 다른 스레드의 스택 영역에 속한 변수를 포인터를 통해 읽거나 쓸 수 있다.
5. **동적 링크 라이브러리 및 파일:** 동적 라이브러리의 코드/데이터 영역 및 프로세스가 연 파일 정보는 모든 스레드에서 공유된다.

## 2.3 스레드 안전 코드는 도대체 어떻게 작성해야 할까?

### 스레드 안전의 핵심
스레드 안전(thread safety)을 달성하려면 스레드 전용 리소스와 스레드 공유 리소스를 구분하는 것이 핵심이다.
- 전용 리소스를 사용하는 스레드는 스레드 안전을 달성한다.
- 공유 리소스를 사용하는 스레드는 **상호 배제(mutex)** 나 **잠금(lock)** 과 같은 제약 조건을 사용하여 다른 스레드의 접근 순서를 방해하지 않아야 한다. 공유 리소스는 주로 힙 영역과 데이터 영역으로 구성된다.

### 스레드 안전 코드 작성 사례
1. **지역 변수 및 값 전달 매개변수:** 함수의 지역 변수나 값으로 전달된 매개변수는 스레드 전용 스택 영역에 저장되므로 항상 스레드 안전하다.
2. **전역 변수:** 전역 변수가 초기화 후 읽기만 가능하다면 스레드 안전하다. 하지만 쓰기 작업이 포함될 경우, 반드시 잠금이나 원자성(atomic) 작업으로 보호해야 한다.
3. **포인터 매개변수:** 포인터가 데이터 영역의 전역 변수나 힙 영역의 공유 데이터를 가리킨다면 스레드 안전을 보장할 수 없으므로 주의해야 한다.
4. **함수 반환값:** 값을 반환하는 경우는 안전하지만, 정적 지역 변수(static local variable)처럼 공유 리소스를 가리키는 포인터를 반환하면 해당 변수를 획득한 다른 스레드가 수정할 위험이 있으므로 스레드 안전이 아니다.

```kotlin
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object ThreadBasics {
    @JvmStatic
    fun main(args: Array<String>) {
        var unsafe = 0                       // 공유 가변 상태 (위험)
        val atomic = AtomicInteger(0)        // 안전(원자성)
        val lock = Any()
        var locked = 0

        val pool = Executors.newFixedThreadPool(8)
        val tls = ThreadLocal.withInitial { "init" }

        repeat(8) {
            pool.submit {
                // 지역 변수: 안전
                var local = 0
                local += 1

                // 비원자: 위험
                repeat(10_000) { unsafe++ }

                // 원자: 안전
                repeat(10_000) { atomic.incrementAndGet() }

                // 락 보호: 안전
                repeat(10_000) {
                    synchronized(lock) { locked++ }
                }

                // TLS: 스레드마다 독립
                val before = tls.get()
                tls.set(Thread.currentThread().name)
                println("[TLS-K] $before -> ${tls.get()}")
            }
        }

        pool.shutdown()
        pool.awaitTermination(5, TimeUnit.SECONDS)
        println("unsafe   (race)   = $unsafe")
        println("atomic   (safe)   = ${atomic.get()}")
        println("locked   (safe)   = $locked")
    }
}
```

## 2.4 프로그래머는 코루틴을 어떻게 이해해야 할까?

### 코루틴의 정의 및 특징
- 일반 함수는 실행이 완료되어야 반환되며 재호출 시 처음부터 시작하지만, **코루틴(coroutine)** 은 실행 중 자신의 이전 실행 상태를 기억하고 있다가, **일시 중지(yield)** 할 수 있으며 재개될 때 중지 지점부터 계속 실행이 가능한 함수이다. 일반 함수는 코루틴의 특별한 예에 불과하다.

### 코루틴의 구현과 효율성
- 일반 함수의 스택 프레임은 프로세스 스택 영역에 저장되지만, 코루틴의 실행 시간 스택 프레임 정보는 힙 영역에 메모리를 요청하여 저장된다.
- 코루틴은 스레드와 달리 커널 상태가 아닌 사용자 상태에서 전환(스케줄링)이 일어나며, 저장 및 복구되는 정보가 가볍기 때문에 효율성이 훨씬 높다. 코루틴은 본질적으로 스레드에 할당된 CPU 시간을 사용자 상태에서 재차 할당하는 것에 해당한다.
- 코루틴의 중요한 역할은 프로그래머가 동기 방식으로 비동기 프로그래밍을 가능하게 한다는 것이다.

### 동기 방식 같은 코루틴 예시 — `launch` / `async` / `await` / `withContext`

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    // 1) launch: Job 반환 (결과 없음)
    val job = launch(Dispatchers.Default) {
        println("[launch] on " + Thread.currentThread().name)
        delay(100)
        println("[launch] done")
    }

    // 2) async: Deferred<T> 반환 (결과 있음)
    val deferred = async(Dispatchers.Default) {
        println("[async] on " + Thread.currentThread().name)
        delay(50)
        21 + 21
    }

    // 3) withContext: 컨텍스트 전환
    val ioResult = withContext(Dispatchers.IO) {
        println("[IO] on " + Thread.currentThread().name)
        "read-file-result"
    }

    job.join()
    println("async result = ${deferred.await()}, ioResult = $ioResult")
}
```

### Race Condition — 코루틴도 공유 가변 상태는 위험하다

```kotlin
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun main() = runBlocking {
    var unsafe = 0                    // 위험
    val atomic = AtomicInteger(0)     // 안전
    val mutex = Mutex()
    var locked = 0

    // 1000개 코루틴이 동시에 증가 시도
    coroutineScope {
        repeat(1_000) {
            launch(Dispatchers.Default) {
                // 비원자 증가 — 레이스
                unsafe++

                // 원자 증가 — 안전
                atomic.incrementAndGet()

                // 뮤텍스로 보호 — 안전
                mutex.withLock {
                    locked++
                }
            }
        }
    }

    println("unsafe (race)   = $unsafe")
    println("atomic (safe)   = ${atomic.get()}")
    println("locked (safe)   = $locked")
}
```

## 2.5 콜백 함수 (Callback Function)
- 콜백은 "코드를 값으로 전달하여 나중에 실행하도록 하는 메커니즘"이다.
- 공통 로직은 재사용하면서도 동작을 외부에서 "주입"할 수 있어 유연한 설계를 가능하게 한다.
- 공통 로직(예: 파일 읽기, 공통 API 호출)을 재사용하면서, 동작의 일부는 사용자가 정의할 수 있게 하기 위해 필요하다.

### 동기 vs 비동기 콜백

| 유형 | 설명 |
|------|------|
| 동기 콜백 | 호출한 함수가 끝나기 전에 콜백이 실행됨 |
| 비동기 콜백 | 호출 즉시 반환되고, 일정 시간이 지난 뒤 콜백 실행. 종종 다른 스레드에서 동시 실행 |

### 비동기 콜백의 문제
- 로직이 중첩될수록 가독성이 급격히 떨어진다. (콜백 지옥)

### 코드 예시 — `HttpClient` (Java 11)

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://example.com"))
        .build();

client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenAccept(response -> {
            System.out.println("응답: " + response.body()); // 비동기 콜백
        })
        .exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
```

콜백은 유연하나 많은 비즈니스 로직이 이어지면 유지보수가 어려워진다.

## 2.6 동기와 비동기 (Synchronization vs Asynchronization)
- **동기(Synchronous):** 호출자가 작업이 끝날 때까지 기다림. 흐름이 직관적이지만 시간이 오래 걸릴 수 있다.
- **비동기(Asynchronous):** 호출자가 기다리지 않고 바로 다음 작업을 수행. I/O 대기 시간이 많은 서버 환경에서 효율을 극대화한다.
- **비동기 작업 완료 감지 방식:**
  - 콜백 기반
  - 알림 기반(이벤트, Signal, 메시지 큐 등)

### 코드 예시 — Spring `@Async`

```java
@Async
public CompletableFuture<String> doAsyncJob() {
    Thread.sleep(1000);
    return CompletableFuture.completedFuture("완료");
}

// 호출 측
var result = doAsyncJob();  // 기다리지 않음
```

## 2.7 블로킹과 논블로킹 (Blocking vs Non-blocking)

| 개념 | 의미 |
|------|------|
| 블로킹 | 호출 스레드가 I/O 작업을 기다리며 OS가 스레드를 sleep 처리 |
| 논블로킹 | 즉시 반환. 스레드가 중단되지 않음 |

**반드시 기억하자. 논블로킹 ≠ 비동기**

- 논블로킹 호출 후 루프를 돌며 결과를 기다리면 **동기 논블로킹**이다.

### 코드 예시 — Java NIO SocketChannel (논블로킹 + 동기 폴링)

```java
SocketChannel channel = SocketChannel.open();
channel.configureBlocking(false);

ByteBuffer buf = ByteBuffer.allocate(1024);
int bytes = 0;

while (bytes == 0) {
    bytes = channel.read(buf); // 논블로킹이지만 동기적 폴링
}
```

## 2.8 높은 동시성과 고성능 서버 설계

### 전통적 모델: 요청당 프로세스 / 요청당 스레드
- 생성 비용이 크다.
- 문맥 전환 비용이 증가한다.
- 스레드 안전 문제가 증가한다.

### 이벤트 기반 프로그래밍 (Event-Driven Programming)

**1) 이벤트 루프 (Event Loop)** — 반복문 + 이벤트 디스패치 구조

```kotlin
while (true) {
    val event = pollEvent()
    handle(event)
}
```

**2) I/O Multiplexing**

`epoll`, `select`, `kqueue` 등이 많은 파일 디스크립터를 감시하며 하나의 스레드로 수천 개의 소켓을 처리할 수 있게 한다.

**3) Reactor Pattern**
- 이벤트 루프는 **빠른 작업**(연결 수락, 간단 검증)을 처리한다.
- **시간이 오래 걸리는 작업은 스레드 풀로 위임**한다.
- Spring WebFlux 내부 구조가 대표적 구현이다.

### 코드 예시 — Spring WebFlux (논블로킹 + 비동기)

```kotlin
@GetMapping("/users/{id}")
fun getUser(@PathVariable id: Long): Mono<User> {
    return userService.findUser(id)  // DB I/O 동안 스레드 블로킹 없음
}
```

Netty의 event loop + Reactor 패턴을 활용한 **논블로킹 + 비동기** 예시에 해당한다.

### 코루틴의 역할
코루틴은 다음 문제를 해결한다.
- 비동기 프로그래밍의 복잡한 콜백 지옥
- 블로킹을 유발하지 않는 API 필요
- 동기처럼 쓰고 싶지만 효율은 유지해야 함

코루틴은 사용자 모드에서 스레드 실행 시간을 스스로 양보(yield)한다.

```kotlin
suspend fun fetchUser(id: Long): User {
    val response = http.get("https://api/users/$id")
    return response.body()
}
```

동기 코드처럼 보이나, 실제로는 **suspend, yield 기반 비동기 처리**다.

## 2.9 컴퓨터 시스템 여행 — 추상화의 층위

### 실행 흐름의 추상화

| 개념 | 특징 |
|------|------|
| 코루틴 | 사용자 모드에서 스케줄링되는 경량 실행 단위 |
| 스레드 | 커널 모드에서 스케줄링됨 |
| 프로세스 | 스레드 + 독립적인 주소 공간 |

### 프로그램 실행 환경의 추상화

| 개념 | 설명 |
|------|------|
| 컨테이너(Container) | OS 자원을 가상화 → 프로세스를 격리하여 배포 환경 표준화 |
| 가상머신(VM) | 하드웨어까지도 가상화 → 클라우드의 기본 단위 |

### 콜백 vs 클로저
- **콜백:** 실행할 코드만 전달
- **클로저:** 코드 + 코드가 참조하는 환경(변수들)까지 함께 저장

```kotlin
fun greet(message: String): () -> Unit {
    return { println("Hello $message") } // message를 캡처 → 클로저
}

val hello = greet("World")
hello()  // Hello World
```

## 요약

| 주제 | 핵심 포인트 |
|------|------------|
| 콜백 | 유연한 설계 가능하지만, 비동기에서 복잡성 증가 |
| 동기/비동기 | 기다리느냐(wait) 아니냐(do next) 관점 |
| 블로킹/논블로킹 | 스레드가 OS에 의해 멈추느냐(sleep) 아니냐 관점 |
| 고성능 서버 | Event Loop, I/O Multiplexing, Reactor, Thread Pool |
| 코루틴 | 동기처럼 작성하지만 비동기와 효율 유지 |
| 추상화 | 코루틴 → 스레드 → 프로세스 → 컨테이너 → VM 순으로 고수준화 |
