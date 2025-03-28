# 1장. 객체, 설계
## 소프트웨어 모듈이 가져야 하는 세 가지 기능
로버트 마틴이 '클린 소프트웨어' 에서 말하는 소프트웨어 모듈이 가져야 하는 세 가지 기능
- 실행 중에 제대로 동작하는 것. 이는 모든 모듈의 존재 이유이다.
- 변경을 위해 존재하는 것. 대부분의 모듈은 생명주기 동안 변경되기 때문에 간단한 작업만으로도 변경이 가능해야 한다. 변경하기 어려운 모듈은 제대로 동작하더라도 개선해야 한다.
- 코드를 읽는 사람과 의사소통하는 것. 모듈은 특별한 훈련 없이도 개발자가 쉽게 읽고 이해할 수 있어야 한다. 읽는 사람과 의사소통 할 수 없는 모듈은 개선해야 한다.

## 변경에 취약한 코드
`src/main/.../before`의 `Theater` 처럼 다른 클래스가 `Audience`의 내부에 대해 더 많이 알면 알수록 `Audience`를 변경하기 어려워진다.

이것은 **객체 사이의 의존성(dependency)과 관련된 문제다.** 문제는 의존성이 변경과 관련돼 있다는 점이다. 의존성은 변경에 대한 영향을 암시한다. 의존성이라는 말 속에는 어떤 객체가 변경될 때 그 객체에게 의존하는 다른 객체도 함께 변경될 수 있다는 사실이 내포돼 있다.

객체 사이의 의존성을 완전히 없애는 것은 말도 안되는 이야기이다. 객체지향 설계는 서로 의존하면서 협력하는 객체들의 공동체를 구축하는 것이다. 따라서 애플리케이션의 기능을 구현하는 데 최소한의 의존성만 유지하고 불필요한 의존성을 제거하는 것을 목표로 하자.

**객체 사이의 의존성이 과한 경우를 가리켜 결합도(coupling)가 높다고 말한다.** 반대로 객체들이 합리적인 수준으로 의존할 경우에는 결합도가 낮다고 말한다. 결합도는 의존성과 관련돼 있기 때문에 결합도 역시 변경과 관련이 있다. 두 객체 사이의 결합도가 높으면 높을수록 함께 변경될 확률도 높아지기 때문에 변경하기 어려워진다.  
**따라서 설계의 목표는 객체 사이의 결합도를 낮춰 변경이 용이한 설계를 만드는 것이어야 한다.**

## 자율성을 높이자
`Theater`가 `Audience`와 `TicketSeller`에 관해 너무 세세한 부분까지 알지 못하도록 정보를 차단하자. 다시 말해 관람객과 판매원을 **자율적인 존재로 만들자.**

개념적으로나 물리적으로 객체 내부의 세부적인 사항을 감추는 것을 **캡슐화(encapsulation)** 라고 부른다. **캡슐화의 목적은 변경하기 쉬운 객체를 만드는 것**이다. 캡슐화를 통해 객체 내부로의 접근을 제한하면 객체와 객체 사이의 결합도를 낮출 수 있기 때문에 설계를 좀 더 쉽게 변경할 수 있게 된다.

`src/main/.../after`의 `Theater`는 오직 `TicketSeller`의 인터페이스(interface)에만 의존한다. `TicketSeller`가 내부에 `TicketOffice` 인스턴스를 포함하고 있다는 사실은 구현(implementation)의 영역에 속한다.

**객체를 인터페이스와 구현으로 나누고 인터페이스만을 공개하는 것은 객체 사이의 결합도를 낮추고 변경하기 쉬운 코드를 작성하기 위해 따라야 하는 가장 기본적인 설계 원칙이다.**

## 캡슐화와 응집도
핵심은 객체 내부의 상태를 캡슐화하고 객체 간에 오직 메시지를 통해서만 상호작용하도록 만드는 것이다. `Theater`는 `TicketSeller`의 내부에 대해서는 전혀 알지 못한다. 단지 `TicketSeller`가 `sellTo` 메시지를 이해하고 응답할 수 있다는 사실만 알고 있을 뿐이다. `TicketSeller` 역시 `Audience`의 내부에 대해 전혀 알지 못한다. 단지 `Audience`가 `buy` 메시지에 응답할 수 있고 자신이 원하는 결과를 반환할 것이라는 사실만 알고 있을 뿐이다.

**밀접하게 연관된 작업만을 수행하고 연관성 없는 작업은 다른 객체에게 위임하는 객체를 가리켜 응집도(cohesion)가 높다고 말한다.** 자신의 데이터를 스스로 처리하는 자율적인 객체를 만들면 결합도를 낮출 수 있을뿐더러 응집도를 높일 수 있다.

객체의 응집도를 높이기 위해서는 객체 스스로 자신의 데이터를 책임져야 한다. 외부의 간섭을 최대한 배제하고 메시지를 통해서만 협력하는 자율적인 객체들의 공동체를 만드는 것이 훌륭한 객체지향 설계를 얻을 수 있는 지름길인 것이다.

## 객체지향 설계
우리가 진정으로 원하는 것은 변경에 유연하게 대응할 수 있는 코드다.

객체지향 프로그래밍은 의존성을 효율적으로 통제할 수 있는 다양한 방법을 제공함으로써 요구사항 변경에 좀 더 수월하게 대응할 수 있는 가능성을 높여준다.

객체지향 패러다임은 우리가 세상을 바라보는 방식대로 코드를 작성할 수 있게 돕는다. 세상에 존재하는 모든 자율적인 존재처럼 객체 역시 자신의 데이터를 스스로 책임지는 자율적인 존재다. 객체지향은 우리가 세상에 대해 예상하는 방식대로 객체가 행동하리라는 것을 보장함으로써 코드를 좀 더 쉽게 이해할 수 있게 한다.

객체지향의 세계에서 애플리케이션은 객체들로 구성되며 애플리케이션의 기능은 객체들 간의 상호작용을 통해 구현된다. 그리고 객체들 사이의 상호작용은 객체 사이에 주고 받는 메시지로 표현된다.

메시지를 전송하기 위한 이런 지식이 두 객체를 결합시키고 이 결합이 객체 사이의 의존성을 만든다.

훌륭한 객체지향 설계란 협력하는 객체 사이의 의존성을 적절하게 관리하는 설계다. 세상에 엮인 것이 많은 사람일수록 변하기 어려운 것처럼 객체가 실행되는 주변 환경에 강하게 결합될수록 변경하기 어려워진다. 객체 간의 의존성은 애플리케이션을 수정하기 어렵게 만드는 주범이다.

데이터와 프로세스를 하나의 덩어리로 모으는 것은 훌륭한 객체지향 설계로 가는 첫걸음 뿐이다. 진정한 객체지향 설계로 나아가는 길은 협력하는 객체들 사이의 의존성을 적절하게 조절함으로써 변경에 용이한 설계를 만드는 것이다.