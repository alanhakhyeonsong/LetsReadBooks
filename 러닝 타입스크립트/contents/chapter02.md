# Chapter 2. 타입 시스템
## 타입의 종류
'타입'은 JavaScript에서 다루는 값의 형태에 대한 설명이다. 여기서 '형태'란 값에 존재하는 속성과 메서드 그리고 내장되어 있는 `typeof` 연산자가 설명하는 것을 의미한다.

TypeScript의 가장 기본적인 타입은 JavaScript의 7가지 primitive type과 동일하다.

- `null`
- `undefined`
- `boolean` : `true`, `false`
- `string`
- `number` : `0`, `2.1`, `-4`
- `bignit` : `0n`, `2n`
- `symbol` : `Symbol()`, `Symbol("hi")`

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/7900febe-1569-467d-bca6-371c57c1b4bd)

> 📌 JavaScript에서 객체와 원시 타입 간의 차이점을 떠올려보자. `Boolean`과 `Number` 같은 객체는 각 원싯값을 감싸는 객체다. TypeScript에선 일반적으로 `boolean`과 `number` 처럼 소문자로 참조하는 것이 모범 사례다.

### 타입 시스템
타입 시스템은 프로그래밍 언어가 프로그램에서 가질 수 있는 타입을 이해하는 방법에 대한 규칙 집합이다.

기본적으로 TypeScript의 타입 시스템은 다음과 같이 작동한다.

1. 코드를 읽고 존재하는 모든 타입과 값을 이해한다.
2. 각 값이 초기 선언에서 가질 수 있는 타입을 확인한다.
3. 각 값이 추후 코드에서 어떻게 사용될 수 있는지 모든 방법을 확인한다.
4. 값의 사용법이 타입과 일치하지 않으면 사용자에게 오류를 표시한다.

타입 추론 과정을 자세히 살펴보자. 다음은 TypeScript가 멤버 속성을 함수로 잘못 호출해 타입 오류가 발생하는 코드다.

```typescript
let firstName = "Sergio";
firstName.length();
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a3cdb257-57a2-42c5-ae32-fa72092b5346)

TypeScript는 다음과 같은 순서로 오류를 표시한다.

1. 코드를 읽고 `firstName`이라는 변수를 이해한다.
2. 초깃값이 `"Sergio"` 이므로 `firstName`이 `string` 타입이라 결론짓는다.
3. `firstName`의 `.length` 멤버를 함수처럼 호출하는 코드를 확인한다.
4. `string`의 `.length` 멤버는 함수가 아닌 숫자라는 오류를 표시한다. 즉, 함수처럼 호출할 수 없다.

TypeScript의 타입 시스템에 대한 이해는 TypeScript 코드를 이해하는 데 중요한 기술이다. 뒤에 나올 코드 스니펫은 TypeScript가 코드로부터 추론할 수 있는 점점 더 복잡한 타입을 보여준다.

### 오류 종류
가장 자주 접하게 되는 오류 두 가지는 다음과 같다.

- 구문 오류: TypeScript가 JavaScript로 변환되는 것을 차단한 경우
- 타입 오류: 타입 검사기에 따라 일치하지 않는 것이 감지된 경우

#### 구문 오류
TypeScript가 코드로 이해할 수 없는 잘못된 구문을 감지할 때 발생한다. 이는 TypeScript가 TypeScript 파일에서 JavaScript 파일을 올바르게 생성할 수 없도록 차단한다.

TypeScript 코드를 JavaScript로 변환하는 데 사용하는 도구와 설정에 따라 JavaScript 코드를 얻을 수도 있다.(tsc 기본 설정에선 가능하다.) 하지만 결과가 예상과 상당히 다를 수 있다.

```typescript
let let wat;
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/87b22a5b-947a-4443-a464-87f367eaadd7)

```javascript
// JavaScript 컴파일러 버전에 따라 컴파일된 JavaScript 결과
let let, wat;
```

#### 타입 오류
TypeScript의 타입 검사기가 프로그램의 타입에서 오류를 감지했을 때 발생한다. 오류가 발생했다고 해서 TypeScript 구문이 JavaScript로 변환되는 것을 차단하진 않는다. 하지만 코드가 실행되면 무언가 충돌하거나 예기치 않게 작동할 수 있음을 나타낸다.

