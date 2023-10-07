# Story 20. 모니터링 API인 JMX
애플리케이션 서버 모니터링 툴을 사용하지 않고도 어느 정도 유용한 정보를 볼 수 있는 기능을 제공하는 것이 바로 JMX다. JDK 5.0 버전 이상의 서버에선 필수로 제공되기 때문에, 이를 토대로 모니터링이 가능하다.

## JMX란?
JMX는 Java Management Extensions의 약자이다. 보통 Sun에서는 JMX 기술이라고 표시한다. 여기서는 그냥 간단하게 JMX라고 하자.

우선 JMX가 어떻게 구성되어 있는지 간단하게 알아보자.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/88b92998-52b9-4717-8a62-d03f7a0eda70)

위 그림을 보면 구성이 엄청나게 복잡하다. 여기 명시된 JMX의 버전은 2006년 말에 나온 버전을 기초로 하고 있으며, 지금까지 거의 변경 사항이 없다고 생각해도 된다. 현재 사용중인 JDK의 JMX에 대해 확인해 보려면 자바 API가 있는 docs의 `docs/technotes/guides/jmx` 디렉터리에 있는 설명 파일들을 확인해보면 된다. 먼저 JMX의 4단계 레벨에 대해 알아보자.

- 인스트루먼테이션 레벨(Instrumentation Level)
- 에이전트 레벨(Agent Level)
- 분산 서비스 레벨(Distributed Services Level)
- 추가 가능한 관리용 프로토콜 API들(Additional Management Protocol APIs)

위의 그림 좌측을 보면 각 레벨 영역이 표시되어 있다. 그리고 추가적으로 다른 관리 프로토콜 API 연동을 위한 레벨이 우측에 표시되어 있다. 상단은 모니터링하는 화면들이고 하단은 서버측이라고 생각하면 된다. 그럼 각 레벨에 대해서 조금 더 상세하게 알아보자.

### 인스트루먼테이션 레벨
여기에선 하나 이상의 MBeans(Management Beans, 관리 빈즈)를 제공한다. 이 MBeans에서 필요한 리소스들의 정보를 취합하여 에이전트로 전달하는 역할을 한다. API를 통해 최소한의 노력으로 MBean의 처리 내용을 전달할 수 있도록 되어 있다.

### 에이전트 레벨
이 레벨에선 에이전트를 구현하기 위한 스펙이 제공되어 있다. 에이전트는 리소스를 관리하는 역할을 수행한다. 보통 에이전트는 모니터링이 되는 서버와 같은 장비에 위치한다. 에이전트는 MBean 서버와 MBeans를 관리하는 서비스의 집합으로 구성되어 있다. JMX의 데이터를 관리하는 관리자와 연계를 위한 어댑터나 커넥터를 이 레벨에서 제공한다.

### 분산 서비스 레벨
분산 서비스 레벨은 JMX 관리자를 구현하기 위한 인터페이스와 컴포넌트를 제공한다. 여러 에이전트에서 제공하는 정보를 관리할 수 있는 화면과 같은 부분을 여기서 담당한다고 생각하면 이해가 쉬울 것이다.

이와 같이 JMX는 주로 3개의 레벨로 나뉘어져 서비스를 제공한다. 이러한 구조를 이용해 모니터링하고자 하는 내용을 개발하여 사용할 수도 있고, 서버에서 JMX의 스펙을 구현하여 제공하는 기능을 사용할 수도 있다.

## MBean에 대해 조금만 더 자세히 알아보자
JMX를 제대로 이해하기 위해서는 MBean에 대해서 정확하게 이해하고 있어야 한다. MBean은 4가지 종류가 있으며 각각의 사용 용도는 다음과 같다.

- 표준 MBean(Standard MBean): 변경이 많지 않은 시스템을 관리하기 위한 MBean이 필요한 경우 사용한다.
- 동적 MBean(Dynamic MBean): 애플리케이션이 자주 변경되는 시스템을 관리하기 위한 MBean이 필요한 경우 사용한다.
- 모델 MBean(Model MBean): 어떤 리소스나 동적으로 설치가 가능한 MBean이 필요한 경우 사용한다.
- 오픈 MBean(Open MBean): 실행 중에 발견되는 객체의 정보를 확인하기 위한 MBean이 필요할 때 사용한다. JMX의 스펙에 지정된 타입만 리턴해야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1a95e600-3231-4345-b972-1c0a8ec0ca38)

