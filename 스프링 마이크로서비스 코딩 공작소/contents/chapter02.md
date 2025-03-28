# 2장. 스프링 클라우드와 함께 마이크로서비스 세계 탐험
## 스프링 클라우드란?
Spring Cloud는 VMWare, HashiCorp, Netflix 등 오픈 소스 회사의 제품을 전달 패턴으로 모아 놓은 도구 집합이다. Spring Cloud는 프로젝트 설정 및 구성을 단순화하고 가장 흔히 접할 수 있는 패턴의 해결안을 스프링 애플리케이션에 제공한다. 덕분에 마이크로서비스 애플리케이션을 만들고 배포하는 데 필요한 모든 인프라스트럭처 구성에 대한 세부 사항은 신경 쓰지 않은 채 코드를 작성하는 데만 집중할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a0a556f8-32a5-447d-8233-0a228ea80e88)

### 스프링 클라우드 컨피그
Spring Cloud Config는 애플리케이션 구성 데이터를 관리한다. 애플리케이션 구성 데이터는 배포된 마이크로서비스에서 완전히 분리된다. 따라서 아무리 많은 마이크로서비스 인스턴스를 실행하더리도 항상 동일한 구성을 보장할 수 있다. 자체 프로퍼티 관리 저장소가 있지만 다음과 같은 오픈 소스 프로젝트와도 통합된다.

- Git
- Consul
- Eureka

### 스프링 클라우드 서비스 디스커버리
Spring Cloud Service Discovery를 사용하면 서비스를 소비하는 클라이언트에서 서버가 배포된 물리적 위치(IP 및 서버 이름)를 추상화할 수 있다. 서비스 소비자는 물리적 위치가 아닌 논리적 이름을 사용하여 서버의 비즈니스 로직을 호출한다. 또한 서비스 인스턴스가 시작되고 종료될 때 인스턴스 등록 및 등록 취소도 처리한다. 다음 서비스를 사용하여 구현할 수 있다.
- Consul
- Zookeeper
- Eureka: 서비스 디스커버리 엔진으로 동작

### 스프링 클라우드 로드 밸런서와 Resilience4j
마이크로서비스 클라이언트 회복성 패턴을 위해 스프링 클라우드는 Resilience4j 라이브러리와 스프링 클라우드 로드 밸런서 프로젝트를 포함하여 마이크로서비스 내부에서 이들을 사용하여 편리하게 구현하게 만들었다.

Resilience4j 라이브러리를 사용하면 회로 차단기, 재시도, 벌크헤드 등 서비스 클라이언트 회복성 패턴을 빠르게 구현할 수 있다.

스프링 클라우드 로드 밸런서 프로젝트는 유레카 같은 서비스 디스커버리 에이전트와 통합하는 것을 단순화하며, 서비스 소비자 호출에 대한 클라이언트 부하 분산 기능도 제공한다. 이것으로 서비스 디스커버리 에이전트를 일시적으로 가용하지 않을 때도 클라이언트가 서비스를 계속 호출할 수 있다.

### 스프링 클라우드 API 게이트웨이
API 게이트웨이는 마이크로서비스 애플리케이션을 위한 서비스 라우팅 기능을 제공한다. 서비스 요청을 프록싱하고 대상 서비스가 호출되기 전에 마이크로서비스에 대한 모든 호출이 '현관'을 통과하도록 하는 서비스 게이투웨이다.

서비스 호출의 중앙 집중화로 보안 인가, 인증, 콘텐츠 필터링과 라우팅 규칙 등 표준 서비스 정책을 시행할 수 있다. Spring Cloud Gateway로 API 게이트웨이를 구현할 수 있다.

### 스프링 클라우드 스트림
Spring Cloud Stream은 경량 메시지 처리 기능을 마이크로서비스에 쉽게 통합하는 기술이며, 애플리케이션에서 발생하는 비동기 이벤트를 사용하는 지능형 마이크로서비스를 구축할 수 있다. 또한 RabbitMQ와 Kafka 같은 메시지 브로커와 마이크로서비스를 빠르게 통합할 수 있다.

### 스프링 클라우드 슬루스
Spring Cloud Sleuth는 고유한 추적 식별자를 애플리케이션에서 사용되는 HTTP 호출 및 메시지 채널에 통합한다. 상관관계 ID나 트레이스 ID라고도 하는 이러한 추적 번호를 사용하면 애플리케이션 내 여러 서비스를 통과하는 트랜잭션을 추적할 수 있다.

