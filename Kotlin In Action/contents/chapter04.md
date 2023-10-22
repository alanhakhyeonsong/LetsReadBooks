# 4장. 클래스, 객체, 인터페이스
## 클래스 계층 정의
### 코틀린 인터페이스
```kotlin
interface Clickable {
    fun click()
}

class Button : Clickable {
    override fun click() = println("I was clicked")
}

fun main(args: Array<String>) {
    Button().click()
}
```

Kotlin에선 크래스 이름 뒤에 `:`를 붙이고 인터페이스와 클래스 이름을 적는 것으로 클래스 확장과 인터페이스 구현을 모두 처리한다. Java와 마찬가지로 클래스는 인터페이스를 원하는 만큼 개수 제한 없이 마음대로 구현할 수 있지만, 클래스는 오직 하나만 확장할 수 있다.

Java의 `@Override` 애노테이션과 비슷한 `override` 변경자는 상위 클래스나 상위 인터페이스에 있는 프로퍼티나 메서드를 오버라이드한다는 표시다. **하지만 Java와는 달리 Kotlin에선 `override` 변경자를 꼭 사용해야 한다.**

인터페이스 메서드도 디폴트 구현을 제공할 수 있다. 그런 경우 메서드 앞에 `default`를 붙여야 하는 Java 8과 달리 Kotlin에선 메서드 본문을 메서드 시그니처 뒤에 추가하면 된다.

```kotlin
interface Clickable {
    fun click()
    fun showOff() = println("I'm clickable!")
}

interface Focusable {
    fun setFocus(b: Boolean) =
        println("I ${if (b) "got" else "lost"} focus.")

    fun showOff() = println("I'm focusable!")
}

class Button : Clickable, Focusable {
    override fun click() = println("I was clicked")

    override fun showOff() {
        super<Clickable>.showOff()
        super<Focusable>.showOff()
    }
}

fun main(args: Array<String>) {
    val button = Button()
    button.showOff()
    button.setFocus(true)
    button.click()
}
```

Kotlin 컴파일러는 이름과 시그니처가 같은 멤버 메서드에 대해 둘 이상의 디폴트 구현이 있는 경우 하위 클래스에 직접 구현하게 강제한다.

### open, final, abstract 변경자: 기본적으로 final
**Java의 클래스와 메서드는 기본적으로 상속에 대해 열려있지만 Kotlin의 클래스와 메서드는 기본적으로 `final`이다.**

클래스의 상속을 허용하려면 클래스 앞에 `open` 변경자를 붙여야 한다. 또한 오버라이드를 허용하고 싶은 메서드나 프로퍼티의 앞에도 `open` 변경자를 붙여야 한다.

```kotlin
open class RichButton : Clickable {
    fun disable() {}
    open fun animate() {}
    override fun click() {}
}
```

오버라이드하는 메서드의 구현을 하위 클래스에서 오버라이드하지 못하게 금지하려면 오버라이드하는 메서드 앞에 `final`을 명시해야 한다.

Java처럼 Kotlin에서도 클래스를 `abstract`로 선언할 수 있다. 이로 선언한 추상 클래스는 인스턴스화할 수 없다. 추상 클래스는 구현이 없는 추상 멤버만 있기 때문에 하위 클래스에서 그 추상 멤버를 오버라이드해야만 하는 게 보통이다. **추상 멤버는 항상 열려있다.**

