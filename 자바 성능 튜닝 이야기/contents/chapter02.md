# Story 2. 내가 만든 프로그램의 속도를 알고 싶다.
시스템의 성능이 느릴 때 가장 먼저 해야 하는 작업은 **병목 지점을 파악하는 것**이다. Java 기반의 시스템에 대해 응답 속도나 각종 데이터를 측정하는 프로그램은 많다. 애플리케이션의 속도에 문제가 있을 때 분석하기 위한 툴로는 프로파일링 툴이나 APM 툴 등이 있다. 이 툴을 사용하면, 고속도로 위에서 헬기나 비행기로 훑어보듯이 병목 지점을 쉽게 파악할 수 있다.

## 프로파일링 툴이란?
프로파일링 툴은 시스템 문제 분석 툴이라 생각하면 된다.

- 프로파일링 툴: 개발자용 툴
  - 소스 레벨의 분석을 위한 툴
  - 애플리케이션의 세부 응답 시간까지 분석할 수 있다.
  - 메모리 사용량을 객체나 클래스, 소스의 라인 단위까지 분석할 수 있다.
  - 가격이 APM 툴에 비해 저렴하다.
  - 보통 사용자수 기반으로 가격이 정해진다.
  - 자바 기반의 클라이언트 프로그램 분석을 할 수 있다.
- APM(Application Performance Management) 툴: 운영 환경용 툴
  - 애플리케이션의 장애 상황에 대한 모니터링 및 문제점 진단이 주 목적이다.
  - 서버의 사용자 수나 리소스에 대한 모니터링을 할 수 있다.
  - 실시간 모니터링을 위한 툴이다.
  - 가격이 프로파일링 툴에 비해 비싸다.
  - 보통 CPU 수를 기반으로 가격이 정해진다.
  - 자바 기반의 클라이언트 프로그램 분석이 불가능하다.

참고로 프로파일링 툴은 대부분 느린 메서드, 느린 클래스를 찾는 것을 목적으로 하지만, APM 툴은 목적에 따라 용도가 상이하다. 어떤 APM 툴은 문제점 진단에 강한 한편, 다른 APM 툴은 시스템 모니터링 및 운영에 강하다.

각 툴이 제공하는 기능은 다양하고 서로 상이하지만, 응답 시간 프로파일링과 메모리 프로파일링 기능을 기본적으로 제공한다.

- 응답 시간 프로파일링 기능
  - 응답 시간을 측정하기 위함이다.
  - 하나의 클래스 내에서 사용되는 메서드 단위의 응답 시간을 측정한다.
  - 응답 시간 프로파일링을 할 땐 보통 CPU 시간과 대기 시간이 제공된다.
- 메모리 프로파일링
  - 잠깐 사용하고 GC의 대상이 되는 부분을 찾거나, 메모리 부족 현상이 발생하는 부분을 찾기 위함이다.
  - 클래스 및 메서드 단위의 메모리 사용량이 분석된다.
  - 툴에 따라 소스 라인 단위의 메모리 사용량도 측정할 수 있다.

## System 클래스
가장 간단하게 프로그램의 속도를 측정할 수 있는 방법은 `System` 클래스에서 제공하는 메서드를 활용하는 것이다.

Java의 JVM에서 사용할 수 있는 설정은 크게 두 가지로 나뉜다.

- Property(속성) 값: JVM에서 지정된 값들. `Properties`로 사용한다.
- Environment(환경) 값: 장비(서버)에 지정되어 있는 값들. `env`로 사용한다.

`Properties`를 사용하는 메서드는 다음과 같다.

- `static Properties getProperties()`: 현재 Java 속성 값들을 받아 온다.
- `static String getProperty(String key)`: key에 지정된 Java 속성 값을 받아 온다.
- `static String getProperty(String key, String def)`: key에 지정된 Java 속성 값을 받아온다. def는 해당 key가 존재하지 않을 경우 지정할 기본값이다.
- `static void setProperties(Properties props)`: props 객체에 담겨 있는 내용을 Java 속성에 지정한다.
- `static String setProperty(String key, String value)`: Java 속성에 있는 지정된 key의 값을 value 값으로 변환한다.

시스템 환경 변수 관련 메서드는 다음과 같다.
- `static Map<String, String> getenv()`: 현재 시스템 환경 값 목록을 스트링 형태의 맵으로 리턴한다.
- `static String getenv(String name)`: name에 지정된 환경 변수의 값을 얻는다.

네이티브 라이브러리를 활용할 때 사용할 수 있는 `System` 클래스는 다음과 같다.

- `static void load(String filename)`: 파일명을 지정하여 네이티브 라이브러리를 로딩한다.
- `static void loadLibrary(String libname)`: 라이브러리의 이름을 지정하여 네이티브 라이브러리를 로딩한다.

