# 14장. 유튜브 설계
2020년에 조사된 유튜브에 대한 놀라운 통계자료 몇 가지를 살펴보자.

- 월간 능동 사용자 수: 20억
- 매일 재생되는 비디오 수: 50억
- 미국 성인 가운데 73%가 유튜브 이용
- 5000만 명의 창작자
- 유튜브 광고 수입은 2019년 기준 150억 달러이며 이는 전년도 대비 36%가 증가한 수치
- 모바일 인터넷 트래픽 가운데 37%를 유튜브가 점유
- 80개 언어로 이용 가능

## 1단계: 문제 이해 및 설계 범위 확정
유튜브에는 단순히 비디오를 보는 것 말고도 댓글, 비디오 공유 및 좋아요, 개인 playlist, 채널과 구독 등 다양한 기능이 있다. 적절한 질문을 통해 설계 범위를 줄여보자.

- 가장 중요한 기능? → 비디오를 올리는 기능과 시청하는 기능
- 클라이언트 지원 종류? → 모바일 앱, 웹 브라우저, 스마트 TV
- 일간 능동 사용자 수? → 500만
- 사용자가 이 제품에 평균적으로 소비하는 시간은 얼마인가? → 30분
- 다국어 지원? → 어떤 언어로도 이용 가능해야 함
- 비디오 해상도? → 현존하는 비디오 종류와 해상도 대부분을 지원해야 함
- 암호화 필요? → O
- 비디오 파일 크기 제한? → 최대 1GB로 제한하자
- AWS 등의 클라우드 서비스 활용 가능

이번 장에는 아래와 같은 기능을 갖는 비디오 스트리밍 서비스 설계에 초점을 맞출 것이다.

- 빠른 비디오 업로드
- 원활한 비디오 재생
- 재생 품질 선택 가능
- 낮은 인프라 비용
- 높은 가용성과 규모 확장성, 안정성
- 지원 클라이언트: 모바일 앱, 웹 브라우저, 스마트 TV

개략적 규모 추정치는 다음과 같다.

- 일간 능동 사용자: 500만
- 한 사용자는 하루에 평균 5개의 비디오를 시청
- 10%의 사용자가 하루에 1비디오 업로드
- 비디오 평균 크기는 300MB
- 비디오 저장을 위해 매일 새로 요구되는 저장 용량 = 500만 * 10% * 300MB = 150TB
- CDN 비용
  - 클라우드 CDN을 통해 비디오를 서비스할 경우 CDN에서 나가는 데이터의 양에 따라 과금
  - AWS CloudFront를 CDN 솔루션으로 사용할 경우, 100% 트래픽이 미국에서 발생한다 가정하면 1GB당 0.02 달러의 요금이 발생한다.
  - 문제를 단순화하기 위해 비디오 스트리밍 비용만 따지도록 한다.
  - 매일 발생하는 요금은 500만 * 5비디오 * 0.3GB * 0.02달러 = 15만 달러이다.

이 추정 결과에 따르면 CDN을 통해 비디오를 서비스하면 비용이 엄청나다. 이 비용을 줄이는 방법에 대해 상세 설계를 진행하면서 보다 자세히 알아보겠다.

## 2단계: 개략적 설계안 제시 및 동의 구하기
개략적으로 보면 이 시스템은 다음 세 개 컴포넌트로 구성된다.

- 단말(client): 컴퓨터, 모바일 폰, 스마트 TV
- CDN: 비디오는 CDN에 저장한다. 재생 버튼을 누르면 CDN으로부터 스트리밍이 이루어진다.
- API 서버: 비디오 스트리밍을 제외한 모든 요청은 API 서버가 처리한다.
  - 피드 추천, 비디오 업로드 URL 생성, 메타데이터 데이터베이스와 캐시 갱신, 사용자 가입 등

