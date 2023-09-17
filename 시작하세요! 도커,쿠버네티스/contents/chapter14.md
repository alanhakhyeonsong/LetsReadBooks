# 14장. 쿠버네티스 모니터링
실제 운영 단계의 쿠버네티스 클러스터를 계획하고 있다면 모니터링 시스템은 반드시 구축해야 하며, 어떠한 상황에서 어떠한 모니터링 데이터를 확인해야 하는지를 알고 있어야 한다. 예를 들어, 다음과 같은 상황에선 CPU나 메모리 같은 기초적인 모니터링 데이터를 확인하는 것부터 트러블슈팅을 시작할 수 있을 것이다.

- 사용자 요청이 갑작스럽게 몰려서 부하가 발생할 때
- 인프라 또는 애플리케이션에 장애가 발생했을 때
- 애플리케이션의 일반적인 리소스 사용 패턴을 파악해야 할 때
- 그 외의 다양한 상황들

실제 상황에선 CPU, 메모리 외에도 굉장히 많은 종류의 메트릭을 확인해야 할 수도 있다. 디스크 사용량, 네트워크 I/O, 초당 요청 수, 애플리케이션 자체에 의존적인 메트릭도 존재할 수 있다. 쿠버네티스는 이러한 메트릭을 수집할 수 있도록 자체적인 모니터링 기능을 제공하진 않는다. 모니터링 시스템은 여러 오픈소스 도구를 조합해서 구축하거나 클라우드 플랫폼이나 상용 솔루션을 구입해서 사용하는 것이 일반적이다.

이 책에선 Prometheus를 기준으로 설명하고 있다. 프로메테우스와 같은 오픈소스를 사용해 모니터링 시스템을 구축할 경우 모든 관리를 직접해야 한다는 단점이 있다. 하지만 커스터마이징이 필요하거나 쿠버네티스와 높은 호환성을 유지하는 모니터링 시스템을 구축할 수 있다는 장점이 있다.

## 모니터링 기본 구조
클라우드에서의 모니터링 메트릭이 어떻게 데이터베이스로 수집되는지 전체적인 흐름을 먼저 파악하는 것이 좋다. CAdvisor로 예를 들면, 이는 컨테이너에 관련된 모니터링 데이터를 확인할 수 있는 모니터링 도구인데 제공되는 웹 UI는 단기간의 메트릭만 제공할 뿐, 체계적으로 데이터를 저장하고 분석하진 않는다. 어떻게 CAdvisor를 통해 모니터링 데이터를 데이터베이스에 수집하고 시각화 할 수 있을까?

<img width="613" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e516fd72-d8d7-4f58-9fc2-6e5b3f83d48c">

모니터링 에이전트 부류의 도구들은 `/metrics`라 하는 경로를 외부에 노출시켜 제공한다. 이 `/metrics` 경로로 요청을 보내면 CAdvisor는 key-value 쌍으로 구성된 메트릭 데이터의 목록을 반환하는데, 이 메트릭 데이터를 프로메테우스 같은 시계열 데이터베이스에 저장하면 된다. 프로메테우스는 CAdvisor의 엔드포인트를 미리 지정해주면 해당 `/metrics`에 접근해 자동으로 데이터를 수집한다.

이처럼 `/metrics` 경로를 외부에 노출시켜 데이터를 수집할 수 있도록 인터페이스를 제공하는 서버를 일반적으로 exporter라 부른다.

## 모니터링 메트릭의 분류
모니터링 메트릭은 크게 3단계로 분류할 수 있다.

- 인프라 수준의 메트릭: 호스트 레벨에서의 메트릭을 의미한다. 예를 들어 호스트에서 사용 중인 파일 디스크립터의 개수, 호스트에 마운트돼 있는 디스크 사용량, 호스트 NIC의 패킷 전송량 등이 될 수 있다. `node-exporter`라는 도구가 제공하는 메트릭은 이 수준의 메트릭에 해당한다.
- 컨테이너 수준의 메트릭: 컨테이너 레벨에서의 메트릭을 의미한다. 컨테이너별 CPU와 메모리 사용량, 컨테이너 프로세스의 상태, 컨테이너에 할당된 리소스의 할당량, 쿠버네티스 포드의 상태 등이 포함될 수 있다. CAdvisor가 제공하는 메트릭은 이 수준에 해당한다.
- 애플리케이션 수준의 메트릭: 인프라와 컨테이너를 제외한 애플리케이션 레벨에서 정의하는 모든 메트릭을 의미한다. 마이크로서비스에서 발생하는 트레이싱 데이터일 수도 있고, 애플리케이션 로직에 종속적인 데이터일 수도 있으며, 서버 프레임워크에서 제공하는 모니터링 데이터일 수도 있다.

모니터링 시스템을 구축한 뒤 다루게 될 데이터는 대부분 인프라 또는 컨테이너 수준의 모니터링 데이터다.

