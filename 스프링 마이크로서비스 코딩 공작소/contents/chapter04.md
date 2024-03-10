# 4장. 도커

마이크로서비스를 성공적으로 유지하려면 이식성 문제, 즉 "마이크로서비스를 어떻게 다른 기술 환경에 실행할 수 있을까"라는 문제를 해결해야 한다. **이식성**은 소프트웨어를 다른 환경에 사용하거나 이동할 수 있는 능력이다.

컨테이너를 사용하면 소프트웨어 개발을 한 플랫폼에서 다른 플랫폼으로 빠르고 유용한 방법으로 이전하고 실행할 수 있다. 개발자 컴퓨터에서 물리 또는 가상의 엔터프라이즈 서버로 이전하는 경우가 되겠다. 우리는 전통적 웹 서버 모델을 더 작고 적응력이 뛰어난 가상화된 소프트웨어 컨테이너로 교체할 수 있고, 컨테이너는 마이크로서비스에 속도와 이식성, 확장성 같은 이점을 제공한다.

## 컨테이너 또는 가상 머신?

여전히 많은 기업에서 가상 머신(VM)은 소프트웨어 배포를 위한 사실상 표준이다.

VM은 한 컴퓨터 내에서 다른 컴퓨터 동작을 애뮬레이션 할 수 있는 소프트웨어 환경이다. 물리 머신을 완벽히 애뮬레이션하는 하이퍼바이저에 기반을 두며, 하이퍼바이저는 시스템 메모리, 프로세서 코어, 디스크 스토리지 및 네트워크, PCI 애드온 등 다른 기술 자원의 요구량을 할당하는 역할을 한다.

반면 컨테이너는 격리되고 독립된 환경에서 애플리케이션의 의존성 구성 요소와 함께 애플리케이션을 실행할 수 있는 가상 운영 체제가 포함된 패키지다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/06156519-b993-4e5b-bc2b-6a703d025308)

VM은 사전에 필요한 물리적 자원량을 설정해야 한다. 사용할 가상 프로세서 수나 램 또는 디스크 스페이스 용량을 정해야 한다. 이러한 값을 정의하는 것은 까다로운 작업일 수 있지만, 다음 사항을 주의 깊게 고려해야 한다.

- 프로세서는 서로 다른 VM 간에 공유될 수 있다.
- VM의 디스크 스페이스는 필요한 만큼만 사용할 수 있도록 설정할 수 있다. 디스크 최대 크기를 정의할 수 있지만, 시스템에서 활발히 사용되는 스페이스만 사용한다.
- 예약 메모리는 총 메모리이며 VM 간 공유되지 않는다.

컨테이너로 쿠버네티스를 사용하는 데 필요한 메모리와 CPU를 설정할 수 있지만 필수는 아니다. 이 값을 지정하지 않으면 컨테이너 엔진은 컨테이너가 정상적으로 작동하는 데 필요한 자원을 할당한다. 컨테이너는 기본 OS를 재사용할 수 있어 완전한 OS가 필요하지 않기 때문에 물리 머신이 지원해야 하는 부하는 물론 사용된 스토리지 스페이스와 애플리케이션 시작 시간도 줄어든다. 따라서 컨테이너는 VM보다 훨씬 가볍다.

결과적으로 두 기술은 모두 장단점이 있으며 궁극적인 결정은 요구 사항에 달려 있다.

마이크로서비스와 함께 컨테이너를 사용한다면 다음 이점을 얻을 수 있다.

- 컨테이너는 어디에서나 실행할 수 있어 개발 및 구현이 용이하고 이식성을 높여 준다.
- 컨테이너는 다른 애플리케이션과 완전히 격리된 예측 가능한 환경을 생성해 주는 기능을 제공한다.
- 컨테이너는 VM보다 더 빠르게 시작하고 중지할 수 있어 클라우드 네이티브가 가능하다.
- 컨테이너는 확장 가능하고 자원 활용을 최적화하는 데 능동적으로 스케줄링하고 관리할 수 있어 컨테이너 내부에서 실행되는 애플리케이션 성능과 유지 보수성을 높인다.
- 최소 서버로 가능한 많은 애플리케이션을 실행할 수 있다.

## 도커란?

> **컨테이너**는 애플리케이션이 실행하는 데 필요한 모든 것을 제공하는 논리적 패키징 메커니즘을 의미한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/4d9c48ae-0be1-4dab-ba28-101cedf16dcf)

도커 엔진은 다음 구성 요소로 되어 있다.

