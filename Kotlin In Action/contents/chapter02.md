# 2장. 코틀린 기초
## 기본 요소: 함수와 변수
```kotlin
fun main(args: Array<String>) {
    println("Hello, world!")
}
```

위 코드에서 Kotlin 문법이나 특성을 간단하게 나열해보면 다음과 같다.

- 함수를 선언할 때 `fun` 키워드를 사용한다.
- 파라미터 이름 뒤에 그 파라미터의 타입을 쓴다. 변수를 선언할 때도 마찬가지의 방식이다.
- **함수를 최상위 수준에 정의할 수 있다.** Java와 달리 꼭 클래스 안에 함수를 넣어야 할 필요가 없다.
- 배열도 일반적인 클래스와 마찬가지다. Kotlin에는 Java와 달리 배열 처리를 위한 문법이 따로 존재하지 않는다.
- `System.out.println` 대신 `println`이라 쓴다. Kotlin 표준 라이브러리는 여러 가지 표준 Java 라이브러리 함수를 간결하게 사용할 수 있게 감싼 래퍼를 제공한다.
- 줄 끝에 세미콜론을 붙이지 않아도 좋다.

### 함수
```kotlin
// 함수이름(파라미터): 리턴타입
fun max(a: Int, b: Int): Int {
    return if (a > b) a else b
}
```

- 함수 선언은 `fun` 키워드로 시작한다.
- 함수 이름 뒤에는 괄호 안에 파라미터 목록이 온다.
- 함수의 반환 타입은 파라미터 목록의 닫는 괄호 다음에 오는데, 괄호와 반환 타입 사이를 콜론으로 구분해야 한다.

```kotlin
fun max(a: Int, b: Int): Int = if (a > b) a else b
```

- Kotlin의 `if`는 문장이 아니고 결과를 만드는 식(expression)이다.
- 식은 값을 만들어 내며, 다른 식의 하위 요소로 계산에 참여할 수 있다.
- 대입문은 Java에서 식이었으나 Kotlin에선 문이라는 차이가 있다.

위 예제는 **식이 본문인 함수**라고 부르는데 다음과 같은 특징이 있다.

- 본문이 중괄호로 둘러싸인 함수를 블록이 본문인 함수
- 등호와 식으로 이루어진 함수를 식이 본문인 함수라고 한다.
- 사용자가 굳이 반환 타입을 적지 않아도 **컴파일러가 함수 본문 식을 분석해서 식의 결과 타입을 함수 반환 타입으로 정해준다.** (타입 추론)
- Kotlin은 정적 타입 지정 언어로 모든 변수나 식에는 타입이 있으며, 모든 함수는 반환 타입이 정해져야 한다.

#### 타입 추론
- 컴파일러가 타입을 분석해 프로그래머 대신 프로그램 구성 요소의 타입을 정해주는 기능
- 식이 본문인 함수의 반환 타입만 생략 가능
- 블록이 본문인 함수가 값을 반환한다면 반드시 반환 타입을 지정하고 `return` 문을 사용해 반환 값을 명시한다.

### 변수
Kotlin에서의 변수 선언의 특징은 다음과 같다.

- 변수 이름 뒤에 타입을 명시하거나 생략을 허용한다.
- 식이 본문인 함수에서와 마찬가지로 타입을 지정하지 않으면 컴파일러가 초기화 식을 분석해서 초기화 식의 타입을 변수 타입으로 지정한다.
- 초기화 식을 사용하지 않고 변수를 선언하려면 변수 타입을 반드시 명시해야 한다.
- 초기화 식이 없다면 변수에 저장될 값에 대해 아무 정보가 없기 때문에 컴파일러가 타입을 추론할 수 없다. 이러한 경우 타입을 반드시 지정해야 한다.

```kotlin
// 타입 표기 생략
val answer = 42

// 타입 표기 명시
val answer: Int = 42

// 초기화 식을 사용하지 않고 변수를 선언하려면 타입을 반드시 명시해야 한다.
val answer: Int
answer = 42
```

