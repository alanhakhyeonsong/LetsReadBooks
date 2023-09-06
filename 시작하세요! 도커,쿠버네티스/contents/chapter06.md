# 6장. 쿠버네티스 시작하기
## Kubernetes?
> 쿠버네티스는 배포, 스케일링, 그리고 컨테이너화된 애플리케이션의 관리를 자동화 해주는 오픈 소스 컨테이너 오케스트레이션 엔진이다. 본 오픈 소스 프로젝트는 Cloud Native Computing Foundation(CNCF)가 주관한다.  (출처: Kubernetes docs)

쿠버네티스의 장점은 다음과 같다.

- 클러스터링, MSA 구조의 컨테이너 배포, 장애 복구 등 운영에 필요한 오케스트레이션 기능을 지원한다.
- Google, Redhat을 비롯한 많은 오픈소스 진영에서 쿠버네티스에 기여하고 있다.
- 영속적 볼륨, 스케줄링, 장애 복구, 오토 스케일링, 서비스 발견, ingress 등의 기능을 개발자가 직접 설정할 수 있다.
- 다른 클라우드 운영도구와 쉽게 연동 가능하다.

### 쿠버네티스는 대부분의 리소스를 '오브젝트'라고 불리는 형태로 관리한다.

- Pods: 컨테이너의 집합
- Replica Set: 컨테이너의 집합을 관리하는 컨트롤러
- Service Account: 사용자
- Node: 노드

쿠버네티스에서 사용할 수 있는 오브젝트는 `kubectl api-resources`를 통해 확인할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/86e23cc8-4020-49ab-a8fe-65ddfcda594b)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1b279a8d-d61a-4f09-a2d0-044333c9bd36)

### 쿠버네티스는 명령어로도 사용할 수 있지만, YAML 파일을 더 많이 사용한다.
Docker 스웜 모드에선 `docker service create...` 명령어로 컨테이너 서비스를 생성하고 삭제했다. 쿠버네티스에서도 **`kubectl`** 이라는 명령어로 쿠버네티스를 사용할 수 있으며, 대부분의 작업은 이 명령어로 실행할 수 있다.

하지만 쿠버네티스에서 YAML 파일을 더 많이 사용하는데, 용도는 **컨테이너뿐만 아니라 거의 모든 리소스 오브젝트들에 사용될 수 있다는 것이 가장 큰 특징이다.** 컨테이너 자체, 컨테이너의 설정값(ConfigMap), 비밀값(Secrets) 등 모두 YAML 파일로 정의해 사용한다.  
**쿠버네티스에서 실제로 서비스를 배포할 때에도 `kubectl` 명령어가 아닌 여러 개의 YAML 파일을 정의해 쿠버네티스에 적용시키는 방식으로 동작하곤 한다.**

<img width="385" alt="image" style="margin-left: auto; margin-right: auto; display: block;" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9d2e0dd1-979c-42e2-8e38-621d47c65751">

### 쿠버네티스는 여러 개의 컴포넌트로 구성돼 있다.
쿠버네티스 노드의 역할은 크게 마스터와 워커로 나뉜다.
- Master Node: 쿠버네티스가 제대로 동작할 수 있게 클러스터를 관리하는 역할을 담당
  - API 서버, 컨트롤러 매니저, 스케줄러, DNS 서버 등
- Worker Node: 워커 노드엔 애플리케이션 컨테이너가 생성된다.

모든 노드엔 오버레이 네트워크 구성을 위한 프록시(kube-proxy), 네트워크 플러그인이 실행된다.

위 컴포넌트들은 기본적으로 도커 컨테이너로서 실행되고 있다.

또한 모든 노드에는 **쿠버네티스 클러스터 구성을 위해 `kubelet`이라는 에이전트가 모든 노드에서 실행된다.** 이는 컨테이너 생성, 삭제뿐만 아니라 마스터와 워커 노드 간의 통신 역할을 함께 담당하는 매우 중요한 에이전트다. `kubelet`이 정상적으로 실행되지 않으면 해당 노드는 쿠버네티스와 제대로 연결되지 않을 수도 있다. `kubelet`은 CRI(Container Runtime Interface)와 통신하는데, 도커 컨테이너의 경우 runC라는 런타임을 제어하는 containerd는 자체적으로 CRI 플러그인을 내장하고 있어 도커 엔진만 설치해도 쿠버네티스와 문제없이 연결해 사용할 수 있다.

