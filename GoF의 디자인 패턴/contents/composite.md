# 컴포짓(Composite) 패턴
의도: 부분과 전체의 계층을 표현하기 위해 객체들을 모아 트리 구조로 구성한다. 사용자로 하여금 개별 객체와 복합 객체를 모두 동일하게 다룰 수 있도록 하는 패턴이다.

그룹 전체와 개별 객체를 동일하게 처리할 수 있는 패턴.

- 클라이언트 입장에서는 '전체'나 '부분'이나 모두 동일한 컴포넌트로 인식할 수 있는 계층 구조를 만든다. (Part-Whole Hierarchy)

![](https://velog.velcdn.com/images/songs4805/post/e2701e47-95f1-46df-b28c-a8e78f40d24c/image.png)

- `Component`: 집합 관계에 정의될 모든 객체에 대한 인터페이스를 정의한다. 모든 클래스에 해당하는 인터페이스에 대해서는 공통의 행동을 구현한다. 전체 클래스에 속한 요소들을 관리하는 데 필요한 인터페이스를 정의한다. 순환 구조에서 요소들을 포함하는 전체 클래스로 접근하는 데 필요한 인터페이스를 정의하며, 적절하다면 그 인터페이스를 구현한다.
- `Leaf`: 가장 말단의 객체, 즉 자식이 없는 객체를 나타낸다. 객체 합성에 가장 기본이 되는 객체의 행동을 정의한다.
- `Composite`: 자식이 있는 구성요소에 대한 행동을 정의한다. 자신이 복합하는 요소들을 저장하면서, `Component` 인터페이스에 정의된 자식 관련 연산을 구현한다.
- `Client`: `Component` 인터페이스를 통해 복합 구조 내의 객체들을 조작한다.

## 활용성
컴포짓 패턴은 다음과 같은 경우에 사용한다.
- 부분-전체의 객체 계통을 표현하고 싶을 때
- 사용자가 객체의 합성으로 생긴 복합 객체와 개개의 객체 사이의 차이를 알지 않고도 자기 일을 할 수 있도록 만들고 싶을 때. 사용자는 복합 구조(composite structure)의 모든 객체를 똑같이 취급하게 된다.

## 구현
먼저 클라이언트가 동일하게 사용하는 오퍼레이션을 인터페이스에 정의해둔다.

```java
public interface Component {
    int getPrice();
}
```

이 후, 이를 구현하는 Composite를 만든다.

```java
public class Item implements Component {

    private String name;

    private int price;

    public Item(String name, int price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public int getPrice() {
        return this.price;
    }
}
```

```java
public class Bag implements Component {

    private List<Component> components = new ArrayList<>();

    public void add(Component component) {
        components.add(component);
    }

    public List<Component> getComponents() {
        return components;
    }

    @Override
    public int getPrice() {
        return components.stream().mapToInt(Component::getPrice).sum();
    }
}
```

가격을 어떻게 구할 지는 `Client`는 몰라도 된다. `Component`가 알아낸다.
```java
public class Client {
    public static void main(String[] args) {
        Item doranBlade = new Item("도란검", 450);
        Item healPotion = new Item("체력 물약", 50);

        Bag bag = new Bag();
        bag.add(doranBlade);
        bag.add(healPotion);

        Client client = new Client();
        client.printPrice(doranBlade);
        client.printPrice(bag);
    }

    private void printPrice(Component component) {
        System.out.println(component.getPrice());
    }
}
```

## 컴포짓 패턴의 장점과 단점
- 장점
  - 복잡한 트리 구조를 편리하게 사용할 수 있다. 
  - 다형성과 재귀를 활용할 수 있다.
  - 클라이언트 코드를 변경하지 않고 새로운 엘리먼트 타입을 추가할 수 있다.
- 단점
  - 트리를 만들어야 하기 때문에 (공통된 인터페이스를 정의해야 하기 때문에) 지나치게 일반화 해야 하는 경우도 생길 수 있다.

## Java와 Spring에서의 활용 예시
### Java
- Swing 라이브러리
- JSF (JavaServer Faces) 라이브러리