면접관이 다음의 두 영역을 설계해 줄 것을 요청했다고 가정하자. 이 각각을 개략적으로 설계해보자.
- 비디오 업로드 절차
- 비디오 스트리밍 절차

### 비디오 업로드 절차
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/35811c22-cf22-4cbf-9218-7dbe11171cef)

이 설계안은 다음의 컴포넌트들로 구성되어 있다.

- 사용자
- 로드밸런서: API 서버 각각으로 고르게 요청을 분산한다.
- API 서버
- 메타데이터 데이터베이스: 비디오의 메타데이터를 보관한다. 샤딩과 다중화를 적용하여 성능 및 가용성 요구사항을 충족한다.
- 메타데이터 캐시: 성능을 높이기 위해 비디오 메타데이터와 사용자 객체는 캐시한다.
- 원본 저장소: 원본 비디오를 보관할 대형 이진 파일 저장소(BLOB, Binary Large Object Storage) 시스템
- 트랜스코딩 서버: 비디오 트랜스코딩은 비디오 인코딩이라 부르기도 하는 절차로, 비디오의 포맷을 변환하는 절차다. 단말이나 대역폭 요구사항에 맞는 최적의 비디오 스트림을 제공하기 위해 필요하다.
- 트랜스코딩 비디오 저장소: 트랜스코딩이 완료된 비디오를 저장하는 BLOB 저장소
- CDN: 비디오를 캐시하는 역할을 담당.
- 트랜스코딩 완료 큐: 비디오 트랜스코딩 완료 이벤트들을 보관할 메시지 큐
- 트랜스코딩 완료 핸들러: 트랜스코딩 완료 큐에서 이벤트 데이터를 꺼내 메타데이터 캐시와 데이터베이스를 갱신할 작업 서버들이다.

비디오 업로드는 다음 두 프로세스가 병렬적으로 수행된다고 보면 된다.

### 프로세스 a: 비디오 업로드
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3c48d9b9-da38-4dfe-977f-fdc6188b3534)

1. 비디오를 원본 저장소에 업로드한다.
2. 트랜스코딩 서버는 원본 저장소에서 해당 비디오를 가져와 트랜스코딩을 시작한다.
3. 트랜스코딩이 완료되면 아래 두 절차가 병렬적으로 수행된다.  
  3a. 완료된 비디오를 트랜스코딩 비디오 저장소로 업로드
  3b. 트랜스코딩 완료 이벤트를 트랜스코딩 완료 큐에 넣는다.  
    3a.1. 트랜스코딩이 끝난 비디오를 CDN에 올린다.  
    3b.1. 완료 핸들러가 이벤트 데이터를 큐에서 꺼낸다.  
    3b.1.a, 3b.1.b. 완료 핸들러가 메타데이터 데이터베이스와 캐시를 갱신한다.
4. API 서버가 단말에게 비디오 업로드가 끝나서 스트리밍 준비가 되었음을 알린다.

### 프로세스 b: 메타데이터 갱신
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2611e2e4-283f-4761-9267-dfadc50b664f)

- 원본 저장소에 파일이 업로드되는 동안, 단말은 병렬적으로 비디오 메타데이터 갱신 요청을 API 서버에 보낸다.
- 메타데이터에 포함된 데이터는 파일 이름, 크기, 포맷 등의 정보가 들어있다.
- API 서버는 이 정보로 메타데이터 캐시와 데이터베이스를 업데이트 한다.

### 비디오 스트리밍 절차
유튜브에서 비디오 재생버튼을 누르면 스트리밍은 바로 시작되며 비디오 다운로드가 완료되어야 영상을 볼 수 있다거나 하는 불편함은 없다.

- 다운로드: 비디오를 단말로 내려 받는 것
- 스트리밍: 단말기가 원격지의 비디오로부터 지속적으로 비디오 스트림을 전송 받아 영상을 재생하는 것

