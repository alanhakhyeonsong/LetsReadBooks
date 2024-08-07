# 14장. 보안 HTTP
## HTTP를 안전하게 만들기
보다 중요한 트랜잭션을 위해서는, HTTP와 디지털 암호화 기술을 결합해야 한다. HTTP의 보안 버전은 효율적이고, 이식성이 좋아야 하고, 관리가 쉬워야 하며, 현실 세계의 변화에 대한 적응력이 좋아야 한다. 또한 현실 세계의 변화에 대한 적응력이 좋아야 한다.

- 서버 인증: 클라이언트는 자신이 위조된 서버가 아닌 진짜와 이야기하고 있음을 알 수 있어야 한다.
- 클라이언트 인증: 서버는 자신이 가짜가 아닌 진짜 사용자와 이야기하고 있음을 알 수 있어야 한다.
- 무결성: 클라이언트와 서버는 그들의 데이터가 위조되는 것으로부터 안전해야 한다.
- 암호화: 클라이언트와 서버는 도청에 대한 걱정 없이 서로 대화할 수 있어야 한다.
- 효율: 저렴한 클라이언트나 서버도 이용할 수 있도록 알고리즘은 충분히 빨라야 한다.
- 편제성: 프로토콜은 거의 모든 클라이언트와 서버에서 지원되어야 한다.
- 관리상 확장성: 누구든 어디서든 즉각적인 보안 통신을 할 수 있어야 한다.
- 적응성: 현재 알려진 최선의 보안 방법을 지원해야 한다.
- 사회적 생존성: 사회의 문화적, 정치적 요구를 만족시켜야 한다.

### HTTPS
HTTPS를 사용할 때, 모든 HTTP 요청과 응답 데이터는 네트워크로 보내지기 전에 암호화된다. HTTPS는 HTTP의 하부에 전송 레벨 암호 보안 계층을 제공함으로써 동작하는데, 이 보안 계층은 안전 소켓 계층(SSL) 혹은 그를 계승한 전송 계층 보안(TLS)을 이용하여 구현된다. SSL과 TLS는 매우 비슷하다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d6129e4f-35d7-4f24-ada4-be13507b23a3)

## 디지털 암호학
- 암호: 텍스트를 아무나 읽지 못하도록 인코딩하는 알고리즘
- 키: 암호의 동작을 변경하는 숫자로 된 매개변수
- 대칭키 암호 체계: 인코딩과 디코딩에 같은 키를 사용하는 알고리즘
- 비대칭키 암호 체계: 인코딩과 디코딩에 다른 키를 사용하는 알고리즘
- 공개키 암호법: 비밀 메시지를 전달하는 수백만 대의 컴퓨터를 쉽게 만들 수 있는 시스템
- 디지털 서명: 메시지가 위조 혹은 변조되지 않았음을 입증하는 체크섬
- 디지털 인증서: 신뢰할 만한 조직에 의해 서명되고 검증된 신원 확인 정보

### 비밀 코드의 기술과 과학
암호법은 메시지 인코딩과 디코딩에 대한 기술이다. 암호법은 단순히 볼 수 없도록 메시지를 암호화하는 것뿐 아니라 메시지의 변조를 방지하기 위해 사용할 수도 있다.

### 암호
암호법은 암호라 불리는 비밀 코드에 기반한다. 암호란 메시지를 인코딩하는 어떤 특정한 방법과 나중에 그 비밀 메시지를 디코딩하는 방법이다. 인코딩되기 전의 원본 메시지는 흔히 텍스트 혹은 평문이라고 불린다. 암호가 적용되어 코딩된 메시지는 보통 암호문이라고 불린다.

### 암호 기계
기술이 진보하면서 복잡한 암호로 메시지를 빠르고 정확하게 인코딩하고 디코딩하는 기계를 만들기 시작하였다. 암호 기계는 암호를 깨뜨리기 어렵게 하기 위해, 단순히 회전을 하는 대신 글자들을 대체하고, 순서를 바꾸며 메시지를 자르고 토막내었다.

