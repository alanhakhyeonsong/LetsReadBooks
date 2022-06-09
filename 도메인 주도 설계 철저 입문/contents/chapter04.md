# 4장. 부자연스러움을 해결하는 도메인 서비스
## 서비스란?
도메인 주도 설계에서 말하는 서비스란 크게 두 가지로 나뉜다.
1. 도메인을 위한 서비스
2. 애플리케이션을 위한 서비스

## 도메인 서비스
값 객체나 엔티티 같은 도메인 객체에는 객체의 행동을 정의할 수 있다. 하지만 몇몇 시스템에는 값 객체나 엔티티로 구현하기 어색한 행동도 있다. **도메인 서비스는 이런 어색함을 해결해주는 객체다.**

### 값 객체나 엔티티에 정의하기 어색한 행동
현실과 달리, 시스템에서는 사용자명을 중복으로 사용할 수 없게 하는 경우가 많다. 사용자명에 중복을 허용하지 않는 것은 도메인의 규칙이며 따라서 도메인 객체에 행동으로 정의돼야 한다.

이를 구체화 하기 위해 `User` 클래스에 사용자명의 중복 여부를 확인하는 행위를 추가해보자.

```java
public class User {
    private UserId id;
    private UserName name;

    public User(UserId id, UserName name) {
        if (id == null) throw new IllegalArgumentException("id가 null 입니다.");
        if (name == null) throw new IllegalArgumentException("name이 null 입니다.");

        this.id = id;
        this.name = name;
    }

    // 사용자명 중복 여부 확인 코드 추가
    public boolean exists(User user) {
        // 사용자명 중복을 확인하는 코드
    }
}
```

중복을 확인하는 수단이 `User` 클래스의 행동으로 정의되어 있다. 객체의 정의만 봐선 문제가 없어 보이나, 사실 이 코드는 자연스럽지 못한 코드다.

```java
UserId userId = new UserId("id");
UserName userName = new UserName("Ramos");
User user = new User(userId, userName);

// 새로 만든 객체에 중복 여부를 묻는 상황이 됨
boolean check = user.exists(user);
System.out.println(check); // true or false?
```

실제 메서드를 이용해 중복을 확인하는 과정은 위와 같다. 사용자명 중복을 확인하는 처리는 `User` 클래스에 정의되어 있으니 결국 자기 자신에게 중복 여부를 묻는 상황이 된다. 이런 부자연스러운 코드는 해당 역할을 하는 객체에 맡기는게 좋다.

### 부자연스러움을 해결해주는 객체
엔티티로 구현한 사용자 객체에 사용자명 중복 처리를 구현하는 것은 부자연스러운 코드의 전형적인 예시다. 이러한 부자연스러움을 해결해주는 것이 도메인 서비스다.  
도메인 서비스도 일반적인 객체와 다를 것이 없다.

```java
public class UserService {
    public boolean exists(User user) {
        // 사용자명 중복을 확인
    }
}
```

도메인 서비스는 자신의 행동을 바꿀 수 있는 인스턴스만의 값을 갖지 않는다는 점에서 값 객체나 엔티티와 다르다.

```java
UserService userService = new UserService();

UserId userId = new UserId("id");
UserName userName = new UserName("Ramos");
User user = new User(userId, userName);

// 도메인 서비스에 요청하기
boolean check = userService.exists(user);
System.out.println(check);
```

**도메인 서비스를 이용하니 자기 자신에게 중복 여부를 확인하거나 중복 확인에만 사용되고 내려질 인스턴스를 만들 필요가 없어졌다.** 값 객체나 엔티티에 정의하기 부자연스러운 처리를 도메인 서비스에 정의하면 자연스러운 코드를 만들 수 있다.

## 도메인 서비스를 남용한 결과
엔티티나 값 객체에 정의하기 부자연스러운 처리는 도메인 서비스에 정의하면 된다. **중요한 것은 '부자연스러운 처리'에만 한정해야 한다**는 점이다. 그렇지 않다면 모든 처리가 도메인 서비스에 정의되는 결과를 낳을 수 있다.

```java
public class UserService {
    public void changeName(User user, UserName name) {
        if (user == null) throw new IllegalArgumentException("user가 null입니다.");
        if (name == null) throw new IllegalArgumentException("name이 null입니다.");

        user.name = name;
    }
}

@Getter
@Setter
public class User {
    private UserId id;
    private UserName name;

    public User(UserId id, UserName name) {
        this.id = id;
        this.name = name;
    }
}
```

모든 처리를 도메인 서비스에 구현하면 엔티티에는 getter/setter만 남게된다. 이러한 코드만으로는 사용자 객체의 처리 내용이나 적용되는 도메인 규칙을 발견하기 어렵다. 생각 없이 모든 처리 코드를 도메인 서비스로 옮기면 다른 도메인 객체는 그저 데이터를 저장할 뿐, 별다른 정보를 제공할 수 없는 객체가 되는 결과를 낳는다.

