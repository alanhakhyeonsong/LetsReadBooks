# 17장. 생성자 함수에 의한 객체 생성
객체는 객체 리터럴 이외에도 다양한 방법으로 생성할 수 있다. 생성자 함수를 사용하여 객체를 생성하는 방식과 장단점을 살펴보자.

## Object 생성자 함수
`new` 연산자와 함께 `Object` 생성자 함수를 호출하면 빈 객체를 생성하여 반환한다. 빈 객체를 생성한 이후 프로퍼티 또는 메서드를 추가하여 객체를 완성할 수 있다.

```javascript
// 빈 객체의 생성
const person = new Object();

// 프로퍼티 추가
person.name = 'Ramos';
person.sayHello = function () {
  console.log(`Hi! My name is ${this.name}`);
};

console.log(person); // {name: "Ramos", sayHello: f}
person.sayHello(); // Hi! My name is Ramos
```

생성자 함수란 `new` 연산자와 함께 호출하여 객체(인스턴스)를 생성하는 함수를 말한다. 생성자 함수에 의해 생성된 객체를 인스턴스라 한다.

JavaScript는 `Object` 생성자 함수 이외에도 `String`, `Number`, `Boolean`, `Function`, `Array`, `Date`, `RegExp`, `Promise` 등의 빌트인 생성자 함수를 제공한다.

반드시 생성자 함수를 사용해 빈 객체를 생성해야 하는 것은 아니며, 객체 리터럴을 사용하여 객체를 생성하는 것이 더 간편하다. 특별한 이유가 없다면 `Object` 생성자 함수를 사용하는 것은 유용해보이진 않는다.

## 생성자 함수
### 객체 리터럴에 의핸 객체 생성 방식의 문제점
**객체 리터럴에 의한 객체 생성 방식은 직관적이고 간편하지만, 단 하나의 객체만 생성한다.** 따라서 동일한 프로퍼티를 갖는 객체를 여러 개 생성해야 하는 경우 매번 같은 프로퍼티를 기술해야 하기 때문에 비효율적이다.

```javascript
const circle1 = {
  radius: 5,
  getDiameter() {
    return 2 * this.radius;
  }
};

console.log(circle1.getDiameter()); // 10

const circle2 = {
  radius: 5,
  getDiameter() {
    return 2 * this.radius;
  }
};

console.log(circle2.getDiameter()); // 10
```

객체는 프로퍼티를 통해 객체 고유의 상태를 표현한다. 그리고 메서드를 통해 상태 데이터인 프로퍼티를 참조하고 조작하는 동작을 표현한다. 따라서 프로퍼티는 객체마다 프로퍼티 값이 다를 수 있지만 메서드는 내용이 동일한 경우가 일반적이다.

위 `circle1` 객체와 `circle2` 객체는 프로퍼티 구조가 동일하다. 객체 고유의 상태 데이터인 `radius` 프로퍼티의 값은 객체마다 다를 수 있지만 `getDiameter` 메서드는 완전히 동일하다.

**객체 리터럴에 의해 객체를 생성하는 경우 프로퍼티 구조가 동일함에도 불구하고 매번 같은 프로퍼티와 메서드를 기술해야 한다.** 만약 수십 개의 객체를 생성해야 한다면 문제가 크다.

### 생성자 함수에 의한 객체 생성 방식의 장점
생성자 함수에 의한 객체 생성 방식은 마치 객체(인스턴스)를 생성하기 위한 템플릿(클래스)처럼 생성자 함수를 사용하여 프로퍼티 구조가 동일한 객체 여러 개를 간편하게 생성할 수 있다.

```javascript
// 생성자 함수
function Circle(radius) {
  // 생성자 함수 내부의 this는 생성자 함수가 생성할 인스턴스를 가리킴
  this.radius = radius;
  this.getDiameter = function () {
    return 2 * radius;
  };
}

// 인스턴스의 생성
const circle1 = new Circle(5);
const circle2 = new Circle(10);

console.log(circle1.getDiameter()); // 10
console.log(circle2.getDiameter()); // 20
```

