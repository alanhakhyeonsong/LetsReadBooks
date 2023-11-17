# Chapter 5. 함수
## 함수 매개변수
```typescript
function sing(song) {
  console.log(`Singing: ${song}!`);
}
```

`song` 매개변수를 제공하기 위해 의도한 값의 타입은 무엇일까? 명시적 타입 정보가 선언되지 않으면 절대 타입을 알 수 없다. **TypeScript가 이를 `any` 타입으로 간주하며 매개변수의 타입은 무엇이든 될 수 있다.**

변수와 마찬가지로 TypeScript를 사용하면 타입 애너테이션으로 함수 매개변수의 타입을 선언할 수 있다.

```typescript
function sing(song: string) {
  console.log(`Singing: ${song}!`);
}
```

코드를 유효한 TypeScript 구문으로 만들기 위해 함수 매개변수에 적절한 타입 애너테이션을 추가할 필요는 없다. TypeScript는 타입 오류로 오류를 계속 알리지만, 이미 시작된 JavaScript는 계속 실행된다. `song` 매개변수에 타입 선언이 누락된 코드 스니펫은 여전히 TypeScript에서 JavaScript로 변환된다.

### 필수 매개변수
JavaScript에서는 인수의 수와 상관없이 함수를 호출할 수 있다. 하지만 **TypeScript는 함수에 선언된 모든 매개변수가 필수라고 가정한다.** 함수가 잘못된 수의 인수로 호출되면, TypeScript는 타입 오류의 형태로 이의를 제기한다. 함수가 너무 적거나 많은 인수로 호출되면 TypeScript는 인수의 개수를 계산한다.

```typescript
function singTwo(first: string, second: string) {
  console.log(`${first} / ${second}`);
}

// Logs: "Ball and Chain / undefined"
singTwo("Ball and Chain");
//
// Error: Expected 2 arguments, but got 1.

// Logs: "I Will Survive / Higher Love"
singTwo("I Will Survive", "Higher Love"); // Ok

// Logs: "Go Your Own Way / The Chain"
singTwo("Go Your Own Way", "The Chain", "Dreams");
//
// Error: Expected 2 arguments, but got 3.
```

함수에 필수 매개변수를 제공하도록 강제하면 예상되는 모든 인숫값을 함수 내에 존재하도록 만들어 타입 안정성을 강화하는 데 도움이 된다. 모든 인숫값이 존재하는지 확인하지 못하면 이전 `singTwo` 함수가 `undefined`를 로그로 남기거나 인수를 무시하는 것과 같이 코드에서 예기치 않은 동작이 발생한다.

---
📌 매개변수는 인수로 받을 것으로 예상되는 함수의 선언을 나타낸다. 인수는 함수를 호출할 때 매개변수에 제공되는 값을 나타낸다. 위 예제에서 `first`와 `second`는 매개변수이고, `Dreams`와 같은 문자열은 인수다.

---

### 선택적 매개변수
JavaScript에서 함수 매개변수가 제공되지 않으면 함수 내부의 인숫값은 `undefined`로 기본값이 설정된다. 때로는 함수 매개변수를 제공할 필요가 없을 때도 있고, `undefined` 값을 위해 의도적으로 사용할 수도 있다.

TypeScript가 이러한 선택적 매개변수에 인수를 제공하지 못하는 경우, 타입 오류를 보고하지 않았으면 한다. TypeScript에선 **선택적 객체 타입 속성과 유사하게 타입 에너테이션의 `:` 앞에 `?`를 추가해 매개변수가 선택적이라고 표시한다.**

함수 호출에 선택적 매개변수를 제공할 필요는 없다. 선택적 매개변수에는 항상 `| undefined`가 유니언 타입으로 추가되어 있다.

```typescript
function announceSong(song: string, singer?: string) {
  console.log(`Song: ${song}`);

  if (singer) {
    console.log(`Singer: ${singer}`);
  }
}

announceSong("Greensleeves"); // Ok
announceSong("Greensleeves", undefined); // Ok
announceSong("Chandelier", "Sia"); // Ok
```

이러한 선택적 매개변수는 항상 암묵적으로 `undefined`가 될 수 있다.

선택적 매개변수는 `| undefined`를 포함하는 유니언 타입 매개변수와는 다르다. `?`로 표시된 선택적 매개변수가 아닌 매개변수는 값이 명시적으로 `undefined`일지라도 항상 제공되어야 한다.

`announceSongBy` 함수의 `singer` 매개변수는 명시적으로 제공되어야 한다. `singer`는 `string` 값이거나 `undefined`가 될 수 있다.

