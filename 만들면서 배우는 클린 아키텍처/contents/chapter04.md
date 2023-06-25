# 4장. 유스케이스 구현하기
## 도메인 모델 구현하기
한 계좌에서 다른 계좌로 송금하는 유스케이스를 구현해보자.

```java
package buckpal.domain;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

  @Getter
  private final AccountId id;

  @Getter
  private final Money baselineBalance;

  @Getter
  private final ActivityWindow activityWindow;

  public static Account withoutId(
  				Money baselineBalance,
  				ActivityWindow activityWindow) {
  	return new Account(null, baselineBalance, activityWindow);
  }

  public static Account withId(
  				AccountId accountId,
  				Money baselineBalance,
  				ActivityWindow activityWindow) {
  	return new Account(accountId, baselineBalance, activityWindow);
  }

  public Optional<AccountId> getId(){
  	return Optional.ofNullable(this.id);
  }

  public Money calculateBalance() {
  	return Money.add(
  			this.baselineBalance,
  			this.activityWindow.calculateBalance(this.id));
  }

  public boolean withdraw(Money money, AccountId targetAccountId) {

  	if (!mayWithdraw(money)) {
  		return false;
  	}

  	Activity withdrawal = new Activity(
  			this.id,
  			this.id,
  			targetAccountId,
  			LocalDateTime.now(),
  			money);
  	this.activityWindow.addActivity(withdrawal);
  	return true;
  }

  private boolean mayWithdraw(Money money) {
  	return Money.add(
  			this.calculateBalance(),
  			money.negate())
  			.isPositiveOrZero();
  }

  public boolean deposit(Money money, AccountId sourceAccountId) {
  	Activity deposit = new Activity(
  			this.id,
  			sourceAccountId,
  			this.id,
  			LocalDateTime.now(),
  			money);
  	this.activityWindow.addActivity(deposit);
  	return true;
  }

  @Value
  public static class AccountId {
  	private Long value;
  }
}
```

`Account` 엔티티는 실제 계좌의 현재 스냅숏을 제공한다. 계좌에 대한 모든 입금과 출금은 `Activity` 엔티티에 포착된다. 모든 입/출금 내역을 항상 메모리에 한꺼번에 올리는 것은 현명한 방법이 아니기 때문에 `Account` 엔티티는 `ActivityWindow` 값 객체에서 포착한 지난 며칠 혹은 몇 주간의 범위에 해당하는 활동만 보유한다.

## 유스케이스 둘러보기
일반적으로 유스케이스는 다음과 같은 단계를 따른다.

1. 입력을 받는다.
2. 비즈니스 규칙을 검증한다.
3. 모델 상태를 조작한다.
4. 출력을 반환한다.

유스케이스는 인커밍 어댑터로부터 입력을 받는다. 저자는 유스케이스 코드가 도메인 로직에만 신경 써야 하고 입력 유효성 검증으로 오염되면 안 된다고 생각한다. 유스케이스는 비즈니스 규칙을 검증할 책임이 있다. 그리고 도메인 엔티티와 이 책임을 공유한다.

비즈니스 규칙을 충족하면 유스케이스는 입력을 기반으로 어떤 방법으로든 모델의 상태를 변경한다. 일반적으로 도메인 객체의 상태를 바꾸고 영속성 어댑터를 통해 구현된 포트로 이 상태를 전달해서 저장될 수 있게 한다. 유스케이스는 또 다른 아웃고잉 어댑터를 호출할 수도 있다. 마지막 단계는 아웃고잉 어댑터에서 온 출력값을, 유스케이스를 호출한 어댑터로 반환할 출력 객체로 변환하는 것이다.

```java
package buckpal.application.service;

@RequiredArgsConstructor
@Transactional
public class SendMoneyService implements SendMoneyUseCase {

	private final LoadAccountPort loadAccountPort;
	private final AccountLock accountLock;
	private final UpdateAccountStatePort updateAccountStatePort;

	@Override
	public boolean sendMoney(SendMoneyCommand command) {

		// TODO: 비즈니스 규칙 검증
		// TODO: 모델 상태 조작
		// TODO: 출력 값 반환
    return false;
	}
}
```

서비스는 인커밍 포트 인터페이스인 `SendMoneyUseCase`를 구현하고, 계좌를 불러오기 위해 아웃고잉 포트 인터페이스인 `LoadAccountPort`를 호출한다. 그리고 DB의 계좌 상태를 업데이트하기 위해 `UpdateAccountStatePort`를 호출한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0998da23-6d5c-4655-b574-48a7f27c9f69)

## 입력 유효성 검증
호출하는 어댑터가 유스케이스에 입력을 전달하기 전에 입력 유효성을 검증하면 어떨까? 다음과 같은 문제가 생길 수 있다.