> 📌 스트리밍 프로토콜(streaming protocol)
> 
> 비디오 스트리밍을 위해 데이터를 전송할 때 쓰이는 표준화된 통신방법이다. 널리 쓰이는 프로토콜로는 다음과 같은 것이 있다.
> - MPEG-DASH(Moving Picture Experts Group-Dynamic Adaptive Streaming over HTTP)
> - Apple HLS(HTTP Liver Streaming)
> - Microsoft Smooth Streaming
> - Adobe HTTP Dynaming Streaming

다만 기억해야 하는 것은 프로토콜마다 지원하는 비디오 인코딩이 다르고 플레이어도 다르다는 것이다. 따라서 비디오 스트리밍 서비스를 설계할 때는 서비스의 용례에 맞는 프로토콜을 잘 골라야 한다.

비디오는 CDN에서 바로 스트리밍된다. 사용자의 단말에 가장 가까운 CDN 에지 서버가 비디오 전송을 담당할 것이다. 따라서 전송 지연은 아주 낮다.

## 3단계: 상세 설계
### 비디오 트랜스코딩
비디오를 녹화하면 단말은 해당 비디오를 특정 포맷으로 저장한다. 이 비디오가 다른 단말에서도 순조롭게 재생되려면 다른 단말과 호환되는 비트레이트와 포맷으로 저장되어야 한다.

비트레이트는 비디오를 구성하는 비트가 얼마나 빨리 처리되어야 하는지를 나타내는 단위다. 비트레이트가 높은 비디오는 일반적으로 고화질 비디오다. 비트레이트가 높은 비디오 스트림을 정상 재생하려면 보다 높은 성능의 컴퓨팅 파워가 필요하고, 인터넷 회선 속도도 빨라야 한다. 비디오 트랜스코딩은 다음과 같은 이유로 중요하다.

- 가공되지 않은 원본 비디오는 저장 공간을 많이 차지한다. 초당 60프레임으로 녹화된 HD 비디오는 수백 GB의 저장공간을 차지하게 될 수 있다.
- 상당수의 단말과 브라우저는 특정 종류의 비디오 포맷만 지원한다. 호환성 문제를 해결하려면 하나의 비디오를 여러 포맷으로 인코딩해 두는 것이 바람직하다.
- 사용자에게 끊김 없는 고화질 비디오 재생을 보장하려면, 네트워크 대역폭이 충분하지 않은 사용자에겐 저화질 비디오를, 대역폭이 충분한 사용자에게는 고화질 비디오를 보내는 것이 바람직하다.
- 모바일 단말의 경우 네트워크 상황이 수시로 달라질 수 있다. 비디오가 끊김 없이 재생되도록 하기 위해서는 비디오 화질을 자동으로 변경하거나 수동으로 변경할 수 있도록 하는 것이 바람직하다.

인코딩 포맷은 아주 다양한데, 대부분은 다음 두 부분으로 구성되어 있다.

- 컨테이너(container): 비디오 파일, 오디오, 메타데이터를 담는 바구니 같은 것이다. .avi, .mov, .mp4 같은 파일 확장자를 보면 알 수 있다.
- 코덱(codec): 비디오 화질은 보존하면서 파일 크기를 줄일 목적으로 고안된 압축 및 압축 해제 알고리즘이다. 가장 많이 사용되는 비디오 코덱으론 H.264, VP9, HEVC가 있다.

### 유향 비순환 그래프(DAG) 모델
간단하게 요약하자면, 페이스북의 스트리밍 비디오 엔진이며 작업을 단계별로 배열할 수 있도록 하여 해당 작업들이 순차적 또는 병렬적으로 실행될 수 있도록 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e6cbad8c-32cc-476a-bdda-4f48f3afd05f)

DAG 스케줄러는 DAG 그래프를 몇 개 단계로 분할한 다음 그 각각을 자원 관리자의 작업 큐에 집어넣는다.

