# Chapter 8. 클래스
## 클래스 메서드
TypeScript는 독립 함수를 이해하는 것과 동일한 방식으로 메서드를 이해한다. 매개변수 타입에 타입이나 기본값을 지정하지 않으면 `any` 타입을 기본으로 갖는다. 메서드를 호출하려면 허용 가능한 수의 인수가 필요하고, 재귀 함수가 아니라면 대부분 반환 타입을 유추할 수 있다.

```typescript
class Greeter {
  greet(name: string) {
    console.log(`${name}, do your stuff!`);
  }
}

new Greeter().greet('Miss Frizzle'); // Ok

new Greeter().greet();
// Error: Expected 1 arguments, but got 0.
```

클래스 생성자는 매개변수와 관련하여 전형적인 클래스 메서드처럼 취급된다. TypeScript는 메서드 호출 시 올바른 타입의 인수가 올바른 수로 제공하는지 확인하기 위해 타입 검사를 수행한다.

```typescript
class Greeted {
  constructor(message: string) {
    console.log(`As I always say: ${message}!`);
  }
}

new Greeted('take chances, make mistakes, get messy');

new Greeted();
// Error: Expected 1 arguments, but got 0.
```

## 클래스 속성
TypeScript에서 클래스의 속성을 읽거나 쓰려면 클래스에 명시적으로 선언해야 한다. 클래스 속성은 인터페이스와 동일한 구문을 사용해 선언한다. 클래스 속성 이름 뒤에는 선택적으로 타입 애너테이션이 붙는다.

TypeScript는 생성자 내의 할당에 대해 그 멤버가 클래스에 존재하는 멤버인지 추론하려고 시도하지 않는다.

```typescript
class FieldTrip {
  destination: string;

  constructor(destination: string) {
    this.destination = destination; // Ok
    console.log(`We're going to ${this.destination}!`);

    this.nonexistent = destination;
    // Error: Property 'nonexistent' does not exist on type 'FieldTrip'.
  }
}
```

클래스 속성을 명시적으로 선언하면 TypeScript는 클래스 인스턴스에서 무엇이 허용되고, 허용되지 않는지 빠르게 이해할 수 있다. 나중에 클래스 인스턴스가 사용될 때, 코드가 `trip.nonexistent`와 같은 클래스 인스턴스에 존재하지 않는 멤버에 접근하려 시도하면 TypeScript는 타입 오류를 발생시킨다.

```typescript
const trip = new FieldTrip('planetarium');

trip.destination; // Ok

trip.nonexistent;
// Error: Property 'nonexistent' does not exist on type 'FieldTrip'.
```

### 함수 속성
JavaScript에는 클래스의 멤버를 호출 가능한 함수로 선언하는 두 가지 구문이 있다.

`myFunction(){}`과 같이 멤버 이름 뒤에 괄호를 붙이는 메서드 접근 방식을 앞서 살펴봤다. 메서드 접근 방식은 함수를 클래스 프로토타입에 할당하므로 모든 클래스 인스턴스는 동일한 함수 정의를 사용한다.

아래 `WithMethod` 클래스는 모든 인스턴스가 참조할 수 있는 `myMethod` 메서드를 선언한다.

```typescript
class WithMethod {
  myMethod() {}
}

new WithMethod().myMethod === new WithMethod().myMethod; // true
```

값이 함수인 속성을 선언하는 방식도 있다. 이렇게 하면 클래스의 인스턴스당 새로운 함수가 생성되며, 항상 클래스 인스턴스를 가리켜야 하는 화살표 함수에서 `this` 스코프를 사용하면 클래스 인스턴스당 새로운 함수를 생성하는 시간과 메모리 비용 측면에서 유용할 수 있다.

아래 `WithProperty` 클래스는 이름이 `myProperty`인 단일 속성을 포함하며 각 클래스 인스턴스에 대해 다시 생성되는 `() => void` 타입이다.

```typescript
class WithProperty {
  myProperty: () => {}
}

