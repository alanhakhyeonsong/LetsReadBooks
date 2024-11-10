# Chapter 5. 자바 동시성 기초

- Java의 동시성 → 블록 구조 동시성, 동기화 기반 동시성
  - `synchronized`, `volatile` 키워드에 의존함

### 하드웨어

- 동시성 프로그래밍은 근본적으로 성능에 관한 것
- 실행 중인 시스템의 성능이 직렬 알고리즘으로 작동하기 충분하다면 기본적으로 동시성 알고리즘을 구현할 이유가 없음.
- 최신 컴퓨터 시스템은 멀티 코어 기반
- 모든 Java 프로그램은 멀티스레드. 애플리케이션 스레드가 하나만 있는 프로그램도 마찬가지임.
  - JVM 자체가 다중 코어를 사용할 수 있는 멀티스레드 바이너리
  - 표준 라이브러리엔 **런타임이 관리하는 동시성**을 사용해 일부 실행 작업에 대해 멀티스레드 알고리즘을 구현한 API도 포함됨.

### Java의 스레드 모델

두 가지 기본 개념을 기반으로 함.

- 공유되며 기본적으로 보이는 가변상태
- OS에 의한 선점형 스레드 스케줄링

이렇게 되면?

- 객체는 프로세스 내의 모든 스레드 간 쉽게 공유할 수 있음
- 객체에 대한 참조가 있는 모든 스레드에서 객체를 변경할 수 있음
- 스레드 스케줄러는 언제든 코어에 스레드를 할당하거나 제거할 수 있음
- 메서드는 실행 중에도 교체할 수 있어야 함.
  - 무한 루프가 있는 메서드가 CPU를 영원히 점유하는 것 방지
- 하지만, 예측할 수 없는 스레드 스왑이 발생해 메서드가 ‘반쯤 완료’돼서 객체가 일관되지 않은 상태로 남을 위험이 있음
- 취약한 데이터 보호를 위해 객체를 ‘잠글’ 수 있다.
  - `synchronized`

Java의 스레드와 잠금 기반의 동시성은 매우 저수준

→ `java.util.concurrent` 동시성 라이브러리 : Since Java 5

## 디자인 콘셉트

- 안전성(동시성 타입 안전성)
- 활성성
- 성능
- 재사용 가능성

### 안전성과 동시성 타입 안전성

다른 동작들이 동시에 발생하는 상황에서도 객체 인스턴스가 항상 자기 일관성을 유지하도록 보장하는 것을 의미

```java
// 단일 스레드 클라이언트 코드에선 괜찮지만, 선점형 스레드 스케줄링은 문제를 일으킬 수 있다.
public boolean push(String s) {
    if (current < values.length) {
        values[current] = s;
        // ... 여기서 context switching 발생
        // 객체가 일관되지 않은 잘못된 상태로 남게 됨
        current + 1;
        return true;
    }
}
```

- 한 가지 전략은, 일관되지 않은 상태의 비공개가 아닌 메서드에선 절대 반환하지 않고, 일관되지 않은 상태에선 비공개가 아닌 어떤 메서드(다른 객체의 메서드)를 호출하지 않는 것이다.
- 동기화 잠금 또는 크리티컬 섹션과 같은 방법과 결합한다면 시스템은 안전하다 보장 가능함.

### 활성성

- 모든 시도된 작업이 최종적으로 진행되거나 실패하는 시스템.
- 활성성이 없는 시스템은 기본적으로 고착상태. 성공을 향해 진행하지도 실패하지도 않는다.

일시적 실패 vs 영구적 실패

일시적 실패는 다음과 같은 여러 가지 문제로 인해 발생할 수 있음.

- 잠금 또는 잠금 획득 대기
- 네트워크 I/O와 같은 입력 대기
- 애셋의 일시적 오류
- 스레드 실행을 위한 CPU 시간이 충분하지 않음

영구적 장애의 가장 일반적 원인은 아래와 같음.

- deadlock
- 복구 불가능한 애셋 문제 (ex. 네트워크 파일 시스템이 사라진 경우)
- 신호 누락

동시성 시스템을 설계하기 어려운 주요 원인

- 안전성과 활성성은 서로 상반된 관계.
  - 좋지 않은 일이 발생하지 않도록 보장 vs 결과에 상관 없이 진행이 이뤄지도록 요구
