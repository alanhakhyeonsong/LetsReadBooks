# Chapter 6. 배열
JavaScript 배열은 매우 유연하고 내부에 모든 타입의 값을 혼합해서 저장할 수 있다.

```javascript
const elements = [true, null, undefined, 42];

elements.push("even", ["more"]);
// elements 배열의 값: [true, null, undefined, 42, "even", ["more"]];
```

그러나 대부분의 개별 JavaScript 배열은 하나의 특정 타입의 값만 가진다. 다른 타입의 값을 추가하게 되면 배열을 읽을 때 혼란을 줄 수 있으며, 최악의 상황으로는 프로그램에 문제가 될 만한 오류가 발생할 수도 있다.

**TypeScript는 초기 배열에 어떤 데이터 타입이 있는지 기억하고, 배열이 해당 데이터 타입에서만 작동하도록 제한한다.** 이런 방식으로 배열의 데이터 타입을 하나로 유지시킨다.

```typescript
const warriors = ["Artemisia", "Boudica"];

// Ok: "Zenobia"의 타입은 string
warriors.push("Zenobia");

warriors.push(true);
//
// Error: Argument of type 'boolean' is not assignable to parameter of type 'string'.
```

TypeScript가 초기 배열에 담긴 요소를 통해 배열의 타입을 유추하는 방법은 변수의 초깃값에서 변수 타입을 유추하는 방법과 유사하다. TypeScript는 값이 할당되는 방식에서 코드의 의도된 타입을 이해하려고 시도하며 배열도 예외는 아니다.

## 배열 타입
다른 변수 선언과 마찬가지로 배열을 저장하기 위한 변수는 초깃값이 필요하지 않다. 변수는 `undefined`로 시작해서 나중에 배열 값을 받을 수 있다.

TypeScript는 변수에 타입 애너테이션을 제공해 배열이 포함해야 하는 값의 타입을 알려주려고 한다. 배열에 대한 타입 애너테이션은 배열의 요소 타입 다음에 `[]`가 와야 한다.

```typescript
let arrayOfNumbers: number[];

arrayOfNumbers = [4, 8, 15, 16, 23, 42];
```

배열 타입은 `Array<number>` 같은 구문으로도 작성할 수도 있다.

### 배열과 함수 타입
배열 타입은 함수 타입에 무엇이 있는지를 구별하는 괄호가 필요한 구문 컨테이너의 예다. 괄호는 애너테이션의 어느 부분이 함수 반환 부분이고 어느 부분이 배열 타입 묶음인지를 나타내기 위해 사용한다.

```typescript
// 타입은 string 배열을 반환하는 함수
let createStrings: () => string[];

// 타입은 각각의 string을 반환하는 함수 배열
let stringCreators: (() => string)[];
```

### 유니언 타입 배열
배열의 각 요소가 여러 선택 타입 중 하나일 수 있음을 나타내려면 유니언 타입을 사용한다.

유니언 타입으로 배열 타입을 사용할 때 애너테이션의 어느 부분이 배열의 콘텐츠이고 어느 부분이 유니언 타입 묶음인지를 나타내기 위해 괄호를 사용해야 할 수도 있다. 유니언 타입 배열에서 괄호 사용은 매우 중요하다. 따라서 다음 코드의 두 타입은 동일하지 않다.

```typescript
// 타입은 string 또는 number의 배열
let stringOrArrayOfNumbers: string | number[];

// 타입은 각각 number 또는 string인 요소의 배열
let arrayOrStringOfNumbers: (string | number)[];
```

TypeScript는 배열의 선언에서 두 가지 이상의 요소 타입이 포함되는 경우 유니언 타입 배열임을 알게 된다. 즉, 배열의 요소 타입은 배열에 담긴 요소에 대한 모든 가능한 타입의 집합이다.

```typescript
// 타입: (string | undefined)[]
const namesMaybe = [
  "Aqualtune",
  "Blenda",
  undefined,
];
```

