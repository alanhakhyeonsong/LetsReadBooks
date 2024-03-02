# 7장. 나쁜 상황에 대비한 스프링 클라우드와 Resilience4j를 사용한 회복성 패턴

모든 시스템, 특히 분산 시스템은 실패를 겪는다. 이러한 실패에 대응하는 애플리케이션을 구축하는 방법은 모든 소프트웨어 개발자의 업무에서 중요한 부분이다. 하지만 회복성을 갖춘 시스템을 구축할 때 소프트웨어 엔지니어 대부분은 인프라스트럭처나 중요 서비스의 한 부분이 완전히 실패한 경우만 고려한다. 그들은 핵심 서버의 클러스터링, 서비스의 로드 밸런싱, 인프라스트럭처를 여러 곳에 분리하는 것 등의 기술을 사용하여 애플리케이션 각 계층에 중복성을 만드는 데 집중한다.

이런 접근 방식에선 시스템 구성 요소가 완전히 손상될 것을 고려하지만, 회복력 있는 시스템 구축에 대한 작은 한 부분의 문제만 해결할 뿐이다. 서비스가 망가지면 쉽게 감지할 수 있고 애플리케이션은 이를 우회할 수 있다. 하지만 서비스가 느려진다면 성능 저하를 감지하고 우회하는 일은 다음 이유로 매우 어렵다.

- 서비스 성능 저하는 간헐적으로 시작되어 확산될 수 있다.
- 원격 서비스 호출은 대개 동기식이며 장기간 수행되는 호출을 중단하지 않는다.
- 대개 원격 자원의 부분적인 저하가 아닌 완전한 실패를 처리하도록 애플리케이션을 설계한다.

성능이 나쁜 원격 서비스가 야기하는 문제를 간과할 수 없는 것은 이를 탐지하기 어려울 뿐만 아니라 전체 애플리케이션 생태계에 파급되는 연쇄 효과를 유발할 수 있기 때문이다. 보호 장치가 없다면 불량한 서비스 하나가 빠르게 여러 애플리케이션을 다운시킬 수 있다. 클라우드 기반이면서 마이크로서비스 기반 애플리케이션이 이러한 유형의 장애에 특히 취약한 이유는 사용자 트랜잭션을 완료하는 데 연관된 다양한 인프라스터럭처와 함께 다수의 세분화된 서비스로 구성되기 때문이다.

회복성 패턴은 마이크로서비스 아키텍처에서 가장 중요한 요소 중 하나다.

## 클라이언트 측 회복성이란?

클라이언트 측 회복성 소프트웨어 패턴들은 에러나 성능 저하로 원격 자원이 실패할 때 원격 자원의 클라이언트가 고장 나지 않게 보호하는 데 중점을 둔다. 이들 패턴을 사용하면 클라이언트가 빨리 실패하고 데이터베이스 커넥션과 스레드 풀 같은 소중한 자원을 소비하는 것을 방지할 수 있다. 또한 제대로 성능이 낮은 원격 서비스 문제가 소비자에게 '상향'으로 확산되는 것을 막는다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/4f62d037-efbb-44cc-bc35-21c109382379)

### 클라이언트 측 로드 밸런싱

클라이언트 측 로드 밸런싱은 클라이언트가 서비스 디스커버리 에이전트에서 서비스의 모든 인스턴스를 검색한 후 해당 서비스 인스턴스의 물리적 위치를 캐싱하는 작업을 포함한다.

서비스 소비자가 서비스 인스턴스를 호출해야 할 때 클라이언트 측 로드 밸런싱은 관리 중인 서비스 위치 풀에서 위치를 반환한다. 클라이언트 측 로드 밸런서는 서비스 클라이언트와 서비스 소비자 사이에 위치하기 때문에 서비스 인스턴스가 에러를 발생하거나 정상적으로 동작하지 않는지 탐지할 수 있다. 클라이언트 측 로드 밸런서가 문제를 탐지하면 가용 서비스 풀에서 문제된 서비스 인스턴스를 제거하여 해당 서비스 인스턴스로 더 이상 호출되지 않게 한다.

