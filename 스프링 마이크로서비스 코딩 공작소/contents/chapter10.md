# 10장. 스프링 클라우드 스트림을 사용한 이벤트 기반 아키텍처

이 장에선 비동기 메시지를 사용하여 다른 마이크로서비스와 통신하는 스프링 기반 마이크로서비스를 설계하고 구현하는 방법을 다룬다. 애플리케이션 간 통신에서 비동기식 메시지를 사용하는 것은 더 이상 새롭지 않다. 새로운 점은 메시지를 사용하여 상태 변경을 표현하는 이벤트로 통신한다는 개념이다. 이를 이벤트 기반 아키텍처 또는 메시지 기반 아키텍처라고 한다.

EDA 기반의 접근 방식으로 특정 라이브러리나 서비스에 밀접하게 결합하지 않고 변화에 대응할 수 있는 높은 수준의 분리된 시스템을 구축할 수 있다. 마이크로서비스를 통합할 때 EDA를 사용하면 애플리케이션에서 발송하는 이벤트 스트림을 서비스는 수신만 하면 되기 때문에 애플리케이션에 새로운 기능을 빠르게 추가할 수 있다.

## 메시징과 EDA, 마이크로서비스의 사례

운영환경에 서비스들을 배포한 후 조직 서비스에서 정보를 조회할 때 라이선싱 서비스 호출이 지나치게 오래걸린다는 것을 발견했다고 하자. 조직 서비스 데이터의 사용 패턴을 살펴보면 조직 데이터는 거의 변경이 없고 조직 서비스에서 읽어 오는 대부분의 데이터가 조직 레코드의 기본 키로 수행된다는 것을 알 수 있다. 데이터베이스 액세스 비용을 들이지 않고 조직 데이터의 읽기를 캐싱할 수 있다면 라이선싱 서비스에 대한 호출 응답 시간을 크게 향상시킬 수 있을 것이다. 캐싱 솔루션을 구현하려면 다음 세 가지 핵심 요구사항을 고려해야 한다.

- 캐싱된 데이터는 라이선싱 서비스의 모든 인스턴스에 일관성이 있어야 한다.
  - 이는 어떤 라이선싱 서비스의 인스턴스에 접근하든 동일한 조직 데이터 읽기가 보장되어야 하므로 라이선싱 서비스 내에서 로컬로 데이터를 캐싱할 수 없다는 것을 의미한다.
- 라이선싱 서비스를 호스팅하는 컨테이너 메모리에 조직 데이터를 캐싱하면 안 된다.
  - 서비스를 호스팅하는 런타임 컨테이너는 크기가 제한되어 있을 때가 많고, 다양한 액세스 패턴으로 데이터를 가져갈 수 있다. 로컬 캐싱은 클러스터 내 다른 서비스들의 동기화를 보장해야 하므로 복잡성을 유발한다.
- 업데이트나 삭제로 조직 레코드가 변경될 때 라이선싱 서비스는 조직 서비스의 상태 변화를 인식해야 한다.
  - 이 경우 라이선싱 서비스는 특정 조직의 캐싱된 데이터를 모두 무효화하고 캐시에서 제거해야 한다.

이러한 요구 사항을 구현하는 두 가지 접근 방법이 있다.

1. 동기식 요청-응답 모델로 요구 사항을 구현: 조직 상태가 변경되면 라이선싱과 조직 서비스는 REST 엔드포인트를 이용하여 서로 통신한다.
2. 조직 서비스는 자기 데이터가 변경되었음을 알리려고 비동기 이벤트를 발송: 조직 서비스는 조직 레코드가 업데이트 또는 삭제(상태 변경) 되었음을 나타내는 메시지를 큐에 발행한다. 라이선싱 서비스는 중개자와 함께 수신하여 조직 이벤트가 발생했는지 확인하고, 발생하면 캐시에서 조직 데이터를 삭제한다.

### 동기식 요청-응답 방식으로 상태 변화 전달

조직 데이터의 캐시에 분산 키-값 저장소 데이터베이스인 Redis를 사용한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ce6f94fa-5948-4907-b48d-e475e762f444)

