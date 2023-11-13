# Chapter 4. 객체
## 객체 타입
`{...}` 구문을 사용해서 객체 리터럴을 생성하면, TypeScript는 해당 속성을 기반으로 새로운 객체 타입 또는 타입 형태를 고려한다. 해당 객체 타입은 객체의 값과 동일한 속성명과 원시 타입을 갖는다. 값의 속성에 접근하려면 `value.멤버` 또는 `value['멤버']` 구문을 사용한다.

```typescript
const poet = {
    born: 1935,
    name: "Mary Oliver",
};

poet['born']; // 타입: number
poet.name; // 타입: string

poet.end;
//
// Error: Property 'end' does not exist on type '{ born: number; name: string; }'.
```

객체 타입은 TypeScript가 JavaScript 코드를 이해하는 방법에 대한 핵심 개념이다. `null`과 `undefined`를 제외한 모든 값은 그 값에 대한 실제 타입의 멤버 집합을 가지므로 TypeScript는 모든 값의 타입을 확인하기 위해 객체 타입을 이해해야 한다.

### 객체 타입 선언
기존 객체에서 직접 타입을 유추하는 방법도 좋지만, 결국에는 객체의 타입을 명시적으로 선언하고 싶다. 명시적으로 타입이 선언된 객체와는 별도로 객체의 형태를 설명하는 방법이 필요하다.

**객체 타입은 객체 리터럴과 유사하게 보이지만 필드 값 대신 타입을 사용해 설명한다.** TypeScript가 타입 할당 가능성에 대한 오류 메시지에 표시하는 것과 동일한 구문이다.

```typescript
let poetLater: {
    born: number;
    name: string;
};

poetLater = {
    born: 1935,
    name: "Mary Oliver",
};

poetLater = "Sappho";
//
// Error: Type 'string' is not assignable to type '{ born: number; name: string; }'
```

### 별칭 객체 타입
`{ born: number, name: string }`과 같은 객체 타입을 계속 작성하는 일은 매우 귀찮다. **각 객체 타입에 타입 별칭을 할당해 사용하는 방법이 더 일반적이다.**

아래 예제는 TypeScript의 할당 가능성 오류 메시지를 좀 더 직접적으로 읽기 쉽게 만드는 추가 이점이 있다.

```typescript
type Poet = {
    born: number;
    name: string;
};

let poetLater: Poet;

// Ok
poetLater = {
    born: 1935,
    name: "Sara Teasdale",
};

poetLater = "Emily Dickinson";
//
// Error: Type 'string' is not assignable to 'Poet'.
```

## 구조적 타이핑
TypeScript의 타입 시스템은 **구조적으로 타입화**되어 있다. 즉, 타입을 충족하는 모든 값을 해당 타입의 값으로 사용할 수 있다. 다시 말하자면 매개변수나 변수가 특정 객체 타입으로 선언되면 TypeScript에 어떤 객체를 사용하든 해당 속성이 있어야 한다고 말해야 한다.

아래 예제 코드에서 별칭 객체 타입인 `WithFirstName`과 `WithLastName`은 오직 `string` 타입의 단일 멤버만 선언한다. `hasBoth` 변수는 명시적으로 선언되지 않았음에도 두 개의 별칭 객체 타입을 모두 가지므로 두 개의 별칭 객체 타입 내에 선언된 변수를 모두 제공할 수 있다.

```typescript
type WithFirstName {
    firstName: string;
};

type WithLastName {
    lastName: string;
};

const hasBoth = {
    firstName: "Lucille",
    lastName: "Clifton",
};

// Ok: 'hasBoth'는 'string' 타입의 'firstName'을 포함함
let withFirstName: WithFirstName = hasBoth;

// Ok: 'hasBoth'는 'string' 타입의 'lastName'을 포함함
let withLastName: WithLastName = hasBoth;
```

구조적 타이핑은 **덕 타이핑**과는 다르다. 덕 타이핑은 '오리처럼 보이고 오리처럼 꽥꽥거리면, 오리일 것이다'라는 문구에서 유래됐다.
- TypeScript의 타입 검사기에서 구조적 타이핑은 정적 시스템이 타입을 검사하는 경우다.
- 덕 타이핑은 런타임에서 사용될 때까지 객체 타입을 검사하지 않는 것을 말한다.

요약하면, **JavaScript는 덕 타입인 반면 TypeScript는 구조적으로 타입화된다.**

