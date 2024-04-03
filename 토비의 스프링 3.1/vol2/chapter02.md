# 2장. 데이터 액세스 기술
Java에는 JDBC라는 호환성이 뛰어나고 편리한 API를 가진 데이터 액세스 기술이 있다. 하지만 전통적인 SQL과 JDBC API의 조합만으로는 엔터프라이즈 애플리케이션의 복잡하고 거대한 데이터를 처리하는 깔끔한 코드를 만드는 데는 많은 제약과 문제점이 있다. JDBC는 JDBC 나름대로 좀 더 효과적인 방식으로 사용하도록 추상화하고 개선할 필요가 있다.

Spring은 주요 Java 데이터 액세스 기술을 모두 지원한다. 이는 Spring의 철학과 일관된 프로그래밍 모델을 유지하면서, 이런 기술을 사용할 수 있다는 뜻이다. 또한 Spring의 트랜잭션 추상화가 지원된다는 의미이기도 하다.

## 공통 개념
### DAO 패턴
데이터 액세스 계층은 DAO 패턴이라 불리는 방식으로 분리하는 것이 원칙이다. 비즈니스 로직이 없거나 단순하면 DAO와 서비스 계층을 통합할 수도 있다. 하지만 의미 있는 비즈니스 로직을 가진 엔터프라이즈 애플리케이션이라면 데이터 액세스 계층을 DAO 패턴으로 분리해야 한다.

DAO 패턴은 DTO 또는 오브젝트만을 사용하는 인터페이스를 통해 데이터 액세스 기술을 외부에 노출하지 않도록 만드는 것이다. **따라서 DAO는 구현 기술에 대한 정보를 외부에 공개해선 안 된다.** 이를 통해 DAO를 사용하는 코드에 영향을 주지 않고 데이터 액세스 기술을 변경하거나 하나 이상의 데이터 액세스 기술을 혼합해서 사용할 수 있게 해준다.

가장 중요한 장점은 **DAO를 이용하는 서비스 계층의 코드를 기술이나 환경에 종속되지 않는 순수한 POJO로 개발할 수 있다는 것이다.** DAO 인터페이스는 기술과 상관없는 단순한 DTO나 도메인 모델만을 사용하기 때문에 언제든지 목 오브젝트 같은 테스트 대역 오브젝트로 대체해서 단위 테스트를 작성할 수 있다.

#### DAO 인터페이스와 DI
**DAO는 인터페이스를 이용해 접근하고 DI 되도록 만들어야 한다.** DAO 인터페이스엔 구쳊거인 데이터 액세스 기술과 관련된 어떤 API나 정보도 노출하지 않는다.

- 인터페이스를 만들 때 습관적으로 DAO 클래스의 모든 `public` 메소드를 추가하지 않도록 주의하자.
  - DAO를 사용하는 서비스 계층 코드에서 의미 있는 메소드만 인터페이스로 공개해야 된다.
- DAO 클래스에 DI를 위해 넣은 `setDataSource()` 같은 수정자 메소드를 `public` 메소드라 해서 기계적으로 인터페이스에 추가하지 않도록 주의하자.
- 특정 데이터 액세스 기술에서만 의미 있는 DAO 메소드의 이름은 피하도록 한다.
  - JPA의 `persist()`, `merge()` 등의 메소드를 사용한다면 좀 더 일반적인 `add()`, `update()`와 같은 이름의 DAO 메소드를 네이밍하자.

#### 예외처리
**데이터 액세스 중 발생하는 예외는 대부분 복구할 수 없다. 따라서 DAO 밖으로 던져질 때는 런타임 예외여야 한다.** 또한 DAO 메소드 선언부에 `throws SQLException`과 같은 내부 기술을 드러내는 예외를 직접 노출해선 안된다. `throws Exception`과 같은 무책임한 선언도 마찬가지다. 서비스 계층 코드는 DAO가 던지는 대부분의 예외는 직접 다뤄야 할 이유가 없다.

