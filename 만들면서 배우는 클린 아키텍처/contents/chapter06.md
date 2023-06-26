# 6장. 영속성 어댑터 구현하기
전통적인 계층형 아키텍처는 모든 것이 영속성 계층에 의존하게 되어 '데이터베이스 주도 설계'가 되는데, 이러한 의존성을 역전시키기 위해 영속성 계층을 애플리케이션 계층의 플러그인으로 만드는 방법을 살펴보자.

## 의존성 역전
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/46013618-155e-47ca-8003-152d9b0c3080)

애플리케이션 서비스에선 영속성 기능을 사용하기 위해 포트 인터페이스를 호출한다. 이 포트는 실제로 영속성 작업을 수행하고 DB와 통신할 책임을 가진 영속성 어댑터 클래스에 의해 구현된다.

포트는 사실상 애플리케이션 서비스와 영속성 코드 사이의 간접적인 계층이다. 영속성 계층에 대한 코드 의존성을 없애기 위해 이러한 간접 계층을 추가하고 있다는 사실을 잊지 말자. 자연스럽게 런타임에도 의존성은 애플리케이션 코어에서 영속성 어댑터로 향한다. 영속성 계층의 코드를 변경하는 중 버그가 생기면 애플리케이션 코어의 기능은 망가질 것이다. 하지만 포트가 계약을 만족하는 한, 코어에 영향을 미치지 않으면서 영속성 코드를 마음껏 수정할 수 있다.

## 영속성 어댑터의 책임
영속성 어댑터는 일반적으로 다음과 같은 일을 한다.

1. 입력을 받는다.
2. 입력을 데이터베이스 포맷으로 매핑한다.
3. 입력을 데이터베이스로 보낸다.
4. 데이터베이스 출력을 애플리케이션 포맷으로 매핑한다.
5. 출력을 반환한다.

- 영속성 어댑터는 포트 인터페이스를 통해 입력을 받는다. 입력 모델은 인터페이스가 지정한 도메인 엔티티나 특정 데이터베이스 연산 전용 객체가 될 것이다.
- 데이터베이스를 쿼리하거나 변경하는 데 사용할 수 있는 포맷으로 입력 모델을 매핑한다.  
(ex. Java → JPA Entity)  
- JPA나 다른 ORM 프레임워크, 데이터베이스와 통신하기 위해 어떤 기술을 사용해도 상관 없다.
- 핵심은 영속성 어댑터의 입력 모델이 영속성 어댑터 내부에 있는 것이 아니라 애플리케이션 코어에 있기 때문에 영속성 어댑터 내부를 변경하는 것이 코어에 영향을 미치지 않는다는 것이다.
- 데이터베이스에 쿼리를 날리고 쿼리 결과를 받아온다.
- 데이터베이스 응답 포트에 정의된 출력 모델로 매핑해서 반환한다.

## 포트 인터페이스 나누기
서비스를 구현하면서 생기는 의문은 데이터베이스 연산을 정의하고 있는 포트 인터페이스를 어떻게 나눌 것인가다. 아래 그림처럼 특정 엔티티가 필요로 하는 모든 데이터베이스 연산을 하나의 리포지토리 인터페이스에 넣어 두는 게 일반적인 방법이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b149bd70-1476-4b71-b893-eab58282253b)

데이터베이스 연산에 의존하는 각 서비스는 인터페이스에서 단 하나의 메서드만 사용하더라도 하나의 '넓은' 포트 인터페이스에 의존성을 갖게 된다. 이는 코드에 불필요한 의존성이 생겼다는 뜻이다.

맥락 안에서 필요하지 않은 메서드에 생긴 의존성은 코드를 이해하고 테스트하기 어렵게 만든다.  
예시로 여러개의 메서드가 있는 인터페이스를 모킹할 때 하나의 메서드만 모킹하고 나머지 메서드는 실제 사용되는 구현체의 코드를 복사했다고 가정한다. 그럼 이때 모킹된 구현체에 기대하는 내용이 호출하는 메서드마다 다를 수 있어서 테스트에 혼란을 줄 수 있다. 에러가 발생한다고 하면 해당 메서드에 찾아가서 모킹 여부를 확인하고 안되어있다면 모킹이 이뤄져야 하므로 많은 시간이 소요될 것 같다.

엉클 밥의 표현을 빌리자면, `필요없는 화물을 운반하는 무언가에 의존하고 있으면 예상하지 못했던 문제가 생길 수 있다.`

**인터페이스 분리 원칙(Interface Segregation Principle, ISP)은 이 문제의 답을 제시한다.** 이 원칙은 클라이언트가 오로지 자신이 필요로 하는 메서드만 알면 되도록 넓은 인터페이스를 특화된 인터페이스로 분리해야 한다고 설명한다.

이 원칙을 아웃고잉 포트에 적용해보면 다음과 같이 변한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/024b7485-f50d-4185-8154-882a8359bb02)

이제 각 서비스는 실제로 필요한 메서드에만 의존한다. 나아가 포트의 이름이 포트의 역할을 명확하게 잘 표현하고 있다. 테스트에선 어떤 메서드를 모킹할지 고민할 필요가 없다. 왜냐하면 대부분의 경우 포트당 하나의 메서드만 있을 것이기 때문이다.

