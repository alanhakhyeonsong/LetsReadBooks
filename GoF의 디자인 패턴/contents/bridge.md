# 브릿지(Bridge) 패턴
의도: 구현에서 추상을 분리하여, 이들이 독립적으로 다양성을 가질 수 있도록 한다.

컴포지션을 사용해서 추상적인 것과 구체적인 것을 구분짓는다. 이 둘 사이를 연결짓는 브릿지를 사용한다.
클라이언트는 추상적인 계층 구조를 사용하고 implementation을 간접적으로 사용한다.

많은 클래스들이 늘어날 수 있다.

- 하나의 계층 구조일 때 보다 각기 나누었을 때 독립적인 계층 구조로 발전 시킬 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/c7039f07-bc6f-4ca0-8742-42fc01a83da6/image.png)

- `Abstraction`: 추상적 개념에 대한 인터페이스를 제공하고 객체 구현자(`Implementation`)에 대한 참조자를 관리한다.
- `Refined Abstraction`: 추상적 개념에 정의된 인터페이스를 확장한다.
- `Implemetation`: 구현 클래스에 대한 인터페이스를 제공한다. 다시 말해, 실질적인 구현을 제공한 서브클래스들에 공통적인 연산의 시그니처만을 정의한다. 이 인터페이스는 `Abstraction` 클래스에 정의된 인터페이스에 정확하게 대응할 필요가 없다. 즉, 두 인터페이스는 서로 다른 형태일 수 있다. 일반적으로 `Implementation` 인터페이스는 기본적인 구현 연산을 수행하고, `Abstraction`은 더 추상화된 서비스 관점의 인터페이스를 제공한다.
- `Concrete Implementation`: `Implementation` 인터페이스를 구현하는 것으로 실제적인 구현 내용을 담았다.

## 활용성
브릿지 패턴은 다음과 같은 경우에 사용한다.
- 추상적 개념과 이에 대한 구현 사이의 지속적인 종속 관계를 피하고 싶을 때. 이를테면, 런타임에 구현 방법을 선택하거나 구현 내용을 변경하고 싶을 때가 이에 해당한다.
- 추상적 개념과 구현 모두가 독립적으로 서브클래싱을 통해 확장되어야 할 때. 이때, 브릿지 패턴은 개발자가 구현을 또 다른 추상적 개념과 연결할 수 있게 할 뿐 아니라, 각각을 독립적으로 확장 가능하게 한다.
- 추상적 개념에 대한 구현 내용을 변경하는 것이 다른 관련 프로그램에 아무런 영향을 주지 않아야 할 때. 즉, 추상적 개념에 해당하는 클래스를 사용하는 코드들은 구현 클래스가 변경되었다고 해서 다시 컴파일되지 않아야 한다.

## 구현
![](https://velog.velcdn.com/images/songs4805/post/63c3bc0c-13ba-4146-a9bd-061eb0952f0a/image.png)

```java
public abstract class App implements Champion {

    public static void main(String[] args) {
        Champion kda아리 = new 아리(new KDA());
        kda아리.skillQ();
        kda아리.skillW();

        Champion poolParty아리 = new 아리(new PoolParty());
        poolParty아리.skillR();
        poolParty아리.skillW();
    }
}
```

```java
public interface Champion extends Skin {

    void move();

    void skillQ();

    void skillW();

    void skillE();

    void skillR();

}
```

```java
public interface Skin {
    String getName();
}
```

```java
public class DefaultChampion implements Champion {

    private Skin skin;

    private String name;

    public DefaultChampion(Skin skin, String name) {
        this.skin = skin;
        this.name = name;
    }

    @Override
    public void move() {
        System.out.printf("%s %s move\n", skin.getName(), this.name);
    }

    @Override
    public void skillQ() {
        System.out.printf("%s %s Q\n", skin.getName(), this.name);
    }

    @Override
    public void skillW() {
        System.out.printf("%s %s W\n", skin.getName(), this.name);
    }

    @Override
    public void skillE() {
        System.out.printf("%s %s E\n", skin.getName(), this.name);
    }

    @Override
    public void skillR() {
        System.out.printf("%s %s R\n", skin.getName(), this.name);
    }

    @Override
    public String getName() {
        return null;
    }
}
```

```java
public class 아칼리 extends DefaultChampion {

    public 아칼리(Skin skin) {
        super(skin, "아칼리");
    }
}
```

```java
public class 아리 extends DefaultChampion {

    public 아리(Skin skin) {
        super(skin, "아리");
    }
}
```

```java
public class PoolParty implements Skin {
    @Override
    public String getName() {
        return "PoolParty";
    }
}
```

```java
public class KDA implements Skin {
    @Override
    public String getName() {
        return "KDA";
    }
}
```

## 브릿지 패턴의 장점과 단점
- 장점
  - 추상적인 코드를 구체적인 코드 변경 없이도 독립적으로 확장할 수 있다. (OCP 원칙)
  - 추상적인 코드와 구체적인 코드를 분리하여 사용할 수 있다. (SRP 원칙)
- 단점
  - 계층 구조가 늘어나 복잡도가 증가할 수 있다.

## Java와 Spring에서의 활용 예시
### Java
- JDBC API, `DriverManager`와 `Driver`
- SLF4J, 로깅 퍼사드와 로거

### Spring
- Portable Service Abstraction