## 포드(Pod): 컨테이너를 다루는 기본 단위
**쿠버네티스에선 컨테이너 애플리케이션의 기본 단위를 Pod** 라고 부르며, Pod는 1개 이상의 컨테이너로 구성된 컨테이너의 집합이다.

예시를 보자.
```yml
# nginx-pod.yaml

apiVersion: v1
kind: Pod
metadata:
  name: my-nginx-pod
spec:
  containers:
  - name: my-nginx-container
    image: nginx:latest
    ports:
    - containerPort: 80
      protocol: TCP
```

- `apiVersion`: YAML 파일에서 정의한 오브젝트의 API 버전을 나타낸다. 오브젝트의 종류 및 개발 성숙도에 따라 설정값이 달라질 수 있다.
- `kind`: 이 리소스의 종류를 나타낸다.
- `metadata`: 라벨, 주석, 이름 등과 같은 리소스의 부가 정보들을 입력한다. 위 예시에선 `name` 항목에서 포드의 고유한 이름을 `my-nginx-pod`로 설정했다.
- `spec`: 리소스를 생성하기 위한 자세한 정보를 입력한다. 위 예시에선 포드에서 실행될 컨테이너 정보를 의미하는 `containers` 항목을 작성한 뒤, 하위 항목인 `image`에서 사용할 도커 이미지를 지정했다. `name` 항목에선 컨테이너의 이름을, `ports` 항목에선 Nginx 컨테이너가 사용할 포트인 80을 입력했다.

작성한 YAML 파일은 `kubectl apply -f` 명령어로 쿠버네티스에 생성할 수 있다. 또한 `kubectl get <오브젝트 이름>`를 통해 특정 오브젝트의 목록을 확인할 수 있다.  
**현재 사용할 포트를 정의하긴 했지만, 외부에서 접근할 수 있도록 노출된 상태는 아니다.** 포드의 Nginx 서버로 요청을 보내려면 포드 컨테이너의 내부 IP로 접근해야 한다.

`kubectl describe` 명령어를 사용하면 생성된 리소스의 자세한 정보를 얻어올 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/db8c618f-aaac-4f92-bc5e-d29f7b1571de)

여기서 IP는 외부에서 접근할 수 있는 IP가 아니라서 클러스터 내부에서만 접근할 수 있다. `docker run`에서 `-p` 옵션 없이 컨테이너를 실행한 것과 비슷하다 생각하면 된다.

**쿠버네티스 외부 또는 내부에서 포드에 접근하려면 service라는 쿠버네티스 오브젝트를 따로 생성해야 한다.**

- `kubectl exec`: 포드의 컨테이너에 명령어를 전달한다.
- `kubectl logs`: 포드의 로그를 확인한다.
- `kubectl delete -f <xxx.yaml>`: 쿠버네티스 오브젝트를 삭제한다.
  - `kubectl delete pod <포드 이름>`

**쿠버네티스가 포드를 사용하는 이유는 컨테이너 런타임의 인터페이스 제공 등 여러 가지가 있지만, 그 이유 중 하나는 여러 리눅스 네임스페이스를 공유하는 여러 컨테이너들을 추상화된 집합으로 사용하기 위해서다.** 실제로 대부분 쿠버네티스의 컨테이너 애플리케이션은 1개의 컨테이너로 포드를 구성해 사용한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/77feefeb-759e-4b72-8c06-07634382ff22)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/43b5a1fb-e668-4613-9beb-31723e2249a6)

하지만 항상 1개의 컨테이너로 구성해야 하는 것은 아니다. `READY` 항목은 포드의 컨테이너 개수에 따라 2/2가 될 수도, 3/3이 될 수도 있다. 보통 파드 내 사이드카로 로그 수집이나 설정 리로딩 프로세스를 띄우는 경우가 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2331ce64-d1f2-4539-8591-40f0b40746d2)

> 📌 포드에 정의된 부가적인 컨테이너를 사이드카(sidecar) 컨테이너라 부르며, 사이드카 컨테이너는 포드 내의 다른 컨테이너와 네트워크 환경 등을 공유하게 된다. 때문에 포드에 포함된 컨테이너들은 모두 같은 워커 노드에서 함께 실행된다.

