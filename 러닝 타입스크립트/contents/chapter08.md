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

**원시 타입의 초깃값을 갖는 `readonly`로 선언된 속성은 다른 속성과 조금 다르다.** 이런 속성은 더 넓은 원싯값이 아니라 값의 타입이 가능한 한 좁혀진 리터럴 타입으로 유추된다. TypeScript는 값이 나중에 변경되지 않는다는 것을 알기 때문에 더 공격적인 초기 타입 내로잉을 더 편한게 느낀다. `const` 변수가 `let` 변수보다 더 좁은 타입을 갖는 것과 유사하다.

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
**타입 시스템에서의 클래스는 클래스 선언이 런타임 값(클래스 자체)과 타입 애너테이션에서 사용할 수 있는 타입을 모두 생성한다는 점에서 상대적으로 독특하다.**

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
**TypeScript는 클래스 이름 뒤에 `implements` 키워드와 인터페이스 이름을 추가함으로써 클래스의 해당 인스턴스가 인터페이스를 준수한다고 선언할 수 있다.** 이렇게 하면 클래스를 각 인터페이스에 할당할 수 있어야 함을 TypeScript에 나타낸다. 타입 검사기에 의해 모든 불일치에 대해 타입 오류가 발생한다.

```typescript
interface Learner {
  name: string;
  study(hours: number): void;
}

class Student implements Learner {
  name: string;

  constructor(name: string) {
    this.name = name;
  }

  study(hours: number) {
    for (let i = 0; i < hours; i++) {
      console.log('studying...');
    }
  }
}

class Slacker implements Learner {
  // Error: Class 'Slacker' incorrectly implements interface 'Learner'.
  // Type 'Slacker' is missing the following properties from type 'Learner': name, study
}
```

**인터페이스를 구현하는 것으로 클래스를 만들어도 클래스가 사용되는 방식은 변경되지 않는다.** 클래스가 이미 인터페이스와 일치하는 경우 TypeScript의 타입 검사기는 인터페이스의 인스턴스가 필요한 곳에서 해당 인스턴스를 사용할 수 있도록 허용한다. TypeScript는 인터페이스에서 클래스의 메서드 또는 속성 타입을 유추하지 않는다. `Slacker` 예제에서 `study(hours) {}` 메서드를 추가했다면 TypeScript는 타입 애너테이션을 지정하지 않는 한 `hours` 매개변수를 암시적 `any`로 간주한다.

```typescript
class Student implements Learner {
  name;
  // Error: Member 'name' implicitly has an 'any' type.

  study(hours) {
    // Error: Parameter 'hours' implicitly has an 'any' type.
  }
}
```

인터페이스를 구현하는 것은 순전히 안정성 검사를 위해서다. 모든 인터페이스 멤버를 클래스 정의로 복사하지 않는다. 대신 인터페이스를 구현하면 클래스 인스턴스가 사용되는 곳에서 나중에 타입 검사기로 신호를 보내고 클래스 정의에서 표면적인 타입 오류가 발생한다. 변수에 초깃값이 있더라도 타입 애너테이션을 추가하는 것과 용도가 비슷하다.

### 다중 인터페이스 구현
**TypeScript의 클래스는 다중 인터페이스를 구현해 선언할 수 있다.** 클래스에 구현된 인터페이스 목록은 인터페이스 이름 사이에 쉼표를 넣고, 개수 제한 없이 인터페이스를 사용할 수 있다.

```typescript
interface Graded {
  grades: number[];
}

interface Reporter {
  report: () => string;
}

class ReportCard implements Graded, Reporter {
  grades: number[];

  constructor(grades: number[]) {
    this.grades = grades;
  }

  report() {
    return this.grades.join(', ');
  }
}

class Empty implements Graded, Reporter { }
// Error: Class 'Empty' incorrectly implements interface 'Graded'.
//  Property 'grades' is missing in type 'Empty' but required in type 'Graded'.
// Error: Class 'Empty' incorrectly implements interface 'Reporter'.
//  Property 'report' is missing in type 'Empty' but required in type 'Reporter'.
```

실제로 클래스가 한 번에 두 인터페이스를 구현할 수 없도록 정의하는 인터페이스가 있을 수 있다. 두 개의 충돌하는 인터페이스를 구현하는 클래스를 선언하려고 하면 클래스에 하나 이상의 타입 오류가 발생한다.

