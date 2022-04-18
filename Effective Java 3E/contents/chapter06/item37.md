# 아이템 37. ordinal 인덱싱 대신 EnumMap을 사용하라

## 식물의 생애주기 예제
이따금 열거 타입의 ordinal을 배열의 인덱스로 사용하는 경우가 있다. 식물의 생애주기를 열거 타입으로 표현한 `LifeCycle` 열거 타입의 예시로 살펴보자.

```java
class Plant {
    enum LifeCycle { ANNUAL, PERENNIAL, BIENNIAL }

    final String name;
    final LifeCycle lifeCycle;

    Plant(String name, LifeCycle lifeCycle) {
        this.name = name;
        this.lifeCycle = lifeCycle;
    }

    @Override
    public String toString() {
        return name;
    }
}
```

이제 정원에 심은 식물들을 배열 하나로 관리하고, 이들을 생애주기(한해살이, 여러해살이, 두해살이) 별로 묶어보자. 생애주기별로 총 3개의 집합을 만들고 정원을 한 바퀴 돌며 각 식물을 해당 집합에 넣는다.

```java
public static void usingOrdinalArray(List<Plant> garden) {
    Set<Plant>[] plantsByLifeCycle = (Set<Plant>[]) new Set[Plant.LifeCycle.values().length];

    for (int i = 0; i < plantsByLifeCycle.length; i++) {
        plantsByLifeCycle[i] = new HashSet<>();
    }

    for (Plant p : garden) {
        plantsByLifeCycle[p.lifeCycle.ordinal()].add(p);
    }

    // 결과 출력
    for (int i = 0; i < plantsByLifeCycle.length; i++) {
        System.out.printf("%s: %s%n", Plant.LifeCycle.values()[i], plantsByLifeCycle[i]);
    }
}
```

위 코드는 동작은 하지만 문제가 한가득이다.

1. 배열은 제네릭과 호환되지 않으니(item 28) 비검사 형변환을 수행해야 하고 깔끔히 컴파일되지 않을 것이다.
2. 배열은 각 인덱스의 의미를 모르니 출력 결과에 직접 레이블을 달아야 한다.
3. 가장 심각한 문제는 정확한 정숫값을 사용한다는 것을 직접 보증해야 한다. 정수는 열거 타입과 달리 타입 안전하지 않기 때문이다.

## `EnumMap`으로 해결
`EnumMap`은 열거 타입을 key로 사용하는 `Map` 구현체이다. 이를 사용해서 코드를 바꿔보자.

```java
public static void usingEnumMap(List<Plant> garden) {
    Map<LifeCycle, Set<Plant>> plantsByLifeCycle = new EnumMap<>(LifeCycle.class);

    for (int i = 0; i < plantsByLifeCycle.length; i++) {
        plantsByLifeCycle.put(lifeCycle, new HashSet<>());
    }

    for (Plant p : garden) {
        plantsByLifeCycle.get(plant.lifeCycle).add(plant);
    }

    System.out.println(plantsByLifeCycle);
}
```

위 코드는 이전보다 더 짧고 명료하고 안전하고 성능도 원래 버전과 비등하다.

1. 이전 ordinal을 사용한 코드와 다르게 안전하지 않은 형변환을 사용하지 않는다.
2. 결과를 출력하기 위해 번거롭던 과정도 `EnumMap` 자체가 `toString`을 제공하기 때문에 번거롭지 않게 되었다.
3. ordinal을 이용한 배열 인덱스를 사용하지 않으니 인덱스를 계산하는 과정에서 오류가 날 가능성이 존재하지 않는다.
4. `EnumMap`은 그 내부에서 배열을 사용하기 때문에 **내부 구현 방식을 안으로 숨겨서 `Map`의 타입 안정성과 배열의 성능을 모두 얻어냈다.**

여기서 `EnumMap`의 생성자가 받는 키 타입의 Class 객체는 한정적 타입 토큰으로, 런타임 제네릭 타입 정보를 제공한다.(item 33)

## Stream으로 코드 리팩토링
```java
// EnumMap을 사용하지 않는다.
public static void streamV1(List<Plant> garden) {
    System.out.println(Arrays.stream(garden).collect(Collectors.groupingBy(p -> p.lifeCycle)));
}

// EnumMap을 이용해 데이터와 열거 타입을 매핑했다.
public static void streamV2(List<Plant> garden) {
    System.out.println(Arrays.stream(garden).collect(groupingBy(p -> p.lifeCycle,
        () -> new EnumMap<>(LifeCycle.class), toSet())));
}
```

