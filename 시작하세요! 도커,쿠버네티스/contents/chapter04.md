# 4장. 도커 컴포즈
여러 개의 컨테이너로 구성된 애플리케이션을 구축하기 위해 `run` 명령어를 여러 번 사용할 수 있지만, 각 컨테이너가 제대로 동작하는지 확인하는 테스트 단계에선 이렇게 하기 번거롭다.

여러 개의 컨테이너를 하나의 서비스로 정의해 컨테이너 묶음으로 관리하는 방식이 더 효율적이다. 이를 위해 Docker Compose는 컨테이너를 이용한 서비스의 개발과 CI를 위해 여러 개의 컨테이너를 하나의 프로젝트로서 다룰 수 있는 작업 환경을 제공한다.

- 여러 개의 컨테이너의 옵션과 환경을 정의한 파일을 읽어 컨테이너를 순차적으로 생성하는 방식으로 동작
- 도커 컴포즈의 설정 파일은 `run` 명령어의 옵션을 그대로 사용할 수 있다.
- 각 컨테이너의 의존성, 네트워크, 볼륨 등을 함께 정의할 수 있다.
- 스웜 모드의 서비스와 유사하게 설정 파일에 정의된 서비스의 컨테이너 수를 유동적으로 조절 가능
- 컨테이너의 서비스 디스커버리도 자동으로 이뤄짐

## 도커 컴포즈 기본 사용법
도커 컴포즈는 컨테이너의 설정의 정의된 YAML 파일을 읽어 도커 엔진을 통해 컨테이너를 생성한다.

다음과 같은 `run` 명령어를 `docker-compose.yml` 파일로 변환해 컨테이너를 생성하고 실행해보자.

```bash
$ docker run -d --name mysql \
alicek106/composetest:mysql \
mysqld

$ docker run -d -p 80:80 \
--link mysql:db --name web \
alicek106/composetest:web \
apachectl - DFOREGROUND
```

```yaml
# docker-compose.yml
version: "3.0"
services:
  web:
    image: alicek106/composetest:web
    ports:
      - "80:80"
    links:
      - mysql:db
    command: apachectl -DFOREGROUND
  mysql:
    image: alicek106/composetest:mysql
    command: mysqld
```

어떠한 설정도 하지 않으면 도커 컴포즈는 현재 디렉터리의 `docker-compose.yml` 파일을 읽어 로컬의 도커 엔진에게 컨테이너 생성을 요청한다.

해당 파일을 저장한 디렉터리에서 `docker-compose up -d` 명령어로 컨테이너를 생성할 수 있다.

`docker-compose.yml` 파일에 대한 설명은 다음과 같다.

- `version`: YAML 파일 포맷의 버전을 나타낸다.
- `services`: 생성될 컨테이너들을 묶어놓은 단위다. 서비스 항목 아래에는 각 컨테이너에 적용될 생성 옵션을 지정한다.
- `web`, `mysql`: 생성될 서비스의 이름이다. 이 항목 아래에 컨테이너가 생성될 때 필요한 옵션을 지정할 수 있다. `docker run`에서 사용하는 옵션과 동일하게 `image`, `ports`, `links`, `command` 등을 정의할 수 있다.

도커 컴포즈는 컨테이너를 프로젝트 및 서비스 단위로 구분하므로 컨테이너의 이름은 일반적으로 다음과 같은 형식으로 정해진다.

`[프로젝트 이름]_[서비스 이름]_[서비스 내에서 컨테이너 번호]`

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/96ae32cf-d90f-4311-b061-5c5239c235e3)

`docker-compose scale` 명령어로 컨테이너 수를 늘리거나 줄일 수 있다.

생성된 프로젝트는 `docker-compose down` 명령어로 삭제할 수 있다.

## 도커 컴포즈 활용
도커 컴포즈를 사용하려면 컨테이너 설정을 저장해 놓은 YAML 파일이 필요하다. 기존에 사용하던 `run` 명령어를 YAML 파일로 변환하는 것이 도커 컴포즈 사용법의 대부분이다.

이 파일은 크게 버전 정의, 서비스 정의, 볼륨 정의, 네트워크 정의의 4가지 항목으로 구성된다. 가장 많이 사용하는 것은 서비스 정의이며, 볼륨 정의와 네트워크 정의는 서비스로 생성된 컨테이너에 선택적으로 사용한다.

