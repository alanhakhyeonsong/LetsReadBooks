# 2장. 도커 엔진
## Dockerfile
개발한 애플리케이션을 컨테이너화할 때 일일이 수작업으로 컨테이너를 생성하고 애플리케이션 설치, 컨테이너 커밋 과정을 거치는 것은 비효율적이다. 도커는 build 명령어를 제공하는데, 완성된 이미지를 생성하기 위해 컨테이너에 설치해야 하는 패키지, 추가해야 하는 소스코드, 실행해야 하는 명령어와 셸 스크립트 등을 하나의 파일에 기록해 두면 도커는 이 파일을 읽어 컨테이너에서 작업을 수행한 뒤 이미지로 만들어낸다.

이와 같은 작업을 기록한 파일의 이름을 Dockerfile이라 하며, 빌드 명령어는 Dockerfile을 읽어 이미지를 생성한다. **Dockerfile을 사용하면 직접 컨테이너를 생성하고 이미지로 커밋해야 하는 번거로움을 덜 수 있을뿐더러 깃과 같은 개발 도구를 통해 애플리케이션의 빌드 및 배포를 자동화할 수 있다.**

### Dockerfile 작성
Dockerfile에서 사용되는 명령어에는 여러 가지가 있지만 `FROM`, `RUN`, `ADD` 등의 기초적인 명령어를 우선적으로 다뤄보자. Dockerfile은 한 줄이 하나의 명령어가 되고, 명령어를 명시한 뒤 옵션을 추가하는 방식이다.

```Dockerfile
FROM ubuntu:14.04
MAINTAINER alicek106
LABEL "purpose"="practice"
RUN apt-get update
RUN apt-get install apache2 -y
ADD test.html /var/www/html
WORKDIR /var/www/html
RUN ["/bin/bash", "-c", "echo hello >> test2.html"]
EXPOSE 80
CMD apachectl -DFOREGROUND
```

도커 엔진은 Dockerfile을 읽어 들일 때 기본적으로 현재 디렉터리에 있는 Dockerfile이라는 이름을 가진 파일을 선택한다. 보통 Dockerfile은 빈 디렉터리에 저장하는 것이 좋은데, 이는 이미지를 빌드할 때 사용되는 context 때문이다.

자주 사용되는 명령어들은 다음과 같다.

- `FROM`: 생성할 이미지의 베이스가 될 이미지를 뜻한다. `FROM` 명령어는 반드시 한 번 이상 입력해야 하며, 이미지 이름의 포맷은 `docker run` 명령어에서 이미지 이름을 사용할 때와 같다. 사용하려는 이미지가 도커에 없다면 자동으로 `pull`한다.
- `MAINTAINER`: 이미지를 생성한 개발자의 정보를 나타낸다. 도커 1.13.0 버전 이후로 사용하지 않는다.
- `LABEL`: 이미지에 메타데이터를 추가한다. 메타데이터는 '키:값'의 형태로 저장되며, 여러 개의 메타데이터가 저장될 수 있다. 추가된 메타데이터는 `docker inspect` 명령어로 이미지의 정보를 구해서 확인할 수 있다.
- `RUN`: 이미지를 만들기 위해 컨테이너 내부에서 명령어를 실행한다. 이미지를 빌드할 때 별도의 입력을 받아야 하는 `RUN`이 있다면 `build` 명령어는 이를 오류로 간주하고 빌드를 종료한다.
- `ADD`: 파일을 이미지에 추가한다. 추가하는 파일은 Dockerfile이 위치한 디렉터리인 컨텍스트에서 가져온다. `ADD` 명령어는 JSON 배열의 형태로 `["추가할 파일 이름", ... "컨테이너에 추가될 위치"]`와 같이 사용할 수 있다. 추가할 파일명은 여러 개를 지정할 수 있으며 배열의 마지막 원소가 컨테이너에 추가될 위치다.
- `WORKDIR`: 명령어를 실행할 디렉터리를 나타낸다. bash shell에서 `cd` 명령어를 입력하는 것과 같은 기능을 한다.
- `EXPOSE`: Dockerfile의 빌드로 생성된 이미지에서 노출할 포트를 설정한다. 이를 설정한 이미지로 컨테이너를 생성했다고 해서 반드시 이 포트가 호스트의 포트와 바인딩 되는 것은 아니며, 단지 컨테이너의 해당 포트를 사용할 것임을 나타내는 것뿐이다. `EXPOSE`는 컨테이너를 생성하는 `run` 명령어에서 모든 노출된 컨테이너의 포트를 호스트에 퍼블리시 하는 `-P` 플래그와 함께 사용된다.
- `CMD`: 컨테이너가 시작될 때마다 실행할 명령어를 설정하며, Dockerfile에서 한 번만 사용할 수 있다.
- `ENV`: Dockerfile에서 사용될 환경 변수 지정
- `VOLUME`: 컨테이너를 생성했을 때 호스트와 공유할 컨테이너 내부의 디렉터리 설정
- `ARG`: build 명령어를 실행할 때 추가로 입력 받아 Dockerfile 내에서 사용될 변수의 값 설정. 빌드할 때 `--build-arg` 옵션으로 사용

