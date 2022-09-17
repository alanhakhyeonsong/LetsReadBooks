# 데코레이터(Decorator) 패턴
의도: 객체에 동적으로 새로운 책임을 추가할 수 있게 한다. 기능을 추가하려면, 서브클래스를 생성하는 것보다 융통성 있는 방법을 제공한다.

기존 코드를 변경하지 않고 부가 기능을 추가하는 패턴
- 상속이 아닌 위임을 사용해서 보다 유연하게(런타임에) 부가 기능을 추가하는 것도 가능하다.

![](https://velog.velcdn.com/images/songs4805/post/0a8cbf91-b237-47f1-a2d6-3aa0b8507562/image.png)

- `Component`: 동적으로 추가할 서비스를 가질 가능성이 있는 객체들에 대한 인터페이스
- `ConcreteComponent`: 추가적인 서비스가 실제로 정의되어야 할 필요가 있는 객체
- `Decorator`: `Component` 객체에 대한 참조자를 관리하면서 `Component`에 정의된 인터페이스를 만족하도록 인터페이스를 정의
- `ConcreteDecorator`: `Component`에 새롭게 추가할 서비스를 실제로 구현하는 클래스

## 활용성
데코레이터 패턴은 다음의 경우에 사용한다.
- 동적으로 또한 투명하게, 다시 말해 다른 객체에 영향을 주지 않고 개개의 객체에 새로운 책임을 추가하기 위해 사용한다.
- 제거될 수 있는 책임에 대해 사용한다.
- 실제 상속으로 서브클래스를 계속 만드는 방법이 실질적이지 못할 때 사용한다. 너무 많은 수의 독립된 확장이 가능할 때 모든 조합을 지원하기 위해 이를 상속으로 해결하면 클래스 수가 폭발적으로 많아지게 된다. 아니면, 클래스 정의가 숨겨지든가, 그렇지 않더라도 서브클래싱을 할 수 없게 된다.

## 구현
### 데코레이터 패턴 적용 전
상속으로 인한 구현으로 부가 기능의 선택이 정적이다.

```java
public class CommentService {
    public void addComment(String comment) {
        System.out.println(comment);
    }
}
```

```java
public class SpamFilteringCommentService extends CommentService {
    @Override
    public void addComment(String comment) {
        boolean isSpam = isSpam(comment);
        if (!isSpam) {
            super.addComment(comment);
        }
    }

    private boolean isSpam(String comment) {
        return comment.contains("http");
    }
}
```

```java
public class SpamFilteringCommentDecorator extends CommentDecorator {
    public SpamFilteringCommentDecorator(CommentService commentService) {
        super(commentService);
    }

    @Override
    public void addComment(String comment) {
        if (isNotSpam(comment)) {
            super.addComment(comment);
        }
    }

    private boolean isNotSpam(String comment) {
        return !comment.contains("http");
    }
}
```

```java
public class Client {
    private CommentService commentService;

    public Client(CommentService commentService) {
        this.commentService = commentService;
    }

    private void writeComment(String comment) {
        commentService.addComment(comment);
    }

    public static void main(String[] args) {
        Client client = new Client(new SpamFilteringCommentService());
        client.writeComment("오징어게임");
        client.writeComment("보는게 하는거 보다 재밌을 수가 없지...");
        client.writeComment("http://whiteship.me");
    }
}
```

### 데코레이터 패턴 적용 후
먼저 `CommentService`를 인터페이스로 정의한다.

```java
public interface CommentService {

    void addComment(String comment);
}
```

이 인터페이스를 다음과 같이 `DefaultCommentService`, `CommentDecorator` 구현한다.

```java
public class DefaultCommentService implements CommentService {
    @Override
    public void addComment(String comment) {
        System.out.println(comment);
    }
}
```

`CommentDecorator`는 추가 기능에 대한 공통적인 클래스이다.

```java
public class CommentDecorator implements CommentService {

    private CommentService commentService;

    public CommentDecorator(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    public void addComment(String comment) {
        commentService.addComment(comment);
    }
}
```

이를 상속받은 추가 기능은 다음과 같다.

```java
public class SpamFilteringCommentDecorator extends CommentDecorator {

    public SpamFilteringCommentDecorator(CommentService commentService) {
        super(commentService);
    }

    @Override
    public void addComment(String comment) {
        if (isNotSpam(comment)) {
            super.addComment(comment);
        }
    }

    private boolean isNotSpam(String comment) {
        return !comment.contains("http");
    }
}
```

```java
public class TrimmingCommentDecorator extends CommentDecorator {

    public TrimmingCommentDecorator(CommentService commentService) {
        super(commentService);
    }

    @Override
    public void addComment(String comment) {
        super.addComment(trim(comment));
    }

    private String trim(String comment) {
        return comment.replace("...", "");
    }
}
```

이 후 클라이언트를 다음과 같이 정의하고,

```java
public class Client {

    private CommentService commentService;

    public Client(CommentService commentService) {
        this.commentService = commentService;
    }

    public void writeComment(String comment) {
        commentService.addComment(comment);
    }
}
```

다음과 같이 추가하고 싶은 기능을 판별하여 런타임에 동적으로 변경할 수 있다.

```java
public class App {

    private static boolean enabledSpamFilter = true;

    private static boolean enabledTrimming = true;

    public static void main(String[] args) {
        CommentService commentService = new DefaultCommentService();

        if (enabledSpamFilter) {
            commentService = new SpamFilteringCommentDecorator(commentService);
        }

        if (enabledTrimming) {
            commentService = new TrimmingCommentDecorator(commentService);
        }

        Client client = new Client(commentService);
        client.writeComment("오징어게임");
        client.writeComment("보는게 하는거 보다 재밌을 수가 없지...");
        client.writeComment("http://whiteship.me");
    }
}
```

결과적으론 DIP 원칙을 잘 지키게 되었다고 할 수 있다.

## 데코레이터 패턴의 장점과 단점
- 장점
  - 새로운 클래스를 만들지 않고 기존 기능을 조합할 수 있다.
  - 컴파일 타임이 아닌 런타임에 동적으로 기능을 변경할 수 있다.
- 단점
  - 데코레이터를 조합하는 코드가 복잡할 수 있다.

## Java와 Spring에서의 활용 예시
### Java
- `InputStream`, `OutputStream`, `Reader`, `Writer`의 생성자를 활용한 랩퍼
- `java.util.Collections`가 제공하는 메소드들 활용한 랩퍼
- `javax.servlet.http.HttpServletRequest/ResponseWrapper`

### Spring
- `ServerHttpRequestDecorator`