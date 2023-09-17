# Story 4. 어디에 담아야 하는지...
자주 사용하는 `Collection`, `Map` 인터페이스를 상속 받는 객체들에 대해 성능상 차이가 얼마나 있는지 이번 장에서 비교해보자.

## Collection 및 Map 인터페이스의 이해
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/99661cd2-b5c7-4b02-96fe-6c329a98d07d)

`Queue` 인터페이스는 JDK 5.0에서 추가되었다.

- `Collection`: 가장 상위 인터페이스
- `Set`: 중복을 허용하지 않는 집합을 처리하기 위한 인터페이스
- `SortedSet`: 오름차순을 갖는 `Set` 인터페이스
- `List`: 순서가 있는 집합을 처리하기 위한 인터페이스로 인덱스가 있어 위치를 지정하여 값을 찾을 수 있다. 중복을 허용하며, `List` 인터페이스를 상속받는 클래스 중 가장 많이 사용하는 것으로 `ArrayList`가 있다.
- `Queue`: 여러 개의 객체를 처리하기 전에 담아서 처리할 때 사용하기 위한 인터페이스. 기본적으로 FIFO를 따른다.
- `Map`: 키와 값의 쌍으로 구성된 객체의 집합을 처리하기 위한 인터페이스. 중복되는 키를 허용하지 않는다.
- `SortedMap`: 키를 오름차순으로 정렬하는 `Map` 인터페이스.

먼저 `Set` 인터페이스에 대해 알아보자. 이 인터페이스는 중복이 없는 집합 객체를 만들 때 유용하다.

- `HashSet`: 데이터를 해시 테이블에 담는 클래스로 순서 없이 저장된다.
- `TresSet`: red-black tree에 데이터를 담는다. 값에 따라 순서가 정해진다. 데이터를 담으면서 동시에 정렬을 하기 때문에 `HashSet`보다 성능상 느리다.
- `LinkedHashSet`: 해시 테이블에 데이터를 담는데, 저장된 순서에 따라 순서가 결정된다.

참고로 red-black tree란 이진 트리 구조로 데이터를 담는 구조를 말하며 다음과 같은 특징이 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/56162c17-92b7-4579-989b-6def09f22fc6)

- 각각의 노드는 검은색이나 붉은색이어야 한다.
- 가장 상위 노드와 가장 말단 노드는 검은색이다.
- 붉은 노드는 검은 하위 노드만을 가진다.
- 모든 말단 노드로 이동하는 경로의 검은 노드 수는 동일하다.

`List`는 배열의 확장판이라 보면 된다. 하지만, `List` 인터페이스를 구현한 클래스들은 담을 수 있는 크기가 자동으로 증가되므로, 데이터의 개수를 확실히 모를 때 유용하게 사용된다. 구현한 클래스엔 `ArrayList`, `LinkedList` 클래스가 있으며, 원조 클래스 격인 `Vector` 클래스가 있다.

- `Vector`: 객체 생성 시 크기를 지정할 필요가 없는 배열 클래스
- `ArrayList`: `Vector`와 비슷하지만, 동기화 처리가 되어 있지 않다.
- `LinkedList`: `ArrayList`와 동일하지만 `Queue` 인터페이스를 구현했기 때문에 FIFO 큐 작업을 수행한다.

`Map`은 키-값 쌍으로 저장되는 구조체다. 단일 객체만 저장되는 다른 Collection API들과는 다르게 따로 분리되어 있다.

- `Hashtable`: 데이터를 해시 테이블에 담는 클래스. 내부에서 관리하는 해시 테이블 객체가 동기화되어 있으므로, 동기화가 필요한 부분에는 이 클래스를 사용하면 좋다.
- `HashMap`: 데이터를 해시 테이블에 담는 클래스. `Hashtable`과 다른 점은 `null` 값을 허용한다는 것과 동기화되어 있지 않다는 점이다.
- `TreeMap`: red-black tree에 데이터를 담는다. `TreeSet`과 다른 점은 키에 의해 순서가 정해진다는 것이다.
- `LinkedHashMap`: `HashMap`과 거의 동일하며 이중 연결 리스트라는 방식을 사용하여 데이터를 담는다는 점만 다르다.

