# 인터프리터(Interpreter) 패턴
의도: 어떤 언어에 대해, 그 언어의 문법에 대한 표현을 정의하면서 그것(표현)을 사용하여 해당 언어로 기술된 문장을 해석하는 해석자를 함께 정의한다.

자주 등장하는 문제를 간단한 언어로 정의하고 재사용하는 패턴
- 반복되는 문제 패턴을 언어 또는 문법으로 정의하고 확장할 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/2b357339-f3cc-4d96-b0a7-92d13a1ad60e/image.png)

- `Expression`: 추상 구문 트리에 속한 모든 노드에 해당하는 클래스들이 공통으로 가져야 할 `interpret()` 연산을 추상 연산으로 정의한다.
- `TerminalExpression`: 문법에 정의한 터미널 기호와 관련된 해석 방법을 구현한다. 문장을 구성하는 모든 터미널 기호에 대해 해당 클래스를 만들어야 한다.
- `NonterminalExpression`: 문법의 오른편에 나타나는 모든 기호에 대해 클래스를 정의해야 한다. 문법에 다음과 같이 정의하고 있다면, `R ::= R1 R2 ... Rn` R에 대해 `NonterminalExpression`에 해당하는 클래스를 정의해야 한다. 또한 규칙의 오른쪽에 나타난 R1에서 Rn에 이르기까지의 모든 기호에 대응하는 인스턴스 변수들을 정의해야 한다. 또한 터미널 기호가 아닌 모든 기호들에 대해 `interpret()` 연산을 구현해야 한다. 이 `interpret()` 연산은 R1에서 Rn에 이르기까지의 각 인스턴스 변수를 재귀적으로 호출하는 것이 일반적이다.
- `Context`: 번역기에 대한 포괄적인 정보를 포함한다.
- `Client`: 언어로 정의한 특정 문장을 나타내는 추상 구문 트리이다. 이 추상 구문 트리는 `NonterminalExpression`과 `TerminalExpression` 클래스의 인스턴스로 구성된다. 이 인스턴스의 `interpret()` 연산을 호출한다.

협력 방법
- 사용자는 `NonterminalExpression`과 `TerminalExpression` 인스턴스들로 해당 문장에 대한 추상 구문 트리를 만든다. 그리고 사용자는 `interpret()` 연산을 호출하는데, 이때 해석에 필요한 문맥 정보를 초기화한다.
- 각 `NonterminalExpression` 노드는 또 다른 서브 표현식에 대한 `interpret()`를 이용하여 자신의 `interpret()` 연산을 정의한다. `interpret()` 연산은 재귀적으로 `interpret()` 연산을 이용하여 기본적 처리를 담당한다.
- 각 노드에 정의한 `interpret()` 연산은 해석자의 상태를 저장하거나 그것을 알기 위해 문맥(context) 정보를 이용한다.

## 활용성
해석이 필요한 언어가 존재하거나 추상 구문 트리로서 그 언어의 문장을 표현하고자 한다면 인터프리터 패턴을 사용할 때이다. 인터프리터 패턴이 가장 잘 먹힐 때를 정리하면 다음과 같다.
- 정의할 언어의 문법이 간단하다. 문법이 복잡하다면 문법을 정의하는 클래스 계통이 복잡해지고 관리할 수 없게 된다. 이는 해석자 패턴을 사용하는 것보다는 파서 생성기와 같은 도구를 이용하는 것이 더 나은 방법이다. 파서 생성기는 추상 구문 트리를 생성하지 않고도 문장을 해석할 수 있기 때문에, 시간과 공간을 절약할 수 있다.
- 효율성은 별로 고려할 사항이 아니다. 사실 가장 효율적인 해석자를 구현하는 방법은 파스 트리를 직접 해석하도록 만드는 것이 아니라, 일차적으로 파스 트리를 다른 형태로 번역(translate)시키는 것이다. 예를 들어, 정규 표현식은 일반적으로 유한 상태 기계(finite state machine) 개념으로 번역한다. 이때에도 정규 표현식을 유한 상태 기계로 변형하는 번역기를 구현해야 하는데, 역시 해석자 패턴을 적용할 수 있다.

## 구현
### 인터프리터 패턴 적용 전
```java
public class PostfixNotation {

    private final String expression;

    public PostfixNotation(String expression) {
        this.expression = expression;
    }

    public static void main(String[] args) {
        PostfixNotation postfixNotation = new PostfixNotation("123+-");
        postfixNotation.calculate();
    }

    private void calculate() {
        Stack<Integer> numbers = new Stack<>();

        for (char c : this.expression.toCharArray()) {
            switch (c) {
                case '+':
                    numbers.push(numbers.pop() + numbers.pop());
                    break;
                case '-':
                    int right = numbers.pop();
                    int left = numbers.pop();
                    numbers.push(left - right);
                    break;
                default:
                    numbers.push(Integer.parseInt(c + ""));
            }
        }

        System.out.println(numbers.pop());
    }
}
```

### 인터프리터 패턴 적용 후
먼저, expression이라는 트리로 만들기 위해 parser가 있다고 가정하자. `Map`이라는 context 또한 정의해둔다.
```java
public class App {
    public static void main(String[] args) {
        PostfixExpression expression = PostfixParser.parse("xyz+-a+");
        int result = expression.interpret(Map.of('x', 1, 'y', 2, 'z', 3, 'a', 4));
        System.out.println(result);
    }
}
```

expression은 다음과 같이 인터페이스로 정의한다.

```java
public interface PostfixExpression {

    int interpret(Map<Character, Integer> context);
}
```

```java
public class VariableExpression implements PostfixExpression {

    private Character character;

    public VariableExpression(Character character) {
        this.character = character;
    }

    @Override
    public int interpret(Map<Character, Integer> context) {
        return context.get(this.character);
    }
}
```

```java
public class PlusExpression implements PostfixExpression {

    private PostfixExpression left;

    private PostfixExpression right;

    public PlusExpression(PostfixExpression left, PostfixExpression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int interpret(Map<Character, Integer> context) {
        return left.interpret(context) + right.interpret(context);
    }
}
```

```java
public class MinusExpression implements PostfixExpression {

    private PostfixExpression left;

    private PostfixExpression right;

    public MinusExpression(PostfixExpression left, PostfixExpression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int interpret(Map<Character, Integer> context) {
        return left.interpret(context) - right.interpret(context);
    }
}
```

```java
public class MultiplyExpression implements PostfixExpression{

    private PostfixExpression left, right;

    public MultiplyExpression(PostfixExpression left, PostfixExpression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int interpret(Map<Character, Integer> context) {
        return left.interpret(context) * right.interpret(context);
    }
}
```

parser는 다음과 같다.

```java
public class PostfixParser {

    public static PostfixExpression parse(String expression) {
        Stack<PostfixExpression> stack = new Stack<>();
        for (char c : expression.toCharArray()) {
            stack.push(getExpression(c, stack));
        }
        return stack.pop();
    }

    private static PostfixExpression getExpression(char c, Stack<PostfixExpression> stack) {
        switch (c) {
            case '+':
                return new PlusExpression(stack.pop(), stack.pop());
            case '-':
                PostfixExpression right = stack.pop();
                PostfixExpression left = stack.pop();
                return new MinusExpression(left, right);
            default:
                return new VariableExpression(c);
        }
    }
}
```