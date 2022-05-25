# 아이템 54. null이 아닌 빈 컬렉션이나 배열을 반환하라
```java
// 컬렉션이 비었으면 null을 반환한다.
// 안 좋은 예시
private final List<Cheese> cheesesInStock = ...;

/**
 * @return 매장 안의 모든 치즈 목록을 반환한다.
 * 단, 재고가 하나도 없다면 null을 반환한다.
 */
public List<Cheese> getCheeses() {
    return cheesesInStock.isEmpty() ? null : new ArrayList<>(cheesesInStock);
}
```
위 코드처럼 `null`을 반환한다면, 클라이언트는 이 `null` 상황을 처리하는 코드를 추가로 작성해야 한다.

```java
List<Cheese> cheeses = shop.getCheeses();
if (cheeses != null && cheeses.contains(Cheese.STILTON)) {
    System.out.println("Good");
}
```

**컬렉션이나 배열 같은 컨테이너가 비었을 때 null을 반환하는 메서드를 사용할 때면 항시 이와 같은 방어 코드를 넣어줘야 한다.** 클라이언트에서 방어 코드를 빼먹으면 오류가 발생할 수 있다. null을 반환하려면 반환하려는 쪽에서도 이 상황을 특별히 취급해야 해서 코드가 더 복잡해진다.

## 빈 컨테이너를 할당하는 데도 비용이 드니 null을 반환해야 한다?
이는 틀린 주장이다.

1. 성능 분석 결과 이 할당이 성능 저하의 주범이라고 확인되지 않는 한, 이 정도의 성능 차이는 신경 쓸 수준이 못 된다.
2. 빈 컬렉션과 배열은 굳이 새로 할당하지 않고도 반환할 수 있다.

## 빈 컬렉션을 반환하라
다음은 빈 컬렉션을 반환하는 전형적인 코드로, 대부분의 상황에서는 이렇게 하면 된다.
```java
// 빈 컬렉션을 반환하는 올바른 예
public List<Cheese> getCheeses() {
    return new ArrayList<>(cheesesInStock);
}
```
가능성은 작지만, 사용 패턴에 따라 빈 컬렉션 할당이 성능을 눈에 띄게 떨어뜨릴 수도 있다. 이 땐, **매번 똑같은 빈 '불변' 컬렉션을 반환하는 것이다. 불변 객체는 자유롭게 공유해도 안전하다.**

- `Collections.emptyList`, `Collections.emptySet`, `Collections.emptyMap`이 이에 대한 예시이다.

```java
// 최적화 - 빈 컬렉션을 매번 새로 할당하지 않도록 한다.
public List<Cheese> getCheeses() {
    return cheesesInStock.isEmpty() ? Collections.emptyList() : new ArrayList<>(cheesesInStock);
}
```

## 빈 배열을 반환하라
배열을 쓸 때도 마찬가지다. **절대 null을 반환하지 말고 길이가 0인 배열을 반환하라.**

```java
// 길이가 0일 수도 있는 배열을 반환하는 올바른 방법
public Cheese[] getCheeses() {
    return cheesesInStock.toArray(new Cheese[0]);
}
```
이 방식이 성능을 떨어뜨릴 것 같다면 길이 0짜리 배열을 미리 선언해두고 매번 그 배열을 반환하면 된다. 길이 0인 배열은 모두 불변이기 때문이다.

```java
// 최적화 - 빈 배열을 매번 새로 할당하지 않도록 한다.
private static final Cheese[] EMPTY_CHEESE_ARRAY = new Cheese[0];

public Cheese[] getCheeses() {
    return cheesesInStock.toArray(EMPTY_CHEESE_ARRAY);
}
```

## 핵심 정리
- **null이 아닌, 빈 배열이나 컬렉션을 반환하라.**
- null을 반환하는 API는 사용하기 어렵고 오류 처리 코드도 늘어난다. 그렇다고 성능이 좋은 것도 아니다.