# 5장. 작은 것으로 큰 성과 이루기, 캐시
## 5.1 캐시, 어디에나 존재하는 것

1.  **CPU와 메모리의 속도 차이:** CPU의 연산 속도는 매우 빠르지만, 메모리(RAM)의 접근 속도는 그에 비해 약 100분의 1 수준으로 매우 느리다. 이로 인해 CPU는 명령어를 실행할 때 메모리를 기다리느라 유휴 상태에 머무는 성능 병목 현상이 발생한다.
2.  **캐시 계층의 도입:** 이 간극을 메우기 위해 CPU와 메모리 사이에 작지만 빠른 **캐시 메모리(L1, L2, L3)** 를 추가했다. CPU는 데이터를 찾을 때 캐시를 먼저 확인하며, 여기서 데이터를 찾으면(캐시 적중) 느린 메모리에 접근하지 않아도 되므로 성능이 비약적으로 향상된다.
3.  **데이터 갱신 전략:** 캐시와 메모리 사이의 데이터 일관성을 유지하기 위해 두 가지 방식을 사용.
    *   **연속 기입(Write-through):** 캐시와 메모리를 동시에 갱신하는 동기식 방식으로, 구현이 간단하지만 메모리 접근 속도 때문에 느리다.
    *   **후기입(Write-back):** 캐시만 먼저 갱신하고, 해당 데이터가 캐시에서 제거될 때 메모리에 기록하는 비동기식 방식으로 성능이 더 우수.
4.  **다중 코어 캐시 일관성:** 여러 CPU 코어가 각자의 캐시를 가질 때, 동일한 데이터에 대한 복사본이 서로 달라지는 문제가 발생. 이를 해결하기 위해 **MESI 프로토콜**과 같은 규칙을 사용하여 코어 간 데이터 일관성을 유지.
5.  **캐시 개념의 확장:** 캐시 원리는 하드웨어뿐만 아니라 시스템 전반에 적용된다. **메모리는 디스크의 캐시(페이지 캐시)** 로 활용되며, 로컬 디스크는 원격 분산 파일 시스템의 캐시 역할을 수행.

## 5.2 어떻게 캐시 친화적인 프로그램을 작성할까?

캐시의 효율을 극대화하려면 프로그램이 **지역성의 원칙(Locality of reference)** 을 따라야 한다.

1.  **지역성의 원칙:**
    *   **시간적 지역성(Temporal locality):** 한 번 참조된 메모리 영역이 가까운 미래에 다시 참조될 가능성이 높다는 특성.
    *   **공간적 지역성(Spatial locality):** 참조된 메모리 영역의 인접한 영역이 곧 참조될 가능성이 높다는 특성.
2.  **캐시 친화적인 코딩 기법:**
    *   **메모리 풀 사용:** `malloc`으로 메모리를 여기저기 흩어지게 할당하는 대신, 큰 덩어리를 한 번에 할당받아 사용하면 공간적 지역성이 좋아진다.
    *   **구조체(struct) 재배치:** 자주 함께 사용되는 필드(**핫 데이터**)를 구조체 내에서 서로 인접하게 배치하여 캐시 적중률을 높인다.
    *   **핫 데이터와 콜드 데이터 분리:** 빈번하게 접근하는 데이터와 거의 사용하지 않는 데이터(**콜드 데이터**)를 분리하여 캐시 공간을 효율적으로 사용한다.
    *   **데이터 구조 선택:** 연속된 메모리를 사용하는 **배열(Vector)** 이 포인터로 연결되어 메모리에 흩어진 연결 리스트보다 캐시 친화적.
    *   **다차원 배열 순회:** C 언어와 같은 행 우선 저장 방식에서는 **행 우선 순회** 를 해야 캐시 적중률이 극대화. 열 우선 순회를 할 경우 매번 캐시 적중에 실패하여 성능이 심각하게 저하될 수 있다.

**결론적으로**, 캐시는 단순히 하드웨어의 부품이 아니라 프로그래머가 데이터의 배치와 순회 방식을 조절함으로써 성능을 크게 개선할 수 있는 소프트웨어 설계의 핵심 요소다.

