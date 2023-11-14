# 9장. 타입 변환과 단축 평가
## 타입 변환이란?

JavaScript의 모든 값은 타입이 있다. 값의 타입은 개발자의 의도에 따라 다른 타입으로 변환할 수 있다. 개발자가 의도적으로 값의 타입을 변환하는 것을 **명시적 타입 변환** 또는 **타입 캐스팅**이라 한다.

```jsx
var x = 10;

// 명시적 타입 변환
// 숫자를 문자열로 타입 캐스팅한다.
var str = x.toString();
console.log(typeof str, str); // string 10

// x 변수의 값이 변경된 것은 아니다.
console.log(typeof x, x); // number 10
```

개발자의 의도와는 상관없이 표현식을 평가하는 도중에 JavaScript 엔진에 의해 암묵적으로 타입이 자동 변환되기도 한다. 이를 **암묵적 타입 변환** 또는 **타입 강제 변환**이라 한다.

```jsx
var x = 10;

// 암묵적 타입 변환
// 문자열 연결 연산자는 숫자 타입 x의 값을 바탕으로 새로운 문자열을 생성한다.
var str = x + '';
console.log(typeof str, str); // string 10

// x 변수의 값이 변경된 것은 아니다.
console.log(typeof x, x); // number 10
```

- 원시 값은 변경 불가능한 값이므로 변경할 수 없다.
- **타입 변환이란 기존 원시 값을 사용해 다른 타입의 새로운 원시 값을 생성하는 것이다.**
- JavaScript 엔진은 표현식 `x + ''`를 평가하기 위해 x 변수의 숫자 값을 바탕으로 새로운 문자열 값 `'10'`을 생성하고 이것으로 표현식 `'10' + ''`를 평가한다. 이때 암묵적으로 생성된 문자열 `'10'` 은 x 변수에 재할당되지 않는다.
- 암묵적 타입 변환은 기존 변수 값을 재할당하여 변경하는 것이 아니다.
- JavaScript 엔진은 표현식을 에러 없이 평가하기 위해 피연산자의 값을 암묵적 타입 변환해 새로운 타입의 값을 만들어 단 한 번 사용하고 버린다.

## 암묵적 타입 변환

```jsx
// 피연산자가 모두 문자열 타입이어야 하는 문맥
'10' + 2
// '102'

// 피연산자가 모두 숫자 타입이어야 하는 문맥
5 * '10'
// 50

// 피연산자 또는 표현식이 불리언 타입이어야 하는 문맥
!0 
// true
if (1) { }
```

### 문자열 타입으로 변환

```jsx
1 + '2'
// '12'
```

위 예제의 `+` 연산자는 피연산자 중 하나 이상이 문자열이므로 문자열 연결 연산자로 동작한다. 문자열 연결 연산자의 역할은 문자열 값을 만드는 것이다. 따라서 문자열 연결 연산자의 모든 피연산자는 코드의 문맥상 모두 문자열 타입이어야 한다.

- JavaScript 엔진은 문자열 연결 연산자 표현식을 평가하기 위해 문자열 연결 연산자의 피연산자 중에서 문자열 타입이 아닌 피연산자를 문자열 타입으로 암묵적 타입 변환한다.
- 연산자 표현식의 피연산자만이 암묵적 타입 변환의 대상이 되는 것은 아니다. JavaScript 엔진은 표현식을 평가할 때 코드 문맥에 부합하도록 암묵적 타입 변환을 실행한다.
    - ES6의 템플릿 리터럴의 표현식 삽입은 표현식의 평가 결과를 문자열 타입으로 암묵적 타입 변환한다.

```jsx
`1 + 1 = ${1 + 1}`
```

JavaScript 엔진은 문자열 타입이 아닌 값을 문자열 타입으로 암묵적 타입 변환을 수행할 때 다음과 같이 동작한다.

