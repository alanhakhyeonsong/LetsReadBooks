# 15장. 엔터티와 인코딩
HTTP는 매일 수십억 개의 미디어 객체를 실어 나른다. HTTP는 또한 메시지가 올바르게 수송되고, 식별되고, 추출되고, 처리되는 것을 보장한다. 구체적으로 말하면 HTTP는 다음을 보장한다.

- 객체는 올바르게 식별되므로(Content-Type 미디어 포맷과 Content-Language 헤더를 이용해서) 브라우저나 다른 클라이언트는 콘텐츠를 바르게 처리할 수 있다.
- 객체는 올바르게 압축이 풀릴 것이다(Content-Length와 Content-Encoding 헤더를 이용해서).
- 객체는 항상 최신이다(엔터티 검사기와 캐시 만료 제어를 이용해서).
- 사용자의 요구를 만족할 것이다(내용 협상을 위한 Accept 관련 헤더들에 기반하여).
- 네트워크 사이를 빠르고 효율적으로 이동할 것이다(범위 요청, 델타 인코딩, 그 외의 데이터 압축을 이용해서).
- 조작되지 않고 온전하게 도착할 것이다(전송 인코딩 헤더와 Content-MD5 체크섬을 이용해서).

이 모든 것을 가능하게 하기 위해, HTTP는 콘텐츠를 나르기 위한 잘 라벨링된 엔터티를 사용한다.

## 메시지는 컨테이너, 엔터티는 화물
HTTP 메시지를 인터넷 운송 시스템의 컨테이너라고 생각한다면, HTTP 엔터티는 메시지의 실질적인 화물이다. 다음 엔터티 헤더는 18자에 불과한 플레인 텍스트 문서를 의미한다.

```
HTTP/1.0 200 OK
Server: Netscape-Enterprise/3.6
Date: Sun, 17 Sep 2000 00:01:05 GMT
Content-type: text/plain
Content-length: 18

Hi! I'm a message!
```

|Content-Type| 엔터티에 의해 전달된 객체의 종류|
|---|---|
|Content-Length| 메시지의 길이나 크기|
|Content-Language|전달되는 객체와 대응되는 언어|
|Content-Encoding|객체 데이터에 대해 행해진 변형|
|Content-Location|요청 시점을 기준으로, 객체의 또 다른 위치|
|Content-Range|부분 엔터티라면, 헤더는 이 엔터티가 전체에서 어느 부분에 해당하는지 정의한다|
|Content-MD5|본문의 콘텐츠에 대한 체크섬|
|Last-Modified|서버에서 이 콘텐츠가 생성 혹은 수정된 날|
|Expires|엔터티 데이터가 더 이상 신선하지 않은 것으로 간주되는 시작하는 날짜와 시각|
|Allow|리소스에 대해 어떤 요청 메서드가 허용되는지|
|ETag|인스턴스에 대한 고유 검사기|
|Cache-Control|문서가 캐시될 수 있는지에 대한 지시자|

### 엔터티 본문
엔터티 본문은 가공되지 않은 데이터만을 담고 있다. 다른 정보들은 모두 헤더에 담겨있다. 엔터티 본문은 가공되지 않은 날 데이터에 불과하기 떄문에 엔터티 헤더는 데이터의 의미에 대해 설명할 필요가 있다.

엔터티 본문은 헤더 필드의 끝을 의미하는 빈 CRLF 줄 바로 다음부터 시작한다. 콘텐츠가 텍스트든 바이너리든, 문서든 이미지든, 압축되었든 안 되었든, 영어든 프랑스어든 일본어든 상관없이 항상 CRLF 바로 다음에 위치한다.

## Content-Length: 엔터티의 길이
Content-Length 헤더는 메시지의 엔터티 본문의 크기를 바이트 단위로 나타난다. 어떻게 인코딩 되었든 상관없이 크기를 표현할 수 있다. 메시지를 청크 인코딩으로 전송하지 않는 이상, 엔터티 본문을 포함한 메시지에서는 필수적으로 있어야 한다. 서버 충돌로 인해 메시지가 잘렸는지 감지하고자 할 때와 지속 커넥션을 공유하는 메시지들 올바르게 분할하고자 할 때 필요하다.

### 잘림 검출
옛날 버전의 HTTP는 커넥션이 닫힌 것을 보고 메시지가 끝났음을 인지했다. 그러나 Content-Length 가 없다면 클라이언트는 커넥션이 정상적으로 닫힌 것인지 메시지 전송 중에 서버에 충돌이 발생한 것인지 구분할 수 없다.