new WithProperty().myProperty === new WithMethod().myProperty; // false
```

함수 속성에는 클래스 메서드와 독립 함수의 동일한 구문을 사용해 매개변수와 반환 타입을 지정할 수 있다. 결국 함수 속성은 클래스 멤버로 할당된 값이고, 그 값은 함수다.

```typescript
class WithPropertyParameters {
  takesParameters = (input: boolean) => input ? 'Yes' : 'No';
}

const instance = new WithPropertyParameters();

instance.takesParameters(true); // Ok

instance.takesParameters(123);
// Error: Argument of type 'number' is not assignable to parameter of type 'boolean'.
```

### 초기화 검사
엄격한 컴파일러 설정이 활성화된 상태에서 TypeScript는 `undefined` 타입으로 선언된 각 속성이 생성자에서 할당되었는지 확인한다. 이와 같은 엄격한 초기화 검사는 클래스 속성에 값을 할당하지 않는 실수를 예방할 수 있어 유용하다.

다음 `WithValue` 클래스는 `unused` 속성에 값을 할당하지 않았고, TypeScript는 이 속성을 타입 오류로 인식한다.

```typescript
class WithValue {
  immediate = 0; // Ok
  later: number; // Ok(constructor에서 할당)
  mayBeUndefined: number | undefined; // Ok(undefined가 되는 것이 허용됨)
  unused: number;
  // Error: Property 'unused' has no initializer
  // and is not definitely assigned in the constructor.

  constructor() {
    this.later = 1;
  }
}
```

엄격한 초기화 검사가 없다면, 비록 타입 시스템이 `undefined` 값에 접근할 수 없다고 말할지라도 클래스 인스턴스는 `undefined` 값에 접근할 수 있다.

아래 예제는 엄격한 초기화 검사가 수행되지 않으면 올바르게 컴파일되지만, 결과 JavaScript는 런타임 시 문제가 발생한다.

```typescript
class MissingInitializer {
  property: string;
}

new MissingInitializer().property.length;
// TypeError: Cannot read property 'length' of undefined
```

#### 확실하게 할당된 속성
엄격한 초기화 검사가 유용한 경우가 대부분이지만 클래스 생성자 다음에 클래스 속성을 의도적으로 할당하지 않는 경우가 있을 수 있다. 엄격한 초기화 검사를 적용하면 안 되는 속성인 경우 이름 뒤에 `!`를 추가해 검사를 비활성화하도록 설정한다. 이렇게 하면 TypeScript에 속성이 처음 사용되기 전에 `undefined` 값이 할당된다.

아래 `ActivitiesQueue` 클래스는 생성자완 별도로 여러 번 다시 초기화될 수 있으므로 `pending` 속성은 `!`와 함께 할당되어야 한다.

```typescript
class ActivitiesQueue {
  pending!: string[]; // Ok

  initialize(pending: string[]) {
    this.pending = pending;
  }

  next() {
    return this.pending.pop();
  }
}

const activities = new ActivitiesQueue();

activities.initialize(['eat', 'sleep', 'learn']);
activities.next();
```

### 선택적 속성
인터페이스와 마찬가지로 클래스는 선언된 속성 이름 뒤에 `?`를 추가해 속성을 옵션으로 선언한다. 선택적 속성은 `| undefined`를 포함하는 유니언 타입과 거의 동일하게 작동한다. 엄격한 초기화 검사는 생성자에서 선택적 속성을 명시적으로 설정하지 않아도 문제가 되지 않는다.

```typescript
class MissingInitializer {
  property?: string;
}

new MissingInitializer().property?.length; // Ok

