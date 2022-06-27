# 아이템 87. 커스텀 직렬화 형태를 고려해보라
**먼저 고민해보고 괜찮다고 판단될 때만 기본 직렬화 형태를 사용하라.** 유연성, 성능, 정확성 측면에서 신중히 고민한 뒤 합당할 때만 사용해야 한다. 일반적으로 우리가 직접 설계하더라도 기본 직렬화 형태와 거의 같은 결과가 나올 경우에만 기본 형태를 사용해야 한다.

어떤 객체의 기본 직렬화 형태는 그 객체를 루트로하는 객체 그래프의 물리적 모습을 나름 효율적으로 인코딩한다. 객체가 포함한 데이터들과 그 객체에서부터 시작해 접근할 수 있는 모든 객체를 담아내며, 심지어 이 객체들이 연결된 위상까지 기술한다. 하지만, 이상적인 직렬화 형태라면 물리적 모습과 독립된 논리적인 모습만을 표현해야 한다.  
**객체의 물리적 표현과 논리적 내용이 같다면 기본 직렬화 형태라도 무방하다.**  

```java
// 기본 직렬화 형태에 적합한 후보
public class Name implements Serializable {
    /**
     * 성. null이 아니어야 함.
     * @serial
     */
    private final String lastName;

    /**
     * 이름. null이 아니어야 함.
     * @serial
     */
    private final String firstName;

    /**
     * 중간이름. 중간이름이 없다면 null.
     * @serial
     */
    private final String middleName;
}
```
성명은 논리적으로 이름, 성, 중간이름이라는 3개의 문자열로 구성되며, 위 코드의 인스턴스 필드들은 이 논리적 구성요소를 정확히 반영했다.

**기본 직렬화 형태가 적합하더라도 결정했더라도 불변식 보장과 보안을 위해 `readObject` 메서드를 제공해야 할 때가 많다.**

## 기본 직렬화 형태에 적합하지 않은 경우
```java
public final class StringList implements Serializable {
    private int size = 0;
    private Entry head = null;

    private static class Entry implements Serializable {
        String data;
        Entry next;
        Entry previous;
    }

    ... // 생략
}
```
논리적으로 이 클래스는 일련의 문자열을 표현한다. 물리적으로는 문자열들을 이중 연결 리스트로 연결했다. 이 클래스에 기본 직렬화 형태를 사용하면 각 노드의 양방향 연결 정보를 포함해 모든 엔트리를 철두철미하게 기록한다.

**객체의 물리적 표현과 논리적 표현의 차이가 클 때 기본 직렬화 형태를 사용하면 크게 4가지 면에서 문제가 생긴다.**
- 공개 API가 현재의 내부 표현 방식에 영구히 묶인다.
- 너무 많은 공간을 차지할 수 있다.
- 시간이 너무 많이 걸릴 수 있다.
- 스택 오버플로를 일으킬 수 있다.

합리적인 직렬화 형태는 단순히 리스트가 포함한 문자열의 개수를 적은 다음, 그 뒤로 문자열들을 나열하는 수준이면 될 것이다.

```java
public final class StringList implements Serializable {
    private transient int size = 0;
    private transient Entry head = null;

    // 이제는 직렬화 되지 않는다.
    private static class Entry {
        String data;
        Entry next;
        Entry previous;
    }

    // 지정한 문자열을 이 리스트에 추가한다.
    public final void add(String s) { ... }

    /**
     * 이 {@code StringList} 인스턴스를 직렬화한다.
     *
     * @serialData 이 리스트의 크기(포함된 문자열의 개수)를 기록한 후
     * ({@code int}), 이어서 모든 원소를(각각은 {@code String})
     * 순서대로 기록한다.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(size);

        // 모든 원소를 올바른 순서로 기록한다.
        for (Entry e = head; e != null; e = e.next)
            s.writeObject(e.data);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int numElements = s.readInt();

        // 모든 원소를 읽어 이 리스트에 삽입한다.
        for (int i = 0; i < numElements; i++) {
            add((String) s.readObject());
        }
    }

    ... // 생략
}
```
`StringList` 필드 모두가 `transient`더라도 `writeObject`, `readObject`는 각각 가장 먼저 `defaultWriteObject`, `defaultReadObject`를 호출한다. 클래스의 인스턴스 필드 모두가 `transient`면 `defaultWriteObject`, `defaultReadObject`를 호출하지 않아도 된다고 들었을지 모르지만, 직렬화 명세는 이 작업을 무조건 하라고 요구한다.  
이렇게 해야 향후 릴리스에서 `transient`가 아닌 인스턴스 필드가 추가되더라도 상호 호환되기 때문이다.

불변식이 세부 구현에 따라 달라지는 객체 또한 기본 직렬화에 적합하지 않다. 해시테이블의 경우가 대표적인 예시이다.

## 직렬화 형태에 관계 없이 적용해야 할 사항
**기본 직렬화 사용 여부와 상관없이 객체의 전체 상태를 읽는 메서드에 적용해야 하는 동기화 메커니즘을 직렬화에도 적용해야 한다.** 따라서 모든 메서드를 `synchronized`로 선언하여 스레드 안전하게 만든 객체에서 기본 직렬화를 사용하려면 `writeObject`도 다음 코드처럼 `synchronized`로 선언해야 한다.

```java
private synchronized void writeObject(ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
}
```

또한 **어떤 직렬화 형태를 택하든 직렬화 가능 클래스 모두에 직렬 버전 UID를 명시적으로 부여하자.** 이렇게 하면 직렬 버전 UID가 일으키는 잠재적인 호환성 문제가 사라진다. 성능도 조금 빨라지는데, 직렬 버전 UID를 명시하지 않으면 런타임에 이 값을 생성하느라 복잡한 연산을 수행하기 때문이다.

```java
private static final long serialVersionUID = <무작위로 고른 long 값>;
```

기본 버전 클래스와의 호환성을 끊고 싶다면 단순히 직렬 버전 UID의 값을 바꿔주면 된다. **구버전으로 직렬화된 인스턴스들과의 호환성을 끊으려는 경우를 제외하고는 직렬 버전 UID를 절대 수정하지 말자.**

## 핵심 정리
- 클래스를 직렬화하기로 했다면 어떤 직렬화 형태를 사용할지 심사숙고하기 바란다.
- 자바의 기본 직렬화 형태는 객체를 직렬화한 결과가 해당 객체의 논리적 표현에 부합할 때만 사용하고, 그렇지 않으면 객체를 적절히 설명하는 커스텀 직렬화 형태를 고안하라.
- 직렬화 형태도 공개 메서드를 설계할 때에 준하는 시간을 들여 설계해야 한다.
- 한번 공개된 메서드는 향후 릴리스에서 제거할 수 없듯이, 직렬화 형태에 포함된 필드도 마음대로 제거할 수 없다.
- 직렬화 호환성을 유지하기 위해 영원히 지원해야 하는 것이다.
- 잘못된 직렬화 형태를 선택하면 해당 클래스의 복잡성과 성능에 영구히 부정적인 영향을 남긴다.