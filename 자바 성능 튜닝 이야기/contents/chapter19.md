# Story 19. GC 튜닝을 항상 할 필요는 없다.
자바의 GC 튜닝은 꼭 필요한 경우에 하는 것이 좋다. WAS를 띄울 때 아무런 옵션 없이 띄워도 된다는 말이 아니라 기본적인 메모리 크기 정도만 지정하면 웬만큼 사용량이 많지 않은 시스템에선 튜닝을 할 필요가 없다는 말이다.

## GC 튜닝을 꼭 해야 할까?
결론부터 말하자면 Java 기반의 모든 서비스에서 GC 튜닝을 진행할 필요는 없다. 단, 운영 중인 Java 기반 시스템의 옵션에 기본적으로 다음과 같은 것들이 추가되어 있을 때의 경우다.

- `-Xms` 옵션과 `-Xmx` 옵션으로 메모리 크기를 지정했다.
- `-server` 옵션이 포함되어 있다.

그리고 시스템의 로그에는 타임아웃 관련 로그가 남아있지 않아야 한다. 여기서 타임아웃은 다음과 같은 것들을 말한다.

- DB 작업과 관련된 타임아웃
- 다른 서버와의 통신시 타임아웃

타임아웃 로그가 존재하고 있다는 것은 그 시스템을 사용하는 사용자 중 대다수나 일부는 정상적인 응답을 받지 못했다는 말이다. 그리고, 대부분 서로 다른 서버간에 통신 문제나 원격 서버의 성능이 느려서 타임아웃이 발생할 수도 있지만, 그 이유가 GC 때문일 수도 있다.

정리하자면,

- JVM의 메모리 크기도 지정하지 않았고,
- Timeout이 지속적으로 발생하고 있다면

시스템에서 GC 튜닝을 하는 것이 좋다. 그렇지 않다면, GC 튜닝할 시간에 다른 작업을 하는 것이 더 낫다. 꼭 한가지 명심해야 하는 점은 **GC 튜닝은 가장 마지막에 하는 작업이라는 것이다.** GC 튜닝을 왜 하는지 근본적인 이유를 생각해보자. Java에서 생성된 객체는 가비지 컬렉터가 처리해서 지운다. 생성된 객체가 많으면 많을 수록 가비지 컬렉터가 처리해야 하는 대상도 많아지고, GC를 수행하는 횟수도 증가한다. 즉, 우리가 운영하고 만드는 시스템이 GC를 적게 하도록 하려면 객체 생성을 줄이는 작업을 먼저 해야 한다.

이 책에서 이야기한 대부분의 내용들이 지켜지면 굳이 튜닝을 할 필요까진 없다. 만약 애플리케이션 메모리 사용도 튜닝을 많이 해서 어느 정도 만족할 만한 상황이 되었다면, 본격적으로 GC 튜닝을 시작하면 된다. 저자는 GC 튜닝의 목적을 두 가지로 나눈다고 한다.

- Old 영역으로 넘어가는 객체의 수를 최소화 하는 것
- Full GC의 실행 시간을 줄이는 것

### Old 영역으로 넘어가는 객체의 수 최소화하기
Oracle JVM에서 제공하는 모든 GC는 Generational GC다. 즉, Eden 영역에서 객체가 처음 만들어지고, Survivor 영역을 오가다가, 끝까지 남아 있는 객체는 Old 영역으로 이동한다. (G1의 경우 약간 상이하게 동작한다.) 간혹 Eden 영역에서 만들어지다가 크기가 커져 Old 영역으로 바로 넘어가는 객체도 있긴 하다.

Old 영역의 GC는 New 영역의 GC에 비해 상대적으로 시간이 오래 소요되기 때문에 Old 영역으로 이동하는 객체의 수를 줄이면 Full GC가 발생하는 빈도를 많이 줄일 수 있다. Old 영역으로 넘어가는 객체의 수를 줄인다는 말을 잘못 이해하면 객체를 마음대로 New 영역에만 남길 수 있다고 생각할 수 있지만, 그렇게 할 순 없다. 하지만 New 영역의 크기를 잘 조절함으로써 큰 효과를 볼 수는 있다.

### Full GC 시간 줄이기
Full GC의 수행 시간은 상대적으로 Young GC에 비해 길다. 그래서 Full GC 실행에 시간이 오래 소요되면 연계된 여러 부분에서 타임아웃이 발생할 수 있다. 그렇다고 Old 영역의 크기를 줄이면 `OutOfMemoryError`가 발생하거나 Full GC 횟수가 늘어난다. 반대로 Old 영역의 크기를 늘리면 Full GC 횟수는 줄어들지만 실행 시간이 늘어난다. Old 영역의 크기를 적절하게 잘 설정해야 한다.

## GC의 성능을 결정하는 옵션들
GC 옵션은 '누가 이 옵션을 썼을 때 성능이 잘 나왔대. 우리도 이렇게 적용하자.' 라고 생각하면 절대 안 된다. 서비스마다 생성되는 객체의 크기도 다르고 살아있는 기간도 다르기 때문이다.

