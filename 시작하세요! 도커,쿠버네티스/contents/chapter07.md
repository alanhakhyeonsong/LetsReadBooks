# 7장. 쿠버네티스 리소스의 관리와 설정
## 네임스페이스(Namespace): 리소스를 논리적으로 구분하는 장벽
도커나 도커 스웜 모드를 사용할 땐 컨테이너를 논리적으로 구분하는 방법이 없었다. 하지만 쿠버네티스에선 리소스를 논리적으로 구분하기 위해 **네임스페이스**라는 오브젝트를 제공한다.  
네임스페이스는 포드, 레플리카셋, 디플로이먼트, 서비스 등과 같은 쿠버네티스 리소스들이 묶여 있는 하나의 가상 공간 또는 그룹이라 이해하면 된다.

목적에 맞는 리소스들의 그룹을 네임스페이스로 묶어서 사용할 수 있고, 여러 개발 조직이 하나의 쿠버네티스 클러스터를 공유해 사용해야 한다면 조직별로 네임스페이스를 사용하도록 구성할 수도 있다. 여러 개의 네임스페이스를 사용하면 마치 하나의 클러스터에서 여러 개의 가상 클러스터를 동시에 사용하는 것처럼 느껴질 것이다.

`kubectl get namespace` 혹은 `kubectl get ns`를 통해 네임스페이스의 목록을 확인할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a36ba41f-8ad7-49bd-8096-e4d4703e4f78)

우리가 네임스페이스를 생성하지 않았더라도 기본적으로 3개의 네임스페이스가 존재한다. 각 네임스페이스는 논리적인 리소스 공간이기 때문에 각 네임스페이스에는 포드, 레플리카셋, 서비스와 같은 리소스가 따로 존재한다.

`default`는 쿠버네티스를 설치하면 자동으로 사용하도록 설정되는 네임스페이스로, `kubectl` 명령어로 쿠버네티스 리소스를 사용할 땐 기본적으로 `default` 네임스페이스를 사용한다. `--namespace` 옵션을 명시하지 않으면 기본적으로 `default` 네임스페이스를 사용한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1a228e90-aa84-4b1e-a95e-38ca63907dce)

`kube-system`이라는 네임스페이스의 포드를 확인하면 위와 같이 우리가 생성한 적 없는 포드가 여러 개 실행되고 있다. `kube-system`은 쿠버네티스 클러스터 구성에 필수적인 컴포넌트들과 설정값 등이 존재하는 네임스페이스다. `default` 네임스페이스와는 논리적으로 구분돼있다. 이 네임스페이스는 충분한 이해 없이는 건드리지 말자.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/dabe1bb3-5b3e-43eb-8484-3cdb031347ee)

`kube-system` 네임스페이스에선 위처럼 쿠버네티스의 포드, 서비스 등을 이름으로 찾을 수 있도록 하는 DNS 서버의 서비스가 미리 생성돼있다. 이처럼 네임스페이스는 쿠버네티스의 리소스를 논리적으로 묶을 수 있는 가상 클러스터처럼 사용할 수 있다. 네임스페이스를 사용하는 경우는 대부분 모니터링, 로드 밸런싱 인그레스(Ingress) 등의 특정 목적을 위한 용도가 대부분일 것이다. 하지만, **각 네임스페이스의 리소스는 논리적으로만 구분된 것일 뿐, 물리적으로 격리된 것이 아니라는 점을 알아둬야 한다.**

네임스페이스는 yaml 파일에 정의해 생성할 수 있다.

```yaml
# production-namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: production
```

쿠버네티스 클러스터 내부에선 서비스 이름을 통해 포드에 접근할 수 있다고 앞서 이야기 했는데, 이는 정확히 말하자면 **'같은 네임스페이스 내의 서비스'에 접근할 때에는 서비스 이름만으로 접근할 수 있다는 뜻이다.**

이전에 사용했던 임시 포드들은 `default` 네임스페이스에 생성됐기 때문에 `production` 네임스페이스의 서비스에 접근하지 못한다. 하지만 `<서비스 이름>.<네임스페이스 이름>.svc` 처럼 서비스 이름 뒤에 네임스페이스 이름을 붙이면 다른 네임스페이스의 서비스에도 접근할 수 있다.