### 사용 검사
객체 타입으로 애너테이션된 위치에 값을 제공할 때 TypeScript는 값을 해당 객체 타입에 할당할 수 있는지 확인한다. 할당하는 값에는 객체 타입의 필수 속성이 있어야 한다. 객체 타입에 필요한 멤버가 객체에 없다면 TypeScript는 타입 오류를 발생시킨다.

아래 예제 코드에서 별칭 객체 타입인 `FirstAndLastNames`는 `first`와 `last` 속성이 모두 있어야 한다. 두 가지 속성을 모두 포함한 객체는 `FirstAndLastNames` 타입으로 선언된 변수에 사용할 수 있지만, 두 가지 속성이 모두 없는 객체는 사용할 수 없다.

```typescript
type FirstAndLastNames = {
    first: string;
    last: string;
};

// Ok
const hasBoth: FirstAndLastNames = {
    first: "Sarojini",
    last: "Naidu",
};

const hasOnlyOne: FirstAndLastNames = {
    //
    // Error: Property 'last' is missing in type '{ first: string; }'
    // but required in type 'FirstAndLastNames'.
    first: "Sappho"
};
```

둘 사이에 일치하지 않는 타입도 허용되지 않는다. 객체 타입은 필수 속성 이름과 해당 속성이 예상되는 타입을 모두 지정한다. 객체의 속성이 일치하지 않으면 TypeScript는 타입 오류를 발생시킨다.

```typescript
type TimeRange = {
    start: Date;
};

const hasStartString: TimeRange = {
    start: "1879-02-13",
    //
    // Error: Type 'string' is not assignable to type 'Date'.
};
```

### 초과 속성 검사
변수가 객체 타입으로 선언되고, 초깃값에 객체 타입에서 정의된 것보다 많은 필드가 있다면 TypeScript에서 타입 오류가 발생한다. 따라서 변수를 객체 타입으로 선언하는 것은 타입 검사기가 해당 타입에 예상되는 필드만 있는지 확인하는 방법이기도 하다.

```typescript
type Poet = {
    born: number;
    name: string;
}

// Ok: Poet 필드와 일치함
const poetMatch: Poet = {
    born: 1928,
    name: "Maya Angelou"
};

const extraProperty: Poet = {
    activity: "walking",
    //
    // Error: Type '{ activity: string; born: number; name: string; }'
    // is not assignable to type 'Poet'.
    //   Object literal may only specify known properties.
    //   and 'activity' does not exist in type 'Poet'.
    born: 1935,
    name: "Mary Oliver",
};
```

초과 속성 검사는 객체 타입으로 선언된 위치에서 생성되는 객체 리터럴에 대해서만 일어난다. 기존 객체 리터럴을 제공하면 초과 속성 검사를 우회한다.

```typescript
const existingObject = {
    activity: "walking",
    born: 1935,
    name: "Mary Oliver",
};

const extraPropertyButOk: Poet = existingObject; // Ok
```

배열 요소, 클래스 필드 및 함수 매개변수가 포함된 객체 타입과 일치할거라 예상되는 위치에서 생성되는, 새로운 객체가 있는 모든 곳에서도 초과 속성 검사가 일어난다. TypeScript에서 초과 속성을 금지하면 코드를 깨끗하게 유지할 수 있고, 예상한 대로 작동하도록 만들 수 있다. 객체 타입에 선언되지 않은 초과 속성은 종종 잘못 입력된 속성 이름이거나 사용되지 않은 코드일 수 있다.

### 중첩된 객체 타입
JavaScript 객체는 다른 객체의 멤버로 중첩될 수 있으므로 TypeScript의 객체 타입도 타입 시스템에서 중첩된 객체 타입을 나타낼 수 있어야 한다. 이를 구현하는 구문은 이전과 동일하지만 기본 이름 대신에 `{...}` 객체 타입을 사용한다.

```typescript
type Poem = {
    author: {
        firstName: string;
        lastName: string;
    };
    name: string;
};

// Ok
const poemMatch: Poem = {
    author: {
        firstName: "Sylvia",
        lastName: "Plath",
    },
    name: "Lady Lazarus",
};

const poemMismatch: Poem = {
    author: {
        name: "Sylvia Plath",
        //
        // Error: Type '{ name: string; }' is not assignable
        // to type '{ firstName: string; lastName: string; }'.
        //   Object literal may only specify known properties, and 'name'
        //   does not exist in type '{ firstName: string; lastName: string; }'.
    },
    name: "Tulips",
};
```

