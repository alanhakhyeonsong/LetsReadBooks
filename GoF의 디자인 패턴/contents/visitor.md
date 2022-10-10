# 비지터(Visitor) 패턴
의도: 객체 구조를 이루는 원소에 대해 수행할 연산을 표현한다. 연산을 적용할 원소의 클래스를 변경하지 않고도 새로운 연산을 정의할 수 있게 한다.

기존 코드를 변경하지 않고 새로운 기능을 추가하는 방법.
- 더블 디스패치(Double Dispatch)를 활용할 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/f38463cf-1b8c-4e93-8a65-e617a7b69b0c/image.png)

- `Visitor`: 객체 구조 내에 있는 각 `ConcreteElement` 클래스를 위한 `visit()` 연산을 선언한다. 연산의 이름과 인터페이스 형태는 `visit()` 요청을 방문자에게 보내는 클래스를 식별한다. 이로써 방문된 원소의 구체 클래스를 결정할 수 있다. 그러고 나서 방문자는 그 원소가 제공하는 인터페이스를 통해 원소에 직접 접근할 수 있다.
- `ConcreteVisitor`: `Visitor` 클래스에 선언된 연산을 구현한다. 각 연산은 구조 내에 있는 객체의 대응 클래스에 정의된 일부 알고리즘을 구현한다. `ConcreteVisitor` 클래스는 알고리즘이 운영될 수 있는 상황 정보를 제공하며 자체 상태를 저장한다. 이 상태는 객체 구조를 순회하는 도중 순회 결과를 누적할 때가 많다.
- `Element`: 방문자를 인자로 받아들이는 `accept()` 연산을 정의한다.
- `ConcreteElement`: 인자로 방문자 객체를 받아들이는 `accept()` 연산을 구현한다.
- `ObjectStructure`: 객체 구조 내의 원소들을 나열할 수 있다. 방문자가 이 원소에 접근하게 하는 상위 수준 인터페이스를 제공한다. `ObjectStructure`는 Composite 패턴으로 만든 복합체일 수도 있고, 리스트나 집합 등 컬렉션일 수 있다.

협력 방법
- 방문자 패턴을 사용하는 사용자는 `ConcreteVisitor` 클래스의 객체를 생성하고 객체 구조를 따라 각 원소를 방문하며 순회해야 한다.
- 방문자가 구성 원소들을 방문할 때, 구성 원소는 해당 클래스의 `Visitor` 연산을 호출한다. 이 원소들은 자신을 `Visitor` 연산에 필요한 인자로 제공하여 (필요하면) 방문자 자신의 상태에 접근할 수 있도록 한다.

## 활용성
방문자 패턴은 다음의 경우에 사용한다.
- 다른 인터페이스를 가진 클래스가 객체 구조에 포함되어 있으며, 구체 클래스에 따라 달라진 연산을 이들 클래스의 객체에 대해 수행하고자 할 때
- 각각 특징이 있고, 관련되지 않은 많은 연산이 한 객체 구조에 속해있는 객체들에 대해 수행될 필요가 있으며, 연산으로 클래스들을 더럽히고 싶지 않을 때. `Visitor` 클래스는 관련된 모든 연산을 하나의 클래스 안에다 정의해 놓음으로써 관련된 연산이 함께 있을 수 있게 해준다. 어떤 객체 구조가 많은 응용 프로그램으로 공유될 때, `Visitor` 클래스를 사용하면 이 객체 구조가 필요한 응용 프로그램에만 연산을 둘 수 있다.
- 객체 구조를 정의한 클래스는 거의 변하지 않지만, 전체 구조에 걸쳐 새로운 연산을 추가하고 싶을 때. 객체 구조를 변경하려면 모든 방문자에 대한 인터페이스를 재정의 해야 하는데, 이 작업에 잠재된 비용이 클 수 있다. 객체 구조가 자주 변경될 때는 해당 연산을 클래스에 정의하는 편이 더 낫다.