```typescript
function announceSongBy(song: string, singer: string | undefined) { /* ... */ }

announceSongBy("Greensleeves");
//
// Error: Expected 2 arguments, but got 1.

announceSongBy("Greensleeves", undefined); // Ok
announceSongBy("Chandelier", "Sia"); // Ok
```

**함수에서 사용되는 모든 선택적 매개변수는 마지막 매개변수여야 한다.** 필수 매개변수 전에 선택적 매개변수를 위치시키면 다음과 같이 TypeScript 구문 오류가 발생한다.

```typescript
function announceSinger(singer?: string, song: string) {}
//
// Error: A required parameter cannot follow an optional parameter.
```

### 기본 매개변수
JavaScript에서 선택적 매개변수를 선언할 때 `=`와 값이 포함된 기본값을 제공할 수 있다. 즉, 선택적 매개변수에는 기본적으로 값이 제공되기 때문에 해당 TypeScript 타입에는 암묵적으로 함수 내부에 `| undefined` 유니언 타입이 추가된다. TypeScript는 함수의 매개변수에 대해 인수를 누락하거나 `undefined` 인수를 사용해서 호출하는 것을 여전히 허용한다.

TypeScript의 타입 추론은 초기 변숫값과 마찬가지로 기본 함수 매개변수에 대해서도 유사하게 작동합니다. 매개변수에 기본값이 있고 타입 애너테이션이 없는 경우, TypeScript는 해당 기본값을 기반으로 매개변수 타입을 유추한다.

```typescript
function rateSong(song: string, rating = 0) {
  console.log(`${song} gets ${rating}/5 stars!`);
}

rateSong("Photograph"); // Ok
rateSong("Set Fire to the Rain", 5); // Ok
rateSong("Set Fire to the Rain", undefined); // Ok

rateSong("At Last!", "100"); // Ok
//
// Error: Argument of type '"100"' is not assignable to
// parameter of type 'number | undefined'.
```

### 나머지 매개변수
JavaScript의 일부 함수는 임의의 수의 인수로 호출할 수 있도록 만들어진다. `...` 스프레드 연산자는 함수 선언의 마지막 매개변수에 위치하고, 해당 매개변수에서 시작해 함수에 전달된 나머지 인수가 모두 단일 배열에 저장되어야 함을 나타낸다.

TypeScript는 이러한 나머지 매개변수의 타입을 일반 매개변수와 유사하게 선언할 수 있다. 단, 인수 배열을 나타내기 위해 끝에 `[]` 구문이 추가된다는 점만 다르다.

```typescript
function singAllTheSongs(singer: string, ...songs: string[]) {
  for (const song of songs) {
    console.log(`${song}, by ${singer}`);
  }
}

singAllTheSongs("Alicia Keys"); // Ok
singAllTheSongs("Lady Gaga", "Bad Romance", "Just Dance", "Poker Face"); // Ok

singAllTheSongs("Ella Fitzgerald", 2000);
//
// Error: Argument of type 'number' is not assignable to parameter of type 'string'.
```

## 반환 타입
TypeScript는 지각적이다. 함수가 반환할 수 있는 가능한 모든 값을 이해하면 함수가 반환하는 타입을 알 수 있다.

```typescript
// 타입: (songs: string[]) => number
function singSongs(songs: string[]) {
  for (const song of songs) {
    console.log(`${song}`);
  }

  return songs.length;
}
```

함수에 다른 값을 가진 여러 개의 반환문을 포함하고 있다면, TypeScript는 반환 타입을 가능한 모든 반환 타입의 조합으로 유추한다.

```typescript
// 타입: (songs: string[], index: number) => string | undefined
function getSongAt(songs: string[], index: number) {
  return index < songs.length
      ? songs[index]
      : undefined;
}
```

### 명시적 반환 타입
변수와 마찬가지로 타입 애너테이션을 사용해 함수의 반환 타입을 명시적으로 선언하지 않는 것이 좋다. 그러나 특히 함수에서 반환 타입을 명시적으로 선언하는 방식이 매우 유용할 때가 종종 있다.

- 가능한 반환값이 많은 함수가 항상 동일한 타입의 값을 반환하도록 강제한다.
- TypeScript는 재귀 함수의 반환 타입을 통해 타입을 유추하는 것을 거부한다.
- 수백 개 이상의 TypeScript 파일이 있는 매우 큰 프로젝트에서 TypeScript 타입 검사 속도를 높일 수 있다.

