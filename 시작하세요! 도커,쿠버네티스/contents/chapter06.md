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

- `apiVersion`: 
- `kind`: 
- `metadata`: 
- `spec`: 