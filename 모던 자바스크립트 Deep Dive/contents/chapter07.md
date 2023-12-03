# 7장. 연산자
- 연산자는 하나 이상의 표현식을 대상으로 산술, 할당, 비교, 논리, 타입, 지수 연산 등을 수행해 하나의 값을 만든다.
- 연산의 대상을 피연산자라 한다.
- 피연산자는 값으로 평가될 수 있는 표현식이어야 한다.
- 피연산자와 연산자의 조합으로 이뤄진 연산자 표현식도 값으로 평가될 수 있는 표현식이다.

---
📌 연산자의 원칙 (언어마다 세부 사항이 결정된다.)

1. 연산자는 항의 갯수에 따라 동작이 다르다.
2. lvalue와 rvalue일 때 동작이 다르다.
3. 연산자도 자료형이 있다.
4. 연산자는 피연산자의 자료형이 일치해야 한다.

---

```javascript
// 산술 연산자
5 * 4 // → 20

// 문자열 연결 연산자
'My name is ' + 'Ramos' // → 'My name is Ramos'

// 할당 연산자
color = 'red' // → 'red'

// 비교 연산자
3 > 5 // → false

// 논리 연산자
true && false // → false

// 타입 연산자
typeof 'Hi' // → string
```

피연산자가 "값"이라는 명사의 역할을 한다면, 연산자는 "피연산자를 연산하여 새로운 값을 만든다"라는 동사의 역할을 한다고 볼 수 있다.

**피연산자는 연산의 대상이 되어야 하므로 값으로 평가될 수 있어야 한다.**

## 산술 연산자
- 산술 연산자는 피연산자를 대상으로 수학적 계산을 수행해 새로운 숫자 값을 만든다.
- 산술 연산이 불가능한 경우, `NaN`을 반환한다.

### 이항 산술 연산자
- 2개의 피연산자를 산술 연산하여 숫자 값을 만든다.
- 모든 이항 산술 연산자는 피연산자의 값을 변경하는 부수 효과가 없다.
- 어떤 산술 연산을 해도 피연산자의 값이 바뀌는 경우는 없고 언제나 새로운 값을 만들 뿐이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1f8d8cf2-d214-4699-9447-7c2741c48c90)

### 단항 산술 연산자
1개의 피연산자를 산술 연산하여 숫자 값을 만든다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1437b7cf-b7e2-4b4b-ae9c-61ffa3498e07)

주의할 점은 이항 산술 연산자와는 달리 **증가/감소 연산자는 피연산자의 값을 변경하는 부수 효과가 있다**는 것이다.

```javascript
var x = 1;

// ++ 연산자는 피연산자의 값을 변경하는 암묵적 할당이 이뤄진다.
x++; // x = x + 1;
console.log(x); // 2

x--; // x = x - 1;
console.log(x); // 1
```

증가/감소 연산자는 위치에 의미가 있다.

- 피연산자 앞에 위치한 전위 증가/감소 연산자는 먼저 피연산자의 값을 증가/감소시킨 후, 다른 연산을 수행한다.
- 피연산자 뒤에 위치한 후위 증가/감소 연산자는 먼저 다른 연산을 수행한 후, 피연산자의 값을 증가/감소시킨다.

```javascript
var x = 5, result;

result = x++;
console.log(result, x); // 5 6

result = ++x;
console.log(result, x); // 7 7

result = x--;
console.log(result, x); // 7 6

result = --x;
console.log(result, x); // 5 5
```

```javascript
var x = '1';

// 문자열을 숫자로 타입 변환한다.
console.log(+x); // 1
// 부수 효과는 없다.
console.log(x); // "1"

// 불리언 값을 숫자로 타입 변환한다.
x = true;
console.log(+x); // 1
// 부수 효과는 없다.
console.log(x); // true

// 문자열을 숫자로 타입 변환할 수 없으므로 NaN을 반환한다.
x = 'Hello';
console.log(+x); // NaN
// 부수 효과는 없다.
console.log(x); // "Hello"
```

### 문자열 연결 연산자
**+ 연산자는 피연산자 중 하나 이상이 문자열인 경우 문자열 연결 연산자로 동작한다.** 그 외의 경우는 산술 연산자로 동작한다.

```javascript
// 문자열 연결 연산자
'1' + 2; // '12'
1 + '2'; // '12'

// 산술 연산자
1 + 2; // 3

// true는 1로 타입 변환된다.
1 + true; // 2

// false는 0으로 타입 변환된다.
1 + false; // 1

// null은 0으로 타입 변환된다.
1 + null; // 1

// undefined는 숫자로 타입 변환되지 않는다.
+undefined; // NaN
1 + undefined; // NaN
```

주목할 것은 개발자의 의도와는 상관없이 JavaScript 엔진에 의해 암묵적으로 타입이 자동 변환되기도 한다는 것이다. 이를 **암묵적 타입 변환** 또는 **타입 강제 변환**이라고 한다.

