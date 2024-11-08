# Story 8. synchronization는 제대로 알고 써야 한다.
웹 기반 시스템을 개발할 때 우리는 스레드를 컨트롤 할 일이 별로 없다. 만약 스레드를 직접 건드리면 서비스의 안전성이 떨어질 수도 있으니 자제하는 것이 좋다.

우리가 개발하는 WAS는 여러 개의 스레드가 동작하도록 되어 있다. 그래서 `synchronized`를 자주 사용한다. 하지만 `synchronized`를 쓴다고 무조건 안정적인 것은 아니며, 성능에 영향을 미치는 부분도 있다.

## 프로세스와 스레드
하나의 프로세스에는 여러 개의 스레드가 생성된다. 단일 스레드가 생성되어 종료될 수도 있고, 여러 개의 스레드가 생성되어 수행될 수도 있다. 간단히 프로세스와 스레드는 1:N 관계라고 생각하자. 스레드는 다른 말로 Lightweight Process(LWP)라고도 한다. **가벼운 프로세스이고, 프로세스에서 만들어 사용하고 있는 메모리를 공유한다.** 별개의 프로세스가 하나씩 뜨는 것보다는 성능이나 자원 사용에 있어서 많은 도움이 된다.

`Thread`와 `Runnable` 을 사용하는 방법과 기본 메서드에 대해선 넘어가도록 하자.

## `interrupt()` 메서드는 절대적인 것이 아니다

`interrupt()` 메서드를 호출하여 특정 메서드를 중지시키려고 할 때 항상 해당 메서드가 멈추는 것은 아니다. → 이 메서드는 해당 스레드가 block 되거나 특정 상태에서만 작동한다.

while을 사용할 때 `while(true)` 처럼 조건에 true를 넣는 것은 코드에 폭탄을 하나 심어 놓는 것과 동일하다. flag 값을 수정하거나 `sleep()` 을 추가하는 방법으로 안전 장치를 두도록 하자.

## synchronized를 이해하자

- `synchronized`는 메서드와 블록으로 사용할 수 있다. 절대로 생성자의 식별자로는 사용할 수 없다.
- 다음과 같은 상황에서 동기화를 사용해야 한다.
  - 하나의 객체를 여러 스레드에서 동시에 사용할 경우
  - `static` 으로 선언한 객체를 여러 스레드에서 동시에 사용할 경우

필요 없는 부분에 `synchronized`를 사용하면 약간이지만 성능에 영향을 준다. 반드시 필요한 부분에만 동기화를 사용해서 성능 저하를 줄이자.

**항상 변하는 값에 대해 `static` 으로 선언하여 사용하면 굉장히 위험하다. `synchronized` 도 꼭 필요할 때만 사용하자.**

## 동기화를 위해 자바에서 제공하는 것들

JDK 5.0 부터 `java.util.concurrent` 가 추가되었다.

- `Lock`: 실행 중인 스레드를 간단한 방법으로 정지시켰다가 실행시킨다. 상호 참조로 인해 발생하는 데드락을 피할 수 있다.
- `Executors`: 스레드를 더 효율적으로 관리할 수 있는 클래스들을 제공한다. 스레드 풀도 제공하므로, 필요에 따라 유용하게 사용할 수 있다.
- `Concurrent` 콜렉션
- `Atomic` 변수: 동기화가 되어 있는 변수를 제공한다. 이 변수를 사용하면, `synchronized` 식별자를 메서드에 지정할 필요 없이 사용할 수 있다.

## JVM 내에서 synchronization은 어떻게 동작할까?

Java의 HotSpot VM은 자바 모니터(monitor)를 제공함으로써 스레드들이 ‘상호 배제 프로토콜’에 참여할 수 있도록 돕는다.
자바 모니터는 lock이나 unlocked 중 하나이며, 동일한 모니터에 진입한 여러 스레드들 중에서 한 시점에는 단 하나의 스레드만 모니터를 가질 수 있다. → **모니터를 가진 스레드만 모니터에 의해 보호되는 영역에 들어가서 작업을 할 수 있다.**

여기서 보호된 영역이란, `synchronized` 로 감싸진 블록들을 의미한다. 모니터를 보유한 스레드가 보호 영역에서의 작업을 마치면, 모니터는 다른 대기중인 스레드에 넘어간다.

JDK 5부터는 `-XX:+UseBiasedLocking` 이라는 옵션을 통해 biased locking이라는 기능을 제공한다. 그 전까지는 대부분의 객체들이 하나의 스레드에 의해서 잠기게 되었지만, 이 옵션을 켜면 스레드가 자기 자신을 향하여 bias된다.
이 상태가 되면 스레드는 많은 비용이 드는 인스트럭션 재배열 작업을 통해 잠김과 풀림 작업을 수행할 수 있게 된다. 이 작업들은 진보된 적응 스피닝 기술을 사용하여 처리량을 개선시킬 수 있다고 한다.

HotSpot VM에서 대부분의 동기화 작업은 fast-path 코드 작업을 통해 진행한다. 만약 여러 스레드가 경합을 일으키는 상황이 발생하면 이 코드는 slow-path 코드 상태로 변환된다.

- slow-path: C++ 코드로 구현됨
- fast-path: JIT compiler에서 제공하는 장비에 의존적인 코드로 작성됨.