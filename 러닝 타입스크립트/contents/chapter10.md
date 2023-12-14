# Chapter 10. 제네릭
지금까지 배운 모든 구문은 해당 구문이 작성될 때 완전히 알려진 타입과 함께 사용해야 했다. 그러나 때로는 코드에서 호출하는 방식에 따라 다양한 타입으로 작동하도록 의도할 수 있다.

JavaScript에서 다음 `identify` 함수는 모든 가능한 타입으로 `input`을 받고, 동일한 `input`을 출력으로 반환한다. 그렇다면 여기서 매개변수 타입과 반환 타입을 어떻게 설명해야 할까?

```javascript
function identity(input) {
  return input;
}

identity('abc');
identity(123);
identity({ quote: 'I think your self emerges more clearly over time.' });
```

`input`을 `any`로 선언할 수 있지만 그렇게 되면 함수의 반환 타입 역시 `any`가 된다.

```typescript
function identity(input: any) {
  return input;
}

identity(42); // value: any 타입
```

`input`이 모든 입력을 허용한다면, `input` 타입과 함수 반환 타입 간의 관계를 말할 수 있는 방법이 필요하다.

TypeScript는 **제네릭**을 사용해 타입 간의 관계를 알아낸다.

TypeScript에서 함수와 같은 구조체는 **제네릭 타입 매개변수**를 원하는 수만큼 선언할 수 있다. 제네릭 타입 매개변수는 제네릭 구조체의 각 사용법에 따라 타입이 결정된다. 이러한 타입 매개변수는 구조체의 각 인스턴스에서 서로 다른 일부 타입을 나타내기 위해 구조체의 타입으로 사용된다. 타입 매개변수는 구조체의 각 인스턴스에 대해 **타입 인수**라고 하는 서로 다른 타입을 함께 제공할 수 있지만, 타입 인수가 제공된 인스턴스 내에서는 일관성을 유지한다.

타입 매개변수는 전형적으로 `T`나 `U` 같은 단일 문자 이름 또는 `Key`와 `Value` 같은 파스칼 케이스 이름을 갖는다.

## 제네릭 함수
매개변수 괄호 바로 앞 홑화살괄호(`<`, `>`)로 묶인 타입 매개변수에 별칭을 배치해 함수를 제네릭으로 만든다. 그러면 해당 타입 매개변수를 함수의 본문 내부의 매개변수 타입 애너테이션, 반환 타입 애너테이션, 타입 애너테이션에서 사용할 수 있다.

```typescript
function identity<T>(input: T) {
  return input;
}

const stringy = identity('me'); // 타입: 'me'
const numeric = identity(123); // 타입: 123
```

화살표 함수도 제네릭을 만들 수 있다. 화살표 함수의 제네릭 선언은 매개변수 목록 바로 전인 `(` 앞에 위치한다.

```typescript
// 앞서 선언했던 것과 기능적으로 동일한 함수 선언식이다.
const identity = <T>(input: T) => input;

identity(123); // 타입: 123
```

이런 방식으로 함수에 타입 매개변수를 추가하면 타입 안정성을 유지하고 `any` 타입을 피하면서 다른 입력과 함께 재사용할 수 있다.

### 명시적 제네릭 호출 타입
제네릭 함수를 호출할 때 대부분의 TypeScript는 함수가 호출되는 방식에 따라 타입 인수를 유추한다.

하지만 클래스 멤버와 변수 타입과 마찬가지로 때로는 타입 인수를 해석하기 위해 TypeScript에 알려줘야 하는 함수 호출 정보가 충분하지 않을 수도 있다. 이러한 현상은 타입 인수를 알 수 없는 제네릭 구문이 다른 제네릭 구문에 제공된 경우 주로 발생한다.

예를 들어 다음 `logWrapper` 함수는 매개변수 타입이 `logWrapper`의 타입 매개변수 `Input`으로 설정된 `callback`을 받는다. 이처럼 매개변수 타입이 명시적으로 선언된 `callback`과 `logWrapper`가 함께 호출되는 경우 TypeScript는 타입 인수를 유추할 수 있다. 그러나 매개변수 타입을 모르는 경우 TypeScript는 `Input`이 무엇이 되어야 하는지 알아낼 방법이 없다.

