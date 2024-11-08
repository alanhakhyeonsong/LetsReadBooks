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
JPA 퍼시스턴스 컨텍스트에 접근하고 엔티티 인스턴스를 관리하려면 JPA의 핵심 인터페이스인 `EntityManager`를 구현한 오브젝트가 필요하다. `EntityManager`는 JPA에서 두 가지 방식으로 관리된다.

- 애플리케이션이 관리하는 `EntityManager`
- 컨테이너가 관리하는 `EntityManager`

#### LocalEntityManagerFactoryBean
`LocalEntityManagerFactoryBean`은 JPA 스펙의 JavaSE 기동 방식을 이용해 `EntityManagerFactory`를 생성해준다. 이 방식은 JPA만을 사용하는 단순한 환경에 적용할 순 있지만 스프링에서 본격적으로 사용하기엔 많은 제약사항이 있다. 가장 큰 단점은 스프링의 빈으로 등록한 `DataSource`를 사용할 수 없다는 것이다. 스프링이 제공하는 바이트코드 위빙 기법도 적용할 수 없다.

많은 제약사항이 있기 때문에 굳이 사용하려 한다면 스프링 기반의 독립형 애플리케이션이나 통합 테스트 정도에서 사용할 수 있다. 실전에선 사용하지 않는게 좋으니 이런게 있다고만 알고 있자.

#### JavaEE 5 서버가 제공하는 EntityManagerFactory
JPA는 JavaEE에서 서버가 제공하는 JPA 프로바이더를 통해 사용하는 것이 일반적이다. 스프링 애플리케이션에선 JNDI를 통해 서버가 제공하는 `EntityManager`와 `EntityManagerFactory`를 제공받을 수 있다. 또 서버의 JTA를 이용해 트랜잭션 관리 기능을 활용할 수 있다.

#### LocalContainerEntityManagerFactoryBean
스프링이 직접 제공하는 컨테이너 관리 `EntityManager`를 위한 `EntityManagerFactory`를 만들어준다. 이 방법을 이용하면 JavaEE 서버에 배치하지 않아도 컨테이너에서 동작하는 JPA의 기능을 활용할 수 있을 뿐만 아니라, 스프링이 제공하는 일관성 있는 데이터 액세스 기술의 접근 방법을 적용할 수 있고 스프링의 JPA 확장 기능도 활용할 수 있다.

`LocalContainerEntityManagerFactoryBean`의 필수 프로퍼티는 빈으로 등록된 `dataSource`를 지정해주는 것뿐이다.

그 외에 다음과 같은 프로퍼티를 추가할 수 있다.

- `persistenceUnitName`
- `persistenceXmlLocation`
- `jpaProperties`, `jpaPropertyMap`
- `jpaVendorAdapter`
- `loadtimeWeaver`

---

JPA는 POJO 클래스를 ORM의 엔티티로 사용한다. **POJO 방식의 단점은 한번 오브젝트가 만들어지면 그 뒤엔 컨테이너가 직접 관리할 수 없다는 점이다.** 엔티티 사이의 관계도 JPA의 인터페이스를 이용해 접근하지 않고 직접 POJO 오브젝트끼리 연결되어 있다. 따라서 JPA 프로바이더나 컨테이너가 특별한 기능을 제공하기 위해 끼어들 여지가 없다.

JPA는 그래서 단순한 자바 코드로 만들어진 엔티티 클래스의 **바이트코드를 직접 조작해서 확장된 기능을 추가하는 방식을 이용한다.** 이를 통해 엔티티 오브젝트 사이에 지연된 로딩이 가능하고, 엔티티 값의 변화를 추적할 수 있으며, 최적화와 그룹 페칭 등의 고급 기능을 적용할 수 있다. 이렇게 이미 컴파일된 클래스 바이트코드를 조작해서 새로운 기능을 추가하는 것을 **바이트코드 향상 기법**이라 한다.

클래스의 바이트코드를 향상시키는 방법은 두 가지가 있다.

- 바이트코드를 빌드 중에 변경하는 것
- 런타임 시에 클래스 바이트코드를 메모리에 로딩하면서 다이내믹하게 바이트코드를 변경해서 기능을 추가하는 것

**두 번째 방법을 로드타임 위빙이라 하고 이런 기능을 가진 클래스를 로드타임 위버라고 부른다.**

Java 에이전트는 JVM을 통해 로딩되는 모든 클래스를 일일이 다 확인하므로 성능이 좋지 않다. **그래서 스프링은 Java 에이전트를 대신할 수 있는 특별한 클래스 로더를 이용해서 로드타임 위빙 기능을 적용할 수 있는 방법을 제공한다.** 스프링에는 JPA의 엔티티 클래스 향상 외에도 바이트코드 조작이 필요한 기능이 있다.

---