---

## Q1. 데이터 갱신 전략?
캐시는 **메모리의 복사본** → "캐시에 쓴 데이터를 언제 메모리에 반영할 것인가?" (두 가지 갱신 전략에 대한 기본 내용은 제외)

- Write-through (연속 기입) → 읽기는 빠르지만, 쓰기는 느림.
  - 일관성 관리가 단순
  - 캐시가 날아가도 메모리는 항상 최신
  - 디버깅, 임베디드/안정성 중시 시스템에 유리
  - 모든 쓰기가 메모리 접근을 동반
  - 캐시를 써도 쓰기 성능 이득이 거의 없음
  - 메모리 버스 병목 발생
- Write-back (후기입) → **단점 때문에 MESI 같은 프로토콜이 필수로 튀어나옴**
  - 쓰기 성능 압도적으로 좋음
  - 동일 데이터에 여러 번 쓰면 메모리에는 한 번만 반영
  - 현대 CPU의 기본 전략
  - 구현이 복잡
  - 전원 장애 시 데이터 유실 위험
  - 멀티코어 환경에서 **캐시 일관성 문제** 심각

## Q2. MESI 프로토콜?
멀티코어 CPU에서는
  - 각 코어가 자기만의 L1/L2 캐시를 가짐
  - 동일한 메모리 주소가 여러 캐시에 복사됨

문제: 한 코어가 데이터를 수정했는데 다른 코어는 옛날 값을 계속 쓰는 상황

→ MESI는 이를 막기 위한 **4가지 캐시 상태 규칙**

- M (Modified)
  - 캐시에만 존재
  - 메모리와 값이 다름
  - dirty 상태

```
# 이 캐시가 쫓겨날 때 메모리에 반드시 write-back
Cache: 최신
Memory: 오래됨
```

- E (Exclusive)
  - 캐시에만 존재
  - 메모리와 값이 같음
  - 아직 수정 안됨

```
# 이 상태에서 쓰면 바로 M으로 전환 (메모리 접근 없음 → 빠름)
Cache = Memory
(혼자만 가지고 있음)
```

- S (Shared)
  - 여러 코어 캐시에 존재
  - 모두 메모리와 같은 값

```
# 읽기 전용. 쓰려면 다른 캐시 무효화 필요
Core A Cache = Core B Cache = Memory
```

- I (Invalid)
  - 캐시 라인이 무효
  - 데이터 없음

### Case Study
상황 1: Core A가 처음 읽기
  - 메모리에서 읽음
  - 다른 캐시에 없음

→ 상태: E (Exclusive)

상황 2: Core B도 같은 주소 읽기
  - Core A의 캐시에서 복사
  - 둘 다 읽기만 함

→ 상태: S / S

상황 3: Core A가 쓰기
  - Core B 캐시에 무효화(Invalidate) 신호
  - Core B: S → I
  - Core A: S → M

→ 이제 Core A만 최신 데이터 보유

상황 4: Core B가 다시 읽기
  - Core A가 데이터 제공 (Cache-to-cache transfer)
  - Core A: M → S
  - Core B: I → S
  - 메모리는 여전히 갱신 안 됨

### 성능 관점에서 중요한 포인트

- False Sharing
  - 서로 다른 변수인데
  - **같은 캐시 라인(보통 64B)**에 있음

→ 한 코어가 쓰기만 해도 다른 코어 캐시가 계속 Invalid됨 → 성능 폭망

해결
- 구조체 padding
- 핫 데이터 분리
- 스레드별 데이터 분리

## 5.3 다중 스레드 성능 방해자
다중 코어 환경에서 캐시가 어떻게 오히려 성능을 떨어뜨릴 수 있는지 두 가지 주요 원인을 통해 설명.

### 캐시 라인 (Cache Line)
캐시와 메모리가 데이터를 주고받는 기본 단위는 개별 변수 크기가 아니라, 보통 **64바이트 크기**의 데이터 묶음인 캐시 라인입니다. **공간적 지역성을 활용하기 위해 한 번 메모리에 접근할 때 인접한 데이터를 한꺼번에 캐시로 가져온다.**

