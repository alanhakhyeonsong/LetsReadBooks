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
`map`이나 `filter` 같은 몇 가지 컬렉션 함수를 살펴봤다. 그런 함수는 결과 컬렉션을 **즉시** 생성한다. 이는 컬렉션 함수를 연쇄하면 매 단계마다 계산 중간 결과를 새로운 컬렉션에 임시로 담는다는 뜻이다. **시퀀스(sequence)를 사용하면 중간 임시 컬렉션을 사용하지 않고도 컬렉션 연산을 연쇄할 수 있다.**

```kotlin
fun main(args: Array<String>) {
  listOf(1, 2, 3, 4).asSequence() // 원본 컬렉션을 시퀀스로 변환한다. 
          .map { print("map($it) "); it * it } // 시퀀스도 컬렉션과 똑같은 API를 제공한다. 
          .filter { print("filter($it) "); it % 2 == 0 }
          .toList() // 결과 시퀀스를 다시 리스트로 변환한다. 
}
```

코틀린 지연 계산 시퀀스는 `Sequence` 인터페이스에서 시작한다. `Sequence` 안에는 `iterator`라는 단 하나의 메소드가 있다. 그 메소드를 통해 시퀀스로부터 원소 값을 얻을 수 있다.

---
📌 왜 시퀀스를 다시 컬렉션으로 되돌려야 할까?

컬렉션보다 시퀀스가 훨씬 더 낫다면 그냥 시퀀스를 쓰는 편이 나을수도 있다. 하지만 "항상 그렇지는 않다". 시퀀스의 원소를 차례로 이터레이션해야 한다면 시퀀스를 직접 써도 된다. 하지만 시퀀스 원소를 인덱스를 사용해 접근하는 등의 다른 API 메소드가 필요하다면 시퀀스를 리스트로 변환해야 한다.

---

### 시퀀스 연산 실행: 중간 연산과 최종 연산
시퀀스에 대한 연산은 **중간 연산**과 **최종 연산**으로 나뉜다. 중간 연산은 다른 시퀀스를 반환한다. 그 시퀀스는 최초 시퀀스의 원소를 변환하는 방법을 안다. 최종 연산은 결과를 반환한다. 결과는 최초 컬렉션에 대해 변환을 적용한 시퀀스로부터 일련의 계산을 수행해 얻을 수 있는 컬렉션이나 원소, 숫자 또는 객체다.

![](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/8ffea406-682b-4471-a11e-9efb8fb5e850)

```kotlin
fun main(args: Array<String>) {
  listOf(1, 2, 3, 4).asSequence()
          .map { print("map($it) "); it * it }
          .filter { print("filter($it) "); it % 2 == 0 }
          .toList()
}

// 결과
map(1) filter(1) map(2) filter(4) map(3) filter(9) map(4) filter(16)
```

시퀀스의 경우 모든 연산은 각 원소에 대해 순차적으로 적용된다. 즉 첫 번째 원소가 처리되고, 다시 두 번째 원소가 처리되며, 이런 처리가 모든 원소에 대해 적용된다.

시퀀스를 사용하면 지연 계산으로 인해 원소 중 일부의 계산은 이뤄지지 않는다.

![](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c6002559-049b-4f5f-b429-e9df4f76a859)


자바 8을 채택하면 현재 코틀린 컬렉션과 시퀀스에서 제공하지 않는 중요한 기능을 사용할 수 있다. 바로 스트림 연산(`map`과 `filter` 등)을 여러 CPU에서 병렬적으로 실행하는 기능이 그것이다.

## 자바 함수형 인터페이스 활용
### 자바 메서드에 람다를 인자로 전달
함수형 인터페이스를 인자로 원하는 자바 메서드에 코틀린 람다를 전달할 수 있다.

```kotlin
// Java
void postponeComputation(int delay, Runnable computation);

// Kotlin
postponeComputation(1000, object: Runnable {
  override fun run() {
    println(42)
  }
})

postponeComputation(1000) { println(42) }
```

람다와 무명 객체 사이에는 차이가 있다. 객체를 명시적으로 선언하는 경우 메서드를 호출할 때마다 새로운 객체가 생성된다. 람다는 다르다. 정의가 들어있는 함수의 변수에 접근하지 않는 람다에 대응하는 무명 객체를 메서드를 호출할 때마다 반복 사용한다.

그러나 람다가 주변 영역의 변수를 포획한다면 매 호출마다 같은 인스턴스를 사용할 수 없다. 그런 경우 컴파일러는 매번 주변 영역의 변수를 포획한 새로운 인스턴스를 생성해준다.

```kotlin
fun handleComputation(id: String) { // 람다 안에서 id 변수를 포획한다.
  postponeComputation(1000) { println(id) } // handleComputation을 호출할 때마다 새로 Runnable 인스턴스를 만든다.
}
```

코틀린 `inline`으로 표시된 코틀린 함수에게 람다를 넘기면 아무런 무명 클래스도 만들어지지 않는다. 대부분의 코틀린 확장 함수들은 `inline` 표시가 붙어있다.

### SAM 생성자: 람다를 함수형 인터페이스로 명시적으로 변경
SAM 생성자는 람다를 함수형 인터페이스의 인스턴스로 변환할 수 있게 컴파일러가 자동으로 생성한 함수다. 컴파일러가 자동으로 람다를 함수형 인터페이스 무명 클래스로 바꾸지 못하는 경우 SAM 생성자를 사용할 수 있다.

