# 1장. 스프링, 클라우드와 만나다
## 모놀리스 아키텍처
애플리케이션은 배포 가능한 하나의 산출물로 생성된다. 모든 UI, 비즈니스 및 데이터베이스 액세스 로직은 함께 고유한 애플리케이션으로 패키징되어 애플리케이션 서버에 배포된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/cc5ebc1e-9b99-4ca3-82ff-e1416ad3dc13)

- 복잡한 아키텍처보다 구축 및 배포가 더 쉽다.
- 사용 사례가 잘 정의되어 있고 변경 가능성이 낮다면 모놀리스로 시작하는 것이 좋다.
- 크기와 복잡성이 증가하기 시작하면 관리가 어렵다.
- 모든 변경이 애플리케이션의 다른 부분까지 차례로 영향을 줄 수 있고, 운영 환경의 시스템에선 더 많은 시간과 비용이 소요될 것이다.

## 마이크로서비스란?
마이크로서비스는 작고 느슨하게 결합된 분산 서비스다. 마이크로서비스를 사용하면 대규모 애플리케이션을 책임이 명확하고 관리하기 쉬운 구성 요소로 분해할 수 있다. 또한 잘 정의된 작은 조각으로 분해해서 대규모 코드베이스에서 발생하는 전통적인 복잡성 문제를 해결하도록 도울 수 있다.

핵심 개념은 **분해**와 **분리**다. 애플리케이션의 기능은 완전히 상호 독립적이어야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/60d90003-21f3-4dc2-8454-d0d5aee046e1)

- 애플리케이션 로직은 명확하고 대등한 책임 경계가 있는 작은 컴포넌트로 분해된다.
- 각 구성 요소는 작은 책임 영역을 담당하고 서로 독립적으로 배포된다. 한 마이크로서비스는 비즈니스 도메인의 한 부분에 책임을 진다.
- 마이크로서비스는 서비스 소비자와 공급자 간 데이터를 교환하고자 HTTP와 JSON 같은 경량 통신 프로토콜을 사용한다.
- 마이크로서비스 애플리케이션은 항상 기술 중립적 포맷(대표적으로 JSON)을 사용해서 통신하기 때문에 서비스 하부의 기술 구현과 무관하다. 이 말은 마이크로서비스 방식으로 구축된 애플리케이션은 다양한 언어와 기술로 구현될 수 있다는 의미이다.
- 작고 독립적이고 분산적인 마이크로서비스 특성 덕분에 조직은 팀을 더 작게 만들고 명확한 책임 영역을 부여할 수 있다. 이들 팀은 애플리케이션 출시처럼 하나의 목표를 향해 일하더라도 각 팀은 그들이 작업하는 서비스에만 책임을 진다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/8a20b1e6-5523-4df9-aed1-045a5aa7e430)

## 애플리케이션 구축 방법을 왜 바꿔야 할까?
- 복잡성이 증가했다.
- 고객은 더 빠른 전달을 원한다.
- 고객 또한 안정적인 성능과 확장성을 요구한다.
- 고객은 애플리케이션을 언제든 사용할 수 있길 기대한다.

애플리케이션을 더 작은 서비스로 분리하고 단일 모놀리식 산출물에서 서비스 산출물을 추출하면 우리는 다음 시스템을 구축할 수 있다.

- 유연성
- 회복성
- 확장성

## 스프링 마이크로서비스
스프링 부트는 마이크로서비스에 다음 이점을 제공한다.

- 개발 시간 단축, 효율성과 생산성 향상
- 웹 애플리케이션 실행을 위한 내장형 HTTP 서버 제공
- 많은 상용구 코드 작성 회피
- Spring Data, Spring Security, Spring Cloud와 같은 스프링 생태계와 통합 용이
- 다양한 개발 플러그인 제공

Spring Cloud를 사용하면 private, public 클라우드에 마이크로서비스를 간단하게 운영하고 배포할 수 있다. 여러 가지 인기 있는 클라우드 관리형 마이크로서비스 프레임워크를 공통 프레임워크에 포함하여 코드에 애너테이션을 추가하는 것만큼 쉽게 이러한 기술을 사용하고 배포할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6ac40085-6e0f-4f55-bedd-2db44bc43d07)

## 마이크로서비스는 코드 작성 이상을 의미한다
견고한 서비스를 작성하려면 여러 가지 주제를 고려해야 한다.

