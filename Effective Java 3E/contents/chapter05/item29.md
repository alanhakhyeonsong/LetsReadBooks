# 아이템 29. 이왕이면 제네릭 타입으로 만들라

## 제네릭 타입과 메서드

JDK가 제공하는 제네릭 타입과 메서드를 사용하는 일은 일반적으로 쉬운 편이지만, 제네릭 타입을 새로 만드는 일은 조금 더 어렵다.

[item 7](../chapter02/item07.md)에서 다룬 단순한 스택 코드를 보면, **클라이언트는 스택에서 꺼낸 객체를 형변환해야 하는데, 이때 런타임 오류가 날 위험이 있다.**

**일반 클래스를 제네릭 클래스로 만드는 첫 단계는 클래스 선언에 타입 매개변수를 추가하는 일이다. 이때, 타입 이름으로는 보통 E를 사용한다.**

```java
public class Stack<E> {
    private E[] elements;
    private int size;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new E[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        E result = elements[--size];
        elements[size] == null; // 다 쓴 참조 해제
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
```

위 코드상에선 `elements = new E[DEFAULT_INITIAL_CAPACITY];`에서 오류가 발생한다.

item 28에서 본 것 처럼, `E`와 같은 실체화 불가 타입으로는 배열을 만들 수 없기 때문이다.

## 해결책

### 1. 제네릭 배열 생성을 금지하는 제약을 대놓고 우회하는 방법

Object 배열을 생성한 다음 제네릭 배열로 형변환한다. 이 경우, 컴파일러는 오류 대신 경고를 내보낼 것이다.

```java
// 배열 elements는 push(E)로 넘어온 E 인스턴스만 담는다.
// 따라서 타입 안전성을 보장하지만,
// 이 배열의 런타임 타입은 E[]가 아닌 Object[]다.
@SuppressWarnings("unchecked")
public Stack() {
    elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
}
```

이 경우, 위와 같은 비검사 형변환이 프로그램의 타입 안전성을 해치지 않음을 우리 스스로 확인해야 한다. 배열 `elements`는 private 필드에 저장되고, 클라이언트로 반환되거나 다른 메서드에 전달되는 일이 전혀 없다. `push` 메서드를 통해 배열에 저장되는 원소의 타입은 항상 E다. 따라서 확실히 안전하다.

이처럼 비검사 형변환이 안전함을 직접 증명했다면 범위를 최소로 좁혀 `@SuppressWarnings` 애너테이션으로 해당 경고를 숨긴다.(item 27)

애너테이션을 달면 Stack은 깔끔히 컴파일되고, 명시적으로 형변환하지 않아도 `ClassCastException` 걱정 없이 사용할 수 있게 된다.

### 2. `elements` 필드의 타입을 `E[]`에서 `Object[]`로 바꾸는 것

이 방법은 첫 번째와는 다른 오류가 발생한다. E는 실체화 불가 타입으므로 마찬가지로 우리가 직접 증명하고 경고를 숨기면 된다.

```java
// 비검사 경고를 적절히 숨긴다
public E pop() {
    if (size == 0) {
        throw new EmptyStackException();
    }

    // push에서 E 타입만 허용하므로 이 형변환은 안전하다.
    @SuppressWarnings("unchecked") E result = (E) elements[--size];

    elements[size] = null; // 다 쓴 참조 해제
    return result;
}
```

보통 현업에선 첫 번째 방식을 더 선호한다고 한다. 첫 번째는 형변환을 배열 생성 시 단 한번만 해주면 되지만, 두 번째 방식에서는 배열에서 원소를 읽을 때마다 해줘야 한다.

하지만, E가 Object가 아닌 한 배열의 런타임 타입이 컴파일타임 타입과 달라 힙 오염을 일으킨다. 힙 오염이 마음에 걸리면 두 번째 방식을 사용하기도 한다.

## 제네릭 Stack을 사용하는 맛보기 프로그램

```java
public static void main(String[] args) {
    Stack<String> stack = new Stack<>();
    for (String arg : args) {
        stack.push(arg);
    }
    while (!stack.isEmpty()) {
        System.out.println(stack.pop().toUpperCase());
    }
}
```

사실 제네릭 타입 안에서 리스트를 사용하는 게 항상 가능하지도, 꼭 더 좋은 것도 아니다. 자바가 리스트를 기본 타입으로 제공하지 않으므로 `ArrayList` 같은 제네릭 타입도 결국은 기본 타입인 배열을 사용해 구현해야 한다. 또한 `HashMap` 같은 제네릭 타입은 성능을 높일 목적으로 배열을 사용하기도 한다.

`Stack` 예시처럼 대다수의 제네릭 타입은 타입 매개변수에 아무런 제약을 두지 않는다. `Stack<Object>`, `Stack<int[]>`, `Stack<List<String>>`, `Stack` 등 어떤 참조 타입으로도 Stack을 만들 수 있다. 단, 기본 타입은 사용할 수 없다. 기본 타입으로 Stack을 만들려고 하면 컴파일 오류가 난다. 이는 자바 제네릭 타입 시스템의 근본적인 문제이나, 박싱된 기본 타입을 사용해 우회할 수 있다.

## 타입 매개변수에 제약을 두는 제네릭 타입

`java.util.concurrent.DelayQueue`는 다음처럼 선언되어 있다.

```java
class DelayQueue<E extends Delayed> implements BlockingQueue<E>
```

타입 매개변수 목록인 `<E extends Delayed>`는 `java.util.concurrent.Delayed`의 하위 타입만 받는다는 뜻이다. 이렇게 하여 `DelayQueue` 자신과 `DelayQueue`를 사용하는 클라이언트는 `DelayQueue`의 원소에서 (형변환 없이) 곧바로 `Delayed` 클래스의 메서드를 호출할 수 있다.

이러한 타입 매개변수 E를 한정적 타입 매개변수(bounded type parameter)라 한다.

또한 모든 타입은 자기 자신의 하위 타입이므로 `DelayQueue`로도 사용할 수 있다.

## 핵심 정리

- 클라이언트에서 직접 형변환해야 하는 타입보다 제네릭 타입이 더 안전하고 쓰기 편하다. 따라서 새로운 타입을 설계할 때는 형변환 없이도 사용할 수 있도록 하라.
- 그렇게 하려면 제네릭 타입으로 만들어야 할 경우가 많다.
- 기존 타입 중 제네릭이었어야 하는 게 있다면 제네릭 타입으로 변경하자.
- 기존 클라이언트에는 아무 영향을 주지 않으면서, 새로운 사용자를 훨씬 편하게 해주는 길이다.
