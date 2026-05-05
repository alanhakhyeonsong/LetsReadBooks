# 1장. IoC 컨테이너와 DI

스프링은 본질적으로 **POJO 클래스와 빈 설정 메타정보**로 구성된 애플리케이션을, **IoC 컨테이너**가 조립·실행해주는 프레임워크다. 이 장에서는 컨테이너 자체의 종류, 빈 등록·DI·라이프사이클 설정 방법, 그리고 스프링 3.1에서 강화된 자바 코드 기반 설정과 환경 추상화를 정리한다.

---

## 1.1 IoC 컨테이너: 빈 팩토리와 애플리케이션 컨텍스트

### 1.1.1 IoC 컨테이너란

스프링 애플리케이션은 결국 두 가지로 환원된다.

- **POJO 클래스**: 비즈니스 로직과 인프라 기능을 담는 일반 자바 클래스
- **빈 설정 메타정보(BeanDefinition)**: 어떤 POJO를 빈으로 등록하고 어떻게 조립할지 기술한 정보

IoC 컨테이너는 메타정보를 읽어 POJO를 인스턴스화하고, **의존관계를 주입(DI)** 한 뒤 라이프사이클을 관리한다. 핵심은 *제어의 역전*이다 — 객체를 직접 `new` 하지 않고 컨테이너에게 맡긴다.

### 1.1.2 빈 팩토리와 애플리케이션 컨텍스트

| 구분 | BeanFactory | ApplicationContext |
| --- | --- | --- |
| 역할 | 빈 등록·생성·조회의 **최소 기능** | BeanFactory + 엔터프라이즈 부가 기능 |
| 부가 기능 | 없음 | 메시지 소스, 이벤트, 리소스 로딩, AOP, 환경 추상화 |
| 사용 위치 | 거의 직접 사용하지 않음 | **실질적인 표준 컨테이너** |

> 실무에서는 거의 항상 `ApplicationContext`를 사용한다. `BeanFactory`는 그 부모 인터페이스로 알고만 두면 된다.

### 1.1.3 IoC 컨테이너 계층구조

- 컨테이너는 **부모-자식 관계**로 계층화 가능
- 자식 컨텍스트는 부모의 빈을 참조할 수 있지만, 부모는 자식의 빈을 모른다
- 같은 이름이면 **자식이 우선** (오버라이드)
- 대표 사례: 웹 애플리케이션의 **Root + Servlet 컨텍스트**

### 1.1.4 IoC/DI를 위한 빈 설정 메타정보 작성

`BeanDefinition` 인터페이스가 곧 메타정보의 추상화다. 핵심 항목:

- **beanClassName**: 빈으로 만들 클래스의 FQCN
- **parentName**: 부모 BeanDefinition (속성 상속)
- **scope**: singleton(기본), prototype, request, session, …
- **lazyInit**: 컨텍스트 시작 시 즉시 생성 vs. 첫 요청 시 생성
- **dependsOn**: 다른 빈이 먼저 생성되어야 함을 명시
- **autowireCandidate**: 자동 와이어링 후보로 포함할지 여부
- **primary**: 동일 타입 중 자동 주입 우선순위
- **abstract**: 직접 인스턴스화하지 않고 부모 빈으로만 사용
- **autowireMode**: byName / byType / constructor / no
- 프로퍼티/생성자 인자 값과 참조

이 메타정보는 XML, 애노테이션, 자바 코드 등 **다양한 방법**으로 만들 수 있고 결국 같은 `BeanDefinition`이 된다.

### 1.1.5 IoC 컨테이너의 종류

#### StaticApplicationContext

- 코드에서 직접 빈을 등록 — 학습/테스트용
- 실무에서 운영 컨텍스트로 쓰지 않는다

#### GenericApplicationContext

- 가장 **일반적인** 컨텍스트
- `XmlBeanDefinitionReader`, `PropertiesBeanDefinitionReader` 등으로 외부 메타정보를 읽어 들인다
- `refresh()` 호출 후 사용 시작

#### GenericXmlApplicationContext

- XML 전용 편의 클래스
- `new GenericXmlApplicationContext("appCtx.xml")`

#### WebApplicationContext

