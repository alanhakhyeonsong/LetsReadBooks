# 26장. ES6 함수의 추가 기능
## 함수의 구분
ES6 이전까지 JavaScript의 함수는 별다른 구분 없이 다양한 목적으로 사용되었다.

- 일반적인 함수로서 호출
- `new` 연산자와 함께 호출하여 인스턴스를 생성할 수 있는 생성자 함수로서 호출
- 객체에 바인딩되어 메서드로서 호출

```javascript
var foo = function () {
  return 1;
};

// 일반적인 함수로서 호출
foo(); // 1

// 생성자 함수로서 호출
new foo(); // foo {}

// 메서드로서 호출
var obj = { foo: foo };
obj.foo(); // 1
```

ES6 이전의 함수는 사용 목적에 따라 명확히 구분되지 않는다. 즉, **ES6 이전의 모든 함수는 일반 함수로서 호출할 수 있는 것은 물론 생성자 함수로서 호출할 수 있다.** 다시 말해, `callable`이면서 `constructor`다.

```javascript
var foo = function () {};

foo(); // undefined
new foo(); // foo {}
```

주의할 것은 ES6 이전에 일반적으로 메서드라고 부르던 객체에 바인딩된 함수도 `callable`이면서 `constructor`라는 것이다. 따라서 객체에 바인딩된 함수도 일반 함수로서 호출할 수 있는 것은 물론 생성자 함수로서 호출할 수도 있다.

```javascript
// 프로퍼티 f에 바인딩된 함수는 callable이면서 constructor
var obj = {
  x: 10,
  f: function () { return this.x; }
};

// 프로퍼티 f에 바인딩된 함수를 메서드로서 호출
console.log(obj.f()); // 10

// 프로퍼티 f에 바인딩된 함수를 일반 함수로서 호출
var bar = obj.f;
console.log(bar()); // undefined

// 프로퍼티 f에 바인딩된 함수를 생성자 함수로서 호출
console.log(new obj.f()); // f {}
```

위 예제와 같이 **객체에 바인딩된 함수를 생성자 함수로서 호출하는 경우가 흔치는 않지만 문법상 가능하다는 것은 문제가 있고 성능 면에도 문제가 있다.** 객체에 바인딩된 함수가 `constructor`라는 것은 객체에 바인딩된 함수가 `prototype` 프로퍼티를 가지며, 프로토타입 객체도 생성한다는 것을 의미하기 때문이다.

함수에 전달되어 보조 함수의 역할을 하는 콜백 함수도 마찬가지다.

```javascript
// 콜백 함수를 사용하는 고차 함수 map.
[1, 2, 3].map(function (item) {
  return item * 2;
}); // [2, 4, 6]
```

**이러한 문제를 해결하기 위해 ES6에서는 함수를 사용 목적에 따라 세 가지 종류로 명확히 구분했다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/344d20af-c7be-4fa5-a78f-d976e7853fbd)

일반 함수는 함수 선언문이나 함수 표현식으로 정의한 함수를 말하며, ES6 이전의 함수와 차이가 없다. 하지만 ES6의 메서드와 화살표 함수는 ES6 이전의 함수와 명확한 차이가 있다.

## 메서드
ES6 이전 사양에서는 메서드에 대한 명확한 정의가 없었다. 일반적으로 메서드는 객체에 바인딩된 함수를 일컫는 의미로 사용되었다.

**ES6 사양에서 메서드는 메서드 축약 표현으로 정의된 함수만을 의미한다.**

```javascript
const obj = {
  x: 1,
  // foo는 메서드
  foo() { return this.x; },
  // bar에 바인딩된 함수는 메서드가 아닌 일반 함수
  bar: function() { return this.x; }
};

console.log(obj.foo()); // 1
console.log(obj.bar()); // 1
```

**ES6 사양에서 정의한 메서드는 인스턴스를 생성할 수 없는 `non-constructor`다.** 따라서 ES6 메서드는 생성자 함수로서 호출할 수 없다.