메시지 잘림은 캐싱 프락시 서버에 특히 취약하다. 잘린 메시지를 캐시하는 위험을 줄이기 위해, 캐싱 프락시 서버는 명시적으로 Content-Length 헤더를 갖고 있지 않은 HTTP 본문은 보통 캐시하지 않는다.

### 잘못된 Content-Length
초창기 클라이언트들은 Content-Length의 계산과 관련된 버그들을 갖고 있기 때문에, 클라이언트, 서버, 프락시 들은 서버가 오동작을 했는지 탐지하고 교정을 시도한다. 공식적으로 HTTP/1.1 사용자 에이전트는 잘못된 길이를 받고 그 사실을 인지했을 때 사용자에게 알려주게 되어 있다.

### Content-Length 와 지속 커넥션 
만약 응답이 지속 커넥션을 통해서 온 것이라면, 또 다른 HTTP 응답이 즉시 그 뒤를 이을 것이다. Content-Length 헤더는 클라이언트에게 메시지 하나가 어디서 끝나고 다음 시작은 어디인지 알려준다. 커넥션이 지속적이기 때문에, 클라이언트가 커넥션이 닫힌 위치를 근거로 메시지의 끝을 인식하는 것은 불가능하다.

### 콘텐츠 인코딩
HTTP는 보안을 강화하거나 압축을 통해 공간을 절약할 수 있도록, 엔터티 본문을 인코딩할 수 있게 해준다. 만약 본문의 콘텐츠가 인코딩되어 있다면, Content-Length 헤더는 인코딩 되지 않은 원본의 길이가 아닌 **인코딩된 본문의 길이를 바이트 단위로 정의**한다. 어떤 HTTP 애플리케이션은 이것을 잘못해서 인코딩 전의 크기를 보내는 것으로 알려져 있는데, 이는 지속 커넥션일 때 심각한 오류를 유발한다.

### 엔터티 본문 길이 판별을 위한 규칙 
1. 본문을 갖는 것이 허용되지 않는 특정 타입의 HTTP 메시지에서는, 본문 계산을 위한 Content-Length가 무시된다. 엔터티 본문을 금하는 메시지는 어떤 엔터티 헤더 필드가 존재하느냐와 상관없이 반드시 헤더 이후의 빈 줄에서 끝나야 한다.
2. 메시지가 transfer-encoding 헤더를 포함하고 있다면, 메시지가 커넥션이 닫혀서 먼저 끝나지 않는 이상 엔터티는 '0바이트 청크' 라 불리는 특별한 패턴으로 끝나야한다.
3. 메시지가 Content-Length 헤더를 갖는다면, Transfer-encoding 헤더가 존재하지 않는 이상 Content-Length 값은 본문의 길이를 담게된다. 만약 Content-Length 헤더 필드와 identity 가 아닌 Transfer-encoding 헤더 필드를 갖고 있는 메시지를 받았다면 반드시 Content-Length 헤더를 무시해야한다.
4. 메시지가 'multipart/byteranges' 미디어 타입을 사용하고 엔터티 길이가 별도로 정의되지 않았다면, 멀티파트 메시지의 각 부분은 각자가 스스로의 크기를 정의할 것이다. 이 미디어 타입은 수신자가 이것을 해설할 수 있다는 사실을 송신자가 알기 전까지는 절대로 보내지 말아야한다.
5. 위의 규칙에 해당하지 않는다면, 엔터티는 커넥션이 닫힐 때 끝난다. 실직적으로, 오직 서버만이 메시지가 끝났음을 알리기 위해서 커넥션을 닫을 수 있다.
6. HTTP/1.0 애플리케이션과의 호환을 위해, 엔터티 본문을 갖고 있는 HTTP/1.1 요청은 반드시 유효한 Content-Length 헤더도 갖고 있어야 한다.

## 엔터티 요약
HTTP가 일반적으로 TCP/IP와 같이 신뢰할 만한 전송 프로토콜 위에서 구현됨에도 불구하고, 불완전한 트랜스코딩 프락시나 버그 많은 중개자 프락시를 비롯한 여러가지 이유로 메시지의 일부분이 전송 중에 변형되는 일이 일어난다.

엔터티 본문 데이터에 대한 의도하지 않은 변경을 감지하기 위해, 최초 엔터티가 생성될 때 송신자는 데이터에 대한 체크섬을 생성할 수 있으며, 수신자는 모든 의도하지 않은 엔터티의 변경을 잡아내기 위해 체크섬으로 기본적인 검사를 할 수 있다.

