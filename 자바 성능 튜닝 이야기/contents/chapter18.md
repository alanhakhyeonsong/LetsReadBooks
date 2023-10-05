# Story 18. GC가 어떻게 수행되고 있는지 보고 싶다.
`jstat`이라는 명령을 사용하여 실시간으로 보거나 `verbosegc` 옵션을 사용하여 로그를 남기면 시스템을 분석할 수 있다. `verbosegc` 옵션은 자바 수행 옵션에 추가하고, JVM을 재시작해야 한다.

이 장에선 JVM의 상태를 확인하는 각종 명령 및 옵션에 대해 살펴보자.

## 자바 인스턴스 확인을 위한 jps
`jps`는 해당 머신에서 운영 중인 JVM의 목록을 보여준다. JDK의 bin 디렉터리에 있다. 사용법은 매우 간단하다. terminal에서 다음과 같은 옵션으로 수행하면 된다.

```bash
jps [-q] [-mlvV] [-Joption] [<hostid>]
```

- `-q`: 클래스나 JAR 파일명, 인수 등을 생략하고 내용을 나타낸다. (단지 프로세스 id만 나타냄)
- `-m`: main 메서드에 지정한 인수들을 나타낸다.
- `-l`: 애플리케이션의 main 클래스나 애플리케이션 JAR 파일의 전체 경로 이름을 나타낸다.
- `-v`: JVM에 전달된 자바 옵션 목록을 나타낸다.
- `-V`: JVM의 플래그 파일을 통해 전달된 인수를 나타낸다.
- `-Joption`: 자바 옵션을 이 옵션 뒤에 지정할 수 있다.

아무 옵션 없이 이 명령어를 입력하면 현재 서버에서 수행되고 있는 자바 인스턴스들의 목록이 나타난다.

```bash
$ jps
2464 Bootstrap
4224 Jps
3732 Bootstrap
```

위 결과는 두 개의 톰캣 서버 인스턴스를 띄워 놓은 상태에서 나온 것이다. `Jps`는 `jps` 명령 자체 인스턴스를 의미하는 프로세스로 이 명령 종료 후 바로 사라진다. 프로세스 아이디는 수행할 때마다 바뀐다. 이렇게 보면, 내가 모니터링하려는 톰캣이 어떤 것인지 알 수가 없다. 따라서, `-v` 옵션을 추가로 지정해주면 자바 옵션까지 포함하여 출력해준다.

## GC 상황을 확인하는 jstat
`jstat`은 GC가 수행되는 정보를 확인하기 위한 명령어다. 이를 사용하면 유닉스 장비에서 `vmstat`이나 `netstat`과 같이 라인 단위로 결과를 보여준다.

```bash
jstat -<option> [-t] [-h<lines>] <vmid> [<interval> [<count>]]
```

- `-t`: 수행 시간을 표시한다.
- `-h:lines`: 각 열의 설명을 지정된 라인 주기로 표시한다.
- `interval`: 로그를 남기는 시간의 차이를 의미한다.
- `count`: 로그 남기는 횟수를 의미한다.

참고로 `-t` 옵션을 주면 수행 시간을 표시한다 되어 있는데, 여기서의 수행 시간은 해당 자바 인스턴스가 생성된 시점부터의 시간이다. 다시 말해, 서버가 기동된 시점부터의 시간이다.

`<option>`의 종류는 다음과 같으며, 옵션에 따라 나타나는 결과의 내용이 많이 달라진다.

- `class`: 클래스 로더에 대한 통계
- `compiler`: 핫스팟 JIT 컴파일러에 대한 통계
- `gc`: GC 힙 영역에 대한 통계
- `gccapacity`: 각 영역의 허용치와 연관된 영역에 대한 통계
- `gccause`: GC의 요약 정보와, 마지막 GC와 현재 GC에 대한 통계
- `gcnew`: 각 영역에 대한 통계
- `gcnewcapacity`: Young 영역과 관련된 영역에 대한 통계
- `gcold`: Old와 Perm 영역에 대한 통계
- `gcoldcapacity`: Old 영역의 크기에 대한 통계
- `gcpermcapacity`: Perm 영역의 크기에 대한 통계
- `gcutil`: GC에 대한 요약 정보
- `printcompilation`: 핫 스팟 컴파일 메서드에 대한 통계

