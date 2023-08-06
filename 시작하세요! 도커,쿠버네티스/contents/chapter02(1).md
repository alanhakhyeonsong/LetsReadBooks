# 2장. 도커 엔진
## 도커 이미지와 컨테이너
도커 엔진에서 사용하는 기본 단위는 이미지와 컨테이너이며, 이 두 가지가 핵심이다.

이미지는 컨테이너를 생성할 때 필요한 요소이며, 가상 머신을 생성할 때 사용하는 iso 파이로가 비슷한 개념이다. **이미지는 여러 개의 계층으로 된 바이너리 파일로 존재하고, 컨테이너를 생성하고 실행할 때 읽기 전용으로 사용된다.** 이미지는 도커 명령어로 내려받을 수 있으므로 별도로 설치할 필요는 없다.

도커에서 사용하는 이미지의 이름은 기본적으로 `[저장소 이름]/[이미지 이름]:[태그]`의 형태로 구성된다.

- 저장소 이름: 이미지가 저장된 장소를 의미한다. 저장소 이름이 명시되지 않은 이미지는 도커에서 기본적으로 제공하는 이미지 저장소인 Docker Hub의 공식 이미지를 뜻한다. 이미지를 생성할 때 저장소 이름을 명시할 필요는 없으므로 생략하는 경우도 있다.
- 이미지 이름: 해당 이미지가 어떤 역할을 하는지 나타낸다. 이미지의 이름은 생략할 수 없으며 반드시 설정해야 한다.
- 태그: 이미지의 버전 관리, 혹은 리비전(Revision) 관리에 사용한다. 일반적으로 14.04 처럼 버전을 명시하지만 태그를 생략하면 도커 엔진은 이미지의 태그를 `latest`로 인식한다.

이미지로 컨테이너를 생성하면 해당 이미지의 목적에 맞는 파일이 들어 있는 파일시스템과 격리된 시스템 자원 및 네트워크를 사용할 수 있는 독립된 공간이 생성되고, 이것이 바로 **도커 컨테이너**가 된다. 대부분의 도커 컨테이너는 생성될 때 사용된 도커 이미지의 종류에 따라 알맞은 설정과 파일을 가지고 있기 때문에 도커 이미지의 목적에 맞도록 사용되는 것이 일반적이다.

**컨테이너는 이미지를 읽기 전용으로 사용하되 이미지에서 변경된 사항만 컨테이너 계층에 저장하므로 컨테이너에서 무엇을 하든지 원래 이미지는 영향을 받지 않는다.** 또한 생성된 각 컨테이너는 각기 독립된 파일시스템을 제공받으며 호스트와 분리돼 있으므로 특정 컨테이너에서 어떤 애플리케이션을 설치하거나 삭제해도 다른 컨테이너와 호스트는 변화가 없다.

## 도커 컨테이너 다루기
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/64075d54-f7b2-41f8-bbcb-efeaef274966)

`docker run` 명령어는 컨테이너를 생성하고 실행하는 역할을 한다. `-i`, `-t` 옵션은 컨테이너와 상호 입출력을 가능하게 한다. 위 예시처럼 `docker run` 명령어를 수행하면 해당 이미지가 로컬 도커 엔진에 존재하지 않으므로 Docker Hub에서 자동으로 이미지를 내려받게 된다.

`-i` 옵션으로 상호 입출력을, `-t` 옵션으로 tty를 활성화해서 bash shell을 사용하도록 컨테이너를 설정했다. 그 결과 컨테이너의 내부로 들어가게 된 것이다. `docker run` 명령어에서 이 두 옵션 중 하나라도 사용하지 않으면 셸을 정상적으로 사용할 수 없다.

컨테이너를 정지하지 않고 빠져나가려면 `Ctrl + P, Q`를 입력하면 된다. `exit`이나 `Ctrl + D`를 입력해서 컨테이너 내부에서 빠져나가는 방법은 컨테이너를 정지시킴과 동시에 빠져나오는 것이라 애플리케이션 개발 목적으로 컨테이너를 사용할 땐 앞선 방법을 많이 사용하곤 한다.

