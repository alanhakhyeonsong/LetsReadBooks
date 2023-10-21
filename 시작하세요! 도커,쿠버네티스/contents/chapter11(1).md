# 11장. 애플리케이션 배포를 위한 고급 설정
## 포드의 자원 사용량 제한
쿠버네티스와 같은 컨테이너 오케스트레이션 툴의 가장 큰 장점 중 하나는 여러 대의 서버를 묶어 리소스 풀로 사용할 수 있다는 것이다. **클러스터의 CPU나 메모리 등의 자원이 부족할 때, 필요한 용량 만큼의 서버를 동적으로 추가함으로써 수평적으로 확장할 수 있기 때문이다.** 하지만 서버를 수평적으로 늘리는 스케일 아웃 만큼 중요한 작업이 또 있다.  
**클러스터 내부에서 컴퓨팅 자원 활용률을 늘리는 것이다.**

쿠버네티스는 컴퓨팅 자원을 컨테이너에 할당하기 위한 여러 기능을 제공한다. 이번 장에선 다음과 같은 내용을 살펴본다.

- 포드나 컨테이너에 CPU, 메모리 등의 자원을 할당하는 기본적인 방법
- 쿠버네티스 클러스터 자원의 활용률을 높이기 위한 Overcommit 방법
- 쿠버네티스 오브젝트 `ResourceQuota`, `LimitRange` 사용 방법

### 컨테이너와 포드의 자원 사용량 제한 : Limits
**쿠버네티스는 기본적으로 Docker를 컨테이너 런타임으로 사용하기 때문에 포드를 생성할 때 Docker와 동일한 원리로 CPU, 메모리의 최대 사용량을 제한할 수 있다.** 포드나 디플로이먼트 등을 생성할 때 자원 할당량을 명시적으로 설정하지 않으면 **포드의 컨테이너가 노드의 물리 자원을 모두 사용할 수 있기 때문에 노드의 자원이 모두 고갈되는 상황이 발생할 수도 있다.**

이를 예방할 수 있는 가장 간단한 방법은 **포드 자체에 자원 사용량을 명시적으로 설정하는 것이다.**

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: resource-limit-pod
  labels:
    name: resource-limit-pod
spec:
  containers:
  - name: nginx
    image: nginx:latest
    resources:
        limits:
          memory: "256Mi"
          cpu: "1000m"
