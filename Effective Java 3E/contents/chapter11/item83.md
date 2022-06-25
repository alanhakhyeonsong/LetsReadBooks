# 아이템 83. 지연 초기화는 신중히 사용하라
## 지연 초기화
지연 초기화(lazy initialization)는 필드의 초기화 시점을 그 값이 처음 필요할 때까지 늦추는 기법이다. 그래서 값이 전혀 쓰이지 않으면 초기화도 결코 일어나지 않는다.

정적 필드와 인스턴스 필드 모두에 사용할 수 있으며, 클래스와 인스턴스 초기화 때 발생하는 위험한 순환 문제를 해결하는 효과가 있다.

### 최적화에서의 지연 초기화
지연 초기화는 주로 최적화 용도로 쓰인다.
- 다른 최적화 기법과 마찬가지로 필요할 때까지는 하지 않는게 최선이다.
- 클래스 혹은 인스턴스 생성 시의 초기화 비용은 줄어든다.
- 지연 초기화하는 필드에 접근하는 비용은 커진다.

지연 초기화하려는 필드들 중 초기화가 이뤄지는 비율에 따라, 실제 초기화에 드는 비용에 따라, 초기화된 각 필드를 얼마나 빈번히 호출하느냐에 따라 지연 초기화가 실제로는 성능을 느려지게 할 수도 있다.

### 지연 초기화가 필요한 경우
해당 클래스의 인스턴스 중 그 필드를 사용하는 인스턴스의 비율이 낮고, 그 필드를 초기화하는 비용이 클 때 필요하다.  
// ex. `hashCode`  
다만, 지연 초기화 적용 전후의 성능 측정이 필요하다.

### 멀티스레드 환경에서의 지연 초기화
지연 초기화하는 필드를 둘 이상의 스레드가 공유한다면 반드시 동기화가 필요하다. 그렇지 않다면 심각한 버그가 발생한다.(item 78)

## 초기화 방법
```java
public class FieldType {
}

public class Initialization {
    private static FieldType computeFieldValue() {
        return new FieldType();
    }
}
```

### 일반적인 인스턴스 필드 초기화
**대부분의 상황에서 일반적인 초기화가 지연 초기화보다 낫다.**
```java
// 인스턴스 필드를 초기화하는 일반적인 방법
private final FieldType field = computeFieldValue();
```

### 인스턴스 필드의 지연 초기화
**지연 초기화가 초기화 순환성(initialization circularity)을 깨뜨릴 것 같으면 `synchronized`를 단 접근자를 사용하자.**
```java
// synchronized 접근자 방식
private FieldType field;

private synchronized FieldType getField() {
    if (field == null)
        field = computeFieldValue();
    return field;
}
```

### 정적 필드용 지연 초기화 홀더 클래스 관용구
**성능 때문에 정적 필드를 지연 초기화해야 한다면 지연 초기화 홀더 클래스 관용구를 사용하자.** 클래스는 클래스가 처음 쓰일 때 비로소 초기화된다는 특성을 이용한 관용구다.

```java
// 정적 필드용 지연 초기화 홀더 클래스 관용구
private static class FieldHolder {
    static final FieldType field = computeFieldValue();
}

private static FieldType getField() { return FieldHolder.field; }
```
`getField`가 처음 호출되는 순간 `FieldHolder.field`가 처음 읽히면서 `FieldHolder` 클래스 초기화를 촉발한다. `getField` 메서드가 필드에 접근하면서 동기화를 전혀 하지 않으니 성능이 느려질 거리가 전혀 없다. 일반적인 VM은 오직 클래스를 초기화할 때만 필드 접근을 동기화할 것이다. 클래스 초기화가 끝난 후에는 VM이 동기화 코드를 제거하여, 그다음부터는 아무런 검사나 동기화 없이 필드에 접근하게 된다.

### 인스턴스 필드 지연 초기화용 이중검사 관용구
**성능 때문에 인스턴스 필드를 지연 초기화해야 한다면 이중검사 관용구를 사용하라.**