### 키가 있는 암호
대부분의 기계들에는 암호의 동작방식을 변경할 수 있는 큰 숫자로 된 다른 값을 설정할 수 있는 다이얼이 달려있다. 누군가 기계를 훔치더라도 올바른 다이얼 설정이 없이는 디코더가 동작하지 않을 것이다. 암호 키는 하나의 암호 기계를 여러 가상 암호 기계의 집합처럼 만들어준다. 가상 암호 기계들은 서로 다른 키 값을 갖고 있기 때문에 제각각 다르게 동작한다. 암호 알고리즘은 'N 번 회전' 암호다. N 의 값은 키에 의해 좌우된다. 같은 메시지가 같은 인코딩 기계를 통과하더라도 키의 값에 따라 다른 출력을 생성한다.

### 디지털 암호
- 속도 및 기능에 대한 기계 장치의 한계에서 벗어남으로써, 복잡한 인코딩과 디코딩 알고리즘이 가능해졌다.
- 큰 키를 지원하는 거이 가능해져서, 단일 암호 알고리즘으로 키의 값마다 다른 수조 개의 가상 암호 알고리즘을 만들어 낼 수 있게 되었다.

기계 장치의 물리적인 금속 키나 다이얼 설정과는 달리, 디지털 키는 그냥 숫자에 불과하다. 디지털 키 값은 인코딩과 디코딩 알고리즘에 대한 입력값이다. 코딩 알고리즘은 데이터 덩어리를 받아서 알고리즘과 키의 값에 근거하여 인코딩하거나 디코딩하는 함수이다.

## 대칭키 암호법
디지털 암호 알고리즘은 대칭키 암호라 불리는데, 그들이 인코딩 할 때 사용하는 키가 디코딩을 할 때와 같기 때문이다. 대칭키 암호에서 발송자와 수신자 모두 통신을 위해 비밀 키 k를 똑같이 공유할 필요가 있다. 발송자는 공유된 비밀 키를 메시지를 암호화하고 결과인 암호문을 수신자에게 발송하기 위해 사용한다. 수신자는 암호문을 받은 뒤 같은 공유된 키를 사용하여 평문을 복원하기 위해 해독 함수를 적용한다.

### 키 길이와 열거 공격
좋은 암호 알고리즘은 공격자가 코드를 크래킹하려면 존재하는 모든 가능한 키 값을 시도해보는 것 외에 다른 방법이 없게 만든다. 가능한 키 값의 개수는 키가 몇 비트이며 얼마나 많은 키가 유효한지에 달려있다. 대칭키 암호에서는 모든 키 값이 유효하다. 평범한 대칭키 암호에서, 40비트 키는 작고 중요하지 않은 업무에는 충분하다고 할 수 있다. 128비트 키를 사용한 대칭키 암호는 매우 강력하다.

### 공유키 발급하기
대칭키 암호의 단점 중 하나는 발송자와 수신자가 서로 대화하려면 둘 다 공유키를 가져야 한다는 것이다.

## 공개키 암호법
한 쌍의 호스트가 하나의 인코딩/디코딩 키를 사용하는 대신, 공개키 암호 방식은 두 개의 비대칭 키를 사용한다. 하나는 호스트의 메시지를 인코딩하기 위한 것이며, 다른 하나는 호스트의 메시지를 디코딩하기 위한 것이다. 인코딩 키는 모두를 위해 공개되어 있다. 하지만 호스트만이 개인 디코딩 키를 알고 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0842358d-3d4d-45a0-8473-9b37b73db526)

키의 분리는, 메시지의 인코딩은 누구나 할 수 있도록 해주는 동시에, 메시지를 디코딩하는 능력은 소유자에게만 부여한다. 이는 노드가 서버로 안전하게 메시지를 발송하는 것을 더 쉽게 해주는데, 왜냐하면 서버의 공개 키만 있으면 되기 때문이다.

공개키 암호화 기술은 보안 프로토콜을 전 세계의 모든 컴퓨터 사용자에게 적용하는 것을 가능하게 했다. 표준화된 공개키 기술 묶음을 만드는 것의 중요성 때문에, 거대한 공개 키 인프라 표준화 작업이 25년 넘게 계속 진행 중인 상태다.

### RSA
- 공개키
- 가로채서 얻은 암호문의 일부(네트워크를 스누핑해서 획득)
- 메시지와 그것을 암호화한 암호문(인코더에 임의의 텍스트를 넣고 실행해서 획득)

공개키 암호 체계중 유명한 하나는 MIT 에서 발명되고 이어서 RSA 데이터 시큐리티에서 상용화된 RSA 알고리즘이다.