때로는 의미 있는 DAO가 던지는 예외를 잡아 비즈니스 로직에 적용하는 경우가 있다. **중복키 예외나 낙관적인 락킹 등이 대표적인 예다.** 하지만 이런 의미 있는 예외를 처리하려 할 때 JDBC나 각 데이터 액세스 기술에서 던져주는 예외에 일관성이 없기 때문에 구현 기술에 따라 달라지는 예외를 서비스 계층에서 알고 있어야만 한다는 문제가 발생한다.

이 때문에 스프링은 특정 기술이나 DB의 종류에 상관없이 **일관된 의미를 갖는 데이터 예외 추상화를 제공하고, 각 기술과 DB에서 발생하는 스프링의 데이터 예외로 변환해주는 변환 서비스를 제공한다.** `JdbcTemplate`과 같은 템플릿/콜백 기능을 사용하면 변환 서비스가 자동으로 적용된다. 데이터 액세스 기술의 API를 직접 사용할 때는 스프링에 내장된, AOP를 이용해 예외를 전환해주는 기능을 사용하면 된다.

최신 데이터 액세스 기술은 JDBC와는 다르게 런타임 예외를 사용한다. 기술에 독립적인 추상 예외로 전환하고 일관된 예외 복구 기능을 적용할 필요가 없다면 굳이 스프링의 예외 변환 서비스를 사용하지 않아도 된다.

### 템플릿과 API
데이터 액세스 기술을 사용하는 코드는 대부분 `try/catch/finally`와 판에 박힌 반복되는 코드로 작성되기 쉽다. 데이터 액세스 기술은 애플리케이션 외부의 리소스와 연동하므로 다양한 예외상황이 발생할 수 있다. **이런 예외상황에서 서버의 제한된 리소스에 누수가 발생하지 않도록 예외상황에서도 사용한 리소스를 적절히 반환해주는 코드가 반드시 필요하다. 이 때문에 코드가 길고 지저분해지기 쉽다.**

스프링은 DI의 응용 패턴인 템플릿/콜백 패턴을 이용해 이런 판에 박힌 코드를 피하고 꼭 필요한 바뀌는 내용만을 담을 수 있도록 데이터 액세스 기술을 위한 **템플릿을 제공한다.** 미리 만들어진 작업 흐름을 담은 **템플릿은 반복되는 코드를 제거해줄 뿐 아니라 예외 변환과 트랜잭션 동기화 기능도 함께 제공해준다.**

**템플릿의 단점은 데이터 액세스 기술의 API를 직접 사용하는 대신 템플릿이 제공하는 API를 이용해야 한다는 점**이다. 또, 콜백 오브젝트를 익명 내부 클래스로 작성해야 하는 경우엔 코드를 이해하기 조금 불편할 수 있다. 대부분의 기능은 내장 콜백을 사용하는 편리한 메소드를 이용하면 되기 때문에 큰 문제는 아니다.

스프링은 일부 데이터 액세스 기술을 템플릿 대신 해당 기술의 API를 그대로 사용하게 해주기도 한다. 데이터 액세스 기술이 제공하는 확장 기법과 AOP 등을 이용해 예외 변환과 트랜잭션 동기화를 해줄 수 있기 때문이다.

### DataSource
JDBC를 통해 DB를 사용하려면 `Connection` 타입의 DB 연결 오브젝트가 필요하다. **`Connection`은 모든 데이터 액세스 기술에서 필수 리소스다.** 사용자의 요청이 빈번하게 일어나는 엔터프라이즈 환경에선 각 요청마다 `Connection`을 새롭게 만들고 종료시킨다. 이는 비효율적이고 성능을 떨어뜨린다.

**그래서 보통 미리 정해진 개수만큼의 DB 커넥션을 pool에 준비해두고, 애플리케이션이 요청할 때마다 풀에서 꺼내 하나씩 할당해주고 다시 돌려받아 풀에 넣는 식의 풀링 기법을 이용한다.**

**스프링에선 `DataSource`를 하나의 독립된 빈으로 등록하도록 강력하게 권장한다.** 어떤 데이터 액세스 기술은 자체의 설정파일에 DB 연결정보를 담게 해서 해당 기술 내부에서 `DataSource`를 생성하고 직접 관리하는 경우가 있는데 스프링에선 그런 식으로 정보가 특정 기술 내부에 종속되어 있으면 곤란하다.

