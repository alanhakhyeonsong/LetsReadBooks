# 25장. 클래스
## 클래스는 프로토타입의 문법적 설탕인가?
JavaScript는 프로토타입 기반 객체지향 언어다. 이는 클래스가 필요 없는 객체지향 프로그래밍 언어다. ES5에선 클래스 없이도 생성자 함수와 프로토타입을 통해 객체지향 언어의 상속을 구현할 수 있다.

ES6에서 도입된 클래스는 기존 프로토타입 기반 객체지향 프로그래밍보다 Java나 C#과 같은 클래스 기반 객체지향 프로그래밍에 익숙한 프로그래머가 더욱 빠르게 학습할 수 있도록 클래스 기반 객체지향 프로그래밍 언어와 매우 흡사한 새로운 객체 생성 메커니즘을 제시한다.

그렇다고 ES6의 클래스가 기존의 프로토타입 기반 객체지향 모델을 폐지하고 새롭게 클래스 기반 객체지향 모델을 제공하는 것은 아니다. **사실 클래스는 함수이며 기존 프로토타입 기반 패턴을 클래스 기반 패턴처럼 사용할 수 있도록 하는 문법적 설탕이라고 볼 수도 있다.**

**단, 클래스와 생성자 함수는 모두 프로토타입 기반의 인스턴스를 생성하지만 정확히 동일하게 동작하지는 않는다.** 클래스는 생성자 함수보다 엄격하며 생성자 함수에서는 제공하지 않는 기능도 제공한다.

- 클래스를 `new` 연산자 없이 호출하면 에러가 발생한다. 하지만 생성자 함수를 `new` 연산자 없이 호출하면 일반 함수로서 호출된다.
- 클래스는 상속을 지원하는 `extends`와 `super` 키워드를 제공한다. 하지만 생성자 함수는 지원하지 않는다.
- 클래스는 호이스팅이 발생하지 않는 것철머 동작한다. 하지만 함수 선언문으로 정의된 생성자 함수는 함수 호이스팅이, 함수 표현식으로 정의한 생성자 함수는 변수 호이스팅이 발생한다.
- 클래스 내의 모든 코드에는 암묵적으로 strict mode가 지정되어 실행되며 strict mode를 해제할 수 없다. 하지만 생성자 함수는 암묵적으로 strict mode가 지정되지 않는다.
- 클래스의 `constructor`, 프로토타입 메서드, 정적 메서드는 모두 프로퍼티 어트리뷰트 `[[Enumerable]]`의 값이 `false`다. 열거되지 않는다는 말이다.

클래스는 **새로운 객체 생성 메커니즘**으로 보는 것이 좀 더 합당하다.

## 클래스 정의
```javascript
// 클래스 선언문
class Person {}

// 익명 클래스 표현식
const Person = class {};

// 기명 클래스 표현식
const Person = class MyClass {};
```

**클래스를 표현식으로 정의할 수 있다는 것은 클래스가 값으로 사용할 수 있는 일급 객체라는 것을 의미한다.** 클래스는 일급 객체로서 다음과 같은 특징을 갖는다.

- 무명의 리터럴로 생성할 수 있다. 즉, 런타임에 생성이 가능하다.
- 변수나 자료구조에 저장할 수 있다.
- 함수의 매개변수에게 전달할 수 있다.
- 함수의 반환값으로 사용할 수 있다.

좀 더 자세히 말하자면 클래스는 함수다. 따라서 클래슨느 값처럼 사용할 수 있는 일급 객체다.

클래스 몸체에는 0개 이상의 메서드만 정의할 수 있다. 클래스 몸체에서 정의할 수 있는 메서드는 생성자, 프로토타입 메서드, 정적 메서드의 3가지가 있다.

```javascript
// 클래스 선언문
class Person {
  // 생성자
  constructor(name) {
    // 인스턴스 생성 및 초기화
    this.name = name; // name 프로퍼티는 public
  }

  // 프로토타입 메서드
  sayHi() {
    console.log(`Hi! My name is ${this.name}`);
  }

  // 정적 메서드
  static sayHello() {
    console.log('Hello!');
  }
}

// 인스턴스 생성
const me = new Person('Ramos');

// 인스턴스의 프로퍼티 참조
console.log(me.name); // Ramos
// 프로토타입 메서드 호출
me.sayHi(); // Hi! My name is Ramos
// 정적 메서드 호출
Person.sayHello(); // Hello!
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6a9f81df-de6a-4040-a23a-20010bde545a)

## 클래스 호이스팅
클래스는 함수로 평가된다.

```javascript
// 클래스 선언문
class Person {}

console.log(typeof Person); // function
```

**클래스 선언문으로 정의한 클래스는 함수 선언문과 같이 소스코드 평가 과정, 즉 런타임 이전에 먼저 평가되어 함수 객체를 생성한다.** 이때 클래스가 평가되어 생성된 함수 객체는 생성자 함수로서 호출할 수 있는 함수, 즉 `constructor`다. **생성자 함수로서 호출할 수 있는 함수는 함수 정의가 평가되어 함수 객체를 생성하는 시점에 프로토타입도 더불어 생성된다.** 프로토타입과 생성자 함수는 단독으로 존재할 수 없고 언제나 쌍으로 존재하기 때문이다.

단, 클래스는 클래스 정의 이전에 참조할 수 없다.

```javascript
console.log(Person);
// ReferenceError: Cannot access 'Person' before initialization

// 클래스 선언문
class Person {}
```

클래스 선언문은 마치 호이스팅이 발생하지 않는 것처럼 보이나 그렇지 않다.

```javascript
const Person = '';

{
  // 호이스팅이 발생하지 않는다면 ''이 출력되어야 한다.
  console.log(Person);
  // ReferenceError: Cannot access 'Person' before initialization

  // 클래스 선언문
  class Person {}
}
```

클래스 선언문도 변수 선언, 함수 정의와 마찬가지로 호이스팅이 발생한다. 단, 클래스는 `let`, `const` 키워드로 선언한 변수처럼 호이스팅된다. 따라서 클래스 선언문 이전에 TDZ에 빠지기 때문에 호이스팅이 발생하지 않는 것처럼 동작한다.

`var`, `let`, `const`, `function`, `function*`, `class` 키워드를 사용하여 선언된 모든 식별자는 호이스팅된다. 모든 선언문은 런타임 이전에 먼저 실행되기 때문이다.

## 인스턴스 생성
**클래스는 생성자 함수이며 `new` 연산자와 함께 호출되어 인스턴스를 새엇ㅇ한다.**

```javascript
class Person {}

