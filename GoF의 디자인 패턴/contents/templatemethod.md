# 템플릿 메서드(Template Method) 패턴
의도: 객체의 연산에는 알고리즘의 뼈대만을 정의하고 각 단계에서 수행할 구체적 처리는 서브클래스 쪽으로 미룬다. 알고리즘의 구조 자체는 그대로 놔둔 채 알고리즘 각 단계 처리를 서브클래스에서 재정의할 수 있게 한다.

알고리즘 구조를 서브 클래스가 확장할 수 있도록 템플릿으로 제공하는 방법.
- 추상 클래스는 템플릿을 제공하고 하위 클래스는 구체적인 알고리즘을 제공한다.

![](https://velog.velcdn.com/images/songs4805/post/59bd199e-eda4-4d5b-8348-5af03e911f14/image.png)

- `AbstractClass`: 서브클래스들이 재정의를 통해 구현해야 하는 알고리즘 처리 단계 내의 기본 연산을 정의한다. 그리고 알고리즘의 뼈대를 정의하는 템플릿 메서드를 구현한다. 템플릿 메서드는 `AbstractClass`에 정의된 연산 또는 다른 객체 연산뿐만 아니라 기본 연산도 호출한다.
- `ConcreteClass`: 서브클래스마다 달라진 알고리즘 처리 단계를 수행하기 위한 기본 연산을 구현한다.

협력 방법: `ConcreteClass`는 `AbstractClass`를 통해 알고리즘의 변하지 않는 처리 단계를 구현한다.

## 활용성
템플릿 메서드 패턴은 다음의 경우에 사용해야 한다.
- 어떤 한 알고리즘을 이루는 부분 중 변하지 않는 부분을 한 번 정의해놓고 다양해질 수 있는 부분은 서브클래스에서 정의할 수 있도록 남겨두고자 할 때
- 서브클래스 사이의 공통적인 행동을 추출하여 하나의 공통 클래스에 몰아둠으로써 코드 중복을 피하고 싶을 때. 이는 일반화를 위한 리팩토링의 좋은 예시이다. 먼저, 기존 코드에서 나타나는 차이점을 뽑아 이를 별도의 새로운 연산들로 구분해 놓는다. 그런 뒤 달라진 코드 부분을 이 새로운 연산을 호출하는 템플릿 메서드로 대체하는 것이다.
- 서브클래스의 확장을 제어할 수 있다. 템플릿 메서드가 어떤 특정한 시점에 "훅(hook)" 연산을 호출하도록 정의함으로써, 그 특정 시점에만 확장되도록 한다.

## 구현
### 템플릿 메서드 패턴 적용 전
```java
public class Client {

    public static void main(String[] args) {
        FileProcessor fileProcessor = new FileProcessor("number.txt");
        int result = fileProcessor.process();
        System.out.println(result);
    }
}
```

```java
public class FileProcessor {

    private String path;
    public FileProcessor(String path) {
        this.path = path;
    }

    public int process() {
        try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
            int result = 0;
            String line = null;
            while((line = reader.readLine()) != null) {
                result += Integer.parseInt(line);
            }
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException(path + "에 해당하는 파일이 없습니다.", e);
        }
    }
}
```

```java
public class MultuplyFileProcessor {

    private String path;
    public MultuplyFileProcessor(String path) {
        this.path = path;
    }

    public int process() {
        try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
            int result = 0;
            String line = null;
            while((line = reader.readLine()) != null) {
                result *= Integer.parseInt(line);
            }
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException(path + "에 해당하는 파일이 없습니다.", e);
        }
    }
}
```
위 예제 코드에선 중복되는 코드가 상당량이다.

### 템플릿 메서드 패턴 적용 후
```java
public class Client {

    public static void main(String[] args) {
        FileProcessor fileProcessor = new Multiply("number.txt");
        int result = fileProcessor.process((sum, number) -> sum += number);
        System.out.println(result);
    }
}
```

템플릿 메소드 패턴을 적용하면 다음과 같다. 추상 클래스를 적용한 뒤 `getResult`를 재정의하도록 한다.
```java
public abstract class FileProcessor {

    private String path;
    public FileProcessor(String path) {
        this.path = path;
    }

    public final int process(Operator operator) {
        try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
            int result = 0;
            String line = null;
            while((line = reader.readLine()) != null) {
                result = getResult(result, Integer.parseInt(line));
            }
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException(path + "에 해당하는 파일이 없습니다.", e);
        }
    }

    protected abstract int getResult(int result, int number);

}
```

```java
public class Multiply extends FileProcessor {
    public Multiply(String path) {
        super(path);
    }

    @Override
    protected int getResult(int result, int number) {
        return result *= number;
    }

}
```

템플릿 콜백 패턴을 적용하게 되면 다음과 같다.
```java
public interface Operator {

    abstract int getResult(int result, int number);
}
```

```java
public class Plus implements Operator {
    @Override
    public int getResult(int result, int number) {
        return result += number;
    }
}
```

## 템플릿 콜백(Template-Callback) 패턴
콜백으로 상속 대신 위임을 사용하는 템플릿 패턴.
- 상속 대신 익명 내부 클래스 또는 람다 표현식을 활용할 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/611a9684-eb13-40dd-bfd0-a5783d599375/image.png)

## 템플릿 메소드 패턴의 장점과 단점
- 장점
  - 템플릿 코드를 재사용하고 중복 코드를 줄일 수 있다.
  - 템플릿 코드를 변경하지 않고 상속을 받아서 구체적인 알고리즘만 변경할 수 있다.
- 단점
  - 리스코프 치환 원칙을 위반할 수도 있다.
  - 알고리즘 구조가 복잡할수록 템플릿을 유지하기 어려워진다.

## Java와 Spring에서의 활용 예시
### Java
- `HttpServlet`

### Spring
- 템플릿 메소드 패턴
  - `Configuration`
- 템플릿 콜백 패턴
  - `JdbcTemplate`
  - `RestTemplate`
  - ...