`docker images` 명령어는 도커 엔진에 존재하는 이미지의 목록을 출력한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f74fe489-2eb4-41b3-a8f6-8ce14395ea04)

컨테이너를 생성할 때 `run`이 아닌 `create` 명령어를 사용할 수도 있다. `create` 명령어는 컨테이너를 생성만 할 뿐 컨테이너로 들어가지 않는다.

```bash
docker create -it --name mycentos centos:7
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/013e6ea3-25b9-4830-866b-63d0add6c699)

`docker start` 명령어로 컨테이너를 시작하고 `docker attach` 명령어로 컨테이너 내부로 들어간다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1128588a-16f2-4bb2-85a2-df3f64db66db)

도커의 컨테이너 목록을 출력하고자 한다면 `docker ps`를 사용하면 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6bc8c4a3-ae0f-487f-8064-e8d46e17dc11)

옵션을 주지 않고 사용하면, 실행중인 컨테이너 목록만 출력하고 `-a` 옵션을 추가하면 정지된 컨테이너를 포함하여 모든 컨테이너를 출력하게 된다.

`docker ps` 명령어의 출력들은 다음과 같다.
- `CONTAINER ID`: 컨테이너에게 자동으로 할당되는 고유한 ID. `docker inspect` 명령어를 사용하면 전체 ID를 확인할 수 있다.
- `IMAGE`: 컨테이너를 생성할 때 사용된 이미지의 이름.
- `COMMAND`: 컨테이너가 시작될 때 실행된 명령어. 커맨드는 대부분 이미지에 미리 내장돼 있기 때문에 별도로 설정할 필요는 없다. 이미지에 내장된 커맨드는 `docker run`이나 `create` 명령어의 맨 끝에 입력해서 컨테이너를 생성할 때 덮어쓸 수 있다.
- `CREATED`: 컨테이너가 생성되고 난 뒤 흐른 시간
- `STATUS`: 컨테이너의 상태. 실행 중이라면 `Up`, 종료는 `Exited`, 일시 중지된 상태라면 `Pause`가 출력된다.
- `PORTS`: 컨테이너가 개방한 포트와 호스트에 연결한 포틀르 나열한다. 컨테이너를 생성할 때 외부에 노출하도록 설정하지 않았다면 위와 같이 아무것도 출력되지 않는다.
- `NAMES`: 컨테이너의 고유한 이름. `--name` 옵션을 설정하지 않고 컨테이너를 생성하면 도커 엔진이 임의로 형용사와 명사를 조합해 설정한다. `docker rename`을 사용하면 컨테이너의 이름을 변경할 수 있다.

만약 더 이상 사용하지 않는 컨테이너를 삭제하고자 한다면 `docker rm` 명령어를 사용한다. 다만, 한 번 삭제한 컨테이너는 복구할 수 없다는 점에 유의하자.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e2d9e676-41aa-4936-96bc-1e1b10905fd3)

- `docker rm -f [이미지 이름]`: 실행 중인 컨테이너를 삭제한다.
- `docker container prune`: 모든 컨테이너를 삭제한다.

### 컨테이너를 외부에 노출
컨테이너는 가상 머신과 마찬가지로 가상 IP 주소를 할당받는다. 기본적으로 도커는 컨테이너에 `172.17.0.x`의 IP를 순차적으로 할당한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/573b82d7-b813-476b-aa2f-4228ed79372c)

도커의 NAT IP인 `172.17.0.2`를 할당받은 eth0 인터페이스와 로컬 호스트인 lo 인터페이스가 있다. 아무런 설정을 하지 않았다면 이 컨테이너는 외부에서 접근할 수 없으며 도커가 설치된 호스트에서만 접근할 수 있다. 외부에 컨테이너의 애플리케이션을 노출하기 위해서는 eth0의 IP와 포트를 호스트의 IP와 포트에 바인딩해야 한다.

`docker run -it -p 3306:3306 --name mywebserver ubuntu:14.04` 처럼 `-p` 옵션을 추가하면 컨테이너의 포트를 호스트의 포트와 바인딩해 연결할 수 있게 설정한다.

위 예시는 호스트의 3306 포트를 컨테이너의 3306 포트를 연결하는 예시이다. 또한 여러 개의 포트를 외부에 개방하려면 `-p` 옵션을 여러 번 써서 설정하면 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b8ae3f81-68a4-4917-b5c5-f3ed7703aedf)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/59a5e4ed-2f6f-433c-a340-b8b3e2b40319)

### 컨테이너 실행 명령어 관련 기타 옵션
- `-d`: 컨테이너를 Detached 모드인 백그라운드에서 동작하는 애플리케이션으로 실행하도록 설정한다. 
- `-e`: 컨테이너 내부의 환경변수를 설정한다.  
  예시) `docker run -it --name mysql_test -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=testdb mysql:8.0`
- `docker exec -it myubuntu /bin/bash`: 컨테이너 내부의 셸을 사용한다.
- `--link`: A 컨테이너에서 B 컨테이너로 접근하는 방법 중 가장 간단한 것은 NAT로 할당받은 내부 IP를 쓰는 것이다. 이 옵션을 사용하면 내부 IP를 알 필요 없이 항상 컨테이너에 별명으로 접근하도록 설정한다.

```bash
docker run -d -e WORDPRESS_DB_HOST=mysql \
-e WORDPRESS_DB_USER=root \
-e WORDPRESS_DB_PASSWORD=password \
--name wordpress \
--link mysql_test:mysql \
-p 80 \
wordpress
```

## 도커 볼륨
도커 이미지로 컨테이너를 생성하면 이미지는 읽기 전용이 되며 컨테이너의 변경 사항만 별도로 저장해서 각 컨테이너의 정보를 보존한다. 이미 생성된 이미지는 어떠한 경우로도 변경되지 않으며, 컨테이너 계층에 원래 이미지에서 변경된 파일시스템 등을 저장한다.  
하지만, 치명적인 단점이 있다. **컨테이너를 삭제하면 컨테이너 계층에 저장돼있던 정보도 삭제된다는 점이다.** 예를 들어, mysql 컨테이너를 삭제하면 데이터베이스의 정보도 삭제된다는 뜻이다. 도커의 컨테이너는 생성과 삭제가 매우 쉬우므로 실수로 컨테이너를 삭제하면 데이터를 복구할 수 없게 된다. 이를 방지하기 위해 컨테이너의 데이터를 영속적 데이터로 활용할 수 있도록 **볼륨**을 활용한다.

- 호스트와 볼륨을 공유
- 볼륨 컨테이너 활용
- 도커가 관리하는 볼륨 생성

### 호스트 볼륨 공유
```docker
docker run -d \
--name wordpressdb_hostvolume \
-e MYSQL_ROOT_PASSWORD=password \
-e MYSQL_DATABASE=wordpress \
-v /home/wordpress_db:/var/lib/mysql \
mysql:5.7