```typescript
interface AgeIsANumber {
  age: number;
}

interface AgeIsNotANumber {
  age: () => string;
}

class AsNumber implements AgeIsANumber, AgeIsNotANumber {
  age = 0;
  // Error: Property 'age' in type 'AsNumber' is not assignable to the same property in base type 'AgeIsNotANumber'.
  Type 'number' is not assignable to type '() => string'.
}


class NotAsNumber implements AgeIsANumber, AgeIsNotANumber {
  age() { return ''; }
  // Error: Property 'age' in type 'NotAsNumber' is not assignable to the same property in base type 'AgeIsANumber'.
  Type '() => string' is not assignable to type 'number'.
}
```

두 인터페이스가 매우 다른 객체 형태를 표현하는 경우에는 동일한 클래스로 구현하지 않아야 한다.

## 클래스 확장
TypeScript는 다른 클래스를 확장하거나 하위 클래스를 만드는 JavaScript 개념에 타입 검사를 추가한다. 먼저 기본 클래스에 선언된 모든 메서드나 속성은 파생 클래스라고도 하는 하위 클래스에서 사용할 수 있다.

```typescript
class Teacher {
  teach() {
    console.log('The surest test of discipline is its absence.');
  }
}

class StudentTeacher extends Teacher {
  learn() {
    console.log('I cannot afford the luxury of a closed mind.');
  }
}

const teacher = new StudentTeacher();
teacher.teach();
teacher.learn();

teacher.other();
// Error: Property 'other' does not exist on type 'StudentTeacher'.
```

### 할당 가능성 확장
파생 인터페이스가 기본 인터페이스를 확장하는 것과 마찬가지로 하위 클래스도 기본 클래스의 멤버를 상속한다. 하위 클래스의 인스턴스는 기본 클래스의 모든 멤버를 가지므로 기본 클래스의 인스턴스가 필요한 모든 곳에서 사용할 수 있다. 만약 기본 클래스에 하위 클래스가 가지고 있는 모든 멤버가 없으면 더 구체적인 하위 클래스가 필요할 때 사용할 수 없다.

```typescript
class Lesson {
  subject: string;

  constructor(subject: string) {
    this.subject = subject;
  }
}

class OnlineLesson extends Lesson {
  url: string;

  constructor(subject: string, url: string) {
    super(subject);
    this.url = url;
  }
}

let lesson: Lesson;
lesson = new Lesson('coding'); // Ok
lesson = new OnlineLesson('coding', 'nhn.com');

let online: OnlineLesson;
online = new OnlineLesson('coding', 'nhn.com');
online = new Lesson('coding');
// Error: Property 'url' is missing in type 'Lesson' but required in type 'OnlineLesson'.
```

TypeScript의 구조적 타입에 따라 하위 클래스의 모든 멤버가 동일한 타입의 기본 클래스에 이미 존재하는 경우 기본 클래스의 인스턴스를 하위 클래스 대신 사용할 수 있다.

```typescript
class PastGrades {
  grades: number[] = [];
}

class LabeledPastGrades extends PastGrades {
  label?: string;
}

let subClass: LabeledPastGrades;

subClass = new LabeledPastGrades(); // Ok
subClass = new PastGrades(); // Ok
```

### 재정의된 생성자
Vanilla JavaScript와 마찬가지로 TypeScript에서 하위 클래스는 자체 생성자를 정의할 필요가 없다. 자체 생성자가 없는 하위 클래스는 암묵적으로 기본 클래스의 생성자를 사용한다.

JavaScript에서 하위 클래스가 자체 생성자를 선언하면 `super` 키워드를 통해 기본 클래스 생성자를 호출해야 한다. 하위 클래스 생성자는 기본 클래스에서의 필요 여부와 상관없이 모든 매개변수를 선언할 수 있다. TypeScript의 타입 검사기는 기본 클래스 생성자를 호출할 때 올바른 매개변수를 사용하는지 확인한다.

```typescript
class GradeAnnouncer {
  message: string;
  
  constructor(grade: number) {
    this.message = grade >= 65 ? 'Maybe next time...' : 'You pass!';
  }
}

class PassingAnnouncer extends GradeAnnouncer {
  constructor() {
    super(100);
  }
}

class FailingAnnouncer extends GradeAnnouncer {
  constructor() { }
  // Error: Constructors for derived classes must contain a 'super' call.
}
```

JavaScript 규칙에 따르면 하위 클래스의 생성자는 `this` 또는 `super`에 접근하기 전에 반드시 기본 클래스의 생성자를 호출해야 한다. TypeScript는 `super()`를 호출하기 전에 `this` 또는 `super`에 접근하려고 하는 경우 타입 오류를 보고한다.