Java에서 `Queue`와 `List`가 따로 있는 이유는 다음과 같다. `List`의 가장 큰 단점은 데이터가 많은 경우 처리 시간이 늘어난다는 점이다. 가장 앞에 있는 데이터를 지우면 그 다음 데이터부터 마지막 데이터까지 한 칸씩 옮기는 작업을 수행해야 하므로, 데이터가 많으면 많을수록 소요되는 시간이 증가된다. `Queue`와 `List` 모두 순서가 있지만 이와 관련한 처리에 효율성이 다르기 때문이다.

- `PriorityQueue`: 큐에 추가된 순서와 상관없이 먼저 생성된 객체가 먼저 나오도록 되어 있는 큐.
- `LinkedBlockingQueue`: 저장할 데이터의 크기를 선택적으로 정할 수도 있는 FIFO 기반의 링크 노드를 사용하는 블로킹 큐.
- `ArrayBlockingQueue`: 저장되는 데이터의 크기가 정해져 있는 FIFO 기반의 블로킹 큐.
- `PriorityBlockingQueue`: 저장되는 데이터의 크기가 정해져 있지 않고, 객체의 생성순서에 따라 순서가 저장되는 블로킹 큐.
- `DelayQueue`: 큐가 대기하는 시간을 지정하여 처리하도록 되어 있는 큐.
- `SynchronousQueue`: `put()` 메서드를 호출하면, 다른 스레드에서 `take()` 메서드가 호출될 때까지 대기하도록 되어 있는 큐다. 이 큐에는 저장되는 데이터가 없다. API에서 제공하는 대부분의 메서드는 0이나 `null`을 리턴한다.

## Set 클래스 중 무엇이 가장 빠를까?
1,000번의 Loop를 돌려 데이터를 단순히 담을 때의 시간차이를 측정해보면 다음과 같다.

|대상|평균 응답 시간(ms)|
|--|--|
|`HashSet`|375|
|`TreeSet`|1249|
|`LinkedHashSet`|378|

`Set`의 초기 크기를 지정하여 객체를 생성한 후 데이터를 담는 경우 다음과 같다.

|대상|평균 응답 시간(ms)|
|--|--|
|`HashSet`|375|
|`HashSetWithInitialSize`|352|

큰 차이는 발생하진 않지만, 저장되는 데이터의 크기를 알고 있을 경우 객체 생성 시 크기를 미리 지정하는 것이 성능상 유리하다.

`Set` 클래스들이 데이터를 읽을 때의 차이는 다음과 같다. 예제 코드는 생략했지만, `Iterator`를 통해 가져온다.

|대상|평균 응답 시간(ms)|
|--|--|
|`HashSet`|26|
|`TreeSet`|35|
|`LinkedHashSet`|16|

일반적으로 `Set`은 여러 데이터를 넣어 두고 해당 데이터가 존재하는지를 확인하는 용도로 많이 사용되기에 랜덤하게 가져와야 한다. 데이터를 랜덤하게 가져오면 다음과 같다.

|대상|평균 응답 시간(ms)|
|--|--|
|`HashSet`|32|
|`TreeSet`|841|
|`LinkedHashSet`|32|

역시나 `TreeSet`의 속도가 항상 느리다.

```java
public class TreeSet<E> extends AbstractSet<E> implements NavigableSet<E>, Cloneable, Serializable {
    // ...
}
```

구현한 인터페이스 중 `NavigableSet`이 있다. 이는 특정 값보다 큰 값이나 작은 값, 가장 큰 값, 가장 작은 값 등을 추출하는 메서드를 선언해 둔 인터페이스다. 데이터를 순서에 따라 탐색하는 작업이 필요할 때는 `TreeSet`이 사용하는 것이 좋지만, 그렇지 않을 경우 속도가 느려 `HashSet`이나 `LinkedHashSet`을 사용하길 권장한다.

## List 클래스 중 무엇이 빠를까?
역시 단순히 값을 추가하는 예제로 비교하면 결과는 다음과 같다.

|대상|평균 응답 시간(ms)|
|--|--|
|`ArrayList`|28|
|`Vector`|31|
|`LinkedList`|40|

데이터를 넣는 속도는 큰 차이가 없다. 이번에는 꺼내는 속도를 확인해보자.

|대상|평균 응답 시간(ms)|
|--|--|
|`ArrayList`|4|
|`Vector`|105|
|`LinkedList`|1512|