- 재사용 가능한 시스템은 일반적으로 내부 구조를 노출하므로 안전성과 관련된 문제가 발생 가능
- 단순하게 작성된 안전한 시스템은 보통 성능이 좋지 않다. 안전성을 보장하기 위해 일반적으로 많은 lock의 사용을 필요로 하기 때문

대략적인 유용성 순서에 따른 가장 일반적인 몇 가지 방법

1. 각 서브 시스템의 외부 통신을 최대한 제한한다. 데이터 숨김은 안전을 지원하는 강력한 도구.
2. 각 서브 시스템의 내부 구조를 가능한 한 결정론적으로 만든다. 각 서브 시스템이 동시적으로 비결정적인 방식으로 상호작용하더라도 각 서브 시스템의 스레드와 객체에 대한 정적인 지식을 바탕으로 설계함.
3. 클라이언트 애플리케이션이 준수해야 할 정책 접근 방식을 적용. 강력하지만 사용자 애플리케이션이 규칙을 어길 경우 디버깅하기 어려움.
4. 필요한 동작을 문서화.

### 동시성 시스템 - 오버헤드

- 모니터
- 콘텍스트 스위치 수
- 스레드 수
- 스케줄링
- 메모리 위치
- 알고리즘 설계

## 블록 구조 동시성 (Before Java 5)

```java
// 한 번에 하나의 스레드만 시도 가능. 다른 스레드가 들어가려 시도하면 JVM에 의해 일시 중단됨.
public synchronized boolean withdraw(int amount) {
    if (balance >= amount) {
        balance -= amount;
        return true;
    }

    return false;
}
```

- 원시 자료형이 아닌 객체만 잠글 수 있음
- 객체들의 배열을 잠가도 개별 객체는 잠기지 않음.
- 동기화된 메서드는 전체 메서드를 포괄하는 `synchronized (this) { ... }` 블록과 동일하다 생각할 수 있음. 바이트코드에선 다르게 표현됨.
- `static synchronized` 메서드는 잠글 인스턴스 객체가 없기 때문에 `Class` 객체를 잠근다.
- `Class` 객체를 잠가야 하는 경우 하위 클래스에서 접근 방식에 따라 동작이 다를 수 있으므로, 명시적으로 잠가야 하는지 아니면 `getClass()` 를 사용해 잠가야 하는지 신중하게 고려해야 함.
- 내부 클래스의 동기화는 외부 클래스와 독립적이다.
- `synchronized` 메서드는 메서드 시그니처의 일부가 아니기 때문에 인터페이스의 메서드 선언에 표시될 수 없다.
- 동기화되지 않은 메서드는 잠금 상태를 고려하지 않고 신경 쓰지 않는다. 따라서 동기화되지 않은 메서드는 동시에 실행할 수 있으며, 동기화된 메서드가 실행 중일 때도 진행할 수 있다.
- Java의 잠금은 재진입이 가능하다. 한 스레드가 이미 보유한 잠금을 다시 얻을 수 있는 특성.
  - 동일 객체에 대해 `synchronized` 메서드가 다른 `synchronized` 메서드를 호출하는 경우, 이미 보유하고 있는 스레드는 다른 동기화 지점을 만나더라도 계속 진행 가능함.

### 완전히 동기화된 객체

다음 조건을 모두 충족해야 함.

- 모든 필드는 모든 생성자에서 일관된 상태로 초기화된다.
- `public` 필드가 없다.
- 객체 인스턴스는 private 메서드에서 반환된 후에도 일관성이 보장됨.
- 모든 메서드는 유한한 시간 안에 종료된다는 것이 증명돼야 함.
- 모든 메서드는 동기화돼야 함.
- 어떤 메서드도 불일치한 상태에서 다른 인스턴스의 메서드를 호출하지 않는다.
- 어떤 메서드도 불일치한 상태에서 현재 인스턴스의 비공개 메서드를 호출하지 않는다.

하지만, 성능 측면에서 문제가 발생함. → 잠금으로 인한 성능 저하.

### Deadlock

예제

```java
public synchronized boolean transferTo(FSOAccount other, int amount) {
    try {
        Thread.sleep(10);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }

    if (balance >= amount) {
        balance -= ammount;
        other.deposit(amount);
        return true;
    }

    return false;
}
```

