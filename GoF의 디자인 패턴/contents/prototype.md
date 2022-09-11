# 프로토타입(Prototype) 패턴
의도: 원형이 되는(prototypical) 인스턴스를 사용하여 생성할 객체의 종류를 명시하고, 이렇게 만든 견본을 복사해서 새로운 객체를 생성한다.

- 복제 기능을 갖추고 있는 기존 인스턴스를 프로토타입으로 사용해 새 인스턴스를 만들 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/272e2010-0e0b-4f95-bad4-cfb8aa43ba93/image.jpeg)

- `Prototype`: 자신을 복제하는 데 필요한 인터페이스를 정의한다.
- `ConcretePrototype`: 자신을 복제하는 연산을 구현한다.
- `Client`: 원형에 자기 자신의 복제를 요청하여 새로운 객체를 생성한다.

## 활용성
프로토 타입 패턴은 제품의 생성, 복합, 표현 방법에 독립적인 제품을 만들고자 할 때 쓴다.
- 인스턴스화할 클래스를 런타임에 지정할 때(ex. 동적 로딩)
- 제품 클래스 계통과 병렬적으로 만드는 팩토리 클래스를 피하고 싶을 때
- 클래스의 인스턴스들이 서로 다른 상태 조합 중에 어느 하나일 때 원형 패턴을 쓴다. 이들을 미리 원형으로 초기화해두고, 나중에 이를 복제해서 사용하는 것이 매번 필요한 상태 조합의 값들을 수동적으로 초기화하는 것보다 더 편리할 수도 있다.

기존의 인스턴스를 만들 때 시간이 오래 걸리는 작업(데이터베이스나 네트워크를 거쳐야 하는 작업)을 통해야 한다면 매번 인스턴스를 생성하는 비용이 상당하다. 최초 한 번만 모든 데이터를 가져와 생성해두고 이후엔 이를 복사해서 인스턴스를 생성한다면 훨씬 효율적으로 바뀐다.

## 구현
![](https://velog.velcdn.com/images/songs4805/post/c0d08f04-5d96-4cfd-92c0-477ed12ba8a1/image.png)

```java
public class GithubRepository {

    private String user;

    private String name;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

다음과 같이 `Cloneable` 인터페이스를 구현하여, `clone()` 메소드를 재정의해야 한다.
```java
public class GithubIssue implements Cloneable {

    private int id;

    private String title;

    private GithubRepository repository;

    public GithubIssue(GithubRepository repository) {
        this.repository = repository;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GithubRepository getRepository() {
        return repository;
    }

    public String getUrl() {
        return String.format("https://github.com/%s/%s/issues/%d",
                repository.getUser(),
                repository.getName(),
                this.getId());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        GithubRepository repository = new GithubRepository();
        repository.setUser(this.repository.getUser());
        repository.setName(this.repository.getName());

        GithubIssue githubIssue = new GithubIssue(repository);
        githubIssue.setId(this.id);
        githubIssue.setTitle(this.title);

        return githubIssue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GithubIssue that = (GithubIssue) o;
        return id == that.id && Objects.equals(title, that.title) && Objects.equals(repository, that.repository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, repository);
    }
}
```

```java
public class App {

    public static void main(String[] args) throws CloneNotSupportedException {
        GithubRepository repository = new GithubRepository();
        repository.setUser("whiteship");
        repository.setName("live-study");

        GithubIssue githubIssue = new GithubIssue(repository);
        githubIssue.setId(1);
        githubIssue.setTitle("1주차 과제: JVM은 무엇이며 자바 코드는 어떻게 실행하는 것인가.");

        String url = githubIssue.getUrl();
        System.out.println(url);

        GithubIssue clone = (GithubIssue) githubIssue.clone();
        System.out.println(clone.getUrl());

        repository.setUser("Keesun");

        System.out.println(clone != githubIssue);
        System.out.println(clone.equals(githubIssue));
        System.out.println(clone.getClass() == githubIssue.getClass());
        System.out.println(clone.getRepository() == githubIssue.getRepository());

        System.out.println(clone.getUrl());
    }

}
```

## 프로토타입 패턴의 장점과 단점
- 장점
  - 복잡한 객체를 만드는 과정을 숨길 수 있다.
  - 기존 객체를 복제하는 과정이 새 인스턴스를 만드는 것보다 비용(시간 또는 메모리)적인 면에서 효율적일 수도 있다.
  - 추상적인 타입을 리턴할 수 있다.
- 단점
  - 복잡한 객체를 만드는 과정 자체가 복잡할 수 있다. (특히, 순환 참조가 있는 경우)

## Java와 Spring에서의 활용 예시
- Java `Object` 클래스의 `clone` 메소드와 `Cloneable` 인터페이스
- shallow copy와 deep copy
- `ModelMapper`

Collections에선 다음과 같이 사용한다.
```java
public class JavaCollectionExample {

    public static void main(String[] args) {
        Student keesun = new Student("keesun");
        Student whiteship = new Student("whiteship");
        List<Student> students = new ArrayList<>();
        students.add(keesun);
        students.add(whiteship);

        List<Student> clone = new ArrayList<>(students);
        System.out.println(clone);
    }
}
```