```bash
jstat -gcnew -t -h10 2624 1000 20 > jstat_WAS1.log
```

위 예시는,

- 각 영역에 대한 통계를 보여주며,
- 수행 시간을 나타내고,
- 10줄에 한 번씩 각 열의 설명(타이틀)을 나타내고,
- 프로세스 번호는 2464이고,
- 1초(1000ms)에 한 번씩 정보를 보여주고,
- 20회 반복 수행을 한다.
- 마지막으로 `jstat_WAS1.log` 파일에 결과를 저장한다.

`jstat`에서 프린트되는 결과를 사용하여 그래프를 그리면 GC가 처리되는 추이를 알아볼 수 있으므로 편리하다. 또한 결과를 파일로도 남길 수 있어 나중에 분석할 때 사용할 수 있다. 단, 이 결과만으로는 어떻게 해석을 하면 좋을지 알기 어렵다는 단점이 있다. 하지만, JVM 파라미터 튜닝을 할 때나, GC를 수행하는 데 소요된 모든 시간을 보고 싶을 때 유용하게 사용할 수 있다.

`jstat`을 로그로 남겨 분석하는 데는 한계가 있다. 로그를 남기는 주기에 GC가 한 번 발생할 수도 있고, 10번 발생할 수도 있기 때문이다. 따라서 정확한 분석을 하고자 할 때는 `verbosegc` 옵션 사용을 권장한다.

## GC 튜닝할 때 가장 유용한 jstat 옵션은 두 개
저자가 GC 튜닝을 위해 `jstat` 명령에서 사용하는 옵션은 `-gcutil`과 `-gccapacity`라고 한다.

```java
pubilc class GCMaker {

    public static void main(String[] args) throws Exception {
        GCMaker maker = new GCMaker();

        for (int i = 0; i < 120; i++) {
            maker.makeObject();
            Thread.sleep(1000);
            System.out.print(".");
        }
    }

    private void makeobject() {
        Integer[] intArr = new Integer[1024000];
        List<Integer> list = new ArrayList<Integer>(1024000);

        for (int i = 0; i < 1024; i++) {
            intArr[i] = i;
            list.add(i);
        }
    }
}
```

`gccapacity` 옵션을 살펴보자. 이 옵션은 현재 각 영역에 할당되어 잇는 메모리의 크기를 KB 단위로 나타낸다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/66d3e815-1e2d-4c6d-b004-66b326c27961)

NGC로 시작하는 것은 New (Young) 영역의 크기 관련, OGC로 시작하는 것은 Old 영역 크기 관련, PGC로 시작하는 것은 Perm 영역 크기 관련 정보다. 중간에 S0C, S1C, EC, OC, PC는 각각 Survivor0, Survivor1, Eden, Old, Perm 영역의 현재 할당된 크기를 나타낸다. 그리고 MN, MX, C로 끝나는 항목들은 각각 Min, Max, Committed를 의미한다. 가장 끝 두 개 항목은 각각 Minor GC 횟수와 Full GC 횟수를 의미한다.

`gccapacity` 옵션을 사용하면 각 영역의 크기를 알 수 있기 때문에 어떤 영역의 크기를 좀 더 늘리고, 줄여야 할지를 확인할 수 있다는 장점이 있다. 이 명령어만 수행해보면 해당 자바 프로세스의 메모리 점유 상황을 쉽게 확인할 수 있다.

`gcutil` 옵션을 살펴보자. 이 옵션은 힙 영역의 사용량을 %로 보여준다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f1f22378-8453-4397-9bf3-ff4d235dd794)

