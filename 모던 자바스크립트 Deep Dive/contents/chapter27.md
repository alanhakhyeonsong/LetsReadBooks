# 27장. 배열
JavaScript의 모든 값은 배열의 요소가 될 수 있다. 원시값은 물론 객체, 함수, 배열 등 JavaScript에서 값으로 인정하는 모든 것은 배열의 요소가 될 수 있다.

```javascript
const arr = ['a', 'b', 'c'];

arr[0]; // 'a'
arr[1]; // 'b'
arr[2]; // 'c'
```

배열은 길이를 나타내는 `length` 프로퍼티를 갖는다.

```javascript
arr.length // 3

// index와 length 프로퍼티를 갖기 때문에 for 문 접근 가능
for (let i = 0; i < arr.length; i++) {
  console.log(arr[i]);
}

typeof arr // object
```

JavaScript에 배열이란 타입은 존재하지 않는다. **배열은 객체 타입이다.**

배열은 배열 리터럴, `Array` 생성자 함수, `Array.of`, `Array.from` 메서드로 생성할 수 있다. 배열의 생성자 함수는 `Array`이며, 배열의 프로토타입 객체는 `Array.prototype`이다. `Array.prototype`은 배열을 위한 빌트인 메서드를 제공한다.

```javascript
const arr = [1, 2, 3];

arr.constructor === Array // true
Object.getPrototypeOf(arr) === Array.prototype // true
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/360eafae-0416-49f7-b94f-05af973769cc)

일반 객체와 배열을 구분하는 가장 명확한 차이는 '값의 순서'와 '`length`' 프로퍼티다.

- 처음부터 순차적으로 요소에 접근 가능
- 역순으로 요소에 접근도 가능
- 특정 위치부터 순차적 접근 가능

## 자바스크립트 배열은 배열이 아니다.
자료구조에서 말하는 배열은 **밀집 배열**이다.
- 하나의 데이터 타입으로 통일되어 있으며 서로 연속적으로 인접해 있다.
- 인덱스를 통해 단 한 번의 연산으로 임의의 요소에 접근할 수 있다. `O(1)`
- `검색 대상 요소의 메모리 주소 = 배열의 시작 메모리 주소 + 인덱스 * 요소의 바이트 수`
- 정렬되지 않은 배열에서 특정 요소를 검색하는 경우 차례대로 검색해야 한다. `O(n)`
- 배열에 요소를 삽입하거나 삭제하는 경우 배열의 요소를 연속적으로 유지하기 위해 요소를 이동시켜야 하는 단점도 있다.

JavaScript의 배열은 자료구조에서 말하는 일반적인 의미의 배열과 다르다. **즉, 배열의 요소를 위한 각각의 메모리 공간은 동일한 크기를 갖지 않아도 되며, 연속적으로 이어져 있지 않을 수도 있다.** 희소 배열이다.

**JavaScript의 배열은 일반적인 배열의 동작을 흉내 낸 특수한 객체다.**

```javascript
console.log(Object.getOwnPropertyDescriptors([1, 2, 3]));
/*
{
  '0': {value: 1, writable: true, enumerable: true, configurable: true}
  '1': {value: 2, writable: true, enumerable: true, configurable: true}
  '2': {value: 3, writable: true, enumerable: true, configurable: true}
  length: {value: 3, writable: true, enumerable: false, configurable: false}
}
*/
```

JavaScript 배열은 인덱스를 나타내는 문자열을 프로퍼티 키로 가지며, `length` 프로퍼티를 갖는 특수한 객체다. JavaScript 배열의 요소는 사실 프로퍼티 값이다. JavaScript에서 사용할 수 있는 모든 값은 객체의 프로퍼티 값이 될 수 있으므로 어떤 타입의 값이라도 배열의 요소가 될 수 있다.

```javascript
const arr = [
  'string',
  10,
  true,
  null,
  undefined,
  NaN,
  Infinity,
  [ ],
  { },
  function () {}
];
```

- 일반적인 배열은 인덱스로 요소에 빠르게 접근할 수 있다. 하지만 요소를 삽입 또는 삭제하는 경우엔 효율적이지 않다.
- JavaScript 배열은 해시 테이블로 구현된 객체이므로 인덱스로 요소에 접근하는 경우 일반적인 배열보다 성능적인 면에서 느릴 수밖에 없는 구조적인 단점이 있다. 하지만 요소를 삽입 또는 삭제하는 경우 일반적인 배열보다 빠른 성능을 기대할 수 있다.

인덱스로 배열 오소에 접근할 때 일반적인 배열보다 느릴 수 밖에 없는 구조적인 단점을 보완하기 위해 대부분의 모던 JavaScript 엔진은 배열을 일반 객체와 구별하여 좀 더 배열처럼 동작하도록 최적화하여 구현했다.

## length 프로퍼티와 희소 배열

```javascript
[].length // 0
[1, 2, 3].length // 3
```

- `length` 프로퍼티의 값은 0과 2^32 - 1 미만의 양의 정수다.
- `length` 프로퍼티의 값은 배열에 요소를 추가하거나 삭제하면 자동 갱신된다.

```javascript
const arr = [1, 2, 3];
console.log(arr.length); // 3

