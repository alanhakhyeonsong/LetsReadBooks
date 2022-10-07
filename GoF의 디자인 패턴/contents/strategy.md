# 전략(Strategy) 패턴
의도: 동일 계열의 알고리즘군을 정의하고, 각 알고리즘을 캡슐화하며, 이들을 상호교환이 가능하도록 만든다. 알고리즘을 사용하는 클라이언트와 상관없이 독립적으로 알고리즘을 다양하게 변경할 수 있게 한다.

여러 알고리즘을 캡슐화하고 상호 교환 가능하게 만드는 패턴.
- 컨텍스트에서 사용할 알고리즘을 클라이언트가 선택한다.

![](https://velog.velcdn.com/images/songs4805/post/efd5d0db-077f-4cf2-b26b-72a2eca088a3/image.png)

- `Strategy`: 제공하는 모든 알고리즘에 대한 공통의 연산들을 인터페이스로 정의한다. `Context` 클래스는 `ConcreteStrategy` 클래스에 정의한 인터페이스를 통해 실제 알고리즘을 사용한다.
- `ConcreteStrategy`: `Strategy` 인터페이스를 실제 알고리즘으로 구현한다.
- `Context`: `ConcreteStrategy` 객체를 통해 구성된다. 즉, `Strategy` 객체에 대한 참조자를 관리하고, 실제로는 `Strategy` 서브클래스의 인스턴스를 갖고 있음으로써 구체화 한다. 또한 `Strategy` 객체가 자료에 접근해가는 데 필요한 인터페이스를 정의한다.

협력 방법
- `Strategy` 클래스와 `Context` 클래스는 의사교환을 통해 선택한 알고리즘을 구현한다. 즉, `Context` 클래스는 알고리즘에 해당하는 연산이 호출되면, 알고리즘 처리에 필요한 모든 데이터를 `Strategy` 클래스로 보낸다. 이때, `Context` 객체 자체를 `Strategy` 연산에다가 인자로 전송할 수도 있다.
- `Context` 클래스는 사용자 쪽에서 온 요청을 각 전략 객체로 전달한다. 이를 위해 사용자는 필요한 알고리즘에 해당하는 `ConcreteStrategy` 객체를 생성하여 이를 `Context` 클래스에 전송하는데, 이 과정을 거치면 사용자는 `Context` 객체와 동작할 때 전달한 `ConcreteStrategy` 객체와 함께 동작한다. 사용자가 선택할 수 있는 동일 계열의 `ConcreteStrategy` 클래스군이 준비될 때가 자주 있다.

## 활용성
다음 상황에서 전략 패턴을 사용할 수 있다.
- 행동들이 조금씩 다를 뿐 개념적으로 관련된 많은 클래스들이 존재할 때. 전략 패턴은 많은 행동 중 하나를 가진 클래스를 구성할 수 있는 방법을 제공한다.
- 알고리즘의 변형이 필요할 때. 이를테면, 기억 공간과 처리 속도 간의 절충에 따라 서로 다른 알고리즘을 정의할 수 있을 것이다. 이러한 변형물들이 알고리즘의 상속 관계로 구현될 때 전략 패턴을 사용할 수 있다.
- 사용자가 몰라야 하는 데이터를 사용하는 알고리즘이 있을 때. 노출하지 말아야 할 복잡한 자료구조는 `Strategy` 클래스에만 두면 되므로 사용자는 몰라도 된다.
- 하나의 클래스가 많은 행동을 정의하고, 이런 행동들이 그 클래스의 연산 안에서 복잡한 다중 조건문의 모습을 취할 때. 많은 조건문보다는 각각을 `Strategy` 클래스로 옮겨놓는 것이 좋다.

## 구현
### 전략 패턴 적용 전
```java
public class BlueLightRedLight {

    private int speed;

    public BlueLightRedLight(int speed) {
        this.speed = speed;
    }

    public void blueLight() {
        if (speed == 1) {
            System.out.println("무 궁 화    꽃   이");
        } else if (speed == 2) {
            System.out.println("무궁화꽃이");
        } else {
            System.out.println("무광꼬치");
        }

    }

    public void redLight() {
        if (speed == 1) {
            System.out.println("피 었 습 니  다.");
        } else if (speed == 2) {
            System.out.println("피었습니다.");
        } else {
            System.out.println("피어씀다");
        }
    }
}
```

```java
public class Client {

    public static void main(String[] args) {
        BlueLightRedLight blueLightRedLight = new BlueLightRedLight(3);
        blueLightRedLight.blueLight();
        blueLightRedLight.redLight();
    }
}
```

### 전략 패턴 적용 후
먼저 `Context`는 다음과 같다.
```java
public class BlueLightRedLight {

    public void blueLight(Speed speed) {
        speed.blueLight();
    }

    public void redLight(Speed speed) {
        speed.redLight();
    }
}
```

`Strategy` 인터페이스는 다음과 같다.
```java
public interface Speed {

    void blueLight();

    void redLight();

}
```
이를 구현한 `ConcreteStrategy`들은 다음과 같다.

```java
public class Normal implements Speed {
    @Override
    public void blueLight() {
        System.out.println("무 궁 화    꽃   이");
    }

    @Override
    public void redLight() {
        System.out.println("피 었 습 니  다.");
    }
}
```

```java
public class Fastest implements Speed{
    @Override
    public void blueLight() {
        System.out.println("무광꼬치");
    }

    @Override
    public void redLight() {
        System.out.println("피어씀다.");
    }
}
```

```java
public class Faster implements Speed {
    @Override
    public void blueLight() {
        System.out.println("무궁화꽃이");
    }

    @Override
    public void redLight() {
        System.out.println("피었습니다.");
    }
}
```

```java
public class Client {

    public static void main(String[] args) {
        BlueLightRedLight game = new BlueLightRedLight();
        game.blueLight(new Normal());
        game.redLight(new Fastest());
        game.blueLight(new Speed() {
            @Override
            public void blueLight() {
                System.out.println("blue light");
            }

            @Override
            public void redLight() {
                System.out.println("red light");
            }
        });
    }
}
```
결과적으로 `Context` 자체의 행동과 알고리즘이 혼합되어 있던 구조를 분리하여 SRP 원칙도 지킬 수 있게 되었다. 게다가 위 `Client` 코드와 같이 새로운 전략을 생성하기 용이하다.

## 전략 패턴의 장점과 단점
- 장점
  - 새로운 전략을 추가하더라도 기존 코드를 변경하지 않는다.
  - 상속 대신 위임을 사용할 수 있다.
  - 런타임에 전략을 변경할 수 있다.
- 단점
  - 복잡도가 증가한다.
  - 클라이언트 코드가 구체적인 전략을 알아야 한다.

## Java와 Spring에서의 활용 예시
### Java
- `Comparator`

### Spring
- `ApplicationContext`
- `PlatformTransactionManager`
- ...

```java
ApplicationContext applicationContext = new ClassPathXmlApplicationContext();
ApplicationContext applicationContext1 = new FileSystemXmlApplicationContext();
ApplicationContext applicationContext2 = new AnnotationConfigApplicationContext();

BeanDefinitionParser parser;

PlatformTransactionManager platformTransactionManager;

CacheManager cacheManager;
```