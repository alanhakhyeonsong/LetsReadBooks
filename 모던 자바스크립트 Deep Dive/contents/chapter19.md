# 19장. 프로토타입
JavaScript는 명령형, 함수형, 프로토타입 기반 객체지향 프로그래밍을 지원하는 멀티 패러다임 프로그래밍 언어다.

C++나 Java 같은 클래스 기반 객체지향 프로그래밍 언어의 특징인 클래스와 상속, 캡슐화를 위한 키워드인 `public`, `private`, `protected` 등이 없어서 JavaScript는 객체지향 언어가 아니라고 오해하는 경우도 있다. 하지만 JavaScript는 클래스 기반 객체지향 프로그래밍 언어보다 효율적이며 더 강력한 객제지향 프로그래밍 능력을 지니고 있는 프로토타입 기반의 객체지향 프로그래밍 언어다.

---
📌 클래스(class)  
ES6에서 클래스가 도입되었다. 하지만 ES6의 클래스가 기존의 프로토타입 기반 객체지향 모델을 폐지하고 새로운 객체지향 모델을 제공하는 것은 아니다. 사실 클래스도 함수이며, 기존 프로토타입 기반 패턴의 문법적 설탕이라 볼 수 있다.

클래스와 생성자 함수는 모두 프로토타입 기반의 인스턴스를 생성하지만 정확히 동일하게 동작하진 않는다. 클래스는 생성자 함수보다 엄격하며 클래스는 생성자 함수에서는 제공하지 않는 기능도 제공한다.

따라서 클래스를 프로토타입 기반 객체 생성 패턴의 단순한 문법적 설탕으로 보기보단 새로운 객체 생성 메커니즘으로 보는 것이 좀 더 합당하다 할 수 있다.

---

JavaScript는 객체 기반의 프로그래밍 언어이며 **JavaScript를 이루고 있는 거의 "모든 것"이 객체다.** 원시 타입의 값들을 제외한 나머지 값들은 모두 객체다.

## 상속과 프로토타입
상속은 객체지향 프로그래밍의 핵심 개념으로, 어떤 객체의 프로퍼티 또는 메서드를 다른 객체가 상속받아 그대로 사용할 수 있는 것을 말한다.

JavaScript는 프로토타입을 기반으로 상속을 구현하여 불필요한 중복을 제거한다. 중복을 제거하는 방법은 기존의 코드를 적극적으로 재사용하는 것이다. 코드 재사용은 개발 비용을 현저히 줄일 수 있는 잠재력이 있으므로 매우 중요하다.

```javascript
// 생성자 함수
function Circle(radius) {
  this.radius = radius;
  this.getArea = function () {
    return Math.PI * this.radius ** 2;
  };
}

// 반지름이 1인 인스턴스 생성
const circle1 = new Circle(1);
// 반지름이 2인 인스턴스 생성
const circle2 = new Circle(2);

// Circle 생성자 함수는 인스턴스를 생성할 때마다 동일한 동작을 하는
// getArea 메서드를 중복 생성하고 모든 인스턴스가 중복 소유한다.
// getArea 메서드는 하나만 생성하여 모든 인스턴스가 공유해서 사용하는 것이 바람직하다.
console.log(circle1.getArea === circle2.getArea); // false

console.log(circle1.getArea()); // 3.141592...
console.log(circle2.getArea()); // 12.566370...
```

생성자 함수는 동일한 프로퍼티(메서드 포함) 구조를 갖는 객체를 여러 개 생성할 때 유용하다. 하지만 위 예제는 문제가 있다.

`Circle` 생성자 함수가 생성하는 모든 객체(인스턴스)는 `radius` 프로퍼티와 `getArea` 메서드를 갖는다. `radius` 프로퍼티 값은 일반적으로 인스턴스마다 다르다. 하지만 `getArea` 메서드는 모든 인스턴스가 동일한 내용의 메서드를 사용하므로 단 하나만 생성하여 모든 인스턴스가 공유해서 사용하는 것이 바람직하다. **그런데 `Circle` 생성자 함수는 인스턴스를 생성할 때마다 `getArea` 메서드를 중복 생성하고 모든 인스턴스가 중복 소유한다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e1ee662d-219d-4d0c-a590-244c61d318c6)

- 동일한 생성자 함수에 의해 생성된 모든 인스턴스가 동일한 메서드를 중복 소유하는 것은 메모리를 불필요하게 낭비한다.
- 인스턴스를 생성할 대마다 메서드를 생성하므로 퍼포먼스에도 악영향을 준다.

상속을 통해 불필요한 중복을 제거해보자. **JavaScript는 프로토타입을 기반으로 상속을 구현한다.**

```javascript
// 생성자 함수
function Circle(radius) {
  this.radius = radius;
}

// Circle 생성자 함수가 생성한 모든 인스턴스가 getArea 메서드를
// 공유해서 사용할 수 있도록 프로토타입에 추가한다.
// 프로토타입은 Circle 생성자 함수의 prototype 프로퍼티에 바인딩되어 있다.
Circle.prototype.getArea = function () {
  return Math.PI * this.radius ** 2;
};

// 인스턴스 생성
const circle1 = new Circle(1);
const circle2 = new Circle(2);

// Circle 생성자 함수가 생성한 모든 인스턴스는 부모 객체의 역할을 하는
// 프로토타입 Circle.prototype으로부터 getArea 메서드를 상속받는다.
// 즉, Circle 생성자 함수가 생성하는 모든 인스턴스는 하나의 getArea 메서드를 공유한다.
console.log(circle1.getArea === circle2.getArea); // true

console.log(circle1.getArea()); // 3.141592...
console.log(circle2.getArea()); // 12.566370...
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/497bef1f-f83f-45cf-938c-7d22fb142d5b)

`Circle` 생성자 함수가 생성한 모든 인스턴스는 자신의 프로토타입, 즉 상위(부모) 객체 역할을 하는 `Circle.prototype`의 모든 프로퍼티와 메서드를 상속받는다.

`getArea` 메서드는 단 하나만 생성되어 프로토타입인 `Circle.prototype`의 메서드로 할당되어 있다. 따라서 생성자 함수가 생성하는 모든 인스턴스는 `getArea` 메서드를 상속받아 사용할 수 있다. 즉, 자신의 상태를 나타내는 `radius` 프로퍼티만 개별적으로 소유하고 내용이 동일한 메서드는 상속을 통해 공유하여 사용하는 것이다.

## 프로토타입 객체
**프로토타입 객체란 객체지향 프로그래밍의 근간을 이루는 객체 간 상속을 구현하기 위해 사용된다.** 프로토타입은 어떤 객체의 상위(부모) 객체의 역할을 하는 객체로서 다른 객체에 공유 프로퍼티(메서드 포함)를 제공한다. 프로토타입을 상속받은 하위(자식) 객체는 상위 객체의 프로퍼티를 자신의 프로퍼티처럼 자유롭게 사용할 수 있다.

모든 객체는 `[[Prototype]]` 내부 슬롯을 가지며, 이 내부 슬롯의 값은 프로토타입의 참조(`null`인 경우도 있다)다. 여기에 저장되는 프로토타입은 객체 생성 방식에 의해 결정된다. 즉, 객체가 생성될 때 객체 생성 방식에 따라 프로토타입이 결정되고 `[[Prototype]]`에 저장된다.

객체 리터럴에 의해 생성된 객체의 프로토타입은 `Object.prototype`이고 생성자 함수에 의해 생성된 객체의 프로토타입은 생성자 함수의 `prototype` 프로퍼티에 바인딩되어 있는 객체다.

**모든 객체는 하나의 프로토타입을 갖는다. 그리고 모든 프로토타입은 생성자 함수와 연결되어 있다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f90a4267-9ede-47e7-87d3-63f6eca32dda)

`[[Prototype]]` 내부 슬롯에는 직접 접근할 수 없지만, 위 그림처럼 `__proto__` 접근자 프로퍼티를 통해 자신의 프로토타입, 즉 자신의 `[[Prototype]]` 내부 슬롯이 가리키는 프로토타입에 간접적으로 접근할 수 있다. 그리고 프로토타입은 자신의 `constructor` 프로퍼티를 통해 생성자 함수에 접근할 수 있고, 생성자 함수는 자신의 `prototype` 프로퍼티를 통해 프로토타입에 접근할 수 있다.

### `__proto__` 접근자 프로퍼티
**모든 객체는 `__proto__` 접근자 프로퍼티를 통해 자신의 프로토타입, 즉 `[[Prototype]]` 내부 슬롯에 간접적으로 접근할 수 있다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/954edabe-de48-4baa-a277-33ae43f382ea)

빨간 박스로 표시한 것이 `person` 객체의 프로토타입인 `Object.prototype`이다. 이는 `__proto__` 접근자 프로퍼티를 통해 `person` 객체의 `[[Prototype]]` 내부 슬롯이 가리키는 객체인 `Object.prototype`에 접근한 결과를 크롬 브라우저가 콘솔에 표시한 것이다. 이처럼 모든 객체는 `__proto__` 접근자 프로퍼티를 통해 프로토타입을 가리키는 `[[Prototype]]` 내부 슬롯에 접근할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/df00a5d0-bc25-4365-a4e7-db149d076521)

`Object.prototype`의 접근자 프로퍼티인 `__proto__`는 getter/setter 함수라고 부르는 접근자 함수를 통해 `[[Prototype]]` 내부 슬롯의 값, 즉 프로토타입을 취득하거나 할당한다. `__proto__` 접근자 프로퍼티를 통해 프로토타입에 접근하면 내부적으로 `__proto__` 접근자 프로퍼티를 통해 새로운 프로토타입을 할당하면 `__proto__` 접근자 프로퍼티의 setter 함수인 `[[Set]]`이 호출된다.

```javascript
const obj = {};
const parent = { x: 1 };

