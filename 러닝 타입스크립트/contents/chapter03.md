# Chapter 3. 유니언과 리터럴
TypeScript가 해당 값을 바탕으로 추론을 수행하는 두 가지 핵심 개념을 알아보자.

- 유니언(union): 값에 허용된 타입을 두 개 이상의 가능한 타입으로 확장하는 것
- 내로잉(narrowing): 값에 허용된 타입이 하나 이상의 가능한 타입이 되지 않도록 좁히는 것

유니언과 내로잉은 다른 주요 프로그래밍 언어에선 불가능하지만 TypeScript에선 가능한 '코드 정보에 입각한 추론'을 해내는 강력한 개념이다.

## 유니언 타입
```typescript
let mathematician = Math.random() > 0.5
    ? undefined
    : "Ramos";
```

`mathematician`은 어떤 타입일까? 둘 다 잠재적인 타입이긴 하지만 무조건 `undefined`거나 혹은 무조건 `string`인 것도 아니다. '이거 혹은 저거'와 같은 타입을 **유니언**이라 한다. 이 타입은 값이 정확히 어떤 타입인지 모르지만 두 개 이상의 옵션 중 하나라는 것을 알고 있는 경우에 코드를 처리하는 훌륭한 개념이다.

TypeScript는 가능한 값 또는 구성 요소 사이에 `|`(수직선) 연산자를 사용해 유니언 타입을 나타낸다.

### 유니언 타입 선언
변수의 초깃값이 있더라도 변수에 대한 명시적 타입 애너테이션을 제공하는 것이 유용할 때 유니언 타입을 사용한다.

아래 예제는 `thinker`의 초깃값은 `null`이지만 잠재적으로 `null` 대신 `string`이 될 수 있음을 알려준다. 명시적으로 `string | null` 타입 애너테이션은 TypeScript가 `thinker`의 값으로 `string` 타입의 값을 할당할 수 있음을 의미한다.
```typescript
let thinker: string | null = null;

if (Math.random() > 0.5) {
    thinker = "Ramos"; // Ok
}
```

유니언 타입 선언은 타입 애너테이션으로 타입을 정의하는 모든 곳에서 사용할 수 있다.

### 유니언 속성
**값이 유니언 타입일 때 TypeScript는 유니언으로 선언한 모든 가능한 타입에 존재하는 멤버 속성에만 접근할 수 있다.** 유니언 외의 타입에 접근하려고 하면 타입 검사 오류가 발생한다.

```typescript
let physicist = Math.random() > 0.5
    ? "Marie Curie"
    : 84

physicist.toString(); // Ok

physicist.toUpperCase();
// 
// Error: Property 'toUpperCase' does not exist on type 'string | number'.
//   Property 'toUpperCase' does not exist on type 'number'.

physicist.toFixed();
//
// Error: Property 'toFixed' does not exist on type 'string | number'.
//   Property 'toFixed' does not exist on type 'string'.
```

모든 유니언 타입에 존재하지 않는 속성에 대한 접근을 제한하는 것은 안전 조치에 해당한다. 객체가 어떤 속성을 포함한 타입으로 확실하게 알려지지 않은 경우, TypeScript는 해당 속성을 사용하려고 시도하는 것이 안전하지 않다고 여긴다. 그런 속성이 존재하지 않을 수도 있기 때문이다.

유니언 타입으로 정의된 여러 타입 중 하나의 타입으로 된 값의 속성을 사용하려면 코드에서 값이 보다 구체적인 타입 중 하나라는 것을 TypeScript에 알려야 한다. 이 과정을 **내로잉**이라 한다.

## 내로잉
내로잉은 값이 정의, 선언 혹은 이전에 유추된 것보다 더 구체적인 타입임을 코드에서 유추하는 것이다. TypeScript가 값의 타입이 이전에 알려진 것보다 더 좁혀졌다는 것을 알게 되면 값을 더 구체적인 타입으로 취급한다. 타입을 좁히는 데 사용할 수 있는 논리적 검사를 **타입 가드**라고 한다.