### 회로 차단기

circuit breaker pattern은 전기 회로의 차단기를 모델링했다. 전기 시스템에서 회로 차단기는 전선을 통해 과전류가 흐르는지 탐지한다. 회로 차단기가 문제를 탐지하면 나머지 전기 시스템의 연결을 끊고 하부 구성 요소가 타 버리지 않도록 보호한다.

소프트웨어 회로 차단기는 원격 서비스가 호출될 때 호출을 모니터링한다. 호출이 너무 오래 걸리면 차단기가 개입해서 호출을 종료한다. 회로 차단기 패턴은 원격 자원에 대한 모든 호출을 모니터링하고, 호출이 충분히 실패하면 회초 라단기 구현체가 열리면서 빠르게 실패하고 고장 난 원격 자원에 대한 추가 호출을 방지한다.

### 폴백 처리

fallback pattern을 사용하면 원격 서비스 호출이 실페할 때 예외를 생성하지 않고 서비스 소비자가 대체 코드 경로를 실행하여 다른 수단을 통해 작업을 수행할 수 있다. 여기에는 보통 다른 데이터 소스에서 데이터를 찾거나 향후 처리를 위해 사용자 요청을 Queue에 입력하는 작업이 포함된다. 사용자 호출에 문제가 있다고 예외를 표시하진 않지만 나중에 요청을 시도해야 한다고 알려 줄 수 있다.

예를 들어 사용자 행동 양식을 모니터링하고 구매 희망 항목을 추천하는 기능을 제공하는 전자 상거래 사이트가 있다고 가정해보자. 일반적으로 마이크로서비스를 호출하여 사용자 행동을 분석하고 특정 사용자에게 맞춤화된 추천 목록을 반환한다. 하지만 기호 설정 서비스가 실패하면, 폴백은 모든 사용자의 구매 정보를 기반으로 더욱 일반화된 기호 목록을 검색할 수 있다. 그리고 이 데이터는 완전히 다른 서비스와 데이터 소스에서 추출될 수 있다.

### 벌크헤드

bulkhead pattern은 선박을 건조하는 개념에서 유래되었다. 배는 격벽이라는 완전히 격리된 수밀 구획으로 나뉘는데, 선체에 구멍이 뚫려도 침수 구역을 구멍이 난 격벽으로만 제한하므로 배 전체에 물이 차서 침몰되는 것을 방지한다.

여러 원격 자원가 상호 작용해야 하는 서비스에도 동일한 개념을 적용할 수 있다. 벌크헤드 패턴을 사용할 때 원격 자원에 대한 호출을 자원별 스레드 풀로 분리하면, 느린 원격 자원 호출 하나로 발생한 문제가 전체 애플리케이션을 다운시킬 위험을 줄일 수 있다. 스레드 풀은 서비스의 벌크헤드 역할을 한다. 각 원격 자원을 분리하여 스레드 풀에 각각 할당한다. 한 서비스가 느리게 응답한다면 해당 서비스의 호출 그룹에 대한 스레드 풀만 포화되어 요청 처리를 중단하게 될 수 있다. 스레드 풀별로 서비스를 할당하면 다른 서비스는 포화되지 않기 때문에 이러한 병목 현상을 우회하는 데 유용하다.

## 클라이언트 회복성이 중요한 이유

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/721954c9-1137-4c42-894d-1a496e1ab617)

위 그림은 데이터베이스와 원격 서비스 같은 자원을 사용하는 것과 관련된 일반적인 시나리오를 보여준다. 회복성 패턴이 적용되지 않았기 때문에 한 개의 고장난 서비스 때문에 어떻게 전체 아키텍처가 다운되는지 보여줄 수 있다.

자기 데이터베이스에 대한 쓰기와 서비스에서 읽기가 동일한 트랜잭션 안에서 수행되는 코드를 작성했는데, 재고 서비스가 느리게 실행되기 시작하면 재고 서비스 요청에 대한 스레드 풀이 쌓이기 시작할 뿐 아니라 서비스 컨테이너의 커넥션 풀에 있는 데이터베이스 커넥션 수도 고갈되었다. 이는 재고 서비스에 대한 호출이 완료되지 않아 커넥션이 사용 중이기 때문에 발생하는 현상이다.