arr.push(4);
console.log(arr.length); // 4

// 현재 length 프로퍼티 값보다 작은 숫자를 length 프로퍼티에 할당
arr.length = 1;
// 길이가 줄어든다.
console.log(arr); // [1]
```

주의할 것은 현재 `length` 값보다 큰 숫자 값을 할당하는 경우다. 이때 값은 변경되지만 실제로 배열의 길이가 늘어나진 않는다.

```javascript
const arr = [1];

arr.length = 3;

// length 프로퍼티 값은 변경되지만 실제로 배열의 길이가 늘어나진 않는다.
console.log(arr.length); // 3
console.log(arr); // [1, empty * 2]
```

`empty * 2`는 실제로 추가된 배열의 요소가 아니라서 값이 존재하지 않는다. 값 없이 비어 있는 요소를 위해 메모리 공간을 확보하지 않으며 빈 요소를 생성하지도 않는다.

```javascript
console.log(Object.getOwnPropertyDescriptors(arr));
/*
{
  '0': {value: 1, writable: true, enumerable: true, configurable: true},
  length: {value: 3, writable: true, enumerable: false, configurable: false}
}
*/
```

배열의 요소가 연속적으로 위치하지 않고 일부가 비어 있는 배열을 희소 배열이라 한다. JavaScript는 희소 배열을 문법적으로 허용한다.

```javascript
const sparse = [, 2, , 4];

console.log(sparse.length); // 4
console.log(sparse); // [empty, 2, empty, 4]

console.log(Object.getOwnPropertyDescriptors(sparse));
/*
{
  '1': { value: 2, writable: true, enumerable: true, configurable: true },
  '3': { value: 4, writable true, enumerable: true, configurable: true },
  'length': { value: 4, writable true, enumerable: false, configurable: false }
}
*/
```

**희소 배열은 `length`와 배열 요소의 개수가 일치하지 않는다. 희소 배열의 `length`는 희소 배열의 실제 요소 개수보다 언제나 크다.**

JavaScript는 문법적으로 희소 배열을 허용하지만 희소 배열은 사용하지 않는 것이 좋다. 의도적으로 희소 배열을 만들어야 하는 상황은 발생하지 않는다. 희소 배열은 연속적인 값의 집합이라는 배열의 기본적인 개념과 맞지 않으며, 성능에도 좋지 않은 영향을 준다. 최적화가 잘 되어 있는 모던 JavaScript 엔진은 요소의 타입이 일치하는 배열을 생성할 때 일반적인 의미의 배열처럼 연속된 메모리 공간을 확보하는 것으로 알려져 있다.

왠만하면 사용하지말고 **배열에는 같은 타입의 요소를 연속적으로 위치시키는 것이 최선이다.**

## 배열 생성
### 배열 리터럴
배열 리터럴은 0개 이상의 요소를 쉼표로 구분하여 대괄호로 묶는다. 객체 리터럴과 달리 프로퍼티 키가 없고 값만 존재한다.

```javascript
const arr = [1, 2, 3];
console.log(arr.length);
```

### Array 생성자 함수
`Object` 생성자 함수를 통해 객체를 생성할 수 있듯이 `Array` 생성자 함수를 통해 배열을 생성할 수도 있다.  
`Array` 생성자 함수는 전달된 인수의 개수에 따라 다르게 동작하므로 주의가 필요하다.

```javascript
const arr = new Array(10);