// 인스턴스 생성
const me = new Person();
console.log(me); // Person {}
```

클래스는 인스턴스를 생성하는 것이 유일한 존재 이유이므로 반드시 `new` 연산자와 함께 호출해야 한다.

```javascript
class Person {}

const me = Person();
// TypeError: Class constructor Person cannot be invoked without 'new'
```

클래스 표현식으로 정의된 클래스의 경우 다음 예제와 같이 클래스를 가리키는 식별자를 사용해 인스턴스를 생성하지 않고 기명 클래스 표현식의 클래스 이름을 사용해 인스턴스를 생성하면 에러가 발생한다.

```javascript
const Person = class MyClass {};

// 함수 표현식과 마찬가지로 클래스를 가리키는 식별자로 인스턴스를 생성해야 한다.
const me = new Person();

// 클래스 이름 MyClass는 함수와 동일하게 클래스 몸체 내부에서만 유효한 식별자다.
console.log(MyClass); // ReferenceError: MyClass is not defined

const you = new MyClass(); // ReferenceError: MyClass is not defined
```

이는 기명 함수 표현식과 마찬가지로 클래스 표현식에서 사용한 클래스 이름은 외부 코드에서 접근 불가능하기 때문이다.

## 메서드
### constructor
`constructor`는 인스턴스를 생성하고 초기화하기 위한 특수한 메서드다. 이는 이름을 변경할 수 없다.

```javascript
class Person {
  // 생성자
  constructor(name) {
    // 인스턴스 생성 및 초기화
    this.name = name;
  }
}
```

앞에서 살펴보았듯이 클래스는 인스턴스를 생성하기 위한 생성자 함수다.

```javascript
console.log(typeof Person); // function
console.dir(Person);
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/14c64284-0af5-4b1c-a26a-08ece186f67d)

클래스는 평가되어 함수 객체가 된다. 클래스도 함수 객체 고유의 프로퍼티를 모두 갖고 있다. 함수와 동일하게 프로토타입과 연결되어 있으며 자신의 스코프 체인을 구성한다.

**모든 함수 객체가 가지고 있는 `prototype` 프로퍼티가 가리키는 프로토타입 객체의 `constructor` 프로퍼티는 클래스 자신을 가리키고 있다. 이는 클래스가 인스턴스를 생성하는 생성자 함수라는 것을 의미한다.** 즉, `new` 연산자와 함께 클래스를 호출하면 클래스는 인스턴스를 생성한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/83dfc5ae-7c04-40ed-84ff-f9d33ee2031a)

`Person` 클래스의 `constructor` 내부에서 `this`에 추가한 `name` 프로퍼티가 클래스가 생성한 인스턴스의 프로퍼티로 추가된 것을 확인할 수 있다. 즉, 생성자 함수와 마찬가지로 `constructor` 내부에서 `this`에 추가한 프로퍼티는 인스턴스 프로퍼티가 된다. `constructor` 내부의 `this`는 생성자 함수와 마찬가지로 클래스가 생성한 인스턴스를 가리킨다.

```javascript
// 클래스
class Person {
  constructor(name) {
    // 인스턴스 생성 및 초기화
    this.name = name;
  }
}

// 생성자 함수
function Person(name) {
  // 인스턴스 생성 및 초기화
  this.name = name;
}
```

클래스가 평가되어 생성된 함수 객체나 클래스가 생성한 인스턴스 어디에도 `constructor` 메서드가 보이지 않는다. 이는 클래스 몸체에 정의한 `constructor`가 단순한 메서드가 아니라는 것을 의미한다.

**`constructor`는 메서드로 해석되는 것이 아니라 클래스가 평가되어 생성한 함수 객체 코드의 일부가 된다. 다시 말해, 클래스 정의가 평가되면 `constructor`의 기술된 동작을 하는 함수 객체가 생성된다.**

---
📌 클래스의 `constructor` 메서드와 프로토타입의 `constructor` 프로퍼티는 이름이 같아 혼동하기 쉽지만 직접적인 관련이 없다. 프로토타입의 `constructor` 프로퍼티는 모든 프로토타입이 가지고 있는 프로퍼티이며, 생성자 함수를 가리킨다.

---

`constructor`는 생성자 함수와 유사하지만 몇 가지 차이가 있다.

```javascript
// constructor는 클래스 내에 최대 한 개만 존재할 수 있다.
class A {
  constructor() {}
  constructor() {}
}
// SyntaxError: A class may only have one constructor

// constructor는 생략할 수 있다.
class B {}

class C {
  // constructor는 생략하면 아래와 같이 빈 constructor가 암묵적으로 정의된다.
  constructor() {}
}

// 빈 객체가 생성된다.
const c = new C();
console.log(c); // C {}
```

프로퍼티가 추가되어 초기화된 인스턴스를 생성하려면 `constructor` 내부에서 `this`에 인스턴스 프로퍼티를 추가한다.

```javascript
class Person {
  constructor() {
    // 고정값으로 인스턴스 초기화
    this.name = 'Ramos';
    this.address = 'Spain';
  }
}

// 인스턴스 프로퍼티가 추가된다.
const me = new Person();
console.log(me); // Person {name: "Ramos", address: "Spain"}
```

```javascript
class Person {
  constructor(name, address) {
    // 인수로 인스턴스 초기화
    this.name = name;
    this.address = address;
  }
}

// 인수로 초기값을 전달한다. 초기값은 constructor에 전달된다.
const me = new Person('Ramos', 'Spain');
console.log(me); // Person {name: "Ramos", address: "Spain"}
```

`constructor`는 별도의 반환문을 갖지 않아야 한다. 생성자 함수의 인스턴스 생성과정에서 본 것처럼 `new` 연산자와 함께 클래스가 호출되면 생성자 함수와 동일하게 암묵적으로 `this`, 즉 인스턴스를 반환하기 때문이다.

만약 `this`가 아닌 다른 객체를 명시적으로 반환하면 `this`, 즉 인스턴스가 반환되지 못하고 `return`문에 명시한 객체가 반환된다. 마찬가지로 명시적으로 원시값을 반환하면 원시값 반환은 무시되고 암묵적으로 `this`가 반환된다.

`constructor` 내부에서 `return`문을 생략하자.

### 프로토타입 메서드
클래스 몸체에서 정의한 메서드는 생성자 함수에 의한 객체 생성 방식과는 다르게 클래스의 `prototype` 프로퍼티에 메서드를 추가하지 않아도 기본적으로 프로토타입 메서드가 된다.

```javascript
class Person {
  // 생성자
  constructor(name) {
    // 인스턴스 생성 및 초기화
    this.name = name;
  }

  // 프로토타입 메서드
  sayHi() {
    console.log(`Hi! My name is ${this.name}`);
  }
}

const me = new Person('Lee');
me.sayHi(); // Hi! My name is Lee
```