## 할당 연산자
- 할당 연산자는 우항에 있는 피연산자의 평가 결과를 좌항에 있는 변수에 할당한다.
- 할당 연산자는 좌항의 변수에 값을 할당하므로 변수 값이 변하는 부수 효과가 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/26c023b0-6a79-4001-b525-f7d6c161a397)

**표현식은 값으로 평가될 수 있는 문이고, 문에는 표현식인 문과 표현식이 아닌 문이 있다.**

```javascript
var x;

// 할당문은 표현식인 문이다.
console.log(x = 10); // 10
```

할당문은 변수에 값을 할당하는 부수 효과만 있을 뿐 값으로 평가되지 않을 것으로 보인다. 하지만 **할당문은 값으로 평가되는 표현식인 문으로서 할당된 값으로 평가된다.** 따라서 할당문을 다른 변수에 할당할 수 있고 여러 변수에 동일한 값을 연쇄 할당할 수 있다.

## 비교 연산자
좌항과 우항의 피연산자를 비교한 다음 그 결과를 불리언 값으로 반환한다.

### 동등/일치 비교 연산자
동등 비교 연산자와 일치 비교 연산자는 좌항과 우항의 피연산자가 같은 값으로 평가되는지 비교해 불리언 값을 반환한다. 하지만 비교하는 엄격성의 정도가 다르다. 동등 비교 연산자는 느슨한 비교를 하지만 일치 비교 연산자는 엄격한 비교를 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9ddde3c2-042e-4f89-ae37-96cecfa3b187)

**동등 비교 연산자는 좌항과 우항의 피연산자를 비교할 때 먼저 암묵적 타입 변환을 통해 타입을 일치시킨 후 같은 값인지 비교한다.** 따라서 동등 비교 연산자는 좌항과 우항의 피연산자가 타입은 다르더라도 암묵적 타입 변환 후에 같은 값일 수 있다면 `true`를 반환한다.

```javascript
'0' == ''; // false
0 == ''; // true
0 == '0'; // true
false == 'false'; // false
false == '0'; // true
false == null; // false
false == undefined; // false
```

동등 비교 연산자는 결과를 예측하기 어렵고 실수하기 쉽다. 대신 일치 비교 연산자를 사용하자.

**일치 비교 연산자는 좌항과 우항의 피연산자가 타입도 같고 값도 같은 경우에 한하여 `true`를 반환한다.** 다시 말해, 암묵적 타입 변환을 하지 않고 값을 비교한다. 따라서 예측하기 쉽다.

```javascript
5 === 5 // true

// 값과 타입이 모두 같은 경우만 true를 리턴
5 === '5'; // false

// NaN은 자신과 일치하지 않는 유일한 값이다.
NaN === NaN; // false

// Number.isNaN 함수는 지정한 값이 NaN인지 확인하고 그 결과를 불리언 값으로 반환한다.
Number.isNaN(NaN); // true
Number.isNaN(10); // false
Number.isNaN(1 + undefined); // true
```

JavaScript에는 양의 0과 음의 0이 있는데 이들을 비교하면 `true`를 반환한다.

```javascript
0 === -0; // true
0 == -0; // true

// ES6에서 도입된 Object.is 메서드는 예측 가능한 정확한 비교 결과를 반환한다.
// 그 외에는 일치 비교 연산자와 동일하게 동작한다.
Object.is(-0, +0); // false
Object.is(NaN, NaN); // true
```

부동등 비교 연산자와 불일치 비교 연산자는 각각 동등 비교 연산자와 일치 비교 연산자의 반대 개념이다.

### 대소 관계 비교 연산자
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0bfdbc51-1e3d-4212-832a-35fb5d9f7907)

## 삼항 조건 연산자
삼항 조건 연산자는 조건식의 평가 결과에 따라 반환할 값을 결정한다. JavaScript의 유일한 삼항 연산자이며, 부수 효과는 없다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/bcfb9fce-6655-4b0c-b116-61de0a7ac54f)

- 삼항 조건 연산자는 두 번째 피연산자 또는 세 번째 피연산자로 평가되는 표현식이다.
- 물음표 앞의 첫 번째 피연산자는 조건식, 즉 불리언 타입의 값으로 평가될 표현식이다.
- 조건식의 평가 결과가 불리언 값이 아니면 불리언 값으로 암묵적 타입 변환된다.

삼항 조건 연산자의 첫 번째 피연산자는 조건식이므로 삼항 조건 연산자 표현식은 조건문이다. `if...else` 문을 사용해도 유사하게 처리할 수 있다.

하지만 삼항 조건 연산자 표현식은 값처럼 사용할 수 있지만 `if...else` 문은 값처럼 사용할 수 없다.

```javascript
var x = 10;

var result = if (x % 2) { result = '홀수'; } else { result = '짝수'; };
// SyntaxError: Unexpected token if
```

