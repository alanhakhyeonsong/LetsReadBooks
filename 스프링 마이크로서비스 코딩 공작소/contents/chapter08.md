# 8장. 스프링 클라우드 게이트웨이를 이용한 서비스 라우팅
마이크로서비스와 같은 분산형 아키텍처에선 보안과 로깅, 여러 서비스 호출에 걸친 사용자 추적처럼 중요한 작업을 해야 할 시점이 온다. 이 기능을 구현하려 모든 개발 팀이 독자적 솔루션을 구축할 필요 없이 이러한 특성을 모든 서비스에 일관되게 적용하길 원할 것이다. 공통 라이브러리나 프레임워크를 사용해서 이들 기능을 각 서비스에 직접 구축할 수도 있지만, 이 방법은 다음과 같은 결과를 초래할 수 있다.

- 이러한 기능을 각 서비스에 일관되게 구현하기 어렵다
- 보안과 로깅 같은 횡단 관심사의 구현 책임을 개별 개발 팀에 전가하면 잘못 구현하거나 아예 누락할 수 있다
- 모든 서비스에 걸쳐 강한 의존성을 만들 수 있다

이 문제를 해결하려면 횡단 관심사를 독립적으로 배치할 수 있고, 아키텍처의 모든 마이크로서비스 호출에 대한 필터와 라우터 역할을 할 수 있는 서비스로 추상화해야 한다. 이러한 서비스를 **게이트웨이**라고 한다. 서비스 클라이언트는 더 이상 마이크로서비스를 직접 호출하지 않는다. 그 대신 모든 호출은 단일 정책 시행 지점 역할을 하는 서비스 게이트웨이를 경유한 다음 최종 목적지로 라우팅된다.

## 서비스 게이트웨이란?
서비스 게이트웨이는 서비스 클라이언트와 호출되는 서비스 사이에서 중개 역할을 하고, 서비스 게이트웨이가 관리하는 하나의 URL로 통신한다. 또한 서비스 클라이언트 호출에서 보낸 경로를 분해하고 서비스 클라이언트가 호출하려는 서비스를 결정한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a6be3bec-b566-478e-a099-0a227db6ab78)

서비스 게이트웨이는 애플리케이션 내 마이크로서비스를 호출하기 위해 유입되는 모든 트래픽의 게이트키퍼 역할을 한다. 서비스 게이트웨이가 있으면 서비스 클라이언트는 각 서비스 URL을 직접 호출하지 않고 서비스 게이트웨이에 호출을 보낸다.

또한 서비스 게이트웨이는 클라이언트와 개별 서비스의 호출 사이에 있기 때문에 서비스를 호출하는 중앙 정책 시행 시점 역할도 한다. 이 지점을 사용하면 각 개발 팀이 서비스 횡단 관심사를 구현하지 않고 한곳에서 수행할 수 있다.

- 정적 라우팅: 서비스 게이트웨이는 단일 서비스 URL과 API 경로로 모든 서비스를 호출한다. 모든 서비스에 대해 하나의 서비스 엔드포인트만 알면 되므로 개발이 편해진다.
- 동적 라우팅: 서비스 게이트웨이는 유입되는 서비스 요청을 검사하고 요청 데이터를 기반으로 서비스 호출자를 위한 지능적 라우팅을 수행할 수 있다. 예를 들어 베타 프로그램에 참여하는 고객의 서비스 호출은 사람들이 사용하는 버전과 다른 버전의 코드가 실행되는 특정 서비스 클러스터로 라우팅된다.
- 인증과 인가: 모든 서비스 호출이 서비스 게이트웨이로 라우팅 되기 때문에 서비스 게이트웨이는 서비스 호출자가 자신의 인증 여부를 확인할 수 있는 적합한 장소다.
- 지표 수집과 로깅: 서비스 호출이 게이트웨이를 통과하기 때문에 서비스 게이트웨이를 지표와 로그를 수집하는 데 사용할 수 있다. 또한 사용자 요청에 대한 중요한 정보가 있는지 확인하여 균일한 로깅을 보장할 수 있다. 서비스 게이트웨이를 사용하면 서비스 호출 횟수 및 응답 시간처럼 많은 기본 지표를 한곳에서 더 잘 수집할 수 있다.