생성자 함수와 마찬가지로 클래스가 생성한 인스턴스는 프로토타입 체인의 일원이 된다.

```javascript
// me 객체의 프로토타입은 Person.prototype이다.
Object.getPrototypeOf(me) === Person.prototype; // true
me instanceof Person; // true

// Person.prototype의 프로토타입은 Object.prototype이다.
Object.getPrototypeOf(Person.prototype) === Object.prototype; // true
me instanceof Object; // true

// me 객체의 constructor는 Person 클래스다.
me.constructor === Person; // true
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/726fbf24-ab73-4714-9a5b-6bfda36b9cfa)

클래스 몸체에서 정의한 메서드는 인스턴스의 프로토타입에 존재하는 프로토타입 메서드가 된다. 인스턴스는 프로토타입 메서드를 상속받아 사용할 수 있다.

프로토타입 체인은 기존의 모든 객체 생성 방식 뿐만 아니라 클래스에 의해 생성된 인스턴스에도 동일하게 적용된다. 생성자 함수의 역할을 클래스가 할 뿐이다.

**결국 클래스는 생성자 함수와 같이 인스턴스를 생성하는 생성자 함수라고 볼 수 있다. 다시 말해, 클래스는 생성자 함수와 마찬가지로 프로토타입 기반의 객체 생성 메커니즘이다.**

### 정적 메서드
정적 메서드는 인스턴스를 생성하지 않아도 호출할 수 있는 메서드를 말한다.

```javascript
class Person {
  // 생성자
  constructor(name) {
    // 인스턴스 생성 및 초기화
    this.name = name;
  }

  // 정적 메서드
  static sayHi() {
    console.log('Hi!');
  }
}
```

위 예제의 `Person` 클래스는 다음과 같이 프로토타입 체인을 생성한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/83bf0bf1-58dd-4542-bb26-5c4efb228c2e)

이처럼 정적 메서드는 클래스에 바인딩된 메서드가 된다. 클래스는 함수 객체로 평가되므로 자신의 프로퍼티/메서드를 소유할 수 있다. 클래스는 클래스 정의(클래스 선언문이나 클래스 표현식)가 평가되는 시점에 함수 객체가 되므로 인스턴스와 달리 별다른 생성 과정이 필요 없다. 따라서 정적 메서드는 클래스 정의 이후 인스턴스를 생성하지 않아도 호출할 수 있다.

정적 메서드는 프로토타입 메서드처럼 인스턴스로 호출하지 않고 클래스로 호출한다.

```javascript
Person.sayHi(); // Hi!

const me = new Person('Ramos');
me.sayHi(); // TypeError: me.sayHi is not a function
```

정적 메서드는 인스턴스로 호출할 수 없다. 정적 메서드가 바인딩된 클래스는 인스턴스의 프로토타입 체인 상에 존재하지 않기 때문이다. 다시 말해, 인스턴스의 프로토타입 체인 상에는 클래스가 존재하지 않기 때문에 인스턴스로 클래스의 메서드를 상속받을 수 없다.

### 정적 메서드와 프로토타입 메서드의 차이
- 정적 메서드와 프로토타입 메서드는 자신이 속해 있는 프로토타입 체인이 다르다.
- 정적 메서드는 클래스로 호출하고 프로토타입 메서드는 인스턴스로 호출한다.
- 정적 메서드는 인스턴스 프로퍼티를 참조할 수 없지만 프로토타입 메서드는 인스턴스 프로퍼티를 참조할 수 있다.

```javascript
class Square {
  static area(width, height) {
    return width * height;
  }
}

console.log(Square.area(10, 10)); // 100
```

정적 메서드 `area`는 인스턴스 프로퍼티를 참조하지 않는다. 만약 인스턴스 프로퍼티를 참조해야 한다면 정적 메서드 대신 프로토타입 메서드를 사용해야 한다.

```javascript
class Square {
  constructor(width, height) {
    this.width = width;
    this.height = height;
  }

  // 프로토타입 메서드
  area() {
    return this.width * this.height;
  }
}

