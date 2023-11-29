# 21장. 빌트인 객체
## 자바스크립트 객체의 분류
- 표준 빌트인 객체: ECMAScript 사양에 정의된 객체. 애플리케이션 전역의 공통 기능을 제공한다. 전역 객체의 프로퍼티로서 제공되어 별도의 선언 없이 전역 변수처럼 언제나 참조할 수 있다.
- 호스트 객체: ECMAScript 사양에 정의되어 있지 않지만 JavaScript 실행 환경에서 추가로 제공하는 객체. (DOM, XMLHttpRequest, ...)
- 사용자 정의 객체: 기본 제공되는 객체가 아닌 사용자가 직접 정의한 객체

## 표준 빌트인 객체
JavaScript는 `Object`, `String`, `Number`, `Boolean` 등 40여 개의 표준 빌트인 객체를 제공한다.

`Math`, `Reflect`, `JSON`을 제외한 표준 빌트인 객체는 모두 인스턴스를 생성할 수 있는 생성자 함수 객체다. 생성자 함수 객체인 표준 빌트인 객체는 프로토타입 메서드와 정적 메서드를 제공하고 아닌 표준 빌트인 객체는 정적 메서드만 제공한다.

```javascript
// String 생성자 함수에 의한 String 객체 생성
const strObj = new String('Ramos'); // String {"Ramos"}
console.log(typeof strObj); // object

// Number 생성자 함수에 의한 Number 객체 생성
const numObj = new Number(123); // Number {123}
console.log(typeof numObj); // object

// Boolean 생성자 함수에 의한 Boolean 객체 생성
const boolObj = new Boolean('Ramos'); // Boolean {true}
console.log(typeof boolObj); // object

// Function 생성자 함수에 의한 Function 객체(함수) 생성
const func = new Function('x', 'return x * x'); // f anonymous(x )
console.log(typeof func); // function

// Array 생성자 함수에 의한 Array 객체(배열) 생성
const arr = new Array(1, 2, 3); // (3) [1, 2, 3]
console.log(typeof arr); // object

// RegExp 생성자 함수에 의한 RegExp 객체(정규 표현식) 생성
const regExp = new RegExp(/ab+c/i); // /ab+c/i
console.log(typeof regExp); // object

// Date 생성자 함수에 의한 Date 객체 생성
const date = new Date(); // Wed Nov 29 2023 14:27:36 GMT+0900 (한국 표준시)
console.log(typeof date); // object
```

**생성자 함수인 표준 빌트인 객체가 생성한 인스턴스의 프로토타입은 표준 빌트인 객체의 `prototype` 프로퍼티에 바인딩된 객체다.** 예를 들어, 표준 빌트인 객체인 `String`을 생성자 함수로서 호출하여 생성한 `String` 인스턴스의 프로토타입은 `String.prototype`이다.

```javascript
const strObj = new String('Ramos'); // String {"Ramos"}
console.log(Object.getPrototypeOf(strObj) === String.prototype); // true
```

- **표준 빌트인 객체의 `prototype` 프로퍼티에 바인딩된 객체는 다양한 기능의 빌트인 프로토타입 메서드를 제공한다.**
- **표준 빌트인 객체는 인스턴스 없이도 호출 가능한 빌트인 정적 메서드를 제공한다.**

```javascript
const numObj = new Number(1.5); // Number {1.5}

// toFixed는 Number.prototype의 프로토타입 메서드다.
console.log(numObj.toFixed()); // 2

// isInteger는 Number의 정적 메서드다.
console.log(Number.isInteger(0.5)); // false
```

## 원시값과 래퍼 객체
원시값이 있는데도 문자열, 숫자, 불리언 객체를 생성하는 `String`, `Number`, `Boolean` 등의 표준 빌트인 생성자 함수가 존재하는 이유는 무엇일까?

```javascript
// 원시값은 객체가 아니므로 프로퍼티나 메서드를 가질 수 없는데도 원시값인 문자열이 마치 객체처럼 동작함.
const str = 'hello';

// 원시 타입인 문자열이 프로퍼티와 메서드를 갖고 있는 객체처럼 동작한다.
console.log(str.length); // 5
console.log(str.toUpperCase()); // HELLO
```

위 예제처럼 원시 값이 문자열, 숫자, 불리언 값의 경우 이들 원시값에 대해 마치 객체처럼 마침표 표기법으로 접근하면 JavaScript 엔진이 일시적으로 원시값을 연관된 객체로 변환해주기 때문이다. **즉, 원시값을 객체처럼 사용하면 JavaScript 엔진은 암묵적으로 연관된 객체를 생성하여 생성된 객체로 프로퍼티에 접근하거나 메서드를 호출하고 다시 원시값으로 되돌린다.**

