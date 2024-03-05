# 6장. 코틀린 타입 시스템
자바와 비교하면 코틀린의 타입 시스템은 코드의 가독성을 향상시키는 데 도움이 되는 몇 가지 특성을 새로 제공한다.

- 널이 될 수 있는 타입
- 읽기 전용 컬렉션
- 자바 타입 시스템에서 불필요하거나 문제가 되던 부분을 제거
  - 배열

## 널 가능성
널 가능성은 `NullPointerException` 오류를 피할 수 있게 돕기 위한 코틀린 타입 시스템의 특성이다.

코틀린을 비롯한 최신 언어에서 `null`에 대한 접근 방법은 가능한 한 이 문제를 실행 시점에서 컴파일 시점으로 옮기는 것이다. 널이 될 수 있는지 여부를 타입 시스템에 추가함으로써 컴파일러가 여러 가지 오류를 컴파일 시 미리 감지해서 실행 시점에 발생할 수 잇는 예외의 가능성을 줄일 수 있다.

### 널이 될 수 있는 타입
널이 될 수 있는 타입은 프로그램 안의 프로퍼티나 변수에 `null`을 허용하게 만드는 방법이다. 어떤 변수가 널이 될 수 있다면 그 변수에 대해 메서드를 호출하면 NPE가 발생할 수 있으므로 안전하지 않다. 코틀린은 그런 메서드 호출을 금지함으로써 많은 오류를 방지한다.

```kotlin
// java
int strLen(String s) {
  return s.length();
}

// kotlin
fun strLen(s: String) = s.length
```

코틀린에서 이런 함수를 작성할 때 가장 먼저 답을 알아야 할 질문은 "이 함수가 널을 인자로 받을 수 있는가?"이다.

```kotlin
fun strLenSafe(s: String?) = ...
```

`String?`, `Int?`, `MyCustomType?` 등 어떤 타입이든 타입 이름 뒤에 물음표를 붙이면 그 타입의 변수나 프로퍼티에 `null` 참조를 지정할 수 있다는 뜻이다.

물음표가 없는 타입은 그 변수가 `null` 참조를 지정할 수 없다는 뜻이다. 따라서 모든 타입은 기본적으로 널이 될 수 없는 타입이다. 뒤에 `?`가 붙어야 널이 될 수 있다.

```kotlin
fun strLenSafe(s: String?): Int =
  if (s != null) s.length else 0 // null 검사를 추가하면 코드가 컴파일된다.
```

널 가능성을 다루기 위해 사용할 수 잇는 도구가 `if` 검사뿐이라면 코드가 번잡해지는 일을 피할 수 없을 것이다. 다행히 코틀린은 널이 될 수 잇는 값을 다룰 때 도움이 되는 여러 도구를 제공한다.

### 타입의 의미
자바에서 `String` 타입의 변수에는 `String`이나 `null`이라는 두 가지 종류의 값이 들어갈 수 있다. 이 두 종류의 값은 서로 완전히 다르다. 심지어 자바 자체의 `instanceof` 연산자도 `null`이 `String`이 아니라고 답한다. 두 종류의 값에 대해 실행할 수 있는 연산도 완전히 다르다. 실제 `String`이 들어있는 변수에 대해서는 `String` 클래스에 정의된 모든 메서드를 호출할 수 있다. 하지만 `null`이 들어 있는 경우엔 사용할 수 잇는 연산이 많지 않다.

이는 자바의 타입 시스템이 널을 제대로 다루지 못한다는 뜻이다. 변수에 선언된 타입이 있지만 널 여부를 추가로 검사하기 전에는 그 변수에 대해 어떤 연산을 수행할 수 있을지 알 수 없다.

코틀린의 널이 될 수 있는 타입은 이런 문제에 대해 종합적인 해법을 제공한다. 널이 될 수 있는 타입과 널이 될 수 없는 타입을 구분하면 각 타입의 값에 대해 어떤 연산이 가능할지 명확히 이해할 수 있고, 실행 시점에 예외를 발생시킬 수 있는 연산을 판단할 수 있다.

### 안전한 호출 연산자: ?.
코틀린이 제공하는 가장 유용한 도구 중 하나가 안전한 호출 연산자인 `?.`이다. **`null` 검사와 메서드 호출을 한 번의 연산으로 수행한다.**