const square = new Square(10, 10)
console.log(square.area()); // 100
```

메서드 내부의 `this`는 메서드를 소유한 객체가 아니라 메서드를 호출한 객체, 즉 메서드 이름 앞의 마침표(.) 연산자 앞에 기술한 객체에 바인딩된다.

프로토타입 메서드는 인스턴스로 호출해야 하므로 프로토타입 메서드 내부의 `this`는 프로토타입 메서드를 호출한 인스턴스를 가리킨다. 
정적 메서드는 클래스로 호출해야 하므로 정적 메서드 내부의 `this`는 인스턴스가 아닌 클래스를 가리킨다. 즉, 프로토타입 메서드와 정적 메서드 내부의 `this` 바인딩이 다르다.

따라서 메서드 내부에서 인스턴스 프로퍼티를 참조할 필요가 있다면 `this`를 사용해야 하며, 이러한 경우 프로토타입 메서드로 정의해야 한다. 하지만 메서드 내부에서 인스턴스 프로퍼티를 참조해야 할 필요가 없다면 `this`를 사용하지 않게 된다.

표준 빌트인 객체들은 다양한 정적 메서드를 가지고 있다. 이들 정적 메서드는 애플리케이션 전역에서 사용할 유틸리티 함수다.

```javascript
Math.max(1, 2, 3); // 3
JSON.stringify({ a: 1 }); // "{"a":1}"
```

이처럼 클래스 또는 생성자 함수를 하나의 네임스페이스로 사용하여 정적 메서드를 모아 놓으면 이름 충돌 가능성을 줄여 주고 관련 함수들을 구조화할 수 있는 효과가 있다. 이 같은 이유로 정적 메서드는 애플리케이션 전역에서 사용할 유틸리티 함수를 전역 함수로 정의하지 않고 메서드로 구조화할 때 유용하다.

### 클래스에서 정의한 메서드의 특징
- `function` 키워드를 생략한 메서드 축약 표현을 사용한다.
- 객체 리터럴과는 다르게 클래스에 메서드를 정의할 때는 콤마가 필요 없다.
- 암묵적으로 strict mode로 실행된다.
- `for...in` 문이나 `Object.keys()` 메서드 등으로 열거할 수 없다. 즉, 프로퍼티의 열거 가능 여부를 나타내며, 불리언 값을 갖는 프로퍼티 어트리뷰트 `[[Enumerable]]`이 `false`다.
- 내부 메서드 `[[Construct]]`를 갖지 않는 `non-constructor`다. 따라서 `new` 연산자와 함께 호출할 수 없다.

## 클래스의 인스턴스 생성 과정
`new` 연산자와 클래스를 호출하면 생성자 함수와 마찬가지로 클래스의 내부 메서드 `[[Construct]]`가 호출된다. 클래스는 `new` 연산자 없이 호출할 수 없다. 생성자 함수의 인스턴스 생성 과정과 유사하게 인스턴스가 생성된다.

### 1. 인스턴스 생성과 this 바인딩
`new` 연산자와 함께 클래스를 호출하면 `constructor`의 내부 코드가 실행되기에 앞서 암묵적으로 빈 객체가 생성된다. 이 빈 객체가 바로 클래스가 생성한 인스턴스다. 이때 클래스가 생성한 인스턴스의 프로토타입으로 클래스의 `prototype` 프로퍼티가 가리키는 객체가 설정된다. 그리고 암묵적으로 생성된 빈 객체, 즉 인스턴스는 `this`에 바인딩된다. 따라서 `constructor` 내부의 `this`는 클래스가 생성한 인스턴스를 가리킨다.

### 2. 인스턴스 초기화
`constructor`의 내부 코드가 실행되어 `this`에 바인딩되어 있는 인스턴스를 초기화한다. 즉, `this`에 바인딩되어 있는 인스턴스에 프로퍼티를 추가하고 `constructor`가 인수로 전달받은 초기값으로 인스턴스의 프로퍼티 값을 초기화한다. 만약 `constructor`가 생략되었다면 이 과정도 생략된다.

### 3. 인스턴스 반환
클래스의 모든 처리가 끝나면 완성된 인스턴스가 바인딩된 `this`가 암묵적으로 반환된다.

```javascript
class Person {
  // 생성자
  constructor(name) {
    // 1. 암묵적으로 인스턴스가 생성되고 this에 바인딩된다.
    console.log(this); // Person {}
    console.log(Object.getPrototypeOf(this) === Person.prototype); // true

    // 2. this에 바인딩되어 있는 인스턴스를 초기화한다.
    this.name = name;

    // 3. 완성된 인스턴스가 바인딩된 this가 암묵적으로 반환된다.
  }
}
```

## 프로퍼티
### 인스턴스 프로퍼티
인스턴스 프로퍼티는 `constructor` 내부에서 정의해야 한다.

```javascript
class Person {
  constructor(name) {
    // 인스턴스 프로퍼티
    this.name = name; // public
  }
}

const me = new Person('Ramos');
console.log(me); // Person {name: "Ramos"}
```

ES6의 클래스는 다른 객체지향 언어처럼 `private`, `public`, `protected` 키워드와 같은 접근 제한자를 지원하지 않는다. 따라서 인스턴스 프로퍼티는 언제나 `public` 하다.

### 접근자 프로퍼티
접근자 프로퍼티는 자체적으로는 값(`[[Value]]` 내부 슬롯)을 갖지 않고 다른 데이터 프로퍼티의 값을 읽거나 저장할 때 사용하는 접근자 함수로 구성된 프로퍼티다.

```javascript
class Person {
  constructor(firstName, lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  // getter
  get fullName() {
    return `${this.firstName} ${this.lastName}`;
  }
  
  // settter
  set fullName() {
    [this.firstName, this.lastName] = name.split(' ');
  }
}

const me = new Person('Sergio', 'Ramos');

// 접근자 프로퍼티를 통한 프로퍼티 값의 참조
console.log(`${me.firstName} ${me.lastName}`); // Sergio Ramos

// 접근자 프로퍼티를 통한 프로퍼티 값의 저장
// setter 함수가 호출된다.
me.fullName = 'Toni Kroos';
console.log(me); // {firstName: "Toni", lastName: "Kroos"}

// 접근자 프로퍼티를 통한 프로퍼티 값의 참조
console.log(me.fullName); // Toni Kroos

// fullName은 접근자 프로퍼티다.
console.log(Object.getOwnPropertyDescriptor(Person.prototype, 'fullName'));
// {get: f, set: f, enumerable: false, configurable: true}
```

getter와 setter 이름은 인스턴스 프로퍼티처럼 사용된다. 다시 말해 getter는 호출하는 것이 아니라 프로퍼티처럼 참조하는 형식으로 사용하며, 참조 시에 내부적으로 getter가 호출된다. setter도 호출하는 것이 아니라 프로퍼티처럼 값을 할당하는 형식으로 사용하며, 할당 시에 내부적으로 setter가 호출된다.

**클래스의 메서드는 기본적으로 프로토타입 메서드가 된다. 따라서 클래스의 접근자 프로퍼티 또한 인스턴스 프로퍼티가 아닌 프로토타입의 프로퍼티가 된다.**

```javascript
Object.getOwnPropertyNames(me); // ["firstName", "lastName"]
Object.getOwnPropertyNames(Object.getPrototypeOf(me)); // ["constructor", "fullName"]
```

### 클래스 필드 정의 제안
JavaScript의 클래스 몸체에는 메서드만 선언할 수 있다. 따라서 클래스 몸체에 Java와 유사하게 클래스 필드를 선언하면 문법 에러가 발생한다.

```javascript
class Person {
  // 클래스 필드 정의
  name = 'Ramos';
}

const me = new Person('Ramos');
```

하지만 위 예제를 최신 브라우저 또는 최신 Node.js(버전 12 이상)에서 실행하면 문법 에러가 발생하지 않고 정상 동작한다.

JavaScript에서도 인스턴스 프로퍼티를 마치 클래스 기반 객체지향 언어의 클래스 필드처럼 정의할 수 있는 새로운 표준 사양이 [TC39 프로세스의 stage 3에 제안](https://github.com/tc39/proposal-class-fields)되어 있다. (2021년 1월 기준)

클래스 필드 정의 제안에 대해 최신 브라우저와 최신 Node.js는 표준 사양으로 승급이 확실시 됐던 이 제안을 선제적으로 미리 구현해 놓았다. 따라서 클래스 필드를 클래스 몸체에 정의할 수 있다.

클래스 몸체에서 클래스 필드를 정의하는 경우 `this`에 클래스 필드를 바인딩해서는 안 된다. `this`는 클래스의 `constructor`와 메서드 내에서만 유효하다.

```javascript
class Person {
  this.name = ''; // SyntaxError: Unexpected token '.'
}
```

클래스 필드를 참조하는 경우 Java와 같은 클래스 기반 객체지향 언어에선 `this`를 생략할 수 있으나 JavaScript에선 `this`를 반드시 사용해야 한다.

```javascript
class Person {
  name = 'Ramos';

