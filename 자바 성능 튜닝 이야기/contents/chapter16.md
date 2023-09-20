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

## JVM이 시작할 때의 절차는 이렇다
`java` 명령으로 특정 클래스를 실행하면 수행되는 단계를 간단히 정리해보면 다음과 같다.

1. `java` 명령어 줄에 있는 옵션 파싱: 일부 명령은 자바 실행 프로그램에서 적절한 JIT 컴파일러를 선택하는 등의 작업을 하기 위해 사용하고, 다른 명령들은 HotSpot VM에 전달된다.
2. 자바 힙 크기 할당 및 JIT 컴파일러 타입 지정: 메모리 크기나 JIT 컴파일러 종류가 명시적으로 지정되지 않은 경우에 자바 실행 프로그램이 시스템의 상황에 맞게 선정한다. 이 과정은 HotSpot VM Adaptive Tuning을 거친다.
3. `CLASSPATH`와 `LD_LIBRARY_PATH` 같은 환경 변수를 지정한다.
4. 자바의 `Main` 클래스가 지정되지 않았으면, Jar 파일의 `manifest` 파일에서 `Main` 클래스를 확인한다.
5. JNI의 표준 API인 `JNI_CreateJavaVM`를 사용하여 새로 생성한 `non-primordial`이라는 스레드에서 HotSpot VM을 생성한다.
6. HotSpot VM이 생성되고 초기화되면, `Main` 클래스가 로딩된 런처에선 `main()` 메서드의 속성 정보를 읽는다.
7. `CallStackVoidMethod`는 네이티브 인터페이스를 불러 HotSpot VM에 있는 `main()` 메서드가 수행된다. 이때 자바 실행 시 `Main` 클래스 뒤에 있는 값들이 전달된다.

자바의 VM을 생성하는 `JNI_CreateJavaVM` 단계의 절차를 자세히 보면 다음과 같다.

1. `JNI_CreateJavaVM`는 동시에 두개의 스레드에서 호출할 수 없고, 오직 하나의 HotSpot VM 인스턴스가 프로세스 내에서 생성될 수 있도록 보장된다. HotSpot VM이 정적인 데이터 구조를 생성하기 때문에 다시 초기화는 불가능하여, 오직 하나의 HotSpot VM이 프로세스에서 생성될 수 있다.
2. JNI 버전이 호환성이 있는지 점검하고, GC 로깅을 위한 준비도 완료된다.
3. OS 모듈들이 초기화된다. 예를 들면 랜덤 번호 생성기, PID 할당 등이 여기에 속한다.
4. 커맨드 라인 변수와 속성들이 `JNI_CreateJavaVM` 변수에 전달되고, 나중에 사용하기 위해 파싱한 후 보관한다.
5. 표준 자바 시스템 속성(`properties`)이 초기화된다.
6. 동기화, 메모리, safepoint 페이지와 같은 모듈들이 초기화된다.
7. `libzip`, `libhpi`, `libjava`, `libthread`와 같은 라이브러리들이 로드된다.
8. 시그널 처리기가 초기화 및 설정된다.
9. 스레드 라이브러리가 초기화된다.
10. 출력 스트림 로거가 초기화된다.
11. JVM을 모니터링하기 위한 에이전트 라이브러리가 설정되어 있으면 초기화 및 시작된다.
12. 스레드 처리를 위해 필요한 스레드 상태와 스레드 로컬 저장소가 초기화된다.
13. HotSpot VM의 글로벌 데이터들이 초기화 된다. 여기에는 이벤트 로그, OS 동기화, 성능 통계 메모리(`perfMemory`), 메모리 할당자(`chunkPool`)들이 있다.
14. HotSpot VM에서 스레드를 생성할 수 있는 상태가 된다. `main` 스레드가 생성되고, 현재 OS 스레드에 붙는다. 그러나 아직 스레드 목록에 추가되진 않는다.
15. 자바 레벨의 동기화가 초기화 및 활성화된다.
16. 부트 클래스로더, 코드 캐시, 인터프리터, JIT 컴파일러, JNI, 시스템 ditcionary, 글로벌 데이터 구조의 집합인 universe 등이 초기화 된다.
17. 스레드 목록에 자바 `main` 스레드가 추가되고, universe의 상태를 점검한다. HotSpot VM의 중요한 기능을 하는 HotSpot VMThread가 생성된다. 이 시점에 HotSpot VM의 현재 상태를 JVMTI에 전달한다.
18. `java.lang` 패키지에 있는 `String`, `System`, `Thread`, `ThreadGroup`, `Class` 클래스와 `java.lang` 하위 패키지에 있는 `Method`, `Finalizer` 클래스 등이 로딩되고 초기화된다.
19. HotSpot VM의 시그널 핸들러 스레드가 시작되고, JIT 컴파일러가 초기화되며, HotSpot의 컴파일 브로커 스레드가 시작된다. 그리고, HotSpot VM과 관련된 각종 스레드들이 시작한다. 이때부터 HotSpot VM의 전체 기능이 동작한다.
20. JNIEnv가 시작되며, HotSpot VM을 시작한 호출자에게 새로운 JNI 요청을 처리할 상황이 되었다고 전달해준다.