```

포드를 정의하는 스펙에 `spec.containers.resources.limits` 항목을 정의했다.

`docker info` 명령어로 Docker 호스트의 가용 자원을 확인할 수 있던 것처럼 쿠버네티스에서도 `kubectl describe node` 명령어로 워커 노드의 가용 자원을 간단하게 확인할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/4b04d982-cdc5-4d4d-bc05-d9d26c58c34e)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ae509624-9a26-480a-b670-314d42d306f8)

`Allocated resources` 항목에서 해당 노드에서 실행 중인 포드들의 자원 할당량을 모두 더한 값이 출력된다. 즉, 방금 생성한 `resource-limit-pod`라는 이름의 포드 및 다른 시스템 컴포넌트의 자원 할당량의 합계를 확인할 수 있다.

### 컨테이너와 포드의 자원 사용량 제한하기 : Requests
**`Limits`는 해당 포드의 컨테이너가 최대로 사용할 수 있는 자원의 상한선을 의미한다.** 앞선 출력 내용 중 `Requests`라는 단어가 등장했다.

**쿠버네티스의 자원 관리에서 `Requests`는 '적어도 이 만큼의 자원은 컨테이너에게 보장돼야 한다'는 것을 의미한다. 이는 쿠버네티스에서 자원의 Overcommit을 가능하게 만드는 기능이다.**

**Overcommit은 한정된 컴퓨팅 자원을 효율적으로 사용하기 위한 방법으로, 사용할 수 있는 자원보다 더 많은 양을 가상 머신이나 컨테이너에 할당함으로써 전체 자원의 사용률을 높이는 방법이다.**

1GB의 메모리를 탑재한 하나의 서버에서 아래와 같이 A와 B 두 개의 컨테이너를 생성했으며, 각 컨테이너엔 500MB 메모리를 할당했다 가정해보자.

<img width="658" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/74b2f0fe-ccc1-4691-b050-5b7f8ea9d74c">

이러한 정적 방식의 자원 제한은 한 가지 단점이 있다. **상황에 따라선 메모리 사용률이 낮은 컨테이너에도 불필요하게 많은 메모리를 할당할 수도 있다.** 컨테이너의 자원 제한을 정적으로 설정했기 때문에 유휴 자원을 제대로 활용하지 못해 전체 자원 사용률을 낮추는 요인이 된다.

이러한 문제를 해결하는 가장 좋은 방법은 애초에 컨테이너를 생성할 때 자원 할당량을 적절히 결정하는 것이겠지만, 컨테이너가 실제로 얼마나 자원을 활용할지 예측하기 어려운 경우도 빈번히 존재한다. 이를 위해 쿠버네티스에선 오버커밋을 통해 실제 물리 자원보다 더 많은 양의 자원을 할당하는 기능을 제공한다.

<img width="646" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/facfc381-a1ad-4b96-9a6f-5c5dff6da933">

실제 물리 자원은 1GB로 한정돼 있기 때문에 750MB의 메모리를 할당한 컨테이너를 두 개 생성한다해서 실제로 1.5GB를 사용할 수 있는 것은 아니다. 하지만 위 그림과 같이 A 컨테이너의 메모리 사용률이 낮다면 컨테이너 B는 A로부터 남는 메모리 자원을 기회적으로 사용할 수 있게 된다. **쿠버네티스에선 이러한 자원 제한 설정을 `Limits`라고 부른다.**

여기서 한 가지 문제가 생길 수 있는데, 컨테이너 A가 500MB를 사용하고 있을 때 컨테이너 B가 750MB를 사용하려 시도한다면 메모리 충돌이 일어나 두 컨테이너의 프로세스는 비정상적인 에러를 출력하며 종료될 수 있다. 이런 상황을 방지하기 위해 각 컨테이너가 **사용을 보장받을 수 있는 경계선**을 정한다. 이러한 경계선을 쿠버네티스에선 **`Requests`**라고 부른다. **컨테이너가 최소한으로 보장받아야 하는 자원의 양을 뜻한다.**

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: resource-limit-with-request-pod
  labels:
    name: resource-limit-with-request-pod
spec:
  containers:
  - name: nginx
    image: nginx:latest
    resources:
      limits:
        memory: "256Mi"
        cpu: "1000m"
      requests:
        memory: "128Mi"
        cpu: "500m"
```

`requests`에서 128Mi를, `limits`에서 256Mi를 설정했기 때문에 **최소한 128Mi의 메모리 사용은 보장되지만, 유휴 메모리 자원이 있다면 최대 256Mi까지 사용할 수 있다**라고 이해하면 된다. CPU 또한 같은 원리로 해석하면 최소한 0.5 CPU만큼은 사용할 수 있지만, 유휴 CPU 자원이 있다면 최대 1 CPU까지 사용할 수 있다라고 말할 수 있다.

**단, `requests`는 컨테이너가 보장받아야 하는 최소한의 자원을 뜻하기 때문에 노드의 총 자원의 크기보다 더 많은 양의 `requests`를 할당할 수는 없다.** 따라서 쿠버네티스의 스케줄러는 포드의 `requests`만큼 여유가 있는 노드를 선택해 포드를 생성한다. 즉, 포드를 할당할 때 사용되는 자원 할당 기준은 `Limits`가 아닌 `requests`이다.

<img width="667" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/825034b8-5970-4ce4-b3e8-c43287d1e93b">

`Requests`의 값을 낮게 설정해 포드를 생성하면 포드가 스케줄링 되어 노드에 할당될 확률은 높아지겠지만, 포드가 사용을 보장받을 수 있는 자원의 양은 적어질 것이다.

### CPU 자원 사용량의 제한 원리
<img width="666" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f4cb8d13-c121-4900-8351-c1d2890b564e">