### 시스템 최적화
#### 속도 최적화: 비디오 병렬 업로드
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/621ecc4c-f038-4326-a738-02a9d8ba26ef)

비디오 전부를 한 번의 업로드로 올리는 것은 비효율적이다. 하나의 비디오는 작은 GOP들로 분할할 수 있다. 위와 같이 분할한 GOP를 병렬적으로 업로드하면 설사 일부가 실패해도 빠르게 업로드를 재개할 수 있다. 따라서 비디오를 GOP 경계에 맞춰 분할하는 작업을 단말이 수행하면 업로드 속도를 높일 수 있다.

#### 속도 최적화: 업로드 센터를 사용자 근거리에 지정
업로드 속도를 개선하는 또 다른 방법은 업로드 센터를 여러 곳에 두는 것이다. 미국 거주자는 비디오를 북미 지역 업로드 센터로, 한국 사용자는 아시아 업로드 센터로 보내도록 하는 것이다. 이를 위해 본 설계안은 CDN을 업로드 센터로 이용한다.

#### 속도 최적화: 모든 절차를 병렬화
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/91076362-3042-41b2-a4f0-6fef64d18075)

- 낮은 응답지연을 달성하기 위해 느슨하게 결합된 시스템을 만들어 병렬성을 높인다.
- 위 시스템은 서로가 강하게 결합된 구조다.
- 메시지 큐를 도입해서 아래와 같이 느슨하게 변경할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/93244d9a-48b9-4577-821c-6d941adf1a8c)

- 메시지 큐를 도입하기 전에 인코딩 모듈은 다운로드 모듈의 작업이 끝나기를 기다려야 했다.
- 메시지 큐를 도입한 뒤에 인코딩 모듈은 다운로드 모듈의 작업이 끝나기를 더 이상 기다릴 필요가 없다. 메시지 큐에 보관된 이벤트 각각을 인코딩 모듈은 병렬적으로 처리할 수 있다.

#### 안정성 최적화: 미리 사인된 업로드 URL
허가받은 사용자만이 올바른 장소에 비디오를 업로드할 수 있도록 하기 위해, 미리 사인된 업로드 URL을 이용한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fb836a64-a2a3-42ce-ada2-9cc25e479325)

이를 위해 업로드 절차는 다음과 같이 변경한다.

1. 클라이언트는 HTTP 서버에 POST 요청을 하여 미리 사인된 URL을 받는다. 해당 URL이 가리키는 object에 대한 접근 권한이 이미 주어져 있는 상태다. (AWS S3)
2. API 서버는 미리 사인된 URL을 돌려준다.
3. 클라이언트는 해당 URL이 가리키는 위치에 비디오를 업로드한다.

#### 안정성 최적화: 비디오 보호
비디오의 저작권을 보고하기 위해, 다음 세 가지 선택지 중 하나를 채택할 수 있다.

- 디지털 저작권 관리(DRM) 시스템 도입
- AES 암호화: 비디오를 암호화하고 접근 권한을 설정하는 방식이다. 암호화된 비디오는 재생 시에만 복호화한다. 허락된 사용자만 암호화된 비디오를 시청할 수 있다.
- 워터마크: 비디오 위에 소유자 정보를 포함하는 이미지 오버레이를 올리는 것이다.

#### 비용 최적화
CDN은 데이터 크기가 크면 클수록 비싸다. 유튜브의 비디오 스트리밍은 롱테일 분포를 따른다. 인기 있는 비디오는 빈번히 재생되는 반면, 나머지는 거의 보는 사람이 없다는 것이다. 이에 착안하여 몇 가지 최적화를 시도해 볼 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/590656d4-292e-4be2-b3e0-51102ca7fb59)