네임스페이스를 사용하면 쿠버네티스 리소스를 사용 목적에 따라 논리적으로 격리할 순 있지만, **모든 리소스가 네임스페이스에 의해 구분되는 것은 아니다.**  
포드, 서비스, 레플리카셋, 디플로이먼트는 네임스페이스 단위로 구분할 수 있다. 이런 경우를 **쿠버네티스에선 '오브젝트가 네임스페이스에 속한다'라고 표현한다.**  
이와 반대로 네임스페이스에 속하지 않는 쿠버네티스 오브젝트도 있다. `kubectl get nodes` 명령어를 사용한 것처럼 **노드(nodes) 또한 쿠버네티스의 오브젝트 중 하나지만, 네임스페이스에 속하지 않는 대표적인 오브젝트 중 하나이다.** 이는 쿠버네티스 클러스터에서 사용되는 저수준의 오브젝트이며, 네임스페이스에 의해 구분되지 않는다.

## 컨피그맵(Configmap), 시크릿(Secret): 설정값을 포드에 전달
애플리케이션의 설정값이나 설정 파일을 애플리케이션에 전달하는 가장 확실한 방법은 도커 이미지 내부에 설정값 또는 설정 파일을 정적으로 저장해 놓는 것이다. 하지만, **도커 이미지는 일단 빌드되고 나면 불변의 상태를 가지기 때문에 이 방법은 상황에 따라 설정 옵션을 유연하게 변경할 수 없다는 단점이 있다.**

포드를 정의하는 yaml 파일에 환경 변수를 직접 적어 놓는 하드 코딩 방식을 사용할 순 있지만 상황에 따라서는 환경 변수의 값만 다른, 동일한 여러 개의 yaml이 존재할 수 있다. 만약 운영 환경과 개발 환경에서 각각 디플로이먼트를 생성해야 한다면 환경 변수가 서로 다르게 설정된 두 가지 버전의 yaml 파일이 따로 존재해야 하기 때문이다.

**쿠버네티스는 yaml 파일과 설정값을 분리할 수 있는 컨피그맵(Configmap)과 시크릿(secret)이라는 오브젝트를 제공한다.**

- `ConfigMap`: 설정값을 저장
- `Secret`: 노출되어서는 안 되는 비밀값을 저장

<img width="703" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ef09b84f-200a-4797-9856-c519f54ec0ae">

위 그림처럼 환경 변수나 설정값까지 쿠버네티스 오브젝트에서 관리할 수 있으며, 이러한 설정값 또한 yaml 파일로 포드와 함께 배포할 수 있다. 컨피그맵이나 시크릿을 사용하면 애플리케이션과 설정값을 별도로 분리해 관리할 수 있다는 장점이 있다.

### 컨피그맵(Configmap)
컨피그맵은 일반적인 설정값을 담아 저장할 수 있는 쿠버네티스 오브젝트이며, 네임스페이스에 속하기 때문에 네임스페이스별로 컨피그맵이 존재한다.  
yaml 파일을 사용해 생성해도 되지만, `kubectl create configmap` 명령어를 사용하면 쉽게 컨피그맵을 생성할 수 있다. 생성된 컨피그맵을 포드에서 사용하려면 디플로이먼트 등의 yaml 파일에서 포드 템플릿 항목에 컨피그맵을 사용하도록 정의하면 된다.

> `kubectl create configmap <컨피그맵 이름> <각종 설정값들>`  
> ex) `kubectl create configmap log-level-configmap --from-literal LOG_LEVEL=DEBUG`

<img width="694" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/11937092-086e-4cb6-b26d-761490c817a7">

컨피그맵을 포드에서 사용하는 방법은 크게 두 가지가 있다.

- 컨피그맵의 값을 컨테이너의 환경 변수로 사용: 컨피그맵에 저장된 키-값 데이터가 컨테이너의 환경 변수의 키-값으로서 그대로 사용된다. 애플리케이션이 시스템 환경 변수로부터 설정값을 가져온다면 이 방법을 사용하는 것이 좋다.
- 컨피그맵의 값을 포드 내부의 파일로 마운트해 사용: 애플리케이션이 `nginx.conf` 같은 파일을 동해 설정값을 읽어 들인다면 이 방법을 사용하는 것이 좋다.

첫 번째 방법을 먼저 살펴보자. 다음 내용으로 yaml 파일을 작성해보자.

```yaml
# all-env-from-configmap.yaml
apiVersion: v1
kind: Pod
metadata:
  name: container-env-example
spec:
  containers:
    - name: my-container
      image: busybox
      args: ['tail', '-f', '/dev/null']
      envFrom:
      - configMapRef:
          name: log-level-configmap # 키-값 쌍이 1개 존재하는 컨피그맵
      - configMapRef:
          name: start-k8s # 키-값 쌍이 2개 존재하는 컨피그맵
```