`Poem` 타입을 작성할 때 `author` 속성의 형태를 자체 별칭 객체 타입으로 추출하는 방법도 있다. 중첩된 타입을 자체 타입 별칭으로 추출하면 TypeScript의 타입 오류 메시지에 더 많은 정보를 담을 수 있다. 이 경우에는 `{ firstName: string, lastName: string; }` 대신 `Author`를 사용할 수 있다.

```typescript
type Author = {
    firstName: string;
    lastName: string;
};

type Poem = {
    author: Author;
    name: string;
};

const poemMismatch: Poem = {
    author: {
        name: "Sylvia Plath",
        // 
        // Error: Type '{ name: string; }' is not assignable to type 'Author'.
        //   Object literal may only specify known properties,
        //   and 'name' does not exist in type 'Author'.
    },
    name: "Tulips",
};
```

### 선택적 속성
모든 객체에 객체 타입 속성이 필요한 건 아니다. **타입의 속성 애너테이션에서 `:` 앞에 `?`를 추가하면 선택적 속성임을 나타낼 수 있다.**

아래 예제 코드에서 `Book` 타입은 `pages` 속성만 필요하고 `author` 속성은 선택적으로 허용한다. 객체가 `pages` 속성을 제공하기만 하면 `author` 속성은 제공하거나 생략할 수 있다.

```typescript
type Book = {
    author?: string;
    pages: number;
};

// Ok
const ok: Book = {
    author: "Rita Dove",
    pages: 80,
};

const missing: Book = {
    //
    // Error: Property 'pages' is missing in type
    // '{ pages: number; }' but required in type 'Book'.
    author: "Rita Dove",
};
```

선택적 속성과 `undefined`를 포함한 유니언 타입의 속성 사이에는 차이가 있음을 명심하자. `?`를 사용해 선택적으로 선언된 속성은 존재하지 않아도 된다. 필수로 선언된 속성과 `| undefined`는 그 값이 `undefined`일지라도 반드시 존재해야 한다.

```typescript
type Writers = {
    author: string | undefined;
    editor?: string;
};

// Ok: author는 undefined으로 제공됨
const hasRequired: Writers = {
    author: undefined,
};

const missingRequired: Writers = {};
//
// Error: Property 'author' is missing in type '{}' but required in type 'Writers'.
```

## 객체 타입 유니언
TypeScript에선 속성이 조금 다른, 하나 이상의 서로 다른 객체 타입이 될 수 있는 타입을 설명할 수 있어야 한다. 또한 속성값을 기반으로 해당 객체 타입 간에 타입을 좁혀야 할수도 있다.

### 유추된 객체 타입 유니언
변수에 여러 객체 타입 중 하나가 될 수 있는 초깃값이 주어지면 TypeScript는 해당 타입을 객체 타입 유니언으로 유추한다. 유니언 타입은 가능한 각 객체 타입을 구성하고 있는 요소를 모두 가질 수 있다. 객체 타입에 정의된 각각의 가능한 속성은 비록 초깃값이 없는 선택적(`?`) 타입이지만 각 객체 타입의 구성 요소로 주어진다.

```typescript
const poem = Math.random() > 0.5
    ? { name: "The Double Image", pages: 7 }
    : { name: "Her Kind", rhymes: true };
// 타입:
// {
//     name: string;
//     pages: number;
//     rhymes?: undefined;
// }
// |
// {
//     name: string;
//     pages?: undefined;
//     rhymes: boolean;
// }

poem.name; // string
poem.pages; // number | undefined
poem.rhymes; // boolean | undefined
```

### 명시된 객체 타입 유니언
객체 타입의 조합을 명시하면 객체 타입을 더 명확히 정의할 수 있다. 코드를 조금 더 작성해야 하지만 객체 타입을 더 많이 제어할 수 있다는 이점이 있다. 특히 값의 타입이 객체 타입으로 구성된 유니언이라면 TypeScript의 타입 시스템은 이런 모든 유니언 타입에 존재하는 속성에 대한 접근만 허용한다.

`poem` 변수는 `pages` 또는 `rhymes`와 함께 필수 속성인 `name`을 항상 갖는 유니언 타입으로 명시적으로 작성되었다. 속성 `name`에 접근하는 것은 `name` 속성이 항상 존재하기 때문에 허용되지만 `pages`와 `rhymes`는 항상 존재한다는 보장이 없다.