- 유스케이스에서 필요로 하는 것을 호출자가 모두 검증했다고 믿을 수 있을까?
- 유스케이스는 하나 이상의 어댑터에서 호출되는데, 유효성 검증을 각 어댑터에서 전부 구현해야 한다.
  - 이 과정에서 실수할 수도 있고, 유효성 검증을 해야 한다는 사실을 잊어버리게 될 수도 있다.

애플리케이션 계층에서 입력 유효성을 검증해야 하는 이유는, 그렇게 하지 않을 경우 애플리케이션 코어의 바깥쪽으로부터 유효하지 않은 입력값을 받게 되고, 모델의 상태를 해칠 수 있기 때문이다. 유스케이스 클래스가 아니라면 입력 모델이 이 문제를 다루도록 해보자.

생성자에서 필드의 필수값, 조건을 메서드로 검사하지 말고, Bean Validation API를 사용해서 처리해보자.

```java
package buckpal.application.port.in;

import lombok.EqualsAndHashCode;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
@EqualsAndHashCode(callSuper = false)
public class SendMoneyCommand extends SelfValidating<SendMoneyCommand> {

    @NotNull
    private final AccountId sourceAccountId;

    @NotNull
    private final AccountId targetAccountId;

    @NotNull
    private final Money money;

    public SendMoneyCommand(
            AccountId sourceAccountId,
            AccountId targetAccountId,
            Money money) {
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.money = money;
        this.validateSelf();
    }
}
```

`SelfValidating` 추상 클래스는 `validateSelf()` 메서드를 제공하며, 이는 필드에 지정된 Bean Validation 애너테이션을 검증하고, 유효성 검증 규칙을 위반한 경우 예외를 던진다.

입력 모델에 있는 유효성 검증 코드를 통해 유스케이스 구현체 주위에 사실상 오류 방지 계층을 만들었다. 이 계층은 하위 계층을 호출하는 계층형 아키텍처에서의 계층이 아니라 잘못된 입력을 호출자에게 돌려주는 유스케이스 보호막을 의미한다.

```java
package shared;

public abstract class SelfValidating<T> {
    
    private Validator;

    public SelfValidating() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    protected void validateSelf() {
        Set<ConstraintViolation<T>> violations = validator.validate((T) this);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
```

## 생성자의 힘
객체를 생성하기 위한 코드는 Builder를 사용하면 좋지만, 필드의 갯수가 계속해서 늘어남에 따라 휴먼 에러가 발생할 확률이 증가할 수 있다. 하지만 모든 필드를 사용하는 생성자를 사용하면 컴파일 타임에 생성자에 추가되지 못한 필드들을 확인할 수 있다.

## 유스케이스마다 다른 입력 모델
케이스 별로 필드의 필수 값이 가변적으로 되는 경우 코드 냄새라고 한다. (불변 커맨드 객체의 필드에 대해 null을 유효한 상태로 받아들이는 것)  
유스케이스에 커스텀 검증 로직을 넣는 작업은 비즈니스 코드를 입력 유효성 검증과 관련된 관심사로 오염시킨다.

각 유스케이스 전용 입력 모델은 유스케이스를 훨씬 명확하게 만들고 다른 유스케이스와의 결합도 제거해서 불필요한 부수효과가 발생하지 않게 한다. 물론 비용이 안드는 것은 아니다. 들어오는 데이터를 각 유스케이스에 해당하는 입력 모델에 매핑해야 하기 때문이다.

## 비즈니스 규칙 검증하기
입력 유효성 검증은 유스케이스 로직의 일부가 아닌 반면, 비즈니스 규칙 검증은 분명히 유스케이스 로직의 일부다. 비즈니스 규칙을 검증하는 것은 도메인 모델의 현재 상태에 접근해야 하는 반면, 입력 유효성 검증은 그럴 필요가 없다는 것이다. 입력 유효성을 검증하는 것은 구문상의 유효성을 검증하는 것이라고도 할 수 있다. 반면 비즈니스 규칙은 유스케이스의 맥락 속에서 의미적인 유효성을 검증하는 일이라고 할 수 있다.

가장 좋은 방법은 "출금 계좌는 초과 인출되어서는 안 된다" 규칙 처럼 비즈니스 규칙을 도메인 엔티티 안에 넣는 것이다.

```java
package buckpal.domain;

public class Account {
    
    // ...

    public boolean withdraw(Money money, AccountId targetAccountId) {
        if (!mayWithdraw(money)) {
            return false;
        }
        // ...
    }
}
```

