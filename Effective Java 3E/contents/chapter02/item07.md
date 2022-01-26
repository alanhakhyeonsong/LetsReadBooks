# 아이템 7. 다 쓴 객체 참조를 해제하라

**메모리 누수**가 생길 수 있는 위치에 대해 알아보자.

Java에는 Garbage Collector가 있어서 메모리 관리에 따로 신경쓰지 않아도 된다고 오해할 수 있는데 절대 사실이 아니다. 아래 예시들을 통해 메모리 관리의 중요성을 알아보자.

## 1. 메모리를 직접 관리하는 클래스

```java
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0) throw new EmptyStackException();
        return elements[--size];
    }

    /**
     * 원소를 위한 공간을 적어도 하나 이상 확보한다.
     * 배열 크기를 늘려야 할 때마다 대략 두 배씩 늘린다.
    **/
    private void ensureCapacity() {
        if (this.elements.length == size) {
            this.elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
```

**위 코드에선 스택의 사이즈가 커졌다가 줄어들 때 스택에서 꺼내진 객체들을 가비지 컬렉터가 회수하지 않는다.**  
pop을 해도 스택에서 여전히 사용이 끝난 참조 값을 갖고 있다. 이를 비활성 영역이라고도 할 수 있으며, 가비지 컬렉터는 비활성 영역이 사용하지 않는 레퍼런스라는 것을 알 방법이 없다.

**여기서 문제는 다 쓴 레퍼런스 값의 메모리 누수 외에도 해당 객체가 참조하고 있는 모든 객체(그리고 그 객체가 참조하는 모든 객체)를 가비지 컬렉터가 회수할 수 없어서 추가적인 메모리 누수가 발생한다.**

이러한 문제를 해결하기 위해 해당 참조를 다 썼을 때 null 처리(참조 해제)하면 된다.  
// 실제 Stack의 경우도 아래와 같이 null 처리를 해줌

```java
public Object pop() {
    if (size == 0) throw new EmptyStackException();
    Object result = elements[--size];
    elements[size] = null; // 다 쓴 참조 해제
    return result;
}
```

객체를 null 처리하면 다음 가비지 컬렉터 발생 시 자동으로 레퍼런스가 정리된다. 그러나 모든 객체를 다 쓰자마자 일일이 null 처리하는 것은 바람직하지도 않고 프로그램을 지저분하게 만든다.

**객체 참조를 null 처리하는 일은 예외적인 경우여야 한다.** 다 쓴 참조를 해제하는 가장 좋은 방법은 그 참조를 담은 변수를 유효 범위(scope) 밖으로 밀어내는 것이다.

## 2. 캐시

**캐시 역시 메모리 누수를 일으키는 주범이다.** 객체 참조를 캐시에 넣어 놓고 캐시를 비우는 것을 잊기 쉽다.

빠른 속도를 위해 사용하는 캐시에 자원이 계속 쌓이게 되면 캐시는 본래의 역할을 못하게 된다.

해결 방법은 다음과 같다.

- WeakHashMap을 사용해 캐시를 만들자. 특정 key 값이 더 이상 사용되지 않는다고 판단되면 해당 key-value 쌍을 삭제해준다. 캐시의 키에 대한 참조가 캐시 밖에서 필요 없어지면 엔트리를 캐시에서 자동으로 비워주는 역할이다.
- 새로운 엔트리를 추가할 때 부가적인 작업으로 기존 캐시를 비우는 방법이다. 캐시를 만들 때 보통 캐시 엔트리의 유효 기간을 정확히 정의하기 어렵기 때문에 시간이 지날수록 엔트리의 가치를 떨어뜨리는 방식을 사용한다. `LinkedHashMap`의 `removeEldestEntry()`를 사용하자.

## 3. 리스너(listener) 혹은 콜백(callback)

클라이언트가 콜백을 등록만 하고 명확히 해지하지 않는다면 콜백은 계속 쌓여만 갈 것이다. 이럴 때 콜백을 약한 참조(weak reference)로 저장하면 가비지 컬렉터가 즉시 수거해간다.

## 핵심 정리

- 메모리 누수는 겉으로 잘 드러나지 않아 시스템에 수년간 잠복하는 사례도 있다.
- 이런 누수는 철저한 코드 리뷰나 힙 프로파일러 같은 디버깅 도구를 동원해야만 발견되기도 한다. 따라서 이런 종류의 문제는 예방법을 잘 익혀두는 것이 매우 중요하다.
