# 10장. 객체 리터럴
## 객체란?

JavaScript는 객체 기반의 프로그래밍 언어이며, JavaScript를 구성하는 거의 모든 것이 객체다. 원시 값을 제외한 나머지 값(함수, 배열, 정규 표현식 등)은 모두 객체다.

- 원시 타입: 단 하나의 값만 나타낸다.
    - 변경 불가능한 값.
- 객체 타입: 다양한 타입의 값을 하나의 단위로 구성한 복합적인 자료구조
    - 변경 가능한 값.

객체는 0개 이상의 프로퍼티로 구성된 집합이며, 프로퍼티는 키와 값으로 구성된다.

<img width="207" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/cc31f193-c37a-46aa-b245-66fe42771a7f">

- JavaScript에서 사용할 수 있는 모든 값은 프로퍼티 값이 될 수 있다.
- JavaScript의 함수는 일급 객체이므로 값으로 취급할 수 있다.
    - 함수도 프로퍼티 값으로 사용할 수 있다.
    - 프로퍼티 값이 함수일 경우, 일반 함수와 구분하기 위해 메서드라 부른다.

<img width="229" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/61e679d5-cbe9-4de4-b847-04676fd02eb3">

- 프로퍼티: 객체의 상태를 나타내는 **값(data)**
- 메서드: 프로퍼티(상태 데이터)를 참조하고 조작할 수 있는 **동작(behavior)**

## 객체 리터럴에 의한 객체 생성

C++, Java 같은 클래스 기반 객체지향 언어는 클래스를 사전에 정의하고 필요한 시점에 `new` 연산자와 함께 생성자를 호출하여 인스턴스를 생성하는 방식으로 객체를 생성한다.

---

📌 인스턴스?

클래스에 의해 생성되어 메모리에 저장된 실체를 말한다. 객체지향 프로그래밍에서 객체는 클래스와 인스턴스를 포함한 개념이다. 클래스는 인스턴스를 생성하기 위한 템플릿의 역할을 한다. 인스턴스는 객체가 메모리에 저장되어 실제로 존재하는 것에 초점을 맞춘 용어다.

---

JavaScript는 프로토타입 기반 객체지향 언어로서 클래스 기반 객체지향 언어와는 달리 다양한 객체 생성 방법을 지원한다.

- 객체 리터럴
- `Object` 생성자 함수
- 생성자 함수
- `Object.create` 메서드
- 클래스(ES6)

// 리터럴: 사람이 이해할 수 있는 문자 또는 약속된 기호를 사용하여 값을 생성하는 표기법

객체 리터럴은 중괄호 내에 0개 이상의 프로퍼티를 정의한다. 변수를 할당하는 시점에 JavaScript 엔진은 객체 리터럴을 해석해 객체를 생성한다.

```jsx
var person = {
	name: 'Ramos'
	sayHello: function() {
		console.log(`Hello! My name is ${this.name}.`);
	}
};

console.log(typeof person); // object
console.log(person); // {name: "Ramos", sayHello: f}

// 증괄호 내에 프로퍼티를 정의하지 않으면 빈 객체가 생성된다.
var empty = {}; // 빈 객체
console.log(typeof empty); // object
```

- **객체 리터럴의 중괄호는 코드 블록을 의미하지 않는다는 데 주의하자.**
    - 코드 블록의 닫는 중괄호 뒤에는 세미 콜론을 붙이지 않는다.
    - 객체 리터럴은 값으로 평가되는 표현식이다.
    - 객체 리터럴의 닫는 중괄호 뒤에는 세미콜론을 붙인다.

객체 리터럴은 JavaScript의 유연함과 강력함을 대표하는 객체 생성 방식이다. 숫자나 문자열을 만드는 것과 유사하게 리터럴로 객체를 생성한다. 객체 리터럴에 프로퍼티를 포함시켜 객체를 생성함과 동시에 프로퍼티를 만들 수도 있고, 객체를 생성한 이후에 프로퍼티를 동적으로 추가할 수도 있다.

## 프로퍼티

**객체는 프로퍼티의 집합이며, 프로퍼티는 키와 값으로 구성된다.**

```jsx
var person = {
	// 프로퍼티 키는 name, 값은 'Ramos'
	name: 'Ramos',
	age: 20
};
```

- 프로퍼티를 나열할 때는 쉼표로 구분한다.

프로퍼티 키와 프로퍼티 값으로 사용할 수 있는 값은 다음과 같다.

- 프로퍼티 키: 빈 문자열을 포함하는 모든 문자열 또는 심벌 값
- 프로퍼티 값: 자바스크립트에서 사용할 수 있는 모든 값

식별자 네이밍 규칙을 따르지 않는 이름에는 반드시 따옴표를 사용해야 한다.

```jsx
var person = {
	firstName: 'Sergio', // 식별자 네이밍 규칙을 준수하는 프로퍼티 키
	'last-name': 'Ramos' // 식별자 네이밍 규칙을 준수하지 않는 프로퍼티 키
};

console.log(person); // {firstName: "Sergio", last-name: "Ramos"}

// JS 엔진은 따옴표를 생략한 last-name을 - 연산자가 있는 표현식으로 해석한다.
var person = {
	firstName: 'Sergio',
	last-name: 'Ramos' // SyntaxError: Unexpected token -
};
```

문자열 또는 문자열로 평가할 수 있는 표현식을 사용해 프로퍼티 키를 동적으로 생성할 수도 있다. 이 경우엔 프로퍼티 키로 사용할 표현식을 대괄호로 묶어야 한다.

