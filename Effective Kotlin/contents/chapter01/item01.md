# 아이템 1. 가변성을 제한하라
코틀린은 모듈로 프로그램을 설계한다. 모듈은 클래스, 객체, 함수, 타입 별칭, 톱레벨 프로퍼티 등 다양한 요소로 구성된다. 이러한 요소 중 일부는 상태를 가질 수 있다.

```kotlin
var a = 10
var list: MutableList<Int> = mutableListOf()
```

요소가 상태를 갖는 경우, 해당 요소의 동작은 사용 방법뿐만 아니라 그 이력에도 의존하게 된다.

```kotlin
import kotlin.jvm.Throws

class BankAccount {
    var balance = 0.0
        private set

    fun deposit(depositAmount: Double) {
        balance += depositAmount
    }

    @Throws(InsufficientFunds::class)
    fun withdraw(withdrawAmount: Double) {
        if (balance < withdrawAmount) {
            throw InsufficientFunds()
        }
        balance -= withdrawAmount
    }
}

class InsufficientFunds : Exception()

fun main() {
    val account = BankAccount()
    println(account.balance)
    account.deposit(100.0)
    println(account.balance)
    account.withdraw(50.0)
    println(account.balance)
}
```

위 코드처럼 상태를 갖게 하는 것은 양날의 검이다. 시간의 변화에 따라 변하는 요소를 표현할 수 있다는 것은 유용하지만, 상태를 적절하게 관리하는 것이 생각보다 꽤 어렵다.

- 프로그램을 이해하고 디버그하기 힘들어진다.
- 가변성이 있으면, 코드의 실행을 추론하기 어려워진다. 시점에 따라 값이 달라질 수 있으므로, 현재 어떤 값을 갖고 있는지 알아야 코드의 실행을 예측할 수 있다. 또한 한 시점에 확인한 값이 계속 동일하게 유지된다 확신할 수 없다.
- 멀티스레드 프로그램일 때는 적절한 동기화가 필요하다. 변경이 일어나는 모든 부분에서 충돌이 발생할 수 있다.
- 테스트하기 어렵다.
- 상태 변경이 일어날 때, 이러한 변경을 다른 부분에 알려야 하는 경우가 있다.
  - ex) 정렬된 리스트에 가변 요소를 추가한다면, 요소에 변경이 일어날 때마다 리스트 전체를 다시 정렬해야 한다.

다음 코드는 멀티스레드를 활용해 프로퍼티를 수정한다. 이때 충돌에 의해 일부 연산이 이뤄지지 않는다.

```kotlin
import kotlin.concurrent.thread

fun main() {
    var num = 0
    for (i in 1..1000) {
        thread {
            Thread.sleep(10)
            num += 1
        }
    }
    Thread.sleep(5000)
    print(num) // 1000이 아닐 확률이 매우 높다.
    // 실행할 때마다 다른 숫자가 나온다.
}
```

코틀린의 코루틴을 활용하면, 더 적은 스레드가 관여되므로 충돌과 관련된 문제가 줄어든다. 하지만 문제가 사라지는 것은 아니다.

```kotlin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun main() {
    var num = 0
    coroutineScope {
        for (i in 1..1000) {
            launch {
                delay(10)
                num += 1
            }
        }
    }
    print(num) // 실행할 때마다 다른 숫자가 나온다.
}
```

실제 프로젝트에선 일부 연산이 충돌되어 사라지므로 적절하게 추가로 동기화를 구현하는 코드를 작성해야 한다. 동기화를 구현하는 것은 굉장히 어려운 일이다. 또한 변할 수 있는 지점이 많다면 훨씬 더 어려워진다. 따라서 변할 수 있는 지점은 줄일수록 좋다.

가변성은 시스템의 상태를 나타내기 위한 중요한 방법이다. 하지만 변경이 일어나야 하는 부분을 신중하고 확실하게 결정하고 사용해야 한다.

## 코틀린에서 가변성 제한하기
**코틀린은 가변성을 제한할 수 있게 설계되어 있다.** immutable 객체를 만들거나, 프로퍼티를 변경할 수 없게 막는 것이 굉장히 쉽다.

- 읽기 전용 프로퍼티(`val`)
- 가변 컬렉션과 읽기 전용 컬렉션 구분하기
- 데이터 클래스의 `copy`

