# 아이템 12. toString을 항상 재정의하라

Object의 기본 toString 메서드는 우리가 작성한 클래스에 적합한 문자열을 반환하는 경우는 거의 없다. 이 메서드는 단순히 `클래스_이름@16진수로_표시한_해시코드`를 반환할 뿐이다.

toString의 일반 규약인 **간결하면서 사람이 읽기 쉬운 형태의 유익한 정보**를 반환해야 한다.

toString의 규약은 "모든 하위 클래스에서 이 메서드를 재정의하라"고 한다.

**toString을 잘 구현한 클래스는 사용하기 훨씬 즐겁고, 그 클래스를 사용한 시스템은 디버깅하기 쉽다.** 내가 직접 호출하지 않더라도 여러 경우에서 시스템 자체적으로 어디선가 호출한다.  
좋은 toString은 (특히 컬렉션처럼) 이 인스턴스를 포함하는 객체에서 유용하게 쓰인다.

**실전에서 toString은 그 객체가 가진 주요 정보 모두를 반환하는 게 좋다.** 하지만 객체가 거대하거나 객체의 상태가 문자열로 표현하기 적합하지 않다면 무리가 있다. 이런 상황이라면 요약 정보를 담아야 한다.

## toString() 포맷 문서화 여부

toString을 구현할 때면 반환값의 포맷을 문서화할지 정해야 한다. 전화번호나 행렬같은 값 클래스는 문서화하는 것을 권장한다.

포맷을 명시하기로 했다면, 포맷에 맞는 문자열과 객체를 상호 전환할 수 있는 정적 팩터리나 생성자를 함께 제공해주면 좋다. 자바 플랫폼의 많은 값 클래스가 따르는 방식이기도 하다. (BigInteger, BigDecimal과 대부분의 기본 타입 클래스가 이에 해당)

다만, 포맷을 지정하면 해당 포맷에 얽매이게 되는 단점이 있다.

**포맷을 명시하든 아니든 의도는 명확히 밝혀야 한다.** 포맷을 명시하려면 아주 명확하게 해야한다.

포맷을 명시하기로 했을 경우의 예시이다.

```java
/**
 * 이 전화번호의 문자열 표현을 반환한다.
 * 이 문자열은 "XXX-YYY-ZZZZ" 형태의 12글자로 구성된다.
 * XXX는 지역 코드, YYY는 프리픽스, ZZZZ는 가입자 번호다.
 * 각각의 대문자는 10진수 숫자 하나를 나타낸다.
 *
 * 전화번호의 각 부분의 값이 너무 작아서 자릿수를 채울 수 없다면,
 * 앞에서부터 0으로 채워나간다. 예컨대 가입자 번호가 123이라면
 * 전화번호의 마지막 네 문자는 "0123"이 된다.
 */
@Override
public String toString() {
    return String.format("%03d-%03d-%04d", areaCode, prefix, lineNum);
}
```

포맷을 명시하지 않기로 했다면 다음처럼 작성할 수 있다.

```java
/**
 * 이 약물에 관한 대략적인 설명을 반환한다.
 * 다음은 이 설명의 일반적인 형태이나,
 * 상세 형식은 정해지지 않았으며 향후 변경될 수 있다.
 *
 * "[약물 #9: 유형=사랑, 냄새=테레빈유, 겉모습=약물]"
 */
@Override
public String toString() { ... }
```

포맷 명시 여부와 상관없이 **toString이 반환한 값에 포함된 정보를 얻어올 수 있는 API를 제공하자.**  
그렇지 않으면 이 정보가 필요한 프로그래머는 toString의 반환값을 파싱할 수 밖에 없다. 이는 성능이 나빠지고, 필요하지도 않은 작업이다. 게다가 향후 포맷을 바꾸면 시스템이 망가지는 결과를 초래할 수 있다.

## toString() 재정의가 필요 없는 경우

- 정적 유틸리티 클래스
- 열거 타입(이미 자바에서 충분한 toString을 제공함)

## 핵심 정리

- 모든 구체 클래스에서 Object의 toString을 재정의하자.
- 상위 클래스에서 이미 알맞게 재정의한 경우는 예외다.
- toString을 재정의한 클래스는 사용하기도 즐겁고 그 클래스를 사용한 시스템을 디버깅하기 쉽게 해준다.
- toString은 해당 객체에 관한 명확하고 유용한 정보를 읽기 좋은 형태로 반환해야 한다.