컨테이너가 하나만 존재하는 상황이라면 `Requests`와 상관없이 CPU의 `Limits`의 값만큼 사용할 수 있을 것이다. 만약 두 개의 컨테이너가 CPU 자원을 최대로 사용하는 상태라면 어떨까?

<img width="665" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ffc21fae-9c94-4b0c-a439-017f1052c30e">

위 그림에서 두 컨테이너의 `Requests` 비율은 500m로 동일하기 때문에 CPU 자원이 포화 상태일 때는 각 컨테이너가 정확히 1:1 만큼의 비율로 CPU를 나눠서 사용할 것이다. 따라서 `Requests`의 값인 500m는 최종적으로 각 컨테이너에 보장돼야 하는 최소한의 CPU 자원을 나타낸다고 이해할 수 있다. 또한 위 그림에선 전체 CPU 자원인 1000m만큼의 `Requests`를 모두 소진했기 때문에 새로운 `Requests`를 가지는 컨테이너를 새롭게 할당하는 것은 불가능하다.

`Requests`보다 더 많은 CPU 자원을 사용하려 할 때, 자원의 경합이 발생한다.

<img width="666" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d9b4ea5a-5352-41b0-aba7-51b0a2bc17ee">

위 상황에선 컨테이너 A에는 CPU 스로틀이 발생한다.

`Requests`에 할당되지 않은 여유 CPU 자원이 노드에 남아있는 경우 `Limits`만큼의 CPU를 사용할 수 있는 상황이라면 컨테이너는 `Limits`까지 CPU를 사용할 수 있다.

<img width="672" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b41a506d-d0c8-4975-b114-881bf03ac14e">

두 컨테이너가 동시에 CPU를 최대한 사용하려 시도한다면 남은 자원 또한 `Requests`의 비율에 맞춰 각 컨테이너가 나눠 사용한다.

### QoS 클래스와 메모리 자원 사용량 제한 원리
CPU 사용량에 경합이 발생하면 일시적으로 컨테이너 내부의 프로세스에 CPU 스로틀이 걸릴 뿐, 컨테이너 자체에는 큰 문제가 발생하지 않는다.

**그렇지만 메모리 사용량에 경합이 발생하면 문제가 심각해질 수 있다.** 프로세스의 메모리는 이미 데이터가 메모리에 적재되어 있기 때문에 CPU와 달리 메모리는 압축 불가능한 자원으로 취급된다. 이런 상황에서 쿠버네티스는 **가용 메모리를 확보하기 위해 우선순위가 낮은 포드 또는 프로세스를 강제로 종료하도록 설계돼 있다.** 강제로 종료된 포드는 다른 노드로 옮겨가게 되는데, 쿠버네티스에선 이를 퇴거(Eviction)이라 한다.

<img width="697" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/686b5cd8-c11f-496d-8fa9-d0565fa81857">

여기서 가장 중요한 부분은 **'노드에 메모리 자원이 부족해지면 어떤 포드나 프로세스가 먼저 종료돼야 하는가'** 가 된다. 이를 위해 쿠버네티스는 포드의 컨테이너에 설정된 `Limits`와 `Requests`의 값에 따라 내부적으로 우선순위를 계산한다. 또한 쿠버네티스는 포드의 우선순위를 구분하기 위해 3가지 종류의 QoS(Quality of Service) 클래스를 명시적으로 포드에 설정한다.

#### 쿠버네티스에서의 메모리 부족과 OOM(Out Of Memory)
쿠버네티스의 노드엔 각종 노드의 이상 상태 정보를 의미하는 `Conditions`라는 값이 존재한다. `kubectl describe nodes` 명령어로 확인 가능하다.

- `MemoryPressure`
- `DiskPressure`

`kubelet`은 노드의 자원 상태를 주기적으로 확인하면서 `Conditions`의 `MemoryPressure`, `DiskPressure` 등의 값을 갱신한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fb42a70b-2215-4602-8072-00c4fca58cf3)

만약 노드의 가용 메모리가 부족해지면 `MemoryPressure` 상태 값이 `True`로 바뀐다.