## JVM이 종료될 때의 절차는 이렇다
만약 정상적으로 JVM을 종료시킬 때는 다음의 절차를 거치지만, OS의 `kill -9`와 같은 명령으로 JVM을 종료시키면 이 절차를 따르지 않는다.

만약 JVM이 시작할 때 오류가 있어 시작을 중지할 때나, JVM에 심각한 에러가 있어 중지할 필요가 있을 때는 `DestroyJavaVM`이라는 메서드를 HotSpot 런처에서 호출한다. HotSpot VM의 종료는 다음의 `DestroyJavaVM` 메서드의 종료 절차를 따른다.

1. HotSpot VM이 작동중인 상황에선 단 하나의 데몬이 아닌 스레드가 수행될 때까지 대기한다.
2. `java.lang` 패키지에 있는 `Shutdown` 클래스의 `shutdown()`이 수행된다. 이 메서드가 수행되면 자바 레벨의 shutdown hook이 수행되고, `finaliation-on-exit`이란 값이 `true`일 경우 자바 객체 finalizer를 수행한다.
3. HotSpot VM 레벨의 shutdown hook을 수행함으로써 HotSpot VM의 종료를 준비한다. 이 작업은 `JVM_OnExit()`을 통해 지정된다. 그리고 HotSpot VM의 profiler, stat sampler, watcher, gc 스레드를 종료시킨다. 이 작업들이 종료되면 JVMTI를 비활성화하며, `Signal` 스레드를 종료시킨다.
4. `HotSpot`의 `JavaThread::exit()`을 호출하여 JNI 처리 블록을 해제한다. 그리고 guard pages, 스레드 목록에 있는 스레드를 삭제한다. 이 순간부터는 HotSpot VM에선 자바 코드를 실행하지 못한다.
5. HotSpot VM 스레드를 종료한다. 이 작업을 수행하면 HotSpot VM에 남아있는 HotSpot VM 스레드들을 safepoint로 옮기고, JIT 컴파일러 스레드들을 중지시킨다.
6. JNI, HotSpot VM, JVMTI barrier에 있는 추적 기능을 종료시킨다.
7. 네이티브 스레드에서 수행하고 있는 스레드들을 위해 HotSpot의 `vm exited` 값을 설정한다.
8. 현제 스레드를 삭제한다.
9. 입출력 스트림을 삭제하고, `PerfMemory` 리소스 연결을 해제한다.
10. JVM 종료를 호출한 호출자로 복귀한다.

꼭 외우고 있어야 하는 내용도 아니고, 참고로만 알아두자. 이해가 안되도 좋다. (나도 모르겠음;)

## 클래스 로딩 절차도 알고 싶어요?
1. 주어진 클래스의 이름으로 클래스 패스에 있는 바이너리로 된 자바 클래스를 찾는다.
2. 자바 클래스를 정의한다.
3. 해당 클래스를 나타내는 `java.lang` 패키지의 `Class` 클래스의 객체를 생성한다.
4. 링크 작업이 수행된다. 이 단계에서 `static` 필드를 생성 및 초기화하고, 메서드 테이블을 할당한다.
5. 클래스의 초기화가 진행되며, 클래스의 `static` 블록과 `static` 필드가 가장 먼저 초기화된다. 당연하지만, 해당 클래스가 초기화 되기 전에 부모 클래스의 초기화가 먼저 이루어진다.

loading → linking → initializing으로 기억하면 된다.

참고로 클래스 로딩 시 다음과 같은 에러가 발생할 수 있는데 일반적으로 이 에러들은 자주 발생하지 않는다.

