# 8장. 제어문
제어문은 조건에 따라 코드 블록을 실행(조건문)하거나 반복 실행(반복문)할 때 사용한다. 일반적으로 코드는 위에서 아래 방향으로 순차적으로 실행된다. 제어문을 사용하면 코드의 실행 흐름을 인위적으로 제어할 수 있다.

하지만 코드의 실행 순서가 변경된다는 것은 단순히 위에서 아래로 순차적으로 진행하는 직관적인 코드의 흐름을 혼란스럽게 만든다. 따라서 제어문은 코드의 흐름을 이해하기 어렵게 만들어 가독성을 해치는 단점이 있다. 가독성이 좋지 않은 코드는 오류를 발생시키는 원인이 된다. `forEach`, `map`, `filter`, `reduce` 같은 고차 함수를 사용한 함수형 프로그래밍 기법에서는 제어문의 사용을 억제하여 복잡성을 해결하려고 노력한다.

## 블록문
블록문은 0개 이상의 문을 중괄호로 묶은 것으로, 코드 블록 또는 블록이라 부르기도 한다. JavaScript는 블록문을 하나의 실행 단위로 취급한다. 블록문은 단독으로 사용할 수도 있으나 일반적으로 제어문이나 함수를 정의할 때 사용하는 것이 일반적이다.

문의 끝에는 세미콜론을 붙이는 것이 일반적이다. **하지만 블록문은 언제나 문의 종료를 의미하는 자체 종결성을 갖기 때문에 블록문의 끝에는 세미 콜론을 붙이지 않는다는 것에 주의하자.**

```javascript
// 블록문
{
  var foo = 10;
}

// 제어문
var x = 1;
if (x < 10) {
  x++;
}

// 함수 선언문
function sum(a, b) {
  return a + b;
}
```

## 조건문
조건문은 주어진 조건식의 평가 결과에 따라 코드 블록의 실행을 결정한다. 조건식은 불리언 값으로 평가될 수 있는 표현식이다.

### if ... else 문
```javascript
if (조건식) {
  // 참이면 이 블록이 실행
} else {
  // 거짓이면 이 블록이 실행
}
```

`if` 문의 조건식은 불리언 값으로 평가되어야 한다. **만약 `if` 문의 조건식이 불리언 값이 아닌 값으로 평가되면 JavaScript 엔진에 의해 암묵적으로 불리언 값으로 강제 변환되어 실행할 코드 블록을 결정한다.**

```javascript
if (조건식1) {
  // 조건식1이 참이면 실행
} else if (조건식2) {
  // 조건식2가 참이면 실행
} else {
  // 조건식 1, 2 모두 거짓이면 실행
}
```

대부분의 `if ... else` 문은 삼항 조건 연산자로 바꿔 쓸 수 있다.

```javascript
var x = 2;

// 0은 false로 취급
var result = x % 2 ? '홀수' : '짝수';
console.log(result); // 짝수
```

삼항 조건 연산자는 값으로 평가되는 표현식을 만든다. 따라서 삼항 조건 연산자 표현식은 값처럼 사용할 수 있기 때문에 변수에 할당할 수 있다. 하지만 `if ... else` 문은 표현식이 아닌 문이다. 따라서 값처럼 사용할 수 없기 때문에 변수에 할당할 수 없다.

조건에 따라 단순히 값을 결정하여 변수에 할당하는 경우 `if ... else` 문보다 삼항 조건 연산자를 사용하는 편이 가독성이 좋다. 하지만 조건에 따라 실행해야 할 내용이 복잡하여 여러 줄의 문이 필요하다면 `if ... else` 문을 사용하는 편이 가독성이 좋다.

### switch 문
주어진 표현식을 평가하여 그 값과 일치하는 표현식을 갖는 `case` 문으로 실행 흐름을 옮긴다. `case` 문은 상황을 의미하는 표현식을 지정하고 콜론으로 마친다. 그리고 그 뒤에 실행할 문들을 위치시킨다.

`switch` 문의 표현식과 일치하는 `case` 문이 없다면 실행 순서는 `default` 문으로 이동한다. 이는 선택사항으로, 사용할 수도 있고 않을 수도 있다.

```javascript
switch (표현식) {
  case 표현식1:
    switch 문의 표현식과 표현식1이 일치하면 실행될 문;
    break;
  case 표현식2:
    switch 문의 표현식과 표현식2가 일치하면 실행될 문;
    break;
  default:
    switch 문의 표현시고가 일치하는 case 문이 없을 때 실행될 문;
}
```

`if ... else` 문의 조건식은 불리언 값으로 평가되어야 하지만 `switch` 문의 표현식은 불리언 값보다는 문자열이나 숫자 값인 경우가 많다. 전자는 논리적 참, 거짓으로 실행할 코드 블록을 결정한다. 후자는 다양한 상황에 따라 실행할 코드 블록을 결정할 때 사용한다.