### ADD vs COPY
`COPY`는 로컬 디렉터리에서 읽어 들인 컨텍스트로부터 이미지에 파일을 복사하는 역할을 한다. 사용하는 형식은 `ADD`와 같다. 차이점이라면 `COPY`는 로컬의 파일만 이미지에 추가할 수 있지만 `ADD`는 외부 URL 및 tar 파일에서도 파일을 추가할 수 있다는 점에서 다르다.

그러나 `ADD`를 사용하는 것보다 `COPY`를 사용하도록 권장된다. `ADD`는 정확히 어떤 파일이 추가될지 알 수 없기 때문이다. `COPY`는 로컬 컨텍스트로부터 파일을 직접 추가하기 때문에 빌드 시점에서도 어떤 파일이 추가될지 명확하다.

### ENTRYPOINT, CMD
`entrypoint`는 커맨드를 인자로 받아 사용할 수 있는 스크립트의 역할을 할 수 있다는 점에서 커맨드와 다르다.

```docker
docker run -it --name entrypoint_sh --entrypoint="/test.sh" ubuntu:14.04 /bin/bash
```

단, 실행할 스크립트 파일은 컨테이너 내부에 존재해야 한다. 이는 이미지 내에 스크립트 파일이 존재해야 한다는 것을 의미하는데, `COPY`를 사용하자.

1. 어떤 설정 및 실행이 필요한지에 대해 스크립트로 정리
2. `ADD` 또는 `COPY`로 스크립트를 이미지로 복사
3. `ENTRYPOINT`를 이 스크립트로 설정
4. 이미지를 빌드에 사용
5. 스크립트에서 필요한 인자는 `docker run` 명령어에서 cmd로 `entrypoint`의 스크립트에 전달

```Dockerfile
FROM ubuntu:14.04
RUN apt-get update
RUN apt-get install apache2 -y
ADD entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]
```

### Dockerfile로 빌드할 때 주의할 점
- Dockerfile이 위치한 곳에서는 이미지 빌드에 필요한 파일만 있는 것이 바람직하다.
  - `docker build -t mybuild:0.0 .`
- Dockerfile에서 명령어 한 줄이 실행될 때마다 이전 step에서 생성된 이미지에 의해 새로운 컨테이너가 생성되고, 명령어 수행 후에 다시 새로운 이미지 레이어로 저장된다.
  - 좋은 습관은 하나로 묶일 수 있는 명령어들은 (`RUN` 여러 개) `&&`, `\`로 묶어서 사용한다.
- 이미지를 빌드할 때 이전 이미지 빌드에서 사용했던 캐시를 재사용한다.
- 빌드할때만 필요한 라이브러리들을 이미지에 포함시키지 않기 위해 멀티 스테이지 빌드를 활용하는 것도 좋은 습관이다.

멀티 스테이지 빌드의 예시는 다음과 같다.

```Dockerfile
FROM golang
ADD main.go /root
WORKDIR /root
RUN go build -o /root/mainApp /root/main.go

FROM alpine:latest
WORKDIR /root
COPY --from=0 /root/mainApp .
CMD ["./mainApp"]
```

참고로 `--from=0`는 첫 번째 `FROM`에서 빌드된 이미지의 최종 상태를 의미한다.