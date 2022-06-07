# 아이템 71. 필요 없는 검사 예외 사용은 피하라
[Checked Exception을 대하는 자세 - cheese10yun님 기술 블로그](https://cheese10yun.github.io/checked-exception/)

검사 예외는 발생한 문제를 프로그래머가 처리하여 안전성을 높이게끔 해준다. 어떤 메서드가 검사 예외를 던질 수 있다고 선언됐다면, 이를 호출하는 코드에서는 `catch` 블록을 두어 그 예외를 붙잡아 처리하거나 더 바깥으로 던져 문제를 전파해야만 한다.

## Checked Exception이 고통스러운 경우
- 검사 예외를 던지는 메서드는 스트림 안에서 직접 사용할 수 없다.  
// [Exceptions in Java 8 Lambda Expressions - www.baeldung.com](https://www.baeldung.com/java-lambda-exceptions)
- 메서드가 단 하나의 검사 예외만을 던질 때

## Checked Exception을 피하는 방법
### Optional 반환하기
// [item 55](../chapter08/item55.md)  
단점: 예외가 발생한 이유를 알려주는 부가 정보를 담을 수 없다.

### 검사 예외를 던지는 메서드를 2개로 쪼개 비검사 예외로 바꾸기
```java
// 검사 예외를 던지는 메서드 - 리팩터링 전
try {
    obj.action(args);
} catch (TheCheckedException e) {
    ... // 예외 상황에 대처한다.
}
```

```java
// 상태 검사 메서드와 비검사 예외를 던지는 메서드 - 리팩터링 후
if (obj.actionPermitted(args)) {
    obj.action(args);
} else {
    ... // 예외 상황에 대처한다.
}
```
이 리팩터링이 적절치 않을 때는 다음과 같다.
- 멀티 쓰레드 환경에서 동기화 문제가 발생할 때
- 외부 요인에 의해 actionPermitted 의 결과가 달라질 수 있을 때
- actionPermitted 의 연산으로 인한 병목이 심할 때

```java
obj.action(args);
```
실패 시 스레드를 중단하길 원한다면 위 처럼 한 줄로 작성해도 상관 없다.

## 핵심 정리
- 꼭 필요한 곳에만 사용한다면 검사 예외는 프로그램의 안전성을 높여주지만, 남용하면 쓰기 고통스러운 API를 낳는다.
- API 호출자가 예외 상황에서 복구할 방법이 없다면 비검사 예외를 던지자.
- 복구가 가능하고 호출자가 그 처리를 해주길 바란다면, 우선 옵셔널을 반환해도 될지 고민하자.
- 옵셔널만으로는 상황을 처리하기에 충분한 정보를 제공할 수 없을 때만 검사 예외를 던지자.