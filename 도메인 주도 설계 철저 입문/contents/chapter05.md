# 5장. 데이터와 관계된 처리를 분리하자 - 리포지토리
## 리포지토리
소프트웨어 개발에서 말하는 리포지토리는 데이터 보관창고를 의미한다.

**엔티티는 생애주기를 갖는 객체이기 때문에 프로그램의 종료와 함께 객체가 사라져서는 안 된다.** 객체를 다시 이용하려면 데이터스토어에 객체 데이터를 저장 및 복원할 수 있어야 한다. 리포지토리는 데이터를 저장하고 복원하는 처리를 추상화하는 객체다. 리포지터리를 거쳐 간접적으로 데이터를 저장 및 복원하는 방식을 취하면 소프트웨어의 유연성이 상당히 향상된다.

정리하자면, 
- **리포지토리의 책임은 도메인 객체를 저장하고 복원하는 퍼시스턴시다.**
- **리포지토리에 정의되는 행위는 객체의 저장 및 복원에 대한 것이다.**

비즈니스 로직이 데이터스토어를 조작하는 코드로 가득 차 있다면 코드의 의도를 이해하기 어렵다. 데이터스토어를 직접 다루는 퍼시스턴시 관련 처리를 추상적으로 뽑아내면 이 코드의 의도를 좀 더 잘 드러낼 수 있다.

사실 데이터스토어가 관계형 데이터베이스든 NoSQL 데이터베이스든, 그냥 파일이라도 도메인 입장에선 중요한 문제가 아니다. **중요한 것은 인스턴스를 어떤 수단을 통해 저장하냐는 것이다.** 데이터스토어에 대한 명령을 추상화함으로써 데이터스토어를 직접 다루는 까다로운 코드에서 해방되고 순수한 로직만 남은 코드가 된다면 의도하는 바가 뚜렷해진다.  
Service 단의 코드가 객체의 퍼시스턴시와 관련된 처리를 리포지토리에 맡기면 비즈니스 로직을 더욱 순수하게 유지할 수 있는 것이다.

## 리포지토리의 인터페이스
- 리포지토리는 인터페이스로 정의된다.
- 리포지토리의 책임은 퍼시스턴스까지다. 도메인 규칙과 관련된 항목은 서비스에서 다루는 곳이 옳다.
- 인터페이스에서 나중에 사용할 메서드를 미리 만들지 말고 현시점에서 필요한 최소한의 정의만 작성하자.

## 리포지토리 구현
비즈니스 로직에서 특정한 기술에 의존하는 구현은 바람직하지 않지만, 리포지토리의 구현 클래스라면 특정 기술에 의존하는 구현도 문제가 없다.

혹시 테스트를 한다면, 테스트를 위해 데이터베이스를 설치하고 테이블을 만드는 등 준비 작업을 복잡하게 하는 것 대신 데이터베이스를 사용하지 않는 방향으로 해결하자. 예를 들면, `ConcurrentHashMap`과 같은 자료구조를 통해 리포지토리를 대신하는 것이다. 이 경우 데이터베이스에 접속할 필요가 없으니 테스트가 놀랄 만큼 간단해질 것이다.

## 리포지토리에 정의되는 행동
- 객체를 저장하려면 저장 대상 객체를 인자로 전달받아야 한다.
- 객체가 저장하고 있는 데이터를 수정하려면 애초부터 객체 자신에게 맡기는 것이 옳다.

```java
// 다음과 같은 코드는 피해야 함
public Interface UserRepository {
    void updateName(UserId id, UserName name);
    void updateEmail(UserId id, Email email);
    void updateAddress(UserId id, Address address);
}
```

- 객체를 생성하는 처리도 리포지토리에 정의해서는 안 된다.
- 객체를 파기하는 메서드는 리포지토리에 정의한다.
- 객체를 복원/조회하는 메서드는 보통 식별자를 사용한다.
- 객체의 존재 여부를 확인해야 한다면 인자에 검색키를 추가하여 전체 객체를 조회하지 않도록 구현하자.

## 정리
로직이 특정한 인프라스트럭처 기술에 의존하면 소프트웨어가 경직되는 현상이 일어난다. 코드의 대부분이 데이터스토어를 직접 다루는 내용으로 오염되며 코드의 의도가 잘 드러나지 않는다.

리포지토리를 이용하면 데이터 퍼시스턴시와 관련된 처리를 추상화할 수 있다. 이정도의 변화만으로도 소프트웨어의 유연성을 놀랄 만큼 향상시킬 수 있다.

개발 초기에 어떤 데이터스토어를 채용할지 결정하기 전이라도 인메모리 리포지토리 등을 이용해 먼저 로직 구현 작업역시 할 수 있다. 다른 데이터스토어로 변경하고 싶다면 이 또한 구현해 데이터스토어를 교체할 수 있고 테스트 역시 실시할 수 있다.

도메인 규칙과 비교한다면 어떤 데이터스토어를 사용할지는 사소한 문제에 지나지 않는다. 리포지토리를 잘 활용해 코드의 의도를 명확히 하자.