`s?.toUpperCase()`는 `if (s != null) s.toUpperCase() else null`과 같다.

```kotlin
class Address(val streetAddress: String, val zipCode: Int, val city: String, val country: String)

class Company(val name: String, val address: Address?)

class Person(val name: String, val company: Company?)

fun Person.countryName(): String {
  val country = this.company?.address?.country
  return if (country != null) country else "Unknown"
}

fun main() {
  val person = Person("Dmitry", null)
  println(person.countryName)
}
```

`?.` 연산자를 사용하면 다른 추가 검사 없이 `Person`의 회사 주소에서 `country` 프로퍼티를 단 한줄로 가져올 수 있다.

### 엘비스 연산자: ?:
코틀린은 `null` 대신 사용할 디폴트 값을 지정할 때 편리하게 사용할 수 있는 연산자를 제공한다. 그 연산자는 **엘비스 연산자**라고 한다.

```kotlin
fun foo(s: String?) {
  val t: String = s ?: "" // s가 null이면 결과는 빈 문자열
}
```

![](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/95838ed1-0c4a-4df1-8dde-5e9d6f924051)

코틀린에선 `return`이나 `throw` 등의 연산도 식이다. 따라서 엘비스 연산자의 우항에 `return`, `throw` 등의 연산을 넣을 수 있고, 엘비스 연산자를 더욱 편하게 사용할 수 있다. 그런 경우 엘비스 연산자의 좌항이 널이면 함수가 즉시 어떤 값을 반환하거나 예외를 던진다. 이런 패턴은 함수의 전제 조건을 검사하는 경우 특히 유용하다.

```kotlin
class Address(val streetAddress: String, val zipCode: Int, val city: String, val country: String)

class Company(val name: String, val address: Address?)

class Person(val name: String, val company: Company?)

fun printShippingLabel(person: Person) {
  val address = person.company?.address
    ?: throw IllegalArgumentException("No address") // 주소가 없으면 예외를 발생시킨다.
  with (address) { // address는 널이 아니다.
    println(streetAddress)
    println("$zipCode $city, $country")
  }
}

fun main() {
  val address = Address("Elsestr. 47", 80687, "Munich", "Germany")
  val jetbrains = Company("JetBrains", address)
  val person = Person("Dmitry", jetbrains)
  printShippingLabel(person)
  printShippingLabel(Person("Alexey", null))
}
```

`printShippingLabel` 함수는 모든 정보가 제대로 있으면 주소를 출력한다. 주소가 없으면 그냥 `NullPointerException`을 던지는 대신에 의미 있는 오류를 발생시킨다.

### 안전한 캐스트: as?
자바 타입 캐스트와 마찬가지로 대상 값을 `as`로 지정한 타입으로 바꿀 수 없으면 `ClassCastException`이 발생한다.

`as?` 연산자는 어떤 값을 지정한 타입으로 캐스트한다. 값을 대상 타입으로 변환할 수 없으면 `null`을 반환한다.

![](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/193dd42e-e625-4472-8e19-929cbb158f4d)

안전한 캐스트를 사용할 때 일반적인 패턴은 캐스트를 수행한 뒤에 엘비스 연산자를 사용하는 것이다.

```kotlin
class Person(val firstName: String, val lastName: String) {
  override fun equals(o: Any?): Boolean {
    val otherPerson = o as? Person ?: return false // 타입이 서로 일치하지 않으면 false 반환

    // 안전한 캐스트를 하고 나면 otherPerson이 Person 타입으로 스마트 캐스트된다.
    return otherPerson.firstName == firstName &&
            otherPerson.lastName == lastName
  }

  override fun hashCode(): Int =
    firstName.hashCode() * 37 + lastName.hashCode()
}

fun main(args: Array<String>) {
  val p1 = Person("Dmitry", "Jemerov")
  val p2 = Person("Dmitry", "Jemerov")
  println(p1 == p2) // == 연산자는 equals 메서드를 호출한다. 
  println(p1.equals(42))
}
```

