# 아이템 1. 타입스크립트와 자바스크립트의 관계 이해하기
TypeScript는 JavaScript의 상위 집합이다. JS 프로그램에 문법 오류가 없다면, 유효한 TS 프로그램이라고 할 수 있다. 그런데 JS 프로그램에 어떤 이슈가 존재한다면 문법 오류가 아니라도 타입 체커에게 지적당할 가능성이 높다. 문법의 유효성과 동작의 이슈는 독립적인 문제다.

JavaScript 파일이 `.js`, `.jsx` 확장자를 사용하는 반면, TypeScript 파일은 `.ts`, `.tsx` 확장자를 사용한다. 그렇다고 JavaScript와 TypeScript는 완전히 다른 언어는 아니다.

TS 컴파일러는 TypeScript 뿐만 아니라 일반 JavaScript 프로그램에도 유용하다.

```javascript
let city = 'new york city';
console.log(city.toUppercase());
```

이 코드를 실행하면 다음과 같은 오류가 발생한다.

```typescript
// TypeError: city.toUppercase is not a function
```

앞의 코드에는 타입 구문이 없지만, TypeScript의 타입 체커는 문제점을 찾아낸다.

```typescript
let city = 'new york city';
console.log(city.toUppercase());
// 'toUppercase' 속성이 'string' 형식에 없습니다.
// 'toUpperCase'를 사용하시겠습니까?
```

`city` 변수가 문자열이라는 것을 알려 주지 않아도 TS는 초깃값으로부터 타입을 추론한다.

**타입 시스템의 목표 중 하나는 런타임에 오류를 발생시킬 코드를 미리 찾아내는 것이다.** TypeScript가 '정적'타입 시스템이라는 것은 바로 이런 특징을 말하는 것이다. 그러나 타입 체커가 모든 오류를 찾아내지는 않는다.

```javascript
const states = [
  {name: 'Alabama', capital: 'Montgomery'},
  {name: 'Alaska', capital: 'Juneau'},
  {name: 'Arizona', capital: 'Phoenix'},
  // ...
];

for (const state of states) {
  console.log(state.capitol);
}

/**
  undefined
  undefined
  undefined
 */
```

위 코드는 유효한 JavaScript(또한 TypeScript)이며 어떠한 오류도 없이 실행된다. 그러나 루프 내의 `state.capitol`은 의도한 코드가 아닌 게 분명하다. 이런 경우에 TS의 타입 체커는 추가적인 타입 구문 없이도 오류를 찾아낸다.

```typescript
for (const state of states) {
  console.log(state.capitol);
  // 'capitol' 속성이 ... 형식에 없습니다.
  // 'capital'을 사용하시겠습니까?
}
```

TypeScript는 타입 구문 없이도 오류를 잡을 수 있지만, 타입 구문을 추가한다면 훨씬 더 많은 오류를 찾아낼 수 있다. 코드의 '의도'가 무엇인지 타입 구문을 통해 TypeScript에게 알려줄 수 있기 때문에 코드의 동작과 의도가 다른 부분을 찾을 수 있다.

```typescript
interface State {
  name: string;
  capital: string;
}

const states: State[] = [
  {name: 'Alabama', capitol: 'Montgomery'},
  {name: 'Alaska', capitol: 'Juneau'},
  {name: 'Arizona', capitol: 'Phoenix'},
  // 개체 리터럴은 알려진 속성만 지정할 수 있지만, 'State' 형식에 'capitol'이 없습니다.
  //  'capital'을 쓰려고 했습니까?
  // ...
];

for (const state of states) {
  console.log(state.capital);
}
```

위처럼 interface를 미리 정의해둠으로써 TypeScript가 작성자의 의도를 정확히 파악할 수 있도록 했다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9be99ac2-8f86-4a59-acf4-e517768d7452)

TypeScript 타입 시스템은 JavaScript의 런타임 동작을 '모델링'한다. 런타임 체크를 엄격하게 하는 언어를 사용해 왔다면 다음 결과들이 꽤 당황스럽게 느껴질 수 있다.

```javascript
const x = 2 + '3'; // 정상, string 타입
const y = '2' + 3; // 정상, string 타입
```

위 예제는 다른 언어였다면 런타임 오류가 될 만한 코드다. 하지만 TypeScript의 타입 체커는 정상으로 인식한다.

반대로 정상 동작하는 코드에 오류를 표시하기도 한다. 다음은 런타임 오류가 발생하지 않는 코드인데, 타입 체커는 문제점을 표시한다.

```typescript
const a = null + 7; // a는 7이 됨.
const b = [] + 12; // b는 12가 됨.
alert('Hello', 'TypeScript'); // Hello 경고 표시함.
```

JavaScript의 런타임 동작을 모델링하는 것은 TypeScript의 타입 시스템의 기본 원칙이다. 그러나 위 경우들처럼 단순히 런타임 동작을 모델링하는 것뿐만 아니라 의도치 않은 이상한 코드가 오류로 이어질 수 있다는 점까지 고려해야 한다.

## 요약
- TypeScript는 JavaScript의 상위집합이다. 다시 말해, 모든 JS 프로그램은 이미 TS 프로그램이다. 반대로, TypeScript는 별도의 문법을 가지고 있기 때문에 일반적으론 유효한 JavaScript 프로그램이 아니다.
- TypeScript는 JavaScript 런타임 동작을 모델링하는 타입 시스템을 가지고 있기 때문에 런타임 오류를 발생시키는 코드를 찾아내려 한다. 그러나 모든 오류를 찾아내리라 기대하면 안 된다. 타입 체커를 통과하면서도 런타임 오류를 발생시키는 코드는 충분히 존재할 수 있다.
- TypeScript 타입 시스템은 전반적으로 JavaScript 동작을 모델링한다. 그러나 잘못된 매개변수 개수로 함수를 호출하는 경우처럼, JavaScript에선 허용되지만 TypeScript에선 문제가 되는 경우도 있다. 이러한 문법의 엄격함은 온전히 취향의 차이이며 우열을 가릴 수 없는 문제다.