#### 변경 가능한 변수와 변경 불가능한 변수
- `val` (value): 변경 불가능한 참조를 저장하는 변수다.
  - `val`로 선언된 변수는 일단 초기화하고 나면 재대입이 불가능하다.
  - Java의 `final` 변수에 해당한다.
- `var` (variable): 변경 가능한 참조다.
  - 변수의 값은 언제든 변경 가능하다.
  - Java의 일반 변수에 해당한다.

**기본적으론 모든 변수를 `val` 키워드를 사용해 불변 변수로 선언하고, 나중에 꼭 필요할 때에만 `var`로 변경하자.** 변경 불가능한 참조와 변경 불가능한 객체를 부수 효과가 없는 함수와 조합해 사용하면 코드가 함수형 코드에 가까워진다.

`val` 변수는 블록을 실행할 때 정확히 한 번만 초기화돼야 한다. 하지만 어떤 블록이 실행될 때 오직 한 초기화 문장만 실행됨을 컴파일러가 확인할 수 있다면 조건에 따라 `val` 값을 다른 여러 값으로 초기화할 수 있다.

```kotlin
val message: String
if (canPerformOperation()) {
    message = "Success"
    // ...
} else {
    message = "Failed"
}
```

**`val` 참조 자체는 불변일지라도 그 참조가 가리키는 객체의 내부 값은 변경될 수 있다는 사실을 기억하자.**

```kotlin
val languages = arrayListOf("Java") // 불변 참조를 선언
languages.add("Kotlin") // 참조가 가리키는 객체 내부를 변경한다.
```

`var` 키워드를 사용하면 변수의 값을 변경할 수 있지만 변수의 타입은 고정돼 바뀌지 않는다.

```kotlin
var answer = 42
answer = "no answer" // Error: type mismatch 컴파일 오류 발생
```

#### 문자열 템플릿
문자열 템플릿이란 스크립트 언어와 비슷하게 변수를 문자열에 사용하는 방법이다.

```kotlin
fun main(args: Array<String>) {
    val name = if (args.size > 0) args[0] else "Kotlin"
    println("Hi, ${name}")
    println("Hello, $name") // 입력 값이 하나일 때 괄호 생략 가능
}
```

컴파일러는 각 식을 정적으로 컴파일 시점에 검사하기 때문에 존재하지 않는 변수를 문자열 템플릿 안에서 사용하면 컴파일 오류가 발생한다.

## 클래스와 프로퍼티
### 프로퍼티
- 클래스의 기본 목적은 캡슐화하고 캡슐화한 데이터를 다루는 코드를 한 주체 아래 가두는 것이다.
  - 멤버 필드의 가시성은 보통 `private`
  - 클라이언트가 데이터에 접근하기 위한 방법으로 접근자 메서드(accessor method)를 제공한다.
  - 일반적으로 필드를 읽기 위한 getter를 제공하고 필드를 변경하게 허용해야 할 경우 setter를 추가 제공할 수 있다.
- Java에서 프로퍼티란 필드와 접근자를 한데 묶은 개념이다.
- Kotlin에서 프로퍼티란 Java의 필드와 getter를 완전하게 대신한다.
  - `val`로 선언한 프로퍼티는 읽기 전용이다. → `private` 필드와 getter를 만들어낸다.
  - `var`로 선언한 프로퍼티는 변경 가능하다. → `private` 필드와 getter/setter를 만들어낸다.

```kotlin
class Person(val name: String, var isMarried: Boolean)

fun test() {
    val person = Person("Ramos", false)
    person.name // person.getName()
    person.isMarried // person.getIsMarried()
    person.isMarried = true // person.setIsMarried(true)
}
```

**대부분의 프로퍼티에는 그 프로퍼티의 값을 저장하기 위한 필드가 있다. 이를 프로퍼티를 뒷받침하는 필드라고 부른다.** 객체에 어떠한 로직 처리가 이루어진 필드로 커스텀 getter를 통해 구현 가능하다.