- 웹 환경 전용 인터페이스
- 기본 구현은 `XmlWebApplicationContext`
- `ServletContext`에 바인딩되어 어디서든 꺼내 쓸 수 있다
- 스프링 3.1부터 `AnnotationConfigWebApplicationContext` 추가

웹 환경에서의 등록은 `web.xml`로 한다.

```xml
<listener>
  <listener-class>
    org.springframework.web.context.ContextLoaderListener
  </listener-class>
</listener>
<context-param>
  <param-name>contextConfigLocation</param-name>
  <param-value>/WEB-INF/applicationContext.xml</param-value>
</context-param>

<servlet>
  <servlet-name>spring</servlet-name>
  <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
  <load-on-startup>1</load-on-startup>
</servlet>
```

> 루트 컨텍스트는 모든 서블릿이 공유하고, 서블릿 컨텍스트(예: DispatcherServlet)는 자신만의 빈(컨트롤러 등)을 갖는다.

---

## 1.2 IoC/DI를 위한 빈 설정 메타정보 작성

빈 등록 방법은 크게 **XML / 빈 자동인식(애노테이션) / 자바 코드** 세 가지다.

### 1.2.1 빈 등록 방법

#### XML `<bean>`

```xml
<bean id="userDao" class="myproject.UserDaoImpl"/>
```

- 가장 명시적이지만, 빈이 늘어나면 관리가 번거롭다
- 외부 라이브러리 클래스(소스 수정 불가)는 여전히 XML로 등록

#### XML 네임스페이스 전용 태그

```xml
<context:property-placeholder location="classpath:db.properties"/>
<tx:annotation-driven/>
<aop:aspectj-autoproxy/>
```

- 컨테이너 인프라 빈을 한 줄로 등록
- 내부적으로는 결국 `<bean>`으로 변환

#### 애노테이션 기반 자동 등록 (`@Component`)

```xml
<context:component-scan base-package="myproject"/>
```

- `@Component`, `@Repository`, `@Service`, `@Controller` (스테레오타입) 부착 클래스를 스캐너가 찾아서 자동 등록
- 빈 ID는 기본적으로 클래스 이름 첫 글자 소문자

#### `@Configuration` + `@Bean` 자바 코드

```java
@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() { ... }

    @Bean
    public UserDao userDao() {
        return new UserDaoImpl(dataSource());
    }
}
```

- 외부 라이브러리도 코드로 직접 빈 정의 가능
- 컴파일 타입 검증, IDE 지원의 이점

### 1.2.2 빈 의존관계 설정

#### XML `<property>`, `<constructor-arg>`

```xml
<bean id="userDao" class="myproject.UserDaoImpl">
  <property name="dataSource" ref="dataSource"/>
</bean>
```

#### 자동 와이어링 (`autowire`)

- `byName`: 프로퍼티 이름과 같은 빈을 찾아 주입
- `byType`: 타입이 일치하는 빈을 찾아 주입 (둘 이상이면 예외)
- `constructor`: 생성자 파라미터 타입 기반
- 권장 X — 명시적 설정에 비해 가독성 떨어짐

#### `@Resource`

- JSR-250 표준
- 기본 **byName** (이름이 없으면 byType)

#### `@Autowired`

- 스프링 표준
- 기본 **byType**, 같은 타입 다수면 이름으로 보조 매칭
- 필드/세터/생성자/임의 메서드에 부착 가능
- `required = false`로 선택적 의존성 표현

#### `@Inject`

- JSR-330 표준, `@Autowired`와 사실상 동일

#### `@Qualifier`

- 같은 타입이 여러 개일 때 보조 키로 식별
- `@Autowired`와 함께 사용

```java
@Autowired
@Qualifier("mainDataSource")
private DataSource dataSource;
```

#### `@Resource` vs `@Autowired` 가이드

- 빈 이름으로 명확히 식별하고 싶으면 `@Resource`
- 타입 기반·다형성 활용 + 선택적 의존성이면 `@Autowired`
- **혼용은 자제** — 팀 컨벤션을 정해 한쪽으로 통일

### 1.2.3 프로퍼티 값 설정

빈의 의존관계는 **다른 빈**, 프로퍼티 값은 **단순 정보**(문자열/숫자/플래그)다.

#### XML

