# 18장. 웹 호스팅
리소스를 공용 웹 서버에 올려놓으면 인터넷을 통해 이용할 수 있다. 이 리소스들은 텍스트 파일이나 이미지 같이 단순할 수도 있고, 실시간 네비게이션이나 전자상거래 쇼핑 게이트웨이 같이 복잡할 수도 있다. 각 조직이 가지고 있는 다양한 종류의 리소스를 웹 사이트에 편하게 배포하거나, 적절한 가격에 좋은 성능을 가진 웹 서버에 배치하는 것은 매우 중요하다.

**콘텐츠 리소스를 저장, 중개, 관리하는 일을 통틀어 웹 호스팅이라 한다.** 호스팅은 웹 서버의 가장 중요한 기능 중 하나다. 콘텐츠를 저장해서 제공하고 관련 로그에 접근하거나 그것을 관리하는 데 서버가 필요하다.

## 호스팅 서비스
월드 와이드 웹 초기에는 각 회사가 자체 컴퓨터 하드웨어를 구매하고 자체 컴퓨터 망을 구축하여 자체 네트워크 연결을 확보하고 자체 웹 서버 소프트웨어를 관리했다.

웹이 빠르게 대세가 되면서, 모든 사람이 웹 사이트를 원했지만, 냉난방 장치가 있는 서버실을 짓고 도메인 이름을 등록하고 네트워크 대역폭을 구매할 기술과 시간을 가진 사람은 드물었다. 그 시간을 절약하기 위해, 전문적으로 관리하는 웹 호스팅 서비스를 제공하는 여러 신사업이 만들어졌다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e6131748-a939-4b31-8a78-1aaf7d23f14e)

## 가상 호스팅
**많은 웹 호스팅 업자는 컴퓨터 한 대를 여러 고객이 공유하게 해서 저렴한 웹 호스팅 서비스를 제공한다. 이를 공유 호스팅 혹은 가상 호스팅이라 부른다.** 각 웹 사이트는 다른 서버에서 호스팅하는 것처럼 보이겠지만, **사실은 물리적으로 같은 서버에서 호스팅되는 것이다.** 최종 사용자의 관점에선 가상 호스팅에 있는 웹 사이트는, 물리적으로 분리된 전용 서버에서 호스팅 하는 사이트와 구분할 수 없어야 한다.

호스팅 업자는 복제 서버 더미(서버 팜)를 만들고 서버 팜에 부하를 분산할 수 있다. 팜에 있는 각 서버는 다른 서버를 복제한 것이며, 수많은 가상 웹 사이트를 호스팅하고 있기 때문에 관리자는 훨씬 편해진다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3945cff1-12d5-487c-8a83-f6957b91f5b0)

### 호스트 정보가 없는 가상 서버 요청
HTTP 1.0 명세는 공용 웹 서버가 호스팅하고 있는 가상 웹 사이트에 누가 접근하고 있는지 식별하는 기능을 제공하지 않는다.

- 클라이언트 A가 `http://www.joes-hardware.com/index.html`에 접속하려 하면, `GET /index.html` 요청이 공용 웹 서버에 전송된다.
- 클라이언트 B가 `http://www.marys-antiques.com/index.html`에 접속하려 하면, 위와 같은 `GET /index.html` 요청이 `joes-hardware.com`과 공유하고 있는 공용 웹 서버에 전송된다.

따라서, 웹 서버는 사용자가 어떤 웹 사이트로 접근하려고 하는지 아는데 필요한 정보가 충분하지 않게 된다. 두 요청이 완전히 다른 문서를(서로 다른 사이트에) 요청하더라도, 요청 자체는 똑같이 생기게 되기 때문이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f5669d88-f7a0-4d5d-bafd-bf16cd7fa06e)

### 가상 호스팅 동작하게 하기
호스트 정보를 HTTP 요청 명세에 넣지 않은 것은, 각 웹 서버가 정확히 한 웹 사이트만 호스팅할 것이라고 잘못 예측한 HTTP 명세의 실수였다. HTTP 설계자들은 공유 서버인 가상 호스팅을 고려하지 않았다. 그래서 URL에 있는 호스트 명 정보는 필요 없는 것으로 여겨 명세에서 제외하고 단순히 경로 컴포넌트만 전송하도록 설계했다.