이 패턴을 사용하면 파라미터로 받은 값이 원하는 타입인지 쉽게 검사하고 캐스트할 수 있고, 타입이 맞지 않으면 쉽게 `false`를 반환할 수 있다. 이 모든 동작을 한 식으로 해결 가능하다. 물론 스마트 캐스트를 이 상황에 적용할 수도 있다. 일단 타입을 검사한 후 `null` 값을 거부하고 나면 컴파일러가 `otherPerson` 변수의 값이 `Person`이라는 사실을 알고 적절히 처리해줄 수 있다.

### 널 아님 단언: !!
널 아님 단언은 코틀린에서 널이 될 수 있는 타입의 값을 다룰 때 사용할 수 있는 도구 중 가장 단순하면서도 무딘 도구다. 느낌표를 이중으로 사용하면 어떤 값이든 널이 될 수 없는 타입으로 (강제로) 바꿀 수 있다. 실제 널에 대해 `!!`를 적용하면 NPE가 발생한다.

![](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c7ba9dd2-d2c2-4e45-a4d9-13deeefb46f0)

```kotlin
fun ignoreNulls(s: String?) {
  val sNotNull: String = s!!
  println(sNotNull.length)
}
```

어떤 함수가 값이 널인지 검사한 다음 다른 함수를 호출한다 해도 컴파일러는 호출된 함수 안에서 안전하게 그 값을 사용할 수 있음을 인식할 수 없다. 하지만 이런 경우 호출된 함수가 언제나 다른 함수에서 널이 아닌 값을 전달받는다는 사실이 분명하다면 굳이 널 검사를 다시 수행하고 싶진 않을 것이다. 이럴 때 널 아님 단언문을 쓸 수 있다.

하지만 사용 시 기억해야만 하는 함정이 한 가지 더 있다. `!!`를 널에 대해 사용해서 발생하는 예외의 스택 드레이스에는 어떤 파일의 몇 번째 줄인지에 대한 정보는 들어있지만 어떤 식에서 예외가 발생했는지에 대한 정보는 들어있지 않다. 어떤 값이 널이었는지 확실히 하기 위해 여러 `!!` 단언문을 한 줄에 함께 쓰는 일을 피하라.

```kotlin
person.company!!.address!!.country // 이런 식으로 크드를 작성하지 말자.
```

### let 함수
`let` 함수를 사용하면 널이 될 수 있는 식을 더 쉽게 다룰 수 있다. `let` 함수를 안전한 호출 연산자와 함께 사용하면 원하는 식을 평가해서 결과가 널인지 검사한 다음 그 결과를 변수에 넣는 작업을 간단한 식을 사용해 한꺼번에 처리할 수 있다.

`let`을 사용하는 가장 흔한 용례는 널이 될 수 있는 값을 널이 아닌 값만 인자로 받는 함수에 넘기는 경우다.

`let` 함수는 자신의 수신 객체를 인자로 전달받은 람다에게 넘긴다. 널이 될 수 있는 값에 대해 안전한 호출 구문을 사용해 `let`을 호출하되 널이 될 수 있는 타입의 값을 널이 될 수 없는 타입의 값으로 바꿔 람다에 전달하게 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9bce9cc4-6766-48b2-8b39-8c19b4e71b80)

```kotlin
fun sendEmailTo(email: String) {
  println("Sending email to $email")
}

fun main(args: Array<String>) {
  var email: String? = "yole@example.com"
  email?.let { sendEmailTo(it) }
  email = null
  email?.let { sendEmailTo(it) }
}
```

`let`을 쓰면 긴 식의 결과를 저장하는 변수를 따로 만들 필요가 없다.

여러 값이 널인지 검사해야 한다면 `let` 호출을 중첩시켜 처리할 수 있다. 그렇게 `let`을 중첩시켜 처리하면 코드가 복잡해져서 알아보기 어려워진다. 그런 경우 일반적인 `if`를 사용해 모든 값을 한꺼번에 검사하는 편이 낫다.