```xml
<property name="username" value="spring"/>
<property name="port" value="8080"/>
```

- `${...}` 치환자는 `<context:property-placeholder>`가 외부 properties 파일에서 가져온다

#### 컬렉션 / 맵

```xml
<property name="urls">
  <list>
    <value>http://example.com/a</value>
    <value>http://example.com/b</value>
  </list>
</property>
```

#### `@Value`

```java
@Value("${db.username}")
private String username;
```

- SpEL(`#{...}`) 표현식도 사용 가능

### 1.2.4 컨테이너가 자동 등록하는 빈

다음 빈들은 따로 등록하지 않아도 컨테이너가 자동으로 노출한다.

- `ApplicationContext` / `BeanFactory`: `ApplicationContextAware`, `BeanFactoryAware` 또는 `@Autowired`로 주입 가능
- `systemProperties`: JVM 시스템 프로퍼티(`Map<String,String>`)
- `systemEnvironment`: OS 환경변수
- `messageSource`, `applicationEventPublisher` 등 인프라 빈

### 1.2.5 컨테이너 인프라 빈

- `<context:annotation-config>`: `@Autowired`, `@Resource`, `@PostConstruct` 등 처리기를 등록
- `<context:component-scan>`: 위 인프라 빈 + 빈 스캐너 동시 등록
- 스프링 3.1: 이런 컨테이너 인프라 빈에는 **`ROLE_INFRASTRUCTURE`** 역할이 부여돼 애플리케이션 빈과 구분된다 (`@Role`)

---

## 1.3 프로토타입과 스코프

### 1.3.1 프로토타입 스코프

- 기본 스코프는 **싱글톤** — 컨테이너 안에 단 하나
- **프로토타입**: `getBean()`을 호출할 때마다 **새 인스턴스**

```xml
<bean id="dailyJob" class="myproject.Job" scope="prototype"/>
```

```java
@Component
@Scope("prototype")
public class Job { ... }
```

#### 사용 시점

- 사용자 요청별로 상태를 가지는 객체
- 매번 다른 값을 만들어야 하는 도우미 객체
- 스레드 안전한 싱글톤으로 만들기 어려운 경우

#### 프로토타입 빈을 싱글톤이 의존할 때의 함정

싱글톤이 프로토타입을 필드로 주입받으면, 그 필드는 **컨테이너 시작 시 한 번** 주입된다. 이후엔 같은 인스턴스를 계속 쓰게 되어 의도와 어긋난다.

해결책 4가지:

1. **`ApplicationContextAware`**: 매번 `context.getBean()` 호출 — 컨테이너에 강결합
2. **`ObjectFactory<T>` / `ObjectProvider<T>`**: 스프링 제공 팩토리, `getObject()` 호출 시점에 새 인스턴스
3. **JSR-330 `Provider<T>`**: 표준 — `provider.get()`
4. **lookup-method 주입**: 추상 메서드를 컨테이너가 동적으로 오버라이드해 새 인스턴스 반환

### 1.3.2 스코프

- 기본 제공: `singleton`, `prototype`, `request`, `session`, `globalSession`, `application`
- **스코프 프록시**: 싱글톤이 더 짧은 스코프 빈을 주입받으려면 프록시가 필요

```java
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoginUser { ... }
```

```xml
<bean id="loginUser" class="..." scope="request">
  <aop:scoped-proxy/>
</bean>
```

#### 커스텀 스코프

- `Scope` 인터페이스 구현
- `CustomScopeConfigurer`로 등록

---

## 1.4 기타 빈 설정 메타정보

### 1.4.1 빈 이름

- `id`, `name` 둘 다 사용 가능
- `name`은 콤마/공백 구분으로 **별칭** 부여

### 1.4.2 빈 라이프사이클 메서드

빈이 생성되어 사용되기 전 **초기화**, 컨테이너 종료 시 **소멸** 작업을 정의할 수 있다.

#### 인터페이스 방식

```java
public class MyBean implements InitializingBean, DisposableBean {
    public void afterPropertiesSet() { ... } // 초기화
    public void destroy() { ... }            // 소멸
}
```

- 인터페이스가 스프링에 종속 → 권장 X

#### XML 속성

