# 7장. 아키텍처 요소 테스트하기
## 테스트 피라미드
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b4c848e3-dbd2-4b8a-99b8-c3635264306a)

테스트의 기본 전제는 **만드는 비용이 적고, 유지보수하기 쉽고, 빨리 실행되고, 안정적인 작은 크기의 테스트들에 대해 높은 커버리지를 유지해야 한다는 것이다.** 테스트는 하나의 '단위'가 제대로 동작하는지 확인할 수 있는 단위 테스트들이다.

여러 개의 단위와 단위를 넘는 경계, 아키텍처 경계, 시스템 경계를 결합하는 테스트는 만드는 비용이 더 비싸지고, 실행이 더 느리며 (기능 에러가 아닌 설정 에러로 인해) 깨지기 더 쉬워진다. 테스트 피라미드는 테스트가 비싸질수록 테스트의 커버리지 목표는 낮게 잡아야 한다는 것을 보여준다. 그렇지 않으면 새로운 기능을 만드는 것보다 테스트를 만드는 데 시간을 더 쓰게 되기 때문이다.

단위 테스트는 피라미드의 토대에 해당한다. 하나의 클래스를 인스턴스화하고 해당 클래스의 인터페이스를 통해 기능들을 테스트한다. 만약 테스트 중인 클래스가 다른 클래스에 의존한다면 의존되는 클래스들은 인스턴스화하지 않고 테스트하는 동안 필요한 작업들을 흉내 내는 mock으로 대체한다.

통합 테스트는 연결된 여러 유닛을 인스턴스화하고 시작점이 되는 클래스의 인터페이스로 데이터를 보낸 후 유닛들의 네트워크가 기대한대로 잘 동작하는지 검증한다. 이 책에서 정의한 통합 테스트에선 두 계층 간의 경계를 걸쳐서 테스트할 수 있기 때문에 객체 네트워크가 완전하지 않거나 어떤 시점에는 mock을 대상으로 수행해야 한다.

시스템 테스트는 애플리케이션을 구성하는 모든 객체 네트워크를 가동시켜 특정 유스케이스가 전 계층에서 잘 동작하는지 검증한다.

## 단위 테스트로 도메인 엔티티 테스트하기
```java
package buckpal.account.domain;

import buckpal.account.domain.Account.AccountId;
import org.junit.jupiter.api.Test;
import static buckpal.common.AccountTestData.*;
import static buckpal.common.ActivityTestData.*;
import static org.assertj.core.api.Assertions.*;

class AccountTest {

	@Test
	void calculatesBalance() {
		AccountId accountId = new AccountId(1L);
		Account account = defaultAccount()
				.withAccountId(accountId)
				.withBaselineBalance(Money.of(555L))
				.withActivityWindow(new ActivityWindow(
						defaultActivity()
								.withTargetAccount(accountId)
								.withMoney(Money.of(999L)).build(),
						defaultActivity()
								.withTargetAccount(accountId)
								.withMoney(Money.of(1L)).build()))
				.build();

		Money balance = account.calculateBalance();

		assertThat(balance).isEqualTo(Money.of(1555L));
	}

	@Test
	void withdrawalSucceeds() {
		AccountId accountId = new AccountId(1L);
		Account account = defaultAccount()
				.withAccountId(accountId)
				.withBaselineBalance(Money.of(555L))
				.withActivityWindow(new ActivityWindow(
						defaultActivity()
								.withTargetAccount(accountId)
								.withMoney(Money.of(999L)).build(),
						defaultActivity()
								.withTargetAccount(accountId)
								.withMoney(Money.of(1L)).build()))
				.build();

		boolean success = account.withdraw(Money.of(555L), new AccountId(99L));

		assertThat(success).isTrue();
		assertThat(account.getActivityWindow().getActivities()).hasSize(3);
		assertThat(account.calculateBalance()).isEqualTo(Money.of(1000L));
	}

	@Test
	void withdrawalFailure() {
		AccountId accountId = new AccountId(1L);
		Account account = defaultAccount()
				.withAccountId(accountId)
				.withBaselineBalance(Money.of(555L))
				.withActivityWindow(new ActivityWindow(
						defaultActivity()
								.withTargetAccount(accountId)
								.withMoney(Money.of(999L)).build(),
						defaultActivity()
								.withTargetAccount(accountId)
								.withMoney(Money.of(1L)).build()))
				.build();

		boolean success = account.withdraw(Money.of(1556L), new AccountId(99L));

		assertThat(success).isFalse();
		assertThat(account.getActivityWindow().getActivities()).hasSize(2);
		assertThat(account.calculateBalance()).isEqualTo(Money.of(1555L));
	}

	@Test
	void depositSuccess() {
		AccountId accountId = new AccountId(1L);
		Account account = defaultAccount()
				.withAccountId(accountId)
				.withBaselineBalance(Money.of(555L))
				.withActivityWindow(new ActivityWindow(
						defaultActivity()
								.withTargetAccount(accountId)
								.withMoney(Money.of(999L)).build(),
						defaultActivity()
								.withTargetAccount(accountId)
								.withMoney(Money.of(1L)).build()))
				.build();

		boolean success = account.deposit(Money.of(445L), new AccountId(99L));

		assertThat(success).isTrue();
		assertThat(account.getActivityWindow().getActivities()).hasSize(3);
		assertThat(account.calculateBalance()).isEqualTo(Money.of(2000L));
	}

}
```

