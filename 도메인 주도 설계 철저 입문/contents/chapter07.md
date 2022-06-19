# 7장. 소프트웨어의 유연성을 위한 의존 관계 제어
> 소프트웨어에서 중심적 지위를 가진 객체를 변경해야 하는 경우를 생각해보자. 이 객체에 의존하는 객체도 이 객체가 의존하는 객체도 여러 가지가 있을 것이다. 하나의 변경도 여러 객체에 영향을 미친다. 이처럼 정교하게 서로 엮인 코드를 수정하는 작업에 대한 부담감은 거의 공포에 가까운 감정으로 개발자를 압박할 것이다.  
> 프로그램을 만들어가는 과정에서 객체 간의 의존 관게가 발생하는 것을 막을 수는 없다. 의존 관계는 객체를 사용하는 것만으로도 발생하기 때문이다. 의존 자체를 피하는 것보다는 이를 잘 제어하는 것이 중요하다.

## 의존이란 무엇인가
**의존은 어떤 객체가 다른 객체를 참조하면서 발생한다.**
```java
public class ObjectA {
    private ObjectB objectB;
}
```
`ObjectA`는 `ObjectB`를 참조한다. 그러므로 `ObjectB`에 대한 정의가 없으면 `ObjectA`의 정의가 성립할 수 없다. 이때 `ObjectA`가 `ObjectB`에 의존한다고 한다.

