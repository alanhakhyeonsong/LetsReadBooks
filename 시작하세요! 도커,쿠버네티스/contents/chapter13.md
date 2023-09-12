# 13장. 포드를 사용하는 다른 오브젝트들
포드를 사용하는 다른 상위 오브젝트에선 포드의 기능을 그대로 사용할 수 있다. 오브젝트 내에서 포드를 사용할 경우 YAML 파일 등에서 포드 템플릿을 이용해 포드의 기능을 정의할 수 있기 때문이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2712009d-e114-47ce-96ed-13080bd43531)

포드를 사용하는 상위 오브젝트에는 디플로이먼트 외에도 몇 가지가 더 있다. 디플로이먼트만큼 자주 쓰이지는 않지만, 상황에 따라 필요할 수 있는 몇 가지 상위 오브젝트에 대해 알아보자.

## 잡(Jobs)
**잡(Jobs)은 특정 동작을 수행하고 종료해야 하는 작업을 위한 오브젝트다.** 포드를 생성해 원하는 동작을 수행한다는 점에선 디플로이먼트와 같지만, **잡에서 원하는 최종 상태는 '특정 개수의 포드가 실행중인 것'이 아닌 '포드가 실행되어 정상적으로 종료되는 것'이라는 점에서 차이가 있다.** 따라서 잡에선 포드의 컨테이너가 종료 코드로서 0을 반환해 `Completed` 상태가 되는 것을 목표로 한다.

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: job-hello-world
spec:
  template:
    spec:
      restartPolicy: Never
      containers:
      - image: busybox
        args: ["sh", "-c", "echo Hello, World && exit 0"]
        name: job-hello-world
```

`kind`가 `Job`으로 설정됐다는 점을 제외하면 단일 포드를 정의하는 방식과 거의 비슷하다. 단, 잡의 포드가 최종적으로 도달해야 하는 상태가 `Completed`이므로 포드의 `restartPolicy`를 명시적으로 `Never` 또는 `OnFailure`로 지정해 줘야 한다.

이를 통해 잡을 생성한 뒤 잡과 포드의 목록을 확인해보면, 생성된 포드는 'Hello, World'만을 출력하고 빠르게 종료되기 때문에 곧바로 `Completed` 상태가 되고, 잡의 `COMPLETIONS` 항목에는 1/1이라는 문구를 통해 1개의 포드가 정상적으로 수행됐음을 확인할 수 있다.

잡 오브젝트는 사용자의 요청을 처리하는 서버와 같은 애플리케이션의 관점이 아닌, 한 번 수행하고 종료되는 배치(Batch) 작업을 위한 관점에서 생각해보면 잡의 쓰임새를 쉽게 이해할 수 있다.

단, 잡은 동시성을 엄격하게 보장해야 하는 병렬 처리를 위해 사용하는 것이 아니라는 점을 기억해야 한다. 또한 잡의 포드가 실패하면 포드가 `restartPolicy`에 따라 재시작될 수도 있어서 잡이 처리하는 작업은 멱등성을 가지는 것이 좋다.

참고로, 쿠버네티스의 공식 문서에는 YAML 템플릿을 이용해 동일한 잡을 여러 개 생성하거나, Message Queue나 Redis에 작업 큐를 저장해둔 뒤 잡이 작업 큐를 꺼내와 처리하도록 하는 패턴 등을 설명하고 있다.

### 잡의 세부 옵션
실제로 배치 워크로드에서 잡을 사용하려면 다양한 옵션을 함께 사용해야만 효율적으로 작업을 끝마칠 수 있다. 잡에서 자주 사용되는 세부 옵션은 다음과 같다.

- `spec.completions`: 잡이 성공했다 여겨지려면 몇 개의 포드가 성공해야 하는지 설정한다. 기본값은 1.
- `spec.parallelism`: 동시에 생성될 포드의 개수를 설정한다. 기본값은 1.

만약 앞서 예시로 살펴봤던 YAML 파일에서 `spec.completions` 값을 3으로 설정하면 다음과 같이 동작한다.

```yaml
...
  name: job-completions
spec:
  completions: 3
  template:
...
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0a7666fe-2e97-4e39-b34b-3b8cd2e3514c)

포드가 순차적으로 하나씩 생성됐고, 포드가 `Completed` 상태가 되자마자 바로 다음 포드가 실행됐다. `job-completion`이라는 잡의 입장에선 3개의 포드가 정상적으로 종료돼야만 잡이 성공적으로 수행된 것으로 간주하기 때문에 포드를 한 개씩 3번 생성한 것이다.