new MissingInitializer().property.length;
// Error: Object is possibly 'undefined'.
```

### 읽기 전용 속성
인터페이스와 마찬가지로 클래스도 선언된 속성 이름 앞에 `readonly` 키워드를 추가해 속성을 읽기 전용으로 선언한다. `readonly` 키워드는 타입 시스템에만 존재하며 JavaScript로 컴파일할 때 삭제된다.

`readonly`로 선언한 속성은 선언된 위치 또는 생성자에서 초깃값만 할당할 수 있다. 클래스 내의 메서드를 포함한 다른 모든 위치에서 속성은 읽을 수만 있고, 쓸 수는 없다.

```typescript
class Quote {
  readonly text: string;

  constructor(text: string) {
    this.text = ;
  }

  emphasize() {
    this.text += '!';
    // Error: Cannot assign to 'text' because it is a read-only property.
  }
}

const quote = new Quote('There is a brilliant child locked inside every student.');
Quote.text = 'Ha!';
// Error: Cannot assign to 'text' because it is a read-only property.
```

원시 타입의 초깃값을 갖는 `readonly`로 선언된 속성은 다른 속성과 조금 다르다. 이런 속성은 더 넓은 원싯값이 아니라 값의 타입이 가능한 한 좁혀진 리터럴 타입으로 유추된다. TypeScript는 값이 나중에 변경되지 않는다는 것을 알기 때문에 더 공격적인 초기 타입 내로잉을 더 편한게 느낀다. `const` 변수가 `let` 변수보다 더 좁은 타입을 갖는 것과 유사하다.

아래 예제에서 클래스 속성은 처음에는 모두 문자열 리터럴로 선언되므로 둘 중 하나를 `string`으로 확장하기 위해서는 타입 애너테이션이 필요하다.

```typescript
class RandomQuote {
  readonly explicit: string = 'Home is the nicest word there is.';
  readonly implicit = 'Home is the nicest word there is.';

  constructor() {
    if (Math.random > 0.5) {
      this.explicit = "We start learning the minute we're born."; // Ok
      this.implicit = "We start learning the minute we're born.";
      // Error: Type '"We start learning the minute we're born."' is
      // not assignable to type '"Home is the nicest word there is."';
    }
  }
}

const quote = new RandomQuote();

quote.explicit; // 타입: string
quote.implicit; // 타입: "Home is the nicest word there is."
```

속성의 타입을 명시적으로 확장하는 작업이 자주 필요하진 않는다. 그럼에도 불구하고 `RandomQuote`에서 등장하는 생성자의 조건부 로직처럼 경우에 따라 유용할 수 있다.

## 타입으로서의 클래스
타입 시스템에서의 클래스는 클래스 선언이 런타임 값(클래스 자체)과 타입 애너테이션에서 사용할 수 있는 타입을 모두 생성한다는 점에서 상대적으로 독특하다.

```typescript
class Teacher {
  sayHello() {
    console.log('Take chances, make mistakes, get messy!');
  }
}

let teacher: Teacher;

teacher = new Teacher(); // Ok

teacher = 'Wahoo!';
// Error: Type 'string' is not assignable to type 'Teacher'.
```

**TypeScript는 클래스의 동일한 멤버를 모두 포함하는 모든 객체 타입을 클래스에 할당할 수 있는 것으로 간주한다.** TypeScript의 구조적 타이핑이 선언되는 방식이 아니라 객체의 형태만 고려하기 때문이다.

```typescript
class SchoolBus {
  getAbilities() {
    return ['magic', 'shapeshifting'];
  }
}

function withSchoolBus(bus: SchoolBus) {
  console.log(bus.getAbilities());
}

withSchoolBus(new SchoolBus()); // Ok

// Ok
withSchoolBus({
  getAbilities: () => ['transmogrification'],
});

withSchoolBus({
  getAbilities: () => 123,
  // Error: Type 'number' is not assignable to type 'string[]'. 
});
```

## 클래스와 인터페이스



```typescript

```

```typescript

```

### 다중 인터페이스 구현


```typescript

```


```typescript

```

## 클래스 확장