두 개의 트랜잭션이 별도의 스레드에 의해 수행된다 가정한다. 잠깐의 `sleep()` 때문에 교착상태의 가능성이 열렸다. → 전송 메서드를 진행하기 전에 각 스레드가 다른 스레드가 보유한 잠금을 해제해야 해서 멈춰버린다.

// TODO deadlock 상태의 스레드 그림 추가

- 모든 스레드에서 항상 동일한 순서로 잠금을 획득하도록 해야 처리 가능해진다.

### volatile 키워드
Java는 초창기부터 `volatile` 키워드를 사용해왔고, 원시 요소를 포함한 객체 필드의 동시성 처리를 하는 간단한 방법으로 사용됐다.

- 스레드가 보는 값은 사용하기 전에 항상 주 메모리에서 다시 읽는다.
- 바이트코드 명령이 완료되기 전에 스레드가 쓴 모든 값은 항상 주 메모리로 플러시된다.

핵심은 메모리 위치에 대해 하나의 작업만 허용하며, 이 작업은 즉시 메모리에 플러시된다. 단일 읽기 또는 단일 쓰기는 가능하지만 그 이상은 불가능하다.

잠금이 포함되지 않아 이 키워드를 통해 교착상태를 유발할 수 없다.

### 불변성
동시성 스타일로 프로그래밍할 때 안전하게 공유해야 하는 데이터를 처리하는 유용한 기법으로 불변 객체를 사용하는 방법도 있다.

상태가 없거나 `final` 필드만 있는 객체다. 이 객체는 상태가 변경될 수 없어 일관되지 않은 상태가 될 수 없기 때문에 항상 안전하고 살아 있다.

단점은, 특정 객체를 초기화 하는 데 필요한 모든 값을 생성자에 전달해야 한다는 것이다. 많은 매개변수가 포함된 복잡한 생성자의 호출이 발생할 수 있다. 그 결과 **팩터리 메서드를 대신 사용하는 경우가 흔하다.**

```java
public final class Deposit {
  private final double amount;
  private final LocalDate date;
  private final Account payee;

  private Deposit(double amount, LocalDate date, Account payee) {
    this.amount = amount;
    this.date = date;
    this.payee = payee;
  }

  public static Deposit of(double amount, LocalDate date, Account payee) {
    return new Deposit(amount, date, payee);
  }

  public static Deposit of(double amount, Account payee) {
    return new Deposit(amount, LocalDate.now(), payee);
  }

  // getter
}
```

불변 객체는 변경할 수 없기에 그 중 하나를 변경해야 할 때는, 해당 타입에 인스턴스 메서드를 추가해 거의 동일한 객체를 반환하지만 일부 필드만 수정된 객체를 반환할 수 있다.

하지만 이 역시 팩토리 메서드에 전달할 매개변수가 많아질 수 있고, 새로운 불변객체를 생성하기 전에 상태를 축적해야 하는 경우, 이 방법이 편리하지 않다.

이를 해결하기 위해 **빌더 패턴**을 사용할 수 있다. 제네릭 빌더 인터페이스를 구현하는 정적 내부 클래스와 불변 클래스 자체의 비공개 생성자로 조합된다.

```java
public interface Builder<T> {
  T build();
}

public final class Deposit {
  // ...

  public static class DepositBuilder implements Builder<Deposit> {
    private double amount;
    private LocalDate date;
    private Account payee;

    public DepositBuilder amount(double amount) {
      this.amount = amount;
      return this;
    }

    public DepositBuilder date(LocalDate date) {
      this.date = date;
      return this;
    }

    public DepositBuilder payee(Account payee) {
      this.payee = payee;
      return this;
    }

    @Override
    public Deposit build() {
      return new Deposit(amount, date, payee);
    }
  }
}
```

- 빌더는 단일 추상 메서드 타입이며, 람다 표현식의 타깃 타입으로 사용할 수 있다.
  - 그러나 빌더의 목적은 불변 인스턴스를 생성하는 것으로, 함수나 콜백을 나타내는 것이 아니라 상태를 수집하는 것이다.
- 빌더는 스레드에 안전하지 않다. 이 설계는 암묵적으로 사용자가 스레드 간에 빌더를 공유하지 않는다고 가정한다.
  - 대신, 한 스레드가 빌더를 사용해 필요한 모든 상태를 집계한 다음 다른 스레드와 간단히 공유할 수 있는 불변 객체를 생성하는 것이 올바른 사용법이다.
