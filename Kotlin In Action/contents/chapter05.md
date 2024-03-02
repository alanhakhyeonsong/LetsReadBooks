# 5장. 람다로 프로그래밍
## 람다 식과 멤버 참조
### 람다 소개: 코드 블록을 함수 인자로 넘기기
클래스를 선언하고 그 클래스의 인스턴스를 함수에 넘기는 대신 함수형 언어에선 함수를 직접 다른 함수에 전달할 수 있다. 람다 식을 사용하면 코드가 더욱 더 간결해진다. 람다 식을 사용하면 함수를 선언할 필요가 없고 코드 블록을 직접 함수의 인자로 전달할 수 있다.

### 람다와 컬렉션
코드에서 중복을 제거하는 것은 프로그래밍 스타일을 개선하는 중요한 방법 중 하나다. 컬렉션을 다룰 때 수행하는 대부분의 작업은 몇 가지 일반적인 패턴에 속한다. 따라서 그런 패턴은 라이브러리 안에 있어야 한다. 하지만 람다가 없다면 컬렉션을 편리하게 처리할 수 있는 좋은 라이브러리를 제공하기 힘들다.

```kotlin
data class Person(val name: String, val age: Int)

// 컬렉션 직접 검색
fun findTheOldest(people: List<Person>) {
    var maxAge = 0
    var theOldest: Person? = null
    for (person in people) {
        if (person.age > maxAge) {
            maxAge = person.age
            theOldest = person
        }
    }
    println(theOldest)
}

>>> val people = listOf(Person("Alice", 29), Person("Bob", 31))
>>> findTheOldest(people)

// 람다를 사용해 컬렉션 검색
>>> val people = listOf(Person("Alice", 29), Person("Bob", 31))
>>> println(people.maxBy { it.age })

// 멤버 참조를 사용해 컬렉션 검색
>>> println(Person::age)
```

Java 컬렉션에 대해 수행하던 대부분의 작업은 람다나 멤버 참조를 인자로 취하는 라이브러리 함수를 통해 개선할 수 있다. 그렇게 람다나 멤버 참조를 인자로 받는 함수를 통해 개선한 코드는 더 짧고더 이해하기 쉽다.

### 람다 식의 문법
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/89c2cd8d-715f-42fe-8e09-31311f939190)

- 코틀린 람다 식은 항상 중괄호로 둘러싸여 있다.
- 인자 목록 주변에 괄호가 없다.
- 화살표가 인자 목록과 람다 본문을 구분해준다.
- 람다 식을 변수에 저장할 수 있다.
  - 람다가 저장된 변수를 다른 일반 함수와 마찬가지로 다룰 수 있다.

```kotlin
val sum = { x: Int, y: Int -> x + y }
println(sum)
```

코드의 일부분을 블록으로 둘러싸 실행할 필요가 있다면 `run`을 사용한다. 이는 인자로 받은 람다를 실행해주는 라이브러리 함수다.

```kotlin
run { println(42) }
```

```kotlin
// 같은 코드다.
people.maxBy({ p: Person -> p.age })
people.maxBy() { p: Person -> p.age }
people.maxBy { p: Person -> p.age }
```

로컬 변수처럼 컴파일러는 람다 파라미터의 타입도 추론할 수 있다. 따라서 파라미터 타입을 명시할 필요가 없다.

```kotlin
people.maxBy { p: Person -> p.age }
people.maxBy { p -> p.age }
```

람다의 파라미터 이름을 디폴트 이름인 `it`으로 바꾸자. 이는 관례다.

```kotlin
people.maxBy { it.age }
```

### 현재 영역에 있는 변수에 접근
자바 메서드 안에서 무명 내부 클래스를 정의할 때 메서드의 로컬 변수를 무명 내부 클래스에서 사용할 수 있다. 람다 안에서도 같은 일을 할 수 있다. 람다를 함수 안에서 정의하면 함수의 파라미터뿐 아니라 람다 정의의 앞에 선언된 로컬 변수까지 람다에서 모두 사용할 수 있다.

```kotlin
fun printMessageWithPrefix(messages: Collection<String>, prefix: String) {
  messages.forEach {
    println("$prefix $it")
  }
}
```

코틀린 람다 안에선 파이널 변수가 아닌 변수에 접근할 수 있다. 또한 람다 안에서 바깥의 변수를 변경해도 된다.

```kotlin
fun printProblemCounts(responses: Collection<String>) {
  var clientErrors = 0
  var serverErrors = 0
  response.forEach {
    if (it.startWith("4")) {
      clientErrors++
    } else if (it.startWith("5")) {
      serverErrors++
    }
  }
  println("$clientErrors client errors, $serverErrors server errors")
}
```

코틀린에선 자바와 달리 람다에서 람다 밖 함수에 있는 파이널이 아닌 변수에 접근할 수 있고, 그 변수를 변경할 수도 있다. 위 예제와 같이 람다 안에서 사용하는 외부 변수를 '람다가 포획한 변수'라고 부른다.

기본적으로 함수 안에 정의된 로컬 변수의 생명주기는 함수가 반환되면 끝난다. 하지만 어떤 함수가 자신의 로컬 변수를 포획한 람다를 반환하거나 다른 변수에 저장한다면 로컬 변수의 생명주기와 함수의 생명주기가 달라질 수 있다. 포획한 변수가 있는 람다를 저장해서 함수가 끝난 뒤에 실행해도 람다의 본문 코드는 여전히 포획한 변수를 읽거나 쓸 수 있다.