사용자가 라이선싱 서비스를 호출하면 라이선싱 서비스는 조직 데이터를 조회해야 한다. 이 작업을 수행하고자 라이선싱 서비스는 먼저 레디스 클러스터에서 원하는 조직을 조직 ID로 조회한다. 조직 데이터가 조회되지 않는다면 REST 기반 엔드포인트로 조직 서비스를 호출하여 전달받은 데이터를 레디스에 저장한 후 사용자에게 전달한다.

누군가 조직 서비스의 REST 엔드포인트를 사용하여 조직 레코드를 업데이트하거나 삭제하면, 조직 서비스는 캐시에 있는 조직 데이터를 무효화하려고 라이선싱 서비스의 엔드포인트를 호출해야 한다.

조직 서비스가 라이선싱 서비스를 호출하여 레디스 캐시를 무효화할 때 적어도 다음 세 가지 문제를 발견할 수 있다.

- 조직 서비스와 라이선싱 서비스는 강하게 결합되어 있고 이런 결합은 깨지기 쉽다.
  - 조직 서비스가 레디스와 통신하면 다른 서비스가 소유하는 데이터 저장소와 직접 통신하게 되므로 그 자체로 문제가 된다.
- 캐시를 무효화하는 라이선싱 서비스 엔드포인트가 변경되면 조직 서비스도 함께 변경되어야 한다. 이런 방식은 유연하지 못하다.
- 조직 데이터 변경을 알리도록 라이선싱 서비스를 호출하는지 확인하는 조직 서비스의 코드를 수정하지 않은 채 조직 데이터에 대한 새로운 소비자를 추가할 수는 없다.

### 메시징을 사용한 서비스 간 상태 변화 전달

메시징 방식에선 라이선싱 및 조직 서비스 사이에 토픽이 추가된다. 메시징 시스템은 조직 서비스에서 데이터를 읽는 데 사용되지 않고, 조직 서비스가 자신이 관리하는 데이터의 상태 변경을 발행하는 데 사용된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f8bcaded-08bc-478f-8dee-bf14910986a4)

위 모델에서 조직 데이터가 변경될 때 조직 서비스는 메시지를 토픽에 발행한다. 라이선싱 서비스는 메시지에 대한 토픽을 모니터링하고 메시지가 도착하면 레디스 캐시에서 해당하는 조직 레코드를 삭제한다. 상태 전달에서 메시지 큐는 라이선싱 서비스와 조직 서비스 사이의 중개자 역할을 한다. 이 방식은 다음 네 가지 이점을 제공한다.

- 느슨한 결합
- 내구성
  - 큐가 있으면 서비스 소비자가 다운된 경우에도 메시지 전달을 보장할 수 있다.
- 확장성
- 유연성

### 메시징 아키텍처의 단점

- 메시지 처리의 의미론
  - 애플리케이션이 메시지가 소비되는 순서에 따라 어떻게 동작하는지와 메시지가 순서대로 처리되지 않을 때 어떻게 되는지를 이해해야 한다.
  - 메시징을 사용하여 데이터의 엄격한 상태 전환을 적용하는 경우, 메시지가 예외를 발생시키거나 에러가 순서대로 처리되지 않는 시나리오를 염두에 두고 애플리케이션 설계를 고려해야 한다.
    - 메시지가 실패하면 에러 처리를 재시도하는가?
    - 그냥 실패하도록 놔둘 것인가?
    - 고객의 메시지 중 하나가 실패하면 해당 고객과 관련된 향후 메시지를 어떻게 처리하는가?
- 메시지 가시성
  - 마이크로서비스에서 메시지를 사용한다는 것은 종종 동기식 서비스 호출과 비동기식 서비스 처리가 혼합됨을 의미한다. 메시지의 비동기적 특성으로 메시지가 발행되거나 소비되는 시점에 바로 수신 또는 처리되지 않을 수 있다.
  - 웹 서비스 호출 및 메시지 전반에 걸쳐 사용자의 트랜잭션을 추적하는 상관관계 ID 등을 사용하는 것은 애플리케이션에서 발생하는 일을 이해하고 디버깅 하는 데 매우 중요하다.