특정 상태의 `Account`를 인스턴스화하고 `withdraw()` 메서드를 호출해서 출금을 성공했는지 검증하고, `Account` 객체의 상태에 대해 기대되는 부수효과들이 잘 일어났는지 확인하는 단순한 단위 테스트다.

이 테스트는 만들고 이해하는 것도 쉬운 편이고, 아주 빠르게 실행된다. 이런 식의 단위 테스트가 도메인 엔티티에 녹아 있는 비즈니스 규칙을 검증하기에 가장 적절한 방법이다. 도메인 엔티티의 행동은 다른 클래스의 거의 의존하지 않기 때문에 다른 종류의 테스트는 필요하지 않다.

## 단위 테스트로 유스케이스 테스트하기
```java
package buckpal.account.application.service;

import buckpal.account.application.port.in.SendMoneyCommand;
import buckpal.account.application.port.out.AccountLock;
import buckpal.account.application.port.out.LoadAccountPort;
import buckpal.account.application.port.out.UpdateAccountStatePort;
import buckpal.account.domain.Account;
import buckpal.account.domain.Account.AccountId;
import buckpal.account.domain.Money;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class SendMoneyServiceTest {

	private final LoadAccountPort loadAccountPort =
			Mockito.mock(LoadAccountPort.class);

	private final AccountLock accountLock =
			Mockito.mock(AccountLock.class);

	private final UpdateAccountStatePort updateAccountStatePort =
			Mockito.mock(UpdateAccountStatePort.class);

	private final SendMoneyService sendMoneyService =
			new SendMoneyService(loadAccountPort, accountLock, updateAccountStatePort, moneyTransferProperties());

	@Test
	void givenWithdrawalFails_thenOnlySourceAccountIsLockedAndReleased() {

		AccountId sourceAccountId = new AccountId(41L);
		Account sourceAccount = givenAnAccountWithId(sourceAccountId);

		AccountId targetAccountId = new AccountId(42L);
		Account targetAccount = givenAnAccountWithId(targetAccountId);

		givenWithdrawalWillFail(sourceAccount);
		givenDepositWillSucceed(targetAccount);

		SendMoneyCommand command = new SendMoneyCommand(
				sourceAccountId,
				targetAccountId,
				Money.of(300L));

		boolean success = sendMoneyService.sendMoney(command);

		assertThat(success).isFalse();

		then(accountLock).should().lockAccount(eq(sourceAccountId));
		then(accountLock).should().releaseAccount(eq(sourceAccountId));
		then(accountLock).should(times(0)).lockAccount(eq(targetAccountId));
	}

	@Test
	void transactionSucceeds() {

		Account sourceAccount = givenSourceAccount();
		Account targetAccount = givenTargetAccount();

		givenWithdrawalWillSucceed(sourceAccount);
		givenDepositWillSucceed(targetAccount);

		Money money = Money.of(500L);

		SendMoneyCommand command = new SendMoneyCommand(
				sourceAccount.getId().get(),
				targetAccount.getId().get(),
				money);

		boolean success = sendMoneyService.sendMoney(command);

		assertThat(success).isTrue();

		AccountId sourceAccountId = sourceAccount.getId().get();
		AccountId targetAccountId = targetAccount.getId().get();

		then(accountLock).should().lockAccount(eq(sourceAccountId));
		then(sourceAccount).should().withdraw(eq(money), eq(targetAccountId));
		then(accountLock).should().releaseAccount(eq(sourceAccountId));

		then(accountLock).should().lockAccount(eq(targetAccountId));
		then(targetAccount).should().deposit(eq(money), eq(sourceAccountId));
		then(accountLock).should().releaseAccount(eq(targetAccountId));

		thenAccountsHaveBeenUpdated(sourceAccountId, targetAccountId);
	}

	private void thenAccountsHaveBeenUpdated(AccountId... accountIds){
		ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
		then(updateAccountStatePort).should(times(accountIds.length))
				.updateActivities(accountCaptor.capture());

		List<AccountId> updatedAccountIds = accountCaptor.getAllValues()
				.stream()
				.map(Account::getId)
				.map(Optional::get)
				.collect(Collectors.toList());

		for(AccountId accountId : accountIds){
			assertThat(updatedAccountIds).contains(accountId);
		}
	}

	private void givenDepositWillSucceed(Account account) {
		given(account.deposit(any(Money.class), any(AccountId.class)))
				.willReturn(true);
	}

	private void givenWithdrawalWillFail(Account account) {
		given(account.withdraw(any(Money.class), any(AccountId.class)))
				.willReturn(false);
	}

	private void givenWithdrawalWillSucceed(Account account) {
		given(account.withdraw(any(Money.class), any(AccountId.class)))
				.willReturn(true);
	}

	private Account givenTargetAccount(){
		return givenAnAccountWithId(new AccountId(42L));
	}

	private Account givenSourceAccount(){
		return givenAnAccountWithId(new AccountId(41L));
	}

	private Account givenAnAccountWithId(AccountId id) {
		Account account = Mockito.mock(Account.class);
		given(account.getId())
				.willReturn(Optional.of(id));
		given(loadAccountPort.loadAccount(eq(account.getId().get()), any(LocalDateTime.class)))
				.willReturn(account);
		return account;
	}

	private MoneyTransferProperties moneyTransferProperties(){
		return new MoneyTransferProperties(Money.of(Long.MAX_VALUE));
	}

}
```