**스프링 데이터 액세스 기술의 다양한 서비스에서 `DataSource`를 필요로 하고 있기 때문에 공유 가능한 스프링 빈으로 등록해줘야 한다.** 다중 사용자를 갖는 엔터프라이즈 시스템에선 반드시 DB 연결 풀 기능을 지원하는 `DataSource`를 사용해야 한다.

스프링이 제공하는 단순한 `DataSource`는 운영환경에서 절대 사용하면 안 된다.

- `SimpleDriverDataSource`: 매번 DB 커넥션을 새로 만들고 따로 풀을 관리하지 않는 가장 단순한 클래스다.
- `SingleConnectionDataSource`: 하나의 물리적인 DB 커넥션만 만들어두고 이를 계속 사용하므로 동시에 두 개 이상의 스레드가 동작하는 경우엔 위험하다.

오픈소스 또는 상용 DB 커넥션 풀이 많이 사용되는데, 서버의 DB 풀로 등록해서 사용할 수도 있지만 일반적으론 애플리케이션 레벨에서 애플리케이션 전용 DB 풀을 만들어 사용한다.

- Apache Commons DBCP
- Hikari CP

## JDBC
스프링 JDBC는 기존 JDBC의 장점과 단순성을 그대로 유지하면서도 단점을 템플릿/콜백 페턴을 이용해 극복할 수 있게 해준다.

이 장의 내용을 간략하게 보고 넘어가자.

### 스프링 JDBC 기술과 동작 원리
스프링 JDBC가 해주는 작업

- `Connection` 열기와 닫기 : `Connection`을 열고 닫는 시점은 스프링 트랜잭션 기능과 맞물려 결정된다.
- `Statement` 준비와 닫기
- `Statement` 실행
- `ResultSet` 루프
- 예외처리와 변환
- 트랜잭션 처리
  - 스프링 JDBC는 트랜잭션 동기화 기법을 이용해 선언적 트랜잭션 기능과 맞물려 돌아간다.
  - 트랜잭션을 시작한 후에 스프링 JDBC의 작업을 요청하면 진행 중인 트랜잭션에 참여한다.
  - 트랜잭션이 없는 채로 호출될 경우 새로운 트랜잭션을 만들어 사용하고 작업을 마치면 트랜잭션을 종료한다.
  - 트랜잭션을 단일 리소스를 사용하는 로컬 트랜잭션을 적용할지, 서버가 제공하는 JTA 글로벌 트랜잭션에 참여할지 등의 문제는 스프링이 알아서 트랜잭션 선언을 이용해 처리하도록 맡기면 된다.

개발자가 해야 하는 일은 DB 커넥션을 가져올 `DataSource`를 정의해주는 것이다.

### 스프링 JDBC DAO
보통 DAO는 도메인 오브젝트 또는 DB 테이블 단위로 만든다. 따라서 애플리케이션 하나에 여러 개의 DAO가 만들어진다. `JdbcTemplate`, `SimpleJdbcTemplate`, `SimpleJdbcInsert` 등은 모두 멀티스레드 환경에서 안전하게 공유해서 사용할 수 있다. 따라서 각각을 싱글톤 빈으로 등록해두고 이를 DAO에서 DI 받아 사용해도 된다.

하지만 스프링 개발자는 DAO엔 `DataSource` 타입의 빈을 DI 받은 뒤 DAO 코드에서 필요한 템플릿 오브젝트 등을 생성해두고 사용하는 방법을 자주 쓴다.

가장 권장되는 작성은 DAO는 `DataSource`에만 의존하게 만들고 스프링 JDBC 오브젝트는 코드를 이용해 직접 생성하거나 초기화해서 DAO의 인스턴스 변수에 저장해두고 사용하는 것이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b6e92fe3-ceb0-4cf2-b1e6-236edcf047cc)