Content-MD5 헤더는 서버가 엔터티 본문에 MD5 알고리즘을 적용한 결과를 보내기 위해 사용된다. 응답을 처음 만든 서버만이 Content-MD5 헤더를 계산해서 보낼 것이다. Content-MD5 헤더는 콘텐츠 인코딩의 적용은 끝났지만 전송 인코딩은 아직 적용하지 않은 엔터티 본문에 대한 MD5를 담고있다. 메시지의 무결성을 검증하려는 클라이언트는 먼저 전송 인코딩을 디코딩한 뒤 그 디코딩 된 엔터티 본문에 대해 MD5를 계산해야 한다.

메시지 무결성 검사에 더해, MD5는 문서의 위치를 빠르게 알아내고 콘텐츠의 중복 저장을 방지하기 위한 해시 테이블의 키로 이용될 수 있다.

## 미디어 타입과 차셋(Charset)
Content-Type 헤더 필드는 엔터티 본문의 MIME 타입을 기술한다. MIME 타입은 전달되는 데이터 매체의 기저 형식의 표준화된 이름이다. Content-Type 헤더가 원본 엔터티 본문의 미디어 타입을 명시한다는 것은 중요하다. 엔터티가 콘텐츠 인코딩을 거친 경우에도 Content-Type 헤더는 여전히 인코딩 전의 엔터티 본문 유형을 명시할 것이다.

|미디어 타입|설명|
|---|---|
|text/html|엔터티 본문은 HTML 문서|
|text/plain|엔터티 본문은 플레인 텍스트 문서|
|image/gif|엔터티 본문은 GIF 이미지|
|image/jpeg|엔터티 본문은 JPEG 이미지|
|audio/x-wav|엔터티 본문은 WAV 음향 데이터를 포함|
|model/vml|엔터티 본문은 삼차원 VRML 모델|
|application/vnd.ms-powerpoint|엔터티 본문은 마이크로소프트 파워포인트 프레젠테이션|
|multipart/byteranges|엔터티 본문은 여러 부분으로 나뉘는데, 각 부분은 전체 문서의 특정 범위(바이트 단위)를 담고 있다.|
|message/http|엔터티 본문은 완전한 HTTP 메시지를 담고 있다.|

### 텍스트 매체를 위한 문자 인코딩
Content-Type 헤더는 내용 유형을 더 자세히 지정하기 위한 선택적인 매개변수도 지원한다.

```
Content-Type: text/html; charset=iso-8859-4
```

### 멀티파트 미디어 타입
MIME '멀티파트' 이메일 메시지는 서로 붙어잇는 여러 개의 메시지를 포함하며, 하나의 복합 메시지로 보내진다. 각 구성요소는 자족적으로 자신에 대해 서술하는 헤더를 포함한다. HTTP는 멀티파트 본문도 지원한다. 그러나 일반적으로는 폼을 채워서 제출할 때와 문서의 일부분을 실어 나르는 범위 응답을 할 때의 두 가지 경우에만 사용한다.

### 멀티파트 폼 제출
HTTP 폼을 채워서 제출하면, 가변 길이 텍스트 필드와 업로드 될 객체는 각각이 멀티파트 본문을 구성하는 하나의 파트가 되어 보내진다. 멀티파트 본문은 여러 다른 종류와 길이의 값으로 채워진 폼을 허용한다.

```
Content-Type: multipart/form-data; boundary=[abcdefghijklmnopqrstuvwxyz]
```

`boundary`는 본문의 서로 다른 부분을 구분하기 위한 구분자로 쓰인다.

### 멀티파트 범위 응답
범위 요청에 대한 HTTP 응답 또한 멀티파트가 될 수 있다. 그러한 응답은 Content-Type: multipart/byteranges 헤더 및 각각 다른 범위를 담고 있는 멀티파트 본문이 함께 온다.

## 콘텐츠 인코딩
HTTP 애플리케이션은 때때로 콘텐츠를 보내기 전에 인코딩을 하려고 한다. 느린 속도로 연결된 클라이언트에게 큰 HTML 문서를 전송하기 전에 서버는 전송 시간을 줄이기 위해 압축을 할 수 있다. 서버는 허가 받지 않은 제삼자가 볼 수 없도록 콘텐츠를 암호화하거나 뒤섞어서 보낼 수도 있다. 인코딩은 발송하는 쪽에서 콘텐츠에 적용한다. 콘텐츠 인코딩이 끝난 데이터는 늘 그렇듯 엔터티 본문에 담아 수신자에게 보낸다.

