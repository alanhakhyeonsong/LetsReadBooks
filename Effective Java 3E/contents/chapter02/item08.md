# 아이템 8. finalizer와 cleaner 사용을 피하라

**Finalizer는 예측 불가능하고, 위험하며, 대부분 불필요하다.** 이걸 사용하면 이상하게 동작하기도 하고, 성능도 안좋아지고, 이식성에도 문제가 생길 수 있다. 이를 유용하게 쓸 수 있는 경우가 극히 드물다.

- 안전망 역할로 자원을 반납하고자 하는 경우
- 네이티브 리소스를 정리해야 하는 경우

위 두 가지 경우 아니고서야 불필요하다.

Java 9에서는 finalizer를 사용 자제(deprecated) API로 지정하고 cleaner를 그 대안으로 소개했다. **finalizer보단 덜 위험하지만, 여전히 예측할 수 없고, 느리고, 일반적으로 불필요하다.**

finalizer, cleaner는 C++의 destructor와는 다른 개념이다. 이는 어떤 객체와 연관있는 자원을 반납할 때도 사용하지만, 자바에서는 `try-with-resources` 또는 `try-finally`블럭이 그 역할을 한다.

## 단점 1

언제 실행될지 알 수 없다. 어떤 객체가 더 이상 필요 없어진 시점으 그 즉시 finalizer 또는 cleaner가 바로 실행되지 않을 수도 있다. 그 사이에 시간이 얼마나 걸릴지는 아무도 모른다. 따라서 **타이밍이 중요한 작업을 절대로 finalizer나 cleaner에서 하면 안된다.** 예를 들어, 파일 리소스를 반납하는 작업을 그 안에서 처리한다면, 실제로 그 파일 리소스 처리가 언제 될지 알 수 없고, 자원 반납이 안되서 더 이상 새로운 파일을 열 수 없는 상황이 발생할 수도 있다.

## 단점 2

특히 finalizer는 인스턴스 반납을 지연시킬 수도 있다. finalizer 쓰레드는 우선 순위가 낮아서 언제 실행될 지 모른다. 따라서, finalizer 안에 어떤 작업이 있고, 그 작업을 쓰레드가 처리 못해서 대기하고 있다면, 해당 인스턴스는 GC가 되지 않고 계속 쌓이다가 결국엔 `OutOfMemoryException`이 발생할 수도 있다.

cleaner는 자신을 수행할 쓰레드를 제어할 수 있다는 점에서 조금 낫지만(해당 쓰레드의 우선순위를 높게 준다거나), 여전히 해당 쓰레드는 백그라운드에서 동작하고 가비지 컬렉터의 통제하에 있으니 언제 처리될지는 알 수 없다.

## 단점 3

자바 언어 명세는 finalizer나 cleaner의 수행 시점뿐 아니라 수행 여부조차 보장하지 않는다. 따라서 **프로그램 생애주기와 상관없는, 상태를 영구적으로 수정하는 작업에서는 절대 finalizer나 cleaner에 의존해서는 안된다.** 데이터베이스 같은 공유 자원의 영구 락(lock) 해제를 finalizer나 cleaner에 맡겨 놓으면 분산 시스템 전체가 서서히 멈출 것이다.

`System.gc`나 `System.runFinalization` 메서드에 속지 말아야 한다. 그걸 실행하더라도 finalizer나 cleaner를 바로 실행한다고 보장되지 않는다. 그걸 보장해준다고 만든 `System.runFinalizersOnExit`과 `Runtime.runFinalizersOnExit`는 둘다 망했고 수십년간 deprecated 상태다.

## 단점 4

**finalizer나 cleaner는 심각한 성능 문제도 동반한다.** `AutoCloseable` 객체를 생성하고, `try-with-resource`로 가비지 컬렉터가 수거하기까지 12ns인데 반해, finalizer를 사용한 경우 550ns가 걸렸다.(약 50배) cleaner를 사용한 경우엔 66ns가 걸렸다.(약 5배)

## 단점 5

**finalizer를 사용한 클래스는 finalizer 공격에 노출되어 심각한 보안 문제를 일으킬 수도 있다.**