docker run -d \
-e WORDPRESS_DB_PASSWORD=password \
--name wordpress_hostvolume \
--link wordpressdb_hostvolume:mysql \
-p 80 \
wordpress
```

`-v` 옵션은 `[호스트의 공유 디렉터리]:[컨테이너의 공유 디렉터리]` 형태로 작성하며, 호스트의 공유 디렉터리를 호스트에 생성하지 않았어도 도커는 자동으로 이를 생성한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c7419dd3-ada3-4412-b538-ef4f125e0363)

컨테이너의 `/var/lib/mysql` 디렉터리는 호스트의 `/home/wordpress_db` 디렉터리와 동기화되는 것이 아니라 완전히 같은 디렉터리다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0e05f812-d0c3-49f8-976d-c507f7fe7b43)

`-v` 옵션을 통한 호스트 볼륨 공유는 호스트의 디렉터리를 컨테이너 디렉터리에 마운트하는 것이다.

### 볼륨 컨테이너
볼륨을 사용하는 두 번째 방법은 `-v` 옵션으로 볼륨을 사용하는 컨테이너를 다른 컨테이너와 공유하는 것이다.  
컨테이너를 생성할 때, `--volumes-from` 옵션을 설정하면 `-v` 또는 `--volume` 옵션을 적용한 컨테이너의 볼륨 디렉터리를 공유할 수 있다. 그러나 이는 직접 볼륨을 공유하는 것이 아닌 `-v` 옵션을 적용한 컨테이너를 통해 공유하는 것이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/79c26a4a-5a36-4a78-8c36-44215069a715)

위와 같은 구조를 활용하면 호스트에서 볼륨만 공유하고 별도의 역할을 담당하지 않는 '볼륨 컨테이너'로서 활용하는 것도 가능하다. 즉, 볼륨을 사용하려는 컨테이너에 `-v` 옵션 대신 `--volumes-from` 옵션을 사용함으로써 볼륨 컨테이너에 연결해 데이터를 간접적으로 공유받는 방식이다.

### 도커 볼륨
도커 자체에서 제공하는 볼륨 기능을 활용해 데이터를 보존하는 방법이다.

도커 볼륨을 다루는 명령어는 `docker volume`으로 시작하며, `docker volume create` 명령어로 볼륨을 생성한다.

볼륨을 생성할 때 플러그인 드라이버를 설정해 여러 종류의 스토리지 백엔드를 사용할 수 있으며 기본적으로 제공되는 드라이버는 local이라는 점만 알고 넘어가자. local 볼륨은 로컬 호스트에 저장되며 도커 엔진에 의해 생성되고 삭제된다.

호스트와 볼륨을 공유할 때 다음과 같은 형식으로 입력해야 한다.

`[볼륨의 이름]:[컨테이너의 공유 디렉터리]`

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/35ea2970-2b99-41a8-bac5-705fe203598a)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c2cea7ca-c348-477f-b981-9888f97527af)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0724e509-f489-444e-9fe6-6377c22c7df7)

결과를 보면 같은 파일인 volume이 존재한다. `docker volume` 명령어로 생성한 볼륨은 아래 그림과 같은 구조로 활용된다. 도커 볼륨도 여러 개의 컨테이너에 공유되어 활용될 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/276b0567-c33a-4c54-82a1-088d0bc156a7)

볼륨은 디렉터리 하나에 상응하는 단위로 도커 엔진에서 관리한다. 도커 볼륨도 호스트 볼륨 공유와 마찬가지로 호스트에 저장함으로써 데이터를 보존하지만 파일이 실제로 어디에 저장되는지 사용자는 알 필요가 없다.

`docker inspect` 명령어를 사용하면 `myvolume` 볼륨이 실제로 어디에 저장되는지 알 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/5e92c1fc-a249-4d1e-b896-1a9a5dba002a)

`docker inspect` 명령어는 컨테이너, 이미지, 볼륨 등 도커의 모든 구성 단위의 정보를 확인할 때 사용되며, 정보를 확인할 종류를 명시하기 위해 `--type` 옵션에 `image`, `volume` 등을 입력하는 것이 좋다.

사용되지 않는 볼륨을 한꺼번에 삭제하려면 `docker volume prune` 명령어를 사용한다.

이처럼 컨테이너가 아닌 외부에 데이터를 저장하고 컨테이너는 그 데이터로 동작하도록 설계하는 것을 stateless 하다고 말한다. 컨테이너 자체는 상태가 없고 상태를 결정하는 데이터는 외부로부터 제공받는다. 컨테이너가 삭제돼도 데이터는 보존되므로 stateless한 컨테이너 설계는 도커를 사용할 때 매우 바람직한 설계다.

이와 반대로 컨테이너가 데이터를 저장하고 있어 상태가 있는 경우를 stateful 하다고 말한다. stateful한 컨테이너 설계는 컨테이너 자체에서 데이터를 보관하므로 지양하는 것이 좋다.

## 도커 네트워크
도커는 컨테이너가 생성될 때, 내부 IP를 순차적으로 할당하는데 다음과 같은 특성을 갖는다.
- 이 IP는 컨테이너를 재시작할 때마다 변경될 수 있다.
- 도커가 설치된 내부망에서만 사용할 수 있다.
- 도커 컨테이너가 **외부와의 통신을 하기 위해** 컨테이너마다 가상 네트워크 인터페이스를 생성한다. (`veth`로 시작)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/da5fcd8f-a529-4465-a7e2-caaf486068e7)

컨테이너와 호스트의 네트워크는 위 그림과 같은 구성이다.

`eth0`는 호스트의 네트워크 인터페이스라 볼 수 있고, `docker0`는 `veth` 가상 인터페이스와 호스트 인터페이스를 연결해줘서 도커 컨테이너가 외부와의 통신을 할 수 있도록 이어주는 역할을 한다. `docker0`를 브릿지라고 표현한다.

여기서 브릿지는 도커 네트워크 드라이버의 한 종류라고 볼 수 있는데, 따라서 기본적으로 생성되는 도커 네트워크 드라이버는 브릿지지만 원한다면 다른 종류의 네트워크 드라이버를 가지는 도커 네트워크를 새로 생성할 수 있다.

대표적인 네트워크 드라이버는 브릿지, 호스트, 논, 컨테이너, 오버레이가 있다. 도커의 네트워크를 다루는 명령어는 `docker network`로 시작한다.

### 브릿지 네트워크
브릿지 네트워크는 `docker0`이 아닌 사용자 정의 브리지를 새로 생성해 각 컨테이너에 연결하는 네트워크 구조다. 컨테이너는 연결된 브릿지를 통해 외부와 통신할 수 있다.

```docker
docker network create --driver bridge mybridge
```

생성한 네트워크를 컨테이너 생성 시 옵션으로 넣어 사용할 땐 다음과 같이 명령어를 작성하면 된다.

```docker
docker run -it --name mynetwork_container --net mybridge ${imagename}
```

이렇게 생성된 사용자 정의 네트워크는 `docker network disconnect`, `connect`를 통해 컨테이너에 유동적으로 붙이고 뗄 수 있다.

네트워크의 서브넷, 게이트웨이, IP 할당 범위 등을 임의로 설정하려면 네트워크를 생성할 때 아래와 같이 `--subnet`, `--ip-range`, `--gateway` 옵션을 추가한다. 단, `--subnet`과 `--ip-range`는 같은 대역이어야 한다.

```docker
docker network create --driver=bridge \
--subnet=172.72.0.0/16 \
--ip-range=172.72.0.0/24 \
--gateway=172.72.0.1 \
my_custom_network
```

### 호스트 네트워크
네트워크를 호스트로 설정하면 호스트의 네트워크 환경을 그대로 쓸 수 있다. 브릿지 드라이버 네트워크와 달리 호스트 드라이버의 네트워크는 별도로 생성할 필요 없이 기존의 host라는 이름의 네트워크를 사용한다.

```bash
root@docker-host:/# docker run -it --name network_host --net host ubuntu:20.04

