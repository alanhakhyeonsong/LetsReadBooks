# 아이템 77. 예외를 무시하지 말라
너무 뻔하지만 반복해 각인해야 할 정도로 사람들이 자주 어기고 있는 사항이다. API 설계자가 메서드 선언에 예외를 명시하는 까닭은, 그 메서드를 사용할 때 적절한 조치를 취해달라고 말하는 것이다. API 설계자의 목소리를 흘려버리지 말자. 안타깝게도 예외를 무시하기란 아주 쉽다. 해당 메서드 호출을 `try`문으로 감싼 후 `catch` 블록에서 아무 일도 하지 않으면 끝이다.

```java
// catch 블록을 비워두면 예외가 무시된다. 아주 의심스러운 코드다.
try {
    ...
} catch (SomeException e) {
}
```

예외는 문제 상황에 잘 대처하기 위해 존재하는데 **catch 블록을 비워두면 예외가 존재할 이유가 없어진다.** 비유하자면 화재경보를 무시하는 수준을 넘어 아예 꺼버려, 다른 누구도 화재가 발생했음을 알지 못하게 하는 것과 같다.

물론 예외를 무시해야 할 때도 있다. **예외를 무시하기로 했다면 catch 블록 안에 그렇게 결정한 이유를 주석으로 남기고 예외 변수의 이름도 `ignored`로 바꿔놓도록 하자.**

```java
Future<Integer> f = exec.submit(planarMap::chromaticNumber);
int numColors = 4; // 기본값. 어떤 지도라도 이 값이면 충분함
try {
    numColors = f.get(1L, TimeUnit.SECONDS);
} catch (TimeoutException | ExecutionException ignored) {
    // 기본 값을 사용한다.(색상 수를 최소화하면 좋지만, 필수는 아니다.)
}
```