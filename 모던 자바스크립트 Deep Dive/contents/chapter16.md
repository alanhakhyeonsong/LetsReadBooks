# 16장. 프로퍼티 어트리뷰트
## 내부 슬롯과 내부 메서드
**내부 슬롯과 내부 메서드는 JavaScript 엔진의 구현 알고리즘을 설명하기 위해 ECMAScript 사양에서 사용하는 의사 프로퍼티와 의사 메서드다.** ECMAScript 사양에 등장하는 이중 대괄호(`[[...]]`)로 감싼 이름들이 이에 해당한다.

내부 슬롯과 내부 메서드는 ECMAScript 사양에 정의된 대로 구현되어 JavaScript 엔진에서 실제로 동작하지만 개발자가 직접 접근할 수 있도록 외부로 공개된 객체의 프로퍼티는 아니다. 즉, 내부 슬롯과 내부 메서드는 JavaScript 엔진의 내부 로직이므로 원칙적으로 JavaScript는 내부 슬롯과 내부 메서드에 직접적으로 접근하거나 호출할 수 있는 방법을 제공하지 않는다. 단, 일부 내부 슬롯과 내부 메서드에 한해 간접적으로 접근할 수 있는 수단을 제공하기는 한다.

예를 들어, 모든 객체는 `[[Prototype]]`이라는 내부 슬롯을 갖는다. 내부 슬롯은 JavaScript 엔진의 내부 로직이므로 원칙적으로 직접 접근할 수 없지만 `[[Prototype]]` 내부 슬롯의 경우, `__proto__`를 통해 간접적으로 접근할 수 있다.

```javascript
const o = {};

// 내부 슬롯은 JavaScript 엔진의 내부 로직이므로 원칙적으로 직접 접근할 수 없다.
o.[[Prototype]] // Uncaught SyntaxError: Unexpected token '['
// 단, 일부 내부 슬롯과 내부 메서드에 한하여 간접적으로 접근할 수 있는 수단을 제공하기는 한다.
o.__proto__ // Object.prototype
```

## 프로퍼티 어트리뷰트와 프로퍼티 디스크립터 객체
**JavaScript 엔진은 프로퍼티를 생성할 때 프로퍼티의 상태를 나타내는 프로퍼티 어트리뷰트를 기본값으로 자동 정의한다.** 프로퍼티의 상태란 프로퍼티의 값, 값의 갱신 가능 여부, 열거 가능 여부, 재정의 가능 여부를 말한다.

프로퍼티 어트리뷰트는 JavaScript 엔진이 관리하는 내부 상태 값인 내부 슬롯 `[[Value]]`, `[[Writable]]`, `[[Enumerable]]`, `[[Configurable]]`이다. 따라서 프로퍼티 어트리뷰트에 직접 접근할 수 없지만 `Object.getOwnPropertyDescriptor` 메서드를 사용하여 간접적으로 확인할 수는 있다.

```javascript
const person = {
  name: 'Ramos'
};

// 프로퍼티 어트리뷰트 정보를 제공하는 프로퍼티 디스크립터 객체를 반환
console.log(Object.getOwnPropertyDescriptor(person, 'name'));
// {value: "Ramos", writable: true, enumerable: true, configurable: true}
```

`Object.getOwnPropertyDescriptor` 메서드를 호출할 때 첫 번째 매개변수에는 객체의 참조를 전달하고, 두번째 매개변수에는 프로퍼티 키를 문자열로 전달한다. 이때 `Object.getOwnPropertyDescriptor` 메서드는 프로퍼티 어트리뷰트 정보를 제공하는 **프로퍼티 디스크립터 객체**를 반환한다. 만약 존재하지 않는 프로퍼티나 상속받은 프로퍼티에 대한 프로퍼티 디스크립터를 요구하면 `undefined`가 반환된다.

`Object.getOwnPropertyDescriptor` 메서드는 하나의 프로퍼티에 대해 프로퍼티 디스크립터 객체를 반환하지만 ES8에서 도입된 `Object.getOwnPropertyDescriptors` 메서드는 모든 프로퍼티의 프로퍼티 어트리뷰트 정보를 제공하는 프로퍼티 디스크립터 객체들을 반환한다.

```javascript
const person = {
  name: 'Ramos'
};

// 프로퍼티 동적 생성
person.age = 28;

// 모든 프로퍼티의 프로퍼티 어트리뷰트 정보를 제공하는 프로퍼티 디스크릅터 객체들을 반환한다.
console.log(Object.getOwnPropertyDescriptors(person));
/*
{
  {value: "Ramos", writable: true, enumerable: true, configurable: true},
  age: {value: 28, writable: true, enumerable: true, configurable: true}
}
*/
```

