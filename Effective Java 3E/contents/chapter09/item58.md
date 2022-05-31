# 아이템 58. 전통적인 for 문보다는 for-each 문을 사용하라
전통적으로 다음과 같이 for 문으로 컬렉션을 순회한다.
```java
// 컬렉션 순회하기
for (Iterator<Element> i = c.iterator(); i.hasNext(); ) {
    Element e = i.next();
    ... // e로 무언가를 한다.
}

// 배열 순회하기
for (int i = 0; i < a.length; i++) {
    ... // a[i]로 무언가를 한다.
}
```
이 관용구들은 while 문보다는 낫지만 가장 좋은 방법은 아니다. 반복자와 인덱스 변수는 모두 코드를 지저분하게 할 뿐 **우리에게 진짜 필요한 건 원소들 뿐**이다. 게다가 이처럼 쓰이는 요소 종류가 늘어나면 오류가 생길 가능성이 높아진다.

1회 반복에서 반복자는 3번 등장하고, 인덱스는 4번이나 등장하여 변수를 잘못 사용할 틈새가 넓어진다. 혹시라도 잘못된 변수를 사용했을 때 컴파일러가 잡아주리라는 보장도 없다. 마지막으로, 컬렉션이냐 배열이냐에 따라 코드 형태가 상당히 달라지므로 주의해야 한다.

**이런 경우, for-each 문을 사용하면 모두 해결된다.**  
반복자와 인덱스 변수를 사용하지 않으니 코드가 깔끔해지고 오류가 날 일도 없다. 하나의 관용구로 컬렉션과 배열을 모두 처리할 수 있어서 어떤 컨테이너를 다루는지는 신경 쓰지 않아도 된다.

```java
// 컬렉션과 배열을 순회하는 올바른 관용구
for (Element e : elements) {
    ... // e로 무언가를 한다.
}
```

for-each 문은 컬렉션을 중첩해 순회해야 할 때 이점이 더욱 커진다.

```java
// NoSuchElementException을 던진다.
enum Suit { CLUB, DIAMOND, HEART, SPADE }
enum Rank { ACE, DEUCE, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING }

...

static Collection<Suit> suits = Arrays.asList(Suit.values());
static Collection<Rank> ranks = Arrays.asList(Rank.values());

List<Card> deck = new ArrayList<>();
for (Iterator<Suit> i = suits.iterator(); i.hasNext(); ) {
    for (Iterator<Rank> j = ranks.iterator(); j.hasNext(); ) {
        deck.add(new Card(i.next(), j.next()));
    }
}
```
위와 같이 트럼프 카드를 만드는 중첩 for 문이 있다면, 각각의 반복자를 가져와 중첩 for 문을 통해 원소들을 순회하면서 카드를 만든다.  
하지만, 카드 문양의 반복자 i가 문양 하나당 호출되어야 하는데 중첩 for 문의 제일 안쪽에서 숫자 하나당 호출되고 있기 때문에 `Suit`의 모든 원소를 순회하고 나면 `NoSuchElementException`을 던질 것이다.

for-each 문을 사용하면 간단하게 해결할 수 있다.

```java
for (Suit suit : suits) {
    for (Rank rank : ranks) {
        deck.add(new Card(suit, rank));
    }
}
```

하지만, for-each 문을 사용할 수 없는 상황이 세 가지 존재한다.
1. **파괴적인 필터링**: 컬렉션을 순회하면서 선택된 원소를 제거해야 한다면 반복자의 `remove` 메서드를 호출해야 한다. → Java 8부터는 `Collection.removeIf()`를 사용해 컬렉션을 명시적으로 순회하는 일을 피할 수 있다.
2. **변형**: 리스트나 배열을 순회하면서 그 원소의 값 일부 혹은 전체를 교체해야 한다면 리스트의 반복자나 배열의 인덱스를 사용해야 한다.
3. **병렬 반복**: 여러 컬렉션을 병렬로 순회해야 한다면 각각의 반복자와 인덱스 변수를 사용해 엄격하고 명시적으로 제어해야 한다.

마지막으로 **for-each 문은 컬렉션과 배열은 물론 `Iterable` 인터페이스를 구현한 객체라면 무엇이든 순회할 수 있다.**

```java
public interface Iterable<E> {
    // 이 객체의 원소들을 순회하는 반복자를 반환한다.
    Iterator<E> iterator();
}
```
`Iterable`을 처음부터 직접 구현하기는 까다롭지만, 원소들의 묶음을 표현하는 타입을 작성해야 한다면 `Iterable`을 구현하는 쪽으로 고민해보자.

## 핵심 정리
- 전통적인 for 문과 비교했을 때 for-each 문은 명료하고, 유연하고, 버그를 예방해준다. 성능 저하도 없다.
- 가능한 모든 곳에서 for 문이 아닌 for-each 문을 사용하자.