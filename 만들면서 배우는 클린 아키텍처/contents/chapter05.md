# 5장. 웹 어댑터 구현하기
오늘날 애플리케이션은 대부분 웹 인터페이스 같은 것을 제공한다. 웹 브라우저를 통해 상호작용할 수 있는 UI나 HTTP API가 여기에 해당한다. 클린 아키텍처에서 외부 세계와의 모든 커뮤니케이션은 어댑터를 통해 이뤄진다.

## 의존성 역전

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/05162edf-3759-4faf-bae3-e51de53891d4)

웹 어댑터는 '주도하는' 혹은 '인커밍' 어댑터다. 외부로부터 요청을 받아 애플리케이션 코어를 호출하고 무슨 일을 해야 할지 알려준다. 제어 흐름은 웹 어댑터에 있는 컨트롤러에서 애플리케이션 계층에 있는 서비스로 흐른다.  
애플리케이션 계층은 웹 어댑터가 통신할 수 있는 특정 포트를 제공한다. 서비스는 이 포트를 구현하고, 웹 어댑터는 이 포트를 호출할 수 있다.

웹 어댑터가 유스케이스를 직접 호출할 수도 있지만, 사이에 간접 계층이 들어 있다. 애플리케이션 코어가 외부 세계와 통신할 수 있는 곳에 대한 명세가 포트이기 때문이다. 포트를 적절한 곳에 위치시키면 외부와 어떤 통신이 일어나고 있는지 정확히 알 수 있다.

## 웹 어댑터의 책임
웹 어댑터는 일반적으로 다음과 같은 일을 한다.

1. HTTP 요청을 Java 객체로 매핑
2. 권한 검사
3. 입력 유효성 검증
4. 입력을 유스케이스의 입력 모델로 매핑
5. 유스케이스 호출
6. 유스케이스의 출력을 HTTP로 매핑
7. HTTP 응답을 반환

웹 어댑터는 URL, 경로, HTTP method, content type과 같이 특정 기준을 만족하는 HTTP 요청을 수신한다. 이 후 HTTP 요청의 파라미터와 콘텐츠를 객체로 역직렬화해야 한다. 보통은 웹 어댑터가 인증과 권한 부여를 수행하고 실패할 경우 에러를 반환한다. 이후 들어오는 객체의 상태 유효성 검증을 한다. 유스케이스 입력 모델에서 하는 유효성 검사와는 다른 이야기다. 웹 어댑터의 입력 모델을 유스케이스의 입력 모델로 변환할 수 있음을 검증한다. 이후 유스케이스를 호출한 뒤, 반환받은 출력을 HTTP 응답으로 직렬화해서 호출자에게 전달한다.

이 과정에서 한 군데서라도 문제가 생기면 예외를 던지고, 웹 어댑터는 에러를 호출자에게 보여줄 메시지로 변환해야 한다.

웹 어댑터에 책임이 상당히 많지만, HTTP와 관련된 것은 애플리케이션 계층으로 침투해서는 안 된다. HTTP를 사용하지 않는 또 다른 인커밍 어댑터라는 선택의 여지를 남기는 것이 좋은 아키텍처다.

## 컨트롤러 나누기
웹 어댑터는 한 개 이상의 클래스로 구성해도 된다. 너무 적은 것보다는 너무 많은게 낫다. 각 컨트롤러가 가능한 한 좁고 다른 컨트롤러와 가능한 한 적게 공유하는 웹 어댑터 조각을 구현해야 한다.

```java
package buckpal.adapter.web;

import buckpal.application.port.in.SendMoneyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final GetAccountBalanceQuery getAccountBalanceQuery;
    private final ListAccountsQuery listAccountsQuery;
    private final LoadAccountQuery loadAccountQuery;

    private final SendMoneyUseCase sendMoneyUseCase;
    private final CreateAccountUseCase createAccountUseCase;

    @GetMapping("/accounts")
    List<AccountResource> listAccounts() {
        //...
    }

    @GetMapping("/accounts/{accountId}")
    AccountResource getAccount(@PathVariable("accountId") Long accountId) {
        //...
    }

    @GetMapping("/accounts/{accountId}/balance")
    long getAccountBalance(@PathVariable("accountId") Long accountId) {
        //...
    }

    @PostMapping("/accounts")
    AccountResource createAccount(@RequestBody AccountResource accountResource) {
        //...
    }

    @PostMapping("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}")
    void sendMoney(
            @PathVariable("sourceAccountId") Long sourceAccountId,
            @PathVariable("targetAccountId") Long targetAccountId,
            @PathVariable("amount") Long amount
    ) {
        //...
    }
}
```