HTTP/1.1을 지원하는 서버는 HTTP 요청 메시지에 있는 전체 URL을 처리할 수 있어야 한다. 하지만 기존에 있던 모든 애플리케이션이 이 명세에 맞추어 업그레이드하기까지는 오랜 시간이 걸릴 것이다. 그 와중에, 네 가지 기술이 나타났다.

- URL 경로를 통한 가상 호스팅
  - 불필요한 접두어가 생기므로 좋지 않은 방법.
- 포트번호를 통한 가상 호스팅
  - 웹 서버에 각각 다른 포트번호를 할당하는 방식. 사용자들은 URL에 비표준 포트를 쓰지 않고서도 리소스를 찾길 원하기 때문에 좋지 않다. 
- IP 주소를 통한 가상 호스팅
  - 가상 웹 사이트에 유일한 IP 주소를 한 개 이상 부여한다. 모든 가상 서버의 IP 주소는 같은 공용 서버에 연결되어 있다. 서버는 HTTP 커넥션의 목적지 IP 주소를 보고 클라이언트가 어떤 웹 사이트에 연결하려 하는지 알 수 있다.
  - 가상 IP 호스팅은 잘 동작하지만, 규묘가 아주 큰 호스팅 업자에겐 약간 어려운 문제를 안겨준다.
    - 일반적으로 컴퓨터 시스템이 연결할 수 있는 장비의 IP의 개수에는 제한이 있다.
    - IP 주소는 희소 상품이다. 가상 사이트를 많이 가지고 있는 호스팅 업자는 호스팅하는 모든 웹 사이트에 할당할 가상 IP 주소를 충분히 얻지 못할 수도 있다.
    - IP 주소가 부족한 문제는 호스팅 업자가 용량을 늘리려고 서버를 복제하면서 더 심각해진다. 부하 균형의 구조상, 각 복제된 서버에 IP 주소를 부여해야 하므로 IP 주소는 복제 서버의 개수만큼 더 필요하게 된다.
  - 가상 IP 호스팅은 위와 같은 IP 주소 부족 문제가 생길 수 있음에도 불구하고 널리 쓰이는 방식이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/789eb271-6cf6-4258-8978-072c7d9b52d3)

- Host 헤더를 통한 가상 호스팅
  - IP 주소의 낭비와 가상 IP의 제한 문제를 피하려면, 가상 사이트들이 같은 IP를 사용하더라도 각 사이트가 어디에 속해 있는지 알 수 있어야 한다. 이 문제를 해결하기 위해 서버 개발자들은 서버가 원 호스트 명을 받아 볼 수 있게 HTTP를 확장하였다.

### HTTP/1.1 Host 헤더
Host 헤더는 RFC 2068에 정의되어 있는 HTTP/1.1 요청 헤더다. 가상 서버는 매우 흔하기 때문에 대부분의 HTTP 클라이언트가 HTTP/1.1과 호환되지 않더라도, Host 헤더는 구현한다.

```
Host = "Host" ":"호스트[ ":"포트]
```

다음과 같은 규칙이 있다.

- Host 헤더에 포트가 기술되어 있지 않으면, 해당 스킴의 기본 포트를 사용한다.
- URL에 IP 주소가 있으면, Host 헤더는 같은 주소를 포함해야 한다.
- URL에 호스트 명이 기술되어 있으면, Host 헤더는 같은 호스트 명을 포함해야 한다.
- URL에 호스트 명이 기술되어 있으면, Host 헤더는 URL의 호스트 명이 가리키는 IP 주소를 포함해서는 안된다.
- 여러 개의 가상 사이트를 한개의 IP에 연결한 가상 호스트 서버에서 문제상황 발생 가능.
- 클라이언트가 특정 프락시 서버를 사용한다면, Host 헤더에 프락시 서버가 아닌 원 서버의 호스트 명과 포트를 기술해야 한다.
- 웹 클라아언트는 모든 요청 메시지에 Host 헤더를 기술해야 한다.
- HTTP/1.1 웹 서버는 Host 헤더 필드가 없는 HTTP/1.1 요청 메시지를 받으면 400 상태 코드로 응답해야 한다.

## 안정적인 웹 사이트 만들기
웹 사이트에 장애가 생기는 몇 가지 상황이 있다.

