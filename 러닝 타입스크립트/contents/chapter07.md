# Chapter 7. 인터페이스
- 인터페이스는 연관된 이름으로 객체 형태를 설명하는 또 다른 방법이다.
- 인터페이스는 별칭으로 된 객체 타입과 여러 면에서 유사하지만 일반적으로 더 쉬운 오류 메시지, 더 빠른 컴파일러 성능, 클래스와의 더 나은 상호 운용성을 위해 선호된다.

## 타입 별칭 vs. 인터페이스
```typescript
// 두 구문은 거의 같다.
type Poet = {
  born: number;
  name: string;
};

interface Poet {
  born: number;
  name: string;
}
```

인터페이스에 대한 타입스크립트의 할당 가능성 검사와 오류 메시지는 객체 타입에서 실행되는 것과 거의 동일하다.

```typescript
let valueLater: Poet;

// Ok
valueLater = {
  born: 1935,
  name: 'Sara Teasdale',
};

valueLater = "Emily Dickinson";
// Error: Type 'string' is not assignable to 'Poet'.

valueLater = {
  born: true,
  // Error: Type 'boolean' is not assignable to type 'number'.
  name: 'Sappho',
};
```

그러나 인터페이스와 타입 별칭 사이에는 몇 가지 주요 차이점이 있다.

- 인터페이스는 속성 증가를 위해 병합할 수 있다. 이 기능은 내장된 전역 인터페이스 또는 npm 패키지와 같은 외부 코드를 사용할 때 특히 유용하다.
- 인터페이스는 클래스가 선언된 구조의 타입을 확인하는 데 사용할 수 있지만 타입 별칭은 사용할 수 없다.
- 일반적으로 인터페이스에서 타입스크립트 타입 검사기가 더 빨리 작동한다. 인터페이스는 타입 별칭이 하는 것처럼 새로운 객체 리터럴의 동적인 복사 붙여넣기보다 내부적으로 더 쉽게 캐시할 수 있는 명명된 타입을 선언합니다.
- 인터페이스는 이름 없는 객체 리터럴의 별칭이 아닌 이름 있는(명명된) 객체로 간주되므로 어려운 특이 케이스에서 나타나는 오류 메시지를 좀 더 쉽게 읽을 수 있다.

가능하다면 타입 별칭보단 인터페이스 사용을 추천한다.

## 속성 타입
getter/setter를 포함해, 가끔 존재하는 속성, 또는 임의의 속성 이름을 사용하는 등 JavaScript 객체를 실제로 사용할 때 낯설고 이상할 수 있다. TypeScript는 인터페이스가 이런 이상한 부분을 모델링할 수 있도록 유용한 타입 시스템 도구를 제공한다.

### 선택적 속성
객체 타입과 마찬가지로 모든 객체가 필수적으로 인터페이스 속성을 가질 필요는 없다. 타입 애너테이션 `:` 앞에 `?`를 사용해 인터페이스의 속성이 선택적 속성임을 나타낼 수 있다.

```typescript
interface Book {
  author?: string;
  pages: number;
};

// Ok
const ok: Book = {
  author: "Rita Dove",
  pages: 80,
};

const missing: Book = {
  pages: 80
};
```

`undefined`를 포함한 유니언 타입의 선택적 속성과 일반 속성 사이의 차이점과 관련된 주의 사항은 객체 타입뿐만 아니라 인터페이스에도 적용된다. 추후 선택적 속성에 대한 TypeScript의 엄격 모드 설정 방법에서 확인해보자.

### 읽기 전용 속성
경우에 따라 인터페이스에 정의된 객체의 속성을 재할당하지 못하도록 인터페이스 사용자를 차단하고 싶다. TypeScript는 속성 이름 앞에 `readonly` 키워드를 추가해 다른 값으로 설정될 수 없음을 나타낸다.

```typescript
interface Page {
  readonly text: string;
}

function read(page: Page) {
  console.log(page.text);
  page.text += '|';
  // Cannot assign to 'text' because it is a read-only property.
}
```

- `readonly` 제한자는 타입 시스템에만 존재하며 인터페이스에서만 사용할 수 있다.
- `readonly` 제한자는 객체의 인터페이스를 선언하는 위치에서만 사용되고 실제 객체에는 적용되지 않는다.

