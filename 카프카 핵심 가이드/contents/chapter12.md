# Chapter 12. 카프카 운영하기
## 토픽 작업
`kafka-topics.sh` 툴은 대부분의 토픽 작업을 쉽게 할 수 있다. 이 툴을 사용해서 클러스터 내 토빅 생성, 변경, 삭제, 정보 조회를 할 수 있다. 토픽 설정 변경은 `kafka-configs.sh`를 사용할 것을 권장한다.

`kafka-topics.sh`를 사용하려면 `--bootstrap-server` 옵션에 연결 문자열과 포트를 넣어줘야 한다. 예시로 사용하는 모든 툴이 저장된 위치는 `/usr/local/kafka/bin/`이라 가정하고 사용자가 이 디렉토리에 있거나 해당 디렉토리를 `$PATH`에 추가해 놓았다 가정하자.

### 새 토픽 생성하기
`--create` 명령을 사용해서 새로운 토픽을 생성할 때 반드시 필요한 인수가 있다. 브로커 단위 기본값이 이미 설정됐다 하더라도 이 명령을 사용할 때는 지정해주어야 한다.

- `--topic`: 생성하려는 토픽의 이름
- `--replication-factor`: 클러스터 안에 유지되어야 할 레플리카의 개수
- `--partitions`: 토픽에서 생성할 파티션의 개수