만약 회로 차단기를 구현했다면 해당 서비스가 제대로 수행되지 못하기 시작했을 때 해당 호출에 대한 회로 차단기가 작동해서 스레드를 소모하지 않고 빠르게 실패했을 것이다.

회로 차단기는 애플리케이션과 원격 서비스 사이에서 중개자 역할을 한다는 것을 기억하자.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1030cbd4-c7fb-4b30-b8c7-ed2b30c3ac14)

위 그림에서 라이선싱 서비스는 조직 서비스를 직접 호출하지 않는다. 그 대신 호출되면 라이선싱 서비스는 회로 차단기에 서비스에 대한 실제 호출을 위임한다. 회로 차단기는 해당 호출을 스레드로 래핑한다. 호출을 스레드로 래핑하면 클라이언트는 더 이상 호출이 완료되길 직접 기다리지 않아도 된다. 회로 차단기가 스레드를 모니터링하고 너무 오래 실행된다면 호출을 종료할 수 있다.

- 정상 시나리오: 회로 차단기는 타이머를 설정하고, 타이머가 만료되기 전에 원격 호출이 완료되면 라이선싱 서비스는 정상적으로 모든 작업을 계속 수행할 수 있다.
- 부분적 서비스 저하 시나리오: 라이선싱 서비스는 회로 차단기를 통해 조직 서비스를 호출한다. 하지만 조직 서비스가 느리게 실행되어 회로 차단기가 관리하는 스레드 타이머가 만료되기 전에 호출이 완료되지 않으면, 회로 차단기는 원격 서비스에 대한 연결을 종료하고 라이선싱 서비스는 호출 오류를 반환한다. 라이선싱 서비스는 조직 서비스 호출이 완료되길 기다리기 위해 자원(자체 스레드 및 커넥션 풀)을 점유하지 않는다.
  - 조직 서비스에 대한 호출 시간이 만료되면 회로 차단기는 발생한 실패 횟수를 추적하기 시작하는데, 특정 시간 동안 서비스에서 오류가 필요 이상으로 발생하면 회로 차단기는 회로를 '차단'하고 조직 서비스에 대한 모든 호출은 조직 서비스 호출 없이 실패한다.
- 빠른 실패 시나리오: 라이선싱 서비스는 회로 차단기의 타임아웃을 기다릴 필요 없이 문제가 있다는 것을 즉시 알 수 있다. 그런 다음 완전히 실패하거나 대체 코드(폴백)을 사용하여 조치하는 것 중에서 선택할 수 있다. 회로 차단기가 차단되면 라이선싱 서비스가 조직 서비스를 호출하지 않았기 때문에 조직 서비스는 회복할 수 있는 기회가 생기며 연쇄 장애를 방지하는 데 도움이 된다.

회로 차단기는 때때로 저하된 서비스에 호출을 허용하는데, 이 호출이 연속적으로 필요한 만큼 성공하면 회로 차단기를 스스로 재설정한다. 원격 호출에 대해 회로 차단기 패턴이 제공하는 주요 이점은 다음과 같다.

- 빠른 실패: 원격 서비스가 성능 저하를 겪으면 애플리케이션은 빠르게 실패하고 전체 애플리케이션을 완전히 다운시킬 수 있는 자원 고갈 이슈를 방지한다. 대부분의 장애 상황에서 완전히 다운되는 것보다 일부가 다운되는 것이 더 낫다.
- 원만한 실패: 타임아웃과 빠른 실패를 사용하는 회로 차단기 패턴은 원만하게 실패하거나 사용자 의도를 충족하는 대체 메커니즘을 제공할 수 있게 해 준다. 예를 들어 사용자는 한 가지 데이터 소스에서 데이터를 검색하려고 하고, 해당 데이터 소스가 서비스 저하를 겪고 있다면 다른 위치에서 해당 데이터를 검색할 수 있다.
- 원만한 회복: 회로 차단기 패턴이 중개자 역할을 하므로 회로 차단기는 요청 중인 자원이 다시 온라인 상태가 되었는지 확인하고, 사람의 개입 없이 자원에 대한 재접근을 허용하도록 주기적으로 확인한다.

