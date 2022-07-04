# 팩토리 메소드(Factory method) 패턴
의도: 객체를 생성하기 위해 인터페이스를 정의하지만, 어떤 클래스의 인스턴스를 생성할지에 대한 결정은 서브클래스가 내리도록 한다.

다른 이름으로 가상 생성자(Virtual Constructor)라고도 한다.

- 다양한 구현체(Product)가 있고, 그중에서 특정한 구현체를 만들 수 있는 다양한 팩토리(Creator)를 제공할 수 있다.

구조는 다음과 같다.

<p aline="center">
  <img src="https://velog.velcdn.com/images/songs4805/post/9bb19910-020a-4a3f-85db-b864647d62a1/image.png">
</p>

- Product: 팩토리 메서드가 생성하는 객체의 인터페이스를 정의한다.
- ConcreteProduct: Product 클래스에 정의된 인터페이스를 실제로 구현한다.
- Creator: Product 타입의 객체를 반환하는 팩토리 메서드를 선언한다.
- ConcreteCreator: 팩토리 메서드를 재정의하여 ConcreteProduct의 인스턴스를 반환한다.

## 활용성
팩토리 메서드는 다음과 같은 상황에서 사용한다.
- 어떤 클래스가 자신이 생성해야 하는 객체의 클래스를 예측할 수 없을 때
- 생성할 객체를 기술하는 책임을 자신의 서브클래스가 지정했으면 할 때
- 객체 생성의 책임을 몇 개의 보조 서브클래스 가운데 하나에게 위임하고, 어떤 서브클래스가 위임자인지에 대한 정보를 국소화시키고 싶을 때

## 구현
### 팩토리 메소드 적용 전
```java
@Data
public class Ship {

    private String name;

    private String color;

    private String logo;
}
```

```java
public class ShipFactory {

    public static Ship orderShip(String name, String email) {
        // validate
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("배 이름을 지어주세요.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("연락처를 남겨주세요.");
        }

        prepareFor(name);

        Ship ship = new Ship();
        ship.setName(name);

        // Customizing for specific name
        if (name.equalsIgnoreCase("whiteship")) {
            ship.setLogo("\uD83D\uDEE5️");
        } else if (name.equalsIgnoreCase("blackship")) {
            ship.setLogo("⚓");
        }

        // coloring
        if (name.equalsIgnoreCase("whiteship")) {
            ship.setColor("whiteship");
        } else if (name.equalsIgnoreCase("blackship")) {
            ship.setColor("black");
        }

        // notify
        sendEmailTo(email, ship);

        return ship;
    }

    private static void prepareFor(String name) {
        System.out.println(name + " 만들 준비 중");
    }

    private static void sendEmailTo(String email, Ship ship) {
        System.out.println(ship.getName() + " 다 만들었습니다.");
    }
}
```

```java
public class Client {

    public static void main(String[] args) {
        Ship whiteship = ShipFactory.orderShip("Whiteship", "ramos@mail.com");
        System.out.println(whiteship);

        Ship blackship = ShipFactory.orderShip("Blackship", "ramos@mail.com");
        System.out.println(blackship);
    }
}
```

위 예제에서 `Ship`의 내부 코드가 바뀌면 `ShipFactory`도 코드가 바뀌게 된다. 변경에 닫혀있지 않기 때문에 OCP 원칙을 지키지 못한다.

팩토리 메소드 패턴을 적용하여 이를 확장에 열려있고 변경에 닫혀있는 구조로 바꿔보자.

### 팩토리 메소드 적용 후
```java
public interface ShipFactory {

    default Ship orderShip(String name, String email) {
        validate(name, email);
        prepareFor(name);
        Ship ship = createShip();
        sendEmailTo(email, ship);
        return ship;
    }

    void sendEmailTo(String email, Ship ship);

    Ship createShip();

    private void validate(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("배 이름을 지어주세요.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("연락처를 남겨주세요.");
        }
    }

    private void prepareFor(String name) {
        System.out.println(name + " 만들 준비 중");
    }
}
```
`orderShip()` 코드의 가독성이 높아졌다. 또한 `Ship`을 생성하는 것을 하위 클래스에 위임하였다.

```java
public abstract class DefaultShipFactory implements ShipFactory {

    @Override
    public void sendEmailTo(String email, Ship ship) {
        System.out.println(ship.getName() + " 다 만들었습니다.");
    }
}
```

```java
public class BlackshipFactory extends DefaultShipFactory {
    @Override
    public Ship createShip() {
        return new Blackship();
    }
}
```

```java
public class WhiteshipFactory extends DefaultShipFactory {

    @Override
    public Ship createShip() {
        return new Whiteship();
    }
}
```

```java
@Data
public class Ship {

    private String name;

    private String color;

    private String logo;

    private Wheel wheel;

    private Anchor anchor;
}
```

```java
public class Whiteship extends Ship {

    public Whiteship() {
        setName("whiteship");
        setLogo("\uD83D\uDEE5️");
        setColor("white");
    }
}
```

```java
public class Blackship extends Ship {

    public Blackship() {
        setName("blackship");
        setColor("black");
        setLogo("⚓");
    }
}
```
`Ship`에 따라 속성값이 다르다.

```java
public class Client {

    public static void main(String[] args) {
        Client client = new Client();
        client.print(new WhiteshipFactory(), "whiteship", "ramos@mail.com");
        client.print(new BlackshipFactory(), "blackship", "ramos@mail.com");
    }

    // 인터페이스를 파라미터로 넣음.
    // 일종의 DI 이기도 하다.
    private void print(ShipFactory shipFactory, String name, String email) {
        System.out.println(shipFactory.orderShip(name, email));
    }
}
```
`Client`의 코드에 변경이 있다.

![](https://velog.velcdn.com/images/songs4805/post/95c8cd0c-44a6-4731-8db1-a83453e135fc/image.png)

## 정리
- 팩토리 메소드 패턴을 적용했을 때의 장점과 단점은?  
→ 기존 코드의 변경 없이 확장성이 높아지는 장점이 있고, 이를 위한 클래스가 많아진다는 단점이 있다.
- "확장에 열려있고 변경에 닫혀있는 객체 지향 원칙" 이란?  
→ 기존 코드의 변경 없이 새로운 기능을 추가할 수 있는 OCP 원칙이다.
- Java 8에 추가된 default 메소드란?
→ 인터페이스 내에 기본 구현체를 만들 수 있어서 해당 인터페이스를 상속/구현하는 쪽에서도 해당 메소드를 그대로 사용할 수 있게 해준다.