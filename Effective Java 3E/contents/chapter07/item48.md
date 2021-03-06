# 아이템 48. 스트림 병렬화는 주의해서 적용하라
## 자바의 동시성
- Java 초기 릴리즈: 스레드, 동기화, wait/notify
- Java 5: `java.util.concurrent`, 실행자(Executor) 프레임워크
- Java 7: fork-join 패키지
- Java 8: `parallel` 메서드만 한 번 호출하면 파이프라인을 병렬 실행할 수 있는 스트림을 지원

동시성 프로그래밍을 할 때는 안전성(safety)과 응답 가능(liveness) 상태를 유지하기 위해 애써야 한다.

## 파이프라인 병렬화
```java
// 스트림을 사용해 처음 20개의 메르센 소수를 생성하는 프로그램
public static void main(String[] args) {
    primes().map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
        .filter(mersenne -> mersenne.isProbablePrime(50))
        .limit(20)
        .forEach(System.out::println);
}

static Stream<BigInteger> primes() {
    return Stream.iterable(TWO, BigInteger::nextProbablePrime);
}
```
만약 이 코드에서 parellel()를 호출 한다면, 이 프로그램은 강제 종료할 때까지 아무 결과도 출력하지 않으며 CPU는 90%나 잡아먹는 상태가 무한히 계속 된다.

이는 스트림 라이브러리가 이 파이프라인을 병렬화하는 방법을 찾아내지 못했기 때문이다. 환경이 아무리 좋더라도 **데이터 소스가 `Stream.iterate`거나 중간 연산으로 `limit`를 쓰면 파이프라인 병렬화로는 성능 개선을 기대할 수 없다.**

이 뿐만 아니라, 이 코드의 경우 새롭게 메르센 소수를 찾을 때마다 그 전 소수를 찾을 때보다 두 배 정도 더 오래 걸린다. 원소 하나를 계산하는 비용이 대략 그 이전까지의 원소 전부를 계산한 비용을 합친 것만큼 든다는 뜻이다.

따라서 스트림 파이프라인을 마구잡이로 병렬화 하면 안 된다. 성능이 오히려 끔찍하게 나빠질 수도 있다.

## 병렬화의 효과를 받을 수 있는 스트림의 소스
**스트림의 소스가 ArrayList, HashMap, HashSet, ConcurrentHashMap의 인스턴스거나 배열, int 범위, long 범위일 때 병렬화의 효과가 가장 좋다.**  
→ 이 자료구조들은 모두 데이터를 원하는 크기로 정확하고 손쉽게 나눌 수 있어서 일을 다수의 스레드에 분배하기에 좋다는 특징이 있다.

또한 **원소들을 순차적으로 실행할 때의 참조 지역성(locality of reference)이 뛰어나다는 공통점이 있다.** 이는 이웃한 원소의 참조들이 메모리에 연속해서 저장되어 있다는 뜻이다.  
→ **참조 지역성은 다량의 데이터를 처리하는 벌크 연산을 병렬화할 때 아주 중요한 요소로 작용한다.**

(참조 지역성이 가장 뛰어난 자료구조는 기본 타입의 배열이다. 이는 데이터 자체가 메모리에 연속해서 저장되기 때문이다.)

## 스트림 파이프라인의 종단 연산의 동작 방식과 병렬 수행
종단 연산에서 수행하는 작업량이 파이프라인 전체 작업에서 상당 비중을 차지하면서 순차적인 연산이라면 파이프라인 병렬 수행의 효과는 제한될 수 밖에 없다. **종단 연산 중 병렬화에 가장 적합한 것은 축소(reduction)다.**

축소는 파이프라인에서 만들어진 모든 원소를 하나로 합치는 작업으로, Stream의 reduce 메서드 중 하나, 혹은 min, max, count, sum 같이 완성된 형태로 제공되는 메서드 중 하나를 선택해 수행한다. anyMatch, allMatch, noneMatch처럼 조건에 맞으면 바로 반환되는 메서드도 병렬화에 적합하다.

