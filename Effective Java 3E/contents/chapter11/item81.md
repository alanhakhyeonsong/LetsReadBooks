# 아이템 81. wait와 notify보다는 동시성 유틸리티를 애용하라
스레드를 다루는 메서드 중 `wait`와 `notify`가 있다.
- `wait`: 스레드를 일시정지 상태로 돌아가도록 함
- `notify`: 일시정지 상태인 스레드 중 하나를 실행 대기상태로 변화
- `notifyAll`: 일시정지 상태인 스레드를 모두 실행대기 상태로 변화

하지만 Java 5부터 도입된 고수준의 동시성 유틸리티가 이 작업들을 대신 처리해주기 때문에 위 메서드들을 사용해야 할 이유가 많이 줄었다. **`wait`와 `notify`는 올바르게 사용하기가 아주 까다로우니 고수준 동시성 유틸리티를 사용하자.**

`java.util.concurrent`의 고수준 유틸리티는 세 범주로 나눌 수 있다.
- 실행자 프레임워크
- 동시성 컬렉션
- 동기화 장치

## 동시성 컬렉션 - Map
`List`, `Queue`, `Map` 같은 표준 컬렉션 인터페이스에 동시성을 가미해 구현한 고성능 컬렉션이다. 높은 동시성에 도달하기 위해 동기화를 각자의 내부에서 수행한다. 따라서 **동시성 컬렉션에서 동시성을 무력화하는 건 불가능하며, 외부에서 락을 추가로 사용하면 오히려 속도가 느려진다.**

- `HashMap`: 동시성 고려 X
- `HashTable`: 동시성을 고려한다. 하지만 메서드 단위이기 때문에 느릴 수 있다.
- `ConcurrentHashMap`: 동시성을 고려한다. 메서드를 통으로 동기화 처리한 `HashTable`보다 빠르다.

```java
// ConcurrentMap으로 구현한 동시성 정규화 맵
public static final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

public static String intern(String s) {
    String result = map.get(s);
    if (result == null) {
        result = map.putIfAbsent(s, s);
        if (result == null)
            result = s;
    }
    return result;
}
```
`Collections.synchronizedMap`보다는 `ConcurrentHashMap`을 사용하는 게 훨씬 좋다. 동기화된 맵을 동시성 맵으로 교체하는 것만으로 동시성 애플리케이션의 성능은 극적으로 개선된다.

## 동시성 컬렉션 - Queue
작업이 성공적으로 완료될 때까지 기다리도록(차단되도록) 구현된 컬렉션도 있다.  
`Queue`를 확장한 `BlockingQueue`에 추가된 메서드 중 `take`는 큐의 첫 원소를 꺼낸다. 이때 만약 큐가 비었다면 새로운 원소가 추가될 때까지 기다린다. 이러한 특성 때문에 `BlockingQueue`는 작업 큐로 쓰기에 적합하다.

작업 큐는 하나 이상의 생산자(producer) 스레드가 작업을 큐에 추가하고, 하나 이상의 소비자(consumer) 스레드가 큐에 있는 작업을 꺼내 처리하는 형태다. `ThreadPoolExecutor`를 포함한 대부분의 실행자 서비스 구현체에서 이 `BlockingQueue`를 사용한다.

## 동기화 장치
스레드가 다른 스레드를 기다릴 수 있게 하여, 서로 작업을 조율할 수 있게하는 장치이다. `CountDownLatch`와 `Semaphore`가 가장 자주 쓰이는 동기화 장치이다. 이 외에 `CyclicBarrier`와 `Exchanger`, `Phaser`가 있다.

`CountDownLatch`는 일회성 장벽으로, 하나 이상의 스레드가 또 다른 하나 이상의 스레드 작업이 끝날 때까지 기다리게 한다. 생성자는 int 값을 받으며, 이 값이 래치의 `countDown` 메서드를 몇 번 호출해야 대기 중인 스레드들을 깨우는지 결정한다.

```java
// 동시 실행 시간을 재는 간단한 프레임워크 예제
public static long time(Executor executor, int concurrency, Runnable action) throws InterruptedException {
    CountDownLatch ready = new CountDownLatch(concurrency);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(concurrency);

    for (int i = 0; i < concurrency; i++) {
        executor.execute(() -> {
            // 타이머에게 준비를 마쳤음을 알린다.
            ready.countDown();
            try {
                // 모든 작업자 스레드가 준비될 때까지 기다린다.
                start.await();
                action.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // 타이머에게 작업을 마쳤음을 알린다.
                done.countDown();
            }
        });
    }
    ready.await(); // 모든 작업자가 준비될 때까지 기다린다.
    long startNanos = System.nanoTime();
    start.countDown(); // 작업자들을 깨운다.
    done.await(); // 모든 작업자가 일을 끝마치기를 기다린다.
    return System.nanoTime - startNanos;
}
```

## wait와 notify
어쩔 수 없이 남아있는 레거시 코드를 위해 `wait`와 `notify`를 다뤄야 할 때가 있다. `wait` 메서드는 스레드가 어떤 조건이 충족되기를 기다리게 할 때 사용한다. 락 객체의 `wait` 메서드는 반드시 그 객체를 잠금 동기화 영역 안에서 호출해야 한다.

```java
// wait 메서드를 사용하는 표준 방식
synchronized (obj) {
    while (<조건이 충족되지 않았다>)
        obj.wait(); // 락을 놓고, 깨어나면 다시 잡는다.
    
    ... // 조건이 충족됐을 때의 동작을 수행한다.
}
```
**`wait` 메서드를 사용할 때는 반드시 대기 반복문(wait loop) 관용구를 사용하라. 반복문 밖에서는 절대로 호출하지 말라.** 이 반복문은 `wait` 호출 전후로 조건이 만족하는지를 검사하는 역할을 한다.

## 핵심 정리
- `wait`와 `notify`를 직접 사용하는 것을 동시성 '어셈블리 언어'로 프로그래밍하는 것에 비유할 수 있다.
- 반면 `java.util.concurrent`는 고수준 언어에 비유할 수 있다.
- **코드를 새로 작성한다면 `wait`와 `notify`를 쓸 이유가 거의(어쩌면 전혀) 없다.**
- 이들을 사용하는 레거시 코드를 유지보수해야 한다면 `wait`는 항상 표준 관용구에 따라 `while`문 안에서 호출하도록 하자.
- 일반적으로 `notify`보다는 `notifyAll`을 사용해야 한다.
- 혹시라도 `notify`를 사용한다면 응답 불가 상태에 빠지지 않도록 각별히 주의하자.