S0, S1은 Survivor 영역을 의미하며 E는 Eden 영역을 의미한다. 즉, 이 세 개 영역이 Young 영역에 해당하고, YGC는 Young 영역의 GC 횟수, YGCT는 Young 영역의 GC가 수행된 누적 시간(초)이다. O는 Old, P는 Perm 영역을 의미하며, 이 두 개 영역 중 하나라도 GC가 발생하면 FGC의 횟수가 증가하고, FGCT 시간이 올라가게 된다. 가장 마지막에 있는 GCT는 Young GC가 수행된 시간인 YGCT와 Full GC가 수행된 시간인 FGCT의 합이다.

Young GC가 한 번 수행될 때의 시간을 구하려면 간단하게 `YGCT / YGC`를 계산하면 된다. 마찬가지로 Full GC별 시간도 이렇게 구할 수 있다.

여기서 조심해야 할 것은 CMS GC를 사용할 경우엔 Full GC의 단계에 따라 수행되는 시간이 다르다는 점이다. 다시 말해 평균값이 낮다고 그냥 무시해선 안 된다.

## 원격으로 JVM 상황을 모니터링하기 위한 jstatd
앞서 살펴본 명령어들을 사용하면 로컬 시스템에서만 모니터링을 할 수 있다. 원격 모니터링이 불가능하기 때문에 `jstatd`라는 데몬이 만들어졌다. 이 명령어를 사용하면 원격 모니터링을 할 수 있지만, 중지하면 서버가 가동 중일 경우에도 원격 모니터링이 불가능하다.

```bash
jstatd [-nr] [-p port] [-n rminame]
```

- `nr`: RMI registry가 존재하지 않을 경우 새로운 RMI 레지스트리를 `jstatd` 프로세스 내에서 시작하지 않는 것을 정의하기 위한 옵션이다.
- `p`: RMI 레지스트리를 식별하기 위한 포트 번호
- `n`: RMI 객체의 이름을 지정한다. 기본 이름은 `JStatRemoteHost`다.

아무 옵션 없이 `jstatd`를 실행해보면 오류가 발생한다. 자바에 기본적으로 지정되어 있는 보안 옵션이 `jstatd`가 리모트 객체를 만드는 것을 억제하기 대문이다. 이를 해결하려면 자바가 설치되어 있는 서버 내 디렉터리의 `lib/security/java.policy` 파일에 다음 허가 명령어를 추가해야 한다.

```vim
grant codebase "file:${java.home}/../lib/tools.jar" {
  permission java.security.AllPermission;
};
```

```bash
jstatd -J-Djava.security.policy=all.policy -p 2020
```

다른 애플리케이션에서 해당 포트 번호를 사용하지 않는다면 정상적으로 프로세스가 수행될 것이다.

## verbosegc 옵션을 이용하여 gc 로그 남기기
`jvmstat`을 사용할 수 없는 상황이라면 어떻게 GC를 분석할 수 있을까? GC를 분석하기 위한 명령어로는 가장 쉬울, `verbosegc` 옵션이 있다. 자바 수행 시에 간단히 `-verbosegc`를 넣어주면 된다.

```bash
java -verbosegc <기타 다른 옵션들> 자바 애플리케이션 이름
```

결과가 출력되는 기본 위치는 `System.out.println()`을 호출했을 때 출력되는 위치와 동일하다.

```bash
...
[Full GC 121104K->9472K(130112K), 0.0647395 secs]
[GC 8128k->848K(130112K), 0.0090257 secs]
...
```

Young 영역에 마이너 GC가 발생했으며, 8,128kbyte에서 848kbyte로 축소되었다. 전체 할당된 크기는 130,112kbyte이며, GC 수행 시간은 0.0090257초다. 여기서 Full GC로 표시되어 있는 행은 수행 시간이 다른 마이너 GC에 비해 월등히 긴 것을 확인할 수 있다.

### PrintGCTimeStamps 옵션
이렇게 옵션을 주고 수행하면 언제 GC가 발생되었는지 알 수 없다. 이 경우를 대비해 `verbosegc`와 함께 사용할 수 있는 `-XX:+PrintGCTimeStamps` 옵션이 있다.