### 콘텐츠 인코딩 과정
1. 웹 서버가 원본 Content-Type과 Content-Length 헤더를 수반한 원본 응답 메시지를 생성한다.
2. 콘텐츠 인코딩 서버가 인코딩된 메시지를 생성한다. 인코딩된 메시지는 Content-type은 같지만 Content-Length는 다르다. 콘텐츠 인코딩 서버는 Content-Encoding 헤더를 인코딩된 메시지에 추가하여, 수신 측 애플리케이션이 그것을 디코딩 할 수 있도록 한다.
3. 수신 측 프로그램은 인코딩된 메시지를 받아서 디코딩하고 원본을 얻는다.

### 콘텐츠 인코딩 유형
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c37ec9a3-620f-4f4c-87ed-3ba0db5cdb98)

### Accept-Encoding 헤더
서버에서 클라이언트가 지원하지 않는 인코딩을 사용하는 것을 막기 위해, 클라이언트는 자신이 지원하는 인코딩의 목록을 Accept-Encoding 요청 헤더를 통해 전달한다. accept-Encoding 헤더를 포함하지 않는다면, 서버는 클라이언트가 어떤 인코딩이든 받아들일 수 있는 것으로 간주한다. identity 인코딩 토큰은 오직 Accept-Encoding 헤더에만 존재할 수 있고 클라이언트에 의해 다른 콘텐츠 인코딩 알고리즘에 대한 상대적 선호도를 정의하는데 이용할 수 있다.

## 전송 인코딩과 청크 인코딩
콘텐츠 인코딩은 콘텐츠 포맷과 긴밀하게 연관되어 있다. 전송 인코딩 또한 엔터티 본문에 적용되는 가역적 변환이지만, 그들은 구조적인 이유 때문에 적용되는 것이며 콘텐츠의 포맷과는 독립적이다. 메시지 데이터가 네트워크를 통해 전송되는 방법을 바꾸기 위해 전송 인코딩을 메시지에 적용할 수 있다.

### 안전한 전송
전송 인코딩은 다른 프로토콜에서도 네트워크를 통한 '안전한 전송'을 위해 존재했다. 표준화되고 더 너그러운 전송 기반을 갖춘 HTTP는 '안전한 전송'의 초점을 다른 데에 맞추고 있다. HTTP에서 전송된 메시지의 본문이 문제를 일으킬 수 있는 이유는 몇 가지 밖에 없다.

- 알 수 없는 크기: 몇몇 게이트웨이 애플리케이션과 콘텐츠 인코더는 콘텐츠를 먼저 생성하지 않고서는 메시지 본문의 최종 크기를 판단할 수 없다.
- 보안: SSL과 같은 유명한 전송 계층 보안 방식이 있기 때문에 전송 인코딩 보안은 흔하지 않다.

### Transfer-Encoding 헤더
- Transfer-Encoding: 안전한 전송을 위해 어떤 인코딩이 메시지에 적용되었는지 수신자에게 알려준다.
- TE: 어떤 확장된 전송 인코딩을 사용할 수 있는지 서버에게 알려주기 위해 요청 헤더에 사용한다.

### 청크 인코딩
청크 인코딩은 메시지를 일정 크기의 청크 여럿으로 쪼갠다. 서버는 각 청크를 순차적으로 보낸다. 청크 인코딩을 이용하면 메시지를 보내기 전에 전체 크기를 알 필요가 없어진다. 본문이 동적으로 생성됨에 따라, 서버는 그중 일부를 버퍼에 담은 뒤 그 한 청크를 그것의 크기와 함께 보낼 수 있다. 본문 전체를 모두 보낼 때까지 이 단계를 반복한다.

#### 청크와 지속 커넥션
지속 커넥션에서는, 본문을 쓰기 전에 반드시 Content-Length 헤더에 본문의 길이를 담아서 보내줘야 한다. 콘텐츠가 서버에서 동적으로 생성되는 경우에는, 보내기전에 본문의 길이를 알아내는 것이 불가능 할 것이다. 청크 인코딩은 서버가 본문을 여러 청크로 쪼개 보낼 수 있게 해줌으로써 이 딜레마에 대한 해법을 제공한다. 동적으로 본문이 생성되면서, 서버는 그중 일부를 버퍼에 담은 뒤 그 한 덩어리를 그의 크기와 함께 보낼 수 있다. 본문을 모두 보낼때까지 이 단계를 반복한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/03ed3f90-c0fe-48d7-bc68-d446961dd559)