### 나중에 초기화할 프로퍼티
코틀린에서 클래스 안의 널이 될 수 없는 프로퍼티를 생성자 안에서 초기화하지 않고 특별한 메서드 안에서 초기화할 수는 없다. 코틀린에선 일반적으로 생성자에서 모든 프로퍼티를 초기화해야 한다. 게다가 프로퍼티 타입이 널이 될 수 없는 타입이라면 반드시 널이 아닌 값으로 그 프로퍼티를 초기화해야 한다. 그런 초기화 값을 제공할 수 없으면 널이 될 수 있는 타입을 사용할 수 밖에 없다. 하지만 널이 될 수 있는 타입을 사용하면 모든 프로퍼티 접근에 널 검사를 넣거나 `!!` 연산자를 써야 한다.

```kotlin
class MyService {
  fun performAction(): String = "foo"
}

class MyTest {
  private var myService: MyService? = null // null로 초기화 하기 위해 널이 될 수 있는 타입인 프로퍼티를 선언

  @Before fun setUp() {
    myService = MyService() // setUp 메서드 안에서 진짜 초깃값을 지정
  }

  @Test fun testAction() {
    // 반드시 널 가능성에 신경 써야 한다. !!나 ?을 꼭 써야 한다.
    Assert.assertEquals("foo", myService!!.performAction())
  }
}
```

이 코드는 보기 나쁘다. 특히 프로퍼티를 여러 번 사용해야 하면 코드가 더 못생겨지낟. 이를 해결하기 위해 `myService` 프로퍼티를 **나중에 초기화**할 수 있다. `lateinit` 변경자를 붙이면 프로퍼티를 나중에 초기화할 수 있다.

```kotlin
class MyService {
  fun performAction(): String = "foo"
}

class MyTest {
  private lateinit var myService: MyService // 초기화하지 않고 널이 될 수 없는 프로퍼티를 선언

  @Before fun setUp() {
    myService = MyService()
  }

  @Test fun testAction() {
    // 널 검사를 수행하지 않고 프로퍼티를 사용
    Assert.assertEquals("foo", myService.performAction())
  }
}
```

**나중에 초기화하는 프로퍼티는 항상 `var` 여야 한다.** `val` 프로퍼티는 `final` 필드로 컴파일되며, 생성자 안에서 반드시 초기화해야 한다. 따라서 생성자 밖에서 초기화해야 하는 나중에 초기화 하는 프로퍼티는 항상 `var` 여야 한다.

그렇지만 나중에 초기화하는 프로퍼티는 널이 될 수 없는 타입이라 해도 더 이상 생성자 안에서 초기화할 필요가 없다. 그 프로퍼티를 초기화하기 전에 프로퍼티에 접근하면 `"lateinit preperty myService has not been initialized"`라는 예외가 발생한다. 예외를 보면 어디가 잘못됐는지 확실히 알 수 있다. 따라서 단순한 NPE가 발생하는 것보다 훨씬 좋다.

## 코틀린의 원시 타입
### 원시 타입: Int, Boolean 등
원시 타입의 변수에는 그 값이 직접 들어가지만, 참조 타입의 변수에는 메모리 상의 객체 위치가 들어간다. 자바는 참조 타입이 필요한 경우 특별한 래퍼 타입(`Integer` 등)으로 원시 타입 값을 감싸서 사용한다. **코틀린은 원시 타입과 래퍼 타입을 구분하지 않으므로 항상 같은 타입을 사용한다.** 래퍼 타입을 따로 구분하지 않으면 편리하다. 더 나아가 코틀린에선 숫자 타입 등 원시 타입의 값에 대해 메소드를 호출할 수 있다.

```kotlin
fun showProgress(progress: Int) {
  val percent = progress.coerceIn(0, 100)
  println("We're ${percent}% done!")
}
```

코틀린은 실행 시점에 숫자 타입이 가능한 한 가장 효율적인 방식으로 표현된다. 대부분의 경우 코틀린의 `Int` 타입은 자바 `int` 타입으로 컴파일된다.

자바의 원시 타입에 해당하는 타입은 다음과 같다.

- 정수 타입: `Byte`, `Short`, `Int`, `Long`
- 부동 소수점 수 타입: `Float`, `Double`
- 문자 타입: `Char`
- 불리언 타입: `Boolean`