```jsx
// 숫자 타입
0 + '' // "0"
-0 + '' // "-0"
1 + '' // "1"
-1 + '' // "-1"
NaN + '' // "NaN"
Infinity + '' // "Infinity"
-Infinity + '' // "-Infinity"

// 불리언 타입
true + '' // "true"
false + '' // "false"

// null 타입
null + '' // "null"

// undefined 타입
undefined + '' // "undefined"

// 심벌 타입
(Symbol()) + '' // TypeError: Cannot convert a Symbol value to string

// 객체 타입
({}) + '' // "[object Object]"
Math + '' // "[object Math]"
[] + '' // ""
[10, 20] + '' // "10, 20"
(function(){}) + '' // "function(){}"
Array + '' // "function Array() { [native code] }"
```

### 숫자 타입으로 변환

```jsx
1 - '1' // 0
1 * '10' // 10
1 / 'one' // NaN
```

산술 연산자의 역할은 숫자 값을 만드는 것이다. 따라서 산술 연산자의 모든 피연산자는 코드 문맥상 모두 숫자 타입이어야 한다.

- JavaScript 엔진은 산술 연산자 표현식을 평가하기 위해 산술 연산자의 피연산자 중에서 숫자 타입이 아닌 피연산자를 숫자 타입으로 암묵적 타입 변환한다.
- 이때 피연산자를 숫자 타입으로 변환할 수 없는 경우에는 산술 연산을 수행할 수 없으므로 표현식의 평가 결과는 `NaN`이 된다.

```jsx
'1' > 0 // true
```

비교 연산자의 역할은 불리언 값을 만드는 것이다. 비교 연산자는 피연산자의 크기를 비교하므로 모든 연산자는 코드의 문맥상 모두 숫자 타입이어야 한다.

- JavaScript 엔진은 비교 연산자 표현식을 평가하기 위해 비교 연산자의 피연산자 중에서 숫자 타입이 아닌 피연산자를 숫자 타입으로 암묵적 타입 변환한다.

```jsx
// 문자열 타입
+'' // 0
+'0' // 0
+'1' // 1
+'string' // NaN

// 불리언 타입
+true // 1
+false // 0

// null 타입
+null // 0

// undefined 타입
+undefined // NaN

// 심벌 타입
+Symbol() // TypeError: Cannot convert a Symbol value to a number

// 객체 타입
+{} // NaN
+[] // 0
+[10, 20] // NaN
+(function(){}) // NaN
```

### 불리언 타입으로 변환

```jsx
if ('') console.log(x);
```

`if` 문이나 `for` 문과 같은 제어문 또는 삼항 조건 연산자의 조건식은 불리언 값, 즉 논리적 참/거짓으로 평가되어야 하는 표현식이다. JavaScript 엔진은 조건식의 평가 결과를 불리언 타입으로 암묵적 타입 변환한다.

```jsx
if ('') console.log('1');
if (true) console.log('2');
if (0) console.log('3');
if ('str') console.log('4');
if (null) console.log('5');

// 2 4
```

**JavaScript 엔진은 불리언 타입이 아닌 값을 Truthy 값 또는 Falsy 값으로 구분한다.** 즉, 제어문의 조건식과 같이 불리언 값으로 평가되어야 할 문맥에서 Truthy 값은 `true`, Falsy 값은 `false` 로 암묵적 타입 변환된다.

아래 값들은 `false`로 평가되는 Falsy 값이다.

- `false`
- `undefined`
- `null`
- `0`, `-0`
- `NaN`
- `''` (빈 문자열)

```jsx
// 전달받은 인수가 Falsy 값이면 true, Truthy 값이면 false를 반환
function isFalsy(v) {
		return !v;
}

// 전달받은 인수가 Truthy 값이면 true, Falsy 값이면 false를 반환
function isTruthy(v) {
		return !!v;
}

// 모두 true 반환
isFalsy(false);
isFalsy(undefined);
isFalsy(null);
isFalsy(0);
isFalsy(NaN);
isFalsy('');

// 모두 true 반환
isTruthy(true);
isTruthy('0'); // 빈 문자열이 아닌 문자열은 Truthy 값이다.
isTruthy({});
isTruthy([]);
```