각각의 MBean은 에이전트 서비스를 통해 MBean 서버에 데이터를 전달하게 된다. 이 MBean 서버를 통해서 클라이언트에서 서버의 상황을 모니터링할 수 있다. 이 에이전트가 제공하는 기능은 다음과 같다.

- 현재 서버에 있는 MBean의 다음 기능들을 관리한다.
  - MBean의 속성값을 얻고, 변경한다.
  - MBean의 메서드를 수행한다.
- 모든 MBean에서 수행된 정보를 받는다.
- 기존 클래스나 새로 다운로드된 클래스의 새로운 MBean을 초기화하고 등록한다.
- 기존 MBean들의 구현과 관련된 관리 정책을 처리하기 위해서 에이전트 서비스를 사용되도록 한다.

필요에 따라 에이전트 서비스를 통해서 서버의 메서드들을 수행하고 값을 변경할 수 있다.

## Visual VM을 통한 JMX 모니터링
JDK를 설치하고 보면 `bin`이라는 디렉터리를 확인할 수 있다. 그 디렉터리 아래에는 `java`와 `javac`, `javadoc`만 있는 것이 아니다. 여러 가지 다양한 툴이 존재하며, 그 중에서 모니터링을 위한 `jconsole`과 `jvisualvm`(이하 Visual VM)이라는 툴도 존재한다. 이 두개의 툴은 모두 JVM을 모니터링하기 위해서 만들어진 툴이며, `jconsole`은 구식 툴이고 Visual VM이 최신 툴이라 생각하는 것이 편하다. 이 두 가지 툴 모두 JMX의 데이터를 볼 수 있도록 만들어졌다.

추가로 Mission Control이라는 툴도 있다. 자바를 만든 Sun이 몰락하고, Oracle이 Sun을 인수했다. 그런데, 오라클에는 이미 JRockit이라는 훌륭한 JVM을 보유하고 있으며, 이 JVM은 오라클의 WebLogic과 같은 WAS를 구매하면 무료로 사용할 수 있다. 이 JRockit에는 Misssion Control이라는 훌륭한 모니터링 툴이 존재한다.

## 원격으로 JMX를 사용하기 위해서는...
원격지에 있는 서버와 통신을 하여 JMX 모니터링을 하기 위해서는 서버나 자바 애플리케이션을 시작할 때 VM 옵션을 지정해야 한다. 일단 간단하게 사용하려면 다음의 3가지 옵션을 지정할 수 있다.

- `Dcom.sun.management.jmxremote.port=9003`
- `Dcom.sun.management.jmxremote.ssl=false`
- `Dcom.sun.management.jmxremote.authenticate=false`

이렇게 지정하면 아이디와 패스워드를 지정할 필요 없이 서버의 IP와 포트만으로 서버에 원격으로 접속할 수 있다.

아이디와 패스워드를 지정하여 접속할 수 있도록 변경하려면 다음과 같이 지정할 수 있다. 여기서 `jmxremote.password` 및 `jmxremote.access`는 패스워드와 권한이 저장되어 있는 파일 이름이다.

- `Dcom.sun.management.jmxremote.port=9003`
- `Dcom.sun.management.jmxremote.password.file=/파일위치/conf/jmxremote.password`
- `Dcom.sun.mangament.jmxremote.access.file=/파일위치/conf/jmxremote.access`
- `Dcom.sun.management.jmxremote.ssl=false`

## 참고로...
JMX 모니터링툴은 굉장히 다양하다. CPU, JVM, 커넥션 정보 등을 JMX 툴에 전달한다 가정하면, 각각의 정보를 JMX 모니터링 툴이 정한 포멧에 맞추어 측정하고 전달해야 한다.

<img width="625" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e212d18c-0646-4045-8ab7-1c23ee6bfcad">

그런데 중간에 사용하는 모니터링 툴을 변경하게 된다면, 기존에 측정했던 코드를 모두 변경한 툴에 맞도록 다시 변경해야 한다. 개발자 입장에선 단순히 툴 하나를 변경했을 뿐인데, 측정하는 코드까지 모두 변경해야 하는 문제가 발생한다. 이런 문제를 해결하는 것이 바로 **Micrometer**라는 라이브러리다.

<img width="627" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1ad721b7-feaf-491c-aa0a-ea08f02f4647">

Spring 마이크로미터와 메트릭에 대해선 이전에 블로그에 정리한 글을 참고하도록 하자.

- [Spring micrometer와 metric | Ramos.log](https://velog.io/@songs4805/Spring-micrometer%EC%99%80-metric)