## Resilience4j

Resilience4j는 히스트릭스에서 영감을 받은 내결함성 라이브러리다. 네트워크 문제나 여러 서비스의 고장으로 발생하는 결함 내성을 높이기 위해 다음 패턴을 제공한다.

- 회로 차단기(circuit breaker): 요청받은 서비스가 실패할 때 요청을 중단한다.
- 재시도(retry): 서비스가 일시적으로 실패할 때 재시도한다.
- 벌크헤드(bulkhead): 과부하를 피하고자 동시 호출하는 서비스 요청 수를 제한한다.
- 속도 제한(rate limit): 서비스가 한 번에 수신하는 호출 수를 제한한다.
- 폴백(fallback): 실패하는 요청에 대해 대체 경로를 설정한다.

Resilience4j를 사용하면 메서드에 여러 애너테이션을 정의하여 동일한 메서드 호출에 여러 패턴을 적용할 수 있다.

회로 차단기, 재시도, 속도 제한, 폴백, 벌크헤드 패턴을 구현하려면 스레드와 스레드 관리에 대한 해박한 지식이 필요하다. 그리고 이러한 패턴을 높은 품질로 구현하려면 엄청난 양의 작업이 필요하다. 하지만 Spring Boot와 Resilience4j 라이브러리를 사용하면 여러 마이크로서비스 아키텍처에서 항상 사용되는 검증된 도구를 제공할 수 있다.

## 스프링 클라우드와 Resilience4j를 사용하는 라이선싱 서비스 설정
Kotlin, Spring Boot 3 기반에서 gradle 설정을 기준으로 예시를 직접 작성해본다.

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "3.2.3"
  id("io.spring.dependency-management") version "1.1.4"
  kotlin("jvm") version "1.9.22"
  kotlin("plugin.spring") version "1.9.22"
  kotlin("plugin.jpa") version "1.9.22"
}

group = "me.ramos"
version = "0.0.1-SNAPSHOT"

java {
  sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
  mavenCentral()
}

// 여기
extra["springCloudVersion"] = "2023.0.0"

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  // 여기
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

  runtimeOnly("com.mysql:mysql-connector-j")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// 여기
dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = "17"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
```

## 회로 차단기 구현
코드에서 회로 차단기가 추구하는 것은 원격 호출을 모니터링하고 서비스를 장기간 기다리지 않게 하는 것이다. 이때 회로 차단기는 연결을 종료하고 더 많이 실패하며 오작동이 많은 호출이 있는지 모니터링하는 역할을 한다. 그런 다음 이 패턴은 **빠른 실패**를 구현하고 실패한 원격 서비스에 추가로 요청하는 것을 방지한다.

Resilience4j의 회로 차단기에는 세 개의 일반 상태를 가진 유한 상태 기계가 구현되어 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3f468632-f3b8-4088-8093-49bc77296313)

처음에 Resilience4j 회로 차단기는 닫힌 상태에서 시작한 후 클라이언트 요청을 기다린다. 이 상태는 링 비트 버퍼를 사용하여 요청의 성과 및 실패 상태를 저장한다. 요청이 성공하면 회로 차단기는 링 비트 버퍼에 0비트를 저장하지만, 호출된 서비스에서 응답받지 못하면 1비트를 저장한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d5c012f2-e452-4899-aa52-acaa5d28aace)

실패율을 계산하려면 링을 모두 채워야 한다. 이전 시나리오에서 실패율을 계산하려면 적어도 12 호출은 평가해야 한다. 11개의 요청만 평가했을 때 11개의 호출이 모두 실패하더라도 회로 차단기는 열린 상태로 변경되지 않는다. 회로 차단기는 고장률이 임계 값을 초과할 때만 열린다.

회로 차단기가 열린 상태라면 설정된 시간 동안 호출은 모두 거부되고 회로 차단기는 `CallNotPermittedException` 예외를 발생시킨다. 설정된 시간이 만료되면 회로 차단기는 반열린 상태로 변경되고 서비스가 여전히 사용 불가한지 일부 요청을 허용한다.

반열린 상태에서 회로 차단기는 설정 가능한 다른 링 비트 버퍼를 사용하여 실패율을 평가한다. 이 실패율이 설정된 임계치보다 높으면 회로 차단기는 다신 열린 상태로 변경된다. 임계치보다 작거나 같다면 닫힌 상태로 돌아간다. 열린 상태에서는 회로 차단기가 모든 요청을 거부하고 닫힌 상태에서는 수락한다는 점을 기억하자.

또한 Resilience4j 회로 차단기 패턴에서 다음과 같은 추가 상태를 정의할 수 있다. 다음 상태를 벗어나는 유일한 방법은 회로 차단기를 재설정하거나 상태 전환을 트리거하는 것이다.

- 비활성 상태: 항상 액세스 허용
- 강제 열린 상태: 항상 액세스 거부

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/4a7e0105-7eaf-47f3-a0be-f03bf8594855)

- 데이터베이스에 대한 모든 호출을 래핑
- 두 서비스간 호출을 래핑

Resilience4j와 Spring Cloud는 `@CircuitBreaker`를 사용하여 Resilience4j 회로 차단기가 관리하는 클래스 메서드를 표시한다.

스프링 프레임워크가 이 애너테이션을 만나면 동적으로 프록시를 생성해서 해당 메서드를 래핑하고, 원격 호출을 처리할 때만 별도로 설정된 스레드 풀을 이용해 해당 메서드에 대한 모든 호출을 관리한다.

```kotlin
package me.ramos.resilience.service

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import me.ramos.resilience.model.License
import me.ramos.resilience.repository.LicenseRepository
import org.springframework.stereotype.Service

@Service
class LicenseService(
    private val licenseRepository: LicenseRepository
) {

    // ...
    
    @CircuitBreaker(name = "licenseService")
    fun getLicensesByOrganization(organizationId: String): List<License> {
        return licenseRepository.findByOrganizationId(organizationId);
    }
}
```

이 한 개의 애너테이션에는 다양한 기능이 들어 있다. `@CircuitBreaker` 애너테이션을 사용하면 `getLicensesByOrganization()`가 호출될 때마다 해당 호출은 Resilience4j 회로 차단기로 래핑된다. 회로 차단기는 실패한 모든 `getLicensesByOrganization()`에 대한 메서드 호출 시도를 가로챈다.

아래 코드처럼 느리거나 타임아웃된 데이터베이스 쿼리가 수행되는 `getLicensesByOrganization()` 메서드를 시뮬레이션 해보자.

```kotlin
package me.ramos.resilience.service

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import me.ramos.resilience.model.License
import me.ramos.resilience.repository.LicenseRepository
import org.springframework.stereotype.Service
import java.util.Random
import java.util.concurrent.TimeoutException

