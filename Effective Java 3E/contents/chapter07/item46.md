# 아이템 46. 스트림에서는 부작용 없는 함수를 사용하라

스트림은 함수형 프로그래밍에 기초한 패러다임이다.  
**스트림 패러다임의 핵심은 계산을 일련의 변환(transformation)으로 재구성하는 부분이다. 이때 각 변환 단계는 가능한 한 이전 단계의 결과를 받아 처리하는 순수 함수여야 한다.**

순수 함수란 오직 입력만이 결과에 영향을 주는 함수를 말한다. 다른 가변 상태를 참조하지 않고, 함수 스스로도 다른 상태를 변경하지 않는다.

## 다양한 스트림 연산

### forEach

forEach 연산은 종단 연산 중 기능이 가장 적고 가장 '덜' 스트림답다. 대놓고 반복적이라 병렬화할 수도 없다.  
**forEach 연산은 스트림 계산 결과를 보고할 때만 사용하고, 계산하는 데는 쓰지 말자.**

### Collector

`java.util.stream.Collectors`가 제공하는 수집기를 잘 활용하자.

- 축소(reduction) 전략을 캡슐화한 블랙박스 객체
- 축소: 스트림의 원소들을 객체 하나에 취합한다는 뜻

자세한 예제 코드는 다음 링크를 참고하도록 하자.  
[자바봄 이펙티브 자바 - 아이템 45, 46](https://javabom.tistory.com/58?category=833277)

## 핵심 정리

- 스트림 파이프라인 프로그래밍의 핵심은 부작용 없는 함수 객체에 있다.
- 스트림뿐 아니라 스트림 관련 객체에 건네지는 모든 하수 객체가 부작용이 없어야 한다.
- 종단 연산 중 `forEach`는 스트림이 수행한 계산 결과를 보고할 때만 이용해야 한다.
- 계산 자체에는 이용하지 말자.
- 스트림을 올바로 사용하려면 수집기를 잘 알아둬야 한다.
- 가장 중요한 수집기 팩터리는 `toList`, `toSet`, `toMap`, `groupingBy`, `joining`이다.