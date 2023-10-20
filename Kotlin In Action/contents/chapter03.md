# 3장. 함수 정의와 호출
- 컬렉션, 문자열, 정규식을 다루기 위한 함수
- 이름 붙인 인자, 디폴트 파라미터 값, 중위 호출 문법 사용
- 확장 함수와 확장 프로퍼티를 사용해 자바 라이브러리 적용
- 최상위 및 로컬 함수와 프로퍼티를 사용해 코드 구조화

## 코틀린에서 컬렉션 만들기
**Kotlin은 자신만의 Collection 기능을 제공하지 않는다. 기존 Java Collection을 사용한다.**

```kotlin
val set = hashSetOf(1, 7, 53)
val list = arrayListOf(1, 7, 53)
val map = hashMapOf(1 to "one", 7 to "seven", 53 to "fifty-three")

fun main(args: Array<String>) {
    println(set.javaClass)
    println(list.javaClass)
    println(map.javaClass)
}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/59acb8c5-0c6a-4b17-bcf7-a4e09a3e4b2c)

- 표준 Java Collection을 활용하면 Java 코드와 상호작용하기가 훨씬 더 쉽다.
- Java에서 Kotlin 함수를 호출하거나 반대의 상황에서 Java와 Kotlin Collection을 서로 변환할 필요가 없다.
- Kotlin Collection은 Java Collection과 똑같은 클래스지만, 더 많은 기능을 쓸 수 있다.

```kotlin
val strings = listOf("first", "second", "fourteenth")
val numbers = setOf(1, 14, 2)

fun main(args: Array<String>) {
    println(strings.last())
    println(numbers.max())
}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/767aa9a0-fed0-4ce9-82c4-00503e8cb004)

## 함수를 호출하기 쉽게 만들기
```kotlin
fun main(args: Array<String>) {
    val list = listOf(1, 2, 3)
    println(list)
}

// 결과: [1, 2, 3]
```

Java Collection에는 디폴트 `toString` 구현이 들어있다. 하지만 디폴트 구현과 달리 `(1; 2; 3)` 처럼 원소 사이를 세미콜론으로 구분하고 괄호로 리스트를 둘러싸고 싶다면 Java 프로젝트에 Guava나 Apache Commons 같은 서드파티 프로젝트를 추가하거나 직접 관련 로직을 구현해야 한다.

Kotlin에는 이런 요구 사항을 처리할 수 있는 함수가 표준 라이브러리에 이미 들어있다.

Kotlin으로 직접 그런 함수를 구현해보고 점진적으로 개선해보자.

### 이름 붙인 인자
초기 구현은 다음과 같다.

```kotlin
fun <T> joinToString(
    collection: Collection<T>,
    separator: String,
    prefix: String,
    postfix: String
): String {
    val result = StringBuilder(prefix)
    for ((index, element) in collection.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return result.toString()
}

fun main(args: Array<String>) {
    val list = listOf(1, 2, 3)
    println(joinToString(list, "; ", "(", ")"))
}

// (1; 2; 3)
```

해결하고픈 첫 번째 문제는 **함수 호출 부분의 가독성이다.**

`joinToString(list, "; ", "(", ")")`를 보면 인자로 전달한 각 문자열이 어떤 역할을 하는지 구분하기 어렵다.

```kotlin
joinToString(list, separator = "; ", prefix = "(", postfix = ")")
```

Kotlin으로 작성한 함수를 호출할 때는 함수에 전달하는 인자 중 일부(또는 전부)의 이름을 명시할 수 있다. 호출 시 인자 중 어느 하나라도 이름을 명시하고 나면 혼동을 막기위해 그 뒤에 오는 모든 인자는 이름을 꼭 명시해야 한다.

### 디폴트 파라미터 값
Java에선 일부 클래스에서 오버로딩한 메서드가 많아진다는 문제가 있다. (ex. `java.lang.Thread`의 생성자)

