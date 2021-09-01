# Chapter 9 - 리팩터링, 테스팅, 디버깅

## 가독성과 유연성을 개선하는 리팩터링

### 코드 가독성 개선

- 익명 클래스를 람다 표현식으로 리팩터링하기
  1.  익명 클래스에서 this는 익명 클래스 자신을 가리키지만 람다에서 this는 람다를 감싸는 클래스를 가리킨다.
  2.  익명 클래스는 감싸고 있는 클래스 변수를 가릴 수 있다.
  3.  익명 클래스는 인스턴스화 할 때 명시적으로 형식이 정해지는 반면 람다의 형식은 콘텍스트에 따라 달라진다.

```java
Runnable r1 = new Runnable() { // 익명 클래스를 사용한 이전 코드
    public void run() {
        System.out.println("Hello");
    }
};
Runnable r2 = () -> System.out.println("Hello"); // 람다를 사용한 최신 코드

int a = 10;
Runnable r3 = () -> {
    int a = 2; // 컴파일 에러(위 2번 사항 참고)
    System.out.println(a);
};
Runnable r4 = new Runnable() {
    public void run() {
        int a = 2; // 모든 것이 잘 작동함.
        System.out.println(a);
    }
}
```

```java
interface Task {
    public void execute();
}
public static void doSomething(Runnable r) { r.run(); }
public static void doSomething(Task a) { r.execute(); }

doSomething(new Task() { // 익명 클래스 전달
    public void execute() {
        System.out.println("Danger danger!!");
    }
});
doSomething(() -> System.out.println("Danger danger!!")); // 콘텍스트에 따른 모호함 발생

doSomething((Task)() -> System.out.println("Danger danger!!")); // 명시적 형변환을 이용해서 모호함 제거
```

- 람다 표현식을 메서드 참조로 리팩터링하기

```java
Map<CaloricLevel, List<Dish>> dishesByCaloricLevel =
    menu.stream()
        .collect(
            groupingBy(dish -> {
                if (dish.getCalories() <= 400) return CaloricLevel.DIET;
                else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
                else return CaloricLevel.FAT;
            })
        );

Map<CaloricLevel, List<Dish>> dishesByCaloricLevel =
    menu.stream().collect(groupingBy(Dish::getCaloricLevel)); // 람다 표현식을 메서드로 추출
```

```java
inventory.sort((Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight())); // 비교 구현에 신경 써야 하는 코드
inventory.sort(comparing(Apple::getWeight)); // 코드가 문제 자체를 설명한다.
```

- 명령형 데이터 처리를 스트림으로 리팩터링하기

```java
// 다음 코드는 두 가지 패턴(필터링과 추출)으로 엉킨 코드
List<String> dishNames = new ArrayList<>();
for(Dish dish : menu) {
    if(dish.getCalories() > 300) {
        dishNames.add(dish.getName());
    }
}

// 다음과 같이 Stream API를 사용하면 문제를 직접적으로 기술할 수 있고 쉽게 병렬화가 가능하다.
menu.parallelStream()
    .filter(d -> d.getCalories() > 300)
    .map(Dish::getName)
    .collect(toList());
```

### 코드 유연성 개선

람다 표현식을 이용하면 동작 파라미터화(behavior parameterzation)을 쉽게 구현할 수 있다. 람다 표현식을 사용하기 위해 함수형 인터페이스에 적용해야 한다.

1. 조건부 연기 실행: 코드 내부에 제어 흐름문이 복잡하게 얽힌 코드를 볼 수 있다. 불필요한 if문을 제거할 수 있으며 코드 가독성이나 캡슐화도 강화된다.
2. 실행 어라운드: 매번 같은 준비, 종료 과정을 반복적으로 수행한다면 이를 람다로 변환할 수 있다.

## 람다로 객체지향 디자인 패턴 리팩터링하기

### 전략(Strategy)

전략 패턴은 한 유형의 알고리즘을 보유한 상태에서 런타임에 적절한 알고리즘을 선택하는 기법이다. 전략을 구현하는 새로운 클래스를 람다 표현식을 통해 직접 전달할 수 있다.

### 템플릿 메서드(template method)

알고리즘의 개요를 제시한 다음에 알고리즘의 일부를 고칠 수 있는 유연함을 제공해야 할 때 템플릿 메서드 디자인 패턴을 사용한다. 추상 메서드로 원하는 동작을 구현하는 곳을 람다 표현식을 통해 전달할 수 있다.

### 옵저버(observer)

어떤 이벤트가 발생했을 때 한 객체(subject)가 다른 객체 리스터(observer)에 자동으로 알림을 보내야 하는 상황에서 사용하는 패턴이다.

```java
interface Observer {
    void notifiy(String tweet);
}
```

Observer 인터페이스는 새로운 트윗이 있을 때 subject가 호출할 수 있도록 notify라고 하는 하나의 메서드를 제공한다. Observer 인터페이스를 구현하는 클래스를 만드는 대신 람다 표현식을 직접 전달해서 실행할 동작을 지정할 수 있다.

