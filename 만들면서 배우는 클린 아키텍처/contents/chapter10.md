# 10장. 아키텍처 경계 강제하기
## 경계와 의존성
아키텍처의 어디에 경계가 있고, '경계를 강제한다'는 것이 어떤 의미인지 먼저 살펴보자.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/4ab5da0e-e46b-41e7-b3e2-4cf12f6f65a7)

가장 안쪽의 계층에는 도메인 엔티티가 있다. 애플리케이션 계층은 애플리케이션 서비스 안에 유스케이스를 구현하기 위해 도메인 엔티티에 접근한다. 어댑터는 인커밍 포트를 통해 서비스에 접근하고, 반대로 서비스는 아웃고잉 포트를 통해 어댑터에 접근한다. 마지막으로 설정 계층은 어댑터와 서비스 객체를 생성할 팩터리를 포함하고 있고, 의존성 주입 메커니즘을 제공한다.

의존성 규칙에 따르면 계층 경계를 넘는 의존성은 항상 안쪽 방향으로 향해야 한다.

이번 장에서는 이러한 의존성 규칙을 강제하는 방법을 알아보고 잘못된 방향을 가리키는 의존성을 없게 만들고자 한다.

## 접근 제한자
`package-private` 제한자는 중요하다. Java 패키지를 통해 클래스들을 응집적인 '모듈'로 만들어 주기 때문이다. 이러한 모듈 내에 있는 클래스들은 서로 접근 가능하지만, 패키지 바깥에서는 접근할 수 없다. 모듈의 진입점으로 활용될 클래스들만 골라 `public`으로 만들면 된다. 이렇게 하면 의존성이 잘못된 방향을 가리켜서 의존성 규칙을 위반할 위험이 줄어든다.

`package-private` 제한자는 몇 개 정도의 클래스로만 이뤄진 작은 모듈에서 가장 효과적이다. 그러나 패키지 내의 클래스가 특정 개수를 넘어가기 시작하면 하나의 패키지에 너무 많은 클래스를 포함하는 것이 혼란스러워지게 된다. 이렇게 되면 코드를 쉽게 찾을 수 있도록 하위 패키지를 만드는 방법을 선호한다. 하지만 이렇게 하면 Java는 하위 패키지를 다른 패키지로 취급하기 때문에 `package-private` 맴버에 접근할 수 없어 `public`으로 만들어 노출시켜야 하기 때문에 아키텍처에서 의존성 규칙이 깨질 수 있는 환경이 만들어진다.

## 컴파일 후 체크
클래스에 `public` 제한자를 쓰면 아키텍처 상의 의존성 방향이 잘못되더라도 컴파일러는 다른 클래스들이 이 클래스를 사용하도록 허용한다. 이런 경우에는 컴파일러가 전혀 도움이 되지 않기 때문에 의존성 규칙을 위반했는지 확인할 다른 수단을 찾아야 한다.

한 가지 방법은 **컴파일 후 체크**를 도입하는 것이다. 다시 말해, 코드가 컴파일된 후에 런타임에 체크한다는 뜻이다. 이러한 런타임 체크는 지속적인 통합 빌드 환경에서 자동화된 테스트 과정에서 가장 잘 동작한다.

이러한 체크를 도와주는 자바용 도구로 ArchUnit이 있다. ArchUnit은 의존성 방향이 기대한 대로 잘 설정돼 있는지 체크할 수 있는 API를 제공한다. 의존성 규칙 위반을 발견하면 예외를 던진다.

## 빌드 아티펙트
지금까지 코드 상에서 아키텍처 경계를 구분하는 유일한 도구는 패키지였다. 모든 코드가 같은 모놀리식 빌드 아티팩트의 일부였던 셈이다.

빌드 아티팩트는 자동화된 빌드 프로세스의 결과물이다. (Maven, Gradle)

빌드 도구의 주요한 기능 중 하나는 **의존성 해결**이다. 어떤 코드베이스를 빌드 아티팩트로 변환하기 위해 빌드 도구가 가장 먼저 할 일은 코드베이스가 의존하고 있는 모든 아티팩트가 사용 가능한지 확인하는 것이다. 만약 사용 불가능한 것이 있다면 아티팩트 리포지토리로부터 가져오려고 시도한다. 이마저도 실패하면 에러와 함께 빌드가 실패한다.

이를 활용해 모듈과 아키텍처의 계층 간의 의존성을 강제할 수 있다. 각 모듈 혹은 계층에 대해 전용 코드베이스와 빌드 아티팩트로 분리된 빌드 모듈(JAR 파일)을 만들 수 있다. 각 모듈의 빌드 스크립트에서는 아키텍처에서 허용하는 의존성만 지정한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/4cc936eb-a369-4c6a-bfa3-0af0c2ba1aba)

**핵심은 모듈을 더 세분화할수록, 모듈 간 의존성을 더 잘 제어할 수 있게 된다는 것이다.** 하지만, 더 작게 분리할수록 모듈 간에 매핑을 더 많이 수행해야 한다.

이 밖에도 빌드 모듈로 아키텍처 경계를 구분하는 것은 패키지로 구분하는 방식과 비교했을 때 몇 가지 장점이 있다.

첫 번째로, 빌드 도구가 순환 의존성을 싫어한다는 것이다. 순환 의존성은 하나의 모듈에서 일어나는 변경이 잠재적으로 다른 모든 모듈을 변경하게 만들며 단일 책임 원칙을 위배한다. 빌드 도구를 이용하면 빌드 모듈 간 순환 의존성이 없음을 확신할 수 있다.

두 번째로, 빌드 모듈 방식에서는 다른 모듈을 고려하지 않고 특정 모듈의 코드를 격리한 채로 변경할 수 있다. 그러므로 여러 개의 빌드 모듈은 각 모듈을 격리한 채로 변경할 수 있게 해준다.

마지막으로, 모듈 간 의존성이 빌드 스크립트에 분명하게 선언돼 있기 때문에 새로 의존성을 추가하는 일은 우연이 아닌 의식적인 행동이 된다. 어떤 개발자가 당장은 접근할 수 없는 특정 클래스에 접근해야 할 일이 생기면 빌드 스크립트에 이 의존성을 추가하기에 앞서 정말로 이 의존성이 필요한 것이지 생각할 여지가 생긴다.

하지만 이런 장점에는 빌드 스크립트를 유지보수하는 비용을 수반하기 때문에 아키텍처를 여러 개의 빌드 모듈로 나누기 전에 아키텍처가 어느 정도는 안정된 상태여야 한다.

## 유지보수 가능한 소프트웨어를 만드는 데 어떻게 도움이 될까?
기본적으로 소프트웨어 아키텍처는 아키텍처 요소 간의 의존성을 관리하는 게 전부다.

그렇기 때문에 아키텍처를 잘 유지해나가고 싶다면 의존성이 올바른 방향을 가리키고 있는지 지속적으로 확인해야 한다.

새로운 코드를 추가하거나 리팩터링할 때 패키지 구조를 항상 염두에 둬야 하고, 가능하다면 `package-private` 가시성을 이용해 패키지 바깥에서 접근하면 안 되는 클래스에 대한 의존성을 피해야 한다.

하나의 빌드 모듈 안에서 아키텍쳐 경계를 강제해야 한다.

그리고 아키텍처가 충분히 안정적이라고 느껴지면 아키텍처 요소를 독립적인 빌드 모듈로 추출해야 한다. 그래야 의존성을 분명하게 제어할 수 있기 때문이다.