### 혼성 암호 체계와 세션 키
공개키 암호 방식의 알고리즘은 계산이 느린 경향이 있다. 실제로는 대칭과 비대칭 방식을 섞은 것이 쓰인다. 노드들 사이의 안전한 의사소통 채널을 수립할 때는 편리하게 공개 키 암호를 사용하고, 안전한 채널을 통해 임시의 무작위 대칭 키를 생성하고 교환하여 이후의 나머지 데이터를 암호화할 때는 빠른 대칭키를 사용한다.

## 디지털 서명
암호 체계는 메시지를 암호화하고 해독하는 것뿐 아니라, 누가 메시지를 썼는지 알려주고 그 메시지가 위조되지 않았음을 증명하기 위해 메시지에 서명을 하도록 이용될 수 있다.

### 서명은 암호 체크섬이다
디지털 서명은 메시지에 붙어있는 특별한 암호 체크섬이다.

- 서명은 메시지를 작성한 저자가 누군지 알려준다. 저자는 저자의 극비 개인 키를 갖고 있기 때문에, 오직 저자만이 체크섬을 계산할 수 있다.
- 서명은 메시지 위조를 방지한다. 악의적인 공격자가 송신 중인 메시지를 수정했다면, 체크섬은 더 이상 그 메시지와 맞지 않게된다. 체크섬 날조가 불가하다

디지털 서명은 보통 비대칭 공개키에 의해 생성된다. 개인 키는 오직 소유자만이 알고 있기 떄문에, 저자의 개인 키는 일종의 지문처럼 사용된다.

- 가변 길이 메시지를 정제하여 고정된 길이의 요약으로 만든다.
- 요약에, 사용자의 개인 키를 매개변수로 하는 서명 함수를 적용한다.
- 한번 서명이 계산되면 메시지의 끝에 그것을 덧붙이고 메시지와 그에 대한 서명 둘 다를 전송한다.
- 개인 키로 알아보기 어렵게 변형된 서명에 공개키를 이용한 역함수를 적용한다. 만약 풀어낸 요약이 버전의 요약과 일치하지 않는다면, 메시지가 송신 중에 위조되었거나 발송자가 개인 키를 갖고 있지 않은 것이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f71775de-3ac9-4ecb-910a-0f4d4fe8fd5d)

## 디지털 인증서
디지털 인증서는 신뢰할 수 있는 기관으로부터 보증 받은 사용자나 회사에 대한 정보를 담고 있다.

### 인증서의 내부
디지털 인증서에는 공식적으로 '인증기관'에 의해 디지털 서명된 정보의 집합이 담겨있다.

- 대상의 이름(사람, 서버, 조직 등)
- 유효 기간
- 인증서 발급자(누가 이 인증서를 보증하는가)
- 인증서 발급자의 디지털 서명

디지털 인증서는 대상과 사용된 서명 알고리즘에 대한 서술적인 정보뿐 아니라 보통 대상의 공개키도 담고있다. 누구나 디지털 인증서를 만들 수 있지만, 모두가 인증서의 정보를 보증하고 인증서를 개인 키로 서명할 수 있는 서명 권한을 얻을 수 있는 것은 아니다.

### x.509 v3 인증서
디지털 인증서에 대한 전 세계적인 단일 표준은 없다. 오늘날 사용되는 인증서가 그들의 정보를 x.509 라 불리는 표준화된 서식에 저장하고 있다. x.509 v3 인증서는 인증 정보를 파싱 가능한 필드에 넣어 구조화하는 표준화된 방법을 제공한다.

- 버전: 인증서가 따르는 버젼 번호, 요즘은 보통 3 버전이다.
- 일련번호: 인증기관에 의해 생성된 고유한 정수
- 서명 알고리즘 ID: 서명을 위해 사용된 알고리즘
- 인증서 발급자: 인증서를 발급하고 서명한 기관의 이름
- 유효 기간: 인증서가 유효한 기간
- 대상의 이름: 인증서에 기술된, 사람이나 조직과 같은 엔터티
- 대상의 공개 키 정보: 인증 대상의 공개 키, 공개 키에 사용된 알고리즘
- 발급자의 고유 ID: 발급자 이름이 겹치는 경우를 대비한 고유한 식별자
- 대상의 고유 ID: 대상의 이름이 겹치는 경우를 대비한, 인증 대상에 대한 선택적인 고유한 식별자
- 확장: 선택적인 확장 필드의 집합버전

