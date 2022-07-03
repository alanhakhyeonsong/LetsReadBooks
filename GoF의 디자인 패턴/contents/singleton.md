# 싱글톤(Singleton) 패턴
의도: 오직 한 개의 클래스 인스턴스만을 갖도록 보장하고, 이에 대한 전역적인 접근점을 제공한다.

- 시스템 런타임, 환경 세팅에 대한 정보 등, 인스턴스가 여러개 일 때 문제가 생길 수 있는 경우가 있다.  
→ **인스턴스를 오직 한개만 만들어 제공하는 클래스가 필요하다.**

<p align="center">
  <img src="https://velog.velcdn.com/images/songs4805/post/67838546-b84c-4e82-8be3-ad961abf16db/image.png"/>
</p>

## 구현
```java
// private 생성자에 static 메소드로 구현
public class Settings {
    private static Settings instance;

    private Settings() {}

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }
}
```
위처럼 구현하면 서로 다른 쓰레드가 `getInsatnce()`를 호출시, if 조건을 검사하는 부분부터 동시 접근이 가능하고 그 결과 서로 다른 인스턴스를 가질 수 있는 문제가 생긴다.

```java
// 동기화를 사용해 멀티쓰레드 환경에 안전하게 만드는 방법
public class Settings {
    private static Settings instance;

    private Settings() {}

    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }
}
```
이 방식은 멀티쓰레드 환경엔 안전하지만, `synchronized` 키워드 때문에 매번 동기화 처리를 하게 되어 성능 이슈가 발생할 수 있다.  
참고로 `getInstance()` 메소드 동기화시 사용하는 락(lock)은 `static` 이기 때문에 클래스 단위의 락이다. 인스턴스의 락이면 동기화 시 하나의 객체를 보장할 수 없다. Java의 동기화 블럭 처리는 `synchronized` 키워드가 붙은 메소드들만 락을 공유한다.

```java
// 이른 초기화(eager initialization)를 사용하는 방법
public class Settings {
    private static final Settings INSTANCE = new Settings();

    private Settings() {}

    public static Settings getInstance() {
        return INSTANCE; // 쓰레드 safe 함
    }
}
```
객체를 꼭 나중에 만들지 않아도 되고, 해당 객체 생성 비용이 비싸지 않다면 위와 같은 이른 초기화를 사용해도 좋다. 다만, 메모리 점유율이 크면서 애플리케이션에서 한 번도 안쓰인다면 단점으로 작용한다. 만약 생성자에서 checked 예외를 던진다면 위 코드를 try-catch로 예외 처리를 하는 식으로 바꿔야 한다. 그렇지 않다면 이른 초기화를 할 수 없다.

```java
// double checked locking
public class Settings {
    private static volatile Settings instance;

    private Settings() {}

    public static Settings getInstance() {
        if (instance == null) {
            synchronized (Settings.class) { // 다른 쓰레드 동시 진입 시 이미 만들어진 인스턴스를 체크함
                if (instance == null) {
                    instance = new Settings();
                }
            }
        }
        return instance;
    }
}
```
double checked locking 방식으로 구현하는데, `volatile`을 사용하여 가장 최신의 객체를 반환하도록 구현한다. `volatile`은 Java 1.5 부터만 동작한다.

```java
// static inner 클래스를 사용하는 방법.
// 효율적인 지연 초기화
public class Settings {
    private Settings() {}

    private static class SettingsHolder {
        private static final Settings SETTINGS = new Settings();
    }

    public static Settings getInstance() {
        return SettingsHolder.SETTINGS;
    }
}
```
// Java의 클래스 로더 정책에 대해 추가로 학습하자.  
내부 정적 클래스는 다른 클래스처럼 최초 사용될 때 로딩된다. inner 클래스가 로딩되는 시점이 해당 클래스를 사용하는 static 메소드가 호출 됐을 때로 미뤄지기 때문에 일반적인 static 필드일 때와 차이가 있다는 점을 생각하자.

## 싱글톤 패턴 구현을 깨트리는 방법
### 리플렉션 사용
```java
public class App {
    public static void main(String[] args)
          throws NoSuchMethodException, InvocationTargetException, InstantiationException {
        Settings settings = Settings.getInstance();

        Constructor<Settings> constuctor = Settings.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Settings settings1 = constructor.newInstance();

        System.out.println(settings == settings1);  // false
    }
}
```
리플렉션의 경우 대응할 방법이 없다.

### 직렬화, 역직렬화 사용
```java
public class Settings implements Serializable {
    //...
}

public class App {
    public static void main(String[] args)
          throws IOException, ClassNotFoundException {
        Settings settings = Settings.getInstance();

        try (ObjectOutput out = new ObjectOutputStream(new FileOutputStream("settings.obj"))) {
            out.writeObject(settings);
        }

        try (ObjectInput in = new ObjectInputStream(new FileInputStream("settings.obj"))) {
            settings1 = (Settings) in.readObject();
        }

        System.out.println(settings == settings1);  // false
    }
}
```
역직렬화 대응 방안은 `Serializable`을 구현한 Settings 클래스 내부에서 다음과 같이 `readResolve()` 메서드를 추가한다.

```java
public class Settings implements Serializable {
    //...

    // 역직렬화시 반드시 getInstance()를 호출하도록 만든다.
    protected Object readResolve() {
        return getInstance();  // 방어적 구현
    }
}
```

### enum 사용으로 안전하게 싱글톤 패턴을 구현하자.
```java
// enum을 사용하는 방법
public enum Settings {
    INSTANCE;
}
```
enum을 사용하게 되면 리플렉션에 안전한 코드가 된다.

1. enum 타입의 인스턴스를 리플렉션을 통해 만들 수 있는가?  
→ 만들 수 없다. enum 타입의 클래스는 리플랙션을 통해 만들 수 없도록 제한한다.
 
2. enum으로 싱글톤 타입을 구현할 때의 단점은?  
→ 단점은 이른 초기화와 같이 미리 만들어진다는 것이다. 그리고 상속을 사용할 수 없다.
 
3. 직렬화 & 역직렬화 시에 별도로 구현해야 하는 메소드가 있는가?  
→ 별다른 장치가 없어도 Enum 클래스는 직렬화 & 역직렬화가 된다. 그러나, `getResolves()` 구현시 역직렬화시 변경을 가할 수 있다.