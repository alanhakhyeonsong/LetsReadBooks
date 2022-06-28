# 아이템 89. 인스턴스 수를 통제해야 한다면 readResolve보다는 열거 타입을 사용하라
## readResolve를 사용한 싱글톤
serializable instance-controlled class를 작성해야 한다면, 컴파일 타임 중에 직렬화된 인스턴스가 타입인지 알아낼 방도가 없으므로 `readResolve`를 해야한다.

`readResolve`를 사용하면 `readObject`가 만든 새로운 인스턴스를 기존의 인스턴스로 대체해서 바로 가비지 컬렉션이 되게 할 수 있다. (객체 생성 후 가비지 컬렉션 작업이 이루어져야 한다는데서 이미 엄청난 오버헤드가 발생할 것 같다.)

주의점은, `readResolve`로 싱글톤을 흉내낼때는 object reference type의 모든 instance field는 **`transient`로 선언**해야 한다. 그렇지 않으면 `readReslove` 메서드가 수행되기 전에 "reference to the deserialized object"를 해서 가비지 컬렉션을 막아버리고, 새로 만들어진 객체가 유지되도록 할 수 있기 때문이다.

또한 `readResolve` 메서드의 접근자를 무엇으로 해야할 지 심각한 고민을 해야한다고 한다.
- private: final class; 즉 하위 클래스에서 사용할 수 없게 하고 싶은 경우
- package-private: 같은 패키지의 하위 클래스에서 사용하게 하고 싶은 경우
- protected, public: 모든 하위 클래스에서 사용하게 하고 싶은 경우

만약 하위 클래스에서 `readResolve`를 해당 하위 클래스 인스턴스를 반환하도록 override 하지 않는다면, deserialize 시에 상위 클래스 인스턴스가 반환되기 때문에 `ClassCastException`을 일으킬 수 있는 위험이 있다. 즉 상속받은 메서드임에도 매번 override를 해줘야 할 수도 있다.

## Enum을 사용한 싱글톤
직렬화된 클래스에 싱글톤을 적용하려는 뻘짓을 하지 말고 이것만 기억하자.
```java
public enum Elvis {
    INSTANCE;

    private String[] favoriteSongs = {"Hound_Dog", "Heartbreak_Hotel"};
    public void printFavorites() {
        System.out.println(Arrays.toString(favoriteSongs));
    }
}
```

## 핵심 정리
- 불변식을 지키기 위해 인스턴스를 통제해야 한다면 가능한 한 열거 타입을 사용하자.
- 여의치 않은 상황에서 직렬화와 인스턴스 통제가 모두 필요하다면 `readResolve` 메서드를 작성해 넣어야 하고, 그 클래스에서 모든 참조 타입 인스턴스 필드를 `transient`로 선언해야 한다.