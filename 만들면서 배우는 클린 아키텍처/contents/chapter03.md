# 3장. 코드 구성하기
새 프로젝트에서 가장 먼저 제대로 만들려고 하는 것은 패키지 구조다. 프로젝트가 계속해서 진행될수록 패키지 구조는 짜임새 없는 엉망진창 코드를 그럴싸하게 보이게 만드는 껍데기일 뿐이라는 점을 깨닫게 된다.

## 계층으로 구성하기

```
buckpal
 ┣ domain
 ┃ ┣ Account.java
 ┃ ┣ AccountRepository.java
 ┃ ┣ AccountService.java
 ┃ ┗ Activity.java
 ┣ persistence
 ┃ ┗ AccountRepositoryImpl.java
 ┗ web
   ┗ AccountController.java
```

웹, 도메인, 영속성 계층 각각에 대해 전용 패키지인 web, domain, persistence를 뒀다. 의존성 역전 원칙을 적용해서 의존성이 domain 패키지에 있는 도메인 코드만을 향하도록 해뒀다. 여기서는 domain 패키지에 `AccountRepository` 인터페이스를 추가하고, persistence 패키지에 `AccountRepositoryImpl` 구현체를 둠으로써 의존성을 역전시켰다.

하지만, 이 패키지 구조는 다음의 이유들로 최적의 구조가 아니다.

애플리케이션의 기능 조각(functional slice)이나 특성(feature)을 구분 짓는 패키지 경계가 없다. 이 구조에서 사용자를 관리하는 기능을 추가해야 한다면 web 패키지에 `UserController`를 추가하고, domain 패키지에 `UserService`, `UserRepository`, `User`를 추가하고 persistence 패키지에 `UserRepositoryImpl`을 추가하게 될 것이다. 추가적인 구조가 없다면, 아주 빠르게 서로 연관되지 않은 기능들끼리 예상하지 못한 부수효과를 일으킬 수 있는 클래스들의 엉망진창 묶음으로 변모할 가능성이 크다.

애플리케이션이 어떤 유스케이스들을 제공하는지 파악할 수 없다. `AccountService`와 `AccountController`가 어떤 유스케이스를 구현했는지 파악할 수 있겠는가? 특정 기능을 찾기 위해서는 어떤 서비스가 이를 구현했는지 추측해야 하고, 해당 서비스 내의 어떤 메서드가 그에 대한 책임을 수행하는지 찾아야 한다.

마지막으로, 패키지 구조를 통해서는 우리가 목표로 하는 아키텍처를 파악할 수 없다.

## 기능으로 구성하기

```
buckpal
 ┗ account
   ┣ Account.java
   ┣ AccountController.java
   ┣ AccountRepository.java
   ┣ AccountRepositoryImpl.java
   ┗ SendMoneyService.java
```

계층형 아키텍처의 단점을 보완하기 위해 기능으로 구성된 아키텍처이다.

계좌와 관련된 모든 코드를 최상위의 account 패키지에 넣었고 계층 패키지들도 없앴다. 각 기능을 묶은 새로운 그룹은 account와 같은 레벨의 새로운 패키지로 들어가고, 패키지 외부에서 접근되면 안 되는 클래스들에 대해 `package-private` 접근 수준을 이용해 패키지 간의 경계를 강화할 수 있다. 패키지 경계를 `package-private` 접근 수준과 결합하면 각 기능 사이의 불필요한 의존성을 방지할 수 있다.

또한 `AccountService`의 책임을 좁히기 위해 `SendMoneyService`로 클래스명을 바꿨다. 그 결과 '송금하기' 유스케이스를 구현한 코드는 클래스명만으로도 찾을 수 있게 됐다. 엉클 밥은 이를 '소리치는 아키텍처'라 명명한 바 있다.

그러나 기능에 의한 패키징 방식은 사실 계층에 의한 패키징 방식보다 아키텍처의 **가시성을 훨씬 더 떨어뜨린다.** 어댑터를 나타내는 패키지명이 없고, 인커밍 포트, 아웃고잉 포트를 확인할 수 없다. 심지어 도메인 코드와 영속성 코드 간의 의존성을 역전시켜서 `SendMoneyService`가 `AccountRepository` 인터페이스만 알고 있고 구현체는 알 수 없도록 했음에도 불구하고, `package-private` 접근 수준을 이용해 도메인 코드가 실수로 영속성 코드에 의존하는 것을 막을 수 없다.

## 아키텍처적으로 표현력 있는 패키지 구조

```
buckpal
 ┗ account
   ┣ adapter
   ┃ ┣ in
   ┃ ┃ ┗ web
   ┃ ┃ ┃ ┗ AccountController.java
   ┃ ┗ out
   ┃ ┃ ┗ persistence
   ┃ ┃ ┃ ┣ AccountPersistenceAdapter.java
   ┃ ┃ ┃ ┗ SpringDataAccountRepository.java
   ┣ application
   ┃ ┣ port
   ┃ ┃ ┣ in
   ┃ ┃ ┃ ┗ SendMoneyUseCase.java
   ┃ ┃ ┗ out
   ┃ ┃ ┃ ┣ LoadAccountPort.java
   ┃ ┃ ┃ ┗ UpdateAccountStatePort.java
   ┃ ┗ SendMoneyService.java
   ┗ domain
     ┣ Account.java
     ┗ Activity.java
```

헥사고날 아키텍처에서 구조적으로 핵심적인 요소는 엔티티, 유스케이스, 인커밍/아웃고잉 포트, 인커밍/아웃고잉 어댑터다. 구조의 각 요소들은 패키지 하나씩에 직접 매핑된다. 최상위에는 `Account`와 관련된 유스케이스를 구현한 모듈임을 나타내는 account 패키지가 있다.