파이널 변수를 포획한 경우엔 람다 코드를 변수 값과 함께 저장한다. 파이널이 아닌 변수를 포획한 경우엔 변수를 특별한 래퍼로 감싸 나중에 변경하거나 읽을 수 있게 한 다음, 래퍼에 대한 참조를 람다 코드와 함께 저장한다.

한 가지 꼭 알아둬야 할 함정이 있다. 람다를 이벤트 핸들러나 다른 비동기적으로 실행되는 코드로 활용하는 경우 함수 호출이 끝난 다음에 로컬 변수가 변경될 수도 있다.

```kotlin
fun tryToCountButtonClicks(button: Button): Int {
  var clicks = 0
  button.onClick { clicks++ }
  return clicks
}
```

위 함수는 항상 0을 반환한다. `onClick` 핸들러는 호출될 때마다 `clicks`의 값을 증가시키지만 그 값의 변경을 관찰할 수는 없다. 핸들러는 `tryToCountButtonClicks`가 `clicks`를 반환한 다음에 호출되기 때문이다. 이 함수를 제대로 구현하려면 클릭 횟수를 세는 카운터 변수를 함수 내부가 아니라 클래스의 프로퍼티나 전역 프로퍼티 등의 위치로 빼내서 나중에 변수 변화를 살펴볼 수 있게 해야 한다.

## 컬렉션 함수형 API
### 필수적인 함수: filter와 map
`filter`와 `map`은 컬렉션을 활용할 때 기반이 되는 함수다. 대부분의 컬렉션 연산을 이 두 함수를 통해 표현할 수 있다.

- `filter` 함수는 컬렉션을 이터레이션하면서 주어진 람다에 각 원소를 넘겨 람다가 `true`를 반환하는 원소만 모은다. 원치 않는 원소를 제거하지만 원소를 변환할 수는 없다.

![](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/73ade8fc-02f9-4f12-9e72-7233a183ba65)

- `map` 함수는 주어진 람다를 컬렉션의 각 원소에 적용한 결과를 모아 새 컬렉션을 만든다.

![](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3d022138-0a6c-4c16-a9f9-fbc6cf17e1f1)

```kotlin
fun main(args: Array<String>) {
  val people = listOf(Person("Alice", 29), Person("Bob", 31))
  println(people.filter { it.age > 30 })
}

fun main(args: Array<String>) {
  val people = listOf(Person("Alice", 29), Person("Bob", 31))
  println(people.map { it.name })
}

fun main(args: Array<String>) {
  val numbers = mapOf(0 to "zero", 1 to "one")
  println(numbers.mapValues { it.value.toUpperCase() })
}
```

### all, any, count, find: 컬렉션에 술어 적용
컬렉션에 대해 자주 수행하는 연산으로 컬렉션의 모든 원소가 어떤 조건을 만족하는지 판단하는 연산이 있다. 코틀린에서는 `all`과 `any`가 이런 연산이다. `count` 함수는 조건을 만족하는 원소의 개수를 반환하며, `find` 함수는 조건을 만족하는 첫 번째 원소를 반환한다.

```kotlin
data class Person(val name: String, val age: Int)

val canBeInClub27 = { p: Person -> p.age <= 27 }

fun main(args: Array<String>) {
  val people = listOf(Person("Alice", 27), Person("Bob", 31))
  println(people.all(canBeInClub27))

  val list = listOf(1, 2, 3)
  println(!list.all { it == 3 })
  println(list.any { it != 3 })

  println(people.count(canBeInClub27))
}
```

### groupBy: 리스트를 여러 그룹으로 이뤄진 맵으로 변경
컬렉션의 모든 원소를 어떤 특성에 따라 여러 그룹으로 나누고 싶다고 하자. 특성을 파라미터로 전달하면 컬렉션을 자동으로 구분해주는 함수가 있으면 편리할 것이다. `groupBy`가 그런 역할을 한다.

```kotlin
val people = listOf(Person("Alice", 27), Person("Bob", 31))
println(people.groupBy { it.age })
```

![](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b2232bbc-c931-4a3b-9700-f3a91ae23e15)

### flatMap과 flatten: 중첩된 컬렉션 안의 원소 처리
`flatMap` 함수는 먼저 인자로 주어진 람다를 컬렉션의 모든 객체에 적용하고 람다를 적용한 결과 얻어지는 여러 리스트를 한 리스트로 한데 모은다.

```kotlin
fun main(args: Array<String>) {
  val strings = listOf("abc", "def")
  println(strings.flatMap { it.toList() })
}
// result
[a, b, c, d, e, f]

fun main(args: Array<String>) {
  val books = listOf(Book("Thursday Next", listOf("Jasper Fforde")),
                     Book("Mort", listOf("Terry Pratchett")),
                     Book("Good Omens", listOf("Terry Pratchett",
                                                "Neil Gaiman")))
  println(books.flatMap { it.authors }.toSet())
}
// result
[Jasper Fforde, Terry Pratchett, Neil Gaiman]
```

컬렉션을 다루는 코드를 작성할 경우에는 원하는 바를 어떻게 일반적인 변환을 사용해 표현할 수 있는지 생각해보고 그런 변환을 제공하는 라이브러리 함수가 있는지 살펴보라.

## 지연 계산(lazy) 컬렉션 연산
