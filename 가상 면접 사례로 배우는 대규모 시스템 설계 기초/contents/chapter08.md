# 8장. URL 단축기 설계
## 1단계: 문제 이해 및 설계 범위 확정
- URL 단축기 동작 예제?
  - `https://www.systeminterview.com/q=chatsystem&c=loggedin&v=v3&l=long`이 입력으로 주어졌다 가정할 때, 이 서비스는 `https://tinyurl.com/y7ke-ocwj`와 같은 단축 URL을 결과로 제공해야 한다. 이 URL에 접속하면 원래 URL로 갈 수도 있어야 한다.
- 트래픽 규모?
  - 매일 1억 개의 단축 URL을 만들어 낼 수 있어야 함.
- 단축 URL의 길이?
  - 짧으면 짧을수록 좋다.
- 단축 URL에 포함될 문자에 제한이 있는가?
  - 숫자와 영문만 사용할 수 있다.
- 단축된 URL을 시스템에서 지우거나 갱신할 수 있는가?
  - 시스템을 단순화하기 위해 삭제나 갱신은 할 수 없다 가정한다.

이 시스템의 기본적 기능은 다음과 같다.
- URL 단축: 주어진 긴 URL을 훨씬 짧게 줄인다.
- URL redirection: 축약된 URL로 HTTP 요청이 오면 원래 URL로 안내
- 높은 가용성과 규모 확장성, 그리고 장애 감내가 요구됨

개략적 추정치는 다음과 같다.
- 쓰기 연산: 매일 1억 개의 단축 URL 생성
- 초당 쓰기 연산: `1억 / 24 / 3600 = 1160`
- 읽기 연산: 읽기 연산과 쓰기 연산의 비율은 10:1이라 하자. 그 경우 읽기 연산은 초당 11,600회 발생한다.
- URL 단축 서비스를 10년간 운영한다 가정하면 `1억 * 365 * 10 = 3650억` 개의 레코드를 보관해야 한다.
- 축약 전 URL의 평균 길이는 100이라고 하자.
- 10년 동안 필요한 저장 용량은 `3650억 * 100바이트 = 36.5TB`다.

## 2단계: 개략적 설계안 제시 및 동의 구하기
### API 엔드포인트
URL 단축기는 기본적으로 두 개의 엔드포인트를 필요로 한다.

1. URL 단축용 엔드포인트  
  - 새 단축 URL을 생성하고자 하는 클라이언트는 이 엔드포인트에 단축할 URL을 인자로 실어서 POST 요청을 보내야 한다.
  - `POST /api/v1/data/shorten`
    - 인자: `{longUrl: longURLstring}`
    - 반환: 단축 URL
2. URL Redirection 엔드포인트
  - 단축 URL에 대해 HTTP 요청이 오면 원래 URL로 보내주기 위한 용도의 엔드포인트
  - `GET /api/v1/shortUrl`
    - 반환: HTTP 리다이렉션 목적지가 될 원래 URL

### URL Redirection
단축 URL을 받은 서버는 그 URL을 원래 URL로 바꾸어 301 응답의 Location 헤더에 넣어 반환한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/971a0dde-9811-4bfe-af12-98909872cc64)

유의할 점은 301 응답과 302 응답의 차이이다. 둘 다 Redirection 응답이지만 차이가 있다.

