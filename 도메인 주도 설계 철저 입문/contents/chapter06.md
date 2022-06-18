# 6장. 유스케이스를 구현하기 위한 '애플리케이션 서비스'
## 애플리케이션 서비스란 무엇인가
애플리케이션 서비스를 한마디로 표현하면 유스케이스를 구현하는 객체라고 할 수 있다.  
예를 들어, 사용자 등록을 해야 하는 시스템에서 사용자 기능을 구현하려면 '사용자 등록하기' 유스케이스와 '사용자 정보 수정하기' 유스케이스가 필요하다. 사용자 기능에 대한 애플리케이션 서비스는 유스케이스를 따라 '사용자 등록하기' 행위와 '사용자 정보 수정하기' 행위를 정의한다. 이들 행위는 도메인 객체를 실제로 조합해 실행되는 스크립트 같은 것이다.

## 도메인 객체 노출문제
유스케이스 구현 과정에서 도메인 객체를 공개하기로 한 경우의 구현 예시는 다음과 같다.

```java
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // ...

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
```

도메인 객체를 공개하기로 했다면 애플리케이션 서비스의 코드가 비교적 간단해진다. 그러나 이로 인해 약간의 위험성이 생긴다.

```java
public class Client {
    private MemberService memberService;

    //...

    public void changeName(Long memberId, String name) {
        Member member = memberService(memberId);
        member.changeName(name);
    }
}
```
문제가 되는 것은 위와 같이 의도하지 않은 도메인 객체의 메서드 호출이 가능하다는 점이다. **애플리케이션 서비스가 아닌 객체가 도메인 객체의 직접적인 클라이언트가 되어 도메인 객체를 자유롭게 조작하고 있다는 점**이 가장 큰 문제이다. 도메인 객체의 행동을 호출하는 것은 애플리케이션 서비스의 책임이다. 이 구조가 지켜진다면 도메인 객체의 행동을 호출하는 코드가 모두 애플리케이션 서비스 안에 모여 있지만, 그렇지 않다면 여러 곳에 코드가 흩어질 수 있다.

또한 **도메인 객체에 대한 의존이 많이 발생하는 것도 문제다.** 도메인의 변화가 즉시 객체에 반영돼야 하는데, 복잡한 의존 관계의 핵심이 되는 코드를 수정하는 것은 숙련된 객발자에게도 조심스러운 작업이다.

이를 방지하기 위한 수단으로 접근제어 수정자를 이용해 메서드의 호출을 제한하는 방법이 있지만, 클라이언트와 애플리케이션 서비스, 도메인 객체가 모두 같은 패키지에 있다면 이 방법도 사용하기 어렵다.

**도메인 객체를 비공개로 남겨두고 클라이언트에 데이터 전송을 위한 객체(DTO, data transfer object)를 만들어 여기에 데이터를 옮겨 넣어 반환하는 방법을 사용하자.**

DTO를 적용하면 DTO를 정의하는 데 필요한 수고와 데이터를 옮겨 담는 데서 오는 약간의 성능 저하가 따르지만, 어지간히 많은 양의 데이터를 옮겨 넣는 것이 아닌 이상 성능 저하는 미미한 수준이다. **오히려 불필요한 의존을 줄의고 도메인 객체의 변경을 방해받지 않는 편익이 더 크다.**

## 도메인 규칙의 유출
애플리케이션 서비스는 도메인 객체가 수행하는 태스크를 조율하는 데만 전념해야 한다. 따라서 **애플리케이션 서비스에 도메인 규칙을 기술해서는 안 된다.** 도메인 규칙이 애플리케이션 서비스에 기술되면 같은 코드가 여러 곳에서 중복되는 현상이 나타난다.

예를 들어, 사용자명의 중복을 금지하는 규칙은 도메인에 있어 중요도가 높은 규칙이다. 이 도메인 규칙이 애플리케이션 서비스에 기술된 상황에서 해당 규칙이 변경된다면 수정 내용 자체는 단순하지만, 수정해야 할 곳이 많아진다면 버그가 발생할 가능성이 굉장히 높다.

이 문제를 해결하는 방법은 간단하다. **도메인 규칙은 도메인 객체에 구현하고 애플리케이션 서비스는 이 도메인 객체를 사용하는 역할만 맡는다.** 도메인 객체에 규칙을 구현하면 같은 규칙을 구현한 코드가 여러 곳에 반복되는 것을 방지하고 향후 수정 시에도 수정이 필요한 곳을 빠뜨려 발생하는 버그를 막을 수 있다.