```kotlin
fun createAllDoneRunnable(): Runnable {
  return Runnable { println("All done!") }
}
```

SAM 생성자의 이름은 사용하려는 함수형 인터페이스의 이름과 같다. SAM 생성자는 그 함수형 인터페이스의 유일한 추상 메서드의 본문에 사용할 람다만을 인자로 받아서 함수형 인터페이스를 구현하는 클래스의 인스턴스를 반환한다.

## 수신 객체 지정 람다: with와 apply
자바의 람다에는 없는 코틀린 람다의 독특한 기능이 있다. 그 기능은 바로 수신 객체를 명시하지 않고 람다의 본문 안에서 다른 객체의 메서드를 호출할 수 있게 하는 것이다. 그런 람다를 **수신 객체 지정 람다**라고 한다.

### with 함수
어떤 객체의 이름을 반복하지 않고도 그 객체에 대해 다양한 연산을 수행할 수 있다면 좋을 것이다. 다양한 언어가 그런 기능을 제공한다.

```kotlin
// with를 사용하지 않은 함수
fun alphabet(): String {
  val result = StringBuilder()
  for (letter in 'A'..'Z') {
    result.append(letter)
  }
  result.append("\nNow I know the alphabet!")
  return result.toString()
}

// with를 사용하여 중복된 변수명을 제거한 함수 사용
fun alphabet(): String {
  val stringBuilder = StringBuilder()
  return with(stringBuilder) { // 메서드를 호출하려는 수신 객체 지정
    for (letter in 'A'..'Z') {
      this.append(letter) // this를 명시해서 앞에서 지정한 수신 객체의 메서드 호출
    }
    append("\nNow I know the alphabet!") // this를 생략하고 메서드 호출
    this.toString() // 람다에서 값을 반환한다.
  }
}
```

`with` 함수는 첫 번째 인자로 받은 객체를 두 번째 인자로 받은 람다의 수신 객체로 만든다. 인자로 받은 람다 본문에선 `this`를 사용해 그 수신 객체에 접근할 수 있다.

`with`가 반환하는 값은 람다 코드를 실행한 결과며, 그 결과는 람다 식의 본문에 있는 마지막 식의 값이다. 하지만 때로는 람다의 결과 대신 수신 객체가 필요한 경우도 있다. 그럴 때는 `apply` 라이브러리 함수를 사용할 수 있다.

### apply 함수
`apply` 함수는 거의 `with`와 동일하다. 유일한 차이란 `apply`는 항상 자신에게 전달된 객체(즉 수신 객체)를 반환한다는 점뿐이다.

```kotlin
fun alphabet() = StringBuilder().apply {
  for (letter in 'A'..'Z') {
    append(letter)
  }
  append("\nNow I know the alphabet!")
}.toString()
```

`with`와 `apply`는 수신 객체 지정 람다를 사용하는 일반적인 예제 중 하나다. 더 구체적인 함수를 비슷한 패턴으로 활용할 수 있다. 예를 들어 표준 라이브러리의 `buildString` 함수를 사용하면 `alphabet` 함수를 더 단순화할 수 있다.

```kotlin
fun alphabet() = buildString {
  for (letter in 'A'..'Z') {
    append(letter)
  }
  append("\nNow I know the alphabet!")
}
```

`buildString` 함수는 `StringBuilder`를 활용해 `String`을 만드는 경우 사용할 수 있는 우아한 해법이다.

## 요약
- 람다를 사용하면 코드 조각을 다른 함수에게 인자로 넘길 수 있다.
- 코틀린에서는 람다가 함수 인자인 경우 괄호 밖으로 람다를 빼낼 수 있고, 람다의 인자가 단 하나뿐인 경우 인자 이름을 지정하지 않고 `it`이라는 디폴트 이름으로 부를 수 있다.
- 람다 안에 있는 코드는 그 람다가 들어있는 바깥 함수의 변수를 읽거나 쓸 수 있다.
- 메소드, 생성자, 프로퍼티의 이름 앞에 `::`을 붙이면 각각에 대한 참조를 만들 수 있다. 그런 참조를 람다 대신 다른 함수에게 넘길 수 있다.
- `filter`, `map`, `all`, `any` 등의 함수를 활용하면 컬렉션에 대한 대부분의 연산을 직접 원소를 이터페이션 하지 않고 수행할 수 있다.
- 시퀀스를 사용하면 중간 결과를 담는 컬렉션을 생성하지 않고도 컬렉션에 대한 여러 연산을 조합할 수 있다.
- 함수형 인터페이스(추상 메소드가 단 하나뿐인 SAM 인터페이스)를 인자로 받는 자바 함수를 호출할 경우 람다를 함수형 인터페이스 인자 대신 넘길 수 있다.
- 수신 객체 지정 람다를 사용하면 람다 안에서 미리 정해둔 수신 객체의 메소드를 직접 호출할 수 있다.
- 표준 라이브러리의 `with` 함수를 사용하면 어떤 객체에 대한 참조를 반복해서 언급하지 않으면서 그 객체의 메소드를 호출할 수 있다. `apply`를 사용하면 어떤 객체라도 빌더 스타일의 API를 사용해 생성하고 초기화할 수 있다.