```jsx
var obj = {};
var key = 'hello';

// ES5: 프로퍼티 키 동적 생성
obj[key] = 'world';
// ES6: 개선된 프로퍼티 이름
// var obj = { [key]: 'world' };

console.log(obj); // {hello: "world"}
```

빈 문자열을 프로퍼티 키로 사용해도 에러가 발생하지는 않는다. 하지만 키로서의 의미를 갖지 못하므로 권
장하지 않는다.

```jsx
var foo = {
	'': '' // 빈 문자열도 프로퍼티 키로 사용할 수 있다.
};

console.log(foo); // {"": ""}
```

프로퍼티 키에 문자열이나 심벌 값 이외의 값을 사용하면 암묵적 타입 변환을 통해 문자열이 된다.

숫자 리터럴을 키로 사용하면 따옴표는 붙지 않지만 내부적으로는 문자열로 변환된다.

```jsx
// 이미 존재하는 프로퍼티 키를 중복 선언하면
// 나중에 선언한 프로퍼티가 먼저 선언한 프로퍼티를 덮어쓴다.
// 에러가 발생하진 않는다.
var foo = {
	name: 'Maldini',
	name: 'Ramos'
};

console.log(foo); // {name: "Ramos"}
```

## 메서드

```jsx
var circle = {
	radius: 5, // 프로퍼티

	// 원의 지름
	getDiameter: function () { // 메서드
		return 2 * this.radius; // this는 circle을 가리킴
	}
};

console.log(circle.getDiameter()); // 10
```

## 프로퍼티 접근

- 마침표 프로퍼티 접근 연산자를 사용하는 마침표 표기법
- 대괄호 프로퍼티 접근 연산자를 사용하는 대괄호 표기법

```jsx
var person = {
		name: 'Ramos'
};

console.log(person.name); // Ramos

console.log(person['name']); // Ramos

console.log(person[name]); // ReferenceError: name is not defined

console.log(person.age); // undefined
```

프로퍼티 키가 식별자 네이밍 규칙을 준수하지 않는 이름이라면 반드시 대괄호 표기법을 사용해야 한다. 단, 프로퍼티 키가 숫자로 이뤄진 문자열인 경우 따옴표를 생략할 수 있다.

## 프로퍼티 값 갱신

이미 존재하는 프로퍼티에 값을 할당하면 프로퍼티 값이 갱신된다.

```jsx
var person = {
		name: 'Ramos'
};

person.name = 'Kim';

console.log(person); // {name: "Kim"}
```

## 프로퍼티 동적 생성

존재하지 않는 프로퍼티에 값을 할당하면 프로퍼티가 동적으로 생성되어 추가되고 프로퍼티 값이 할당된다.

```jsx
var person = {
		name: 'Ramos'
};

// person 객체에는 age 프로퍼티가 존재하지 않는다.
// 따라서 person 객체에는 age 프로퍼티가 동적으로 생성되고 같이 할당된다.
person.age = 20;

console.log(person); // {name: "Ramos", age: 20}
```

## 프로퍼티 삭제

`delete` 연산자는 객체의 프로퍼티를 삭제한다. 이때 피연산자는 프로퍼티 값에 접근할 수 있는 표현식이어야 한다. 존재하지 않는 프로퍼티를 삭제하면 아무런 에러 없이 무시된다.

```jsx
var person = {
		name: 'Ramos'
};

// 프로퍼티 동적 생성
person.age = 20;

// 존재하는 프로퍼티는 삭제 가능
delete person.age;

// 존재하지 않는 프로퍼티라서 삭제할 수 없지만 에러가 발생하지 않는다.
delete person.address;

console.log(person); // {name: "Ramos"}
```

## ES6에서 추가된 객체 리터럴의 확장 기능

### 프로퍼티 축약 표현

```jsx
// ES5
var x = 1, y = 2;

var obj = {
  x: x,
	y: y
};

console.log(obj); // {x: 1, y: 2}

// ES6
let x = 1, y = 2;

// 프로퍼티 축약 표현
const obj = { x, y };

console.log(obj); // {x: 1, y: 2}
```

### 계산된 프로퍼티 이름

문자열 또는 문자열로 타입 변환할 수 있는 값으로 평가되는 표현식을 사용해 프로퍼티 키를 동적으로 생성할 수도 있다. **단, 프로퍼티 키로 사용할 표현식을 대괄호로 묶어야 한다.** 이를 계산된 프로퍼티 이름이라 한다.

```jsx
// ES5
const prefix = 'prop';
let i = 0;

var obj = {};

// 계산된 프로퍼티 이름으로 프로퍼티 키 동적 생성
obj[prefix + '-' + ++i] = i;
obj[prefix + '-' + ++i] = i;
obj[prefix + '-' + ++i] = i;

console.log(obj); // {prop-1: 1, prop-2: 2, prop-3: 3}

// ES6
const prefix = 'prop';
let i = 0;

// 객체 리터럴 내부에서 계산된 프로퍼티 이름으로 프로퍼티 키를 동적 생성
const obj = {
	[`${prefix}-${++i}`]: i,
	[`${prefix}-${++i}`]: i,
	[`${prefix}-${++i}`]: i
};

console.log(obj); // {prop-1: 1, prop-2: 2, prop-3: 3}
```

### 메서드 축약 표현
```javascript
// ES5
var obj = {
	name: 'Ramos',
	sayHi: function() {
		console.log('Hi! ' + this.name);
	}
};

obj.sayHi(); // Hi! Ramos

// ES6
const obj = {
	name: 'Ramos',
	// 메서드 축약 표현
	sayHi: {
		console.log('Hi! ' + this.name);
	}
};

obj.sayHi(); // Hi! Ramos
```