// getter 함수인 get __proto__가 호출되어 obj 객체의 프로토타입을 취득
obj.__proto__;
// setter 함수인 set __proto__가 호출되어 obj 객체의 프로토타입을 교체 
obj.__proto__ = parent;

console.log(obj.x); // 1
```

`__proto__` 접근자 프로퍼티는 객체가 직접 소유하는 프로퍼티가 아니라 `Object.prototype`의 프로퍼티다. 모든 객체는 상속을 통해 `Object.prototype.__proto__` 접근자 프로퍼티를 사용할 수 있다.

```javascript
const person = { name: 'Ramos' };

// person 객체는 __proto__ 프로퍼티를 소유하지 않는다.
console.log(person.hasOwnProperty('__proto__')); // false

// __proto__ 프로퍼티는 모든 객체의 프로토타입 객체인 Object.prototype의 접근자 프로퍼티다.
console.log(Object.getOwnPropertyDescriptor(Object.prototype, '__proto__'));
// {get: f, set: f, enumerable: false, configurable: true}

// 모든 객체는 Object.prototype의 접근자 프로퍼티 __proto__를 상속받아 사용할 수 있다.
console.log({}.__proto__ === Object.prototype); // true
```

---
📌 `Object.prototype`  
모든 객체는 프로토타입의 계층 구조인 프로토타입 체인에 묶여 있다. JavaScript 엔진은 객체의 프로퍼티에 접근하려할 때 해당 객체에 접근하려는 프로퍼티가 없다면 `__proto__` 접근자 프로퍼티가 가리키는 참조를 따라 자신의 부모 역할을 하는 프로토타입의 프로퍼티를 순차적으로 검색한다. 프로토타입 체인의 종점, 즉 프로토타입 체인의 최상위 객체는 `Object.prototype`이며, 이 객채의 프로퍼티와 메서드는 모든 객체에 상속된다.

---

`[[Prototype]]` 내부 슬롯의 값, 즉 프로토타입에 접근하기 위해 접근자 프로퍼티를 사용하는 이유는 **상호 참조에 의해 프로토타입 체인이 생성되는 것을 방지하기 위해서다.**

```javascript
const parent = {};
const child = {};

// child의 프로토타입을 parent로 설정
child.__proto__ = parent;
// parent의 프로토타입을 child로 설정
parent.__proto__ = child; // TypeError: Cyclic __proto__ value
```

위와 같은 코드가 에러 없이 정상적으로 처리되면 서로가 자신의 프로토타입이 되는 비정상적인 프로토타입 체인이 만들어지기 때문에 `__proto__` 접근자 프로퍼티는 에러를 발생시킨다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/620255e9-19b4-4aba-912f-de248c1610f3)

**프로토타입 체인은 단방향 링크드 리스트로 구현되어야 한다.** 즉, 프로퍼티 검색 방향이 한쪽 방향으로만 흘러가야 한다. 하지만 위 그림과 같이 서로가 자신의 프로토타입이 되는 비정상적인 프로토타입 체인, **다시 말해 순환 참조하는 프로토타입 체인이 만들어지면 프로토타입 체인 종점이 존재하지 않기 때문에 프로토타입 체인에서 프로퍼티를 검색할 때 무한 루프에 빠진다.**

따라서 아무런 체크 없이 무조건적으로 프로토타입을 교체할 수 없도록 `__proto__` 접근자 프로퍼티를 통해 프로토타입에 접근하고 교체하도록 구현되어 있다.

`__proto__` 접근자 프로퍼티는 ES5까지 ECMAScript 사양에 포함되지 않은 비표준이었다. 하지만 일부 브라우저에서 `__proto__`를 지원하고 있었기 때문에 브라우저 호환성을 고려하여 ES6에서 `__proto__`를 표준으로 채택했다. 현재 대부분의 브라우저는 이를 지원한다.

하지만 코드 내에서 `__proto__` 접근자 프로퍼티를 직접 사용하는 것은 권장하지 않는다. 모든 객체가 이 접근자 프로퍼티를 사용할 수 있는 것은 아니기 때문이다.

```javascript
// obj는 프로토타입 체인의 종점이다. 따라서 Object.__proto__를 상속받을 수 없다.
const obj = Object.create(null);

// obj는 Object.__proto__를 상속받을 수 없다.
console.log(obj.__proto__); // undefined

// 따라서 __proto__ 보다 Object.getPrototypeOf 메서드를 사용하는 편이 좋다.
console.log(Object.getPrototypeOf(obj)); // null
```

따라서 `__proto__` 접근자 프로퍼티 대신 프로토타입이 참조를 취득하고 싶은 경우엔 `Object.getPrototypeOf` 메서드를 사용하고, 프로토타입을 교체하고 싶은 경우에는 `Object.setPrototypeOf` 메서드를 사용할 것을 권장한다.

### 함수 객체의 prototype 프로퍼티
**함수 객체만이 소유하는 `prototype` 프로퍼티는 생성자 함수가 생성할 인스턴스의 프로토타입을 가리킨다.**

```javascript
// 함수 객체는 prototype 프로퍼티를 소유한다.
(function () {}).hasOwnProperty('prototype'); // true