- 메시지 코레오그래피
  - 비즈니스 로직을 추론하는 것이 더 어렵다. 메시징 기반 애플리케이션을 디버깅하려면 여러 다른 서비스의 로그(사용자 트랜잭션이 순서 없이 다양한 시간에 실행되어 있는 로그)를 모두 살펴봐야 한다는 것이다.

## 스프링 클라우드 스트림 소개

스프링 클라우드 스트림은 애플리케이션의 메시지 발행자와 소비자를 쉽게 구축할 수 있는 애너테이션 기반 프레임워크다.

우리가 사용하는 메시징 플랫폼의 세부 구현도 추상화한다. Apache Kafka 프로젝트나 RabbitMQ를 포함한 여러 메시지 플랫폼을 스프링 클라우드 스트림과 함께 사용할 수 있으며, 특정 플랫폼을 위한 세부 구현은 애플리케이션 코드에서 제외된다. 애플리케이션에서 메시지 발행과 소비에 대한 구현은 플랫폼 중립적인 스프링 인터페이스로 수행된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/75a6f0d9-c296-4c12-b470-276c8ab1a869)

스프링 클라우드에서 메시지를 발행하고 소비하는 데 다음 네 개의 컴포넌트가 관련이 있다.

- 소스
  - 서비스가 메시지를 발행할 준비가 되면 소스를 사용하여 메시지를 발행한다.
  - 발행할 메시지를 표현하는 POJO를 전달받는 스프링의 애너테이션 인터페이스다.
  - 소스는 메시지를 받아 직렬화하고 메시지를 채널에 발행한다.
- 채널
  - 메시지 생산자가 발행하거나 메시지 소비자가 소비한 후 메시지를 보관할 큐를 추상화한 것이다.
  - 채널은 메시지를 보내고 받는 큐로 기술할 수 있다.
- 바인더
  - 스프링 클라우드 스트림 프레임워크의 일부며, 특정 메시지 플랫폼과 통신하는 스프링 코드다.
  - 메시지를 발행하고 소비하는데, 특정 플랫폼 라이브러리 및 API를 노출하지 않고도 메시지 작업을 할 수 있게 해 준다.
- 싱크
  - 서비스는 싱크를 사용하여 큐에서 메시지를 받는다.
  - 들어오는 메시지 채널을 수신 대기하고 메시지를 다시 POJO 객체로 역직렬화한다.

## 간단한 메시지 생산자와 소비자 작성

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1c4f8d60-2908-40a9-a592-92e29ed1e401)

### 아파치 카프가 및 레디스 도커 구성

`docker-compose.yml` 파일에 아래와 같이 추가해보자.

```yaml
# ...
zookeeper:
    image: wurstmeister/zookeeper:latest
    ports:
      - 2181:2181
    networks:
      backend:
        aliases:
          - "zookeeper"
  kafkaserver:
    image: wurstmeister/kafka:latest
    ports:
      - 9092:9092
    environment:
      - KAFKA_ADVERTISED_HOST_NAME=kafka
      - KAFKA_ADVERTISED_PORT=9092
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_CREATE_TOPICS=dresses:1:1,ratings:1:1
    volumes:
      - "/var/run/docker.sock:/var/run/docker.sock"
    depends_on:
      - zookeeper
    networks:
      backend:
        aliases:
          - "kafka"
  redisserver:
    image: redis:alpine
    ports:
      - 6379:6379
    networks:
      backend:
        aliases:
          - "redis"
```

### 조직 서비스에서 메시지 생산자 작성

```java
package com.optimagrowth.organization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.security.oauth2.config.annotation.web.configuration. EnableResourceServer;

@SpringBootApplication
@RefreshScope
@EnableResourceServer
@EnableBinding(Source.class)
public class OrganizationServiceApplication {

   public static void main(String[] args) {
      SpringApplication.run(OrganizationServiceApplication.class, args);
   }
}
```

