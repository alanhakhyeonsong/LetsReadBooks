# 아이템 16. number 인덱스 시그니처보다는 Array, 튜플, ArrayLike를 사용하기
JavaScript의 암시적 타입 강제와 관련된 부분은 가장 이상하게 동작한다.

```bash
> "0" == 0
true
```

암시적 타입 강제와 관련된 문제는 대부분 `===`와 `!==`를 사용해서 해결이 가능하다.

JavaScript 객체 모델에도 이상한 부분들이 있으며, 이 중 일부는 TypeScript 타입 시스템으로 모델링되기 때문에 JavaScript 객체 모델을 이해하는 것이 중요하다.

JavaScript에서 객체란 키/값 쌍의 모음이다. 키는 보통 문자열이다. (ES2015 이후론 심벌일 수 있다) 그리고 값은 어떤 것이든 될 수 있다.

Python이나 Java에서 볼 수 있는 '해시 가능' 객체라는 표현이 JavaScript엔 없다. 만약 더 복잡한 객체를 키로 사용하려고 하면, `toString` 메서드가 호출되어 객체가 문자열로 변환된다.

```bash
> x = {}
{}
> x[[1, 2, 3]] = 2
2
> x
{ '1,2,3': 1 }
```

특히, 숫자는 키로 사용할 수 없다. 만약 속성 이름으로 숫자를 사용하려고 하면, JavaScript 런타임은 문자열로 변환할 것이다.

```bash
> { 1: 2, 3: 4 }
{ '1': 2, '3': 4 }
```

배열은 분명히 객체다.

```bash
> typeof []
'object'
```

숫자 인덱스를 사용하는 것이 당연하다.

```bash
> x = [1, 2, 3]
[ 1, 2, 3 ]
> x[0]
1
```

이상하게 보이는데, 인덱스들은 문자열로 변환되어 사용된다. 문자열 키를 사용해도 역시 배열의 요소에 접근할 수 있다.

```bash
> x['1']
2
```

`Object.keys`를 이용해 배열의 키를 나열해 보면, 키가 문자열로 출력된다.

```bash
> Object.keys(x)
[ '0', '1', '2' ]
```

TypeScript는 이러한 혼란을 바로잡기 위해 숫자 키를 허용하고, 문자열 키와 다른 것으로 인식한다. `Array`에 대한 타입 선언에서 확인할 수 있다.

```typescript
// lib.es5.d.ts
interface Array<T> {
  // ...
  [n: number]: T;
}
```

런타임엔 ECMAScript 표준이 서술하는 것처럼 문자열 키로 인식하므로 이 코드는 완전히 가상이라 할 수 있지만, 타입 체크 시점에 오류를 잡을 수 있어 유용하다.

```typescript
const xs = [1, 2, 3]
const x0 = xs[0] // OK
const x1 = xs['1']
// ~~~ Element implicitly has an 'any' type
//      because index expression is not of type 'number'

function get<T>(array: T[], k: string): T {
  return array[k]
  // ~ Element implicitly has an 'any' type
  //   because index expression is not of type 'number'
}
```

위 코드는 실제로 동작하지 않는다. 그리고 TypeScript의 타입 시스템의 다른 것들과 마찬가지로, 타입 정보는 런타임에 제거된다. 한편 `Object.keys` 같은 구문은 여전히 문자열로 반환된다.

```typescript
const keys = Object.keys(xs); // 타입이 string[]
for (const key in ks) {
  key; // 타입이 string
  const x = xs[key]; // 타입이 number
}
```

`string`이 `number`에 할당될 수 없기 때문에, 예제의 마지막 줄이 동작하는 것이 이상하게 보일 것이다. 배열을 순회하는 코드 스타일에 대한 실용적인 허용이라 생각하는 것이 좋다. JavaScript에선 흔한 일이지만, 이 예제가 배열을 순회하기에 좋은 방법은 아니다. 인덱스에 신경 쓰지 않는다면, `for ... of`를 사용하는게 더 좋다.

```typescript
for (const x of xs) {
  x; // 타입이 number
}
```

만약 인덱스의 타입이 중요하다면, `number` 타입을 제공해 줄 `Array.prototype.forEach`를 사용하면 된다.

```typescript
xs.forEach((x, i) => {
  i; // 타입이 number
  x; // 타입이 number
});
```

루프 중간에 멈춰야 한다면, C 스타일인 `for (;;)` 루프를 사용하는 것이 좋다.

```typescript
for (let i = 0; i < xs.length; i++) {
  const x = xs[i];
  if (x < 0) break;
}
```

타입이 불확실하다면, `for-in` 루프는 `for-of` 또는 C 스타일 `for` 루프에 비해 몇 배나 느리다.

인덱스 시그니처가 `number`로 표현되어 있다면 입력한 값이 `number`여야 한다는 것을 의미하지만, 실제 런타임에 사용되는 키는 `string` 타입이다.

이 부분이 혼란스럽게 느껴질 수 있다. 일반적으로 `string` 대신 `number`를 타입의 인덱스 시그니처로 사용할 이유는 많지 않다. 만약 숫자를 사용하여 인덱스할 항목을 지정한다면 `Array` 또는 튜플 타입을 대신 사용하게 될 것이다. `number`를 인덱스 타입으로 사용하면 숫자 속성이 어떤 특별한 의미를 지닌다는 오해를 불러 일으킬 수 있다.

한편 `Array` 타입이 사용하지도 않을 `push`, `concat` 같은 다른 속성(프로토타입에서 온)을 가지는게 납득하기 어려울 수 있다.

어떤 길이를 가지는 배열과 비슷한 형태의 튜플을 사용하고 싶다면 TypeScript에 있는 `ArrayLike` 타입을 사용한다.

```typescript
function checkedAccess<T>(xs: ArrayLike<T>, i: number): T {
  if (i < xs.length) {
    return xs[i];
  }
  throw new Error(`배열의 끝을 지나서 ${i}를 접근하려 했습니다.`);
}
```

위 예제는 길이와 숫자 인덱스 시그니처만 있다. 이런 경우가 실제론 드물긴 하지만 필요하다면 `ArrayLike`를 사용해야 한다. 하지만 이를 사용하더라도 키는 여전히 문자열이라는 점을 잊지 말자.

```typescript
const tupleLike: ArrayLike<string> = {
  '0': 'A',
  '1': 'B',
  length: 2,
} // OK
```

## 요약
- 배열은 객체이므로 키는 숫자가 아니라 문자열이다. 인덱스 시그티처로 사용된 `number` 타입은 버그를 잡기 위한 순수 TypeScript 코드다.
- 인덱스 시그니처에 `number`를 사용하기보다 `Array`나 튜플, 또는 `ArrayLike` 타입을 사용하는 것이 좋다.