```bash
$ bin/kafka-topics.sh --bootstrap-server <connection-string>:<port> \
--create --topic <string> --replication-factor <integer> --partitions <integer>
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/16bd7d39-283a-4382-a8d8-1a6cb2b17283)

토픽 이름에는 영문 혹은 숫자, `_`, `-`, `.`를 사용할 수 있다. 하지만 토픽 이름에 `.`를 사용하는 것은 권장되지 않는다. 카프카 내부적으로 사용되는 지표에선 `.`를 `_`로 변환해서 처리하는 탓에 토픽 이름에 충돌이 발생할 수 있기 때문이다. 또한 `__`로 시작하는 것 역시 권장하지 않는다. 카프카 내부에서 사용되는 토픽을 생성할 때 `__`로 시작하는 이름을 쓰는 것이 관례이기 때문이다. 예를 들면 컨슈머 그룹 오프셋을 저장하는 `__consumer_offsets` 토픽이 있다.

토픽 생성 시 같은 이름의 토픽이 이미 있다고 해서 에러를 리턴하지 않는 `--if-not-exists` 인수를 사용하는 것 또한 좋다. `--if-exists`와 `--alter`를 함께 사용하지 않도록 하자.

### 토픽 목록 조회하기
`--list` 명령은 클러스터 안의 모든 토픽을 보여준다. 출력되는 결과는 한 줄에 하나의 토픽이며, 특정한 순서는 없다.

```bash
$ bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
__consumer_offsets
my-topic
other-topic
```

`--exclude-internal`과 함께 실행하면 `__`로 시작하는 토픽들을 제외하고 보여준다.

### 토픽 상세 내역 조회하기
클러스터 안에 있는 1개 이상의 토픽에 대해 상세한 정보를 보는 것 역시 가능하다. 파티션 수, 재정의된 토픽 설정, 파티션별 레플리카 할당 역시 함께 출력된다. 하나의 토픽에 대해서만 보고 싶다면 `--topic` 인수를 지정해주도록 하자.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/7956e16d-5749-4617-afad-f27123c96e5c)

`--describe` 명령은 출력을 필터링할 수 있는 몇몇 유용한 옵션들을 갖고 있다. 이 옵션들은 클러스터에 발생한 문제를 찾을 때 도움이 된다. 단, 이 옵션들에 대해선 대개 `--topic` 인수를 지정하지 않는다. 클러스터 내에서 뭔가 기준에 부합하는 모든 토픽이나 파티션을 찾는 것이기 때문이다. 또한 `list` 명령과 함께 사용할 수 없다.

- `--topics-with-overrides`: 설정 중 클러스터 기본값을 재정의한 것이 있는 토픽들을 보여준다.
- `--exclude-internal`: 내부 토픽 앞에 붙는 `__`로 시작하는 모든 토픽들을 결과에서 제외한다.

다음 명령은 문제가 발생했을 수 있는 토픽 파티션을 찾는 데 도움이 된다.

- `--under-replicated-partitions`: 1개 이상의 레플리카가 리더와 동기화되지 않고 있는 모든 파티션을 보여준다. 클러스터 정비, 설치 혹은 리밸런스 과정에서 불완전 복제 파티션이 발생할 수 있기에 주의를 기울일 필요는 있다.
- `--at-min-isr-partitions`: 레플리카 수(리더 포함)가 인-싱크 레플리카 최소값과 같은 모든 파티션을 보여준다. 이 토픽들은 프로듀서나 컨슈머 클라이언트가 여전히 사용할 수 있지만 중복 저장된 게 없기 때문에 작동 불능에 빠질 위험이 있다.
- `--under-min-isr-partitions`: ISR 수가 쓰기 작업이 성공하기 위해 필요한 최소 레플리카 수에 미달하는 모든 파티션을 보여준다. 이 파티션들은 사실상 읽기 전용 모드라고 할 수 있고, 쓰기 작업은 불가능하다.
- `--unavailable-partitions`: 리더가 없는 모든 파티션을 보여준다. 이것은 매우 심각한 상황이므로, 파티션이 오프라인 상태이며 프로듀서나 컨슈머 클라이언트가 사용 불가능하다는 것을 의미한다.

### 파티션 추가하기
토픽의 파티션 수를 증가시켜야 할 경우가 있다. 파티션은 클러스터 안에서 토픽이 확장되고 복제되는 수단이기도 하다.

파티션 수를 증가시키는 가장 일반적인 이유는 단일 파티션에 쏟아지는 처리량을 줄임으로써 토픽을 더 많은 브로커에 대해 수평적으로 확장하기 위해서다. 하나의 파티션은 컨슈머 그룹 내의 하나의 컨슈머만 읽을 수 있기 때문에, 컨슈머 그룹 안에서 더 많은 컨슈머를 활용하는 경우에도 토픽의 파티션 수를 증가시킬 수 있다.

`--alter` 명령으로 파티션 수를 증가시킬 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/77c51d35-eb56-45c9-aa53-80d8036b7cb6)

컨슈머 입장에서, 키가 있는 메시지를 갖는 토픽에 파티션을 추가하는 것은 매우 어려울 수 있다. 파티션의 수가 변하면 키값에 대응되는 파티션도 달라지기 때문이다. 바로 이러한 이유 때문에 키가 포함된 메시지를 저장하는 토픽을 생성할 때는 미리 파티션의 개수를 정해 놓고, 일단 생성한 뒤에는 설정한 파티션의 수를 바꾸지 않는 것이 좋다.

### 파티션 개수 줄이기
**토픽의 파티션 개수는 줄일 수 없다.** 토픽에서 파티션을 삭제한다는 것은 곧 토픽에 저장된 데이터의 일부를 삭제한다는 의미인데, 이는 클라이언트 입장에서 일관적이지 않아 보일 수 있다. 뿐만 아니라 데이터를 남은 파티션에 다시 분배하는 것은 어려울 뿐 아니라 메시지의 순서를 바꾸게 된다.

만약 파티션의 수를 줄여야 한다면, 토픽을 삭제하고 다시 만들거나 토픽 삭제가 불가능할 경우엔 새로운 버전의 토픽을 생성해서 모든 쓰기 트래픽을 새 토픽으로 몰아주는 것을 권장한다.

### 토픽 삭제하기
메시지가 하나도 없는 토픽이라 할지라도 디스크 공간이나 파일 핸들, 메모리와 같은 클러스터 자원을 잡아먹는다. 컨트롤러 역시 아무 의미 없는 메타데이터에 대한 정보를 보유하고 있어야 하는데, 이는 대규모 클러스터에선 성능 하락으로 이어지게 된다. 만약 토픽이 더 이상 필요 없다면 이러한 자원을 해제하기 위해 삭제할 수 있다. 이를 위해선 클러스터 브로커의 `delete.topic.enable` 옵션이 `true`로 설정되어 있어야 한다. `false`라면 삭제 요청은 무시되어 아무 처리가 이뤄지지 않는다.

**토픽 삭제는 비동기적인 작업이다.** 이 명령을 실행하면 토픽이 삭제될 것이라고 표시만 될 뿐, 삭제 작업이 즉시 일어나는 것은 아니다. 컨트롤러가 가능하면 빨리 브로커에 아직 계류중인 삭제 작업에 대해 통지하면, 브로커는 해당 토픽에 대한 메타데이터를 무효화한 뒤 관련된 파일을 디스크에서 지우게 된다. 컨트롤러가 삭제 작업을 처리하는 방식의 한계 때문에 토픽을 지울 때는 2개 이상의 토픽을 동시에 삭제하지 말고 삭제 작업 사이에 충분한 시간을 둘 것을 권장한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/62e14fe4-a0d5-49a9-842c-e97cc1c898f7)

토픽 삭제의 성공 여부를 알려주는 명시적인 메시지가 없으므로 `--list`나 `--describe` 옵션을 사용해 클러스터 내 토픽이 더 이상 존재하지 않는다는 걸 확인하자.

## 컨슈머 그룹
**컨슈머 그룹은 서로 협업해서 여러 개의 토픽 혹은 하나의 토픽에 속한 여러 파티션에서 데이터를 읽어오는 카프카 컨슈머의 집단**을 가리킨다. `kafka-consumer-groups.sh`를 사용하면 클러스터에서 토픽을 읽고 있는 컨슈머 그룹을 관리하고 인사이트를 얻을 수 있다. 이 툴은 컨슈머 그룹 목록을 조회하거나, 특정한 그룹의 상세 내역을 보거나, 컨슈머 그룹을 삭제하거나 아니면 컨슈머 그룹 오프셋 정보를 초기화하는 데 사용할 수 있다.

참고로 구버전 카프카에선 컨슈머 그룹을 주키퍼에 저장해서 관리하기에 `--zookeeper` 매개변수를 사용했는데, 지원 중단됐음을 참고하자.

### 컨슈머 그룹 목록 및 상세 내역 조회하기
컨슈머 그룹 목록을 보려면 `--bootstrap-server`와 `--list`를 사용하면 된다. `kafka-consumer-group.sh`를 사용할 경우, 컨슈머 목록에 `console-consumer-{생성된 ID}`로 보인다.

```bash
$ bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
console-consumer-95554
console-consumer-9581
my-consumer
$
```

목록에 포함된 모든 그룹에 대해 `--list` 매개변수를 `--describe`로 바꾸고 `--group` 매개변수를 추가하면 상세 정보를 조회할 수 있다. 컨슈머 그룹이 읽어오고 있는 모든 토픽과 파티션 목록, 각 토픽 파티션에서의 오프셋과 같은 추가 정보를 보여준다.

```bash
$ bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-consumer
GROUP       TOPIC    PARTITION CURRENT-OFFSET LOG-END-OFFSET LAG CONSUMER-ID    HOST      CLIENT-ID
my-consumer my-topic 0         2              4              2   consumer-1-... 127.0.0.1 consumer-1
my-consumer my-topic 1         2              3              1   consumer-1-... 127.0.0.1 consumer-1
my-consumer my-topic 2         2              3              1   consumer-2-... 127.0.0.1 consumer-2
```

- `GROUP`: 컨슈머 그룹의 이름
- `TOPIC`: 읽고 있는 토픽의 이름
- `PARTITION`: 읽고 있는 파티션의 ID
- `CURRENT-OFFSET`: 컨슈머 그룹이 이 파티션에서 다음번에 읽어올 메시지의 오프셋. 이 파티션에서의 컨슈머 위치.
- `LOG-END-OFFSET`: 브로커 토픽 파티션의 하이 워터마크 오프셋 현재값. 이 파티션에 쓰여질 다음번 메시지의 오프셋.
- `LAG`: 컨슈머의 `CURRENT-OFFSET`과 브로커의 `LOG-END-OFFSET` 간의 차이
- `CONSUMER-ID`: 설정된 `client-id` 값을 기준으로 생성된 고유한 `consumer-id`
- `HOST`: 컨슈머 그룹이 읽고 있는 호스트의 IP 주소
- `CLIENT-ID`: 컨슈머 그룹에서 속한 클라이언트를 식별하기 위해 클라이언트에 설정된 문자열.

### 컨슈머 그룹 삭제하기
`--delete` 매개변수를 사용해서 컨슈머 그룹을 삭제할 수 있다. 이는 그룹이 읽고 있는 모든 토픽에 대해 저장된 모든 오프셋을 포함한 전체 그룹을 삭제한다. 이 작업을 수행하려면 컨슈머 그룹 내의 모든 컨슈머들이 모두 내려간 상태여서 컨슈머 그룹에 활동중인 멤버가 하나도 없어야 한다. 만약 비어 있지 않은 그룹을 삭제하려 시도할 경우, 그룹이 비어 있지 않다는 에러가 발생하고 아무 작업도 수행되지 않을 것이다.

`--topic` 매개변수에 삭제하려는 토픽의 이름을 지정해서 컨슈머 그룹 전체를 삭제하는 대신 컨슈머 그룹이 읽어오고 있는 특정 토픽에 대한 오프셋만 삭제하는 것도 가능하다.

```bash
$ bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --delete --group my-consumer
Deletion of requested consumer groups ('my-consumer') was successful.
$
```

### 오프셋 관리
컨슈머 그룹에 대한 오프셋을 조회하거나 삭제하는 것 외에도 저장된 오프셋을 가져오거나 새로운 오프셋을 저장하는 것도 가능하다. 특정 문제로 메시지를 다시 읽어와야 하거나 문제가 있는 메시지를 건너뛰기 위해 컨슈머의 오프셋을 리셋하는 경우 유용하다.

컨슈머 그룹을 csv 파일로 내보내려면 `--dry-run` 옵션과 함께 `--reset-offsets` 매개변수를 사용해주면 된다. 이렇게 하면 나중에 오프셋을 가져오거나 롤백하기 위해 사용할 수 있는 파일 형태로 현재 오프셋을 내보낼 수 있다.

`{토픽 이름},{파티션 번호},{오프셋}` 형식의 예시다.

`--dry-run` 없이 같은 명령을 수행하면 오프셋이 완전히 리셋되니 주의해야 한다.

```bash
$ bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --export --group my-consumer --topic my-topic \
  --reset-offsets --to-current --dry-run > offsets.csv

