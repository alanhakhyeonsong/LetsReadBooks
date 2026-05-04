# 아이템 4. inferred 타입으로 리턴하지 말라
코틀린의 타입 추론(type inference)은 매우 강력하고 편리한 기능이지만, **공개 API(외부에 노출되는 함수/프로퍼티의 리턴 타입)에서는 사용을 지양**해야 한다. 타입 추론은 우변(할당되는 값)의 **정확한 타입**을 따라가기 때문에, 절대로 슈퍼클래스나 인터페이스로 자동 변환되지 않는다.

```kotlin
open class Animal
class Zebra : Animal()

fun main() {
    var animal = Zebra()
    animal = Animal() // ❌ 오류: Type mismatch (required: Zebra, found: Animal)
}
```

위 코드처럼 일반적인 상황에서는 타입을 명시적으로 지정하면 해결된다.

```kotlin
fun main() {
    var animal: Animal = Zebra()
    animal = Animal() // ✅ OK
}
```

---

## 그러나 외부 라이브러리(외부 모듈)의 경우는 다르다
우리가 직접 수정할 수 없는 외부 코드의 리턴 타입을 추론에 의존하면, **라이브러리 내부 구현이 변경될 때 사용하는 쪽 코드가 깨질 수 있다.**

### 위험 사례 - 인터페이스의 default 메서드 리턴 타입을 추론에 맡길 때
```kotlin
interface CarFactory {
    fun produce(): Car
}

// 기본 구현
val DEFAULT_CAR: Car = Fiat126P()

class DefaultCarFactory : CarFactory {
    override fun produce() = DEFAULT_CAR // 리턴 타입은 Car
}
```

여기까진 문제가 없다. 그런데 어느 날 누군가 `DEFAULT_CAR`의 타입을 명시하지 않도록 "정리"하면?

```kotlin
// "리팩토링" - 타입 명시 제거
val DEFAULT_CAR = Fiat126P() // 이제 타입은 Fiat126P
```

이 변경은 컴파일 에러를 유발한다.

```kotlin
class DefaultCarFactory : CarFactory {
    override fun produce() = DEFAULT_CAR // produce()의 리턴 타입이 Fiat126P로 추론됨
}
// → CarFactory 인터페이스 계약(produce(): Car)과 충돌하지 않을 것 같지만,
//   하위 클래스에서 다른 Car 구현체로 변경하려는 시점에 문제가 발생함
```

> 더 큰 문제는 외부에서 `Fiat126P`만 리턴받을 거라 가정하고 작성된 코드들이 **암묵적인 결합**을 갖게 된다는 점이다. 실제 인터페이스 계약은 `Car`인데, 사용자는 `Fiat126P`라고 믿고 사용해버린다.

---

## 핵심 정리
- 타입 추론은 **우변의 정확한 타입**을 사용한다. 슈퍼타입으로 올라가지 않는다.
- 함수의 **리턴 타입은 API의 핵심 명세**이므로 명시적으로 지정하는 것이 안전하다.
- 외부에 공개되는(public, protected) 함수/프로퍼티는 반드시 리턴 타입을 명시한다.
- 내부에서만 사용하는 private/internal 요소는 추론을 활용해도 무방하다.

```kotlin
// ❌ 외부에 노출되는 함수에서 추론 사용
fun loadUser() = userRepository.findCurrent()

// ✅ 명시적 리턴 타입 - API 계약을 코드로 고정
fun loadUser(): User = userRepository.findCurrent()
```

## 정리
- 타입을 확실하게 지정해야 하는 경우엔 명시적으로 타입을 지정한다.
- 안전을 위해서 외부 API를 만들 땐 반드시 타입을 지정한다.
- 그리고 이렇게 지정한 타입은 특별한 이유와 확실한 확인 없이는 제거하지 않는다.
- inferred 타입은 프로젝트가 진전될 때, 제한이 너무 많아지거나 예측하지 못한 결과를 낼 수 있다.