```kotlin
abstract class Animated {

    abstract fun animate()

    open fun stopAnimating() {}

    fun animateTwice() {}
}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/614c8291-ae55-4734-872b-766a1b34a086)

### 가시성 변경자: 기본적으로 공개
기본적으로 Kotlin 가시성 변경자는 Java와 비슷하다. 하지만 Kotlin의 기본 가시성은 Java와 다르다. **아무 변경자도 없는 경우 선언은 모두 공개된다.**

- Java의 기본 가기성인 package-private는 Kotlin엔 없다. → Kotlin은 패키지를 네임스페이스를 관리하기 위한 용도로만 사용한다.
- `internal`이라는 새로운 가시성 변경자를 도입했다.
- Kotlin에선 최상위 선언에 대해 `private` 가시성을 허용한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0a5c5e0d-66e7-4f6c-940e-312a2761ba94)

### 내부 클래스와 중첩된 클래스: 기본적으로 중첩 클래스
Java처럼 Kotlin에서도 클래스 안에 다른 클래스를 선언할 수 있다. 클래스 안에 다른 클래스를 선언하면 도우미 클래스를 캡슐화하거나 코드 정의를 그 코드를 사용하는 곳 가까이에 두고 싶을 때 유용하다.

**Java와의 차이는 Kotlin의 nested class는 명시적으로 요청하지 않는 한 바깥쪽 클래스 인스턴스에 대한 접근 권한이 없다는 점이다.** 이는 Kotlin nested class에 아무런 변경자가 붙지 않으면 Java `static` 중첩 클래스와 같기 때문이다. 이를 내부 클래스로 변경해서 바깥쪽 클래스에 대한 참조를 포함하게 만들고 싶다면 `inner` 변경자를 붙여야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3172ce52-c204-4b7e-99d2-a68d47ec9671)

```kotlin
clas Outer {
    inner class Inner {
        fun getOuterReference(): Outer = this@Outer
    }
}
```

### 봉인된 클래스: 클래스 계층 정의 시 계층 확장 제한

```kotlin
interface Expr
class Num(val value: Int) : Expr
class Sum(val left: Expr, val right: Expr) : Expr

fun eval(e: Expr): Int = 
    when (e) {
        is Num -> e.value
        is Sum -> eval(e.right) + eval(e.left)
        else -> throw IllegalArgumentException("Unknown expression")
    }
```

상위 클래스인 `Expr`에는 숫자를 표현하는 `Num`과 덧셈 연산을 표현하는 `Sum`이라는 두 하위 클래스가 있다. `when` 식에서 이 모든 하위 클래스를 처리하면 편리하지만, `Num`과 `Sum`이 아닌 경우를 처리하는 `else` 분기를 반드시 넣어줘야 한다.

Kotlin 컴파일러는 `when`을 사용해 `Expr` 타입의 값을 검사할 때 꼭 디폴트 분기인 `else`를 덧붙이게 강제한다. 이는 항상 편하지도 않고 버그가 발생할 수 있다.

Kotlin은 이런 문제애 대해 `sealed` 클래스라는 해법을 제공한다. **상위 클래스에 `sealed` 변경자를 붙이면 그 상위 클래스를 상속한 하위 클래스 정의를 제한할 수 있다.** `sealed` 클래스의 하위 클래스를 정의할 때는 반드시 상위 클래스 안에 중첩시켜야 한다.

```kotlin
sealed class Expr {
    class Num(val values: Int) : Expr()
    class Sum(val left: Expr, val right: Expr) : Expr()
}

fun eval(e: Expr): Int = 
    when (e) {
        is Expr.Num -> e.value
        is Expr.Sum -> eval(e.right) + eval(e.left)
    }
```

`when` 식에서 `sealed` 클래스의 모든 하위 클래스를 처리한다면 디폴트 분기가 필요 없다. **`sealed`로 표시된 클래스는 자동으로 `open`된다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b5b8dae3-8390-494e-8d8d-e09a49f36390)

## 뻔하지 않은 생성자와 프로퍼티를 갖는 클래스 선언
Java에선 생성자를 하나 이상 선언할 수 있다. Kotlin도 비슷하지만 Kotlin은 주 생성자와 부 생성자를 구분한다. 또한 Kotlin에선 초기화 블록을 통해 초기화 로직을 추가할 수 있다.

### 클래스 초기화: 주 생성자와 초기화 블록
```kotlin
class User(val nickname: String)
```

위와 같이 클래스 이름 뒤에 오는 괄호로 둘러싸인 코드를 **주 생성자**라고 한다. **생성자 파라미터를 지정하고 그 생성자 파라미터에 의해 초기화되는 프로퍼티를 정의하는 두 가지 목적에 쓰인다.**

가장 명시적인 선언으로 풀어보면 다음과 같다.

```kotlin
class User constructor(_nickname: String) {
    val nickname: String