생성자 함수는 이름 그대로 객체(인스턴스)를 생성하는 함수다. 하지만 Java와 같은 클래스 기반 객체지향 언어의 생성자와는 다르게 그 형식이 정해져 있는 것이 아니라 일반 함수와 동일한 방법으로 생성자 함수를 정의하고 **`new` 연산자와 함께 호출하면 해당 함수는 생성자 함수로 동작한다.** 만약 `new` 연산자와 함께 생성자 함수를 호출하지 않으면 생성자 함수가 아니라 일반 함수로 동작한다.

```javascript
function Circle(radius) {
  this.radius = radius;
  this.getDiameter = function () {
    return 2 * radius;
  };
}

// new 연산자와 함께 호출하지 않으면 생성자 함수로 동작하지 않는다.
// 즉, 일반 함수로서 호출된다.
const circle3 = Circle(15);

// 일반 함수로서 호출된 Circle은 반환문이 없으므로 암묵적으로 undefined를 반환한다.
console.log(circle3); // undefined

// 일반 함수로서 호출된 Circle 내의 this는 전역 객체를 가리킨다.
console.log(radius); // 15
```

### 생성자 함수의 인스턴스 생성 과정
생성자 함수의 역할은 프로퍼티 구조가 동일한 인스턴스를 생성하기 위한 템플릿(클래스)으로서 동작하여 **인스턴스를 생성**하는 것과 **생성된 인스턴스를 초기화(인스턴스 프로퍼티 추가 및 초기값 할당)** 하는 것이다. 생성자 함수가 인스턴스를 생성하는 것은 필수이고, 생성된 인스턴스를 초기화하는 것은 옵션이다.

```javascript
// 생성자 함수
function Circle(radius) {
  // 인스턴스 초기화
  this.radius = radius;
  this.getDiameter = function () {
    return 2 * radius;
  };
}

// 인스턴스 생성
const circle1 = new Circle(5); // 반지름이 5인 Circle 객체를 생성
```

생성자 함수 내부의 코드를 살펴보면 `this`에 프로퍼티를 추가하고 필요에 따라 전달된 인수를 프로퍼티의 초기값으로 할당하여 인스턴스를 초기화한다. 하지만 인스턴스를 생성하고 반환하는 코드는 보이지 않는다.

**JavaScript 엔진은 암묵적인 처리를 통해 인스턴스를 생성하고 반환한다.** `new` 연산자와 함께 생성자 함수를 호출하면 JavaScript 엔진은 다음과 같은 과정을 거쳐 암묵적으로 인스턴스를 생성하고 인스턴스를 초기화한 후 암묵적으로 인스턴스를 반환한다.

1. 인스턴스 생성과 `this` 바인딩
2. 인스턴스 초기화
3. 인스턴스 반환

#### 인스턴스 생성과 `this` 바인딩
암묵적으로 빈 객체가 생성된다. 이 빈 객체가 바로 (아직 완성되진 않았지만) 생성자 함수가 생성한 인스턴스다. 그리고 암묵적으로 생성된 빈 객체, 즉 인스턴스는 `this`에 바인딩된다. **생성자 함수 내부의 `this`가 생성자 함수가 생성할 인스턴스를 가리키는 이유가 바로 이것이다.** **이 처리는 함수 몸체의 코드가 한 줄씩 실행되는 런타임 이전에 실행된다.**

---
📌 바인딩  
식별자와 값을 연결하는 과정을 의미한다. 예를 들어, 변수 선언은 변수 이름(식별자)과 확보된 메모리 공간의 주소를 바인딩하는 것이다. `this` 바인딩은 `this`와 `this`가 가리킬 객체를 바인딩하는 것이다.

---

```javascript
function Circle(radius) {
  // 1. 암묵적으로 인스턴스가 생성되고 this에 바인딩된다.
  console.log(this); // Circle {}

  this.radius = radius;
  this.getDiameter = function () {
    return 2 * radius;
  };
}
```

#### 인스턴스 초기화
**생성자 함수에 기술되어 있는 코드가 한 줄씩 실행되어 `this`에 바인딩되어 있는 인스턴스를 초기화한다.** 즉, `this`에 바인딩되어 있는 인스턴스에 프로퍼티나 메서드를 추가하고 생성자 함수가 인수로 전달받은 초기값을 인스턴스 프로퍼티에 할당하여 초기화하거나 고정값을 할당한다. 이 처리는 개발자가 기술한다.