### 널이 될 수 있는 원시 타입: Int?, Boolean? 등
`null` 참조를 자바의 참조 타입의 변수에만 대입할 수 있기 때문에 널이 될 수 있는 코틀린 타입은 자바 원시 타입으로 표현할 수 없다. 따라서 코틀린에서 널이 될 수 있는 원시 타입을 사용하면 그 타입은 자바의 래퍼 타입으로 컴파일된다.

```kotlin
data class Person(val age: String,
                  val age: Int? = null) {
  fun isOlderThan(other: Person): Boolean? {
    if (age == null || other.age == null)
      return null
    return age > other.age
  }
}

fun main(args: Array<String>) {
  println(Person("Sam", 35).isOlderThan(Person("Amy", 42)))
  println(Person("Sam", 35).isOlderThan(Person("Jane")))
}
```

`Person` 클래스에 선언된 `age` 프로퍼티의 값은 `Integer`로 저장된다. 코틀린에서 적절한 타입을 찾으려면 그 변수나 프로퍼티에 널이 들어갈 수 있는지만 고민하면 된다.

### 숫자 변환
코틀린과 자바의 가장 큰 차이점 중 하나는 숫자를 변환하는 방식이다. 코틀린은 한 타입의 숫자를 다른 타입의 숫자로 자동 변환하지 않는다. 결과 타입이 허용하는 숫자의 범위가 원래 타입의 범위보다 넓은 경우 조차도 자동 변환은 불가능하다.

```kotlin
val i = 1
val l: long = i // "Error: type mismatch" 컴파일 오류 발생
val l2: long = i.toLong()
```

코틀린은 모든 원시 타입에 대한 변환 함수를 제공한다. 그런 변환 함수의 이름은 `toByte()`, `toShort()`, `toChar()` 등과 같다. 즉, 어떤 타입을 더 표현 범위가 넓은 타입으로 변환하는 함수도 있고, 타입을 범위가 더 표현 범위가 좁은 타입으로 변환하면서 값을 벗어나는 경우에는 일부를 잘라내는 함수(`Long.toInt()`)도 있다.

코틀린 표준 라이브러리는 문자열을 원시 타입으로 변환하는 여러 함수를 제공한다. 이런 함수는 문자열의 내용을 각 원시 타입을 표기하는 문자열로 파싱한다. 파싱에 실패하면 `NumberFormatException`이 발생한다.

- `toInt`
- `toByte`
- `toBoolean`
- ...

### Any, Any?: 최상위 타입
자바에서 `Object`가 클래스 계층의 최상위 타입이듯 코틀린에선 `Any` 타입이 모든 널이 될 수 없는 타입의 조상 타입이다. 하지만 코틀린에선 `Any`가 `Int` 등의 원시 타입을 포함한 모든 타입의 조상 타입이다.

```kotlin
val answer: Any = 42 // Any가 참조 타입이므로 42가 박싱됨.
```

### Unit 타입: 코틀린의 void
코틀린의 `Unit` 타입은 자바의 `void`와 같은 기능을 한다. 관심을 가질 만한 내용을 전혀 반환하지 않는 함수의 반환 타입으로 `Unit`을 쓸 수 있다. 이는 반환 타입 선언 없이 정의한 블록이 본문인 함수와 같다.

`Unit`은 모든 기능을 갖는 일반적인 타입이며, `void`와 달리 `Unit`을 타입 인자로 쓸 수 있다. `Unit` 타입에 속한 값은 단 하나뿐이며, 그 이름도 `Unit`이다. `Unit` 타입의 함수는 `Unit` 값을 묵시적으로 반환한다. 이 두 특성은 제네릭 파라미터를 반환하는 함수를 오버라이드하면서 반환 타입으로 `Unit`을 쓸 때 유용하다.

```kotlin
interface Processor<T> {
  fun process(): T
}

class NoResultProcessor : Processor<Unit> {
  override fun process() { // Unit을 반환하지만 타입을 저장할 필요는 없음.
    // do stuff
    // return을 명시할 필요가 없음.
  }
}
```

함수형 프로그래밍에서 전통적으로 `Unit`은 '단 하나의 인스턴스만 갖는 타입'을 의미해왔고 바로 그 유일한 인스턴스의 유무가 자바 `void`와 코틀린 `Unit`을 구분하는 가장 큰 차이다. 어쩌면 자바 등의 명령형 프로그래밍 언어에서 관례적으로 사용해 온 `Void` 라는 이름을 사용할 수도 있겠지만, 코틀린엔 `Nothing`이라는 전혀 다른 기능을 하는 타입이 하나 존재한다.