## 레플리카셋(Replica Set): 일정 개수의 포드를 유지하는 컨트롤러
Pod를 yaml로만 관리하면 생성, 삭제와 같은 관리를 개발자가 직접해야 한다. 이런 방식은 여러 가지 이유로 적절하지 않다.
- 동일한 포드가 많아질수록 일일이 정의하는 것은 매우 비효율적이다.
- 포드가 어떤 이유로든지 삭제되거나, 포드가 위치한 노드에 장애가 발생해 더 이상 포드에 접근하지 못하게 됐을 때, 직접 포드를 삭제하고 다시 생성하지 않는 한 해당 포드는 다시 복구되지 않는다.

레플리카셋은 이러한 한계점을 해결해준다. 레플리카셋의 특징은 다음과 같다.
- 정해진 수의 동일한 포드가 항상 실행되도록 관리한다. 이미 일정량의 파드가 실행중이라도 설정을 변경하면 변경된 만큼 파드를 생성 또는 삭제해준다.
- 노드 장애 등의 이유로 포드를 사용할 수 없다면 다른 노드에서 포드를 다시 생성한다.

`replicaset-nginx.yaml` 파일을 다음과 같이 작성해보자.

```yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: replicaset-nginx
spec:
  replicas: 3
  selector:
    matchLabels:
      app: my-nginx-pods-label
  template:
    metadata:
      name: my-nginx-pod
      labels:
        app: my-nginx-pods-label
    spec:
      containers:
      - name: nginx
        image: nginx:latest
        ports:
        - containerPort: 80
```

- `spec.replicas`: 동일한 포드를 몇 개 유지시킬 것인지 설정한다. 위 예시에선 포드의 개수를 3으로 설정했기 때문에 레플리카셋은 3개의 포드를 새롭게 생성할 것이다.
- `spec.template`: 포드를 생성할 때 사용할 템플릿을 정의한다. `template` 아래의 내용들은 포드를 사용했던 내용을 동일하게 레플리카셋에도 정의함으로써 어떠한 포드를 어떻게 생성할 것인지 명시하는 것이다. 이를 보통 **포드 스펙**, 또는 **포드 템플릿**이라 말한다.

레플리카셋의 포드 개수를 변경할 땐 이미 생성된 레플리카셋을 삭제하고 다시 생성할 필요가 없다. 쿠버네티스에선 이미 생성된 리소스의 속성을 변경하는 기능을 제공하기 때문이다. `kubectl edit`, `kubectl patch` 등 여러 방법을 사용할 수 있지만, 간단히 YAML 파일에서 숫자만 바꿔도 좋다.

### 레플리카셋의 동작 원리
실제로 레플리카셋은 포드와 연결돼 있지 않다. 오히려 느슨한 연결을 유지하고 있으며, 이러한 느슨한 연결은 포드와 레플리카셋의 정의 중 **라벨 셀렉터**를 이용해 이뤄진다.

- 라벨은 단순히 메타데이터의 부가정보만 표시하는 것이 아니라, **리소스를 분류**할 때 유용하게 사용할 수 있는 메타데이터다.
- 레플리카셋은 `spec.selector.matchLabel`에 정의된 라벨을 통해 생성해야 하는 포드를 찾는다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/efe54e11-9897-49e3-bb4b-acdf3fff5836)

만약 포드의 라벨이 바뀌면 더 이상 해당 레플리카셋에 의해 관리되지 않는다. 라벨을 삭제한 포드는 레플리카셋의 `selector.matchLabel` 항목의 값과 더 이상 일치하지 않으므로 레플리카셋에 의해 관리되지 않으며, 직접 수동으로 생성한 포드와 동일한 상태가 된다. 따라서 레플리카셋을 삭제해도 이 포드는 삭제되지 않으며, 직접 수동으로 삭제해야 한다.

참고로, 레플리카셋의 목적은 '포드를 생성하는 것'이 아닌, '일정 개수의 포드를 유지하는 것'이라는 점을 기억하자. `replicas`에 설정된 숫자만큼 동일한 포드를 유지해 바람직한 상태로 만드는 것이 레플리카셋의 목적이다.

## 디플로이먼트(Deployment): 레플리카셋, 포드의 배포를 관리
실제 쿠버네티스 운영 환경에서 레플리카셋을 YAML 파일에서 사용하는 경우는 거의 없다. 대부분은 레플리카셋과 포드의 정보를 정의하는 **디플로이먼트(Deployment)** 라는 이름의 오브젝트를 YAML 파일에 정의해 사용한다.