반면, 가변 축소(mutable reduction)를 수행하는 Stream의 collect 메서드는 병렬화에 적합하지 않다. 이는 컬렉션들을 합치는 부담이 크기 때문이다.

## 스트림 병렬화
**스트림을 잘못 병렬화하면 (응답 불가를 포함해) 성능이 나빠질 뿐만 아니라 결과 자체가 잘못되거나 예상 못한 동작이 발생할 수 있다.**

결과가 잘못되거나 오동작하는 것은 안전 실패(safety failure)라 한다. 안전 실패는 병렬화한 파이프라인이 사용하는 mappers, filters, 혹은 프로그래머가 제공한 다른 함수 객체가 명세대로 동작하지 않을 때 벌어진다.

Stream 명세는 이때 사용되는 함수 객체에 관한 규약을 정의해놨다.

Strema의 reduce 연산에 건네지는 accumulator, combiner 함수는 반드시 결합법칙을 만족하고, 간섭받지 않고, 상태를 갖지 않아야 한다.

출력 순서를 순차 버전처럼 정렬하고 싶다면 종단 연산 forEach를 forEachOrdered로 바꿔주면 된다. 이 연산은 병렬 스트림들을 순회하며 소수를 발견한 순서대로 출력되도록 보장해줄 것이다.

### 스트림 병렬화의 필요성?
**스트림 병렬화는 오직 성능 최적화 수단임을 기억해야 한다.** 다른 최적화와 마찬가지로 변경 전후로 반드시 성능을 테스트하여 병렬화를 사용할 가치가 있는지 확인해야 한다. 보통은 병렬 스트림 파이프라인도 공통의 포크-조인 풀에서 수행되므로(즉, 같은 스레드 풀을 사용하므로), 잘못된 파이프라인 하나가 시스템의 다른 부분의 성능에까지 악영향을 줄 수 있음을 유념하자.

### 스트림 병렬화 예시
**조건이 잘 갖춰지면 `parallel` 메서드 호출 하나로 거의 프로세스 코어 수에 비례하는 성능 향상을 만끽할 수 있다.**

다음은 π(n), 즉 n보다 작거나 같은 소수의 개수를 계산하는 함수다.
```java
// 소수 계산 스트림 파이프라인 - 병렬화에 적합하다.
static log pi(long n) {
    return LongStream.rangeClosed(2, n)
        .mapToObj(BigInteger::valueOf)
        .filter(i -> i.isProbablePrime(50))
        .count();
}

// 소수 계산 스트림 파이프라인 - 병렬화 버전(parallel 추가)
static log pi(long n) {
    return LongStream.rangeClosed(2, n)
        .parallel()
        .mapToObj(BigInteger::valueOf)
        .filter(i -> i.isProbablePrime(50))
        .count();
}
```
`parallel()`을 추가하면 병렬화 덕분에 더 빨라졌다. 만약 n이 크다면 레머의 공식이라는 알고리즘을 사용하자.

무작위 수들로 이뤄진 스트림을 병렬화하려거든 ThreadLocalRandom 보다는 SplittableRandom 인스턴스를 이용하자. 병렬화할 시 성능이 선형으로 증가한다. ThreadLocalRandom은 단일 스레드에서 쓰고자 만든것. 병렬 스트림용으로는 사용할 수 있지만 SplittableRandom보다는 느릴 것이다.

Random은 모든 연산을 동기화하기 때문에 병렬 처리하면 최악의 성능을 보일 것이다.

## 핵심 정리
- 계산도 올바로 수행하고 성능도 빨라질 거라는 확신 없이는 스트림 파이프라인 병렬화는 시도조차 하지 말라.
- 스트림을 잘못 병렬화하면 프로그램을 오동작하게 하거나 성능을 급격히 떨어뜨린다.
- 병렬화하는 편이 낫다고 믿더라도, 수정 후의 코드가 여전히 정확한지 확인하고 운영 환경과 유사한 조건에서 수행해보며 성능지표를 유심히 관찰하라.
- 그래서 계산도 정확하고 성능이 좋아졌음이 확실해졌을 때, 오직 그럴 때만 병렬화 버전 코드를 운영 코드에 반영하라.