### any 배열의 진화
초기에 빈 배열로 설정된 변수에서 타입 애너테이션을 포함하지 않으면 TypeScript는 배열을 `any[]`로 취급하고 모든 콘텐츠를 받을 수 있다. 하지만 `any` 변수가 변경되는 것처럼 `any[]` 배열이 변경되는 것도 좋아하지 않는다. 타입 애너테이션이 없는 빈 배열은 잠재적으로 잘못된 값 추가를 허용해 TypeScript의 타입 검사기가 갖는 이점을 부분적으로 무력화하기 때문이다.

```typescript
// 타입: any[]
let values = [];

// 타입: any[]
values.push('');

// 타입: (number | string)[]
values[0] = 0;
```

변수와 마찬가지로 배열이 `any` 타입이 되도록 허용하거나 일반적으로 `any` 타입을 사용하도록 허용하면 TypeScript의 타입 검사 목적을 부분적으로 무효화한다.

### 다차원 배열
2차원 배열 또는 배열의 배열은 두 개의 `[]`를 갖는다.

```typescript
let arrayOfArraysOfNumbers: number[][];

arrayOfArraysOfNumbers = [
  [1, 2, 3],
  [2, 4, 6],
  [3, 6, 9],
];
```

3차원 배열 또는 배열의 배열의 배열에는 세 개의 `[]`가 있고 4차원 배열에는 네 개의 `[]`가 있다.

이러한 다차원 배열 타입에는 배열 타입에 새로운 개념을 도입한 게 아니다. 즉, 2차원 배열은 원래의 타입을 가지며 끝에 `[]`가 있고, 그 뒤에 `[]`를 추가한다고 생각하면 쉽다.

```typescript
// 타입: number[][]
let arrayOfArraysOfNumbers: (number[])[];
```

## 배열 멤버
TypeScript는 배열의 멤버를 찾아서 해당 배열의 타입 요소를 되돌려주는 전형적인 인덱스 기반 접근 방식을 이해하는 언어다.

```typescript
const defenders = ["Clarenza", "Dina"];

// 타입: string
const defender = defenders[0];
```

유니언 타입으로 된 배열의 멤버는 그 자체로 동일한 유니언 타입이다.

```typescript
const soldiersOrDates = ["Deborah Sampson", new Date(1782, 6, 3)];

// 타입: string | Date
const soldierOrDate = soldiersOrDates[0];
```

### 주의 사항: 불안정한 멤버
TypeScript 타입 시스템은 기술적으로 불안정하다고 알려져 있다. 대부분 올바른 타입을 얻을 수 있지만, 때로는 값 타입에 대한 타입 시스템의 이해가 올바르지 않을 수 있다. 특히 배열은 타입 시스템에서 불안정한 소스다. 기본적으로 TypeScript는 모든 배열의 멤버에 대한 접근이 해당 배열의 멤버를 반환한다고 가정하지만, JavaScript에서조차도 배열의 길이보다 큰 인덱스로 배열 요소에 접근하면 `undefined`를 제공한다.

다음 코드는 TypeScript 컴파일러의 기본 설정에서 오류를 표시하지 않는다.

```typescript
function withElements(elements: string[]) {
  console.log(elements[9001].length); // 타입 오류 없음
}

withElements(["It's", "over"]);
```

런타임 시 `Cannot read property 'length' of undefined`가 발생하며 충돌할 거라고 유추할 수 있지만, TypeScript는 검색된 배열의 멤버가 존재하는지 의도적으로 확인하지 않는다. 위 예제에서 `elements[9001]`은 `undefined`가 아니라 `string` 타입으로 간주된다.

TypeScript에는 배열 조회를 더 제한하고 타입을 안전하게 만드는 `noUncheckedIndexedAccess` 플래그가 있지만 이 플래그는 매우 엄격해서 대부분의 프로젝트에서 사용하지 않는다.