`@EnableBinding`은 서비스를 메시지 브로커에 바인딩하려는 것을 스프링 클라우드 스트림에 알리는 역할을 한다. `Source.class`를 사용하면 이 서비스가 `Source` 클래스에서 정의된 채널들을 이용하여 메시지 브로커와 통신할 것이라고 스프링 클라우드 스트림에 알린다.

```java
@Component
public class UserContext {
    public static final String CORRELATION_ID = "tmx-correlation-id";
    public static final String AUTH_TOKEN = "Authorization";
    public static final String USER_ID = "tmx-user-id";
    public static final String ORG_ID = "tmx-org-id";

    private static final ThreadLocal<String> correlationId = new ThreadLocal<String>();
    private static final ThreadLocal<String> authToken = new ThreadLocal<String>();
    private static final ThreadLocal<String> userId = new ThreadLocal<String>();
    private static final ThreadLocal<String> orgId = new ThreadLocal<String>();

    public static HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(CORRELATION_ID, getCorrelationId());

        return httpHeaders;
    }
}

@Component
public class SimpleSourceBean {
    private Source source;

    private static final Logger logger =
       LoggerFactory.getLogger(SimpleSourceBean.class);

    public SimpleSourceBean(Source source){
        this.source = source;
    }

    public void publishOrganizationChange(ActionEnum action, String organizationId) {
       logger.debug("Sending Kafka message {} for Organization Id: {}",
                    action, organizationId);
        OrganizationChangeModel change =  new OrganizationChangeModel(
                OrganizationChangeModel.class.getTypeName(),
                action.toString(),
                organizationId,
                UserContext.getCorrelationId());

        source.output().send(MessageBuilder
                          .withPayload(change)
                          .build());
    }
}
```

스프링 클라우드의 `Source` 클래스를 코드에 주입했다. 특정 메시지 토픽에 대한 모든 통신은 스프링 클라우드 스트림의 채널이라는 자바 인터페이스 클래스로 한다는 점을 기억하자. 이 코드에서 `output()` 메소드를 노출하는 `Source` 인터페이스를 사용했다.

`Source` 인터페이스는 서비스를 하나의 채널에서만 발행할 때 사용하면 편리하다. `output()` 메서드는 `MessageChannel` 타입의 클래스를 반환하고 이 타입을 이용하여 메시지 브로커에 메시지를 보낸다.

```java
public enum ActionEnum {
    GET,
    CREATED,
    UPDATED,
    DELETED
}

@Getter @Setter @ToString
public class OrganizationChangeModel {
    private String type;
    private String action; // 이벤트를 발생 시킨 액션
    private String organizationId; // 이벤트와 연관된 조직 ID
    private String correlationId; // 이벤트를 발생시킨 서비스 호출의 상관관계 ID

    public OrganizationChangeModel(String type,
            String action, String organizationId,
            String correlationId) {
        this.type = type;
        this.action = action;
        this.organizationId = organizationId;
        this.correlationId = correlationId;
    }
}

@Service
public class OrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @Autowired
    private OrganizationRepository repository;

    @Autowired
    SimpleSourceBean simpleSourceBean;

    public Organization create(Organization organization){
        organization.setId( UUID.randomUUID().toString());
        organization = repository.save(organization);
        simpleSourceBean.publishOrganizationChange(
            ActionEnum.CREATED,
            organization.getId());
        return organization;

    }

    // ...
}
```

```properties
spring.cloud.stream.bindings.output.destination=orgChangeTopic
spring.cloud.stream.bindings.output.content-type=application/json
spring.cloud.stream.kafka.binder.zkNodes=localhost
spring.cloud.stream.kafka.binder.brokers=localhost
```

### 라이선싱 서비스에서 메시지 소비자 작성

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a03ebcef-31cf-41b0-82a9-5f27f6f7df38)

```java
@EnableBinding(Sink.class)
public class LicenseServiceApplication {

    @StreamListener(Sink.INPUT)
    public void loggerSink(OrganizationChangeModel orgChange) {
      logger.debug("Received an {} event for organization id {}",
       orgChange.getAction(), orgChange.getOrganizationId());
    }

    // ...
}
```

