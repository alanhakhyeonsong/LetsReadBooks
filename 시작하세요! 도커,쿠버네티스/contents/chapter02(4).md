# 2장. 도커 엔진
## 도커 데몬
컨테이너나 이미지를 다루는 명령어는 `/usr/bin/docker`에서 실행되지만, 도커 엔진의 프로세스는 `/usr/bin/dockerd` 파일로 실행된다. 이는 docker 명령어가 실제 도커 엔진이 아닌 클라이언트로서의 도커이기 때문이다.

도커의 구조는 크게 두 가지로 나뉜다.

- 클라이언트로서의 도커: API를 사용할 수 있도록 CLI를 제공
  - `/var/run/docker.sock`에 위치한 유닉스 소켓을 통해 도커 데몬의 API 호출
  - `/usr/bin/docker`에서 도커 명령어가 실행된다.
- 서버로서의 도커: 실제 컨테이너를 생성/실행하며 이미지를 관리하는 주체다. `dockerd` 프로세스
  - 도커 데몬: 도커 프로세스가 실행되어 서버로서 입력을 받을 준비가 된 상태
  - `/usr/bin/dockerd` 파일로 도커 엔진의 프로세스가 실행되고 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b6d4fc99-66e1-4755-a223-154e82f23599)

터미널 등으로 도커가 설치된 호스트에 접속해 docker 명령어를 입력하면 아래와 같은 과정으로 도커가 제어된다.

1. 사용자가 `docker version` 같은 도커 명령어를 입력한다.
2. `/usr/bin/docker`는 `/var/run/docker.sock` 유닉스 소켓을 사용해 도커 데몬에게 명령어를 전달한다.
3. 도커 데몬은 이 명령어를 파싱하고 명령어에 해당하는 작업을 수행한다.
4. 수행 결과를 도커 클라이언트에게 반환하고 사용자에게 결과를 출력한다.

도커 데몬의 옵션은 `/etc/default/docker` 파일의 `DOCKER_OPTS`를 변경하면 가능하다.

### 도커 데몬 제어: -H

`-H` 옵션은 도커 데몬의 API를 사용할 수 있는 방법을 추가 한다.

```bash
# dockerd

// -H에 IP주소와 포트 번호를 입력하면 원격 API인 Docker remote API 도커를 제어할 수 있다
# dockerd -H unix:///var/run/docker.sock

// /var/run/docker.sock을 지정하지 않고, Remote API만을 위한 바인딩 주소를 입력했다면
// 유닉스 소켓은 비활성화되므로 도커 클라이언트를 사용할 수 없게 되며
// docker로 시작하는 명령어를 할 수 없다. 
# dockerd -H tcp://0.0.0.0:2375

// IP주소와 2375번 포트로 도커 데몬 제어함과 동시에
// 도커 클라이언트도 사용할 수 있는 예
# dockerd -H unix:///var/run/docker.sock -H tcp://0.0.0.0:2375
```

### 도커 데몬에 보안 적용  : —tlserify

도커를 설치하면 기본적으로 보안 연결이 설정돼 있지 않다. 이는 도커 클라이언트, Remote API를 사용할 대 별도의 보안이 적용되지 않음을 의미한다. 

보안을 적용할 때 사용될 파일은 총 5개 : ca.pem, server-cer.pem, server-key.pem, cert.pem, **key.pem**