$ cat offsets.csv
my-topic,0,8905
my-topic,1,8915
my-topic,2,9845
my-topic,3,8702
my-topic,4,8008
my-topic,5,8319
my-topic,6,8102
my-topic,7,12739
$
```

오프셋 가져오기는 내보내기의 반대라고 할 수 있다. 파일을 가져와 컨슈머 그룹의 현재 오프셋을 설정하는 데 사용한다. 주로 현재 컨슈머 그룹의 오프셋을 내보낸 뒤, 백업을 하기 위한 복사본을 하나 만들어 놓고, 오프셋을 원하는 값으로 바꿔 사용하는 식으로 사용된다.

이 작업을 하기 전, 컨슈머 그룹에 속한 모든 컨슈머를 중단시키는 것이 중요하다. 컨슈머 그룹이 현재 돌아가는 상황에서 새 오프셋을 넣어준다 해서 컨슈머가 새 오프셋 값을 읽어오지는 않기 때문이다. 이 경우 컨슈머는 그냥 새 오프셋들을 덮어써 버린다.

```bash
$ bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --reset-offsets --group my-consumer \
  --from-file offsets.csv --execute

TOPIC       PARTITION    NEW-OFFSET
my-topic    0            8905
my-topic    1            8915
my-topic    2            9845
my-topic    3            8702
my-topic    4            8008
my-topic    5            8319
my-topic    6            8102
my-topic    7            12739
$
```

## 동적 설정 변경
**토픽, 클라이언트, 브로커 등 많은 설정 중 클러스터를 끄거나 재설치할 필요 없이 돌아가는 와중에 동적으로 바꿀 수 있는 설정은 굉장히 많다.** 이런 설정들을 수정할 때는 `kafka-configs.sh`가 주로 사용된다.

동적으로 재정의가 가능한 설정은 카프카의 버전에 따라 다르므로 사용중인 카프카와 같은 버전의 툴을 사용하도록 하자. 이러한 설정 작업을 자동화하기 위해 관리하고자 하는 설정을 미리 형식에 맞춰 담아놓은 파일과 `--add-config-file`을 통해 사용할 수 있다.

### 토픽 설정 기본값 재정의하기
```bash
$ bin/kafka-configs.sh --bootstrap-server localhost:9092 \
    --alter --entity-type topics --entity-name {토픽 이름} \
    --add-config {key}={value}[,{key}={value}...]
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/027b72d3-4a37-44c9-adff-8cd2366d1a1c)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/61620058-0f76-463c-aa34-4755bf693bf1)