## 스프레드와 나머지 매개변수
`...` 연산자를 사용하는 나머지 매개변수와 배열 스프레드는 JavaScript에서 배열과 상호작용하는 핵심 방법이다. TypeScript는 두 방법을 모두 이해한다.

### 스프레드
`...` 스프레드 연산자를 사용해 배열을 결합한다. TypeScript는 입력된 배열 중 하나의 값이 결과 배열에 포함될 것임을 이해한다.

만약에 입력된 배열이 동일한 타입이라면 출력 배열도 동일한 타입이다. 서로 다른 타입의 두 배열을 함께 스프레드해 새 배열을 생성하면 새 배열은 두 개의 원래 타입 중 어느 하나의 요소인 유니언 타입 배열로 이해된다.

```typescript
// 타입: string[]
const soldiers = ["Harriet Tubman", "Joan of Arc", "Khutulun"];

// 타입: number[]
const soldierAges = [90, 19, 45];

// 타입: (string | number)[]
const conjoined = [...soldiers, ...soldierAges];
```

### 나머지 매개변수 스프레드
TypeScript는 나머지 매개변수로 배열을 스프레드 하는 JavaScript 실행을 인식하고 이에 대해 타입 검사를 수행한다. 나머지 매개변수를 위한 인수로 사용되는 배열은 나머지 매개변수와 동일한 배열 타입을 가져야 한다.

```typescript
function logWarriors(greeting: string, ...names: string[]) {
  for (const name of names) {
    console.log(`${greeting}, ${name}!`);
  }
}

const warriors = ["Cathay Williams", "Lozen", "Nzinga"];

logWarriors("Hello", ...warriors);

const birthYears = [1844, 1840, 1583];

logWarriors("Born in", ...birthYears);
//
// Error: Argument of type 'number' is not assignable to parameter of type 'string'.
```

## 튜플
JavaScript 배열은 이론상 어떤 크기라도 될 수 있다. 하지만 때로는 튜플이라고 하는 고정된 크기의 배열을 사용하는 것이 유용하다. 튜플 배열은 각 인덱스 알려진 특정 타입을 가지며 배열의 모든 가능한 멤버를 갖는 유니언 타입보다 더 구체적이다. 튜플 타입을 선언하는 구문은 배열 리터럴처럼 보이지만 요소의 값 대신 타입을 적는다.

```typescript
let yearAndWarrior: [number, string];

yearAndWarrior = [530, "Tomyris"]; // Ok

yearAndWarrior = [false, "Tomyris"];
//
// Error: Type 'boolean' is not assignable to type 'number'.

yearAndWarrior = [530];
//
// Error: Type '[number]' is not assignable to type '[number, string]'.
//   Source has 1 element(s) but target requires 2.
```

JavaScript에서는 단일 조건을 기반으로 두 개의 변수에 초깃값을 설정하는 것처럼 한 번에 여러 값을 할당하기 위해 튜플과 배열 구조 분해 할당을 함께 자주 사용한다.

```typescript
// year 타입: number
// warrior 타입: string
let [year, warrior] = Math.random() > 0.5
    ? [340, "Archidamia"]
    : [1828, "Rani of Jhansi"];
```

### 튜플 할당 가능성
TypeScript에서 튜플 타입은 가변 길이의 배열 타입보다 더 구체적으로 처리된다. 즉, 가변 길이의 배열 타입은 튜플 타입에 할당할 수 없다.

```typescript
// 타입: (boolean | number)[]
const pairLoose = [false, 123];

const pairTupleLoose: [boolean, number] = pairLoose;
//
// Error: Type '(number | boolean)[]' is not assignable to type '[boolean, number]'.
//   Target requires 2 element(s) but source may have fewer.
```

`pairLoose`가 `[boolean, number]` 자체로 선언된 경우 `pairTupleLoose`에 대한 값 할당이 허용되었을 것이다. 하지만 TypeScript는 튜플 타입의 튜플에 얼마나 많은 멤버가 있는지 알고 있기 때문에 길이가 다른 튜플은 서로 할당할 수 없다.