`SendMoney` 유스케이스는 출금 계좌의 잔고가 다른 트랜잭션에 의해 변경되지 않도록 lock을 건다. 출금 계좌에서 돈이 출금되고 나면 똑같이 입금 계좌에 락을 걸고 돈을 입금시킨다. 그러고 나서 두 계좌에서 모두 lock을 해제한다.

- given: 출금 및 입금 `Account` 인스턴스를 각각 생성하고 적절한 상태로 만들어서 `given...()`으로 시작하는 메서드에 인자로 넣었다. `SendMoneyCommand` 인스턴스도 만들어서 유스케이스의 입력으로 사용했다.
- when: 유스케이스를 실행하기 위해 `sendMoney()` 메서드를 호출.
- then: 트랜잭션이 성공적이었는지 확인하고, 출금 및 입금 `Account`, 그리고 계좌에 lock을 걸고 해제하는 책임을 가진 `AccountLock`에 대해 특정 메서드가 호출됐는지 검증한다.

Mockito 라이브러리를 이용해 `given...()` 메서드의 mock 객체를 생성한다. Mockito는 mock 객체에 대해 특정 메서드가 호출됐는지 검증할 수 있는 `then()` 메서드도 제공한다.

테스트 중인 유스케이스 서비스는 상태가 없기 때문에 `then` 섹션에서 특정 상태를 검증할 수 없다. 대신 테스트는 서비스가 모킹된 의존 대상의 특정 메서드와 상호작용했는지 여부를 검증한다. **이는 테스트가 코드의 행동 변경뿐만 아니라 코드의 구조 변경에도 취약해진다는 의미가 된다.** 자연스럽게 코드가 리팩토링 되면 테스트도 변경될 확률이 높아진다.