### 서버 인증을 위해 인증서 사용하기
사용자가 HTTPS를 통한 안전한 웹 트랜잭션을 시작할 때, 최신 브라우저는 자동으로 접속한 서버에서 디지털 인증서를 가져온다. 만약 서버가 인증서를 갖고 있지 않다면, 보안 커넥션은 실패한다.

서버 인증서는 웹 사이트의 이름, 호스트명, 공개키, 서명 기관의 이름, 서명 등을 가지고 있다.

브라우저가 인증서를 받으면, 서명 기관을 검사한다. 만약 그 기관이 공공이 신뢰할만한 서명 기관이라면 브라우저는 그것의 공개키를 이미 알고 있을 것이며 브라우저들은 여러 서명 기관의 인증서가 미리 설치된 채로 출하한다.

## HTTPS의 세부사항
HTTPS는 HTTP의 가장 유명한 보안 버전이다. HTTPS는 HTTP 프로토콜에 대칭, 비대칭 인증서 기반 암호 기법의 가장 강력한 집합을 결합한 것이다. 이 기법들의 집합은 무정부 상태의 분권화된 글로벌 인터넷 환경에서도 HTTPS를 매우 안전한 동시에 매우 유연하고 관리하기 쉽게 만들어 준다. 또한 분산된 웹 애플리케이션의 광역 보안 관리에 있어 중요하다.

### HTTPS 개요
HTTPS는 그냥 보안 전송 계층을 통해 전송되는 HTTP이다. 암호화되지 않은 HTTP 메시지를 TCP를 통해 전 세계의 인터넷 곳곳으로 보내는 대신에 HTTPS는 HTTP 메시지를 TCP로 보내기전에 그것들을 암호화하는 보안계층으로 보낸다.

### HTTPS 스킴
보안 HTTP는 선택적이다. 따라서 웹 서버로의 요청을 만들 때, 우리는 웹 서버에게 HTTP의 보안 프로토콜 버전을 수행한다고 말해줄 방법이 필요하다. 이것은 URL의 스킴을 통해 이루어진다.

```
https://cajun-shop.securesites.com/Merchant2/merchant.mv?Store_Code=AGCGS
```

- 만약 URL이 http 스킴을 갖고 있다면, 클라이언트는 서버에 80번(기본값) 포트로 연결하고 평범한 HTTP 명령을 전송한다.
- 만약 URL이 https 스킴을 갖고 있다면, 클라이언트는 서버에 443번(기본값) 포트로 연결하고 서버와 바이너리 포맷으로 된 몇몇 SSL 보안 매개변수를 교환하면서 '핸드셰이크'를 하고, 암호화된 HTTP 명령이 뒤를 잇는다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/78bd99dc-8a58-4688-9469-f43c09175ece)

SSL 트래픽은 바이너리 프로토콜이기 때문에, HTTP와는 완전히 다르다. 그 트래픽은 다른 포트로 전달된다. 만약 SSL과 HTTP 트래픽 모두 80번 포트로 도착한다면, 대부분의 웹 브라우저는 바이너리 SSL 트래픽을 잘못된 HTTP로 해석하고 커넥션을 닫을 것이다.

### 보안 전송 셋업
암호화되지 않은 HTTP에서 클라이언트는 웹 서버의 80번 포트로 TCP 커넥션을 열고 요청 메시지를 보내고 응답 메시지를 받고 커넥션을 닫는다. HTTPS는 SSL 보안 계층 때문에 약간 더 복잡한다. 클라이언트는 먼저 웹 서버의 443 포트로 연결한다. 연결 후 클라이언트와 서버는 암호법 매개변수와 교환 키를 협상하면서 SSL 계층을 초기화한다. 핸드셰이크가 완료되면 SSL 초기화는 완료되며, 클라이언트는 요청 메시지를 보안 계층에 보낼 수 있다. 메시지는 TCP로 보내지기전에 암호화된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/7947498a-f73b-4cdc-9288-5f3e72c47652)

### SSL 핸드셰이크
암호화된 HTTP 메시지를 보낼 수 있게 되기 전에, 클라이언트와 서버는 SSL 핸드셰이크를 할 필요가 있다. 핸드셰이크에서는 다음과 같은 일이 이루어진다.

