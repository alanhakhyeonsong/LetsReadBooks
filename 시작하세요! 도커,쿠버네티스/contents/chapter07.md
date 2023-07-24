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