**Kotlin에선 함수 선언에서 파라미터의 디폴드 값을 지정할 수 있으므로 이런 오버로드 중 상당수를 피할 수 있다.** 디폴트 값을 사용해 함수를 개선해보자.

```kotlin
fun <T> joinToString(
    collection: Collection<T>,
    separator: String = ", ",
    prefix: String = "",
    postfix: String = ""
): String {
    val result = StringBuilder(prefix)
    for ((index, element) in collection.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return result.toString()
}

fun main(args: Array<String>) {
    val list = listOf(1, 2, 3)
    println(joinToString(list))
}
```

- 일반 호출 문법을 사용하려면 함수를 선언할 때와 같은 순서로 인자를 지정해야 한다. 이름 붙인 인자를 사용한 경우엔 인자 목록의 중간에 있는 인자를 생략하고, 지정하고 싶은 인자에 이름을 붙여 순서와 관계없이 지정할 수 있다.
- **함수의 디폴트 파라미터의 값은 함수를 호출하는 쪽이 아니라 함수 선언 쪽에서 지정된다.**

### 정적인 유틸리티 클래스 없애기: 최상위 함수와 프로퍼티
Java에선 모든 코드를 클래스의 메서드로 작성해야 한다. 하지만, 실전에선 어느 한 클래스에 포함시키기 어려운 코드가 많이 생기곤 한다. 일부 연산에는 비슷하게 중요한 역할을 하는 클래스가 둘 이상 있을 수도 있다. 중요한 객체는 하나뿐이지만 그 연산을 객체의 인스턴스 API에 추가해서 API를 너무 크게 만들고 싶지는 않은 경우도 있다.

그 결과 다양한 정적 메서드를 모아두는 역할만 담당하며, 특별한 상태나 인스턴스 메서드는 없는 클래스가 생겨난다.

- JDK의 `Collections` 클래스, `XxxUtil` 클래스

#### 최상위 함수
Kotlin에선 이런 무의미한 클래스가 필요 없다. 대신 함수를 직접 소스 파일의 최상위 수준, 모든 다른 클래스의 밖에 위치시키면 된다. 그런 함수들은 여전히 그 파일의 맨 앞에 정의된 패키지의 멤버 함수이므로 다른 패키지에서 그 함수를 사용하고 싶을 때는 그 함수가 정의된 패키지를 임포트해야만 한다. 하지만 임포트 시 유틸리티 클래스 이름이 추가로 들어갈 필요는 없다.

함수를 최상위로 선언하면 JVM에서 컴파일러가 컴파일할 때 자동으로 새로운 클래스를 정의해준다. 클래스 이름은 최상위 함수가 들어 있던 Kotlin 소스 파일 이름과 동일하게 생성된다.

```kotlin
// Join.kt
package me.ramos.chap03.strings

fun <T> joinToString(
    collection: Collection<T>,
    separator: String = ", ",
    prefix: String = "",
    postfix: String = ""
): String {
    val result = StringBuilder(prefix)
    for ((index, element) in collection.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return result.toString()
}
```

```java
// Java
package me.ramos.chap03.strings;

public class JoinKt {
    public static String joinToString(...) { ... }
}
```

#### 최상위 프로퍼티
함수와 마찬가지로 프로퍼티도 파일의 최상위 수준에 놓을 수 있다.

기본적으로 최상위 프로퍼티도 다른 모든 프로퍼티처럼 접근자 메서드를 통해 Java 코드에 노출된다.

- `val`의 경우 getter
- `var`의 경우 getter, setter

더 자연스럽게 사용하려면 상수를 `public static final` 필드로 컴파일해야 한다. `const` 변경자를 추가하면 프로퍼티를 `public static final` 필드로 컴파일하게 만들 수 있다.

```kotlin
const val UNIX_LINE_SEPARATOR = "\n"
```

```java
// java
public static final String UNIX_LINE_SEPARATOR = "\n"
```