### 버전 정의
YAML 파일 포맷에는 버전 1, 2, 2.1, 3이 있지만 도커 스웜 모드와 호환되는 최신 버전인 3을 기준으로 사용하도록 하자. 일반적으로 버전 항목은 YAML 파일의 맨 윗부분에 명시한다.

### 서비스 정의
서비스는 도커 컴포즈로 생성할 컨테이너 옵션을 정의한다. 이 항목에 쓰인 각 서비스는 컨테이너로 구현되며, 하나의 프로젝트로서 도커 컴포즈에 의해 관리된다.

```yaml
services:
  my_container_1:
    image: ...
  my_container_2:
    image: ...
```

- `image`: 서비스의 컨테이너를 생성할 때 쓰일 이미지의 이름을 설정한다. 이미지가 도커에 존재하지 않으면 저장소에서 자동으로 내려받는다.
- `links`: `docker run` 명령어의 `--link`와 같으며, 다른 서비스에 서비스명만으로 접근할 수 있도록 설정한다.

```yaml
services:
  web:
    links:
      - db
      - db:database
      - redis
```

- `environment`: `docker run` 명령어의 `--env`, `-e` 옵션과 동일하다. 서비스의 컨테이너 내부에서 사용할 환경변수를 지정하며, 딕셔너리나 배열 형태로 사용할 수 있다.

```yaml
services:
  web:
    environment:
      - MYSQL_ROOT_PASSWORD=mypassword
      - MYSQL_DATABASE_NAME=mydb

# 또는
services:
  web:
    environment:
      MYSQL_ROOT_PASSWORD: mypassword
      MYSQL_DATABASE_NAME: mydb
```

- `command`: 컨테이너가 실행될 때 수행할 명령어를 설정하며, `docker run` 명령어의 마지막에 붙는 커맨드와 같다. Dockerfile의 `RUN`과 같은 배열로도 사용할 수 있다.

```yaml
services:
  web:
    image: alicek106/composetest:web
    command: apachectl -DFOREGROUND

# 또는
services:
  web:
    image: alicek106/composetest:web
    command: [apachectl, -DFOREGROUND]
```

- `depends_on`: 특정 컨테이너에 대한 의존관계를 나타내며, 이 항목에 명시된 컨테이너가 먼저 생성되고 실행된다. `links`도 컨테이너의 생성 순서와 실행 순서를 정의하지만 `depends_on`은 서비스 이름으로만 접근할 수 있다.

```yaml
services:
  web:
    image: alicek106/composetest:web
    depends_on
      - mysql
  mysql:
    image: alicek106/composetest:mysql
```

특정 서비스의 컨테이너만 생성하되 의존성이 없는 컨테이너를 생성하려면 `--no-deps` 옵션을 사용한다.

`docker compose up --no-deps web`

- `ports`: `docker run` 명령어의 `-p`와 같으며 서비스의 컨테이너를 개방할 포트를 설정한다. 그러나 단일 호스트 환경에서 `80:80`과 같이 호스트의 특정 포트를 서비스의 컨테이너와 연결하면 `docker-compose scale` 명령어로 서비스의 컨테이너의 수를 늘릴 수 없다.

```yaml
services:
  web:
    image: alicek106/composetest:web
      ports:
        - "8080"
        - "8081-8085"
        - "80:80"
        ...
```

```yaml
services:
  web:
    build: ./composetest
    image: alicek106/composetest:web
```

또한 `build` 항목에선 Dockerfile에 사용될 컨텍스트나 Dockerfile의 이름, Dockerfile에서 사용될 인자 값을 설정할 수 있다. `image` 항목을 설정하지 않으면 이미지의 이름은 `[프로젝트 이름]:[서비스 이름]`이 된다.

```yaml
services:
  web:
    build: ./composetest
    context: ./composetest
    dockerfile: myDockerfile
    args:
      HOST_NAME: web
      HOST_CONFIG: self_config
```

- `extends`: 다른 YAML 파일이나 현재 YAML 파일에서 서비스 속성을 상속받게 설정한다.

```yaml
# 설정을 상속받을 docker-compose.yml 파일
version: '3.0'
  services:
    web:
      extends:
        file: extend_compose.yml
        service: extend_web
```

```yaml
# 설정을 상속해줄 extend-compose.yml 파일
version: '3.0'
  services:
    extend_web:
    image: ubuntu:14.04
    ports:
      - "80:80"
```

