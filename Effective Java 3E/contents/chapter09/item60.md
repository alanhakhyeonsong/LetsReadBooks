# 아이템 60. 정확한 답이 필요하다면 float과 double은 피하라
float과 double 타입은 과학과 공학 계산용으로 설계되었다. 이진 부동소수점 연산에 쓰이며, 넓은 범위의 수를 빠르게 정밀한 '근사치'로 계산하도록 세심하게 설계되었다. **따라서 정확한 결과가 필요할 때는 사용하면 안 된다.**  
**float와 double 타입은 특히 금융 관련 계산과는 맞지 않는다.**

```java
// 오류 발생 - 금융 계산에 부동소수 타입을 사용했다.
public static void main(String[] args) {
    double funds = 1.00;
    int itemsBought = 0;
    for (double price = 0.10; funds >= price; price += 0.10) {
        funds -= price;
        itemsBought++;
    }
    System.out.println(itemsBought + "개 구입");
    System.out.println("잔돈(달러):" + funds);
}
```

**금융 계산에는 `BigDecimal`, `int` 혹은 `long`을 사용해야 한다.**

```java
// BigDecimal을 사용한 해법. 속도가 느리고 쓰기 불편하다.
public static void main(String[] args) {
    final BigDecimal TEN_CENTS = new BigDecimal(".10");
    int itemsBought = 0;
    for (BigDecimal price = TEN_CENTS; funds.compareTo(price) >= 0; price = price.add(TEN_CENTS)) {
        funds -= price;
        itemsBought++;
    }
    System.out.println(itemsBought + "개 구입");
    System.out.println("잔돈(달러):" + funds);
}
```
이 프로그램을 실행하면 사탕 4개를 구입한 후 잔돈은 0달러가 남으므로 올바른 답이 나온다.  
하지만, BigDecimal에는 단점이 있다. 기본 타입보다 쓰기가 훨씬 불편하고, 훨씬 느리다. 단발성 계산이라면 느리다는 문제는 무시할 수 있지만, 쓰기 불편하다는 점은 못내 아쉽다.

이에 대한 대안으로 int 혹은 long 타입을 쓸 수도 있다. 이 경우, 다룰 수 있는 값의 크기가 제한되고, 소수점을 직접 관리해야 한다.
```java
// 정수 타입을 사용한 해법
public static void main(String[] args) {
    int itemsBought = 0;
    for (int price = 10; funds >= price; price += 10) {
        funds -= price;
        itemsBought++;
    }
    System.out.println(itemsBought + "개 구입");
    System.out.println("잔돈(센트):" + funds);
}
```

## 핵심 정리
- 정확한 답이 필요한 계산에는 float이나 double을 피하라.
- 소수점 추적은 시스템에 맡기고, 코딩 시의 불편함이나 성능 저하를 신경 쓰지 않겠다면 `BigDecimal`을 사용하라.
- `BigDecimal`이 제공하는 여덟 가지 반올림 모드를 이용하여 반올림을 완벽히 제어할 수 있다. 법으로 정해진 반올림을 수행해야 하는 비즈니스 계산에서 아주 편리한 기능이다.
- 반면, 성능이 중요하고 소수점을 직접 추적할 수 있고 숫자가 너무 크지 않다면 int나 long을 사용하라.
- 숫자를 아홉 자리 십진수로 표현할 수 있다면 int를 사용하고, 열여덟 자리 십진수로 표현할 수 있다면 long을 사용하라.
- 열여덟 자리를 넘어가면 `BigDecimal`을 사용해야 한다.