### 값 할당을 통한 내로잉
변수에 값을 직접 할당하면 TypeScript는 변수의 타입을 할당된 값의 타입으로 좁힌다.

```typescript
let admiral: number | string;

admiral = "Grace Hopper";

admiral.toUpperCase(); // Ok: string

admiral.toFixed();
//
// Error: Property 'toFixed' does not exist on type 'string'.
```

**변수에 유니언 타입 애너테이션이 명시되고 초깃값이 주어질 때 값 할당 내로잉이 작동한다.** TypeScript는 변수가 나중에 유니언 타입으로 선언된 타입 중 하나의 값을 받을 수 있지만, 처음에는 초기에 할당된 값의 타입으로 시작한다는 것을 이해한다.

다음 코드에서 `inventor`는 `number | string` 타입으로 선언되었지만 초깃값으로 문자열이 할당되었기 때문에 타입스크립트는 즉시 `string` 타입으로 바로 좁혀졌다는 것을 알고 있다.

```typescript
let inventor: number | string = "Hedy Lamarr";

inventor.toUpperCase(); // Ok: string

inventor.toFixed();
//
// Error: Property 'toFixed' does not exist on type 'string'.
```

### 조건 검사를 통한 내로잉
일반적으로 TypeScript에선 변수가 알려진 값과 같은지 확인하는 `if` 문을 통해 변수의 값을 좁히는 방법을 사용한다. TypeScript는 `if` 문 내에서 변수가 알려진 값과 동일한 타입인지 확인한다.

```typescript
// scientist: number | string의 타입
let scientist = Math.random() > 0.5
    ? "Rosalind Franklin"
    : 51;

if (scientist === "Rosalind Franklin") {
    // scientist: string의 타입
    scientist.toUpperCase(); // Ok
}

// scientist: number | string의 타입
scientist.toUpperCase();
//
// Error: Property 'toUpperCase' does not exist on type 'string | number'.
//   Property 'toUpperCase' does not exist on type 'number'.
```

조건부 로직으로 내로잉할 때, TypeScript 타입 검사 로직은 훌륭한 JavaScript 코딩 패턴을 미러링해 구현한다. 만약 변수가 여러 타입 중 하나라면, 일반적으로 필요한 타입과 관련된 검사를 원할 것이다. TypeScript는 강제로 코드를 안전하게 작성할 수 있도록 하는 고마운 언어다.

### typeof 검사를 통한 내로잉
TypeScript는 직접 값을 확인해 타입을 좁히기도 하지만, `typeof` 연산자를 사용할 수도 있다.

```typescript
let researcher = Math.random() > 0.5
    ? "Rosalind Franklin"
    : 51;

if (typeof researcher === "string") {
    researcher.toUpperCase(); // Ok: string
}
```

`!`를 사용한 논리적 부정과 `else` 문도 잘 작동한다.

```typescript
if (!(typeof researcher === "string")) {
    researcher.toFixed(); // Ok: number
} else {
    researcher.toUpperCase(); // Ok: string
}
```

이러한 코드 스니펫은 타입 내로잉에서도 지원되는 삼항 연산자를 이용해 다시 작성할 수 있다.

```typescript
typeof researcher === "string"
    ? researcher.toUpperCase() // Ok: string
    : researcher.toFixed(); // Ok: number
```

어떤 방법으로 작성하든 `typeof` 검사는 타입을 좁히기 위해 자주 사용하는 실용적인 방법이다.

## 리터럴 타입
리터럴 타입은 좀 더 구체적인 버전의 원시 타입이다.

```typescript
const philosopher = "Hypatia";
```

`philosopher`는 어떤 타입일까? `string` 타입이다. 하지만 이 변수는 단지 `string` 타입이 아닌 `"Hypatia"`라는 특별한 값이다. 따라서 변수 `philosopher`의 타입은 기술적으로 더 구체적인 `"Hypatia"`이다.

