# 아이템 3. 최대한 플랫폼 타입을 사용하지 말라
코틀린의 등장과 함께 소개된 null-safety는 코틀린의 주요 기능 중 하나다. 이런 메커니즘이 없는 Java, C 등의 프로그래밍 언어와 코틀린을 연결해서 사용할 때는 NPE가 발생할 수 있다.

- `@Nullable` 어노테이션이 붙어있다면, 이를 nullable로 추정하고 `String?`으로 변경하면 된다.
- `@NotNull` 어노테이션이 붙어있다면, `String`으로 변경하면 된다.

```java
public class JavaTest {
  public String giveName() {
    // ...
  }
}
```

위 Java 코드를 가정하면 모든 것이 nullable일 수 있으므로 최대한 안전하게 접근한다면, 이를 nullable로 가정하고 다루어야 한다. 하지만 어떤 메서드는 `null`을 리턴하지 않을 것이 확실할 수 있다. 이런 경우엔 마지막에 not-null 단정을 나타내는 `!!`을 붙인다.

nullable과 관련하여 자주 문제가 되는 부분은 Java의 제네릭 타입이다. `List<User>`를 리턴하고, 어노테이션이 따로 붙어 있지 않은 경우 리스트와 리스트 내부의 객체들이 널이 아니라는 것을 알아야 한다.

```kotlin
// java
public class UserRepo {
  public List<User> getUsers() {
    // ...
  }
}

// kotlin
val users: List<User> = UserRepo().users!!.filterNotNull()
```

만약 함수가 `List<List<User>>`를 리턴한다면?

```kotlin
val users: List<List<User>> = UserRepo().groupedUsers!!
        .map { it!!.filterNotNull() }
```

리스트는 적어도 `map`과 `filterNotNull` 등의 메서드를 제공한다. 다른 제네릭 타입이라면, 널을 확인하는 것 자체가 정말로 복잡한 일이 된다. 그래서 코틀린은 자바 등 다른 프로그래밍 언어에서 넘어온 타입들을 특수하게 다룬다. 이를 **플랫폼 타입**이라 부른다.

- `String!` 처럼 타입 이름 뒤에 `!` 를 붙여 표기한다.
- 이런 노테이션이 직접적으로 코드에 나타나진 않는다. 대신 선택적으로 사용한다.

```kotlin
// java
public class UserRepo {
  public List<User> getUsers() {
    // ...
  }
}

// kotlin
val repo = UserRepo()
val user1 = repo.user // user1의 타입은 User!
val user2: User = repo.user // user2의 타입은 User
val user3: User? = repo.user // user3의 타입은 User?

// TO-BE
val users: List<User> = UserRepo().users
val users: List<List<User>> = UserRepo().groupedUsers
```

- 문제는 null이 아니라고 생각되는 것이 null일 가능성이 있으므로, 여전히 위험하다는 것이다.
- 플랫폼 타입을 사용할 때는 항상 주의를 기울여야 한다.
- 설계자가 명시적으로 어노테이션으로 표시하거나, 주석으로 달아두지 않으면, 언제든지 동작이 변경될 가능성이 있다.

### 플랫폼 타입을 빠르게 제거해야 하는 이유 - 예시
플랫폼 타입(`User!`)을 그대로 흘려보내면 NPE가 호출 지점에서 멀리 떨어진 곳에서 터지므로, 원인을 추적하기 어렵다. 따라서 **자바와 만나는 경계에서 즉시 `User` 또는 `User?`로 좁혀야** 한다.

```java
// Java - null을 반환할 수도 있는 메서드 (어노테이션 없음)
public class UserRepo {
    public User getUser() {
        return null; // 어떤 조건에서는 null 반환
    }
}
```

```kotlin
// 위험: 플랫폼 타입(User!)을 그대로 사용
fun statedType() {
    val user = UserRepo().user        // user의 타입은 User! (플랫폼 타입)
    println(user.name)                // ✅ 컴파일 OK, 그러나 런타임에 NPE 발생 가능
    // → NPE가 user.name 사용 시점에 터지므로, 원인이 'UserRepo'에 있다는 것을 추적하기 어려움
}

// 안전: 경계에서 명시적으로 타입을 지정 (플랫폼 타입 제거)
fun platformType() {
    val user: User = UserRepo().user  // ❗ null이면 이 줄에서 즉시 NPE 발생
    println(user.name)                // 이후 코드는 항상 안전하게 사용 가능
    // → NPE가 '값을 받는 시점'에서 터지므로, 원인 파악이 쉬움
}

// 또는 nullable을 명시
fun nullableType() {
    val user: User? = UserRepo().user
    println(user?.name)               // null-safe 호출
}
```

| 구분 | 변수 타입 | NPE 발생 시점 | 디버깅 난이도 |
|------|----------|-------------|------------|
| 플랫폼 타입 (위험) | `User!` | 사용 시점 (어디서든) | 어렵다 |
| 명시적 non-null | `User` | 대입 시점 (즉시) | 쉽다 |
| 명시적 nullable | `User?` | 발생 안 함 (컴파일러가 강제) | - |

**결론**: 자바 API에서 값을 받는 즉시 `User` 또는 `User?`로 타입을 명시해 플랫폼 타입을 코드 내부로 전파시키지 말자.

자바를 코틀린과 함께 사용할 때, 자바 코드를 직접 조작할 수 있다면, 가능한 `@Nullable`과 `@NotNull` 어노테이션을 붙여서 사용하자.

- JetBrains(`org.jetbrains.annotations`) : `@Nullable`, `@NotNull`
- Android
- JSR-305(`javax.annotation`) : `@Nullable`, `@CheckForNull`, `@Nonnull`
- JavaX(`javax.annotation`) : `@Nullable`, `@CheckForNull`, `@Nonnull`
- FindBugs
- ReactiveX(`io.reactivex.annotations`) : `@Nullable`, `@NonNull`
- Eclipse
- Lombok : `@NonNull`



## 정리
- 다른 프로그래밍 언어에서 와서 nullable 여부를 알 수 없는 타입을 플랫폼 타입이라 부른다.
- 플랫폼 타입을 사용하는 코드는 해당 부분만 위험할 뿐만 아니라, 이를 활용하는 곳까지 영향을 줄 수 있는 위험한 코드다.
- 연결되어 있는 자바 생성자, 메서드, 필드에 nullable 여부를 지정하는 어노테이션을 활용하는 것도 좋다.