### 클라이언트와 사용자 설정 기본값 재정의하기
카프카 클라이언트와 사용자의 경우, 재정의 가능한 설정은 쿼터에 관련된 것 몇 개밖에 없다. 가장 일반적인 설정 두 개는 특정한 클라이언트 ID에 대해 브로커별로 설정되는 프로듀서와 컨슈머 `bytes/sec` 속도다.

- `consumer_bytes_rate`: 하나의 클라이언트 ID가 하나의 브로커에서 초당 읽어올 수 있는 메시지의 양
- `producer_bytes_rate`: 하나의 클라이언트 ID가 하나의 브로커에 초당 쓸 수 있는 메시지의 양
- `controller_mutations_rate`: 컨트롤러의 변경률. 사용 가능한 토픽 생성, 파티션 생성, 토픽 삭제 요청의 양
- `request_precentage`: 사용자 혹은 클라이언트로부터의 요청에 대한 쿼터 윈도우 비율

```bash
$ bin/kafka-configs.sh --bootstrap-server localhost:9092 \
    --alter --add-config "controller_mutations_rate=10"
    --entity-type clients --entity-name {Client ID}
    --entity-type users --entity-name {User ID}
$
```

### 브로커 설정 기본값 재정의하기
브로커와 클러스터 수준 설정은 주로 클러스터 설정 파일에 정적으로 지정되지만, 카프카를 재설치할 필요 없이 프로세스가 돌아가는 중에 재정의가 가능한 설정들도 많다. `kafka-configs.sh` 내에서 각 항목들에 대해 `--help` 명령을 사용하거나 공식 문서를 찾아보자.