```typescript
type PoemWithPages = {
    name: string;
    pages: number;
};

type PoemWithRhymes = {
    name: string;
    rhymes: boolean;
};

type Poem = PoemWithPages | PoemWithRhymes;

const poem: Poem = Math.random() > 0.5
    ? { name: "The Double Image", pages: 7 }
    : { name: "Her Kind", rhymes: true };

poem.name; // Ok

pome.pages;
//
// Error: Property 'pages' does not exist on type 'Poem'.
//   Property 'pages' does not exist on type 'PoemWithRhymes'.

poem.rhymes;
//
// Error: Property 'rhymes' does not exist on type 'Poem'.
//   Property 'rhymes' does not exist on type 'PoemWithPages'.
```

잠재적으로 존재하지 않는 객체의 멤버에 대한 접근을 제한하면 코드의 안전을 지킬 수 있다. 값이 여러 타입 중 하나일 경우, 모든 타입에 존재하지 않는 속성이 객체에 존재할 거라 보장할 수 없다.

리터럴 타입이나 원시 타입 모두, 혹은 둘 중 하나로 이루어진 유니언 타입에서 모든 타입에 존재하지 않은 속성에 접근하기 위해 타입을 좁혀야 하는 것처럼 객체 타입 유니언도 타입을 좁혀야 한다.

### 객체 타입 내로잉
타입 검사기가 유니언 타입 값에 특정 속성이 포함된 경우에만 코드 영역을 실행할 수 있음을 알게 되면, 값의 타입을 해당 속성을 포함하는 구성 요소로만 좁힌다. 즉, 코드에서 객체의 형태를 확인하고 타입 내로잉이 객체에 적용된다.

명시적으로 입력된 `poem` 예제를 계속 살펴보면, `poem`의 `pages`가 TypeScript의 타입 가드 역할을 해 `PoemWithPages`임을 나타내는지 확인한다. 만일` Poem`이 `PoemWithPages`가 아니라면 `PoemWithRhymes`이어야 한다.

```typescript
if ("pages" in poem) {
    poem.pages; // Ok: poem은 PoemWithPages로 좁혀짐
} else {
    poem.rhymes; // Ok: poem은 PoemWithRhymes로 좁혀짐
}
```

TypeScript는 `if (poem.pages)`와 같은 형식으로 참 여부를 확인하는 것을 허용하지 않는다. 존재하지 않는 객체 속성에 접근하려 시도하면 타입 가드처럼 작동하는 방식으로 사용되더라도 타입 오류로 간주된다.

```typescript
if (poem.pages) { /* ... */ }
//
// Error: Property 'pages' does not exist on type 'PoemWithPages | PeomWithRhymes'.
//   Property 'pages' does not exist on type 'PoemWithRhymes'.
```

### 판별된 유니언
JavaScript와 TypeScript에서 유니언 타입으로 된 객체의 또 다른 인기 있는 형태는 객체의 속성이 객체의 형태를 나타내도록 하는 것이다. 이렇나 타입 형태를 **판별된 유니언**이라 부르고, 객체의 타입을 가리키는 속성이 **판별값**이다. TypeScript는 코드에서 판별 속성을 사용해 타입 내로잉을 수행한다.

```typescript
type PoemWithPages = {
    name: string;
    pages: number;
    type: 'pages';
};

type PoemWithRhymes = {
    name: string;
    rhymes: boolean;
    type: 'rhymes';
};

type Poem = PoemWithPages | PoemWithRhymes;

const poem: Poem = Math.random() > 0.5
    ? { name: "The Double Image", pages: 7, type: "pages" }
    : { name: "Her Kind", rhymes: true, type: "rhymes" };

if (poem.type === "pages") {
    console.log(`It's got pages: ${poem.pages}`); // Ok
} else {
    console.log(`It rhymes: ${poem.rhymes}`);
}

poem.type; // 타입: 'pages' | 'rhymes'

poem.pages;
//
// Error: Property 'pages' does not exist on type 'Poem'.
//   Property 'pages' does not exist on type 'PoemWithRhymes'.
```

판별된 유니언은 우아한 JavaScript 패턴과 TypeScript의 타입 내로잉을 아름답게 결합하므로 TypeScript에서 저자가 가장 좋아하는 기능이라 한다.

## 교차 타입
TypeScript 유니언 타입은 둘 이상의 다른 타입 중 하나의 타입이 될 수 있음을 나타낸다. JavaScript의 런타임 `|` 연산자가 `&` 연산자에 대응하는 역할을 하는 것처럼, TypeScript에서도 **`&` 교차 타입**을 사용해 여러 타입을 동시에 나타낸다. 교차 타입은 일반적으로 여러 기존 객체 타입을 별칭 객체 타입으로 결합해 새로운 타입을 생성한다.

```typescript
type Artwork = {
    genre: string;
    name: string;
};

