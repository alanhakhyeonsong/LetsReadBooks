# 프록시(Proxy) 패턴
의도: 다른 객체에 대한 접근을 제어하기 위한 대리자 또는 자리채움자 역할을 하는 객체를 둔다.

특정 객체에 대한 접근을 제어하거나 기능을 추가할 수 있는 패턴이다. 클라이언트가 원래 사용하려는 객체를 직접 쓰는게 아니라 중간에 대리인을 거쳐 사용한다. 맨 처음 요청은 프록시가 받는다.

- 초기화 지연, 접근 제어, 로깅, 캐싱 등 다양하게 응용해 사용 할 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/3954d29a-4d82-40be-a2b8-d742bf01f8de/image.png)

- **Proxy**
  - 실제로 참조할 대상에 대한 참조자를 관리한다. `RealSubject`와 `Subject` 인터페이스가 동일하다면 프록시는 `Subject`에 대한 참조자를 갖는다.
  - `Subject`와 동일한 인터페이스를 제공하여 실제 대상을 대체할 수 있어야 한다.
  - 실제 대상에 대한 접근을 제어하고 실제 대상의 생성과 삭제를 책임진다.
  - 원격지 프록시: 요청 메시지와 인자를 인코딩하여 이를 다른 주소 공간에 있는 실제 대상에게 전달한다.
  - 가상의 프록시: 실제 대상에 대한 추가적 정보를 보유하여 실제 접근을 지연할 수 있도록 해야 한다.
  - 보호용 프록시: 요청한 대상이 실제 요청할 수 있는 권한이 있는지 확인한다.
- **Subject**: `RealSubject`와 `Proxy`에 공통적인 인터페이스를 정의하여, `RealSubject`가 요청되는 곳에 `Proxy`를 사용할 수 있게 한다.
- **RealSubject**: 프록시가 대표하는 실제 객체이다.

**협력 방법: 프록시 클래스는 자신이 받은 요청을 `RealSubject` 객체에 전달한다.**

## 활용성
GoF의 디자인 패턴에선 다음과 같이 제시하고 있다.

프록시 패턴은 단순한 포인터보다는 조금 더 다방면에 활용할 수 있거나 정교한 객체 참조자가 필요할 때 적용할 수 잇다.

- 원격지 프록시: 서로 다른 주소 공간에 존재하는 객체를 가리키는 대표 객체로, 로컬 환경에 위치한다.
- 가상 프록시: 요청이 있을 때만 필요한 고비용 객체를 생성한다.
- 보호용 프록시: 원래 객체에 대한 실제 접근을 제어한다. 이는 객체별로 다른 접근 제어 권한이 다를 때 유용하게 사용할 수 있다.
- 스마트 참조자: 원시 포인터의 대체용 객체로, 실제 객체에 접근이 일어날 때 추가적인 행동을 수행한다.
  - 실제 객체에 대한 참조 횟수를 저장하다가 더는 참조가 없을 때 해당 객체를 자동으로 없앤다.(스마트 포인터)
  - 맨 처음 참조되는 시점에 영속적 저장소의 객체를 메모리로 옮긴다.
  - 실제 객체에 접근하기 전에, 다른 객체가 그것을 변경하지 못하도록 실제 객체에 대해 잠금(lock)을 건다.

## 예시

`Client`는 Proxy를 호출해서 실제 객체의 동작을 요청한다.
```java
public class Client {
    
    public static void main(String[] args) {
        GameService gameService = new GameServiceProxy();
        gameService.startGame();
    }
}
```

인터페이스인 `GameService`는 다음과 같다.
```java
public Interface GameService {
    
    void startGame();
}
```

실제 객체인 `DefaultGameService`는 인터페이스를 구현하고 본연의 역할에만 집중하도록 한다.
```java
public class DefaultGameService implements GameService {
    
    @Override
    public void startGame() {
        System.out.println("이 자리에 오신 여러분을 진심으로 환영합니다.");
    }
}
```

프록시 객체는 다음과 같다.
```java
public class GameServiceProxy implements GameService {

    private GameService gameService;

    @Override
    public void startGame() {
        long before = System.currentTimeMillis();

        if (this.gameService == null) {
            this.gameService = new DefaultGameService();
        }

        gameService.startGame();
        System.out.println(System.currentTimeMillis() - before);
    }
}
```

## 프록시 패턴의 장단점
- 장점
  - 기존 코드를 변경하지 않고 새로운 기능을 추가할 수 있다. → OCP 원칙
  - 기존 코드가 해야 하는 일만 유지할 수 있다. → SRP 원칙
  - 기능 추가 및 초기화 지연 등으로 다양하게 활용할 수 있다.
- 단점
  - 코드의 복잡도가 증가한다.

## Java와 Spring에서의 활용 예시
- Java
  - 다이나믹 프록시, `java.lang.reflect.Proxy`
- Spring
  - Spring AOP