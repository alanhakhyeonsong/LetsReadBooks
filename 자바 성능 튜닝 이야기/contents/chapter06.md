# Story 6. static 제대로 한번 써 보자.
잘 모르고 `static`을 사용하다가는 시스템이 더 느려지거나, 오류를 내뿜는 시스템이 될 수도 있다. 심지어 JVM이 죽어버리는 상황도 발생할 수 있다.

## static의 특징
```java
public class Test {
    int instance variable;
    static int classVariable;
    public void method(int parameter) {
        int localVariable;
    }
}
```

`static`으로 선언한 변수는 클래스 변수라고 한다. 이는 '객체의 변수'가 되는 것이 아니라, '클래스의 변수'가 되기 때문이다. 100개의 `Test` 클래스의 인스턴스를 생성하더라도, 모든 객체가 `classVariable`에 대해서는 동일한 주소의 값을 참조한다.

`static` 변수는 객체를 생성해서 참조할 필요는 없다. 객체를 참조해서 값을 더하든, 클래스를 직접 참조해서 값을 더하든 동일한 값을 참조한다.

`static` 초기화 블록을 간단하게 짚어보고 넘어가자.

```java
public class Test2 {
    static String staticVal;
    static {
        staticVal = "Static Value";
        staticVal = StaticBasicSample.staticInt + "";
    }

    public static void main(String[] args) {
        System.out.println(Test2.staticVal);
    }

    static {
        staticVal = "Performance is important!!!";
    }
}
```

`static` 초기화 블록은 위와 같이 클래스 어느 곳에서나 지정할 수 있다. 이 `static` 블록은 클래스가 최초 로딩될 때 수행되므로 생성자 실행과 상관없이 수행된다. 또한 위의 예제와 같이 여러 번 사용할 수 있으며, 이와 같이 사용했을 때 `staticVal`의 값은 마지막에 지정한 값이 된다.

`static`의 특징은 다른 JVM에선 `static`이라 선언해도 다른 주소나 다른 값을 참조하지만, 하나의 JVM이나 WAS 인스턴스에선 같은 주소에 존재하는 값을 참조한다는 것이다. 그리고 GC의 대상도 되지 않는다. 따라서 `static`을 잘 사용하면 성능을 뛰어나게 향상시킬 수 있지만, 잘못 사용하면 예기치 못한 결과를 초래하게 된다. 특히 웹 환경에서 잘못 사용하다간 여러 쓰레드에서 하나의 변수에 접근할 수도 있기 때문에 데이터가 꼬이는 큰 일이 발생할 수도 있다.

## static 잘 활용하기
- 자주 사용하고 절대 변하지 않는 변수는 `final static`으로 선언하자
- 설정 파일 정보도 `static`으로 관리하자
- 코드성 데이터는 DB에서 한 번만 읽자.
- 서버 인스턴스가 하나만 있다면 상관 없지만, 서로 다른 JVM에 올라가 있는 상황이라면 상이한 결과가 나오는 것을 방지하기 위해 cache를 사용하자.

## static 잘못 쓰면 이렇게 된다
```java
public class BadQueryManager {
    private static String queryURL = null;

    public BadQueryManager(String badUrl) {
        queryURL = badUrl;
    }

    public static String getSql(String idSql) {
        try {
            FileReader reader = new FileReader();
            HashMap<String, String> document = reader.read(queryURL);
            return document.get(idSql);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }
}
```

`getSql()` 메서드와 `queryURL`을 `static`으로 선언한 것이 잘못된 부분이다. 웹 환경에서 동작한다면, 여러 화면에서 호출할 경우에 `queryURL`은 그때 그때 바뀌게 된다. 그 결과 모든 스레드에서 동일한 주소를 가리키게 되어 문제가 발생한다.

## static과 메모리 릭
**`static`으로 선언한 부분은 GC가 되지 않는다.** 만약 어떤 클래스에 데이터를 `Collection`에 담을 때 해당 `Collection` 객체를 `static`으로 선언하면, 지속적으로 해당 객체에 데이터가 쌓이는 경우 더이상 GC가 되지 않으면서 시스템은 `OutOfMemoryError`를 발생시키게 된다.

더 이상 사용 가능한 메모리가 없어지는 현상을 **메모리 릭(Memory Leak)** 이라 한다. `static`과 `Collection` 객체를 잘못 사용하면 이와 같은 현상이 발생한다.

메모리 릭의 원인은 메모리의 현재 상태를 파일로 남기는 `HeapDump`라는 파일을 통해 확인 가능하다. `JDK/bin` 디렉터리에 있는 `jmap`이라는 파일을 사용하여 덤프를 남길 수 있으며, 남긴 덤프는 eclipse 프로젝트에서 제공하는 MAT과 같은 툴을 통해 분석하면 된다.