- [301 Permanently Moved](https://developer.mozilla.org/ko/docs/Web/HTTP/Status/301)
  - 이 응답은 해당 URL에 대한 HTTP 요청의 처리 책임이 영구적으로 Location 헤더에 반환된 URL로 이전되었다는 응답이다. 영구적으로 이전되었으므로, 브라우저는 이 응답을 캐시(cache)한다.
  - 추후 같은 단축 URL에 요청을 보낼 필요가 있을 때 브라우저는 캐시된 원래 URL로 요청을 보내게 된다.
  - 단축 URL 서버 부하를 줄이고 싶은 경우, 301을 사용하는 것이 좋을 수 있다.
- [302 Found](https://developer.mozilla.org/ko/docs/Web/HTTP/Status/302)
 - 이 응답은 주어진 URL로의 요청이 '일시적으로' Location 헤더가 지정하는 URL에 의해 처리되어야 한다는 응답이다. 따라서 클라이언트의 요청은 언제나 단축 URL 서버에 먼저 보내진 후 원래 URL로 Redirection 되어야 한다.
 - 트래픽 분석 등 단축 URL 서버에서 모든 요청을 받아보는 것의 유의미한 경우, 302를 사용하는 것이 좋을 수 있다.

URL Redirection을 구현하는 가장 직관적인 방법은 해시 테이블을 사용하는 것이다. 해시 테이블에 `<단축 URL, 원래 URL>`의 쌍을 저장한다 가정하면, URL Redirection은 다음과 같이 구현될 수 있을 것이다.
- 원래 URL = `hashTable.get(단축 URL)`
- 301 또는 302 응답: Location 헤더에 원래 URL을 넣은 후 전송

### URL 단축
중요한 것은 긴 URL을 해시 값으로 대응시킬 해시 함수를 찾는 것이다. 이 해시 함수는 다음 요구사항을 만족해야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fc3616ef-a533-4ec1-9f24-24ec90d63445)

- 입력으로 주어지는 긴 URL이 다른 값이면 해시 값도 달라야 한다.
- 계산된 해시 값은 원래 입력으로 주어졌던 긴 URL로 복원될 수 있어야 한다.

## 3단계: 상세 설계
### 데이터 모델
개략적 설계를 진행할 때는 모든 것을 해시 테이블에 두었다. 이 접근법은 초기 전략으론 괜찮지만, 실제 시스템에 쓰기엔 곤란하다. 메모리는 유한한 데다 비싸다.

더 나은 방법은 `<단축 URL, 원래 URL>`의 순서쌍을 RDB에 저장하는 것이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2a24057a-3b74-472b-a9ac-ee5ff2056c47)

### 해시 함수 - 해시 값 길이
해시 함수가 계산하는 단축 URL 값을 `hashValue`라고 지칭하겠다.

`hashValue`는 알파벳과 숫자로만 이루어져야 하므로 사용할 수 있는 문자는 총 62개다. `hashValue`의 길이를 정하기 위해서는 `62^n >= 3650억`에서의 n의 최솟값을 찾아야 한다.

`n = 7`이면 3.5조 개의 URL을 만들 수 있다. 요구사항을 만족시키기 충분한 값이라 길이를 7로 결정하겠다.

### 해시 함수 - 해시 후 충돌 해소
긴 URL을 줄이려면, 원래 URL을 7글자 문자열로 줄이는 해시 함수가 필요하다. 책에선 CRC32, MD5, SHA-1 함수를 언급하는데, 모두 결과가 7보다는 길다.

|해시 함수|해시 결과 (16진수)|
|--|--|
|CRC32|5cb54054|
|MD5|5a62509a84df9ee03fe1230b9df8b84e|
|SHA-1|0eeae7916c06853901d9ccbefbfcaf4de56ed85b|

이 문제를 해결할 첫 번째 방법으로 계산된 해시 값에서 처음 7개 글자만 이용하는 것이다.  
하지만, 이렇게 하면 해시 결과가 서로 충돌할 확률이 높아진다.

충돌이 실제로 발생했을 때는, 충돌이 해소될 때까지 사전에 정한 문자열을 해시값에 덧붙인다. 이 절차는 다음과 같다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/eb386ec5-2940-4754-9393-f71d842fbd1e)

이 방법을 쓰면 한 번 이상 데이터베이스 질의를 해야 하므로 오버헤드가 크다.
데이터베이스 대신 블룸 필터를 사용하면 성능을 높일 수 있다고 한다.

### 해시 함수 - base-62 변환
이 기법은 수의 표현 방식이 다른 두 시스템이 같은 수를 공유하여야 하는 경우에 유용하다. 62진법을 쓰는 이유는 `hashValue`에 사용할 수 있는 문자 개수가 62개이기 때문이다.

- 62진법은 수를 표현하기 위해 총 62개의 문자를 사용하는 진법이다. 따라서 0은 0으로, 9는 9로, 10은 a로, 61은 Z로 대응시켜 표현하도록 할 것이다.
- `11157(10진수) = 2 x 62^2 + 55 x 62 ^1 + 59 x 62 ^ 0` = [2, 55, 59] → [2, T, X] → 2TX(62진수)이다.
- 따라서 단축 URL은 `https://tinyurl.com/2TX`가 된다.

두 접근법 사이의 차이를 요약하면 다음과 같다.