만약 잡에서 한 번에 포드를 여러 개 생성해 실행하고 싶다면 `parallelism`의 값을 적절히 높여 설정하면 된다.

```yaml
...
  name: job-completions
spec:
  parallelism: 3
...
```

이렇게 잡을 실행해보면 한 번에 3개의 포드가 동시에 생성되고 있음을 확인할 수 있다. 또한 위 두 옵션을 함께 사용하면 잡의 수행 속도를 적절히 조절할 수 있다.

### 크론잡(CronJobs)으로 잡을 주기적으로 실행하기
**크론잡(CronJob)은 잡을 주기적으로 실행하는 쿠버네티스 오브젝트**이다. 크론잡을 사용하면 특정 시간 간격으로 잡을 반복적으로 실행할 수 있기 때문에 데이터 백업이나 이메일 전송 등의 용도로 사용하기에 적합하다.

크론잡은 리눅스에서 흔히 쓰이는 크론의 스케줄 방법을 그대로 사용한다.

```yaml
apiVersion: batch/v1beta1
kind: CronJob
metadata:
  name: cronjob-example
spec:
  schedule: "*/1 * * * *"
  jobTemplate:
    spec:
      template:
        spec:
          restartPolicy: Never
          containers:
          - name: cronjob-example
            image: busybox
            args: ["sh", "-c", "date"]
```

이는 `schedule`에 설정된 주기마다 `jobTemplate`의 설정값을 갖는 잡을 실행한다는 의미다. 위 파일로 크론잡을 생성하면 1분마다 잡이 생성될 것이다.

## 데몬셋(DaemonSets)
**데몬셋(DaemonSets)은 쿠버네티스의 모든 노드에 동일한 포드를 하나씩 생성하는 오브젝트다.** 로깅, 모니터링, 네트워킹 등을 위한 에이전트를 각 노드에 생성해야 할 때 유용하게 사용할 수 있다.

예를 들어, 쿠버네티스 네트워킹을 위한 `kube-proxy` 컴포넌트나 `calico` 등의 네트워크 플러그인은 `kube-system` 네임스페이스에서 데몬셋으로 실행되고 있다. `kube-proxy` 포드나 `calico`는 쿠버네티스의 오버레이 네트워크를 구성할 때 필수적인 요소이므로 기본적으로 모든 노드에서 에이전트처럼 실행된다.

## 스테이트풀셋(StatefulSets)
### 스테이트풀셋 사용하기
쿠버네티스에서 마이크로서비스 구조로 동작하는 애플리케이션은 대부분 상태를 갖지 않는 경우가 많다. 데이터베이스처럼 상태를 갖는 애플리케이션을 쿠버네티스에서 실행하는 것은 매우 복잡하다. 포드 내부의 데이터를 어떻게 관리해야 할지, 상태를 갖는 포드에는 어떻게 접근할 수 있을지 등을 꼼꼼히 고려해야 하기 때문이다.

쿠버네티스가 이에 대한 해결책을 완벽하게 제공하는 것은 아니지만, 스테이트풀셋이라는 쿠버네티스 오브젝트를 통해 어느 정도는 해결할 수 있도록 제공하고 있다. 이는 **상태를 갖는(Stateful) 포드를 관리하기 위한 오브젝트다.**

쿠버네티스에서 상태가 없는 포드를 지칭할 땐 흔히 '가축'에 비유한다. 이름을 지어주지 않으며, 구분하지도 않는다. 언제든 대체될 수 있는 모두 동일해 보이는 개체에 불과하기 때문이다.  
이와 반대로 쿠버네티스에서 상태가 존재하는 포드를 지칭할 때는 '애완동물'에 비유한다. 특별한 이름을 붙여주고 다른 개체와 명확히 구분한다. 상태를 갖는 각 포드는 모두 고유하며, 쉽게 대체될 수 없기 때문이다.

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: statefulset-example
spec:
  serviceName: statefulset-service
  selector:
    matchLabels:
      name: statefulset-example
  replicas: 3
  template:
    metadata:
      labels:
        name: statefulset-example
    spec:
      containers:
      - name: statefulset-example
        image: alicek106/rr-test:echo-hostname
        ports:
        - containerPort: 80
          name: web
---
apiVersion: v1
kind: Service
metadata:
  name: statefulset-service
spec:
  ports:
    - port: 80
      name: web
  clusterIP: None
  selector:
    name: statefulset-example
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/18d3f09c-e70a-468e-bd4c-2e4680f70415)