```bash
# -verbosegc -XX:+PrintGCTimeStamps 옵션 적용 후
0.668: [GC 8128k->848K(130112K), 0.0090257 secs]
...
```

가장 좌측에 수행한 시간이 포함되어 출력된다. 서버가 기동되기 시작한 이후부터 해당 GC가 수행될 때까지의 시간을 로그에 포함하기 때문에 언제 GC가 발생됐는지 확인할 수 있다.

### PrintHeapAtGC 옵션
이 옵션을 지정하면 GC에 대한 더 많은 정보를 볼 수 있지만, 너무 많은 내용을 보여주기 때문에 분석하기 그리 쉽지는 않다. 그러나 툴로 분석할 땐 아주 상세한 결과도 확인할 수 있다.

```bash
# -verbosegc -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC 옵션 적용 후
{Heap before GC invocations=1 (full 0):
 PSYoungGen    total 37696K, used 29307K [...)
  eden space 32320K, 90% used [...)
  from space 5376K, 0% used [...)
  to   space 5376K, 0% used [...)
 ParOldGen     total 86144K, used 0K [...)
  object space 86144K, 0% used [...)
 PSPermGen     total 21248K, used 2521K [...)
  object space 21248K, 11% used [...)
3.536: [GC 29307K->4649K(123840K), 0.0708125 secs]
Heap after GC invocations=1 (full 0):
 PSYoungGen    total 37696K, used 4649K [...)
  eden space 32320K, 0% used [...)
  from space 5376K, 86% used [...)
  to   space 5376K, 0% used [...)
 ParOldGen     total 86144K, used 0K [...)
  object space 86144K, 0% used [...)
 PSPermGen     total 21248K, used 2521K [...)
  object space 21248K, 11% used [...)
}
# 대괄호 안에는 주소 값이 나온다. (생략)
```

GC가 한 번 수행될 때 각 영역에서 얼마나 많은 메모리 영역을 사용하고 있는지 상세하게 볼 수 있다. 이 결과의 특징은 Before라고 되어 있는 블록에는 GC 전의 메모리 상황을, After라고 되어 있는 블록에는 GC 후의 메모리 상황을 제공한다는 것이다. 참고로 이 결과는 JVM 벤더마다 다를 수 있다.

### PrintGCDetails
더 간결하고 보기 쉽게 출력하는 옵션이다.

```bash
# -verbosegc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails 옵션 적용 후
0.719: [GC 0.719: [DefNew: 8128K->848K(9088K), 0.0090986 secs]
8128K->848K(130112K), 0.0092344 secs]
...
63.616: [Tenured: 112634K->9923K(121024K), 0.0633237 secs]
121416K->9923K(130112K), 0.0634597 secs]
```

텍스트 기반으로 나온 결과를 토대로 분석하지 말고 각 서버에 알맞은 분석 툴들을 사용하도록 하자.

- GC Analyzer
- IBM GC  분석기
- HPjtune

## 어설프게 아는 것이 제일 무섭다
정말 메모리 릭이 있는 경우는 저자의 경험상 전체 자바 애플리케이션 1%도 안된다고 한다. 게다가 요즘은 대부분 프레임워크를 사용하기 때문에 개발자의 실수가 발생할 확률이 더 줄어들었다.

메모리 릭이 발생했는지 확인하는 가장 확실한 방법은 `verbosegc`를 남겨 보는 방법이다. 그리고, 간단하게 확인할 수 있는 또 한가지 방법은 Full GC가 일어난 이후에 메모리 사용량을 보는 것이다. 정확하게 이야기해서 Full GC가 수행된 후에 Old 영역의 메모리 사용량을 보자. 만약 사용량이 80% 이상이면 메모리 릭을 의심해야 한다. `jstat`과 `verbosegc` 로그 결과를 갖고 확인하자. 그냥 보면 `jstat`의 Old 영역은 항상 올라가는 것이 정상이다. Full GC가 발생한 이후의 메모리 사용량으로 메모리 릭 여부를 판단하자.