```javascript
function Circle(radius) {
  // 1. 암묵적으로 인스턴스가 생성되고 this에 바인딩된다.

  // 2. this에 바인딩되어 있는 인스턴스를 초기화한다.
  this.radius = radius;
  this.getDiameter = function () {
    return 2 * radius;
  };
}
```

### 인스턴스 반환
생성자 함수 내부에서 모든 처리가 끝나면 완성된 인스턴스가 바인딩된 `this`를 암묵적으로 반환한다.

```javascript
function Circle(radius) {
  // 1. 암묵적으로 인스턴스가 생성되고 this에 바인딩된다.

  // 2. this에 바인딩되어 있는 인스턴스를 초기화한다.
  this.radius = radius;
  this.getDiameter = function () {
    return 2 * radius;
  };

  // 3. 완성된 인스턴스가 바인딩된 this가 암묵적으로 반환된다.
}

// 인스턴스 생성. Circle 생성자 함수는 암묵적으로 this를 반환한다.
const circle = new Circle(1);
console.log(circle); // Circle {radius: 1, getDiameter: f}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ae4b4569-5171-4759-803c-60d45393cd54)
\

만약 `this`가 아닌 다른 객체를 명시적으로 반환하면 `this`가 반환되지 못하고 `return` 문에 명시한 객체가 반환된다.

```javascript
function Circle(radius) {
  // 1. 암묵적으로 인스턴스가 생성되고 this에 바인딩된다.

  // 2. this에 바인딩되어 있는 인스턴스를 초기화한다.
  this.radius = radius;
  this.getDiameter = function () {
    return 2 * radius;
  };

  // 3. 암묵적으로 this를 반환한다.
  // 명시적으로 객체를 반환하면 암묵적인 this 반환이 무시된다.
  return {};
}

// 인스턴스 생성. Circle 생성자 함수는 명시적으로 반환한 객체를 반환한다.
const circle = new Circle(1);
console.log(circle); // {}
```

하지만 명시적으로 원시 값을 반환하면 원시 값 반환은 무시되고 암묵적으로 `this`가 반환된다.

```javascript
function Circle(radius) {
  // 1. 암묵적으로 인스턴스가 생성되고 this에 바인딩된다.

  // 2. this에 바인딩되어 있는 인스턴스를 초기화한다.
  this.radius = radius;
  this.getDiameter = function () {
    return 2 * radius;
  };

  // 3. 암묵적으로 this를 반환한다.
  // 명시적으로 원시 값을 반환하면 원시 값 반환은 무시되고 암묵적으로 this가 반환된다.
  return 100;
}

// 인스턴스 생성. Circle 생성자 함수는 명시적으로 반환한 객체를 반환한다.
const circle = new Circle(1);
console.log(circle); // Circle {radius: 1, getDiameter: f}
```

이처럼 생성자 함수 내부에서 명시적으로 `this`가 아닌 다른 값을 반환하는 것은 생성자 함수의 기본 동작을 훼손한다. 따라서 생성자 함수 내부에서 `return` 문을 반드시 생략해야 한다.

### 내부 메서드 [[Call]]과 [[Construct]]
함수 선언문 또는 함수 표현식으로 정의한 함수는 일반적인 함수로서 호출할 수 있는 것은 물론 생성자 함수로서 호출할 수 있다. 생성자 함수로서 호출한다는 것은 `new` 연산자와 함께 호출하여 객체를 생성하는 것을 의미한다.

함수는 객체이므로 일반 객체와 동일하게 동작할 수 있다. 함수 객체는 일반 객체가 가지고 있는 내부 슬롯과 내부 메서드를 모두 가지고 있기 때문이다.

```javascript
// 함수는 객체다.
function foo() {}

// 함수는 객체이므로 프로퍼티를 소유할 수 있다.
foo.prop = 10;

// 함수는 객체이므로 메서드를 소유할 수 있다.
foo.method = function () {
  console.log(this.prop);
}

foo.method(); // 10
```

함수는 객체이지만 일반 객체와는 다르다. **일반 객체는 호출할 수 없지만 함수는 호출할 수 있다.**

따라서 함수 객체는 일반 객체가 가지고 있는 내부 슬롯과 내부 메서드는 물론, 함수로서 동작하기 위해 함수 객체만을 위한 `[[Environment]]`, `[[FormalParameters]]` 등의 내부 슬롯과 `[[Call]]`, `[[Construct]]` 같은 내부 메서드를 추가로 가지고 있다.

```javascript
function foo() {}