## 메서드를 다른 클래스에 추가: 확장 함수와 확장 프로퍼티
**Kotlin의 확장 함수는 기존 Java API를 재작성하지 않고도 Kotlin이 제공하는 여러 편리한 기능을 사용할 수 있게 한다.**

**확장 함수는 어떤 클래스의 멤버 메서드인 것처럼 호출할 수 있지만 그 클래스의 밖에 선언된 함수다.**

```kotlin
package me.ramos.chap03.strings

fun String.lastChar(): Char = this.get(this.length - 1)

fun main(args: Array<String>) {
    println("Kotlin".lastChar())
}

// 결과: n
```

확장 함수를 만들려면 추가하려는 함수 이름 앞에 그 함수가 확장할 클래스의 이름을 덧붙이기만 하면 된다.

- 클래스 이름: **수신 객체 타입(receiver type)**
- 확장 함수가 호출되는 대상이 되는 값(객체): **수신 객체(receiver object)**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/4dba758b-30ce-4132-ac5c-ca7fbcfbd181)

- 원하는 메서드를 클래스에 추가할 수 있다. 심지어 Groovy와 같은 다른 JVM 언어로 작성된 클래스도 확장할 수 있다.
- 확장 함수 내부에는 일반적인 인스턴스 메서드의 내부에서와 마찬가지로 수신 객체의 메서드나 프로퍼티를 바로 사용할 수 있다.
  - 하지만 확장 함수가 캡슐화를 깨진 않는다는 사실을 기억하자.
- 클래스 안에서 정의한 메서드와 달리 **확장 함수 안에서는 클래스 내부에서만 사용할 수 있는 `private`, `protected` 멤버를 사용할 수 없다.**

### 임포트와 확장 함수
- 확장함수를 사용하려면 그 함수를 다른 클래스나 함수와 마찬가지로 `import` 해야 한다.
- `as` 키워드를 사용하면 임포트한 클래스나 함수를 다른 이름으로 부를 수 있다.

```kotlin
package me.ramos.chap03

import me.ramos.chap03.strings.lastChar as last

fun main(args: Array<String>) {
    println("Kotlin".last())
}
```

Kotlin 문법 상 확장 함수는 반드시 짧은 이름을 사용하자. 임포트 할 때 이름을 바꾸는 것이 확장 함수 이름 충돌을 해결할 수 있는 유일한 방법이다.

### 자바에서 확장 함수 호출
내부적으로 확장 함수는 수신 객체를 첫 번째 인자로 받는 정적 메서드다. 그래서 확장 함수를 호출해도 다른 어댑터 객체나 실행 시점 부가 비용이 들지 않는다. 이런 설계로 인해 Java에서 확장 함수를 사용하기도 편하다. 단지 정적 메서드를 호출하면서 첫 번째 인자로 수신 객체를 넘기기만 하면 된다.

다른 최상위 함수와 마찬가지로 확장 함수가 들어있는 Java 클래스 이름도 확장 함수가 들어있는 파일 이름에 따라 결정된다.

확장 함수를 `StringUtil.kt` 파일에 정의했다면 Java 파일에서 다음과 같이 호출 가능하다.

```java
char c = StringUtil.lastChar("Java");
```

### 확장 함수로 유틸리티 함수 정의
```kotlin
package me.ramos.chap03.strings

fun <T> Collection<T>.joinToString(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = ""
): String {
    val result = StringBuilder(prefix)
    for ((index, element) in this.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return result.toString()
}
```

이제 `joinToString()`을 마치 클래스의 멤버인 것처럼 호출할 수 있다.

```kotlin
package me.ramos.chap03

fun main(args: Array<String>) {
    val list = listOf(1, 2, 3)
    println(list.joinToString(separator = "; ", prefix = "(", postfix = ")"))
    println(list.joinToString())
}
```

확장 함수는 단지 정적 메서드 호출에 대한 문법적인 편의일 뿐이다. 그래서 클래스가 아닌 더 구체적인 타입을 수신 객체 타입으로 지정할 수도 있다.