type Writing = {
    pages: number;
    name: string;
};

type WrittenArt = Artwork & Writing;
// 다음과 같음:
// {
//     genre: string;
//     name: string;
//     pages: number;
// }
```

교차 타입은 유니언 타입과 결합할 수 있으며, 이는 하나의 타입으로 판별된 유니언 타입을 설명하는 데 유용하다.

```typescript
type ShortPoem = { author: string } & (
    | { kigo: string; type: "haiku"; }
    | { meter: number; type: "villanelle"; }
);

// Ok
const morningGlory: ShortPoem = {
    author: "Fukuda Chiyo-ni",
    kigo: "Morning Glory",
    type: "haiku",
};

const oneArt: ShortPoem = {
    //
    // Error: Type '{ author: string; type: "villanelle"; }'
    // is not assignable to type 'ShortPoem'.
    //   Type '{ author: string; type: "villanelle"; }' is not assignable to
    //   type '{ author: string; } & { meter: number; type: "villanelle"; }'.
    //    Property 'meter' is missing in type '{ author: string; type: "villanelle"; }'
    //    but required in type '{ meter: number; type: "villanelle"; }'.
    author: "Elizabeth Bishop",
    type: "villanlle",
};
```

### 교차 타입의 위험성
교차 타입은 유용한 개념이지만, 우리 스스로나 TypeScript 컴파일러를 혼동시키는 방식으로 사용하기 쉽다. 교차 타입을 사용할 때는 가능한 한 코드를 간결하게 유지해야 한다.

#### 긴 할당 가능성 오류
유니언 타입과 결합하는 것처럼 복잡한 교차 타입을 만들게 되면 할당 가능성 오류 메시지는 읽기 어려워진다. 다시 말해 복잡하면 복잡할수록 타입 검사기의 메시지도 이해하기 더 어려워진다. 이 현상은 TypeScript의 타입 시스템, 그리고 타입을 지정하는 프로그래밍 언어에서 공통적으로 관측된다.

```typescript
type ShortPoemBase = { author: string };
type Haiku = ShortPoemBase & { kigo: string; type: "haiku" };
type Villanelle = ShortPoemBase & { meter: number; type: "villanelle" };
type ShortPoem = Haiku | Villanelle;

const oneArt: ShortPoem = {
    //
    // Error: Type '{ author: string; type: "villanelle"; }'
    // is not assignable to type 'ShortPoem'.
    //   Type '{ author: string, type: "villanelle"; }' is not assignable to type
    //   'Villanelle'.
    //   Property 'meter' is missing in type '{ author: string; type: "villanelle"; }'
    //   but required in type '{ meter: number; type: "villanelle"; }'.
    author: "Elizabeth Bishop",
    type: "villanelle",
};
```

#### never
교차 타입은 잘못 사용하기 쉽고 불가능한 타입을 생성한다. 원시 타입의 값은 동시에 여러 타입이 될 수 없기 때문에 교차 타입의 구성 요소로 함께 결합할 수 없다. 두 개의 원시 타입을 함께 시도하면 `never` 키워드로 표시되는 `never` 타입이 된다.

```typescript
type NotPossible = number & string; // 타입: never
```

`never` 키워드와 `never` 타입은 프로그래밍 언어에서 `bottom` 타입 또는 `empty` 타입을 뜻한다. `bottom` 타입은 값을 가질 수 없고 참조할 수 없는 타입이므로 `bottom` 타입에 그 어떠한 타입도 제공할 수 없다.

```typescript
let notNumber: NotPossible = 0;
//
// Error: Type 'number' is not assignable to type 'never'.

let notString: never = "";
//
// Error: Type 'string' is not assignable to type 'never'.
```

대부분의 TypeScript 프로젝트는 `never` 타입을 거의 사용하지 않지만 코드에서 불가능한 상태를 나타내기 위해 가끔 등장한다. 하지만 대부분 교차 타입을 잘못 사용해 발생한 실수일 가능성이 높다.