`MemoryPressure`는 기본적으로 노드의 가용 메모리가 100Mi 이하일 때 발생하도록 `kubelet`에 설정됐다. `MemoryPressure`가 발생하면 쿠버네티스는 해당 노드에서 실행 중이던 모든 포드에 대해 순위를 매긴 다음, 가장 우선순위가 낮은 포드를 다른 노드로 퇴거시킨다. 또한 `MemoryPressure`의 값이 `True`인 노드에는 더 이상 포드를 할당하지 않는다.

만약 `kubelet`이 `MemoryPressure` 상태를 감지하기 전에 급작스럽게 메모리 사용량이 많아질 경우, 리눅스 시스템의 OOM Killer라는 기능이 우선순위 점수가 낮은 컨테이너의 프로세스를 강제로 종료해 사용 가능한 메모리를 확보할 수도 있다.

#### QoS 클래스의 종류 - Guaranteed 클래스
**`Guaranteed` 클래스는 포드의 컨테이너에 설정된 `Limits`와 `Requests` 값이 완전히 동일할 때 부여되는 클래스다.** 

```yaml
...
containers:
  - name: nginx
    image: nginx:latest
    resources:
      limits:
        memory: "256Mi"
        cpu: "1000m"
```
이전 포드를 생성할 때 `limits`만 명시했음에도 `Guaranteed` 포드로 분류됐다. 이는 `Requests` 없이 `Limits`만 정의하면 `Requests`의 값 또한 `Limits`로 동일하게 설정되기 때문이다.

이 클래스의 포드를 명시적으로 생성하고 싶다면 YAML 파일에서 `Limits`와 `Requests`를 동일하게 설정한 뒤 사용해도 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f59082ed-34ec-4908-a6cb-79bc52910c28)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/5ba557d3-ec1b-40d9-90d7-703093504d36)

#### QoS 클래스의 종류 - BestEffort 클래스
**`BestEffort` 클래스는 `Requests`와 `Limits`를 아예 설정하지 않은 포드에 설정되는 클래스다. 즉, 포드의 스펙을 정의하는 YAML 파일에서 `resources` 항목을 아예 사용하지 않으면 자동으로 이 클래스로 분류된다.**

이 클래스의 포드는 `Limits` 값을 설정하지 않았기 때문에 노드에 유휴 자원이 있다면 제한 없이 모든 자원을 사용할 수 있다. 그렇지만 `Requests` 또한 설정하지 않았기 때문에 포드는 사용을 보장받을 수 있는 자원이 존재하지 않는다. 따라서 때에 따라선 노드에 존재하는 모든 자원을 사용할 수도 있지만, 자원을 전혀 사용하지 못할 수도 있다.

#### QoS 클래스의 종류 - Burstable 클래스
**`Burstable` 클래스는 `Requests`와 `Limits`가 설정돼 있지만, `Limits`의 값이 `Requests`보다 큰 포드를 의미한다.** 따라서 이 클래스의 포드는 `Requests`에 지정된 자원만큼 사용을 보장받을 수 있지만, 상황에 따라선 `Limits`까지 자원을 사용할 수도 있다. 필요에 따라 순간적으로 자원의 한계를 확장해 사용할 수 있는 포드라 생각하며 ㄴ된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a02cbcec-1ef0-41b7-b274-c81e16390d1c)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c39984e8-e768-4e72-ac1e-021f349bd0a8)

#### QoS 클래스와 메모리 부족
포드가 다른 노드로 퇴거되면 단순히 다른 노드에서 포드가 다시 생성될 뿐이지만, OOM Killer에 의해 포드 컨테이너의 프로세스가 종료되면 해당 컨테이너는 포드의 재시작 정책에 의해 다시 시작된다.

기본적으로 포드의 우선순위는 `Guaranteed` > `Burstable` > `BestEffort` 순이다. 포드가 메모리를 많이 사용하면 할수록 우선순위가 낮아지기 때문에 이 순서가 절대적인 것은 아니다.

### ResourceQuota와 LimitRange