Spring Cloud Sleuth의 진정한 가치는 ELK 스택 등 로깅 집계 기술 도구와 Zipkin 등 추적 도구와 결합될 때 발현된다. Open Zipkin은 Spring Cloud Sleuth에서 생성한 데이터를 받아 단일 트랜잭션과 연결된 서비스 호출 흐름을 시각화할 수 있다.

### 스프링 클라우드 시큐리티
Spring Cloud Security는 서비스에 액세스할 수 있는 사용자와 이 사용자가 서비스에서 수행할 수 있는 작업을 제어하는 인증 및 인가 프레임워크다. Spring Cloud Security는 토큰을 기반으로 하기 때문에 서비스는 인증 시스템에서 발급된 토큰을 사용하여 서로 통신할 수 있다. HTTP 호출을 받는 각 서비스는 전달받은 토큰을 확인하여 사용자의 신원과 액세스 권한을 검증하낟.

Spring Cloud Security는 JWT도 지원하며, JWT는 OAuth2 토큰 생성 포맷을 표준화하고 생성된 토큰에 대한 서명을 정규화한다.

## 클라우드 네이티브 마이크로서비스 구축 방법
클라우드는 특정 장소가 아니라 가상의 인프라스트럭처를 사용하여 로컬 머신과 사설 데이터 센터를 대체할 수 있는 기술 자원의 관리 시스템이다. 클라우드 애플리케이션에는 여러 수준과 유형이 있지만 이 절에서는 클라우드 레디와 클라우드 네이티브라는 두 가지 유형의 클라우드 애플리케이션에 중점을 두고 설명한다.

클라우드 레디 애플리케이션은 한때 컴퓨터나 현장에 구축된 서버에서 사용되었던 애플리케이션이다. 클라우드 시대가 도래하면서 클라우드에서 실행하는 것을 목표로 정적 환경에서 동적 환경으로 이동했다. 클라우드 레디 애플리케이션으로 만드려면 애플리케이션의 구성 정보를 외부화하여 다양한 환경에 맞게 신속히 적용할 수 있어야 한다. 이것으로 애플리케이션은 빌드 중에 소스 코드를 변경하지 않고 여러 환경에서 실행되도록 만들 수 있다.

클라우드 네이티브 애플리케이션은 클라우드 컴퓨팅 아키텍처의 모든 이점과 서비스를 활용할 수 있도록 특별히 설계되었다. 개발자는 기능을 컨테이너 같은 확장 가능한 마이크로서비스로 나누어 여러 서버에 실행할 수 있다. 그런 다음 이들 서비스는 지속적 전달 워크플로가 지원되는 데브옵스 프로세스를 이용하여 가상의 인프라스트럭처에서 관리된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/84f45eed-05c9-4f25-be32-6fe25fe3f021)

클라우드 레디 애플리케이션은 클라우드에서 작동하기 위해 어떤 변경이나 변환이 필요하지 않다는 점을 이해하는 것이 중요하다. 이 애플리케이션은 하위 컴포넌트가 가용하지 안흔 문제를 처리하도록 설계되어야 한다.

클라우드 네이티브 개발을 위한 네 가지 원칙은 다음과 같다.

- **데브옵스는 개발과 운영의 약어**로 개발자와 IT 운영 간 커뮤니케이션과 협업, 통합에 중점을 둔 소프트웨어 개발 방법론을 의미한다. 주요 목표는 소프트웨어 전달 프로세스와 인프라스트럭처의 변경을 저렴한 비용으로 자동화하는 것이다.
- **마이크로서비스는 작고, 느슨하게 결합된 분산 서비스다.** 마이크로서비스를 이용하여 대규모 애플리케이션을 좁게 정의된 책임을 가진 관리하기 쉬운 컴포넌트로 분해할 수 있다. 또한 명확히 정의된 작은 조각으로 분해하여 대규모 코드베이스에서 발생하는 전통적인 복잡성 문제를 해결하는 데 도움이 된다.
- **지속적 전달은 소프트웨어 개발 관행이다.** 이를 실천하면 소프트웨어 전달 프로세스를 자동화하여 운영 환경에서 단기간 전달(배포)이 가능하다.
- **컨테이너는 VM 이미지에 마이크로서비스를 배포하는 자연스러운 확장이다.** 많은 개발자가 서비스를 한 VM 전체에 배포하는 대신 클라우드에 도커 컨테이너(또는 유사한 컨테이너 기술)로 배포한다.