DAO는 `DataSource`를 DI 받긴 하지만 내부에 저장해둘 필요는 없다. 따라서 DI 주입용 메소드에서 필요한 오브젝트 생성 작업을 하고, 주입받은 `DataSource`는 저장하지 않아도 된다. 모든 JDBC 오브젝트는 한번 만들어지면 반복적으로 사용할 수 있기 때문에 초기에 만들어 인스턴스 변수에 저장해두는 게 좋다.

```java
public class MemberDao {
    private SimpleJdbcTemplate simpleJdbcTemplate;
    private SimpleJdbcInsert memberInsert;
    private SimpleJdbcCall memberFindCall;

    @Autowired
    public void init(DataSource dataSource) {
        this.simpletJdbcTemplate = new SimpleJdbcTemplate(dataSource);
        this.memberInsert = new SimpleJdbcInsert(dataSource).withTableName("member");
        this.memberFindCall = new SimpleJdbcCall(dataSource).withFunctionName("find_member");
    }

    // ...
}
```

`SimpleJdbcTemplate`을 생성하는 코드의 중복을 제거하는 방법은 두 가지다.

- `SimpleJdbcTemplate`을 독립적인 빈으로 등록하고 주입받는 방법이다.
- DAO의 공통 코드를 뽑아내 추상 클래스를 만들어두고 모든 DAO가 이를 상속하게 하는 것이다.
  - `SimpleJdbcTemplate`의 생성이나 `DataSource`의 주입 같은 부분은 슈퍼 클래스에서 처리하도록 만든다.
  - DAO 클래스에 상속관계를 만드는 데 문제가 없다면 가장 깔끔한 방법이다.

## JPA
### EntityManagerFactory 등록


## 트랜잭션
선언적 트랜잭션 경계설정을 사용하면 이전 방식의 문제를 모두 해결할 수 있다.

- 트랜잭션이 시작되고 종료되는 지점은 별도의 설정을 통해 결정된다.
- 작은 단위로 분리되어 있는 데이터 액세스 로직과 비즈니스 로직 컴포넌트와 메소드를 조합해서 하나의 트랜잭션에서 동작하게 만드는 것도 간단하다.
- 의미 있는 작은 단위로 만들어진 오브젝트와 메소드를 적절한 순서대로 조합해서 호출하기만 하면 코드의 중복 없이 다양한 트랜잭션 안에서 동작하는 코드를 만들 수 있다.

EJB의 선언적 트랜잭션 기능을 복잡한 환경이나 구현조건 없이 평범한 POJO로 만든 코드에 적용하게 해주는 것이 바로 스프링이다.

스프링의 선언적 트랜잭션은 매우 매력적인 기능이다. JavaEE 서버에서 동작하는 엔티티빈이나 JPA로 만든 컴포넌트에 JTA를 이용한 글로벌 트랜잭션을 적용해야만 가능했던 고급 기능을 간단한 톰캣 서버에서 동작하는 가벼운 애플리케이션에도 적용해주기 때문이다.

### 트랜잭션 추상화와 동기화
스프링이 제공하는 트랜잭션 서비스는 **트랜잭션 추상화와 트랜잭션 동기화 두 가지로 생각해볼 수 있다.**

트랜잭션 서비스의 종류는 데이터 액세스 기술보다 더 다양하다. 트랜잭션 서비스는 데이터 액세스 기술은 변하지 않더라도 환경에 따라 바뀔 수 있기 때문이다. 또, 스프링 없이 선언적 트랜잭션을 이용하려면 특정 기술과 서버 플랫폼, 특정 트랜잭션 서비스에 종속될 수 밖에 없다.

**스프링은 데이터 액세스 기술과 트랜잭션 서비스 사이의 종속성을 제거하고 스프링이 제공하는 트랜잭션 추상 계층을 이용해 트랜잭션 기능을 활용하도록 만들어준다.** 또한, **스프링의 트랜잭션 동기화는 트랜잭션을 일정 범위 안에서 유지해주고, 어디서든 자유롭게 접근할 수 있게 만들어준다.**

