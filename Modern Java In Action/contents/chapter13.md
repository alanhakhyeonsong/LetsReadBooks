# Chapter 13 - 디폴트 메서드
전통적인 Java에서 인터페이스와 관련 메서드는 한 몸처럼 구성된다. 인터페이스를 구현하는 클래스는 인터페이스에서 정의하는 모든 메서드 구현을 제공하거나 아니면 슈퍼클래스의 구현을 상속받아야 한다. 라이브러리 설계자 입장에서 인터페이스에 새로운 메서드를 추가하는 등 인터페이스를 바꾸고 싶을 때는 문제가 발생한다. 이전에 해당 인터페이스를 구현했던 모든 클래스의 구현도 고쳐야 하기 때문이다.

Java 8에선 이 문제를 해결하는 새로운 기능을 제공한다. 기본 구현을 포함하는 인터페이스를 정의하는 두 가지 방법을 제공하는데 다음과 같다.
- 인터페이스 내부에 static method를 사용하는 것
- 인터페이스의 기본 구현을 제공할 수 있도록 default method 기능을 사용하는 것

결과적으로 기존 인터페이스를 구현하는 클래스는 자동으로 인터페이스에 추가된 새로운 메서드의 디폴트 메서드를 상속받게 된다. 이렇게 하면 기존의 코드 구현을 바꾸도록 강요하지 않으면서도 인터페이스를 바꿀 수 있다.

```java
// List 인터페이스의 sort 메서드
default void sort(Comparator<? super E> c) {
    Collections.sort(this, c);
}

// Collection의 stream 메서드 정의 코드
default Strean<E> stream() {
    return StreamSupport.stream(spliterator(), false);
}
```

디폴트 메서드를 이용하면 Java API의 호환성을 유지하면서 라이브러리를 바꿀 수 있다.

> 📌 호환성
> - 바이너리 호환성: 뭔가를 바꾼 이후에도 에러 없이 기존 바이너리가 실행될 수 있는 상황
> - 소스 호환성: 코드를 고쳐도 기존 프로그램을 성공적으로 재컴파일할 수 있는 상황
> - 동작 호환성: 코드를 바꾼 다음에도 같은 입력값이 주어지면 프로그램이 같은 동작을 실행하는 상황

## 디폴트 메서드란 무엇인가?
Java 8에선 호환성을 유지하면서 API를 바꿀 수 있도록 새로운 기능인 **디폴트 메서드**를 제공한다. 이제 인터페이스는 자신을 구현하는 클래스에서 메서드를 구현하지 않을 수 있는 새로운 메서드 시그니처를 제공한다. 인터페이스를 구현하는 클래스에서 구현하지 않은 메서드는 인터페이스 자체에서 기본으로 제공한다.

디폴트 메서드는 `default` 키워드로 시작하며 다른 클래스에 선언된 메서드처럼 메서드 바디를 포함한다. 해당 인터페이스를 구현하는 모든 클래스는 디폴트 메서드의 구현도 상속받는다. 다시 말해, 인터페이스에 디폴트 메서드를 추가하면 소스 호환성이 유지된다.

추상 클래스와 인터페이스는 뭐가 다를까? 둘 다 추상 메서드와 바디를 포함하는 메서드를 정의할 수 있다.
- 클래스는 하나의 추상 클래스만 상속받을 수 있지만 인터페이스를 여러 개 구현할 수 있다.
- 추상 클래스는 인스턴스 변수(필드)도 공통 상태를 가질 수 있다. 하지만 인터페이스는 인스턴스 변수를 가질 수 없다.

## 디폴트 메서드 활용 패턴
디폴트 메서드를 이용하는 두 가지 방식은 선택형 메서드(optional method)와 동작 다중 상속(multiple inheritance of behavior)다.