root@docker-host:/# echo "컨테이너 내부입니다"
```

`--net` 옵션을 입력해 호스트를 설정한 컨테이너의 내부에서 네트워크 환경을 확인하려면 호스트와 같은 것을 알 수 있다. 호스트 머신에서 설정한 호스트 이름도 컨테이너가 물려받기 때문에 컨테이너의 호스트 이름도 무작위 16진수가 아닌 도커 엔진이 설치된 호스트 머신의 호스트 이름으로 설정된다.

컨테이너의 네트워크를 호스트 모드로 설정하면 컨테이너 내부의 애플리케이션을 별도의 포트 포워딩 없이 바로 서비스할 수 있다.

### 논 네트워크
아무런 네트워크를 쓰지 않는 것을 뜻한다. 이 옵션을 주면 외부와 연결이 단절된다.

`--net` 옵션으로 `none`을 설정한 컨테이너 내부에서 네트워크 인터페이스를 확인하면 로컬호스트를 나타내는 `lo` 외엔 존재하지 않는 것을 확인할 수 있다.

```bash
# ifconfig
lo        Link encap:Local Loopback
          inet addr:127.0.0.1  Mask:255.0.0.0
...
```

### 컨테이너 네트워크
`--net` 옵션으로 `container`를 입력하면 다른 컨테이너의 네트워크 네임스페이스 환경을 공유할 수 있다. 공유되는 속성은 내부 IP, 네트워크 인터페이스의 MAC 주소 등이다. `--net` 옵션의 값으로 `container:[다른 컨테이너의 ID]`와 같이 입력한다.

다른 컨테이너의 네트워크 환경을 공유하면 내부 IP를 새로 할당받지 않으며 호스트에 `veth`로 시작하는 가상 네트워크 인터페이스도 생성되지 않는다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/427b5eae-2d60-45af-adf3-210f0808a471)

## 컨테이너 로깅
도커는 컨테이너의 표준 출력(`StdOut`)과 에러(`StdErr`) 로그를 별도의 메타데이터 파일로 저장하며 이를 확인하는 명령어를 제공한다. 기본적으로 `docker logs`를 통해 접근하는 로그들은 JSON 형태로 도커 내부에 저장된다. 이에 접근하기 위한 명령어는 아래와 같다.

```bash
cat /var/lib/docker/containers/${CONTAINER_ID}/${CONTAINER_ID}-json.log
```

컨테이너 내부의 출력이 너무 많은 상태로 방치하면 json 파일의 크기가 계속해서 커질 수 있고, 결국 호스트의 남은 저장 공간을 전부 사용할 수도 있다. 이러한 상황을 방지하기 위해 `--log-opt` 옵션으로 컨테이너 json 로그 파일의 최대 크기를 지정할 수 있다.
- `max-size`: 로그 파일의 최대 크기
- `max-file`: 로그 파일의 개수

```docker
docker run -it --log-opt max-size=10k -log-opt max-file=3 \
--name log-test ubuntu:20.04
```

### syslog 로그
syslog는 유닉스 계열 OS에서 로그를 수집하는 오래된 표준 중 하나로서, 커널, 보안 등 시스템과 관련된 로그, 애플리케이션의 로그 등 다양한 종류의 로그를 수집해 저장한다. 대부분의 유닉스 계열 OS에선 syslog를 사용하는 인터페이스가 동일하기 때문에 체계적으로 로그를 수집하고 분석할 수 있다는 장점이 있다.

```docker
docker run -d --name syslog_container \
--log-driver=syslog \
ubuntu: 20.04 \
echo syslogtest
```

syslog 로깅 드라이버는 기본적으로 로컬호스트의 syslog에 저장된다. ubuntu 16.04 이상이면 `journalctl -u docker.service`를 통해 생성된 로그를 확인할 수 있다. syslog를 원격 서버에 설치하면 로그 옵션을 추가해 로그 정보를 원격 서버로 보낼 수 있다. 이땐 `rsyslog`를 사용하면 된다.

## 컨테이너 자원 할당 제한
컨테이너를 생성하는 `run`, `create` 명령어에서 컨테이너의 자원 할당량을 조정하도록 옵션을 입력할 수 있다. 아무런 옵션을 입력하지 않으면 컨테이너는 호스트의 자원을 제한 없이 쓸 수 있게 설정된다.

- `--memory`: 컨테이너 메모리 제한
- `--cpu-shares`: 컨테이너에 가중치를 설정해 해당 컨테이너가 CPU를 상대적으로 얼마나 사용할 수 있는지 나타낸다.
  - default: 1024
- `--cpuset-cpu`: 호스트에 CPU가 여러 개 있을 때 이 옵션을 지정해 컨테이너가 특정 CPU만 사용하도록 설정
- `--cpu-period`, `--cpu-quota`: 컨테이너의 CFS(Completely Fair Scheduler) 주기를 변경한다.
- `--cpus`: 좀 더 직관적으로 CPU의 개수를 지정한다.
- `--device-write-bps`, `--device-read-bps`, `--device-write-iops`, `--device-read-iops`: 컨테이너 내부에서 파일을 읽고 쓰는 대역폭을 제한. 블록 입출력 제한.