// 일반 객체는 prototype 프로퍼티를 소유하지 않는다.
({}).hasOwnProperty('prototype'); // false
```

`prototype` 프로퍼티는 생성자 함수가 생성할 객체(인스턴스)의 프로토타입을 가리킨다. 따라서 생성자 함수로서 호출할 수 없는 함수, 즉 `non-constructor`인 화살표 함수와 ES6 메서드 축약 표현으로 정의한 메서드는 `prototype` 프로퍼티를 소유하지 않으며 프로토타입도 생성되지 않는다.

```javascript
// 화살표 함수는 non-constructor다.
const Person = name => {
  this.name = name;
}; 

// non-constructor는 prototype 프로퍼티를 소유하지 않는다.
console.log(Person.hasOwnProperty('prototype')); // false

// non-constructor는 프로토타입을 생성하지 않는다.
console.log(Person.prototype); // undefined

// ES6의 메서드 축약 표현으로 정의한 메서드는 non-constructor다.
const obj = {
  foo() {}
};

// non-constructor는 prototype 프로퍼티를 소유하지 않는다.
console.log(obj.foo.hasOwnProperty('prototype')); // false

// non-constructor는 프로토타입을 생성하지 않는다.
console.log(obj.foo.prototype); // false
```

생성자 함수로 호출하기 위해 정의하지 않은 일반 함수도 `prototype` 프로퍼티를 소유하지만 객체를 생성하지 않는 일반 함수의 `prototype` 프로퍼티는 아무런 의미가 없다.

**모든 객체가 가지고 있는 `__proto__` 접근자 프로퍼티와 함수 객체만이 가지고 있는 `prototype` 프로퍼티는 결국 동일한 프로토타입을 가리킨다.** 하지만 이들 프로퍼티를 사용하는 주체가 다르다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/91cec4d0-d5d9-4ed7-b768-10757f875d37)

```javascript
// 생성자 함수
function Person(name) {
  this.name = name;
}

const me = new Person('Lee');

// 결국 Person.prototype과 me.__proto__는 결국 동일한 프로토타입을 가리킨다.
console.log(Person.prototype === me.__proto__); // true
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/5db33ee5-ba78-48f1-8cd5-27705bde2304)

### 프로토타입의 constructor 프로퍼티와 생성자 함수
모든 프로토타입은 `constructor` 프로퍼티를 갖는다. 이 `constructor` 프로퍼티는 `prototype` 프로퍼티로 자신을 참조하고 있는 생성자 함수를 가리킨다. **이 연결은 생성자 함수가 생성될 때, 즉 함수 객체가 생성될 때 이뤄진다.**

```javascript
// 생성자 함수
function Person(name) {
  this.name = name;
}

const me = new Person('Lee');

// me 객체의 생성자 함수는 Person이다.
console.log(me.constructor === Person); // true
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/8f6ed3ee-9161-4dd0-a698-9417e932f9b7)

`me` 객체엔 `constructor` 프로퍼티가 없지만 `me` 객체의 프로토타입인 `Person.prototype`에는 있다. 따라서 `me` 객체는 프로토타입인 `Person.prototype`의 `constructor` 프로퍼티를 상속받아 사용할 수 있다.

## 리터럴 표기법에 의해 생성된 객체의 생성자 함수와 프로토타입
```javascript
const obj = {};

// obj 객체의 생성자 함수는 Object 생성자 함수다.
console.log(obj.constructor === Object); // true
```

`obj` 객체는 객체 리터럴에 의해 생성된 객체다. 하지만 `obj` 객체는 `Object` 생성자 함수와 `constructor` 프로퍼티로 연결되어 있다. ECMAScript 사양을 보면 다음과 같다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f33746eb-6ecf-43bb-b204-7102a2cda76d)

2에서 `Object` 생성자 함수에 인수를 전달하지 않거나 `undefined` 또는 `null`을 인수로 전달하면서 호출하면 내부적으론 추상 연산 `OrdinaryObjectCreate`를 호출하여 `Object.prototype`을 프로토타입으로 갖는 빈 객체를 생성한다.

```javascript
// Object 생성자 함수에 의한 객체 생성
// 인수가 전달되지 않았을 때 추상 연산 OrdinaryObjectCreate를 호출하여 빈 객체를 생성한다.
let obj = new Object();
console.log(obj); // {}

// new.target이 undefined나 Object가 아닌 경우
// 인스턴스 → Foo.prototype → Object.prototype 순으로 프로토타입 체인이 생성된다.
class Foo extends Object {}
new Foo(); // Foo {}

// 인수가 전달되는 경우에는 인수를 객체로 반환한다.
// Number 객체 생성
obj = new Object(123);
console.log(obj); // Number {123}

// String 객체 생성
obj = new Object('123');
console.log(obj); // String {"123"}
```

객체 리터럴이 평가될 때는 다음과 같이 추상 연산 `OrdinaryObjectCreate`를 호출하여 빈 객체를 생성하고 프로퍼티를 추가하도록 정의되어 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a28684ac-a510-4989-b932-c4d9c0851876)

이처럼 `Object` 생성자 함수 호출과 객체 리터럴 평가는 추상 연산 `OrdinaryObjectCreate`를 호출하여 빈 객체를 생성하는 점에서 동일하나 `new.target`의 확인이나 프로퍼티를 추가하는 처리 등 세부 내용은 다르다. **따라서 객체 리터럴에 의해 생성된 객체는 `Object` 생성자 함수가 생성한 객체가 아니다.**

함수 객체의 경우 차이가 더 명확하다. `Function` 생성자 함수를 호출하여 생성한 함수는 렉시컬 스코프를 만들지 않고 전역 함수인 것처럼 스코프를 생성하며 클로저도 만들지 않는다. 따라서 함수 선언문과 함수 표현식을 평가하여 함수 객체를 생성한 것은 `Function` 생성자 함수가 아니다. 하지만 `constructor` 프로퍼티를 통해 확인해보면 `foo` 함수의 생성자 함수는 `Function` 생성자 함수다.

```javascript
function foo() {}

console.log(foo.constructor === Function); // true
```

리터럴 표기법에 의해 생성된 객체도 상속을 위해 프로토타입이 필요하다. 따라서 리터럴 표기법에 의해 생성된 객체도 가상적인 생성자 함수를 갖는다. 프로토타입은 생성자 함수와 더불어 생성되며 `prototype`, `constructor` 프로퍼티에 의해 연결되어 있기 때문이다. 다시 말해, **프로토타입과 생성자 함수는 단독으로 존재할 수 없고 언제나 쌍으로 존재한다.**

리터럴 표기법에 의해 생성된 객체는 생성자 함수에 의해 생성된 객체는 아니다. 하지만 큰 틀에서 생각해보면 리터럴 표기법으로 생성한 객체도 생성자 함수로 생성한 객체와 본질적인 면에서 큰 차이는 없다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/7ece8230-153f-422a-803a-83b69e335bd6)

## 프로토타입의 생성 시점
객체는 리터럴 표기법 또는 생성자 함수에 의해 생성되므로 결국 모든 객체는 생성자 함수와 연결되어 있다.

**프로토타입은 생성자 함수가 생성되는 시점에 더불어 생성된다.**

### 사용자 정의 생성자 함수와 프로토타입 생성 시점
```javascript
// 함수 정의(constructor)가 평가되어 함수 객체를 생성하는 시점에 프로토타입도 더불어 생성된다.
console.log(Person.prototype); // {constructor: f}