  constructor() {
    console.log(name); // ReferenceError: name is not defined
  }
}

new Person();
```

클래스 필드에 초기값을 할당하지 않으면 `undefined`를 갖는다.

인스턴스를 생성할 때 외부의 초기값으로 클래스 필드를 초기화해야 할 필요가 있다면 `constructor`에서 클래스 필드를 초기화해야 한다.

인스턴스를 생성할 때 클래스 필드를 초기화할 필요가 있다면 `constructor` 밖에서 클래스 필드를 정의할 필요가 없다. 클래스 필드를 초기화할 필요가 있다면 어차피 `constructor` 내부에서 클래스 필드를 참조하여 초기값을 할당해야 한다. 이때 `this`, 즉 클래스가 생성한 인스턴스에 클래스 필드에 해당하는 프로퍼티가 없다면 자동 추가되기 때문이다.

```javascript
class Person {
  constructor(name) {
    this.name = name;
  }
}

const me = new Person('Ramos');
console.log(me); // Person {name: "Ramos"}
```

함수는 일급 객체이므로 함수를 클래스 필드에 할당할 수 있다. 따라서 클래스 필드를 통해 메서드를 정의할 수도 있다.

```javascript
class Person {
  // 클래스 필드에 문자열을 할당
  name = 'Ramos';

  // 클래스 필드에 함수를 할당
  getName = function () {
    return this.name;
  }
  // 화살표 함수로 정의할 수도 있다.
  // getName = () => this.name;
}

const me = new Person();
console.log(me); // Person {name: "Ramos", getName: f}
console.log(me.getName()); // Ramos
```

**이처럼 클래스 필드에 함수를 할당하는 경우, 이 함수는 프로토타입 메서드가 아닌 인스턴스 메서드가 된다.** 모든 클래스 필드는 인스턴스 프로퍼티가 되기 때문이다. 따라서 클래스 필드에 함수를 할당하는 것은 권장하지 않는다.

### private 필드 정의 제안
클래스 필드 정의 제안을 사용하더라도 클래스 필드는 기본적으로 `public` 하기 때문에 외부에 그대로 노출된다. TC39 프로세스의 stage 3(candidate)에는 `private` 필드를 정의할 수 있는 새로운 표준 사양이 제안 되어 있다.

```javascript
class Person {
  // private 필드 정의
  #name = '';

  constructor(name) {
    // private 필드 참조
    this.#name = name;
  }
}

const me = new Person('Ramos');

// private 필드 #name은 클래스 외부에서 참조할 수 없다.
console.log(me.#name);
// SyntaxError: Private field '#name' must be declared in an enclosing class
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/89d4b57b-fcda-4fff-93c3-7b83453f66ad)

```javascript
class Person {
  // private 필드 정의
  #name = '';

  constructor(name) {
    // private 필드 참조
    this.#name = name;
  }

  // name은 접근자 프로퍼티다.
  get name() {
    // private 필드를 참조하여 trim한 다음 반환
    return this.#name.trim();
  }
}

const me = new Person('Ramos');
console.log(me.name); // Ramos
```

`private` 필드는 반드시 클래스 몸체에 정의해야 한다. `private` 필드를 직접 `constructor`에 정의하면 에러가 발생한다.

```javascript
class Person {
  constructor(name) {
    // private 필드는 클래스 몸체에서 정의해야 한다.
    this.#name = name;
    // SyntaxError: Private field '#name' must be declared in an enclosing class
  }
}
```

### static 필드 정의 제안
클래스에는 `static` 키워드를 사용하여 정적 메서드를 정의할 수 있지만 정적 필드를 정의할 수는 없었다.

하지만 `static public` 필드, `static private` 필드, `static private` 메서드를 정의할 수 있는 새로운 표준 사양인 "Static class features"가 TC39 프로세스의 stage 3에 제안되어 있다.

```javascript
class MyMath {
  // static public 필드 정의
  static PI = 22 / 7;

  // static private 필드 정의
  static #num = 10;

  // static 메서드
  static increment() {
    return ++MyMath.#num;
  }
}

console.log(MyMath.PI); // 3.142857...
console.log(MyMath.increment()); // 11
```

## 상속에 의한 클래스 확장
### 클래스 상속과 생성자 함수 상속
**상속에 의한 클래스 확장은 프로토타입 기반 상속과는 다른 개념이다.** 프로토타입 기반 상속은 프로토타입 체인을 통해 다른 객체의 자산을 상속받는 개념이지만 **상속에 의한 클래스 확장은 기존 클래스를 상속받아 새로운 클래스를 확장하여 정의**하는 것이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d1a5c97c-fe27-4a77-9a70-00214c55c5d6)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/cf62af43-5153-40d8-ad15-af2f0b59536f)

```javascript
class Animal {
  constructor(age, weight) {
    this.age = age;
    this.weight = weight;
  }

  eat() { return 'eat'; }

  move() { return 'move'; }
}

// 상속을 통해 Animal 클래스를 확장한 Bird 클래스
class Bird extends Animal {
  fly() { return 'fly'; }
}

const bird = new Bird(1, 5);

console.log(bird); // Bird {age: 1, weight: 5}
console.log(bird instanceof Bird); // true
console.log(bird instanceof Animal); // true

console.log(bird.eat()); // eat
console.log(bird.move()); // move
console.log(bird.fly()); // fly
```

상속에 의해 확장된 클래스 `Bird`를 통해 생성된 인스턴스의 프로토타입 체인은 다음과 같다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/035c62ca-6542-4458-8dc0-d7e4aeb84dcf)

클래스는 상속을 통해 다른 클래스를 확장할 수 있는 문법인 `extends` 키워드가 기본적으로 제공된다. 이를 통한 클래스 확장은 간편하고 직관적이다. 하지만 생성자 함수는 클래스와 같이 상속을 통해 다른 생성자 함수를 확장할 수 있는 문법이 제공되지 않는다.

JavaScript는 클래스 기반 언어가 아니므로 생성자 함수를 사용해 클래스를 흉내 내려는 시도를 권장하진 않지만 의사 클래스 상속 패턴을 사용하여 상속에 의한 클래스 확장을 흉내 내기도 했다. 클래스의 등장으로 해당 패턴은 더는 필요하지 않다.

### extends 키워드
```javascript
class Base {}

class Derived extends Base {}
```

`extends` 키워드의 역할은 수퍼클래스와 서브클래스 간의 상속 관계를 설정하는 것이다. 클래스도 프로토타입을 통해 상속 관계를 구현한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/cf0067e5-bcf8-4841-bdae-8db5fb976ef9)