    init {
        nickname = _nickname
    }
}
```

- `constructor`: 주 생성자나 부 생성자 정의를 시작할 때 사용한다.
- `init`: 초기화 블록을 시작한다. 여기에는 클래스의 객체가 만들어질 때 실행될 초기화 코드가 들어가며 주 생성자와 함께 사용된다.

```kotlin
// 이렇게 바꿀 수 있다.
class User constructor(_nickname: String) {
    val nickname = _nickname
}
```

클래스에 기반 클래스가 있다면 주 생성자에서 기반 클래스의 생성자를 호출해야 할 필요가 있다. 기반 클래스를 초기화하려면 기반 클래스 이름 뒤에 괄호를 치고 생성자 인자를 넘긴다.

```kotlin
open class User(val nickname: String) { ... }
class TwitterUser(nickname: String) : User(nickname) { ... }
```

클래스를 정의할 때 별도로 생성자를 정의하지 않으면 컴파일러가 자동으로 아무 일도 하지 않는 인자가 없는 디폴트 생성자를 만들어준다.

```kotlin
open class Button // 인자가 없는 디폴트 생성자가 만들어진다.

class RadioButton: Button() // 반드시 Button 클래스의 생성자를 호출해야 한다.
```

별도로 생성자를 정의하지 않은 클래스를 상속한 하위 클래스는 반드시 기반 클래스의 생성자를 호출해야 한다. 반면 인터페이스는 생성자가 없기 때문에 이름 뒤에는 아무 괄호도 없다.

어떤 클래스를 외부에서 인스턴스화하지 못하게 막고 싶다면 모든 생성자를 `private`으로 만들면 된다.

```kotlin
class Secretive private constructor() {}
```

### 부 생성자: 상위 클래스를 다른 방식으로 초기화
일반적으로 Kotlin에선 생성자가 여럿 있는 경우가 Java보다 훨씬 적다. Java에서 오버로드한 생성자가 필요한 상황 중 상당수는 Kotlin의 디폴트 파라미터 값과 이름 붙인 인자 문법을 사용해 해결할 수 있다.

> 인자에 대한 디폴트 값을 제공하기 위해 부 생성자를 여럿 만들지 말라. 대신 파라미터의 디폴트 값을 생성자 시그니처에 직접 명시하라.

```kotlin
open class View {
    constructor(cxt: Context) {
        // ...
    }

    constructor(cxt: Context, attr: AttributeSet) {
        // ...
    }
}

class MyButton : View {
    // 이렇게 상속하듯이 콜론(:) 을 이용한다.
    constructor(cxt: Context) : super(ctx) {
        // ...
    }

    constructor(cxt: Context, attr: AttributeSet) : super(ctx, attr) {
        // ...
    }

    constructor(cxt: Context, _name: String) : this(ctx) { // 다른 생성자를 재사용
        name = _name
    }
}
```

클래스에 주 생성자가 없다면 모든 부 생성자는 반드시 상위 클래스를 초기화하거나 다른 생성자에게 생성을 위임해야 한다.

### 인터페이스에 선언된 프로퍼티 구현
Kotlin에선 인터페이스에 추상 프로퍼티 선언을 넣을 수 있다.

```kotlin
interface User {
    val nickname: String
}

class PrivateUser(override val nickname: String) : User // 주 생성자에 있는 프로퍼티

class SubscribingUser(val email: String) : User {
    override val nickname: String
        get() = email.substringBefore('@') // 커스텀 getter
}

class FacebookUser(val accountId: Int) : User {
    override val nickname = getFacebookName(accountId) // 프로퍼티 초기화 식
}
```

인터페이스에는 추상 프로퍼티 뿐 아니라 getter, setter가 있는 프로퍼티를 선언할 수도 있다. 물론 그런 getter, setter는 뒷받침하는 필드를 참조할 수 없다.

```kotlin
interface User {
    val email: String
    val nickname: String
        get() = email.substringBefore('@') // 프로퍼티에 뒷받침 하는 필드가 없다. 대신 매번 결과를 계산해 돌려준다.
}
```

### 게터와 세터에서 뒷받침하는 필드에 접근
```kotlin
class User(val name: String) {
    var address: String = "unspecified"
        set(value: String) {
            println("""
                Address was changed for $name:
                "$field" -> "$value".""".trimIndent()) // 뒷받침하는 필드 값 읽기
            field = value // 뒷받침하는 필드 값 변경하기
        }
}

fun main(args: Array<String>) {
    val user = User("Ramos")
    user.address = "Seoul, Republic of Korea"
}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1b2b6c6c-6897-4849-bd09-c6ed0cf5078e)

Kotlin에서 프로퍼티의 값을 바꿀 때는 `user.address = "new value"` 같이 필드 설정 구문을 사용한다. 이 구문은 내부적으론 `address`의 setter를 호출한다.

