# Chapter 11 - null 대신 Optional 클래스
## 값이 없는 상황을 어떻게 처리할까?
### 보수적인 자세로 `NullPointerException` 줄이기
```java
public String getCarInsuranceName(Person person) {
    if (person != null) {
        Car car = person.getCar();
        if (car != null) {
            Insurance insurance = car.getInsurance();
            if (insurance != null) {
                return insurance.getName();
            }
        }
    }
    return "Unknown";
}
```

위 예제 코드처럼 `null` 확인 코드를 추가해서 NPE 문제를 해결한다. 변수를 접근할 때마다 중첩된 if가 추가되면서 코드 들여쓰기 수준이 증가한다. 이와 같은 반복 패턴 코드를 **'깊은 의심'**이라 한다. 이를 반복하다보면 코드의 구조가 엉망이 되고 가독성도 떨어진다.

### null 때문에 발생하는 문제
- 에러의 근원: `NullPointerException`은 Java에서 가장 흔히 발생하는 에러다.
- 코드를 어지럽힌다: 중첩된 `null` 확인 코드를 추가해야 하므로 `null` 때문에 코드 가독성이 떨어진다.
- 아무 의미가 없다: `null`은 아무 의미도 표현하지 않는다. 특히 정적 형식 언어에서 값이 없음을 표현하는 방법으로는 적절하지 않다.
- 자바 철학에 위배된다: Java는 개발자로부터 모든 포인터를 숨겼다. 하지만 예외가 있는데 그것이 바로 `null` 포인터다.
- 형식 시스템에 구멍을 만든다: `null`은 무형식이며 정보를 포함하고 있지 않으므로 모든 참조 형식에 `null`을 할당할 수 있다. 이런 식으로 `null`이 할당되기 시작하면서 시스템의 다른 부분으로 `null`이 퍼졌을 때 애초에 `null`이 어떤 의미로 사용되었는지 알 수 없다.

### 다른 언어는 null 대신 무얼 사용하나?
최근 Groovy 같은 언어에선 안전 내비게이션 연산자(safe navigation operator) `?.`를 도입해서 `null` 문제를 해결했다.

```groovy
def carInsuranceName = person?.car?.insurance?.name
```

## Optional 클래스 소개
Java 8은 하스켈과 스칼라의 영향을 받아 `java.util.Optional<T>`라는 새로운 클래스를 제공한다. **`Optional`은 선택형 값을 캡슐화하는 클래스다.**

값이 있으면 `Optional` 클래스는 값을 감싼다. 반면 값이 없으면 `Optional.empty` 메서드로 `Optional`을 반환한다. `Optional.empty`는 `Optional`의 특별한 싱글턴 인스턴스를 반환하는 정적 팩토리 메서드다.

null을 참조하려 하면 NPE가 발생하지만, `Optional.empty()`는 `Optional` 객체이므로 이를 다양한 방식으로 활용할 수 있다.

`Optional` 클래스를 사용하면 모델의 의미가 더 명확해진다. 변수가 `Optional`일 경우 그 값을 가질 수도 있고 가지지 않을 수도 있다. `Optional`의 역할은 더 이해하기 쉬운 API를 설계하도록 돕는 것이다. 즉, 메서드의 시그니처만 보고도 선택형값인지 여부를 구별할 수 있다. `Optional`이 등장하면 이를 언랩해서 값이 없을 수 있는 상황에 적절하게 대응하도록 강제하는 효과가 있다.

## Optional 적용 패턴
### Optional 객체 만들기
- 빈 `Optional`
```java
Optional<Car> optCar = Optional.empty();
```

- `null`이 아닌 값으로 `Optional` 만들기
```java
Optional<Car> optCar = Optional.of(car); // car가 null이면 즉시 NPE가 발생한다.
```

- `null`값으로 `Optional` 만들기
```java
Optional<Car> optCar = Optional.ofNullable(car); // car가 null이면 빈 Optional 객체가 반환됨.
```

### 맵으로 Optional의 값을 추출하고 변환하기
```java
Optional<Insurance> optInsurance = Optional.ofNullable(insurance);
Optional<String> name = optInsurance.map(Insurance::getName());
```