```java
private volatile FieldType field;

private FieldType getField() {
    FieldType result = field;
    if (result != null) { // 첫 번째 검사: 락 사용 안함
        return result;
    }

    synchronized(this) {
        if (field == null) // 두 번째 검사: 락 사용
            field = computeFieldValue();
        return field;
    }
}
```
이 관용구는 초기화된 필드에 접근할 때의 동기화 비용을 없애준다.
필드의 값을 두 번 검사하는 방식으로, 한 번은 동기화 없이 검사하고, 필드가 아직 초기화되지 않았다면 두 번째는 동기화하여 검사한다. 두 번째 검사에서도 필드가 초기화되지 않았을 때만 필드를 초기화한다.  
필드가 초기화 된 후로는 동기화하지 않으므로 해당 필드는 반드시 `volatile`로 선언해야 한다. (item 78)

// result 지역변수 : 필드가 이미 초기화된 상황에서는 이 필드를 딱 한번만 읽도록 보장하는 역할을 한다.

### 단일 검사 관용구
이중검사이 변종이다. 반복해서 초기화해도 상관 없는 인스턴스 필드를 지연 초기화해야 할 때 이중검사에서 두 번째 검사를 생략할 수 있다.

다만, 초기화가 중복해서 일어날 수 있다.
```java
// 초기화가 중복해서 일어날 수 있다.
private volatile FieldType field;

private FieldType getField() {
    FieldType result = field;
    if (result == null)
        field = result = computeFieldValue();
    return result;
}
```

### 짜릿한 단일검사(racy single-check) 관용구
```java
private FieldType field;

private FieldType getField() {
    FieldType result = field;
    if (result == null)
        field = result = computeFieldValue();
    return result;
}
```
모든 스레드가 필드의 값을 다시 계산해도 상관없고 필드의 타입이 `long`과 `double`을 제외한 다른 기본 타입이라면, 단일 검사의 필드 선언에서 `volatile` 한정자를 없애도 된다.  
어떤 환경에선 필드 접근 속도를 높여주지만, 초기화가 스레드당 최대 한 번 더 이뤄질 수 있다. (거의 쓰이지 않는다.)

## 핵심 정리
- 대부분의 필드는 지연시키지 말고 곧바로 초기화해야 한다.
- 성능 때문에 혹은 위험한 초기화 순환을 막기 위해 꼭 지연 초기화를 써야 한다면 올바른 지연 초기화 기법을 사용하자.
- 인스턴스 필드에는 이중검사 관용구를, 정적 필드에는 지연 초기화 홀더 클래스 관용구를 사용하자.
- 반복해 초기화해도 괜찮은 인스턴스 필드에는 단일검사 관용구도 고려 대상이다.

## 좀 더 공부하면 좋을 자료
- [Lazy Initialization in Spring Boot 2](https://www.baeldung.com/spring-boot-lazy-initialization)

```
<Effects of Lazy Initialization>
Enabling lazy initialization in the whole application could produce both positive and negative effects.

Let's talk about some of these, as they're described in the official announcement of the new functionality:

1. Lazy initialization may reduce the number of beans created when the application is starting – therefore, we can improve the startup time of the application
2. As none of the beans are created until they are needed, we could mask issues, getting them in run time instead of startup time
3. The issues can include out of memory errors, misconfigurations, or class-definition-found errors
4. Also, when we're in a web context, triggering bean creation on demand will increase the latency of HTTP requests – the bean creation will affect only the first request, but this may have a negative impact on load-balancing and auto-scaling.

```


- [Eager/Lazy Loading In Hibernate](https://www.baeldung.com/hibernate-lazy-eager-loading)

```
<Lazy Loading>
Advantages:

- Much smaller initial load time than in the other approach
- Less memory consumption than in the other approach

Disadvantages:

- Delayed initialization might impact performance during unwanted moments.
- In some cases we need to handle lazily initialized objects with special care, or we might end up with an exception.

<Eager Loading>
Advantages:

- No delayed initialization-related performance impacts

Disadvantages:

- Long initial loading time
- Loading too much unnecessary data might impact performance
```