함수 선언 반환 타입 애너테이션은 매개변수 목록이 끝나는 `)` 다음에 배치된다. 함수 선언의 경우는 `{` 앞에 배치된다.

```typescript
function singSongsRecursive(songs: string[], count = 0): number {
  return songs.length ? singSongsRecursive(songs.slice(1), count + 1) : count;
}

const singSongsRecursive = (songs: string[], count = 0): number =>
    songs.length ? singSongsRecursive(songs.slice(1), count + 1) : count;
```

함수의 반환문이 함수의 반환 타입으로 할당할 수 없는 값을 반환하는 경우 TypeScript는 할당 가능성 오류를 표시한다.

```typescript
function getSongRecordingDate(song: string):
Date | undefined {
  switch (song) {
    case "Strange Fruit":
      return new Date('April 20, 1939'); // Ok
    
    case "Greensleeves":
      return "unknown";
      //
      // Error: Type 'string' is not assignable to type 'Date'.
    default:
      return undefined; // Ok
  }
}
```

## 함수 타입
JavaScript에선 함수를 값으로 전달할 수 있다. 즉, 함수를 가지기 위한 매개변수 또는 변수의 타입을 선언하는 방법이 필요하다.

함수 타입 구문은 화살표 함수와 유사하지만 함수 본문 대신 타입이 있다.

```typescript
let nothingInGivesString: () => string;
```

```typescript
let inputAndOutput: (songs: string[], count?: number) => number;
```

**함수 타입은 콜백 매개변수를 설명하는 데 자주 사용된다.**

```typescript
const songs = ["Juice", "Shake It Off", "What's Up"];

function runOnSongs(getSongAt: (index: number) => string) {
  for (let i = 0; i < songs.length; i += 1) {
    console.log(getSongAt(i));
  }
}

function getSongAt(index: number) {
  return `${songs[index]}`;
}

runOnSongs(getSongAt); // Ok

function logSong(song: string) {
  return `${song}`;
}

runOnSongs(logSong);
//
// Error: Argument of type '(song: string) => string' is not
// assignable to parameter of type '(index: number) => string'.
//  Type of parameters 'song' and 'index' ar incompatible.
//    Type 'number' is not assignable to type 'string'.
```

`runOnSongs(logSong)`에 대한 오류 메시지는 할당 가능성 오류의 예로 몇 가지 상세한 단계까지 제공한다. 두 함수를 서로 할당할 수 없다는 오류를 출력할 때 TypeScript는 일반적으로 세 가지 상세한 단계를 제공한다.

1. 첫 번째 들여쓰기 단계는 두 함수 타입을 출력한다.
2. 다음 들여쓰기 단계는 일치하지 않는 부분을 지정한다.
3. 마지막 들여쓰기 단계는 일치하지 않는 부분에 대한 정확한 할당 가능성 오류를 출력한다.

### 함수 타입 괄호
함수 타입은 다른 타입이 사용되는 모든 곳에 배치할 수 있다. 여기에는 유니언 타입도 포함된다.

유니언 타입의 애너테이션에서 함수 반환 위치를 나타내거나, 유니언 타입을 감싸는 부분을 표시할 때 괄호를 사용한다.

```typescript
// 타입은 string | undefined 유니언을 반환하는 함수
let returnsStringOrUndefined: () => string | undefined;

// 타입은 undefined나 string을 반환하는 함수
let maybeReturnsString: (() => string) | undefined;
```

### 매개변수 타입 추론
매개변수로 사용되는 인라인 함수를 포함하여 작성한 모든 함수에 대해 매개변수를 선언해야 한다면 번거로울 것이다. 다행히도 TypeScript는 선언된 타입의 위치에 제공된 함수의 매개변수 타입을 유추할 수 있다.

```typescript
let singer: (song: string) => string;

singer = function (song) {
  // song: string의 타입
  return `Singing: ${song.toUpperCase()}!`; // Ok
}
```

함수를 매개변수로 갖는 함수에 인수로 전달된 함수는 해당 매개변수 타입도 잘 유추할 수 있다.

```typescript
const songs = ["Call Me", "Jolene", "The Chain"];

// song: string
// index: number
songs.forEach((song, index) => {
  console.log(`${song} is at index ${index}`);
});
```

### 함수 타입 별칭
```typescript
type StringToNumber = (input: string) => number;

let stringToNumber: StringToNumber;

stringToNumber = (input) => input.length; // Ok

stringToNumber = (input) => input.toUpperCase();
//
// Error: Type 'string' is not assignable to type 'number'.
```