### 첫 번째 성능 방해자: 캐시 튕김 (Cache Bouncing / Ping-Pong)

아래처럼, CPU Core마다 자기 캐시를 가지는 구조.
```
                (Cache Line: counter)

                    Memory
                       │
                       ▼
                 Shared L3 Cache
                    │      │
                    │      │
           ┌────────┘      └────────┐
           ▼                         ▼
       Core 1                    Core 2
       L1 Cache                  L1 Cache
   [counter = 0]              [counter = 0]
   (Shared state)             (Shared state)
```

- 여러 스레드가 각기 다른 CPU 코어에서 동일한 전역 변수를 동시에 수정하려고 할 때 발생한다.
- 캐시 일관성을 유지하기 위해(예: MESI 프로토콜), 한 코어가 변수를 수정하면 다른 코어의 캐시를 무효화(invalidation)해야 한다. 두 코어가 경쟁적으로 서로의 캐시를 계속 무효화하게 되어, 메모리에서 데이터를 다시 읽어오는 오버헤드가 발생해 다중 스레드가 단일 스레드보다 훨씬 느려지는 현상이 생긴다.

```
// Core1이 값을 수정한다.

Thread1 → counter++

Core1 L1 Cache               Core2 L1 Cache
[counter = 1] (Modified)     [counter = 0] (Invalidated)

- Core1 → 다른 코어 캐시 invalidation
- Core2 cache line → INVALID

// Core2가 값을 수정한다.
- Core2 → Core1 또는 L3에서 cache line 다시 가져옴 (자신의 캐시가 invalid 상태이므로)

Core1 L1 Cache               Core2 L1 Cache
[counter = INVALID]          [counter = 2] (Modified)

- Core2 write → Core1 invalid
```

위 흐름을 반복한다.

```
# Step1 (read)

Core1 L1: counter=0
Core2 L1: counter=0

# Step2 (Core1 write)

Core1 L1: counter=1 (Modified)
Core2 L1: INVALID

# Step3 (Core2 write)

Core1 L1: INVALID
Core2 L1: counter=2 (Modified)

# Step4

Core1 write → Core2 invalid
Core2 write → Core1 invalid

→ 반복

# 결과

Cache Line 이동

Core1  ⇄  Core2
```

### 두 번째 성능 방해자: 거짓 공유 (False Sharing)
- 두 스레드가 서로 다른 변수(예: 변수 a와 b)를 사용하더라도, 두 변수가 인접해 있어 같은 '캐시 라인'에 속해 있다면 동일한 문제가 발생한다.
- 스레드들은 실제로 데이터를 공유하지 않지만, 하드웨어 구조(캐시 라인) 때문에 서로의 캐시를 무효화하는 '캐시 튕김'을 유발한다.

```
|---- 64 byte cache line ----|

a (4B)
b (4B)
c (4B)
d (4B)
...
```

→ a와 b는 같은 cache line

해결책: 두 변수 사이에 사용하지 않는 데이터(패딩)를 채워 넣어 서로 다른 캐시 라인에 위치하도록 분리하면 이 문제를 해결할 수 있다.

```c
struct Data {
  int a;
  char padding[60]; // 요기
  int b;
}
```

## 5.4 봉화희제후와 메모리 장벽
CPU의 명령어 실행 순서 최적화가 다중 스레드 환경에서 일으키는 혼란과 그 해결책을 설명.

### 명령어의 비순차적 실행 (Out-of-Order Execution)
- 성능 극대화를 위해 컴파일러는 코드를 최적화하며 명령어 순서를 재정렬하고, CPU 역시 파이프라인의 빈 공간을 채우기 위해 서로 의존성이 없는 명령어를 미리 실행(부정 출발)한다.
- 또한, **저장 버퍼(Store Buffer)** 의 도입으로 쓰기 작업이 캐시나 메모리에 비동기적으로 반영되므로, 특정 코어의 명령어 실행 순서가 다른 코어의 시점에서는 뒤죽박죽으로 보일 수 있다.
- 단일 스레드에서는 이런 비순차적 실행이 전혀 문제 되지 않지만, **잠금 없는 프로그래밍(Lock-free programming)**을 하는 다중 스레드 환경에서는 치명적인 동기화 버그를 유발한다.