### 읽기 전용 프로퍼티(val)
`val`로 선언된 프로퍼티는 마치 값처럼 동작하며, 일반적인 방법으론 값이 변하지 않는다.

```kotlin
val a = 10
a = 20 // 오류
```

**읽기 전용 프로퍼티가 완전히 변경 불가능한 것은 아니다.** 읽기 전용 프로퍼티가 `mutable` 객체를 담고 있다면, 내부적으로 변할 수 있다.

```kotlin
val list = mutableListOf(1, 2, 3)
list.add(4)

print(list) // [1, 2, 3, 4]
```

읽기 전용 프로퍼티는 다른 프로퍼티를 활용하는 사용자 정의 getter로도 정의할 수 있다. 이렇게 `var` 프로퍼티를 사용하는 `val` 프로퍼티는 `var` 프로퍼티가 변할 때 변할 수 있다.

```kotlin
var name: String = "Sergio"
var surname: String = "Ramos"
val fullName
    get() = "$name $surname"

fun main() {
    println(fullName)
    surname = "Busquets"
    println(fullName)
}
```

코틀린의 프로퍼티는 기본적으로 캡슐화되어 있고, 추가적으로 사용자 정의 접근자(getter, setter)를 가질 수 있다. 이런 특성으로 코틀린은 API를 변경하거나 정의할 때 굉장히 유연하다.

추가적으로 `var`는 getter, setter를 모두 제공하지만, `val`은 변경이 불가능하므로 `getter`만 제공한다. 그래서 `val`을 `var`로 오버라이드할 수 있다.

```kotlin
interface Element {
  val active: Boolean
}

class ActualElement : Element {
  override var active: Boolean = false
}
```

**읽기 전용 프로퍼티 `val`의 값은 변경될 수 있긴 하지만, 프로퍼티 레퍼런스 자체를 변경할 수는 없으므로 동기화 문제 등을 줄일 수 있다. 그래서 일반적으로 `var`보다 `val`을 많이 사용한다.**

`val`은 읽기 전용 프로퍼티지만, 변경할 수 없음을 의미하는 것은 아니라는 것을 기억하자. 또한 이는 getter 또는 delegate로 정의할 수 있다. 만약 완전히 변경할 필요가 없다면, `final` 프로퍼티를 사용하는 것이 좋다. `val`은 정의 옆에 상태가 바로 적히므로, 코드의 실행을 예측하는 것이 훨씬 간단하다. 또한 스마트 캐스트 등의 추가적인 기능을 활용할 수 있다.

```kotlin
val name: String? = "Sergio"
val surname: String = "Ramos"

val fullName: String?
    get() = name?.let { "$it $surname" }

val fullName2: String? = name?.let { "$it $surname" }

fun main() {
    if (fullName != null) {
        println(fullName.length) // 오류
    }

    if (fullName2 != null) {
        println(fullName2.length) // Sergio Ramos
    }
}
```

`fullName`은 getter로 정의했으므로 스마트 캐스트할 수 없다. getter를 활용하므로, 값을 사용하는 시점의 `name`에 따라 다른 결과가 나올 수 있기 때문이다. `fullName2`처럼 지역 변수가 아닌 프로퍼티가 `final`이고, 사용자 정의 getter를 갖지 않을 경우 스마트 캐스트 할 수 있다.

### 가변 컬렉션과 읽기 전용 컬렉션 구분하기
코틀린은 읽고 쓸 수 있는 컬렉션과 읽기 전용 컬렉션으로 구분된다. 이는 컬렉션 계층이 설계된 방식 덕분이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/8b9a2730-39c9-4527-8d2a-177fc855fbdd)

읽기 전용 컬렉션이 내부의 값을 변경할 수 없다는 의미는 아니다. 대부분의 경우엔 변경할 수 있다. 하지만 읽기 전용 인터페이스가 이를 지원하지 않으므로 변경할 수 없다. 예를 들어 `Iterable<T>.map`과 `Iterable<T>.filter` 함수는 `ArrayList`를 리턴한다. 이는 변경할 수 있는 리스트다.

```kotlin
inline fun <T, R> Iterable<T>.map(
    transformation: (T) -> R
): List<R> {
    val list = ArrayList<R>()
    for (elem in this) {
        list.add(transformation(elem))
    }
    return list
}
```

