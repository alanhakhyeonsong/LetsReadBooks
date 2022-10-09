# 옵저버(Observer) 패턴
의도: 객체 사이에 일 대 다의 의존 관계를 정의해두어, 어떤 객체의 상태가 변할 때 그 객체에 의존성을 가진 다른 객체들이 그 변화를 통지받고 자동으로 갱신될 수 있게 만든다.

다수의 객체가 특정 객체 상태 변화를 감지하고 알림을 받는 패턴.
- 발생(publish)-구독(subscribe) 패턴을 구현할 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/95e3651a-af60-4469-9f1a-fbedadf09400/image.png)

- `Subject`: 감시자들을 알고 있는 주체이다. 임의 개수의 감시자 객체는 주체를 감시할 수 있다. 주체는 감시자 객체를 붙이거나 떼는 데 필요한 인터페이스를 제공한다.
- `Observer`: 주체에 생긴 변화에 관심 있는 객체를 갱신하는 데 필요한 인터페이스를 정의한다. 이로써 주체의 변경에 따라 변화되어야 하는 객체들의 일관성을 유지한다.
- `ConcreteObserver`: `ConcreteSubject` 객체에 대한 참조자를 관리한다. 주체의 상태와 일관성을 유지해야 하는 상태를 저장한다. 주체의 상태와 감시자의 상태를 일관되게 유지하는 데 사용하는 갱신 인터페이스를 구현한다.
- `ConcreteSubject`: `ConcreteObserver` 객체에게 알려주어야 하는 상태를 저장한다. 또한 이 상태가 변경될 때 감시자에게 변경을 통보한다.

협력 방법
- `ConcreteSubject`는 `Observer`의 상태와 자신의 상태가 달라지는 변경이 발생할 때마다 감시자에게 통보한다.
- `ConcreteSubject`에서 변경이 통보된 후, `ConcreteObserver`는 필요한 정보를 주체에게 질의하여 얻어온다. `ConcreteObserver`는 이 정보를 이용해서 주체의 상태와 자신의 상태를 일치시킨다.

## 활용성
다음 상황 중 어느 한 가지에 속하면 감시자 패턴을 사용한다.
- 어떤 추상 개념이 두 가지 양상을 갖고 하나가 다른 하나에 종속적일 때. 각 양상을 별도의 객체로 캡슐화하여 이들 각각을 재사용할 수 있다.
- 한 객체에 가해진 변경으로 다른 객체를 변경해야 하고, 프로그래머들은 얼마나 많은 객체들이 변경되어야 하는지 몰라도 될 때
- 어떤 객체가 다른 객체에 자신의 변화를 통보할 수 있는데, 그 변화에 관심있어 하는 객체들이 누구인지에 대한 가정 없이도 그러한 통보가 될 때

## 구현
### 옵저버 패턴 적용 전
```java
public class Client {

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();

        User user1 = new User(chatServer);
        user1.sendMessage("디자인패턴", "이번엔 옵저버 패턴입니다.");
        user1.sendMessage("롤드컵2021", "LCK 화이팅!");

        User user2 = new User(chatServer);
        System.out.println(user2.getMessage("디자인패턴"));

        user1.sendMessage("디자인패턴", "예제 코드 보는 중..");
        System.out.println(user2.getMessage("디자인패턴"));
    }
}
```

```java
import java.util.List;

public class User {

    private ChatServer chatServer;

    public User(ChatServer chatServer) {
        this.chatServer = chatServer;
    }


    public void sendMessage(String subject, String message) {
        chatServer.add(subject, message);
    }

    public List<String> getMessage(String subject) {
        return chatServer.getMessage(subject);
    }
}
```

```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {

    private Map<String, List<String>> messages;

    public ChatServer() {
        this.messages = new HashMap<>();
    }


    public void add(String subject, String message) {
        if (messages.containsKey(subject)) {
            messages.get(subject).add(message);
        } else {
            List<String> messageList = new ArrayList<>();
            messageList.add(message);
            messages.put(subject, messageList);
        }
    }

    public List<String> getMessage(String subject) {
        return messages.get(subject);
    }
}
```


### 옵저버 패턴 적용 후
옵저버 인터페이스는 다음과 같다.
```java
public interface Subscriber {

    void handleMessage(String message);
}
```

```java
public class User implements Subscriber {

    private String name;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void handleMessage(String message) {
        System.out.println(message);
    }
}
```

`Subject`에 해당하는 `ChatServer`는 다음과 같다.
```java
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {

    private Map<String, List<Subscriber>> subscribers = new HashMap<>();

    public void register(String subject, Subscriber subscriber) {
        if (this.subscribers.containsKey(subject)) {
            this.subscribers.get(subject).add(subscriber);
        } else {
            List<Subscriber> list = new ArrayList<>();
            list.add(subscriber);
            this.subscribers.put(subject, list);
        }
    }

    public void unregister(String subject, Subscriber subscriber) {
        if (this.subscribers.containsKey(subject)) {
            this.subscribers.get(subject).remove(subscriber);
        }
    }

    public void sendMessage(User user, String subject, String message) {
        if (this.subscribers.containsKey(subject)) {
            String userMessage = user.getName() + ": " + message;
            this.subscribers.get(subject).forEach(s -> s.handleMessage(userMessage));
        }
    }
}
```

```java
public class Client {

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        User user1 = new User("Ramos");
        User user2 = new User("whiteship");

        chatServer.register("오징어게임", user1);
        chatServer.register("오징어게임", user2);

        chatServer.register("디자인패턴", user1);

        chatServer.sendMessage(user1, "오징어게임", "아.. 이름이 기억났어.. 일남이야.. 오일남");
        chatServer.sendMessage(user2, "디자인패턴", "옵저버 패턴으로 만든 채팅");

        chatServer.unregister("디자인패턴", user2);

        chatServer.sendMessage(user2, "디자인패턴", "옵저버 패턴 장, 단점 보는 중");
    }
}
```

## 옵저버 패턴의 장점과 단점
- 장점
  - 상태를 변경하는 객체(publisher)와 변경을 감지하는 객체(subscriber)의 관계를 느슨하게 유지할 수 있다.
  - Subject의 상태 변경을 주기적으로 조회하지 않고 자동으로 감지할 수 있다.
  - 런타임 중에 새로운 옵저버를 추가하거나 제거할 수 있다.
- 단점
  - 복잡도가 증가한다.
  - 다수의 Observer 객체를 등록 이후 해제하지 않는다면 memory leak가 발생할 수도 있다. (GC 매커니즘과 `WeakRefernece`에 대해 참고해볼 것)

## Java와 Spring에서의 활용 예시
### Java
- `Observable`과 `Observer` (Java 9부터 deprecated)
- Java 9 이후부터는
  - `PropertyChangerListener`, `PropertyChangeEvent`
  - `Flow API`
- SAX (Simple API for XML) 라이브러리

### Spring
- `ApplicationContext`와 `ApplicationEvent`