#### 청크 인코딩된 메시지의 트레일러
- 클라이언트의 TE 헤더가 트레일러를 받아들일 수 있음을 나타내고 있는 경우
- 트레일러가 응답을 만든 서버에 의해 추가되었으며, 트레일러의 콘텐츠는 클라이언트가 이해하고 사용할 필요가 없는 선택적인 메타 데이터이므로 클라이언트가 무시하고 버려도 되는 경우

### 콘텐츠와 전송 인코딩의 조합
콘텐츠 인코딩과 전송 인코딩은 동시에 사용될 수 있다.

### 전송 인코딩 규칙
- 전송 인코딩의 집합은 반드시 'chunked'를 포함해야 한다. 유일한 예외는 메시지가 커넥션의 종료로 끝나는 경우뿐이다.
- 청크 전송 인코딩이 사용되었다면, 메시지 본문에 적용된 마지막 전송 인코딩이 존재해야 한다.
- 청크 전송 인코딩은 반드시 메시지 본문에 한 번 이상 적용되어야 한다.

## 시간에 따라 바뀌는 인스턴스
HTTP 프로토콜은 어떤 특정한 종류의 요청이나 응답을 다루는 방법들을 정의하는데, 이것은 인스턴스 조작이라 불리며 객체의 인스턴스에 작용한다. 대표적인 두 가지가 범위 요청과 델타 인코딩이다. 클라이언트가 자신이 갖고 있는 리소스의 사본이 서버가 갖고 있는 것과 정확히 같은지 판단하고, 상황에 따라서는 새 인스턴스를 요청할 수 있는 능력을 가질 것을 요구한다.

## 검사기와 신선도
문서가 클라이언트에서 만료되면 클라이언트는 반드시 서버에게 최신 사본을 요구해야한다. 만약 서버에서도 문서가 변경되지 않았다면 클라이언트는 다시 받을 필요가 없다. 조건부 요청이라고 불리는 특별한 요청은 클라이언트가 서버에게 자신이 갖고 있는 버전을 말해주고 검사기를 사용해 자신의 사본 버전이 더 이상 유효하지 않을 때만 사본을 보내달라고 요청하는 것이다.

### 신선도
서버는 클라이언트에게 얼마나 오랫동안 콘텐츠를 캐시하고 그것이 신선하다고 가정할 수 있는지에 대한 정보를 줄 것 이다. 서버는 Expires나 Cache-Control 헤더를 통해 이러한 정보를 제공할 수 있다.

### 조건부 요청과 검사기
캐시의 사본이 요청되었을 때 그것이 더 이상 신선하지 않다면 캐시는 자신이 갖고 있는 사본을 신선한 것으로 만들 필요가 있다. 캐시는 원 서버에서 현 시점의 사본을 가져올 수 있지만, 대개 서버에 있는 문서는 여전히 캐시에 들어있는 신선하지 못한 사본과 같을 것이다. 만약 서버의 문서가 캐시가 갖고 있는 것과 같음에도 불구하고 항상 그 문서를 가져온다면 캐시는 네트워크의 대역폭을 낭비하고, 캐시와 서버에 불필요한 부하를 주고, 모든것을 느려지게 만든다.

이를 고치기 위해, HTTP는 클라이언트에게 리소스가 바뀐 경우에만 사본을 요청하는 조건부 요청이라 불리는 특별한 요청을 할 수 있는 방법을 제공한다.

HTTP 검사기를 약한 검사기와 강한 검사기의 두 가지로 분류한다. 약한 검사기는 리소스의 인스턴스를 고유하게 식별하지 못하는 경우도 있다. 강한 검사기는 언제나 고유하게 식별한다. 약한 검사기의 예로 객체의 바이트 단위 크기가 있다. 리소스 콘텐츠는 크기가 같더라도 내용이 다를 수 있으므로, 바이트의 개수를 세는 방식으로 동작하는 가상의 횟수 검사기는 변경이 발생했음을 약하게만 감지할 수 있다. 그러나 리소스의 콘텐츠에 대한 암호 체크섬은 강한 검사기다.