`ArrayList`가 가장 빠르고, `Vector`와 `LinkedList`는 속도가 매우 느리다. 특히 `LinkedList`는 `Queue` 인터페이스를 상속받기 때문이다. 이를 수정하기 위해선 순차적으로 결과를 받아오는 `peek()` 메서드를 사용해야 한다. 변경 후 결과는 다음과 같다.

|대상|평균 응답 시간(ms)|
|--|--|
|`ArrayList`|4|
|`Vector`|105|
|`LinkedList`|1512|
|`LinkedListPeek`|0.16|

`LinkedList`를 사용할 땐 `get()`이 아닌 `peek()`이나 `poll()`을 사용해야 한다. `ArrayList`와 `Vector`의 성능 차이는 `get()` 메서드에 `synchronized`가 선언되어 있기 때문에 성능 저하가 발생할 수밖에 없다.

데이터를 삭제하는 속도를 비교하면 다음과 같다.

|대상|평균 응답 시간(ms)|
|--|--|
|`ArrayListFirst`|418|
|`ArrayListLast`|146|
|`VectorFirst`|687|
|`VectorLast`|426|
|`LinkedListFirst`|423|
|`LinkedListLast`|407|

`ArrayList`, `Vector`의 첫 번째 값을 삭제하는 메서드와 마지막 값을 삭제하는 메서드의 속도 차이가 큰 이유는 하나의 값만 옮기는 것이 아닌 전체 위치를 변경해야 하는 구조때문으로 첫 번째 값 삭제 시 느릴수 밖에 없다.

## Map 관련 클래스 중에서 무엇이 빠를까?
대부분의 데이터 추가 작업 속도는 비슷하므로, `get()`을 사용해 데이터를 꺼내는 시간을 비교해보자.

|대상|평균 응답 시간(ms)|
|--|--|
|`SeqHashMap`|32|
|`SeqHashTable`|106|
|`SeqLinkedHashMap`|34|
|`SeqTreeMap`|197|
|`RamdomHashMap`|40|
|`RamdomHashTable`|120|
|`RamdomLinkedHashMap`|46|
|`RamdomTreeMap`|277|

대부분의 클래스들이 동일하지만, 트리 형태로 처리하는 `TreeMap` 클래스가 가장 느리다.

Sun에서 정리한, 각 인터페이스별로 가장 일반적으로 사용되는 클래스는 다음과 같다.

|인터페이스|클래스|
|--|--|
|`Set`|`HashSet`|
|`List`|`ArrayList`|
|`Map`|`HashMap`|
|`Queue`|`LinkedList`|

## Collection 관련 클래스의 동기화
`HashSet`, `TreeSet`, `LinkedHashSet`, `ArrayList`, `LinkedList`, `HashMap`, `TreeMap`, `LinkedHashMap`은 동기화되지 않은 클래스다. 이와 반대로 `Vector`, `Hashtable`은 동기화되어 있다. JDK 1.0 버전에 생성된 `Vector`, `HashTable`은 동기화 처리가 되어 있지만, JDK 1.2 버전 이후 만들어진 클래스는 모두 동기화 처리가 되어 있지 않다.

`Collections` 클래스에는 최신 버전 클래스들의 동기화를 지원하기 위한 `synchronized`로 시작하는 메서드들이 있다.

```java
Collections.synchronizedSet(new HashSet());

Collections.synchronizedList(new ArrayList());

Collections.synchronizedMap(new HashMap());
```

그리고 `Map`의 경우 키 값들을 `Set`으로 가져와 `Iterator`를 통해 데이터를 처리하는 경우가 발생한다. 이때 `ConcurrentModificationException`이라는 예외가 발생할 수 있다. 이 예외가 발생하는 여러 가지 원인 중 하나는 스레드에서 `Iterator`로 어떤 `Map` 객체의 데이터를 꺼내는 중, 다른 스레드에서 해당 `Map`을 수정하는 경우다. 이런 문제가 발생할 때는 `java.util.concurrent` 패키지에 있는 클래스들을 사용하자.

참고로 일반적인 웹을 개발할 때는 `Collection` 성능 차이를 비교하는 것은 큰 의미가 없다는 점을 기억하자. 각 클래스에는 사용 목적이 있기에 목적에 알맞게 클래스를 선택해서 사용하도록 하자.