이것이 바로 리터럴 타입의 개념이다. **원시 타입 값 중 어떤 것이 아닌 특정 원싯값으로 알려진 타입이 리터럴 타입이다.** 원시 타입 `string`은 존재할 수 있는 모든 가능한 문자열의 집합을 나타낸다. 하지만 리터럴 타입인 `"Hypatia"`는 하나의 문자열만 나타낸다.

만약 변수를 `const`로 선언하고 직접 리터럴 값을 할당하면 TypeScript는 해당 변수를 할당된 리터럴 값으로 유추한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/294a2c82-ae53-4ff5-aeb1-596bcf6136c5)

각 원시 타입은 해당 타입이 가질 수 있는 가능한 모든 리터럴 값의 전체 조합으로 생각할 수 있다. 즉, 원시 타입은 해당 타입의 가능한 모든 리터럴 값의 집합니다.

`boolean`, `null`, `undefined` 타입 외에 `number`, `string`과 같은 모든 원시 타입에는 무한한 수의 리터럴 타입이 있다. 일반적인 TypeScript 코드에서 발견할 수 있는 타입은 다음과 같다.

- `boolean` : `true | false`
- `null`과 `undefined` : 둘 다 자기 자신. 즉, 오직 하나의 리터럴 값만 가짐
- `number` : `0 | 1 | 2 | ... | 0.1 | 0.2 | ...`
- `string` : `"" | "a" | "b" | ... | "aa" | "ab" | ...`

유니언 타입 애너테이션에선 리터럴과 원시 타입을 섞어 사용할 수 있다. 예를 들어 `lifespan`은 `number` 타입이거나 선언된 `"ongoing"` 혹은 `"uncertain"` 값 중 하나로 나타낼 수 있다.

```typescript
let lifespan: number | "ongoing" | "uncertain";

lifespan = 89; // Ok
lifespan = "ongoing"; // Ok

lifespan = true;
//
// Error: Type 'true' is not assignable to type 'number | "ongoing" | "uncertain"'.
```

### 리터럴 할당 가능성
앞서 `number`와 `string`과 같은 서로 다른 원시 타입이 서로 할당되지 못한다는 것을 보았다. 마찬가지로 0과 1처럼 동일한 원시 타입일지라도 서로 다른 리터럴 타입은 서로 할당할 수 없다.

```typescript
let specificallyAda: "Ada";

specificallyAda = "Ada"; // Ok

specificallyAda = "Byron";
//
// Error: Type '"Byron"' is not assignable to type '"Ada"'.

let someString = ""; // 타입: string

specificallyAda = someString;
//
// Error: Type 'string' is not assignable to type '"Ada"'.
```

그러나 리터럴 타입은 그 값이 해당하는 원시 타입에 할당할 수 있다. 모든 특정 리터럴 문자열은 여전히 `string` 타입이기 때문이다.

```typescript
// 앞서 string 타입으로 간주된 someString 변수에 할당된다.
someString = ":)";
```

## 엄격한 null 검사
리터럴로 좁혀진 유니언의 힘은 TypeScript에서 **엄격한 `null` 검사**라 부르는 타입 시스템 영역인 '잠재적으로 정의되지 않은 `undefined` 값'으로 작업할 때 특히 두드러진다. TypeScript는 두려운 '십억 달러의 실수'를 바로잡기 위해 엄격한 `null` 검사를 사용하며 이는 최신 프로그래밍 언어의 큰 변화 중 하나다.

TypeScript 컴파일러는 실행 방식을 변경할 수 있는 다양한 옵션을 제공한다. 가장 유용한 옵션 중 하나인 `strictNullChecks`는 엄격한 `null` 검사를 활성화할지 여부를 결정한다. 간략하게 설명하면, `strictNullChecks`를 비활성화하면 코드의 모든 타입에 `| null | undefined`를 추가해야 모든 변수가 `null` 또는 `undefined`를 할당할 수 있다.

