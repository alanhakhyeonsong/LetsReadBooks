# 아이템 76. 가능한 한 실패 원자적으로 만들라
작업 도중 예외가 발생해도 그 객체는 여전히 정상적으로 사용할 수 있는 상태라면 멋지지 않을까? 검사 예외를 던진 경우라면 호출자가 오류 상태를 복구할 수 있을 테니 특히 더 유용할 것이다. **일반화해 이야기하면, 호출된 메서드가 실패하더라도 해당 객체는 메서드 호출 전 상태를 유지해야 한다.** 이러한 특성을 실패 원자적(failure-atomic)이라 한다.

## 메서드를 실패 원자적으로 만드는 방법
### 불변 객체로 설계하는 것
불변 객체는 태생적으로 실패 원자적이다. 메서드가 실패하면 새로운 객체가 만들어지지는 않을 수 있으나 기존 객체가 불안정한 상태에 빠지는 일은 결코 없다. 불변 객체의 상태는 생성 시점에 고정되어 절대 변하지 않기 때문이다.

가변 객체의 메서드를 실패 원자적으로 만드는 가장 흔한 방법은 **작업 수행에 앞서 매개변수의 유효성을 검사하는 것이다.** 객체의 내부 상태를 변경하기 전에 잠재적 예외의 가능성 대부분을 걸러낼 수 있는 방법이다. 예를들면 다음과 같다.

```java
public Object pop() {
    if (size == 0)
        throw new EmptyStackException();
    Object result = elements[--size];
    elements[size] = null; // 다 쓴 참조 해제
    return result;
}
```

위 메서드는 처음의 if 문에서 size의 값을 확인하여 0이면 예외를 던진다. 사실 이 부분을 제거하더라도 스택이 비었다면 여전히 예외를 던진다. 다만 size의 값이 음수가 되어 다음번 호출도 실패하게 만들며, 이때 던지는 `ArrayIndexOutOfBoundsException`은 추상화 수준이 상황에 어울리지 않다고 볼 수 있다.

### 실패할 가능성이 있는 모든 코드를, 객체의 상태를 바꾸는 코드보다 앞에 배치하는 것
계산을 수행해보기 전에는 인수의 유효성을 검사해볼 수 없을 때 앞서의 방식에 덧붙여 쓸 수 있는 기법이다. `TreeMap`을 생각해보면, 원소들을 어떤 기준으로 정렬한다. 이 자료구조에 원소를 추가하려면 그 원소는 `TreeMap`의 기준에 따라 비교할 수 있는 타입이어야 한다. 엉뚱한 타입의 원소를 추가하려 들면 트리를 변경하기에 앞서, 해당 원소가 들어갈 위치를 찾는 과정에서 `ClassCastException`을 던질 것이다.

### 객체의 임시 복사본에서 작업을 수행한 다음, 작업이 성공적으로 완료되면 원래 객체와 교체하는 것
데이터를 임시 자료구조에 저장해 작업하는 게 더 빠를 때 적용하기 좋은 방식이다. 예를 들어 어떤 정렬 메서드에서는 정렬을 수행하기 전에 입력 리스트의 원소들을 배열로 옮겨 담는다.배열을 사용하면 정렬 알고리즘의 반복문에서 원소들에 훨씬 빠르게 접근할 수 있기 때문이다. 이는 성능을 높이고자 취한 결정이지만, 혹시나 정렬에 실패하더라도 입력 리스트는 변하지 않는 효과를 덤으로 얻게 된다.

### 작업 도중 발생하는 실패를 가로채는 복구 코드를 작성하여 작업 전 상태로 되돌리는 것
주로 (디스크 기반의) 내구성을 보장해야 하는 자료구조에 쓰이는데, 자주 쓰이는 방법은 아니다.

## 실패 원자성은 항상 달성할 수 있는 것은 아니다.
실패 원자성은 일반적으로 권장하는 덕목이지만 항상 달성할 수 있는 것은 아니다. 예를 들어 두 스레드가 동기화 없이 같은 객체를 동시에 수정한다면 그 객체의 일관성이 깨질 수 있다. 따라서 `ConcurrentModificationException`을 잡아냈다고 해서 그 객체가 여전히 쓸 수 있는 상태라고 가정해서는 안 된다. 한편, `Error`는 복구할 수 없으므로 `AssertionError`에 대해서는 실패 원자적으로 만들려는 시도조차 할 필요가 없다.

또한 실패 원자적으로 만들 수 있더라도 항상 그리 해야 하는 것도 아니다. 실패 원자성을 달성하기 위한 비용이나 복잡도가 아주 큰 연산도 있기 때문이다.