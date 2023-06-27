# 9장. 애플리케이션 조립하기
애플리케이션이 시작될 때 클래스를 인스턴스화하고 묶기 위해서 의존성 주입 매커니즘을 이용한다. 평범한 Java, Spring, Spring Boot에서 각각 어떻게 하는지 살펴보자.

## 왜 조립까지 신경 써야 할까?
- 코드 의존성이 올바른 방향을 가리키게 하기 위해서
- 모든 의존성은 안쪽으로, 애플리케이션의 도메인 코드 방향으로 향해야 도메인 코드가 바깥 계층으로부터 안전하다.
- 유스케이스가 영속성 어댑터를 호출하는 경우를 맊기 위해 아웃고잉 포트 인터페이스를 생성한다.
- 유스케이스는 인터페이스만 알아야 하고, 런타임에 이 인터페이스의 구현을 제공 받아야 한다.

이 프로그래밍 스타일은 코드를 테스트하기 훨씬 쉽다. 한 클래스가 필요로 하는 모든 객체를 생성자로 전달할 수 있다면 실제 객체 대신 mock으로 전달할 수 있고, 이렇게 되면 격리된 단위 테스트를 생성하기 쉬워진다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/aeb4274a-c798-4071-97de-a1edd6559dd0)

아키텍처에 대해 중립적이고 인스턴스 생성을 위해 모든 클래스에 대한 의존성을 가지는 설정 컴포넌트(Configuration Component)가 필요하다.

설정 컴포넌트는 애플리케이션을 조립하는 것을 책임진다. 이 컴포넌트는 다음과 같은 역할을 수행해야 한다.
- 웹 어댑터 인스턴스 생성
- HTTP 요청이 실제로 웹 어댑터로 전달되도록 보장
- 유스케이스 인스턴스 생성
- 웹 어댑터에 유스케이스 인스턴스 제공
- 영속성 어댑터 인스턴스 생성
- 유스케이스에 영속성 어댑터 인스턴스 제공
- 영속성 어댑터가 실제로 데이터베이스에 접근할 수 있도록 보장

더불어 설정 파일이나 커맨드라인 파라미터 등과 같은 설정 파라미터의 소스에도 접근할 수 있어야 한다. 이러한 파라미터를 애플리케이션 컴포넌트에 제공해서 어떤 데이터베이스에 접근하고 어떤 서버를 메일 전송에 사용할지 등의 행동 양식을 제어한다.

책임이 굉장히 많아서 단일 책임 원칙을 위반하지만, 애플리케이션의 나머지 부분을 깔끔하게 유지하고 싶다면 이처럼 구성요소들을 연결하는 바깥쪽 컴포넌트가 필요하다. 이 컴포넌트는, 작동하는 애플리케이션으로 조립하기 위해 애플리케이션을 구성하는 모든 움직이는 부품을 알아야 한다.

## 평범한 코드로 조립하기
의존성 주입 프레임워크의 도움 없이 애플리케이션을 만들고 있다면 평범한 코드로 아래와 같이 컴포넌트를 만들 수 있다.

```java
package copyeditor.configuration;

class Application {
    public static void main(String[] args) {
        AccountRepository accountRepository = new AccountRepository();
        ActivityRepository activityRepository = new ActivityRepository();

        AccountPersistenceAdapter accountPersistenceAdapter = 
                        new AccountPersistenceAdapter(accountRepository, activityRepository);
        
        SendMoneyUseCase sendMoneyUseCase = new SendMoneyUseService(
                accountPersistenceAdapter, // LoadAccountPort
                accountPersistenceAdapter); // UpdateAccountStatePort
        
        SendMoneyController sendMoneyController = new SendMoneyController(sendMoneyUseCase);
        
        startProcessingWebRequests(sendMoneyController);
    }
}
```

의존성이 있는 모든 인스턴스를 생성 후 `startProcessingWebRequests()`를 호출하여 실행한다. 이 평범한 코드 방식은 애플리케이션을 조립하는 가장 기본적인 방법이다. 하지만 다음과 같은 단점들이 있다.