```typescript
class GradesTally {
  grades: number[] = [];

  addGrades(...grades: number[]) {
    this.grades.push(...grades);
    return this.grades.length;
  }
}

class ContinuedGradesTally extends GradesTally {
  constructor(previousGrades: number[]) {
    this.grades = [...previousGrades];
    // Error: 'super' must be called before accessing 'this' in the constructor of a derived class.

    super();

    console.log('Starting with length', this.grades.length);
  }
}
```

### 재정의된 메서드
하위 클래스의 메서드가 기본 클래스의 메서드에 할당될 수 있는 한 하위 클래스는 기본 클래스와 동일한 이름으로 새 메서드를 다시 선언할 수 있다. 기본 클래스를 사용하는 모든 곳에 하위 클래스를 사용할 수 있으므로 새 메서드의 타입도 기본 메서드 대신 사용할 수 있어야 한다는 점을 명심하자.

```typescript
class GradeCounter {
  countGrades(grades: string[], letter: string) {
    return grades.filter(grade => grade === letter).length;
  }
}

class FailureCounter extends GradeCounter {
  countGrades(grades: string[]): number {
      return super.countGrades(grades, 'F');
  }
}

class AnyFailureCounter extends GradeCounter {
  countGrades(grades: string[]) {
    // Error: Property 'countGrades' in type 'AnyFailureCounter' is not assignable 
    // to the same property in base type 'GradeCounter'.
    //  Type '(grades: string[]) => boolean' is not assignable to type '(grades: string[], letter: string) => number'.
    //    Type 'boolean' is not assignable to type 'number'.
    return super.countGrades(grades, 'F') !== 0;
  }
}

const counter: GradeCounter = new AnyFailureCounter();

// 예상 타입: number
// 실제 타입: boolean
const count = counter.countGrades(['A', 'C', 'F']);
```

### 재정의된 속성
하위 클래스는 새 타입을 기본 클래스의 타입에 할당할 수 있는 한 동일한 이름으로 기본 클래스의 속성을 명시적으로 다시 선언할 수 있다. 재정의된 메서드와 마찬가지로 하위 클래스는 기본 클래스와 구조적으로 일치해야 한다.

속성을 다시 선언하는 대부분의 하위 클래스는 해당 속성을 유니언 타입의 더 구체적인 하위 집합으로 만들거나 기본 클래스 속성 타입에서 확장되는 타입으로 만든다.

```typescript
class Assignment {
  grade?: number;
}

class GradeAssignment extends Assignment {
  grade: number;

  constructor(grade: number) {
    super();
    this.grade = grade;
  }
}
```

속성의 유니언 타입의 허용된 값 집합을 확장할 수는 없다. 만약 확장한다면 하위 클래스 속성은 더 이상 기본 클래스 속성 타입에 할당할 수 없다.

```typescript
class NumericGrade {
  value = 0;
}

class VagueGrade extends NumericGrade {
  value = Math.random() > 0.5 ? 1 : '...';
  // Error:  Property 'value' in type 'VagueGrade' is not assignable to the same property in base type 'NumericGrade'.
  //   Type 'string | number' is not assignable to type 'number'.
  //     Type 'string' is not assignable to type 'number'.
}

const instance: NumericGrade = new VagueGrade();

// 예상한 타입: number
// 실제 타입: number | string
instance.value;
```

## 추상 클래스
때론 일부 메서드의 구현을 선언하지 않고, 대신 하위 클래스가 해당 메서드를 제공할 것을 예상하고 기본 클래스를 만드는 방법이 유용할 수 있다. 추상화하려는 클래스 이름과 메서드 앞에 TypeScript의 `abstract` 키워드를 추가한다. 이러한 추상화 메서드 선언은 추상화 기본 클래스에서 메서드의 본문을 제공하는 것을 건너뛰고, 대신 인터페이스와 동일한 방식으로 선언된다.

```typescript
abstract class School {
  readonly name: string;

  constructor(name: string) {
    this.name = name;
  }

  abstract getStudentTypes(): string[];
}

class Preschool extends School {
  getStudentTypes() {
    return ['preschooler'];
  }
}

class Absence extends School { }
// Error: Non-abstract class 'Absence' does not implement inherited abstract member 'getStudentTypes' from class 'School'.

let school: School;

school = new Preschool('Synnyside Daycare'); // Ok

school = new School('somewhere else');
// Error: Cannot create an instance of an abstract class.
```

추상 클래스는 클래스의 세부 사항이 채워질거라 예상되는 프레임워크에서 자주 사용된다. 클래스는 `school: School` 처럼 값이 클래스를 준수해야 함을 나타내는 타입 애너테이션으로 사용할 수 있다. 그러나 새 인스턴스를 생성하려면 하위 클래스를 사용해야 한다.