```javascript
new obj.foo(); // TypeError: obj.foo is not a constructor
new obj.bar(); // bar {}

// obj.foo는 prototype 프로퍼티가 없다.
obj.foo.hasOwnProperty('prototype'); // false

// obj.bar는 prototype 프로퍼티가 있다.
obj.bar.hasOwnProperty('prototype'); // true
```

참고로 표준 빌트인 객체가 제공하는 프로토타입 메서드와 정적 메서드는 모두 `non-constructor`다.

```javascript
String.prototype.toUpperCase.prototype; // undefined
String.fromCharCode.prototype; // undefined
```

**ES6 메서드는 자신을 바인딩한 객체를 가리키는 내부 슬롯 `[[HomeObject]]`를 갖는다.** `super` 참조는 내부 슬롯 `[[HomeObject]]`를 사용하여 수퍼클래스의 메서드를 참조하므로 내부 슬롯 `[[HomeObject]]`를 갖는 ES6에서는 `super` 키워드를 사용할 수 있다.

```javascript
const base = {
  name: 'Ramos',
  sayHi() {
    return `Hi! ${this.name}`;
  }
};

const derived = {
  __proto__: base,
  sayHi() {
    return `${super.sayHi()}. How are you doing?`;
  }
};

console.log(derived.sayHi()); // Hi! Ramos. How are you doing?
```

ES6 메서드가 아닌 함수는 `super` 키워드를 사용할 수 없다. 이는 내부 슬롯 `[[HomeObject]]`를 갖지 않기 때문이다.

```javascript
const derived = {
  __proto__: base,
  // sayHi는 ES6 메서드가 아님
  // [[HomeObject]]를 갖지 않으므로 super 키워드를 사용할 수 없다.
  sayHi: function () {
    // SyntaxError: 'super' keyword unexpected here
    return `${super.sayHi()}. How are you doing?`;
  }
};
```

이처럼 ES6 메서드는 본연의 기능(`super`)을 추가하지 않고 의미적으로 맞지 않는 기능(`constructor`)은 제거했다. 따라서 메서드를 정의할 때 프로퍼티 값으로 익명 함수 표현식을 할당하는 ES6 이전의 방식은 사용하지 않는 것이 좋다.

## 화살표 함수
- 화살표 함수는 `function` 키워드 대신 화살표를 사용하여 기존의 함수 정의 방식보다 간략하게 함수를 정의할 수있다.
- 화살표 함수는 표현만 간략한 것이 아니라 내부 동작도 기존의 함수보다 간략하다.
- **화살표 함수는 콜백 함수 내부에서 `this`가 전역 객체를 가리키는 문제를 해결하기 위한 대안으로 유용하다.

### 화살표 함수 정의

```javascript
// 화살표 함수는 함수 선언문으로 정의할 수 없고 함수 표현식으로 정의해야 한다.
// 호출 방식은 기존 함수와 동일하다.
const multiply = (x, y) => x * y;
multiply(2, 3); // 6

// 매개변수가 여러 개인 경우 소괄호 () 안에 매개변수를 선언한다.
const arrow = (x, y) => { ... };

// 매개변수가 한 개인 경우 소괄호 ()를 생략 가능
const arrow = x => { ... };

// 매개변수가 없는 경우 소괄호 ()를 생략 불가
const arrow = () => { ... };

// 함수 몸체가 하나의 문으로 구성된다면 함수 몸체를 감싸는 중괄호 {}를 생략할 수있다.
// 함수 몸체 내부의 문이 값으로 평가될 수 있는 표현식인 문이라면 암묵적으로 반환된다.
const power = x => x ** 2;
power(2);

// 함수 몸체를 감싸는 중괄호 {}를 생략한 경우 함수 몸체 내부의 문이 표현식이 아닌 문이라면 에러가 발생한다.
// 표현식이 아닌 문은 반환할 수 없기 때문이다.
const arrow = () => const x = 1; // SyntaxError: Unexpected token 'const'

// 위 표현은 다음과 같이 해석된다.
const arrow = () => { return const x = 1; };


// 함수 몸체가 하나의 문으로 구성된다 해도 몸체의 문이 표현식이 아닌 문이라면 중괄호를 생략할 수없다.
const arrow = () => { const x = 1; };

// 객체 리터럴을 반환하는 경우 객체 리터럴을 소괄호 ()로 감싸 주어야 한다.
const create = (id, content) => ({ id, content });
create(1, 'JavaScript'); // {id: 1, content: "JavaScript"}

// 위 표현은 다음과 같이 해석된다.
const create = (id, content) => { return { id, content }; };


// 객체 리터럴을 소괄호 ()로 감싸지 않으면 객체 리터럴의 중괄호 {}를 함수 몸체를 감싸는 중괄호 {}로 잘못 해석한다.
// { id, content }를 함수 몸체 내의 쉼표 연산자문으로 해석한다.
const create = (id, content) => { id, content };
create(1, "JavaScript"); // undefined

// 함수 몸체가 여러 개의 문으로 구성된다면 함수 몸체를 감싸는 중괄호 {}를 생략할 수 없다.
// 이때 반환값이 있다면 명시적으로 반환해야 한다.
const sum = (a, b) => {
  const result = a + b;
  return result;
};

// 화살표 함수도 즉시 실행 함수로 사용할 수 있다.
const person = (name => ({
  sayHi() { return `Hi! My name is ${name}.`; }
}))('Ramos');

console.log(person.sayHi()); // Hi! My name is Ramos.
```