yaml 파일에서 `envFrom` 항목은 하나의 컨피그맵에 여러 개의 키-값 쌍이 존재하더라도 모두 환경 변수로 가져오도록 설정한다. 따라서 위 예시는 총 3개의 키-값 쌍을 포드로 넘긴 셈이다.

포드를 생성한 뒤, `kubectl exec container-env-example env` 명령어를 사용하면 설정한 환경 변수의 목록을 출력하게 된다.

`valueFrom`, `configMapKeyRef`를 사용하면 여러 개의 키-값 쌍이 들어 있는 컨피그맵에서 특정 데이터만을 선택해 환경 변수로 가져올 수도 있다. 다음과 같이 yaml 파일을 작성해보자.

```yaml
# selective-env-from-configmap.yaml
apiVersion: v1
kind: Pod
metadata:
  name: container-env-example
spec:
  containers:
    - name: my-container
      image: busybox
      args: ['tail', '-f', '/dev/null']
      envFrom:
      - configMapRef:
          name: log-level-configmap
      - configMapRef:
          name: start-k8s
      env:
      - name: ENV_KEYNAME_1
        valueFrom:
          configMapKeyRef:
            name: log-level-configmap # 참조할 컨피그맵의 이름
            key: LOG_LEVEL            # 가져올 데이터 값의 키
      - name: ENV_KEYNAME_2
        valueFrom:
          configMapKeyRef:
            name: start-k8s
            key: k8s
```

위 두 가지 방식에서 사용한 옵션을 정리해보면 다음과 같다.

- `envFrom`: 컨피그맵에 존재하는 모든 키-값 쌍을 가져온다.
- `valueFrom`과 `configMapKeyRef`: 컨피그맵에 존재하는 키-값 쌍 중에서 원하는 데이터만 선택적으로 가져온다.

다음으로 두 번째 방법을 살펴보자. 특정 파일로부터 설정값을 읽어온다면 컨피그맵의 데이터를 포드 내부의 파일로 마운트해 사용할 수 있다. 예를 들어 아래의 yaml 파일은 `start-k8s` 컨피그맵에 존재하는 모든 키-값 쌍을 `/etc/config` 디렉터리에 위치시킨다.

```yaml
# volume-mount-configmap.yaml
apiVersion: v1
kind: Pod
metadata:
  name: configmap-volume-pod
spec:
  containers:
    - name: my-container
      image: busybox
      args: [ "tail", "-f", "/dev/null" ]
      volumeMounts:
      - name: configmap-volume # volumes에서 정의한 컨피그맵 볼륨 이름
        mountPath: /etc/config # 컨피그맵의 데이터가 위치할 경로

  volumes:
    - name: configmap-volume # 컨피그맵 볼륨 이름
      configMap:
        name: start-k8s # 키-값 쌍을 가져올 컨피그맵 이름
```

- `spec.volumes`: yaml 파일에서 사용할 볼륨의 목록을 정의한다. `volumes` 항목에서 정의한 볼륨은 `spec.containers` 항목에서 참조해 사용하고 있다.
- `spec.containers.volumeMounts`: `volumes` 항목에서 정의된 볼륨을 컨테이너 내부의 어떤 디렉터리에 마운트할 것인지 명시한다.

위 yaml 파일로 포드를 생성한 뒤, 포드의 `/etc/config` 디렉터리를 조회해보자.

```bash
$ kubectl exec configmap-volume-pod ls /etc/config
container
k8s

$ kubectl exec configmap-volume-pod cat /etc/config/k8s
kubernetes
```

<img width="571" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c4de616e-f2a7-4b33-8a5b-15c6362e1452">

여기서 알아둬야 할 것은 컨피그맵의 모든 키-값 쌍 데이터가 마운트됐으며, 파일 이름은 키의 이름과 같다는 것이다.

원하는 키-쌍 데이터만 선택해서 포드에 파일로 가져올 수도 있다.

```yaml
# selective-volume-configmap.yaml
apiVersion: v1
kind: Pod
metadata:
  name: configmap-volume-pod
spec:
  containers:
    - name: my-container
      image: busybox
      args: [ "tail", "-f", "/dev/null" ]
      volumeMounts:
      - name: configmap-volume
        mountPath: /etc/config  # 마운트되는 위치는 변경되지 않았다.

  volumes:
    - name: configmap-volume
      configMap:
        name: start-k8s
        items:                  # 컨피그맵에서 가져올 키-값의 목록을 나열한다.
        - key: k8s              # k8s라는 키에 대응하는 값을 가져온다.
          path: k8s_fullname    # 최종 파일 이름은 k8s_fullname이 된다.
```