그렇기 때문에, 테스트에서 어떤 상호작용을 검증하고 싶은지 신중하게 생각해야 한다. 모든 동작을 검증하는 대신 중요한 핵심만 골라 집중해서 테스트하는 것이 좋다. **만약 모든 동작을 검증하려고 하면 클래스가 조금이라도 바뀔 때마다 테스트를 변경해야 한다. 이는 테스트의 가치를 떨어뜨리는 일이다.**

이 테스트는 단위 테스트이긴 하지만 의존성의 상호작용을 테스트하고 있기 때문에 통합 테스트에 가깝다. 하지만 mock으로 작업하고 있고 실제 의존성을 관리해야 하는 것은 아니기 때문에 완전한 통합 테스트에 비해 만들고 유지보수하기가 쉽다.

## 통합 테스트로 웹 어댑터 테스트하기
웹 어댑터를 테스트할 흐름은 다음과 같다.

- JSON 문자열 등 형태로 HTTP를 통해 입력을 받는다.
- 입력에 대한 유효성 검증
- 유스케이스에서 사용하는 포멧으로 컨버팅
- 유스케이스에 전달
- 유스케이스 결과값 반환
- 결과값 JSON으로 컨버팅 후 HTTP 응답

```java
package buckpal.account.adapter.in.web;

import buckpal.account.application.port.in.SendMoneyUseCase;
import buckpal.account.application.port.in.SendMoneyCommand;
import buckpal.account.domain.Account.AccountId;
import buckpal.account.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SendMoneyController.class)
class SendMoneyControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SendMoneyUseCase sendMoneyUseCase;

	@Test
	void testSendMoney() throws Exception {

		mockMvc.perform(post("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
				41L, 42L, 500)
				.header("Content-Type", "application/json"))
				.andExpect(status().isOk());

		then(sendMoneyUseCase).should()
				.sendMoney(eq(new SendMoneyCommand(
						new AccountId(41L),
						new AccountId(42L),
						Money.of(500L))));
	}

}
```

위 코드는 Spring Boot 프레임워크에서 `SendMoneyController`라는 웹 컨트롤러를 테스트하는 표준적인 통합 테스트 방법이다. `MockMvc` 객체를 이용해 모킹했기 때문에 실제로 HTTP 프로토콜을 통해 테스트한 것은 아니다. 프레임워크가 HTTP 프로토콜에 맞게 모든 것을 적절히 잘 변환한다고 믿는 것이다.

그러나 입력을 JSON에서 `SendMoneyCommand` 객체로 매핑하는 전 과정은 다루고 있다. 또한 유스케이스가 실제로 호출됐는지도 검증했고, HTTP 응답이 기대한 상태를 반환했는지도 검증했다.