위 예제를 살펴보면 모든 내용이 하나의 컨트롤러에 모여 있으며 괜찮아 보인다. 하지만 단점이 있다.

- 클래스마다 코드는 적을수록 좋다. 클래스에 코드의 양이 많다면, 기존 코드에서 추가된 코드에 대한 내용을 파악하기 어려워진다.
- 테스트 코드도 마찬가지다. 코드가 길어지면 테스트 코드를 찾는데 어려움이 있다.
- 모든 연산을 단일 컨트롤러에 넣는 것이 데이터 구조의 재활용을 촉진한다. 이는 필요없는 필드가 들어있어 응답이 명확하지 않아 요청하는 입장에서 헷갈릴 수 있다.

```java
package buckpal.adapter.web;

import buckpal.application.port.in.SendMoneyCommand;
import buckpal.application.port.in.SendMoneyUseCase;
import buckpal.domain.Account.AccountId;
import buckpal.domain.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SendMoneyController {

	private final SendMoneyUseCase sendMoneyUseCase;

	@PostMapping("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}")
	void sendMoney(
			@PathVariable("sourceAccountId") Long sourceAccountId,
			@PathVariable("targetAccountId") Long targetAccountId,
			@PathVariable("amount") Long amount) {

		SendMoneyCommand command = new SendMoneyCommand(
				new AccountId(sourceAccountId),
				new AccountId(targetAccountId),
				Money.of(amount));

		sendMoneyUseCase.sendMoney(command);
	}
}
```

저자는 각 연산에 대해 가급적이면 별도의 패키지 안에 별도의 컨트롤러를 만드는 방식을 선호한다고 한다. 또한 가급적 메서드와 클래스명은 유스케이스를 최대한 반영해서 지어야한다고 한다.

위와 같이 변경할 경우, 각 컨트롤러가 `CreateAccountResouce`나 `UpdateAccountResource` 같은 컨트롤러 자체의 모델을 가지고 있거나, 앞 예제 코드처럼 원시값을 받아도 된다. 이러한 전용 모델 클래스들은 컨트롤러의 패키지에 대해 `private`으로 선언할 수 있기 때문에 실수로 다른 곳에서 재사용될 일이 없다.  
컨트롤러끼리는 모델을 공유할 수 있지만 다른 패키지에 있는 덕분에 공유해서 사용하기 전에 다시 한번 생각해볼 수 있고, 다시 생각해봤을 때, 필드의 절반은 사실 필요없다는 걸 깨달아서 결국 컨트롤러에 맞는 모델을 새로 만들게 될 확률이 높다.

이렇게 나누면 서로 다른 연산에 대한 동시 작업이 쉬워진다. 여러 개발자가 서로 다른 연산에 대한 코드를 짜고 있다면 병합 충돌이 일어나지 않을 것이다.

## 유지보수 가능한 소프트웨어를 만드는 데 어떻게 도움이 될까?
웹 어댑터를 구현할 때는 HTTP 요청을 애플리케이션의 유스케이스 메서드 호출로 변환하고 결과를 다시 HTTP로 변환하고 어떤 도메인 로직도 수행하지 않는 어댑터를 만들고 있다는 점을 염두에 둬야 한다.

반면, 애플리케이션 계층은 HTTP에 대한 상세 정보를 노출시키지 않도록 HTTP와 관련된 작업을 해서는 안 된다. 이렇게 하면 필요할 경우 웹 어댑터를 다른 어댑터로 쉽게 교체할 수 있다.

웹 컨트롤러를 나눌 때는 모델을 공유하지 않는 여러 작은 클래스로 만들어야 한다. 이는 파악이 쉽고, 테스트가 쉬우며, 동시 작업을 할 경우 형상관리도구에서 충돌이 일어나지 않도록 해준다. 기존의 방법보단 코드의 양과 공수가 더 들겠지만 유지보수할때 클린아키텍처의 빛을 발할 것이라 저자는 말한다.