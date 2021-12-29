# 7. 함께 모으기

마틴 파울러가 주장하는 객체지향 설계 안에 존재하는 세 가지 상호 연관된 관점은 다음과 같다.

1. 개념 관점(Conceptual Perspective)

   - 설계는 도메인 안에 존재하는 개념과 개념들 사이의 관계를 표현한다.
   - 도메인: 사용자들이 관심을 가지고 있는 특정 분야나 주제로 소프트웨어는 여기에 존재하는 문제를 해결하기 위해 개발됨.
   - 이 관점은 사용자가 도메인을 바라보는 관점을 반영한다.
   - 실제 도메인의 규칙과 제약을 최대한 유사하게 반영하는 것이 핵심

2. 명세 관점(Specification Perspective)

   - 도메인의 개념이 아니라 객체의 인터페이스를 바라보게 된다.
   - 프로그래머는 객체가 협력을 위해 '무엇'을 할 수 있는가에 초점을 맞춤.
   - **인터페이스와 구현을 분리하는 것**은 객체지향 설계의 가장 기본적인 원칙

3. 구현 관점(Implementation Perspective)
   - 이 관점의 초점은 객체들이 책임을 수행하는 데 필요한 동작하는 코드를 작성하는 것.
   - 프로그래머는 객체의 책임을 '어떻게' 수행할 것인가에 초점을 맞춤.

이 세 가지 관점은 동일한 클래스를 세 가지 다른 방향에서 바라보는 것을 의미한다. 클래스는 세 가지 관점을 모두 수용할 수 있도록 개념, 인터페이스, 구현을 함께 드러내야 한다.

## 커피 전문점 도메인

- Menu: 아메리카노, 카푸치노, 카라멜 마끼아또, 에스프레소
- 도메인: **손님** 객체, **메뉴항목** 객체, **메뉴판** 객체, **바리스타** 객체, **커피** 객체
- 객체들 간의 관계
  - 손님은 메뉴판을 알아야 함
  - 손님은 바리스타에게 메뉴 주문
  - 바리스타는 커피를 제조