중요한 것 3가지는 다음과 같다.

- `min.insync.replicas`: 프로듀서의 `acks` 설정값이 `all`로 잡혀 있을 때 쓰기 요청에 응답이 가기 전에 쓰기가 이루어져야 하는 레플리카 수의 최솟값을 결정한다.
- `unclean.leader.election.enable`: 리더로 선출되었을 경우 데이터 유실이 발생하는 레플리카를 리더로 선출할 수 있게 한다. 약간의 데이터 유실이 허용되는 경우 또는 데이터 유실을 피할 수 없어서 카프카 클러스터의 설정을 잠깐 풀어주거나 해야할 때 유용하다.
- `max.connections`: 브로커에 연결할 수 있는 최대 연결 수.

### 재정의된 설정 상세 조회하기
`kafka-configs.sh`를 사용하면 모든 재정의된 설정의 목록을 조회할 수 있다. 토픽, 브로커, 클라이언트별 설정값을 확인할 수 있다. `--describe` 명령을 사용하면 된다.

### 재정의된 설정 삭제하기
동적으로 재정의된 설정은 통째로 삭제될 수 있으며, 이 경우 해당 설정은 클러스터 기본값으로 돌아가게 된다. 재정의된 설정을 삭제하려면 `--delete-config` 매개변수와 함께 `--alter` 명령을 사용한다.

```bash
$ bin/kafka-configs.sh --bootstrap-server localhost:9092 \
    --alter --entity-type topics --entity-name {토픽 이름} \
    --delete-config {key}={value}[,{key}={value}...]
```

## 쓰기 작업과 읽기 작업
카프카를 사용할 때 애플리케이션이 제대로 동작하는지 확인하기 위해 수동으로 메시지를 쓰거나 샘플 메시지를 읽어와야 하는 경우가 있다. 이를 위해 `kafka-console-consumer.sh`와 `kafka-console-producer.sh`가 제공된다.

이 툴들은 자바 클라이언트 라이브러리를 살짝 감싸는 형태로 구현되었으며, 해당 작업을 수행하는 애플리케이션 전체를 작성할 필요 없이 카프카 토픽과 상호작용할 수 있도록 해준다.

### 콘솔 프로듀서
`kafka-console-producer.sh`를 사용해서 카프카 토픽에 메시지를 써넣을 수 있다. 기본적으로 메시지는 줄 단위로, 키와 밸류값은 탭 문자를 기준으로 구분된다. 콘솔 컨슈머와 마찬가지로, 콘솔 프로듀서는 기본 시리얼라이저를 사용해서 읽어들인 데이터를 바이트 뭉치로 변환한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0ad3129d-0ee8-480c-9f44-cdb11ce7f1d0)

프로듀서를 설정할 때 사용하는 설정을 콘솔 프로듀서에 전달하는 것도 가능하다.

- `--producer.config {설정 파일}`
- `--producer-property {키} = {값}`

`--producer-property`와 사용되는 몇 가지를 살펴보자.

