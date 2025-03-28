# 아이템 6. 편집기를 사용하여 타입 시스템 탐색하기
TypeScript를 설치하면, 다음 두 가지를 실행할 수 있다.

- TypeScript 컴파일러 (tsc)
- 단독으로 실행할 수 있는 TypeScript 서버 (tsserver)

보통은 tsc를 실행하는 것이 주된 목적이지만, tsserver 또한 언어 서비스를 제공한다는 점에서 중요하다. 언어 서비스에는 코드 자동 완성, 명세 검사, 검색, 리팩터링이 포함된다.

보통은 편집기를 통해 언어 서비스를 사용하는데, TypeScript 서버에서 언어 서비스를 제공하도록 설정하는 게 좋다. 유용한 기능이니 꼭 사용하자.

실제 코드에서 함수 호출이 길게 이어진다면 추론 정보는 디버깅하는 데 꼭 필요하다. 편집기사으이 타입 오류를 살펴보는 것도 타입 시스템의 성향을 파악하는 데 좋은 방법이다. 타입 선언은 TypeScript가 무엇을 하는지, 어떻게 라이브러리가 모델링되었는지, 어떻게 오류를 찾아낼지 살펴볼 수 있는 훌륭한 수단이라는 것을 알 수 있다.

## 요약
- 편집기에서 TypeScript 언어 서비스를 적극 활용해야 한다.
- 편집기를 사용한다면 어떻게 타입 시스템이 동작하는지, 그리고 TypeScript가 어떻게 타입을 추론하는지 개념을 잡을 수 있다.
- TypeScript가 동작을 어떻게 모델링하는지 알기 위해 타입 선언 파일을 찾아보는 방법을 터득해야 한다.