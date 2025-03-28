# 15장. 구글 드라이브 설계
## 1단계: 문제 이해 및 설계 범위 확정
다음 기능의 설계에 집중해보자.

- 파일 추가. 가장 쉬운 방법은 파일을 구글 드라이브 안으로 떨구는 것.
- 파일 다운로드
- 여러 단말에 파일 동기화. 한 단말에서 파일을 추가하면 다른 단말에도 자동으로 동기화되어야 함.
- 파일 갱신 이력 조회
- 파일 공유
- 파일이 편집되거나 삭제되거나 새롭게 공유되었을 때 알림 표시
- 파일 암호화
- 파일 크기 제한: 10GB
- 일간 능동 사용자(DAU) 기준으로 천만명 예상

구글 문서 편집 및 협업 기능은 논의하지 않을 것이다.

비기능적 요구사항은 다음과 같다.
- 안정성: 저장소 시스템에서 안정성은 아주 중요하다. 데이터 손실은 발생하면 안된다.
- 빠른 동기화 속도: 파일 동기화에 시간이 너무 많이 걸리면 사용자는 인내심을 잃고 해당 제품을 더 이상 사용하지 않게 될 것이다.
- 네트워크 대역폭: 이 제품이 네트워크 대역폭을 불필요하게 많이 소모한다면 사용자는 좋아하지 않을 것이다. 모바일 데이터 플랜을 사용하는 경우라면 더욱 그렇다.
- 규모 확장성: 이 시스템은 아주 많은 양의 트래픽도 처리 가능해야 한다.
- 높은 가용성: 일부 서버에 장애가 발생하거나, 느려지거나, 네트워크 일부가 끊겨도 시스템은 계속 사용 가능해야 한다.

개략적 추정치는 다음과 같다.
- 가입 사용자는 5000만 명, 1000만명의 DAU
- 모든 사용자에게 10GB의 무료 저장공간 할당
- 매일 각 사용자가 평균 2개의 파일을 업로드한다고 가정. 각 파일의 평균 크기는 500KB
- 읽기와 쓰기의 비율은 1:1
- 필요한 저장공간 총량 = 5000만 사용자 * 10GB = 500PB
- 업로드 API QPS = 1000만 사용자 * 2회 업로드 / 24시간 / 3600초 = 약 240
- 최대 QPS = QPS * 2 = 480

## 2단계: 개략적 설계안 제시 및 동의 구하기
모든 것을 담은 한 대의 서버에서 출발해 점진적으로 요구사항에 맞춰 천만 사용자 지원이 가능한 시스템으로 발전시켜보자.

아래와 같은 구성의 서버 한 대로 시작해보자.

- 파일을 올리고 다운로드 하는 과정을 처리할 웹 서버
- 사용자 데이터, 로그인 정보, 파일 정보 등의 메타데이터를 보관할 DB
- 파일을 저장할 저장소 시스템. 파일 저장을 위해 1TB의 공간을 사용할 것

웹 서버 구축 및 MySQL을 구축한 뒤 업로드되는 파일을 저장할 `drive/` 라는 디렉터리를 준비한다. 디렉터리 안에는 네임스페이스라 불리는 하위 디렉터리들을 두는데 각 네임스페이스 안에는 특정 사용자가 올린 파일이 보관된다. 해당 파일들은 원래 파일과 같은 이름을 갖고 각 파일과 폴더는 그 상대 경로를 네임스페이스 이름과 결합하면 유일하게 식별해낼 수 있다.

### 파일 업로드 API
- 단순 업로드: 파일 크기가 작을 때 사용한다.
- 이어 올리기: 파일 사이즈가 크고 네트워크 문제로 업로드가 중단될 가능성이 높다고 생각되면 사용한다.  
  - `https://api.example.com/files/upload?uploadType=resumable`
  - data: 업로드할 로컬 파일

이어 올리기의 절차는 다음과 같다.

1. 이어 올리기 URL을 받기 위한 최초 요청 전송
2. 데이터를 업로드하고 업로드 상태 모니터링
3. 업로드에 장애가 밠애하면 장애 발생시점부터 업로드를 재시작

### 파일 다운로드 API
`https://api.example.com/files/download`

```json
{
  "path": "/recipes/soup/best_soup.txt"
}
```

### 파일 갱신 히스토리 API
`https://api.example.com/files/list_revisions`

```json
{
  "path": "/recipes/soup/best_soup.txt",
  "limit": 20 // 히스토리 길이의 최대치
}
```

위 API들은 모두 사용자 인증을 필요로 하고 HTTPS 프로토콜을 사용해야 한다. SSL을 지원하는 프로토콜을 이용하는 것은 클라이언트와 백엔드 서버가 주고받는 데이터를 보호하기 위한 것이다.