@Service
class LicenseService(
    private val licenseRepository: LicenseRepository
) {

    // ...

    @CircuitBreaker(name = "licenseService")
    fun getLicensesByOrganization(organizationId: String): List<License> {
        randomlyRunLong();
        return licenseRepository.findByOrganizationId(organizationId);
    }
    
    private fun randomlyRunLong() {
        val rand = Random();
        val randomNum = rand.nextInt(3) + 1;
        if (randomNum == 3) sleep();
    }
    
    private fun sleep() {
        try {
            Thread.sleep(5000);
            throw TimeoutException();
        } catch (e: InterruptedException) {
            println(e.message);
        }
    }
}
```

Postman에서 해당 엔드포인트를 여러 번 호출하면 라이선싱 서비스에서 다음 에러 메시지를 반환한다.

```json
{
  "timestamp": 1595178498383,
  "status": 500,
  "error": "Internal Server Error",
  "message": "No message available",
  "path": "/v1/organization/e6a625cc-718b-48c2-ac76-1dfdff9a531e/
           license/"
}
```

실패 중인 서비스를 계속 호출하면 결국 링 비트 버퍼가 다 차서 아래와 같은 에러가 표시된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d1e4cfea-5337-4f88-b59c-4107d714979b)

### 조직 서비스에 회로 차단기 추가
메서드 레벨의 애너테이션으로 회로 차단기 기능을 호출에 삽입할 경우 장점은 데이터베이스를 액세스하든지 마이크로서비스를 호출하든지 간에 동일한 애너테이션을 사용할 수 있다는 것이다.

라이선싱 서비스에서 라이선스와 연관된 조직 이름을 검색해야 할 때를 예로 들어 보자. 회로 차단기로 조직 서비스에 대한 호출을 래핑하고 싶다면, 간단하게 다음과 같이 `RestTemplate` 호출 부분을 메서드로 분리하고 `@CircuitBreaker`를 추가한다.

```kotlin
@CircuitBreaker(name = "organizationService")
private fun getOrganization(organizationId: String): Organization {
    return organizationRestClient.getOrganization(organizationId);
}
```

회로 차단기의 기본값을 알아보려면 Postman에서 `http://localhost:<service_port>/actuator/health`를 선택한다. 기본적으로 스프링 부트 액추에이터의 상태 정보 서비스로 회로 차단기의 구성 정보를 노출한다.

### 회로 차단기 사용자 정의


```yaml
spring:
    application:
     name: licensing-service 
    profiles:
      active: dev
    cloud:
        config: 
            uri: http://configserver:8071

logging:
  level:
    com.netflix: WARN
    org.springframework.web: WARN
    com.optimagrowth: DEBUG

#auditevents  , Exposes audit events information for the current application.
#beans  , Displays a complete list of all the Spring beans in your application.
#caches , Exposes available caches.
#conditions , Shows the conditions that were evaluated on configuration and auto-configuration classes and the reasons why they did or did not match.
#configprops  , Displays a collated list of all\u00A0@ConfigurationProperties.
#env  , Exposes properties from Spring\u2019s\u00A0ConfigurableEnvironment.
#flyway , Shows any Flyway database migrations that have been applied.
#health , Shows application health information.
#httptrace  , Displays HTTP trace information (by default, the last 100 HTTP request-response exchanges).
#info , Displays arbitrary application info.
#integrationgraph , Shows the Spring Integration graph.
#loggers  , Shows and modifies the configuration of loggers in the application.
#liquibase  , Shows any Liquibase database migrations that have been applied.
#metrics  , Shows \u2018metrics\u2019 information for the current application.
#mappings , Displays a collated list of all\u00A0@RequestMapping\u00A0paths.
#scheduledtasks , Displays the scheduled tasks in your application.
#sessions , Allows retrieval and deletion of user sessions from a Spring Session-backed session store. Not available when using Spring Session\u2019s support for reactive web applications.
#shutdown , Lets the application be gracefully shutdown.
#threaddump , Performs a thread dump.

#management.endpoints.web.base-path=/
management.endpoints.enabled-by-default: false
management.endpoint.health:
 enabled: true
 show-details: always


resilience4j.circuitbreaker:
  instances:
    licenseService: // 라이선싱 서비스의 인스턴스 구성
      registerHealthIndicator: true // 상태 정보 엔드포인트에 대한 구성 정보 노출 여부 설정
      ringBufferSizeInClosedState: 5 // 링 버퍼의 닫힌 상태 크기를 설정
      ringBufferSizeInHalfOpenState: 3 // 링 버퍼의 반열린 상태 크기를 설정
      waitDurationInOpenState: 10s // 열린 상태의 대기 시간 설정
      failureRateThreshold: 50 // 실패율 임계치를 백분율로 설정
      recordExceptions: // 실패로 기록될 예외를 설정
        - org.springframework.web.client.HttpServerErrorException
        - java.io.IOException
        - java.util.concurrent.TimeoutException
        - org.springframework.web.client.ResourceAccessException
    organizationService: // 조직 서비스의 인스턴스 구성
      registerHealthIndicator: true
      ringBufferSizeInClosedState: 6
      ringBufferSizeInHalfOpenState: 4
      waitDurationInOpenState: 20s
      failureRateThreshold: 60

resilience4j.ratelimiter:
  instances:
    licenseService:
      limitForPeriod: 5
      limitRefreshPeriod: 5000
      timeoutDuration: 1000ms

resilience4j.retry:
  instances:
    retryLicenseService:
      maxRetryAttempts: 5
      waitDuration: 10000
      retry-exceptions:
        - java.util.concurrent.TimeoutException

resilience4j.bulkhead:
  instances:
    bulkheadLicenseService:
      maxWaitDuration: 2ms
      maxConcurrentCalls: 20


resilience4j.thread-pool-bulkhead:
  instances:
    bulkheadLicenseService:
      maxThreadPoolSize: 1
      coreThreadPoolSize: 1
      queueCapacity: 1
```

