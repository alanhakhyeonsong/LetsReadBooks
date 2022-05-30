# 아이템 52. 다중정의는 신중히 사용하라
## 다중정의
```java
// 컬렉션 분류기 - 오류! 이 프로그램은 무엇을 출력할까?
public class CollectionClassifier {
    public static String classify(Set<?> s) {
        return "집합";
    }

    public static String classify(List<?> list) {
        return "리스트";
    }

    public static String classify(Collection<?> c) {
        return "그 외";
    }

    public static void main(String[] args) {
        Collection<?>[] collections = {
            new HashSet<String>(),
            new ArrayList<BigInteger>(),
            new HashMap<String, String>().values()
        };

        for (Collection<?> c : collections) {
            System.out.println(classify(c));
        }
    }
}

// 예상: "집합", "리스트", "그 외"
// 실제: "그 외", "그 외", "그 외"
```
위 예제 코드는 예상과 달리 실제 결과 값이 다르게 출력된다. 이는 다중정의(overloading)된 세 `classify` 중 **어느 메서드를 호출할지가 컴파일타임에 정해지기 때문이다.** 컴파일타임에는 for 문 안의 c는 항상 `Collection<?>` 타입이다. 런타임에는 타입이 매번 달라지지만, 호출할 메서드를 선택하는 데는 영향을 주지 못한다. 따라서 컴파일타임의 매개변수 타입을 기준으로 항상 세 번째 메서드만 호출하는 것이다.

## 재정의
이처럼 **직관과 어긋나는 이유는 재정의한 메서드는 동적으로 선택되고, 다중정의한 메서드는 정적으로 선택되기 때문이다.** 메서드를 재정의했다면 해당 객체의 런타임 타입이 어떤 메서드를 호출할지의 기준이 된다.

```java
// 재정의된 메서드 호출 메커니즘 - 이 프로그램은 무엇을 출력할까?
class Wine {
    String name() { return "포도주"; }
}

class SparklingWine extends Wine {
    @Override String name() { return "발포성 포도주"; }
}

class Champagne extends SparklingWine {
    @Override String name() { return "샴페인"; }
}

public class Overriding {
    public static void main(String[] args) {
        List<Wine> wineList = List.of(new Wine(), new SparklingWine(), new Champagne());

        for (Wine wine : wineList) {
            System.out.println(wine.name());
        }
    }
}

// 예상: "포도주", "발포성 포도주", "샴페인"
// 실제: "포도주", "발포성 포도주", "샴페인"
```
for 문에서의 컴파일타임 타입의 모두 `Wine`인 것에 무관하게 항상 '가장 하위에서 정의한' 재정의 메서드가 실행되는 것이다.

## 다중정의가 혼동을 일으키는 상황을 피하자
- 안전하고 보수적으로 가려면 매개변수 수가 같은 다중정의는 만들지 말자.
- 다중정의하는 대신 메서드 이름을 다르게 지어주는 길도 항상 열려 있다.
- 생성자는 두 번째 생성자부터는 무조건 다중정의가 된다. 정적 팩터리라는 대안을 활용할 수 있는 경우가 많으니 적절히 사용하자.

헷갈릴 수 있는 코드는 작성하지 않는 게 좋다. 특히나 공개된 API라면 더욱 신경 써야 한다. API 사용자가 매개변수를 넘기면서 어떤 다중정의 메서드가 호출될지를 모른다면 프로그램이 오동작하기 쉽다.

## ArrayList - remove()
```java
public class SetList {
    public static void main(String[] args) {
        Set<Integer> set = new TreeSet<>();
        List<Integer> list = new ArrayList<>();

        for (int i = -3; i < 3; i++) {
            set.add(i);
            list.add(i);
        }

        for (int i = 0; i < 3; i++) {
            set.remove(i);
            list.remove(i); // 인덱스를 지움
        }
        System.out.println(set + " " + list);
    }
}
// 예상: [-3, -2, -1], [-3, -2, -1]
// 실제: [-3, -2, -1], [-2, 0 ,2]
```
이는 `set.remove(i)`의 시그니처는 `remove(Object)`다. 다중정의된 다른 메서드가 없기 때문에 기대한 대로 동작하여 집합에서 0 이상의 수들을 제거한다.  
한편, `list.remove(i)`는 다중정의된 `remove(int indext)`를 선택한다. 이는 '지정한 위치'의 원소를 제거하는 기능을 수행하게 된다.

이 문제는 `list.remove`의 인수를 `Integer`로 형변환하여 올바른 다중정의 메서드를 선택하게 하면 해결한다. 또는 `Integer.valueOf`를 이용해 i를 Integer로 변환한 뒤 `list.remove`에 전달해도 된다.

```java
for (int i = 0; i < 3; i++) {
    set.remove(i);
    list.remove((Integer) i); // 값을 지움
    // list.remove(Integer.valueOf(i));
}
```

## 핵심 정리
- 프로그래밍 언어가 다중정의를 허용한다고 해서 다중정의를 꼭 활용하란 뜻은 아니다.
- 일반적으로 매개변수 수가 같을 때는 다중정의를 피하는 게 좋다.
- 상황에 따라, 특히 생성자라면 이 조언을 따르기가 불가능할 수 있다. 그럴 때는 헷갈릴 만한 매개변수는 형변환하여 정확한 다중정의 메서드가 선택되도록 해야 한다.
- 이것이 불가능하면, 예컨데 기존 클래스를 수정해 새로운 인터페이스를 구현해야 할 때는 같은 객체를 입력받는 다중정의 메서드들이 모두 동일하게 동작하도록 만들어야 한다.
- 그렇지 못하면 프로그래머들은 다중정의된 메서드나 생성자를 효과적으로 사용하지 못할 것이고, 의도대로 동작하지 않는 이유를 이해하지도 못할 것이다.