### 선택형 메서드
Java 8 이전에는 인터페이스를 구현하는 클래스는 사용하지 않는 메서드에 대해 비어있는 메서드까지 필수적으로 구현해야 했다. 하지만, 디폴트 메서드를 이용하면 메서드의 기본 구현을 제공할 수 있으므로 인터페이스를 구현하는 클래스에서 빈 구현을 제공할 필요가 없다. 그 결과 불필요한 코드를 줄일 수 있다.

한 가지 예시로, Java 8의 `Iterator` 인터페이스는 다음처럼 `remove` 메서드를 정의한다.

```java
interface Interator {
    boolean hasNext();
    T next();
    default void remove() {
        throw new UnsupportedOperationException();
    }
}
```

### 동작 다중 상속
디폴트 메서드를 이용하면 기존에는 불가능했던 동작 다중 상속 기능도 구현할 수 있다. Java에선 클래스는 한 개의 다른 클래스만 상속할 수 있지만 인터페이스는 여러 개 구현할 수 있다. 메서드가 중복되지 않는 최소한의 인터페이스를 유지한다면 코드에서 동작을 쉽게 재사용하고 조합할 수 있다.

```java
public class ArrayList<E> extends AbstractList<E>
                implements List<E>, RandomAccess, Cloneable, Serializable {
    // ...
}
```

## 해석 규칙
클래스는 여러 인터페이스를 동시에 구현할 수 있다. 그렇기에 같은 시그니처를 갖는 디폴트 메서드를 상속받는 상황이 생길 수 있다.

```java
public interface A {
    default void hello() {
        System.out.println("Hello from A");
    }
}

public interface B {
    default void hello() {
        System.out.println("Hello from B");
    }
}

public class C implements B, A {
    public static void main(String[] args) {
        new C().hello(); // 무엇이 출력될까?
    }
}
```

이런 문제는 C++의 다이아몬드 문제와도 유사하며, Java에서 이러한 문제에 대한 해결 규칙을 제공한다.

1. 클래스가 항상 이긴다. 클래스나 슈퍼클래스에서 정의한 메서드가 디폴트 메서드보다 우선권을 갖는다.
2. 1번 규칙 이외의 상황에선 서브인터페이스가 이긴다. 상속관계를 갖는 인터페이스에서 같은 시그니처를 갖는 메서드를 정의할 때는 서브인터페이스가 이긴다. 즉, B가 A를 상속받는다면 B가 A를 이긴다.
3. 여전히 디폴트 메서드의 우선순위가 결정되지 않았다면 여러 인터페이스를 상속받는 클래스가 명시적으로 디폴트 메서드를 오버라이드하고 호출해야 한다.

### 디폴트 메서드를 제공하는 서브인터페이스가 이긴다
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2551236c-3f13-4e5f-a28b-3eedf339d6df)

컴파일러는 누구의 `hello` 메서드 정의를 사용할까? 2번 규칙에 따라 B가 A를 상속받았으므로 컴파일러는 B의 `hello` 메서드를 선택한다.

C가 D를 상속받는다면 어떤 일이 발생할까?

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9816b9e1-86f5-4a9e-ac73-3589129d7d9a)

```java
public class D implements A { }
public class C extends D implements B, A {
    public static void main(String[] args) {
        new C().hello(); // 무엇이 출력될까?
    }
}
```

D는 `hello`를 오버라이드하지 않았고 단순히 인터페이스 A를 구현했다. 따라서 D는 인터페이스 A의 디폴트 메서드 구현을 상속받는다. 2번 규칙에 따라 클래스나 슈퍼클래스에 메서드 정의가 없을 때는 디폴트 메서드를 정의하는 서브인터페이스가 선택된다. 따라서 컴파일러는 인터페이스 A의 `hello`나 인터페이스 B의 `hello` 둘 중 하나를 선택해야 한다. 여기서 B가 A를 상속받는 관계이므로 B의 `hello`가 실행된다.

만약 D가 `hello`를 오버라이드 했다면 클래스인 D가 슈퍼클래스의 메서드 정의가 우선권을 갖는다.