화살표 함수도 일급 객체이므로 `Array.prototype.map`, `Array.prototype.filter`, `Array.prototype.reduce` 같은 고차 함수에 인수로 전달할 수 있다. 이 경우 일반적인 함수 표현식보다 표현이 간결하고 가독성이 좋다.

```javascript
// ES5
[1, 2, 3].map(function (v) {
  return v * 2;
});

// ES6
[1, 2, 3].map(v => v * 2); // [2, 4, 6]
```

### 화살표 함수와 일반 함수의 차이
**1. 화살표 함수는 인스턴스를 생성할 수 없는 `non-constructor`다.**

```javascript
const Foo = () => {};
// 화살표 함수는 생성자 함수로서 호출할 수 없다.
new Foo(); // TypeError: Foo is not a constructor
```

화살표 함수는 인스턴스를 생성할 수 없으므로 `prototype` 프로퍼티가 없고 프로토타입도 생성하지 않는다.

```javascript
const Foo = () => {};
// 화살표 함수는 prototype 프로퍼티가 없다.
Foo.hasOwnProperty('prototype'); // false
```

**2. 중복된 매개변수 이름을 선언할 수 없다.**

일반 함수에선 에러가 발생하지 않지만, strict mode에선 발생한다. 화살표 함수에서도 중복된 매개변수 이름을 선언하면 에러가 발생한다.

```javascript
const arrow = (a, a) => a + a;
// SyntaxError: Duplicate parameter name not allowed in this context
```

**3. 화살표 함수는 함수 자체의 `this`, `arguments`, `super`, `new.target` 바인딩을 갖지 않는다.**

화살표 함수 내부에서 `this`, `arguments`, `super`, `new.target`을 참조하면 스코프 체인을 통해 상위 스코프의 `this`, `arguments`, `super`, `new.target`을 참조한다.

### this
**화살표 함수가 일반 함수와 구별되는 가장 큰 특징은 `this`다.** 그리고 화살표 함수는 다른 함수의 인수로 전달되어 콜백 함수로 사용되는 경우가 많다.

화살표 함수의 `this`는 일반 함수의 `this`와 다르게 동작한다. 이는 "콜백 함수 내부의 `this` 문제", 즉 콜백 함수 내부의 `this`가 외부 함수의 `this`와 다르기 때문에 발생하는 문제를 해결하기 위해 의도적으로 설계된 것이다.

- `this` 바인딩은 함수의 호출 방식, 즉 함수가 어떻게 호출되었는지에 따라 동적으로 결정된다.
- 함수를 정의할 때 `this`에 바인딩할 객체가 정적으로 결정되는 것이 아니고, **함수를 호출할 때 함수가 어떻게 호출되었는지에 따라 `this`에 바인딩할 객체가 동적으로 결정된다.**

주의할 것은 일반 함수로서 호출되는 콜백 함수의 경우다. 고차 함수의 인수로 전달되어 고차 함수 내부에서 호출되는 콜백 함수도 중첩 함수라고 할 수 있다.