## 멤버 접근성
JavaScript에선 클래스 멤버 이름 앞에 `#`을 추가해 `private` 클래스 멤버임을 나타낸다. 이 클래스 멤버는 해당 클래스 인스턴스에서만 접근할 수 있다. JavaScript 런타임은 클래스 외부 코드 영역에서 `private` 메서드나 속성에 접근하려 하면 오류를 발생시킴으로써 프라이버시를 강화한다.

TypeScript의 클래스 지원은 JavaScript의 `#` 프라이버시보다 먼저 만들어졌다. 또한 TypeScript는 `private` 클래스 멤버를 지원하지만, 타입 시스템에만 존재하는 클래스 메서드와 속성에 대해 조금 더 미묘한 프라이버시 정의 집합을 허용한다. TypeScript의 멤버 접근성은 클래스 멤버의 선언 이름 앞에 다음 키워드 중 하나를 추가해 만든다.

- `public` (기본값): 모든 곳에서 누구나 접근 가능
- `protected`: 클래스 내부 또는 하위 클래스에서만 접근 가능
- `private`: 클래스 내부에서만 접근 가능

이러한 키워드는 순수하게 타입 시스템 내에 존재한다. 코드가 JavaScript로 컴파일되면 다른 모든 타입 시스템 구문과 함께 키워드도 제거된다.

```typescript
class Base {
  isPublicImplicit = 0;
  public isPublicExplicit = 1;
  protected isProtected = 2;
  private isPrivate = 3;
  #truePrivate = 4;
}

class Subclass extends Base {
  examples() {
    this.isPublicImplicit; // Ok
    this.isPublicExplicit; // Ok
    this.isProtected; // Ok

    this.isPrivate;
    // Error: Property 'isPrivate' is private and only accessible within class 'Base'.

    this.#truePrivate;
    // Error: Property '#truePrivate' is not accessible outside class 'Base' because it has a private identifier.
  }
}

new Subclass().isPublicImplicit; // Ok
new Subclass().isPublicExplicit; // Ok

new Subclass().isProtected;
// Error: Property 'isProtected' is protected and only accessible within class 'Base' and its subclasses.

new Subclass.isPrivate;
// Error: Property 'isPrivate' does not exist on type 'typeof Subclass'.
```

- **TypeScript의 멤버 접근성은 타입 시스템에서만 존재하는 반면 JavaScript의 `private` 선언은 런타임에도 존재한다는 점이 주요 차이점이다.**
- `protected` 또는 `private`으로 선언된 TypeScript 클래스 멤버는 명시적으로 또는 암묵적으로 `public`으로 선언된 것 처럼 동일한 JavaScript 코드로 컴파일된다.
- 인터페이스와 타입 애너테이션처럼 접근성 키워드는 JavaScript로 컴파일될 때 제거된다.
- JavaScript 런타임에서는 `#`, `private` 필드만 진정한 `private`다.

접근성 제한자는 `readonly`와 함께 표시할 수 있다. `readonly`와 명시적 접근성 키워드로 멤버를 선언하려면 접근성 키워드를 먼저 적어야 한다.

```typescript
class TwoKeywords {
  private readonly name: string;

  constructor() {
    this.name = 'Ramos'; // Ok
  }

  log() {
    console.log(this.name); // Ok
  }
}

const two = new TwoKeywords();

two.name = 'Sergio';
// Error: Property 'name' is private and only accessible within class 'TwoKeywords'.
// Cannot assign to 'name' because it is a read-only property.
```

TypeScript의 이전 멤버 접근성 키워드를 JavaScript의 `#`, `private` 필드와 함께 사용할 수 없다는 점을 기억하자. `private` 필드는 기본적으로 항상 `private`이므로 `private` 키워드를 추가로 표시할 필요가 없다.

### 정적 필드 제한자
JavaScript는 `static` 키워드를 사용해 클래스 자체에서 멤버를 선언한다. TypeScript는 `static` 키워드를 단독으로 사용하거나 `readonly`와 접근성 키워드를 함께 사용할 수 있도록 지원한다. 함께 사용할 경우 접근성 키워드를 먼저 작성하고, 그다음 `static`, `readonly` 키워드가 온다.

```typescript
class Question {
  protected static readonly answer: 'bash';
  protected static readonly prompt = 
    "What's an ogre's favorite programming language?";

  guess(getAnswer: (prompt: string) => string) {
    const answer = getAnswer(Question.prompt);

    if (answer === Question.answer) {
      console.log('You got it!');
    } else {
      console.log('Try again...');
    }
  }
}

Question.answer;
// Error: Property 'answer' is protected and only accessible within class 'Question' and its subclasses.
```