# 아이템 10. 객체 래퍼 타입 피하기
JavaScript에는 객체 이외에도 기본형 값들에 대한 일곱 가지 타입이 있다. 기본형들은 불변이며 메서드를 가지지 않는다는 점에서 객체와 구분된다. 그런데, 기본형인 `string`의 경우 메서드를 가지고 있는 것처럼 보인다.

```bash
> 'primitive'.charAt(3)
"m"
```

하지만 사실 `charAt`은 `string`의 메서드가 아니며, `string`을 사용할 때 JavaScript 내부적으로 많은 동작이 일어난다. `string` 기본형에는 메서드가 없지만, JavaScript에는 메서드를 가지는 `String` 객체 타입이 정의되어 있다.

**JavaScript는 기본형과 객체 타입을 서로 자유롭게 변환한다.**

`string` 기본형에 `charAt` 같은 메서드를 사용할 때, JavaScript는 기본형을 `String` 객체로 래핑하고, 메서드를 호출하고, 마지막에 래핑한 객체를 버린다.

만약 `String.prototype`을 몽키-패치 한다면 내부적인 동작을 관찰할 수 있다.

```javascript
// 실제로는 이렇게 하면 안됨!
const originalCharAt = String.prototype.charAt;
String.prototype.charAt = function(pos) {
  console.log(this, typeof this, pos);
  return originalCharAt.call(this, pos);
};
console.log('primitive'.charAt(3));
```

위 코드는 다음을 출력한다.

```
[String: 'primitive'] 'Object' 3
m
```
메서드 내의 `this`는 `string` 기본형이 아닌 `String` 객체 래퍼다. `String` 객체를 직접 생성할 수도 있으며, `string` 기본형처럼 동작한다. **그러나 `string` 기본형과 `String` 객체 래퍼가 항상 동일하게 동작하는 것은 아니다.**

```javascript
// String 객체는 오직 자기 자신하고만 동일함.
"hello" === new String("hello"); // false
new String("hello") === new String("hello"); // false
```

객체 래퍼 타입의 자동 변환은 종종 당황스러운 동작을 보일 때가 있다.

```bash
# 어떤 속성을 기본형에 할당한다면 그 속성이 사라진다.
> x = "hello";
> x.language = 'English';
'English'
> x.language
undefined
```

실제론 `x`가 `String` 객체로 변환된 후 `language` 속성이 추가되었고, `language` 속성이 추가된 객체는 버려진 것이다.

다른 기본형에도 동일하게 객체 래퍼 타입이 존재한다. 이 래퍼 타입들 덕분에 기본형 값에 메서드를 사용할 수 있고, 정적 메서드도 사용할 수 있다. 그러나 보통은 래퍼 객체를 직접 생성할 필요는 없다.

TypeScript는 기본형과 객체 래퍼 타입을 별도로 모델링한다.

- `string`과 `String`
- `number`와 `Number`
- `boolean`과 `Boolean`
- `symbol`과 `Symbol`
- `bigint`와 `BigInt`

**그런데 `string`을 사용할 땐 특히 유의해야 한다.** 오타를 내기 쉽고, 실수를 하더라도 처음에는 잘 동작하는 것처럼 보이기 때문이다.

```typescript
function getStringLen(foo: String) {
  return foo.length;
}

getStringLen('hello'); // Ok
getStringLen(new String('hello')); // 정상

function isGreeting(phrase: String) {
  return [
    'hello',
    'good day'
  ].includes(phrase);
  // 'String' 형식의 인수는
  // 'string' 형식의 매개변수에 할당될 수 없습니다.
  // 'string'은(는) 기본 개체이지만 'String'은(는) 래퍼 개체입니다.
  // 가능한 경우 'string'을 사용하세요.
}
```

**`string`은 `String`에 할당할 수 있지만 `String`은 `string`에 할당할 수 없다.** 대부분의 라이브러리와 마찬가지로 TypeScript가 제공하는 타입 선언은 전부 기본형 타입으로 되어 있다.

래퍼 객체는 타입 구문의 첫 글자를 대문자로 표기하는 방법으로도 사용할 수 있다.

```typescript
const s: String = "primitive";
const n: Number = 12;
const b: Boolean = true;
```

**당연히 런타임의 값은 객체가 아니고 기본형이다.** 그러나 기본형 타입은 객체 래퍼에 할당할 수 있기 때문에 TypeScript는 기본형 타입을 객체 래퍼에 할당하는 선언을 허용한다. 그러나 기본형 타입을 객체 래퍼에 할당하는 구문은 오해하기 쉽고, 굳이 그렇게 할 필요도 없다. 그냥 기본형 타입을 사용하는 것이 낫다.

그런데 `new` 없이 `BigInt`와 `Symbol`을 호출하는 경우는 기본형을 생성하기 때문에 사용해도 좋다.

이들은 값이지만, TypeScript 타입은 아니다.

## 요약
- 기본형 값에 메서드를 제공하기 위해 객체 래퍼 타입이 어떻게 쓰이는지 이해해야 한다. 직접 사용하거나 인스턴스를 생성하는 것은 피해야 한다.
- TypeScript 객체 래퍼 타입은 지양하고, 대신 기본형 타입을 사용해야 한다. `String` 대신 `string`, `Number` 대신 `number`, `Boolean` 대신 `boolean`, `Symbol` 대신 `symbol`, `BigInt` 대신 `bigint`를 사용해야 한다.