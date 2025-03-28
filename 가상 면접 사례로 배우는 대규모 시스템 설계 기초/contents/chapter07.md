# 7장. 분산 시스템을 위한 유일 ID 생성기 설계
분산 시스템에서 `auto_increment` 속성이 설정된 RDBMS의 PK를 사용하는 접근법은 통하지 않을 것이다.
- 데이터베이스 서버 한 대로 요구를 담당할 수 없다.
- 여러 데이터베이스 서버를 쓰는 경우에는 delay를 낮추기가 무척 힘들 것이다.

유일성이 보장되는 ID의 몇 가지 예를 보자.

## 1단계: 문제 이해 및 설계 범위 확정
요구 사항은 아래와 같다.
- ID는 유일해야 한다.
- ID는 숫자로만 구성되어야 한다.
- ID는 64비트로 표현될 수 있는 값이어야 한다.
- ID는 발급 날짜에 따라 정렬 가능해야 한다.
- 초당 10,000개의 ID를 만들 수 있어야 한다.

## 2단계: 개략적 설계안 제시 및 동의 구하기
분산 시스템에서 유일성이 보장되는 ID를 만드는 방법은 여러 가지인데, 다음과 같은 선택지를 살펴보자.
- 다중 마스터 복제(multi-master replication)
- UUID(Universally Unique Identifier)
- 티켓 서버(ticket server)
- 트위터 스노플레이크(twitter snowflake) 접근법

### 다중 마스터 복제
DB의 `auto_increment` 기능을 활용하는 방법이다. 다만, 다음 ID의 값을 구할 때 1만큼 증가시켜 얻는 것이 아니라, 데이터베이스 서버의 수인 k만큼 증가시킨다.

이렇게 하면, 규모 확장성 문제를 어느 정도 해결할 수 있는데, DB 수를 늘리면 초당 생산 가능 ID 수도 늘릴 수 있기 때문이다.  
하지만 다음과 같은 중대한 단점이 있다.
- 여러 데이터 센터에 걸쳐 규모를 늘리기 어렵다.
- ID의 유일성은 보장되겠지만 그 값이 시간 흐름에 맞추어 커지도록 보장할 수는 없다.
- 서버를 추가하거나 삭제할 때도 잘 동작하도록 만들기 어렵다.

### UUID
UUID는 컴퓨터 시스템에 저장되는 정보를 유일하게 식별하기 위한 128비트짜리 수다.

장점은 다음과 같다.
- 충돌 가능성이 지극히 낮다.
- 서버 간 조율 없이 독립적으로 생성 가능하다. → 동기화 이슈가 없다.
- 각 서버가 자기가 쓸 ID를 알아서 만드는 구조이므로 규모 확장도 쉽다.

단점은 다음과 같다.
- ID가 128비트로 길다.
- ID를 시간순으로 정렬할 수 없다.
- ID에 숫자가 아닌 값이 포함될 수 있다.

### 티켓 서버
이 아이디어의 핵심은 `auto_increment` 기능을 갖춘 데이터베이스 서버, 즉 티켓 서버를 중앙 집중형으로 하나만 사용하는 것이다.

장점은 다음과 같다.
- 유일성이 보장되는 오직 숫자로만 구성된 ID를 쉽게 만들 수 있다.
- 구현하기 쉽고, 중소 규모 애플리케이션에 적합하다.

단점은 다음과 같다.
- 티켓 서버가 SPOF가 된다. 서버에서 장애가 발생하면, 해당 서버를 이용하는 모든 시스템이 영향을 받는다.
  - 티켓 서버를 여러 대 준비하게 된다면, 데이터 동기화 같은 새로운 문제가 발생할 것이다.

### 트위터 스노플레이크 접근법
트위터는 스노플레이크(snowflake)라고 부르는 독창적인 ID 생성 기법을 사용한다. 이 기법은 이번 장의 요구사항을 만족시킬 수 있다.

ID는 64비트로 표현할 수 있는 값이어야 하므로 다음과 같이 설계한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/5b558360-5922-4235-9b30-95c044c668d6)

- 사인(sign) 비트: 1비트를 할당한다. 당장은 쓰임새가 없지만, 나중을 위해 유보해둔다. 음수와 양수를 구별할 수 있을 것이다.
- 타임스탬프(timestamp): 41비트를 할당한다. 기원 시각 이후로 몇 밀리초가 경과했는지 나타낸다.
- 데이터 센터 ID: 5비트를 할당한다. 32개 데이터센터를 지원할 수 있다.
- 서버 ID: 5비트를 할당한다. 데이터센터당 32개 서버를 사용할 수 있다.
- 일련번호: 12비트를 할당한다. 각 서버에는 ID를 생성할 때마다 이 일련 번호를 1만큼 증가시킨다. 1밀리초가 경과할 때마다 0으로 초기화된다.

## 3단계: 상세 설계
요구 사항에 모두 일치하는 트위터 스노플레이크 접근법을 선택한다.

데이터센터 ID와 서버 ID는 시스템이 시작할 때 결정되며, 일반적으로 시스템 운영 중에는 바뀌지 않는다. 두 개의 ID를 잘못 변경하게 되면 ID 충돌이 발생할 수 있으므로, 그런 작업을 해야할 때는 신중해야 한다.  
타임 스탬프나 일련 번호는 ID 생성기가 돌고 있는 중에 만들어지는 값이다.

### 타임스탬프
타임스탬프가 41비트일 경우 69년이 지나면 기원 시각을 바꾸거나 ID 체계를 다른 것으로 이전(migration)해야 한다.

### 일련번호
일련번호는 12비트이므로, 4096개의 값을 가질 수 있다. 어떤 서버가 같은 밀리초 동안 하나 이상의 ID를 만들어 낸 경우에만 0보다 큰 값을 갖게 된다.

## 4단계: 마무리
추가로 논의할 수 있는 사항들은 다음과 같을 것이다.

- 시계 동기화(clock synchronization): ID 생성 서버들이 전부 같은 시계를 사용한다고 가정했지만, 이는 하나의 서버가 여러 코어에서 실행될 경우 유효하지 않을 수 있다. 여러 서버가 물리적으로 독립된 여러 장비에서 실행되는 경우에도 마찬가지다.
  - NTP(Network Time Protocol)은 이 문제를 해결하는 가장 보편적 수단이다.
- 각 절(section)의 길이 최적화: 동시성이 낮고 수명이 긴 애플리케이션이라면 일련번호 절의 길이를 줄이고 타임스탬프 절의 길이를 늘리는 것이 효과적일 수도 있다.
- 고가용성: ID 생성기는 필수 불가결 컴포넌트이므로 아주 높은 가용성을 제공해야 할 것이다.