1. **서버 측 파일 생성**
    
    1) 인증서에 사용될 키를 생성한다
    
    ```bash
    # mkdir keys && cd keys
    # openssl genrsa -aes256 -out ca-key.pem 4096
    ```
    
    2) 공용 키(public key)를 생성한다. 입력하는 모든 항목은 공백으로 둬도 상관없다.
    
    ```bash
    # openssl req -new -x509 -days 10000 -key ca-key.pem -sha256 -out ca.pem
    ```
    
    3) 서버 측에서 사용될 키를 생성
    
    ```bash
    # openssl genrsa -out server-key.pem 4096
    ```
    
    4) 서버 측에서 사용될 인증서를 위한 인증 요청서 파일을 생성.
    
    $HOST 부분에는 사용 중인 도커 호스트의 IP 주소 또는 도메인 이름을 입력하며, 이는 외부에서 접근 가능한 IP 주소 또는 도메인 이름이여야 한다.
    
    ```bash
    # openssl req -subj "/CN=$HOST" -sha256 -new -key server-key.pem -out server.csr
    ```
    
    5) 접속에 사용될 IP 주소를 extfile.cnf 파일로 저장. $HOST에는 도커 호스트의 IP 주소 또는 도메인 이름을 입력 
    
    ```bash
    # echo subjectAltName = IP:$HOST, IP:127.0.0.1 > extfile.cnf
    ```
    
    6) 다음 명령을 입력해 서버 측의 인증서 파일을 생성.
    
    ```bash
    # openssl x509 -req -days 365 -sha256 -in server.csr -CA ca.pem -CAkey ca-key.pem -CAcreateserial -out server-cert.pem -extfile extfile.cnf
    ```
    
2. 클라이언트 측에서 사용할 파일 생성  
    
    1) 클라이언트 측의 키 파일과 인증 요청 파일을 생성하고, extfile.cnf 파일에 extendedKeyUsage 항목을 추가  
    
    ```bash
    # openssl genrsa -out key.pem 4096
    ```
    
    2) 다음 명령을 입력해 클라이언트 측의 인증서를 생성  
    
    ```bash
    # openssl x509 -req -days 30000 -sha256 -in client.csr -CA ca.pem -CAkey ca-key.pem CAcreateserial -out cert.pem -extfile extfile.cnf
    ```
    
    3) 여기까지 따라 했다면 같은 파일이 만들어졌는지 확인.  총 필요한 파일은 5개  
    
    ```bash
    # ls
    ```
    
    4) 생성된 파일의 쓰기 권한을 삭제해 읽기 전용 파일로 만든다  
    
    ```bash
    # chmod -v 0400 ca-key.pem key.pem server-key.pem ca.pem server-cert.pem cert.pem
    ```
    
    5) 도커 데몬의 설정 파일이 존재하는 디렉터인 ~/.docker로 도커 데몬 측에서 필요한 파일을 옮긴다. 이것은 필수적이지는 않지만 도커 데몬에 필요한 파일을 한 곳에 모아두면 관리하기 쉽기 때문이다.  
    
    172.31.32.1
    
    ```bash
    # cp {ca,server-cert,server-key,cert,key}.pem ~/.docker
    ```
    
    6) 다음 명령을 입력해 서버 측의 인증서 파일을 생성.  
    
    ```bash
    # openssl x509 -req -days 365 -sha256 -in server.csr -CA ca.pem -CAkey ca-key.pem -CAcreateserial -out server-cert.pem -extfile extfile.cnf
    ```
    
    TLS 보안 적용을 활성화하기 위해 `-tlsverify` 옵션 추가, `—tlscacert`,` —tlscert`, `—tlskey`에는 각각 보안을 적용하는데 필요한 파일의 위치를 입력  
    
    ```bash
    # dockerd --tlsverify --tlscacert=/root/.docker/ca.pem --tlscert=/root/.docker/server-cert.pem --tlskey=/root/.docker/server-key.epm -H=0.0.0.0:2376 -H unix:///var/run/docker.sock
    
    # docker -H 192.168.99.100:2376 version 
    
    // TLS 연결 설정을 하지 않았다는 에러가 출력
    // 보안이 적용된 도커 데몬을 사용하려면 
    // 이 파일을 docker 명령어의 옵션에 명시하고 다시 원격 제어를 시도
    
    # docker -H 192.168.99.100.2376 --tlscacert=/root/.docker/ca.pem --tlscert=/root/.docker/server-cert.pem --tlskey=/root/.docker/server-key.epm --tlsverify version
    
    ```
    

### 도커 스토리지 드라이버 변경: —storage-driver

도커는 특정 스토리지 백엔드 기술을 사용해 도커 컨테이너와 이미지를 저장하고 관리한다. 

```bash
# docker info | grep "Storage Driver"
```