// 생성자 함수
function Person(name) {
  this.name = name;
}
```

생성자 함수로서 호출할 수 없는 함수는 프로토타입이 생성되지 않는다.

```javascript
const Person = name => {
  this.name = name;
};

console.log(Person.prototype); // undefined
```

**함수 선언문은 런타임 이전에 JavaScript 엔진에 의해 먼저 실행된다. 따라서 함수 선언문으로 정의된 `Person` 생성자 함수는 어떤 코드보다 먼저 평가되어 함수 객체가 된다. 이때 프로토타입도 더불어 생성된다. 생성된 프로토타입은 `Person` 생성자 함수의 `prototype` 프로퍼티에 바인딩된다.**

생성된 프로토타입은 오직 `constructor` 프로퍼티만을 갖는 객체다. 프로토타입도 객체이고 모든 객체는 프로토타입을 가지므로 프로토타입도 자신의 프로토타입을 갖는다. 생성된 프로토타입의 프로토타입은 `Object.prototype`이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/28cf00ac-4219-4e8b-be12-a7f7dd7fce9f)

이처럼 빌트인 생성자 함수가 아닌 사용자 정의 생성자 함수는 자신이 평가되어 함수 객체로 생성되는 시점에 프로토타입도 더불어 생성되며, 생성된 프로토타입은 언제나 `Object.prototype`이다.

### 빌트인 생성자 함수와 프로토타입 생성 시점
`Object`, `String`, `Number`, `Function`, ... 등과 같은 **빌트인 생성자 함수도 일반 함수와 마찬가지로 빌트인 생성자 함수가 생성되는 시점에 프로토타입이 생성된다. 모든 빌트인 생성자 함수는 전역 객체가 생성되는 시점에 생성된다.** 생성된 프로토타입은 빌트인 생성자 함수의 `prototype` 프로퍼티에 바인딩된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a89a1699-7ebb-4ffd-aad1-4c3457128a1c)

이처럼 객체가 생성되기 이전에 생성자 함수와 프로토타입은 이미 객체화되어 존재한다. **이후 생성자 함수 또는 리터럴 표기법으로 객체를 생성하면 프로토타입은 생성된 객체의 `[[Prototype]]` 내부 슬롯에 할당된다.** 이로써 생성된 객체는 프로토타입을 상속받는다.

## 객체 생성 방식과 프로토타입의 결정
객체는 다음과 같이 다양한 생성 방법이 있다.

- 객체 리터럴
- `Object` 생성자 함수
- 생성자 함수
- `Object.create` 메서드
- 클래스(ES6)

각 방식마다 세부적인 객체 생성 방식의 차이는 있으나 추상 연산 `OrdinaryObjectCreate`에 의해 생성된다는 공통점이 있다.

추상 연산 `OrdinaryObjectCreate`는 필수적으로 자신이 생성할 객체의 프로토타입을 인수로 전달받는다. 그리고 자신이 생성할 객체에 추가할 프로퍼티 목록을 옵션으로 전달할 수 있다. 추상 연산 `OrdinaryObjectCreate`는 빈 객체를 생성한 후, 객체에 추가할 프로퍼티 목록이 인수로 전달된 경우 프로퍼티를 객체에 추가한다. 그리고 인수로 전달받은 프로토타입을 자신이 생성한 객체의 `[[Prototype]]` 내부 슬롯에 할당한 다음, 생성한 객체를 반환한다.

**즉, 프로토타입은 추상 연산 `OrdinaryObjectCreate`에 전달되는 인수에 의해 결정된다. 이 인수는 객체가 생성되는 시점에 객체 생성 방식에 의해 결정된다.**

### 객체 리터럴에 의해 생성된 객체의 프로토타입
JavaScript 엔진은 객체 리터럴을 평가하여 객체를 생성할 때 추상 연산 `OrdinaryObjectCreate`를 호출한다. 이때 추상 연산에 전달되는 프로토타입은 `Object.prototype`이다. 즉, **객체 리터럴에 의해 생성되는 프로토타입은 `Object.prototype`이다.**

```javascript
const obj = { x: 1 };

// 객체 리터럴에 의해 생성된 obj 객체는 Object.prototype을 상속받는다.
console.log(obj.constructor === Object); // true
console.log(obj.hasOwnProperty('x')); // true
```

위 객체 리터럴이 평가되면 추상 연산 `OrdinaryObjectCreate`에 의해 다음과 같이 `Object` 생성자 함수와 `Object.prototype`과 생성된 객체 사이에 연결이 만들어진다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/98fb2387-5a45-493f-9092-404caa1c06d1)

객체 리터럴에 의해 생성된 `obj` 객체는 `Object.prototype`을 프로토타입으로 가지며 이로써 `Object.prototype`을 상속받는다. `obj` 객체는 `constructor` 프로퍼티와 `hasOwnProperty` 메서드를 자신의 자산인 것처럼 자유롭게 사용할 수 잇다. 이는 `obj` 객체가 자신의 프로토타입인 `Object.prototype` 객체를 상속받았기 때문이다.

### Object 생성자 함수에 의해 생성된 객체의 프로토타입
`Object` 생성자 함수를 인수 없이 호출하면 빈 객체가 생성된다. `Object` 생성자 함수를 호출하면 객체 리터럴과 마찬가지로 추상 연산 `OrdinaryObjectCreate`가 호출된다. 이 때 추상 연산에 전달되는 프로토타입은 `Object.prototype`이다. 즉, `Object` 생성자 함수에 의해 생성되는 객체의 프로토타입은 `Object.prototype`이다.

```javascript
const obj = new Object();
obj.x = 1;

// Object 생성자 함수에 의해 생성된 obj 객체는 Object.prototype을 상속받는다.
console.log(obj.constructor === Object); // true
console.log(obj.hasOwnProperty('x')); // true
```

위 코드가 실행되면 추상 연산 `OrdinaryObjectCreate`에 의해 다음과 같이 `Object` 생성자 함수와 `Object.prototype`과 생성된 객체 사이에 연결이 만들어진다. 객체 리터럴에 의해 생성된 객체와 동일한 구조를 갖는다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fe4f0475-bad4-490d-903a-94d1d52284ab)

객체 리터럴과 `Object` 생성자 함수에 의한 객체 생성 방식의 차이는 프로퍼티를 추가하는 방식에 있다. 객체 리터럴 방식은 객체 리터럴 내부에 프로퍼티를 추가하지만 `Object` 생성자 함수 방식은 일단 빈 객체를 생성한 이후 프로퍼티를 추가해야 한다.

### 생성자 함수에 의해 생성된 객체의 프로토타입
`new` 연산자와 함께 생성자 함수를 호출하여 인스턴스를 생성하면 다른 객체 생성 방식과 마찬가지로 추상 연산 `OrdinaryObjectCreate`가 호출된다. 이때 추상 연산에 전달되는 프로토타입은 생성자 함수의 `prototype` 프로퍼티에 바인딩 되어 있는 객체다. 즉, 생성자 함수에 의해 생성되는 객체의 프로토타입은 생성자 함수의 `prototype` 프로퍼티에 바인딩되어 있는 객체다.

```javascript
function Person(name) {
  this.name = name;
}