```typescript
function logWrapper<Input>(callback: (input: Input) => void) {
  return (input: Input) => {
    console.log('Input: ', input);
    callback(input);
  };
}

// 타입: (input: string) => void
logWrapper((input: string) => {
  console.log(input.length);
});

// 타입: (input: unknown) => void
logWrapper((input: unknown) => {
  console.log(input.length);
  // Error: Property 'length' does not exist on type 'unknown'.
});
```

기본값이 `unknown`으로 설정되는 것을 피하기 위해 TypeScript에 해당 타입 인수가 무엇인지 명시적으로 알려주는 **명시적 제네릭 타입 인수**를 사용해 함수를 호출할 수 있다. TypeScript는 매개변수가 타입 인수로 제공된 것과 일치하는지 확인하기 위해 제네릭 호출 타입 검사를 수행한다.

앞서 본 `logWrapper`는 `Input` 제네릭을 위한 명시적 `string`과 함께 제공된다. 그러면 TypeScript는 제네릭 타입 `Input`의 콜백 `input` 매개변수가 `string` 타입으로 해석된다고 유추한다.

```typescript
// 타입: (input: string) => void
logWrapper<string>((input) => {
  console.log(input.length);
});

// 타입: (input: unknown) => void
logWrapper<string>((input: boolean) => {
  // Error: Argument of type '(input: boolean) => void' is not assignable to parameter of type '(input: string) => void'.
  //  Types of parameters 'input' and 'input' are incompatible.
  //   Type 'string' is not assignable to type 'boolean'.
});
```

변수에 대한 명시적 타입 애너테이션과 마찬가지로 명시적 타입 인수는 항상 제네릭 함수에 지정할 수 있지만 때로는 필요하지 않다. 많은 TypeScript 개발자는 필요할 때만 명시적으로 타입 인수를 지정한다.

다음 `logWrapper`는 타입 인수와 함수 매개변수 타입을 모두 `string`으로 명시적으로 지정한다. 둘 중 하나는 제거할 수 있다.

```typescript
// 타입: (input: string) => void
logWrapper<string>((input: string) => { /* ... */ });
```

### 다중 함수 타입 매개변수
임의의 수의 타입 매개변수를 쉼표로 구분해 함수를 정의한다. 제네릭 함수의 각 호출은 각 타입 매개변수에 대한 자체 값 집합을 확인할 수 있다.

아래 예제에서 `makeTuple`은 두 개의 타입 매개변수를 선언하고 입력된 값을 읽기 전용 튜플로 반환한다.

```typescript
function makeTuple<First, Second>(first: First, second: Second) {
  return [first, second] as const;
}

let tuple = makeTuple(true, 'abc'); // value: readonly [boolean, string] 타입
```

함수가 여러 개의 타입 매개변수를 선언하면 해당 함수에 대한 호출은 명시적으로 제네릭 타입을 모두 선언하지 않거나 모두 선언해야 한다. TypeScript는 아직 제네릭 호출 중 일부 타입만을 유추하진 못한다.

```typescript
function makePair<Key, Value>(key: Key, value: Value) {
  return { key, value };
}

// Ok: 타입 인수가 둘 다 제공되지 않음
makePair('abc', 123); // 타입: { key: string; value: number }

// Ok: 두 개의 타입 인수가 제공됨
makePair<string, number>('abc', 123); // 타입: { key: string; value: number }
makePair<'abc', 123>('abc', 123); // 타입: { key: 'abc'; value: 123 }

makePair<string>('abc', 123);
// Error: Expected 2 type arguments, but got 1.
```

## 제네릭 인터페이스
인터페이스도 제네릭으로 선언할 수 있다. 인터페이스는 함수와 유사한 제네릭 규칙을 따르며 인터페이스 이름 뒤 `<`과 `>` 사이에 선언된 임의의 수의 타입 매개변수를 갖는다. 해당 제네릭 타입은 나중에 멤버 타입과 같이 선언의 다른 곳에서 사용할 수 있다.

```typescript
interface Box<T> {
  inside: T
}

let stringyBox: Box<string> = {
  inside: 'abc',
};

let numberBox: Box<number> = {
  inside: 123,
};

let incorrectBox: Box<number> = {
  inside: false,
  // Error: Type 'boolean' is not assignable to type 'number'.
}
```