**수퍼클래스와 서브클래스는 인스턴스의 프로토타입 체인뿐 아니라 클래스 간의 프로토타입 체인도 생성한다. 이를 통해 프로토타입 메서드, 정적 메서드 모두 상속이 가능하다.**

### 동적 상속
`extends` 키워드는 클래스뿐만 아니라 생성자 함수를 상속받아 클래스를 확장할 수도 있. 단, `extends` 키워드 앞에는 반드시 클래스가 와야 한다.

```javascript
// 생성자 함수
function Base(a) {
  this.a = a;
}

// 생성자 함수를 상속받는 서브클래스
class Derived extends Base {}

const derived = new Derived(1);
console.log(derived); // Derived {a: 1}
```

`extends` 키워드 다음에는 클래스뿐만이 아니라 `[[Construct]]` 내부 메서드를 갖는 함수 객체로 평가될 수 있는 모든 표현식을 사용할 수 있다. 이를 통해 동적으로 상속받을 대상을 결정할 수 있다.

```javascript
function Base1() {}

class Base2 {}

let condition = true;

// 조건에 따라 동적으로 상속 대상을 결정하는 서브클래스
class Derived extends (condition ? Base1 : Base2) {}

const derived = new Derived();
console.log(derived); // Derived {}

console.log(derived instanceof Base1); // true
console.log(derived instanceof Base2); // false
```

### 서브클래스의 constructor
서브클래스에서 `constructor`를 생략하면 클래스에 다음과 같은 `constructor`가 암묵적으로 정의된다. `args`는 `new` 연산자와 함께 클래스를 호출할 때 전달한 인수의 리스트다.

```javascript
constructor(...args) { super(...args); }
```

`super()`는 수퍼클래스의 `constructor`를 호출하여 인스턴스를 생성한다.

```javascript
class Base {}

class Derived extends Base {}

// 위는 아래와 같이 암묵적으로 constructor가 정의된다.
class Base {
  constructor() {}
}

class Derived extends Base {
  constructor(...args) { super(...args); }
}

const derived = new Derived();
console.log(derived); // Derived {}
```

위와 같이 수퍼, 서브 모두 `constructor`를 생략하면 빈 객체가 생성된다.

### super 키워드
`super` 키워드는 함수처럼 호출할 수도 있고 `this`와 같이 식별자처럼 참조할 수 잇는 특수한 키워드다. `super`는 다음과 같이 동작한다.

- `super`를 호출: 수퍼클래스의 `constructor`를 호출한다.
- `super`를 참조: 수퍼클래스의 메서드를 호출할 수 있다.

#### super 호출
```javascript
class Base {
  constructor(a, b) {
    this.a = a;
    this.b = b;
  }
}

class Derived extends Base {
  // 암묵적으로 constructor가 정의된다.
  // constructor(...args) { super(...args); }
}

const derived = new Derived(1, 2);
console.log(derived); // Derived {a: 1, b: 2}
```

아래 예제와 같이 수퍼클래스에서 추가한 프로퍼티와 서브클래스에서 추가한 프로퍼티를 갖는 인스턴스를 생성한다면 `constructor`를 생략할 수 없다. 이때 `new` 연산자와 함께 서브클래스를 호출하면서 전달한 인수 중 수퍼클래스의 `constructor`에 전달할 필요가 있는 인수는 서브클래스의 `constructor`에서 호출하는 `super`를 통해 전달한다.

```javascript
class Base {
  constructor(a, b) {
    this.a = a;
    this.b = b;
  }
}

class Derived extends Base {
  constructor(a, b, c) {
    super(a, b);
    this.c = c;
  }
}

const derived = new Derived(1, 2, 3);
console.log(derived); // Derived {a: 1, b: 2, c: 3}
```

이처럼 인스턴스 초기화를 위해 전달한 인수는 수퍼클래스와 서브클래스에 배분되고 상속 관계의 두 클래스는 서로 협력하여 인스턴스를 생성한다.

`super`를 호출할 때 주의할 사항은 다음과 같다.

1. 서브클래스에서 `constructor`를 생략하지 않는 경우 서브클래스의 `constructor`에서는 반드시 `super`를 호출해야 한다.
2. 서브클래스의 `constructor`에서 `super`를 호출하기 전에는 `this`를 참조할 수 없다.
3. `super`는 반드시 서브클래스의 `constructor`에서만 호출한다. 서브클래스가 아닌 클래스의 `constructor`나 함수에서 `super`를 호출하면 에러가 발생한다.

```javascript
// 1.
class Base {}

class Derived extends Base {
  constructor() {
    // ReferenceError: Must call super constructor in derived class
    // before accessing 'this' or returning from derived constructor
    console.log('constructor call');
  }
}

const derived = new Derived();

// 2.
class Base {}

class Derived extends Base {
  constructor() {
    // ReferenceError: Must call super constructor in derived class
    // before accessing 'this' or returning from derived constructor
    this.a = 1;
    super();
  }
}

const derived = new Derived(1);

// 3.
class Base {
  constructor() {
    super(); // SyntaxError: 'super' keyword unexpected here
  }
}

function Foo() {
  super(); // SyntaxError: 'super' keyword unexpected here
}
```

#### super 참조
1. 서브클래스의 프로토타입 메서드 내에서 `super.sayHi`는 수퍼클래스의 프로토타입 메서드 `sayHi`를 가리킨다.

```javascript
class Base {
  constructor(name) {
    this.name = name;
  }

  sayHi() {
    return `Hi! ${this.name}`;
  }
}

class Derived extends Base {
  sayHi() {
    return `${super.sayHi()} how are you doing?`;
  }
}

const derived = new Derived('Ramos');
console.log(derived.sayHi()); // Hi! Ramos how are you doing?

// 위는 아래와 같이 동작한다.
class Base {
  constructor(name) {
    this.name = name;
  }

  sayHi() {
    return `Hi! ${this.name}`;
  }
}

class Derived extends Base {
  sayHi() {
    // __super는 Base.prototype을 가리킨다.
    const __super = Object.getPrototypeOf(Derived.prototype);
    return `${__super.sayHi.call(this)} how are you doing?`;
  }
}
```

주의할 것은 ES6의 메서드 축약 표현으로 정의된 함수만이 `[[HomeObject]]`를 갖는다는 것이다.

```javascript
const obj = {
  // ES6의 메서드 축약 표현으로 정의한 메서드다. 따라서 [[HomeObject]]를 갖는다.
  foo() {},

  // ES6의 메서드 축약 표현으로 정의한 메서드가 아니라 일반 함수다.
  // [[HomeObject]]를 갖지 않는다.
  bar: function () {}
}
```

