# 10장. 보안을 위한 인증과 인가 - ServiceAccount와 RBAC
쿠버네티스는 보안 측면에서도 다양한 기능을 제공하고 있는데, 그중 가장 자주 사용되는 것은 RBAC(Role Based Access Control)를 기반으로 하는 서비스 어카운트(Service Account)라는 기능이다.

**서비스 어카운트는 사용자 또는 애플리케이션 하나에 해당하며, RBAC라는 기능을 통해 특정 명령을 실행할 수 있는 권한을 서비스 어카운트에 부여한다.** 권한을 부여받은 서비스 어카운트는 해당 권한에 해당하는 기능만 사용할 수 있게 된다.

<img width="544" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2b75ac31-ee28-4b81-9e71-134ba8eea98d">

리눅스에서 root 유저와 일반 유저를 나누는 것처럼 쿠버네티스에서도 유사하게 사용할 수 있다 생각하면 된다. 쿠버네티스의 API에 접근하는 애플리케이션을 운영 환경에 배포하거나, 여러 명의 사용자가 동시에 쿠버네티스를 사용해야 한다면, 최상위 권한을 사용하지 않는 것이 좋다. 사용자에게 필요한 권한만을 최소한으로 부여함으로써 실행할 수 있는 기능을 제한하는 것이 바람직하다.

## 쿠버네티스의 권한 인증 과정
쿠버네티스는 `kube-apiserver`, `kube-controller`, `kube-scheduler`, `etcd` 등과 같은 컴포넌트들로 구성되어 있다.

`kubectl` 명령어를 사용해 쿠버네티스의 기능을 실행하면 내부에선 다음과 같은 절차를 걸쳐 실제 기능을 실행한다.

<img width="681" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/431fa07a-60b0-4207-8a14-33a19074df69">

- `kubectl` 명령어는 쿠버네티스 API 서버의 HTTP 핸들러에 요청을 전송
- API 서버는 해당 클라이언트가 쿠버네티스의 사용자가 맞는지, 해당 기능을 실행할 권한이 있는지 확인
- 어드미션 컨트롤러 단계를 거친 뒤 요청받은 기능을 수행한다.

지금까지 `kubectl` 명령어를 사용할 때는 인증을 위한 별도의 절차를 거치지 않았는데, 사실 이는 **설치 도구를 이용해 쿠버네티스를 설치할 때 설치 도구가 자동으로 `kubectl`이 관리자 권한을 갖도록 설정해 두기 때문**이다. 해당 설정은 `~/.kube/config`라는 파일에서 확인할 수 있다.

<img width="362" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/66bdb4b6-6daa-4d0d-bc08-67a39bd13ab3">

`kubectl`을 사용할 땐 기본적으로 `~/.kube/config` 파일에 저장된 설정을 읽어 들여 쿠버네티스 클러스터를 제어한다. 이 파일에 저장된 내용 중 users라는 항목엔 인증을 위한 데이터가 설정돼 있다. `client-certificate-data`와 `client-key-data`에 설정된 데이터는 base64로 인코딩된 인증서인데, 이 키 쌍은 쿠버네티스에서 최고 권한을 갖는다. 그렇기에 지금까지 별도의 설정 없이 모든 명령어를 사용할 수 있었던 것이다.

쿠버네티스에선 인증서 키 쌍을 사용해 API 서버에 인증하는 방법 외에 여러 가지가 있다. 그중 하나가 서비스 어카운트다.

## 서비스 어카운트와 롤(Role), 클러스터 롤(Cluster Role)
**서비스 어카운트는 체계적으로 권한을 관리하기 위한 쿠버네티스 오브젝트다.** 서비스 어카운트는 한 명의 사용자나 애플리케이션에 해당한다 생각하면 이해하기 쉽다. 서비스 어카운트는 **네임스페이스에 속하는 오브젝트로, `serviceaccount` 또는 `sa`라는 이름으로 사용할 수 있다.**

서비스 어카운트를 생성하지 않았더라도 각 네임스페이스에는 기본적으로 `default`라는 이름의 서비스 어카운트가 존재한다. 서비스 어카운트는 `kubectl create`나 `delete`를 통해 간단하게 생성 또는 삭제할 수 있다.

서비스 어카운트를 생성한 뒤 여기에 적절한 권한을 부여해야만 쿠버네티스의 기능을 제대로 사용할 수 있다. 권한을 부여하는 방법은 크게 두 가지로 롤과 클러스터 롤을 이용해 권한을 설정할 수 있다.

<img width="558" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a9df558c-ffe6-43b9-ad79-39033dc0bee3">