```kotlin
class Test {
    var name: String = ""
        set(value) {
            // field 키워드로 실제 필드에 접근 가능
            // 필드이름을 그대로 사용(value)하면 프로퍼티(getValue(), setValue())로 사용되어 메서드가 무한 재귀할 수 있음. 
            field = if (value.isNotEmpty()) value else ""
        }
}
```

프로퍼티와 실제 내부에 생성되는 필드는 다를 수 있다. (컴파일러에 의해 최적화됨)

```kotlin
class Test {
    var size = 0; // 실제 private int size 필드가 생성됨

    var isEmpty // isEmpty 는 프로퍼티로만 존재하고, 실제 필드는 생성하지 않음 (컴파일러에 의한 최적화)
        get() = size == 0
        set(value) {
            size = size * 2
        }
}
```

### 커스텀 접근자
프로퍼티의 접근자를 사용자 정의로 작성하는 방식이다.

```kotlin
class Rectangle(val height: Int, val width: Int) {
    val isSquare: Boolean
      get() { // 프로퍼티 getter 재정의
          return height == width
      }
}
```

### 코틀린 소스코드 구조: 디렉터리와 패키지
- Java와 같은 개념이다. 조금 다른점은 선언이 간결하기 때문에 하나의 파일에 여러 클래스를 넣어도 괜찮다는 점이다.
- 기존 Java와 Kotlin의 다른점이라면 최상위에 정의된 함수에 대한 개념이 추가되었다는 것이다.
- 최상위에 정의된 함수로 선언된 함수들은 동일한 패키지내에서 `import` 없이 접근이 가능하다.
- 하지만 최종적으로 Java와의 상호운용성이라는 측면에서 봤을 때 Java에서 사용하던 방식으로 패키지를 구성해야 한다.

```kotlin
package geometry.shapes

import java.util.Random // 표준 자바 라이브러리 클래스를 임포트한다.

class Rectangle(val height: Int, val width: Int) {
    val isSquare: Boolean
      get() = height == width
}

fun createRandomRectangle(): Rectangle {
    val random = Random()
    return Rectangle(random.nextInt(), random.nextInt())
}
```

## 선택 표현과 처리: enum과 when
```kotlin
enum class Color {
    RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET
}
```

- 소프트 키워드: `enum`
  - `class` 앞에서는 `enum` 클래스를 의미한다. `class`가 없다면 키워드가 아니다.
  - `enum`에서도 일반적인 클래스와 마찬가지로 생성자와 프로퍼티를 선언한다.

프로퍼티와 메서드가 있는 `enum` 클래스를 선언하는 예제는 다음과 같다.

```kotlin
enum class Color(
    val r: Int, val g: Int, val b: Int
) {
    RED(255, 0, 0), ORANGE(255, 165, 0),
    YELLOW(255, 255, 0), GREEN(0, 255, 0), BLUE(0, 0, 255),
    INDIGO(75, 0, 130), VIOLET(238, 130, 238); // 마지막에 반드시 세미콜론을 추가하여야한다.

    fun rgb() = (r * 256 + g) * 256 + b // enum 메서드 정의
}
```

### when으로 enum 클래스 다루기
`if`와 마찬가지로 `when`도 값을 만들어내는 식이다. 따라서 식이 본문인 함수에 `when`을 바로 사용할 수 있다.

```kotlin
fun getMnemonic(color: Color) = // 함수의 반환 값으로 when 식을 직접 사용한다.
    when (color) {
        Color.RED -> "Richard"
        Color.ORANGE -> "Of"
        Color.YELLOW -> "York"
        Color.GREEN -> "Gave"
        Color.BLUE -> "Battle"
        Color.INDIGO -> "In"
        Color.VIOLET -> "Vain"
    }
```

한 분기 안에서 여러 값을 매치 패턴으로 사용할 수 있다. 그럴 경우엔 값 사이를 콤마로 분리한다.

```kotlin
fun getWarmth(color: Color) = when (color) {
    Color.RED, Color.ORANGE, Color.YELLOW -> "Warm"
    Color.GREEN -> "neutral"
    else -> "cold"
}
```