**접근자의 본문에선 `field`라는 특별한 식별자를 통해 뒷받침하는 필드에 접근할 수 있다. getter에선 `field` 값을 읽을 수만 있고, setter에선 `field` 값을 읽거나 쓸 수 있다. 이를 백킹 필드라고 한다.**

### 접근자의 가시성 변경
접근자의 가시성은 기본적으론 프로퍼티의 가시성과 같다. 원한다면 `get`이나 `set` 앞에 가시성 변경자를 추가해 접근자의 가시성을 변경할 수 있다.

```kotlin
class LengthCounter {
    var counter: Int = 0
        private set // 이 클래스 밖에서 이 프로퍼티의 값을 바꿀 수 없다.
    
    fun addWord(word: String) {
        counter += word.length
    }
}
```

## 컴파일러가 생성한 메서드: 데이터 클래스와 클래스 위임
Java 플랫폼에선 클래스가 `equals`, `hashCode`, `toString` 등의 메서드를 구현해야 한다. 그리고 이런 메서드들은 보통 비슷한 방식으로 기계적으로 구현할 수 있다. Java IDE들이 이런 메서드를 자동으로 만들어줄 수 있어서 직접 이런 메서드를 작성할 일은 많지 않다.

Kotlin 컴파일러는 한걸음 더 나가 이런 메서드를 기계적으로 생성하는 작업을 보이지 않는 곳에서 해준다.

### 데이터 클래스: 모든 클래스가 정의해야 하는 메서드 자동 생성
Kotlin은 `equals`, `hashCode`, `toString` 메서드를 생성할 필요도 없이 `data`라는 변경자를 클래스 앞에 붙이면 필요한 메서드를 컴파일러가 자동으로 만들어준다. `data` 변경자가 붙은 클래스를 데이터 클래스라고 부른다.

```kotlin
data class Client(val name: String, val postalCode: Int)
```

- 인스턴스 간 비교를 위한 `equals`
- `HashMap`과 같은 해시 기반 컨테이너에서 키로 사용할 수 있는 `hashCode`
- 클래스의 각 필드를 선언 순서대로 표시하는 문자열 표현을 만들어주는 `toString`
- 객체를 복사하면서 일부 프로퍼티를 바꿀 수 있게 해주는 `copy`
- `componentX`

자세한 사항은 아래 다음을 참고하자.

