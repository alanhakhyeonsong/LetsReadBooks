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

클러스토 롤 애그리게이션을 사용하면 여러 개의 클러스터 롤 권한을 하나의 클러스토 롤에 합쳐서 사용할 수도 있으며, 여러 단계의 클러스터 롤 권한 상속 구조를 만들 수도 있다. 자동으로 생성돼 있는 admin, edit, view라는 이름의 클러스터 롤의 내용을 확인해보면 view → edit → admin 순으로 권한이 전파되는 것을 알 수 있다.

```bash
$ kubectl get clusterrole view -o yaml | grep labels -F2
labels:
    kubernetes.io/bootstrapping: rbac-defaults
    rbac.authorization.k8s.io/aggregate-to-edit: "true"

$ kubectl get clusterrole edit -o yaml | grep -F3 aggregationRule
aggregationRule:
  clusterRoleSelectors:
  - matchLabels:
      rbac.authorization.k8s.io/aggregate-to-edit: "true"

$ kubectl get clusterrole edit -o yaml | grep -F2 labels
labels:
    kubernetes.io/bootstrapping: rbac-defaults
    rbac.authorization.k8s.io/aggregate-to-admin: "true"

$ kubectl get clusterrole admin -o yaml | grep -F4 aggregation
aggregationRule:
  clusterRoleSelectors:
  - matchLabels:
      rbac.authorization.k8s.io/aggregate-to-admin: "true"
```

이 경우엔 view 클러스터 롤에 권한을 부여하면 자동적으로 admin에도 권한이 적용된다.

## 쿠버네티스 API 서버에 접근
### 서비스 어카운트의 시크릿을 이용해 쿠버네티스 API 서버에 접근
우리의 애플리케이션이 쿠버네티스 API를 사용해야 한다면 일반적으로 `kubectl`이 아닌 다른 방법으로 API 서버에 접근할 것이다.

도커 데몬의 실행 옵션에 `-H` 옵션을 추가함으로써 REST API를 사용했던 것처럼, 쿠버네티스의 API 서버도 HTTP 요청을 통해 쿠버네티스의 기능을 사용할 수 있도록 REST API를 제공하고 있다. 쿠버네티스의 REST API에 접근하기 위한 엔드포인트는 자동으로 개방되기 때문에 별도의 설정을 하지 않아도 API 서버에 접근할 수 있다.

단, 쿠버네티스 API 서버는 기본적으로 HTTPS 요청만 처리하도록 설정돼 있으며, 기본적으로 보안 연결을 위해 스스로 사인한 인증서를 사용한다는 점에 유의하자.

```bash
$ curl https://localhost:6443 -k
{
  "kind": "Status",
  "apiVersion": "v1",
  "metadata": {},
  "status": "Failure",
  "message": "Unauthorized",
  "reason": "Unauthorized",
  "code": 401
}
```

쿠버네티스의 API 서버로 요청이 전송됐지만 401 에러와 함께 API 요청이 실패했다. 따라서 API 서버에 접근하려면 별도의 인증 정보를 HTTP 페이로드에 포함시켜 REST API 요청을 전송해야 한다. 이를 위해 쿠버네티스는 서비스 어카운트를 위한 인증 정보를 시크릿에 저장한다. 서비스 어카운트를 생성하면 이에 대응하는 시크릿이 자동으로 생성되며, 해당 시크릿은 서비스 어카운트를 증명하기 위한 수단으로 사용된다. 시크릿의 목록을 출력해 보면 서비스 어카운트의 이름으로 시작하는 시크릿이 존재할 것이다.

```bash
$ kubectl get secrets
NAME                     TYPE                                   DATA  AGE
alicek106-token-nrzgb    kubernetes.io/service-account-token    3     5h49m
defalut-token-xdlvn      kubernetes.io/service-account-token    3     4d2h
```

`kubectl describe` 명령어를 이용해 서비스 어카운트의 자세한 정보를 조회해보면 어떠한 시크릿이 서비스 어카운트에 연결돼 있는지 확인할 수 있다.

