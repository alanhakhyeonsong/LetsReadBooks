# 아이템 45. 스트림은 주의해서 사용하라

스트림 API는 **다량의 데이터 처리 작업(순차적이든 병렬적이든)을 돕고자 Java 8에 추가 되었다.**

- 스트림(stream): 데이터 원소의 유한 혹은 무한 시퀀스(sequence)를 뜻한다.
- 스트림 파이프라인(stream pipeline): 이 원소들로 수행하는 연산 단계를 표현하는 개념

스트림의 원소들은 어디로부터든 올 수 있다. 컬렉션, 배열, 파일, 정규표현식 패턴 매처, 난수 생성기, 다른 스트림 등이 있다. **스트림 안의 데이터 원소들은 객체 참조나 기본 타입 값(int, long, double)이다.**

## 스트림 파이프라인

스트림 파이프라인은 **소스 스트림 → (중간 연산) → 종단 연산**으로 이루어진다.

### 중간 연산

각 중간 연산은 스트림을 어떠한 방식으로 변환한다. 결과 스트림의 원소 타입은 변환 전 스트림의 원소 타입과 같을 수도 있고 다를 수도 있다.

```java
// sorted
Stream<Integer> sorted = operands.stream().sorted();

// filter
Stream<Integer> integerStream = operands.stream().filter((value) -> value > 2);

// map
Stream<Double> doubleStream = operands.stream().map(Double::new);
```

### 종단 연산

마지막 중간 연산이 내놓은 스트림에 최후의 연산을 가한다.  
스트림 파이프라인은 지연 평가(lazy evaluation)된다.

- 종단 연산이 없는 파이프라인은 어떤 연산도 수행되지 않는다.
- 지연 평가는 무한 스트림을 다룰 수 있게 해주는 열쇠다.
- 지연 평가를 하지 않는다면 중간 연산은 끝나지 않는다.

## 스트림은 주의해서 사용해야 한다.

스트림 API는 다재다능하여 사실상 어떠한 계산이라도 해낼 수 있다. 하지만 할 수 있다는 뜻이지, 해야 한다는 뜻은 아니다. **스트림을 제대로 사용하면 프로그램이 짧고 깔끔해지지만, 잘못 사용하면 읽기 어렵고 유지보수도 힘들어진다.** 또한 성능상 좋지 않을수도 있다.

다음은 스트림을 과도하게 사용한 예시이다.

```java
public class Anagrams {
    public static void main(String[] args) throws IOException {
        Path dictionary = Paths.get(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        try (Stream<String> words = Files.lines(dictionary)) {
            words.collect(
                groupingBy(word -> word.chars().sorted()
                    .collect(StringBuilder::new,
                        (sb, c) -> sb.append((char) c),
                        StringBuilder::append).toString())
            )
            .values().stream()
            .filter(group -> group.size() >= minGroupSize)
            .map(group -> group.size() + ": " + group)
            .forEach(System.out::println);
        }
    }
}
```

스트림을 적절히 활용하면 깔끔하고 명료해진다.

```java
public class Anagrams {
    public static void main(String[] args) throws IOException {
        Path dictionary = Paths.get(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        try (Stream<String> words = Files.lines(dictionary)) {
            words.collect(groupingBy(word -> alphabetize(word)))
                .values().stream()
                .filter(group -> group.size() >= minGroupSize)
                .forEach(g -> System.out.println(g.size() + ": " + g));
        }
    }

    // alphabetize 메서드
}
```

또한, 스트림의 변수는 람다형이기 때문에 스트림 변수를 이해하기 쉽게 짓는 것도 중요하다.

참고로 **Java는 char용 스트림을 지원하지 않는다.**

```java
IntStream chars = "Hello".chars(); // IntStream이 반환됨
```

**따라서 기존 코드는 스트림을 사용하도록 리팩터링하되, 새 코드가 더 나아 보일 때만 반영하자.**

## 스트림을 사용하지 못하는 경우(함수 객체 사용 관점)

- 지역변수를 읽고 수정할 필요가 있을 때
- 람다의 변수는 사실상 final 이다.
- 람다는 return, break, continue 문이 불가능하다.

## 스트림이 적절한 경우

- 원소들의 시퀀스를 일관되게 변환한다.
- 원소들의 시퀀스를 필터링한다.
- 원소들의 시퀀스를 하나의 연산을 사용해 결합한다.(더하기, 연결하기, 최솟값 구하기 등)
- 원소들의 시퀀스를 컬렉션에 모은다.(공통된 속성을 기준으로 묶어가며)
- 원소들의 시퀀스에서 특정 조건을 만족하는 원소를 찾는다.

## 스트림으로 처리하기 어려운 경우

원본 스트림을 계속 써야할 때이다. **스트림 파이프라인은 일단 한 값을 다른 값에 매핑하고 나면 원래의 값은 잃는 구조이기 때문이다.**

## 핵심 정리

- 스트림을 사용해야 멋지게 처리할 수 있는 일이 있고, 반복 방식이 더 알맞은 일도 있다.
- 그리고 수많은 작업이 이 둘을 조합했을 때 가장 멋지게 해결된다.
- 어느 쪽을 선택하는 확고부동한 규칙은 없지만 참고할 만한 지침 정도는 있다.
- 어느 쪽이 나은지가 확연히 드러나는 경우가 많겠지만, 아니더라도 방법은 있다.
- **스트림과 반복 중 어느 쪽이 나은지 확신하기 어렵다면 둘 다 해보고 더 나은 쪽을 택하라.**