![image](https://user-images.githubusercontent.com/60968342/147658142-48e6aa54-fbd0-4c1e-a20d-bb585f20738f.png)

### 포함 관계 또는 합성 관계

메뉴판 타입과 메뉴 항목 타입 간의 **포함(containment) 관계** 또는 **합성(composition) 관계**는 다음과 같다.  
메뉴판 타입에서 메뉴 항목 쪽으로 향하는 선에 그려진 속이 찬 마름모가 해당 관계를 나타내며, 메뉴 항목이 메뉴판에 포함된다는 사실을 표현한다. 메뉴 항목 좌측 아래의 4라는 숫자는 메뉴 항목이 4개라는 의미이다.

![image](https://user-images.githubusercontent.com/60968342/147658169-0575aa54-2cb8-4407-9688-9e0e81e76b5e.png)

### 연관 관계

손님 타입은 메뉴판 타입을 알고 있어야 원하는 커피를 선택할 수 있다.  
다음은 타입들 사이를 단순한 선으로 연결하여 한 인스턴스가 다른 타입의 인스턴스를 포함하진 않지만 서로 알고 있어야 할 경우인 **연관(association) 관계**를 나타낸다.

![image](https://user-images.githubusercontent.com/60968342/147658195-743bb5f7-ad1d-4260-a3c5-1a34bea56365.png)

### 커피 전문점의 도메인을 단순화 시킨 것

![image](https://user-images.githubusercontent.com/60968342/147658209-e9f717c7-af83-45df-90c0-ed1171b29164.png)

#### 📌 참고

> 실제로 도메인 모델을 작성하는 단계에서 어떤 관계가 포함 관계이고 어떤 관계가 연관 관계인지는 중요하지 않다. 초점은 **어떤 타입이 도메인을 구성하느냐와 타입들 사이에 어떤 관계가 존재하는지를 파악함으로써 도메인을 이해하는 것이다.** 위 예시에서는 메뉴판과 메뉴 항목 사이, 손님과 메뉴판 사이에 관계가 존재한다는 사실을 이해하는 것만으로도 충분하다.

## 설계하고 구현하기

### 협력 찾기

협력을 설계할 때는 메시지를 먼저 선택하고, 메시지를 수신하기에 적절한 객체를 선택해야 한다.
![image](https://user-images.githubusercontent.com/60968342/147658223-dfb95b76-3e72-4914-928c-d035f8175186.png)

1. 커피를 주문하라

   - 어떤 객체가 커피를 주문할 책임이 있는가 = 손님
   - 손님은 메뉴 항목을 모른다.

2. 메뉴 항목을 찾아라

   - 어떤 객체가 메뉴 항목을 반환할 수 있는가 = 메뉴판
   - 메뉴 항목을 얻었으니 커피를 제조해달라고 요청할 수 있다.

3. 커피를 제조하라
   - 바리스타는 커피제조법을 모두 알고 있다.(자율적인 객체: 어떻게 만들든 상관 x)

### 인터페이스 정리하기

객체가 수신한 메시지가 객체의 인터페이스를 결정한다.

객체가 어떤 메시지를 수신할 수 있다는 것은 객체의 인터페이스 안에 메시지에 해당하는 오퍼레이션(외부에서 접근 가능한)이 존재한다는 것을 의미한다.

- 손님 객체: 커피를 주문하라
- 메뉴판 객체: 메뉴 항목을 찾아라
- 바리스타 객체: 커피를 제조하라
- 커피 객체: 생성하라

```java
class Customer {
    public void order(String menuName) {}
}

class MenuItem { }

class Menu {
    public MenuItem choose(String name) {}
}

class Barista {
    public Coffee makeCoffee(MenuItem menuItem) {}
}

class Coffee {
    public Coffee(MenuItem menuItem) {}
}
```

### 구현하기

```java
/**
*    1. Menu에게 menuName에 해당하는 MenuItem을 요청해야 함.
*    2. 이를 받아 Barista에게 커피를 만들라고 전달해야 함.
*    3. 즉 Menu 객체와 Barista 객체에 대한 참조를 알아야하므로 인자로 전달받는 방법을 선택
**/
class Customer {
    public void order(String menuName, Menu menu, Barista barista) {
        MenuItem menuItem = menu.choose(menuName);
        Coffee coffee = barista.makeCoffee(menuItem);
        //...
    }
}

/**
*    MenuItem
**/
class MenuItem {
    private String name;
    private int price;

    public MenuItem(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public int getPrice() {
        return this.price;
    }

    public String getName() {
        return this.name;
    }
}

/**
*    1. menuName에 해당하는 MenuItem을 찾아야 함.
*    2. 이를 위해 내부적으로 MenuItem을 관리
**/
class Menu {
    private List<MenuItem> items;

    public Menu(List<MenuItem> items) {
        this.items = items;
    }

    public MenuItem choose(String name) {
        for (MenuItem each : items) {
            if (each.getName().equals(name)) {
                return each;
            }
        }
        return null;
    }
}

/**
*    커피를 만들어서 Coffee 리턴
**/
class Barista {
    public Coffee makeCoffee(MenuItem menuItem) {
        Coffee coffee = new Coffee(menuItem);
        return coffee;
    }
}

/**
*    1. 자기 자신을 생성하기 위한 생성자를 제공
*    2. Coffee는 커피이름과 가격을 가지고 생성자 안에서 MenuItem에 요청을 보내
*       커피 이름과 가격을 얻고 속성에 저장
**/
class Coffee {
    private String name;
    private int price;

    public Coffee(MenuItem menuItem) {
        this.name = menuItem.getName();
        this.price = menuItem.getPrice();
    }

    public int getPrice() {
        return this.price;
    }

    public String getName() {
        return this.name;
    }
}
```

최종 클래스 다이어그램은 다음과 같다.

![image](https://user-images.githubusercontent.com/60968342/147658237-623cdbea-9d86-40a1-8bc2-c3b5476f2c21.png)

## 코드와 세 가지 관점

### 코드는 세 가지 관점을 모두 제공해야 한다

- 개념 관점

  - 현실세계에서도 바리스타가 커피를 제조하듯 Barista라는 클래스가 커피를 제조할 것이라고 쉽게 유추 가능

- 명세 관점

  - 클래스의 인터페이스를 바라본다.
  - 클래스의 public 메서드는 다른 클래스가 협력할 수 있는 공용 인터페이스
  - 변화의 안정적인 인터페이스를 만들기 위해서는 인터페이스를 통해 구현과 관련된 세부사항이 드러나지 않게 해야한다.

- 구현 관점

  - 클래스의 내부 구현을 바라본다.
  - 메서드의 구현과 속성의 변경은 원칙적으로 외부의 객체에게 영향을 미쳐서는 안된다.
  - 메서드와 속성은 클래스 내부의 비밀(캡슐화돼야 함)

훌륭한 객체지향 프로그래머는 하나의 클래스 안에 세 가지 관점을 모두 포함하면서도 각 관점에 대응되는 요소를 명확하고 깔끔하게 드러낼 수 있어야 한다.

### 도메인 개념을 참조하는 이유

도메인 개념 안에서 적절한 객체를 선택하는 것은 도메인에 대한 지식을 기반으로 코드의 구조와 의미를 쉽게 유추할 수 있게 한다. 이것은 시스템의 유지보수성에 커다란 영향을 미친다.

소프트웨어는 항상 변하고 설계는 변경을 위해 존재한다. 여러 개의 클래스로 기능을 분할하고 클래스 안에서 인터페이스와 구현을 분리하는 이유는 변경이 발생했을 때 코드를 좀 더 수월하게 수정하기 위함이다. **소프트웨어 클래스가 도메인 개념을 따르면 변화에 쉽게 대응할 수 있다.**

### 인터페이스와 구현을 분리하라

저자가 가장 강조하는 말이다.

명세 관점과 구현 관점이 뒤섞여 머릿속을 함부로 어지럽히지 못하게 하라.

명세 관점은 클래스의 안정적인 측면을 드러내야 한다. 구현 관점은 클래스의 불안정한 측면을 드러내야 한다. 인터페이스가 구현 세부 사항을 노출하기 시작하면 아주 작은 변동에도 전체 협력이 요동치는 취약한 설계를 얻을 수 밖에 없다.

**명세 관점이 설계를 주도하게 하면 설계의 품질이 향상될 수 있다는 사실을 기억하라.**