```typescript
const tupleThree: [boolean, number, string] = [false, 1583, "Nzinga"];

const tupleTwoExact: [boolean, number] = [tupleThree[0], tupleThree[1]];

const tupleTwoExtra: [boolean, number] = tupleThree;
//
// Error: Type '[boolean, number, string]' is not assignable to type '[boolean, number]'.
//   Source has 3 element(s) but target allows only 2.
```

#### 나머지 매개변수로서의 튜플
튜플은 구체적인 길이와 요소 타입 정보를 가지는 배열로 간주되므로 함수에 전달할 인수를 저장하는 데 특히 유용하다. TypeScript는 `...` 나머지 매개변수로 전달된 튜플에 정확한 타입 검사를 제공할 수 있다.

아래 `logPair` 함수의 매개변수는 `string`과 `number`로 입력된다. `(string | number)[]` 타입의 값을 인수로 전달하려고 하면 둘 다 동일한 타입이거나 타입의 잘못된 순서로 인해 내용이 일치하지 않을 가능성이 있어 타입의 안전을 보장할 수 없다. 그러나 값이 `[string, number]` 튜플이라고 알고 있다면 값이 일치한다는 것을 알게 된다.

```typescript
function logPair(name: string, value: number) {
  console.log(`${name} has ${value}`);
}

const pairArray = ["Amage", 1];

logPair(...pairArray);
//
// Error: A spread argument must either have a tuple type or be passed to a rest parameter.

const pairTupleIncorrect: [number, string] = [1, "Amage"];

logPair(...pairTupleIncorrect);
//
// Error: Argument of type 'number' is not assignable to parameter of type 'string'.

const pairTupleCorrect: [string, number] = ["Amage", 1];

logPair(...pairTupleCorrect); // Ok
```

나머지 매개변수 튜플을 사용하고 싶다면 여러 번 함수를 호출하는 인수 목록을 배열에 저장해 함께 사용할 수 있다. 다음 코드의 `trios`는 튜플 배열이고, 각 튜플은 두 번째 멤버로 또 튜플을 가진다. `trios.forEach(trio => logTrio(...trio))`는 각 `...trio`가 `logTrio`의 매개변수 타입과 일치하므로 안전한 것으로 알려진다. 그러나 `trio.forEach(logTrio)`는 첫 번째 매개변수로 타입이 `string`인 `[string, [number, boolean]]` 전체를 전달하려고 시도하기 때문에 할당할 수 없다.

```typescript
function logTrio(name: string, value: [number, boolean]) {
  console.log(`${name} has ${value[0]} (${value[1]})`);
}

const trios: [string, [number, boolean]][] = [
  ["Amanitore", [1, true]],
  ["Æthelflæd", [2, false]],
  ["Ann E. Dunwoody", [3, false]],
];

trios.forEach(trio => logTrio(...trio)); // Ok

trios.forEach(logTrio);
//
// Error: Argument of type '(name: string, value: [number, boolean]) => void' is not
// assignable to parameter of type '(value: [string, [number, boolean]], ...) => void'.
//   Types of parameters 'name' and 'value' are incompatible.
//     Type '[string, [number, boolean]]' is not assignable to type 'string'.
```

### 튜플 추론
TypeScript는 생성된 배열을 튜플이 아닌 가변 길이의 배열로 취급한다. 배열이 변수의 초깃값 또는 함수에 대한 반환값으로 사용하는 경우, 고정된 크기의 튜플이 아니라 유연한 크기의 배열로 가정한다.

다음 코드에서 반환된 배열 리터럴 기반으로 타입을 유추해보면 `firstCharAndSize` 함수는 `[string, number]`가 아니라 `(string | number)[]`를 반환하는 것으로 유추된다.