// 일반적인 함수로서 호출: [[Call]]이 호출된다.
foo();

// 생성자 함수로서 호출: [[Construct]]가 호출된다.
new foo();
```

내부 메서드 `[[Call]]`을 갖는 함수 객체를 `callable`이라 하며, 내부 메서드 `[[Construct]]`를 갖는 함수 객체를 `constructor`, `[[Construct]]`를 갖지 않는 함수 객체를 `non-constructor`라 부른다.

- `callable`: 호출할 수 있는 객체, 즉 함수
- `constructor`: 생성자 함수로서 호출할 수 있는 함수
- `non-constructor`: 객체를 생성자 함수로서 호출할 수 없는 함수

호출할 수 없는 객체는 함수 객체가 아니므로 함수로서 기능하는 객체, 즉 함수 객체는 반드시 `callable`이어야 한다. 따라서 모든 함수 객체는 내부 메서드 `[[Call]]`을 갖고 있으므로 호출할 수 있다. 하지만 모든 함수 객체가 `[[Construct]]`를 갖는 것은 아니다. 다시 말해, 함수 객체는 `constructor`일 수도 있고 `non-constructor`일 수도 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/82577621-9685-47f3-bb48-eebd4342e64b)

### constructor와 non-constructor의 구분
JavaScript 엔진이 어떻게 `constructor`와 `non-constructor`를 구분하는지 살펴보자. JavaScript 엔진은 함수 정의를 평가하여 함수 객체를 생성할 때 다음과 같이 구분한다.

- `constructor`: 함수 선언문, 함수 표현식, 클래스(클래스도 함수임)
- `non-constructor`: 메서드(ES6 메서드 축약 표현), 화살표 함수

주의할 것은 ECMAScript 사양에서 메서드로 인정하는 범위가 일반적인 메서드보다 좁다는 것이다.

```javascript
// 일반 함수 정의: 함수 선언문, 함수 표현식
function foo() {}
const bar = function () {};
// 프로퍼티 x의 값으로 할당된 것은 일반 함수로 정의된 함수다. 이는 메서드로 인정하지 않는다.
const baz = {
  x: function() {}
};

// 일반 함수로 정의한 함수만이 constructor
new foo(); // foo {}
new bar(); // bar {}

new baz.x(); // x {}

// 화살표 함수 정의
const arrow = () => {};

new arrow(); // TypeError: arrow is not a constructor

// 메서드 정의: ES6의 메서드 축약 표현만 메서드로 인정한다.
const obj = {
  x() {}
};

new obj.x(); // TypeError: obj.x is not a constructor
```

함수를 프로퍼티 값으로 사용하면 일반적으로 메서드로 통칭한다. 하지만 ECMAScript 사양에서 메서드란 ES6의 메서드 축약 표현만을 의미한다. 다시 말해 함수가 어디에 할당되어 있는지에 따라 메서드인지를 판단하는 것이 아니라 함수 정의 방식에 따라 `constructor`와 `non-constructor`를 구분한다.

```javascript
function foo() {}

// 일반적인 함수로서 호출
// [[Call]]이 호출된다. 모든 함수 객체는 [[Call]]이 구현되어 있다.
foo();

// 생성자 함수로서 호출
// [[Construct]]가 호출된다. 이때 [[Construct]]를 갖지 않는다면 에러가 발생한다.
new foo();
```

주의할 것은 생성자 함수로서 호출될 것을 기대하고 정의하지 않은 일반 함수(`callable`이면서 `constructor`)에 `new` 연산자를 붙여 호출하면 생성자 함수처럼 동작할 수 있다는 것이다.

### new 연산자
일반함수와 생성자함수에 특별한 형식적 차이는 없다. `new` 연산자와 함께 함수를 호출하면 해당 함수는 생성자 함수로 동작한다. 다시 말해, 함수 객체의 내부 메서드 `[[Call]]`이 호출되는 것이 아니라 `[[Construct]]` 가 호출된다. 단, `new` 연산자와 함께 호출하는 함수는 `non-constructor` 가 아닌 `constructor` 이어야 한다.

```javascript
// 생성자 함수로서 정의하지 않은 일반 함수
function add(x, y) {
  return x + y;
}