const me = new Person('Ramos');
```

위 코드가 실행되면 추상 연산 `OrdinaryObjectCreate`에 의해 다음과 같이 생성자 함수와 생성자 함수의 `prototype` 프로퍼티에 바인딩되어 있는 객체와 생성된 객체 사이에 연결이 만들어진다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/c74d7f3a-5499-4d5f-ad32-7f5ee7c4b743)

표준 빌트인 객체인 `Object` 생성자 함수와 더불어 생성된 프로토타입 `Object.prototype`은 다양한 빌트인 메서드를 갖고 있다. 하지만 사용자 정의 생성자 함수 `Person`과 더불어 생성된 프로토타입 `Person.prototype`의 프로퍼티는 `constructor` 뿐이다.

프로토타입은 객체다. 따라서 일반 객체와 같이 프로토타입에도 프로퍼티를 추가/삭제할 수 있다. 이렇게 추가/삭제된 프로퍼티는 프로토타입 체인에 즉각 반영된다.

```javascript
// 프로토타입 Person.prototype에 프로퍼티를 추가하여 자식 객체가 상속받을 수 있도록 해보자.
function Person(name) {
  this.name = name;
}

Person.prototype.sayHello = function () {
  console.log(`Hi! My name is ${this.name}`);
};

const me = new Person('Ramos');
const you = new Person('Kroos');

me.sayHello(); // Hi! My name is Ramos
you.sayHello(); // Hi! My name is Kroos
```

`Person` 생성자 함수를 통해 생성된 모든 객체는 프로토타입에 추가된 `sayHello` 메서드를 상속받아 자신의 메서드처럼 사용할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3feaf7b4-95ba-4321-a7c2-4ae4c2c17141)

## 프로토타입 체인
```javascript
function Person(name) {
  this.name = name;
}

Person.prototype.sayHello = function () {
  console.log(`Hi! My name is ${this.name}`);
}

const me = new Person('Ramos');

// hasOwnProperty는 Object.prototype의 메서드다.
console.log(me.hasOwnProperty('name')); // true

console.log(Object.getPrototypeOf(me) === Person.prototype); // true
console.log(Object.getPrototypeOf(Person.prototype) === Object.prototype); // true
```

위 예제는 `me` 객체가 `Person.prototype`뿐만 아니라 `Object.prototype`도 상속받았다는 것을 의미한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/5038f2d2-802a-42fe-a0b9-de1189127e6d)

**JavaScript는 객체의 프로퍼티(메서드 포함)에 접근하려고 할 때 해당 객체에 접근하려는 프로퍼티가 없다면 `[[Prototype]]` 내부 슬롯의 참조를 따라 자신의 부모 역할을 하는 프로토타입의 프로퍼티를 순차적으로 검색한다. 이를 프로토타입 체인이라 한다. 프로토타입 체인은 JavaScript가 객체지향 프로그래밍의 상속을 구현하는 메커니즘이다.**

`me.hasOwnProperty('name')`과 같이 메서드를 호출하면 JavaScript는 다음과 같은 과정을 거쳐 메서드를 검색한다. 프로퍼티를 참조하는 경우도 마찬가지다.

1. `hasOwnProperty`를 호출한 `me` 객체에서 `hasOwnProperty` 메서드를 검색한다. `me` 객체에는 해당 메서드가 없으므로 프로토타입 체인을 따라, 다시 말해 `[[Prototype]]` 내부 슬롯에 바인딩되어 있는 프로토타입으로 이동하여 `hasOwnProperty` 메서드를 검색한다.
2. `Person.prototype`에도 `hasOwnProperty` 메서드가 없으므로 프로토타입 체인을 따라, 다시 말해 `[[Prototype]]` 내부 슬롯에 바인딩되어 있는 프로토타입으로 이동하여 해당 메서드를 검색한다.
3. `Object.prototype`에는 해당 메서드가 존재한다. JavaScript 엔진은 `Object.prototype.hasOwnProperty` 메서드를 호출한다. 이때 `Object.prototype.hasOwnProperty` 메서드의 `this`에는 `me` 객체가 바인딩된다.

**프로토타입 체인의 최상위에 위치하는 객체는 언제나 `Object.prototype`이다.** 따라서 모든 객체는 `Object.prototype`을 상속받는다. 이를 프로토타입 체인의 종점이라 한다. `Object.prototype`의 프로토타입, 즉 `[[Prototype]]` 내부 슬롯의 값은 `null`이다.

프로토타입 체인의 종점인 `Object.prototype`에서도 프로퍼티를 검색할 수 없는 경우 `undefined`를 반환하며 이때 에러가 발생하진 않는다.

JavaScript 엔진은 프로토타입 체인을 따라 프로퍼티/메서드를 검색한다. 다시 말해, JavaScript 엔진은 객체 간의 상속 관계로 이루어진 프로토타입의 계층적인 구조에서 객체의 프로퍼티를 검색한다. 따라서 **프로토타입 체인은 상속과 프로퍼티 검색을 위한 메커니즘**이라 할 수 있다.

이에 반해, 프로퍼티가 아닌 식별자는 스코프 체인에서 검색한다. JavaScript 엔진은 함수의 중첩 관계로 이루어진 스코프의 계층적 구조에서 식별자를 검색한다. 따라서 **스코프 체인은 식별자 검색을 위한 메커니즘**이라 할 수 있다.

```javascript
me.hasOwnProperty('name');
```

위 예제는 먼저 스코프 체인에서 `me` 식별자를 검색한다. `me` 식별자는 전역에서 선언되었으므로 전역 스코프에서 검색된다. `me` 식별자를 검색한 다음, `me` 객체의 프로토타입 체인에서 `hasOwnProperty` 메서드를 검색한다.

이처럼 **스코프 체인과 프로토타입 체인은 서로 연관없이 별도로 동작하는 것이 아니라 서로 협력하여 식별자와 프로퍼티를 검색하는 데 사용된다.**

## 오버라이딩과 프로퍼티 섀도잉
```javascript
const Person = (function () {
  // 생성자 함수
  function Person(name) {
    this.name = name;
  }

  Person.prototype.sayHello = function () {
    console.log(`Hi! My name is ${this.name}`);
  };

  // 생성자 함수를 반환
  return Person;
}());

const me = new Person('Ramos');

// 인스턴스 메서드
me.sayHello = function () {
  console.log(`Hey! My name is ${this.name}`);
};

// 인스턴스 메서드가 호출된다. 프로토타입 메서드는 인스턴스 메서드에 의해 가려진다.
me.sayHello(); // Hey! My name is Ramos
```

생성자 함수로 객체(인스턴스)를 생성한 다음, 인스턴스에 메서드를 추가한 예제다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/21b653e3-e27f-411e-8c90-8bd414e3e99d)

- 프로토타입 프로퍼티: 프로토타입이 소유한 프로퍼티(메서드 포함)
- 인스턴스 프로퍼티: 인스턴스가 소유한 프로퍼티

프로토타입 프로퍼티와 같은 이름의 프로퍼티를 인스턴스에 추가하면 프로토타입 체인을 따라 프로토타입 프로퍼티를 검색하여 프로토타입 프로퍼티를 덮어쓰는 것이 아니라 인스턴스 프로퍼티로 추가한다. 이때 인스턴스 메서드 `sayHello`는 프로토타입 메서드 `sayHello`를 오버라이딩했고 프로토타입 메서드 `sayHello`는 가려진다. 이처럼 **상속 관계에 의해 프로퍼티가 가려지는 현상을 프로퍼티 섀도잉이라 한다.**

```javascript
// 인스턴스 메서드를 삭제
delete me.sayHello;
// 인스턴스에는 sayHello 메서드가 없으므로 프로토타입 메서드가 호출된다.
me.sayHello(); // Hi! My name is Ramos