#### 트랜잭션 매니저
컨테이너가 관리하는 `EntityManager` 방식에선 컨테이너가 제공하는 트랜잭션 매니저가 반드시 필요하다. 애플리케이션 관리 `EntityManager`에선 코드에서 직접 트랜잭션을 제어할 수 있지만, 이 방법은 테스트 목적이 아니라면 권장되지 않는다.

따라서 스프링의 `EntityManager`를 사용하려면 **적절한 트랜잭션 매니저가 등록되어 있어야 한다.** 스프링 JDBC는 트랜잭션 매니저가 없어도 동작한다. JDBC 자체가 자동 트랜잭션 모드를 갖고 있기 때문에 명시적으로 관리를 해주지 않아도 된다. 반면 JPA는 반드시 트랜잭션 안에서 동작하도록 설계되어 있다.

`JpaTransactionManager`를 등록하고 `EntityManagerFactory` 빈을 프로퍼티에 등록해주면 된다. JPA를 사용하는 DAO 코드는 스프링이 관리하는 트랜잭션 관리 기능을 이용할 수 있다. `@Transactional`이나 트랜잭션 AOP를 이용해 트랜잭션 경계설정을 해주면 자동으로 JPA 트랜잭션을 시작하고 커밋하도록 만들 수 있다.

`JpaTransactionManager`를 사용하면 같은 `DataSource`를 공유하는 JDBC DAO와 트랜잭션을 공유할 수도 있다. JPA가 JTA 트랜잭션을 이용하는 경우라면 `JtaTransactionManager`를 사용해야 한다.

### EntityManager와 JpaTemplate
`EntityManager`를 사용해 JPA 코드를 작성하는 가장 대표적인 방법은 컨테이너가 제공하는 `EntityManager`를 직접 제공받아 사용하는 것이다. DAO가 컨테이너로부터 `EntityManager`를 직접 주입받으려면 JPA의 `@PersistenceContext`를 사용해야 한다.

`EntityManager`는 스프링의 빈으로 등록되지 않는다. 빈으로 등록한 것은 `EntityManagerFactory` 타입의 빈을 생성하는 `LocalContainerEntityManagerFactoryBean`이다. 따라서 `@Autowired` 같은 스프링의 DI 방법으론 `EntityManager`를 주입받을 수 없다.

하지만 스프링엔 JPA의 스펙에 나오는 JavaEE 컨테이너가 관리하는 `EntityManager`를 주입받는 방법을 스프링 애플리케이션의 코드에도 동일하게 사용할 수 있다.

```java
public class MemberDao {
    @PersistenceContext
    EntityManager em;

    public void addMember(Member member) {
        em.persist(member);
    }
}
```

원래 `EntityManager`는 이렇게 인스턴스 변수에 한 번 주입받아 계속 재사용할 수 있는게 아니다. `EntityManager`는 그 자체로 멀티스레드에서 공유해서 사용할 수 없다. `Connection`을 하나 가져와 DAO에서 계속 재사용할 수 없는 것과 마찬가지다. 사용자의 요청에 따라 만들어지는 스레드별로 독립적인 `EntityManager`가 만들어져 사용돼야 한다. **트랜잭션마다 하나씩만 만들어져서 사용되고 종료되면 함께 제거돼야 하는데 이것이 가능한 이유는 `@PersistenceContext`로 주입받는 `EntityManager`는 실제 `EntityManager`가 아니라 현재 진행 중인 트랜잭션에 연결되는 퍼시스턴스 컨텍스트를 갖는 일종의 프록시기 때문이다.**

`EntityManager`의 `type` 엘리먼트는 디폴트 값인 `PersistenceContextType.TRANSACTION`이 적용된다.

이렇게 트랜잭션 스코프의 컨텍스트를 갖는 `EntityManager`를 사용하면 번거롭게 `EntityManagerFactory`로부터 `EntityManager`를 매번 생성할 필요가 없을 뿐만 아니라 트랜잭션 동기화를 위해 스프링이 제공해주는 템플릿/콜백 방식을 사용하지 않아도 된다.

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

AOP를 이용해 트랜잭션 기능을 부여하는 방법은 다양하지만 보통 다음 두 가지 방법이 가장 많이 사용된다.

- AOP와 tx 네임 스페이스
- `@Transactional`

`@Transactional`을 이용하는 것은 설정파일에 명시적으로 포인트컷과 어드바이스를 정의하지 않는다. 대신 트랜잭션이 적용될 타깃 인터페이스나 클래스, 메소드 등에 `@Transactional` 애노테이션을 부여해서 트랜잭션 대상으로 지정하고 트랜잭션의 속성을 제공한다.

```java
@Transactional
public interface MemberDao {
    public void add(Member m);

    public void add(List<Member> members);

    public void deleteAll();

    @Transactional(readOnly = true)
    public long count();
}
```

인터페이스에 붙은 `@Transactional` 선언은 인터페이스 내 모든 메소드에 적용된다. 동시에 메소드 레벨에도 이 애노테이션을 지정할 수 있다. 이땐 메소드의 선언이 인터페이스의 선언에 우선한다.