이전 yaml 파일과 비교하여 `volumes` 항목이 약간 달라졌다.

- `items` 항목: 컨피그맵에서 가져올 키-값의 목록을 의미하며, k8s라는 키만 가져오도록 명시했다.
- `path` 항목: 최종적으로 디렉터리에 위치할 파일의 이름을 입력하는 항목으로, `k8s_fullname`이라는 값을 입력했다.

위 예시에선 `k8s`라는 키에 해당하는 값이 `k8s_fullname`이라는 파일로 포드 내부에 존재할 것이다.

### 파일로부터 컨피그맵 생성하기
컨피그맵을 볼륨으로 포드에 제공할 땐 대부분 설정 파일 그 자체를 컨피그맵으로 사용하는 경우가 많다. 예를 들어 Nginx의 `nginx.conf` 또는 MySQL의 `mysql.conf`의 내용을 통째로 컨피그맵에 저장한 뒤 이를 볼륨 파일로 포드 내부에 제공하면 좀 더 효율적인 설정 관리가 가능해질 것이다. 이러한 경우를 위해 쿠버네티스는 컨피그맵을 파일로부터 생성하는 기능 또한 제공한다.

`kubectl create configmap <컨피그맵 이름> --from-file <파일 이름> ...` 과 같이 `--from-file` 옵션을 여러 번 사용해 여러 개의 파일을 컨피그맵에 저장할 수 있다.

`--from-file` 옵션에서 별도의 키를 지정하지 않으면 파일 이름이 키로, 파일의 내용이 값으로 저장된다.

파일로부터 컨피그맵을 생성할 때는 파일 내용에 해당하는 키의 이름을 직접 지정할 수도 있다.  
ex) `kubectl create configmap index-file-customkey --from-file myindex=index.html`

또는 `--from-env-file` 옵션으로 여러 개의 키-값 형태의 내용으로 구성된 설정 파일을 한꺼번에 컨피그맵으로 가져올 수도 있다.

```bash
$ kubectl create configmap from-envfile --from-env-file multiple-keyvalue.env
configmap/from-envfile created

$ kubectl get cm from-envfile -o yaml
apiVersion: v1
data:
  mykey1: myvalue1
  mykey2: myvalue2
  mykey3: myvalue3
...
```

컨피그맵은 반드시 명령어를 통해 생성해야 하는 것은 아니다. `kubectl create` 명령어에서 `--dry-run`, `-o yaml` 옵션을 사용하면 컨피그맵을 생성하지 않은 채로 yaml 파일의 내용을 출력할 수 있다. 출력된 내용을 yaml 파일로 사용하면 컨피그맵 또한 yaml 파일로 배포해 사용할 수 있다.

```bash
$ kubectl create configmap my-configmap --from-literal mykey=myvalue --dry-run -o yaml > my-configmap.yaml

$ kubectl apply -f my-configmap.yaml
configmap/my-configmap created
```

참고로, `dry run`이란 특정 작업의 실행 가능 여부를 검토하는 명령어 또는 API를 의미한다. 이때 실제로 쿠버네티스 리소스를 생성하진 않는다.

### 시크릿(Secret)
**시크릿은 SSH 키, 비밀번호 등과 같이 민감한 정보를 저장하기 위한 용도로 사용되며, 네임스페이스에 종속되는 쿠버네티스 오브젝트이다.** 시크릿과 컨피그맵은 사용 방법이 매우 비슷하다. 하지만 시크릿은 민감한 정보를 저장하기 위해 컨피그맵보다 좀 더 세분화된 사용 방법을 제공한다.

```bash
$ kubectl create secret generic my-password --from-literal password=1q2w3e4r

secret/my-password created

$ echo mypassword > pw1 && echo yourpassword > pw2
$ kubectl create secret generic our-password --from file pw1 --from file pw2

secret/our-password created
```

`kubectl get secret my-password -o yaml` 명령어를 실행해 시크릿의 내용을 확인해보면 컨피그맵과 비슷한 형식으로 데이터가 저장됐지만, 키-값 쌍에서 값에 해당하는 부분이 base64로 인코딩 된 형태로 저장되어있다. 이는 쿠버네티스가 기본적으로 base64로 값을 인코딩하기 때문이다.

이렇게 생성된 시크릿은 컨피그맵과 비슷하게 사용할 수 있다. 시크릿의 키-값 데이터를 포드의 환경 변수로 설정할 수도 있고, 특정 경로의 파일로 포드 내에 마운트할 수도 있다.