```javascript
const base = {
  name: 'Ramos',
  sayHi() {
    return `Hi! ${this.name}`;
  }
};

const derived = {
  __proto__: base,
  // [[HomeObject]]를 갖는다.
  sayHi() {
    return `${super.sayHi()} how are you doing?`
  }
};

console.log(derived.sayHi()); // Hi! Ramos how are you doing?
```

2. 서브클래스의 정적 메서드 내에서 `super.sayHi`는 수퍼클래스의 정적 메서드 `sayHi`를 가리킨다.

```javascript
class Base {
  static sayHi() {
    return 'Hi!';
  }
}

class Derived extends Base {
  static sayHi() {
    return `${super.sayHi()} how are you doing?`;
  }
}

console.log(Derived.sayHi()) // Hi! how are you doing?
```

### 상속 클래스의 인스턴스 생성 과정
상속 관계에 있는 두 클래스가 어떻게 협력하며 인스턴스를 생성하는지 살펴보도록 하자. 이를 통해 `super`를 명확하게 이해할 수 있을 것이다.

클래스가 단독으로 인스턴스를 생성하는 과정보다 상속 관계에 있는 두 클래스가 협력하며 인스턴스를 생성하는 과정은 좀 더 복잡하다.

```javascript
class Rectangle {
  constructor(width, height) {
    this.width = width;
    this.height = height;
  }

  getArea() {
    return this.width * this.height;
  }

  toString() {
    return `width = ${this.width}, height = ${this.height}`;
  }
}

class ColorRectangle extends Rectangle {
  constructor(width, height, color) {
    super(width, height);
    this.color = color;
  }

  // 메서드 오버라이딩
  toString() {
    return super.toString() + `, color = ${this.color}`;
  }
}

const colorRectangle = new ColorRectangle(2, 4, 'red');
console.log(colorRectangle); // ColorRectangle {width: 2, height: 4, color: "red"}

// 상속을 통해 메서드 호출
console.log(colorRectangle.getArea()); // 8
// 오버라이딩된 메서드 호출
console.log(colorRectangle.toString()); // width = 2, height = 4, color = red
```

`ColorRectangle` 클래스에 의해 생성된 인스턴스의 프로토타입 체인은 다음과 같다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3bdc83dc-31a8-49fb-8c43-3de105146da9)

서브클래스가 `new` 연산자와 함께 호출되면 다음 과정을 통해 인스턴스를 생성한다.

#### 1. 서브클래스의 `super` 호출
JS 엔진은 클래스를 평가할 때 수퍼클래스와 서브클래스를 구분하기 위해 "base" 또는 "derived"를 값으로 갖는 내부 슬롯 `[[ConstructorKind]]`를 갖는다.

- 다른 클래스를 상속받지 않는 클래스 → "base"로 설정
- 다른 클래스를 상속받는 클래스 → "derived"로 설정

이를 통해 수퍼클래스와 서브클래스는 `new` 연산자와 함께 호출되었을 때의 동작이 구분된다.

다른 클래스를 상속받지 않는 클래스(그리고 생성자 함수)는 `new` 연산자와 함께 호출되었을 때 암묵적으로 빈 객체, 즉 인스턴스를 생성하고 이를 `this`에 바인딩한다.

하지만 **서브클래스는 자신이 직접 인스턴스를 생성하지 않고 수퍼클래스에게 인스턴스 생성을 위임한다. 이것이 바로 서브클래스의 `constructor`에서 반드시 `super`를 호출해야 하는 이유다.**