**삼항 조건 연산자 표현식은 값으로 평가할 수 있는 표현식인 문이다.**

```javascript
var x = 10;

var result = x % 2 ? '홀수' : '짝수';
console.log(result); // 짝수
```

## 논리 연산자
논리 연산자는 우항과 좌항의 피연산자를 논리 연산한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/73bb6fe4-21ff-45e9-8264-16ac923ba46e)

- 논리 부정 연산자는 언제나 불리언 값을 반환한다.
  - 피연산자가 불리언 값이 아니면 불리언 타입으로 암묵적 타입 변환된다.
- 논리합 또는 논리곱 연산자의 평가 결과는 불리언 값이 아닐 수도 있다.
  - 논리합 또는 논리곱 연산자 표현식은 언제나 2개의 피연산자 중 어느 한쪽으로 평가된다.

## 쉼표 연산자
왼쪽 피연산자부터 차례대로 피연산자를 평가하고 마지막 피연산자의 평가가 끝나면 마지막 피연산자의 평가 결과를 반환한다.

```javascript
var x, y, z;

x = 1, y = 2, z = 3; // 3
```

## 그룹 연산자
소괄호로 피연산자를 감싸는 그룹 연산자는 자신의 피연산자인 표현식을 가장 먼저 평가한다. 따라서 이를 사용하면 연산자의 우선순위를 조절할 수 있다. 그룹 연산자는 연산자 우선순위가 가장 높다.

```javascript
10 * 2 + 3; // 23

10 * (2 + 3); // 50
```

## typeof 연산자
`typeof` 연산자는 피연산자의 데이터 타입을 문자열로 반환한다. `null`을 반환하는 경우는 없으며, 함수의 경우 `function`을 반환한다. `typeof` 연산자가 반환하는 문자열은 7개의 데이터 타입과 정확히 일치하지는 않는다.

```javascript
typeof '' // "string"
typeof 1 // "number"
typeof NaN // "number"
typeof true // "boolean"
typeof undefined // "undefined"
typeof Symbol() // "symbol"
typeof null // "object"
typeof [] // "object"
typeof {} // "object"
typeof new Date() // "object"
typeof /test/gi // "object"
typeof function () {} // "function"
```

`typeof` 연산자로 `null` 값을 연산해보면 `object`를 반환한다는 데 주의하자. 이는 JavaScript의 첫 번째 버전의 버그다. 하지만 기존 코드에 영향을 줄 수 있기 때문에 아직까지 수정되지 못하고 있다.

따라서 `null` 타입 체크는 일치 연산자를 사용하자.

선언하지 않은 식별자를 `typeof` 연산자로 연산해 보면 `ReferenceError`가 발생하지 않고 `undefined`를 반환한다는 점 역시 주의하자.

```javascript
// undeclared 식별자를 선언한 적이 없다.
typeof undeclared; // undefined
```

## 지수 연산자
- ES7에서 도입된 지수 연산자는 좌항의 피연산자를 밑으로, 우항의 피연산자를 지수로 거듭 제곱하여 숫자 값을 반환한다.
- 지수 연산자가 도입되기 전에는 `Math.pow` 메서들르 사용했다.
- 음수를 거듭제곱의 밑으로 사용해 계산하려면 괄호로 묶어야 한다.
- 다른 산술 연산자와 마찬가지로 할당 연산자와 함께 사용할 수 있다.

```javascript
var num = 5;
num **= 2; // 25
```

- 지수 연산자는 이항 연산자 중 우선순위가 가장 높다.

## 그 외의 연산자
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3110f1c0-b321-49ed-b93c-21f26ffa2118)

## 연산자의 부수효과
대부분의 연산자는 다른 코드에 영향을 주지 않는다. 하지만 일부 연산자는 다른 코드에 영향을 주는 부수 효과가 있다.

- 할당 연산자
- 증가/감소 연산자
- `delete` 연산자

```javascript
var x;

// 할당 연산자는 변수 값이 변하는 부수 효과가 있다. 이는 x 변수를 사용하는 다른 코드에 영향을 준다.
x = 1;
console.log(x); // 1

// 증가/감소 연산자는 피연산자의 값을 변경하는 부수 효과가 있다.
// 피연산자 x의 값이 재할당되어 변경된다. 이는 x 변수를 사용하는 다른 코드에 영향을 준다.
x++;
console.log(x); // 2

var o = { a: 1 };

// delete 연산자는 객체의 프로퍼티를 삭제하는 부수 효과가 있다. 이는 o 객체를 사용하는 다른 코드에 영향을 준다.
delete o.a;
console.log(o); // {}
```

## 연산자 우선순위
![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d526f4de-3010-4829-b320-cdc820c5df2c)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d923a530-e9e3-4ed2-a8b0-6f00aa6550c5)

## 연산자 결합 순서
어느 쪽부터 평가를 수행할 것인지를 나타내는 순서를 말한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/03fa6f73-05f3-4849-8224-61611b18c44e)