### 한 대 서버의 제약 극복
업로드되는 파일이 많아지면 결국 시스템이 가득 차게 된다. 이렇게 되면 문제를 해결하기 위해 데이터를 샤딩하여 어러 서버에 나누어 저장하는 방법이 가장 먼저 떠오른다. 급한 불은 껐지만, 서버에 장애가 생기면 데이터의 유실 위험이 있을 것이다.

대안으로 AWS S3를 사용하자.  
S3는 다중화를 지원하는데, 같은 지역 안에서 다중화를 할 수도 있고 여러 지역에 걸쳐 다중화를 할 수도 있다. 이를 활용하면 데이터 손실을 막고 가용성을 최대한 보장할 수 있다. S3 bucket은 마치 파일 시스템의 폴더와도 같은 것이다.

추 후 비슷한 문제가 발생할 수 있으니 다음 사항을 좀 더 고민해보자.
- 로드밸런서: 네트워크 트래픽을 분산하기 위해 로드밸런서를 사용한다. 트래픽을 고르게 분산할 수 있을 뿐 아니라, 특정 웹 서버에 장애가 발생하면 자동으로 해당 서버를 우회해준다.
- 웹 서버: 로드밸런서를 추가하고 나면 더 많은 웹 서버를 손쉽게 추가할 수 있다. 따라서 트래픽이 폭증해도 쉽게 대응이 가능하다.
- 메타데이터 데이터베이스: DB를 파일 저장 서버에서 분리하여 SPOF를 회피한다. 다중화 및 샤딩 정책을 적용하여 가용성과 규모 확장성 요구사항에 대응한다.
- 파일 저장소: S3를 파일 저장소로 사용하고 가용성과 데이터 무손실을 보장하기 위해 두 개 이상의 지역에 데이터를 다중화한다.

### 동기화 충돌
구글 드라이브 같은 대형 저장소 시스템의 경우 때때로 동기화 충돌이 발생할 수 있다. 여기서는 다음 전략을 사용할 것이다.  
**먼저 처리되는 변경은 성공한 것으로 보고, 나중에 처리되는 변경은 충돌이 발생한 것으로 표시하는 것이다.**

동기화 충돌 오류는 오류가 발생한 시점에 이 시스템에는 같은 파일의 두 가지 버전이 존재하게 된다. 사용자가 가지고 있는 로컬 사본과 서버에 있는 최신 버전이다. 이 상태에서 사용자는 두 파일을 하나로 합칠지 아니면 둘 중 하나를 다른 파일로 대체할지를 결정해야 한다.

동기화 충돌 오류에 관련해선 다음 글을 읽어보자.
- [Differential Synchronization](https://neil.fraser.name/writing/sync/)

### 개략적 설계안
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fd4ae655-d4a8-4bfc-a2a2-ed568a53b461)

- 블록 저장소 서버: 파일 블록을 클라우드 저장소에 업로드하는 서버다. 파일을 여러 개의 블록으로 나눠 저장하며, 각 블록에는 고유한 해시값이 할당된다. 이 해시값은 메타데이터 데이터베이스에 저장된다. 각 블록은 독립적인 객체로 취급되며 S3에 보관된다. 파일을 재구성하려면 블록들을 원래 순서대로 합쳐야 한다. 한 블록은 Dropbox의 사례처럼 최대 4MB로 생각하자.
- 클라우드 저장소: 파일은 블록 단위로 나눠져 클라우드 저장소에 보관
- 아카이빙 저장소: 오랫동안 사용되지 않은 비활성 데이터를 저장하기 위한 곳
- 메타데이터 DB: 사용자, 파일, 블록, 버전 등의 메타데이터 정보를 관리한다. 실제 파일은 클라우드에 보관하며, 이 DB에는 오직 메타 데이터만 둔다.
- 메타데이터 캐시: 성능을 높이기 위해 자주 쓰이는 메타데이터는 캐시한다.
- 오프라인 사용자 백업 큐: 클라이언트가 접속 중이 아니라서 파일의 최신 상태를 확인할 수 없을 때는 해당 정보를 이 큐에 두어 나중에 클라이언트가 접속했을 때 동기화될 수 있도록 한다.

## 3단계: 상세 설계
### 블록 저장소 서버
정기적으로 갱신되는 큰 파일들은 업데이트가 일어날 때마다 전체 파일을 서버로 보내면 네트워크 대역폭을 많이 잡아먹게 된다. 최적화를 생각해보자.

- 델타 동기화: 파일이 수정되면 전체 파일 대신 수정이 일어난 블록만 동기화하는 것.
- 압축: 블록 단위로 압축해 두면 데이터 크기를 많이 줄일 수 있다. 압축 알고리즘은 파일 유형에 따라 정한다. 텍스트 파일은 gzip이나 bzip2를 쓰고, 이미지나 비디오를 압축할 때는 다른 압축 알고리즘을 쓰는 것이다.