```xml
<bean id="myBean" class="..." init-method="initialize" destroy-method="cleanup"/>
```

- `default-init-method`, `default-destroy-method`로 일괄 지정 가능

#### 표준 애노테이션

```java
@PostConstruct
public void initialize() { ... }

@PreDestroy
public void cleanup() { ... }
```

- JSR-250 표준 — **권장**
- `<context:annotation-config>` 또는 `<context:component-scan>` 필요

#### `@Bean` 속성

```java
@Bean(initMethod = "initialize", destroyMethod = "cleanup")
public MyBean myBean() { ... }
```

### 1.4.3 팩토리 빈

- 일반 클래스로 만들 수 없는 객체(생성자가 private 이거나 복잡한 빌드 절차)를 빈으로 만들 때 사용
- 세 가지 방식

#### `FactoryBean<T>` 인터페이스 구현

```java
public class JdkProxyFactoryBean implements FactoryBean<Object> {
    public Object getObject() { ... }
    public Class<?> getObjectType() { ... }
    public boolean isSingleton() { return true; }
}
```

#### static factory-method

```xml
<bean id="connection" class="java.sql.DriverManager" factory-method="getConnection">
  <constructor-arg value="jdbc:..."/>
</bean>
```

#### instance factory-method

```xml
<bean id="connectionFactory" class="..."/>
<bean id="connection" factory-bean="connectionFactory" factory-method="create"/>
```

#### `@Bean` 메서드는 사실상 instance factory-method

`@Configuration` 클래스의 `@Bean` 메서드도 **`AppConfig` 빈**의 인스턴스 메서드를 호출해 빈을 만든 셈이다.

---

## 1.5 스프링 3.1의 IoC 컨테이너와 DI

스프링 3.0까지의 IoC/DI 기능은 이미 충분히 성숙했다. **3.1**은 그 위에 두 가지 큰 흐름을 더했다.

1. **자바 코드 메타정보**의 완전한 자립 — XML 없이도 모든 설정 가능
2. **런타임 환경 추상화** — 프로파일, 프로퍼티 소스로 환경별 설정 손쉽게

### 1.5.1 빈의 역할과 빈 등록 메타정보의 분류

스프링 3.1은 빈을 **역할(Role)** 로 구분한다.

- **애플리케이션 빈**: 개발자가 작성한 도메인/서비스 로직 빈
- **인프라스트럭처 빈**: 컨테이너 동작을 위한 인프라 빈 — `<context:annotation-config>` 같은 전용 태그가 등록하는 빈들이 `ROLE_INFRASTRUCTURE` 값을 가진다
- **(서포트) 빈**: 라이브러리에서 제공하는 컴포넌트

3.0까지 빈의 역할 속성은 컨테이너 내부에서만 쓰였지만, 3.1부터는 `@Role`로 직접 지정할 수 있다.

### 1.5.2 컨테이너 인프라 빈을 위한 자바 코드 메타정보

IoC/DI 설정 방법의 발전:

| 버전 | 특징 |
| --- | --- |
| **1.x** | XML `<bean>`만 사용 — 모든 종류의 빈이 한 파일에 혼재 |
| **2.0** | 전용 태그(`<aop:config>`, `<tx:advice>`)와 네임스페이스로 인프라 빈 분리 |
| **2.5** | 빈 스캐너 + `@Component` 등 스테레오타입 — 애플리케이션 로직 빈은 자바 코드로 작성 |
| **3.0** | `@Configuration` + `@Bean`으로 자바 코드 기반 빈 정의 가능. 단, 인프라 빈은 여전히 XML 전용 태그가 우세 |
| **3.1** | 자바 코드만으로 모든 설정 가능. `@EnableXxx`, `@ComponentScan` 등 메타 애노테이션 도입 |

#### 3.1의 핵심 인프라 애노테이션

- `@ComponentScan(basePackages = "myproject")` ↔ `<context:component-scan>`
- `@EnableTransactionManagement` ↔ `<tx:annotation-driven>`
- `@EnableAspectJAutoProxy` ↔ `<aop:aspectj-autoproxy>`
- `@EnableWebMvc` ↔ `<mvc:annotation-driven>`
- `@Import(OtherConfig.class)` — 다른 `@Configuration` 클래스 합치기