하지만, 옵저버가 상태를 가지며, 여러 메서드를 정의하는 등 복잡하다면 람다 표현식보다 기존의 클래스 구현방식을 고수하는 것이 바람직할 수 있다.

### 의무 체인(chain-of-responsibility)

작업 처리 객체의 체인(동작 체인 등)을 만들 때는 의무 체인 패턴을 사용한다. 한 객체가 어떤 작업을 처리한 다음에 다른 객체로 결과를 전달하고, 다른 객체도 해야할 작업을 처리한 다음에 또 다른 객체로 전달하는 식이다.

```java
public abstract class ProcessingObject<T> {
    protected ProcessingObject<T> successor;
    public void setSuccessor(ProcessingObject<T> successor) {
        this.successor = successor;
    }

    public T handle(T input) {
        T r = handleWork(input);
        if (successor != null) {
            return successor.handle(r);
        }
        return r;
    }

    abstract protected T handleWork(T input);
}
```

ProcessingObject 클래스를 상속받아 handleWork 메서드를 구현하여 다양한 종류의 작업 처리 객체를 만들 수 있다. 이때 작업 처리 객체를 UnaryOperator 형식의 인스턴스로 표현하여 사용할 수 있다.

### 팩토리(factory)

인스턴스화 로직을 클라이언트에게 노출하지 않고 객체를 만들 때 팩토리 디자인 패턴을 사용한다. 분기문을 통해 상황에 맞는 객체를 만들어 전달하는 것 대신 input을 키 Supplier를 값으로 갖는 Map을 만들고, 그리고 Supplier로는 생성자 메서드 참조를 사용하여 팩토리 패턴을 사용할 수 있다.

```java
final static Map<String, Supplier<Product>> map = new HashMap<>();
static {
    map.put("loan", Loan::new);
    map.put("stock", Stock::new);
    map.put("bond", Bond::new);
}
```

## 람다 테스팅

람다에 대해서도 단위 테스팅(unit testing)이 작성되어야만 한다. 하지만 람다는 익명이므로 테스트 코드 이름을 호출할 수 없다. 대신 람다 표현식은 함수형 인터페이스의 인스턴스를 생성한다. 따라서 인스턴스의 동작으로 람다 표현식을 테스트할 수 있다.

람다의 목표는 정해진 동작을 다른 메서드에서 사용할 수 있도록 하나의 조각으로 캡슐화하는 것이다. 그러려면 세부 구현을 포함하는 람다 표현식을 공개하지 말아야 한다.

결국 람다에 대한 테스팅은 세세한 람다의 동작 스텝 과정마다의 산출물이 아닌 전체 람다 표현식에 대하여 입력한 input과 기대하는 output이 잘 반환되는지에 대한 테스트가 필요하다.

복잡한 람다 표현식이라면 개별 메서드로 분할하여 테스트 단위를 잘게 쪼개는 것이다. 또한 함수를 인수로 받거나 다른 함수를 반환하는 메서드라면 테스트 내에서 또 다른 람다를 사용해서 테스트하자.

## 디버깅

### 스택 트레이스 확인

람다 표현식은 이름이 없기 때문에 조금 복잡한 스택 트레이스가 생성된다. 그렇기에 람다 표현식과 관련된 스택 트레이스는 이해하기 어려울 수 있다. 이는 미래의 자바 컴파일러가 개선해야 할 부분이다.

### 정보 로깅

forEach를 통해 스트림 결과를 출력하거나 로깅할 수 있다. 하지만 forEach는 스트림을 소비하는 연산이다. 스트림 파이프라인에 적용된 각각의 연산의 결과를 확인할 수 있다면 대신 peek라는 스트림 연산을 활용할 수 있다. peek는 스트림의 각 요소를 소비한 것 처럼 동작을 실행하지만, 실제로 스트림을 소비하지 않고 자신이 확인한 요소를 파이프라인의 다음 연산 그대로 전달한다.

## 📌 정리

- 람다 표현식으로 가독성이 좋고 더 유연한 코드를 만들 수 있다.
- 익명 클래스는 람다 표현식으로 바꾸는 것이 좋다. 하지만 이때 this, 변수 섀도 등 미묘하게 의미상 다른 내용이 있음을 주의하자.
- 메서드 참조로 람다 표현식보다 더 가독성이 좋은 코드를 구현할 수 있다.
- 반복적으로 컬렉션을 처리하는 루틴은 스트림 API로 대체할 수 있을지 고려하는 것이 좋다.
- 람다 표현식으로 전략, 템플릿 메서드, 옵저버, 의무 체인, 팩토리 등의 객체지향 디자인 패턴에서 발생하는 불필요한 코드를 제거할 수 있다.
- 람다 표현식도 단위 테스트를 수행할 수 있다. 하지만 람다 표현식 자체를 테스트하는 것 보다는 람다 표현식이 사용되는 메서드의 동작을 테스트하는 것이 바람직하다.
- 복잡한 람다 표현식은 일반 메서드로 재구현할 수 있다.
- 람다 표현식을 사용하면 스택 트레이스를 이해하기 어려워진다.
- 스트림 파이프라인에서 요소를 처리할 때 peek 메서드로 중간값을 확인할 수 있다.