`Optional`의 `map` 메서드는 스트림의 `map` 메서드와 개념적으로 비슷하다. `Optional`은 최대 요소의 개수가 한 개 이하인 데이터 컬렉션으로 생각할 수 있다. `Optional`이 값을 포함하면 `map`의 인수로 제공된 함수가 값을 바꾼다. 반면, `Optional`이 비어있으면 아무 일도 일어나지 않는다.

### flatMap으로 Optional 객체 연결
```java
Optional<Person> optPerson = Optional.of(person);
Optional<String> name = optPerson.map(Person::getCar)
                                 .map(Car::getInsurance)
                                 .map(Insurance::getName);
```

위 코드는 컴파일되지 않는다. `map` 메서드로 반환되는 값은 `Optional`로 감싸진다. 이는 결과적으로 `Optional<Optional>` 처럼 중첩된 구조로 반환된다. 이를 해결하고자 한다면 `flatMap`을 사용하자. `Optional`의 `flatMap` 메서드는 전달된 `Optional` 객체의 요소에 대해 새로운 `Optional`로 반환해준다.

```java
Optional<Person> optPerson = Optional.of(person);
String name = optPerson.flatMap(Person::getCar)
                                 .flatMap(Car::getInsurance)
                                 .map(Insurance::getName)
                                 .orElse("Unknown");
```

### 도메인 모델에 Optional을 사용했을 때 데이터를 직렬화할 수 없는 이유
`Optional` 클래스는 필드 형식으로 사용할 것을 가정하지 않았으므로 `Serializable` 인터페이스를 구현하지 않았다. 따라서 도메인 모델에 `Optional`을 사용한다면 직렬화 모델을 사용하는 도구나 프레임워크에서 문제가 생길 수 있다. 만약 직렬화 모델이 필요하다면 변수는 일반 객체로 두되, `Optional`로 값을 반환받을 수 있는 메서드를 추가하는 방식이 권장된다.

```java
public class Person {
	private Car car;
	public Optional<Car> getCarAsOptional() {
		return Optional.ofNullable(car);
	}
}
```

### Optional 스트림 조작
Java 9에선 `Optional`을 포함하는 스트림을 쉽게 처리할 수 있도록 `Optional`에 `stream()` 메서드를 추가했다. `Optional` 스트림을 값을 가진 스트림으로 변환할 때 이 기능을 유용하게 활용할 수 있다.

```java
public Set<String> getCarInsuranceName(List<Person> persons) {
    return persons.stream()
                  .map(Person::getCar)
                  .map(optCar -> optCar.flatMap(Car::getInsurance))
                  .map(optIns -> optIns.map(Insurance::getName))
                  .flatMap(Optional::stream)
                  .collect(toSet());
}
```

### 디폴트 액션과 Optional 언랩
- `get()`: 값을 읽는 가장 간단한 메서드면서 동시에 가장 안전하지 않은 메서드다. 값이 없으면 `NoSuchElementException`을 발생시킨다. `Optional`에 값이 반드시 있다고 가정할 수 있는 상황이 아니면 `get` 메서드를 사용하지 않는 것이 바람직하다.
- `orElse(T other)`: `Optional`이 값을 포함하지 않을 때 기본값을 제공할 수 있다.
- `orElseGet(Supplier<? extends T> other)`: `orElse` 메서드에 대응하는 게으른 버전의 메서드다. `Optional`에 값이 없을 대만 `Supplier`가 실행된다.
- `orElseThrow(Supplier<? extends X> exceptionSupplier)`: `Optional`이 비어있을 때 예외를 발생시킨다는 점에서 `get` 메서드와 비슷하다. 하지만 이 메서드는 발생시킬 예외의 종류를 선택할 수 있다.
- `ifPresent(Consumer<? super T> consumer)`: 값이 존재할 때 인수로 넘겨준 동작을 실행할 수 있다. 값이 없으면 아무 일도 일어나지 않는다.
- `ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction)`: Java 9에서 추가된 이 메서드는 `Optional`이 비었을 때 실행할 수 있는 `Runnable`을 인수로 받는다는 점만 `ifPresent`와 다르다.