### 확장 함수는 오버라이드할 수 없다.
Kotlin의 메서드 오버라이드도 일반적인 객체지향의 메서드 오버라이드와 마찬가지다. 하지만 확장 함수는 오버라이드 할 수 없다.

```kotlin
open class View {
    open fun click() = println("View clicked")
}

class Button: View() {
    override fun click() = println("Button clicked")
}

fun main(args: Array<String>) {
    val view: View = Button()
    view.click()
}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/27ccceca-b4f2-4bdb-ad5f-44562252080b)

`Button`은 `View`의 하위 타입이기 때문에 `View = Button()`과 같이 대입할 수 있다. 이 때 `click`은 `Button` 클래스가 오버라이드했다면 실제로는 `Button`이 오버라이드한 `click`이 호출된다.

```kotlin
open class View {
    open fun click() = println("View clicked")
}

class Button: View() {
    override fun click() = println("Button clicked")
}

fun View.showOff() = println("I'm a view!")
fun Button.showOff() = println("I'm a button!")

fun main(args: Array<String>) {
    val view: View = Button()
    view.showOff()
}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d16bbce7-9689-4932-b13a-6a46e6d5206f)

확장 함수는 클래스의 일부가 아니다. 확장 함수는 클래스 밖에 선언된다. 이름과 파라미터가 완전히 같은 확장 함수를 기반 클래스와 하위 클래스에 대해 정의해도 **실제로는 확장 함수를 호출할 때 수신 객체로 지정한 변수의 정적 타입에 의해 어떤 확장 함수가 호출될지 결정**되지, **그 변수에 저장된 객체의 동적인 타입에 의해 확장 함수가 결정되지 않는다.**

확장 함수를 첫 번째 인자가 수신 객체인 정적 자바 메서드로 컴파일한다는 사실을 기억한다면 이런 동작을 쉽게 이해할 수 있다.

### 확장 프로퍼티
확장 프로퍼티를 사용하면 기존 클래스 객체에 대한 프로퍼티 형식의 구문으로 사용할 수 있는 API를 추가할 수 있다. 프로퍼티라는 이름으로 불리기는 하지만 상태를 저장할 적절한 방법이 없기 때문에 실제로 확장 프로퍼티는 아무 상태도 가질 수 없다. 하지만 프로퍼티 문법으로 더 짧게 코드를 작성할 수 있어 편한 경우가 많다.

```kotlin
val String.lastChar: Car
    get() = get(length - 1)
```

- 확장 프로퍼티도 일반적인 프로퍼티와 같은데, 단지 수신 객체 클래스가 추가됐을 뿐이다.
- 뒷받침하는 필드가 없어 기본 getter 구현을 제공할 수 없으므로 최소한 getter는 꼭 정의해야 한다.
- 초기화 코드에서 계산한 값을 담을 장소가 전혀 없으므로 초기화 코드도 쓸 수 없다.

```kotlin
// 변경 가능한 확장 프로퍼티

var StringBuilder.lastChar: Char
    get() = get(length - 1)
    set(value: Char) {
        this.setCharAt(length - 1, value)
    }
```

Java에서 확장 프로퍼티를 사용하고 싶다면 항상 getter, setter를 명시적으로 호출해야 한다.

```java
StringUtilKt.getLastChar("Java")
```

## 컬렉션 처리: 가변 길이 인자, 중위 함수 호출, 라이브러리 지원
- `vararg` 키워드를 사용하면 호출 시 인자 개수가 달라질 수 있는 함수를 정의할 수 있다.
- 중위 함수 호출 구문을 사용하면 인자가 하나뿐인 메서드를 간편하게 호출할 수 있다.
- 구조 분해 선언(destructuring declaration)을 사용하면 복합적인 값을 분해해서 여러 변수에 나눠 담을 수 있다.