Java의 GC 옵션도 마찬가지다. 이런 저런 옵션을 많이 설정한다해서 시스템의 GC 수행 속도가 월등히 빨라지진 않는다. 오히려 더 느려질 확률이 높다. 두 대 이상의 서버에 GC 옵션을 다르게 적용해서 비교해 보고, 옵션을 추가한 서버의 성능이나 GC 시간이 개선된 때에만 옵션을 추가하는 것이 GC 튜닝의 기본 원칙이다.

다음은 성능에 영향을 주는 GC 옵션 중 메모리 크기와 관련된 옵션이다.

|구분|옵션|설명|
|--|--|--|
|Heap 영역 크기|`-Xms`|JVM 시작 시 힙 영역 크기|
||`-Xmx`|최대 힙 영역 크기|
|New 영역의 크기|`-XX:NewRatio`|New 영역과 Old 영역의 비율|
||`-XX:NewSize`|New 영역의 크기|
||`-XX:SurvivorRatio`|Eden 영역과 Survivor 영역의 비율|

`-Xms`, `-Xmx`는 필수로 지정해야 하는 옵션이다. `NewRatio` 옵션을 어떻게 설정하느냐에 따라 GC 성능에 많은 차이가 발생한다. Perm 영역의 크기는 `OutOfMemoryError`가 발생하고, 그 문제의 원인이 Perm 영역의 크기 때문일 때만 `-XX:PermSize` 옵션과 `-XX:MaxPermSize` 옵션으로 지정해도 큰 문제는 없다.

GC 방식 역시 GC의 성능에 많은 영향을 주는 옵션이다. 다음은 GC 방식에 따라 지정할 수 있는 옵션이다. (JDK 6.0 기준)

|구분|옵션|비고|
|--|--|--|
|Serial GC|`-XX:+UseSerialGC`||
|Parallel GC|`-XX:+UseParallelGC`||
||`-XX:ParallelGCThreads=value`||
|Parallel Compacting GC|`-XX:+UseParallelOldGC`||
|CMS GC|`-XX:+UseConcMarkSweepGC`||
||`-XX:+UsePerNewGC`||
||`-XX:+CMSParallelRemarkEnabled`||
||`-XX:CMSInitiatingOccupancyFraction=value`||
||`-XX:+UseCMSInitiatingOccupancyOnly`||
|G1|`-XX:+UnlockExperimentalVMOptions`|JDK 6에선 두 옵션을 반드시 같이 사용해야 함|
||`-XX:+UseG1GC`||

G1 GC를 제외하곤, 각 GC 방식의 첫 번째 줄에 있는 옵션을 지정하면 GC 방식이 변경된다. GC 방식 중 특별히 신경 쓸 필요가 없는 방식은 Serial GC다. 이는 클라이언트 장비에 최적화되어 있기 때문이다.

## GC 튜닝의 절차
저자가 사용하는 GC 튜닝 절차는 다음과 같다고 한다.

1. GC 상황 모니터링  
  GC 상황을 모니터링하며 현재 운영되는 시스템의 GC 상황을 확인해야 한다.
2. 모니터링 결과 분석 후 GC 튜닝 여부 결정  
  분석한 결과를 확인했는데 GC 수행 시간이 1~3초, 심지어 10초가 넘는 상황이라면 GC 튜닝을 진행해야 한다. GC 튜닝 전에 시스템의 메모리를 왜 높게 잡아야 하는지에 생각해봐야 한다. `OutOfMemoryError`가 발생한다면, 힙 덤프를 떠서 그 원인을 확인하고, 문제점을 제거해야만 한다.
3. GC 방식/메모리 크기 지정  
  서버가 여러 대면 서버에 GC 옵션을 서로 다르게 지정해서 GC 옵션에 따른 차이를 확인하는 것이 중요하다. 메모리 크기가 크면, GC 발생 횟수는 감소하고 GC 수행 시간은 길어진다. 설정에 대한 정답은 없다.
4. 결과 분석  
  GC 옵션을 지정하고 적어도 24시간 이상 데이터를 수집한 후에 분석을 실시한다. 로그를 분석해 메모리가 어떻게 할당되는지 확인해야 한다. 그 다음 GC 방식/메모리 크기를 변경해가며 최적의 옵션을 찾아 나간다.
5. 결과가 만족스러울 경우 전체 서버에 반영 및 종료  
  이 작업은 매우 조심해서 접근해야 한다. 잘못하면 장애로 이어질 수도 있기 때문이다.

GC 옵션을 적용하고, `verbosegc` 옵션을 지정한 다음 `tail` 명령어로 로그가 제대로 쌓이고 있는지 확인해야 한다. 로그가 잘 쌓이고 있다면, 하루 혹은 이틀 정도의 데이터가 축적된 후 결과를 확인해보자. GC 튜닝 결과를 분석할 때는 다음의 사항을 중심으로 살펴보는 것이 좋다.

- Full GC 수행 시간
- Minor GC 수행 시간
- Full GC 수행 간격
- Minor GC 수행 간격
- 전체 Full GC 수행 시간
- 전체 Minor GC 수행 시간
- 전체 GC 수행 시간
- Full GC 수행 횟수
- Minor GC 수행 횟수