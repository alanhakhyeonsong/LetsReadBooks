# 8장. 인그레스
서비스 오브젝트가 외부 요청을 받아들이기 위한 것이었다면, **'인그레스'는 외부 요청을 어떻게 처리할 것인지 네트워크 7계층 레벨에서 정의하는 쿠버네티스 오브젝트다.** 인그레스 오브젝트가 담당할 수 있는 기본적인 기능을 간단히 나열하면 다음과 같다.

- 외부 요청의 라우팅: `/apple`, `/apple/red` 등과 같이 특정 경로로 들어온 요청을 어떠한 서비스로 전달할지 정의하는 라우팅 규칙을 설정할 수 있다.
- 가상 호스트 기반의 요청 처리: 같은 IP에 대해 다른 도메인 이름으로 요청이 도착했을 때, 어떻게 처리할 것인지 정의할 수 있다.
- SSL/TLS 보안 연결 처리: 여러 개의 서비스로 요청을 라우팅할 때, 보안 연결을 위한 인증서를 쉽게 적용할 수 있다.

## 인그레스를 사용하는 이유
예를 들어, 애플리케이션이 3개의 디플로이먼트로 생성돼 있다 가정해보자. 앞 장까지 공부한 방법을 이용해 각 디플로이먼트를 외부에 노출해야 한다면, `NodePort` 또는 `LoadBalancer` 타입의 서비스 3개를 생성하는 방법을 먼저 떠올릴 것이다. 각 디플로이먼트에 대응하는 서비스를 하나씩 연결해 준 셈이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/78faea89-eed7-495b-978a-9d5f48f8eb52)

위 방식은, 서비스마다 세부적인 설정을 할 때 추가적인 복잡성이 발생하게 된다. **SSL/TLS 보안 연결, 접근 도메인 및 클라이언트 상태에 기반한 라우팅 등을 구현하려면 각 서비스와 디플로이먼트에 대해 일일이 설정을 해야 하기 때문이다.**

**쿠버네티스가 제공하는 인그레스 오브젝트를 사용하면 URL 엔드포인트를 단 하나만 생성함으로써 이러한 번거로움을 쉽게 해결할 수 있다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c4ede032-ee71-4550-bf5f-e00fbb634244)

위 그림에선 3개의 서비스에 대해 3개의 URL이 각각 존재하는 것이 아닌, 인그레스에 접근하기 위한 단 하나의 URL만 존재한다. 따라서 **클라이언트는 인그레스의 URL로만 접근하게 되며, 해당 요청은 인그레스에서 정의한 규칙에 따라 처리된 뒤 적절한 디플로이먼트의 포트로 전달된다.**

여기서 중요한 점은 **라우팅 정의나 보안 연결 등과 같은 세부 설정은 서비스와 디플로이먼트가 아닌 인그레스에 의해 수행된다는 것이다.** 각 디플로이먼트에 대해 일일이 설정을 적용할 필요 없이 하나의 설정 지점에서 처리 규칙을 정의하기만 하면 된다. **외부 요청에 대한 처리 규칙을 쿠버네티스 자체의 기능으로 편리하게 관리할 수 있다는 것이 인그레스의 핵심이다.**

## 인그레스의 구조
```yaml
# ingress-example.yaml
apiVersion: networking.k8s.io/v1beta1
kind: Ingress
metadata:
  name: ingress-example
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    kubernetes.io/ingress.class: "nginx"
spec:
  rules:
  - host: alicek106.example.com
    http:
      paths:
      - path: /echo-hostname
        backend:
          serviceName: hostname-service
          servicePort: 80
```

- `host`: 해당 도메인 이름으로 접근하는 요청에 대해 처리 규칙을 적용한다. 위 예시에선 `alicek106.example.com`이라는 도메인으로 접근하는 요청만 처리하지만, 여러 개의 host를 정의해 사용할 수도 있다.
- `path`: 해당 경로에 들어온 요청을 어느 서비스로 전달할 것인지 정의한다. 위 예시에선 `/echo-hostname`이란 경로의 요청을 `backend`에 정의된 서비스로 전달한다. 여러 개의 `path`를 정의해 경로를 처리할 수도 있다.
- `serviceName`, `servicePort`: `path`로 들어온 요청이 전달된 서비스와 포트다. 위 예시에선 `/echo-hostname` 이란 경로로 들어온 요청을 `hostname-service` 서비스의 80 포트로 전달한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/70b1ed8e-e492-41ee-8e42-418f9c584532)

인그레스는 단지 요청을 처리하는 규칙을 정의하는 선언적인 오브젝트일 뿐, 외부 요청을 받아들일 수 있는 실제 서버가 아니다. **인그레스는 인그레스 컨트롤러라고 하는 특수한 서버에 적용해야만 그 규칙을 사용할 수 있다.** 즉, 실제로 외부 요청을 받아들이는 것은 인그레스 컨트롤러 서버이며, 이 서버가 인그레스 규칙을 로드해 사용한다. 따라서 **쿠버네티스의 인그레스는 반드시 인그레스 컨트롤러라는 서버와 함께 사용해야 한다.** 대표적으론 **Nginx 웹 서버 인그레스 컨트롤러**가 있는데 쿠버네티스에서 공식적으로 개발되고 있기 때문에 이를 활용하도록 하자.

**Nginx 인그레스 컨트롤러를 설치하면 자동으로 생성되는 서비스는 `LoadBalancer` 타입이다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/cf99fece-bc0a-40e0-81e5-50788d0151e0)