- `NoClassDefFoundError`
- `ClassFormatError`
- `UnsupportedClassVersionError`
- `ClassCircularityError`
- `IncompatibleClassChangeError`
- `VerifyError`

클래스 로더가 클래스를 찾고 로딩할 때 다른 클래스 로더에 클래스를 로딩해 달라고 요청하는 경우가 있다. 이를 class loader delegation이라 부른다. 클래스 로더는 계층적으로 구성되어 있다.

기본 클래스 로더는 시스템 클래스 로더라 불리며 `main` 메서드가 있는 클래스와 클래스 패스에 있는 클래스들이 이에 속한다. 그 하위에 있는 애플리케이션 클래스 로더는 Java SE의 기본 라이브러리에 있는 것이 될 수도 있고, 개발자가 임의로 만든 것일 수도 있다.

### 부트스트랩 클래스 로더
HotSpot VM은 부트스트랩 클래스 로더를 구현한다. 부트스트랩 클래스 로더는 HotSpot VM의 `BOOTCLASSPATH`에서 클래스들을 로드한다. 예를 들면, Java SE 클래스 라이브러리들을 포함하는 `rt.jar`가 이에 속한다.

### HotSpot의 클래스 메타데이터
HotSpot VM 내에서 클래스를 로딩하면 클래스에 대한 `instanceKlass`와 `arrayKlass`라는 내부적인 형식을 VM의 Perm 영역에 생성한다. `instanceKlass`는 클래스 정보를 포함하는 `java.lang.Class` 클래스의 인스턴스를 말한다.

HotSpot VM은 내부 데이터 구조인 `klassOop`이라는 것을 사용하여 내부적으로 `instanceKlass`에 접근한다. 여기서 Oop은 ordinary object pointer의 약자다.

### 내부 클래스 로딩 데이터의 관리
HotSpot VM은 클래스 로딩을 추적하기 위해 다음 3개의 해시 테이블을 관리한다.

- `SystemDictionary`: 로드된 클래스를 포함하며, 클래스 이름 및 클래스 로더를 키를 갖고 그 값으로 `klassOop`를 갖고 있다. `SystemDictionary`는 클래스 이름과 초기화한 로더의 정보, 클래스 이름과 정의한 로더의 정보도 포함한다. 이 정보들은 `safepoint`에서만 제거된다.
- `PlaceholderTable`: 현재 로딩된 클래스들에 대한 정보를 관리한다. 이 테이블은 `ClasCircularityError`를 체크할 때 사용하며, 다중 스레드에서 클래스를 로딩하는 클래스 로더에서도 사용된다.
- `LoaderConstraintTable`: 타입 체크시의 제약 사항을 추정하는 용도로 사용된다.

## 예외는 JVM에서 어떻게 처리될까?
JVM은 자바 언어의 제약을 어겼을 때 exception이라는 시그널로 처리한다. HotSpot VM 인터프리터, JIT 컴파일러 및 다른 HotSpot VM 컴포넌트는 예외 처리와 모두 관련되어 있다. 일반적인 예외 처리 케이스는 두 가지다.

- 예외를 발생한 메서드에서 잡을 경우
- 호출한 메서드에 의해 잡힐 경우

후자의 경우엔 보다 복잡하며, 스택을 뒤져서 적당한 핸들러를 찾는 작업을 필요로 한다.

예외는,
- 던져진 바이트 코드에 의해 초기화 될 수 있으며,
- VM 내부 호출의 결과로 넘어올 수도 있고,
- JNI 호출로부터 넘어올 수도 있고,
- 자바 호출로부터 넘어올 수도 있다.

여기서 가장 마지막 경우는 단순히 앞의 세가지 경우의 마지막 단계에 속할 뿐이다.

VM이 예외가 던져졌다는 것을 알아차렸을 때, 해당 예외를 처리하는 가장 가까운 핸들러를 찾기 위해 HotSpot VM 런타임 시스템이 수행된다. 이 때, 핸들러를 찾기 위해선 다음 3개의 정보가 사용된다.

- 현재 메서드
- 현재 바이트 코드
- 예외 객체

만약 현재 메서드에서 핸들러를 찾지 못했을 땐 현재 수행되는 스택 프리엠을 통해 이전 프레임을 찾는 작업을 수행한다. 적당한 핸들러를 찾으면, HotSpot VM 수행 상태가 변경되며, HotSpot VM은 핸들러로 이동하고 자바 코드 수행은 계속된다.