문자열에 대해 마침표 표기법으로 접근하면 그 순간 래퍼 객체인 `String` 생성자 함수의 인스턴스가 생성되고 문자열은 래퍼 객체의 `[[StringData]]` 내부 슬롯에 할당된다.

```javascript
const str = 'hi';

// 원시 타입인 문자열이 래퍼 객체인 String 인스턴스로 변환된다.
console.log(str.length); // 2
console.log(str.toUpperCase()); // HI

// 래퍼 객체로 프로퍼티에 접근하거나 메섣르르 호출한 후, 다시 원시값으로 되돌린다.
console.log(typeof str); // string
```

이때 문자열 래퍼 객체인 `String` 생성자 함수의 인스턴스는 `String.prototype`의 메서드를 상속받아 사용할 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9ea1678d-dd06-440e-b07c-e490825d695d)

**그 후 래퍼 객체의 처리가 종료되면 래퍼 객체의 `[[StringData]]` 내부 슬롯에 할당된 원시값으로 원래의 상태, 즉 식별자가 원시값을 갖도록 되돌리고 래퍼 객체는 GC의 대상이 된다.**

```javascript
// 1. 식별자 str은 문자열을 값으로 가지고 있다.
const str = 'hello';

// 2. 식별자 str은 암묵적으로 생성된 래퍼 객체를 가리킨다.
// 식별자 str의 값 'hello'는 래퍼 객체의 [[StringData]] 내부 슬롯에 할당된다.
// 래퍼 객체에 name 프로퍼티가 동적 추가된다.
str.name = 'Ramos';

// 3. 식별자 str은 다시 원래의 문자열, 즉 래퍼 객체의 [[StringData]] 내부 슬롯에 할당된 원시값을 갖는다.
// 이때 2에서 생성된 래퍼 객체는 아무도 참조하지 않는 상태이므로 GC 대상이 된다.

// 4. 식별자 str은 새롭게 암묵적으로 생성된(2에서 생성된 래퍼 객체와는 다른) 래퍼 객체를 가리킨다.
// 새롭게 생성된 래퍼 객체에는 name 프로퍼티가 존재하지 않는다.
console.log(str.name); // undefined

// 5. 식별자 str은 다시 원래의 문자열, 즉 래퍼 객체의 [[StringData]] 내부 슬롯에 할당된 원시값을 갖는다.
// 이때 4에서 생성된 래퍼 객체는 아무도 참조하지 않는 상태이므로 GC 대상이 된다.
console.log(typeof str, str); // string hello
```

숫자 값, 불리언 값도 마찬가지지만 불리언 값으로 메서드를 호출하는 경우는 없으므로 그다지 유용하진 않다.

ES6에서 도입된 원시값인 심벌도 래퍼 객체를 생성한다. 심벌은 일반적인 원시값과는 달리 리터럴 표기법으로 생성할 수 없고 `Symbol` 함수를 통해 생성해야 하므로 다른 원시값과는 차이가 있다. 추후 살펴보자.

이처럼 문자열, 숫자, 불리언, 심벌은 암묵적으로 생성되는 래퍼 객체에 의해 마치 객체처럼 사용할 수 있으며, 표준 빌트인 객체인 `String`, `Number`, `Boolean`, `Symbol`의 프로토타입 메서드 또는 프로퍼티를 참조할 수 있다. 따라서 `String`, `Number`, `Boolean` 생성자 함수를 `new` 연산자와 함께 호출하여 문자열, 숫자, 불리언 인스턴스를 생성할 필요가 없으며 권장하지도 않는다.

문자열, 숫자, 불리언, 심벌 이외의 원시값, 즉 `null`과 `undefined`는 래퍼 객체를 생성하지 않는다. 따라서 이를 객체처럼 사용하면 에러가 발생한다.

## 전역 객체
**전역 객체는 코드가 실행되기 이전 단계에 JavaScript 엔진에 의해 어떤 객체보다도 먼저 생성되는 특수한 객체이며, 어떤 객체에도 속하지 않은 최상위 객체다.**

JavaScript 환경에 따라 지칭하는 이름이 제각각이다. 브라우저 환경에서는 `window`(또는 `self`, `this`, `frames`), Node.js 환경에선 `global`이 전역 객체를 가리킨다.

전역 객체는 표준 빌트인 객체와 환경에 따른 호스트 객체, 그리고 `var` 키워드로 선언한 전역 변수와 전역 함수를 프로퍼티로 갖는다.

