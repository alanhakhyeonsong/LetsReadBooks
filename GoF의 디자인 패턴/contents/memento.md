# 메멘토(Memento) 패턴
의도: 캡슐화를 위배하지 않은 채 어떤 객체의 내부 상태를 잡아내고 실체화시켜 둠으로써, 이후 해당 객체가 그 상태로 되돌아올 수 있도록 한다.

캡슐화를 유지하면서 객체 내부 상태를 외부에 저장하는 방법.
- 객체 상태를 외부에 저장했다가 해당 상태로 다시 복구할 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/6d0e41e9-3987-43dc-a3b2-296217dde98d/image.png)

- `Memento`: 원조본 객체의 내부 상태를 저장한다. 메멘토는 원조본 객체의 내부 상태를 필요한 만큼 저장해둔다. 메멘토는 원조본 객체를 제외한 다른 객체는 자신에게 접근할 수 없도록 막는다. 그래서 `Memento` 클래스에는 사실상 두 종류의 인터페이스가 있다. 관리 책임을 갖는 `Caretaker` 클래스는 `Memento`에 대한 제한 범위 인터페이스만을 볼 수 있다. 즉, `Caretaker`는 메멘토를 다른 객체에게 넘겨줄 수만 있다. 이에 반해, `Originator` 클래스에게는 광범위 인터페이스가 보인다. 즉, 자신의 상태를 이전 상태로 복원하기 위해 필요한 모든 자료에 접근하게 해주는 인터페이스이다. 이상적으로는 메멘토를 생성하는 원조본 객체만이 메멘토의 내부 상태에 접근할 수 있는 권한을 갖는다.
- `Originator`: 원조본 객체이다. 메멘토를 생성하여 현재 객체의 상태를 저장하고 메멘토를 사용하여 내부 상태를 복원한다.
- `Caretaker`: 메멘토의 보관을 책임지는 보관자이다. 메멘토의 내용을 검사하거나 그 내용을 건드리진 않는다.

협력 방법
- `Caretaker` 객체는 원조본 객체에 메멘토 객체를 요청한다. 또 요청한 시간을 저장하며, 받은 메멘토 객체를 다시 원조본 객체에게 돌려주는데, 이를 상호작용 다이어그램으로 나타내면 다음과 같다.
![](https://velog.velcdn.com/images/songs4805/post/972ecb86-b7a9-425d-a761-a3d0e40a8951/image.png)  
보관자 객체는 메멘토 객체를 원조본 객체에 전달하지 않을 수도 있다. 원조본 객체가 이전 상태로 돌아갈 필요가 없을 때는 전달할 필요가 없기 때문이다.
- 메멘토 객체는 수동적이다. 메멘토 객체를 생성한 원조본 객체만이 상태를 설정하고 읽어올 수 있다.

## 활용성
메멘토 패턴은 다음의 경우에 사용한다.
- 어떤 객체의 상태에 대한 스냅샷(몇 개의 일부)을 저장한 후 나중에 이 상태로 복구해야 할 때
- 상태를 얻는 데 필요한 직접적인 인터페이스를 두면 그 객체의 구현 세부 사항이 드러날 수 밖에 없고, 이것으로 객체의 캡슐화가 깨질 때

## 구현
### 메멘토 패턴 적용 전
```java
import java.io.Serializable;

public class Game implements Serializable {

    private int redTeamScore;

    private int blueTeamScore;

    public int getRedTeamScore() {
        return redTeamScore;
    }

    public void setRedTeamScore(int redTeamScore) {
        this.redTeamScore = redTeamScore;
    }

    public int getBlueTeamScore() {
        return blueTeamScore;
    }

    public void setBlueTeamScore(int blueTeamScore) {
        this.blueTeamScore = blueTeamScore;
    }
}
```

```java
public class Client {

    public static void main(String[] args) {
        Game game = new Game();
        game.setRedTeamScore(10);
        game.setBlueTeamScore(20);

        int blueTeamScore = game.getBlueTeamScore();
        int redTeamScore = game.getRedTeamScore();

        Game restoredGame = new Game();
        restoredGame.setBlueTeamScore(blueTeamScore);
        restoredGame.setRedTeamScore(redTeamScore);
    }
}
```
위 코드는 캡슐화가 깨진 코드이다.

### 메멘토 패턴 적용 후
```java
public class Game {

    private int redTeamScore;

    private int blueTeamScore;

    public int getRedTeamScore() {
        return redTeamScore;
    }

    public void setRedTeamScore(int redTeamScore) {
        this.redTeamScore = redTeamScore;
    }

    public int getBlueTeamScore() {
        return blueTeamScore;
    }

    public void setBlueTeamScore(int blueTeamScore) {
        this.blueTeamScore = blueTeamScore;
    }

    public GameSave save() {
        return new GameSave(this.blueTeamScore, this.redTeamScore);
    }

    public void restore(GameSave gameSave) {
        this.blueTeamScore = gameSave.getBlueTeamScore();
        this.redTeamScore = gameSave.getRedTeamScore();
    }
}
```

```java
public final class GameSave {

    private final int blueTeamScore;

    private final int redTeamScore;

    public GameSave(int blueTeamScore, int redTeamScore) {
        this.blueTeamScore = blueTeamScore;
        this.redTeamScore = redTeamScore;
    }

    public int getBlueTeamScore() {
        return blueTeamScore;
    }

    public int getRedTeamScore() {
        return redTeamScore;
    }
}
```

```java
public class Client {

    public static void main(String[] args) {
        Game game = new Game();
        game.setBlueTeamScore(10);
        game.setRedTeamScore(20);

        GameSave save = game.save();

        game.setBlueTeamScore(12);
        game.setRedTeamScore(22);

        game.restore(save);

        System.out.println(game.getBlueTeamScore());
        System.out.println(game.getRedTeamScore());
    }
}
```
객체 내부 상태를 알지 않고도 저장했다 복원할 수 있게 되었다.

## 메멘토 패턴의 장점과 단점
- 장점
  - 캡슐화를 지키면서 객체 상태 스냅샷을 만들 수 있다.
  - 객체 상태를 저장하고 또는 복원하는 역할을 CareTaker에게 위임할 수 있다.
  - 객체 상태가 바뀌어도 클라이언트 코드는 변경되지 않는다.
- 단점
  - 많은 정보를 저장하는 Memento를 자주 생성하는 경우 메모리 사용량에 많은 영향을 줄 수 있다.

## Java와 Spring에서의 활용 예시
### Java
- 객체 직렬화, `java.io.Serializable`
- `java.util.Date`