위 예제에서 `text` 속성의 부모 객체는 함수 내부에서 `text`로 명시적으로 사용되지 않았기 때문에 함수 밖에서 속성을 수정할 수 있다. 쓰기 가능한 속성을 `readonly` 속성에 할당할 수 있으므로 아래와 같이 `pageIsh`는 `Page`로 사용할 수 있다. 가변 속성은 `readonly` 속성이 필요한 모든 위치에서 읽을 수 있다.

```typescript
const pageIsh = {
  text: "Hello, World!",
};

// Ok: pageIsh는 Page 객체가 아니라 text가 있는, 유추된 객체 타입이다.
pageIsh.text += "!";

// OK: pageIsh의 더 구체적 버전인 Page를 읽는다.
read(pageIsh);
```

명시적 타입 애너테이션인 `: Page`로 변수 `pageIsh`를 선언하면 타입스크립트에 `text` 속성이 `readonly`라고 가리키게 되지만, 유추된 타입은 `readonly`가 아니다.

- `readonly` 인터페이스 멤버는 코드 영역에서 객체를 의도치 않게 수정하는 것을 막는 편리한 방법이다.
- `readonly`는 타입 시스템 구성 요소일 뿐 컴파일된 JavaScript 출력 코드에는 존재하지 않는다.
- `readonly`는 단지 TypeScript 타입 검사기를 사용해 개발 중에 그 속성이 수정되지 못하도록 보호하는 역할을 한다.

### 함수와 메서드
JavaScript에서 객체 멤버가 객체 함수가 되는 것은 매우 일반적이다. TypeScript에서도 인터페이스 멤버를 함수 타입으로 선언할 수 있다.

TypeScript는 인터페이스 멤버를 함수로 선언하는 두 가지 방법을 제공한다.

- 메서드 구문: 인터페이스 멤버를 `member(): void`와 같이 객체의 멤버로 호출되는 함수로 선언
- 속성 구문: 인터페이스 멤버를 `member: () => void`와 같이 독립 함수와 동일하게 선언

두 가지 선언 방법은 JavaScript에서 객체를 함수로 선언하는 방법과 동일하다.

```typescript
interface HasBothFunctionTypes {
  property: () => string;
  method(): string;
}

const hasBoth: HasBothFunctionTypes = {
  property: () => "property",
  method() {
    return "method";
  }
};

console.log(hasBoth.property()); // property
console.log(hasBoth.method()); // method
```

두 가지 방법 모두 선택적 속성 키워드인 `?`를 사용해 필수로 제공하지 않아도 되는 멤버로 나타낼 수 있다.

```typescript
interface OptionalReadonlyFunctions {
  optionalProperty?: () => string;
  optionalMethod?(): string;
}
```

메서드와 속성 선언은 대부분 서로 교환하여 사용할 수 있다. 책 내에서 메서드와 속성의 주요 차이점은 다음과 같다.

- 메서드는 `readonly`로 선언할 수 있지만 속성은 가능하다.
- 인터페이스 병합은 메서드와 속성을 다르게 처리한다.
- 타입에서 수행되는 일부 작업은 메서드와 속성을 다르게 처리한다.

2022년 기준으로 추천하는 스타일 가이드는 다음과 같다고 한다.

- 기본 함수가 `this`를 참조할 수 있다는 것을 알고 있다면 메서드 함수를 사용하자. 가장 일반적으로 클래스의 인스턴스에서 사용된다.
- 반대의 경우는 속성 함수를 사용하자.

`this` 스코프와 선택한 방식을 의도하지 않는 한 코드에 거의 영향을 주지 않는다고 하니 두 가지를 혼동하거나 차이점을 이해하지 못해도 걱정하지 말자.

### 호출 시그니처
인터페이스와 객체 타입은 호출 시그니처로 선언할 수 있다. **호출 시그니처는 값을 함수처럼 호출하는 방식에 대한 타입 시스템의 설명이다.** 호출 시그니처가 선언한 방식으로 호출되는 값만 인터페이스에 할당할 수 있다.

**즉, 할당 가능한 매개변수와 반환타입을 가진 함수다.** 호출 시그니처는 함수 타입과 비슷하지만, 콜론대신 화살표로 표시한다.