12 팩터 앱은 클라우드 네이티브 마이크로서비스를 만들 때 참고할 수 있는 모범 사례 지침이다. 이 방법론은 분산 서비스를 구축할 때 동적인 확장과 기본 사항에 관한 개발 및 설계 지침의 모음이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0ebec7ff-ecbd-46ab-a37f-644af9e2d5e6)

### 코드베이스
각 마이크로서비스는 소스 제어 가능한 단일 코드베이스를 가진다. 또한 코드베이스에 서버 프로비저닝 정보를 버전 관리한다고 강조하는 것도 중요하다. 버전 관리는 한 파일 및 파일들의 변경 사항을 관리한다는 것을 기억하자.

코드베이스는 여러 배포 환경을 포함할 수 있지만 다른 마이크로서비스와 공유되지 않는다. 코드베이스가 모든 마이크로서비스에 공유되면 다른 환경에 속하는 많은 불변 릴리스를 만들어 낼 수 있기 때문에 이 점은 중요한 지침이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0cdb2d34-f953-43bf-8d6b-1da5afda508a)

### 의존성
이 사례는 애플리케이션이 Maven이나 Gradle 같은 자바용 비륻 도구로 사용되는 의존성을 선언하는 것이다. 써드 파티 JAR 의존성은 특정 버전 번호를 명시하는데, 이것으로 항상 동일한 라이브러리 버전으로 마이크로서비스를 빌드할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2830e2fe-43c2-40ae-87aa-8c9401b64462)

### 구성 정보
이 사례는 애플리케이션 구성 정보, 특히 환경별 구성 정보를 저장하는 방식에 관한 것이다. **절대로 소스 코드에 구성 정보를 추가하지 마라. 그리고 구성 정보를 배포할 마이크로서비스와 완전히 분리해서 관리하는 것이 최선이다.**

100개로 확장된 특정 마이크로서비스의 구성 정보를 업데이트해야 하는 시나리오를 상상해 보자. 마이크로서비스 내 구성 정보가 포함되어 있다면 100개의 인스턴스를 모두 재배포해야 할 것이다. **하지만 마이크로서비스가 외부 구성 정보를 가져오거나 클라우드 서비스를 사용하면 마이크로서비스를 재시작하지 않고 실행 중 구성 정보를 갱신할 수 있다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f7a1fe3b-27f0-415a-8970-c393b81d06c3)

### 백엔드 서비스
마이크로서비스는 대개 데이터베이스나 API RESTful 서비스, 다른 서버, 메시징 시스템 등과 네트워크로 통신한다. 이 경우 애플리케이션 코드의 변경 없이 로컬 및 써드 파티와 연결하는 데 배포 구현체를 교체할 수 있는지 확인해야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a6576f60-2733-4c96-a473-0aaf7e3f16c1)

### 빌드, 릴리스, 실행
이 모범 사례는 애플리케이션 배포 단계, 즉 빌드, 릴리스, 실행 단계가 철저히 분리되어야 한다는 점을 상기시킨다. 실행할 환경에 독립적으로 마이크로서비스를 구축해야 한다는 것이다. 코드가 빌드되면 런타임 변경 사항은 빌드 프로세스를 거쳐 재배포되어야 하고 빌드된 서비스는 변경될 수 없어야 한다.

릴리스 단계는 빌드된 서비스를 대상 환경 구성 정보와 결합하는 역할을 한다. 각 단계를 분리하지 않으면 아예 추적이 불가하거나 잘해야 어려운 정도의 코드상 문제를 겪게 된다. 예를 들어 운영 환경에 이미 배포된 서비스를 변경한다면, 이 변경 사항은 저장소에 기록되지 않아서 서비스의 새 버전에서 변경 사항이 누락되거나 새 버전에 변경 사항을 다시 복사해야 하는 두 가지 상황이 발생할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e4dd0213-0ad1-40bf-a81b-9b8e572117e3)