디플로이먼트는 레플리카셋의 상위 오브젝트이기 때문에 디플로이먼트를 생성하면 해당 디플로이먼트에 대응하는 레플리카셋도 함께 생성된다. 따라서 포드와 레플리카셋을 직접 생성할 필요가 없다.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: my-nginx
  template:
    metadata:
      name: my-nginx-pod
      labels:
        app: my-nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.10
        ports:
        - containerPort: 80
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/61371197-95a3-44b1-91bc-385ea31f6eba)

`kind` 항목이 `Deployment`로 바뀌었을 뿐, 레플리카셋의 파일에서 변경된 부분은 거의 없다. 디플로이먼트를 통해 레플리카셋과 포드를 띄우면 각각 동일한 해시값을 갖는다. 이 해시값은 **포드 템플릿**으로부터 계산되어, 레플리카셋의 라벨 셀렉터에서 `pod-template-hash`라는 이름의 라벨값으로 자동으로 설정된다.

### 디플로이먼트를 사용하는 이유
핵심적인 이유 중 하나는 **애플리케이션의 업데이트와 배포를 더욱 편하게 만들기 위해서다.** 디플로이먼트는 컨테이너 애플리케이션을 배포하고 관리하는 역할을 담당한다. 예를 들어 애플리케이션을 업데이트할 때 레플리카셋의 변경 사항을 저장하는 `revision`을 남겨 롤백을 가능하게 해주고, 무중단 서비스를 위해 포드의 롤링 업데이트의 전략을 지정할 수도 있다.

`kubectl set image deployment my-nginx-deployment nginx=nginx:1.11 --recode` 처럼 `--recode` 옵션을 지정해서 실행하면, 컨테이너 애플리케이션의 버전이 업데이트되어 새롭게 생성된 포드들이 존재할 것이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/04e23eb1-1260-434f-9b40-1e1108987784)

**디플로이먼트는 포드의 정보가 변경되어 업데이트가 발생했을 때, 이전의 정보를 리비전으로서 보존한다.**

`kubectl rollout history deployment my-nginx-deployment` 명령어를 실행하면, 리비전 정보를 더욱 자세히 확인할 수 있다. 결과에 `--recode=true` 옵션이 나타난다면, 디플로이먼트에 기록함으로써 해당 버전의 레플리카셋을 보존한다는 의미다. 만약 이전 버전의 레플리카셋으로 되돌리는 롤백을 하고 싶다면, `--to-revision=[리비전 번호]`를 통해 되돌리자.

- `kubectl rollout undo deployment my-nginx-deployment --to-revision=1`

쿠버네티스 리소스의 자세한 정보를 출력하고자 한다면, `kubectl describe` 명령어를 사용해 디플로이먼트의 정보를 출력하고 현재의 레플리카셋 리비전 정보와 활성화된 레플리카셋의 이름을 확인할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ec199eeb-7e2e-4f89-8ca8-c6c003c2954c)

// `kubectl delete deployment.pod.rs --all`: 생성한 리소스를 모두 삭제

## 서비스(Service): 포드를 연결하고 외부에 노출
포드는 클러스터 내에서 항상 접근할 수 있지만, 포드 IP는 영속적이지 않아 항상 변하기 때문에 여러 개의 디플로이먼트를 하나의 완벽한 애플리케이션으로 연동하려면 포드 IP가 아닌, 서로를 발견할 수 있는 다른 방법이 필요하다.  
Docker에서 `-p` 옵션과는 달리 **쿠버네티스는 디플로이먼트를 생성할 때 포드를 외부로 노출하지 않으며, 디플로이먼트의 YAML 파일에는 단지 포드의 애플리케이션이 사용할 내부 포트만 정의한다.**

앞서 `containerPort` 항목을 정의했다고 이 포드가 바로 외부로 노출되는 것은 아니라는 말이다. 이 포트를 외부로 노출해 사용자들이 접근하거나, 다른 디플로이먼트의 포드들이 내부적으로 접근하려면 **서비스(service)** 라고 부르는 별도의 쿠버네티스 오브젝트를 생성해야 한다. 핵심 기능만 나열해보면 다음과 같다.

- 여러 개의 포드에 쉽게 접근할 수 있도록 고유한 도메인 이름을 부여한다.
- 여러 개의 포드에 접근할 때, 요청을 분산하는 로드 밸런서 기능을 수행한다.
- 클라우드 플랫폼의 로드 밸런서, 클러스터 노드의 포트 등을 통해 포드를 외부로 노출한다.

### 서비스의 종류
쿠버네티스의 서비스는 포드에 어떻게 접근할 것이냐에 따라 종류가 여러 개로 세분화돼 있다. 따라서 목적에 맞는 적절한 서비스의 종류를 선택해야 한다. 주로 사용하는 서비스의 타입은 크게 3가지다.