- 이러한 컬렉션을 진짜로 불변하게 만들지 않고, 읽기 전용으로 설계한 것은 굉장히 중요한 부분이다. 이로 인해 더 많은 자유를 얻을 수 있다. 내부적으로 인터페이스를 사용하고 있으므로, 실제 컬렉션을 리턴할 수 있다. 따라서 플랫폼 고유의 컬렉션을 사용할 수 있다.
- 이는 코틀린이 내부적으로 immutable하지 않은 컬렉션을 외부적으로 immutable하게 보이게 만들어 얻어지는 안정성이다.

```kotlin
val list = listOf(1, 2, 3)

// 이렇게 하면 안된다.
if (list is MutableList) {
    list.add(4)
}

// 만약 변경이 필요하다면 다음과 같이 새로운 mutable list를 만들어서 리턴
val mutableList = list.toMutableList()
mutableList.add(4)
```

- **리스트를 읽기 전용으로 리턴하면, 이를 읽기 전용으로만 사용해야 한다.** 이는 단순한 규약의 문제인데 다운 캐스팅은 이런 계약을 위반하고, 추상화를 무시하는 행위다. 이런 코드는 안전하지 않고, 예측하지 못한 결과를 초래한다.
- 위 코드의 실행 결과는 플랫폼에 따라 다르다. JVM에서 `listOf`는 자바의 `List` 인터페이스를 구현한 `Array.ArrayList` 인스턴스를 리턴한다. 그러나 1년 뒤에 이것이 어떻게 동작할지 보장할 수 없다.
- **읽기 전용 컬렉션을 mutable 컬렉션으로 다운 캐스팅하면 안 된다.** 만약 변경이 필요하다면 copy를 통해 새로운 mutable 컬렉션을 만들어서 활용해야 한다.

### 데이터 클래스의 copy
`String`, `Int` 처럼 내부적인 상태를 변경하지 않는 immutable 객체를 많이 사용하는 데는 이유가 있다.

- 한 번 정의된 상태가 유지되므로 코드 이해가 쉬움
- 공유했을 때도 충돌이 따로 이루어지지 않으므로, 병렬 처리를 안전하게 할 수 있음
- 객체에 대한 참조는 변경되지 않으므로 쉽게 캐시가 가능
- 방어적 복사본을 만들 필요가 없음
- 다른 객체를 만들 때 활용하기 좋고 실행을 더 쉽게 예측가능
- set, map 키로 사용할 수 있는데 mutable 객체는 이러한 것으로 사용할 수 없다.
  - 세트와 맵인 내부적으로 해시 테이블을 사용하고 해시 테이블은 처음 요소를 넣을때 요소의 값을 기반으로 버킷을 결정하기 때문에 요소의 값이 수정이 되면 해시 테이블 내부에서 요소를 찾을 수 없게되기 때문

```kotlin
val names: SortedSet<FullName> = TreeSet()
val person = FullName("AAA", "AAA")
names.add(person)
names.add(FullName("Jordan", "Hansen"))
names.add(FullName("David", "Blanc"))
println(names)
println(person in names) // true

person.name = "ZZZ"
println(names)
println(person in names) // false
```

immutable 객체는 변경할 수 없다는 단점이 있다. 따라서 자신의 일부를 수정한 새로운 객체를 만들어내는 메서드를 가져야 한다.

```kotlin
class User(
    val name: String,
    val surname: String,
) {
    fun withSurname(surname: String) = User(name, surname)
}
var user = User("Sergio", "Ramos")
user = user.withSurname("Busquets")
print(user)
```

다만 모든 프로퍼티를 대상으로 이런 함수 하나하나 만드는 것은 굉장히 귀찮은 일이다. 그럴 때는 `data` 한정자를 사용해 `copy` 메소드를 활용하면, 모든 기본 생성자 프로퍼티가 같은 새로운 객체를 만들어 낼 수 있다.

```kotlin
data class User(
    val name: String,
    val surname: String,
)
var user = User("Sergio", "Ramos")
user = user.copy(surname = "Busquets")
print(user)
```