```bash
$ curl localhost:8080/metrics
...
container_cpu_system_seconds_total{...name=""} 0.01 1600693225593
...
container_memory_rss{...name="k8s_POD_kube-proxy-..."} 20480 1600693238719
...
machine_cpu_cores 2 # 호스트에 존재하는 CPU 코어 수
```

대부분이 컨테이너에 관련된 메트릭인데, 이러한 메크릭을 시계열 데이터베이스에 수집하고, 원하는 형태로 가공하면 컨테이너 수준의 모니터링을 할 수 있다.

## 쿠버네티스 모니터링 기초
### metrics-server
쿠버네티스는 자체적으로 모니터링 기능을 제공하진 않지만 쿠버네티스 메트릭을 수집해 사용할 수 있도록 몇 가지 애드온을 제공한다. 가장 기초적인 것은 **컨테이너와 인프라 레벨에서의 메트릭을 수집하는 `metrics-server`라는 도구다.** 이를 설치하면 **포드의 오토스케일링, 사용 중인 리소스 확인 등 여러 기능을 추가적으로 사용할 수 있으므로 가능하다면 반드시 설치할 것을 권장한다.**

`docker stats`와 비슷하게 `kubectl top` 명령어가 있다. 이는 포드 또는 노드의 리소스 사용량을 확인할 수 있는데, 기본적으로 에러가 출력될 것이다. **단일 도커 호스트와는 달리 쿠버네티스는 여러 개의 노드로 구성돼 있기 때문에 `docker stats`처럼 쉽게 메트릭을 확인할 수는 없다.** `kubectl top` 명령어를 사용하려면 클러스터 내부의 메트릭을 모아 제공하는 `metrics-server`가 필요하다.

```bash
$ wget https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.3.8/components.yaml

# metrics-server의 디플로이먼트 부분에서 실행 옵션에 다음과 같이 추가한다.
$ vim component.yaml
...
      containers:
      - name: metrics-server
        image: k8s.gcr.io/metrics-server/metrics-server:v0.3.7
        imagePullPolicy: IfNotPresent
        args:
          - --cert-dir=/tmp
          - --secure-port=4443
          - --kubelet-insecure-tls
...

# 수정한 YAML 파일을 통해 metrics-server를 설치
$ kubectl apply -f components.yaml

$ kubectl get po -n kube-system | grep metrics-server
metrics-server-7949d47784-7gfjl      1/1      Running     0       2m56s

$ kubectl top po -n kube-system
NAME                                    CPU(cores)  MEMORY(bytes)
calico-kube-controllers-75d555c48-4nffh 1m          8Mi
calico-node-7chq8                       18m         62Mi
...

$ kubectl top no
NAME                 CPU(cores)  CPU%   MEMORY(bytes)   MEMORY%
ip-10-40-0-10.ap-..  134m        6%     1161Mi          30%
ip-10-40-0-30.ap-..  76m         3%     805Mi           21%
...
```

### metrics-server 동작 원리: APIService 리소스
`metrics-server`는 어떠한 방식으로 메트릭을 모아 사용자에게 보여줄 수 있을까?

**쿠버네티스의 노드 에이전트인 `kubelet`은 CAdvisor를 자체적으로 내장하고 있으며, 포드와 노드 메트릭을 반환하는 `/stats/summary`라는 엔드포인트를 제공한다.** `kubelet`은 기본적으로 노드의 10250 포트로 연결돼 있다.

```bash
$ kubectl run --generator=run-pod/v1 -i --tty --rm debug \
  --image=alicek106/ubuntu:curl bash
root@debug:/ curl https://10.40.0.10:10250/stats/summary -k
Unauthorized
``` 

`kubelet`으로부터 Unauthorized 응답이 되돌아오는데, 이는 해당 엔드포인트에 접근하기 위한 권한 정보를 넣어주지 않았기 때문이다. `kubelet`으로부터 메트릭을 가져오기 위한 권한을 ClusterRole로 정의해서 서비스 어카운트에 연결한 뒤, 서비스 어카운트의 토큰을 curl의 헤더에 포함시켜 요청해야 정상적인 메트릭을 확인할 수 있다.

`metrics-server`의 YAML 파일엔 `metrics-server` 서비스 어카운트가 `nodes/stats`라는 리소스에 접근할 수 있도록 권한이 이미 부여돼 있다.

```bash
$ cat components.yaml | grep -B10 -F10 nodes/stats
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: system:metrics-server
...
  resources:
  ...
  - nodes/stats
...
```

따라서 존재하는 `metrics-server` 서비스 어카운트의 토큰을 복사해 curl 요청을 보내면 메트릭이 정상적으로 반환될 것이다.

```bash
$ kubectl get secrets -n kube-system | grep metrics-server
metrics-server-token-4n2tj    kubernetes.io/service-account-token    3    81m

$ kubectl get secrets metrics-server-token-4n2tj \
-n kube-system -o jsonpath=${.data.token} | base64 -d
```