상수 값을 import하여 enum 클래스의 수식자 없이 사용 가능하다.

```kotlin
import ch02.colors.Color // 다른 패키지에 있는 Color 클래스
import ch02.colors.Color.* // Color 안에 있는 모든 값 (enum 의 모든 상수)

fun getWarmth(color: Color) = when (color) {
    RED, ORANGE, YELLOW -> "Warm"
    GREEN -> "neutral"
    else -> "cold"
}
```

`when`의 분기 조건에 여러 다른 객체를 사용할 수 있다.

- 객체 사이를 매치할 때 동등성을 사용한다.
- `when` 식은 인자 값과 매치하는 조건 값을 찾을 때까지 각 분기를 검사한다.

```kotiln
fun mix(c1: Color, c2: Color) =
    when (setOf(c1, c2)) { // when 식의 인자는 비교 가능한 객체라면 아무거나 다 된다.
        setOf(RED, YELLOW) -> ORANGE
        setOf(BLUE, VIOLET) -> INDIGO
        setOf(YELLOW, BLUE) -> GREEN
        else -> throw Exception("dirty color")
    }
```

위 예제는 약간 비효율적이다. 이 함수는 호출될 때마다 함수 인자로 주어진 두 색이 `when`의 분기 조건에 있는 다른 두 색과 같은지 비교하기 위해 여러 `Set` 인스턴스를 생성한다. **인자 없는 `when` 식을 사용하면 불필요한 객체 생성을 막을 수 있다.**

```kotlin
fun mix(c1: Color, c2: Color) =
    when {
        (c1 == RED && c2 == YELLOW) -> ORANGE
        (c1 == BLUE && c2 == YELLOW) -> GREEN
        else -> throw Exception("dirty color")
    }
```

`when`에 아무 인자도 없으려면 각 분기의 조건이 불리언 결과를 계산하는 식이어야 한다.

### 스마트 캐스트: 타입 검사와 타입 캐스트를 조합
`(1 + 2) + 4`와 같은 간단한 산술식을 계산하는 함수를 만들어보자.

- `Expr` 인터페이스에는 두 가지 구현 클래스가 존재한다.
  - 식을 평가하려면 두 가지 경우를 고려해야 한다.
  - 어떤 식이 수라면 그 값을 반환한다.
  - 어떤 식이 합계라면 좌항, 우항의 값을 계산한 뒤 그 두 값을 합한 값을 반환한다.

```kotlin
interface Expr // Expr 인터페이스 선언
class Num(val value: Int) : Expr
class Sum(val left: Expr, val right: Expr) : Expr
```

Java 스타일로 작성한 함수를 먼저 살펴보자.

```kotlin
fun eval(e: Expr): Int {
    if (e is Num) {
        val n = e as Num // Num으로 타입을 변환하는데, 이는 불필요한 중복이다.
        return n.value
    }
    if (e is Sum) {
        return eval(e.right) + eval(e.left) // 변수 e에 대해 스마트 캐스트를 사용한다.
    }
    throw IllegalArgumentException("Unknown expression")
}
```

Kotlin에선 `is`를 사용해 변수 타입을 검사한다. 이는 Java의 `instanceof`와 비슷하다. Java에서 어떤 변수의 타입을 `instanceof`로 확인한 다음 그 타입에 속한 멤버에 접근하기 위해서는 명시적으로 변수 타입을 캐스팅해야 한다.

하지만, Kotlin에선 컴파일러가 캐스팅을 해준다. 어떤 변수가 원하는 타입인지 일단 `is`로 검사하고 나면 굳이 변수를 원하는 타입으로 캐스팅하지 않아도 마치 처음부터 그 변수가 원하는 타입으로 선언된 것처럼 사용할 수 있다. 실제로는 컴파일러가 캐스팅을 수행해준다. 이를 스마트 캐스트라고 한다.

### 리팩토링: if를 when으로 변경
```kotlin
fun eval(e: Expr): Int =
    when(e) {
        is Num -> e.value
        is Sum -> evalu(e.right) + eval(e.left)
        else -> throw IllegalArgumentException("Unknown expression")
    }
```