이 시스템에서 블록 저장소 서버는 파일 업로드에 관계된 힘든 일을 처리하는 컴포넌트다.
- 클라이언트가 보낸 파일을 블록 단위로 나눔 → 각 블록에 압축 알고리즘 적용 → 암호화
- 전체 파일을 저장소 시스템으로 보내는 대신 수정된 블록만 전송

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6cfcc9f0-d765-4f92-ad57-ffd8b72e76b0)

블록 저장소 서버에 델타 동기화 전략과 압축 알고리즘을 도입하였으므로, 네트워크 대역폭 사용량을 절감할 수 있다.

### 높은 일관성 요구사항
같은 파일이 단말이나 사용자에 따라 다르게 보이면 안된다. 메타데이터 캐시와 데이터베이스 계층에도 같은 원칙이 적용되어야 한다.

메모리 캐시는 보통 최종 일관성 모델을 지원한다. 따라서 다음 사항을 보장해야 한다.
- 캐시에 보관된 사본과 데이터베이스에 있는 원본이 일치한다.
- 데이터베이스에 보관된 원본에 변경이 발생하면 캐시에 있는 사본을 무효화한다.

RDB는 ACID를 보장하므로 강한 일관성을 보장하기 쉽다. 하지만 NoSQL은 이를 기본으로 지원하지 않으므로, 동기화 로직 안에 프로그램해 넣어야 한다. 이 설계안에서는 ACID를 기본 지원하는 RDB를 채택하도록 한다.

### 메타데이터 데이터베이스
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/92caaca3-039b-4c99-8e59-c4bf3c12d02a)

### 업로드 절차
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a6315740-5ae3-4929-b477-4d751b6b88ac)

위 시퀀스 다이어그램은 두 개의 요청이 병렬적으로 전송된 상황을 보여준다. 첫 번재 요청은 파일 메타데이터를 추가하기 위한 것이고, 두 번째 요청은 파일을 클라우드 저장소로 업로드하기 위한 것이다. 모두 같은 클라이언트가 보낸 것이다.

- 메타데이터 추가
  - 메타데이터를 DB에 저장하고 업로드 상태를 대기중으로 변경
  - 새 파일이 추가되었음을 알림 서비스에 통지
- 클라우드 저장소 업로드
  - 블록 저장소 서버는 파일을 블록 단위로 분할, 압축, 암호화 후 클라우드 저장소로 전송
  - 업로드 완료 후 클라우드 스토리지는 완료 콜백을 호출, API 서버로 전송
  - 메타데이터 DB의 파일 업로드 상태를 완료로 변경
  - 파일 업로드가 끝났음을 알림 서비스에 통지

### 다운로드 절차
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9a8a80a7-d020-49de-bb50-0fcd7dccebf3)

파일 다운로드는 파일이 새로 추가되거나 편집되면 자동으로 시작된다. 클라이언트는 다른 클라이언트가 파일을 편집하거나 추가했다는 사실을 어떻게 감지할까?

- 접속중이면 알림 서비스가 알림
- 접속중이 아니면 데이터는 캐시에 보관 후 접속 시 알림

시퀀스 다이어그램은 위와 같다.

- 알림 서비스가 클라이언트2에게 누군가 파일 변경했음을 알림
- 알림을 확인한 클라이언트2는 새로운 메타데이터를 요청하여 받아옴
- 클라이언트2는 메타데이터를 받자마자 블록 다운로드 요청 전송
- 블록 저장소 서버는 요청한 블록을 클라우드 저장소에서 가져와서 반환
- 클라이언트2는 전송된 블록을 사용하여 파일 재구성

### 알림 서비스
- long polling: Dropbox가 이 방식을 채택하고 있다.
- WebSocket: 클라이언트와 서버 사이에 지속적인 통신 채널을 제공한다. 양방향 통신이 가능하다.

본 설계안에선 long polling을 사용할 것이다. 이유는 다음과 같다.

- 양방향 통신이 필요하지 않음(서버는 파일이 변경된 사실을 클라이언트에게 알려야함. 하지만 반대 방향의 통신은 필요 X)
- 알림을 보낼 일은 자주 발생하지 않고, 보낸다 해도 단시간에 많은 양을 보낼 일은 없음
- 롱 폴링을 쓰게되면 각 클라이언트는 알림 서버와 롱 폴링용 연결을 유지하다가 파일 변경 감지 시 연결을 끊음 → 클라이언트는 메타데이터 서버와 연결해 파일의 최신 내역 다운로드 다운로드 끝 or 타임아웃 시 즉시 새 요청을 보내어 롱 폴링 연결 복원 및 유지