**도메인 객체가 원래 포함했어야 할 지식이나 처리 내용을 모두 도메인 서비스나 애플리케이션 서비스에 빼앗겨 자신이 제공할 수 있는 정보가 없는 도메인 객체를 빈혈 도메인 모델이라 한다.** 이런 객체는 데이터와 행위를 함께 모아 놓는다는 객체지향의 기본 원칙을 정면으로 거스르는 것이다.

### 도메인 서비스는 가능한 한 피할 것
어떤 행위를 값 객체나 엔티티에 구현할지 아니면 도메인 서비스에 구현할지 망설여진다면 우선 엔티티나 값 객체에 정의하는 것이 좋으며, 도메인 서비스에 행위를 구현하는 것은 가능한 한 피해야 한다.

**도메인 서비스를 남용하면 데이터와 행위가 단절돼 로직이 흩어지기 쉽다. 로직이 흩어지면 소프트웨어가 변화에 대응하는 유연성이 저해돼 심각하게 정체된다.**

## 엔티티/값 객체와 함께 유스케이스 수립하기
### 사용자 엔티티 확인
```java
@Getter
public class User {
    private UserId id;
    private UserName name;

    public User(UserName name) {
        if (name == null) throw new IllegalArgumentException("name이 null입니다.");

        UserId id = new UserId(UUID.randomUUID().toString());

        this.id = id;
        this.name = name;
    }
}

@Getter
public class UserId {
    private String value;

    public UserId(String value) {
        if (value == null) IllegalArgumentException("value가 null입니다.");

        this.value = value;
    }
}

@Getter
public class UserName {
    private String value;

    public UserName(String value) {
        if (value == null) throw new IllegalArgumentException("value가 null입니다.");
        if (value.length() < 3) throw new IllegalArgumentException("사용자명은 3글자 이상이어야 합니다.");

        this.value = value;
    }
}
```

### 사용자 생성 처리 구현
```java
@Repository
@RequiredArgsConstructor;
public class Program {
    private final UserService userService;
    private final EntityManager em;

    public void createUser(String username) {
        User user = new User(new UserName(username));
        if (userService.exists(user)) {
            throw new UserExistException();
        }

        em.persist(user);
    }
}

@Repository
@RequiredArgsConstructor;
public class UserService {
    private final EntityManager em;

    public boolean exists(User user) {
        String username = user.getUserName().getValue();
        List<User> list = em.createQuery("select u from User u where u.username.value := username", User.class)
                .setParameter("username", username)
                .getResultList();
        
        if (list.size() != 0) return true;
        return false;
    }
}
```

위 코드는 언뜻 보면 문제가 없어보인다. 하지만 코드가 지나치게 데이터스토어를 다루는 것에 집중되어 있다. 이렇게 작성하면 코드의 유연성 또한 부족해진다. 데이터베이스가 관계형 DB에서 NoSQL로 변경된다면 코드를 전부 수정해야 한다.  
(덧붙여 JPA 기반 코드로 작성했는데, Service가 Repository를 수행하고 있다는 점, DB접근 관련 로직을 한 곳에서 수행할 수 있는데 여기저기 흩어진 점 등 문제가 많은 코드다.)

사용자 생성 처리의 본질은 **사용자를 생성하는 것과 사용자명 중복 여부를 확인하는 것, 생성된 사용자 데이터를 저장하는 것**이다. 도메인 서비스는 이런 본질적인 로직을 다뤄야 하지 특정 데이터스토어를 직접 다루는 내용이어서는 안 된다.

이는 리포지토리 패턴으로 해결이 가능하다.

### 도메인 서비스의 기준
데이터스토어는 본래 도메인에는 없는 존재로, 애플리케이션 구축을 위해 추가된 애플리케이션만의 관심사다. 따라서 도메인 개념이나 지식을 코드로 옮긴 대상인 도메인 객체가 데이터스토어를 직접 다루는 것은 바람직하지 못하다. **도메인 객체는 오로지 도메인 모델만을 나타내야 한다.**  
// 저자는 이 주장과 다름

저자는 어떤 처리를 도메인 서비스로 만들어야 할지를 판단할 때 그 처리가 도메인에 기초한 것인지를 중요하게 본다. '사용자명 중복'이라는 개념이 도메인에 기초한 것이라면 이를 구현하는 서비스도 도메인 서비스여야 한다. 반대로, 애플리케이션을 만들며 필요하게 된 것이라면 도메인 서비스가 아니다. 그런 처리는 애플리케이션 서비스로 정의해야 한다.