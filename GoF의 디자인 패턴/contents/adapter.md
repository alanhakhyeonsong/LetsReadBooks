# 어댑터(Adapter) 패턴
의도: 클래스의 인터페이스를 사용자가 기대하는 인터페이스 형태로 적응(변환)시킨다. 서로 일치하지 않는 인터페이스를 갖는 클래스들을 함께 동작시킨다.

다른 이름으로 래퍼(Wrapper)라고도 한다.

<p aline="center">
  <img src="https://velog.velcdn.com/images/songs4805/post/30ed96cb-9b05-4760-9509-96e2934d4934/image.png">
</p>

- Target: 사용자가 사용할 응용 분야에 종속적인 인터페이스를 정의하는 클래스
- Client: Target 인터페이스를 만족하는 객체와 동작할 대상
- Adaptee: 인터페이스의 적응이 필요한 기존 인터페이스를 정의하는 클래스로 적응 대상자라 한다.
- Adapter: Target 인터페이스에 Adaptee의 인터페이스를 적응시키는 클래스

기존에 있는 시스템에 새로운 써드파티 라이브러리가 추가된다거나, 레거시 인터페이스를 새로운 인터페이스로 교체하는 경우에 코드의 재사용성을 높일 수 있는 방법에 주로 사용하는 패턴이다.

## 활용성
GoF의 디자인 패턴에선 다음과 같이 제시하고 있다.

- **기존 클래스를 사용하고 싶은데 인터페이스가 맞지 않을 때**
- 아직 예측하지 못한 클래스나 실제 관련되지 않는 클래스들이 기존 클래스를 재사용하고자 하지만, 이미 정의된 재사용 가능한 클래스가 지금 요청하는 인터페이스를 꼭 정의하고 있지 않을 때. 다시 말해, **이미 만든 것을 재사용하고자 하나 이 재사용 가능한 라이브러리를 수정할 수 없을 때**
- 이미 존재하는 여러 개의 서브클래스를 사용해야 하는데, 이 서브클래스들의 상속을 통해 이들의 인터페이스를 다 개조한다는 것이 현실성이 없을 때. 객체 적응자를 써서 부모 클래스의 인터페이스를 변형하는 것이 더 바람직함(이 케이스는 Object Adapter만 해당됨)

## 예시
Spring Security에서 흔히 볼 수 있는 예제이다.

![](https://velog.velcdn.com/images/songs4805/post/3c507c7f-79dc-4d7d-b27c-a2113e884452/image.png)


먼저 Spring Security에서 제공하는 `UserDetails`, `UserDetailsService`의 일부를 제시한다.
```java
public interface UserDetails {

    String getUsername();

    String getPassword();

}
```

```java
public interface UserDetailsService {

    UserDetails loadUser(String username);

}
```

```java
public class LoginHandler {

    UserDetailsService userDetailsService;

    public LoginHandler(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public String login(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUser(username);
        if (userDetails.getPassword().equals(password)) {
            return userDetails.getUsername();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
```

이 후 다음과 같이 `Account`라는 클래스가 있다면, Security에서 제공하는 인터페이스가 일치하지 않는 문제가 있다. 이 때 어댑터를 구현한다.
```java
@Getter @Setter
public class Account {

    private String name;
    private String password;
    private String email;

}
```

`AccountService`는 Adaptee에 해당한다.
```java
public class AccountService {

    public Account findAccountByUsername(String username) {
        Account account = new Account();
        account.setName(username);
        account.setPassword(username);
        account.setEmail(username);
        return account;
    }

    public void createNewAccount(Account account) {

    }

    public void updateAccount(Account account) {

    }

}
```

`AccountUserDetails`와 `AccountUserDetailsService`는 Adapter에 해당한다.
```java
public class AccountUserDetails implements UserDetails {

    private Account account;

    public AccountUserDetails(Account account) {
        this.account = account;
    }

    @Override
    public String getUsername() {
        return account.getName();
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }
}
```

```java
public class AccountUserDetailsService implements UserDetailsService {

    private AccountService accountService;

    public AccountUserDetailsService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public UserDetails loadUser(String username) {
        return new AccountUserDetails(accountService.findAccountByUsername(username));
    }
}
```

결과적으로 클라이언트가 사용하는 인터페이스를 따르지 않는 기존 코드를 재사용할 수 있게 되었다.
```java
public class App {

    public static void main(String[] args) {
        AccountService accountService = new AccountService();
        UserDetailsService userDetailsService = new AccountUserDetailsService(accountService);
        LoginHandler loginHandler = new LoginHandler(userDetailsService);
        String login = loginHandler.login("ramos", "ramos");
        System.out.println(login);
    }
}
```

## 어댑터 패턴의 장단점
- 장점
  - **기존 코드를 변경하지 않고 원하는 인터페이스 구현체를 만들어 재사용할 수 있다. (OCP 원칙)**
  - **기존 코드가 하던 일과 특정 인터페이스 구현체로 변환하는 작업을 각기 다른 클래스로 분리하여 관리할 수 있다. (SRP 원칙)**
- 단점
  - 새 클래스가 생겨 복잡도가 증가할 수 있다. 경우에 따라서는 기존 코드가 해당 인터페이스를 구현하도록 수정하는 것이 좋은 선택이 될 수도 있다.

## Java와 Spring에서의 활용 예시
- Java
  - `java.util.Arrays`의 `asList(T...)`
  - `java.util.Collections`의 `list(Enumeration)`, `enumeration()`
  - `java.io.InputStreamReader(InputStream)`
  - `java.io.OutputStreamWriter(OutputStream)`
- Spring
  - `HandlerAdapter`: 우리가 작성하는 다양한 형태의 핸들러 코드를 스프링 MVC가 실행할 수 있는 형태로 반환해주는 어댑터용 인터페이스