이렇게 하면 이 규칙을 지켜야 하는 비즈니스 로직 바로 옆에 규칙이 위치하기 때문에 위치를 정하는 것도 쉽고 추론하기도 쉽다. 만약 도메인 엔티티에서 비즈니스 규칙을 검증하기가 여의치 않다면 유스케이스 코드에서 도메인 엔티티를 사용하기 전에 해도 된다.

```java
package buckpal.application.service;

@RequiredArgsConstructor
@Transactional
public class SendMoneyService implements SendMoneyUseCase {
    
    // ...

    @Override
    public boolean sendMoney(SendMoneyCommand command) {
        requireAccountExists(command.getSourceAccountId());
        requireAccountExists(command.getTargetAccountId());
        // ...
    }
}
```

## 풍부한 도메인 모델 vs. 빈약한 도메인 모델
도메인 모델을 구현하는 방법은 어떤 지침도 없기 때문에 각자의 필요에 맞는 스타일을 자유롭게 택해서 사용하도록 하자.

### 풍부한 도메인 모델
애플리케이션의 코어에 있는 엔티티에서 가능한 한 많은 도메인 로직이 구현된다. 엔티티들은 상태를 변경하는 메서드를 제공하고, 비즈니스 규칙에 맞는 유효한 변경만을 허용한다.

유스케이스는 도메인 모델의 진입점으로 동작한다. 이어서 유스케이스는 사용자의 의도만을 표현하면서 이 의도를 실제 작업을 수행하는 체계화된 도메인 엔티티 메서드 호출로 변환한다. 많은 비즈니스 규칙이 유스케이스 구현체 대신 엔티티에 위치하게 된다.

### 빈약한 도메인 모델
엔티티 자체가 굉장히 얇다. 일반적으로 엔티티는 상태를 표현하는 필드와 이 값을 읽고 바꾸기 위한 getter/setter 메서드만 포함하고 어떤 도메인 로직도 가지고 있지 않다. 이 말은 도메인 로직이 유스케이스 클래스에 구현돼 있다는 것이다. 비즈니스 규칙을 검증하고, 엔티티의 상태를 바꾸고, 데이터베이스 저장을 담당하는 아웃고잉 포트에 엔티티를 전달할 책임 역시 유스케이스 클래스에 있다. '풍부함'이 엔티티 대신 유스케이스에 존재하는 것이다.

## 유스케이스마다 다른 출력 모델
입력과 비슷하게 출력도 가능하면 각 유스케이스에 맞게 구체적일수록 좋다. 출력은 호출자에게 꼭 필요한 데이터만 들고 있어야 한다.

유스케이스들 간에 같은 출력 모델을 공유하게 되면 유스케이스들도 강하게 결합된다. 한 유스케이스에서 출력 모델에 새로운 필드가 필요해지면 이 값과 관련이 없는 다른 유스케이스에서도 이 필드를 처리해야 한다. 단일 책임 원칙을 적용하고 모델을 분리해서 유지하는 것은 유스케이스의 결합을 제거하는 데 도움이 된다. 같은 이유로 도메인 엔티티를 출력 모델로 사용하고 싶은 유혹도 견뎌야 한다.

## 읽기 전용 유스케이스는 어떨까?
애플리케이션 코어의 관점에서 간단한 조회 기능을 하는 내용은 프로젝트 맥락에서 유스케이스로 간주되지 않는다면 실제 유스케이스와 구분하기 위해 유스케이스를 만들기 보단 쿼리로 구현할 수 있다. 이를 위해 쿼리를 위한 인커밍 전용 포트를 만들고 이를 쿼리 서비스에 구현하는 것이다. 읽기 전용 쿼리는 쓰기가 가능한 유스케이스(또는 커맨드)와 코드 상에서 명확하게 구분된다. 이런 방식은 CQRS 같은 개념과 아주 잘 맞는다.

## 유지보수 가능한 소프트웨어를 만드는 데 어떻게 도움이 될까?
입출력 모델을 독립적으로 모델링한다면 원치 않는 부수효과를 피할 수 있다. 유스케이스 간에 모델을 공유하는 것보다는 더 많은 작업이 필요하다. 각 유스케이스마다 별도의 모델을 만들어야 하고, 이 모델과 엔티티를 매핑해야 한다.

그러나 유스케이스별로 모델을 만들면 유스케이스를 명확하게 이해할 수 있고, 장기적으로 유지보수하기도 더 쉽다. 또한 여러 명의 개발자가 다른 사람이 작업 중인 유스케이스를 건드리지 않은 채로 여러 개의 유스케이스를 동시에 작업할 수 있다. 꼼꼼한 입력 유효성 검증, 유스케이스별 입출력 모델은 지속 가능한 코드를 만드는 데 큰 도움이 된다.