## 범위 요청
HTTP는 클라이언트가 문서의 일부분이나 특정 범위만 요청할 수 있도록 해준다. 범위 요청을 이용하면, HTTP 클라이언트는 받다가 실패한 엔터티를 일부 혹은 범위로 요청함으로써 다운로드를 중단된 시점에서 재개할 수 있다. Range 헤더는 피어 투 피어 파일 공유 클라이언트가 멀티미디어 파일의 다른 부분을 여러 다른 피어로부터 동시에 다운로드 받을 때도 널리 사용된다. 범위 요청은 객체의 특정 인스턴스를 클라이언트와 서버 사이에서 교환하는 것이기 때문에, 인스턴스 조작의 일종이라는 것에 주의해야 한다.

## 델타 인코딩
델타 인코딩은 객체 전체가 아닌 변경된 부분에 대해서만 통신하여 전송량을 최적화하는, HTTP 프로토콜의 확장이다. 델타 인코딩은 일종의 인스턴스 조작인데, 왜냐하면 어떤 객체의 특정 인스턴스들에 대한 클라이언트와 서버 사이의 정보 교환에 의존하기 때문이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/cf56295a-44ef-4a8a-8b4c-91cc3a6a4eba)

1. 클라이언트는 페이지의 어떤 버전을 갖고 있는지 서버에게 말해줘야 한다.
2. 클라이언트는 자신이 갖고 있는 현재 버전에 델타를 적용하기 위해 어떤 알고리즘을 알고 있는지 서버에게 말해줘야 한다.
3. 서버는 클라이언트가 갖고 있는 버전을 갖고 있는지, 어떻게 최신 버전과 클라이언트 버전에 델타를 계산할것인지 체크해야 한다.
4. 델타를 계산해서 클라이언트에게 보내주고, 서버가 델타를 보내고 있음을 클라이언트에게 알려주고 페이지의 최신 버전에 대한 새 식별자를 명시해야 한다.
5. 클라이언트는 자신이 갖고 있는 버전에 대한 유일한 식별자를 If-None-Match 헤더에 담는다. (서버에게 내가 갖고있는 최신 버전의 페이지가 이것과 같은 ETag를 갖고 있지 않다면, 최신 버전의 페이지를 보내달라고 말하는 클라언트의 방식이다)
6. If-None-Match 헤더에 의해 서버는 클라이언트에게 페이지의 최신 버전 전체를 보내게 될 것 이다.
7. 클라이언트는 서버에게 A-IM 헤더를 보내서 페이지에 대한 델타를 받아들일 수 있음을 알려줄 수도 있다.
8. 서버는 클라이언트에게 요청한 객체에 대해 객체 자체가 아닌 인스턴스 조작을 보내고 있음을 말해주는 특별한 응답 코드 델타를 계산하기 위해 사용된 알고리즘을 명시한 IM 헤더, 새 ETag 헤더, 그리고 델타를 계산할 때 기반이 된 문서의 ETag를 지정한 Delta-Base 헤더를 되돌려준다.

### 인스턴스 조작, 델타 생성기 그리고 델타 적용기
클라이언트는 A-IM 헤더를 사용해서 자신이 받아들일 수 있는 인스턴스 조작의 종류를 명시할 수 있다.
서버는 IM 헤더에 사용한 인스턴스 조작의 종류를 명시할 수 있다.

|vcdiff|vcdiff 알고리즘을 이용한 델타|
|---|---|
|diffe|유닉스 diff -e 명령을 이용한 델타|
|gdiff|gdiff 알고리즘을 이용한 델타|
|gzip|gzip 알고리즘을 이용한 압축|
|deflate|feflate 알고리즘을 이용한 압축|
|range|현재 응답이 범위 선택에 대한 결과인 부분콘텐츠임을 말해주기 위해 서버 응답에서 사용된다| 
|identity|클라이언트가 identity 인스턴스 조작을 받아들일 의사가 있음을 말해주기 위해 클라이언트 요청의 A-IM 헤더에서 사용된다| 

델타 인코딩은 전송 시간을 줄일 수 있지만 구현하기가 까다로울 수 있다.

델타 인코딩을 지원하는 서버는 자신이 제공하는 페이지가 변경되는 매 순간의 사본을 모두 유지하고 있어야한다. 클라이언트가 요청 보냈을 때 클라이언트가 갖고 있는 사본과 최신 사본간의 차이점을 알아 낼 수 있기 때문이다. 문서를 제공하는데 걸리는 시간이 줄어드는 대신, 서버는 문서의 과거 사본을 모두 유지하기 위해 디스크 공간을 더 늘려야한다. 이는 전송량 감소로 얻은 이득은 금방 무의미하게 만든다.