# 중재자(Mediator) 패턴
의도: 한 집합에 속해있는 객체의 상호작용을 캡슐화하는 객체를 정의한다. 객체들이 직접 서로를 참조하지 않도록 하여 객체 사이의 소결합(loose coupling)을 촉진시키며, 개발자가 객체의 상호작용을 독립적으로 다양화시킬 수 있게 만든다.

여러 객체들이 소통하는 방법을 캡슐화 하는 패턴.
- 여러 컴포넌트간의 결합도를 중재자를 통해 낮출 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/8d42d568-8ec8-40fc-b03b-8d9457adb853/image.png)

- `Mediator`: `Colleague` 객체와 교류하는 데 필요한 인터페이스를 정의한다.
- `ConcreteMediator`: `Colleague` 객체와 조화를 이뤄서 협력 행동을 구현하며, 자신이 맡을 동료(colleague)를 파악하고 관리한다.
- `Colleague` 클래스들: 자신의 중재자 객체가 무엇인지 파악한다. 다른 객체와 통신이 필요하면 그 중재자를 통해 통신되도록 하는 동료 객체를 나타내는 클래스이다.

협력 방법: `Colleague`는 `Mediator`에서 요청을 송수신한다. `Mediator`는 필요한 `Colleague` 사이에 요청을 전달할 의무가 있다.

## 활용성
중재자 패턴은 다음의 경우에 사용한다.
- 여러 객체가 잘 정의된 형태이기는 하지만 복잡한 상호작용을 가질 때. 객체간의 의존성이 구조화되지 않으며, 잘 이해하기 어려울 때.
- 한 객체가 다른 객체를 너무 많이 참조하고, 너무 많은 의사소통을 수행해서 그 객체를 재사용하기 힘들 때
- 여러 클래스에 분산된 행동들이 상속 없이 상황에 맞게 수정되어야 할 때

## 구현
### 중재자 패턴 적용 전
```java
public class Restaurant {

    private CleaningService cleaningService = new CleaningService();
    public void dinner(Guest guest) {
        System.out.println("dinner " + guest);
    }

    public void clean() {
        cleaningService.clean(this);
    }
}
```

```java
public class CleaningService {
    public void clean(Gym gym) {
        System.out.println("clean " + gym);
    }

    public void getTower(Guest guest, int numberOfTower) {
        System.out.println(numberOfTower + " towers to " + guest);
    }

    public void clean(Restaurant restaurant) {
        System.out.println("clean " + restaurant);
    }
}
```

```java
public class Guest {

    private Restaurant restaurant = new Restaurant();

    private CleaningService cleaningService = new CleaningService();

    public void dinner() {
        restaurant.dinner(this);
    }

    public void getTower(int numberOfTower) {
        cleaningService.getTower(this, numberOfTower);
    }

}
```

```java
public class Gym {

    private CleaningService cleaningService;

    public void clean() {
        cleaningService.clean(this);
    }
}
```

```java
public class Hotel {

    public static void main(String[] args) {
        Guest guest = new Guest();
        guest.getTower(3);
        guest.dinner();

        Restaurant restaurant = new Restaurant();
        restaurant.clean();
    }
}
```
위 예제 코드는 각 객체들이 서로 얽혀있어 변경하거나 재사용하거나 테스트하기도 어렵다.

### 중재자 패턴 적용 후


## 중재자 패턴의 장점과 단점
- 장점
  - 컴포넌트 코드를 변경하지 않고 새로운 중재자를 만들어 사용할 수 있다.
  - 각각의 컴포넌트 코드를 보다 간결하게 유지할 수 있다.
- 단점
  - 중재자 역할을 하는 클래스의 복잡도와 결합도가 증가한다.

## Java와 Spring에서의 활용 예시
### Java
- `ExecutorService`
- `Executor`

### Spring
- `DispatcherServlet`