### 네트워크 정의
- `driver`: 도커 컴포즈는 생성된 컨테이너를 위해 기본적으로 브리지 타입의 네트워크를 생성한다. 그러나 YAML 파일에서 `driver` 항목을 정의해 서비스의 컨테이너가 브리지 네트워크가 아닌 다른 네트워크를 사용하도록 설정할 수 있다. 특정 드라이버에 필요한 옵션은 하위 항목인 `driver_ops`로 전달할 수 있다.

```yaml
version: '3.0'
services:
  myservice:
    image: nginx
    networks:
      - mynetwork
networks:
  mynetwork:
    driver: overlay
    driver_opts:
      subnet: "255.255.255.0"
      IPAdress: "10.0.0.2"
```

- `ipam`: IPAM(IP Address Manager)를 위해 사용할 수 있는 옵션으로서 `subnet`, `ip` 범위 등을 설정할 수 있다. `driver` 항목에는 IPAM을 지원하는 드라이버의 이름을 입력한다.

```yaml
services:
  ...

networks:
  ipam:
    driver: mydriver
    config:
      subnet: 172.20.0.0/16
      ip_range: 172.20.5.0/24
      gateway: 172.20.5.1
```

- `external`: YAML 파일을 통해 프로젝트를 생성할 때마다 네트워크를 생성하는 것이 아닌, 기존의 네트워크를 사용하도록 설정한다. 이를 설정하려면 사용하려는 외부 네트워크의 이름을 하위 항목으로 입력한 뒤 `external`의 값을 `true`로 설정한다. `external` 옵션은 준비된 네트워크를 사용하므로 `driver`, `driver_ops`, `ipam` 옵션과 함께 사용할 수 없다.

```yaml
services:
  web:
    image: alicek106/composetest:web
    networks:
      - alicek106_network
networks:
  alicek106_network:
    external: true
```

### 볼륨 정의
- `driver`: 볼륨을 생성할 때 사용될 드라이버를 설정한다. default는 `local`이며 사용하는 드라이버에 따라 변경해야 한다. 드라이버를 사용하기 위한 추가 옵션은 하위 항목인 `driver_opts`를 통해 인자로 설정할 수 있다.

```yaml
version: '3.0'
services:
   ...

volumes:
  driver: flocker
    driver_opts:
      opt: "1"
      opt2 : 2
```

- `external`: 도커 컴포즈는 YAML 파일에서 `volume`, `volumes-from` 옵션 등을 사용하면 프로젝트마다 볼륨을 생성한다. 이때 `external` 옵션을 설정하면 볼륨을 프로젝트를 생성할 때마다 매번 생성하지 않고 기존 볼륨을 사용하도록 설정한다.

```yaml
services:
  web:
    image: alicek106/composetest:web
    volumes:
      - myvolume: /var/www/html
volumes:
  myvolume:
    external: true
```

### YAML 파일 검증하기
YAML 파일을 작성할 때 오타 검사나 파일 포맷이 적절한지 등을 검사하려면 `docker-compose config` 명령어를 사용한다. 특정 파일의 경로를 설정하려면 `docker-compose -f {yml 파일 경로} config`와 같이 사용하면 된다.

## 도커 컴포즈 네트워크
YAML 파일에 네트워크 항목을 정의하지 않으면 도커 컴포즈는 프로젝트별로 브리지 타입의 네트워크를 생성한다. 생성된 네트워크의 이름은 `{프로젝트 이름}_default`로 설정되며, `docker-compose up` 명령어로 생성되고 `docker-compose down` 명령어로 삭제된다.

`docker-compose up` 뿐 아니라 `docker-compose scale` 명령어로 생성되는 컨테이너 전부가 이 브리지 타입의 네트워크를 사용한다. 서비스 내의 컨테이너는 `--net-alias`가 서비스의 이름을 갖도록 자동으로 설정되므로 이 네트워크에 속한 컨테이너는 서비스의 이름으로 서비스 내의 컨테이너에 접근할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/217b89fc-27b4-47d6-85b2-22e6d812313e)

web 서비스와 mysql 서비스가 각기 존재할 때 web 서비스의 컨테이너가 mysql이라는 호스트 이름으로 접근하면 mysql 서비스의 컨테이너 중 하나의 IP로 변환되며, 컨테이너가 여러 개 존재할 경우 라운드 로빈으로 연결을 분산한다.