## 명시적 타입 변환

개발자의 의도에 따라 명시적으로 타입을 변경하는 방법은 다양하다. 표준 빌트인 생성자 함수(`String`, `Number`, `Boolean`)를 `new` 연산자 없이 호출하는 방법과 빌트인 메서드를 사용하는 방법, 암묵적 타입 변환을 이용하는 방법이 있다.

### 문자열 타입으로 변환

1. `String` 생성자 함수를 `new` 연산자 없이 호출하는 방법
2. `Object.prototype.toString` 메서드를 사용하는 방법
3. 문자열 연결 연산자를 이용하는 방법

```jsx
// 1. String 생성자를 new 연산자 없이 호출하는 방법
// 숫자 타입 => 문자열 타입
String(1); // "1"
String(NaN); // "NaN"
String(Infinity); // "Infinity"
// 불리언 타입 -> 문자열 타입
String(true); // "true"
String(false); // "false"

// 2. Object.prototype.toString 메서드를 사용하는 방법
// 숫자 타입 => 문자열 타입
(1).toString(); // "1"
(NaN).toString(); // "NaN"
(Infinity).toString(); // "Infinity"
// 불리언 타입 => 문자열 타입
(true).toString(); // "true"
(false).toString(); // "false"

// 3. 문자열 연결 연산자
// 숫자 타입 => 문자열 타입
1 + ''; // "1"
NaN + ''; // "NaN"
Infinity + ''; // "Infinity"
// 불리언 타입 => 문자열 타입
true + ''; // "true"
false + ''; // "false"
```

### 숫자 타입으로 변환

1. `Number` 생성자 함수를 `new` 연산자 없이 호출하는 방법
2. `parseInt`, `parseFloat` 함수를 사용하는 방법(문자열만 숫자 타입으로 변환 가능)
3. `+` 단항 산술 연산자를 이용하는 방법
4. `*` 산술 연산자를 이용하는 방법

```jsx
// 1. Number 생성자 함수를 new 연산자 없이 호출하는 방법
// 문자열 타입 => 숫자 타입
Number('0'); // 0
Number('-1'); // -1
Number('10.53'); // 10.53
// 불리언 타입 -> 숫자 타입
Number(true); // 1
Number(false); // 0

// 2. parseInt, parseFloat 함수를 사용하는 방법
// 숫자 타입 => 숫자 타입
parseInt('0'); // 0
parseInt('-1'); // -1
parseInt('10.53'); // 10.53

// 3. + 단항 산술 연산자를 이용하는 방법
// 문자열 타입 => 숫자 타입
+'0'; // 0
+'-1'; // -1
+'10.53'; // 10.53
// 불리언 타입 => 숫자 타입
+true; // 1
+false; // 0

// 4. * 산술 연산자를 이용하는 방법
// 문자열 타입 => 숫자 타입
'0' * 1; // 0
'-1' * 1; // -1
'10.53' * 1 // 10.53
// 불리언 타입 => 숫자 타입
true * 1; // 1
false * 1; // 0
```

### 불리언 타입으로 변환

1. `Boolean` 생성자 함수를 `new` 연산자 없이 호출하는 방법
2. `!` 부정 논리 연산자를 두 번 사용하는 방법