### 프로세스
마이크로서비스는 항상 무상태가 되어야 하며 요청받은 트랜잭션을 수행하는 데 필요한 정보만 포함해야 한다. 마이크로서비스는 서비스 인스턴스 손실로 데이터가 손실될 것이라는 걱정 없이 언제든 중단되고 교체될 수 있다. 상태를 저장해야 하는 특별한 요구 사항이 있다면 Redis 같은 메모리 캐시나 백업 데이터베이스를 사용하여 수행할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/597761bd-4049-41e6-afac-75410566b6be)

### 포트 바인딩
포트 바인딩은 특정 포트로 서비스를 게시하는 것을 의미한다. 마이크로서비스 아키텍처에서 마이크로서비스는 서비스 실행 파일로 패키징된 서비스용 런타임 엔진이 포함되어 완전히 독립적이다. 따라서 별도의 웹 서버나 애플리케이션 서버 없이 서비스를 실행할 수 있다. 서비스는 명령줄에서 시작되어야 하고 노출된 HTTP 포트를 사용하여 바로 액세스되어야 한다.

### 동시성
동시성의 모범 사례는 클라우드 네이티브 애플리케이션이 프로세스 모델을 사용해서 확장해야 한다고 설명한다. 중요한 프로세스 하나를 더 크게 만드는 대신 프로세스를 많이 생성하여 서비스 부하 또는 애플리케이션을 여러 프로세스에 분산한다 생각하면 그 의미를 이해할 수 있을 것이다.

수직 확장은 하드웨어 인프라스트럭처를 늘리는 것이며, 수평 확장은 애플리케이션 인스턴스를 더 추가하는 것이다. 확장이 필요하다면 수직 확장 대신 더 많은 인스턴스를 시작해서 수평 확장 하라.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9d00597b-5ec2-4398-9647-1fc1212c2329)

### 폐기 가능
마이크로서비스는 폐기 가능하며 탄력적으로 확장을 유도하고 애플리케이션 코드나 구성 변경 사항을 신속하게 배포하고자 필요에 따라 시작하고 중지할 수 있다. 이상적으로 시작은 시작 명령이 실행되는 순간부터 프로세스가 요청받을 준비가 될 때까지 몇 초 동안 소요된다.

**폐기 가능하다는 것은 다른 서비스에 영향을 주지 않고 새로운 인스턴스로, 실패한 인스턴스를 제거할 수 있다는 의미다.** 예를 들어 내부 하드웨어 고장으로 마이크로서비스 인스턴스 중 하나가 실패하더라도 다른 마이크로서비스에 영향을 주지 않고 해당 인스턴스를 종료한 채 필요한 다른 인스턴스를 시작할 수 있다.

### 개발 및 운영 환경 일치
이 모범 사례는 가능한 유사한 여러 환경을 갖게 된다는 것을 나타낸다. 환경에는 항상 유사한 버전의 인프라 및 서비스뿐 아니라 배포된 코드도 포함되어야 한다. 이는 배포 프로세스를 최대한 자동화하는 지속적 배포로 완성할 수 있어 마이크로서비스를 짧은 시간에 여러 환경에 배포할 수 있다.

코드가 커밋되는 즉시 테스트를 거쳐 개발 환경에서 운영 환경까지 가능한 신속하게 이동해야 한다. 배포 오류를 피하려면 이 지침은 필수적이다. 유사한 개발 및 운영 환경을 유지하면 애플리케이션을 배포하고 실행하는 동안 발생할 수 있는 모든 가능한 시나리오를 통제할 수 있다.

### 로그
로그는 이벤트 스트림이다. 출력된 로그를 수집하고 중앙 저장소에 기록하는 Logstash나 Fluentd와 같은 도구로 관리해야 한다. 마이크로서비스는 이러한 내부 동작 메커니즘에 관여하지 않고 표준 출력으로 로그를 기록하는 데만 집중해야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9a3f8a6f-7f89-449d-9f37-6721a27a5def)

### 관리 프로세스
개발자는 종종 데이터 이전이나 변환 같은 서비스 관리 작업을 해야 한다. 이러한 작업은 임시변통해서는 안 되며 소스 코드 저장소에서 관리 및 유지되는 스크립트로 수행되어야 한다. 스크립트는 실행되는 모든 환경에서 반복 가능하고 변경되지 않아야 하며 각 환경에 따라 개별 수정되지 않는다. 마이크로서비스 실행 중 고려되어야 할 작업 유형을 정의하는 것이 중요한데, 스크립트와 관련된 여러 마이크로서비스가 있을 때는 수동 작업 없이도 모든 관리 작업을 실행할 수 있다.