1. 인기 비디오는 CDN을 통해 재생하되 다른 비디오는 비디오 서버를 통해 재생한다.
2. 인기가 별로 없는 비디오는 인코딩 할 필요가 없을 수도 있다. 짧은 비디오라면 필요할 때 인코딩하여 재생할 수 있다.
3. 어떤 비디오는 특정 지역에서만 인기가 높다. 이런 비디오는 다른 지역에 옮길 필요가 없다.
4. CDN을 직접 구축하고 인터넷 서비스 제공자(ISP)와 제휴한다.

이 모든 최적화는 콘텐츠 인기도, 이용 패턴, 비디오 크기 등의 데이터에 근거한 것이다. 최적화를 시도하기 전 시청 패턴을 분석하는 것은 중요하다.

#### 오류 처리
- 회복 가능 오류: 특정 비디오 세그먼트를 트랜스코딩하다 실패했다든가 하는 오류는 이에 속한다. 일반적으론 이런 오류는 몇 번 재시도하면 해결한다. 하지만 계속해서 실패하고 복구가 어렵다 판단되면 클라이언트에 적절한 오류 코드를 반환해야 한다.
- 회복 불가능 오류: 비디오 포맷이 잘못되었다거나 하는 회복 불가능한 오류가 발견되면 시스템은 해당 비디오에 대한 작업을 중단하고 클라이언트에게 적절한 오류 코드를 반환해야 한다.

시스템 컴포넌트 각각에 발생할 수 있는 오류에 대한 전형적 해결 방법은 다음과 같이 요약할 수 있다.

- 업로드 오류: 몇 회 재시도한다.
- 비디오 분할 오류: 낡은 버전의 클라이언트가 GOP 경계에 따라 비디오를 분할하지 못하는 경우라면 전체 비디오를 서버로 전송하고 서버가 해당 비디오 분할을 처리하도록 한다.
- 트랜스코딩 오류: 재시도한다.
- 전처리 오류: DAG 그래프를 재생성한다.
- DAG 스케줄러 오류: 작업을 다시 스케줄링한다.
- 자원 관리자 큐에 장애 발생: 사본을 이용한다.
- 작업 서버 장애: 다른 서버에서 해당 작업을 재시도한다.
- API 서버 장애: API 서버는 무상태 서버이므로 신규 요청은 다른 API 서버로 우회될 것이다.
- 메타데이터 캐시 서버 장애: 데이터는 다중화되어 있으므로 다른 노드에서 데이터를 여전히 가져올 수 있을 것이다. 장애가 난 캐시 서버는 새로운 것으로 교체한다.
- 메타데이터 데이터베이스 서버 장애
  - 주 서버가 죽었다면 부 서버 가운데 하나를 주 서버로 교체한다.
  - 부 서버가 죽었다면 다른 부 서버를 통해 읽기 연산을 처리하고 죽은 서버는 새것으로 교체한다.

## 4단계: 마무리
다음과 같은 내용을 추가적으로 논의해도 좋을 것이다.

- API 계층의 규모 확장성 확보 방안
- 데이터베이스 계층의 규모 확장성 확보 방안
- 라이브 스트리밍: 라이브 스트리밍과 비 라이브 스트리밍 시스템 간에는 비슷한 점도 많지만 가장 중요한 차이는 다음과 같다.
  - 라이브 스트리밍의 경우, 응답지연이 좀 더 낮아야 한다. 스트리밍 프로토콜 선정에 유의해야 한다.
  - 라이브 스트리밍의 경우, 병렬화 필요성은 떨어질 텐데 작은 단위의 데이터를 실시간으로 빨리 처리해야 하기 때문이다.
  - 라이브 스트리밍의 경우, 오류 처리 방법을 달리해야 한다. 너무 많은 시간이 걸리는 방안은 사용하기 어렵다.
- 비디오 삭제: 저작권을 위반한 비디오, 선정적 비디오, 불법적 행위에 관계된 비디오는 내려야 한다. 내릴 비디오는 업로드 과정에서 식별해 낼 수도 있지만, 사용자의 신고 절차를 통해 판별할 수도 있다.