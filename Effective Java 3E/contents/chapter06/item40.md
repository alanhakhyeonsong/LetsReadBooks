# 아이템 40. @Override 애너테이션을 일관되게 사용하라
Java가 기본으로 제공하는 애너테이션 중 보통의 프로그래머에게 가장 중요한 것은 `@Override`일 것이다. `@Override`는 메서드 선언에만 달 수 있으며, 이 애너테이션이 달렸다는 것은 상위 타입의 메서드를 재정의했음을 뜻한다. 이 애너테이션을 일관되게 사용하면 여러가지 악명 높은 버그들을 예방해준다.

```java
public class Bigram {
    private final char first;
    private final char second;

    public Bigram(char first, char second) {
        this.first = first;
        this.second = second;
    }

    public boolean equals(Bigram b) {
        return b.first == first && b.second == second;
    }

    public int hashCode() {
        return 31 * first + second;
    }

    public static void main(String[] args) {
        Set<Bigram> s = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            for (char ch = 'a'; ch <= 'z'; ch++) {
                s.add(new Bigram(ch, ch));
            }
            System.out.println(s.size());
        }
    }
}
```

위 예제 코드에서 main 메서드를 보면 똑같은 소문자 2개로 구성된 바이그램 26개를 10번 반복해 집합에 추가한 다음, 그 집합의 크기를 출력한다. Set은 중복을 허용하지 않으니 26이 출력될 거 같지만, 실제로는 260이 출력된다.

이는 `equals`를 재정의(overriding)한 게 아니라 다중정의(overloading) 해버렸기 때문이다.  
Object의 equals를 재정의하려면 매개변수 타입을 Object로 해야만 하는데, 그렇게 하지 않은 것이다. 그래서 Object에서 상속한 equals와 별개인 equals를 새로 정의한 꼴이 되었다.

다행히 이 오류는 컴파일러가 찾아낼 수 있지만, 그러려면 `Object.equals`를 재정의한다는 의도를 명시해야 한다.  
`@Override`를 달고 다시 컴파일하면 컴파일 오류가 발생한다.

이를 올바르게 수정하면 다음과 같다.
```java
@Override
public boolean equals(Object o) {
    if (!(o instanceof Bigram)) {
        return false;
    }
    Bigram b = (Bigram) o;
    return b.first == first && b.second == second;
}
```

그러니 **상위 클래스의 메서드를 재정의하려는 모든 메서드에 `@Override` 애너테이션을 달자.**

## 핵심 정리
- 재정의한 모든 메서드에 `@Override` 애너테이션을 의식적으로 달면 우리가 실수했을 때 컴파일러가 바로 알려줄 것이다.
- 예외는 한 가지 뿐이다. 구체 클래스에서 상위 클래스의 추상 메서드를 재정의한 경우엔 이 애너테이션을 달지 않아도 된다.(단다고 해서 해로울 것도 없다)