- [Java의 Record와 Kotlin의 Data Class - 기술블로그 정리](https://velog.io/@songs4805/Java%EC%9D%98-Recode%EC%99%80-Kotlin%EC%9D%98-Data-Class)

`equals`와 `hashCode`는 주 생성자에 나열된 모든 프로퍼티를 고려해 만들어진다. 생성된 `equals` 메서드는 모든 프로퍼티 값의 동등성을 확인한다. `hashCode` 메서드는 모든 프로퍼티의 해시 값을 바탕으로 계산한 해시 값을 반환한다. 이때 주 생성자 밖에 정의된 프로퍼티는 `equals`나 `hashCode`를 계산할 때 고려의 대상이 아니라는 사실에 유의해야 한다.

데이터 클래스의 프로퍼티가 꼭 `val`일 필요는 없다. 하지만 **데이터 클래스의 모든 프로퍼티를 읽기 전용으로 만들어 데이터 클래스를 불변 클래스로 만들라고 권장한다.**

- HashMap 등 컨테이너에 데이터 클래스를 담는 경우 불변성이 필수적이다.
- 다중스레드에서 스레드를 동기화해야 할 필요성이 줄어든다.

데이터 클래스 인스턴스를 불변 객체로 더 쉽게 활용할 수 있게 `copy` 메서드를 제공한다.

- 객체를 복사하면서 일부 프로퍼티를 바꿔서 복사본을 생성할 수 있다.
- 복사본은 원본과 다른 생명주기를 가진다.
- 복사를 하면서 일부 프로퍼티 값을 바꾸거나 복사본을 제거해도 원본을 참조하는 다른 부분에 영향을 끼치지 않는다.

### 클래스 위임: by 키워드 사용
상속을 허용하지 않는 클래스에 새로운 동작을 추가해야 할 때가 있다. 이럴 때 사용하는 방법이 **데코레이터 패턴이다.**

이 패턴의 핵심은 **상속을 허용하지 않는 클래스(기존 클래스) 대신 사용할 수 있는 새로운 클래스(데코레이터)를 만들되 기존 클래스와 같은 인터페이스를 데코레이터가 제공하게 만들고, 기존 클래스를 데코레이터 내부에 필드로 유지하는 것이다.**

- 새로 정의해야 하는 기능은 데코레이터의 메서드에 새로 정의한다.
- 기존 기능이 그대로 필요한 부분은 데코레이터의 메서드가 기존 클래스의 메서드에게 요청을 전달한다.

데코레이터 패턴의 단점은 준비 코드가 상당히 많이 필요하다는 점이다. 예를 들어 `Collection` 같이 비교적 단순한 인터페이스를 구현하면서 아무 동작도 변경하지 않는 데코레이터를 만들 때조차도 다음과 같이 복잡한 코드를 작성해야 한다.

```kotlin
class DelegatingCollection<T> : Collection<T> {
    private val innerList = arrayListOf<T>()

    override val size: Int get() = innerList.size
    override fun isEmpty(): Boolean = innerList.isEmpty()
    override fun contains(element: T): Boolean = innerList.contains(element)
    override fun iterator(): Iterator<T> = innerList.iterator()
    override fun containsAll(elements: Collection<T>): Boolean = innerList.containsAll(elements)
}
```

**이런 위임을 언어가 제공하는 일급 시민 기능으로 지원한다는 점이 Kotlin의 장점이다.** 인터페이스를 구현할 때 `by` 키워드를 통해 그 인터페이스에 대한 구현을 다른 객체에 위임 중이라는 사실을 명시할 수 있다.

```kotlin
class DelegatingCollection<T>(
    innerList: Collection<T> = ArrayList<T>()
) : Collection<T> by innerList {}
```

클래스 안에 있던 모든 메서드 정의가 없어졌다. 컴파일러가 그런 전달 메서드를 자동으로 생성하며 자동 생성한 코드의 구현은 `DelegatingCollection`에 있던 구현과 비슷하다.

메서드 중 일부 동작을 변경하고 싶은 경우 메서드를 오버라이드하면 컴파일러가 생성한 메서드 대신 오버라이드한 메서드가 쓰인다.

```kotlin
class CountingSet<T>(
    val innerSet: MutableCollection<T> = HashSet<T>()
) : MutableCollection<T> by innerSet { // MutableCollection 구현을 innerSet에게 위임

    var objectAdded = 0

    override fun add(element: T): Boolean { // 위임하지 않고 새로운 구현 제공
        objectsAdded++
        return innerSet.add(element)
    }

    override fun addAll(c: Collection<T>): Boolean { // 위임하지 않고 새로운 구현 제공
        objectsAdded += c.size
        return innerSet.addAll(c)
    }
}
```

## object 키워드: 클래스 선언과 인스턴스 생성
- 객체 선언(object declaration): 싱글턴을 정의하는 방법 중 하나다.
- 동반 객체(companion object): 인스턴스 메서드는 아니지만 어떤 클래스와 관련 있는 메서드와 팩토리 메서드를 담을 때 쓰인다.
- 객체 식: Java의 anonymous inner class 대신 사용된다.

### 객체 선언: 싱글턴을 쉽게 만들기
Kotlin은 객체 선언 기능을 통해 싱글턴을 언어에서 기본 지원한다. 객체 선언은 클래스 선언과 그 클래스에 속한 단일 인스턴스의 선언을 합친 선언이다. 객체 선언은 `object` 키워드로 시작한다. 객체 선언은 클래스를 정의하고 그 클래스의 인스턴스를 만들어서 변수에 저장하는 모든 작업을 단 한 문장으로 처리한다.

```kotlin
object Payroll {
    val allEmployees = arrayListOf<Person>()

    fun calculateSalary() {
        for (person in allEmployees) {
            // ...
        }
    }
}
```

- 싱글턴 객체는 객체 선언문이 있는 위치에서 생성자 호출 없이 즉시 만들어진다.
  - 따라서 객체 선언에는 생성자 정의가 필요 없다.
- 변수와 마찬가지로 객체 선언에 사용한 이름 뒤에 `.`를 붙이면 객체에 속한 메서드나 프로퍼티에 접근할 수 있다.

```kotlin
Payroll.allEmployees.add(Person(...))
Payroll.calculateSarary()
```

객체 선언도 클래스나 인터페이스를 상속할 수 있다. 프레임워크를 사용하기 위해 특정 인터페이스를 구현해야 하는데, 그 구현 내부에 다른 상태가 필요하지 않은 경우에 이런 기능이 유용하다. 일반 객체를 사용할 수 있는 곳에서는 항상 싱글턴 객체를 사용할 수 있다. 아래 예시와 같이 `Comparator`를 구현하여 `compare` 메서드가 있는 `CaseInsensitiveFileComparator` 객체가 있을 때, `sortedWith` 메서드에서 해당 객체를 전달받아 사용하고 있다.

```kotlin
object CaseInsensitiveFileComparator : Comparator<File> {
    override fun compare(file1: File, file2: File): Int {
        return file1.path.compareTo(file2.path, ignoreCase = true)
    }
}

fun main(args: Array<String>) {
    println(CaseInsensitiveFileComparator.compare(
        File("/User"), File("/user")))
    val files = listOf(File("/Z"), File("/a"))
    println(files.sortedWith(CaseInsensitiveFileComparator))
}
```

그러나 위와 같은 방식보단 아래와 같이 중첩 객체를 사용해서 `Comparator` 클래스를 내부에 정의하는 게 더 좋다.

```kotlin
data class Person(val name: String) {
    object NameComparator : Comparator<Person> {
        override fun compare(p1: Person, p2: Person): Int = 
            p1.name.compareTo(p2.name)
    }
}
```

> 📌 참고: 코틀린 객체 선언은 유일한 인스턴스에 대한 정적인 필드가 있는 자바 클래스로 컴파일된다. 자바 코드에서 코틀린 싱글턴 객체를 사용하려면 정적인 INSTANCE 필드를 통하면 된다.

### 동반 객체: 팩토리 메서드와 정적 멤버가 들어갈 장소
Kotlin은 Java의 `static` 키워드를 지원하지 않는다. 대신 Kotlin에선 패키지 수준의 최상위 함수와 객체 선언을 활용한다. 대부분의 경우 최상위 함수를 활용하는 편을 더 권장한다. 하지만, 최상위 함수는 아래 그림처럼 `private`로 표시된 클래스의 비공개 멤버에 접근할 수 없다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/daa170c7-e231-457d-85e8-9227c380751a)