### 자바 컬렉션 API 확장
어떻게 Java 라이브러리 클래스의 인스턴스인 Collection에 대해 Kotlin이 새로운 기능을 추가할 수 있을까?

`last`, `max` 등 Kotlin에서 지원하는 함수는 모두 **확장 함수**다.

### 가변 인자 함수: 인자의 개수가 달라질 수 있는 함수 정의
```kotlin
fun listOf<T>(vararg values: T): List<T> { ... }
```

가변 길이 인자는 메서드를 호출할 때 원하는 개수만큼 값을 인자로 넘기면 Java 컴파일러가 배열에 그 값들을 넣어주는 기능이다.

Kotlin의 가변 길이 인자도 Java와 비슷하다. 다만 문법이 조금 다르다. 타입 뒤에 `...`를 붙이는 대신 Kotlin에선 파라미터 앞에 `vararg` 변경자를 붙인다.

이미 배열에 들어있는 원소를 가변 길이 인자로 넘길 때도 Kotlin과 Java의 구문이 다르다. Java에선 배열을 그냥 넘기면 되지만 Kotlin에선 배열을 명시적으로 풀어서 배열의 각 원소가 인자로 전달되게 해야 한다. 스프레드 연산자가 그런 작업을 해준다. 실제로는 전달하려는 배열 앞에 `*`를 붙이기만 하면 된다.

```kotlin
fun main(args: Array<String>) {
    val list = listOf("args: ", *args) // 스프레드 연산자가 배열의 내용을 펼쳐준다.
    println(list)
}
```

### 값의 쌍 다루기: 중위 호출과 구조 분해 선언
`to`라는 키워드는 Kotlin 키워드가 아니다. 이는 **중위 호출(infix call)이라는 특별한 방식으로 `to`라는 일반 메서드를 호출한 것이다.**

중위 호출 시에는 수신 객체와 유일한 메서드 인자 사이에 메서드 이름을 넣는다. 이때 객체, 메서드 이름, 유일한 인자 사이에는 공백이 들어가야 한다. 다음 두 호출은 동일하다.

<img width="511" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/bc7258d5-ff25-4d1e-bd51-9f3da4c1145b">

인자가 하나뿐인 일반 메서드나 인자가 하나뿐인 확장 함수에 중위 호출을 사용할 수 있다. **메서드를 중위 호출에 사용하게 허용하고 싶으면 `infix` 변경자를 메서드 선언 앞에 추가해야 한다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/7d6a69d8-012d-4bd7-bc76-2c69af9a6a71)

이 `to` 함수는 `Pair`의 인스턴스를 반환한다. `Pair`는 Kotlin 표준 라이브러리 클래스로, 두 원소로 이뤄진 순서쌍을 표현한다.

```kotlin
// Pair의 내용으로 두 변수를 즉시 초기화 할 수 있다.

val (number, name) = 1 to "one"
```

위와 같은 기능을 구조 분해 선언이라 부른다. 다음 그림은 구조 분해가 어떻게 작동하는지 보여준다.

<img width="330" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9d0be43a-8f22-4182-9d50-e2ef93b5f67e">

`Pair` 인스턴스 외 다른 객체에도 구조 분해를 적용할 수 있다. `key`, `value`라는 두 변수를 맵의 원소를 사용해 초기화 하는것도 예시다.

루프에서도 구조 분해 선언을 활용할 수 있다.

```kotlin
for ((index, element) in collection.withIndex()) {
    println("$index: $element")
}
```

## 문자열과 정규식 다루기
Kotlin 문자열은 Java 문자열과 같다. Kotlin 코드가 만들어낸 문자열을 아무 Java 메서드에 넘겨도 되며, Java 코드에서 받은 문자열을 아무 Kotlin 표준 라이브러리 함수에 전달해도 전혀 문제없다. 특별한 변환도 필요 없고 Java 문자열을 감싸는 별도의 wrapper도 생기지 않는다.

Kotlin은 다양한 확장 함수를 제공함으로써 표준 Java 문자열을 더 즐겁게 다루게 해준다.