- `ClusterIP` 타입: 쿠버네티스 내부에서만 포드들에 접근할 때 사용한다. 외부로 포드를 노출하지 않기 때문에 쿠버네티스 클러스터 내부에서만 사용되는 포드에 적합하다.
- `NodePort` 타입: 포드에 접근할 수 있는 포트를 클러스터의 모든 노드에 동일하게 개방한다. 따라서 외부에서 포트에 접근할 수 있는 서비스 타입이다. 접근할 수 있는 포트는 랜덤으로 정해지지만, 특정 포트로 접근하도록 설정할 수도 있다.
- `LoadBalancer` 타입: 클라우드 플랫폼에서 제공하는 로드 밸런서를 동적으로 프로비저닝해 포드에 연결한다. 외부에서 포드에 접근할 수 있는 서비스 타입이지만, 일반적으로 AWS, GCP 등과 같은 클라우드 플랫폼 환경에서만 사용할 수 있다.

### ClusterIP 타입의 서비스 - 쿠버네티스 내부에서만 포드에 접근하기
`hostname-svc-clusterip.yaml` 파일을 다음과 같이 작성해보자.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: hostname-svc-clusterip
spec:
  ports:
    - name: web-port
      port: 8080
      targetPort: 80
  selector:
    app: webserver
  type: ClusterIP
```

- `spec.selector`: `selector` 항목은 이 서비스에서 어떠한 라벨을 가지는 포드에 접근할 수 있게 만들 것인지 결정한다.
- `spec.ports.port`: 생성된 서비스는 쿠버네티스 내부에서만 사용할 수 있는 고유한 IP(Cluster IP)를 할당받는다. `port` 항목에는 서비스의 IP에 접근할 대 사용할 포트를 설정한다.
- `spec.ports.targetPort`: `selector` 항목에서 정의한 라벨에 의해 접근 대상이 된 포드들이 내부적으로 사용하고 있는 포트를 입력한다.
- `spec.type`: 이 서비스가 어떤 타입인지 나타낸다.

서비스를 생성할 때 별도의 설정을 하지 않아도 서비스는 연결된 포드에 대해 로드 밸런싱을 수행한다. 서비스엔 IP뿐만 아니라 서비스 이름 그 자체로도 접근할 수 있다. **쿠버네티스는 애플리케이션이 서비스나 포드를 쉽게 찾을 수 있도록 내부 DNS를 구동하고 있으며, 포드들은 자동으로 이 DNS를 사용하도록 설정되기 때문이다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e98655db-d6d2-421e-808f-30d16c320320)

- 특정 라벨을 가지는 포드를 서비스와 연결하기 위해 서비스의 YAML 파일에 `selector` 항목을 정의한다.
- 포드에 접근할 때 사용하는 포트(포드에 설정된 `containerPort`)를 YAML 파일의 `targetPort` 항목에 정의한다.
- 서비스를 생성할 때, YAML 파일의 `port` 항목에 8080을 명시해 서비스의 Cluster IP와 8080 포트로 접근할 수 있게 설정한다.
- `kubectl apply -f` 명령어로 서비스가 생성되면 이 서비스는 쿠버네티스 클러스터 내부에서만 사용할 수 있는 고유한 내부 IP를 할당받는다.
- 쿠버네티스 클러스터에서 서비스의 내부 IP 또는 서비스의 이름으로 포드에 접근할 수 있다.

### NodePort 타입의 서비스 - 서비스를 이용해 포드를 외부에 노출하기
`NodePort` 타입의 서비스는 모든 노드의 특정 포트를 개방해 서비스에 접근하는 방식이다.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: hostname-svc-nodeport
spec:
  ports:
    - name: web-port
      port: 8080
      targetPort: 80
  selector:
    app: webserver
  type: NodePort
```

GKE에서 쿠버네티스를 사용하고 있는 경우, 각 노드의 랜덤한 포트에 접근하기 위해 별도로 방화벽 설정을 추가해야 한다. 또한, AWS에서도 마찬가지로 Security Group에 별도의 Inbound 규칙을 추가하지 않으면 `NodePort`로 통신이 실패할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c161f1a6-5999-4087-a6cd-76242246cf80)

