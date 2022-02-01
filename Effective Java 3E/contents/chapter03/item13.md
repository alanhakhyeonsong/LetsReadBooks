# 아이템 13. clone 재정의는 주의해서 진행하라

`Cloneable`은 복재해도 되는 클래스임을 명시하는 용도의 믹스인 인터페이스(mixin interface)이지만, 의도한 목적을 제대로 이루지 못했다.

객체를 복사하고 싶다면 Cloneable 인터페이스를 구현해 clone 메서드를 재정의하는 방법이 일반적이지만, clone 메서드가 정의된 곳은 Cloneable이 아닌 Object이고 접근 제어자도 protected이다. 이 때문에 Cloneable 인터페이스를 구현하는 것 만으로는 외부 객체에서 clone 메서드를 호출할 수 없다.

하지만 이를 포함한 여러 문제점에도 불구하고 Cloneable 방식은 널리 쓰이고 있다.

## Cloneable의 역할?

Object 클래스의 protected 메서드인 clone의 동작 방식을 결정한다.

Cloneable을 구현하지 않은 클래스에서 clone을 호출하면 `CloneNotSupportedException`이 발생한다.

## clone 메서드의 규약

- `x.clone() != x`
- `x.clone().getClass() == x.getClass()`
- `(optional) x.clone.equals(x)`

강제성만 없다는 점만 빼면 생성자 연쇄와 살짝 비슷한 매커니즘이다. clone 메서드가 super.clone이 아닌, 생성자를 호출해 얻은 인스턴스를 반환해도 문제가 없을 것이다.

하지만, 이 클래스의 하위 클래스에서 super.clone을 호출한다면 잘못된 클래스의 객체가 만들어져 하위 클래스의 clone 메서드가 제대로 동작하지 않게 된다.

## clone 메서드 재정의

쓸데없는 복사를 지양한다는 관점에서 보면 불변 클래스는 굳이 clone 메서드를 제공하지 않는게 좋다.

```java
@Override
public PhoneNumber clone() {
    try {
        return (PhoneNumber) super.clone();
    } catch (CloneNotSupportedException e) {
        throw new AssertionError(); // 일어날 수 없는 일이다.
    }
}
```

이 메서드가 동작하려면 PhoneNumber의 클래스 선언에 Cloneable을 구현한다고 추가해야 한다. Object의 clone 메서드는 Object를 반환하지만 PhoneNumber의 clone 메서드는 PhoneNumber를 반환하게 했다. 자바가 공변 반환 타이핑(covariant return typing)을 지원하니 이렇게 하는 것이 가능하고 권장하는 방식이기도 하다.

super.clone 호출을 try-catch 블록으로 감싼 이유는 Object의 clone 메서드가 검사 예외(checked exception)인 `CloneNotSupportedException`을 던지도록 선언되었기 때문이다.

## 가변 객체를 참조하는 클래스의 clone 재정의

```java
public class stack implements Cloneable {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        this.elements = new Objects[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        Object result = elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }

    // 원소를 위한 공간을 적어도 하나 이상 확보한다.
    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }

    @Override
    public Stack clone() {
        try {
            return (Stack) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (elements[i] != null) {
            sb.append(elements[i].num);
            sb.append(",");
            i++;
        }
        return sb.toString();
    }
}
```

위 clone() 메서드처럼 단순히 super.clone의 결과를 그대로 반환한다면 반환된 Stack 인스턴스의 size 필드는 올바른 값을 갖겠지만, elements 필드는 원본 Stack 인스턴스와 똑같은 배열을 참조할 것이다.  
**이는 원본이나 복제본 중 하나를 수정하면 다른 하나도 수정되어 불변식을 해친다는 이야기다.**  
따라서 오동작하거나 `NullPointerException`을 던질 것이다.

Stack 클래스의 하나뿐인 생성자를 호출한다면 이러한 상황은 절대 일어나지 않는다. **clone 메서드는 사실상 생성자와 같은 효과를 낸다. 즉, clone은 원본 객체에 아무런 해를 끼치지 않는 동시에 복제된 객체의 불변식을 보장해야 한다.**

이를 해결하려면, 스택 내부 정보를 복사해야 하는데, 가장 쉬운 방법은 elements 배열의 clone을 재귀적으로 호출해주는 방법을 적용하면 된다.

```java
@Override
public Stack clone() {
    try {
        Stack result = (Stack) super.clone();
        result.elements = elements.clone();
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```

하지만, 이 방법도 단점이 존재한다. 기존 객체와 복사된 객체의 객체 배열이 서로 다른 주소를 가지고 있어도 배열의 원소가 가지고 있는 Object라는 객체는 같은 객체이다. 이 경우 반복자를 이용해 기존 elements가 가지고 있는 원소들을 복사된 배열에 맞게 새로 생성하면 쉽게 해결할 수 있을 것이다.

// Effective Java에선 HashTable을 예로 들어 설명함. 자세한 내용은 책을 참고하도록 하자.

## clone 재정의시 주의사항

- clone은 원본 객체에 아무런 해를 끼치지 않는 동시에 객체의 불변식을 보장해야 한다.
- 복제할 수 있는 클래스를 만들기 위해 일부 필드에서 final 한정자를 제거해야 할 수도 있다.
- 재정의될 수 있는 메서드를 호출하지 않아야 한다.

## 핵심 정리

- 새로운 인터페이스를 만들 때는 절대 Cloneable을 확장해서는 안되며, 새로운 클래스도 이를 구현해서는 안된다.
- final 클래스라면 Cloneable을 구현해도 위험이 크지 않지만, 성능 최적화 관점에서 검토한 후 별다른 문제가 없을 때만 드물게 허용해야 한다.
- 기본 원칙은 '복제 기능은 생성자와 팩터리를 이용하는게 최고'라는 것이다.
- 단, 배열만은 clone 메서드 방식이 가장 깔끔한, 이 규칙의 합당한 예외라 할 수 있다.