### 저장소 공간 절약
파일 갱신 이력을 보존하고 안정성을 보장하기 위해서는 파일의 여러 버전을 여러 데이터센터에 보관할 필요가 있다. 이런 상황에서 모든 버전을 자주 백업하게 되면 저장용량이 너무 빨리 소진될 가능성이 있다. 이런 문제를 피하고 비용을 절감하기 위해서는 보통 아래 세 가지 방법을 사용한다.

- 중복 제거(de-dupe): 중복된 파일 블록을 계정 차원에서 제거하는 방법이다. 두 블록이 같은 블록인지는 해시 값을 비교해서 판단한다.
- 지능적 백업 전략 도입
  - 한도 설정: 보관할 파일 버전 개수에 상한을 둔다. 상한에 도달하면 제일 오래된 버전은 버린다.
  - 중요한 버전만 보관: 업데이트가 자주되는 파일의 경우 업데이트마다 새로운 버전으로 관리한다면 너무 많은 버전이 만들어지므로 그 가운데 중요한 것만 골라내야 한다.
- 아카이빙 저장소 활용: 자주 쓰이지 않는 데이터는 아카이빙 저장소로 옮긴다. AWS S3 glacier 같은 저장소 이용료는 훨씬 저렴하다.

### 장애 처리
- 로드밸런서 장애: 부(secondary) 로드밸런서가 활성화되어 트래픽을 이어받아야 한다. 로드밸런서끼리는 보통 heartbeat 신호를 주기적으로 보내서 상태를 모니터링한다. 일정 시간동안 신호에 응답하지 않은 로드밸런서는 장애가 발생한 것으로 간주한다.
- 블록 저장소 서버 장애: 블록 저장소 서버에 장애가 발생했다면 다른 서버가 미완료 상태 또는 대기 상태인 작업을 이어받아야 한다.
- 클라우드 저장소 장애: S3 버킷은 여러 지역에 다중화 할 수 있으므로, 한 지역에서 장애가 발생하였다면 다른 지역에서 파일을 가져오면 된다.
- API 서버 장애: API 서버들은 무상태 서버다. 따라서 로드밸런서는 API 서버에 장애가 발생하면 트래픽을 해당 서버로 보내지 않음으로써 장애 서버를 격리할 것이다.
- 메타데이터 캐시 장애: 메타데이터 캐시 서버도 다중화한다. 따라서 한 노드에 장애가 생겨도 다른 노드에서 데이터를 가져올 수 있다. 장애가 발생한 서버는 새 서버로 교체하면 된다.
- 메타데이터 데이터베이스 장애
  - 주 데이터베이스 서버 장애: 부 데이터베이스 서버 중 하나를 주 데이터베이스 서버로 바꾸고, 부 데이터베이스 서버를 새로 하나 추가한다.
  - 부 데이터베이스 서버 장애: 다른 부 데이터베이스 서버가 읽기 연산을 처리하도록 하고 그동안 장애 서버는 새 것으로 교체한다.
- 알림 서비스 장애: 접속 중인 모든 사용자는 알림 서버와 롱 폴링 연결을 하나씩 유지한다. 따라서 알림 서비스는 많은 사용자와의 연결을 유지하고 관리해야 한다. 2012년도 Dropbox 발표 자료에 따르면 알림 서비스 서버가 관리하는 연결의 수는 100만 개가 넘는다. 한 대 서버에 장애가 발생하면 100만 명 이상의 사용자가 롱 폴링 연결을 다시 만들어야 한다. 주의할 점은 한 대 서버로 100만 개 이상의 접속을 유지하는 것은 가능하나, 동시에 100만 개 접속을 **시작**하는 것은 불가하다. 이를 복구하는 것은 상대적으로 느릴 수 있다.
- 오프라인 사용자 백업 큐 장애: 이 역시 다중화해 두어야 한다. 큐에 장애가 발생하면 구독 중인 클라이언트들은 백업 큐로 구독 관계를 재설정해야 할 것이다.

## 4단계: 마무리
만약 블록 저장소 서버를 거치지 않고 파일을 클라우드 저장소에 직접 업로드한다면, 업로드 시간이 빨라질 수 있다. 하지만 몇 가지 단점이 있다.

- 분할, 압축, 암호화 로직을 클라이언트에 두어야 하므로 플랫폼별로 따로 구현해야 한다.
- 클라이언트가 해킹 당할 가능성이 있으므로 암호화 로직을 클라이언트 안에 두는 것은 적절치 않은 선택일 수 있다.

접속상태를 관리하는 로직을 별도 서비스로 옮기는 것도 생각해 볼 만하다. 관련 로직을 알림 서비스에서 분리해내면, 다른 서비스에서도 쉽게 활용할 수 있다.