```properties
spring.cloud.stream.bindings.input.destination=orgChangeTopic
spring.cloud.stream.bindings.input.content-type=application/json
spring.cloud.stream.bindings.input.group=licensingGroup
spring.cloud.stream.kafka.binder.zkNodes=localhost
spring.cloud.stream.kafka.binder.brokers=localhost
```

소비자 그룹 개념은 다음과 같다. 같은 메시지 큐를 수신하는 여러 서비스가 있는 경우 각 고유 서비스가 메시지 사본을 처리하길 원하지만, 서비스 인스턴스 그룹 안에서는 한 서비스 인스턴스만 메시지를 사용하고 처리하길 원한다. `group` 프로퍼티는 서비스가 속한 소비자 그룹을 식별하는 데 사용된다.

모든 서비스 인스턴스가 동일한 그룹 이름을 갖는 한, 스프링 클라우드 스트림과 하부 메시지 브로커는 그 그룹에 속한 서비스 인스턴스에서 메시지 사본 하나만 사용하도록 보장한다. 라이선싱 서비스의 `group` 프로퍼티 값은 `licensingGroup`이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2fef4356-4353-4ca0-8b81-ddb57364d6c6)

## 스프링 클라우드 스트림 사용 사례: 분산 캐싱

라이선싱 서비스는 특정 라이선스와 연관된 조직 데이터에 대해 분산 레디스 캐시를 확인한다. 캐시에 없다면 조직 서비스를 호출하고 결과를 레디스 해시에 캐싱한다.

조직 서비스에서 데이터가 업데이트되면 조직 서비스는 카프카에 메시지를 발행한다. 라이선싱 서비스는 메시지를 가져와 캐시를 삭제하도록 레디스에 DELETE를 호출한다.

### 캐시 검색을 위한 레디스

할 일은 다음과 같다.

- 스프링 데이터 레디스 의존성을 포함하여 설정한다.
- 레디스 서버에 대한 데이터베이스 커넥션을 설정한다.
- 코드에서 레디스 해시와 상호 작용하는 데 사용될 스프링 데이터 레디스 저장소를 정의한다.
- 레디스와 라이선싱 서비스가 조직 데이터를 저장하고 읽어 오게 한다.

커넥션 설정 및 일부 코드는 생략한다.

```java
@Repository
public interface OrganizationRedisRepository extends CrudRepository<Organization, String> {

}

@Getter @Setter @ToString
@RedisHash("organization")
public class Organization extends RepresentationModel<Organization> {

    @Id
    String id;
    String name;
    String contactName;
    String contactEmail;
    String contactPhone;
}

@Component
public class OrganizationRestTemplateClient {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    OrganizationRedisRepository redisRepository;
    private static final Logger logger = LoggerFactory.getLogger(OrganizationRestTemplateClient.class);

    private Organization checkRedisCache(String organizationId) {
        try {
          return redisRepository
                    .findById(organizationId)
                    .orElse(null);
        } catch (Exception ex) {
          logger.error("Error encountered while trying to retrieve
            organization{} check Redis Cache.  Exception {}",
            organizationId, ex);
          return null;
        }
    }

    private void cacheOrganizationObject(Organization organization) {
        try {
          redisRepository.save(organization);
        } catch (Exception ex) {
          logger.error("Unable to cache organization {} in
            Redis. Exception {}",
            organization.getId(), ex);
        }
    }

    public Organization getOrganization(String organizationId){
        logger.debug("In Licensing Service.getOrganization: {}",
            UserContext.getCorrelationId());

        Organization organization = checkRedisCache(organizationId);
        if (organization != null) {
          logger.debug("I have successfully retrieved an organization {} from the redis cache: {}", organizationId, organization);
          return organization;
        }
        logger.debug("Unable to locate organization from the redis cache: {}.", organizationId);
        ResponseEntity<Organization> restExchange =
          restTemplate.exchange(
            "http://gateway:8072/organization/v1/organization/
            {organizationId}",HttpMethod.GET,
            null, Organization.class, organizationId);

        organization = restExchange.getBody();
        if (organization != null) {
          cacheOrganizationObject(organization);
        }
        return restExchange.getBody();
    }
}
```

// 나머지 코드는 귀찮아서 생략...