이 테스트가 단위 테스트가 아닌 통합 테스트인 이유는 다음과 같다. 이 테스트에선 하나의 웹 컨트롤러 클래스만 테스트한 것처럼 보이지만, 사실 보이지 않는 곳에서 더 많은 일들이 벌어지고 있다. `@WebMvcTest`는 Spring이 특정 요청 경로, Java와 JSON 간의 매핑, HTTP 입력 검증 등에 필요한 전체 객체 네트워크를 인스턴스화하도록 만든다. 그리고 테스트에선 웹 컨트롤러가 이 네트워크의 일부로서 잘 동작하는지 검증한다.

웹 컨트롤러가 Spring 프레임워크에 강하게 묶여 있기 때문에 격리된 상태로 테스트하기 보다는 이 프레임워크와 통합된 상태로 테스트하는 것이 합리적이다.

## 통합 테스트로 영속성 어댑터 테스트하기
비슷한 이유로 영속성 어댑터의 테스트에는 단위 테스트보다는 통합 테스트를 적용하는 것이 합리적이다. 단순히 어댑터의 로직만 검증하고 싶은 게 아니라 데이터베이스 매핑도 검증하고 싶기 때문이다.

```java
package buckpal.account.adapter.out.persistence;

import java.time.LocalDateTime;

import buckpal.account.domain.Account;
import buckpal.account.domain.Account.AccountId;
import buckpal.account.domain.ActivityWindow;
import buckpal.account.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import static buckpal.common.AccountTestData.*;
import static buckpal.common.ActivityTestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({AccountPersistenceAdapter.class, AccountMapper.class})
class AccountPersistenceAdapterTest {

	@Autowired
	private AccountPersistenceAdapter adapterUnderTest;

	@Autowired
	private ActivityRepository activityRepository;

	@Test
	@Sql("AccountPersistenceAdapterTest.sql")
	void loadsAccount() {
		Account account = adapterUnderTest.loadAccount(new AccountId(1L), LocalDateTime.of(2018, 8, 10, 0, 0));

		assertThat(account.getActivityWindow().getActivities()).hasSize(2);
		assertThat(account.calculateBalance()).isEqualTo(Money.of(500));
	}

	@Test
	void updatesActivities() {
		Account account = defaultAccount()
				.withBaselineBalance(Money.of(555L))
				.withActivityWindow(new ActivityWindow(
						defaultActivity()
								.withId(null)
								.withMoney(Money.of(1L)).build()))
				.build();

		adapterUnderTest.updateActivities(account);

		assertThat(activityRepository.count()).isEqualTo(1);

		ActivityJpaEntity savedActivity = activityRepository.findAll().get(0);
		assertThat(savedActivity.getAmount()).isEqualTo(1L);
	}

}
```

`@DataJpaTest` 애너테이션으로 Spring Data 리포지토리들을 포함해서 데이터베이스 접근에 필요한 객체 네트워크를 인스턴스화해야 한다고 스프링에 알려준다. `@Import` 애너테이션을 추가해서 특정 객체가 이 네트워크에 추가됐다는 것을 명확하게 표현할 수 있다.

이 테스트에선 데이터베이스를 모킹하지 않았다는 점이 중요하다. 테스트가 실제로 데이터베이스에 접근한다. 데이터베이스를 모킹했더라도 테스트는 여전히 같은 코드 라인 수만큼 커버해서 똑같이 높은 커버리지를 보여줬을 것이다. 하지만 여전히 실제 데이터베이스와 연동했을 때 SQL 구문의 오류나 데이터베이스 테이블과 Java 객체 간의 매핑 에러 등으로 문제가 생길 확률이 높아진다.

참고로 Spring에선 기본적으로 인메모리 데이터베이스를 테스트에서 사용한다. 이는 아무것도 설정할 필요 없이 곧바로 테스트할 수 있으므로 아주 실용적이다.

하지만, 프로덕션 환경에선 인메모리 데이터베이스를 사용하지 않는 경우가 많기 때문에 인메모리 데이터베이스에서 테스트가 완벽하게 통과했더라도 실제 데이터베이스에서는 문제가 생길 가능성이 높다. 예를 들면, 데이터베이스마다 고유한 SQL 문법이 있어서 이 부분이 문제가 되는 식으로 말이다.

