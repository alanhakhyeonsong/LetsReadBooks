# 아이템 78. 공유 중인 가변 데이터는 동기화해 사용하라
`synchronized` 키워드는 해당 메서드나 블록을 한번에 한 스레드씩 수행하도록 보장한다.

동기화의 기능은 다음 두 가지가 있다.
- 배타적 실행, 즉 한 스레드가 변경하는 중이라서 상태가 일관되지 않은 순간의 객체를 다른 스레드가 보지 못하게 막는 용도
- 동기화는 동기화된 메서드나 블록에 들어간 스레드가 같은 락의 보호하에 수행된 모든 이전 수정의 최종 결과를 보게 해준다.

언어 명세상 long과 double 외의 변수를 읽고 쓰는 동작은 원자적이다. 여러 스레드가 같은 변수를 동기화 없이 수정하는 중이라도, 항상 어떤 스레드가 정상적으로 저장한 값을 온전히 읽어옴을 보장한다는 뜻이다.  
다만, 원자적 데이터를 읽고 쓸 때 동기화하지 말아야 겠다는 잘못된 발상을 떠올리면 안된다.

자바 언어 명세는 스레드가 필드를 읽을 때 항상 '수정이 완전히 반영된' 값을 얻는다고 보장하지만, 한 스레드가 저장한 값이 다른 스레드에게 '보이는가'는 보장하지 않는다.

**동기화는 배타적 실행뿐 아니라 스레드 사이의 안정적인 통신에 꼭 필요하다.** 이는 한 스레드가 만든 변화가 다른 스레드에게 언제 어떻게 보이는지를 규정한 자바의 메모리 모델 때문이다.

## 다른 스레드를 멈추는 작업
우선, `Thread.stop`은 절대 사용하면 안된다.  
대신 자신의 `boolean` 필드를 폴링하면서 그 값이 true가 되면 멈추는 방식이 있다.
```java
// 잘못된 코드 - 영원히 실행된다.
public class StopThread {
    private static boolean stopRequested;

    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while (!stopRequested)
                i++;
        });
        backgroundThread.start();

        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
    }
}
```
하지만, 이 역시 잘못된 방법이다.

원인은 동기화에 있다. 동기화하지 않으면 메인 스레드가 수정한 값을 백그라운드 스레드가 언제쯤 보게 될지 보증할 수 없다.

```java
// 적절히 동기화해 스레드가 정상 종료한다.
public class StopThread {
    private static boolean stopRequested;

    private static synchronized void requestStop() {
        stopRequested = true;
    }

    private static synchronized boolean stopRequested() {
        return stopRequested;
    }

    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while (!stopRequested())
                i++;
        });
        backgroundThread.start();

        TimeUnit.SECONDS.sleep(1);
        requestStop();
    }
}
```
쓰기 메서드(`requestStop`)와 읽기 메서드(`stopRequest`) 모두를 동기화 했음에 주목하자. 쓰기 메서드만 동기화해서는 충분하지 않다. **쓰기와 읽기 모두가 동기화되지 않으면 동작을 보장하지 않는다.**

## Volatile
반복문에서 매번 동기화하는 비용이 크진 않지만 속도가 더 빠른 대안은 다음과 같다.

```java
public class StopThread {
    private static volatile boolean stopRequested;

    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while (!stopRequested)
                i++;
        });
        backgroundThread.start();

        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
    }
}
```

`stopRequested` 필드를 `volatile`으로 선언하면 동기화를 생략해도 된다. `volatile` 한정자는 배타적 수행과는 상관없지만 항상 가장 최근에 기록된 값을 읽게 됨을 보장한다. 다만 `volatile`은 주의해서 사용해야 한다.

```java
// 잘못된 코드 - 동기화가 필요하다
private static volatile int nextSerialNumber = 0;

public static int generateSerialNumber() {
    return nextSerialNumber++;
}
```
증가 연산자가 문제를 유발한다. 이 연산자는 코드상으로는 하나지만 실제로는 해당 필드에 두 번 접근한다. 먼저 값을 읽고, 그런 다음 (1 증가한) 새로운 값을 저장하는 것이다.  
만약 두 번째 스레드가 이 두 접근 사이를 비집고 들어와 값을 읽어가면 첫 번째 스레드와 똑같은 값을 돌려받게 된다. 이런 오류를 안전 실패라 한다.

이를 해결하기 위해선, `synchronized` 한정자를 붙이고 `volatile`을 제거해야 한다.

이번 아이템에서 언급한 문제들을 피하는 가장 좋은 방법은 애초에 가변 데이터를 공유하지 않는 것이다. 가변 데이터는 단일 스레드에서만 쓰도록 하자.

## 핵심 정리
- **여러 스레드가 가변 데이터를 공유한다면 그 데이터를 읽고 쓰는 동작은 반드시 동기화 해야 한다.**
- 동기화하지 않으면 한 스레드가 수행한 변경을 다른 스레드가 보지 못할 수도 있다.
- 공유되는 가변 데이터를 동기화하는 데 실패하면 응답 불가 상태에 빠지거나 안전 실패로 이어질 수 있다.
- 이는 디버깅 난이도가 가장 높은 문제에 속한다.
- 간헐적이거나 특정 타이밍에만 발생할 수도 있고, VM에 따라 현상이 달라지기도 한다.
- 배타적 실행은 필요 없고 스레드끼리의 통신만 필요하다면 `volatile` 한정자만으로 동기화할 수 있다. 다만 올바로 사용하기가 까다롭다.