### 메모리 장벽 (Memory Barrier)
- CPU가 비순차적 실행을 하지 못하도록 막아주는 특수한 기계 명령어.
- 네 가지 유형: LoadLoad(읽기 뒤 재정렬 방지), StoreStore(쓰기 뒤 재정렬 방지), LoadStore, StoreLoad(가장 무거운 장벽)로 나뉜다.

### 획득-해제 의미론 (Acquire-Release Semantics)
- 무거운 StoreLoad 장벽 대신, 스레드 간 동기화를 효율적으로 해결하는 개념.
- 획득(Acquire): 읽기 작업 이후의 코드가 앞으로 당겨져 실행되는 것을 막는다.
- 해제(Release): 쓰기 작업 이전의 코드가 뒤로 미뤄져 실행되는 것을 막는다.
- C++11 등의 언어는 std::atomic 등을 통해 아키텍처(x86의 강한 메모리 모델, ARM의 약한 메모리 모델 등)의 차이를 메꾸고 이 의미론을 쉽게 사용할 수 있는 표준 인터페이스를 제공한다.

---
## Q1. CPU Out-of-Order + Store Buffer 적용의 대표적인 사례? → Java Memory Model
- Java Memory Model(JMM)의 핵심 개념
  - instruction reordering
  - happens-before
  - visibility

```java
a = 1;
flag = true;

//위 코드가 CPU 입장에선 아래와 반대 순서로 실행될 수도 있음.
flag = true;
a = 1;
```

`프로그램 순서 ≠ 관측 순서` 라는 문제를 정의하는 것이 JMM.

happens-before의 의미는 아래와 같음.
- eg) A happens-before B
  - A의 결과는 B에서 반드시 보인다
  - 순서도 보장된다

대표적인 관계
- lock unlock
- volatile write → volatile read
- thread start
- thread join

여기서 volatile은 happens-before 관계를 강제로 만드는 도구에 해당.

```java
class Example {

  int data = 0;
  volatile boolean ready = false;

  void writer() {
    data = 42;
    ready = true;
  }

  void reader() {
    if (ready) {
      System.out.println(data);
    }
  }
}
```

위 코드 예시에서 보장되는 것은 `ready = true` 이전의 모든 write → ready read 이후에 반드시 보임
- `data = 42` → `ready = true` 순서 보장

Double-Checked Locking??

```java
// 싱글톤 패턴 예시
class Singleton {

  private static Singleton instance;

  static Singleton getInstance() {
    if (instance == null) {
      synchronized (Singleton.class) {
        if (instance == null) {
          instance = new Singleton(); // 요기가 문제.
        }
      }
    }
    return instance;
  }
}
```

코드 상의 흐름은 아래와 같지만,
1. memory allocate
2. constructor 실행
3. instance reference assign

CPU는 여전히 reorder 할 수 있음
1. memory allocate
3. instance reference assign
2. constructor 실행

이렇게 되면 다른 스레드는 `instance != null`을 보고 접근하지만 객체 초기화가 안됨. 이 문제 때문에 코드를 아래와 같이 개선함.

```java
public class Singleton {

  // 핵심: volatile로 선언
  // memory barrier 처리로 순서 강제
  private static volatile Singleton instance;

  // 외부에서 new 못하도록
  private Singleton() {
  }

  public static Singleton getInstance() {
    // 첫 번째 체크 (락 없이 빠르게 확인)
    if (instance == null) {
      synchronized (Singleton.class) {
        // 두 번째 체크 (락 내부)
        if (instance == null) {
          instance = new Singleton();
        }
      }
    }
    return instance;
  }
}
```

이런 사항들을 기반으로 `ConcurrentHashMap`은 **lock 최소화**를 목표로 설계되어 핵심 전략은 `volatile + CAS + fine-grained locking`에 해당함.