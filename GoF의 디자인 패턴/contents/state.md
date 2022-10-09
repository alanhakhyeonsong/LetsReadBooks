# 상태(State) 패턴
의도: 객체의 내부 상태에 따라 스스로 행동을 변경할 수 있게 허가하는 패턴으로, 이렇게 하면 객체는 마치 자신의 클래스를 바꾸는 것처럼 보인다.

객체 내부 상태 변경에 따라 객체의 행동이 달라지는 패턴.
- 상태에 특화된 행동들을 분리해 낼 수 있으며, 새로운 행동을 추가하더라도 다른 행동에 영향을 주지 않는다.

![](https://velog.velcdn.com/images/songs4805/post/9ee0c4f2-5585-4a12-97fe-e0e04d670f34/image.png)

- `Context`: 사용자가 관심 있는 인터페이스를 정의한다. 객체의 현재 상태를 정의한 `ConcreteState` 서브클래스의 인스턴스를 유지, 관리한다.
- `State`: `Context`의 각 상태별로 필요한 행동들을 캡슐화하여 인터페이스로 정의한다.
- `ConcreteState` 서브 클래스들: 각 서브 클래스들은 `Context`의 상태에 따라 처리되어야 할 실제 행동들을 구현한다.

협력 방법
- 상태에 따라 다른 요청을 받으면 `Context` 클래스는 현재의 `ConcreteState` 객체로 전달한다. 이 `ConcreteState` 클래스의 객체는 `State` 클래스를 상속하는 서브클래스들 중 하나의 인스턴스일 것이다.
- `Context` 클래스는 실제 연산을 처리할 `State` 객체에 자신을 매개변수로 전달한다. 이로써 `State` 객체는 `Context` 클래스에 정의된 정보에 접근할 수 있게 된다.
- `Context` 클래스는 사용자가 사용할 수 있는 기본 인터페이스를 제공한다. 사용자는 상태 객체를 `Context` 객체와 연결시킨다. 즉, `Context` 클래스에 현재 상태를 정의하는 것이다. 이렇게 `Context` 객체를 만들고 나면 사용자는 더는 `State` 객체를 직접 다루지 않고 `Context` 객체에 요청을 보내기만 하면 된다.
- `Context` 클래스 또는 `ConcreteState` 서브 클래스들은 자기 다음의 상태가 무엇이고, 어떤 환경에서 다음 상태로 가는지 결정할 수 있다. 즉, 상태는 상태 전이의 규칙이 있으므로, 각각 한 상태에서 다른 상태로 전의하는 규칙을 알아야 한다.

## 활용성
다음 상황 가운데 하나에 속하면 상태 패턴을 사용할 수 있다.
- 객체의 행동이 상태에 따라 달라질 수 있고, 객체의 상태에 따라서 런타임에 행동이 바뀌어야 한다.
- 어떤 연산에 그 객체의 상태에 따라 달라지는 다중 분기 조건 처리가 너무 많이 들어 있을 때. 객체의 상태를 표현하기 위해 상태를 하나 이상의 나열형 상수로 정의해야 한다. 동일한 조건 문장들을 하나 이상의 연산에 중복 정의해야 할 때도 잦다. 이 때, 객체의 상태를 별도의 객체로 정의하면, 다른 객체들과 상관 없이 그 객체의 상태를 다양화시킬 수 있다.

## 구현
### 상태 패턴 적용 전
```java
public class Client {

    public static void main(String[] args) {
        Student student = new Student("whiteship");
        OnlineCourse onlineCourse = new OnlineCourse();

        Student keesun = new Student("keesun");
        keesun.addPrivateCourse(onlineCourse);

        onlineCourse.addStudent(student);
        onlineCourse.changeState(OnlineCourse.State.PRIVATE);

        onlineCourse.addStudent(keesun);

        onlineCourse.addReview("hello", student);

        System.out.println(onlineCourse.getState());
        System.out.println(onlineCourse.getStudents());
        System.out.println(onlineCourse.getReviews());
    }
}
```

```java
import java.util.ArrayList;
import java.util.List;

public class OnlineCourse {

    public enum State {
        DRAFT, PUBLISHED, PRIVATE
    }

    private State state = State.DRAFT;

    private List<String> reviews = new ArrayList<>();

    private List<Student> students = new ArrayList<>();

    public void addReview(String review, Student student) {
        if (this.state == State.PUBLISHED) {
            this.reviews.add(review);
        } else if (this.state == State.PRIVATE && this.students.contains(student)) {
            this.reviews.add(review);
        } else {
            throw new UnsupportedOperationException("리뷰를 작성할 수 없습니다.");
        }
    }

    public void addStudent(Student student) {
        if (this.state == State.DRAFT || this.state == State.PUBLISHED) {
            this.students.add(student);
        } else if (this.state == State.PRIVATE && availableTo(student)) {
            this.students.add(student);
        } else {
            throw new UnsupportedOperationException("학생을 해당 수업에 추가할 수 없습니다.");
        }

        if (this.students.size() > 1) {
            this.state = State.PRIVATE;
        }
    }

    public void changeState(State newState) {
        this.state = newState;
    }

    public State getState() {
        return state;
    }

    public List<String> getReviews() {
        return reviews;
    }

    public List<Student> getStudents() {
        return students;
    }

    private boolean availableTo(Student student) {
        return student.isEnabledForPrivateClass(this);
    }
}
```

```java
import java.util.ArrayList;
import java.util.List;

public class Student {

    private String name;

    public Student(String name) {
        this.name = name;
    }

    private List<OnlineCourse> privateCourses = new ArrayList<>();

    public boolean isEnabledForPrivateClass(OnlineCourse onlineCourse) {
        return privateCourses.contains(onlineCourse);
    }

    public void addPrivateCourse(OnlineCourse onlineCourse) {
        this.privateCourses.add(onlineCourse);
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                '}';
    }
}
```
각 상태 케이스에 따라 `if-else`로 복잡하게 얽혀있어 코드의 가독성이 좋지 않다.

### 상태 패턴 적용 후
```java
import java.util.HashSet;
import java.util.Set;

public class Student {

    private String name;

    public Student(String name) {
        this.name = name;
    }

    private Set<OnlineCourse> onlineCourses = new HashSet<>();

    public boolean isAvailable(OnlineCourse onlineCourse) {
        return onlineCourses.contains(onlineCourse);
    }

    public void addPrivate(OnlineCourse onlineCourse) {
        this.onlineCourses.add(onlineCourse);
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                '}';
    }
}
```

```java
import java.util.ArrayList;
import java.util.List;

public class OnlineCourse {

    private State state = new Draft(this);

    private List<Student> students = new ArrayList<>();

    private List<String> reviews = new ArrayList<>();

    public void addStudent(Student student) {
        this.state.addStudent(student);
    }

    public void addReview(String review, Student student) {
        this.state.addReview(review, student);
    }

    public State getState() {
        return state;
    }

    public List<Student> getStudents() {
        return students;
    }

    public List<String> getReviews() {
        return reviews;
    }

    public void changeState(State state) {
        this.state = state;
    }
}
```

상태 인터페이스를 정의해둔다.
```java
public interface State {

    void addReview(String review, Student student);

    void addStudent(Student student);
}
```

이제 각 상태별로 구현체를 정의하면 된다.
```java
public class Draft implements State {

    private OnlineCourse onlineCourse;

    public Draft(OnlineCourse onlineCourse) {
        this.onlineCourse = onlineCourse;
    }

    @Override
    public void addReview(String review, Student student) {
        throw new UnsupportedOperationException("드래프트 상태에서는 리뷰를 남길 수 없습니다.");
    }

    @Override
    public void addStudent(Student student) {
        this.onlineCourse.getStudents().add(student);
        if (this.onlineCourse.getStudents().size() > 1) {
            this.onlineCourse.changeState(new Private(this.onlineCourse));
        }
    }
}
```

```java
public class Published implements State {

    private OnlineCourse onlineCourse;

    public Published(OnlineCourse onlineCourse) {
        this.onlineCourse = onlineCourse;
    }

    @Override
    public void addReview(String review, Student student) {
        this.onlineCourse.getReviews().add(review);
    }

    @Override
    public void addStudent(Student student) {
        this.onlineCourse.getStudents().add(student);
    }
}
```

```java
public class Private implements State {

    private OnlineCourse onlineCourse;

    public Private(OnlineCourse onlineCourse) {
        this.onlineCourse = onlineCourse;
    }

    @Override
    public void addReview(String review, Student student) {
        if (this.onlineCourse.getStudents().contains(student)) {
            this.onlineCourse.getReviews().add(review);
        } else {
            throw new UnsupportedOperationException("프라이빗 코스를 수강하는 학생만 리뷰를 남길 수 있습니다.");
        }
    }

    @Override
    public void addStudent(Student student) {
        if (student.isAvailable(this.onlineCourse)) {
            this.onlineCourse.getStudents().add(student);
        } else {
            throw new UnsupportedOperationException("프라이빗 코스를 수강할 수 없습니다.");
        }
    }
}
```

변경된 Client는 다음과 같다.
```java
public class Client {

    public static void main(String[] args) {
        OnlineCourse onlineCourse = new OnlineCourse();
        Student student = new Student("whiteship");
        Student keesun = new Student("keesun");
        keesun.addPrivate(onlineCourse);

        onlineCourse.addStudent(student);

        onlineCourse.changeState(new Private(onlineCourse));

        onlineCourse.addReview("hello", student);

        onlineCourse.addStudent(keesun);

        System.out.println(onlineCourse.getState());
        System.out.println(onlineCourse.getReviews());
        System.out.println(onlineCourse.getStudents());
    }
}
```

## 상태 패턴의 장점과 단점
- 장점
  - 상태에 따른 동작을 개별 클래스로 옮겨서 관리할 수 있다.
  - 기존의 특정 상태에 따른 동작을 변경하지 않고 새로운 상태에 다른 동작을 추가할 수 있다.
  - 코드 복잡도를 줄일 수 있다.
- 단점
  - 복잡도가 증가한다.