```kotlin
fun eval(e: Expr): Int =
    when(e) {
        is Num -> { // 블록으로 사용할 수도 있다.
            println("hello $e.value")
            e.value
        }
        is Sum -> evalu(e.right) + eval(e.left)
        else -> throw IllegalArgumentException("Unknown expression")
    }
```

## 대상을 이터레이션: while과 for 루프
기본적인 반복문은 Java와 크게 다르지 않다. `while`, `do-while`은 특히나 그렇다.

### 수에 대한 이터레이션: 범위와 수열
- Kotlin의 `for`는 `for <아이템> in <원소들>` 형태를 취한다.
- Java의 `for` 루프에 해당하는 요소가 Kotlin에는 없다. 이런 루프의 가장 흔한 용례인 초깃값, 증가 값, 최종 값을 사용한 루프를 대신하기 위해 범위(range)를 사용한다.

```kotlin
val oneToTen = 1..10
// 1.rangeTo(10) 과 같다.
```

Kotlin의 범위는 폐구간(닫힌 구간) 또는 양끝을 포함하는 구간이다. 이는 두 번째 값이 항상 범위에 포함된다는 뜻이다.

```kotlin
for (i in 1..100) {
    /* 1 2 3 ... 100 */
}

// downTo: 역방향 수열을 만든다.
// step: 증가 값의 절댓값을 바꾼다.
// until: 끝 값을 포함하지 않는 반만 닫힌 범위를 만든다.
for (i in 100 downTo 1 step 2) { // 100 부터 1까지 +(2)씩
    /* 100 98 96 ... 2 */
}
```

참고로 `downTo`와 `step` 메서드는 기본 패키지인 `kotlin.range`에 아래와 같이 정의되어 있다.

```kotlin
public infix fun Int.downTo(to: Int): IntProgression {
    return IntProgression.fromClosedRange(this, to, -1)
}

public infix fun IntProgression.step(step: Int): IntProgression {
    checkStepIsPositive(step > 0, step)
    return IntProgression.fromClosedRange(first, last, if (this.step > 0) step else -step)
}
```

### 맵에 대한 이터레이션
Kotlin에선 아래와 같이 map 자료형도 간결하게 사용할 수 있다.

```kotlin
val binaryReps = TreeMap<Char, String>()

for (c in 'A'..'F') {
    val binary = Integer.toBinaryString(c.toInt()) // ASCII 코드를 2진 표현으로 바꾼다.
    binaryReps[c] = binary
}

for ((letter, binary) in binaryReps) { // 맵에 대해 이터레이션한다. 맵의 키와 값을 두 변수에 각각 대입한다.
    println("$letter = $binary")
}
```

Kotlin에서의 map은 `get`, `put`을 사용하는 대신 `map[key]`나 `map[key] = value`를 값을 가져오고 설정할 수 있다.

맵에 사용했던 구조 분해 구문을 맵이 아닌 컬렉션에도 활용할 수 있다. 그런 구조 분해 구문을 사용하면 원소의 현재 인덱스를 유지하면서 컬렉션을 이터레이션할 수 있다. 인덱스를 저장하기 위한 변수를 별도로 선언하고 루프에서 매번 그 변수를 증가시킬 필요가 없다.

```kotlin
val list = arrayListOf("10", "11", "1001")
for ((index, element) in list.withIndex()) {
    println("$index: $element")
}
```

### in으로 컬렉션이나 범위의 원소 검사
`in` 연산자를 사용해 어떤 값이 범위에 속하는지 검사할 수 있다. 반대로 `!in`을 사용하면 어떤 값이 범위에 속하지 않는지 검사할 수 있다.

```kotlin
fun isLetter(c: Char) = c in 'a'..'z' || c in 'A'..'Z'
fun isNotDigit(c: Char) = c !in '0'..'9'
```