- 프로토콜 버전 번호 교환
- 양쪽이 알고 있는 암호 선택
- 양쪽의 신원을 인증
- 채널을 암호화하기 위한 임시 세션 키 생성

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/dc2d50bd-7b16-47ff-a17f-c78779105735)

### 서버 인증서
SSL은 서버 인증서를 클라이언트로 나르고 다시 클라이언트 인증서를 서버로 날라주는 상호 인증을 지원한다. 보안 HTTPS 트랜잭션은 항사 서버 인증서를 요구한다. 서명된 서버 인증서는 서버를 얼마나 신뢰할 수 있는지 평가하는 것을 도와준다. 서버 인증서는 조직의 이름, 주소, 서버 DNS 도메인 이름, 그 외의 정보를 보여주는 x.509 v3 에서 파생된 인증서이다. 사용자와 사용자의 클라이언트 소프트웨어는 모든 것이 믿을 만한 것인지 확인하기 위해서 인증서를 검증할 수 있다.

### 사이트 인증서 검사
SSL 자체는 사용자에게 웹 서버 인증서를 검증할 것을 요구하지 않지만, 최신 웹브라우저들 대부분은 인증서에 대해 간단하게 기본적인 검사를 하고 그 결과를 더 철저한 검사를 할 수 있는 방법과 함께 사용자에게 알려준다.

- 날짜 검사: 브라우저는 인증서가 유효함을 확인하기 위해 인증서의 시작 및 종료일을 검사한다.
- 서명자 신뢰도 검사: 모든 인증서는 서버를 보증하는 인증 기관에 의해 서명되어 있다.
- 서명 검사: 서명 기관이 믿을 만하다고 판단하면, 브라우저는 서명기관의 공개키를 서명에 적용하여 체크섬과 비교해봄으로써 인증서의 무결성을 검사한다.
- 사이트 신원 검사: 누군가 다른 이의 인증서를 복사하거나 트래픽을 가로채는 것을 방지하기 위해, 대부분의 브라우저는 인증서의 도메인 이름이 대화중인 서버의 도메인 이름과 비교하여 맞는지 검사한다.

### 가상 호스팅과 인증서
가상 호스트로 운영되는 사이트의 보안 트래픽을 다루는 것은 까다로운 경우도 많다. 사용자가 인증서의 이름과 정확히 맞지 않는 가상 호스트 명에 도착했다면 경고 상자가 나타날 것이다.

## 진짜 HTTPS 클라이언트
SSL은 복잡한 바이너리 프로토콜이다.

### OpenSSL
OpenSSL은 SSL과 TLS의 가장 인기 있는 오픈 소스 구현이다. OpenSSL 프로젝트는, 강력한 다목적 암호법 라이브러리인 동시에 SSL과 TLS 프로토콜을 구현한 강건하고 완전한 기능을 갖춘 상용 수준의 툴킷을 개발하고자 한 자원봉사자들이 협업한 결과물이다.

## 프락시를 통한 보안 트래픽 터널링
클라이언트는 종종 그들을 대신하여 웹 서버에 접근해주는 웹 프락시 서버를 이용한다. 클라이언트가 서버로 보낼 데이터를 서버의 공개키로 암호화하기 시작했다면 프락시는 더 이상 HTTP 헤더를 읽을 수 없다. 헤더를 읽을 수 없다면 프락시는 요청을 어디로 보내야 하는지 알 수 없게된다.

이 문제를 해결하는 유명한 방법중 하나는 HTTPS SSL 터널링 프로토콜이다. HTTPS 터널링 프로토콜을 사용해서 르라이언트는 먼저 프락시에게 자신이 연결하고자 하는 안전한 호스트와 포트를 말해준다. 클라이언트는 내용을 프락시가 읽을 수 있도록 암호화가 시작되기 전의 평문으로 말해준다.

HTTP는 CONNECT라 불리는 새로운 확장 메서드를 이용해서 평문으로 된 종단 정보를 전송하기 위해 사용된다. CONNECT 메서드는 프락시에게 희망하는 호스트와 포트번호로 연결을 해달라고 말해주며, 그것이 완료되면, 클라이언트와 서버 사이에서 데이터가 직접적으로 오갈 수 있게 해주는 터널을 만든다.