console.log(arr); // [empty * 10]
console.log(arr.length); // 10

console.log(Object.getOwnPropertyDescriptors(arr));
/*
{
  length: {value: 10, writable: true, enumerable: false, configurable: false}
}
*/

new Array(4294967295); // 최대 length
new Array(4294967296); // RangeError: Invalid array length
new Array(-1); // RangeError: Invalid array length

new Array(); // []
new Array({});  // [{}]
```

`Array` 생성자 함수는 `new` 연산자와 함께 호출하지 않더라도, 즉 일반 함수로서 호출해도 배열을 생성하는 생성자 함수로 동작한다. 이는 `Array` 생성자 함수 내부에서 `new.target`을 확인하기 때문이다.

### Array.of
ES6에서 도입된 `Array.of` 메서드는 전달된 인수를 요소로 갖는 배열을 생성한다. `Array.of`는 `Array` 생성자 함수와 다르게 전달된 인수가 1개이고 숫자이더라도 인수를 요소로 갖는 배열을 생성한다.

```javascript
Array.of(1); // [1]
Array.of(1, 2, 3); // [1, 2, 3]
Array.of('string'); // ['string']
```

### Array.from
ES6에서 도입된 `Array.from` 메서드는 유사 배열 객체 또는 이터러블 객체를 인수로 전달받아 배열로 변환하여 반환한다.

```javascript
// 유사 배열 객체를 변환하여 배열 생성
Array.from({ length: 2, 0: 'a', 1: 'b' }); // ['a', 'b']

// 이터러블을 변환하여 배열 생성. 문자열은 이터러블.
Array.from('Hello'); // ['H', 'e', 'l', 'l', 'o']
```

`Array.from`을 사용하면 두 번째 인수로 전달한 콜백 함수를 통해 값을 만들면서 요소를 채울 수 있다. `Array.from` 메서드는 두 번째 인수로 전달한 콜백 함수에 첫 번째 인수에 의해 생성된 배열의 요소값과 인덱스를 순차적으로 전달하면서 호출하고, 콜백 함수의 반환값으로 구성된 배열을 반환한다.

```javascript
Array.from({ length: 3 }); // [undefined, undefined, undefined]

// 두 번째 인수로 전달한 콜백 함수의 반환값으로 구성된 배열을 반환한다.
Array.from({ length: 3 }, (_, i) => i); // [0, 1, 2]
```

## 배열 요소의 참조
대괄호 표기법을 사용하며 내부에는 인덱스가 와야 한다. 정수로 평가되는 표현식이라면 인덱스 대신 사용할 수 있다. 인덱스는 값을 참조할 수 있다는 의미에서 객체의 프로퍼티 키와 같은 역할을 한다.

```javascript
const arr = [1, 2];

console.log(arr[0]); // 1

console.log(arr[2]); // undefined
```

배열은 사실 인덱스를 나타내는 문자열을 프로퍼티 키로 갖는 객체다. 따라서 존재하지 않는 프로퍼티 키로 객체의 프로퍼티에 접근했을 때 `undefined`를 반환하는 것처럼 배열도 존재하지 않는 요소를 참조하면 `undefined`를 반환한다.

같은 이유로 희소 배열의 존재하지 않는 요소를 참조해도 `undefined`가 반환된다.

```javascript
const arr = [1, , 3];