범위는 문자에만 국한되지 않는다. 비교가 가능한 클래스라면(`java.lang.Comparable` 인터페이스를 구현한 클래스) 그 클래스의 인스턴스 객체를 사용해 범위를 만들 수 있다. `Comparable`을 사용하는 범위의 경우 그 범위 내의 모든 객체를 항상 이터레이션하지는 못한다.

## 코틀린의 예외 처리
Kotlin의 예외처리는 Java와 비슷하다. 함수는 정상적으로 종료할 수 있지만 올가 발생하면 예외를 던질 수 있다. 함수를 호출하는 쪽에서는 그 예외를 잡아 처리할 수 있다.

```kotlin
if (percentage !in 0..100) {
    throw IllegalArgumentException("A percentage value must be between 0 and 100: $percentage")
}
```

Java와 달리 Kotlin의 `throw`는 식이므로 다른 식에 포함될 수 있다.

Java와 마찬가지로 예외를 처리하려면 `try`, `catch`, `finally` 절을 함께 사용한다. **Java와 가장 큰 차이는 `throws` 절이 코드에 없다는 점이다.** Java에선 함수 선언 뒤에 `throws IOException`과 같이 체크 예외를 명시적으로 처리해야 한다. 어떤 함수가 던질 가능성이 있는 예외나 그 함수가 호출한 다른 함수에서 발생할 수 있는 예외를 모두 `catch`로 처리해야 하며, 처리되지 않은 예외는 `throws` 절에 명시해야 한다.

**Kotlin에선 함수가 던지는 예외를 지정하지 않고 발생한 예외를 잡아내도 되고 잡아내지 않아도 된다.** Kotlin 예외는 내부적으로 Kotlin 컴파일러가 변환하는게 아니라 그냥 체크 예외가 없는 것이다. 컴파일러가 이를 무시하는 것이다.

참고로 Java 7의 자원을 사용하는 `try-with-resource`의 경우 Kotlin은 그런 경우를 위한 특별한 문법을 제공하지 않는다. 하지만 라이브러리 함수로 같은 기능을 구현한다. 이는 8장에서 살펴보자.

## 요약
- 함수를 정의할 때 `fun` 키워드를 사용한다. `val`과 `var`는 각각 읽기 전용 변수와 변경 가능한 변수를 선언할 때 쓰인다.
- 문자열 템플릿을 사용하면 문자열을 연결하지 않아도 되므로 코드가 간결해진다. 변수 이름 앞에 `$`를 붙이거나, 식을 `${식}`처럼 `${}`로 둘러싸면 변수나 식의 값을 문자열 안에 넣을 수 있다.
- Kotlin에서는 값 객체 클래스를 아주 간결하게 표현할 수 있다.
- 다른 언어에도 있는 `if`는 Kotlin에서 식이며, 값을 만들어낸다.
- Kotlin `when`은 Java의 `switch`와 비슷하지만 더 강력하다.
- 어떤 변수의 타입을 검사하고 나면 굳이 그 변수를 캐스팅하지 않아도 검사한 타입의 변수처럼 사용할 수 있다. 그런 경우 컴파일러가 스마트 캐스트를 활용해 자동으로 타입을 바꿔준다.
- `for`, `while`, `do-while` 루프는 Java가 제공하는 같은 키워드의 기능과 비슷하다. 하지만 Kotlin의 `for`는 `Java`의 `for` 보다 더 편리하다. 특히 맵을 이터레이션하거나 이터레이션하면서 컬렉션의 원소와 인덱스를 함께 사용해야 하는 경우 Kotlin의 `for`가 더 편리하다.
- `1..5`와 같은 식은 범위를 만들어낸다. 범위와 수열은 Kotlin에서 같은 문법을 사용하며, `for` 루프에 대해 같은 추상화를 제공한다. 어떤 값이 범위 안에 들어있거나 들어있지 않은지 검사하기 위해 `in`이나 `!in`을 사용한다.
- Kotlin 예외 처리는 Java와 비슷하다. 다만 Kotlin에서는 함수가 던질 수 있는 예외를 선언하지 않아도 된다.