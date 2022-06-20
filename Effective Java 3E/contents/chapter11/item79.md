# 아이템 79. 과도한 동기화는 피하라
과도한 동기화는 성능을 떨어뜨리고, 교착상태에 빠뜨리고, 심지어 예측할 수 없는 동작을 낳기도 한다.

**응답 불가와 안전 실패를 피하려면 동기화 메서드나 동기화 블록 안에서는 제어를 절대로 클라이언트에 양도하면 안 된다.** 예를 들어, 동기화된 영역 안에서는 재정의할 수 있는 메서드는 호출하면 안 되며, 클라이언트가 넘겨준 함수 객체를 호출해서도 안 된다. 동기화된 영역을 포함한 클래스 관점에서는 이런 메서드는 모두 바깥 세상에서 온 외계인이다.  
외계인 메서드가 하는 일에 따라 동기화된 영역은 예외를 일으키거나, 교착상태에 빠지거나, 데이터를 훼손할 수도 있다.

## 동기화 블럭에서 제어를 클라이언트에게 양도한 예시
```java
// 잘못된 코드. 동기화 블록 안에서 외계인 메서드를 호출한다.
public class ObservableSet<E> extends ForwardingSet<E> {
    public ObservableSet(Set<E> set) { super(set); }

    private final List<SetObserver<E>> observers = new ArrayList<>(); // 관찰자리스트 보관

    public void addObserver(SetObserver<E> observers) { // 관찰자 추가
        synchronized(observers) {
            observers.add(observer);
        }
    }

    public boolean removeObserver(SetObserver<E> observer) { // 관찰자 제거
        synchronized(observers) {
            return observers.remove(observer);
        }
    }

    private void notifyElementAdded(E element) { // Set에 add하면 관찰자의 added 메서드를 호출한다.
        synchronized(observers) {
            for (SetObserver<E> observer : observers)
                observer.added(this, element);
        }
    }

    @Override
    public boolean add(E element) {
        boolean added = super.add(element);
        if (added)
            notifyElementAdded(element);
        return added;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean result = false;
        for (E element : c)
            result |= add(element); // notifyElementAdded를 호출
        return result;
    }
}
```

```java
public class Test {
    public static void main(String[] args) {
        ObservableSet<Integer> set = new ObservableSet<>(new HashSet<>());

        set.addObserver((s, e) -> System.out.println(e));

        for (int i = 0; i < 100; i++) {
            set.add(i);
        }
    }
}
```
위 코드는 문제 없지만, 제어로직이 람다식으로 코드 바깥에 있다.

## 익명함수 콜백
```java
public class Test {
    public static void main(String[] args) {
        ObservableSet<Integer> set = new ObservableSet<>(new HashSet<>());

        set.addObserver(new SetObserver<>() {
            public void added(ObservableSet<Integer> s, Integer e) {
                System.out.println(e);
                if (e == 23)
                    s.removeObserver(this);
            }
        });

        for (int i = 0; i < 100; i++) {
            set.add(i);
        }
    }
}
```
위 메서드를 실행하면 `ConcurrentModificationException`을 던지고 종료된다.  
관찰자의 `added` 메서드 호출이 일어난 시점이 `notifyElementAdded`가 관찰자들의 리스트를 순회하는 도중이기 때문이다.

`added` 메서드는 `ObservableSet`의 `removeObserver` 메서드를 호출하고, 이 메서드는 다시 `observers.remove` 메서드를 호출한다. 여기서 문제가 발생한다. 리스트를 순회하는 도중이기 때문에 허용되지 않은 동작이다. 동시 수정이 일어나지 않도록 보장하지만, 정작 자신이 콜백을 거쳐 되돌아와 수정하는 것까지 막지는 못한다.