## 애플리케이션 서비스와 프로그램의 응집도
### 응집도
- 응집도는 모듈의 책임 범위가 얼마나 집중되어 있는지 나타내는 척도다.
- 응집도가 높으면 모듈이 하나의 관심사에 집중하고 있다는 의미이므로 모듈의 견고성, 신뢰성, 재사용성, 가독성의 측면에서 바람직하다.
- 응집도를 측정하는 방법에는 LCOM(Lack of Cohesion in Methods)라는 방식이 있다.

```java
// 응집도가 낮은 클래스의 예
public class LowCohesion {
    private int value1;
    private int value2;
    private int value3;
    private int value4;

    public int methodA() {
        return value1 + value2;
    }

    public int methodB() {
        return value3 + value4;
    }
}
```
`LowCohesion` 클래스의 `value1`은 `methodA` 메서드에는 사용됐지만, `methodB`에는 사용되지 않았다. 이를 볼 때, `value1`과 `methodB`는 본질적으로 관계가 없다고 볼 수 있다. 다른 속성에도 이런 논리를 적용할 수 있다. 이들을 분리하면 응집도가 더 높아진다.

![](https://velog.velcdn.com/images/songs4805/post/2c8cf3cc-0554-490a-8cca-d8c08c042b5d/image.jpg)

```java
// 서로 관계가 없는 속성과 메서드를 분리하면 응집도가 높아진다.
public class HighCohesionA {
    private int value1;
    private int value2;

    public int methodA() {
        return value1 + value2;
    }
}

public class HighCohesionB {
    private int value3;
    private int value4;

    public int methodB() {
        return value3 + value4;
    }
}
```
두 클래스 모두 각 클래스의 모든 속성이 해당 클래스의 모든 메서드에 사용되어 응집도가 높은 상태가 됐다.

![](https://velog.velcdn.com/images/songs4805/post/0ea19f6a-2875-4ca5-b0f4-6254e9d0bd11/image.jpg)

코드가 처한 맥락과 환경에 따라 오히려 응집도가 낮아지는 선택이 정답일 수도 있다. 그러나 응집도는 클래스를 설계할 때 한 번쯤 고려해볼 만한 가치가 있는 척도임은 틀림없다.

### 응집도가 낮은 애플리케이션 서비스
```java
@Service
@RequiredArgsConstructor
public class MemberApplicationSerivce {
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    //...
    public void register(RegisterRequestDto dto) {
        String username = dto.getUsername();

        if (memberService.exists(username)) {
            throw new MemberDuplicateException();
        }
        Member member = dto.toEntity();
        memberRepository.save(member);
    }

    public void delete(Long memberId) {
        Member member = memberRepository.findById(memberId);
        if (member == null) {
            throw new MemberNotFoundException();
        }

        memberRepository.deleteById(memberId);
    }
}
```

위 코드는 응집도의 관점에서 볼 때 그리 바람직한 상태가 아니다. 응집도를 높이려면 간단히 클래스를 분리하면 된다. 책임을 분명하게 나눈다면 클래스를 분리하는 것이 당연하다고 할 수 있다.  
하지만, 사용자와 관련된 처리에 어떤 것이 있는지 전체적인 파악도 가능해야 한다. **이런 경우 한꺼번에 관련된 코드를 모아두기 위한 기능이 패키지다.**

패키지를 이용해 클래스를 모아두면 사용자와 관련된 처리가 한 패키지, 같은 디렉터리 안에 모이게 된다. 이렇게 구성해두면 개발자가 사용자와 관련된 처리를 찾기도 쉬울 것이다.

응집도가 절대적인 지표가 되는 것은 아니기에 코드를 정돈하기 위한 참고사항으로 기억해두자.

## 애플리케이션 서비스의 인터페이스
더 유연한 코드를 위해 애플리케이션 서비스의 인터페이스를 만드는 경우가 있다. 애플리케이션 서비스를 호출하는 클라이언트는 애플리케이션 서비스의 구현체를 직접 호출하는 것이 아니라 인터페이스를 통해 호출한다.

**애플리케이션 서비스의 인터페이스를 정의하면 클라이언트 측의 편의성이 높아진다.** 서비스의 구현과 독립적으로 클라이언트 측의 개발을 할 수 있기 때문이다.

// Spring의 PSA와 비슷한 맥락이 아닐까 한다.

애플리케이션 서비스의 인터페이스를 미리 만들어 두면 이를 구현한 목업 객체를 이용해 애플리케이션 서비스의 실제 구현이 완료되기를 기다릴 필요 없이 클라이언트의 구현을 진행할 수 있다.

목업 객체는 이 외에 테스트시에도 유용하다.