디플로이먼트에서 생성된 포드는 랜덤한 이름이 붙여지지만, 스테이트풀셋으로부터 생성된 포드들의 이름에는 0, 1, 2, ... 처럼 숫자가 붙어 있다는 사실에 주목하자. 스테이트풀셋에선 이처럼 포드 이름에 붙여지는 숫자를 통해 각 포드를 고유하게 식별한다.

디플로이먼트에서 사용했던 일반적인 서비스를 스테이트풀셋에 사용한다면, 서비스는 기본적으로 라벨 셀렉터가 일치하는 랜덤한 포트를 선택해 트래픽을 전달하기 때문에 스테이트풀셋의 랜덤한 포드들에게 요청이 분산될 것이다. 하지만 이것은 스테이트풀셋이 원하는 동작이 아니다. 각 포드는 고유하게 식별돼야 하며, 포드에 접근할 때도 '랜덤한 포드'가 아닌, '개별 포드'에 접근해야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/83dc2638-49a8-450f-8e64-65fbaaf47118)

이러한 경우에는 일반적인 서비스가 아닌 **헤드리스 서비스(Headless Service)** 를 사용할 수 있다. 이는 서비스의 이름으로 포드의 접근 위치를 알아내기 위해 사용되며, 서비스의 이름과 포드의 이름을 통해 포드에 직접 접근할 수 있다. 앞서 사용했던 YAML 파일에서 `clusterIP` 항목의 값이 `None`으로 돼 있는데, 이것이 헤드리스 서비스라는 의미다. 이 때문에 서비스의 목록에서도 Cluster IP는 출력되지 않는다.

헤드리스 서비스의 이름은 SRV 레코드로 쓰이기 대문에 헤드리스 서비스의 이름을 통해 포드에 접근할 수 있는 IP를 반환할 수 있다. `nslookup` 명령어에 헤드리스 서비스의 이름을 입력하면 접근 가능한 포드의 IP가 출력된다.

`<포드의 이름>.<서비스 이름>`을 통해서도 포드에 접근할 수 있다. 스테이트풀셋에서 포드의 이름은 0부터 시작하는 숫자가 붙기 때문에 쿠버네티스 클러스터 내부에선 아래 그림과 같이 고유한 포드 이름으로 접근할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e319a567-c790-4cb8-9cfd-061922236094)

이러한 개념은 쿠버네티스뿐만 아니라 데브옵스에서도 자주 등장하기 때문에 스테이트풀셋이 의도하는 바를 정확히 알아두는 것이 좋다.

### 스테이트풀셋과 퍼시스턴트 볼륨
앞서 사용했던 예제는 단순히 포드 컨테이너의 호스트 이름을 반환하는 단순한 웹 서버를 생성했다. 하지만 실제로 스테이트풀셋을 사용할 때는 포드 내부에 데이터가 저장되는, 상태가 존재하는 애플리케이션을 배포하는 경우가 대부분일 것이다.

스테이트풀셋도 마찬가지로 퍼시스턴트 볼륨을 포드에 마운트해 데이터를 보관하는 것이 바람직하다. 하지만 스테이트풀셋의 포드가 여러 개라면 포드마다 퍼시스턴트 볼륨 클레임을 생성해줘야 하는 번거로움이 있다. **쿠버네티스는 스테이트풀셋을 생성할 때 포드마다 퍼시스턴트 볼륨 클레임을 자동으로 생성함으로써 다이나믹 프로비저닝 기능을 사용할 수 있도록 지원한다.**

`spec.volume.ClaimTemplates` 항목을 정의하면 사용가능하다.

```yaml
...
          name: web
        volumeMounts:
        - name: webserver-files
          mountPath: /var/www/html/
  volumeClaimTemplates:
  - metadata:
      name: webserver-files
    spec:
      accessModes: ["ReadWriteOnce"]
      storageClassName: generic
      resources:
        requests:
          storage: 1Gi
...
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3b83502c-2411-475c-9034-38a17703319a)

`volumeClaimTemplates`를 사용하면 스테이트풀셋의 각 포드에 대해 퍼시스턴트 볼륨 클레임이 생성된다. 단어가 의미하는 것처럼 포드가 사용할 퍼시스턴트 볼륨 클레임의 템플릿을 정의하는 것이라 생각하면 된다.

단, 스테이트풀셋을 삭제한다 해서 `volumeClaimTemplates`로 인해 생성된 볼륨이 함께 삭제되진 않는다. 여타 이유로 인해 스테이트풀셋이 재생성되거나 포드의 수가 줄어들더라도 기존의 데이터를 안전하게 보존할 수 있어야 하기 때문이다. 따라서 `volumeClaimTemplates`로 생성된 퍼시스턴트 볼륨과 클레임은 직접 삭제해야 한다.