## 문제를 일으키는 스레드 - 교착상태
```java
// 쓸데없이 백그라운드 스레드를 사용하는 관찰자
public class Test {
    public static void main(String[] args) {
        ObservableSet<Integer> set =
            new ObservableSet<>(new HashSet<>());

        set.addObserver(new SetObserver<Integer>() {
            public void added(ObservableSet<Integer> s, Integer e) {
                System.out.println(e);
                if (e == 23) {
                    ExecutorService exec =
                        Executors.newSingleThreadExecutor();
                    try {
                        exec.submit(() -> s.removeObserver(this)).get();
                    } catch (ExecutionException | InterruptedException ex) {
                        throw new AssertionError(ex);
                    } finally {
                        exec.shutdown();
                    }
                }
            }
        });

        for (int i = 0; i < 100; i++)
            set.add(i);
    }
}
```
이 프로그램을 실행하면 교착상태에 빠진다. 백그라운드 스레드가 `s.removeObserver`를 호출하면 관찰자를 잠그려 시도하지만 락을 얻을 수 없다. 메인 스레드가 이미 락을 쥐고 있기 때문이다. 그와 동시에 메인 스레드는 백그라운드 스레드가 관찰자를 제거하기만을 기다리는 중이다.

## 불변식이 임시로 깨진 경우
자바 언어의 락은 재진입(reentrant)을 허용하므로 교착상태에 빠지지는 않는다. 단, 안전 실패(데이터 훼손)로 변모시킬 여지가 있다.

> 락의 재진입 가능성: 이미 락을 획득한 스레드는 다른 synchronized 블록을 만났을 때 락을 다시 검사하지 않고 진입 가능하다.

이 문제의 해결 방법은 간단하다. 외부 클라이언트에 의해 제어되는 코드를 동기화 블록 바깥으로 옮기는 것이다.

```java
// 외계인 메서드를 동기화 블록 바깥으로 옮겼다.
private void notifyElementAdded(E element) {
    List<SetObserver<E>> snapshot = null;
    synchronized(observers) {
        snapshot = new ArrayList<>(observers);
    }
    for (SetObserver<E> observer : snapshot) // 락이 필요 없이 순회 가능
        observer.added(this, element);
}
```

또는 `java.util.concurrent.CopyOnWriteArrayList`를 사용하는 방법도 있다.

동기화 영역 바깥에서 호출되는 외계인 메서드를 열린 호출(open call)이라 한다. 이 메서드는 언제 끝날지 알 수 없는데, 동기화 영역 안에서 호출된다면 그동안 다른 스레드는 보호된 자원을 사용하지 못하고 대기해야만 한다. 따라서 열린 호출은 실패 방지 효과 외에도 동시성 효율을 크게 개선해준다.

동기화의 기본 법칙은 동기화 영역 내에서는 가능한 일을 적게하는 것이다.

가변 클래스를 작성할 때 따라야할 선택지

1. 동기화를 고려하지 말고 사용하는 측에서 고려하도록 하라
2. 동기화를 내부에서 수행해 스레드안전하게 만들자(외부에서 전체에 락을 거는 것보다 더 효율이 높을 시에만)

- 락 분할(lock splitting) - 하나의 클래스에서 기능적으로 락을 분리해서 사용하는 것(두개 이상의 락 - ex: 읽기전용 락, 쓰기전용 락)
- 락 스트라이핑(lock striping) - 자료구조 관점에서 한 자료구조 전체가 아닌 일부분(buckets or stripes 단위)에 락을 적용하는 것
- 비차단 동시성 제어(nonblocking concurrency control) 

## 핵심 정리
- 교착상태와 데이터 훼손을 피하려면 동기화 영역 안에서 외계인 메서드를 절대 호출하지 말자.
- 일반화해 이야기하면, 동기화 영역 안에서의 작업은 최소한으로 줄이자. 가변 클래스를 설계할 때는 스스로 동기화해야 할지 고민하자.
- 멀티코어 세상인 지금은 과도한 동기화를 피하는 게 과거 어느 때보다 중요하다.
- 합당한 이유가 있을 때만 내부에서 동기화하고, 동기화했는지 여부를 문서에 명확히 밝히자.