- Docker daemon: 도커 이미지를 생성하고 관리하는 `dockerd`라는 서버다. REST API가 데몬에 명령을 보내고 CLI 클라이언트가 도커 명령을 입력한다.
- Docker client: 도커 사용자는 클라이언트로 도커와 상호 작용한다. 도커 명령이 실행되면 클라이언트는 데몬에 명령을 보내는 역할을 수행한다.
- Docker registry: 도커 이미지가 저장되는 곳이다. 이 레지스트리는 공개 또는 사설 레지스트리일 수 있다. Docker Hub는 기본 공개 레지스트리이고 전용 사설 레지스트리도 만들 수 있다.
- Docker images: 도커 컨테이너를 생성하는 몇 가지 명령이 포함된 읽기 전용 템플릿이다. 이미지는 도커 허브에서 가져올 수 있고 그대로 사용하거나 추가 명령으로 수정할 수 있다. 또한 Dockerfile을 사용해서 새로운 이미지를 생성할 수도 있다.
- Docker containers: `docker run` 명령이 생성되고 수행되면 도커 이미지는 컨테이너를 생성한다. 애플리케이션과 주변 환경은 이 컨테이너에서 실행된다. 도커 컨테이너를 시작, 중지, 삭제하려면 도커 API나 CLI를 사용할 수 있다.
- Docker volumes: 도커 볼륨은 도커가 생성하고 컨테이너가 사용한 데이터를 저장하는 데 적합한 메커니즘이다. 도커 API나 CLI로 관리된다.
- Docker networks: 도커 네트워크를 사용하면 컨테이너를 가능한 많은 네트워크에 연결할 수 있다. 네트워크를 격리된 컨테이너의 통신 수단으로 생각할 수 있으며 도커에는 `bridge`, `host`, `overlay`, `none`, `macvlan`의 다섯 가지 네트워크 드라이버 타입이 있다.

도커 데몬이 모든 컨테이너 활동을 담당한다는 것에 주목하자. 데몬은 클라이언트에서 명령을 받고 CLI나 REST API로 명령을 전달한다. 그리고 레지스트리에서 찾은 도커 이미지가 컨테이너를 생성하는 방법도 나타낸다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/91362f57-9e77-4d1d-8e1a-c91584934f28)

## Dockerfiles

**Dockerfile**은 도커 클라이언트가 이미지를 생성하고 준비하기 위해 호출하는 데 필요한 지시어와 명령어들이 포함된 단순한 테스트 파일이다. 이 파일은 이미지 생성 과정을 자동화한다. 도커 파일에서 사용된 명령은 리눅스 명령과 유사하다.

```dockerfile
FROM openjdk:11-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/886ae106-c577-4fff-8d18-da50e3959be4)

Dockerfile 명령어는 다음과 같다.

| 명령어       | 설명                                                                                                                     |
| ------------ | ------------------------------------------------------------------------------------------------------------------------ |
| `FROM`       | 빌드 프로세스를 시작하는 기본 이미지를 정의한다. 즉, `FROM` 명령어는 도커 런타임에 사용할 도커 이미지를 지정한다.        |
| `LABEL`      | 도커 이미지에 메타데이터를 추가한다. 메타데이터는 키-값 쌍으로 되어 있다.                                                |
| `ARG`        | 사용자가 `docker build` 명령을 사용하여 빌더에 전달할 수 있는 변수를 정의한다.                                           |
| `COPY`       | 원본의 새 파일, 디렉터리 또는 리모트 파일 URL을 복사하고 지정된 대상 경로에 생성 중인 이미지의 파일 시스템에 추가한다.   |
| `VOLUME`     | 컨테이너의 마운트 지점을 만든다. 동일한 이미지를 사용해서 새 컨테이너를 만들 때 이전 볼륨과 격리되는 새 볼륨을 생성한다. |
| `RUN`        | 명령과 해당 매개변수를 받아 이미지에서 컨테이너를 실행한다. 대개 소프트웨어 패키지를 설치하는 데 이 명령어를 사용한다.   |
| `CMD`        | `ENTRYPOINT`에 매개변수를 제공한다. 이 명령어는 `docker run`과 유사하지만 컨테이너가 인스턴스화된 후에만 실행될 수 있다. |
| `ADD`        | 원천에서 파일을 복사하고 컨테이너 대상 위치에 추가한다.                                                                  |
| `ENTRYPOINT` | 실행 파일로 실행할 컨테이너를 구성한다.                                                                                  |
| `ENV`        | 환경 변수를 설정한다.                                                                                                    |

## 도커 컴포즈

Docker Compose는 서비스 설계와 구축이 용이한 스크립트를 작성하여 도커를 더 쉽게 사용하게 한다. 이를 사용하면 여러 컨테이너를 하나의 서비스로 실행하거나 다른 컨테이너를 동시에 생성할 수 있다.

- yaml 파일을 생성하여 애플리케이션 서비스를 구성한다. 이 파일명을 `docker-compose.yaml`로 지정해야 한다.
- `docker-compose config` 명령을 사용하여 파일 유효성을 확인한다.
- `docker-compose up` 명령을 사용하여 서비스를 시작한다.

```yaml
version: <docker-compose-version>
services:
  database:
     image: <database-docker-image-name>
     ports:
       - "<databasePort>:<databasePort>"
     environment:
       POSTGRES_USER: <databaseUser>
       POSTGRES_PASSWORD: <databasePassword>
       POSTGRES_DB:<databaseName>

  <service-name>:
     image: <service-docker-image-name>
     ports:
       - "<applicationPort>:<applicationPort>"
     environment:
       PROFILE: <profile-name>
       DATABASESERVER_PORT: "<databasePort>"
     container_name: <container_name>
       networks:
       backend:
       aliases:
         - "alias"