// 배열 arr에는 인덱스가 1인 요소가 존재하지 않는다.
console.log(Object.getOwnPropertyDescriptors(arr));
/*
{
  '0': {value: 1, writable: true, enumerable: true, configurable: true},
  '2': {value: 3, writable: true, enumerable: true, configurable: true},
  'length': {value: 3, writable: true, enumerable: false, configurable: false}
}
*/

console.log(arr[1]); // undefined
console.log(arr[3]); // undefined
```

## 배열 요소의 추가와 갱신
객체에 프로퍼티를 동적으로 추가하듯 배열에도 요소를 동적으로 추가할 수 있다. 존재하지 않는 인덱스를 사용해 값을 할당하면 새로운 요소가 추가된다. 이때 `length` 프로퍼티 값은 자동 갱신된다.

```javascript
const arr = [0];

// 배열 요소의 추가
arr[1] = 1;

console.log(arr); // [0, 1]
console.log(arr.length); // 2
```

만약 현재 배열의 `length` 프로퍼티 값보다 큰 인덱스로 새로운 요소를 추가하면 희소 배열이 된다.

```javascript
arr[100] = 100;

console.log(arr); // [0, 1, empty * 98, 100]
console.log(arr.length); // 101
```

이때 인덱스로 요소에 접근하여 명시적으로 값을 할당하지 않은 요소는 생성되지 않는다는 것에 주의하자.

```javascript
// 명시적으로 값을 할당하지 않는 요소는 생성되지 않는다.
console.log(Object.getOwnPropertyDescriptors(arr));
/*
{
  '0': {value: 0, writable: true, enumerable: true, configurable: true},
  '1': {value: 1, writable: true, enumerable: true, configurable: true},
  '100': {value: 100, writable: true, enumerable: true, configurable: true},
  'length': {value: 101, writable: true, enumerable: false, configurable: false}
}
*/

// 요소 값 갱신
arr[1] = 10;
console.log(arr); // [0, 10, empty * 98, 100]
```

인덱스는 요소의 위치를 나타내므로 반드시 0 이상의 정수(또는 정수 형태의 문자열)를 사용해야 한다. 만약 정수 이외의 값을 인덱스처럼 사용하면 요소가 생성되는 것이 아니라 프로퍼티가 생성된다. 이때 추가된 프로퍼티는 `length` 프로퍼티 값에 영향을 주지 않는다.

```javascript
const arr = [];

arr[0] = 1;
arr['1'] = 2;

arr['foo'] = 3;
arr.bar = 4;
arr[1.1] = 5;
arr[-1] = 6;

console.log(arr); // [1, 2, foo: 3, bar: 4, '1.1': 5, '-1': 6]
console.log(arr.length); // 2
```

## 배열 요소의 삭제
배열은 사실 객체이기 때문에 배열의 특정 요소를 삭제하기 위해 `delete` 연산자를 사용할 수 있다.

```javascript
const arr = [1, 2, 3];

delete arr[1];
console.log(arr); // [1, empty, 3]

// length 프로퍼티에 영향을 주지 않는다. 희소 배열이 된다.
console.log(arr.length); // 3
```

`delete` 연산자는 객체의 프로퍼티를 삭제한다. 위 예제의 `delete arr[1]`은 `arr`에서 프로퍼티 키가 `'1'`인 프로퍼티를 삭제한다. 이때 배열은 희소 배열이 되며 `length` 프로퍼티 값은 변하지 않는다. 따라서 희소 배열을 만드는 `delete` 연산자는 사용하지 않는 것이 좋다.

희소 배열을 만들지 않으면서 배열의 특정 요소를 완전히 삭제하려면 `Array.prototype.splice` 메서드를 사용한다.

```javascript
const arr = [1, 2, 3];

// Array.prototype.splice(삭제를 시작할 인덱스, 삭제할 요소 수)
arr.splice(1, 1);
console.log(arr); // [1, 3]