그 다음 레벨에는 도메인 모델이 속한 domain 패키지, 도메인 모델을 둘러싼 서비스 계층을 포함한 application 패키지가 있다. adapter 패키지는 애플리케이션 계층의 인커밍/아웃고잉 포트에 대한 구현을 제공하는 어댑터를 포함한다.

이 패키지 구조는 '아키텍처-코드 갭'을 효과적으로 다룰 수 있는 강력한 요소다. 만약 패키지 구조가 아키텍처를 반영할 수 없다면 시간이 지남에 따라 코드는 점점 목표하던 아키텍처로부터 멀어지게 될 것이다. 또한 이처럼 표현력 있는 패키지 구조는 아키텍처에 대한 적극적인 사고를 촉진한다. 많은 패키지가 생기고, 현재 작업 중인 코드를 어떤 패키지에 넣어야 할지 계속 생각해야 하기 때문이다.

그런데 패키지가 아주 많다는 것은 모든 것을 `public`으로 만들어서 패키지 간의 접근을 허용해야 한다는 것을 의미하는 게 아닐까? 적어도 어댑터 패키지에 대해서는 그렇지 않다. 이 패키지에 들어 있는 모든 클래스들은 application 패키지 내에 있는 포트 인터페이스를 통하지 않고는 바깥에서 호출되지 않기 때문에 `package-private` 접근 수준으로 둬도 된다. 애플리케이션 계층에서 어댑터 클래스로 향하는 우발적인 의존성은 있을 수 없다.

하지만 application 패키지와 domain 패키지 내의 일부 클래스들은 `public`으로 지정해야 한다. 의도적으로 어댑터에서 접근 가능해야 하는 포트들은 `public`이어야 한다. 도메인 클래스들은 서비스, 그리고 잠재적으로는 어댑터에서도 접근 가능하도록 `public`이어야 한다. 서비스는 인커밍 포트 인터페이스 뒤에 숨겨질 수 있기 때문에 `public`일 필요가 없다.

어댑터 코드를 자체 패키지로 이동시키면 필요할 경우 하나의 어댑터를 다른 구현으로 쉽게 교체할 수 있다는 장점도 있다. 데이터베이스를 NoSQL에서 RDBMS로 변경해야 한다면, 이 패키지 구조에서는 관련 아웃고잉 포트들만 새로운 어댑터 패키지에 구현하고 기존 패키지를 지우면 된다.

또한 이 패키지 구조의 매력적인 장점은 DDD 개념에 직접적으로 대응시킬 수 있다는 점이다. account 같은 상위 레벨 패키지는 다른 바운디드 컨텍스트와 통신할 전용 진입점과 출구(포트)를 포함하는 바운디드 컨텍스트에 해당한다. domain 패키지 내에서는 DDD가 제공하는 모든 도구를 이용해 우리가 원하는 어떤 도메인 모델이든 만들 수 있다.

## 의존성 주입의 역할
클린 아키텍처의 가장 본질적인 요건은 애플리케이션 계층이 인커밍/아웃고잉 어댑터에 의존성을 갖지 않는 것이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d6e32cad-4814-4810-bfea-554bacf12c9d)

애플리케이션 계층으로의 진입점을 구분 짓기 위해 실제 서비스를 포트 인터페이스들 사이에 숨겨두고 싶을 수 있다. 예제에서 영속성 어댑터와 같이 아웃고잉 어댑터에 대해서는 제어 흐름의 반대 방향으로 의존성을 돌리기 위해 의존성 역전 원칙을 이용해야 한다. 애플리케이션 계층에 인터페이스를 만들고 어댑터에 해당 인터페이스를 구현한 클래스를 두면 된다. 헥사고날 아키텍처에선 이 인터페이스가 포트다. 애플리케이션 계층은 어댑터의 기능을 실행하기 위해 이 포트 인터페이스를 호출한다.

그런데, 포트 인터페이스를 구현한 실제 객체를 누가 애플리케이션 계층에 제공해야 할까? 이 부분에서 의존성 주입을 활용할 수 있다. 모든 계층에 의존성을 가진 중립적인 컴포넌트를 하나 도입하는 것이다. 이 컴포넌트는 아키텍처를 구성하는 대부분의 클래스를 초기화하는 역할을 한다. 위 그림에서 중립적인 의존성 주입 컴포넌트는 `AccountController`, `SendMoneyService`, `AccountPersistenceAdapter` 클래스의 인스턴스를 만들 것이다. `AccountController`가 `SendMoneyUseCase` 인터페이스를 필요로 하기 때문에 의존성 주입을 통해 `SendMoneyService` 클래스의 인스턴스를 주입한다. 컨트롤러는 인터페이스만 알면 되기 때문에 자신이 `SendMoneyService` 인스턴스를 실제로 가지고 있는지도 모른다. `SendMoneyService` 인스턴스를 만들 때도 의존성 주입 메커니즘이 `LoadAccountPort` 인터페이스로 가장한 `AccountPersistenceAdapter` 클래스의 인스턴스를 주입할 것이다.

## 유지보수 가능한 소프트웨어를 만드는 데 어떻게 도움이 될까?
코드에서 아키텍처의 특정 요소를 찾으려면 이제 아키텍처 다이어그램의 박스 이름을 따라 패키지 구조를 탐색하면 된다. 그 결과 의사소통, 개발, 유지보수 모두가 조금 더 수월해진다.