클래스 내부 정보에 접근해야 하는 함수가 필요할 때는 클래스에 중첩된 객체 선언의 멤버 함수로 정의해야 한다. 그런 함수의 대표적인 예로 팩토리 메서드를 들 수 있다.

클래스 안에 정의된 객체 중 하나에 `companion`이라는 특별한 표시를 붙이면 그 클래스의 동반 객체로 만들 수 있다. 동반 객체의 프로퍼티나 메서드에 접근하려면 그 동반 객체가 정의된 클래스 이름을 사용한다. 이때 객체의 이름을 따로 지정할 필요가 없다. 그 결과 동반 객체의 멤버를 사용하는 구문은 Java의 정적 메서드 호출이나 정적 필드 사용 구문과 같아진다.

```kotlin
class A {
    companion object {
        fun bar() {
            println("Companion object called")
        }
    }
}

fun main(args: Array<String>) {
    A.bar()
}
```

동반 객체는 자신을 둘러싼 클래스의 모든 `private` 멤버에 접근할 수 있다. 따라서 동반 객체는 바깥쪽 클래스의 `private` 생성자도 호출할 수 있다. 따라서 동반 객체는 팩토리 패턴을 구현하기 가장 적합한 위치다.

```kotlin
class User private constructor(val nickname: String) { // 주 생성자를 비공개로 만든다.
    companion object { // 동반 객체를 선언한다.
        fun newSubscribingUser(email: String) = 
            User(email.substringBefore('@'))
        fun newFacebookUser(accountId: Int) = // 페이스북 사용자 ID로 사용자를 만드는 팩토리 클래스 
            User(getFacebookName(accountId))
    }
}
```

- 팩토리 메소드 이름을 정할 수 있고, 팩토리 메소드는 팩토리 메서드가 선언된 클래스의 하위 클래스 객체를 반환할 수 있다.
- 클래스를 확장해야 하는 경우에는 동반 객체 멤버를 하위 클래스에서 오버라이드 할 수 없으므로 여러 생성자를 사용하는 편이 더 낫다.

### 동반 객체를 일반 객체처럼 사용
```kotlin
class Person(val name: String) {
    companion object Loader { // 동반 객체에 이름을 붙인다.
        fun fromJSON(jsonText: String): Person = ...
    }
}
```