### 문자열 나누기
Kotlin은 Java의 `split` 대신 여러 가지 다른 조합의 파라미터를 받는 `split` 확장 함수를 제공함으로써 혼동을 야기하는 메서드를 감춘다. 정규식을 파라미터로 받는 함수는 `String`이 아닌 `Regex` 타입의 값을 받는다. 따라서 Kotlin에선 `split` 함수에 전달하는 값의 타입에 따라 정규식이나 일반 텍스트 중 어느 것으로 문자열을 분리하는지 쉽게 알 수 있다.

```kotlin
fun main(args: Array<String>) {
    println("12.345-6.A".split("\\.|-".toRegex()))
}
```

regex를 잘 몰라도 여러 구분 문자열을 지정할 수 있는 `split` 확장 함수를 오버로딩한 버전도 있다.

```kotlin
fun main(args: Array<String>) {
    println("12.345-6.A".split(".", "-"))
}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d16130bd-5495-4477-aa35-8e8e49fc6482)

### 정규식과 3중 따옴표로 묶은 문자열
<img width="360" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fa9de5e0-1686-4d0c-8ff4-01b59f7c36c8">

```kotlin
fun parsePath(path: String) {
    val directory = path.substringBeforeLast("/")
    val fullName = path.substringAfterLast("/")

    val fileName = fullName.substringBeforeLast(".")
    val extension = fullName.substringAfterLast(".")

    println("Dir: $directory, name: $fileName, ext: $extension")
}

fun main(args: Array<String>) {
    parsePath("/Users/yole/kotlin-book/chapter.adoc")
}
```

```kotlin
fun parsePath(path: String) {
    val regex = """(.+)/(.+)\.(.+)""".toRegex()
    val matchResult = regex.matchEntire(path)

    if (matchResult != null) {
        val (directory, fileName, extension) = matchResult.destructured
        println("Dir: $directory, name: $fileName, ext: $extension")
    }
}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b9ea1eef-0755-42ec-948f-6decbcc4dbfd)

아래와 같이 삼중 따옴표를 통해 이스케이프 문자도 신경 쓰지 않아도 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/765af761-16de-4ab1-b221-6fd6708b9bdc)

### 여러 줄 3중 따옴표 문자열
```kotlin
val kotlinLogo = """| //
                   .|//
                   .|/ \"""
```

여러 줄 문자열에는 들여쓰기나 줄 바꿈을 포함한 모든 문자가 들어간다.

## 코드 다듬기: 로컬 함수와 확장
**Kotlin에선 함수에서 추출한 함수를 원 함수 내부에 중첩시킬 수 있다.**

```kotlin
class User(val id: Int, val name: String, val address: String)

fun saveUser(user: User) {
    if (user.name.isEmpty()) {
        throw IllegalArgumentException(
            "Can't save user ${user.id}: empty Name")
    }

    if (user.address.isEmpty()) {
        throw IllegalArgumentException(
            "Can't save user ${user.id}: empty Address")
    }

    // Save user to the database
}

fun main(args: Array<String>) {
    saveUser(User(1, "", ""))
}
```

위 예제는 중복된 코드가 있다. 아래와 같이 로컬 함수를 사용하면 간단하게 중복을 제거하고 코드 구조를 깔끔하게 유지할 수 있다.

```kotlin
class User(val id: Int, val name: String, val address: String)

fun saveUser(user: User) {

    fun validate(user: User,
                 value: String,
                 fieldName: String) {
        if (value.isEmpty()) {
            throw IllegalArgumentException(
                "Can't save user ${user.id}: empty $fieldName")
        }
    }

    validate(user, user.name, "Name")
    validate(user, user.address, "Address")

    // Save user to the database
}

fun main(args: Array<String>) {
    saveUser(User(1, "", ""))
}
```

**로컬 함수는 자신이 속한 바깥 함수의 모든 파라미터와 변수를 사용할 수 있다.** 이런 성질을 이용해 불필요한 `User` 파라미터를 없애보자.

