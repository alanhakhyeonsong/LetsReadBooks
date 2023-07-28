# Chapter 20 - OOP와 FP의 조화 : 자바와 스칼라 비교
스칼라는 객체지향과 함수형 프로그래밍을 혼합한 언어다. 스칼라 또한 JVM에서 수행되는 언어이며 Java에 비해 더 다양하고 심화된 함수형 기능을 제공한다. 스칼라와 Java에 적용된 함수형의 기능을 살펴보면서 Java의 한계가 무엇인지 살펴보자.

## 함수
스칼라의 함수는 어떤 작업을 수행하는 일련의 명령어 그룹이다. 명령어 그룹을 쉽게 추상화할 수 있는 것도 함수 덕분이며 동시에 함수는 함수형 프로그래밍의 중요한 기초석이다.

Java에선 클래스와 관련된 함수에 메서드라는 이름이 사용된다. 익명 함수의 일종인 람다 표현식도 살펴봤다. 스칼라에서 제공하는 다음과 같은 기능을 차례대로 살펴보자.

- 함수 형식: 함수 형식은 Java 함수 디스크립터의 개념을 표현하는 편의 문법(함수형 인터페이스에 선언된 추상 메서드의 시그니처를 표현하는 개념)이다.
- 익명 함수: 익명 함수는 Java의 람다 표현식과 달리 비지역 변수 기록에 제한을 받지 않는다.
- 커링 지원: 커링은 여러 인수를 받는 함수를 일부 인수를 받는 여러 함수로 분리하는 기법이다.

### 스칼라의 일급 함수
스칼라의 함수는 일급값이다. `Integer`나 `String`처럼 함수를 인수로 전달하거나, 결과로 반환하거나, 변수에 저장할 수 있다. Java의 메서드 참조와 람다 표현식도 일급 함수다.

```scala
def isJavaMentioned(tweet: String) : Boolean = tweet.contains("Java")
def isShortTweet(tweet: String) : Boolean = tweet.length() < 20

val tweets = List(
  "I love the new features in Java 8",
  "How's it going?",
  "An SQL query walks into a bar, sees two tables and says 'Can I join you?'"
)
tweets.filter(isJavaMentioned).foreach(println)
tweets.filter(isShortTweet).foreach(println)
```

### 익명 함수
스칼라는 익명 함수의 개념을 지원한다. 스칼라는 람다 표현식과 비슷한 문법을 통해 익명 함수를 만들 수 있다.

```scala
val isLongTweet : String => Boolean =
  (tweet : String) => tweet.length() > 60

// 위와 같음
val isLongTweet : String => Boolean = 
  new Function1[String, Boolean] {
    def apply(tweet : String): Boolean = tweet.length() > 60
}
```

### 클로저
클로저(closure)란 함수의 비지역 변수를 자유롭게 참조할 수 있는 함수의 인스턴스를 가리킨다. Java의 람다 표현식에는 람다가 정의된 메서드의 지역 변수를 고칠 수 없다는 제약이 있다. 이들 변수는 암시적으로 `final`로 취급된다. 즉, 람다는 변수가 아닌 값을 닫는다는 사실을 기억하자.

스칼라의 익명 함수는 값이 아닌 변수를 캡처할 수 있다.

```scala
def main(args: Array[String]) {
	var count = 0
	val inc = () => count+=1
	inc() // count를 캡처하고 증가시키는 클로저
	println(count)
	inc()
	println(count)
}
```

### 커링
커링은 x와 y라는 두 인수를 받는 함수 f를 한 개의 인수를 받는 g라는 함수로 대체하는 기법이다. 스칼라는 커링을 자동으로 처리하는 특수 문법을 제공한다. 따라서 커리된 함수를 직접 만들어 제공할 필요가 없다.

```scala
def multiplyCurry(x: Int)(y: Int) = x * y
val r1 = multiplyCurry(2)(10) // result = 20

val multiplyByTwo : Int => Int = multiplyCurry(2) // 부분 적용된 함수라 부른다.
val r2 = multiplyByTwo(10) // result = 20
```

## 클래스와 트레이트
스칼라의 클래스와 인터페이스는 Java에 비해 더 유연함을 제공한다.

### 클래스
필드 리스트만 정의함으로 getter, setter, constructor가 암시적으로 생성되어 코드가 훨씬 단순해진다.

```scala
class Student(var name: String, var id: Int)
```

### 트레이트
스칼라의 트레이트는 자바의 인터페이스를 대체한다. 트레이트로 추상 메서드와 기본 구현을 가진 메서드 두 가지를 모두 정의할 수 있다. 트레이트는 인터페이스와 달리 구현 가능하며 하나의 부모 클래스를 갖는 상속과 달리 몇 개라도 조합해 사용 가능하다. 또한 인스턴스화 과정에서도 조합할 수 있다.

```scala
trait Sized {
	var size : Int = 0
	def isEmpty() = size == 0 // 기본 구현을 제공하는 isEmpty 메서드
}

class Empty extends Sized
println(new Empty().isEmpty()) // true

class Box
val b1 = new Box() with Sized // 객체를 인스턴스화 할 때 트레이트를 조합함
println(b1.isEmpty()) // true
val b2 = new Box()
b2.isEmpty() // 컴파일 에러: Box 클래스 선언이 Sized를 상속하지 않음
```


## 📌 정리
- Java와 스칼라는 객체지향과 함수형 프로그래밍 모두를 하나의 프로그래밍 언어로 수용한다. 두 언어 모두 JVM에서 실행되며 넓은 의미에서 상호운용성을 갖는다.
- 스칼라는 Java처럼 리스트, 집합, 맵, 스트림, 옵션 등의 추상 컬렉션을 제공한다. 또한 튜플도 추가로 제공한다.
- 스칼라는 Java에 비해 풍부한 함수 관련 기능을 제공한다. 스칼라는 함수 형식, 지역 변수에 접근할 수 있는 클로저, 내장 커링 형식 등을 지원한다.
- 스칼라의 클래스는 암묵적으로 생성자, 게터, 세터를 제공한다.
- 스칼라는 트레이트를 지원한다. 트레이트는 필드와 디폴트 메서드를 포함할 수 있는 인터페이스다.