// 프로토타입 체인을 통해 프로토타입 메서드가 삭제되지 않는다.
delete me.sayHello;
// 프로토타입 메서드가 호출된다.
me.sayHello(); // Hi! My name is Ramos
```

하위 객체를 통해 프로토타입의 프로퍼티를 변경 또는 삭제하는 것은 불가능하다. 다시 말해 하위 객체를 통해 프로토타입에 get 액세스는 허용되나 set 액세스는 허용되지 않는다.

프로토타입 프로퍼티를 변경 또는 삭제하려면 하위 객체를 통해 프로토타입 체인으로 접근하는 것이 아니라 프로토타입에 직접 접근해야 한다.

```javascript
Person.prototype.sayHello = function () {
  console.log(`Hey! My name is ${this.name}`);
};
me.sayHello(); // Hey! My name is Ramos

delete Person.prototype.sayHello;
me.sayHello(); // TypeError: me.sayHello is not a function
```

## 프로토타입의 교체
프로토타입은 임의의 다른 객체로 변경할 수 있다. 이는 부모 객체인 프로토타입을 동적으로 변경할 수 있다는 것을 의미한다. 이러한 특징을 활용하여 **객체 간의 상속 관계를 동적으로 변경할 수 있다.** 프로토타입은 생성자 함수 또는 인스턴스에 의해 교체할 수 있다.

다만, 프로토타입 교체를 통해 객체 간의 상속 관계를 동적으로 변경하는 것은 꽤 번거롭고 프로토타입은 직접 교체하지 않는 것이 좋다. 상속 관계를 인위적으로 설정하려면 직접 상속이 더 편리하고 안전하다. 또한 ES6에서 도입된 클래스를 사용하면 간편하고 직관적으로 상속 관계를 구현할 수 있다.

### 생성자 함수에 의한 프로토타입 교체
```javascript
const Person = (function () {
  function Person(name) {
    this.name = name;
  }

  // 생성자 함수의 prototype 프로퍼티를 통해 프로토타입을 객체 리터럴로 교체
  Person.prototype = {
    sayHello() {
      console.log(`Hi! My name is ${this.name}`);
    }
  };

  return Person;
}());

const me = new Person('Ramos');
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fa565583-7ffe-4a61-9a87-233e88238a04)

프로토타입으로 교체한 객체 리터럴에는 `constructor` 프로퍼티가 없다. `constructor` 프로퍼티는 JavaScript 엔진이 프로토타입을 생성할 때 암묵적으로 추가한 프로퍼티다. 따라서 `me` 객체의 생성자 함수를 검색하면 `Person`이 아닌 `Object`가 나온다.

```javascript
// 프로토타입을 교체하면 constructor 프로퍼티와 생성자 함수 간의 연결이 파괴된다.
console.log(me.constructor === Person); // false
// 프로토타입 체인을 따라 Object.prototype의 constructor 프로퍼티가 검색된다.
console.log(me.constructor === Object); // true
```

```javascript
const Person = (function () {
  function Person(name) {
    this.name = name;
  }

  // 생성자 함수의 prototype 프로퍼티를 통해 프로토타입을 객체 리터럴로 교체
  Person.prototype = {
    // constructor 프로퍼티와 생성자 함수 간의 연결을 설정
    constructor: Person,
    sayHello() {
      console.log(`Hi! My name is ${this.name}`);
    }
  };

  return Person;
}());

const me = new Person('Ramos');

// constructor 프로퍼티가 생성자 함수를 가리킨다.
console.log(me.constructor === Person); // true
console.log(me.constructor === Object); // false
```

### 인스턴스에 의한 프로토타입의 교체
프로토타입은 생성자 함수의 `prototype` 프로퍼티뿐만 아니라 인스턴스의 `__proto__` 접근자 프로퍼티 또는 `Object.getPropertyOf` 메서드를 통해 접근할 수 있다. 따라서 인스턴스의 해당 프로퍼티와 메서드를 통해 프로토타입을 교체할 수 있다.

- 생성자 함수의 `prototype` 프로퍼티에 다른 임의의 객체를 바인딩하는 것은 미래에 생성할 인스턴스의 프로토타입을 교체하는 것이다.
- `__proto__` 접근자 프로퍼티를 통해 프로토타입을 교체하는 것은 이미 생성된 객체의 프로토타입을 교체하는 것이다.

```javascript
function Person(name) {
  this.name = name;
}
const me = new Person('Ramos');

// 프로토타입으로 교체할 객체
const parent = {
  sayHello() {
    console.log(`Hi! My name is ${this.name}`);
  }
};

// me 객체의 프로토타입을 parent 객체로 교체한다.
Object.setPrototypeOf(me, parent);

me.sayHello(); // Hi! My name is Ramos
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/41d5d364-8469-41f6-abf1-b9cfaa778f66)

프로토타입으로 교체한 객체에는 `constructor` 프로퍼티가 없으므로 `constructor` 프로퍼티와 생성자 함수 간의 연결이 파괴된다.

생성자 함수에 의한 프로토타입 교체와 인스턴스에 의한 프로토타입 교체는 별다른 차이가 없어 보이지만 미묘한 차이가 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/92ec3281-4ff5-4a67-9730-087856885ecd)

```javascript
function Person(name) {
  this.name = name;
}
const me = new Person('Ramos');

// 프로토타입으로 교체할 객체
const parent = {
  constructor: Person,
  sayHello() {
    console.log(`Hi! My name is ${this.name}`);
  }
};

// 생성자 함수의 prototype 프로퍼티와 프로토타입 간의 연결을 설정
Person.prototype = parent;

// me 객체의 프로토타입을 parent 객체로 교체한다.
Object.setPrototypeOf(me, parent);

me.sayHello(); // Hi! My name is Ramos

// constructor 프로퍼티가 생성자 함수를 가리킨다.
console.log(me.constructor === Person); // true
console.log(me.constructor === Object); // false

// 생성자 함수의 prototype 프로퍼티가 교체된 프로토타입을 가리킨다.
console.log(Person.prototype === Object.getPrototypeOf(me)); // true
```

## instanceof 연산자
`instanceof` 연산자는 이항 연산자로서 좌변에 객체를 가리키는 식별자, 우변에 생성자 함수를 가리키는 식별자를 피연산자로 받는다. 만약 우변의 피연산자가 함수가 아닌 경우 `TypeError`가 발생한다.

```javascript
function Person(name) {
  this.name = name;
}

const me = new Person('Ramos');

// Person.prototype이 me 객체의 프로토타입 체인 상에 존재하므로 true
console.log(me instanceof Person); // true

// Object.prototype이 me 객체의 프로토타입 체인 상에 존재하므로 true
console.log(me instanceof Object); // true
```

```javascript
function Person(name) {
  this.name = name;
}

const me = new Person('Ramos');

const parent = {};

// 프로토타입 교체
Object.setPrototypeOf(me, parent);

// Person 생성자 함수와 parent 객체는 연결되어 있지 않다.
console.log(Person.prototype === parent); // false
console.log(parent.constructor === Person); // false

// Person.prototype이 me 객체의 프로토타입 체인 상에 존재하지 않는다.
console.log(me instanceof Person); // false