TypeScript에서 내장 `Array` 메서드는 제네릭 인터페이스로 정의된다는 특징이 있다. `Array`는 타입 매개변수 `T`를 사용해서 배열 안에 저장된 데이터의 타입을 나타낸다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/91fab4e6-4dd3-435a-b1fc-f351b1f96347)

### 유추된 제네릭 인터페이스 타입
제네릭 함수와 마찬가지로 제네릭 인터페이스의 타입 인수는 사용법에서 유추할 수 있다. TypeScript는 제네릭 타입을 취하는 것으로 선언된 위치에 제공된 값의 타입에서 타입 인수를 유추한다.

```typescript
interface LinkedNode<Value> {
  next?: LinkedNode<Value>;
  value: Value;
}

function getLast<Value>(node: LinkedNode<Value>): Value {
  return node.next? getLast(node.next) : node.value;
}

// 유추된 Value 타입 인수: Date
let lastDate = getLast({
  value: new Date('12-14-2023'),
});

// 유추된 Value 타입 인수: string
let lastFruit = getLast({
  next: {
    value: 'banana',
  },
  value: 'apple',
});

// 유추된 Value 타입 인수: number
let lastMismatch = getLast({
  next: {
    value: 123,
  },
  value: false,
  // Error: type 'boolean' is not assignable to type 'number'.
});
```

인터페이스가 타입 매개변수를 선언하는 경우, 해당 인터페이스름 참조하는 모든 타입 애너테이션은 이에 상응하는 타입 인수를 제공해야 한다.

```typescript
interface CreateLike<T> {
  contents: T;
}

let missingGeneric: CreateLike = {
  // Error: Generic type 'CreateLike<T>' requires 1 type argument(s).
  inside: '??'
};
```

## 제네릭 클래스
인터페이스처럼 클래스도 나중에 멤버에서 사용할 임의의 수의 타입 매개변수를 선언할 수 있다. 클래스의 각 인스턴스는 타입 매개변수로 각자 다른 타입 인수 집합을 가진다.

```typescript
class Secret<Key, Value> {
  key: Key;
  value: Value;

  constructor(key: Key, value: Value) {
    this.key = key;
    this.value = value;
  }

  getValue(key: Key): Value | undefined {
    return this.key === key
        ? this.value
        : undefined;
  }
}

const storage = new Secret(12345, 'luggage'); // 타입: Secret<number, string>

storage.getValue(1987); // 타입: string | undefined
```

제네릭 인터페이스와 마찬가지로 클래스를 사용하는 타입 애너테이션은 해당 클래스의 제네릭 타입이 무엇인지를 TypeScript에 나타내야 한다.

### 명시적 제네릭 클래스 타입
제네릭 클래스 인스턴스화는 제네릭 함수를 호춣하는 것과 동일한 타입 인수 유추 규칙을 따른다. `new Secret(12345, 'luggage')`와 같이 함수 생성자에 전달된 매개변수 타입으로부터 타입 인수를 유추할 수 있다면 TypeScript는 유추된 타입을 사용한다. 하짐나 생성자에 전달된 인수에서 클래스 타입 인수를 유추할 수 없는 경우엔 타입 인수의 기본값은 `unknown`이 된다.

다음 `CurriedCallback` 클래스는 제네릭 함수를 받는 생성자를 선언한다. 제네릭 함수가 명시적 타입 인수의 타입 애너테이션과 같은 알려진 타입을 갖는 경우라면 클래스 인스턴스의 `Input` 타입 인수는 이를 통해 타입을 알아낼 수 있다.

```typescript
class CurriedCallback<Input> {
  #callback: (input: Input) => void;

  constructor(callback: (input: Input) => void) {
    this.#callback = (input: Input) => {
      console.log('Input:', input);
      callback(input);
    };
  }

  call(input: Input) {
    this.#callback(input);
  }
}

// 타입: CurriedCallback<string>
new CurriedCallback((input: string) => {
  console.log(input.length);
});

  // 타입: CurriedCallback<unknown>
new CurriedCallback((input) => {
  console.log(input.length);
  // Error: Property 'length' does not exist on type 'unknown'.
});
```