전역 객체가 최상위 객체라는 것은 프로토타입 상속 관계상에서 최상위 객체라는 의미가 아니다. 전역 객체 자신은 어떤 객체의 프로퍼티도 아니며 객체의 계층적 구조상 표준 빌트인 객체와 호스트 객체를 프로퍼티로 소유한다는 것을 말한다.

특징은 다음과 같다.

- 전역 객체는 개발자가 의도적으로 생성할 수 없다. 즉, 전역 객체를 생성할 수 있는 생성자 함수가 제공되지 않는다.
- 전역 객체의 프로퍼티를 참조할 때 `window`(또는 `global`)를 생략할 수 있다.
- 전역 객체는 `Object`, `String`, `Number`, `Boolean`, `Function` 등과 같은 표준 빌트인 객체를 프로퍼티로 가지고 있다.

```javascript
window.parseInt('F', 16); // 15
```

- JavaScript 실행 환경에 따라 추가적으로 프로퍼티와 메서드를 갖는다.
  - 브라우저 환경 : DOM, BOM, Canvas, `XMLHttpRequest`, `fetch`, `requestAnimationFrame`, ...
  - Node.js 환경 : Node.js 고유의 API를 호스트 객체로 제공
- `var` 키워드로 선언한 전역 변수와 선언하지 않은 변수에 값을 할당한 암묵적 전역, 그리고 전역 함수는 전역 객체의 프로퍼티가 된다.

```javascript
var foo = 1;
console.log(window.foo); // 1

bar = 2; // window.bar = 2
```

- `let`이나 `const` 키워드로 선언한 전역 변수는 전역 객체의 프로퍼티가 아니다. 즉, `window.foo`와 같이 접근할 수 없다. 이 키워드로 선언한 전역 변수는 보이지 않는 개념적인 블록(전역 렉시컬 환경의 선언적 환경 레코드) 내에 존재하게 된다.
- 브라우저 환경의 모든 JavaScript 코드는 하나의 전역 객체 `window`를 공유한다.
  - 여러 개의 `script` 태그를 통해 JS 코드를 분리해도 하나의 전역 객체 `window`를 공유하는 것은 변함이 없다.
  - 분리되어 있는 JS 코드가 하나의 전역을 공유한다는 의미다.

### 빌트인 전역 프로퍼티
- `Infinity`
- `NaN`
- `undefined`

### 빌트인 전역 함수
- `eval`
- `isFinite`
- `isNaN`
- `parseFloat`
- `parseInt`
- `encodeURI`, `decodeURI`
- `encodeURIComponent`, `decodeURIComponent`

### 암묵적 전역
```javascript
var x = 10;

function foo () {
  // 선언하지 않은 식별자에 값 할당
  y = 20; // window.y = 20;
}
foo();

// 선언하지 않은 식별자 y를 전역에서 참조할 수 있다.
console.log(x + y); // 30
```

`foo` 함수가 호출되면 JS 엔진은 `y` 변수에 값을 할당하기 위해 먼저 스코프 체인을 통해 선언된 변수인지 확인한다. 이때 `foo` 함수의 스코프와 전역 스코프 어디에서도 `y` 변수의 선언을 찾을 수 없으므로 참조 에러가 발생한다. 하지만 JS 엔진은 `y = 20`을 `window.y = 20`으로 해석하여 전역 객체에 프로퍼티를 동적 생성한다. 결국 `y`는 전역 객체의 프로퍼티가 되어 마치 전역 변수처럼 동작한다. 이러한 현상을 **암묵적 전역**이라 한다.

하지만 `y`는 변수 선언 없이 단지 전역 객체의 프로퍼티로 추가되었을 뿐이다. **따라서 `y`는 변수가 아니다. 그 결과 변수 호이스팅이 발생하지 않는다.**

```javascript
console.log(x); // undefined
console.log(y); // ReferenceError: y is not defined

var x = 10;

function foo () {
  y = 20;
}
foo();

console.log(x + y); // 30
```

또한 변수가 아니라 단지 프로퍼티인 `y`는 `delete` 연산자로 삭제할 수 있다. 전역 변수는 프로퍼티이지만 `delete` 연산자로 삭제할 수 없다.

```javascript
console.log(x); // undefined

var x = 10;

function foo () {
  y = 20;
  console.log(x + y);
}
foo();

console.log(window.x); // 10
console.log(window.y); // 20

delete x; // 전역 변수는 삭제되지 않는다.
delete y; // 프로퍼티는 삭제된다.

console.log(window.x); // 10
console.log(window.y); // undefined
```