- 적정 규모: 서비스가 한 가지 책임 영역에 집중되도록 하려면 어떻게 해야 하는가?
- 위치 투명성: 서비스 클라이언트에 영향을 주지 않고 서비스 인스턴스를 추가하고 삭제하려면 물리적 위치를 어떻게 관리해야 하는가?
- 회복성: 서비스에 문제가 있을 때 서비스 클라이언트가 빠르게 실패하려면 어떻게 해야 하는가?
- 반복성: 새로운 서비스가 시작할 때마다 항상 기존과 동일한 코드와 구성을 갖게 하려면 어떻게 해야 하는가?
- 확장성: 서비스 간 종속성을 최소화하면서 애플리케이션을 신속히 확장하려면 어떻게 해야 하는가?

이 책에선 다음 마이크로서비스 패턴을 다룬다.
- 핵심 개발 패턴
- 라우팅 패턴
- 클라이언트 탄력성 패턴
- 보안 패턴
- 로깅 및 추적 패턴
- 애플리케이션 지표 해턴
- 빌드 및 배포 패턴

## 핵심 마이크로서비스 개발 패턴
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b7c17dd7-21a0-4bb7-bc63-3e82a02156a6)

- **서비스 세분성**: 비즈니스 도메인을 마이크로서비스로 분해하여 각 서비스가 적정 수준의 책임을 갖도록 하는 방법은 어떤 것이 있을까?
- **통신 프로토콜**: 마이크로서비스들끼리 데이터 통신하는 방법?
- **인터페이스 설계**: 개발자가 서비스 호출에 사용하는 실제 서비스 인터페이스를 직관적으로 사용할 수 있도록 설계하는 가장 좋은 방법은 어떤 것이 있을까? 서비스를 어떻게 구조화할 것인가?
- **서비스 구성 관리**: 클라우드에 있는 서로 다른 환경 간 마이크로서비스의 구성을 호환하려면 어떻게 구성을 관리해야 하는가?
- **서비스 간 이벤트 처리**: 서비스간 하드코딩된 의존성을 최소화하고 애플리케이션의 탄력성을 높이고자 이벤트를 사용하여 서비스를 분리하는 방법은 무엇인가?

## 요약
- 모놀리식 아키텍처에서 모든 프로세스는 강하게 결합되어 하나의 서비스로 실행된다.
- 마이크로서비스는 하나의 특정 영역을 담당하는 매우 작은 기능 부분이다.
- 스프링 부트를 사용하면 두 유형의 아키텍처를 모두 구축할 수 있다.
- 모놀리식 아키텍처는 단순하고 가벼운 애플리케이션에 이상적이며 마이크로서비스 아키텍처는 일반적으로 복잡하고 진화하는 애플리케이션에 더 적합하다. 결과적으로 소프트웨어 아키텍처의 선택은 프로젝트의 규모, 기간, 요구 사항 등 다른 요소에 전적으로 좌우된다. 스프링 부트는 REST 기반/JSON 마이크로서비스 구축을 단순화한다. 그 목표는 몇 가지 애너테이션만으로도 마이크로서비스를 신속하게 구축할 수 있게 하는 것이다.
- 마이크로서비스를 작성하는 것은 쉽지만, 실제 환경에서 완전하게 운영하려면 사전에 추가로 고려할 사항이 많다.
- 마이크로서비스 라우팅 패턴은 마이크로서비스를 사용하려는 클라이언트 애플리케이션이 서비스 위치와 서비스로 라우팅되는 방법을 다룬다.
- 서비스 인스턴스 문제가 서비스 소비자에게 연쇄적으로 전파되는 것을 방지하려면 클라이언트 회복성 패턴을 사용하라. 이 패턴에는 실패한 서비스에 대한 호출을 피할 수 있는 회로 차단기 패턴, 서비스가 실패할 때 데이터를 조회하거나 특정 작업을 수행하는 대체 경로를 만드는 폴백 패턴, 가능한 모든 병목 및 장애 시나리오 지점을 해소하고 제거하는 클라이언트 부하 분산 패턴, 다른 서비스에 악영향을 미치는 성능 낮은 서비스에 호출을 방지하도록 서비스에 대한 동시 호출 수를 제한하는 벌크헤드 패턴이 포함된다.
- OAuth 2.0은 가장 보편화된 사용자 인가 프로토콜이며 마이크로서비스를 보호할 수 잇는 탁월한 선택이다.
- 빌드/배포 패턴을 사용하면 인프라스트럭처의 구성을 빌드/배포 프로세스에 바로 통합할 수 있어 자바 WAR나 EAR 파일 같은 산출물을 이미 실행 중인 인프라스트럭처에 배포하지 않아도 된다.