롤과 클러스터 롤은 부여할 권한이 무엇인지를 나타내는 쿠버네티스 오브젝트다. 단, 롤은 네임스페이스에 속하는 오브젝트이므로 디플로이먼트나 서비스처럼 네임스페이스에 속하는 오브젝트들에 대한 권한을 정의할 때 쓰인다. 클러스터 롤은 클러스터 단위의 권한을 정의할 때 사용한다. 또한 네임스페이스에 속하지 않는 오브젝트뿐만 아니라 클러스터 전반에 걸친 기능을 사용하기 위해서도 클러스터 롤을 정의할 수 있으며, 여러 네임스페이스에서 반복적으로 사용되는 권한을 클러스터 롤로 만들어 재사용하는 것도 가능하다.

먼저 롤을 직접 작성해보자.

```yaml
# service-reader-role.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: default
  name: service-reader
rules:
- apiGroups: [""]         # 대상이 될 오브젝트의 API 그룹
  resources: ["services"] # 대상이 될 오브젝트의 이름
  verbs: ["get", "list"]  # 어떠한 동작을 허용할 것인지 명시
```

- `apiGroups`: 어떠한 API 그룹에 속하는 오브젝트에 대해 권한을 지정할지 설정한다. API 그룹은 쿠버네티스 오브젝트가 가지는 목적에 따라 분류되는 일종의 카테고리다. `kubectl api-resources`를 통해 확인할 수 있다.
- `resources`: 어떠한 쿠버네티스 오브젝트에 대해 권한을 정의할 것인지 입력한다.
- `verbs`: 이 롤을 부여받은 대상이 `resources`에 지정된 오브젝트들에 대해 어떤 동작을 수행할 수 있는지 정의한다.

이 롤을 특정 대상에게 부여하려면 **롤 바인딩(RoleBinding)이라는 오브젝트를 통해 특정 대상과 롤을 연결해야 한다.**

```yaml
# rolebinding-service-reader.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: service-reader-rolebinding
  namespace: default
subjects:
- kind: ServiceAccount # 권한을 부여할 대상이 ServiceAccount
  name: alicek106      # alicek106이라는 이름의 서비스 어카운트에 권한 부여
  namespace: default
roleRef:
  kind: Role           # Role에 정의된 권한을 부여
  name: service-reader # service-reader라는 이름의 Role을 대상에 연결
  apiGroup: rbac.authorization.k8s.io
```

**롤 바인딩과 롤, 서비스 어카운트는 모두 1:1 관계가 아니라는 점에 유의해야 한다.** 하나의 롤은 여러 개의 롤 바인딩에 의해 참조될 수도 있고, 하나의 서비스 어카운트는 여러 개의 롤 바인딩에 의해 권한을 부여받을 수도 있다. 즉, **롤은 권한을 부여하기 위한 일종의 템플릿과 같은 역할을, 롤 바인딩은 롤과 서비스 어카운트를 연결하기 위한 중간 다리 역할을 하는 셈이다.**

### 롤 vs. 클러스터 롤
- 롤과 롤 바인딩은 네임스페이스에 한정되는 오브젝트다.
- 롤은 포드, 서비스, 디플로이먼트 등과 같이 네임스페이스에 한정된 오브젝트에 대한 권한을 정의하기 위해 사용할 수 있다.
- 클러스터 롤은 클러스터 단위의 리소스에 대한 권한을 정의하기 위해 사용한다.
  - 노드, 퍼시스턴트 볼륨 등 네임스페이스에 종속되지 않는 오브젝트들 포함

롤을 정의하는 yaml 파일처럼 작성하면 된다. 다만, `kind: ClusterRole`만 다르다.

### 여러 개의 클러스터 롤을 조합해서 사용하기
자주 사용되는 클러스터 롤이 있다면 다른 클러스터 롤에 포함시켜 재사용할 수 있는데, 이를 클러스터 롤 애그리게이션이라 한다.

```yaml
# clusterrole-aggregation.yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: parent-clusterrole
  labels:
    rbac.authorization.k8s.io/aggregation-to-child-clusterrole: "true"
rules:
- apiGroups: [""]
  resourecs: ["nodes"]
  verbs: ["get", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: child-clusterrole
aggregationRule:
  clusterRoleSelectors:
  - matchLabels:
      rbac.authorization.k8s.io/aggregation-to-child-clusterrole: "true"
rules: [] # 어떠한 권한도 정의하지 않음
```

클러스터 롤에 포함시키고자 하는 다른 클러스터 롤을 `matchLabels`의 라벨 셀렉터로 선택하면 하위 클러스터 롤에 포함돼 있는 권한을 그대로 부여받을 수 있다.