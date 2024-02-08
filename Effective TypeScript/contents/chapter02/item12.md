# 아이템 12. 함수 표현식에 타입 적용하기
JavaScript, TypeScript에선 함수 문장과 함수 표현식을 다르게 인식한다.

```typescript
function rollDice1(sides: number): number { /** ... */ } // 문장
const rollDice2 = function(sides: number): number { /** ... */ } // 표현식
const rollDice3 = (sides: number): number => { /** ... */ } // 표현식
```

**TypeScript에선 함수 표현식을 사용하는 것이 좋다.** 함수의 매개변수부터 반환값까지 전체를 함수 타입으로 선언하여 함수 표현식에 재사용할 수 있다는 장점이 있기 때문이다.

```typescript
type DiceRollFn = (sides: number) => number;
const rollDice: DiceRollFn = sides => { /** ... */ };
```

함수 타입 선언의 장점은 다음과 같다.

- 불필요한 코드의 반복을 줄인다.
- 반복되는 함수 시그니처를 하나의 함수 타입으로 통합할 수도 있다.
- 라이브러리는 공통 함수 시그니처를 타입으로 제공하기도 한다.
  - 라이브러리를 직접 만들고 있다면, 공통 콜백 함수를 위한 타입 선언을 제공하는 것이 좋다.
- 시그니처가 일치하는 다른 함수가 있을 때도 함수 표현식에 타입을 적용해볼 만하다.

```typescript
async function getQuote() {
  const response = await fetch('/quote?by=Mark+Twain');
  const quote = await response.json();
  return quote;
}
```

위 코드에서 버그가 있다. 404 Not Found가 반환된다면 응답은 JSON 형식이 아닐 수 있다.

`fetch`의 타입 선언은 다음과 같다.

```typescript
declare function fetch(
  input: RequestInfo, init?: RequestInit
): Promise<Response>;
```

```typescript
async function checkedFetch(input: RequestInfo, init?: RequestInit) {
  const response = await fetch(input, init);
  if (!response.ok) {
    // 비동기 함수 내에선 거절된 프로미스로 반환한다.
    throw new Error(`Request failed: ${response.status}`);
  }
  return response;
}
```

위 코드를 좀 더 간결하게 작성할 수 있다.

```typescript
const checkedFetch: typeof fetch = async (input, init) => {
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  return response;
}
```

함수 문장을 함수 표현식으로 바꾸고 함수 전체에 타입을 적용했다. 이는 TypeScript가 `input`과 `init`의 타입을 추론할 수 있게 해 준다.

타입 구문은 또한 `checkedFetch`의 반환 타입을 보장하며, `fetch`와 동일하다.

함수의 매개변수에 타입 선언을 하는 것보다 함수 표현식 전체 타입을 정의하는 것이 코드도 간결하고 안전하다. 다른 함수의 시그니처와 동일한 타입을 가지는 새 함수를 작성하거나, 동일한 타입 시그니처를 가지는 여러 개의 함수를 작성할 때는 매개변수의 타입과 반환 타입을 반복해서 작성하지 말고 함수 전체의 타입 선언을 적용해야 한다.

## 요약
- 매개변수나 반환 값에 타입을 명시하기보단 함수 표현식 전체에 타입 구문을 적용하는 것이 좋다.
- 만약 같은 타입 시그니처를 반복적으로 작성한 코드가 있다면 함수 타입을 분리해 내거나 이미 존재하는 타입을 찾아보도록 한다. 라이브러리를 직접 만든다면 공통 콜백에 타입을 제공해야 한다.
- 다른 함수의 시그니처를 참조하려면 `typeof fn`을 사용하면 된다.