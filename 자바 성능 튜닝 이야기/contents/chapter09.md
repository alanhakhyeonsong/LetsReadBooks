# Story 9. IO에서 발생하는 병목 현상
Java에서 입력과 출력은 스트림(stream)을 통해 이루어진다.  
IO는 성능에 영향을 가장 많이 미친다. IO에서 발생하는 시간은 CPU를 사용하는 시간과 대기 시간 중 대기 시간에 속하기 때문이다.

스트림을 읽는 데 관련된 주요 클래스는 다음과 같다. 여기 명시된 모든 입력과 관련된 스트림들은 `java.io.InputStream` 클래스로부터 상속받았다.

바이트 기반의 스트림 입력을 처리하기 위해서는 이 클래스의 하위 클래스를 사용한다.

- `ByteArrayInputStream`: 바이트로 구성된 배열을 읽어 입력 스트림을 만든다.
- `FileInputStream`: 이미지와 같은 바이너리 기반의 파일의 스트림을 만든다.
- `FilterInputStream`: 여러 종류의 유용한 입력 스트림의 추상 클래스다.
- `ObjectInputStream`: `ObjectOutputStream`을 통해 저장해 놓은 객체를 읽기 위한 스트림을 만든다.
- `PipedInputStream`: `PipedOutputStream`을 통해 출력된 스트림을 읽어서 처리하기 위한 스트림을 만든다.
- `SequenceInputStream`: 별개인 두 개의 스트림을 하나의 스트림으로 만든다.

문자열 기반의 스트림을 읽기 위해서 사용하는 클래스는 `java.io.Reader` 클래스의 하위 클래스들이다.

- `BufferedReader`: 문자열 입력 스트림을 버퍼에 담아 처리한다. 일반적으로 문자열 기반의 파일을 읽을 때 가장 많이 사용된다.
- `CharArrayReader`: `char` 배열로 된 문자 배열을 처리한다.
- `FilterReader`: 문자열 기반의 스트림을 처리하기 위한 추상 클래스이다.
- `FileReader`: 문자열 기반의 파일을 읽기 위한 클래스이다.
- `InputStreamReader`: 바이트 기반의 스트림을 문자열 기반의 스트림으로 연결하는 역할을 수행한다.
- `PipedReader`: 파이프 스트림을 읽는다.
- `StringReader`: 문자열 기반의 소스를 읽는다.

바이트 단위로 읽거나, 문자열 단위로 읽을 때 중요한 것은 한 번 연 스트림은 반드시 닫아 주어야 한다는 것이다. 스트림을 닫지 않으면 나중에 리소스가 부족해질 수 있다. 예를 들어, 파일을 열지 못하는 경우가 발생하면 관련된 파일을 관리하는 스트림의 상태 변경이 불가능해지기 때문이다.

## IO에서 병목이 발생한 사례
```java
String configUrl;

public Vector getRoute(String type) {
    if (configUrl == null) {
        configUrl = this.getClass().getResource("/xxx/config.xml");
    }
    obj - new DaoUtility(configUrl, "1");
    // ...
}
```

위 소스는 어떤 경로를 확인하는 시스템의 일부인데, 경로 하나를 가져오기 위해 매번 `configUrl`을 `DaoUtility`에 넘겨 준다. 요청이 올 때마다 `config.xml` 파일을 읽고 파싱하여 관련 DB 쿼리 데이터를 읽는다. 이 애플리케이션이 실제 운영된다면, 서버에는 엄청난 IO가 발생할 것이며, 응답 시간이 좋지 않으리라는 점도 쉽게 예상할 수 있다.

많은 프로젝트의 웹 애플리케이션에서 생각보다 많은 IO 작업이 수행된다. DB 쿼리나 여러 종류의 설정을 파일에 저장하고 사용하는 경우가 많다.

IO 병목 문제를 해결하기 위해 가장 좋은 방법은 데몬 스레드를 하나 생성하여 일정 주기로 한 번씩 확인하게 하는 것이다.

