# Story 16. JVM은 도대체 어떻게 구동될까?
웹 기반 시스템을 배포할 때 그냥 재시작만 한다면, 배포 직후 시스템 사용자들은 엄청나게 느린 응답 시간과 함께 시스템에 대한 많은 불만을 갖게 될 수도 있다. Warming up이 필요한데 도대체 왜 WAS를 재시작하면 성능이 느릴까?

이번 장에선 JVM은 어떻게 구동되는지 살펴보며 그 답을 찾아간다.

- HotSpot VM의 구조
- JIT 옵티마이저
- JVM의 구동 절차
- JVM의 종료 절차
- 클래스 로딩의 절차
- 예외 처리의 절차

들어가기에 앞서 다음 자료들을 먼저 읽어보자.

- [핫스팟 (가상 머신) - 위키피디아](https://ko.wikipedia.org/wiki/%ED%95%AB%EC%8A%A4%ED%8C%9F_(%EA%B0%80%EC%83%81_%EB%A8%B8%EC%8B%A0))
- [OpenJDK - HotSpot Glossary of Terms](https://openjdk.org/groups/hotspot/docs/HotSpotGlossary.html)

## HotSpot VM은 어떻게 구성되어 있을까?
Java를 만든 Sun에선 성능을 개선하기 위해 JIT 컴파일러를 만들었고, 이름을 HotSpot으로 지었다. JIT 컴파일러는 프로그램의 성능에 영향을 주는 지점에 대해 지속적으로 분석한다. 분석된 지점은 부하를 최소화하고, 높은 성능을 내기 위한 최적화의 대상이 된다.

HotSpot은 Java 1.3 버전부터 기본 VM으로 사용되어 왔다. HotSpot VM은 다음 세 가지 주요 컴포넌트로 구성되어 있다.

- VM 런타임
- JIT 컴파일러
- 메모리 관리자

HotSpot VM은 높은 성능과 확장성을 제공한다. JIT 컴파일러는 자바 애플리케이션이 수행되는 상황을 보고 동적으로 최적화를 수행한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2eefd72c-f59e-4dc2-aaf4-0780f7fe854e)

HotSpot VM의 아키텍처는 위와 같은데, HotSpot VM 런타임에 GC 방식과 JIT 컴파일러를 끼워 맞춰 사용할 수 있다. 이를 위해 VM 런타임은 JIT 컴파일러용 API와 가비지 컬렉터용 API를 제공한다. 그리고, JVM을 시작하는 런처와 스레드 관리, JNI 등도 VM 런타임에서 제공한다.

## JIT Optimizer라는 게 도대체 뭘까?
전통적인 컴파일러인 C, C++는 소스코드에서 object 파일을 만들고, 이 object로 수행 가능한 라이브러리로 만든다. 이는 애플리케이션이 수행되는 것과 비교해서 지속, 반복적으로 수행되지 않고 한 번만 수행된다.

반면, Java는 javac라는 컴파일러를 사용한다. 이 컴파일러는 소스코드를 바이트 코드로 된 class라는 파일로 변환해준다. 그렇기 때문에 JVM은 항상 바이트 코드로 시작하며, 동적으로 기계에 의존적인 코드로 변환한다.

JIT는 애플리케이션에서 각각의 메서드를 컴파일할 만큼 시간적 여유가 많지 않다. 그러므로, 모든 코드는 초기에 인터프리터에 의해 시작되고, 해당 코드가 충분히 많이 사용될 경우에 컴파일할 대상이 된다. HotSpot VM에서 이 작업은 **각 메서드에 있는 카운터를 통해 통제되며, 메서드에는 두 개의 카운터가 존재한다.**

- 수행 카운터(invocation counter): 메서드를 시작할 때마다 증가
- 벡에지 카운터(backedge counter): 높은 바이트 코드 인덱스에서 낮은 인덱스로 컨트롤 흐름이 변경될 때마다 증가

벡에지 카운터는 메서드가 루프가 존재하는지를 확인할 때 사용되며, 수행 카운터보다 컴파일 우선순위가 높다. 이 카운터들이 인터프리터에 의해 증가될 때마다, 그 값들이 한계치에 도달했는지를 확인하고, 도달했을 경우 인터프리터는 컴파일을 요청한다. 여기서 수행 카운터에서 사용하는 한계치는 `CompileThreshold`이며, 벡에지 카운터에서 사용하는 한계치는 다음의 공식으로 계산한다.

`CompileThreshold * OnStackReplacePercentage / 100`

JVM이 시작할 때 위 두 값들을 지정 가능하고 시작 옵션에 다음과 같이 지정할 수 있다.

- `XX:CompileThreshold=35000`
- `XX:OnStackReplacePercentage=80`

컴파일 요청 이후 동작 과정은 다음과 같다.

1. 컴파일이 요청되면 컴파일 대상 목록의 큐에 쌓이고, 하나 이상의 컴파일러 스레드가 이 큐를 모니터링한다.
2. 만약 컴파일러 스레드가 바쁘지 않을 때는 큐에서 대상을 빼내 컴파일을 시작한다. 보통 인터프리터는 컴파일이 종료되기를 기다리지 않는 대신, 수행 카운터를 리셋하고 인터프리터에서 메서드 수행을 계속한다.
3. 컴파일이 종료되면, 컴파일된 코드와 메서드가 연결되어 그 이후부터는 메서드가 호출되면 컴파일된 코드를 사용하게 된다.

만약 인터프리터에서 컴파일이 종료될 때까지 기다리도록 하려면 JVM 시작시 `-Xbatch`나 `-XX:-BackgroundCompilation` 옵션을 지정하여 컴파일을 기다리도록 할 수도 있다.

HotSpot VM은 OSR(On Stack Replacement)이라는 특별한 컴파일도 수행한다. 이 OSR은 인터프리터에서 수행한 코드 중 오랫동안 루프가 지속되는 경우에 사용된다. 만약 해당 코드의 컴파일이 완료된 상태에서 최적화되지 않은 코드가 수행되고 있는 것을 발견한 경우에 인터프리터에 계속 머무리지 않고 컴파일된 코드로 변경한다. 이 작업은 인터프리터에서 시작된 오랫동안 지속되는 루프가 다시는 불리지 않을 경우엔 도움이 되지 않지만, 루프가 끝나지 않고 지속적으로 수행되고 있을 경우에는 큰 도움이 된다.

> 📌 Java 5 HotSpot VM의 추가된 기능  
> JVM이 시작될 때 플랫폼과 시스템 설정을 평가하여 자동으로 gc를 선정하고, 자바 힙 크기와 JIT 컴파일러를 선택하는 기능이다. 이를 통해 애플리케이션의 활동과 객체 할당 비율에 따라 gc가 동적으로 자바 힙 크기를 조절하며 New의 Eden과 Survivor, Old 영역의 비율을 자동적으로 조절하는 것을 의미한다. `-XX:+UseParallelGC`와 `-XX:+UseParallelOldGC`에서만 적용되며, 이 기능을 제거하려면 `-XX:UseAdaptiveSizePolicy`라는 옵션을 적용하여 끌 수 있다.

## JRockit의 JIT 컴파일 및 최적화 절차
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/7483f64e-3a6d-4c47-a460-fbbf4c48a3b0)