|해시 후 충돌 해소 전략|base-62 변환|
|--|--|
|단축 URL의 길이가 고정됨|단축 URL의 길이가 가변적. ID 값이 커지면 같이 길어짐|
|유일성이 보장되는 ID 생성기가 필요치 않음|유일성 보장 ID 생성기가 필요|
|충돌이 가능해서 해소 전략이 필요|ID의 유일성이 보장된 후에야 적용 가능한 전략이라 충돌은 아예 불가능|
|ID로부터 단축 URL을 개선하는 방식이 아니라서 다음에 쓸 수 있는 URL을 알아내는 것이 불가능|ID가 1씩 증가하는 값이라고 가정하면 다음에 쓸 수 있는 단축 URL이 무엇인지 쉽게 알아낼 수 있어서 보안상 문제가 될 소지가 있음|

### URL 단축기 상세 설계
URL 단축기는 시스템의 핵심 컴포넌트이므로, 그 처리 흐름이 논리적으로는 단순해야 하고 기능적으로는 언제나 동작하는 상태로 유지되어야 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/dd283fb6-7783-4dd0-808c-4905db83fe07)

1. 입력으로 긴 URL을 받는다.
2. 데이터베이스에 해당 URL이 있는지 검사한다.
3. 데이터베이스에 있다면 해당 URL에 대한 단축 URL을 만든 적이 있는 것이다. 따라서 데이터베이스에서 해당 단축 URL을 가져와 클라이언트에게 반환한다.
4. 데이터베이스에 없는 경우에는 해당 URL은 새로 접수된 것이므로 유일한 ID를 생성한다. 이 ID는 데이터베이스의 기본 키로 사용된다.
5. 62진법 변환을 적용, ID를 단축 URL로 만든다.
6. ID, 단축 URL, 원래 URL로 새 데이터베이스 레코드를 만든 후 단축 URL을 클라이언트에 전달한다.

예제는 다음과 같다.
- 입력된 URL이 `https://en.wikipedia.org/wiki/Systems_design`이라고 하면
- 이 URL에 대해 ID 생성기가 반환한 ID는 2009215674938이다.
- 이 ID를 62진수로 변환하면 zn9edcu를 얻는다.

|ID|shortURL|longURL|
|--|--|--|
|2009215674938|zn9edcu|`https://en.wikipedia.org/wiki/Systems_design`|

### URL Redirection 상세 설계
쓰기보다 읽기를 더 자주 하는 시스템이라, `<단축 URL, 원래 URL>`의 쌍을 캐시에 저장하여 성능을 높였다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2c36d3b3-3842-40a2-8d14-bb3aae99e7a0)

1. 사용자가 단축 URL을 클릭한다.
2. 로드밸런서가 해당 클릭으로 발생한 요청을 웹 서버에 전달한다.
3. 단축 URL이 이미 캐시에 있는 경우에는 원래 URL을 바로 꺼내서 클라이언트에게 전달한다.
4. 캐시에 해당 단축 URL이 없는 경우에는 데이터베이스에서 꺼낸다. 데이터베이스에 없다면 아마 사용자가 잘못된 단축 URL을 입력한 경우일 것이다.
5. 데이터베이스에서 꺼낸 URL을 캐시에 넣은 후 사용자에게 반환한다.

## 4단계: 마무리
설계를 마친 후에도 다음과 같은 것들을 더 논의해보면 좋을 것이다.

- 처리율 제한 장치(rate limiter)
  - 지금까지 살펴본 시스템은 엄청난 양의 URL 단축 요청이 밀려들 경우 무력화될 수 있다는 잠재적 보안 결함을 갖고 있다. 처리율 제한 장치를 두면, IP 주소를 비롯한 필터링 규칙들을 이용해 요청을 걸러낼 수 있다.
- 웹 서버 규모 확장
  - 웹 계층은 무상태 계층이므로 웹 서버를 자유로이 증설하거나 삭제할 수 있다.
- 데이터베이스 규모 확장
  - 데이터베이스를 다중화하거나 샤딩하여 규모 확장성을 달성할 수 있다.
- 데이터 분석 솔루션
  - 성공적인 비즈니스를 위해서는 데이터가 중요하다. URL 단축기에 데이터 분석 솔루션을 통합해 두면 어떤 링크를 얼마나 많은 사용자가 클릭했는지, 언제 주로 클릭했는지 등 중요한 정보를 알아 낼 수 있을 것이다.
- 가용성, 데이터 일관성, 안정성
  - 대규모 시스템이 성공적으로 운영되기 위해서는 반드시 갖추어야 할 속성이다.