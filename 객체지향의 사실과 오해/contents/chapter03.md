# 3. 타입과 추상화

## 추상화를 통한 복잡성 극복

이해하기 쉽고 예측 가능한 수준으로 현실을 분해하고 단순화하는 전략.

- 추상화: 복잡한 현실 → 단순화(의도적 생략, 감춤)

객체지향 패러다임은 객체라는 추상화를 통해 현실의 복잡성을 극복한다.

## 객체지향과 추상화

### 개념

- 개념(concept): 공통점을 기반으로 객체들을 묶기 위한 그릇
- 분류(classification): 개념을 이용해서 여러 그룹으로 **분류** 가능
- 인스턴스(instance): 객체가 개념에 분류되면 객체는 그 개념의 **인스턴스**이다.

추상화: 객체 → 몇 가지 개념의 인스턴스

📌 **객체란 특정한 개념을 적용할 수 있는 구체적인 사물을 의미한다. 개념이 객체에 적용됐을 때 객체를 개념의 인스턴스라고 한다.**

### 개념의 세 가지 관점

- 심볼(symbol): 개념을 가리키는 간략한 이름이나 명칭
- 내연(intension): 개념의 완전한 정의. 내연으로 객체가 개념에 속하는지 여부 확인
- 외연(extension): 개념에 속하는 모든 객체의 집합

### 객체를 분류하기 위한 틀

분류란 객체에 특정한 개념을 적용하는 작업이다. 객체에 특정한 개념을 적용하기로 결심했을 때 우리는 그 객체를 특정한 집합의 멤버로 분류하고 있는 것이다.

개념 분류 → 객체 지향 품질 결정 : 유지 보수 good

// 분류는 추상화를 위한 도구이다.

## 타입

### 타입은 개념이다

개념(concept) = 타입(type) → 공통점 기반으로 객체 묶기

### 데이터 타입

- 타입 시스템(type system): 0, 1 혼란 방지
  > - 데이터가 어떻게 사용되는가 → 자료구조: 데이터 타입 + 연산자
  > - 타입에 속한 데이터를 메모리에 어떻게 표현하는지 외부로부터 철저히 감춤: 저장 방식 몰라도 Integer 형 데이터 사용 가능

📌 데이터 타입은 메모리 안에 저장된 데이터의 종류를 분류하는 데 사용하는 메모리 집합에 관한 메타데이터다. 데이터에 대한 분류는 암시적으로 어떤 종류의 연산이 해당 데이터에 대해 수행될 수 있는지를 결정한다.

### 객체와 타입

객체 != 데이터 → 객체의 행동이 중요하다!

- 어떤 객체가 어떤 타입에 속하는지 결정하는 것은 객체가 수행하는 행동이다.
- 객체의 내부적인 표현은 외부로부터 철저히 감추어진다.

### 행동이 우선이다.

객체 타입 결정: 객체의 행동(only)

동일한 행동 = 동일한 책임 = 동일한 메시지 수신

같은 타입인 객체 = 행동이 동일하면 서로 다른 데이터를 가질 수 있다. // 데이터가 달라도 동일 메시지 수신 가능

캡슐화: 외부에 행동만 제공하고 데이터는 감춘다. (행동으로 객체를 분류하자)

데이터 주도 설계(Data Driven Design) → 책임 주도 설계(Responsibility Driven Design)

## 타입의 계층

### 일반화 / 특수화

- 일반적 타입: 더 적은 수 행동 = 슈퍼타입(Supertype)
- 특수한 타입: 더 많은 행동 = 서브타입(Subtype)

일반화도 결국 개념과 같이 추상화를 위한 도구이다.

## 정적 모델

### 타입의 목적

타입을 사용하는 이유? → 인간의 인지 능력으로는 시간에 따라 동적으로 변하는 객체의 복잡성을 극복하기가 너무 어렵다. 객체의 상태를 시간과 무관한 정적인 모습으로 다룰 수 있게 해준다.

타입을 이용하면 객체의 동적인 특성을 추상화 할 수 있다.

### 동적 모델과 정적 모델

객체지향 애플리케이션을 설계하고 구현하기 위해서는 객체 관점의 동적 모델(dynamic model)과 객체를 추상화한 타입 관점의 정적 모델(static model)을 적절히 혼용해야 한다. 동적 모델과 정적 모델의 구분은 실제로 프로그래밍이라는 행위와도 관련이 깊다. 객체지향 프로그래밍 언어를 이용해 클래스를 작성하는 시점에는 시스템을 정적인 관점에서 접근하는 것이다. 반면 실제로 애플리케이션을 실행해 객체의 상태 변경을 추적하고 디버깅하는 동안에는 객체의 동적인 모델을 탐험하고 있는 것이다.

### 클래스

클래스와 타입은 다르다. 클래스가 더 상위 개념이며 객체지향에서 결국 중요한 것은 동적으로 변하는 **객체의 상태와 상태를 변경하는 행위이다.**
