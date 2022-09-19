# 커맨드(Command) 패턴
의도: 요청 자체를 캡슐화하는 것이다. 이를 통해 요청이 서로 다른 사용자를 매개변수로 만들고, 요청을 대기시키거나 로깅하며, 되돌릴 수 있는 연산을 지원한다.

요청을 캡슐화 하여 호출자(invoker)와 수신자(receiver)를 분리하는 패턴
- 요청을 처리하는 방법이 바뀌더라도, 호출자의 코드는 변경되지 않는다.

![](https://velog.velcdn.com/images/songs4805/post/cd407133-98da-4d00-bc4b-05fe5da4008c/image.png)

- `Command`: 연산 수행에 필요한 인터페이스를 선언한다.
- `ConcreteCommand`: `Receiver` 객체와 액션 간의 연결성을 정의한다. 또한 처리 객체에 정의된 연산을 호출하도록 `Execute`를 구현한다.
- `Client`: `ConcreteCommand` 객체를 생성하고 처리 객체로 정의한다.
- `Invoker`: 명령어에 처리를 수행할 것을 요청한다.
- `Receiver`: 요청에 관련된 연산 수행 방법을 알고 있다. 어떤 클래스도 요청 수신자로서 동작할 수 있다.

협력 방법
- 사용자는 `ConcreteCommand` 객체를 생성하고 이를 수신자로 지정한다.
- `Invoker` 클래스는 `ConcreteCommand` 객체를 저장한다.
- `Invoker` 클래스는 command에 정의된 `Execute()`를 호출하여 요청을 발생시킨다. 명령어가 취소 가능한 것이라면 `ConcreteCommand`는 이전에 `Execute()` 호출 전 상태의 취소 처리를 위해 저장한다.
- `ConcreteCommand` 객체는 요청을 실제 처리할 객체에 정의된 연산을 호출한다.

## 활용성
커맨드 패턴은 다음과 같은 일을 하고 싶을 때 사용한다.
- 수행할 동작을 객체로 매개변수화하고자 할 때. 절차지향 프로그램에서는 이를 **콜백(callback) 함수**, 즉 어딘가 등록되었다가 나중에 호출되는 함수를 사용해서 이러한 매개변수화를 표현할 수 있다. 명령 패턴은 콜백을 객체지향 방식으로 나타낸 것이다.
- 서로 다른 시간에 요청을 명시하고, 저장하며, 실행하고 싶을 때. Command 객체는 원래의 요청과 다른 생명주기가 있다. 요청을 받아 처리하는 객체가 주소 지정 방식과는 독립적으로 표현될 수 있다면, Command 객체를 다른 프로세스에게 넘겨주고 거기서 해당 처리를 진행하게 할 수 있다.
- 실행 취소 기능을 지원하고 싶을 때. Command의 `Execute()` 연산은 상태를 저장할 수 있는데, 이를 이용해 지금까지 얻은 결과를 바꿀 수 있다. 이를 위해 `Unexecute()` 연산을 Command 클래스의 인터페이스에 추가한다. 실행된 명령어를 모두 기록해 두었다가 이 리스트를역으로 탐색해서 다시 `Unexecute()`를 수행하게 한다. `Execute()`와 `Unexecute()` 연산의 반복 사용을 통해 수행과 취소를 무한 반복할 수 있다.
- 시스템이 고장 났을 때 재적용이 가능하도록 변경 과정에 대한 로깅을 지원하고 싶을 때. Command 인터페이스를 확장해서 `load()`와 `store()` 연산을 정의하면 상태의 변화를 지속적 저장소에 저장해 둘 수 있다. 시스템 장애가 발생했을 때 해당 저장소에서 저장된 명령어를 읽어 다시 `Execute()` 연산을 통해 재실행하면 된다.

## 구현
### 커맨드 패턴 적용 전
```java
public class Button {

    private Light light;

    public Button(Light light) {
        this.light = light;
    }

    public void press() {
        light.off();
    }

    public static void main(String[] args) {
        Button button = new Button(new Light());
        button.press();
        button.press();
        button.press();
        button.press();
    }
}
```

```java
public class Light {

    private boolean isOn;

    public void on() {
        System.out.println("불을 켭니다.");
        this.isOn = true;
    }

    public void off() {
        System.out.println("불을 끕니다.");
        this.isOn = false;
    }

    public boolean isOn() {
        return this.isOn;
    }
}
```

```java
public class Game {

    private boolean isStarted;

    public void start() {
        System.out.println("게임을 시작합니다.");
        this.isStarted = true;
    }

    public void end() {
        System.out.println("게임을 종료합니다.");
        this.isStarted = false;
    }

    public boolean isStarted() {
        return isStarted;
    }
}
```

```java
public class MyApp {

    private Game game;

    public MyApp(Game game) {
        this.game = game;
    }

    public void press() {
        game.start();
    }

    public static void main(String[] args) {
        Button button = new Button(new Light());
        button.press();
        button.press();
        button.press();
        button.press();
    }
}
```

`Button`(invoker) 클릭 시 불을 끼거나 꺼야한다면 `press()` 내부를 변경해야 한다. `Light`(receiver)가 바뀌면 요청을 보내는 인보커 또한 바뀌게 된다.

또한 코드의 변경이 자주 일어나고 요청을 보내는 쪽에서 중복이 자주 일어나는 이유는 invoker와 receiver의 관계가 타이트하게 연결되어 있기 때문이다.

### 커맨드 패턴 적용 후
![](https://velog.velcdn.com/images/songs4805/post/4cb3b30c-510c-4c04-9af4-965c88dff6b1/image.png)

먼저 Command를 인터페이스로 다음과 같이 정의한다.
```java
public interface Command {

    void execute();

    void undo();

}
```

invoker에 해당하는 Button은 다음과 같이 변경되었다.
```java
public class Button {

    private Stack<Command> commands = new Stack<>();

    public void press(Command command) {
        command.execute();
        commands.push(command);
    }

    public void undo() {
        if (!commands.isEmpty()) {
            Command command = commands.pop();
            command.undo();
        }
    }

    public static void main(String[] args) {
        Button button = new Button();
        button.press(new GameStartCommand(new Game()));
        button.press(new LightOnCommand(new Light()));
        button.undo();
        button.undo();
    }
}
```

ConcreteCommand에 해당하는 실제 구현체들은 다음과 같다.
```java
public class GameEndCommand implements Command {

    private Game game;

    public GameEndCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute() {
        game.end();
    }

    @Override
    public void undo() {
        new GameStartCommand(this.game).execute();
    }
}
```

```java
public class GameStartCommand implements Command {

    private Game game;

    public GameStartCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute() {
        game.start();
    }

    @Override
    public void undo() {
        new GameEndCommand(this.game).execute();
    }
}
```

```java
public class LightOffCommand implements Command {

    private Light light;

    public LightOffCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.off();
    }

    @Override
    public void undo() {
        new LightOnCommand(this.light).execute();
    }
}
```

```java
public class LightOnCommand implements Command {

    private Light light;

    public LightOnCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.on();
    }

    @Override
    public void undo() {
        new LightOffCommand(this.light).execute();
    }
}
```

```java
public class MyApp {

    private Command command;

    public MyApp(Command command) {
        this.command = command;
    }

    public void press() {
        command.execute();
    }

    public static void main(String[] args) {
        MyApp myApp = new MyApp(new GameStartCommand(new Game()));
    }
}
```

## 커맨드 패턴의 장점과 단점
- 장점
  - 기존 코드를 변경하지 않고 새로운 커맨드를 만들 수 있다.
  - 수신자의 코드가 변경되어도 호출자의 코드는 변경되지 않는다.
  - 커맨드 객체를 로깅, DB에 저장, 네트워크로 전송 하는 등 다양한 방법으로 활용할 수도 있다.
- 단점
  - 코드가 복잡하고 클래스가 많아진다.

## Java와 Spring에서의 활용 예시
### Java
- `Runnable`
- 람다
- 메소드 레퍼런스

### Spring
- `SimpleJdbcInsert`
- `SimpleJdbcCall`