// length 프로퍼티가 자동 갱신된다.
console.log(arr.length); // 2
```

## 배열 메서드
JavaScript는 배열을 다룰 때 유용한 다양한 빌트인 메서드를 제공한다. `Array` 생성자 함수는 정적 메서드를 제공하며, 배열 객체의 프로토타입인 `Array.prototype`은 프로토타입 메서드를 제공한다. 배열은 사용 빈도가 높은 자료구조이므로 배열 메서드의 사용법을 잘 알아둘 필요가 있다.

배열 메서드는 결과물을 반환하는 패턴이 두 가지이므로 주의가 필요하다.

- **배열에는 원본 배열(배열 메서드를 호출한 배열, 즉 배열 메서드의 구현체 내부에서 `this`가 가리키는 객체)을 직접 변경하는 메서드**
- **원본 배열을 직접 변경하지 않고 새로운 배열을 생성하여 반환하는 메서드**

```javascript
const arr = [1];

// 원본 배열을 직접 변경함
arr.push(2);
console.log(arr); // [1, 2]

// 새로운 배열을 생성해서 반환
const result = arr.concat(3);
console.log(arr); // [1, 2]
console.log(result); // [1, 2, 3]
```

ES5부터 도입된 배열 메서드는 대부분 원본 배열을 직접 변경하지 않지만 초창기 배열 메서드는 원본 배열을 직접 변경하는 경우가 많다. 원본 배열을 직접 변경하는 메서드는 외부 상태를 직접 변경하는 부수 효과가 있으므로 사용할 때 주의해야 한다. **따라서 가급적 원본 배열을 직접 변경하지 않는 메서드를 사용하는 편이 좋다.**

### Array.isArray
`Array.isArray`는 `Array` 생성자 함수의 정적 메서드다.

전달된 인수가 배열이면 `true`, 아니면 `false`를 반환한다.

```javascript
// true
Array.isArray([]);
Array.isArray([1, 2]);
Array.isArray(new Array());

// false
Array.isArray();
Array.isArray({});
Array.isArray(null);
Array.isArray(undefined);
Array.isArray(1);
Array.isArray('Array');
Array.isArray(true);
Array.isArray(false);
Array.isArray({ 0: 1, length: 1 });
```

### Array.prototype.indexOf
원본 배열에서 인수로 전달된 요소를 검색하여 인덱스를 반환한다.

- 중복되는 요소가 여러 개 있다면 첫 번째로 검색된 요소의 인덱스를 반환
- 요소가 존재하지 않으면 -1을 반환

```javascript
const arr = [1, 2, 2, 3];

arr.indexOf(2); // 1
arr.indexOf(4); // -1
// 두 번째 인수는 검색을 시작할 인덱스다. 생략하면 처음부터 검색한다.
arr.indexOf(2, 2); // 2
```

배열에 특정 요소가 존재하는지 확인할 때 유용하다.

`indexOf` 메서드 대신 ES7에서 도입된 `Array.prototype.includes` 메서드를 사용하면 가독성이 더 좋다.

```javascript
const foods = ['apple', 'banana', 'orange'];

if (!foods.includes('orange')) {
  foods.push('orange');
}

console.log(foods);
```

### Array.prototype.push
`push` 메서드는 인수로 전달받은 모든 값을 원본 배열의 마지막 요소로 추가하고 변경된 `length` 프로퍼티 값을 반환한다. `push` 메서드는 원본 배열을 직접 변경한다.

```javascript
const arr = [1, 2];

let result = arr.push(3, 4);
console.log(result); // 4

console.log(arr); // [1, 2, 3, 4]
```

`push` 메서드는 성능 면에서 좋지 않다. 마지막 요소로 추가할 요소가 하나뿐이라면 이를 사용하지 않고 `length` 프로퍼티를 사용하여 배열의 마지막에 요소를 직접 추가할 수도 있다. 이 방법이 더 빠르다.

```javascript
const arr = [1, 2];