### Nothing 타입: 이 함수는 결코 정상적으로 끝나지 않는다
코틀린에는 결코 성공적으로 값을 돌려주는 일이 없으므로 '반환 값'이라는 개념 자체가 의미 없는 함수가 일부 존재한다.

```kotlin
fun fail(message: String): Nothing {
  throw IllegalStateException(message)
}

val address = company.address ?: fail("No address")
println(address.city)
```

`Nothing` 타입은 아무 값도 포함하지 않는다. 따라서 `Nothing`은 함수의 반환 타입이나 반환 타입으로 쓰일 타입 파라미터만 쓸 수 있다. 컴파일러는 `Nothing`이 반환 타입인 함수가 결코 정상 종료되지 않음을 알 수 있고 그 함수를 호출하는 코드를 분석할 때 사용한다.

## 컬렉션과 배열
### 널 가능성과 컬렉션
컬렉션 안에 널 값을 넣을 수 있는지 여부는 어떤 변수의 값이 널이 될 수 있는지 여부와 마찬가지로 중요하다.

```kotlin
fun addValidNumbers(numbers: List<Int?>) {
  var sumOfValidNumbers = 0
  var invalidNumbers = 0
  for (number in numbers) {
    if (number != null) {
      sumOfValidNumbers += number
    } else {
      invalidNumbers++
    }
  }
  println("Sum of valid numbers: ${validNumbers.sum()}")
  println("Invalid numbers: ${numbers.size - validNumbers.size}")
}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/623fe1ae-c971-435e-8a63-c82ff2061438)

리스트의 원소에 접근하면 `Int?` 타입의 값을 얻는다. 따라서 그 값을 산술식에 사용하기 전에 널 여부를 검사해야 한다. 널이 될 수 있는 값으로 이뤄진 컬렉션으로 널 값을 걸러내는 경우가 자주 있어서 코틀린 표준 라이브러리는 그런 일을 하는 `filterNotNull` 이라는 함수를 제공한다.

```kotlin
fun addValidNumbers(numbers: List<Int?>) {
  val validNumbers = numbers.filterNotNull()
  println("Sum of valid numbers: ${validNumbers.sum()}")
  println("Invalid numbers: ${numbers.size - validNumbers.size}")
}
```

### 읽기 전용과 변경 가능한 컬렉션
코틀린 컬렉션과 자바 컬렉션을 나누는 가장 중요한 특성 하나는 코틀린에선 컬렉션안의 데이터에 접근하는 인터페이스와 컬렉션 안의 데이터를 변경하는 인터페이스를 분리했다는 점이다.

일반적인 읽기 전용 라이브러리를 사용하면 `kotlin.collections.Collection` 라이브러리를 사용하면 된다. 그러나 컬렉션의 데이터를 수정하려면 `kotlin.collections.MutableCollection` 인터페이스를 사용하면 된다. 원소를 추가하거나, 삭제하거나, 컬렉션 안의 원소를 모두 지우는 등의 메소드를 더 제공한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ed14d00d-9615-4251-b36c-99c670299a6e)

### 코틀린 컬렉션과 자바
모든 코틀린 컬렉션은 그에 상응하는 자바 컬렉션 인터페이스의 인스턴스라는 점은 사실이다. 하지만 코틀린은 모든 자바 컬렉션 인터페이스마다 읽기 전용 인터페이스와 변경 가능한 인터페이스라는 두 가지 표현을 제공한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b3f56fb3-d446-4b44-bd81-ca4eb5ff5329)

이런 성질로 인해 컬렉션의 변경 가능성과 관련해 중요한 문제가 생긴다. 자바는 읽기 전용 컬렉션과 변경 가능 컬렉션을 구분하지 않으므로, 코틀린에서 읽기 전용 `Collection`으로 선언된 객체라도 자바 코드에선 그 컬렉션 객체의 내용을 변경할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/43781b9c-6dd9-4c97-ba3c-3bd624ca3c17)

```kotlin
// Java
// CollectionUtils.java
public class CollectionUtils {
  public static List<String> uppercaseAll(List<String> items) {
    for (int i = 0; i < items.size(); i++) {
      items.set(i, items.get(i).toUpperCase());
    }
    return items;
  }
}