JVM은 각 OS에서 작동할 수 있도록 자바 코드를 입력 값(바이트 코드)으로 받아 각종 변환을 거친 후 해당 칩의 아키텍처에서 잘 돌아가는 기계어 코드로 변환되어 수행되는 구조로 되어 있다. 좀 더 상세한 최적화 절차는 다음과 같다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3cb95b52-7d8b-4c29-ad4c-7f6062648e5c)

- [Understanding Just-In-Time Compilation and Optimization | oracle docs](https://docs.oracle.com/cd/E15289_01/JRSDK/underst_jit.htm)

JRockit의 최적화 단계는 위와 같으며 각각의 단계는 다음과 같은 작업을 수행한다.

### JRockit performs JIT compilation
자바 애플리케이션을 실행하면 기본적으론 1번 단계인 JIT 컴파일을 거친 후 실행이 된다. 이를 거친 후 메서드가 수행되면, 그 다음부턴 컴파일 된 코드를 호출하기 때문에 처리 성능이 빨라진다.

애플리케이션이 시작하는 동안 몇천 개의 새로운 메서드가 수행되며 이로 인해 다른 JVM보다 JRockit JVM이 더 느릴 수도 있다. 이 작업으로 인해 JIT가 메서드를 수행하고 컴파일 하는 작업은 오버헤드가 되지만, JIT가 없으면 JVM은 계속 느린 상태로 지속될 것이다. 정리하자면 **JIT를 사용하면 시작할 때의 성능은 느리겠지만, 지속적으로 수행할 때는 더 빠른 처리가 가능하다.** 모든 메서드를 컴파일하고 최적화하는 작업은 JVM 시작 시간을 느리게 만들기에 시작할 때는 모든 메서드를 최적화하진 않는다.

### JRockit monitors threads
JRockit엔 **sampler thread**라는 스레드가 존재하여 주기적으로 애플리케이션의 스레드를 점검한다. 이 스레드는 어떤 스레드가 동작 중인지 여부와 수행 내역을 관리한다. 이 정보들을 통해 어떤 메서드가 많이 사용되는지를 확인하여 최적화 대상을 찾는다.

### JRockit JVM Runs Optimization
sampler thread가 식별한 대상을 최적화한다. 이는 백그라운드에서 진행되며 수행중인 애플리케이션에 영향을 주진 않는다.

최적화 예시는 다음과 같다.

```java
class A {
    B b;
    public void foo() {
        y = b.get();
        z = b.get();
        sum = y + z;
    }
}

class B {
    int value;
    final int get() {
        return value;
    }
}
```

JRockit JIT 컴파일러의 최적화 결과는 다음과 같다.

```java
class A {
    B b;
    public void foo() {
        y = b.value;
        sum = y + y;
    }
}
```

자세한 내용은 [Understanding Just-In-Time Compilation and Optimization | oracle docs](https://docs.oracle.com/cd/E15289_01/JRSDK/underst_jit.htm)를 참고하자.

## IBM JVM의 JIT 컴파일 및 최적화 절차
IBM JVM의 JIT 컴파일 방식은 5가지로 나뉜다.

- 인라이닝
- 지역 최적화
- 조건 구문 최적화
- 글로벌 최적화
- 네이티브 코드 최적화


컴파일된 코드는 코드 캐시라고 하는 JVM 프로세스 영역에 저장된다. 결과적으로 JVM 프로세스는 JVM 수행 파일과 컴파일된 JIT 코드의 집합으로 구분된다.

자세한 사항은 [JIT 컴파일러 - IBM Documentation](https://www.ibm.com/docs/ko/sdk-java-technology/8?topic=reference-jit-compiler)과 [JIT 컴파일러의 코드 최적화 방법 - IBM Documentation](https://www.ibm.com/docs/ko/sdk-java-technology/8?topic=compiler-how-jit-optimizes-code)을 참고하자.

## JVM이 시작할 때의 절차는 이렇다.