## 데이터 프로퍼티와 접근자 프로퍼티
- 데이터 프로퍼티: 키와 값으로 구성된 일반적인 프로퍼티다.
- 접근자 프로퍼티: 자체적으로는 값을 갖지 않고 다른 데이터 프로퍼티의 값을 읽거나 저장할 때 호출되는 접근자 함수로 구성된 프로퍼티다.

### 데이터 프로퍼티
데이터 프로퍼티는 다음과 같은 프로퍼티 어트리뷰트를 갖는다. 이 프로퍼티 어트리뷰트는 JavaScript 엔진이 프로퍼티를 생성할 때 기본값으로 자동 정의된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/7ba0ca58-2e55-45b1-8e52-e49f86d63768)

프로퍼티가 생성될 때 `[[Value]]` 값은 프로퍼티 값으로 초기화되며 `[[Writable]]`, `[[Enumerable]]`, `[[Configurable]]`의 값은 `true`로 초기화된다. 이것은 프로퍼티를 동적 추가해도 마찬가지다.

### 접근자 프로퍼티
접근자 프로퍼티는 자체적으로는 값을 갖지 않고 다른 데이터 프로퍼티의 값을 읽거나 저장할 때 사용하는 접근자 함수로 구성된 프로퍼티다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ee95cae2-73e6-4b21-b091-8b79f186e053)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6766dcf7-fab6-4bba-8dc4-cd2d0a9edcae)

접근자 함수는 getter/setter 함수라고도 부른다. 접근자 프로퍼티는 이들 모두를 정이할 수도 있고 하나만 정의할 수도 있다.

```javascript
const person = {
  // 데이터 프로퍼티
  firstName: 'Sergio',
  lastName: 'Ramos',

  // fullName은 접근자 함수로 구성된 접근자 프로퍼티
  // getter 함수
  get fullName() {
    return `${this.firstName} ${this.lastName}`;
  },

  // setter 함수
  get fullName(name) {
    // 배열 디스트럭처링 할당
    [this.firstName, this.lastName] = name.split(' ');
  }
};

// 데이터 프로퍼티를 통한 프로퍼티 값의 참조
console.log(person.firstName + ' ' + person.lastName); // Sergio Ramos

// 접근자 프로퍼티를 통한 프로퍼티 값의 저장
// 접근자 프로퍼티 fullName에 값을 저장하면 setter 함수가 호출됨
person.fullName = 'Toni Kroos';
console.log(person); // {firstName: "Toni", lastName: "Kroos"}

// 접근자 프로퍼티를 통한 프로퍼티 값의 참조
// 접근자 프로퍼티 fullName에 접근하면 getter 함수가 호출됨
console.log(person.fullName); // Toni Kroos

// firstName은 데이터 프로퍼티다.
// 데이터 프로퍼티는 [[Value]], [[Writable]], [[Enumarable]], [[Configurable]] 프로퍼티 어트리뷰트를 갖는다.
let descriptor = Object.getOwnPropertyDescriptor(person, 'firstName');
console.log(descriptor);
// {value: "Toni", writable: true, enumerable: true, configurable: true}


// fullName은 접근자 프로퍼티다.
// 접근자 프로퍼티는 [[Get]], [[Set]], [[Enumerable]], [[Configurable]] 프로퍼티 어트리뷰트를 갖는다.
descriptor = Object.getOwnPropertyDescriptor(person, 'fullName');
console.log(descriptor);
// {get: f, set: f, enumerable: true, configurable: true}
```

접근자 프로퍼티는 자체적으로 값을 가지지 않으며 다만 데이터 프로퍼티의 값을 읽거나 저장할 때 관여할 뿐이다. 이를 내부 슬롯/메서드 관점에서 설명하면 다음과 같다. 접근자 프로퍼티 `fullName`으로 프로퍼티 값에 접근하면 내부적으로 `[[Get]]` 내부 메서드가 호출되어 다음과 같이 동작한다.