서브클래스가 `new` 연산자와 함께 호출되면 서브클래스 `constructor` 내부의 `super` 키워드가 함수처럼 호출된다. `super 가 호출되면 수퍼클래스의 `constructor`(super-constructor)가 호출된다. 좀 더 정확히 말하자면 수퍼클래스가 평가 되어 생성된 함수 객체의 코드가 실행되기 시작한다.

만약 서브클래스 `constructor` 내부에 `super` 호출이 없으면 에러가 발생한다. 실제로 인스턴스를 생성하는  체는 수퍼클래스이므로 수퍼클래스의 `constructor`를 호출하는 `super`가 호출되지 않으면 인스턴스를 생성할 수 없기 때문이다.

#### 2. 수퍼클래스의 인스턴스 생성과 `this` 바인딩
수퍼클래스의 `constructor` 내부의 코드가 실행되기 이전에 암묵적으로 빈 객체를 생성한다. 이 빈 객체가 바로 (아직 완성되지는 않았지만) 클래스가 생성한 인스턴스다. 그리고 암묵적으로 생성된 빈 객체, 즉 인스턴스는 `this`에 바인딩된다. 따라서 수퍼클래스의 `constructor` 내부의 `this`는 생성된 인스턴스를 가리킨다.

```javascript
class Rectangle {
  constructor(width, height) {
    // 암묵적으로 빈 객체, 즉 인스턴스가 생성되고 this에 바인딩된다.
    console.log(this); // ColorRectangle {}
    // new 연산자와 함께 호출된 함수, 즉 new.target은 ColorRectangle이다.
    console.log(new.target); // ColorRectangle
  }
  // ...
}
```

이때 인스턴스는 수퍼클래스가 생성한 것이다. 하지만 `new` 연산자와 함께 호출된 클래스가 서브클래스라는 것이 중요하다. 즉, `new` 연산자와 함께 호출된 함수를 가리키는 `new.target`은 서브클래스를 가리킨다. 따라서 인스턴스는 `new.target`이 가리키는 서브클래스가 생성한것으로 처리된다.

따라서 생성된 인스턴스의 프로토타입은 수퍼클래스의 `prototype` 프로퍼티가 가리키는 객체(`Rectangle.prototype`)가 아니라 `new.target`, 즉 서브클래스의 `prototype` 프로퍼티가 가리키는 객체(`ColorRectangle.prototype`)다.

```javascript
class Rectangle {
  constructor(width, height) {
    // 암묵적으로 빈 객체, 즉 인스턴스가 생성되고 this에 바인딩된다.
    console.log(this); // ColorRectangle {}
    // new 연산자와 함께 호출된 함수, 즉 new.target은 ColorRectangle이다.
    console.log(new.target); // ColorRectangle

    // 생성된 인스턴스의 프로토타입으로 ColorRectangle.prototype이 설정된다.
    console.log(Object.getPrototypeOf(this) === ColorRectangle.prototype); // true
    console.log(this instanceof ColorRectangle); // true
    console.log(this instanceof Rectangle); // true
  }
  // ...
}
```

#### 3. 수퍼클래스의 인스턴스 초기화
수퍼클래스의 `constructor` 가 실행되어 `this`에 바인딩되어 있는 인스턴스를 초기화 한다. 즉, `this`에 바인딩 되어있는 인스턴스에 프로퍼티를 추가하고 `constructor`가 인수로 전달받은 초기값으로 인스턴스의 프로퍼티를 초기화한다.

```javascript
class Rectangle {
  constructor(width, height) {
    // 암묵적으로 빈 객체, 즉 인스턴스가 생성되고 this에 바인딩된다.
    console.log(this); // ColorRectangle {}
    // new 연산자와 함께 호출된 함수, 즉 new.target은 ColorRectangle이다.
    console.log(new.target); // ColorRectangle

    // 생성된 인스턴스의 프로토타입으로 ColorRectangle.prototype이 설정된다.
    console.log(Object.getPrototypeOf(this) === ColorRectangle.prototype); // true
    console.log(this instanceof ColorRectangle); // true
    console.log(this instanceof Rectangle); // true

    // 인스턴스 초기화
    this.width = width;
    this.height = height;

    console.log(this); // ColorRectangle {width: 2, height: 4}
  }
  // ...
}
```

#### 4. 서브클래스 `constructor`로의 복귀와 `this` 바인딩
`super`의 호출이 종료되고 제어 흐름이 서브클래스 `constructor`로 돌아온다. **이때 `super`가 반환한 인스턴스가 `this`에 바인딩된다. 서브클래스는 별도의 인스턴스를 생성하지 않고 `super`가 반환한 인스턴스를 `this`에 바인딩하여 그대로 사용한다.**

```javascript
class ColorRectangle extends Rectangle {
  constructor(width, height, color) {
    super(width, height);

    // super가 반환한 인스턴스가 this에 바인딩된다.
    console.log(this); // ColorRectangle {width: 2, height: 4}
  }
  // ...
}
```

#### 5. 서브클래스의 인스턴스 초기화
`super` 호출 이후, 서브클래스의 `constructor`에 기술되어 있는 인스턴스 초기화가 실행된다. 즉, `this`에 바인딩되어 있는 인스턴스에 프로퍼티를 추가하고 `constructor`가 인수로 전달받은 초기값으로 인스턴스의 프로퍼티를 초기화한다.

#### 6. 인스턴스 반환
클래스의 모든 처리가 끝나면 완성된 인스턴스가 바인딩된 `this`가 암묵적으로 반환된다.

```javascript
class ColorRectangle extends Rectangle {
  constructor(width, height, color) {
    super(width, height);

    // super가 반환한 인스턴스가 this에 바인딩된다.
    console.log(this); // ColorRectangle {width: 2, height: 4}

    this.color = color;

    // 완성된 인스턴스가 바인딩된 this가 암묵적으로 반환된다.
    console.log(this); // ColorRectangle {width: 2, height: 4, color: "red"}
  }
  // ...
}
```

### 표준 빌트인 생성자 함수 확장
앞서 동적 상속에서 살펴보았듯 `extends` 키워드 다음에는 클래스뿐만이 아니라 `[[Contructor]]` 내부 메서드를 갖는 함수 객체로 평가될 수 있는 모든 표현식을 사용할 수 있다. `String`, `Number`, `Array` 같은 표준 빌트인 객체도 `[[Construct]]` 내부 메서드를 갖는 생성자 함수이므로 `extends` 키워드를 사용하여 확장할 수 있다.

```javascript
class MyArray extends Array {
  uniq() {
    return this.filter((v, i, self) => self.indexOf(v) === i);
  }

  average() {
    return this.reduce((pre, cur) => pre + cur, 0) / this.length;
  }
}

const myArray = new MyArray(1, 1, 2, 3);
console.log(myArray); // MyArray(4) [1, 1, 2, 3]

console.log(myArray.uniq()); // MyArray(3) [1, 2, 3]
console.log(myArray.average()); // 1.75
```

`Array` 생성자 함수를 상속받아 확장한 `MyArray` 클래스가 생성한 인스턴스는 `Array.prototype`과 `MyArray.prototype`의 모든 메서드를 사용할 수 있다.

**이때 주의할 것은 `Array.prototype`의 메서드 중에서 `map`, `filter`와 같이 새로운 배열을 반환하는 메서드가 `MyArray` 클래스의 인스턴스를 반환한다는 것이다.**

```javascript
console.log(myArray.filter(v => v % 2) instanceof MyArray); // true
```

만약 새로운 배열을 반환하는 메서드가 `MyArray` 클래스의 인스턴스를 반환하지 않고 `Array`의 인스턴스를 반환하면 `MyArray` 클래스의 메서드와 메서드 체이닝이 불가능하다.

```javascript
// 메서드 체이닝
// [1, 1, 2, 3] => [1, 1, 3] => [1, 3] => 2
console.log(myArray.filter(v => v % 2).uniq().average()); // 2
```

`myArray.filter`가 반환하는 인스턴스는 `MyArray` 클래스가 생성한 인스턴스, 즉 `MyArray` 타입이다. 따라서 `myArray.filter`가 반환하는 인스턴스로 `uniq` 메서드를 메서드 체이닝 할 수 있다. `uniq` 메서드가 반환하는 인스턴스는 `Array.prototype.filter`에 의해 생성되었기 때문에 `Array` 생성자 함수가 생성한 인스턴스로 생성할 수도 있다. 하지만 `uniq` 메서드가 반환하는 인스턴스도 `MyArray` 타입이다. 마찬가지로 메서드 체이닝할 수 있다.

만약 `MyArray` 클래스의 `uniq` 메서드가 `MyArray` 클래스가 생성한 인스턴스가 아닌 `Array`가 생성한 인스턴스를 반환하게 하려면 다음과 같이 `Symbol.species`를 사용하여 정적 접근자 프로퍼티를 추가한다.

```javascript
// Array 생성자 함수를 상속받아 확장한 MyArray
class MyArray extends Array {
  // 모든 메서드가 Array 타입의 인스턴스를 반환하도록 한다.
  static get [Symbol.species]() { return Array; }

  uniq() {
    return this.filter((v, i, self) => self.indexOf(v) === i);
  }

  average() {
    return this.reduce((pre, cur) => pre + cur, 0) / this.length;
  }
}

const myArray = new MyArray(1, 1, 2, 3);
console.log(myArray.uniq() instanceof MyArray); // false
console.log(myArray.uniq() instanceof Array); // true

// 메서드 체이닝
// uniq 메서드는 Array 인스턴스를 반환하므로 average 메서드를 호출할 수 없다.
console.log(myArray.uniq().average());
// TypeError: myArray.uniq(...).average is not a function
```