- 예제 코드는 웹 컨트롤러, 유스케이스, 영속성 어댑터가 단 하나씩만 있는 애플리케이션을 예로 들었다. 완전한 엔터프라이즈 애플리케이션을 실행하기 위해선 이러한 코드가 굉장히 많아진다.
- 각 클래스가 속한 패키지 외부에서 인스턴스를 생성하기 때문에 이 클래스들은 전부 `public`이어야 한다. 원치 않는 의존성을 피하기 위해 `package-private`을 사용하면 더 좋았을 것이다.

`package-private` 의존성을 유지하면서 지저분한 작업을 대신해줄 수 있는 의존성 프레임워크들이 있다. Java에선 Spring Framework가 가장 인기 있다. 웹과 데이터베이스 환경을 지원하기에 `startProcessingWebRequests()` 메서드 같은 것을 구현할 필요가 없다.

## 스프링의 클래스패스 스캐닝으로 조립하기
Spring Framework를 이용해서 애플리케이션을 조립한 결과물을 애플리케이션 컨텍스트(application context)라고 한다. 애플리케이션 컨텍스트는 애플리케이션을 구성하는 모든 객체(bean)를 포함한다.

> 📌 클래스패스 스캐닝: 클래스패스에서 접근 가능한 모든 클래스를 확인해서 `@Component` 애너테이션이 붙은 클래스를 찾는다. 이때 클래스는 필요한 모든 필드를 인자로 받는 생성자를 가지고 있어야 한다.

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

flow는 다음과 같다.

1. 스프링에선 생성자에 인자로 사용된 `@Component`가 붙은 클래스들을 찾는다.
2. 이 클래스들의 인스턴스를 만들어 애플리케이션 컨텍스트에 추가한다.
3. 필요한 객체가 모두 생성되면 `AccountPersistenceAdapter`의 생성자를 호출한다.
4. 3에서 생성된 객체도 마찬가지로 애플리케이션 컨텍스트에 추가한다.

클래스패스 스캐닝 방식을 이용하면 아주 편리하게 애플리케이션을 조립할 수 있다.

또한 스프링이 인식할 수 있는 애너테이션을 직접 만들 수도 있다.

```java
package buckpal.common;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface PersistenceAdapter {
    @AliasFor(annotation = Component.class)
    String value() default "";
}
```

이 애너테이션은 `@Component`를 포함하고 있어서 스프링이 클래스패스 스캐닝을 할 때 인스턴스를 생성할 수 있도록 한다. `@Component` 대신 `@PersistenceAdapter`를 이용해 영속성 어댑터 클래스들이 애플리케이션의 일부임을 표시할 수 있다. `@PersistenceAdapter` 덕에 코드를 더 쉽게 파악할 수 있다.

하지만 이 방식에도 단점이 있다.

- 클래스에 프레임워크에 특화된 애너테이션을 붙어야 한다는 점에서 침투적이다.
  - 강경한 클린 아키텍처파는 이런 방식이 코드를 특정한 프레임워크와 결합시키기 때문에 사용하지 말아야 한다고 주장한다.
- 다른 개발자들이 사용할 라이브러리나 프레임워크를 만드는 입장에선 사용하지 말아야 할 방법이다.
  - 라이브러리 사용자가 스프링 프레임워크의 의존성에 엮이게 된다.
- 마법이 일어난다.
  - 스프링 전문가가 아니라면 원인을 찾는 데 수일이 걸릴 수 있는 숨겨진 부수효과를 야기할 수도 있다.
  - 단순하게 상위 패키지명, `@Component` 애너테이션을 이용해서 스캔을 하기 때문에 실제로 애플리케이션 컨텍스트에 올라가지 않았으면 하는 클래스가 악의적으로 조작해(의도치 않은 작용?)으로 추적이 어려운 에러를 일으킬 수도 있다.

## 스프링의 자바 컨피그로 조립하기
이 방식에선 애플리케이션 컨텍스트에 추가할 빈을 생성하는 설정 클래스를 만든다.

```java
package buckpal.common;

import buckpal.adapter.persistence.AccountMapper;
import buckpal.adapter.persistence.AccountPersistenceAdapter;
import buckpal.adapter.persistence.AccountRepository;
import buckpal.adapter.persistence.ActivityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories
class PersistenceAdapterConfiguration {

    @Bean
    AccountPersistenceAdapter accountPersistenceAdapter(
            AccountRepository accountRepository,
            ActivityRepository activityRepository,
            AccountMapper accountMapper
    ) {
        return new AccountPersistenceAdapter(
                accountRepository,
                activityRepository,
                accountMapper
        );
    }

    @Bean
    AccountMapper accountMapper() {
        return new AccountMapper();
    }
}
```