```bash
$ kubectl describe sa alicek106
Name:               alicek106
Namespace:          default
Labels:             <none>
Annotations:        <none>
Image pull secrets: <none>
Mountable secrets:  alicek106-token-nrzgb
Tokens:             alicek106-token-nrzgb
Events:             <none>
```

서비스 어카운트와 연결된 시크릿에는 `ca.crt, namespace, token` 총 3개의 데이터가 저장돼 있다. 이 중 `ca.crt`는 쿠버네티스 클러스터의 공개 인증서를, `namespace`는 해당 서비스 어카운트가 존재하는 네임스페이스를 저장하고 있다.

```bash
$ kubectl describe secret alicek106-token-nrzgb
...
Data
====
ca.crt:    1025 bytes
namespace: 7 bytes
token:     eyJhbGci0iJ...
```

`token` 데이터는 쿠버네티스 API 서버와의 JWT 인증에 사용된다. 따라서 API 서버의 REST API 엔드포인트로 요청을 보낼 때 `token`의 데이터를 함께 담아서 보내면 인증할 수 있다.

<img width="857" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a552fa60-1281-46bf-aee6-f287705a86a2">

```bash
# alicek106-token-nrzgb 대신 우리의 시크릿 이름을 사용한다.
$ export secret_name=alicek106-token-nrzgb
$ export decoded_token=$(kubectl get secret $secret_name -o jsonpath='{.data.token}' | base64 -d)
# Mac OS X에서 명령어를 사용하고 있다면 base64 -D 명령어를 사용한다.

$ echo $decoded_token
eyJhbGci0iJSUzI1...

$ curl https://localhost:6443/apis --header "Authorization: Bearer $decoded_token" -k
{
  "kind": "APIGroupList",
  "apiVersion": "v1",
  "groups": [
    {
      "name": "apiregistration.k8s.io",
      "versions": [
        { ... }
      ], ...
    }
  ], ...
}
```

디코드된 token 데이터를 HTTP 페이로드의 Bearer 헤더에 담아 API 요청을 보내 보면 정상적으로 응답이 반환되는 것을 확인할 수 있다.

`kubectl`에서 사용할 수 있는 기능은 모두 REST API에서도 동일하게 사용할 수 있다. 예를 들어 `/api/v1/namespace/default/services` 경로로 요청을 보내면 `default` 네임스페이스에 존재하는 서비스의 목록을 가져올 수 있다. 따라서 이 API 경로는 `kubectl get services -n default` 명령어와 같은 기능을 하는 셈이다.

```bash
$ curl https://localhost:6443/api/v1/namespace/default/services -k \
--header "Authorization: Bearer $decoded_token"
```

하지만 API 서버로의 REST 요청 또한 롤 또는 클러스터 롤을 통해 서비스 어카운트에 권한을 부여하지 않으면 접근이 불가능하다. 위의 예시에선 `alicek106` 서비스 어카운트에 `default` 네임스페이스에서 서비스의 목록을 조회할 수 잇는 롤 바인딩을 미리 생성해뒀기 때문에 정상적으로 접근할 수 있었던 것이다.

### 클러스터 내부에서 kubernetes 서비스를 통해 API 서버에 접근
쿠버네티스 클러스터 내부에서 실행되는 애플리케이션은 어떻게 API 서버에 접근하고 인증을 수행할까?

예를 들어, Nginx 인그레스 컨트롤러는 인그레스의 생성을 동적으로 감지해 Nginx의 라우팅 규칙을 업데이트했다. 이를 위해 Nginx 인그레스 컨트롤러는 인그레스 규칙이 생성, 삭제될 때마다 알림을 받을 수 있도록 쿠버네티스 API 서버에 Watch API를 걸어둬야 하며, 해당 API를 상요하기 위한 적절한 권한을 부여받아야 한다.

즉, 포드 내부에서도 쿠버네티스 API 서버에 접근하기 위한 방법이 필요할 뿐만 아니라, 포드를 위한 권한 인증도 수행할 수 있어야 한다.

