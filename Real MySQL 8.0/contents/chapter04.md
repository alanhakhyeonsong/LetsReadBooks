# 4장. 아키텍처

## MySQL 엔진 아키텍처

![MySQL의 전체 구조](https://velog.velcdn.com/images/songs4805/post/9d878e36-5f24-448e-8cbb-08f638ec87b8/image.png)

![](https://velog.velcdn.com/images/songs4805/post/1e86c697-7c1b-409a-aeb9-fa4f90fa1342/image.png)

- MySQL 엔진: 클라이언트로부터의 접속 및 쿼리 요청을 처리하는 커넥션 핸들러와 SQL 파서 및 전처리기, 쿼리의 최적화된 실행을 위한 옵티마이저가 중심을 이룬다.
- MySQL 스토리지 엔진: 실제 데이터를 디스크 스토리지에 저장하거나 디스크 스토리지로부터 데이터를 읽어오는 부분. MySQL 서버에서 MySQL 엔진은 하나지만 스토리지 엔진은 여러 개를 동시에 사용할 수 있다. 테이블 마다 스토리지 엔진을 선택할 수 있으나, InnoDB가 압도적이며 기본값이다.
- 핸들러 API: MySQL 스토리지 엔진이 MySQL 엔진을 위해 열어놓은 API. 스토리지 엔진 자체.

### MySQL 스레딩 구조
![](https://velog.velcdn.com/images/songs4805/post/4edeb7b0-b4c2-405f-ac94-ac1cba1b78c5/image.jpg)

- 포그라운드 스레드(클라이언트 스레드): 연결 요청이 왔을 때 처리를 위해 할당하는 스레드. 다른 스레드와 독립적
- 백그라운드 스레드: MyISAM은 사용자 스레드가 쓰기 스레드 역할까지 담당해서 응답시간이 늦다. InnoDB는 백그라운드 쓰기를 담당한다.
  - Insert Buffer를 병합하는 스레드
  - **로그를 디스크로 기록하는 스레드**
  - **InnoDB 버퍼 풀의 데이터를 디스크에 기록하는 스레드**
  - 데이터를 버퍼로 읽어 오는 스레드
  - 잠금이나 데드락을 모니터링하는 스레드

> **사용자의 요청을 처리하는 도중 데이터의 쓰기 작업은 지연(버퍼링)되어 처리될 수 있지만 데이터의 읽기 작업은 절대 지연될 수 없다.** 그래서 일반적인 상용 DBMS에는 대부분 쓰기 작업을 버퍼링해서 일괄 처리하는 기능이 탑재되어 있으며, InnoDB 또한 이런 방식으로 처리한다. 하지만 MyISAM은 그렇지 않고 사용자 쓰레드가 쓰기 작업까지 함께 처리하도록 설계되어 있다. 따라서 InnoDB는 `INSERT`, `UPDATE`, `DELETE` 쿼리로 데이터가 변경되는 경우 데이터가 디스크의 데이터 파일로 완전히 저장될 때 까지 기다리지 않아도 된다. 하지만 MyISAM에서 일반적인 쿼리는 쓰기 버퍼링 기능을 사용할 수 없다.

### 메모리 할당 및 사용 구조
- 글로벌 메모리 영역: 시스템 변수에 설정된 만큼 OS로부터 할당받는 모든 스레드가 공유하는 공간. 할당 시 주의 필요. 테이블 캐시, InnoDB 버퍼 풀, InnoDB 어댑티브 해시 인덱스, InnoDB 리두 로그 버퍼.
- 로컬 메모리 영역(세션 메모리 영역): 사용자 스레드가 할당되어 요청을 처리하는 과정에서 스레드마다 독립적으로 할당되는 공간. 정렬 버퍼, 조인 버퍼, 바이너리 로그 캐시, 네트워크 버퍼.

### 플러그인
MySQL 서버 전체에 적용가능한 기능. 완전히 새로운 기능을 플러그인을 이용해 구현 가능하다. 핵심은 최소한 MySQL 엔진이 각 스토리지 엔진에게 데이터를 읽어오거나 저장하도록 명령하려면 반드시 핸들러를 통해야 한다는 점만 기억하자.

### 컴포넌트
플러그인과 비슷한 역할을 하나, 플러그인의 다음과 같은 단점을 보완해서 구현됐다.
  - 플러그인은 오직 MySQL 서버와 인터페이스할 수 있고, 플러그인끼리는 통신할 수 없음
  - 플러그인은 MySQL 서버의 변수나 함수를 직접 호출하기 때문에 안전하지 않음(캡슐화 안 됨)
  - 플러그인은 상호 의존 관계를 설정할 수 없어서 초기화가 어려움

### 쿼리 실행 구조
![](https://velog.velcdn.com/images/songs4805/post/3bd9470e-efb8-4259-9aaf-0c1ee907386c/image.jpeg)

- 토큰: MySQL이 인식 가능한 최소 단위의 어휘, 기호
- 쿼리 파서: 기본 문법 오류 검증. 사용자 요청으로 들어온 쿼리 문장을 토큰으로 분리해 트리 형태의 구조로 만들어 내는 작업을 의미한다.
- 전처리기: 토큰에 사용된 테이블 이름, 칼럼 이름, 내장 함수 등이 실제로 존재하는지, 접근 권한이 있는지 검증.
- 옵티마이저: DBMS의 두뇌. 쿼리 최적화를 담당한다. 9장에서 확인하자.
- 실행 엔진: 스토리지 엔진이 제공하는 핸들러 API를 호출하고, 결과를 다음 핸들러 API를 호출하며 전달하는 등 요청과 결과를 연결시킨다.

### 복제
MySQL 서버에서 복제(Replication)는 매우 중요한 역할을 담당하며, 지금까지 MySQL 서버에서 복제는 많은 발전을 거듭해왔다. 16장에서 확인하자.

### 쿼리 캐시
빠른 응답이 필요한 환경에서 SQL 실행 결과를 메모리에 캐시했다 빠르게 응답하는 용도. 캐싱 유지 및 관리 비용으로 인한 성능 저하로 인해 8.0에서 제거됨.  
쿼리 캐시는 테이블의 데이터가 변경되면 캐시에 저장된 결과 중에서 변경된 테이블과 관련된 것들은 모두 삭제해야 했다. 이는 **심각한 동시 처리 성능 저하를 유발**한다.

### 스레드 풀
MySQL 서버 엔터프라이즈 에디션은 Thread Pool 기능을 제공하지만, 커뮤니티 에디션은 지원하지 않는다.  
// Percona Server에서 제공하는 스레드 풀 기능을 간단히 살펴보자.

MySQL 서버 엔터프라이즈 스레드 풀 기능은 MySQL 서버 프로그램에 내장되어 있지만, Percona Server의 스레드 풀은 플러그인 형태로 작동하게 구현돼 있다는 차이점이 있다.

**스레드 풀은 내부적으로 사용자의 요청을 처리하는 스레드 개수를 줄여서 동시 처리되는 요청이 많다 하더라도 MySQL 서버의 CPU가 제한된 개수의 스레드 처리에만 집중할 수 있게 해서 서버의 자원 소모를 줄이는 것이 목적이다.**

스레드 풀만 설치하면 성능이 그냥 두 배 쯤 올라가는 게아니라, 스케줄링 과정에서 CPU 시간을 제대로 확보하지 못하는 경우에는 역으로 쿼리 처리가 더 느려질 수 있다. 제한된 수의 스레드만으로 CPU가 처리하도록 적절히 유도한다면 CPU의 프로세서 친화도도 높이고 운영체제 입장에서는 불필요한 컨텍스트 스위치를 줄여 오버헤드를 낮출 수 있다.

스레드 풀의 타이머 스레드는 주기적으로 스레드 그룹의 상태를 체크해서 `thread_pool_max_threads` 시스템 변수에 정의된 밀리초만큼 작업 스레드가 지금 처리 중인 작업을 끝내지 못하면 새로운 스레드를 생성해서 스레드 그룹에 추가한다. 이때 전체 스레드 풀에 있는 스레드 개수는 `thread_pool_max_threads` 시스템 변수에 설정된 개수를 넘어설 수 없다. 응답 시간에 아주 민감한 서비스라면 `thread_pool_max_threads` 시스템 변수를 적절히 낮춰 설정해야 한다.

Percona Server의 스레드 풀 플러그인은 선순위 큐와 후순위 큐를 이용해 특정 트랜잭션이나 쿼리를 우선적으로 처리할 수 있는 기능도 제공한다. 이렇게 먼저 시작된 트랜잭션 내에 속한 SQL을 빨리 처리해주면 해당 트랜잭션이 가지고 있던 잠금이 빨리 해제되고 잠금 경합을 낮춰 전체적인 처리 성능을 향상시킬 수 있다.

## InnoDB 스토리지 엔진 아키텍처
독보적인 스토리지 엔진으로, MVCC를 지원하여 높은 동시성 처리가 가능하다.

![](https://velog.velcdn.com/images/songs4805/post/2977e2ca-d33a-45a7-8f77-17d8015493a2/image.jpeg)

### 프라이머리 키에 의한 클러스터링
**InnoDB의 모든 테이블은 기본적으로 PK를 기준으로 클러스터링되어 저장된다.** PK가 클러스터링 인덱스이기 때문에 PK를 이용한 레인지 스캔은 상당히 빨리 처리될 수 있다. 결과적으로 쿼리의 실행 계획에서 PK는 기본적으로 다른 보조 인덱스에 비해 비중이 높게 설정된다. (쿼리의 실행 계획에서 다른 보조 인덱스보다 PK가 선택될 확률이 높음)

InnoDB 스토리지 엔진과는 달리 MyISAM 스토리지 엔진에서는 클러스터링 키를 지원하지 않는다. 따라서 MyISAM 테이블에서는 PK와 세컨더리 인덱스는 구조적으로 아무런 차이가 없다. PK는 유니크 제약을 가진 세컨더리 인덱스일 뿐이다.

### 외래키 지원
InnoDB에서 FK는 부모 테이블과 자식 테이블 모두 해당 칼럼에 인덱스 생성이 필요하고, 변경 시에는 반드시 부모 테이블이나 자식 테이블에 데이터가 있는지 체크하는 작업이 필요하므로 잠금이 여러 테이블로 전파되고, 그로 인해 데드락이 발생할 때가 많으므로 개발할 때도 외래 키의 존재에 주의하는 것이 좋다.

### MVCC(Multi Version Concurrency Control)
**일반적으로 레코드 레벨의 트랜잭션을 지원하는 DBMS가 제공하는 기능이며, MVCC의 가장 큰 목적은 잠금을 사용하지 않는 일관된 읽기를 제공하는 데 있다. InnoDB는 언두 로그(Undo log)를 이용해 이 기능을 구현한다.** 여기서 멀티 버전이라 함은 하나의 레코드에 대해 여러 개의 버전이 동시에 관리된다는 의미다.

쉽게 말하면, Undo log를 이용해 격리 수준에 따른 읽기 기능을 제공한다. 이를 통해 Non-Locking Consistent Read가 가능함.

// 비관적 락 vs 낙관적 락

### 잠금 없는 일관된 읽기(Non-Locking Consistent Read)
**InnoDB 스토리지 엔진은 MVCC 기술을 이용해 잠금을 걸지 않고 읽기 작업을 수행한다.** 잠금을 걸지 않기 때문에 InnoDB에서 읽기 작업은 다른 트랜잭션이 가지고 있는 잠금을 기다리지 않고, 읽기 작업이 가능하다. 격리 수준이 `SERIALIZABLE`이 아닌 `READ_UNCOMMITTED`, `READ_COMMITTED`, `REPEATABLE_READ` 수준인 경우 **`INSERT`와 연결되지 않은 순수한 읽기 작업은 다른 트랜잭션의 변경 작업과 관계없이 항상 잠금을 대기하지 않고 바로 실행된다.**

InnoDB에선 변경되기 전의 데이터를 읽기 위해 언두 로그를 사용한다.

오랜 시간 동안 활성 상태인 트랜잭션으로 인해 MySQL 서버가 느려지거나 문제가 발생할 때가 가끔 있는데, 바로 이러한 일관된 읽기를 위해 언두 로그를 삭제하지 못하고 계속 유지해야 하기 때문에 발생하는 문제다. 따라서 **트랜잭션이 시작됐다면 가능한 한 빨리 롤백이나 커밋을 통해 트랜잭션을 완료하는 것이 좋다.**

### 자동 데드락 감지
InnoDB 스토리지 엔진은 내부적으로 잠금이 교착 상태에 빠지지 않았는지 체크하기 위해 잠금 대기 목록을 그래프(Wait-for List) 형태로 관리한다. InnoDB 스토리지 엔진은 데드락 감지 스레드를 가지고 있어 데드락 감지 스레드가 주기적으로 잠금 대기 그래프를 검사해 교착 상태에 빠진 트랜잭션들을 찾아 그중 하나를 강제 종료한다. 어느 트랜잭션을 먼저 강제 종료할 것인지를 판단하는 기준은 트랜잭션의 언두 로그 양이며, 언두 로그 레코드를 더 적게 가진 트랜잭션이 일반적으로 롤백의 대상이 된다.

이는 롤백을 해도 언두 처리를 해야 할 내용이 적다는 것이며, 트랜잭션 강제 롤백으로 인한 MySQL 서버의 부하도 덜 유발하기 때문이다.

동시 처리 스레드가 매우 많은 경우 데드락 감지 스레드는 더 많은 CPU 자원을 소모할 수도 있다.

### 자동화된 장애 복구
InnoDB에는 손실이나 장애로부터 데이터를 보호하기 위한 여러 가지 매커니즘이 탑재돼 있다. 이를 통해 MySQL 서버가 시작될 때 완료되지 못한 트랜잭션이나 디스크에 일부만 기록된 데이터 페이지 등에 대한 일련의 복구 작업이 자동으로 진행된다.

MySQL 서버의 설정 파일에 `innodb_force_recovery` 시스템 변수를 설정해서 MySQL 서버를 시작해야 한다. 이 설정값은 서버가 시작될 때 InnoDB 스토리지 엔진이 데이터 파일이나 로그 파일의 손상 여부 검사 과정을 선별적으로 진행할 수 있게 한다.
- InnoDB의 로그 파일이 손상됐다면 6으로 설정하고 MySQL 서버를 기동한다.
- InnoDB 테이블의 데이터 파일이 손상됐다면 1로 설정하고 MySQL 서버를 기동한다.
- 어떤 부분이 문제인지 알 수 없다면 설정값을 1~6 까지 변경하면서 재시작 해본다. 이와 같이 진행했음에도 MySQL 서버가 시작되지 않으면 백업을 이용해 다시 구축하는 방법밖에 없다.

### InnoDB 버퍼 풀
**InnoDB 스토리지 엔진 중 가장 핵심적인 부분으로, 디스크의 데이터 파일이나 인덱스 정보를 메모리에 캐시해 두는 공간이다.** 쓰기 작업을 지연시켜 일괄 작업으로 처리할 수 있게 해주는 버퍼 역할도 같이 한다. 변경된 데이터를 모아 처리하여 랜덤한 디스크 작업의 횟수를 줄인다.

InnoDB 스토리지 엔진은 버퍼 풀이라는 거대한 메모리 공간을 페이지 크기(`innodb_page_size` 시스템 변수에 설정된)의 조각으로 쪼개어 InnoDB 스토리지 엔진이 데이터를 필요로 할 때 해당 데이터 페이지를 읽어 각 조각에 저장한다.  
버퍼 풀의 페이지 크기 조각을 관리하기 위해 InnoDB 스토리지 엔진은 크개 LRU 리스트와 플러시 리스트, 프리 리스트라는 3개의 자료 구조를 관리한다.

버퍼 풀과 리두 로그는 매우 밀접한 관계를 맺고 있다. InnoDB의 버퍼 풀은 서버의 메모리가 허용하는 만큼 크게 설정하면 할수록 쿼리의 성능이 빨라진다. InnoDB 버퍼 풀은 데이터베이스 서버의 성능 향상을 위해 데이터 캐시와 쓰기 버퍼링이라는 두 가지 용도가 있는데, 버퍼 풀의 메모리 공간만 단순히 늘리는 것은 데이터 캐시 기능만 향상시키는 것이다. 쓰기 버퍼링 기능까지 향상시키려면 리두 로그와의 관계를 먼저 이해해야 한다.  

버퍼 풀은 디스크에서 읽은 상태로 전혀 변경되지 않은 클린 페이지와 함께 `INSERT`, `UPDATE`, `DELETE` 명령으로 변경된 데이터를 가진 더티 페이지도 가지고 있다. 더티 페이지는 디스크와 메모리(버퍼 풀)의 데이터 상태가 다르기 때문에 언젠가는 디스크로 기록돼야 한다. 하지만, 더티 페이지는 버퍼 풀에 무한정 머무를 수 있는 것은 아니다. InnoDB 스토리지 엔진에서 리두 로그는 1개 이상의 고정 크기 파일을 연결해서 순환 고리처럼 사용한다. 즉, 데이터 변경이 계속 발생하면 리두 로그 파일에 기록됐던 로그 엔트리는 어느 순간 다시 새로운 로그 엔트리로 덮어 쓰인다.  
InnoDB 스토리지 엔진은 전체 리두 로그 파일에서 재사용 가능한 공간과 당장 재사용 불가능한 공간을 구분해서 관리해야 하는데, 재사용 불가능한 공간을 활성 리두 로그라고 한다.

### 언두 로그
InnoDB 스토리지 엔진은 트랜잭션과 격리 수준을 보장하기 위해 DML로 변경되기 이전 버전의 데이터를 별도로 백업한다. 이렇게 백업된 데이터를 언두 로그(Undo log)라고 한다.

- 트랜잭션 보장: 트랜잭션이 롤백되면 트랜잭션 도중 변경된 데이터를 변경 전 데이터로 복구해야 하는데, 이때 언두 로그에 백업해둔 이전 버전의 데이터를 이용해 복구한다.
- 격리 수준 보장: 특정 커넥션에서 데이터를 변경하는 도중에 다른 커넥션에서 데이터를 조회하면 트랜잭션 격리 수준에 맞게 변경중인 레코드를 읽지 않고 언두 로그에 백업해둔 데이터를 읽어서 반환하기도 한다.

언두 로그는 InnoDB 스토리지 엔진에서 매우 중요한 역할을 담당하지만 관리 비용도 많이 필요하다.

`UPDATE` 실행 시, 이전 버전은 언두 로그에 기록 되고, 새 요청은 버퍼 풀에 저장된다. 트랜잭션 완료 시 언두 로그 내 내용은 제거된다. 트랜잭션 롤백이 수행될 경우, 언두 로그에 있는 값을 이용해 복구한다. 언두 로그가 장시간 유지되는 경우 성능에 좋지 않아 급증 여부를 모니터링 하는 것이 좋다. 비정상적 DB 요청이 많이 오거나 처리되고 있는지 확인할 수 있는 기준이 될 수 있다.

### 리두 로그 및 로그 버퍼
리두 로그는 트랜잭션의 4가지 요소인 ACID 중 D(Durable)에 해당하는 영속성과 가장 밀접하게 연관돼 있다. **리두 로그는 하드웨어나 소프트웨어 등 여러 가지 문제점으로 인해 MySQL 서버가 비정상적으로 종료됐을 때 데이터 파일에 기록되지 못한 데이터를 잃지 않게 해주는 안전장치다.**

MySQL 서버가 새로 기동될 때 리두 로그를 확인해서 반영되지 않은 데이터가 있는지 검사한다.

### 어댑티브 해시 인덱스
일반적인 '인덱스'라면 테이블에 사용자가 생성해둔 B-Tree 인덱스를 의미한다.  
하지만, 여기서 언급하는 '어댑티브 해시 인덱스(Adaptive Hash Index)'는 사용자가 수동으로 생성하는 인덱스가 아니라 **InnoDB 스토리지 엔진에서 사용자가 자주 요청하는 데이터에 대해 자동으로 생성하는 인덱스**이며, `innodb_adaptive_hash_index` 시스템 변수를 이용해서 어댑티브 해시 인덱스 기능을 활성화하거나 비활성화할 수 있다.

B-Tree 인덱스에서 특정 값을 찾는 과정은 데이터베이스 서버가 얼마나 많은 일을 하느냐에 따라 속도가 달라진다. B-Tree 인덱스에서 특정 값을 찾기 위해서는 B-Tree의 루트 노드를 거쳐 브랜치 노드, 그리고 최종적으로 리프 노드까지 찾아가야 원하는 레코드를 읽을 수 있다. 이런 작업을 동시에 몇천 개의 스레드로 실행하면 CPU는 엄청난 프로세스 스케줄링을 하게 되고 자연히 쿼리의 성능은 떨어진다.

어댑티브 해시 인덱스는 이러한 B-Tree의 검색 시간을 줄여주기 위해 도입된 기능이다.  
InnoDB 스토리지 엔진은 자주 읽히는 데이터 페이지의 키 값을 이용해 해시 인덱스를 만들고, 필요할 때마다 어댑티브 해시 인덱스를 검색해서 레코드가 저장된 데이터 페이지를 즉시 찾아갈 수 있다. B-Tree를 루트 노드부터 리프 노드까지 찾아가는 비용이 없어지고 그만큼 CPU는 적은 일을 하지만 쿼리의 성능은 빨라진다. 또한 컴퓨터는 더 많은 쿼리를 동시에 처리할 수 있게 된다.

어댑티브 해시 인덱스가 성능 향상에 크게 도움이 되지 않는 경우는 다음과 같다.
- 디스크 읽기가 많은 경우
- 특정 패턴의 쿼리가 많은 경우(조인이나 LIKE 패턴 검색)
- 매우 큰 데이터를 가진 테이블의 레코드를 폭넓게 읽는 경우

다음과 같은 경우에는 성능 향상에 많은 도움이 된다.
- 디스크의 데이터가 InnoDB 버퍼 풀 크기와 비슷한 경우(디스크 읽기가 많지 않은 경우)
- 동등 조건 검색(동등 비교와 IN 연산자)이 많은 경우
- 쿼리가 데이터 중에서 일부 데이터에만 집중되는 경우

확실한 것은 어댑티브 해시 인덱스는 데이터 페이지를 메모리(버퍼 풀) 내에서 접근하는 것을 더 빠르게 만드는 기능이기 때문에 데이터 페이지를 디스크에서 읽어오는 경우가 빈번한 데이터베이스 서버에서는 아무런 도움이 되지 않는다는 점이다.

## MyISAM 스토리지 엔진 아키텍처
![](https://velog.velcdn.com/images/songs4805/post/69a8095c-a33d-4d4b-a7e1-cce2f5f8b875/image.jpeg)

### 키 캐시
InnoDB의 버퍼 풀과 비슷한 역할을 하는 것이 MyISAM의 키 캐시(Key cache, 키 버퍼)다.  
키 캐시는 인덱스만을 대상으로 작동하며, 또한 인덱스의 디스크 쓰기 작업에 대해서만 부분적으로 버퍼링 역할을 한다. 키 캐시가 얼마나 효율적으로 작동하는지는 다음 수식으로 간단히 확인할 수 있다.

`키 캐시 히트율(Hit rate) = 100 - (Key_reads / Key_read_requests * 100)`

`Key_reads`는 인덱스를 디스크에서 읽어 들인 횟수를 저장하는 상태 변수이며, `Key_read_requests`는 키 캐시로부터 인덱스를 읽은 횟수를 저장하는 상태 변수다. 이 상태 값을 알아보려면 `SHOW GLOBAL STATUS` 명령을 사용하면 된다.

### 운영체제의 캐시 및 버퍼
MyISAM 테이블의 인덱스는 키 캐시를 이용해 디스크를 검색하지 않고도 충분히 빠르게 검색할 수 있다. 하지만 MyISAM 테이블의 데이터에 대해서는 디스크로부터의 I/O를 해결해 줄 만한 어떠한 캐시나 버퍼링 기능도 MyISAM 스토리지 엔진은 가지고 있지 않다.

따라서 MyISAM 테이블의 데이터 읽기나 쓰기 작업은 항상 OS의 디스크 읽기 또는 쓰기 작업으로 요청될 수밖에 없다. 대부분의 OS에는 디스크로부터 읽고 쓰는 파일에 대한 캐시나 버퍼링 메커니즘을 탑재하고 있기 때문에 MySQL 서버가 요청하는 디스크 읽기 작업을 위해 매번 디스크의 파일을 읽지는 않는다.

### 데이터 파일과 프라이머리 키(인덱스) 구조
InnoDB 스토리지 엔진을 사용하는 테이블은 프라이머리 키에 의해 클러스터링되어 저장되는 반면, MyISAM 테이블은 프라이머리 키에 의한 클러스터링 없이 데이터 파일이 힙(Heap) 공간처럼 활용된다.  
MyISAM 테이블에 레코드는 프라이머리 키 값과 무관하게 `INSERT` 되는 순서대로 데이터 파일에 저장된다. 그리고 MyISAM 테이블에 저장되는 레코드는 모두 `ROWID`라는 물리적인 주솟값을 가지는데, PK와 세컨더리 인덱스는 모두 데이터 파일에 저장된 레코드의 `ROWID` 값을 포인터로 가진다.

## MySQL 로그 파일
로그 파일을 이용하면 MySQL 서버의 깊은 내부 지식이 없어도 상태나 부하를 일으키는 원인을 쉽게 찾아 해결할 수 있다. MySQL 서버에 문제가 생겼을 때는 다음 로그 파일들을 자세히 확인하도록 하자.

### 에러 로그 파일
MySQL 설정 파일(my.cnf)에서 `log_error`라는 이름의 파라미터로 정의된 경로에 생성된다. 다음 메시지들을 가장 자주 보게 될 것이다.
- MySQL이 시작하는 과정과 관련된 정보성 및 에러 메시지
- 마지막으로 종료할 때 비정상적으로 종료된 경우 나타나는 InnoDB의 트랜잭션 복구 메시지
- 쿼리 처리 도중에 발생하는 문제에 대한 에러 메시지
- 비정상적으로 종료된 커넥션 메시지
- InnoDB의 모니터링 또는 상태 조회 명령의 결과 메시지
- MySQL의 종료 메시지

### 제너럴 쿼리 로그 파일(제너럴 로그 파일, General log)
쿼리 로그 파일에는 시간 단위로 실행됐던 쿼리의 내용이 모두 기록된다. 슬로우 쿼리 로그와는 조금 다르게 제너럴 쿼리 로그는 실행되기 전에 MySQL이 쿼리 요청을 받으면 바로 기록하기 때문에 쿼리 실행 중에 에러가 발생해도 일단 로그 파일에 기록된다.

### 슬로우 쿼리 로그
MySQL 서버의 쿼리 튜닝은 크게 서비스가 적용되기 전에 전체적으로 튜닝하는 경우와 서비스 운영 중에 MySQL 서버의 전체적인 성능 저하를 검사하거나 정기적인 점검을 위한 튜닝으로 나눌 수 있다. 후자의 경우 문제의 쿼리가 어떤 것인지 판단하기 힘들기 때문에 슬로우 쿼리 로그가 상당히 많은 도움이 된다.

슬로우 쿼리 로그 파일에는 `long_query_time` 시스템 변수에 설정한 시간 이상의 시간이 소요된 쿼리가 모두 기록된다. 슬로우 쿼리 로그는 MySQL이 쿼리를 실행한 후, 실제 소요된 시간을 기준으로 슬로우 쿼리 로그에 기록할지 여부를 판단하기 때문에 반드시 쿼리가 정상적으로 실행이 완료돼야 기록될 수 있다.

`log_output` 옵션을 이용해 슬로우 쿼리 로그를 파일로 기록할지 테이블로 기록할지 선택할 수 있다.

로그 파일의 분석이 완료되면 그 결과는 다음과 같이 3개의 그룹으로 나뉘어 저장된다.

- 슬로우 쿼리 통계
- 실행 빈도 및 누적 실행 시간순 랭킹
- 쿼리별 실행 횟수 및 누적 실행 시간 상세 정보