// Object.prototype이 me 객체의 프로토타입 체인 상에 존재하므로 true
console.log(me instanceof Object); // true
```

`me` 객체는 프로토타입이 교체되어 프로토타입과 생성자 함수 간의 연결이 파괴되었지만 `Person` 생성자 함수에 의해 생성된 인스턴스임에는 틀림 없다. 그러나 `me instanceof Person`은 `false`로 평가된다.

이는 `Person.prototype`이 `me` 객체의 프로토타입 체인 상에 존재하지 않기 때문이다. 따라서 프로토타입으로 교체한 `parent` 객체를 `Person` 생성자 함수의 `prototype` 프로퍼티에 바인딩되면 `me instanceof Person`은 `true`로 평가될 것이다.

```javascript
function Person(name) {
  this.name = name;
}

const me = new Person('Ramos');

const parent = {};

// 프로토타입 교체
Object.setPrototypeOf(me, parent);

// Person 생성자 함수와 parent 객체는 연결되어 있지 않다.
console.log(Person.prototype === parent); // false
console.log(parent.constructor === Person); // false

Person.prototype = parent;

// Person.prototype이 me 객체의 프로토타입 체인 상에 존재하므로 true
console.log(me instanceof Person); // true

// Object.prototype이 me 객체의 프로토타입 체인 상에 존재하므로 true
console.log(me instanceof Object); // true
```

이처럼 `instanceof`는 프로토타입의 `constructor` 프로퍼티가 가리키는 생성자 함수를 찾는 것이 아니라 **생성자 함수의 `prototype`에 바인딩된 객체가 프로토타입 체인 상에 존재하는지 확인한다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d5f208d3-9506-40cc-a7ee-aac7e440995e)

## 직접 상속
### Object.create에 의한 직접 상속
`Object.create` 메서드는 명시적으로 프로토타입을 지정하여 새로운 객체를 생성한다. 다른 객체 생성 방식과 마찬가지로 추상 연산 `OrdinaryObjectCreate`를 호출한다.

```javascript
/**
 * 지정된 프로토타입 및 프로퍼티를 갖는 새로운 객체를 생성하여 반환한다.
 * @param {Object} prototype - 생성할 객체의 프로토타입으로 지정할 객체
 * @param {Object} [propertiesObject] - 생성할 객체의 프로퍼티를 갖는 객체
 * @returns {Object} 지정된 프로토타입 및 프토퍼티를 갖는 새로운 객체
 */
Object.create(prototype[, propertiesObject])
```

```javascript
// 프로토타입이 null인 객체를 생성한다. 생성된 객체는 프로토타입 체인의 종점에 위치한다.
// obj → null
let obj = Object.create(null);
console.log(Object.getPrototypeOf(obj) === null); // true
// Object.prototype을 상속받지 못한다.
console.log(obj.toString()); // TypeError: obj.toString is not a function

// obj → Object.prototype → null
// obj = {}과 동일
obj = Object.create(Object.prototype);
console.log(Object.getPrototypeOf(obj) === Object.prototype); // true

// obj → Object.prototype → null
// obj = { x: 1 }과 동일
obj = Object.create(Object.prototype, {
  x: { value: 1, writable: true, enumerable: true, configurable: true }
});
// 위 코드는 아래와 동일함
// obj = Object.create(Object.prototype);
// obj.x = 1;

console.log(obj.x); // 1
console.log(Object.getPrototypeOf(obj) === Object.prototype); // true

const myProto = { x: 10 };
// 임의의 객체를 직접 상속받는다.
// obj → myProto → Object.prototype → null
obj = Object.create(myProto);
console.log(obj.x); // 10
console.log(Object.getPrototypeOf(obj) === myProto); // true

// 생성자 함수
function Person(name) {
  this.name = name;
}

// obj → Person.prototype → Object.prototype → null
// obj = new Person('Ramos')와 동일
obj = Object.create(Person.prototype);
obj.name = 'Ramos';
console.log(obj.name); // Ramos
console.log(Object.getPrototypeOf(obj) === Person.prototype); // true
```

**`Object.create` 메서드는 첫 번째 매개변수에 전달한 객체의 프로토타입 체인에 속하는 객체를 생성한다.** 즉, 객체를 생성하면서 직접적으로 상속을 구현하는 것이다. 장점은 다음과 같다.

- `new` 연산자 없이도 객체를 생성할 수 있다.
- 프로토타입을 지정하면서 객체를 생성할 수 있다.
- 객체 리터럴에 의해 생성된 객체도 상속받을 수 있다.

`Object.prototype`의 빌트인 메서드인 `Object.prototype.hasOwnProperty`, `Object.prototype.isPrototypeOf`, `Object.prototype.propertyIsEnumerable` 등은 모든 객체의 프로토타입 체인의 종점, 즉 `Object.prototype`의 메서드이므로 모든 객체가 상속받아 호출할 수 있다.

하지만 ESLint에선 `Object.prototype`의 빌트인 메서드를 객체가 직접 호출하는 것을 권장하지 않는다. `Object.create` 메서드를 통해 프로토타입 체인의 종점에 위치하는 객체를 생성할 수 있기 때문이다. 종점에 위치하는 객체는 `Object.prototype`의 빌트인 메서드를 사용할 수 없다.

따라서 에러를 발생시킬 위험을 없애기 위해 `Object.prototype`의 빌트인 메서드는 다음과 같이 간접적으로 호출하는 것이 좋다.

```javascript
// 프로토타입이 null인 객체를 생성
const obj = Object.create(null);
obj.a = 1;

console.log(Object.prototype.hasOwnProperty.call(obj, 'a')); // true
```

### 객체 리터럴 내부에서 `__proto__`에 의한 직접 상속
`Object.create` 메서드에 의한 직접 상속은 여러 장점이 있지만 두 번째 인자로 프로퍼티를 정의하는 것은 번거롭다. 일단 객체를 생성한 이후 프로퍼티를 추가하는 방법도 있으나 이 또한 깔끔한 방법은 아니다.

ES6에선 객체 리터럴 내부에서 `__proto__` 접근자 프로퍼티를 사용하여 직접 상속을 구현할 수 있다.

```javascript
const myProto = { x: 10 };

// 객체 리터럴에 의해 객체를 생성하면서 프로토타입을 지정하여 직접 상속받을 수 있다.
const obj = {
  y: 20,
  // 객체를 직접 상속받는다.
  // obj → myProto → Object.prototype → null
  __proto__: myProto
};

console.log(obj.x, obj.y); // 10 20
console.log(Object.getPrototypeOf(obj) === myProto); // true
```

## 정적 프로퍼티/메서드
**정적 프로퍼티/메서드는 생성자 함수로 인스턴스를 생성하지 않아도 참조/호출할 수 있는 프로퍼티/메서드를 말한다.**

```javascript
function Person(name) {
  this.name = name;
}

// 프로토타입 메서드
Person.prototype.sayHello = function () {
  console.log(`Hi! My name is ${this.name}`);
};

// 정적 프로퍼티
Person.staticProp = 'static prop';

// 정적 메서드
Person.staticMethod = function () {
  console.log('staticMethod');
};

const me = new Person('Ramos');

// 생성자 함수에 추가한 정적 프로퍼티/메서드는 생성자 함수로 참조/호출한다.
Person.staticMethod(); // staticMethod