이를 위해 쿠버네티스는 클러스터 내부에서 API 서버에 접근할 수 있는 서비스 리소스를 미리 생성해 놓는다. 우리가 서비스의 목록을 조회했을 때 기본적으로 존재하고 있던 `kubernetes`라는 이름의 서비스가 바로 그것이다.

```bash
$ kubectl get svc
NAME        TYPE        CLUSTER-IP   EXTERNAL-IP  PORT(S)  AGE
kubernetes  ClusterIP   10.96.0.1    <none>       443/TCP  119d
```

쿠버네티스 클러스터 내부에서 실행 중인 포드는 default 네임스페이스의 `kubernetes` 서비스를 통해 API 서버에 접근할 수 있다. 따라서 포드는 `kubernetes.default.svc`라는 DNS 이름을 통해 쿠버네티스 API를 사용할 수 있다.

<img width="503" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6ab49b14-f1ff-49d3-84db-51b86e63f383">

하지만, 포드 내부에서 `kubernetes`라는 이름의 서비스에 접근한다 해서 특별한 권한이 따로 주어지는 것은 아니다. 이전 API 서버에 접근했던 방식과 동일하게 서비스 어카운트에 부여되는 시크릿의 토큰을 HTTP 요청에 담아 해당 서비스에 전달해야만 인증/인가를 진행할 수 있다.

한 가지 알아둬야 할 점은, **쿠버네티스는 포드를 생성할 때 자동으로 서비스 어카운트의 시크릿을 포드 내부에 마운트한다는 것이다.** 따라서 포드 내부에서 API 서버에 접근하기 위해 시크릿의 데이터를 일부러 포드 내부로 가져올 필요는 없다. 지금까지 생성해 왔던 디플로이먼트나 포드도 모두 서비스 어카운트의 시크릿을 자동으로 내부에 마운트하고 있다.

### 쿠버네티스 SDK를 이용해 포드 내부에서 API 서버에 접근
포드 내부에서 실행되는 애플리케이션이라면 특정 언어로 바인딩된 쿠버네티스 SDK를 활용하는 프로그래밍 방식을 더 많이 사용할 것이다.

서비스 어카운트의 시크릿과 쿠버네티스 SDK를 이용해 API 서버에 접근하는 흐름은 다음과 같다.

<img width="657" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/94b292a7-97b0-49cf-8fa5-a09ef6db0aa3">

1. 롤과 롤 바인딩을 통해 특정 서비스 어카운트에 권한이 부여돼 있어야 한다. 서비스 어카운트가 존재하지 않는다면 새롭게 생성한다.
2. YAML 파일에 `serviceAccountName` 항목을 명시적으로 지정해 포드를 생성한다.
3. 포드 내부에 마운트된 서비스 어카운트의 시크릿을 확인한다.
4. 포드 내부에서 쿠버네티스의 API를 사용할 수 있는 간단한 코드를 작성한다.

## 서비스 어카운트에 이미지 레지스트리 접근을 위한 시크릿 설정
이전에 시크릿을 다뤘을 때 `docker-registry` 타입의 시크릿을 사용한 적이 있다. 이 타입의 시크릿은 도커 이미지 레지스트리에 접근하기 위해 사용하는 싴릣으로, 디플로이먼트 등과 같이 포드의 스펙을 정의하는 YAML에서 `imagePullSecrets` 항목에 명시해 사용할 수 있었다.

서비스 어카운트를 이용하면 비공개 레지스트리 접근을 위한 시크릿을 서비스 어카운트 자체에 설정할 수 있다. 어떤 시크릿을 사용할지는 서비스 어카운트의 정보에 저장돼 있기 때문이다.

예를 들어, `registry-auth`라는 이름의 시크릿이 존재한다면 아래와 같이 서비스 어카운트를 정의하는 YAML 파일을 작성한다.

```yaml
# sa-reg-auth.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: reg-auth-alicek106
  namespace: default
imagePullSecrets:
- name: registry-auth
```

