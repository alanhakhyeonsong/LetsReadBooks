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