- 외부에서 포드에 접근하기 위해 각 노드에 개방된 포트로 요청을 전송한다. 예를 들어, 위 그림에서 31514 포트로 들어온 요청은 서비스와 연결된 포드 중 하나로 라우팅된다.
- 클러스터 내부에선 `ClusterIP` 타입의 서비스와 동일하게 접근할 수 있다.


실제 운영 환경에서 사용하기엔 여러 문제점이 있다. 라우팅 노드가 스케일링, 장애 등의 이유로 IP가 변경되면 클라이언트도 수정되어야 하기도 하고, SSL 문제도 잇따른다. 따라서 NodePort 자체를 통해 서비스를 외부로 제공하기 보단 **Ingress**라 부르는 쿠버네티스의 오브젝트에서 간접적으로 사용되는 경우가 많다.

### LoadBalancer 타입의 서비스 - 클라우드 플랫폼의 로드 밸런서와 연동하기
`LoadBalancer` 타입의 서비스는 서비스 생성과 동시에 로드 밸런서를 새롭게 생성해 포드와 연결한다. 이 서비스는 클라우드 플랫폼으로부터 도메인 이름과 IP를 할당받기 때문에 NodePort보다 더욱 쉽게 포드에 접근할 수 있다.

**단, LoadBalancer 타입의 서비스는 로드 밸런서를 동적으로 생성하는 기능을 제공하는 환경에서만 사용할 수 있다는 점을 알아둬야 한다.** 가상 머신이나 온프레미스 환경에선 사용하기 어려울 수 있다.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: hostname-svc-lb
spec:
  ports:
    - name: web-port
      port: 8080
      targetPort: 80
  selector:
    app: webserver
  type: LoadBalancer
```

`ports.port` 항목은 로드 밸런서에 접근하기 위한 포트를 의미하기 때문에 이번엔 80으로 설정했다.

위 파일로 서비스를 생성한 뒤 목록을 확인했을 때, `EXTERNAL-IP` 항목을 눈여겨보자. 이는 클라우드 플랫폼으로부터 자동으로 할당된 것이며, 이 주소와 80 포트를 통해 포드에 접근할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1cde8e72-5b50-4e88-bf77-1ab703aefc28)

- `LoadBalancer` 타입의 서비스가 생성됨과 동시에 모든 워커 노드는 포트에 접근할 수 있는 랜덤한 포트를 개방한다. 위 예시는 32620 포트가 개방됐다.
- 클라우드 플랫폼에서 생성된 로드 밸런서로 요청이 들어오면 이 요청은 쿠버네티스의 워커 노드 중 하나로 전달되며, 이때 사용되는 포트는 1번에서 개방된 포트인 32620 포트다.
- 워커 노드로 전달된 요청은 포트 중 하나로 전달되어 처리된다.

### 트래픽의 분배를 결정하는 서비스 속성: externalTrafficPolicy
서비스는 기본적으로 `externalTrafficPolicy`가 Cluster로 설정된다. 이 경우 노드로 들어온 다른 요청은 다른 노드의 파드로 리다이렉트 되는 경우가 있고, 이 때 네트워크 홉으로 인해 NAT가 발생하며 클라이언트 IP가 보존 불가해진다.

이 속성을 Local로 설정하면 포드가 생성된 노드에서만 포드로 접근할 수 있으며, 로컬 노드에 위치한 포드 중 하나로 요청이 전달된다. 즉, 추가적인 네트워크 홉이 발생하지 않으며, 전달되는 요청의 클라이언트 IP 또한 보존된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/993bebdb-1b8f-452f-ade7-841500c01be8)

그렇지만, `externalTrafficPolicy`를 Local로 설정하는 것이 무조건 좋은 것은 아니다. 각 노드에 포드가 고르지 않게 스케줄링됐을 때, 요청이 고르게 분산되지 않을 수도 있기 때문이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/4895f944-6faf-48a7-8003-d18adad0f29d)

특정 노드의 포드에 부하가 집중되거나 적어질 수도 있으며, 이는 곧 자원 활용률 측면에서 바람직하지 않을 수도 있다는 것을 의미한다.

장단점이 있기 때문에 정답은 없다. 불필요한 네트워크 홉으로 인한 레이턴시나 클라이언트의 IP 보존이 중요하지 않다면 Cluster를 사용하고 그 반대라면 Local을 사용하는 것이 좋을 것 같다.

### 요청을 외부로 리다이렉트하는 서비스: ExternalName
`ExternalName` 타입의 서비스는 서비스가 외부 도메인을 가리키도록 설정할 수 있다.