```typescript
// FunctionAlias와 CallSignature 타입은 모두 동일한 함수 매개변수와 반환 타입을 설명한다.
type FunctionAlias = (input: string) => number;

interface CallSignature {
  (input: string): number;
}

// 타입: (input: string) => number
const typedFunctionAlias: FunctionAlias = (input) => input.length; // Ok
// 타입: (input: string) => number
const typedCallSignature: CallSignature = (input) => input.length; // Ok
```

호출 시그니처는 사용자 정의 속성을 추가로 갖는 함수를 설명하는 데 사용할 수 있다. TypeScript는 함수 선언에 추가된 속성을 해당 함수 선언의 타입에 추가하는 것으로 인식한다.

아래 `keepsTrackOfCalls` 함수 선언에는 `number` 타입인 `counter` 속성이 주어져 `FunctionWithCount` 인터페이스에 할당할 수 있다. 따라서 `FunctionWithCount` 타입의 `hasCallCount` 인수에 할당할 수 있다. 코드의 마지막 함수에는 `count` 속성이 주어지지 않았다.

```typescript
interface FunctionWithCount {
  count: number;
  (): void;
}

let hasCallCount: FunctionWithCount;

function keepsTrackOfCalls() {
  keepsTrackOfCalls.count += 1;
  console.log(`I've been called ${keepsTrackOfCalls.count} times!`);
}

keepsTrackOfCalls.count = 0;

hasCallCount = keepsTrackOfCalls; // Ok

function doesNotHaveCount() {
  console.log("No idea!");
}

hasCallCount = doesNotHaveCount;
// Property 'count' is missing in type '() => void' but required in type 'FunctionWithCount'.
```

### 인덱스 시그니처
일부 JavaScript 프로젝트는 임의의 `string` 키에 값을 저장하기 위한 객체를 생성한다. 이러한 컨테이너 객체의 경우 모든 가능한 키에 대해 필드가 있는 인터페이스를 선언하는 것은 비현실적이거나 불가능하다.

- TypeScript는 인덱스 시그니처 구문을 제공해 인터페이스의 객체가 임의의 키를 받고, 해당 키 아래의 특정 타입을 반환할 수 있음을 나타낸다.
- JavaScript 객체 속성 조회는 암묵적으로 키를 문자열로 변환하기 때문에 인터페이스의 객체는 문자열 키와 함께 가장 일반적으로 사용된다.
- 인덱스 시그니처는 일반 속성 정의와 유사하지만 키 다음에 타입이 있고 `{[i: string]: ...}`과 같이 배열의 대괄호를 갖는다.

```typescript
// 아래 인터페이스는 number 값을 갖는 모든 string 키를 허용하는 것으로 선언되었다.
// 이런 타입의 객체는 값이 number면 string 키가 아닌 그 어떤 키도 바인딩할 수 없다.
interface WordCounts {
  [i: string]: number;
}

const counts: WordCounts = {};

counts.apple = 0; // Ok
counts.banana = 1; // Ok

counts.cherry = false;
// Error: Type 'boolean' is not assignable to type 'number'.
```

인덱스 시그니처는 객체에 값을 할당할 때 편리하지만 타입 안정성을 완벽하게 보장하지는 않는다. 인덱스 시그니처는 객체가 어떤 속성에 접근하든 간에 값을 반환해야 함을 나타낸다.

다음 `publishDates` 값은 `Date` 타입으로 `Frankenstein`을 안전하게 반환하지만 TypeScript는 `Beloved`가 정의되지 않았음에도 불구하고 정의되었다고 생각하도록 속인다.

```typescript
interface DatesByName {
  [i: string]: Date;
}

const publishDates: DatesByName = {
  Frankenstein: new Date("1 January 1818"),
};

publishDates.Frankenstein; // 타입: Date
console.log(publishDates.Frankenstein.toString()); // Ok