// 생성자 함수로서 정의하지 않은 일반 함수를 new 연산자와 함께 호출
let inst = new add();

// 함수가 객체를 반환하지 않았으므로 반환문이 무시된다. 따라서 빈 객체가 생성되어 반환된다.
console.log(inst); // {}

// 객체를 반환하는 일반 함수
function createUser(name, role) {
  return { name, role };
}

// 일반 함수를 new 연산자와 함께 호출
inst = new createUser('Ramos', 'admin');
// 함수가 생성한 객체를 반환한다.
console.log(inst); // {name: "Ramos", role: "admin"}
```

반대로 `new` 연산자 없이 생성자 함수를 호출하면 일반 함수로 호출된다. 다시 말해, 함수 객체의 내부 메서드 `[[Construct]]`가 호출되는 것이 아니라 `[[Call]]`이 호출된다.

```javascript
function Circle(radius) {
  this.radius = radius;
  this.getDiameter = function () {
    return 2 * this.radius;
  };
}

// new 연산자 없이 생성자 함수 호출하면 일반 함수로서 호출
const circle = Circle(5);
console.log(circle); // undefined

// 일반 함수의 내부 this는 전역 객체 window를 가리킴
// radius 프로퍼티와 getDiameter 메서드는 전역 객체의 프로퍼티와 메서드가 된다.
console.log(radius); // 5
console.log(getDiameter()); // 10

circle.getDiameter();
// TypeError: Cannot read property 'getDiameter' of undefined
```

### new.target
생성자 함수가 `new` 연산자 없이 호출되는 것을 방지하기 위해 파스칼 케이스 컨벤션을 사용한다 하더라도 실수는 언제나 발생할 수 있다. 이러한 위험성을 회피하기 위해 ES6에선 `new.target`을 지원한다.

이는 `this`와 유사하게 `constructor`인 모든 함수 내부에서 암묵적인 지역 변수와 같이 사용되며 메타 프로퍼티라고 부른다. IE는 이를 지원하지 않는다는 점을 주의하자.

함수 내부에서 `new.target`을 사용하면 `new` 연산자와 함께 생성자 함수로서 호출되었는지 확인할 수 있다. **`new` 연산자와 함께 생성자 함수로서 호출되면 함수 내부의 `new.target`은 함수 자신을 가리킨다. `new` 연산자 없이 일반 함수로서 호출된 함수 내부의 `new.target`은 `undefined`다.**

따라서 함수 내부에서 `new.target`을 사용하여 `new` 연산자와 생성자 함수로서 호출했는지 확인하여 그렇지 않은 경우 `new` 연산자와 함께 재귀 호출을 통해 생성자 함수로서 호출할 수 있다.

```javascript
// 생성자 함수
function Circle(radius) {
  // 이 함수가 new 연산자와 함께 호출되지 않았다면 new.target은 undefined다.
  if (!new.target) {
    // new 연산자와 함께 생성자 함수를 재귀 호출하여 생성된 인스턴스를 반환한다.
    return new Circle(radius);
  }

  this.radius = radius;
  this.getDiameter = function () {
    return 2 * this.radius;
  };
}

// new 연산자 없이 생성자 함수를 호출하여도 new.target을 통해 생성자 함수로서 호출된다.
const circle = Circle(5);
console.log(circle.getDiameter());
```

`new` 연산자와 함께 생성자 함수에 의해 생성된 객체(인스턴스)는 프로토타입에 의해 생성자 함수와 연결된다. 이를 이용해 `new` 연산자와 함께 호출되었는지 확인할 수 있다.

참고로 대부분의 빌트인 생성자 함수는 `new` 연산자와 함께 호출되었는지를 확인한 후 적절한 값을 반환한다. `Object`와 `Function` 생성자 함수는 `new` 연산자 없이 호출해도 `new` 연산자와 함께 호출했을 때와 동일하게 동작한다.

하지만, `String`, `Number`, `Boolean` 생성자 함수는 `new` 연산자와 함께 호출했을 때 해당 객체를 생성하여 반환하지만 `new` 연산자 없이 호출하면 문자열, 숫자, 불리언 값을 반환한다. 이를 통해 데이터 타입을 변환하기도 한다.

```javascript
const str = String(123);
console.log(str, typeof str); // 123 string
```