네이티브 라이브러리를 사용할 기회는 많지 않다. 필요하다면 다음 가이드를 참고하자.
- [JNI APIs and Developer Guides - Oracle](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)
- [Guide to JNI (Java Native Interface) - Baeldung](https://www.baeldung.com/jni)

그리고 운영중인 코드에 절대로 사용해선 안되는 메서드들이 있다.

- `static void gc()`: Java에서 사용하는 메모리를 명시적으로 해제하도록 GC를 수행하는 메서드
- `static void exit(int status)`: 현재 수행중인 Java VM을 멈춘다. 이 메서드는 절대로 수행하면 안 된다.
- `static void runFinalization()`: `Object` 객체에 있는 `finalize()` 메서드는 자동으로 호출되는데, 가비지 콜렉터가 알아서 해당 객체를 더 이상 참조할 필요가 없을 때 호출한다. 하지만 이 메서드를 호출하면 참조 해제 작업을 기다리는 모든 객체의 `finalize()` 메서드를 수동으로 수행해야 한다.

## System.currentTimeMillis와 System.nanoTime
시간 관련 메서드에 대해 알아보자.

- `static long currentTimeMillis()`: 현재의 시간을 ms로 리턴한다. (1/1,000초)

UTC라는 시간 표준 체계를 따른다. 참고로 우리는 UTC+9 지역에서 살고 있다.

간단한 예제를 살펴보자.

```java
public class CompareTimer {
    public static void main(String[] args) {
        CompareTimer timer = new CompareTimer();
        for (int i = 0; i < 10; i++) {
            timer.checkNanoTime();
            timer.checkCurrentTimeMillis();
        }
    }

    private DummyData dummy;

    public void checkCurrentTimeMillis() {
        long startTime = System.currentTimeMillis();
        dummy = timeMakeObjects();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("milli=" + elapsedTime);
    }

    public void checkNanoTime() {
        long startTime = System.nanoTime();
        dummy = timeMakeObjects();
        long endTime = System.nanoTime();
        double elapsedTime = (endTime - startTime) / 1000000.0;
        System.out.println("nano=" + elapsedTime);
    }

    public DummyData timeMakeObjects() {
        HashMap<String, String> map = new HashMap<>(1000000);
        ArrayList<String> list = new ArrayList<>(1000000);
        return new DummyData(map, list);
    }
}

@AllArgsConstructor
public class DummyData {
    HashMap<String, String> map;
    ArrayList<String> list;
}
```

여기서 사용한 `nanoTime()` 메서드는 뭘까?

- `static long nanoTime()`: 현재의 시간을 ns로 리턴한다. (1/1,000,000,000초)

이 메서드는 JDK 5.0부터 추가된 메서드다. JDK 1.4 버전에선 이 메서드가 없어 `System.currentTimeMillis()` 만을 써야 했다. 또한 JDK에 있는 설명에 의하면 `nanoTime()` 메서드를 만든 목적은 수행된 시간 측정이기 때문에 오늘의 날짜를 알아내는 부분에는 사용하면 안 된다.

저자는 `nanoTime()` 메서드가 나노 단위의 시간을 리턴해주기 때문에 `currentTimeMillis()` 메서드보다 정확하다고 생각한다. 되도록이면 `nanoTime()` 메서드의 결과로 판단하도록 하자.

위 예제의 실행 결과를 확인해보면 결과가 `System.currentTimeMillis()`로 측정한 결과가 `System.nanoTime()`으로 측정한 것보다 더 느린 것으로 나타난다. 사용 중인 Java 버전이 JDK 5.0 이상이라면 시간 측정용으로 만들어진 `nanoTime()` 메서드를 사용하기를 권장한다.

초기에 성능이 느리게 나온 이유는 여러 가지이지만, 클래스가 로딩되면서 성능 저하도 발생하고, JIT Optimizer가 작동하면서 성능 최적화도 되기 때문이라 보면 된다.

작성된 메서드의 성능을 측정하는 여러 가지 방법이 존재한다. 전문 측정 라이브러리를 사용하고자 한다면 다음과 같은 것들을 선택할 수 있겠다.

- JMH
- Caliper
- JUnitPerf
- JUnitBench
- ContiPerf

JMH(Java Microbenchmark Harness)는 JDK를 오픈 소스로 제공하는 OpenJDK에서 만든 성능 측정용 라이브러리다.

- [Github - OpenJDK JMH](https://github.com/openjdk/jmh)
- [자바봄 - JMH 사용해보기](https://javabom.tistory.com/75)

JMH는 여러 개의 스레드로 테스트도 가능하고, 워밍업 작업도 자동으로 수행해주기 때문에 정확한 측정이 가능하다. 일일이 케이스크를 구성할 필요 없이 각 케이스별로 테스트를 수행하고 그 결과를 확인할 수 있다. 자세한 사용 방법은 위 링크들을 참고하자.