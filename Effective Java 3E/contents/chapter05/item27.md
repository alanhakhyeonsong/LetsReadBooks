# 아이템 27. 비검사 경고를 제거하라

제네릭을 사용하기 시작하면 수많은 컴파일러 경고를 보게 될 것이다. 비검사 형변환 경고, 비검사 메서드 호출 경고, 비검사 매개변수화 가변인수 타입 경고, 비검사 변환 경고 등이다. 제네릭에 익숙해질수록 마주치는 경고 수는 줄겠지만 새로 작성한 코드가 한번에 깨끗하게 컴파일되리라 기대하지는 말자.

대부분의 비검사 경고는 쉽게 제거할 수 있다. Java 7부터 지원하는 다이아몬드 연산자(<>)만으로 해결할 수 있는 경우가 많다.

제거하기 어려운 경고도 많지만, **할 수 있는 한 모든 비검사 경고를 제거하라. 제거한다면 그 코드는 타입 안전성이 보장된다.**

**경고를 제거할 수는 없지만 타입 안전하다고 확신할 수 있다면 `@SuppressWarnings("unchecked")` 애너테이션을 달아 경고를 숨기자.**

- 해당 애노테이션은 항상 좁은 범위에 적용시키자.
- 절대로 클래스 전체에 적용해서는 안된다.
- `@SuppressWarnings("unchecked")` 애너테이션을 사용할 때면 그 경고를 무시해도 안전한 이유를 항상 주석으로 남겨야 한다.

```java
public <T> T[] toArray(T[] a) {
    if (a.length < size) {
        // 생성한 배열과 매개변수로 받은 배열의 타입이 모두 T[]로 같으므로
        // 올바른 형변환이다.
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Arrays.copyOf(elements, size, a.getClass());
        return result;
    }
    System.arraycopy(elements, 0, a, 0, size);
    if (a.length > size)
        a[size] = null;
    return a;
}
```

### SuppressWarnings 옵션 종류
- all : 모든 경고 
- cast : 캐스트 연산자 관련 경고 
- dep-ann : 사용하지 말아야 할 주석 관련 경고 
- deprecation : 사용하지 말아야 할 메서드 관련 경고 
- fallthrough : switch문에서 break 누락 관련 경고 
- finally : 반환하지 않는 finally 블럭 관련 경고 
- null : null 분석 관련 경고 
- rawtypes : 제너릭을 사용하는 클래스 매개 변수가 불특정일 때의 경고 
- unchecked : 검증되지 않은 연산자 관련 경고 
- unused : 사용하지 않는 코드 관련 경고

## 핵심 정리

- 비검사 경고는 중요하니 무시하지 말자.
- 모든 비검사 경고는 런타임에 `ClassCastException`을 일으킬 수 있는 잠재적 가능성을 뜻하니 최선을 다해 제거하라.
- 경고를 없앨 방법을 찾지 못하겠다면, 그 코드가 타입 안전함을 증명하고 가능한 한 범위를 좁혀 `@SuppressWarnings("unchecked")` 애너테이션으로 경고를 숨겨라.
- 그런 다음 경고를 숨기기로 한 근거를 주석으로 남겨라.