### 두 Optional 합치기
`Optional`에서 `map`과 `flatMap`은 `Optional`이 비어있다면 빈 `Optional`을 반환한다. 두 `Optional`에 대한 연산을 이 메서드들을 적절히 활용하여 수행할 수 있다.

```java
public Insurance findCheapestInsurance(Person person, Car car) {
    // 다양한 보험회사가 제공하는 서비스 조회
    // 모든 결과 데이터 비교
    return cheapestCompany;
}

// 기존 메서드
public Optional<Insurance> nullSafeFindCheapestInsurance(Optional<Person> person, Optional<Car> car) {
    if (person.isPresent() && car.isPresent()) {
        return Optional.of(findCheapestInsurance(person.get(), car.get()));
    } else {
        return Optional.empty();
    }
}

// 리팩토링 후
public Optional<Insurance> nullSafeFindCheapestInsurance(Optional<Person> person, Optional<Car> car) {
    return person.flatMap(p -> car.map(c -> findCheapestInsurance(p, c)));
}
```

### 필터로 특정값 거르기
스트림과 비슷하게 `Optional` 객체에 `filter` 메서드를 통하여 특정 조건에 대해 거를 수 있다.

```java
// 기존
Insurance insurance = ...;
if (insurance != null && "CambridgeInsurance".equals(insurance.getName())) {
    System.out.println("ok");
}

// 리팩토링 후
Optional<Insurance> optInsurance = ...;
optInsurance.filter(insurance -> "CambridgeInsurance".equals(insurance.getName()))
            .ifPresent(x -> System.out.println("ok"));
```

## Optional을 사용한 실용 예제
### 잠재적으로 null이 될 수 있는 대상을 Optional로 감싸기
참조하는 객체에 대해 `null`이 될 수 있는 경우가 있다면 `Optional` 객체로 대체한다.

```java
// 기존
Object value = map.get("key");

// 리팩토링 후
Optional<Object> value = Optional.ofNullable(map.get("key"));
```

### 예외와 Optional 클래스
Java API에서 값을 제공할 수 없을 때 `null`을 반환하는 대신 예외를 발생시킬 때가 있다. 이 대신 `Optional.empty()`를 반환하도록 모델링할 수 있다.

```java
public static Optional<Integer> stringToInt(String s) {
    try {
        return Optional.of(Integer.parseInt(s));
    } catch (NumberFormatException e) {
        return Optional.empty();
    }
}
```

### 기본형 Optional을 사용하지 말아야 하는 이유
`Optional`도 스트림처럼 기본형으로 특화된 `OptionalInt`, `OptionalLong`, `OptionalDouble` 등의 클래스를 제공한다. 하지만 `Optional`의 최대 요소 수는 한 개이므로 성능 개선을 할 수 없다. 기본형 특화 `Optional`은 앞에서 살펴본 유용한 메서드인 `map`, `flatMap`, `filter` 등을 지원하지 않으므로 사용을 권장하지 않는다. 게다가 다른 일반 `Optional`과 혼용할 수 없다.

## 📌 정리
- 역사적으로 프로그래밍 언어에서는 `null` 참조로 값이 없는 상황을 표현해왔다.
- Java 8에선 값이 있거나 없음을 표현할 수 있는 클래스 `java.util.Optional<T>`를 제공한다.
- 팩토리 메서드 `Optional.empty`, `Optional.of`, `Optional.ofNullable` 등을 이용해서 `Optional` 객체를 만들 수 있다.
- `Optional` 클래스는 스트림과 비슷한 연산을 수행하는 `map`, `flatMap`, `filter` 등의 메서드를 제공한다.
- `Optional`로 값이 없는 상황을 적절하게 처리하도록 강제할 수 있다. 즉, `Optional`로 예상치 못한 `null` 예외를 방지할 수 있다.
- `Optional`을 활용하면 더 좋은 API를 설계할 수 있다. 즉, 사용자는 메서드의 시그니처만 보고도 `Optional` 값이 사용되거나 반환되는지 예측할 수 있다.