---
### 📌 서비스 게이트웨이가 단일 장애 지점이나 잠재적 병목 지점은 아닐까?
구현 시 다음 사항을 염두에 두어야 한다.

- 로드 밸런서는 서비스 앞에 있을 때 유용하다: 로드 밸런서를 여러 서비스 게이트웨이 인스턴스 앞에 두는 것은 적절한 설계이며, 필요할 때 서비스 게이트웨이 구현체를 확장할 수 있다. 하지만 모든 서비스 인스턴스 앞에 로드 밸런서를 두는 것은 병목점이 될 수 있어 좋은 생각은 아니다.
- 서비스 게이트웨이를 stateless로 작성하라: 어떤 정보도 서비스 게이트웨이의 메모리에 저장하면 안 된다. 주의하지 않으면 게이트웨이의 확장성을 제한할 수 있다. 따라서 데이터는 모든 서비스 게이트웨이 인스턴스에 복제되어야 한다.
- 서비스 게이트웨이를 가볍게 유지하라: 서비스 게이트웨이는 서비스를 호출할 때 '병목점'이 될 수 있다. 서비스 게이트웨이에서 여러 데이터베이스를 호출하는 복잡한 코드가 있다면 추적하기 어려운 성능 문제의 원인이 될 수 있다.

## 스프링 클라우드 게이트웨이 소개
스프링 클라우드 게이트웨이는 스프링 프레임워크 5, 프로젝트 리액터, 스프링 부트 2.0을 기반으로 한 API 게이트웨이 구현체다. 논블로킹 애플리케이션은 주요 스레드를 차단하지 않는 방식으로 작성된다. 따라서 이러한 스레드는 언제나 요청을 받아 백그라운드에서 비동기식으로 처리하고 처리가 완료되면 응답을 반환한다.

스프링 클라우드 게이트웨이는 다음 기능들을 제공한다.

- 애플리케이션의 모든 서비스 경로를 단일 URL에 매핑한다: 하나의 URL에 제한되지 않고 실제 여러 경로의 진입점을 정의하고 경로 매핑을 세분화할 수 있다. 하지만 가장 일반적인 사용 사례라면 모든 서비스 클라이언트 호출이 통과하는 단일 진입점을 제공하는 것이다.
- 게이트웨이로 유입되는 요청과 응답을 검사하고 조치를 취할 수 있는 필터를 작성한다: 이 필터를 사용하면 코드에 정책 시행 지점을 삽입해서 모든 서비스 호출에 다양한 작업을 수행할 수 있다. 즉, 이 필터로 유입되고 유출되는 HTTP 요청 및 응답을 수정할 수 있다.
- 요청을 실행하거나 처리하기 전에 해당 요청이 주어진 조건을 충족하는지 확인할 수 있는 서술자를 만든다.

## 스프링 클라우드 게이트웨이에서 라우팅 구성
스프링 클라우드 게이트웨이는 본래 **리버스 프록시**다. 이는 자원에 도달하려는 클라이언트와 자원 사이에 위치한 중개 서버다. 클라이언트는 어떤 서버와 통신하고 있는지도 알지 못한다. 리버스 프록시는 클라이언트 요청을 캡처한 후 클라이언트를 대신하여 원격 자원을 호출한다.

마이크로서비스 아키텍처에서 스프링 클라우드 게이트웨이(리버스 프록시) 클라이언트의 마이크로서비스 호출을 받아 상위 서비스에 전달한다. 상위 서비스와 통신하려면 게이트웨이는 유입된 호출이 상위 경로에 매핑하는 방법을 알아야 한다.

### 서비스 디스커버리를 이용한 자동 경로 매핑
게이트웨이에 대한 모든 경로 매핑은 `/configserver/src/main/resources/config/gateway-server.yml` 파일에서 경로를 정의해서 수행한다. 하지만 스프링 클라우드 게이트웨이는 다음 코드처럼 `gateway-server` 구성 파일에 구성 정보를 추가해 서비스 ID를 기반으로 요청을 자동으로 라우팅할 수 있다.