```java
@Configuration
@ComponentScan("myproject")
@EnableTransactionManagement
public class AppConfig {
    @Bean
    public DataSource dataSource() { ... }
}
```

### 1.5.3 새로운 웹 IoC 컨테이너 구성 (`AnnotationConfigWebApplicationContext`)

`web.xml`에서 컨텍스트 클래스를 바꿔주면 자바 설정 클래스를 그대로 웹 컨테이너 설정으로 사용할 수 있다.

#### 루트 컨텍스트

```xml
<context-param>
  <param-name>contextClass</param-name>
  <param-value>
    org.springframework.web.context.support.AnnotationConfigWebApplicationContext
  </param-value>
</context-param>
<context-param>
  <param-name>contextConfigLocation</param-name>
  <param-value>myproject.config.AppConfig</param-value>
</context-param>

<listener>
  <listener-class>
    org.springframework.web.context.ContextLoaderListener
  </listener-class>
</listener>
```

- 클래스 이름 대신 **패키지 이름**을 주면 그 패키지 내 모든 `@Configuration` 클래스를 자동으로 사용
- 여러 클래스는 콤마/공백 구분으로 나열

#### 서블릿 컨텍스트

```xml
<servlet>
  <servlet-name>spring</servlet-name>
  <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
  <init-param>
    <param-name>contextClass</param-name>
    <param-value>
      org.springframework.web.context.support.AnnotationConfigWebApplicationContext
    </param-value>
  </init-param>
  <init-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>myproject.config.WebConfig</param-value>
  </init-param>
  <load-on-startup>1</load-on-startup>
</servlet>
```

> `AnnotationConfigWebApplicationContext`는 내부적으로 `<context:annotation-config>`가 등록하는 인프라 빈을 자동으로 추가해 주므로, 별도의 설정 없이 각종 빈 후처리기·자동 와이어링이 동작한다.

### 1.5.4 런타임 환경 추상화와 프로파일

#### `Environment` 인터페이스

스프링 3.1은 **런타임 환경**을 `Environment` 객체로 추상화한다. `Environment`는 두 가지를 포함한다.

- **활성 프로파일(active profiles)**: 현재 환경에서 어떤 프로파일을 활성화할지
- **프로퍼티 소스(property sources)**: 환경에 따라 달라지는 프로퍼티 값을 통합 조회

```java
@Autowired
Environment env;

env.getProperty("db.username");
env.getActiveProfiles();
```

#### 프로파일

빈 정의를 환경별로 묶어두고, 활성 프로파일에 해당하는 것만 등록되게 한다.

#### XML

```xml
<beans profile="dev">
  <bean id="dataSource" class="...BasicDataSource">...</bean>
</beans>

<beans profile="production">
  <jee:jndi-lookup id="dataSource" jndi-name="jdbc/myDS"/>
</beans>
```

- 한 `<beans>`에 여러 프로파일 — `profile="dev, test"` (OR)

#### 자바 코드

```java
@Configuration
@Profile("dev")
public class DevDataSourceConfig {
    @Bean public DataSource dataSource() { ... }
}
```

- 클래스/메서드 단위 모두 부착 가능

#### 활성 프로파일 지정 방법 (우선순위 높은 순)

1. 서블릿 초기화 파라미터 `<init-param> spring.profiles.active`
2. 서블릿 컨텍스트 파라미터 `<context-param> spring.profiles.active`
3. JNDI 환경 변수 `spring.profiles.active`
4. 시스템 프로퍼티 `-Dspring.profiles.active=dev`
5. OS 환경변수 `SPRING_PROFILES_ACTIVE=dev`

```xml
<context-param>
  <param-name>spring.profiles.active</param-name>
  <param-value>dsDev, mockMailServer</param-value>
</context-param>
```

> WAS의 JNDI 환경 값을 활용하면 가벼운 톰캣 같은 컨테이너에서도 환경별로 프로파일 전환이 가능하다. 활성 프로파일은 **두 가지 이상** 동시 사용 가능 (DB 프로파일 + 메일 프로파일 분리).

### 1.5.5 프로퍼티 소스

프로퍼티 소스는 `${...}`/`@Value("${...}")`로 참조되는 **외부화된 값들의 출처**를 추상화한다.