<img width="585" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d235d1b9-6493-41a2-9045-32c184e39eb7">

위와 같은 방식을 통해 `metrics-server`는 `kubelet`으로부터 주기적으로 노드, 포드 메트릭을 수집한다. 이후 `metrics-server`는 수집한 데이터를 한 번 더 정제해 외부에 제공하며, `kubectl top` 명령어는 `metrics-server`가 제공하는 메트릭을 통해 결과를 보여준다.

<img width="515" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6df4f545-1302-40e0-8d77-888ea04639f9">

`metrics-server`는 일종의 확장된 API 서버 역할을 한다. API 서버의 스펙을 준수하는 별도의 서버를 구축한 뒤, 이를 쿠버네티스에 등록하면 마치 확장된 API 서버처럼 사용할 수 있다고 생각하면 된다. 예제에서 사용하고 있는 `metrics-server`가 쿠버네티스에 확장된 API 서버로서 등록돼 있으며, API 서버의 역할을 일부 담당하고 있다. `metrics-server`에 의해 확장된 API는 APIService라고 하는 리소스를 사용해 쿠버네티스에 등록할 수 있는데, 이 APIService 리소스는 이전에 `metrics-server`를 배포할 때 함께 생성됐다.

```bash
$ kubectl apply -f component.yaml
...
apiservice.apiregistration.k8s.io/v1beta1.metrics.k8s.io created
...
```

<img width="747" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/5abd968f-efc0-4444-ab9e-41cfb8e1a2bc">

APIService 리소스는 **새로운 API를 확장해 사용하기 위해선 어떠한 서비스에 접근해야 하는가**를 정의하고 있다. APIService를 생성한 뒤 `metrics.k8s.io`라는 이름의 API를 호출하게 되면 이는 API 서버에서 처리하지 않고, 확장된 API 서버인 `metrics-server`에서 처리하게 된다.

이러한 API 확장 방식을 쿠버네티스에선 API Aggregation이라 한다.

<img width="629" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/73cb7d80-b8f6-4ed5-bdd7-2a1a73c12cf1">

1. APIService 리소스를 통해 `metrics-server`를 확장된 API 서버로서 등록한다.
2. 사용자는 쿠버네티스 API 서버(`kube-apiserver`)에 `metrics.k8s.io` API 요청을 전송한다.
3. 해당 API는 API Aggregation에 의해 `metrics-server`로부터 제공되고 있으므로 쿠버네티스 API 서버는 해당 요청을 `metrics-server`에 전달한다.
4. 사용자는 `metrics-server`가 처리한 API 응답을 반환받는다. 위 경우엔 `metrics-server`가 `/stats/summary`로부터 수집한 노드, 포드의 리소스 사용량 메트릭을 반환받았다.

일반적으론 API Aggregation 기능을 활용하는 경우는 그다지 많지 않지만 `metrics-server`처럼 별도의 외부 데이터 소스로부터 메트릭을 가져다 쓰거나 추가적인 쿠버네티스 기능을 구현해야 할 때는 API Aggregation을 유용하게 사용할 수 있다.

### kube-state-metrics
`kube-state-metrics`는 쿠버네티스 리소스의 상태에 관한 메트릭을 제공하는 에드온이다. 포드의 상태가 Running인지 또는 디플로이먼트의 레플리카 개수가 몇 개인지 등은 리소스 상태에 해당하며 이 애드온을 통해 확인할 수 있다.

### node-exporter
`node-exporter`는 인프라 수준에서의 메트릭을 제공하는 exporter다. 컨테이너 메트릭에 초점을 맞춘 CAdvisor완 달리 `node-exporter`는 **파일 시스템, 네트워크 패킷 등과 같이 호스트 측면의 다양하고 자세한 메트릭을 제공한다.** 가능하다면 쿠버네티스에 배포해두는 것을 추천한다고 한다.

정리하자면,

- `kubelet`에 내장된 CAdvisor: 특정 노드에서 컨테이너 수준의 메트릭을 반환한다. `metrics-server`는 `kubelet`의 `/stats/summary`에 접근해 포드와 리소스 관련 메트릭을 임시로 메모리에 수집하고, 이를 확장된 API로서 제공했다.
- `kube-state-metrics`: 쿠버네티스 리소스의 상태에 관한 메트릭을 반환한다. 별도의 인증 없이 `kube-state-metrics` 서버의 8080 포트와 `/metrics` 경로로 요청해서 메트릭을 확인할 수 있다.
- `node-exporter`: 컨테이너가 아닌 인프라 수준에서의 메트릭을 반환한다. 데몬셋의 형태로 모든 노드에 배포했으며, 9100 포트와 `/metrics` 경로로 요청해서 메트릭을 확인할 수 있다.

이제 프로메테우스와 그라파나를 사용해 모니터링 환경 구축에 관해 나오는데, 이 부분은 책을 통해 확인하도록 하자.