코틀린에선 이와 같은 형태로 immutable 특성을 가지는 데이터 모델 클래스를 만든다. 변경을 할 수 있다는 측면만 보면 mutable 객체가 더 좋아 보이지만, 이렇게 데이터 모델 클래스를 만들어 immutable 객체로 만드는 것이 더 많은 장점을 가지므로, 기본적으론 이렇게 만드는 것이 좋다.

## 다른 종류의 변경 가능 지점
변경할 수 있는 리스트는 다음 두 가지 방식으로 만들 수 있다.

```kotlin
val list1: MutableList<Int> = mutableListOf()
var list2: List<Int> = listOf()

list1.add(1)
list2 = list2 + 1

list1 += 1 // list1.plusAssign(1)
list2 += 1 // list2 = list2.plus(1)
```

- `list1` : mutable 컬렉션 사용
  - 구체적인 리스트 구현 내부에 변경 가능 지점이 있음
  - 멀티스레드 처리가 이루어질 경우, 내부적으로 적절한 동기화가 되어 있는지 확실하게 알 수 없어 위험하다.
- `list2` : immutable 컬렉션 사용
  - 객체 변경을 제어하기 더 쉬움
  - 사용자 정의 setter를 활용해 변경을 추적할 수 있다.

```kotlin
var list = listOf<Int>()
for(i in 1..1000) {
    thread {
        list = list + i
    }
}
Thread.sleep(1000)
println(list.size) // 1000이 되지 않는다.
// 실행할 때마다 911과 같은 다른 숫자가 나온다.
```

`Delegates.observable`을 사용하면, 리스트에 변경이 있을 때 로그를 출력할 수 있다.

```kotlin
var names by Delegates.observable(listOf<String>()) {_, old, new ->
    println("Names changed from $old to $new")
}

names += "Fabio"
names += "Bill"
```

최악의 방식은 프로퍼티와 컬렉션을 모두 변경 가능한 지점으로 만드는 것이다.

```kotlin
// 이렇게 하면 안됨.
var list3 = mutableListOf<Int>()
```

상태를 변경할 수 있는 불필요한 방법은 만들지 않아야 한다. 상태를 변경하는 모든 방법은 코드를 이해하고 유지해야 하므로 비용이 발생한다. 따라서 가변성을 제한하는 것이 좋다.

## 변경 가능 지점 노출하지 말기
상태를 나타내는 mutable 객체를 외부에 노출하는 것은 굉장히 위험하다.

```kotlin
data class User(val name: String)

class UserRepository {
    private val storedUsers: MutableMap<Int, String> = mutableMapOf()

    fun loadAll(): MutableMap<Int, String> {
      return storedUsers
    }
}

val userRepository = UserRepository()

val storedUsers = userRepository.loadAll()
storedUsers[4] = "Kiraill"
```

이런 코드는 돌발적인 수정이 일어나면 위험할 수 있다. 이를 처리하는 방법은 두 가지다.

- 리턴되는 객체를 복제 (방어적 복제)
  - `data` 한정자로 만들어지는 `copy` 메서드를 활용
- 읽기 전용 슈퍼타입으로 업캐스트하여 가변성을 제한

```kotlin
data class User(val name: String)

class UserRepository {
    private val storedUsers: MutableMap<Int, String> = mutableMapOf()

    fun loadAll(): Map<Int, String> {
      return storedUsers
    }
}
```

## 정리
가변성을 제한한 immutable 객체를 사용하는 것이 좋은 이유가 다양하다. 코틀린은 가변성을 제한하기 위해 다양한 도구들을 제공한다. 이를 활용해 가변 지점을 제한하며 코드를 작성하자.

- `var` 보다는 `val`을 사용하는 것이 좋음
- mutable 프로퍼티보다는 immutable 프로퍼티를 사용하는 것이 좋음
- mutable 객체와 클래스보다는 immutable 객체와 클래스를 사용하는 것이 좋음
- 변경이 필요한 대상을 만들어야 한다면, immutable 데이터 클래스로 만들고 `copy`를 활용하는 것이 좋음
- 컬렉션에 상태를 저장해야 한다면, mutable 컬렉션보다는 읽기 전용 컬렉션을 사용하는 것이 좋음
- 변이 지점을 적절하게 설계하고, 불필요한 변이 지점은 만들지 않는 것이 좋음
- mutable 객체를 외부에 노출하지 않는 것이 좋음