arr[arr.length] = 3;
console.log(arr); // [1, 2, 3]
```

`push` 메서드는 원본 배열을 직접 변경하는 부수 효과가 있다. 따라서 `push` 메서드보다는 ES6의 스프레드 문법을 사용하는 편이 좋다. 이는 함수 호출 없이 표현식으로 마지막에 요소를 추가할 수 있으며 부수 효과도 없다.

```javascript
const arr = [1, 2];

// ES6 스프레드 문법
const newArr = [...arr, 3];
console.log(newArr); // [1, 2, 3]
```

### Array.prototype.pop
`pop` 메서드는 원본 배열에서 마지막 요소를 제거하고 제거한 요소를 반환한다. 원본 배열이 빈 배열이면 `undefined`를 반환한다. `pop` 메서드는 원본 배열을 직접 변경한다.

```javascript
const arr = [1, 2];

let result = arr.pop();
console.log(result); // 2

// pop 메서드는 원본 배열을 직접 변경한다.
console.log(arr); // [1]
```

```javascript
class Stack {
  #array;

  constructor(array = []) {
    if (!Array.isArray(array)) {
      throw new TypeError(`${array} is not an array.`);
    }
    this.#array = array;
  }

  // 스택의 가장 마지막에 데이터를 밀어 넣는다.
  push(value) {
    return this.#array.push(value);
  }

  // 스택의 가장 마지막 데이터를 꺼낸다.
  pop() {
    return this.#array.pop();
  }