```javascript
class Prefixer {
  constructor(prefix) {
    this.prefix = prefix;
  }

  // add 메서드는 인수로 전달된 배열 arr을 순회하며 배열의 모든 요소에 prefix를 추가한다.
  // (1)
  add(arr) {
    return arr.map(function (item) {
      return this.prefix + item; // (2)
      // TypeError: Cannot read property 'prefix' of undefined
    });
  }
}

const prefixer = new Prefixer('-webkit-');
console.log(prefixer.add(['transition', 'user-select']));
```

프로토타입 메서드 내부인 (1)에서 `this`는 메서드를 호출한 객체를 가리킨다. 그런데 `Array.prototype.map`의 인수로 전달한 콜백 함수의 내부인 (2)에서 `this`는 `undefined`를 가리킨다. 이는 `Array.prototype.map` 메서드가 콜백 함수를 일반 함수로서 호출하기 때문이다.

// ... (정리중)

### super


### arguments
화살표 함수는 함수 자체의 `arguments` 바인딩을 갖지 않는다. 따라서 화살표 함수 내부에서 `arguments`를 참조하면 `this`와 마찬가지로 상위 스코프의 `arguments`를 참조한다.

```javascript
(function () {
  // 화살표 함수 foo의 arguments는 상위 스코프인 즉시 실행 함수의 arguments를 가리킨다.
  const foo = () => console.log(arguments); // [Arguments] { '0': 1, '1': 2 }
  foo(3, 4);
}(1, 2));

// 화살표 함수 foo의 arguments는 상위 스코프인 전역 arguments를 가리킨다.
// 하지만 전역에는 arguments가 존재하지 않는다. arguments 객체는 함수 내부에서만 유효하다.
const foo = () => console.log(arguments);
foo(1, 2); // ReferenceError: arguments is not defined
```

화살표 함수로 가변 인자 함수를 구현해야 할 때는 반드시 Rest 파라미터를 사용해야 한다.

## Rest 파라미터
## 매개변수 기본값
함수를 호출할 때 매개변수의 개수만큼 인수를 전달하는 것이 바람직하지만 그렇지 않은 경우에도 에러가 발생하지 않는다. 이는 JavaScript 엔진이 매개변수의 개수와 인수의 개수를 체그하지 않기 때문이다.

인수가 전달되지 않은 매개변수의 값은 `undefined`다. 이를 방치하면 의도치 않은 결과가 나올 수 있다.

```javascript
function sum(x, y) {
  return x + y;
}

console.log(sum(1)); // NaN
```

따라서 다음 예제와 같이 매개변수에 인수가 전달되었는지 확인하여 인수가 전달되지 않은 경우 매개변수에 기본값을 할당할 필요가 있다. 즉, 방어 코드가 필요하다.

```javascript
function sum(x, y) {
  x = x || 0;
  x = y || 0;

  return x + y;
}

console.log(sum(1, 2)); // 3
console.log(sum(1)); // 1

// ES6에서 도입된 매개변수 기본값을 사용하면 간소화 할 수 있다.
function sum(x = 0, y = 0) {
  return x + y;
}

console.log(sum(1, 2)); // 3
console.log(sum(1)); // 1
```

매개변수 기본값은 매개변수에서 인수를 전달하지 않은 경우와 `undefined`를 전달한 경우에만 유효하다.

```javascript
function logName(name = 'Ramos') {
  console.log(name);
}

logName(); // Ramos
logName(undefined); // Ramos
logName(null); // null
```

```javascript
function foo(...rest = []) {
  console.log(rest);
}
// SyntaxError: Rest parameter may not have a default initializer
```

매개변수 기본값은 함수 정의 시 선언한 매개변수 개수를 나타내는 함수 객체의 `length` 프로퍼티와 `arguments` 객체에 아무런 영향을 주지 않는다.

```javascript
function sum(x, y = 0) {
  console.log(arguments);
}

console.log(sum.length); // 1

sum(1); // Arguments { '0': 1 }
sum(1, 2); // Arguments { '0': 1, '1': 2 }
```