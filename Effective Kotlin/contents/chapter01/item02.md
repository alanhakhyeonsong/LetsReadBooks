# 아이템 2. 변수의 스코프를 최소화하라
상태를 정의할 때는 변수와 프로퍼티의 스코프를 최소화하는 것이 좋다.

- 프로퍼티보단 지역 변수를 사용하는 것이 좋다.
- 최대한 좁은 스코프를 갖게 변수를 사용한다. 예를 들어 반복문 내부에서만 변수가 사용된다면, 변수를 반복문 내부에 작성하는 것이 좋다.

요소의 스코프라는 것은 요소를 볼 수 있는 컴퓨터 프로그램 영역이다.

- 코틀린의 스코프는 기본적으로 중괄호로 만들어지며, 내부 스코프에서 외부 스코프에 있는 요소에만 접근할 수 있다.

```kotlin
val a = 1
fun fizz() {
    val b = 2
    print(a + b)
}
val buzz = {
    val c = 3
    print(a + c)
}
// 이 위치에선 a를 사용할 수 있지만, b와 c는 사용할 수 없다.
```

아래는 변수 스코프를 제한하는 예시다.

```kotlin
// 나쁜 예
var user: User
for (i in users.indices) {
    user = users[i]
    print("User at $i is $user")
}

// 조금 더 좋은 예
for (i in users.indices) {
    val user = users[i]
    print("User at $i is $user")
}

// 제일 좋은 예
for ((i, user) in users.withIndex()) {
    print("User at $i is $user")
}
```

스코프를 좁게 만드는 것이 가장 중요한 이유는 **프로그램을 추적하고 관리하기 쉽기 때문이다.** 스코프 범위가 너무 넓으면, 다른 개발자에 의해 변수가 잘못 사용될 수 있다.

변수는 변수를 정의할 때 초기화되는 것이 좋다. if, when, try-catch, Elvis 표현식 등을 활용하면, 최대한 변수를 정의할 때 초기화할 수 있다.

```kotlin
// 나쁜 예
val user: User

if (hasValue) {
   user = getValue()
} else {
   user = User()
}

// 조금 더 좋은 예
val user: User = if (hasValue) {
    getValue()
} else {
    User()
}

// 나쁜 예
fun updateWeather(degrees: Int) {
    val description: String
    val color: String
    if (degrees < 5) {
        description = "cold"
        color = "BLUE"
    } else if (degrees < 23) {
        description = "mild"
        color = "YELLOW"
    } else {
        description = "hot"
        color = "RED"
    }
}

// 조금 더 좋은 예
fun updateWeather(degrees: Int) {
    val (description, color) = when {
        degrees < 5 -> "color" to "BLUE"
        degrees < 23 -> "mild" to "YELLOW"
        else -> "hot" to "RED"
    }
}
```

## 캡처링
에라토스테네스의 체를 통해 캡처링 이슈를 파악해보자.

```kotlin
var numbers = (2..100).toList()
val primes = mutableListOf<Int>()
while (numbers.isNotEmpty()) {
    val prime = numbers.first()
    primes.add(prime)
    numbers = numbers.filter { it % prime != 0 }
}
print(primes)

// 시퀀스 활용
val primes: Sequence<Int> = sequence {
    var numbers = generateSequence(2) { it + 1 }

    while (true) {
        val prime = numbers.first()
        yield(prime)
        numbers = numbers.drop(1).filter { it % prime != 0 }
    }
}

print(primes.take(10).toList())
```

잘못 활용한 예는 아래와 같다.

```kotlin
val primes: Sequence<Int> = sequence {
    var numbers = generateSequence(2) { it + 1 }
    var prime: Int

    while (true) {
        prime = numbers.first()
        yield(prime)
        numbers = numbers.drop(1).filter { it % prime != 0 }
    }
}

print(primes.take(10).toList()) // [2, 3, 5, 6, 7, 8, 9, 10, 11, 12]
```

위 결과가 잘못 나온 이유는 다음과 같다.

- 필터링은 시퀀스를 사용하기 때문에 나중에 실행되는데, 모든 스텝에서 점점 필터가 체이닝되는데 위 코드에선 항상 변경 가능한 `prime`을 참조하게 된다.
- 따라서 항상 가장 마지막의 `prime` 값으로만 필터링 된다.

이러한 문제가 발생할 수 있으므로, 항상 잠재적인 캡처 문제를 주의해야 한다. 가변성을 피하고 스코프 범위를 좁게 만들면, 이런 문제를 간단하게 피할 수 있다.

## 정리
- 여러 가지 이유로 변수의 스코프는 좁게 만들어 활용하는 것이 좋다.
- 또한 `var` 보단 `val`을 사용하는 것이 좋다.
- 람다에서 변수를 캡처한다는 것을 꼭 기억하자. 간단한 규칙만 지켜주면, 발생할 수 있는 여러 문제를 차단할 수 있다.