```typescript
type NumberToString = (input: number) => string;

function usesNumberToString(numberToString: NumberToString) {
  console.log(`The string is: ${numberToString(1234)}`);
}

usesNumberToString((input) => `${input}! Hooray!`); // Ok
usesNumberToString((input) => input * 2);
//
// Error: Type 'number' is not assignable to type 'string'.
```

타입 별칭은 특히 함수 타입에 유용하다. 타입 별칭을 이용하면 반복적으로 작성하는 매개변수와 반환 타입을 갖는 코드 공간을 많이 절약할 수 있다.

## 그 외 반환 타입
### void 반환 타입
일부 함수는 어떤 값도 반환하지 않는다. **TypeScript는 `void` 키워드를 사용해 반환 값이 없는 함수의 반환 타입을 확인할 수 있다.**

반환 타입이 `void`인 함수는 값을 반환하지 않을 수 있다. 다음 `logSong` 함수는 `void`를 반환하도록 선언되었으므로 값 반환을 허용하지 않습니다.

```typescript
function logSong(song: string | undefined): void {
  if (!song) {
    return; // Ok
  }

  console.log(`${song}`);

  return true;
  //
  // Error: Type 'boolean' is not assignable to type 'void'.
}
```

함수 타입 선언 시 `void` 반환 타입은 매우 유용하다. 함수 타입을 선언할 때 `void`를 사용하면 함수에서 반환되는 모든 값은 무시된다.

```typescript
let songLogger: (song: string) => void;

songLogger = (song) => {
  console.log(`${songs}`);
};

songLogger("Heart of Glass"); // Ok
```

JavaScript 함수는 실젯값이 반환되지 않으면 기본으로 모두 `undefined`를 반환하지만 `void`는 `undefined`와 동일하지 않는다. `void`는 함수의 반환 타입이 무시된다는 것을 의미하고 `undefined`는 반환되는 리터럴 값이다. `undefined`를 포함하는 대신 `void` 타입의 값을 할당하려고 하면 타입 오류가 발생한다.

```typescript
function returnsVoid() {
  return;
}

let lazyValue: string | undefined;

lazyValue = returnsVoid();
//
// Error: Type 'void' is not assignable to type 'string | undefined'
```

`undefined`와 `void`를 구분해서 사용하면 매우 유용하다. 특히 `void`를 반환하도록 선언된 타입 위치에 전달된 함수가 반환된 모든 값을 무시하도록 설정할 때 유용하다.

예를 들어 배열의 내장 `forEach` 메서드는 `void`를 반환하는 콜백을 받는다. `forEach`에 제공되는 함수는 원하는 모든 값을 반환할 수 있다. 다음 `saveRecords` 함수의 `records.push(record)`는 `number`를 반환하지만, 여전히 `newRecords.forEach`에 전달된 화살표 함수에 대한 반환값이 허용된다.

```typescript
const records: string[] = [];

function saveRecords(newRecords: string[]) {
  newRecords.forEach(record => records.push(record));
}

saveRecords(['21', 'Come On Over', 'The Bodyguard'])
```

`void` 타입은 JavaScript가 아닌 함수의 반환 타입을 선언하는 데 사용하는 TypeScript 키워드다. `void` 타입은 함수의 반환값이 자체적으로 반환될 수 있는 값도 아니고, 사용하기 위한 것도 아니라는 표시임을 기억하다.

### never 반환 타입
일부 함수는 값을 반환하지 않을 뿐만 아니라 반환할 생각도 전혀 없다. `never` 반환 함수는 (의도적으로) 항상 오류를 발생시키거나 무한 루프를 실행하는 함수다.

함수가 절대 반환하지 않도록 의도하려면 명시적 `: never` 타입 애너테이션을 추가해 해당 함수를 호출한 후 모든 코드가 실행되지 않음을 나타낸다. 다음 `fail` 함수는 오류만 발생시키므로 `param`의 타입을 `string`으로 좁혀서 TypeScript의 제어 흐름 분석을 도와준다.

```typescript
function fail(message: string): never {
  throw new Error(`Invariant failuer: ${message}.`);
}

function workWithUnsafeParam(param: unknown) {
  if (typeof param !== "string") {
    fail(`param should be a string, not ${typeof param}`);
  }

  // 여기에서 param의 타입은 string으로 알려진다.
  param.toUpperCase(); // Ok
}
```

참고로 `never`는 `void`와 다르다. `void`는 아무것도 반환하지 않는 함수를 위한 것이고, `never`는 절대 반환하지 않는 함수를 위한 것이다.

## 함수 오버로드
