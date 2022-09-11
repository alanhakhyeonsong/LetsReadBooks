# 추상 팩토리(Abstract factory) 패턴
의도: 상세화된 서브클래스를 정의하지 않고도 서로 관련성이 있거나 독립적인 여러 객체의 군을 생성하기 위한 인터페이스를 제공한다.

- 구체적으로 어떤 클래스의 인스턴스(concrete product)를 사용하는지 감출 수 있다.

구조는 다음과 같다.

![](https://velog.velcdn.com/images/songs4805/post/bf7e987b-cebb-42ac-9aa5-3d7a21f09885/image.jpeg)

- `AbstractFactory`: 개념적 제품에 대한 객체를 생성하는 연산으로 인터페이스를 정의한다.
- `ConcreteFactory`: 구체적인 제품에 대한 객체를 생성하는 연산을 구현한다.
- `Product`: 개념적 제품 객체에 대한 인터페이스를 정의한다.
- `ConcreteProduct`: 구체적으로 팩토리가 생성할 객체를 정의하고, `Product`가 정의하는 인터페이스를 구현한다.
- `Client`: `AbstractFactory`와 `Product` 클래스에 선언된 인터페이스를 사용한다.

## 활용성
추상 팩토리는 다음의 경우에 사용한다.
- 객체가 생성되거나 구성, 표현되는 방식과 무관하게 시스템을 독립적으로 만들고자 할 때
- 여러 제품군 중 하나를 선택해서 시스템을 설정해야 하고 한번 구성한 제품을 다른 것으로 대체할 수 있을 때
- 관련된 제품 객체들이 함께 사용되도록 설계되었고, 이 부분에 대한 제약이 외부에도 지켜지도록 하고 싶을 때
- 제품에 대한 클래스 라이브러리를 제공하고, 그들의 구현이 아닌 인터페이스를 노출시키고 싶을 때

## 구현
클라이언트 코드에서 구체적인 클래스의 의존성을 제거한다.

### 추상 팩토리 적용 전
```java
public class WhiteshipFactory extends DefaultShipFactory {

    @Override
    public Ship createShip() {
        Ship ship = new Whiteship();
        ship.setAnchor(new WhiteAnchor());
        // 제품군이 바뀔 시 ship.setAnchor(new NewWhiteAnchor()); 와 같이 변경돼야 함.
        ship.setWheel(new WhiteWheel());
        return ship;
    }
}
```

setter에 `WhiteAnchor`, `WhiteWheel` 같은 구체적 클래스 타입을 직접 만들어 주고 있는데, 만약 이 제품군이 바뀌게 된다면 코드 역시 바뀌게 된다. 이는 OCP 원칙을 깨버리는 구조이다.

### 추상 팩토리 적용 후
먼저, 추상 팩토리는 다음과 같다.
```java
public interface ShipPartsFactory {

    Anchor createAnchor();

    Wheel createWheel();

}
```

이를 구현한 구체적 팩토리는 다음과 같다.

```java
public class WhiteshipPartsFactory implements ShipPartsFactory {

    @Override
    public Anchor createAnchor() {
        return new WhiteAnchor();
    }

    @Override
    public Wheel createWheel() {
        return new WhiteWheel();
    }
}
```

`WhiteAnchor`와 `WhiteWheel`은 각각 `Anchor`와 `Wheel` 인터페이스를 구현하도록 정의해야 한다.

이를 통해, 클라이언트 코드는 다음과 같이 변경된다.

```java
public class WhiteshipFactory extends DefaultShipFactory {

    private ShipPartsFactory shipPartsFactory;

    public WhiteshipFactory(ShipPartsFactory shipPartsFactory) {
        this.shipPartsFactory = shipPartsFactory;
    }

    @Override
    public Ship createShip() {
        Ship ship = new Whiteship();
        ship.setAnchor(shipPartsFactory.createAnchor());
        ship.setWheel(shipPartsFactory.createWheel());
        return ship;
    }
}
```

만약 다른 제품군이 필요하더라도 위 클라이언트 코드를 변경할 필요가 없어진다. 새로운 제품군을 규약에 맞게 새로 정의하고 클라이언트 코드의 생성자에 넣어주기만 하면 된다. 결과적으로 OCP 원칙을 지킬 수 있게 되었다.

새로운 제품군의 구체적 팩토리는 다음과 같다.
```java
public class WhitePartsProFactory implements ShipPartsFactory {
    @Override
    public Anchor createAnchor() {
        return new WhiteAnchorPro();
    }

    @Override
    public Wheel createWheel() {
        return new WhiteWheelPro();
    }
}
```

마찬가지로 `WhiteAnchorPro`와 `WhiteWheelPro`는 각각 `Anchor`, `Wheel` 인터페이스를 구현하도록 정의해둔다.

## 추상 팩토리 패턴와 팩토리 메소드 패턴 비교
팩토리 메소드 패턴과 굉장히 흡사한데 무엇이 다른걸까?

- 모양과 효과는 비슷하지만
  - 둘 다 구체적인 객체 생성 과정을 추상화한 인터페이스를 제공한다.
- 관점이 다르다.
  - 팩토리 메소드 패턴은 **팩토리를 구현하는 방법(inheritance)** 에 초점을 둔다.
  - 추상 팩토리 패턴은 **팩토리를 사용하는 방법(composition)** 에 초점을 둔다.
- 목적이 조금 다르다.
  - 팩토리 메소드 패턴은 **구체적인 객체 생성 과정을 하위 또는 구체적인 클래스로 옮기는 것이 목적**
  - 추상 팩토리 패턴은 **관련있는 여러 객체를 구체적인 클래스에 의존하지 않고 만들 수 있게 해주는 것이 목적**

## Java와 Spring에서의 활용 예시
### Java 라이브러리
- `javax.xml.xpath.XPathFactory#newInstance()`
- `javax.xml.transform.TransformerFactory#newInstance()`
- `javax.xml.parsers.DocumentBuilderFactory#newInstance()`

### Spring
`FactoryBean`과 그 구현체

```java
public class ShipFactory implements FactoryBean<Ship> {

    @Override
    public Ship getObject() throws Exception {
        Ship ship = new Whiteship();
        ship.setName("whiteship");
        return ship;
    }

    @Override
    public Class<?> getObjectType() {
        return Ship.class;
    }
}
```

```java
@Configuration
public class FactoryBeanConfig {

    @Bean
    public ShipFactory shipFactory() {
        return new ShipFactory();
    }
}
```

```java
public class FactoryBeanExample {

    public static void main(String[] args) {
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("config.xml");
//        Ship whiteship = applicationContext.getBean("whiteship", Ship.class);
//        System.out.println(whiteship.getName());

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(FactoryBeanConfig.class);
        Ship bean = applicationContext.getBean(Ship.class);
//        ShipFactory bean = applicationContext.getBean(ShipFactory.class);
        System.out.println(bean);
    }
}
```