TypeScript는 타입 오류가 있음에도 불구하고 JavaScript 코드를 출력할 수 있지만, 출력된 JavaScript 코드가 원하는 대로 실행되지 않을 가능성이 있다는 신호를 타입 오류로 알려준다. JavaScript를 실행하기 전에 타입 오류를 확인하고 발견된 문제를 먼저 해결하는 것이 가장 좋다.

## 할당 가능성
TypeScript는 변수의 초깃값을 읽고 해당 변수가 허용되는 타입을 결정한다. 나중에 해당 변수에 새로운 값이 할당되면, 새롭게 할당된 값의 타입이 변수의 타입과 동일한지 확인한다.

TypeScript 변수에 동일한 타입의 다른 값이 할당될 때는 문제가 없다.

```typescript
let firstName = "Sergio";
firstName = "sergio";
```

하지만 TypeScript 변수에 다른 타입의 값이 할당되면 타입 오류가 발생한다.

```typescript
let firstName = "Sergio";
firstName = true;
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/db54ee51-02fa-43a7-b609-b99eeaad08a5)

TypeScript에서 함수 호출이나 변수에 값을 제공할 수 있는지 여부를 확인하는 것을 **할당 가능성**이라 한다. 즉, 전달된 값이 예상된 타입으로 할당 가능한지 여부를 확인한다.

### 할당 가능성 오류 이해하기
`'Type...is not assignable to type...'` 형태의 오류는 TypeScript 코드를 작성할 때 만나게 되는 가장 일반적인 오류 중 하나다.

해당 오류 메시지에서 언급된 첫 번째 `type`은 코드에서 변수에 할당하려 시도하는 값이다. 두 번째 `type`은 첫 번째 타입, 즉, 값이 할당되는 변수다. 예를 들어 이전 코드 스니펫에서 `lastName = true`를 작성할 때 `boolean` 타입인 `true` 값을 `string` 타입인 변수 `lastName`에 할당하려 했다.

## 타입 애너테이션
때로는 변수에 TypeScript가 읽어야 할 초깃값이 없는 경우도 있다. TypeScript는 나중에 사용할 변수의 초기 타입을 파악하려고 시도하지 않는다. 그리고 기본적으로 변수를 암묵적인 `any` 타입으로 간주한다. 즉, 변수는 세상의 모든 것이 될 수 있음을 나타낸다.

초기 타입을 유추할 수 없는 변수는 **진화하는 `any`** 라고 부른다. 특정 타입을 강제하는 대신 새로운 값이 할당될 때마다 변수 타입에 대한 이해를 발전시킨다.

```typescript
let name; // 타입: any

name = "Ramos"; // 타입: string
name.toUpperCase(); // Ok

name = 19.58; // 타입: number
name.toPrecision(1); // ok