스트림을 사용하면 `EnumMap`만 사용했을 때와는 살짝 다르게 동작한다.  
`EnumMap` 버전은 언제나 식물의 생애주기당 하나씩의 중첩 맵을 만들지만, 스트림 버전에서는 해당 생애주기에 속하는 식물이 있을 때만 만든다.

## 상태 전이 예제
다음은 두 가지 상태(Phase)를 전이(Transition)와 매핑하도록 구현한 예제이다. LIQUID에서 SOLID로의 전이는 FREEZE가 되고, LIQUID에서 GAS로의 전이는 BOIL이 된다.

```java
public enum Phase {
    SOLID, LIQUID, GAS;

    public enum Transition {
        MELT, FREEZE, BOIL, CONDENSE, SUBLIME, DEPOSIT;

        // 행은 from의 ordinal을, 열은 to의 ordinal을 인덱스로 쓴다.
        private static final Transition[][] TRANSITIONS = {
            { null, MELT, SUBLIME },
            { FREEZE, null, BOIL },
            { DEPOSIT, CONDENSE, null },
        };

        // 한 상태에서 다른 상태로의 전이를 반환한다.
        public static Transition from(Phase from, Phase to) {
            return TRANSITIONS[from.ordinal()][to.ordinal()];
        }
    }
}
```

앞서 보여준 간단한 정원 예제와 마찬가지로 컴파일러는 `ordinal`과 배열 인덱스의 관계를 알 도리가 없다. 즉, `Phase`나 `Phase.Transition` 열거 타입을 수정하면서 상전이 표 `TRANSITION`를 함께 수정하지 않거나 실수로 잘못 수정하면 런타임 오류가 날 것이다.

## 중첩 `EnumMap`으로 리팩토링
```java
public enum Phase {
    SOLID, LIQUID, GAS;

    public enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID), BOIL(LIQUID, GAS),
        CONDENSE(GAS, LIQUID), SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID);

        private final Phase from;
        private final Phase to;

        Transition(Phase from, Phase to) {
            this.from = from,
            this.to = to;
        }

        // 상 전이 맵을 초기화
        private static final Map<Phase, Map<Phase, Transition>> m = Stream.of(values())
                                .collect(groupingBy(t -> t.from,
                                    () -> new EnumMap<>(Phase.class),
                                    toMap(t -> t.to, t -> t,
                                        (x, y) -> y, () -> new EnumMap<>(Phase.class))));

        public static Transition from(Phase from, Phase to) {
            return m.get(from).get(to);
        }
    }
}
```

`Map<Phase, Map<Phase, Transition>>`은 "이전 상태에서 '이후 상태에서 전이로의 맵'에 대응 시키는 맵" 이라는 뜻이다.  
이러한 맵의 맵을 초기화하기 위해 수집기(`java.util.stream.Collector`) 2개를 차례로 사용했다.
1. `groupingBy` : 전이를 이전 상태를 기준으로 묶는다.
2. `toMap` : 이후 상태를 전이에 대응시키는 `EnumMap`을 생성한다.

두 번째 수집기의 병합 함수인 `(x, y) -> y`는 선언만 하고 실제로는 쓰이지 않는데, 이는 단지 `EnumMap`을 얻으려면 맵 팩터리가 필요하고 수집기들은 점층적 팩터리를 제공하기 때문이다.

이제 여기에 새로운 상태인 플라즈마(PLASMA)를 추가해보자. 이 상태와 연결된 전이는 2개(IONIZE, DEIONIZE)이다.

```java
public enum Phase {
    SOLID, LIQUID, GAS, PLASMA;

    public enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID), BOIL(LIQUID, GAS),
        CONDENSE(GAS, LIQUID), SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID),
        IONIZE(GAS, PLASMA), DEIONIZE(PLASMA, GAS);
        
        // 나머지 코드는 그대로다.
    }
}
```

## 핵심 정리
- **배열의 인덱스를 얻기 위해 ordinal을 쓰는 것은 일반적으로 좋지 않으니, 대신 `EnumMap`을 사용하라.**
- 다차원 관계는 `EnumMap<..., EnumMap<...>>`으로 표현하라.
- "애플리케이션 프로그래머는 `Enum.ordinal`을 (웬만해서는) 사용하지 말아야한다.(item 35)"는 일반 원칙의 특수한 사례다.