### 충돌 그리고 명시적인 문제 해결
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3bdc3fa3-f397-4530-8b30-cff5d300ac3d)

```java
public interface A {
    default void hello() {
        System.out.println("hello from A");
    }
}

public interface B {
    default void hello() {
        System.out.println("hello from B");
    }
}

public class C implements B, A { }
```

이번에는 인터페이스 간 상속관계가 없으므로 2번 규칙을 적용할 수 없다. 그러므로 A와 B의 `hello` 메서드를 구별할 기준이 없다. 따라서 자바 컴파일러는 어떤 메서드를 호출해야 할지 알 수 없으므로 `"Error: class C inherits unrelated defaults for hello() from types B and A."` 같은 에러가 발생한다.

충돌 해결을 위해선 개발자가 직접 클래스 C에서 사용하려는 메서드를 명시적으로 선택해야 한다.

```java
public class C implements B, A {
    void hello() {
        B.super.hello();
    }
}
```

### 다이아몬드 문제
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/73117a66-5bda-43f5-a010-ec3b784cbc9f)

```java
public interface A {
	default void hello() {
        System.out.println("Hello from A");
    }
}

public interface B extends A { }
public interface C extends A { }

public class D implements B, C { 
	public static void main(String[] args) {
		new D().hello(); // 무엇이 출력될까?
	}
}
```

다이어그램의 모양이 다이아몬드를 닮았으므로 이를 다이아몬드 문제라 부른다. D는 B와 C 중 누구의 디폴테 메서드 정의를 상속받을까? 실제로 선택할 수 있는 메서드 선언은 하나뿐이다. A만 디폴트 메서드를 정의하고 있기 때문에 결국 출력 결과는 'Hello from A'가 된다.

만약 B에 같은 디폴트 메서드가 있었다면 어떻게 될까? 가장 하위의 인터페이스인 B의 `hello`가 호출될 것이다. B와 C가 모두 디폴트 메서드를 정의했다면 디폴트 메서드 우선순위로 인해 에러가 발생하므로 둘 중 하나의 메서드를 명시적으로 호출해야 한다.

만약 인터페이스 C에 디폴트 메서드가 아닌 추상 메서드 `hello`를 추가하면 어떤 일이 발생할까?

```java
public interface C extends A {
    void hello();
}
```

C는 A를 상속받으므로 C의 추상 메서드 `hello`가 A의 디폴트 메서드 `hello` 보다 우선권을 갖는다. 따라서 컴파일 에러가 발생하며, 클래스 D가 어떤 `hello`를 사용할지 명시적으로 선택해서 에러를 해결해야 한다.

## 📌 정리
- Java 8의 인터페이스는 구현 코드를 포함하는 디폴트 메서드, 정적 메서드를 정의할 수 있다.
- 디폴트 메서드의 정의는 `default` 키워드로 시작하며 일반 클래스 메서드처럼 바디를 갖는다.
- 공개된 인터페이스에 추상 메서드를 추가하면 소스 호환성이 깨진다.
- 디폴트 메서드 덕분에 라이브러리 설계자가 API를 바꿔도 기존 버전과 호환성을 유지할 수 있다.
- 선택형 메서드와 동작 다중 상속에도 디폴트 메서드를 사용할 수 있다.
- 클래스가 같은 시그니처를 갖는 여러 디폴트 메서드를 상속하면서 생기는 충돌 문제를 해결하는 규칙이 있다.
- 클래스나 슈퍼클래스에 정의된 메서드가 다른 디폴트 메서드 정의보다 우선한다. 이 외의 상황에서는 서브인터페이스에서 제공하는 디폴트 메서드가 선택된다.
- 두 메서드의 시그니처가 같고, 상속 관계로도 충돌 문제를 해결할 수 없을 때는 디폴트 메서드를 사용하는 클래스에서 메서드를 오버라이드해서 어떤 디폴트 메서드를 호출할지 명시적으로 결정해야 한다.