```jsx
// 1. Boolean 생성자 함수를 new 연산자 없이 호출하는 방법
// 문자열 타입 => 불리언 타입
Boolean('x'); // true
Boolean(''); // false
Boolean('false'); // true
// 숫자 타입 => 불리언 타입
Boolean(0); // false
Boolean(1); // true
Boolean(NaN); // false
Boolean(Infinity); // true
// null 타입 => 불리언 타입
Boolean(null); // false
// undefined 타입 => 불리언 타입
Boolean(undefined); // false
// 객체 타입 => 불리언 타입
Boolean({}); // true
Boolean([]); // true

// 2. ! 부정 논리 연산자를 두 번 사용하는 방법
// 문자열 타입 => 불리언 타입
!!'x'; // true
!!''; // false
!!'false' // true
// 숫자 타입 => 불리언 타입
!!0; // false
!!1; // true
!!NaN; // false
!!Infinity; // true
// null 타입 => 불리언 타입
!!null; // false
// undefined 타입 => 불리언 타입
!!undefined; // false
// 객체 타입 => 불리언 타입
!!{}; // true
!![]; // true
```

## 단축 평가

### 논리 연산자를 사용한 단축 평가

```jsx
'Cat' && 'Dog' // "Dog"
```

- 논리곱(`&&`) 연산자는 두 개의 피연산자가 모두 `true`로 평가될 때 `true`를 반환한다.
- 논리곱 연산자는 좌항에서 우항으로 평가가 진행된다.
- 논리곱 연산자는 **논리 연산의 결과를 결정하는 두 번째 피연산자를 그대로 반환한다.**

```jsx
'Cat' || 'Dog' // "Cat"
```

- 논리합(`||`) 연산자는 두 개의 피연산자 중 하나만 `true`로 평가되어도 `true`를 반환한다.
- 좌항에서 우항으로 평가가 진행된다.
- 논리합 연산자는 **논리 연산의 결과를 결정한 첫 번째 피연산자를 그대로 반환한다.**

이 두 연산자는 **논리 연산의 결과를 결정하는 피연산자를 타입 변환하지 않고 그대로 반환한다. 이를 단축 평가라 한다.** 단축 평가는 표현식을 평가하는 도중에 평가 결과가 확정된 경우 나머지 평가 과정을 생략하는 것을 말한다.

<img width="405" alt="스크린샷 2023-11-14 오후 1 30 05" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/67bacd2b-c115-4dd4-8de2-41ed4b65e3ed">

```jsx
// 논리합 연산자
'Cat' || 'Dog' // "Cat"
false || 'Dog' // "Dog"
'Cat' || false // "Cat"

// 논리곱 연산자
'Cat' && 'Dog' // "Dog"
false && 'Dog' // false
'Cat' && false // false
```

```jsx
// 단축 평가를 사용하면 if 문을 대체할 수 있다.
// 어떤 조건이 Truthy 값일 때 무언가를 해야 한다면 논리곱(&&) 연산자 표현식으로 if 문을 대체할 수 있다.
var done = true;
var message = '';

// 주어진 조건이 true일 때
if (done) message = '완료';

// if 문은 단축 평가로 대체 가능하다.
// done이 true이면 message에 '완료'를 할당
message = done && '완료';
console.log(message); // 완료
```

```jsx
// 조건이 Falsy 값일 때 무언가를 해야 한다면 논리합 연산자 표현식으로 if 문을 대체할 수 있다.
var done = false;
var message = '';

// 주어진 조건이 false일 때
if (!done) message = '미완료';

// if 문은 단축 평가로 대체 가능하다.
// done이 false라면 message에 '미완료'를 할당.
message = done || '미완료';
console.log(message); // 미완료
```

```jsx
// 삼항 조건 연산자는 if...else 문을 대체할 수 있다.
var done = true;
var message = '';

// if...else 문
if (done) message = '완료';
else      message = '미완료';
console.log(message); // 완료

// if...else 문은 삼항 조건 연산자로 대체 가능하다.
message = done ? '완료' : '미완료';
console.log(message); // 완료
```

단축 평가를 사용하면 유용한 경우가 있다.