- 서버 다운
- 트래픽 폭증
- 네트워크 장애나 손실

### 미러링 된 서버 팜
서버 팜은 서로 대신할 수 있고 식별할 수 있게 설정된 웹 서버들의 집합이다. 서버 팜의 서버에 있는 콘텐츠들은 한 곳에 문제가 생기면 다른 한 곳에서 대신 전달할 수 있게 미러링할 수 있다.

- 보통 미러링 된 서버는 계층적인 관계에 있다. 한 서버는 '콘텐츠의 원본 제작자' 같이 행동한다. 이 서버를 마스터 원 서버라 부른다. 마스터 원 서버로부터 콘텐츠를 받은 미러링 된 서버는 복제 원 서버라 부른다.
- 서버 팜에 배포하는 간단한 방법 하나는, 네트워크 스위치를 사용해서 서버에 분산 요청을 보내는 것이다. 서버에 호스팅 되고 있는 각 웹 사이트의 IP 주소는 스위치의 IP 주소가 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/5d35ec3b-160b-41b1-856a-be961643aa94)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b58009ce-f466-4712-a27f-2696917ed6d2)

- HTTP 리다이렉션: 콘텐츠에 대한 URL은 마스터 서버의 IP를 가리키고, 마스터 서버는 요청을 받는 즉시 복제 서버로 리다이렉트 시킨다.
- DNS 리다이렉션: 콘텐츠의 URL은 네 개의 IP 주소를 가리킬 수 있고, DNS 서버는 클라이언트에게 전송할 IP 주소를 선택할 수 있다.

### CDN의 대리 캐시
대리 캐시는 복제 원 서버를 대신해 사용될 수 있다. 리버스 프락시라고도 불리는 대리 서버는 미러링 된 웹 서버처럼 콘텐츠에 대한 요청을 받는다. 그들은 특정 원 서버 집합을 대신해 요청을 받는다.

대리 서버와 미러링된 서버의 차이점은, 대리 서버는 보통 수요에 따라 동작한다는 것이다. 대리 서버는 원 서버의 전체 콘텐츠를 복사하진 않는다. 클라이언트가 요청하는 콘텐츠만 저장할 뿐이다. 대리 서버의 캐시에 콘텐츠라 분산되는 방식은 그들이 받는 요청에 따라 달라진다. 원 서버는 그들의 콘텐츠를 업데이트해 줄 의무는 없다. 많은 요청이 있는 콘텐츠를 빠르게 제공하려고, 사용자가 요청하기도 전에 콘텐츠를 가져오는 '미리 가져오기' 기능을 가진 대리 서버도 있다. CDN이 대리 서버보다 캐시를 계층화하기 더 어렵다.

### CDN의 프락시 캐시
대리 서버와는 다르게, 전통적인 프락시 캐시는 어떤 웹 서버 요청이든 다 받을 수 있다. 하지만 대리 서버를 사용하면, 프락시 캐시의 콘텐츠는 요청이 있을 때만 저장될 것이고 원본 서버 콘텐츠를 정확히 복제한다는 보장이 없다. 어떤 프락시는 요청을 많이 받는 콘텐츠를 미리 로딩하기도 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/188d1625-5c8d-4e91-ae79-815c29a18ac5)

## 웹 사이트 빠르게 만들기
이번 장에서 언급된 많은 기술들은 웹 사이트를 더 빠르게 하는 데 도움이 된다. 서버 팜이나 분산 프락시 캐시나 대리 서버는 혼잡을 조절하고 네트워크 트래픽을 분산시킨다. 콘텐츠를 분산시키면, 그 콘텐츠를 사용자에게 더 가깝게 만들어 주므로 콘텐츠를 서버에서 클라이언트로의 전송하는 시간이 단축된다. 리소스의 로딩 속도를 좌우하는 핵심 요소는, 어떻게 요청과 응답이 클라이언트와 서버 사이에서 연결을 맺고 인터넷을 가로질러 데이터를 전송하는지다.

웹 사이트 속도를 높이는 또 다른 접근 방법은 콘텐츠를 인코딩하는 것이다. 예를 들면, 클라이언트가 받은 압축을 해제할 수 있다는 가정하에, 콘텐츠를 압축하는 것이다.