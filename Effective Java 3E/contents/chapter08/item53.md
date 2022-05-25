# 아이템 53. 가변인수는 신중히 사용하라
가변인수(varargs) 메서드는 명시한 타입의 인수를 0개 이상 받을 수 있다. 가변인수 메서드를 호출하면, 가장 먼저 인수의 개수와 길이가 같은 배열을 만들고 인수들을 이 배열에 저장하여 가변인수 메서드에 건네준다.

```java
// 간단한 가변인수 활용 예시
static int sum(int... args) {
    int sum = 0;
    for (int arg : args) {
        sum += arg;
    }
    return sum;
}
```
인수가 1개 이상이어야 할 때도 있다. 예를 들어 최솟값을 찾는 메서드인데 인수를 0개만 받을 수도 있도록 설계하는 것은 좋지 않다. 인수 개수는 런타임에 (자동 생성된) 배열의 길이로 알 수 있다.

```java
// 인수가 1개 이상이어야 하는 가변인수 메서드 - 잘못 구현한 예시
static int sum(int... args) {
    if (args.length == 0) {
        throw new IllegalArgumentException("인수가 1개 이상 필요합니다.");
    }
    int min = args[0];
    for (int i = 1; i < args.length; i++) {
        if (args[i] < min)
            min = args[i];
    }
    return min;
}
```
위 방식에는 문제가 있다. 가장 심각한 문제는 인수를 0개만 넣어 호출하면 (컴파일타임이 아닌) 런타임에 실패한다는 점이다. 코드도 지저분하다. `args` 유효성 검사를 명시적으로 해야 하고, min의 초깃값을 `Integer.MAX_VALUE`로 설정하지 않고는 `for-each` 문도 사용할 수 없다.

```java
// 인수가 1개 이상이어야 할 때 가변인수를 제대로 사용하는 방법
static int min(int firstArg, int... remainingArgs) {
    int min = firstArg;
    for (int arg : remainingArgs) {
        if (arg < min)
            min = arg;
    }
    return min;
}
```
위 코드처럼 매개변수를 2개 받도록 하면 문제는 해결 가능하다. 첫 번째로는 평범한 매개변수를 받고, 가변인수는 두 번째로 받으면 문제가 깔끔하게 사라진다.

이런 예시처럼, 가변인수는 인수 개수가 정해지지 않았을 때 아주 유용하다. `printf`는 가변인수와 한 묶음으로 Java에 도입되었고, 이 때 핵심 리플렉션 기능도 재정비되었다고 한다.

하지만 성능에 민감한 상황이라면 가변인수가 걸림돌이 될 수 있다. 가변인수 메서드는 호출될 때마다 배열을 새로 하나 할당하고 초기화한다.

이 비용을 감당할 수는 없지만, 가변인수의 유연성이 필요할 때 선택할 수 있는 패턴이 있다. 예를 들어 해당 메서드 호출의 95%가 인수를 3개 이하로 사용한다면, 다중정의 메서드가 유용하다.

```java
public void foo() { }
public void foo(int a1) { }
public void foo(int a1, int a2) { }
public void foo(int a1, int a2, int a3) { }
public void foo(int a1, int a2, int a3, int... rest) { }
```
마지막 다중정의 메서드가 인수 4개 이상인 5%의 호출을 담당하는 것이다. 이 기법도 평소엔 별 이득이 없지만, 꼭 필요한 특수 상황에서 유용하다.

![](https://velog.velcdn.com/images/songs4805/post/58434490-fecd-4e4e-9585-288feb897e56/image.png)

## 핵심 정리
- 인수 개수가 일정하지 않은 메서드를 정의해야 한다면 가변인수가 반드시 필요하다.
- 메서드를 정의할 때 필수 매개변수는 가변인수 앞에 두고, 가변인수를 사용할 때는 성능 문제까지 고려하자.