1. 프로퍼티 키가 유효한지 확인한다. 프로퍼티 키는 문자열 또는 심벌이어야 한다. 프로퍼티 키 `"fullName"`은 문자열이므로 유효한 프로퍼티 키다.
2. 프로토타입 체인에서 프로퍼티를 검색한다. `person` 객체에 `fullName` 프로퍼티가 존재한다.
3. 검색된 `fullName` 프로퍼티가 데이터 프로퍼티인지 접근자 프로퍼티인지 확인한다. `fullName` 프로퍼티는 접근자 프로퍼티다.
4. 접근자 프로퍼티 `fullName`의 프로퍼티 어트리뷰트 `[[Get]]`의 값, 즉 getter 함수를 호출하여 그 결과를 반환한다. 프로퍼티 `fullName`의 프로퍼티 어트리뷰트 `[[Get]]`의 값은 `Object.getOwnPropertyDescriptor` 메서드가 반환하는 프로퍼티 디스크립터 객체의 `get` 프로퍼티 값과 같다.

접근자 프로퍼티와 데이터 프로퍼티를 구별하는 방법은 다음과 같다.

```javascript
// 일반 객체의 __proto__는 접근자 프로퍼티
Object.getOwnPropertyDescriptor(Object.prototype, '__proto__');
// {get: f, set: f, enumerable: true, configurable: true}

// 함수 객체의 __proto__는 데이터 프로퍼티
Object.getOwnPropertyDescriptor(function() {}, 'prototype');
// {value: {...}, writable: true, enumerable: true, configurable: true}
```

## 프로퍼티 정의
프로퍼티 정의란 새로운 프로퍼티를 추가하면서 프로퍼티 어트리뷰트를 명시적으로 정의하거나, 기존 프로퍼티의 프로퍼티 어트리뷰트를 재정의하는 것을 말한다. 예를 들어, 프로퍼티 값을 갱신 가능하도록 할 것인지, 프로퍼티를 열거 가능하도록 할 것인지, 프로퍼티를 재정의 가능하도록 할 것인지 정의할 수 있다. 이를 통해 객체의 프로퍼티가 어떻게 동작해야 하는지를 명확히 정의할 수 있다.

`Object.defineProperty` 메서드를 사용하면 프로퍼티의 어트리뷰트를 정의할 수 있다. 인수로는 객체의 참조와 데이터 프로퍼티의 키인 문자열, 프로퍼티 디스크립터 객체를 전달한다.

```javascript

```

`Object.defineProperty` 메서드로 프로퍼티를 정의할 때 프로퍼티 디스크립터 객체의 프로퍼티를 일부 생략 할 수 있다. 프로퍼티 디스크립터 객체에서 생략된 어트리뷰트는 다음과 같이 기본값이 적용된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0b574a3d-52b1-4281-a8e4-90c8bf5aa1c2)

`Object.defineProperty` 메서드는 한번에 하나의 프로퍼티만 정의할 수 있다. ``Object.defineProperties` 메서드를 사용하면 여러 개의 프로퍼티를 한 번에 정의할 수 있다.

```javascript

```

## 객체 변경 방지
**객체는 변경 가능한 값이므로 재할당 없이 직접 변경할 수 있다.** 즉, 프로퍼티를 추가하거나 삭제할 수 있고, 프로퍼티 값을 갱신할 수 있으며, `Object.defineProperty` 또는 `Object.defineProperties` 메서드를 사용하여 프로퍼티 어트리뷰트를 재정의할 수도 있다.

JavaScript는 객체의 변경을 방지하는 다양한 메서드를 제공한다. 객체 변경 방지 메서드들은 객체의 변경을 금지하는 강도가 다르다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3aa5cf97-8719-4505-b523-44d7fe924be0)

### 객체 확장 금지
`Object.preventExtensions` 메서드는 객체의 확장을 금지한다. 객체 확장 금지란 프로퍼티 추가 금지를 의미한다. 즉, **확장이 금지된 객체는 프로퍼티 추가가 금지된다.**

프로퍼티는 프로퍼티 동적 추가와 `Object.defineProperty` 메서드로 추가할 수 있다. 이 두 가지 추가 방법이 모두 금지된다.

확장이 가능한 객체인지 여부는 `Object.isExtensible` 메서드로 확인할 수 있다.

```javascript
const person = { name: 'Ramos' };

// person 객체는 확장이 금지된 객체가 아니다.
console.log(Object.isExtensible(person)); // true

// person 객체의 확장을 금지하여 프로퍼티 추가를 금지한다.
Object.preventExtensions(person);

// person 객체는 확장이 금지된 객체다.
console.log(Object.isExtensible(person)); // false

// 프로퍼티 추가가 금지된다.
person.age = 28; // 무시. strict mode에서는 에러
console.log(person); // {name: 'Ramos'}

// 프로퍼티 추가는 금지되지만 삭제는 가능하다.
delete person.name;
console.log(person); // {}