```yaml
spring:
  cloud:
    gateway:
      discovery.locator: // 서비스 디스커버리에 등록된 서비스를 기반으로 게이트웨이가 경로를 생성하도록 설정.
        enabled: true
        lowerCaseServiceId: true
```

스프링 클라우드 게이트웨이는 호출되는 서비스의 유레카 서비스 ID를 자동으로 사용하여 하위 서비스 인스턴스와 매핑한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2cbb0de9-8781-4b47-8c40-67b2ad253a3e)

유레카와 함께 스프링 클라우드 게이트웨이를 사용하는 장점은 호출할 수 있는 엔드포인트가 하나라는 사실 외에 게이트웨이를 수정하지 않고도 서비스 인스턴스를 추가 및 제거할 수 있다는 것이다. 예를 들어 유레카에 새로운 서비스를 추가하면 게이트웨이는 서비스의 물맂거 엔드포인트 위치에 대해 유레카와 통신하기 때문에 자동으로 호출을 라우팅할 수 있다.

게이트웨이 서버가 관리하는 경로를 확인하려면 게이트웨이 서버의 `actuator/gateway/routes` 엔드포인트를 통해 경로 목록을 볼 수 있다. 이 엔드포인트는 서비스의 모든 매핑 목록을 반환한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c62ef214-531e-4192-a2a1-a8cf78465140)


또한 서술자, 관리 포트, 경로 ID, 필터 등 추가적인 데이터를 확인할 수 있다.

### 서비스 디스커버리를 이용한 수동 경로 매핑
스프링 클라우드 게이트웨이는 유레카 서비스 ID로 생성된 자동화된 경로에만 의존하지 않고 명시적으로 경로 매핑을 정의할 수 있어 코드를 더욱 세분화할 수 있다. 게이트웨이 기본 경로로 액세스하는 대신 조직 이름을 줄여 경로를 단순화한다 가정해보자.

```yaml
spring:
  cloud:
    gateway:
      discovery.locator:
        enabled: true
        lowerCaseServiceId: true
      routes:
      // 이 선택적 ID는 임의의 경로에 대한 ID다.
      - id: organization-service
        // 이 경로의 대상 URI를 설정한다.
        url: lb://organization-service

        // 경로는 load() 메서드로 설정되지만, 여러 옵션 중 하나.
        predicates:
        - Path=/organization/**

        // 응답을 보내기 전이나 후에 요청 또는 응답을 수정하고자 web.filters들을 필터링.
        filters:
        - RewritePath=/organization/(?<path>.*), /$\{path}
        // 매개변수 및 교체 순서로 경로 정규식을 받아 요청 경로를 /organization/**에서 /**로 변경한다.
```

### 동적으로 라우팅 구성을 재로딩
스프링 클라우드 게이트웨이에서 라우팅을 구성할 때 다음으로 살펴볼 것은 동적으로 경로를 갱신하는 방법이다. 동적 라우팅 재로딩 기능은 게이트웨이 서버의 재시작 없이 경로 매핑을 변경할 수 있기 때문에 유용하다. 따라서 기존 경로를 빠르게 수정할 수 있고, 해당 환경 내 각 게이트웨이 서버를 새로운 경로로 변경하는 작업을 수행한다.

스프링 액추에이터는 라우팅 구성 정보를 다시 로드할 수 있도록 POST 기반 엔드포인트 경로인 `actuator/gateway/refresh`를 노출한다. 이후 `/routes` 엔드포인트를 호출하면 두 개의 새로운 경로를 확인할 수 있다. 이 호출에 대한 응답은 body 없이 HTTP 200 상태 코드만 반환한다.