![](https://velog.velcdn.com/images/songs4805/post/3c413118-5e58-47d6-91f4-1f86a6b6c225/image.jpg)

의존은 의존하는 객체에서 의존의 대상이 되는 객체 쪽으로 향하는 화살표를 통해 나타낸다.

**의존 관계는 인터페이스와 그 구현체가 되는 구상 클래스 사이에서도 생긴다.**

```java
public interface JoinRoomQuerydslRepository {

    List<JoinRoomDto> findAllJoinRoomDtoByMemberId(Long memberId);
}

@RequiredArgsConstructor
public class JoinRoomQuerydslRepositoryImpl implements JoinRoomQuerydslRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<JoinRoomDto> findAllJoinRoomDtoByMemberId(Long memberId) {
        //...
    }
}
```
`JoinRoomQuerydslRepositoryImpl` 클래스는 `JoinRoomQuerydslRepository` 인터페이스를 구현한다. 만약 인터페이스가 정의되어 있지 않다면 클래스 선언부에 컴파일 에러가 발생하며 구현체의 정의가 성립하지 않는다.

당연히 **모듈 간의 의존 관계 또한 존재한다.**

```java
@Service
@RequiredArgsConstuctor
public class MemberService {
    private final MemberRepository memberRepository;

    //...
}
```
`MemberRepository`가 특정 퍼시스턴시 기술에 의존한다는 점은 문제가 될 수 있다.  
특정 데이터베이스와 항상 붙어있으므로, 가볍게 로컬머신에서 돌리거나 테스트를 한다거나 혹은 데이터베이스를 변경해야 할 때, 코드를 다 변경해주어야 한다. 따라서 변수의 타입을 인터페이스로 변경하는 것이 바람직하다. 생성자는 인터페이스를 구현한 어떤 구현체라도 인자로 받을 수 있다.  
Spring에서 `QueryDSL`이든 `JPA`든 기본 `JDBCTemplate`이든 여러 방식을 사용할 때 해당 인터페이스와 extends를 활용했던 점을 상기해본다면 위 논리가 이해 될 것이다.

## 의존 관계 역전 원칙이란 무엇인가
의존 관계 역전 원칙(Dependency Inversion Principle)은 다음과 같이 정의한다.
- 추상화 수준이 높은 모듈이 낮은 모듈에 의존해서는 안 되며 두 모듈 모두 추상 타입에 의존해야 한다.
- 추상 타입이 구현의 세부 사항에 의존해서는 안 된다. 구현의 세부 사항이 추상 타입에 의존해야 한다.

의존 관계 역전 원칙은 소프트웨어를 유연하게 하며, 기술적 요소가 비즈니스 로직을 침범하는 일을 막기 위해서는 필수적이다.

### 추상 타입에 의존하라
푸상화 수준은 입/출력으로부터의 거리를 뜻한다. 추상화 수준이 낮을수록 기계와 가까운 구체적인 처리, 높을수록 사람과 가까운 추상적인 처리를 말한다.

![](https://velog.velcdn.com/images/songs4805/post/07c43639-0a9e-4ef2-8716-d08443bb1cc8/image.png)

추상 타입을 도입하면 `Service`와 `QuerydslRepositoryImpl` 두 클래스 모두 추상 타입인 `Repository`를 향한 의존 관계를 갖는다. 높은 추상화 수준의 모듈이 낮은 추상화 모듈에 의존하는 상황도 해소되고 '두 모듈 모두 추상 타입에 의존할 것'이라는 원칙도 지켜진다. 본래 구상 타입의 구현에 의존하던 것이 추상 타입을 의존하게 되면서 의존 관계가 역전된다.

### 주도권을 추상 타입에 둬라
중요도가 높은 도메인 규칙은 항상 추상화 수준이 높은 쪽에 기술된다. 하지만 추상화 수준이 높은 모듈이 낮은 모듈을 의존하게 되면, 나중에 기술적인 변경사항이 생겼을 때 그에 맞춰 도메인 규칙을 변경해야하는 이상한 일이 생긴다. 따라서 항상 주도권은 추상화 수준이 높은 추상타입에게 있어야한다.

## 의존 관계 제어하기
### 1. 노가다
```java
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService() {
        // this.memberRepository = new InMemoryMemberRepository();
        this.memberRepository = new MySqlMemberRepository();
    }

    //...
}
```
개발 상황에 따라 리포지토리를 변경해야 하는데 위 처럼 일일이 교체해야 한다면, 원하는 것을 선택하는 데 비효율적이다.

### 2. Service Locator 패턴
Service Locator 패턴은 ServiceLocator 객체에 의존 해소 대상이 되는 객체를 미리 등록해 둔 다음, 인스턴스가 필요한 곳에서 ServiceLocator를 통해 인스턴스를 받아 사용하는 패턴이다.

https://www.baeldung.com/java-service-locator-pattern

하지만 이 패턴은 두 가지 단점이 있다.
- 의존 관계를 외부에서 보기 어렵다.
- 테스트 유지가 어렵다.

### 3. IoC Container 패턴
IoC Container(DI Container) 패턴은 IoC 컨테이너가 의존성을 생성자로 주입해주는 것을 말한다. Spring의 `ApplicationContext`가 이에 해당한다.

[스프링 컨테이너와 스프링 빈 - Ramos 기술 블로그](https://velog.io/@songs4805/Spring-%EC%8A%A4%ED%94%84%EB%A7%81-%EC%BB%A8%ED%85%8C%EC%9D%B4%EB%84%88%EC%99%80-%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B9%88)

```java
// 의존 관계 주입
ClassA classA = new ClassA();
ClassB classB = new ClassB(classA);
```
위 방식은 의존 관계를 주입하는 데 생성자 메서드를 사용하므로 생성자 주입이라고도 한다.

Dependency Injection 패턴을 적용하면 의존 관계를 변경했을 때 테스트 코드 수정을 강제할 수 있다. 그러나 의존하는 객체를 만드는 코드를 여기저기 작성해야 하는 불편함이 있다.

이런 문제를 해결해 주는 것이 IoC Container 패턴이다.  
보통 해당 구현체를 시작하기 전에 미리 설정정보로 등록을 해놓으면, 서비스가 시작하면서 IoC 컨테이너에 필요한 의존 인스턴스들이 모인다. 그래서 프로그램 실행 중 추상 타입의 구현체를 찾아서 알아서 주입해주는 방식이다. 보통 IoC 컨테이너에 해당 인스턴스가 없으면 생성자로 해당 인스턴스가 전달되지 않기 때문에, 테스트를 실행하지 않아도 프로그램을 실행하면서 오류가 난다.