publishDates.Beloved; // 타입은 Date이지만 런타임 값은 undefined
console.log(publishDates.Beloved.toString());
// 타입 시스템에서는 오류가 나지 않지만 실제 런타임에서는 오류가 발생함
// Runtime error: Cannot read property 'toString' of undefined (reading publishDates.Beloved)
```

키/값 쌍을 저장하려 하는데 키를 미리 알 수 없다면 `Map`을 사용하는 편이 더 안전하다. `.get` 메서드는 항상 키가 존재하지 않음을 나타내기 위해 `| undefined` 타입을 반환한다.  
// 9장에서 제네릭 컨테이너 클래스로 작업하는 법을 알아보자.

#### 속성과 인덱스 시그니처 혼합
인터페이스는 명시적으로 명명된 속성과 포괄적인 용도의 `string` 인덱스 시그니처를 한번에 포함할 수 있다. 각각의 명명된 속성의 타입은 포괄적인 용도의 인덱스 시그니처로 할당할 수 있어야 한다. 명명된 속성이 더 구체적인 타입을 제공하고, 다른 모든 속성은 인덱스 시그니처 타입으로 대체하는 것으로 혼합해서 사용할 수 있다.

```typescript
interface HistoricalNovels {
  Oroonoko: number;
  [i: string]: number;
}

// Ok
const novels: HistoricalNovels = {
  Outlander: 1991,
  Oroonoko: 1688,
};

const missingOroonoko: HistoricalNovels = {
  // Error: Property 'Oroonoko' is missing in type
  // '{ Outlander: number; }' but required in type 'HistoricalNovels'.
  Outlander: 1991,
};
```

속성과 인덱스 시그니처를 혼합해서 사용하는 일반적인 타입 시스템 기법 중 하나는 인덱스 시그니처의 원시 속성보다 명명된 속성에 대해 더 구체적인 속성 타입 리터럴을 사용하는 것이다. 명명된 속성의 타입이 인덱스 시그니처에 할당될 수 있는 경우(각각의 리터럴 및 원시 속성에 해당) TypeScript는 더 구체적인 속성 타입 리터럴을 사용하는 것을 허용한다.

```typescript
interface ChapterStarts {
  preface: 0;
  [i: string]: number;
}

const correctPreface: ChapterStarts = {
  preface: 0,
  night: 1,
  shopping: 5
};

const wrongPreface: ChapterStarts = {
  preface: 1,
  // Error: Type '1' is not assignable to type '0'.
};
```

`ChapterStarts`는 `preface` 속성은 0으로, 다른 모든 속성은 더 일반적인 `number`를 갖도록 선언한다. 즉, `ChapterStarts`를 사용하는 모든 객체의 `preface` 속성은 반드시 0이어야 한다.

#### 숫자 인덱스 시그니처
JavaScript가 암묵적으로 객체 속성 조회 키를 문자열로 변환하지만 때로는 객체의 키로 숫자만 허용하는 것이 바람직할 수 있다. TypeScript 인덱스 시그니처는 키로 `string` 대신 `number`를 사용할 수 있지만, 명명된 속성은 그 타입을 포괄적인 용도의 `string` 인덱스 시그니처의 타입으로 할당할 수 있어야 한다.

```typescript
// Ok
interface MoreNarrowNumbers {
  [i: number]: string;
  [i: string]: string | undefined;
}

// Ok
const mixesNumbersAndStrings: MoreNarrowNumbers = {
  0: '',
  key1: '',
  key2: undefined,
};

interface MoreNarrowStrings {
  [i: number]: string | undefined;
  // Error: 'number' index type 'string | undefined'
  // is not assingable to 'string' index type 'string'.
  [i: string]: string;
}
```

### 중첩 인터페이스
객체 타입이 다른 객체 타입의 속성으로 중첩될 수 있는 것처럼 **인터페이스 타입도 자체 인터페이스 타입 혹은 객체 타입을 속성으로 가질 수 있다.**

```typescript
interface Novel {
  author: {
    name: string;
  };
  setting: Setting;
}

interface Setting {
  place: string;
  year: number;
}

let myNovel: Novel;

// Ok
myNovel = {
  author: {
    name: 'Jane Austen',
  },
  setting: {
    place: 'England',
    year: 1812,
  }
};

myNovel = {
  author: {
    name: 'Emily Bronte',
  },
  setting: {
    // Property 'year' is missing in type '{ place: string; }' but required in type 'Setting'.
    place: 'West Yorkshire',
  },
};
```

## 인터페이스 확장