networks:
  backend:
     driver: bridge
```

도커 컴포즈 지시어는 다음과 같다.

| 지시어        | 설명                                                                                                                                                                                                                                                                    |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `version`     | 도커 컴포즈 도구의 버전을 지정한다.                                                                                                                                                                                                                                     |
| `service`     | 배포할 서비스를 지정한다. 서비스 이름은 도커 인스턴스에 대한 DNS 엔트리이며, 다른 서비스에서 액세스하는 데 사용된다.                                                                                                                                                    |
| `image`       | 특정 이미지를 사용하여 컨테이너를 실행하도록 지정한다.                                                                                                                                                                                                                  |
| `port`        | 시작한 도커 컨테이너가 외부에 노출할 포트 번호를 지정한다. 내부 및 외부 포트를 매핑한다.                                                                                                                                                                                |
| `environment` | 시작하는 도커 이미지에 환경 변수를 전달한다.                                                                                                                                                                                                                            |
| `network`     | 복잡한 토폴로지를 만들 수 있도록 커스텀 네트워크를 지정한다. 타입을 지정하지 않았다면 디폴트 타입은 `bridge`다. 브리지 네트워크를 사용하면 동일한 네트워크 내 컨테이너 연결을 관리할 수 있고, 이 네트워크는 동일한 도커 데몬 호스트에서 실행되는 컨테이너에만 적용된다. |
| `alias`       | 네트워크 내 서비스에 대한 호스트 별명을 지정한다.                                                                                                                                                                                                                       |

도커 컴포즈 명령어는 다음과 같다.

| 명령어                             | 설명                                                                                                                                                                                           |
| ---------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `docker-compose up -d`             | 애플리케이션 이미지를 빌드하고 정의된 서비스를 시작한다. 이 명령은 필요한 모든 이미지를 내려받아 배포하고 컨테이너를 시작한다. `-d` 매개변수는 백그라운드 모드에서 도커를 실행하도록 지정한다. |
| `docker-compose logs`              | 최신 배포에 대한 모든 정보를 볼 수 있다.                                                                                                                                                       |
| `docker-compose logs <service_id>` | 특정 서비스에 대한 로그를 볼 수 있다. 예를 들어 라이선싱 서비스 배포를 보려면 `docker-compose logs licensingservice` 명령을 사용한다.                                                          |
| `docker-compose ps`                | 시스템에 배포한 모든 컨테이너 목록을 출력한다.                                                                                                                                                 |
| `docker-compose stop`              | 서비스를 마치고 나서 서비스를 중지한다. 이렇게 하면 컨테이너도 중지된다.                                                                                                                       |
| `docker-compose down`              | 모든 것을 종료하고 컨테이너도 모두 제거한다.                                                                                                                                                   |

## 마이크로서비스와 도커 통합하기

### 도커 이미지 만들기

`pom.xml` 파일에 아래와 같이 추가한다.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
    </plugin>
    <!-- This plugin is used to create a Docker image and publish it to Docker hub-->
    <plugin>
      <groupId>com.spotify</groupId>
      <artifactId>dockerfile-maven-plugin</artifactId>
      <version>1.4.13</version>
      <configuration>
        <repository>${docker.image.prefix}/${project.artifactId}</repository>
        <tag>${project.version}</tag>
        <buildArgs>
          <!-- JAR 파일 위치를 설정. 이 값은 Dockerfile에서 사용된다.  -->
          <JAR_FILE>target/${project.build.finalName}.jar</JAR_FILE>
        </buildArgs>
      </configuration>
      <executions>
        <execution>
          <id>default</id>
          <phase>install</phase>
          <goals>
            <goal>build</goal>
            <goal>push</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

#### 기본 Dockerfile

```Dockerfile
# Start with a base image containing Java runtime
FROM openjdk:11-slim

# Add Maintainer Info
LABEL maintainer="Illary Huaylupo <illaryhs@gmail.com>"

# The application's jar file
ARG JAR_FILE

