# 2장. 분해 전략
## 마이크로서비스 아키텍처란 무엇인가?
소프트웨어 아키텍처는 구성 요소 및 그들 간의 디펜던시로 엮인 고수준의 구조물이다. 애플리케이션 아키텍처는 다차원적이므로 기술하는 방법도 다양하다. 아키텍처가 중요한 이유는 소프트웨어의 품질 속성 지표가 아키텍처에 의해 결정되기 때문이다.

확장성, 신뢰성, 보안 등이 아키텍처의 목표였지만, 이제는 신속/안전하게 소프트웨어를 전달하는 능력도 매우 중요하다. 마이크로서비스 아키텍처는 관리성, 테스트성, 배포성이 높은 애플리케이션을 구축하는 아키텍처 스타일이다.

### 소프트웨어 아키텍처의 정의
> 컴퓨팅 시스템의 소프트웨어 아키텍처는 소프트웨어 엘리먼트와 그들 간의 관계, 그리고 이 둘의 속성으로 구성된 시스템을 추론하는 데 필요한 구조의 집합이다. - Documenting Software Architectures, Bass 등

핵심은 애플리케이션 아키텍처가 여러 파트로의 분해와 이런 파트 간의 관계(연관성)라는 것이다. 분해가 중요한 이유는 다음 두 가지 때문이다.
- 업무와 지식을 분리한다. 덕분에 전문 지식을 보유한 사람들(또는 여러 팀)이 함께 생산적으로 애플리케이션 작업을 할 수 있다.
- 소프트웨어 엘리먼트가 어떻게 상호 작용하는지 밝힌다.

### 소프트웨어 아키텍처의 4+1 뷰 모델
- 논리 뷰(logical view): 개발자가 작성한 소프트웨어 엘리먼트, 객체 지향 언어라면 클래스 패키지가 해당되며 결국 상속, 연관, 의존 등 클래스와 패키지의 관계를 말한다.
- 구현 뷰(implementation view): 빌드 시스템의 결과물, 모듈(패키징된 코드)과 컴포넌트(하나 이상의 모듈로 구성된 실행/배포 가능 단위)로 구성된다. Java에서 모듈은 보통 JAR 파일, 컴포넌트는 WAR 파일이나 실행 가능한 JAR 파일이다. 모듈 간 디펜던시와 컴포넌트/모듈 간 조합 관계도 이 뷰에 포함된다.
- 프로세스 뷰(process view): 런타임 컴포넌트. 각 엘리먼트는 개별 프로세스고, IPC는 프로세스 간 관계를 나타낸다.
- 배포 뷰(deployment view): 프로세스가 머신에 매핑되는 방법. 이 뷰의 엘리먼트는 (물리 또는 가상) 머신 및 프로세스고, 머신 간의 관계가 바로 네트워킹이다. 프로세스와 머신 사이의 관계도 이 뷰에서 기술된다.

이 외에도 뷰를 구동시키는 시나리오가 있다. 각 시나리오는 특정 뷰 내에서 얼마나 다양한 아키텍처 요소가 협동하여 요청을 처리하는지 기술한다.

### 아키텍처의 중요성
애플리케이션의 요건은 크게 두 가지 종류로 나뉜다.
- 애플리케이션이 해야 할 일을 정의한 기능 요건
- '~성'으로 끝나는 서비스 품질 요건

## 계층화 아키텍처 스타일
소프트웨어 엘리먼트를 계층별로 구성하는 전형적인 아키텍처 스타일이다. 계층마다 명확히 정의된 역할을 분담하며, 계층 간 디펜던시는 아키텍처로 제한한다. 따라서 어떤 계층은 바로 하위에 있는 계층에만 의존하거나(계층화를 엄격히 할 경우), 하위에 위치한 어느 한 계층에 의존한다.

- presentation layer: 사용자 인터페이스 또는 외부 API가 구현된 계층
- business logic layer: 비즈니스 로직이 구현된 계층
- persistence layer: DB 상호 작용 로직이 구현된 계층

계층화 아키텍처는 몇 가지 중요한 흠이 있다.
- 표현 계층이 하나뿐이다: 애플리케이션을 호출하는 시스템이 하나밖에 없을까?
- 영속화 계층이 하나뿐이다: 애플리케이션이 상호 작용하는 DB가 정말 하나뿐일까?
- 비즈니스 로직 계층을 영속화 계층에 의존하는 형태로 정의한다: 이론적으로 이런 디펜던시 때문에 DB 없이 비즈니스 로직을 테스트하는 것은 불가능함