#### PlatformTransactionManager
스프링 트랜잭션 추상화의 핵심 인터페이스는 `PlatformTransactionManager`다. 모든 스프링의 트랜잭션 기능과 코드는 이 인터페이스를 통해 로우레벨의 트랜잭션 서비스를 이용할 수 있다.

```java
public interface PlatformTransactionManager {
    TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;

    void commit(TransactionStatus status) throws TransactionException;

    void rollback(TransactionStatus status) throws TransactionException;
}
```

`PlatformTransactionManager`는 트랜잭션 경계를 지정하는데 사용한다. **트랜잭션이 어디서 시작하고 종료하는지, 종료할 때 정상 종료인지 비정상 종료인지를 결정하는 것이다.** 스프링에선 시작과 종료를 트랜잭션 전파 기법을 이용해 자유롭게 조합하고 확장할 수 있다.

`getTransaction()` 메소드는 트랜잭션 속성에 따라 새로 시작하거나 진행 중인 트랜잭션에 참여하거나, 진행 중인 트랜잭션을 무시하고 새로운 트랜잭션을 만드는 식으로 상황에 따라 다르게 동작한다.

`TransactionDefinition`은 트랜잭션의 네 가지 속성을 나타내는 인터페이스다. `TransactionStatus`는 현재 참여하고 있는 트랜잭션의 ID와 구분정보를 담고 있다. 커밋 또는 롤백 시에 이 `TransactionStatus`를 사용한다.

#### 트랜잭션 매니저의 종류
스프링이 제공하는 `PlatformTransactionManager` 구현 클래스를 살펴보자.

- `DataSourceTransactionManager`
  - `Connection`의 트랜잭션 API를 이용해서 트랜잭션을 관리해주는 트랜잭션 매니저다.
  - 이 트랜잭션 매니저를 사용하려면 트랜잭션을 적용할 `DataSource`가 스프링의 빈으로 등록돼야 한다.
  - JDBC, MyBatis에 적용할 수 있다.
  - `DataSourceTransactionManager`가 사용할 `DataSource`는 `getConnection()`이 호출될 때마다 매번 새로운 `Connection`을 돌려줘야 한다. `ThreadLocal` 등을 이용해 트랜잭션을 저장해두고 돌려주는 특별한 기능을 가진 `DataSource`를 사용하면 안 된다.
  - `JdbcTemplate`이나 `SqlMapClientTemplate`처럼 내부에서 `Connection`과 트랜잭션 작업을 알아서 처리해주는 템플릿을 사용하는 방법이 제일 좋다.
  - 서버가 제공하는 `DataSource`와 트랜잭션 서비스를 JNDI로 접근해 사용해야 한다면 `DataSourceTransactionManager`는 사용할 수 없다. 그때는 JTA를 지원하는 스프링의 트랜잭션 매니저를 사용해야 한다.
  - 하나 이상의 DB에 대한 작업을 트랜잭션으로 묶어야 하는 경우에도 JTA를 써야 한다.
- `JpaTransactionManager`
  - JPA를 사용하는 DAO에서 사용한다.
  - JTA로 트랜잭션 서비스를 이용하는 경우엔 `JpaTransactionManager`가 필요 없다.
  - `JpaTransactionManager`엔 `LocalContainerEntityManagerFactoryBean` 타입의 빈을 프로퍼티로 등록해줘야 한다. (Spring Data JPA를 사용할 때 내부 구조를 찾아봐야 할 것 같다.)
  - `JpaTransactionManager`는 `DataSourceTransactionManager`가 제공하는 `DataSource` 레벨의 트랜잭션 관리 기능을 동시에 제공한다.
