# 플라이웨이트(Flyweight) 패턴
의도: 공유를 통해 많은 수의 소립(fine-grained) 객체들을 효과적으로 지원한다.

객체를 가볍게 만들어 메모리 사용을 줄이는 패턴

- 자주 변하는 속성(또는 외적인 속성, extrinsit)과 변하지 않는 속성(또는 내적인 속성, intrinsit)을 분리하고 재사용하여 메모리 사용을 줄일 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/9294d954-c79c-4587-bb30-081f45776224/image.png)

- `Flyweight`: `Flyweight`가 받아들일 수 있고, 부가적 상태에서 동작해야 하는 인터페이스를 선언한다.
- `ConcreteFlyweight`: `Flyweight` 인터페이스를 구현하고 내부적으로 갖고 있어야 하는 본질적 상태에 대한 저장소를 정의한다. `ConcreteFlywieght` 객체는 공유할 수 있는 것이어야 한다. 그러므로 관리하는 어떤 상태라도 본질적인 것이어야 한다.
- `UnsharedConcreteFlyweight`: 모든 플라이웨이트 서브클래스들이 공유될 필요는 없다. `Flyweight` 인터페이스는 공유를 가능하게 하지만, 그것을 강요해서는 안된다. `UnsharedConcreteFlyweight` 객체가 `ConcreteFlyweight` 객체를 자신의 자식으로 갖는 것은 흔한 일이다.
- `FlyweightFactory`: 플라이웨이트 객체를 생성하고 관리하며, 플라이웨이트 객체가 제대로 공유되도록 보장한다. 사용자가 플라이웨이트 객체를 요청하면 `FlyweightFactory` 객체는 이미 존재하는 인스턴스를 제공하거나 만약 존재하지 않는다면 새로 생성한다.
- `Client`: 플라이웨이트 객체에 대한 참조자를 관리하며 플라이웨이트 객체의 부가적 상태를 저장한다.

## 활용성
플라이웨이트 패턴은 언제 사용하는가에 따라 그 효과가 달라진다. 다음의 경우에 사용할 수 있다.
- 응용프로그램이 대량의 객체를 사용해야 할 때
- 객체의 수가 너무 많아져 저장 비용이 너무 높아질 때
- 대부분의 객체 상태를 부가적인 것으로 만들 수 있을 때
- 부가적인 속성들을 제거한 후 객체들을 조사해보니 객체의 많은 묶음이 비교적 적은 수의 공유된 객체로 대체될 수 있을 때. 현재 서로 다른 객체로 간주한 이유는 이들 부가적인 속성 때문이었지 본질이 달랐던 것은 아닐 때
- 응용프로그램이 객체의 정체성에 의존하지 않을 때. 플라이웨이트 객체들은 공유 될 수 있음을 의미하는데, 식별자가 있다는 것은 서로 다른 객체로 구별해야 한다는 의미이므로 플라이웨이트 객체를 사용할 수 없다.

## 구현
### 플라이웨이트 패턴 적용 전
```java
public class Character {
    private char value;
    private String color;
    private String fontFamily;
    private int fontSize;

    public Character(char value, String color, String fontFamily, int fontSize) {
        this.value = value;
        this.color = color;
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
    }
}
```

```java
public class Client {
    public static void main(String[] args) {
        Character c1 = new Character('h', "white", "Nanum", 12);
        Character c2 = new Character('e', "white", "Nanum", 12);
        Character c3 = new Character('l', "white", "Nanum", 12);
        Character c4 = new Character('l', "white", "Nanum", 12);
        Character c5 = new Character('o', "white", "Nanum", 12);
    }
}
```

### 플라이웨이트 패턴 적용 후
![](https://velog.velcdn.com/images/songs4805/post/dc2c1069-277c-4acf-a427-8b801fa2661b/image.png)

```java
public class Character {
    private char value;
    private String color;
    private Font font;

    public Character(char value, String color, Font font) {
        this.value = value;
        this.color = color;
        this.font = font;
    }
}
```

`Font`를 플라이웨이트로 묶어둔다.
```java
public final class Font {
    final String family;
    final int size;

    public Font(String family, int size) {
        this.family = family;
        this.size = size;
    }

    public String getFamily() {
        return family;
    }

    public int getSize() {
        return size;
    }
}
```

플라이웨이트 팩토리는 다음과 같이 정의한다.
```java
public class FontFactory {

    private Map<String, Font> cache = new HashMap<>();

    public Font getFont(String font) {
        if (cache.containsKey(font)) {
            return cache.get(font);
        } else {
            String[] split = font.split(":");
            Font newFont = new Font(split[0], Integer.parseInt(split[1]));
            cache.put(font, newFont);
            return newFont;
        }
    }
}
```

```java
public class Client {
    public static void main(String[] args) {
        FontFactory fontFactory = new FontFactory();
        Character c1 = new Character('h', "white", fontFactory.getFont("nanum:12"));
        Character c2 = new Character('e', "white", fontFactory.getFont("nanum:12"));
        Character c3 = new Character('l', "white", fontFactory.getFont("nanum:12"));
    }
}
```

## 플라이웨이트 패턴의 장점과 단점
- 장점
  - 애플리케이션에서 사용하는 메모리를 줄일 수 있다.
- 단점
  - 코드의 복잡도가 증가한다.

## Java와 Spring에서의 활용 예시
### Java
- `Integer.valueOf(int)`
- 캐시를 제공한다.
- https://docs.oracle.com/javase/8/docs/api/java/lang/Integer.html#valueOf-int-