## 영속성 어댑터 나누기
모든 영속성 포트를 구현하는 한, 하나 이상의 클래스 생성을 금지하는 규칙은 없다. 예를 들어, 아래와 같이 영속성 연산이 필요한 도메인 클래스(또는 DDD 애그리거트) 하나당 하나의 영속성 어댑터를 구현하는 방식을 선택할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/655ab400-c6b6-4662-8508-07da5fca083d)

이렇게 하면 영속성 어댑터들은 각 영속성 기능을 이용하는 도메인 경계를 따라 자동으로 나눠진다. 도메인 코드는 영속성 포트에 의해 정의된 명세를 어떤 클래스가 충족시키는지에 관심 없다는 사실을 기억하자. 모든 포트가 구현돼 있기만 한다면 영속성 계층에서 하고 싶은 어떤 작업이든 해도 된다.

바운디드 컨텍스트(bounded context)의 영속성 어댑터 시나리오는 다음과 같다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a9614fb2-7c45-4255-9386-d90420335e8b)

각 바운디드 컨텍스트는 영속성 어댑터를 하나씩 가지고 있다. **'바운디드 컨텍스트'라는 표현은 경계를 암시한다.** `account` 맥락의 서비스가 `billing` 맥락의 영속성 어댑터에 접근하지 않고, 반대로도 마찬가지라는 의미다. 어떤 맥락이 다른 맥락에 있는 무엇인가를 필요로 한다면 전용 인커밍 포트를 통해 접근해야 한다.

## 스프링 데이터 JPA 예제
```java
package buckpal.domain;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {
    @Getter
    private final AccountId id;

    @Getter
    private final ActivityWindow activityWindow;

    private final Money baselineBalance;

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

    public Money calculateBalance() {
        // ...
    }

    public boolean withdraw(Money money, AccountId targetAccountId) {
        // ...
    }

    public boolean deposit(Money money, AccountId sourceAccountId) {
        // ...
    }
}
```

`Account` 클래스는 getter와 setter만 가진 간단한 데이터 클래스가 아니며 최대한 불변성을 유지하려 한다는 사실을 상기하자. 유효한 상태의 `Acccount` 엔티티만 생성할 수 있는 팩터리 메서드를 제공하고 유효성 검증을 모든 상태 변경 메서드에서 수행하기 때문에 유효하지 않은 도메인 모델을 생성할 수 없다.

```java
package buckpal.adapter.persistence;

@Entity
@Table(name = "account")
@Data
@AllArgsConstructor
@NoArgsConstructor
class AccountJpaEntity {
    @Id
    @GeneratedValue
    private Long id;
}
```

```java
package buckpal.adapter.persistence;

@Entity
@Table(name = "activity")
@Data
@AllArgsConstructor
@NoArgsConstructor
class ActivityJpaEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private LocalDateTime timestamp;

    @Column
    private Long ownerAccountId;

    @Column
    private Long sourceAccountId;

    @Column
    private Long targetAccountId;

    @Column
    private Long amount;
}
```

이 단계에선 계좌의 상태가 ID 하나만으로 구성돼 있다. 나중에 사용자 ID 같은 필드가 추가될 것이다. JPA의 `@ManyToOne`이나 `@OneToMany` 애너테이션을 이용해 `ActivityJpaEntity`와 `AccountJpaEntity`를 연결해서 관계를 표현할 수도 있었겠지만 데이터베이스 쿼리에 부수효과가 생길 수 있기 때문에 일단 이 부분은 제외되었다.

다음으로 기본적인 CRUD 기능과 데이터베이스에서 activity들을 로드하기 위한 커스텀 쿼리를 제공하는 리포지토리 인터페이스를 생성하기 위해 Spring Data를 사용한다.

```java
package buckpal.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface AccountRepository extends JpaRepository<AccountJpaEntity, Long> {
}
```

```java
package buckpal.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

interface ActivityRepository extends JpaRepository<ActivityJpaEntity, Long> {

	@Query("select a from ActivityJpaEntity a " +
			"where a.ownerAccountId = :ownerAccountId " +
			"and a.timestamp >= :since")
	List<ActivityJpaEntity> findByOwnerSince(
			@Param("ownerAccountId") Long ownerAccountId,
			@Param("since") LocalDateTime since);

	@Query("select sum(a.amount) from ActivityJpaEntity a " +
			"where a.targetAccountId = :accountId " +
			"and a.ownerAccountId = :accountId " +
			"and a.timestamp < :until")
	Long getDepositBalanceUntil(
			@Param("accountId") Long accountId,
			@Param("until") LocalDateTime until);

	@Query("select sum(a.amount) from ActivityJpaEntity a " +
			"where a.sourceAccountId = :accountId " +
			"and a.ownerAccountId = :accountId " +
			"and a.timestamp < :until")
	Long getWithdrawalBalanceUntil(
			@Param("accountId") Long accountId,
			@Param("until") LocalDateTime until);

}
```