// Kotlin
// collections.kt
fun printInUpperCase(list: List<String>) {
  println(CollectionUtils.uppercaseAll(list))
  println(list.first())
}
```

### 객체의 배열과 원시 타입의 배열
```kotlin
fun main(args: Array<String>) {
  for (i in args.indices) {
    println("Argument $i is: ${args[i]}")
  }
}
```

코틀린 배열은 타입 파라미터를 받는 클래스다. 배열의 원소 타입은 바로 그 타입 파라미터에 의해 정해진다. 코틀린에서 배열을 만드는 방법은 다양하다.

- `arrayOf` 함수에 원소를 넘기면 배열을 만들 수 있다.
- `arrayOfNulls` 함수에 정수 값을 인자로 넘기면 모든 원소가 `null`이고 인자로 넘긴 값과 크기가 같은 배열을 만들 수 있다. 물론 원소 타입이 널이 될 수 있는 타입인 경우에만 이 함수를 쓸 수 있다.
- `Array` 생성자는 배열 크기와 람다를 인자로 받아서 람다를 호출해서 각 배열 원소를 초기화해준다. `arrayOf`를 쓰지 않고 각 원소가 널이 아닌 배열을 만들어야 하는 경우 이 생성자를 사용한다.

```kotlin
fun main(args: Array<String>) {
  val letters = Array<String>(26) { i -> ('a' + i).toString() }
  println(letters.joinToString(""))
}

fun main(args: Array<String>) {
  val strings = listOf("a", "b", "c")
  println("%s/%s/%s".format(*strings.toTypedArray()))
}

fun main(args: Array<String>) {
  val squares = IntArray(5) { i -> (i+1) * (i+1) }
  println(squares.joinToString())
}

fun main(args: Array<String>) {
  args.forEachIndexed { index, element ->
    println("Argument $index is: $element")
  }
}
```

## 요약
- 코틀린은 널이 될 수 있는 타입을 지원해 `NullPointerException` 오류를 컴파일 시점에 감지할 수 있다.
- 코틀린의 안전한 호출(`?.`), 엘비스 연산자(`?:`), 널 아님 단언(`!!`), `let` 함수 등을 사용하면 널이 될 수 있는 타입을 간결한 코드로 다룰 수 있다.
- `as?` 연산자를 사용하면 값을 다른 타입으로 취급한다. 개발자는 플랫폼 타입을 널이 될 수 있는 타입으로도, 널이 될 수 없는 타입으로도 사용할 수 있다.
- 코틀린에서는 수를 표현하는 타입(`Int` 등)이 일반 클래스와 똑같이 생겼고 일반 클래스와 똑같이 동작한다. 하지만 대부분 컴파일러는 숫자 타입을 자바 원시 타입(int 등)으로 컴파일한다.
- 널이 될 수 있는 원시 타입(`Int?` 등)은 자바의 박싱한 원시 타입에 대응한다.
- `Any` 타입은 다른 모든 타입의 조상 타입이며, 자바의 `Object`에 해당한다. `Unit`은 자바의 `void`와 비슷하다.
- 정상적으로 끝나지 않는 함수의 반환 타입을 지정할 때 `Nothing` 타입을 사용한다.
- 코틀린 컬렉션은 표준 자바 컬렉션 클래스를 사용한다. 하지만 코틀린은 자바보다 컬렉션을 더 개선해서 읽기 전용 컬렉션과 변경 가능한 컬렉션을 구별해 제공한다.
- 자바 클래스를 코틀린에서 확장하거나 자바 인터페이스를 코틀린에서 구현하는 경우 메소드 파라미터의 널 가능성과 변경 가능성에 대해 깊이 생각해야 한다.
- 코틀린의 `Array` 클래스는 일반 제네릭 클래스처럼 보인다. 하지만 `Array`는 자바 배열로 컴파일된다.
- 원시 타입의 배열은 `IntArray`와 같이 각 타입에 대한 특별한 배열로 표현된다.