어떤 클래스가 있고 그 클래스를 공격하려는 클래스가 해당 클래스를 상속받는다. 그리고 그 나쁜 클래스의 인스턴스를 생성하는 도중에 예외가 발생하거나, 직렬화 할 때 예외가 발생하면, 이 생성되다 만 객체에서 악의적인 하위 클래스의 finalizer가 실행될 수 있다. 이렇게 되면 그 안에서 해당 인스턴스의 레퍼런스를 기록할 수도 있고, GC가 수집하지 못하게 할 수도 있다. 또한 그 안에서 인스턴스가 가진 메소드를 호출할 수도 있다.

원래는 생성자에서 예외가 발생해서 존재하지 않았어야 하는 인스턴스인데, finalizer 때문에 살아 남아 있는 것이다.

final 클래스는 상속이 안되니까 근본적으로 이런 공격이 막혀있고, 다른 클래스는 아무 일도 하지 않는 `finalize()` 메소드를 만들고 여기에 final 키워드를 사용해서 막을 수 있다.

## 자원 반납하는 방법

finalizer나 cleaner를 대신할 방법은 다음과 같다.

자원 반납이 필요한 클래스 `AutoCloseable` 인터페이스를 구현해주고 `try-with-resource`를 사용하거나, 클라이언트에서 인스턴스를 다 쓰면 `close`를 호출하면 된다. `close`메소드는 현재 인스턴스가 이미 종료된 상태인지 확인하고, 이미 반납이 끝난 상태에서 `close`가 호출됐다면 `IllegalStateException`을 던진다.

## finalizer나 cleaner가 사용되는 곳?

### 1. finalizer나 cleaner를 안전망으로 쓰기

자원 반납에 쓸 close 메소드를 클라이언트가 호출하지 않는 것에 대비한 안전망 역할이다. finalizer나 cleaner가 즉시 호출되리라는 보장은 없지만, 클라이언트가 하지 않은 자원 회수를 늦게라도 해주는 것이 아예 안 하는 것보다는 낫기 때문이다. 실제로 자바에서 제공하는 일부 클래스는 안전망 역할의 finalizer를 제공한다. `FileInputStream`, `FileOutputStream`, `ThreadPoolExecutor`, `java.sql.Connection`이 대표적이다.

### 2. 네이티브 피어 정리할 때 쓰기

네이티브 피어란 일반 자바 객체가 네이티브 메서드를 통해 기능을 위임한 네이티브 객체를 말한다.

네이티브 피어는 자바 객체가 아니라서 가비지 컬렉터는 그 존재를 알지 못한다. 그 결과 자바 피어를 회수할 때 네이티브 객체까지 회수하지 못한다. 이 피어가 들고있는 리소스가 중요하지 않은 자원이고 성능상 영향이 크지 않다면 상관 없지만, 중요한 리소스인 경우엔 close 메서드를 사용해야 한다.

```java
// cleaner를 안전망으로 활용하는 AutoCloseable 클래스
public class Room implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();

    // 청소가 필요한 자원. 절대 Room을 참조해서는 안 된다.
    private static class State implements Runnable {
        int numJunkPiles; // 방(Room) 안의 쓰레기 수

        State(int numJunkPiles) {
            this.numJunkPiles = numJunkPiles;
        }

        @Override
        public void run() {
            System.out.println("방 청소");
            numJunkPiles = 0;
        }
    }

    // 방의 상태. cleanable과 공유한다.
    private final State state;

    // cleanable 객체. 수거 대상이 되면 방을 청소한다.
    private final Cleaner.Cleanable cleanable;

    public Room(int numJunkPiles) {
        state = new State(numJunkPiles);
        cleanable = cleaner.register(this, state);
    }

    @Override
    public void close() {
        cleanable.clean();
    }
}
```

주의 할 점?

- State 인스턴스는 절대로 Room 인스턴스를 참조하면 안된다. 순환 참조가 생겨 가비지 컬렉터가 Room 인스턴스를 회수해갈 기회가 오지 않게 된다.
- Cleaner 쓰레드를 만들 클래스는 반드시 static 클래스여야 한다. non-static 클래스(익명 클래스도 마찬가지)의 인스턴스는 그것을 감싸고 있는 클래스의 인스턴스를 참조하지 않는다.

## 핵심 정리

- cleaner(Java 8까지는 finalizer)는 안전망 역할이나 중요하지 않은 네이티브 자원 회수용으로만 사용하자.
- 물론 이런 경우라도 불확실성과 성능 저하에 주의해야 한다.