`strictNullChecks` 옵션을 `false`로 설정하면 다음 코드의 타입은 완벽히 안전하다고 간주된다. 하지만 틀렸다. `nameMaybe` 변수가 `.toLowerCase`에 접근할 때 `undefined`가 되는 것은 잘못된 것이다.

```typescript
let nameMaybe = Math.random() > 0.5
    ? "Tony Hoare"
    : undefined;

nameMaybe.toLowerCase();
// Potential runtime error: Cannot read property 'toLowerCase' of undefined.
```

엄격한 `null` 검사가 활성화되면, TypeScript는 다음 코드에서 발생하게 될 잠재적인 충돌을 확인한다.

```typescript
let nameMaybe = Math.random() > 0.5
    ? "Tony Hoare"
    : undefined;

nameMaybe.toLowerCase();
//
// Error: Object is possibly 'undefined'.
```

엄격한 `null` 검사를 활성화해야만 코드가 `null` 또는 `undefined` 값으로 인한 오류로부터 안전한지 여부를 쉽게 파악할 수 있다.

TypeScript의 모범 사례는 일반적으로 엄격한 `null` 검사를 활성화 하는 것이다. 그렇게 해야만 충돌을 방지하고 십억 달러의 실수를 제거할 수 있다.

### 참 검사를 통한 내로잉
JavaScript에서 참 또는 truthy는 `&&` 연산자 또는 `if` 문처럼 `boolean` 문맥에서 `true`로 간주된다는 점을 떠올려보자. JavaScript에서 `false`, `0`, `-0`, `0n` `""`, `null`, `undefined`, `NaN` 처럼 falsy로 정의된 값을 제외한 모든 값은 모두 참이다.

TypeScript는 잠재적인 값 중 truthy로 확인된 일부에 한해서만 변수의 타입을 좁힐 수 있다. 다음 코드에서 `geneticist`는 `string | undefined` 타입이며 `undefined` 타입은 항상 falsy이므로 TypeScript는 `if` 문의 코드 블록에선 `geneticist`가 `string` 타입이 되어야 한다고 추론할 수 있다.

```typescript
let geneticist = Math.random() > 0.5
    ? "Barbara McClintock"
    : undefined;

if (geneticist) {
    geneticist.toUpperCase(); // Ok: string
}

geneticist.toUpperCase();
//
// Error: Object is possibly 'undefined'.
```

논리 연산자인 `&&`와 `?`는 참 여부를 검사하는 일도 잘 수행한다. 하지만 안타깝게도 참 여부 확인 외에 다른 기능은 제공하지 않는다. `string | undefined` 값에 대해 알고 있는 것이 falsy라면, 그것이 빈 문자열인지 `undefined`인진 알 수 없다.

```typescript
geneticist && geneticist.toUpperCase(); // Ok: string | undefined
geneticist?.toUpperCase(); // Ok: string | undefined
```

```typescript
let biologist = Math.random() > 0.5 && "Rachel Carson";

if (biologist) {
    biologist; // 타입: string
} else {
    biologist; // 타입: false | string
}
```

### 초깃값이 없는 변수
JavaScript에서 초깃값이 없는 변수는 기본적으로 `undefined`가 된다. 이는 타입 시스템에서 극단적인 경우를 나타내기도 한다. 만일 `undefined`를 포함하지 않는 타입으로 변수를 선언한 다음, 값을 할당하기 전에 사용하려 시도하면 어떻게 될까?

TypeScript는 값이 할당될 때까지 변수가 `undefined`임을 이해할 만큼 충분히 영리하다. 값이 할당되기 전에 속성 중 하나에 접근하려는 것처럼 해당 변수를 사용하려 시도하면 다음과 같은 오류 메시지가 나타난다.

```typescript
let mathematician: string;

mathematician?.length;
//
// Error: Variable 'mathematician' is used before being assigned.

mathematician = "Mark Goldberg";
mathematician.length; // Ok
```

