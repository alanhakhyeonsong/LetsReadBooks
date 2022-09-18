# 책임 연쇄(Chain of Responsibility) 패턴
의도: 메시지를 보내는 객체와 이를 받아 처리하는 객체들 간의 결합도를 없애기 위한 패턴이다. 하나의 요청에 대한 처리가 반드시 한 객체에서만 되지 않고, 여러 객체에게 그 처리 기회를 주려는 것이다.

요청을 보내는 쪽(sender)와 요청을 처리하는 쪽(receiver)을 분리하는 패턴
- 핸들러 체인을 사용해서 요청을 처리한다.

![](https://velog.velcdn.com/images/songs4805/post/c9c9c35d-992d-47ed-a2eb-eedfd1b28070/image.png)

- `Handler`: 요청을 처리하는 인터페이스를 정의하고, 후속 처리자(successor)와 연결을 구현한다. 즉, 연결 고리에 연결된 다음 객체에게 다시 메시지를 보낸다.
- `ConcreteHandler`: 책임져야 할 행동이 있다면 스스로 요청을 처리하여 후속 처리자에 접근할 수 있다. 즉, 자신이 처리할 행동이 있으면 처리하고, 그렇지 않으면 후속 처리자에게 다시 처리를 요청한다.
- `Client`: `ConcreteHandler` 객체에게 필요한 요청을 보낸다.

협력 방법: 사용자는 처리를 요청하고, 이 처리 요청은 실제로 그 요청을 받을 책임이 있는 `ConcreteHandler` 객체를 만날 때까지 정의된 연결 고리를 따라서 계속 전달된다.

## 활용성
책임 연쇄 패턴은 다음의 경우에 사용한다.
- 하나 이상의 객체가 요청을 처리해야 하고, 그 요청 처리자 중 어떤 것이 선행자인지 모를 때, 처리자가 자동으로 확정되어야 한다.
- 메시지를 받을 객체를 명시하지 않은 채 여러 객체 중 하나에게 처리를 요청하고 싶을 때
- 요청을 처리할 수 있는 객체 집합이 동적으로 정의되어야 할 때

## 구현
### 책임 연쇄 패턴 적용 전
```java
public class Request {

    private String body;

    public Request(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
```

```java
public class RequestHandler {

    public void handler(Request request) {
        System.out.println(request.getBody());
    }
}
```

필요한 추가 기능은 다음과 같이 상속받아 구현한다.
```java
public class LoggingRequestHandler extends RequestHandler {

    @Override
    public void handler(Request request) {
        System.out.println("로깅");
        super.handler(request);
    }
}
```

```java
public class AuthRequestHandler extends RequestHandler {

    public void handler(Request request) {
        System.out.println("인증이 되었나?");
        System.out.println("이 핸들러를 사용할 수 있는 유저인가?");
        super.handler(request);
    }
}
```

클라이언트 코드는 다음과 같다.
```java
public class Client {

    public static void main(String[] args) {
        Request request = new Request("무궁화 꽃이 피었습니다.");
//        RequestHandler requestHandler = new LoggingRequestHandler();
        RequestHandler requestHandler = new AuthRequestHandler(); 
        requestHandler.handler(request);
    }
}
```

클라이언트가 구체적인 핸들러를 직접 선택 해야 한다. 만약 핸들러 여러 개가 필요하다면 문제가 된다. 목표는 클라이언트 측에서 구체적 핸들러 타입을 모르게 하고 싶다.

### 책임 연쇄 패턴 적용 후
![](https://velog.velcdn.com/images/songs4805/post/912cbf2f-c90f-4ab4-b4ec-f7c7f5cd5a56/image.png)

먼저, 핸들러를 추상 클래스 또는 인터페이스로 정의한다. (다음 핸들러에 대한 필드가 필요하므로 추상 클래스를 사용하자.)
```java
public abstract class RequestHandler {

    private RequestHandler nextHandler;

    public RequestHandler(RequestHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void handle(Request request) {
        if (nextHandler != null) {
            nextHandler.handle(request);
        }
    }
}
```

추상 클래스 내에서 다음 핸들러에게 위임하는 구조를 만들어 두었다.  
이를 상속받아 구현한 실제 핸들러들은 다음과 같다. 각 구체적 핸들러 내에선 특정 조건을 거쳐 아무것도 안하고 다음 핸들러에게 위임할 수도 있다.
```java
public class PrintRequestHandler extends RequestHandler {

    public PrintRequestHandler(RequestHandler nextHandler) {
        super(nextHandler);
    }

    @Override
    public void handle(Request request) {
        System.out.println(request.getBody());
        super.handle(request);
    }
}
```

```java
public class LoggingRequestHandler extends RequestHandler {

    public LoggingRequestHandler(RequestHandler nextHandler) {
        super(nextHandler);
    }

    @Override
    public void handle(Request request) {
        System.out.println("로깅");
        super.handle(request);
    }
}
```

```java
public class AuthRequestHandler extends RequestHandler {

    public AuthRequestHandler(RequestHandler nextHandler) {
        super(nextHandler);
    }

    @Override
    public void handle(Request request) {
        System.out.println("인증이 되었는가?");
        super.handle(request);
    }
}
```

클라이언트는 다음과 같이 변경된다. 체인을 구성한 부분에 대해 주목하자.
```java
public class Client {

    private RequestHandler requestHandler;

    public Client(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public void doWork() {
        Request request = new Request("이번 놀이는 뽑기입니다.");
        requestHandler.handle(request);
    }

    public static void main(String[] args) {
        RequestHandler chain = new AuthRequestHandler(new LoggingRequestHandler(new PrintRequestHandler(null)));
        Client client = new Client(chain);
        client.doWork();
    }
}
```

## 책임 연쇄 패턴의 장점과 단점
- 장점
  - 클라이언트 코드를 변경하지 않고 새로운 핸들러를 체인에 추가할 수 있다.
  - 각각의 체인은 자신이 해야하는 일만 한다.
  - 체인을 다양한 방법으로 구성할 수 있다.
- 단점
  - 디버깅이 조금 어렵다.

## Java와 Spring에서의 활용 예시
- Java
  - Servlet Filter
- Spring
  - Spring Security Filter

![](https://velog.velcdn.com/images/songs4805/post/f5a87652-5534-4f49-8a96-1062116750ce/image.png)