따라서 영속성 어댑터 테스트는 실제 데이터베이스를 대상으로 진행해야 한다. Testcontainers 같은 라이브러리는 필요한 데이터베이스를 Docker 컨테이너에 띄울 수 있기 때문에 이런 측면에서 아주 유용하다.

실제 데이터베이스를 대상으로 테스트를 진행하면 두 개의 다른 데이터베이스 시스템을 신경 쓸 필요가 없다는 장점도 생긴다.

## 시스템 테스트로 주요 경로 테스트하기
```java
package buckpal;

import java.time.LocalDateTime;

import buckpal.account.application.port.out.LoadAccountPort;
import buckpal.account.domain.Account;
import buckpal.account.domain.Account.AccountId;
import buckpal.account.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import static org.assertj.core.api.BDDAssertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SendMoneySystemTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private LoadAccountPort loadAccountPort;

	@Test
	@Sql("SendMoneySystemTest.sql")
	void sendMoney() {

		Money initialSourceBalance = sourceAccount().calculateBalance();
		Money initialTargetBalance = targetAccount().calculateBalance();

		ResponseEntity response = whenSendMoney(
				sourceAccountId(),
				targetAccountId(),
				transferredAmount());

		then(response.getStatusCode())
				.isEqualTo(HttpStatus.OK);

		then(sourceAccount().calculateBalance())
				.isEqualTo(initialSourceBalance.minus(transferredAmount()));

		then(targetAccount().calculateBalance())
				.isEqualTo(initialTargetBalance.plus(transferredAmount()));

	}

	private Account sourceAccount() {
		return loadAccount(sourceAccountId());
	}

	private Account targetAccount() {
		return loadAccount(targetAccountId());
	}

	private Account loadAccount(AccountId accountId) {
		return loadAccountPort.loadAccount(
				accountId,
				LocalDateTime.now());
	}


	private ResponseEntity whenSendMoney(
			AccountId sourceAccountId,
			AccountId targetAccountId,
			Money amount) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		HttpEntity<Void> request = new HttpEntity<>(null, headers);

		return restTemplate.exchange(
				"/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
				HttpMethod.POST,
				request,
				Object.class,
				sourceAccountId.getValue(),
				targetAccountId.getValue(),
				amount.getAmount());
	}

	private Money transferredAmount() {
		return Money.of(500L);
	}

	private Money balanceOf(AccountId accountId) {
		Account account = loadAccountPort.loadAccount(accountId, LocalDateTime.now());
		return account.calculateBalance();
	}

	private AccountId sourceAccountId() {
		return new AccountId(1L);
	}

	private AccountId targetAccountId() {
		return new AccountId(2L);
	}

}
```

`@SpringBootTest` 애너테이션은 Spring이 애플리케이션을 구성하는 모든 객체 네트워크를 띄우게 한다. 또한 랜덤 포트로 이 애플리케이션을 띄우도록 설정하고 있다.

여기선 웹 어댑터에서처럼 `MockMvc`를 이용해 요청을 보내는 것이 아니라 `TestRestTemplate`을 이용해서 요청을 보낸다. 테스트를 프로덕션 환경에 조금 더 가깝게 만들기 위해 실제 HTTP 통신을 하는 것이다.

시스템 테스트라고 하더라도 언제나 서드파티 시스템을 실행해서 테스트할 수 있는 것은 아니기 때문에 결국 모킹을 해야 할 때도 있다. 헥사고날 아키텍처는 이러한 경우 몇 개의 출력 포트 인터페이스만 모킹하면 되기 때문에 아주 쉽게 이 문제를 해결할 수 있다.