`switch` 문은 `case`, `default`, `break` 등 다양한 키워드를 사용해야 하고 폴스루가 발생하는 등 문법도 복잡하다. 따라서 C언어를 기반으로 하는 프로그래밍 언어는 대부분 `swtich` 문을 지원하지만 파이썬과 같이 지원하지 않는 언어도 있다.

`if ... else`로 해결할 수 있다면 `switch` 문보다 `if ... else`를 사용하는 편이 좋다. 하지만 조건이 너무 많아 `switch` 문을 사용했을 때 가독성이 더 좋다면 이를 사용하는 편이 좋다.

## 반복문
조건식의 평가 결과가 참인 경우 코드 블록을 실행한다. 그 후 조건식을 다시 평가하여 여전히 참인 경우 코드 블록을 다시 실행한다. 이는 조건식이 거짓일 때까지 반복된다.

### for 문
조건식이 거짓으로 평가될 때까지 코드 블록을 반복 실행한다.

```javascript
for (변수 선언문 또는 할당문; 조건식; 증감식) {
  조건식이 참인 경우 반복 실행될 문;
}
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ea44ad18-f0b2-4f51-b6cb-5ee8e8b8ad19)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9973673b-b3d0-46f6-971c-0d77ba8328ef)

`for` 문의 변수 선언문, 조건식, 증감식은 모두 옵션이므로 반드시 사용할 필요는 없다. 단, 어떤 식도 선언하지 않으면 무한루프가 된다.

### while 문
`while` 문은 주어진 조건식의 평가 결과가 참이면 코드 블록을 반복 실행한다. `for` 문은 반복 횟수가 명확할 때 주로 사용하고 `while` 문은 반복 횟수가 불명확할 때 주로 사용한다.

`while` 문은 조건문의 평가 결과가 거짓이 되면 코드 블록을 실행하지 않고 종료한다. 만약 조건식의 평가 결과가 불리언 값이 아니면 불리언 값으로 강제 변환하여 논리적 참, 거짓을 구별한다.

```javascript
var count = 0;

while (count < 3) {
  console.log(count); // 0 1 2
  count++;
}
```

조건식의 평가 결과가 언제나 참이면 무한루프가 된다. 탈출 조건을 내부에서 만들고 `break` 문으로 코드 블록을 만들어 탈출해야 한다.

### do ... while 문
`do ... while` 문은 코드 블록을 먼저 실행하고 조건식을 평가한다. 따라서 코드 블록은 무조건 한 번 이상 실행된다.

```javascript
var count = 0;

do {
  console.log(count); // 0 1 2
  count++;
} while (count < 3);
```

## break 문
`switch` 문과 `while` 문에서 살펴보았듯 `break` 문은 코드 블록을 탈출한다. 좀 더 정확히 표현하자면 코드 블록을 탈출하는 것이 아니라 레이블 문, 반복문 또는 `switch` 문의 코드 블록을 탈출한다. 이 외에 사용하면 `SyntaxError`가 발생한다.

```javascript
if (true) {
  break; // Uncaught SyntaxError: Illegal break statement
}
```

레이블 문이란 식별자가 붙은 문을 말한다.

```javascript
// foo라는 레이블 식별자가 붙은 레이블 문
foo: console.log('foo');
```

```javascript
// outer라는 식별자가 붙은 레이블 for 문
outer: for (var i = 0; i < 3; i++) {
  for (var j = 0; j < 3; j++) {
    if (i + j === 3) break outer;
    console.log(`inner [${i}, ${j}]`);
  }
}

console.log('Done!');
```

## continue 문
`continue` 문은 반복문의 코드 블록 실행을 현 지점에서 중단하고 반복문의 증감식으로 실행 흐름을 이동시킨다. `break` 문처럼 반복문을 탈출하지는 않는다.

```javascript
var string = 'Hello World';
var search = 'l';
var count = 0;

// 문자열은 유사 배열이므로 for 문으로 순회 가능
for (var i = 0; i < string.length; i++) {
  // 'l'이 아니면 현 지점에서 실행을 중단하고 반복문의 증감식으로 이동
  if (string[i] !== search) continue;
  count++; // continue 문이 실행되면 이 문은 실행되지 않는다.
}

console.log(count); // 3
```

```javascript
// continue 문을 사용하지 않으면 if 문 내에 코드를 작성해야 함.
for (var i = 0; i < string.length; i++) {
  // 'l'이면 카운트 증가
  if (string[i] === search) {
    count++;
    // code
    // code
    // code
  }
}

// continue 문을 사용하면 if 문 밖에 코드 작성 가능.
for (var i = 0; i < string.length; i++) {
  // 'l'이 아니면 카운트를 증가시키지 않는다.
  if (string[i] !== search) continue;

  count++;
  // code
  // code
  // code
}
```