## 구현
### 비지터 패턴 적용 전
```java
public class Client {

    public static void main(String[] args) {
        Shape rectangle = new Rectangle();
        Device device = new Phone();
        rectangle.printTo(device);
    }
}
```

```java
public interface Device {
}

public class Phone implements Device {
}

public class Watch implements Device {
}
```

```java
public interface Shape {

    void printTo(Device device);

}
```

```java
public class Rectangle implements Shape {

    @Override
    public void printTo(Device device) {
        if (device instanceof Phone) {
            System.out.println("print Rectangle to phone");
        } else if (device instanceof Watch) {
            System.out.println("print Rectangle to watch");
        }
    }
}
```

```java
public class Triangle implements Shape {

    @Override
    public void printTo(Device device) {
        if (device instanceof Phone) {
            System.out.println("print Triangle to Phone");
        } else if (device instanceof Watch) {
            System.out.println("print Triangle to Watch");
        }
    }
}
```

```java
public class Circle implements Shape {
  
    @Override
    public void printTo(Device device) {
        if (device instanceof Phone) {
            System.out.println("print Circle to phone");
        } else if (device instanceof Watch) {
            System.out.println("print Circle to watch");
        }
    }
}
```

### 비지터 패턴 적용 후
```java
public class Client {

    public static void main(String[] args) {
        Shape rectangle = new Rectangle();
        Device device = new Pad();
        rectangle.accept(device);
    }
}
```

각 Shape 마다의 메소드를 정의해 둔다. [참고로 메소드 오버로딩은 컴파일 타임에 체크한다.](https://sungman.tistory.com/9)
```java
public interface Device {
    void print(Circle circle);

    void print(Rectangle rectangle);

    void print(Triangle triangle);
}
```

```java
public class Pad implements Device {

    @Override
    public void print(Circle circle) {
        System.out.println("Print Circle to Pad");
    }

    @Override
    public void print(Rectangle rectangle) {
        System.out.println("Print Rectangle to Pad");
    }

    @Override
    public void print(Triangle triangle) {
        System.out.println("Print Triangle to Pad");
    }
}
```

```java
public class Phone implements Device {

    @Override
    public void print(Circle circle) {
        System.out.println("Print Circle to Phone");
    }

    @Override
    public void print(Rectangle rectangle) {
        System.out.println("Print Rectangle to Phone");

    }

    @Override
    public void print(Triangle triangle) {
        System.out.println("Print Triangle to Phone");
    }
}
```

```java
public class Watch implements Device {

    @Override
    public void print(Circle circle) {
        System.out.println("Print Circle to Watch");
    }

    @Override
    public void print(Rectangle rectangle) {
        System.out.println("Print Rectangle to Watch");
    }

    @Override
    public void print(Triangle triangle) {
        System.out.println("Print Triangle to Watch");
    }
}
```

```java
public interface Shape {

    void accept(Device device);

}
```

```java
public class Triangle implements Shape {
    
    @Override
    public void accept(Device device) {
        device.print(this);
    }
}
```

```java
public class Rectangle implements Shape {

    @Override
    public void accept(Device device) {
        device.print(this);
    }
}
```

```java
public class Circle implements Shape {

    @Override
    public void accept(Device device) {
        device.print(this);
    }
}
```
더블 디스패치를 사용해서 새로운 기능을 추가할 때 기존 코드를 변경하지 않고도 새로운 코드를 추가할 수 있게 되었다.

## 비지터 패턴의 장점과 단점
- 장점
  - 기존 코드를 변경하지 않고 새로운 코드를 추가할 수 있다.
  - 추가 기능을 한 곳에 모아둘 수 있다.
- 단점
  - 복잡하다.
  - 새로운 Element를 추가하거나 제거할 때 모든 Visitor 코드를 변경해야 한다.

## Java와 Spring에서의 활용 예시
### Java
- `FileVisitor`, `SimpleFileVisitor`
- `AnnotationValueVisitor`
- `ElementVisitor`

### Spring
- `BeanDefinitionVisitor`