#### 통합된 우선순위

```
서블릿 초기화 파라미터 > 서블릿 컨텍스트 파라미터 > JNDI > 시스템 프로퍼티 > 시스템 환경변수 > 별도 등록한 properties 파일
```

#### `Environment.getProperty()`

```java
@Autowired Environment env;

String os = env.getProperty("os.name");
```

- 빈 안에서 자주 쓰인다면 `@PostConstruct` 시점에 한 번만 읽어 필드에 저장

#### `@PropertySource` 등록

```java
@Configuration
@PropertySource("classpath:/db.properties")
public class AppConfig { ... }
```

- 자바 코드로 properties 파일을 환경에 추가

#### `PropertySourcesPlaceholderConfigurer` ≠ `PropertyPlaceholderConfigurer`

| 항목 | `PropertyPlaceholderConfigurer` (≤3.0) | `PropertySourcesPlaceholderConfigurer` (3.1) |
| --- | --- | --- |
| 출처 | 지정한 properties 파일 | **환경 오브젝트의 모든 프로퍼티 소스** |
| `@Value`/`${...}` 치환 | 가능 | 가능 |
| 추가 등록 필요 | 필요 | `@PropertySource`만으로도 충분 |

```java
@Bean
public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
}
```

> **3.1에서는 `PropertySourcesPlaceholderConfigurer` 사용을 권장** — 통합된 환경 오브젝트와 일관되게 동작한다.

#### 컨텍스트 초기화 오브젝트(`ApplicationContextInitializer`)

- 컨텍스트 `refresh` 직전에 환경 오브젝트를 수정하거나 프로퍼티 소스를 추가
- DB나 원격 서버에서 프로퍼티를 읽어와야 할 때 같이 코드 기반 초기화에만 한정해서 쓴다 — 활성 프로파일 지정에는 외부 파라미터로 충분

```xml
<context-param>
  <param-name>contextInitializerClasses</param-name>
  <param-value>myproject.MyContextInitializer</param-value>
</context-param>
```

---

## 1.6 정리

스프링은 매우 다양하고 세련된 IoC/DI 방법을 제공한다. 그만큼 선택의 폭이 크기 때문에 **처음부터 컨테이너 구성·빈 등록·빈 의존관계 정의 전략을 치밀하게 세워두고** 개발해야 한다. 자칫 여러 방식이 혼재하면 코드와 설정정보를 이해·관리하기가 매우 힘들어진다.

### 1장 핵심 정리

- **스프링 애플리케이션은 POJO 클래스 + 빈 설정 메타정보**로 구성된다.
- 빈 설정 메타정보는 특정 포맷에 종속되지 않는다 — 필요하면 새로운 작성 방법을 만들어 쓸 수 있다.
- 빈 등록 방법은 **XML / 빈 자동인식(애노테이션) / 자바 코드** 세 가지로 나뉜다.
- 빈 의존관계 설정 방법도 마찬가지로 **XML / 애노테이션 / 자바 코드**로 구분된다.
- **프로퍼티 값**은 빈에 주입되는 빈 오브젝트가 아닌 정보다 — 환경에 따라 자주 바뀌면 properties 파일 같은 별도 리소스로 분리해 둔다.
- 빈의 존재 범위는 스코프 — **싱글톤 / 프로토타입 / 기타 스코프**로 구분된다.
- 프로토타입과 싱글톤이 아닌 스코프 빈은 **DL** 방식을 이용하거나 **스코프 프록시**로 받는다.
- 스프링 **3.1**은 자바 코드 메타정보 기능을 발전시켜 자바 코드만으로도 모든 빈 설정이 가능하게 만들었다.
- 스프링 **3.1**의 프로파일·프로퍼티 소스로 이뤄진 **런타임 환경 추상화** 기능을 사용하면, 환경에 따라 달라지는 빈 구성과 속성 지정 문제를 손쉽게 다룰 수 있다.

> 한 번쯤 스프링 레퍼런스 매뉴얼을 참고해 다양한 IoC/DI 사용 방법을 학습해 두면, 이후 어떤 규모의 프로젝트에서도 컨테이너 구성에 시간을 낭비하지 않게 된다.