- `JtaTransactionManager`
  - 하나 이상의 DB 또는 트랜잭션 리소스가 참여하는 글로벌 트랜잭션을 적용하려면 JTA를 이용해야 한다.
  - 여러 개의 트랜잭션 리소스에 대한 작업을 하나의 트랜잭션으로 묶을 수 있고, 여러 대의 서버에 분산되어 진행되는 작업을 트랜잭션으로 연결해주기도 한다.
  - 트랜잭션 서비스를 제공하는 WAS를 이용하거나 독립 JTA 서비스를 제공해주는 프레임워크를 사용해야 한다.
  - DB가 하나라면 트랜잭션 매니저 또한 하나만 등록돼야 한다.
  - DB가 여러 개라도 JTA를 이용해 글로벌 트랜잭션을 적용할 것이라면 `JtaTransactionManager` 하나만 등록돼야 한다.
  - 단, 두 개 이상의 DB를 완전히 독립적으로 사용하는 경우라면 두 개 이상의 트랜잭션 매니저를 등록할 수는 있다.
  - DB가 두 개라면 `DataSource`도 두 개가 등록돼야 한다.

### 트랜잭션 경계설정 전략
트랜잭션의 시작과 종료가 되는 경계는 보통 서비스 계층 오브젝트의 메소드다. 비즈니스 로직이 거의 없어서 서비스 계층과 데이터 액세스 계층을 통합했다면, 통합된 계층의 메소드가 트랜잭션 경계가 될 것이다.

트랜잭션 경계를 설정하는 방법은 코드에 의한 프로그래밍적인 방법과, AOP를 이용한 선언적인 방법으로 구현할 수 있다.

#### 코드에 의한 트랜잭션 경계 설정
스프링의 트랜잭션 매니저는 모두 `PlatformTransactionManager`를 구현하고 있다. 따라서 이 인터페이스로 현재 등록되어 있는 트랜잭션 매니저 빈을 가져올 수 있다면 트랜잭션 매니저의 종류에 상관없이 동일한 방식으로 트랜잭션을 제어하는 코드를 만들 수 있다. 트랜잭션을 처리하기 위해 `PlatformTransactionManager`의 메소드를 직접 사용해도 되지만 `try/catch` 블록을 써야 하는 번거로움이 발생한다. 트랜잭션 안에서 작업 중 예외가 발생한 경우엔 트랜잭션을 롤백해주도록 만들어야 하기 때문이다.

**그래서 `PlatformTransactionManager`의 메소드를 직접 사용하는 대신 템플릿/콜백 방식의 `TransactionTemplate`을 이용하면 편리하다.**

스프링의 트랜잭션 서비스 추상화와 동기화 기법 덕에 기술에서 독립적인 트랜잭션 코드를 만들 수 있다.

```java
public class MemberService {
    @Autowired
    private MemberDao memberDao;
    private TransactionTemplate transactionTemplate;

    @Autowired
    public void init(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void addMembers(final List<Member> members) {
        this.transactionTemplate.execute(new TransactionCallback {
            public Object doInTransaction(TransactionStatus status) {
                for (Member m : members) {
                  memberDao.addMember(m);
                }
                return null;
            }
        });
    }
}
```

트랜잭션의 기본 속성을 변경하려면 `TransactionTemplate`을 만들 때 `TransactionDefinition` 오브젝트를 만들어서 파라미터로 제공해주면 된다. 기본 속성을 사용한다면 위의 코드처럼 한 번 만들어주고 재사용할 수도 있다.

**이런 방식보단 대개는 선언적 트랜잭션 방식을 많이 사용한다.** 이를 잘 사용하지 않더라도 알아두면 좋다.

#### 선언적 트랜잭션 경계 설정
선언적 트랜잭션을 사용하면 코드에는 전혀 영향을 주지 않음녀서 특정 메소드 실행 전후에 트랜잭션이 시작되고 종료되거나 기존 트랜잭션에 참여하도록 만들 수 있다. 이를 위해선 데코레이터 패턴을 적용한 트랜잭션 프록시 빈을 사용해야 한다.

**선언적 트랜잭션의 경계설정은 트랜잭션 프록시 빈 덕분에 가능한 것이다.** 트랜잭션은 대부분 성격이 비슷하기 때문에 적용 대상마다 일일이 선언해주기보단 일괄적으로 선언하는 것이 편리하다. 그래서 간단한 설정으로 특정 부가기능을 임의의 타깃 오브젝트에 부여해줄 수 있는 프록시 AOP를 주로 활용한다.