```kotlin
class User(val id: Int, val name: String, val address: String)

fun saveUser(user: User) {

    fun validate(value: String, fieldName: String) { // user 파라미터 중복 제거
        if (value.isEmpty()) {
            throw IllegalArgumentException(
                "Can't save user ${user.id}: empty $fieldName") // 바깥 함수의 파라미터에 직접 접근 가능
        }
    }

    validate(user.name, "Name")
    validate(user.address, "Address")

    // Save user to the database
}

fun main(args: Array<String>) {
    saveUser(User(1, "", ""))
}
```

이 예제를 더 개선하고 싶다면 검증 로직을 `User` 클래스를 확장한 함수로 만들 수도 있다.

```kotlin
class User(val id: Int, val name: String, val address: String)

fun User.validateBeforeSave() {
    fun validate(value: String, fieldName: String) {
        if (value.isEmpty()) {
            throw IllegalArgumentException(
                "Can't save user ${id}: empty $fieldName")
        }
    }

    validate(name, "Name")
    validate(address, "Address")
}

fun saveUser(user: User) {
    user.validateBeforeSave()

    // Save user to the database
}

fun main(args: Array<String>) {
    saveUser(User(1, "", ""))
}
```

코드를 확장 함수로 뽑아내는 기법은 놀랄 만큼 유용하다. `User`는 라이브러리에 있는 클래스가 아니라 우리의 코드 기반에 있는 클래스지만, 이 경우 검증 로직은 `User`를 사용하는 다른 곳에선 쓰이지 않는 기능이기 때문에 `User`에 포함시키고 싶진 않다.

`User`를 간결하게 유지하면 생각해야 할 내용이 줄어들어 더 쉽게 코드를 파악할 수 있다. 반면 한 객체만을 다루면서 객체의 비공개 데이터를 다룰 필요는 없는 함수는 위와 같이 확장 함수로 만들면 `객체.멤버` 처럼 수신 객체를 지정하지 않고도 공개된 멤버 프로퍼티나 메서드에 접근할 수도 있다.

확장 함수를 로컬 함수로 정의할 수도 있다. 하지만 중첩된 함수의 깊이가 깊어지면 코드를 읽기가 상당히 어려워진다. **따라서 일반적으론 한 단계만 함수를 중첩시키는 걸 권장한다.**

## 요약
- Kotlin은 자체 Collection 클래스를 정의하지 않지만 Java 클래스를 확장해서 더 풍부한 API를 제공한다.
- 함수 파라미터의 디폴트 값을 정의하면 오버로딩한 함수를 정의할 필요성이 줄어든다. 이름 붙인 인자를 사용하면 함수의 인자가 많을 때 함수 호출의 가독성을 더 향상시킬 수 있다.
- Kotlin 파일에서 클래스 멤버가 아닌 최상위 함수와 프로퍼티를 직접 선언할 수 있다. 이를 활용하면 코드 구조를 더 유연하게 만들 수 있다.
- 확장 함수와 프로퍼티를 사용하면 외부 라이브러리에 정의된 클래스를 포함해 모든 클래스의 API를 그 클래스의 소스코드를 바꿀 필요 없이 확장할 수 있다. 확장 함수를 사용해도 실행 시점에 부가 비용이 들지 않는다.
- 중위 호출을 통해 인자가 하나 밖에 없는 메서드나 확장 함수를 더 깔끔한 구문으로 호출할 수 있다.
- Kotlin은 정규식과 일반 문자열을 처리할 때 유용한 다양한 문자열 처리 함수를 제공한다.
- Java 문자열로 표현하려면 수많은 이스케이프가 필요한 문자열의 경우 3중 따옴표 문자열을 사용하면 더 깔끔하게 표현할 수 있다.
- 로컬 함수를 써서 코드를 더 깔끔하게 유지하면서 중복을 제거할 수 있다.