변수 타입에 `undefined`가 포함되어 있는 경우엔 오류가 보고되지 않는다. 변수 타입에 `| undefined`를 추가하면, `undefined`는 유효한 타입이기에 사용 전에는 정의할 필요가 없음을 TypeScript에 나타낸다.

```typescript
let mathematician: string | undefined;

mathematician?.length; // Ok

mathematician = "Mark Goldberg";
mathematician.length; // Ok
```

## 타입 별칭
코드에서 볼 수 있는 유니언 타입 대부분은 두세 개의 구성 요소만 갖는다. 그러나 가끔 반복해서 입력하기 불편한 조금 긴 형태의 유니언 타입을 발견할 수 있다.

다음 각 변수는 5개의 가능한 타입 중 하나가 될 수 있다.

```typescript
let rawDataFirst: boolean | number | string | null | undefined;
let rawDataSecond: boolean | number | string | null | undefined;
let rawDataThird: boolean | number | string | null | undefined;
```

TypeScript에는 재사용하는 타입에 더 쉬운 이름을 할당하는 **타입 별칭**이 있다. 이는 `type 새로운 이름 = 타입` 형태를 갖는다. 편의상 타입 별칭은 파스칼 케이스로 이름을 지정한다.

```typescript
type MyName = ...;
```

타입 별칭은 타입 시스템의 '복사해서 붙여넣기'처럼 작동한다. TypeScript가 타입 별칭을 발견하면 해당 별칭이 참조하는 실제 타입을 입력한 것처럼 작동한다. 앞서 살펴본 변수의 타입 애너테이션에서 상당히 길었던 유니언 타입을 타입 별칭을 사용해 다음과 같이 작성할 수 있다.

```typescript
type RawData = boolean | number | string | null | undefined;

let rawDataFirst: RawData;
let rawDataSecond: RawData;
let rawDataThird: RawData;
```

타입 별칭은 타입이 복잡해질 때마다 사용할 수 있는 편리한 기능이다. 여기선 여러 타입을 가질 수 있는 형태의 유니언 타입만 다뤘지만 `array`, `function`, `object` 타입도 포함해보자.

### 타입 별칭은 자바스크립트가 아닙니다
타입 별칭은 타입 애너테이션처럼 JavaScript로 컴파일되지 않는다. 순전히 TypeScript 타입 시스템에만 존재한다.

앞서 다룬 예제는 다음 JavaScript로 컴파일된다.

```javascript
let rawDataFirst;
let rawDataSecond;
let rawDataThird;
```

타입 별칭은 순전히 타입 시스템에만 존재하므로 런타임 코드에선 참조할 수 없다. TypeScript는 런타임에 존재하지 않는 항목에 접근하려고 하면 타입 오류로 알려준다.

```typescript
type SomeType = string | undefined;

console.log(SomeType);
//
// Error: 'SomeType' only refers to a type, but is being used as a value here.
```

다시 말하지만 타입 별칭은 순전히 '개발 시'에만 존재한다.

### 타입 별칭 결합
타입 별칭은 다른 타입 별칭을 참조할 수 있다. 유니언 타입인 타입 별칭 내에 또 다른 유니언 타입인 타입 별칭을 포함하고 있다면 다른 타입 별칭을 참조하는 것이 유용하다.

`IdMaybe` 타입은 `undefined`와 `null`, 그리고 `Id` 내의 타입을 포함한 유니언 타입이다.

```typescript
type Id = number | string;

// IdMaybe 타입은 다음과 같음: number | string | undefined | null
type IdMaybe = Id | undefined | null;
```

사용 순서대로 타입 별칭을 선언할 필요는 없다. 파일 내에서 타입 별칭을 먼저 선언하고 참조할 타입 별칭을 나중에 선언해도 된다.

```typescript
type IdMaybe = Id | undefined | null; // Ok
type Id = number | string;
```