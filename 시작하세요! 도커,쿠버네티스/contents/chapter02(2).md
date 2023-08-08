# 2장. 도커 엔진
## 도커 이미지
모든 컨테이너는 이미지를 기반으로 생성되므로 이미지를 다루는 방법은 도커 관리에서 빼놓을 수 없다. 이미지의 이름을 구성하는 저장소, 이미지 이름, 태그를 잘 관리하는 것뿐만 아니라 이미지가 어떻게 생성되고 삭제되는지, 이미지의 구조는 어떻게 돼 있는지 등을 아는 것 또한 중요하다.

<img width="326" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d4663ba6-29c0-40a7-9302-3a22481588ef">

데비안 운영체제에서 `apt-get install`을 실행하면 apt 리포지터리에서 패키지를 내려받고 레드햇 운영체제에서 `yum install`을 실행하면 yum 리포지터리에서 패키지를 내려받듯 도커는 기본적으로 Docker Hub라는 중앙 저장소에서 이미지를 내려받는다.  
도커 허브는 도커가 공식적으로 제공하고 있는 이미지 저장소로서, 도커 계정을 가지고 있다면 누구든지 이미지를 올리고 내려받을 수 있기 때문에 다른 사람들에게 이미지를 쉽게 공유할 수 있다.

`docker create`, `docker run`, `docker pull`의 명령어로 이미지를 내려받을 때 도커는 도커 허브에서 해당 이미지를 검색한 뒤 내려받는다. 필요한 대부분의 이미지는 도커 허브에서 공식적으로 제공하거나 다른 사람들이 도커 허브에 이미 올려놓은 경우가 대부분이라 애플리케이션에서 이미지를 직접 만들지 않아도 손쉽게 사용할 수 있다는 장점이 있다. 단, 도커 허브는 누구나 이미지를 올릴 수 있기 때문에 공식 라벨이 없는 이미지는 사용법을 찾을 수 없거나 제대로 동작하지 않을 수 있다.

## 도커 이미지 생성
```bash
$ docker run -it --name commit_test ubuntu:20.04
root@acc525940263:/# echo test_first! >> first
```

`first`라는 파일을 만들어 우분투 이미지로부터 변경 사항을 만들었다면 컨테이너에서 호스트로 빠져나와 `docker commit` 명령어를 입력해 컨테이너를 이미지로 만든다.

```docker
docker commit [OPTIONS] CONTAINER [REPOSITORY[:TAG]]
```

```bash
docker commit -a "alicek106" -m "my first commit" \
commit_test 
commit_test:first
```

- `-a`: author를 뜻하며, 이미지의 작성자를 나타내는 메타데이터를 이미지에 포함시킨다.
- `-m`: 커밋 메시지를 뜻하며, 이미지에 포함될 부가 설명을 입력한다.

## 이미지 구조 이해
`docker inspect` 명령어를 통해 출력된 정보에서 가장 아랫부분에 있는 `Layers` 항목을 주의 깊게 살펴보자.

<img width="598" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a858e25c-3589-4a42-8ef6-5ce48434ef91">

이를 좀 더 보기 쉽게 그림으로 나타내면 다음과 같다.

<img width="274" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/cd28396f-aff7-40c4-aeb0-c306bfb50089">

docker images에서 위 3개의 이미지 크기가 각각 188MB라고 출력돼도 188MB 크기의 이미지가 3개 존재하는 것은 아니다. **이미지를 커밋할 때 컨테이너에서 변경된 사항만 새로운 레이어로 저장하고, 그 레이어를 포함해 새로운 이미지를 생성하기 때문에 전체 이미지의 실제 크기는 188MB + first 파일의 크기 + second 파일의 크기가 된다.** first 파일은 `commit_test:first` 이미지를 생성할 때 사용했던 컨테이너에서 변경된 사항이고, second 파일은 `commit_test:second` 이미지를 생성할 때 컨테이너에서 변경된 사항이다.

<img width="622" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0aa078f6-4dd6-4d4c-bcb8-0b8f52ab3aa0">

`docker rmi` 명령어를 사용하면 이미지를 삭제할 수 있다. 하지만 이미지를 사용 중인 컨테이너가 존재하면 해당 이미지는 삭제할 수 없다. `docker rm -f [컨테이너 이름]` 처럼 `-f` 옵션을 추가해 이미지를 강제로 삭제할 수도 있지만 이는 이미지 레이어 파일을 실제로 삭제하지 않고 이미지 이름만 삭제하기 때문에 의미가 없다.

```bash
$ docker stop commit_test2 && docker rm commit_test2
$ docker rmi commit_test:first
Untagged: commit_test:first
```

`commit_test:first` 이미지를 삭제했다고 해서 실제로 해당 이미지의 레이어 파일이 삭제되지는 않는다. 이를 기반으로 하는 하위 이미지인 `commit_test:second`가 존재하기 때문이다. 따라서 실제 이미지 파일을 삭제하지 않고 레이어에 부여된 이름만 삭제한다. `rmi` 명령어의 출력 결과인 `Untagged: ...`는 이미지에 부여된 이름만 삭제함을 뜻한다.

<img width="336" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6f26e6e6-9092-4c62-a6b4-593da0d27ad4">

`Deleted:`라는 출력 결과는 이미지 레이어가 실제로 삭제됐음을 뜻한다. 해당 이미지를 사용하고 있는 컨테이너가 없을 때 바로 삭제 가능하다.

## 이미지 추출
도커 이미지를 별도로 저장하거나 옮기는 등 필요에 따라 이미지를 단일 바이너리 파일로 저장해야 할 때가 있다. `docker save` 명령어를 사용하면 컨테이너의 커맨드, 이미지 이름과 태그 등 이미지의 모든 메타데이터를 포함해 하나의 파일로 추출할 수 있다. `-o` 옵션에는 추출될 파일명을 입력한다.

```
docker save -o ubuntu_14.04.tar ubuntu:14.04
```

추출된 이미지는 `load` 명령어로 도커에 다시 로드할 수 있다. `save` 명령어로 추출된 이미지는 이미지의 모든 메타데이터를 포함하기 때문에 `load` 명령어로 이미지를 로드하면 이전의 이미지와 완전히 동일한 이미지가 도커 엔진에 생성된다.

```
docker load -i ubuntu_14.04.tar
```

그러나 이미지를 단일 파일로 저장하는 것은 효율적인 방법이 아니다. 추출된 이미지는 레이어 구조의 파일이 아닌 단일 파일이기 때문에 여러 버전의 이미지를 추출하면 이미지 용량을 각기 차지하게 된다.

## 이미지 배포
이미지를 생성했다면 이를 다른 도커 엔진에 배포할 방법이 필요하다. `save`나 `export`와 같은 방법으로 이미지를 단일 파일로 추출해서 배포할 수도 있지만 이미지 파일의 크기가 너무 크거나 도커 엔진의 수가 많다면 이미지를 파일로 배포하기 어렵다. 또한 도커의 이미지 구조인 레이어 형태를 이용하지 않으므로 매우 비효율적이다.

- Docker Hub 사용
- Docker Private Registry 사용

실제 사용법은 책에 있는 예제를 따라해보자.