위와 같이 동반 객체에 이름을 붙여 사용할 수 있다.

#### 동반 객체에서 인터페이스 구현
```kotlin
interface JSONFactory<T> {
    fun fromJSON(jsonText: String): T
}

class Person(val name: String) {
    companion object : JSONFactory<Person> {
        override fun fromJSON(jsonText: String): Person = ... // 동반 객체가 인터페이스를 구현
    }
}
```

#### 동반 객체 확장
```kotlin
// 비즈니스 로직 모듈
class Person(val firstName: String, val lastName: String) {
    companion object { // 비어있는 동반 객체를 선언한다.
    }
}

// 클라이언트/서버 통신 모듈
fun Person.Companion.fromJSON(json: String): Person { // 확장 함수를 선언한다.
    // ...
}
```

동반 객체에 대한 확장 함수를 작성할 수 있으려면 원래 클래스에 동반 객체를 꼭 선언해야 한다. 설령 빈 객체라도 동반 객체가 꼭 있어야 한다.

### 객체 식: 무명 내부 클래스를 다른 방식으로 작성
무명 객체를 정의할 때도 `object` 키워드를 쓴다. 무명 객체는 Java의 무명 내부 클래스를 대신한다.

```kotlin
window.addMouseListener(
    object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            // ...
        }

        override fun mouseEntered(e: MouseEvent) {
            // ...
        }
    }
)
```

객체 선언과 같지만 객체 이름이 빠졌다는 점이 다르다. 객체 식은 클래스를 정의하고 그 클래스에 속한 인스턴스를 생성하지만, 그 클래스나 인스턴스에 이름을 붙이진 않는다. 이런 경우 보통 함수를 호출하면서 인자로 무명 객체를 넘기기 때문에 클래스와 인스턴스 모두 이름이 필요하지 않다. 하지만 객체에 이름을 붙여야 한다면 변수에 무명 객체를 대입하면 된다.

```kotlin
val listener = object : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent) { ... }
    override fun mouseEntered(e: MouseEvent) { ... }
}
```

## 요약
- Kotlin의 인터페이스는 Java 인터페이스와 비슷하지만 디폴트 구현을 포함할 수 있고, 프로퍼티도 포함할 수 있다.
- 모든 Kotlin 선언은 기본적으로 `final`이며 `public`이다.
- 선언이 `final`이 되지 않게 만들려면 앞에 `open`을 붙여야 한다.
- `internal` 선언은 같은 모듈 안에서만 볼 수 있다.
- 중첩 클래스는 기본적으로 내부 클래스가 아니다. 바깥쪽 클래스에 대한 참조를 중첩 클래스 안에 포함시키려면 `inner` 키워드를 중첩 클래스 선언 앞에 붙여서 내부 클래스로 만들어야 한다.
- `sealed` 클래스를 상속하는 클래스를 정의하려면 반드시 부모 클래스 정의 안에 중첩 클래스로 정의해야 한다.
- 초기화 블록과 부 생성자를 활용해 클래스 인스턴스를 더 유연하게 초기화할 수 있다.
- `field` 식별자를 통해 프로퍼티 접근자 안에서 프로퍼티의 데이터를 저장하는 데 쓰이는 뒷받침하는 필드를 참조할 수 있다.
- 데이터 클래스를 사용하면 컴파일러가 `equals`, `hashCode`, `toString`, `copy` 등의 메서드를 자동으로 생성해준다.
- 클래스 위임을 사용하면 위임 패턴을 구현할 때 필요한 수많은 성가신 준비 코드를 줄일 수 있다.
- 객체 선언을 사용하면 Kotlin답게 싱글턴 클래스를 정의할 수 있다.
- 동반 객체는 Java의 정적 메서드와 필드 정의를 대신한다.
- 동반 객체도 다른 싱글턴 객체와 마찬가지로 인터페이스를 구현할 수 있다. 외부에서 동반 객체에 대한 확장 함수와 프로퍼티를 정의할 수 있다.
- Kotlin의 객체 식은 Java의 무명 내부 클래스를 대신한다. 하지만 Kotlin 객체 식은 여러 인스턴스를 구현하거나 객체가 포함된 영역에 있는 변수의 값을 변경할 수 있는 등 Java 무명 내부 클래스보다 더 많은 기능을 제공한다.