// 프로퍼티 정의에 의한 프로퍼티 추가도 금지된다.
Object.defineProperty(person, 'age', { value: 28 });
// TypeError: Cannot define property age, object is not extensible
```

### 객체 밀봉
`Object.seal` 메서드는 객체를 밀봉한다. **객체 밀봉이란 프로퍼티 추가 및 삭제와 프로퍼티 어트리뷰트 재정의 금지를 의미한다.** 즉, **밀봉된 객체는 읽기와 쓰기만 가능하다.**

```javascript
const person = { name: 'Ramos' };

// person 객체는 밀봉된 객체가 아니다.
console.log(Object.isSealed(person)); // false

// person 객체를 밀봉하여 프로퍼티 추가, 삭제, 재정의를 금지한다.
Object.seal(person);

// person 객체는 밀봉된 객체다.
console.log(Object.isSealed(person)); // true

// 밀봉된 객체는 configurable이 false
console.log(Object.getOwnPropertyDescriptors(person));
/*
{
  name: {value: 'Ramos', writable: true, enumerable: true, configurable: false},
}
*/

// 프로퍼티 추가가 금지된다.
person.age = 28; // 무시. strict mode에서는 에러
console.log(person); // {name: 'Ramos'}

// 프로퍼티 삭제가 금지된다.
delete person.name; // 무시. strict mode에서는 에러
console.log(person); // {name: 'Ramos'}

// 프로퍼티 값 갱신은 가능하다.
person.name = 'Kim';
console.log(person); // {name: 'Kim'}

// 프로퍼티 어트리뷰트 재정의가 금지된다.
Object.defineProperty(person, 'name', { configurable: true });
// TypeError: Cannot redefine property: name
```

### 객체 동결
`Object.freeze` 메서드는 객체를 동결한다. **객체 동결이란 프로퍼티 추가 및 삭제와 프로퍼티 어트리뷰트 재정의 금지, 프로퍼티 값 갱신 금지를 의미한다. 즉, **동결된 객체는 읽기만 가능하다.**

```javascript
const person = { name: 'Ramos' };

// person 객체는 동결된 객체가 아니다.
console.log(Object.isFrozen(person)); // false

// person 객체를 동결하여 프로퍼티 추가, 삭제, 재정의를 금지한다.
Object.freeze(person);

// person 객체는 동결된 객체다.
console.log(Object.isFrozen(person)); // true

// 동결된 객체는 writable, configurable이 false
console.log(Object.getOwnPropertyDescriptors(person));
/*
{
  name: {value: 'Ramos', writable: false, enumerable: true, configurable: false},
}
*/

// 프로퍼티 추가가 금지된다.
person.age = 28; // 무시. strict mode에서는 에러
console.log(person); // {name: 'Ramos'}

// 프로퍼티 삭제가 금지된다.
delete person.name; // 무시. strict mode에서는 에러
console.log(person); // {name: 'Ramos'}

// 프로퍼티 값 갱신이 금지된다.
person.name = 'Kim'; // 무시. strict mode에서는 에러
console.log(person); // {name: 'Ramos'}

// 프로퍼티 어트리뷰트 재정의가 금지된다.
Object.defineProperty(person, 'name', { configurable: true });
// TypeError: Cannot redefine property: name
```

### 불변 객체
**앞서 살펴본 변경 방지 메서드들은 얕은 변경 방지로 직속 프로퍼티만 변경이 방지되고 중첩 객체까지는 영향을 주지는 못한다.**

객체의 중첩 객체까지 동결하여 변경이 불가능한 읽기 전용의 불변 객체를 구현하려면 객체를 값으로 갖는 모든 프로퍼티에 대해 재귀적으로 `Object.freeze` 메서드를 호출해야 한다.

```javascript
function deepFreeze(target) {
  // 객체가 아니거나 동결된 객체는 무시하고 객체이고 동결되지 않은 객체만 동결한다.
  if (target && typeof target === 'object' && !Object.isFrozen(target)) {
    Object.freeze(target);

    Object.keys(target).forEach(key => deepFreeze(target[key]));
  }
  return target;
}

const person = {
  name: 'Ramos',
  address: { city: 'Seoul' }
};

// 깊은 객체 동결
deepFreeze(person);

console.log(Object.isFrozen(person)); // true
// 중첩 객체까지 동결한다.
console.log(Object.isFrozen(person.address)); // true

person.address.city = 'Madrid';
console.log(person); // {name: 'Ramos', address: {city: 'Seoul'}}
```