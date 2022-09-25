# 이터레이터(Iterator) 패턴
의도: 내부 표현부를 노출하지 않고 어떤 집합 객체에 속한 원소들을 순차적으로 접근할 수 있는 방법을 제공한다.

집합 객체 내부 구조를 노출시키지 않고 순회 하는 방법을 제공하는 패턴.
- 집합 객체를 순회하는 클라이언트 코드를 변경하지 않고 다양한 순회 방법을 제공할 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/db8fee77-2937-4d92-ade4-5e72503ec6c1/image.png)

- `Iterator`: 원소를 접근하고 순회하는 데 필요한 인터페이스를 제공한다.
- `ConcreteIterator`: `Iterator`에 정의된 인터페이스를 구현하는 클래스로, 순회 과정 중 집합 객체 내에서 현재 위치를 기억한다.
- `Aggregate`: `Iterator` 객체를 생성하는 인터페이스를 정의한다.
- `ConcreteAggregate`: 해당하는 `ConcreteIterator`의 인스턴스를 반환하는 `Iterator` 생성 인터페이스를 구현한다.

협력 방법: `ConcreteIterator`는 집합 객체 내 현재 객체를 계속 추적하고 다음번 방문할 객체를 결정한다.

## 활용성
이터레이터 패턴은 다음과 같은 목적에 사용한다.
- 객체 내부 표현 방식을 모르고도 집합 객체의 각 원소들에 접근하고 싶을 때
- 집합 객체를 순회하는 다양한 방법을 지원하고 싶을 때
- 서로 다른 집합 객체 구조에 대해서도 동일한 방법으로 순회하고 싶을 때

## 구현
### 이터레이터 패턴 적용 전
```java
public class Post {

    private String title;

    private LocalDateTime createdDateTime;

    public Post(String title) {
        this.title = title;
        this.createdDateTime = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }
}
```

```java
public class Board {

    List<Post> posts = new ArrayList<>();

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public void addPost(String content) {
        this.posts.add(new Post(content));
    }
}
```

```java
public class Client {

    public static void main(String[] args) {
        Board board = new Board();
        board.addPost("디자인 패턴 게임");
        board.addPost("선생님, 저랑 디자인 패턴 하나 학습하시겠습니까?");
        board.addPost("지금 이 자리에 계신 여러분들은 모두 디자인 패턴을 학습하고 계신 분들입니다.");

        // TODO 들어간 순서대로 순회하기
        List<Post> posts = board.getPosts();
        for (int i = 0 ; i < posts.size() ; i++) {
            Post post = posts.get(i);
            System.out.println(post.getTitle());
        }

        // TODO 가장 최신 글 먼저 순회하기
        Collections.sort(posts, (p1, p2) -> p2.getCreatedDateTime().compareTo(p1.getCreatedDateTime()));
        for (int i = 0 ; i < posts.size() ; i++) {
            Post post = posts.get(i);
            System.out.println(post.getTitle());
        }
    }
}
```
위 예시는 `Board` 내의 자료구조를 `Client`가 알고 있다는 점이 단점이다. 만약 자료구조가 바뀌면 클라이언트의 코드도 변경된다.

### 이터레이터 패턴 적용 후
```java
public class RecentPostIterator implements Iterator<Post> {

    private Iterator<Post> internalIterator;

    public RecentPostIterator(List<Post> posts) {
        Collections.sort(posts, (p1, p2) -> p2.getCreatedDateTime().compareTo(p1.getCreatedDateTime()));
        this.internalIterator = posts.iterator();
    }

    @Override
    public boolean hasNext() {
        return this.internalIterator.hasNext();
    }

    @Override
    public Post next() {
        return this.internalIterator.next();
    }
}
```

```java
public class Board {

    List<Post> posts = new ArrayList<>();

    public List<Post> getPosts() {
        return posts;
    }

    public void addPost(String content) {
        this.posts.add(new Post(content));
    }

    public Iterator<Post> getRecentPostIterator() {
        return new RecentPostIterator(this.posts);
    }
}
```

```java
public class Client {

    public static void main(String[] args) {
        Board board = new Board();
        board.addPost("디자인 패턴 게임");
        board.addPost("선생님, 저랑 디자인 패턴 하나 학습하시겠습니까?");
        board.addPost("지금 이 자리에 계신 여러분들은 모두 디자인 패턴을 학습하고 계신 분들입니다.");

        // TODO 들어간 순서대로 순회하기
        List<Post> posts = board.getPosts();
        Iterator<Post> iterator = posts.iterator();
        System.out.println(iterator.getClass());

        for (int i = 0 ; i < posts.size() ; i++) {
            Post post = posts.get(i);
            System.out.println(post.getTitle());
        }

        // TODO 가장 최신 글 먼저 순회하기
        Iterator<Post> recentPostIterator = board.getRecentPostIterator();
        while(recentPostIterator.hasNext()) {
            System.out.println(recentPostIterator.next().getTitle());
        }
    }
}
```

## 이터레이터 패턴의 장점과 단점
- 장점
  - 집합 객체가 가지고 있는 객체들에 손쉽게 접근할 수 있다.
  - 일관된 인터페이스를 사용해 여러 형태의 집합 구조를 순회할 수 있다.
- 단점
  - 클래스가 늘어나고 복잡도가 증가한다.

## Java와 Spring에서의 활용 예시
### Java
- `java.util.Enumeration`과 `java.util.Iterator`
- Java StAX (Streaming API for XML)의 `Iterator` 기반의 API
  - `XmlEventReader`, `XmlEventWriter`

### Spring
- `CompositeIterator`