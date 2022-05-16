# 아이템 47. 반환 타입으로는 스트림보다 컬렉션이 낫다
Java 7까지는 일련의 원소를 반환하는 메서드(원소 시퀀스)의 반환 타입으로 `Collection`, `Set`, `List` 같은 컬렉션 인터페이스 혹은 `Iterable`이나 배열을 사용했다. 기본은 컬렉션 인터페이스다.

for-each 문에서만 쓰이거나 반환된 원소 시퀀스가 (주로 `contains(Object)` 같은) 일부 `Collection` 메서드를 구현할 수 없을 때는 `Iterable` 인터페이스를 썼다.  
반환 원소들이 기본 타입이거나 성능에 민감한 상황이라면 배열을 썼다.

그런데 Java 8이 스트림이라는 개념을 들고 오면서 이 선택이 아주 복잡한 일이 되어버렸다.

스트림은 반복(iteration)을 지원하지 않는다. **따라서 스트림과 반복을 알맞게 조합해야 좋은 코드가 나온다.**

사실 Stream 인터페이스는 Iterable 인터페이스가 정의한 추상 메서드를 전부 포함할 뿐만 아니라, Iterable 인터페이스가 정의한 방식대로 동작한다. 그럼에도 for-each로 스트림을 반복할 수 없는 까닭은 바로 Stream이 Iterable을 확장(extend)하지 않아서다.

```java
// 자바 타입 추론의 한계로 컴파일되지 않음
for (ProcessHandle ph : ProcessHandle.allProcesses()::iterator) {
    // 프로세스를 처리한다.
}

// 스트림을 반복하기 위한 '끔찍한' 우회 방법
for (ProcessHandle ph : (Iterable<ProcessHandle>) ProcessHandle.allProcesses()::iterator) {
    // 프로세스를 처리한다.
}

// Stream<E>를 Iterable<E>로 중개해주는 어댑터
// 어댑터를 사용하면 어떤 스트림도 for-each 문으로 반복할 수 있다.
public static <E> Iterable<E> iterableOf(Stream<E> stream) {
    return stream::iterator;
}

for (ProcessHandle p : iterableOf(ProcessHandle.allProcesses())) {
    // 프로세스를 처리한다.
}
```

객체 시퀀스를 반환하는 메서드를 작성하는데, 이 메서드가 오직 스트림 파이프라인에서만 쓰일 걸 안다면 마음 놓고 스트림을 반환하게 해주자.  
반대로 반환된 객체들이 반복문에서만 쓰일 걸 안다면 Iterable을 반환하자.

하지만, 공개 API를 작성할 때는 스트림 파이프라인을 사용하는 사람과 반복문에서 쓰려는 사람 모두를 배려해야 한다.

Collection 인터페이스는 Iterable의 하위 타입이고 stream 메서드도 제공하니 반복과 스트림을 동시에 지원한다. 따라서 **원소 시퀀스를 반환하는 공개 API의 반환 타입에는 Collection이나 그 하위 타입을 쓰는 게 일반적으로 최선이다.**

Arrays 역시 `Arrays.asList`와 `Stream.of` 메서드로 손쉽게 반복과 스트림을 지원할 수 있다. 반환하는 시퀀스의 크기가 메모리에 올려도 안전할 만큼 작다면 `ArrayList`나 `HashSet` 같은 표준 컬렉션 구현체를 반환하는 게 최선일 수 있다.  
하지만, **단지 컬렉션을 반환한다는 이유로 덩치 큰 시퀀스를 메모리에 올려서는 안 된다.**

이는 책에 제시된 멱집합 예제를 참고하자.

## 핵심 정리
- 원소 시퀀스를 반환하는 메서드를 작성할 때는, 이를 스트림으로 처리하기를 원하는 사용자와 반복으로 처리하길 원하는 사용자가 모두 있을 수 없음을 떠올리고, 양쪽을 다 만족시키려 노력하자.
- 컬렉션을 반환할 수 있다면 그렇게 하라.
- 반환 전부터 이미 원소들을 컬렉션에 담아 관리하고 있거나 컬렉션을 하나 더 만들어도 될 정도로 원소 개수가 적다면 `ArrayList` 같은 표준 컬렉션에 담아 반환하라.
- 그렇지 않으면 앞서의 멱집합 예처럼 전용 컬렉션을 구현할지 고민하라.
- 컬렉션을 반환하는 게 불가능하면 스트림과 `Iterable` 중 더 자연스러운 것을 반환하라.
- 만약 나중에 `Stream` 인터페이스가 `Iterable`을 지원하도록 자바가 수정된다면, 그때는 안심하고 스트림을 반환하면 될 것이다.