## 폴백 처리
회로 차단기 패턴의 장점 중 하나는 이 패턴이 '중개자'로, 원격 자원과 그 소비자 사이에 위치하기 때문에 서비스 실패를 가로채서 다른 대안을 취할 수 있다는 것이다.

Resilience4j에서 이 대안을 폴백 전략이라고 하며 쉽게 구현할 수 있다.

```kotlin
@Throws(TimeoutException::class)
@CircuitBreaker(name = "licenseService", fallbackMethod = "buildFallbackLicenseList")
fun getLicensesByOrganization(organizationId: String): List<License> {
    logger.debug("getLicensesByOrganization Correlation id: {}", UserContextHolder.getContext().getCorrelationId());
    randomlyRunLong();
    return licenseRepository.findByOrganizationId(organizationId);
}

private fun buildFallbackLicenseList(organizationId: String, t: Throwable) {
    val fallbackList: MutableList<License> = mutableListOf();
    var license: License = License();
    license.licenseId = "0000000-00-00000";
    license.organizationId = organizationId;
    license.productName = "Sorry no licensing information currently available";
    fallbackList.add(license);
    return fallbackList;
}
```

Resilience4j로 폴백 전략을 구현하려면 두 가지 작업이 필요하다.

- `@CircuitBreaker` 또는 다른 애너테이션에 `fallbackMethod` 속성을 추가한다.
  - 이 속성은 실패해서 Resilience4j가 호출을 중단할 때 대신 호출할 메서드 이름을 포함해야 한다.
- 폴백 메서드를 정의한다.
  - `@CircuitBreaker`가 보호하는 원래 메서드와 동일한 클래스에 위치해야 한다.
  - 폴백 메서드를 생성하려면 원래 메서드처럼 하나의 매개변수를 받도록 동일한 서식을 가져야 한다. 동일한 서식을 사용해야 원래 메서드의 모든 매개변수를 폴백 메서드에 전달할 수 있다.

Postman에서 호출하고 3분의 1 확률로 타임아웃이 발생할 때 서비스 호출에서 예외가 발생하지 않아야 하고, 대신 아래처럼 더미 라이선스 값이 반환되어야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/640f7ef3-d56d-40b2-9067-037930b9b18f)

---
📌 폴백 전략

폴백 전략의 구현 여부를 결정할 때 몇 가지 염두에 두어야 할 사항은 다음과 같다.

- 폴백은 자원이 타임아웃되거나 실패했을 때 동작 가이드를 제공한다. 타임아웃 예외를 포착하는 데 폴백을 사용하고 에러를 기록하는 것에 아무것도 하지 않는다면 서비스 호출 주위에 표준 `try ... catch` 블록을 사용해야 한다. 즉, 예외를 잡고 로깅 로직을 `try ... catch` 블록에 추가한다.
- 폴백 함수에서 수행할 동작에 유의하기 바란다. 폴백 서비스에서 다른 분산 서비스를 호출하는 경우 `@CircuitBreaker`로 그 폴백을 또 래핑해야 할 수 있다. 1차 폴백이 발생한 것과 동일한 실패가 2차 폴백에서도 발생할 수 있음을 고려하라. 방어적으로 코딩해야 한다.

---

## 벌크헤드 패턴 구현