## 스프링 클라우드 게이트웨이의 진정한 능력: Predicate과 Filter Factories
게이트웨이로 모든 요청을 프록시 할 수 있기 때문에 서비스 호출을 단순화할 수 있다. 하지만 진정한 힘은 게이트웨이를 통하는 모든 서비스 호출에 적용될 사용자 정의 로직을 작성할 때 발휘된다. 대부분의 경우 모든 서비스에서 보안, 로깅, 추적 등 일관된 애플리케이션 정책을 적용하기 위해 이런 사용자 정의 로직이 사용된다.

- 횡단 관심사 로직을 스프링의 관점 클래스와 유사하게 사용할 수 있다.
- 게이트웨이로 라우팅되는 모든 서비스에 대한 공통 관심사를 구현할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/22fdae0e-0079-4724-a3fb-078577789925)

### 게이트웨이 Predicate Factories
게이트웨이의 서술자는 요청을 실행하거나 처리하기 전에 요청이 조건 집합을 충족하는지 확인하는 객체다. 경로마다 논리 AND로 결합할 수 있는 여러 Predicate Factories를 설정할 수 있다.

이런 서술자는 코드에 프로그래밍 방식이나 구성 파일을 사용하여 적용할 수 있다.

```yaml
predicates
  - Path=/organization/**
```

|Predicate|설명|예|
|--|--|--|
|Before|설정된 일시 전에 발생한 요청인지 확인|Before=2020-03-11T...|
|After|설정된 일시 이후에 발생한 요청인지 확인|After=2020-03-11T...|
|Between|설정된 두 일시 사이에 발생한 호출인지 확인. 시작 일시는 포함되고 종료 일시는 포함되지 않는다.|Between=2020-03-11T..., 2020-04-11T...|
|Header|헤더 이름과 정규식 매개변수를 사용하여 해당 값과 정규식을 확인|Header=X-Request-Id, \d+|
|Host|"." 호스트 이름 패턴으로 구분된 안티-스타일 패턴을 매개변수로 받아 Host 헤더를 주어진 패턴과 비교|Host=**.example.com|
|Method|HTTP 메서드를 비교|Method=GET|
|Path|스프링 PathMatcher를 사용|Path=/organization/{id}|
|Query|필수 매개변수의 정규식 매개변수를 사용하여 쿼리 매개변수와 비교|Query=id, 1|
|Cookie|쿠키 이름과 정규식 매개변수를 사용하여 HTTP 요청 헤더에서 쿠키를 찾아 그 값과 정규식이 일치하는지 비교|Cookie=SessionID, abc|
|RemoteAddr|IP 목록에서 요청의 원격 주소와 비교|RemoteAddr=192.168.3.5/24|

### 게이트웨이 Filter Factories
게이트웨이의 Filter Factories를 사용하면 코드에 정책 시행 지점을 삽입하여 모든 서비스 호출에 대해 일관된 방식으로 작업을 수행할 수 있다. 즉, 이런 필터로 수신 및 발신하는 HTTP 요청과 응답을 수정할 수 있다.

내장형 필터는 다음 문서를 참고하자.

- [GatewayFilter Factories :: Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway/gatewayfilter-factories.html)

### 사용자 정의 필터
스프링 클라우드 게이트웨이 내에서 필터를 사용하여 사용자 정의 로직을 만들 수 있다. 필터를 사용하여 각 서비스 요청이 통과하는 비즈니스 로직 체인을 구현할 수 있다. 다음 두 가지 종류의 필터를 지원한다.

- 사전 필터: 실제 요청이 목적지로 전송되기 전에 호출된다. 일반적으로 서비스가 일관된 메시지 형식인지 확인하는 작업을 수행하거나 서비스를 호출하는 사용자가 인증되었는지 확인하는 게이트키퍼 역할을 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fb9e4565-ef72-49e1-aed2-180b5ace89df)

- 사후 필터: 사후 필터는 대상 서비스 이후에 호출되고 응답은 클라이언트로 다시 전송된다. 일반적으로 대상 서비스의 응답을 다시 기록하거나 오류를 처리하거나 민감한 정보에 대한 응답을 검사하려고 사후 필터를 구현한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ded1e99e-5755-4320-aa0a-80fe01597753)