이미지 레지스트리 접근을 위해 `docker-registry` 타입의 시크릿을 사용할수도 있다. 이전에 생성한 시크릿은 모두 `Opaque` 타입으로 설정돼있다. 이는 사용자가 정의하는 데이터를 저장할 수 있는 일반적인 목적의 시크릿이다.

한편, 비공개 레지스트리에 접근할 때 사용하는 인증 설정 시크릿을 만들 수도 있다. **쿠버네티스의 디플로이먼트 등을 이용해 포드를 생성할 때, yaml 파일에 정의된 이미지가 로컬에 존재하지 않으면 쿠버네티스는 자동으로 이미지를 받아온다.** 쿠버네티스는 `docker login` 명령어 대신 레지스트리의 인증 정보를 저장하는 별도의 시크릿을 생성해 사용한다.

- `docker login` 명령어로 로그인에 성공했을 때 도커 엔진이 자동으로 생성하는 `~/.docker/config.json` 파일을 사용하는 방법  
ex) `kubectl create secret generic registry-with-auth --from-file=.dockerconfigjson=/root/.docker/config.json --type=kubernetes.io/dockerconfigjson`
- 시크릿을 생성하는 명령어에서 직접 로그인 인증 정보를 명시하는 방법  
ex) `kubectl create secret generic registry-with-auth-by-cmd --docker-username=test123 --docker-password=1q2w3e4r`

참고로 `--docker-server`는 필수 옵션이 아니다. 이 옵션을 사용하지 않으면 기본적으로 도커 허브를 사용하도록 설정되지만, 다른 사설 레지스트리를 사용하려면 `--docker-server` 옵션에 해당 서버의 주소 또는 도메인 이름을 입력하면 된다.

위 명령어로 생성된 시크릿은 `kubernetes.io/dockerconfigjson`이라는 타입으로 설정된다. 이 시크릿은 디플로이먼트 또는 포드 등에서 사설 레지스트리로부터 이미지를 받아올 때 사용할 수 있다.  
예를 들어, 도커 허브의 프라이빗 저장소에 저장된 이미지를 통해 포드를 생성하려면 yaml 파일에서 `imagePullSecret` 항목을 정의하면 된다.

```yaml
apiVersion: apps/v1
kind: Deployment
...

spec:
  containers:
  - name: test-container
    image: <이미지 이름>
  imagePullSecrets:
  - name: registry-auth-registry
```

또한 시크릿은 TLS 연결에 사용되는 공개키, 비밀키 등을 쿠버네티스에 자체적으로 저장할 수 있도록 `tls` 타입을 지원한다. 포드 내부의 애플리케이션이 보안 연결을 위해 인증서나 비밀키 등을 가져와야 할 때 시크릿의 값을 포드에 제공하는 방식으로 사용할 수 있다.

보안 연결에 사용되는 키 페어가 미리 준비돼 있다면 `kubectl create secret tls` 명령어로 쉽게 생성할 수 있다.

### 컨피그맵이나 시크릿을 업데이트해 애플리케이션의 설정값 변경하기
애플리케이션의 설정값을 변경해야 한다면 컨피그맵이나 시크릿의 값을 `kubectl edit` 명령어로 수정해도 되고, yaml 파일을 변경한 뒤 다시 `kubectl apply` 명령어를 사용해도 된다. `kubectl patch` 명령어 역시 사용할 수 있다.

환경 변수로 포드 내부에 설정값을 제공하는 방법으로 설정된 값은 컨피그맵이나 시크릿의 값을 변경해도 자동으로 재설정되지 않으며, 디플로이먼트의 포드를 다시 생성해야만 한다.

볼륨 파일로 포드 내부에 마운트하는 방법으로 설정하면 컨피그맵이나 시크릿을 변경하면 파일의 내용 또한 자동으로 갱신된다. **단, 포드 내부에 마운트된 설정 파일이 변경됐다고 해서 포드에서 실행 중인 애플리케이션의 설정이 자동으로 변경되는 것은 아니다.** 업데이트된 설정값을 포드 내부의 프로세스가 다시 로드하려면 별도의 로직을 직접 구현해야 한다.

- 변경된 파일을 다시 읽어들이도록 컨테이너의 프로세스에 별도의 시그널을 보내는 사이드카 컨테이너를 포드에 포함시킨다.
- 애플리케이션의 소스코드 레벨에서 쿠버네티스 API를 통해 컨피그맵, 시크릿의 데이터 변경에 대한 알림을 받은 뒤, 자동으로 리로드하는 로직을 구현한다.