- `--batch-size`: (동기 모드로 작동 중이지 않을 경우) 하나의 배치로 전달되어야 할 메시지의 수를 지정
- `--timeout`: 프로듀서가 비동기 모드로 작동 중일 때, 이 옵션은 메시지 배치를 쓰기 전 기다리는 최대 시간을 지정한다. 써야 할 메시지가 느리게 주어지는 토픽에 대해 오랫동안 기다리는 사태를 방지할 수 있다.
- `--compression-codec {압축 코덱}`: 메시지를 쓸 때 사용할 압축 코덱을 지정한다. `none`, `gzip`, `snappy`, `zstd`, `lz4` 중 하나를 사용할 수 있고 기본값은 `gzip`이다.
- `--sync`: 메시지를 동기적으로 쓴다. 즉, 다음 메시지를 보내기 전 이전에 쓴 메시지에 대한 응답이 올 때까지 기다린다.

표준 입력으로 들어온 값을 읽어 프로듀서 레코드를 생성하는 `kafka.tools.ConsoleProducer$LineMessageReader` 클래스 역시 여러 유용한 옵션을 가지고 있다. `--property`를 사용해서 지정 가능하다.

- `ignore.error`: 이 값이 `false`이고 `parse.key`가 `true`인 상태에서 키 구분자가 정의되어 있지 않을 경우 예외가 발생한다. 기본값은 `true`
- `parse.key`: 키값을 항상 `null`로 고정하고 싶다면 `false`로 잡아주면 된다. 기본값은 `true`다.
- `key.separator`: 메시지 키와 밸류를 구분할 때 사용되는 구분자로 기본값은 탭 문자다.

### 콘솔 컨슈머
`kafka-console-consumer.sh`를 사용하면 카프카 클러스타 안의 1개 이상의 토픽에 대해 메시지를 읽어올 수 있다. 메시지는 표준 출력에 한 줄씩 출력된다. 기본적으로 키나 형식 같은 것 없이 메시지 안에 지정된 raw byte 뭉치가 출력된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/43fc5ca6-dff6-4a6f-b8a2-0b5c7f6cdb60)

일반적인 컨슈머 설정 옵션 역시 콘솔 컨슈머에 지정해 줄 수있다. 콘솔 프로듀서의 사용법과 비슷하지만, `consumer`만 다르다.

자주 사용되는 옵션 몇 가지는 다음과 같다.

- `--formatter {클래스 이름}`: 메시지를 바이트 뭉치에서 문자열로 변환하기 위해 사용될 메시지 포매터 클래스를 지정한다. 기본값은 `kafka.tools.DefaultMessageFormatter`
- `--from-beginning`: 지정된 토픽의 가장 오래된 오프셋부터 메시지를 읽어온다. 이것을 지정하지 않으면 가장 최근 오프셋부터 읽어온다.
- `--max-messages {정수값}`: 종료되기 전 읽어올 최대 메시지 수.
- `--partition {정수값}`: 지정된 ID의 파티션에서만 읽어온다.
- `--offset`: 읽어오기 시작할 오프셋. `earliest`로 지정할 경우 맨 처음부터, `latest`로 지정할 경우 가장 최신 값부터 읽어온다.
- `--skip-message-on-error`: 메시지에 에러가 있을 경우 실행을 중단하는 게 아니라 그냥 넘어간다. 디버깅할 때 사용하자.

기본값 외에 사용가능한 메시지 포매터는 3개다.

- `kafka.tools.LoggingMessageFormatter`: 표준 출력이 아니라 로거를 사용해 메시지를 출력한다. 각 메시지는 INFO 레벨로 출력되며 타임스탬프, 키, 밸류를 포함한다.
- `kafka.tools.ChecksumMessageFormatter`: 메시지의 체크섬만 출력한다.
- `kafka.tools.NoOpMessageFormatter`: 메시지를 읽어오되 아무것도 출력하지 않는다.

`kafka.tools.DefaultMessageFormatter` 역시 `--property` 명령줄 옵션으로 설정 가능한 여러 유용한 옵션들을 가지고 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/983072b0-a83b-440a-a2b5-81b4bec91d22)

클러스터의 컨슈머 그룹별로 커밋된 오프셋을 확인해봐야 하는 경우도 있다. 특정 그룹이 오프셋을 커밋하고 있는지의 여부를 확인하거나 얼마나 자주 커밋했는지를 알고 싶을 수도 있다. 콘솔 컨슈머를 사용해 `__consumer_offsets` 내부 토픽을 읽어오면 된다. 모든 컨슈머 오프셋은 이 토픽 메시지로 쓰여진다. 이 토픽에 저장된 메시지를 열어보고 싶다면, `kafka.coordinator.group.GruopMetadataManager$OffsetsMessageFormatter` 포매터 클래스를 사용하자.