클래스 인스턴스는 다른 제네릭 함수 호출과 동일한 방식으로 명시적 타입 인수를 제공해서 기본값 `unknown`이 되는 것을 피할 수 있다.

아래 코드에선 `CurriedCallback`의 `Input` 타입 인수를 `string`으로 명시적으로 제공하므로 TypeScript는 해당 콜백의 `Input` 타입 매개변수가 `string`으로 해석됨을 유추할 수 있다.

```typescript
new CurriedCallback<string>((input: string) => {
  console.log(input.length);
});

new CurriedCallback<string>((input: boolean) => {
  // Error: Argument of type '(input: boolean) => void' is not assignable to parameter of type '(input: string) => void'.
  //  Types of parameters 'input' and 'input' are incompatible.
  //   Type 'string' is not assignable to type 'boolean'.
});
```

## 제네릭 클래스 확장
제네릭 클래스는 `extends` 키워드 다음에 오는 기본 클래스로 사용할 수 있다. TypeScript는 사용법에서 기본 클래스에 대한 타입 인수를 유추하지 않는다. 기본값이 없는 모든 타입 인수는 명시적 타입 애너테이션을 사용해 지정해야 한다.

```typescript
class Quote<T> {
  lines: T;

  constructor(lines: T) {
    this.lines = lines;
  }
}

class SpokenQuote extends Quote<string[]> {
  speak() {
    console.log(this.lines.join('\n'));
  }
}

new Quote('The only real failure is the failure to try.').lines; // 타입: string
new Quote([4, 8, 15, 16, 23, 42]).lines; // 타입: number[]

new SpokenQuote([
  'Greed is so destructive.',
  'It destroys everything'
]).lines; // 타입: string[]

new SpokenQuote([4, 8, 15, 16, 23, 42]);
// Error: Argument of type 'number' is not assignable to parameter of type 'string'.
```

제네릭 파생 클래스는 자체 타입 인수를 기본 클래스에 번갈아 전달할 수 있다. 타입 이름은 일치하지 않아도 된다.

```typescript
class Quote<T> {
  lines: T;

  constructor(lines: T) {
    this.lines = lines;
  }
}

class AttributeQuote<Value> extends Quote<Value> {
  speaker: string

  constructor(value: Value, speaker: string) {
    super(value);
    this.speaker = speaker;
  }
}

// 타입: 
// (Quote<string> 확장하기)
new AttributeQuote(
  "The road to success is always under construction.",
  "Lily Tomlin",
);
```

### 제네릭 인터페이스 구현
제네릭 클래스는 모든 필요한 매개변수를 제공함으로써 제네릭 인터페이스를 구현한다. 제네릭 인터페이스는 제네릭 기본 클래스를 확장하는 것과 유사하게 작동한다. 기본 인터페이스의 모든 타입 매개변수는 클래스에 선언되어야 한다.

```typescript
interface ActingCredit<Role> {
  role: Role;
}

class MoviePart implements ActingCredit<string> {
  role: string;
  speaking: boolean;

  constructor(role: string, speaking: boolean) {
    this.role = role;
    this.speaking = speaking;
  }
}

const part = new MoviePart('Miranda Priestly', true);

part.role; // 타입: string

class IncorrectExtension implements ActingCredit<string> {
  role: boolean;
  // Error: Property 'role' in type 'IncorrectExtension' is not assignable to the same property in base type 'ActingCredit<string>'.
  //  Type 'boolean' is not assignable to type 'string'.
}
```

### 메서드 제네릭
클래스 메서드는 클래스 인스턴스와 별개로 자체 제네릭 타입을 선언할 수 있다. 제네릭 클래스 메서드에 대한 각각의 호출은 각 타입 매개변수에 대해 다른 타입 인수를 갖는다.

```typescript
class CreatePairFactory<Key> {
  key: Key;

  constructor(key: Key) {
    this.key = key;
  }

  createPair<Value>(value: Value) {
    return { key: this.key, value };
  }
}

// 타입: CreatePairFactory<string>
const factory = new CreatePairFactory('role');

// 타입: { key: string, value: number }
const numberPair = new CreatePairFactory(10);

// 타입: { key: string, value: string }
const stringPair = new CreatePairFactory('Sophie');
```

### 정적 클래스 제네릭