**메소드에 `@Transactional`이 있으면 클래스 레벨의 선언보다 우선해서 적용된다.** 클래스에 `@Transactional`을 부여했을 때 트랜잭션이 적용되는 메소드는 프록시 방식과 인터페이스의 여부에 따라 조금 달라진다. 클래스의 선언은 인터페이스의 선언보다 우선한다.

트랜잭션 경계설정 방법 두 가지는 각기 장단점이 있다. aop와 tx 스키마의 태그를 이용하는 방식은 선언이 조금 복잡해 보이지만 코드엔 전혀 영향을 주지 않고 일괄적으로 트랜잭션을 적용하거나 변경할 수 있다는 장점이 있다. `@Transactional`을 일일이 대상 인터페이스나 클래스, 메소드에 부여하는 건 상대적으로 번거로운 작업이다. 반면 `@Transactional`은 aop와 tx 스키마의 태그를 사용하는 경우보다 훨씬 세밀한 설정이 가능하다.

#### 프록시 모드: 인터페이스와 클래스
**스프링의 AOP는 기본적으로 다이내믹 프록시 기법을 이용해 동작한다.** 다이내믹 프록시를 적용하려면 인터페이스가 있어야 한다. 인터페이스의 사용은 DI에서도 가장 기본 원칙인 만큼 문제 될 것은 없다. 하지만 특별한 경우에 인터페이스를 구현하지 않은 클래스에 트랜잭션을 적용해야 할 수도 있다. 인터페이스가 없는 레거시 클래스 코드를 그대로 가져다 사용하는데 수정은 불가하고 대신 스프링의 트랜잭션의 경계설정 대상으로 만들고 싶다면 어떻게 해야 할까?

이때는 스프링이 지원하는 클래스 프록시 모드를 사용하면 된다. 스프링에선 JDK 다이내믹 프록시 외에도 CGLib 라이브러리가 제공해주는 클래스 레벨의 프록시도 사용할 수 있다. 클래스 프록시는 aop/tx 스키마를 사용하는 경우와 `@Transactional`을 사용하는 경우 모두 이용할 수 있다.

- `@Transactional`은 클래스에 부여해야 한다.
  - **클래스 프록시는 일반적으로 인터페이스를 구현하지 않는 클래스에 주로 사용된다.**
  - 하지만 원한다면 인터페이스가 있는 클래스에 클래스 프록시를 강제로 적용할 수도 있다. 이땐 반드시 클래스에 `@Transactional`을 부여해줘야 한다.
  - 인터페이스에 붙인 `@Transactional` 애노테이션은 구현 클래스로 그 정보가 전달되지 않는다. 따라서 인터페이스에만 `@Transactional`을 부여하고 클래스 프록시 모드로 바꾸면 트랜잭션이 적용되지 않는다.
  - `@Transactional`을 클래스에 부여한다 해서 클래스 프록시가 적용되는 것은 아니다.
- 클래스 프록시의 제약사항을 알아야 한다.
  - **클래스 프록시는 `final` 클래스엔 적용할 수 없다.** 타깃 클래스를 상속해서 프록시를 만드는 방법을 사용하기 때문에 상속이 불가한 `final` 클래스엔 적용되지 않는다.
  - **클래스 프록시를 적용하면 클래스의 생성자가 두 번 호출된다.** 상속을 통해 프록시를 만들기 때문에 발생하는 현상인데, 이 때문에 **생성자에서 리소스를 할당하는 것 같은 중요한 작업은 피하도록 해야 한다.**
- 불필요한 메소드에 트랜잭션이 적용될 수 있다.
  - 클래스 프록시 방식을 사용하면 모든 `public` 메소드에 트랜잭션이 적용된다. 따라서 수정자 같은, 클라이언트가 사용하진 않지만 `public`으로 정의하는 메소드에도 트랜잭션이 적용되는 문제가 발생한다.
  - 아무런 DB 작업이 없으므로 빈 트랜잭션이 시작됐다 종료되지만 그만큼 시간과 리소스에 낭비가 발생한다.

**클래스 프록시는 코드를 함수로 손댈 수 없는 레거시 코드나, 여러 제한 때문에 인터페이스를 사용하지 못했을 경우에만 사용해야 한다.** 인터페이스를 사용하는 이유는 단지 트랜잭션 AOP를 적용하는 것이 전부가 아니다. 비록 스프링이 런타임 바이트코드 생성 기법을 지원하는 라이브러리를 이용해서 클래스에도 프록시를 적용하도록 해주곤 있지만, 이 방법을 남용하는 것은 적절하지 않다.

#### AOP 방식: 프록시와 AspectJ
**스프링의 AOP는 기본적으로 프록시 방식이다.** JDK 다이내믹 프록시든 CGLib이든 모두 프록시 오브젝트를 타깃 오브젝트 앞에 두고 호출 과정을 가로채 트랜잭션과 같은 부가적인 작업을 진행해준다.