## 그럼 NIO의 원리는 어떻게 되는 거지?
JDK 1.4 부터 새롭게 추가된 NIO가 무엇인지 알아보자. 근본적으로 IO 작업이 OS에서 어떻게 수행되는지 먼저 생각해보자. Java를 사용하여 하드 디스크에 있는 데이터를 읽는다면 어떤 프로세스로 진행될까?

1. 파일을 읽으려는 메서드를 Java에 전달
2. 파일명을 전달받은 메서드가 OS 커널에게 파일을 읽어 달라고 요청
3. 커널이 하드 디스크로부터 파일을 읽어 자신의 커널에 있는 버퍼에 복사하는 작업을 수행한다. DMA에서 이 작업을 하게 됨.
4. Java에선 마음대로 커널의 버퍼를 사용하지 못하므로, JVM으로 그 데이터를 전달함.
5. JVM에서 메서드에 있는 스트림 관리 클래스를 사용하여 데이터를 처리한다.

Java에선 3, 4 작업을 수행할 때 대기하는 시간이 발생할 수밖에 없다. 이러한 단점을 보완하기 위해 NIO가 탄생했다. 3번 작업을 Java에서 직접 통제하여 시간을 더 단축할 수 있게 한 것이다.

추가된 개념들을 간단하게 정리하면 다음과 같다.

- 버퍼의 도입
- 채널의 도입
- 문자열 인코더와 디코더 제공
- Perl 스타일의 정규 표현식에 기초한 패턴 매칭 방법 제공
- 파일을 잠그거나 메모리 매핑이 가능한 파일 인터페이스 제공
- 서버를 위한 복합적인 Non-blocking IO 제공

## DirectByteBuffer를 잘못 사용하여 문제가 발생한 사례
NIO를 사용할 때 `ByteBuffer`를 사용하는 경우가 있다. 이는 네트워크나 파일에 있는 데이터를 읽어 들일 때 사용한다. `ByteBuffer`를 생성하는 메서드에는 `wrap()`, `allocate()`, `allocateDirect()`가 있다. 이 중 `allocateDirect()` 메서드는 데이터를 Java JVM에 올려서 사용하는 것이 아니라, OS 메모리에 할당된 메모리를 Native한 JNI로 처리하는 `DirectByteBuffer` 객체를 생성한다. 그런데, 이 객체는 필요할 때 계속 생성해선 안된다.

```java
public class DirectByteBufferCheck {
    public static void main(String[] args) {
        DirectByteBufferCheck check = new DirectByteBufferCheck();
        for (int i = 1; i < 1024000; i++) {
            check.getDirectByteBuffer();
            if (i % 100 == 0) {
                System.out.println(i);
            }
        }
    }

    public ByteBuffer getDirectByteBuffer() {
        ByteBuffer buffer;
        buffer = ByteBuffer.allocateDirect(65536);
        return buffer;
    }
}
```

위 예제를 실행하고 나서 GC 상황을 모니터링하기 위해 jstat 명령을 사용하여 확인해보면 결과가 좋지 않다.

FGC(Full GC)는 거의 5~10초에 한 번씩 발생하는 것을 볼 수 있다. O라고 되어 있는 Old 영역의 메모리는 증가하지 않는다. 그 이유는 `DirectByteBuffer`의 생성자 때문이다. 이 생성자는 `java.nio`에 아무런 접근 제어자 없이 선언된 `Bits`라는 클래스의 `reserveMemory()` 메서드를 호출한다. 이 메서드에는 JVM에 할당되어 있는 메모리보다 더 많은 메모리를 요구할 경우 `System.gc()` 메서드를 호출하도록 되어 있다.

JVM에 있는 코드에 `System.gc()` 메서드가 있기 때문에 해당 생성자가 무차별적으로 생성될 경우 GC가 자주 발생하고 성능에 영향을 줄 수 밖에 없다. 따라서 이 `DirectByteBuffer` 객체를 생성할 때는 매우 신중하게 접근해야만 하며, 가능하다면 싱글톤 패턴을 사용하는 것을 권장한다.

## lastModified() 메서드의 성능 저하