또한 잘 설계된 애플리케이션에서 디펜던시를 잘못 나타내는 문제도 있다.  
일반적으로 비즈니스 로직 계층은 인터페이스나 데이터 접근 메서드가 정의된 인터페이스 리포지터리를 정의하고, 영속화 계층은 리포지터리 인터페이스를 구현한 DAO 클래스를 정의한다. 결국 실제 디펜던시가 계층화 아키텍처에서 기술된 것과는 정반대이다.

## 헥사고날 아키텍처 스타일
애플리케이션에 표현 계층 대신 비즈니스 로직을 호출하여 외부에서 들어온 요청을 처리하는 인바운드 어댑터들과 영속화 계층 대신 비즈니스 로직에 의해 호출되고 외부 애플리케이션을 호출하는 아웃바운드 어댑터들을 둔다. **비즈니스 로직이 어댑터에 전혀 의존하지 않는다는 것이 이 아키텍처의 가장 중요한 특장점이다.**

![](https://velog.velcdn.com/images/songs4805/post/5e58d270-5cdb-4b0c-a91b-db9614790bad/image.png)

- 인바운드 포트: 비즈니스 로직이 표출된 API로서, 외부 애플리케이션은 이 API를 통해 비즈니스 로직을 호출한다. (ex. Service의 public 메서드가 정의된 Service Interface)
- 아웃바운드 포트: 비즈니스 로직이 외부 시스템을 호출하는 방법에 관한 것. (ex. 데이터 접근 작업이 정의된 Repository Interface)
- 인바운드 어댑터: 외부에서 들어온 요청을 인바운드 포트를 호출해서 처리. (ex. REST 끝점, 웹 페이지가 구현된 Spring MVC 컨트롤러, 메시지를 구독하는 메시지 브로커 클라이언트)
  - 동일한 인바운드 포트를 여러 인바운드 어댑터가 호출할 수도 있다.
- 아웃바운드 어댑터: 비즈니스 로직에서 들어온 요청을 외부 애플리케이션/서비스를 호출해서 처리. (ex. DB 작업이 구현된 데이터 접근 객체(DAO) 클래스, 원격 서비스를 호출하는 프록시 클래스)
  - 아웃바운드 어댑터는 이밴트를 발행하기도 한다.

육각형 아키텍처 스타일의 가장 큰 장점은 **비즈니스 로직에 있던 표현/데이터 접근 로직이 어댑터와 분리되었기 때문에 비즈니스 로직이 표현/데이터 접근 로직 어디에도 의존하지 않는다는 점이다.**

이렇게 분리를 하니 **비즈니스 로직만 따로 테스트하기도 쉽고, 현대 에플리케이션 아키텍처를 좀 더 명확하게 반영할 수 있다.** 제각기 특정한 API나 UI가 구현된 인바운드 어댑터가 비즈니스 로직을 호출하고, 비즈니스 로직은 다양한 외부 시스템을 호출하는 아웃바운드 어댑터를 호출하는 구조다. MSA를 이루는 각 서비스 아키텍처를 기술하는 가장 좋은 방법이다.

![](https://velog.velcdn.com/images/songs4805/post/b02b1628-f61a-4cdb-b39b-29cff5b9e992/image.png)

**모놀리식 아키텍처는 구현 뷰를 단일 컴포넌트(하나의 실행 파일이나 WAR 파일)로 구성한 아키텍처 스타일이다.** 다른 뷰는 일체 등장하지 않는다.

마이크로서비스 아키텍처는 **구현 뷰를 다수의 컴포넌트(여러 실행 파일이나 WAR 파일)로 구성하는 차이점이 있다.** 여기서 컴포넌트는 곧 서비스고, 각 서비스는 자체 논리 뷰 아키텍처를 갖고 있다. 커넥터는 이런 서비스가 서로 협동할 수 있게 해주는 통신 프로토콜이다.

**마이크로서비스 아키텍처의 핵심 제약 조건은 서비스를 느슨하게 결합한다는 것이다.**

### 서비스란 무엇인가?
**서비스는 어떤 기능이 구현되어 단독 배포가 가능한 소프트웨어 컴포넌트이다.** 클라이언트가 자신이 서비스하는 기능에 접근할 수 있도록 커멘드, 쿼리, 이벤트로 구성된 API를 제공한다. 서비스 작업은 크게 Command(명령/CUD)와 Query(조회/R)로 나뉜다. 서비스는 클라이언트가 소비하는 `OrderCreated`와 같은 이벤트를 발행하기도 한다.

서비스 API는 내부 구현 상세를 캡슐화한다. 모놀리스와 달리 개발자는 API를 우회하여 서비스에 접근하는 코드를 작성할 수 없으므로 MSA에서 애플리케이션 모듈성은 보장된다.

API는 서비스에 구현된 비즈니스 로직과 소통하는 어댑터를 이용하여 구현한다. 작업 어댑터(operations adapter)는 비즈니스 로직을 호출하고 이벤트 어댑터(events adapter)는 비즈니스 로직이 내어 준 이벤트를 발행한다.

![image](https://github.com/alanhakhyeonsong/TIL_SpringBoot/assets/60968342/ac45ef76-7e31-4fbd-8598-4719f7a44cd7)

### 느슨한 결합
서비스는 구현 코드를 감싼 API를 통해서만 상호 작용하므로 클라이언트에 영향을 끼치지 않고 서비스 내부 구현 코드를 바꿀 수 있다. 느슨하게 결합된 서비스는 유지보수성, 테스트성을 높이고 애플리케이션 개발 시간을 단축하는 효과가 있다. 무엇보다 개발자가 서비스를 이해하고, 변경하고, 테스트하기가 더 쉽다.

**서비스가 직접 DB와 통신하는 일은 불가능하다.** 또 클래스 필드 같은 서비스의 영속적 데이터는 반드시 프라이빗으로 유지해야 한다. 이렇게 해야 개발자가 자신이 맡은 서비스의 DB 스키마를 변경할 때 다른 서비스 개발자와 조율하느라 시간을 허비하지 않는다. 서비스가 DB 테이블을 서로 공유하지 않기 때문에 런타임 격리도 향상된다. 어떤 서비스가 DB 락을 획득하여 다른 서비스를 블로킹하는 일 자체가 불가능하다. **물론 DB를 공유하지 않기 때문에 여러 서비스에 걸쳐 데이터를 쿼리하고 일관성을 유지하는 일은 더 복잡해지는 단점이 있다.**

### 공유 라이브러리의 역할
코드 중복을 방지하기 위해 여러 애플리케이션에서 재사용 가능한 기능을 라이브러리(모듈)로 패키징하는 것은 개발자에게 당연한 일이다. MSA에서도 공유 라이브러리를 사용하고픈 유혹에 빠지기 쉬운데, 서비스 코드 중복을 줄이는 것은 좋지만 의도치 않은 서비스 간 결합도를 유발하지 않도록 조심해야 한다.

### 서비스 규모는 별로 중요하지 않다
크기보다는 작은 팀이 가장 짧은 시간에, 다른 팀과 협동하는 부분은 최소로 하여 개발 가능한 서비스를 설계해야 한다. 이론적으론 한 팀이 한 서비스를 맡을 수도 있는데, 이런 경우라면 마이크로서비스가 '마이크로'하다고 볼 수 없다. 반대로 대규모 팀을 꾸려야 하거나 서비스를 테스트하는 시간이 너무 오래 걸리면 팀과 서비스를 분할해야 한다. 다른 서비스의 변경분 때문에 내가 맡은 서비스도 계속 바꾸어야 한다거나, 내 서비스 때문에 다른 서비스가 바뀌어야 한다면 서비스가 느슨하게 결합되지 않았다는 반증이다.

## 마이크로서비스 아키텍처 정의
![image](https://github.com/alanhakhyeonsong/TIL_SpringBoot/assets/60968342/cfa2b435-201e-4f80-8baf-4aadb09eb2ed)

애플리케이션은 사용자의 요청을 처리하기 위해 존재한다. 아키텍처를 정의하는 단계는 다음과 같다.

1. 애플리케이션 요건을 핵심 요청으로 추출한다.
2. 어떻게 여러 서비스로 분해할지 결정한다.
3. 서비스별로 API를 정의한다.

분해 과정에는 장애물이 많다.
- 네트워크 지연
- 서비스 간 동기 통신으로 인해 가용성이 떨어지는 문제 → 자기 완비형 서비스 개념
- 여러 서비스에 걸쳐 데이터 일관성을 지키는 요건 → 사가
- 애플리케이션 도처에 숨어 있는 만능 클래스 → DDD 개념을 활용하여 제거

### 시스템 작업 식별
도메인 모델은 주로 사용자 스토리의 명사에서 도출한다. 이벤트 스토밍이라는 기법을 사용해도 된다. 시스템 작업은 주로 동사에서 도출하며, 각각 하나 이상의 도메인 객체와 그들 간의 관계로 기술한다. 시스템 작업은 도메인 모델을 생성, 수정, 삭제하거나 모델 간 관계를 맺고 끊을 수 있다.

고수준 도메인 모델을 정의하는 방법을 먼저 알아보자. 예를 들어 주문하기 스토리는 아래와 같이 다양한 사용자 시나리오로 확장시킬 수 있다.

```
전제(Given)
  소비자가 있다.
  음식점이 있다.
  음식점은 소비자의 주소로 제시간에 음식을 배달할 수 있다.
  주문 총액이 음식점의 최소 주문량 조건에 부합한다.

조건(When)
  소비자가 음식점에 음식을 주문한다.

결과(Then)
  소비자 신용카드가 승인된다.
  주문이 PENDING_ACCEPTANCE 상태로 생성된다.
  생성된 주문이 소비자와 연관된다.
  생성된 주문이 음식점과 연관된다.
```

이 사용자 시나리오에 포함된 명사를 보면 `Consumer`, `Order`, `Restaurant`, `CreditCard` 등 다양한 클래스가 필요해 보인다.

주문 접수 스토리는 다음 시나리오로 확장할 수 있다.

```
전제(Given)
  현재 주문은 PENDING_ACCEPTANCE 상태다.
  주문 배달 가능한 배달원이 있다.

조건(When)
  주문을 접수한 음식점은 언제까지 음식을 준비할 수 있다고 약속한다.

결과(Then)
  주문의 상태가 ACCEPTED로 변경된다.
  주문의 promiseByTime 값을 음식점이 준비하기로 약속한 시간으로 업데이트한다.
  주문을 배달할 배달원을 배정한다.
```

`Courier`, `Delivery` 클래스 역시 필요할 것 같다. 이런 분석을 몇 차례 거듭하면 여타 클래스도 도출되어 도메인 모델이 완성된다.

![image](https://github.com/alanhakhyeonsong/TIL_SpringBoot/assets/60968342/ee77d623-d418-4afc-9161-cd388cfd4dcd)

시스템 작업 정의 단계는 애플리케이션이 어떤 요청을 처리할지 식별하는 단계이다. 백엔드 애플리케이션의 경우 대부분 HTTP 요청 기반이지만, 메시징을 이용하는 클라이언트도 있을 테니 특정 프로토콜로 제한할 것이 아니라 요청을 나타내는 시스템 작업 개념을 좀 더 추상화하는 것이 좋다.

- 커맨드(Command, 명령): 데이터 생성, 수정, 삭제(CUD)
- 쿼리(Query, 조회): 데이터 읽기(R)

**시스템 커맨드를 식별하려면 사용자 스토리/시나리오에 포함된 동사를 먼저 분석한다.**

|액터|스토리|커맨드|설명|
|---|---|---|---|
|Consumer|주문 생성|`createOrder()`|주문을 생성한다.|
|Restaurant|주문 접수|`acceptOrder()`|음식점에 주문이 접수되었고 주어진 시각까지 음식을 준비하도록 지시한다.|
|Restaurant|주문 픽업 준비됨|`noteOrderReadyForPickup()`|주문한 음식이 픽업 가능함을 알린다.|
|Courier|위치 업데이트|`noteUpdatedLocation()`|배달원의 현재 위치를 업데이트 한다.|
|Courier|배달 픽업|`noteDeliveryPickedUp()`|주문한 음식을 배달원이 픽업했음을 알린다.|
|Courier|주문 배달됨|`noteDeliveryDelivered()`|주문한 음식을 배달원이 소비자에게 배달했음을 알린다.|

커맨드는 매개변수, 반환값, 동작 방식의 명세를 도메인 모델 클래스로 정의한다. 이 명세는 작업 호출 시 충족되어야 할 선행 조건, 작업 호출 후 충족되어야 할 후행 조건으로 구성된다.

선행 조건과 후행 조건을 기반으로 사용자 시나리오를 구성하면 쿼리가 나온다.

고수준 도메인 모델과 시스템 작업을 보면 애플리케이션이 무슨 일을 하는지 알 수 있기 때문에 아키텍처를 정의하는데 대단히 유용하다.

### 서비스 정의: 비즈니스 능력 패턴별 분해
마이크로서비스 아키텍처를 구축하는 첫 번째 전략은 **비즈니스 능력에 따라 분해하는 것이다.**

![image](https://github.com/alanhakhyeonsong/TIL_SpringBoot/assets/60968342/066511de-c404-41df-bffb-0e8628d6df17)

위 서비스는 첫 번째 버전에 불과하다. 도메인을 더 많이 알수록 서비스 역시 점점 더 정교하게 발전할 것이다. 특히 아키텍처를 정의하는 과정에서는 각각의 핵심 아키텍처 서비스와 나머지 서비스가 어떻게 협동하는지 살피는 과정이 중요하다.

### 서비스 정의: 하위 도메인 패턴별 분해
DDD는 객체지향 도메인 중심의 복잡한 소프트웨어 애플리케이션을 구축하는 방법이다. 도메인 내부에서 문제 해결이 가능한 형태로 도메인을 모델링 하는 기법이다. 이는 팀에서 사용할 공용 언어를 정의한다.

기존에는 전체 비즈니스를 포괄하는 단일 통합 모델을 만들다보니 소비자, 주문 등의 비즈니스 엔티티를 각각 따로 정의했다. 그 결과 하나의 모델에 대해 조직 내 여러 부서의 합의를 이끌어 내기가 정말 어려운 단점이 있다.

**DDD는 범위가 분명한 도메인 모델을 여러 개 정의하여 기존 방식의 문제점을 해결하는 전혀 다른 방식의 모델링이다.** 도메인을 구성하는 각 하위 도메인마다 도메인 모델을 따로 정의한다. 하위 도메인은 비즈니스 능력과 같은 방법으로 식별하므로 비즈니스 능력과 유사한 하위 도메인이 도출된다.

도메인 모델의 범위를 경계 컨텍스트라고 한다. 이는 도메인 모델을 구현한 코드 아티팩트를 포함하며, 마이크로 서비스 아키텍처에 DDD를 적용하면 각 서비스들이 경계 컨텍스트가 된다.

DDD와 MSA는 찰떡궁합이다. DDD의 하위 도메인, 경계 컨텍스트 개념은 MSA의 서비스와 잘 맞고, MSA의 서비스 자율팀 개념은 도메인 모델을 개별 팀이 소유/개발한다는 DDD 사고방식과 어울린다.

### 분해 지침
- 단일 책임 원칙(SRP): 클래스는 오직 하나의 변경 사유를 가져야 한다.  
  클래스가 맡은 책임은 각각 그 클래스가 변경될 잠재적 사유이다. 클래스가 독립적으로 변경 가능한 책임을 여럿 짊어지고 있다면 안정적일 수 없다. 이 원칙을 MSA에 적용하면 하나의 책임만 가진 작고 응집된 서비스를 정의 할 수 있다.
- 공동 폐쇄 원칙(CCP): 패키지의 클래스들은 동일한 유형의 변경에 대해 닫혀 있어야 한다. 패키지에 영향을 주는 변경은 그 패키지에 속한 모든 클래스에 영향을 끼친다.  
  **즉, 어떤 두 클래스가 동일한 사유로 맞물려 변경되면 동일한 패키지에 있어야 한다는 것이다.** 이를 잘 지키면 애플리케이션의 유지보수성이 현저히 향상된다. 또한 MSA에 이를 적용하여 구축하면 동일한 사유로 변경되는 컴포넌트들을 모두 같은 서비스로 묶을 수 있다. 요건이 바뀌어도 수정/배포할 서비스 개수는 줄어들 것이다.

### 서비스 분해의 장애물
- 네트워크 지연
- 동기 IPC로 인한 가용성 저하
- 여러 서비스에 걸쳐 데이터 일관성 유지
- 일관된 데이터 뷰 확보
- 분해를 저해하는 만능 클래스

만능 클래스의 예시로는 `Order` 클래스를 들 수 있다. 책의 예제에 따르면, 모든 시스템이 주문과 연관되어 있다. 단일 도메인 모델 체제라면 `Order`는 애플리케이션 곳곳의 상태/동작을 가리키는 아주 큰 클래스일 것이다.

- `Order` 클래스를 라이브러리로 묶고 Order DB를 중앙화해서 주문을 처리하는 모든 서비스가 이 라이브러리를 통해 DB에 접근하도록 만드는 방식 → MSA의 핵심 원칙 위배
- 주문 DB를 주문 서비스 안으로 캡슐화해서 다른 서비스가 주문 서비스를 통해서만 주문을 조회/수정하게 만드는 방식 → 주문 서비스가 비즈니스 로직이 거의 없는 빈껍데기 도메인 모델을 가진 데이터 구조로 전락한다.

DDD를 통해 각 서비스를 자체 도메인 모델을 갖고 있는 개별 하위 도메인으로 취급하는 방법이 가장 좋다.  
즉, 주문과 조금이라도 연관된 서비스는 모두 각자 버전의 `Order` 클래스를 가진 도메인 모델을 따로 두는 것이다.

- 배달 서비스: `Delivery`
- 주방 서비스: `Ticket`