위 YAML 파일로 서비스 어카운트를 생성하면 서비스 어카운트에 Image pull secrets 정보가 추가된다. 앞으로 포드를 생성하는 YAML 파일에서 `serviceAccountName` 항목에 `reg-auth-alicek106` 서비스 어카운트를 지정해 생성하면 자동으로 `imagePullSecrets` 항목이 포드 스펙에 추가된다.

## kubeconfing 파일에 서비스 어카운트 인증 정보 설정
여러 명의 개발자가 `kubectl` 명령어를 사용해야 한다면 서비스 어카운트를 이용해 적절한 권한을 조절하는 것이 바람직할 것이다. 이를 위해 권한이 제한된 서비스 어카운트를 통해 `kubectl` 명령어를 사용하도록 `kubeconfig`에서 설정할 수 있다.

즉, 서비스 어카운트와 연결된 시크릿의 `token` 데이터를 `kubeconfig`에 명시함으로써 `kubectl` 명령어의 권한을 제한할 수 있다.

<img width="609" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/99eb1d39-b377-4a76-8e7a-43a0904d7674">

`kubeconfig` 파일은 일반적으로 `~/.kube/config` 경로에 있으며, 필요에 따라 `KUBECONFIG` 셸 환경 변수로 경로를 직접 설정할 수 있다. `kubectl` 명령어로 쿠버네티스의 기능을 사용하면 기본적으로 `kubeconfig`의 설정 정보에서 API 서버의 주소와 사용자 인증 정보를 로드한다. 이 파일은 크게 3가지 파트로 나누어져 있다.

- `clusters`: `kubectl`이 사용할 쿠버네티스 API 서버의 접속 정보 목록이다. 기본적으로는 클러스터 스스로의 접속 정보 1개만 존재하지만, 필요하다면 원격의 쿠버네티스 API 서버의 주소를 추가해 사용할 수도 있다.
- `users`: 쿠버네티스 API 서버에 접속하기 위한 사용자 인증 정보 목록이다. 이 인증 정보만으로는 아직 어떠한 클러스터에 대해 사용할 것인지 알 수 없다.
- `contexts`: `clusters` 항목과 `users` 항목에 정의된 값을 조합해 최종적으로 사용할 쿠버네티스 클러스터의 정보(컨텍스트)를 설정한다.

<img width="336" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/5e8c6061-4b2e-452c-b3d1-fdf1cb332d49">

이를 잘 활용하면 로컬 개발 환경의 쿠버네티스 컨텍스트, AWS 운영 환경의 쿠버네티스 컨텍스트 등 여러 개의 쿠버네티스 클러스터를 유동적으로 선택해 `kubectl` 명령어를 사용하는 것도 가능하다.

## 유저(User)와 그룹(Group)의 개념
쿠버네티스에선 서비스 어카운트 외에도 유저와 그룹이라는 개념이 있다.
- 유저: 실제 사용자
- 그룹: 여러 유저들을 모아 놓은 집합

따라서 롤 바인딩이나 클러스터 롤 바인딩을 정의하는 YAML 파일의 `Kind` 값에는 `ServiceAccount` 대신 `User`나 `Group`을 사용할 수도 있다.

서비스 어카운트라는 오브젝트가 쿠버네티스에 이미 존재하는데 왜 굳이 이 개념이 별도로 존재하는지 의문이 들 수 있다. 한 가지 알아야 할 점은 쿠버네티스에선 유저나 그룹이라는 오브젝트가 없기 때문에 `kubectl get`과 같은 명령어 또한 사용할 수 없다.

서비스 어카운트를 생성하면 `system:serviceaccount:<네임스페이스 이름>:<서비스 어카운트 이름>`이라는 유저 이름으로 서비스 어카운트를 지칭할 수 있다. 그룹은 이러한 유저를 모아 놓은 집합이다. 대표적인 예시는 `system:serviceaccount`로 시작하는 그룹이다. 이 그룹은 모든 네임스페이스에 속하는 모든 서비스 어카운트가 속해 있는 그룹이다.