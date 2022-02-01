# 아이템 11. equals를 재정의하려거든 hashCode도 재정의하라

**equals를 재정의한 클래스 모두에서 hashCode도 재정의해야 한다.** 그렇지 않으면 hashCode 일반 규약을 어기게 되어 해당 클래스의 인스턴스를 HashMap이나 HashSet 같은 컬렉션의 원소로 사용할 때 문제를 일으킬 것이다.

> 📌 hashCode 재정의 조건
>
> - equals 비교에 사용되는 정보가 변경되지 않는다면, hashCode는 변하면 안된다.
> - equals가 두 객체가 같다고 판단했다면, 두 객체의 hashCode는 똑같은 값을 반환해야 한다.
> - equals가 두 객체를 다르다고 판단했더라도, 두 객체의 hashCode가 꼭 다를 필요는 없다. 하지만 성능을 챙기려면 hashCode값이 달라야 한다.

## 문제 발생 사례

equals(Object) 메서드는 물리적으로는 다른 객체지만, 논리적 동치성은 성립할 경우 같다고 재정의 할 수있다.  
그런데 이 경우, hashCode가 재정의되지 않는다면, Object의 기본 hashCode가 수행되는데 해당 메서드에서는 논리적으로 같다고 해도 물리적으로 다르다고 판단되면 서로 다른 값을 반환한다.

```java
public class PhoneNumber {
    private String prefix;
    private String middle;
    private String suffix;

    ...

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber)) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(prefix, that.prefix)
                && Objects.equals(middle, that.middle)
                && Objects.equals(suffix, that.suffix);
    }

    public class PhoneNumberApplication {
        public static void main(String[] args) {
            PhoneNumber phoneNumber1 = new PhoneNumber("010", "1234", "5678");
            PhoneNumber phoneNumber2 = new PhoneNumber("010", "1234", "5678");

            System.out.println("phoneNumber1.equals(phoneNumber2) = " + phoneNumber1.equals(phoneNumber2));

            Map<PhoneNumber, String> map = new HashMap<>();
            map.put(phoneNumber1, "ramos");

            System.out.println(map.get(phoneNumber1)); // ramos
            System.out.println(map.get(phoneNumber2)); // null
        }
    }
}
```

equals는 재정의했기 때문에 equals(Object)의 결과는 true이다. 논리적으로 볼 때, phoneNumber1과 phoneNumber2는 동일하다는 의미이다.

하지만, HashMap에서 key값으로 phoneNumber1이 아닌 phoneNumber2를 사용할 경우 null을 반환한다. 그 이유는 PhoneNumber 클래스는 hashCode 메서드를 재정의하지 않았기 때문이다. 따라서 **논리적으로 동일하더라도 해시코드는 서로 다르게 반환되기에 두 번째 규약을 지키지 못한다.**

## 해결책

문제는 없지만 다음 방법은 사용해서는 안된다.

```java
@Override
public int hashCode() { return 42; }
```

hashCode를 위와 같이 재정의한다면, 같은 객체는 모두 같은 해시코드를 반환한다. 하지만, 모든 객체가 같은 해시코드를 반환하기에 모든 객체가 해시테이블 버킷 하나에 담기기 때문에 마치 연결리스트처럼 동작하게 된다.

평균 수행 시간이 O(1)인 해시테이블이 O(n)으로 느려져서, 객체가 많아지면 도저히 쓸 수 없게 된다.

좋은 hashCode 작성법은 다음과 같다.

이상적인 해시 함수는 주어진 인스턴스들을 32bit 정수 범위에 균일하게 분배해야 한다.

- 지역변수 선언 후 핵심필드 값 하나의 해시코드로 초기화
  - 기본타입 필드라면 `Type.hashCode(f)`를 수행 (Type: Wrapper Class)
  - 참조타입 필드라면 이 필드의 hashCode를 재귀적으로 호출한다. 계산이 더 복잡해질 것 같으면, 이 필드의 표준형(canonical representation)을 만들어 표준형의 hashCode를 호출한다. 필드의 값이 null이면 0을 사용한다.
  - 배열이라면 핵심 원소 각각을 별도 필드처럼 다룬다. 만약 모든 원소가 핵심 원소라면 `Arrays.hashCode`를 사용한다.