테스트 가독성을 높이기 위해 지저분한 로직들을 헬퍼 메서드 안으로 감췄다. 이 헬퍼 메서들은 여러 가지 상태를 검증할 때 사용할 수 있는 도메인 특화 언어(DSL)을 형성한다. 도메인 특화 언어는 어떤 테스트에서도 유용하지만 시스템 테스트에선 더욱 의미를 가진다. 시스템 테스트는 단위 테스트나 통합 테스트가 할 수 있는 것보다 훨씬 더 실제 사용자를 잘 흉내내기 때문에 사용자 관점에서 애플리케이션을 검증할 수 있다.

시스템 테스트는 단위 테스트, 통합 테스트에서 커버한 코드와 겹치는 부분이 많을 것이다. 그럼에도 일반적으로 시스템 테스트는 단위 테스트와 통합 테스트가 발견하는 버그와는 또 다른 종류의 버그를 발견해서 수정할 수 있게 해준다. 그 예로는 계층 간 매핑 버그 같은 것이 있다.

시스템 테스트는 여러 개의 유스케이스를 결합해서 시나리오를 만들 때 더 빛이 난다. 각 시나리오는 사용자가 애플리케이션을 사용하면서 거쳐갈 특정 경로를 의미한다. 시스템 테스트를 통해 중요한 시나리오들이 커버된다면 최신 변경사항들이 애플리케이션을 망가뜨리지 않았음을 가정할 수 있고, 배포될 준비가 됐다는 확신을 가질 수 있다.

## 얼마만큼의 테스트가 충분할까?
라인 커버리지는 테스트 성공을 측정하는 데 있어서는 잘못된 지표라고 한다. 코드의 중요한 부분이 전혀 커버되지 않을 수 있기 때문에 100%를 제외한 어떤 목표도 완전히 무의미하며 심지어 버그가 잘 잡혔는지 확신할 수 없다.

저자는 테스트를 성공하고 배포를 하고나서 발생하는 버그를 수정하고 이로부터 배우는 것을 우선순위로 삼으면 방향성이 맞다고 말한다.

발생한 버그를 커버할 수 있도록 테스트 코드를 수정하고 지속적으로 개선해나간다면 배포할 때 마음을 편안하게 해줄 것이다.

헥사고날 아키텍처에서의 테스트 전략은 다음과 같다.
- 도메인 엔티티를 구현할 때는 단위 테스트
- 유스케이스를 구현할 때는 단위 테스트
- 어댑터를 구현할 때는 통합 테스트
- 사용자가 취할 수 있는 중요 애플리케이션 경로는 시스템 테스트

리팩터링 할 때마다 테스트 코드가 변경되고 그 시간이 오래 걸린다면 테스트로서의 가치를 잃는다.

## 유지보수 가능한 소프트웨어를 만드는 데 어떻게 도움이 될까?
헥사고날 아키텍처는 도메인 로직과 바깥으로 향한 어댑터를 깔끔하게 분리한다. 덕분에 핵심 도메인 로직은 단위 테스트로, 어댑터는 통합 테스트로 처리하는 명확한 테스트 전략을 정의할 수 있다.

입출력 포트는 테스트에서 아주 뚜렷한 모킹 지점이 된다. 각 포트에 대해 모킹할지, 실제 구현을 이용할지 선택할 수 잇다. 만약 포트가 아주 작고 핵심만 담고 있다면 모킹하는 것이 아주 쉬울 것이다. 포트 인터페이스가 더 작은 메서드를 제공할수록 어떤 메서드를 모킹해야 할지 덜 헷갈린다.

모킹하는 것이 너무 버거워지거나 코드의 특정 부분을 커버하기 위해 어떤 종류의 테스트를 써야 할지 모르겠다면 이는 경고 신호라고 저자는 말한다. 테스트는 아키텍처의 문제에 대해 경고하고 유지보수 가능한 코드를 만들기 위한 올바른 길로 인도하는 역할도 한다고 할 수 있다.