  // 스택의 복사본 배열을 반환한다.
  entries() {
    return [...this.#array];
  }
}

const stack = new Stack([1, 2]);
console.log(stack.entries()); // [1, 2]

stack.push(3);
console.log(stack.entries()); // [1, 2, 3]

stack.pop();
console.log(stack.entries()); // [1, 2]
```

### Array.prototype.unshift
인수로 전달받은 모든 값을 원본 배열의 선두에 요소로 추가하고 변경된 `length` 프로퍼티 값을 반환한다. `unshift`는 원본 배열을 직접 변경한다.

```javascript
const arr = [1, 2];

let result = arr.unshift(3, 4);
console.log(result); // 4

console.log(arr); // [3, 4, 1, 2]
```

`unshift`는 원본 배열을 직접 변경하는 부수 효과가 있다. 따라서 ES6의 스프레드 문법을 사용하는 편이 좋다.

```javascript
const arr = [1, 2];

const newArr = [3, ...arr];
console.log(newArr); // [3, 1, 2]
```

### Array.prototype.shift
`shift`는 원본 배열에서 첫 번째 요소를 제거하고 제거한 요소를 반환한다. 원본 배열이 빈 배열이면 `undefined`를 반환한다. `shift` 메서드는 원본 배열을 직접 변경한다.

```javascript
const arr = [1, 2];

let result = arr.shift();
console.log(result); // 1

console.log(arr); // [2]
```

```javascript
const Queue = (function () {
  function Queue(array = []) {
    if (!Array.isArray(array)) {
      throw new TypeError(`${array} is not an array.`);
    }
    this.array = array;
  }

  Queue.prototype = {
    constructor: Queue,

    enqueue(value) {
      return this.array.push(value);
    },

    dequeue() {
      return this.array.shift();
    },

    entries() {
      return [...this.array];
    }
  };

  return Queue;
}());

const queue = new Queue([1, 2]);
console.log(queue.entries()); // [1, 2]

queue.enqueue(3);
console.log(queue.entries()); // [1, 2, 3]

queue.dequeue()
console.log(queue.entries()); // [2, 3]
```

### Array.prototype.concat
`concat` 메서드는 인수로 전달된 값들(배열 또는 원시값)을 원본 배열의 마지막 요소로 추가한 새로운 배열을 반환한다. 인수로 전달한 값이 배열인 경우 배열을 해체하여 새로운 배열의 요소로 추가한다. 원본 배열은 변경되지 않는다.

```javascript
const arr1 = [1, 2];
const arr2 = [3, 4];

let result = arr1.concat(arr2);
console.log(result); // [1, 2, 3, 4]

result = arr1.concat(3);
console.log(result); // [1, 2, 3]

result = arr1.concat(arr2, 5);
console.log(result); // [1, 2, 3, 4, 5]

console.log(arr1); // [1, 2]
```

- `push`, `unshift` 메서드는 원본 배열을 직접 변경하지만 `concat` 메서드는 원본 배열을 변경하지 않고 새로운 배열을 반환한다.
- 인수로 전달받은 값이 배열인 경우 `push`와 `unshift`는 배열을 그대로 원본 배열의 마지막/첫 번째 요소로 추가하지만 `concat` 메서드는 인수로 전달받은 배열을 해체하여 새로운 배열의 마지막 요소로 추가한다.

`concat` 메서드는 ES6의 스프레드 문법으로 대체할 수 있다.

```javascript
let result = [1, 2].concat([3, 4]);
console.log(result); // [1, 2, 3, 4]

result = [...[1, 2], ...[3, 4]];
console.log(result); // [1, 2, 3, 4]
```

`push`, `unshift` 메서드와 `concat` 메서드를 사용하는 대신 스프레드 문법을 일관성 있게 사용하는 것을 권장한다.

### Array.prototype.splice
원본 배열의 중간에 요소를 추가하거나 중간에 있는 요소를 제거하는 경우 `splice` 메서드를 사용한다. 이 메서드는 3개의 매개변수가 있으며 원본 배열을 직접 변경한다.

- `start`: 원본 배열의 요소를 제거하기 시작할 인덱스다. `start`만 지정하면 원본 배열의 `start`부터 모든 요소를 제거한다. 음수인 경우 배열의 끝에서의 인덱스를 나타낸다.
- `deleteCount`: 원본 배열의 요소를 제거하기 시작할 인덱스인 `start`부터 제거할 요소의 개수. 0인 경우 아무런 요소도 제거되지 않는다.(옵션)
- `items`: 제거한 위치에 삽입할 요소들의 목록. 생략할 경우 원본 배열에서 요소들을 제거하기만 한다.(옵션)

```javascript
const arr = [1, 2, 3, 4];

const result = arr.splice(1, 2, 20, 30);

// 제거한 요소가 배열로 반환된다.
console.log(result); // [2, 3]
// 원본 배열을 직접 변경
console.log(arr); // [1, 20, 30, 4]
```

```javascript
const arr = [1, 2, 3, 4];

const result = arr.splice(1, 0, 100);

console.log(arr); // [1, 100, 2, 3, 4]
console.log(result); // []
```

```javascript
const arr = [1, 2, 3, 4];

const result = arr.splice(1, 2);

console.log(arr); // [1, 4]
console.log(result); // [2, 3]
```

```javascript
const arr = [1, 2, 3, 4];
const result = arr.splice(1);

console.log(arr);  // [1]
console.log(result);  // [2, 3, 4]
```

배열에서 특정 요소를 제거하려면 `indexOf` 메서드를 통해 특정 요소의 인덱스를 취득한 다음 `splice` 메서드를 사용한다.

`filter` 메서드를 사용하여 특정 요소를 제거할 수도 있다. 하지만 특정 요소가 중복된 경우 모두 제거된다.

```javascript
const arr = [1, 2, 3, 1, 2];

function removeAll(array, item) {
  return array.filter(v => v !== item);
}

console.log(removeAll(arr, 2)); // [1, 3, 1]
```

### Array.prototype.slice
`slice`는 인수로 전달된 범위의 요소들을 복사하여 배열로 반환한다. 원본 배열은 변경되지 않는다.

- `start`: 복사를 시작할 인덱스. 음수인 경우 배열의 끝에서의 인덱스를 나타낸다.
- `end`: 복사를 종료할 인덱스. 이 인덱스에 해당하는 요소는 복사되지 않는다. 생략 시 기본 값은 `length` 값이다.

```javascript
const arr = [1, 2, 3];

arr.slice(0, 1); // [1]
arr.slice(1, 2); // [2]
console.log(arr); // [1, 2, 3]
```

```javascript
const arr = [1, 2, 3];

const copy = arr.slice();
console.log(copy); // [1, 2, 3]
console.log(copy === arr); // false
```

이때 생성된 복사본은 얕은 복사를 통해 생성된다.

```javascript
const todos = [
  { id: 1, content: 'HTML', completed: false },
  { id: 2, content: 'CSS', completed: false },
  { id: 3, content: 'JavaScript', completed: false },
];

const _todos = todos.slice();
// const _todos = [...todos];

console.log(_todos === todos); // false
console.log(_todos[0] === todos[0]); // true
```

### Array.prototype.join
`join` 메서드는 원본 배열의 모든 요소를 문자열로 변환한 후, 인수로 전달받은 문자열, 즉 구분자로 연결한 문자열을 반환한다.

```javascript
const arr = [1, 2, 3, 4];

// 기본 구분자는 콤마
arr.join(); // '1,2,3,4'

arr.join(''); // '1234'

arr.join(':'); // '1:2:3:4'
```

### Array.prototype.reverse
`reverse` 메서드는 원본 배열의 순서를 반대로 뒤집는다. 이때 원본 배열이 변경된다. 반환값은 변경된 배열이다.

```javascript
const arr = [1, 2, 3];
const result = arr.reverse();

console.log(arr); // [3, 2, 1]
console.log(result); // [3, 2, 1]
```

### Array.prototype.fill
ES6에서 도입된 `fill` 메서드는 인수로 전달받은 값을 배열의 처음부터 끝까지 요소로 채운다. 이때 원본 배열이 변경된다.

```javascript
const arr = [1, 2, 3];

arr.fill(0);

console.log(arr); // [0, 0, 0]
```

- 두 번째 인수로 요소 채우기를 시작할 인덱스를 전달할 수 있다.
- 세 번째 인수로 요소 채우기를 멈출 인덱스를 전달할 수 있다.

### Array.prototype.includes
ES7에서 도입된 `includes` 메서드는 배열 내의 특정 요소가 포함되어 있는지 확인하여 `true` 또는 `false`를 반환한다. 첫 번째 인수로 검색할 대상을 지정한다.

```javascript
const arr = [1, 2, 3];

arr.includes(2); // true
```

- 두 번째 인수로 검색을 시작할 인덱스를 전달할 수 있다. 생략할 경우 기본값 0이 설정된다.
- 두 번째 인수에 음수를 전달하면 `length` 프로퍼티 값과 음수 인덱스를 합산하여 검색 시작 인덱스를 설정한다.
- `indexOf` 메서드를 사용하면 반환값이 -1인지 확인해봐야 하고 배열에 `NaN`이 포함되어 있는지 확인할 수 없다는 문제가 있다.

### Array.prototype.flat
ES10에서 도입된 `flat` 메서드는 인수로 전달한 깊이만큼 재귀적으로 배열을 평탄화한다.

```javascript
[1, [2, 3, 4, 5]].flat(); // [1, 2, 3, 4, 5]
```

중첩 배열을 평탄화할 깊이를 인수로 전달할 수 있다. 인수를 생략할 경우 기본값은 1이다. 인수로 `Infinity`를 전달하면 중첩 배열 모두를 평탄화한다.

```javascript
[1, [2, [3, [4]]]].flat(); // [1, 2, [3, [4]]]
[1, [2, [3, [4]]]].flat(1); // [1, 2, [3, [4]]]

// 2단계 깊이까지 평탄화한다.
[1, [2, [3, [4]]]].flat(2); // [1, 2, 3, [4]]
[1, [2, [3, [4]]]].flat().flat(); // [1, 2, 3, [4]]

[1, [2, [3, [4]]]].flat(Infinity); // [1, 2, 3, 4]
```

## 배열 고차 함수