- 다른 핵심필드들도 동일하게 해시코드화하여 지역변수에 합친다.  
  `지역변수 = 31 * 지역변수 + 핵심필드의 해시코드`
- 지역변수의 값을 반환한다.
  ```java
  @Override
  public int hashCode() {
      int result = prefix.hashCode();
      result = 31 * result + middle.hashCode();
      result = 31 * result + suffix.hashCode();
      return result;
  }
  ```

참고로, `지역변수 = 31 * 지역변수 + 핵심필드의 해시코드`에서 곱할 숫자가 31인 이유는 31이 홀수이면서 소수(prime)이기 때문이다. 만약 이 숫자가 짝수이고 오버플로가 발생한다면 정보를 잃게 된다. 2를 곱하는건 시프트 연산과 같기 때문이다. 소수를 곱하는 이유는 전통적으로 그리 해왔다고 한다. 결과적으로 31을 이용하면, 이 곱셈을 시프트 연산과 뺄셈으로 대체해 최적화할 수 있다. 요즘 VM들은 이런 최적화를 자동으로 해준다.

## Objects 클래스의 hash 메서드 사용

Objects 클래스는 임의의 갯수만큼 객체를 받아 해시코드를 계산해주는 정적 메서드인 hash를 제공한다.

장점은 위와 비슷한 수준의 hashCode를 단 한 줄로 작성가능하다.  
단점은 속도가 더 느리다. 입력 인수를 담기 위한 배열을 만들고 입력 중 기본 타입이 있다면 박싱/언박싱도 거쳐야 하기 때문이다.

```java
@Override
public int hashCode() {
    return Objects.hash(prefix, middle, suffix);
}
```

## 고려사항

클래스가 불변이고 해시코드를 계산하는 비용이 크다면, 매번 새로 계산하기보다는 캐싱하는 방식을 고려해야 한다. 객체가 주로 해시의 키로 사용될 것 같다면 인스턴스가 만들어질 때 해시코드를 계산해둬야 한다. 그렇지 않은 경우 hashCode를 미리 계산해놓고 캐싱까지 해두는 것은 비용낭비이다.

이럴 경우, 지연 초기화(lazy initialization) 전략을 고려해볼만하다.

```java
private int hashCode; // 자동으로 0으로 초기화

@Override
public int hashCode() {
    int result = hashCode;
    if (result == 0) {
        result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        hashCode = result;
    }
    return result;
}
```

**성능을 높인답시고 해시코드를 계산할 때 핵심 필드를 생략해서는 안 된다.** 속도가 빨라지지만, 해시 품질이 나빠져 해시테이블의 성능을 심각하게 떨어뜨릴 수도 있다.  
특히 어떤 필드는 특정 영역에 몰린 인스턴스들의 해시코드를 넓은 범위로 고르게 퍼트려주는 효과가 있을지도 모른다. 하필 이런 필드를 생략한다면 해당 영역의 인스턴스가 단 몇 개의 해시코드로 집중되어 해시테이블의 속도가 선형으로 느려질 것이다.

**hashCode가 반환하는 값의 생성 규칙을 API 사용자에게 자세히 공표하지 말자. 그래야 클라이언트가 이 값에 의지하지 않게 되고, 추후에 계산 방식을 바꿀 수도 있다.**

## 핵심 정리

- equals를 재정의할 때는 hashCode도 반드시 재정의해야 한다. 그렇지 않으면 프로그램이 제대로 동작하지 않을 것이다.
- 재정의한 hashCode는 Object의 API 문서에 기술된 일반 규약을 따라야 하며, 서로 다른 인스턴스라면 되도록 해시코드도 서로 다르게 구현해야 한다.
- 이런 구현 방식이 귀찮다면, AutoValue 프레임워크를 사용하면 멋진 equals와 hashCode를 자동으로 만들어준다. IDE들도 이런 기능을 일부 제공한다.