// 정적 프로퍼티/메서드는 생성자 함수가 생성한 인스턴스로 참조/호출할 수 없다.
// 인스턴스로 참조/호출할 수 있는 프로퍼티/메서드는 프로토타입 체인 상에 존재해야 한다.
me.staticMethod(); // TypeError: me.staticMethod is not a function
```

**`Person` 생성자 함수는 객체이므로 자신의 프로퍼티/메서드를 소유할 수 있다.** 생성자 함수 객체가 소유한 프로퍼티/메서드를 정적 프로퍼티/메서드라고 한다. 정적 프로퍼티/메서드는 생성자 함수가 생성한 인스턴스로 참조/호출할 수 없다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/87262498-e29c-4c78-801b-6fb283b05570)

생성자 함수가 생성한 인스턴스는 자신의 프로토타입 체인에 속한 객체의 프로퍼티/메서드에 접근할 수 있다. 하지만 정적 프로퍼티/메서드는 인스턴스의 프로토타입 체인에 속한 객체의 프로퍼티/메서드가 아니므로 인스턴스로 접근할 수 없다.

```javascript
// Object.create는 정적 메서드다.
const obj = Object.create({ name: 'Ramos' });

// Object.prototype.hasOwnProperty는 프로토타입 메서드다.
obj.hasOwnProperty('name'); // false
```

만약 인스턴스/프로토타입 메서드 내에서 `this`를 사용하지 않는다면 그 메서드는 정적 메서드로 변경할 수 있다. **인스턴스가 호출한 인스턴스/프로토타입 메서드 내에서 `this`는 인스턴스를 가리킨다.** 메서드 내에서 인스턴스를 참조할 필요가 없다면 정적 메서드로 변경하여도 동작한다. 프로토타입 메서드를 호출하려면 인스턴스를 생성해야 하지만 정적 메서드는 인스턴스를 생성하지 않아도 호출할 수 있다.

```javascript
function Foo() {}

// 프로토타입 메서드
// this를 참조하지 않는 프로토타입 메서드는 정적 메서드로 변경하여도 동일한 효과를 얻을 수 있다.
Foo.prototype.x = function () {
  console.log('x');
};

const foo = new Foo();
// 프로토타입 메서드를 호출하려면 인스턴스를 생성해야 한다.
foo.x(); // x

// 정적 메서드
Foo.x = function () {
  console.log('x');
};

// 정적 메서드는 인스턴스를 생성하지 않아도 호출할 수 있다.
Foo.x(); // x
```

## 프로퍼티 존재 확인
### in 연산자
객체 내에 특정 프로퍼티가 존재하는지 여부를 확인한다.

```javascript
/**
 * key: 프로퍼티 키를 나타내는 문자열
 * object: 객체로 평가되는 표현식
 */
key in object
```

```javascript
const person = {
  name: 'Ramos',
  address: 'Spain'
};

console.log('name' in person); // true
console.log('address' in person); // true
console.log('age' in person); // false
```

**`in` 연산자는 확인 대상 객체의 프로퍼티뿐만 아니라 확인 대상 객체가 상속받은 모든 프로토타입의 프로퍼티를 확인하므로 주의가 필요하다.**

```javascript
console.log('toString' in person); // true
```

`in` 연산자 대신 ES6에서 도입된 `Reflect.has` 메서드를 사용할 수도 있다.

```javascript
const person = { name: 'Ramos' };

console.log(Reflect.has(person, 'name')); // true
console.log(Reflect.has(person, 'toString')); // true
```

### Object.prototype.hasOwnProperty 메서드
`Object.prototype.hasOwnProperty` 메서드를 사용해도 객체에 특정 프로퍼티가 존재하는지 확인할 수 있다.

```javascript
console.log(person.hasOwnProperty('name')); // true
console.log(person.hasOwnProperty('age')); // false
```

`Object.prototype.hasOwnProperty`는 인수로 전달받은 프로퍼티 키가 객체 고유의 프로퍼티 키인 경우에만 `true`를 반환하고 **상속받은 프로토타입의 프로퍼티 키인 경우 `false`를 반환한다.**

```javascript
console.log(person.hasOwnProperty('toString')); // false
```

## 프로퍼티 열거
### for ... in 문
객체의 모든 프로퍼티를 순회하며 열거하려면 `for ... in` 문을 사용한다.

```javascript
// for (변수선언문 in 객체) {...}
const person = {
  name: 'Ramos',
  address: 'Spain'
};

// 변수 선언문에서 선언한 변수에 프로퍼티 키를 할당함
for (const key in person) {
  console.log(key + ': ' + person[key]);
}
// toString과 같은 Object.prototype의 프로퍼티가 열거되지 않는다.

// name: Ramos
// address: Spain
```

`for ... in` 문은 `in` 연산자처럼 순회 대상 객체의 프로퍼티뿐만 아니라 프로토타입의 프로퍼티까지 열거한다. 하지만 `Object.prototype`의 프로퍼티가 열거되지 않는데 이는 `toString` 메서드가 열거할 수 없도록 정의되어 있는 프로퍼티이기 때문이다. 다시 말해, `Object.prototype.toString`의 프로퍼티 어트리뷰트 `[[Enumerable]]`이 `false`이기 때문이다.

**`for ... in` 문은 객체의 프로토타입 체인 상에 존재하는 모든 프로토타입의 프로퍼티 중 프로퍼티 어트리뷰트 `[[Enumerable]]`의 값이 `true`인 프로퍼티를 순회하며 열거한다.**

- `for ... in` 문은 프로퍼티 키가 심벌인 프로퍼티는 열거하지 않는다.
- 상속받은 프로퍼티는 제외하고 객체 자신의 프로퍼티만 열거하려면 `Object.prototype.hasOwnProperty` 메서드를 사용하여 객체 자신의 프로퍼티인지 확인해야 한다.
- `for ... in` 문은 프로퍼티를 열거할 때 순서를 보장하지 않으므로 주의해야 한다.

**배열에는 일반적인 `for` 문이나 `for ... of` 문 또는 `Array.prototype.forEach` 메서드를 사용하기를 권장한다.** 사실 배열도 객체이므로 프로퍼티와 상속받은 프로퍼티가 포함될 수 있다.

```javascript
const arr = [1, 2, 3];
arr.x = 10;

for (const i in arr) {
  console.log(arr[i]); // 1 2 3 10
}

// arr.length는 3이다.
for (let i = 0; i < arr.length; i++) {
  console.log(arr[i]); // 1 2 3
}

// forEach 메서드는 요소가 아닌 프로퍼티는 제외한다.
arr.forEach(v => console.log(v)); // 1 2 3

// for...of는 변수 선언문에서 선언한 변수에 키가 아닌 값을 할당한다.
for (const value of arr) {
  console.log(value); // 1 2 3
}
```

### Object.keys/values/entries 메서드
객체 자신의 고유 프로퍼티만 열거하기 위해서는 `for ... in` 문을 사용하는 것보다 `Object.keys/values/entries` 메서드를 사용하는 것을 권장한다.

`Object.keys`는 객체 자신의 열거 가능한 프로퍼티 키를 배열로 반환한다.

```javascript
const person = {
  name: 'Ramos',
  address: 'Spain',
  __proto__: { age: 28 }
};

console.log(Object.keys(person)); // ["name", "address"]
```

ES8에서 도입된 `Object.values` 메서드는 객체 자신의 열거 가능한 프로퍼티 값을 배열로 반환한다.

```javascript
console.log(Object.values(person)); // ["Ramos", "Spain"]
```

ES8에서 도입된 `Object.entries` 메서드는 객체 자신의 열거 가능한 프로퍼티 키와 값의 쌍의 배열을 배열에 담아 반환한다.

```javascript
console.log(Object.entries(person)); // [["name", "Ramos"], ["address", "Spain"]]

Object.entries(person).forEach(([key, value]) => console.log(key, value));
// name Ramos
// address Spain
```