```typescript
// 반환 타입: (string | number)[]
function firstCharAndSize(input: string) {
  return [input[0], input.length];
}

// firstChar 타입: string | number
// size 타입: string | number
const [firstChar, size] = firstCharAndSize("Gudit");
```

TypeScript에선 값이 일반적인 배열 타입 대신 좀 더 구체적인 튜플 타입이어야 함을 다음 두 가지 방법으로 나타낸다. 명시적 튜플 타입과 `const` 어서션을 사용한 방법이다.

#### 명시적 튜플 타입
함수에 대한 반환 타입 애너테이션처럼 튜플 타입도 타입 애너테이션에 사용할 수 있다. 함수가 튜플 타입을 반환한다고 선언되고, 배열 리터럴을 반환한다면 해당 배열 리터럴은 일반적인 가변 길이의 배열 대신 튜플로 간주된다.

```typescript
// 반환 타입: [string, number]
function firstCharAndSizeExplicit(input: string): [string, number] {
  return [input[0], input.length];
}

// firstChar 타입: string | number
// size 타입: string | number
const [firstChar, size] = firstCharAndSizeExplicit("Cathay Williams");
```

#### const 어서션
명시적 타입 애너테이션에 튜플 타입을 입력하는 작업은 명시적 타입 애너테이션을 입력할 때와 동일한 이유로 고통스러울 수 있다. 즉, 코드 변경에 따라 작성 및 수정이 필요한 구문을 추가해야 한다.

하지만 그 대안으로 TypeScript는 값 뒤에 넣을 수 있는 `const` 어서션인 `as const` 연산자를 제공한다. `const` 어서션은 TypeScript에 타입을 유추할 때 읽기 전용이 가능한 값 형식을 사용하도록 지시한다.

다음과 같이 배열 리터럴 뒤에 `as const`가 배치되면 배열이 튜플로 처리되어야 함을 나타낸다.

```typescript
// 타입: (string | number)[]
const unionArray = [1157, "Tomoe"];

// 타입: readonly [1157, "Tomoe"]
const readonlyTuple = [1157, "Tomoe"] as const;
```

`const` 어서션은 유연한 크기의 배열을 고정된 크기의 튜플로 전환하는 것을 넘어서, 해당 튜플이 읽기 전용이고 값 수정이 예상되는 곳에서 사용할 수 없음을 나타낸다.

아래 예제에서 `pairMutable`은 전형적인 명시적 튜플 타입이므로 수정될 수 없다. 그러나 `as const`는 값이 변경될 수 있는 `pairAlsoMutable`에 할당할 수 없도록 하고, 상수 `pairConst`의 멤버는 수정을 허용하지 않는다.

```typescript
const pairMutable: [number, string] = [1157, "Tomoe"];
pairMutable[0] = 1247; // Ok

const pairAlsoMutable: [number, string] = [1157, "Tomoe"] as const;
//
// Error: The type 'readonly [1157, "Tomoe"]' is 'readonly'
// and cannot be assigned to the mutable type '[number, string]'.

const pairConst = [1157, "Tomoe"] as const;
pairConst[0] = 1247;
//
// Error: Cannot assign to '0' because it is a read-only property.
```

실제로 읽기 전용 튜플은 함수 반환에 편리하다. 튜플을 반환하는 함수로부터 반환된 값은 보통 즉시 구조화되지 않으므로 읽기 전용인 튜플은 함수를 사용하는 데 방해가 되지 않는다.

다음 `firstCharAndSizeAsConst`는 읽기 전용 `[string, number]`를 반환하지만, 이를 사용하는 코드는 해당 튜플에서 값을 찾는 것에만 관심을 둔다.

```typescript
// 반환 타입: readonly [string, number]
function firstCharAndSizeAsConst(input: string) {
  return [input[0], input.length] as const;
}

// firstChar 타입: string
// size 타입: number
const [firstChar, number] = firstCharAndSizeAsConst("Ching Shih");
```