```jsx
// 객체의 프로퍼티 참조
var elem = null;
var value = elem.value; // TypeError: Cannot read property 'value' of null

var elem = null;
// elem이 null이나 undefined와 같은 Falsy 값이면 elem으로 평가되고
// elem이 Truthy 값이면 elem.value로 평가된다.
var value = elem && elem.value; // null

// 단축 평가를 사용한 매개변수의 기본값 설정
function getStringLength(str) {
		str = str || '';
		return str.length;
}

getStringLength(); // 0
getStringLength('hi'); // 2

// ES6와 매개변수의 기본값 설정
function getStringLength(str = '') {
		return str.length;
}

getStringLength(); // 0
getStringLength('hi'); // 2
```

### 옵셔널 체이닝 연산자

ES11(ECMAScript 2020)에서 도입된 옵셔널 체이닝 연산자 `?.` 는 좌항의 피연산자가 `null` 또는 `undefined`인 경우 `undefined`를 반환하고, 그렇지 않으면 우항의 프로퍼티 참조를 이어간다.

```jsx
var elem = null;

// elem이 null 또는 undefined이면 undefined를 반환, 그렇지 않으면 우항의 프로퍼티 참조를 이어간다.
var value = elem?.value;
console.log(value); // undefined
```

**옵셔널 체이닝 연산자는 객체를 가리키기를 기대하는 변수가 `null` 또는 `undefined`가 아닌지 확인하고 프로퍼티를 참조할 때 유용하다.**

```jsx
// 옵셔널 체이닝 연산자가 도입되기 이전
var elem = null;

// elem이 Falsy 값이면 elem으로 평가되고, elem이 Truthy 값이면 elem.value로 평가된다.
var value = elem && elem.value;
console.log(value); // null
```

논리 연산자 `&&`는 좌항 피연산자가 `false`로 평가되는 Falsy 값(`false`, `undefined`, `null`, `0`, `-0`, `NaN`, `''`)이면 좌항 피연산자를 그대로 반환한다. 좌항 피연산자가 `Falsy` 값인 0이나 `''`인 경우도 마찬가지다. 하지만 0이나 `''`는 객체로 평가될 때도 있다.

```jsx
var str = '';

// 문자열의 길이(length)를 참조한다.
var length = str && str.length;

// 문자열의 길이(length)를 참조하지 못한다.
console.log(length); // ''
```

하지만 옵셔널 체이닝 연산자는 좌항 피연산자가 `false`로 평가되는 Falsy 값이라도 `null` 또는 `undefined` 가 아니면 우항의 프로퍼티 참조를 이어간다.

```jsx
var str = '';

var length = str?.length;
console.log(length); // 0
```

### null 병합 연산자

ES11(ECMAScript 2020)에서 도입된 null 병합 연산자 `??`는 좌항의 피연산자가 `null` 또는 `undefined`인 경우 우항의 피연산자를 반환하고, 그렇지 않으면 좌항의 피연산자를 반환한다. **null 병합 연산자 `??`는 변수에 기본값을 설정할 때 유용하다.**

```jsx
var foo = null ?? 'default string';
console.log(foo); // "default string"
```

null 병합 연산자가 도입되기 이전에는 논리 연산자 `||`를 사용한 단축 평가를 통해 변수에 기본값을 설정했다.

논리 연산자를 사용한 단축 평가의 경우 좌항의 피연산자가 `false`로 평가되는 Falsy 값이면 우항의 피연산자를 반환한다. 만약 Falsy 값인 0이나 `''`도 기본값으로서 유효하다면 예기치 않은 동작이 발생할 수 있다.

```jsx
// Falsy 값인 0이나 ''도 기본값으로서 유효하다면 예기치 않은 동작이 발생할 수 있다.
var foo = '' || 'default string';
console.log(foo); // "default string"
```

하지만 null 병합 연산자는 좌항의 피연산자가 `false` 로 평가되는 Falsy 값이라도 `null` 또는 `undefined`가 아니면 좌항의 피연산자를 그대로 반환한다.

```javascript
var foo = '' ?? 'default string';
console.log(foo); // ""
```