# Add the application's jar to the container
COPY ${JAR_FILE} app.jar

# execute the application
ENTRYPOINT ["java","-jar","/app.jar"]
```

#### 멀티스테이지 빌드 Dockerfile

이 방식을 사용하면 애플리케이션 실행에 필수적이지 않은 것을 제외할 수 있다. 스프링 부트의 경우 도커 이미지에 `target` 디렉터리를 모두 복사하는 대신 스프링 부트 애플리케이션에 실행하는 데 필요한 것만 복사하면 된다. 이 방식은 생성할 도커 이미지를 최적화한다.

```Dockerfile
# stage 1
# Start with a base image containing Java runtime
FROM openjdk:11-slim as build

# Add Maintainer Info
LABEL maintainer="Illary Huaylupo <illaryhs@gmail.com>"

# The application's jar file
ARG JAR_FILE

# Add the application's jar to the container
COPY ${JAR_FILE} app.jar

# unpackage jar file
RUN mkdir -p target/dependency &&
    (cd target/dependency; jar -xf /app.jar)

# stage 2
# Same Java runtime
FROM openjdk:11-slim

# Add volume pointing to /tmp
VOLUME /tmp

# Copy unpackaged application to new container
ARG DEPENDENCY=/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# execute the application
ENTRYPOINT ["java","-cp","app:app/lib/*","com.optimagrowth.license.
            LicenseServiceApplication"]
```

```bash
mvn clean package
mvn package dockerfile:build

docker run ostock/licensing-service:0.0.1-SNAPSHOT
# docker run -d ostock/licensing-service:0.0.1-SNAPSHOT

docker stop <container_id>
```

### 도커 컴포즈로 서비스 실행하기

도커 컴포즈는 도커 설치 과정의 일부로 설치되며 서비스를 그룹으로 정의한 후 단일 단위로 시작할 수 있는 서비스 오케스트레이션 도구다. 도커 컴포즈는 서비스별 환경 변수를 정의하는 기능도 있다.

```yaml
version: "3.7"

services:
  licensingservice: # 시작한 서비스에 레이블 적용. 이 서비스 이름은 도커 인스턴스가 시작할 때 이에 대한 DNS 엔트리가 되며, 다른 서비스가 액세스하는 데 사용된다.
    image: ostock/licensing-service:0.0.1-SNAPSHOT

    ports: # 도커 컨테이너의 포트 번호 정의. 포트 번호는 외부에 노출된다.
      - "8080:8080"

    environment:
    # 시작하는 도커 이미지에 환경 변수를 전달한다.
      - "SPRING_PROFILES_ACTIVE=dev"

    networks:
      backend: # 서비스가 속한 네트워크 이름 지정.
        aliases:
          - "licenseservice" # 네트워크상의 서비스에 대한 대체 호스트 이름 지정.

networks:
  backend: # 디폴트 타입은 bridge. backend라 명명된 커스텀 네트워크를 생성한다.
    driver: bridge
```

## 요약
- 컨테이너를 사용하면 개발자 컴퓨터부터 물리 또는 가상의 엔터프라이즈 서버까지 모든 환경에서 개발 중인 소프트웨어를 성공적으로 실행할 수 있다.
- 가상 머신(VM)을 사용하면 다른 컴퓨터에서 다른 컴퓨터의 동작을 에뮬레이션할 수 있다. 이것은 물리 머신을 완전히 모방하는 하이퍼바이저에 기반을 두며 요구되는 양의 시스템 메모리, 프로세서 코어, 디스크 스토리지나 PCI 추가 기능 등 다른 리소스를 할당한다.
- 컨테이너는 격리되고 독립적인 환경에서 의존 요소와 애플리케이션을 포함해서 실행할 수 있는 OS 가상화 방법 중 하나다.
- 컨테이너를 사용하면 실행 프로세스의 속도를 높이는 경량의 VM을 만들어 일반 비용을 줄여 각 프로젝트 비용을 절감할 수 있다.
- 도커는 도커 엔진, 클라이언트, 레지스트리, 이미지, 컨테이너, 볼륨, 네트워크라는 요소로 구성되어 있다.
- Dockerfile은 도커 클라이언트가 이미지를 생성하고 준비하고자 호출하는 지시와 명령어가 포함된 단순한 테스트 파일이다. 이 파일은 이미지 생성 과정을 자동화한다. Dockerfile에 사용된 명령은 리눅스 명령과 유사해서 이해하기 더 쉽다.
- 도커 컴포즈는 서비스를 그룹으로 정의하고 단일 단위로 함께 시작할 수 있게 해 주는 서비스 오케스트레이션 도구다.
- 도커 컴포즈는 도커 설치 과정의 일부로 설치된다.
- Dockerfile 메이븐 플러그인은 메이븐과 도커를 통합한다.