- `@Configuration` 애너테이션을 통해 이 클래스가 스프링 클래스패스 스캐닝에서 발견해야 할 설정 클래스임을 표시해둔다.
- 여전히 클래스패스 스캐닝을 사용하고 있는 것이기는 하다.
- 모든 빈을 가져오는 대신 설정 클래스만 선택하기 때문에 해로운 마법이 일어날 확률이 줄어든다.

빈 자체는 설정 클래스 내의 `@Bean` 애너테이션이 붙은 팩터리 메서드를 통해 생성된다.

스프링은 이 리포지토리 객체들을 어디서 가져올까? 다른 설정 클래스의 팩터리 메서드에서 수동으로 생성됐다면, 스프링이 자동으로 팩터리 메서드의 파라미터로 제공할 것이다. 하지만 위 예제에선 `@EnabledJpaRepositories` 애너테이션으로 인해 스프링이 직접 생성해서 제공한다. 스프링 부트가 이 애너테이션을 발견하면 자동으로 우리가 정의한 모든 스프링 데이터 리포지토리 인터페이스의 구현체를 제공할 것이다.

메인 애플리케이션에도 `@EnabledJpaRepositories`를 붙일 수도 있다. 하지만 애플리케이션을 시작할 때마다 JPA를 활성화해서 영속성이 실질적으로 필요 없는 테스트에서 애플리케이션을 실행할 때도 JPA 리포지토리들을 활성화할 것이다. 따라서 이러한 '기능 애너테이션'을 별도의 설정 모듈로 옮기는 편이 애플리케이션을 더 유연하게 만들고, 항상 모든 것을 한꺼번에 시작할 필요 없게 해준다.

비슷한 방법으로 웹 어댑터, 혹은 애플리케이션 계층의 특정 모듈을 위한 설정 클래스를 만들 수도 있다. 그러면 특정 모듈만 포함하고, 그 외의 다른 모듈의 빈은 모킹해서 애플리케이션 컨텍스트를 만들 수 있다. 이는 테스트에 큰 유연성이 생긴다.

또한 이 방식에서는 클래스패스 스캐닝 방식과 달리 `@Component` 애너테이션을 코드 여기저기에 붙이도록 강제하지 않는다. 그래서 애플리케이션 계층을 프레임워크에 대한 의존성 없이 깔끔하게 유지할 수 있다.

이 방법에도 문제점은 있다. 설정 클래스가 생성하는 빈이 설정 클래스와 같은 패키지에 존재하지 않는다면 이 빈들을 `public`으로 만들어야 한다. 가시성을 제한하기 위해 패키지를 모듈의 경계로 사용하고 각 패키지 안에 전용 설정 클래스를 만들 수는 있다. 하지만 이러면 하위 패키지를 사용할 수 없다.

## 유지보수 가능한 소프트웨어를 만드는 데 어떻게 도움이 될까?
프레임워크는 개발을 편하게 만들어주는 다양한 기능들을 제공한다. 그중 하나가 바로 애플리케이션 개발자로서 우리가 제공하는 부품들을 이용해서 애플리케이션을 조립하는 것이다.

클래스패스 스캐닝은 다음과 같은 특징이 있었다.
- 스프링에게 패키지만 알려주면 거기서 찾은 클래스로 애플리케이션을 조립한다.
- 전체를 고민하지 않고도 빠르게 개발할 수 있다.
- 코드의 규모가 커지면 금방 투명성이 낮아진다. 어떤 빈이 애플리케이션 컨텍스트에 올라오는지 정확히 알 수 없게 된다.
- 테스트에서 애플리케이션 컨텍스트의 일부만 독립적으로 띄우기가 어려워진다.

반면, 애플리케이션 조립을 책임지는 전용 설정 컴포넌트를 만들면 애플리케이션이 이러한 책임으로부터 자유로워진다. 이 방식을 이용하면 서로 다른 모듈로부터 독립되어 코드 상에서 손쉽게 옮겨 다닐 수 있는 응집도가 매우 높은 모듈을 만들 수 있다. 하지만 설정 컴포넌트를 유지보수하는 데 약간의 시간을 추가로 들여야 한다.