Spring Boot는 이 리포지토리를 자동으로 찾고, Spring Data는 실제로 데이터베이스와 통신하는 리포지터리 인터페이스 구현체를 제공하는 마법을 부린다. 영속성 기능을 제공하는 영속성 어댑터를 구현해보자.

```java
package buckpal.adapter.persistence;

import buckpal.application.port.out.LoadAccountPort;
import buckpal.application.port.out.UpdateAccountStatePort;
import buckpal.domain.Account;
import buckpal.domain.Activity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
class AccountPersistenceAdapter implements
		LoadAccountPort,
		UpdateAccountStatePort {

	private final AccountRepository accountRepository;
	private final ActivityRepository activityRepository;
	private final AccountMapper accountMapper;

	@Override
	public Account loadAccount(
			Account.AccountId accountId,
			LocalDateTime baselineDate) {

		AccountJpaEntity account =
				accountRepository.findById(accountId.getValue())
						.orElseThrow(EntityNotFoundException::new);

		List<ActivityJpaEntity> activities =
				activityRepository.findByOwnerSince(
						accountId.getValue(),
						baselineDate);

		Long withdrawalBalance = orZero(activityRepository
				.getWithdrawalBalanceUntil(
						accountId.getValue(),
						baselineDate));

		Long depositBalance = orZero(activityRepository
				.getDepositBalanceUntil(
						accountId.getValue(),
						baselineDate));

		return accountMapper.mapToDomainEntity(
				account,
				activities,
				withdrawalBalance,
				depositBalance);

	}

	private Long orZero(Long value){
		return value == null ? 0L : value;
	}


	@Override
	public void updateActivities(Account account) {
		for (Activity activity : account.getActivityWindow().getActivities()) {
			if (activity.getId() == null) {
				activityRepository.save(accountMapper.mapToJpaEntity(activity));
			}
		}
	}
}
```

영속성 어댑터는 애플리케이션에 필요한 `LoadAccountPort`와 `UpdateAccountStatePort`라는 2개의 포트를 구현했다.

데이터베이스로부터 계좌를 가져오기 위해 `AccountRepository`로 계좌를 불러온 다음, `ActivityRepository`로 해당 계좌의 특정 시간 범위 동안의 활동을 가져온다. 유효한 `Account` 도메인 엔티티를 생성하기 위해선 이 활동창 시작 직전의 계좌 잔고가 필요하다. 마지막으로 이 모든 데이터를 `Account` 도메인 엔티티에 매핑하고 호출자에게 반환한다.

`Account` 엔티티의 모든 활동을 순회하며 ID가 있는지 확인한다. 없다면 새로운 활동이므로 `ActivityRepository`를 이용해 저장해야 한다.

앞에서 설명한 시나리오에선 양방향 매핑이 존재한다. JPA 애너테이션을 `Account`와 `Activity` 클래스로 옮기고 이를 그대로 데이터베이스에 엔티티로 저장하면 안 될까?

8장에서 살펴보겠지만 이런 '매핑하지 않기' 전략도 유효한 전략일 수 있다. `@ManyToOne` 관계를 설정하면 성능 측면에서 이득이 있지만, 예제에선 항상 데이터의 일부만 가져오기를 바라기 때문에 도메인 모델에선 이 관계가 반대가 되기를 원한다.

영속성 측면과의 타협 없이 풍부한 도메인 모델을 생성하고 싶다면 도메인 모델과 영속성 모델을 매핑하는 것이 좋다.

## 데이터베이스 트랜잭션은 어떻게 해야 할까?
**트랜잭션은 하나의 특정한 유스케이스에 대해 일어나는 모든 쓰기 작업에 걸쳐 있어야 한다.** 그래야 그중 하나라도 실패할 경우 다 같이 롤백될 수 있기 때문이다.

영속성 어댑터는 어떤 데이터베이스 연산이 같은 유스케이스에 포함되는지 알지 못하기 때문에 언제 트랜잭션을 열고 닫을지 결정할 수 없다. **이 책임은 영속성 어댑터 호출을 관장하는 서비스에 위임해야 한다.**

Java와 Spring에서 가장 쉬운 방법은 `@Transactional` 애너테이션을 애플리케이션 서비스에 붙여 Spring이 모든 `public` 메서드를 트랜잭션으로 감싸게 하는 것이다.

```java
package buckpal.application.service;

@Transactional
public class SendMoneyService implements SendMoneyUseCase {
    // ...
}
```

## 유지보수 가능한 소프트웨어를 만드는 데 어떻게 도움이 될까?
도메인 코드에 플러그인처럼 동작하는 영속성 어댑터를 만들면 도메인 코드가 영속성과 관련된 것들로부터 분리되어 풍부한 도메인 모델을 만들 수 있다. 좁은 포트 인터페이스를 사용하면 포트마다 다른 방식으로 구현할 수 있는 유연함이 생긴다. 심지어 포트 뒤에서 애플리케이션이 모르게 다른 영속성 기술을 사용할 수도 있다. 포트의 명세만 지켜진다면 영속성 계층 전체를 교체할 수도 있다.