name.toUpperCase();
//
// Error: 'toUpperCase' does not exist on type 'number'.
```

TypeScript는 `number` 타입으로 진화한 변수가 `toUpperCase()` 메서드를 호출하는 것을 포착했다. 그러나 변수가 `string` 타입에서 `number` 타입으로 진화된 것이 처음부터 의도된 것인지에 대한 여부는 더 일찍 알 수 없다.

일반적으로 `any` 타입을 사용해 `any` 타입으로 진화하는 것을 허용하게 되면 TypeScript의 타입 검사 목적을 부분적으로 쓸모없게 만든다. TypeScript는 값이 어떤 타입인지 알고 있을 때 가장 잘 작동한다. `any` 타입을 가진 값에는 TypeScript의 타입 검사 기능을 잘 적용할 수 없다. 검사를 위해 알려진 타입이 없기 때문이다.

TypeScript는 초깃값을 할당하지 않고도 변수의 타입을 선언할 수 있는 구문인 **타입 에너테이션**을 제공한다. 이는 변수 이름 뒤에 배치되며 콜론과 타입 이름을 차례대로 기재한다.

```typescript
let name: string;
name = "Ramos";
```

이러한 타입 애너테이션은 TypeScript에만 존재하며 런타임 코드에 영향을 주지도 않고, 유효한 JavaScript 구문도 아니다. `tsc` 명령어를 실행해 TypeScript 소스 코드를 JavaScript로 컴파일하면 해당 코드가 삭제된다.

```javascript
// 출력된 .js 파일
let name;
name = "Ramos";
```

변수에 타입 애너테이션으로 정의한 타입 외의 값을 할당하면 타입 오류가 발생한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6cc41ddf-e2b7-46d0-bd7c-fa3c5ed43d27)

### 불필요한 타입 애너테이션
타입 애너테이션은 TypeScript가 자체적으로 수집할 수 없는 정보를 TypeScript에 제공할 수 있다. 타입을 즉시 유추할 수 있는 변수에도 타입 애너테이션을 사용할 수 있다. 하지만 TypeScript가 아직 알지 못하는 것은 알려주지 못한다.

```typescript
// 타입 애너테이션 중복. 이미 유추할 수 있기 때문.
let firstName: string = "Tina"; // 타입 시스템은 변경되지 않음
```

초깃값이 있는 변수에 타입 애너테이션을 추가하면 TypeScript는 변수에 할당된 값의 타입이 일치하는지 확인한다.

많은 개발자들은 아무것도 변하지 않는 변수에는 타입 애너테이션을 추가하지 않기를 선호한다. 타입 애너테이션을 수동으로 작성하는 일은 번거롭다. 특히 타입이 변경되거나 복잡한 타입일 때 더욱 그렇다. 코드를 명확하게 문서화하거나 실수로 변수 타입이 변경되지 않도록 타입스크립트를 보호하기 위해 변수에 명시적으로 타입 애너테이션을 포함하는 것이 경우에 따라선 유용할 수 있다.

## 타입 형태
TypeScript는 변수에 할당된 값이 원래 타입과 일치하는지 확인하는 것 이상을 수행한다. TypeScript는 객체에 어떤 멤버 속성이 존재하는지 알고 있다. 만약 코드에서 변수의 속성에 접근하려 한다면 TypeScript는 접근하려는 속성이 해당 변수의 타입에 존재하는지 확인한다.

`string` 타입의 `rapper` 변수를 선언한다 가정하자. 나중에 이 변수를 사용할 때 TypeScript가 `string` 타입에서 사용 가능한 작업만을 허용한다.

```typescript
let rapper = "Queen Latifah";
rapper.length; // Ok
```

TypeScript가 `string` 타입에서 작동하는지 알 수 없는 작업은 허용되지 않는다.

```typescript
rapper.push('!');
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e1508496-9e66-4fdd-8737-166b97e26612)

타입은 더 복잡한 형태, 특히 객체일 수도 있다. 아래 예제에서 TypeScript는 `cher` 객체에 `middleName` 키가 없다는 것을 알고 오류를 표시한다.

```typescript
let cher = {
  firstName: "Cherilyn",
  lastName: "Sarkisian",
};

cher.middleName;
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0c85985f-cfc8-4a7a-a130-a18222b15e2b)

TypeScript는 객체의 형태에 대한 이해를 바탕으로 할당 가능성뿐만 아니라 객체 사용과 관련된 문제도 알려준다.

### 모듈
JavaScript는 비교적 최근까지 서로 다른 파일에 작성된 코드를 공유하는 방법과 관련된 사양을 제공하지 않았다. ES6에는 파일 간에 가져오고 내보내는 구문을 표준화하기 위해 ECMAScript Modules가 추가되었다.

```javascript
import { value } from "./values";

export const doubled = value * 2;
```

- 모듈: `export` 또는 `import`가 있는 파일
- 스크립트: 모듈이 아닌 모든 파일

TypeScript는 최신 모듈 파일을 기존 파일과 함께 실행할 수 있다. 모듈 파일에 선언된 모든 것은 해당 파일에서 명시한 `export` 문에서 내보내지 않는 한 모듈 파일에서만 사용할 수 있다. 한 모듈에서 다른 파일에 선언된 변수와 동일한 이름으로 선언된 변수는 다른 파일의 변수를 가져오지 않는 한 이름 충돌로 간주하지 않는다.

`a.ts`와 `b.ts` 파일 모두 모듈이고 이름이 동일한 `shared` 변수를 문제없이 내보내는 코드 예제다. `c.ts`는 가져온 `shared` 변수와 `c.ts`에 정의된 `shared` 변수의 이름이 충돌되어 타입 오류가 발생한다.

```typescript
// a.ts
export const shared = "Cher";
```

```typescript
// b.ts
export const shared = "Cher";
```

```typescript
// c.ts
import { shared } from "./a";

export const shared = "Cher";
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/cc65e08c-8288-48c3-8242-b8ea4787ddc9)

**그러나 파일이 스크립트면 TypeScript는 해당 파일을 전역 스코프로 간주하므로 모든 스크립트가 파일의 내용에 접근할 수 있다.** 즉, 스크립트 파일에 선언된 변수는 다른 스크립트 파일에 선언된 변수와 동일한 이름을 가질 수 없다.