# 아이템 8. 타입 공간과 값 공간의 심벌 구분하기
TypeScript의 심벌은 타입 공간이나 값 공간 중의 한 곳에 존재한다.

심벌은 이름이 같더라도 속하는 공간에 따라 다른 것을 나타낼 수 있기 때문에 혼란스러울 수 있다.

```typescript
interface Cylinder {
  radius: number
  height: number
}

const Cylinder = (radius: number, height: number) => ({ radius, height })
```

`interface Cylinder`에서 `Cylinder`는 타입으로 쓰인다. `const Cylinder`에서 `Cylinder`와 이름은 같지만 값으로 쓰이며, 서로 아무런 관련도 없다. 상황에 따라 타입으로 쓰일 수도 있고, 값으로 쓰일 수도 있다. 이런 점이 가끔 오류를 야기한다.

```typescript
function calculateVolume(shape: unknown) {
  if (shape instanceof Cylinder) {
    shape.radius
    // ~~~~~~ Property 'radius' does not exist on type '{}'
  }
}
```

아마 `instanceof`를 이용해 `shape`가 `Cylinder` 타입인지 체크하려 했을 것이다. 그러나 `instanceof`는 JavaScript의 런타임 연산자이고, 값에 대해 연산을 한다. 그래서 `instanceof Cylinder`는 타입이 아니라 함수를 참조한다.

한 심벌이 타입인지 값인진 언뜻 봐서 알 수 없다. 어떤 형태로 쓰이는지 문맥을 살펴 알아내야 한다. 많은 타입 코드가 값 코드와 비슷해 보이기 때문에 더더욱 혼란스럽다.

```typescript
type T1 = 'string literal'
type T2 = 123
const v1 = 'string literal'
const v2 = 123
```

**일반적으로 `type`이나 `interface` 다음에 나오는 심벌은 타입인 반면, `const`나 `let` 선언에 쓰이는 것은 값이다.**

TypeScript 코드에서 타입과 값은 번갈아 나올 수 있다. 타입 선언 또는 단언문 다음에 오는 심벌은 타입인 반면, `=` 다음에 나오는 모든 것은 값이다.

```typescript
interface Person {
  first: string;
  last: string;
}
const p: Person = { first: 'Jane', last: 'Jacobs' }
//    -           --------------------------------- Values
//       ------ Type
```

일부 함수에선 타입과 값이 반복적으로 번갈아 가며 나올 수도 있다.

`class`와 `enum`은 상황에 따라 타입과 값 두 가지 모두 가능한 예약어다.

```typescript
class Cylinder {
  radius = 1;
  height = 1;
}

function calculateVolume(shape: unknown) {
  if (shape instanceof Cylinder) {
    shape // 정상, 타입은 Cylinder
    shape.radius // 정상, 타입은 number
  }
}
```

클래스가 타입으로 쓰일 때는 형태(속성과 메서드)가 사용되는 반면, 값으로 쓰일 때는 생성자가 사용된다.

한편, 연산자 중에서도 타입에서 쓰일 때와 값에서 쓰일 때 다른 기능을 하는 것들이 있다. 예시로 `typeof`를 들 수 있다.

```typescript
type T1 = typeof p // Type is Person
type T2 = typeof email
// Type is (p: Person, subject: string, body: string) => Response

const v1 = typeof p // Value is "object"
const v2 = typeof email // Value is "function"
```

타입의 관점에서, `typeof`는 값을 읽어 TypeScript 타입을 반환한다. 타입 공간의 `typeof`는 보다 큰 타입의 일부분으로 상요할 수 있고, `type` 구문으로 이름을 붙이는 용도로도 사용할 수 있다.

값의 관점에선 `typeof`는 JavaScript 런타임의 `typeof` 연산자가 된다. 값 공간의 `typeof`는 대상 심벌의 런타임 타입을 가리키는 문자열을 반환하며, TypeScript 타입과는 다르다. JavaScript의 런타임 타입 시스템은 TypeScript의 정적 타입 시스템보다 훨씬 간단하다.

```typescript
const v = typeof Cylinder // Value is "function"
type T = typeof Cylinder // Type is typeof Cylinder

declare let fn: T
const c = new fn() // Type is Cylinder
// 생성자 함수

type C = InstanceType<typeof Cylinder> // Type is Cylinder
```

인덱스 위치에는 유니온 타입과 기본형 타입을 포함한 어떠한 타입이든 사용할 수 있다.

```typescript
type PersonEl = Person['first' | 'last'] // Type is string
type Tuple = [string, number, Date]
type TupleEl = Tuple[number] // Type is string | number | Date (number가 0, 1, 2 중에 하나만 올 수 있으니까)
```

두 공간 사이에서 다른 의미를 가지는 코드 패턴들이 있다.

- 값으로 쓰이는 `this`는 JavaScript의 `this` 키워드다. 타입으로 쓰이는 `this`는, 일명 '다형성 `this`'라 불리는 `this`의 TypeScript 타입이다. 서브클래스의 메서드 체인을 구현할 때 유용하다.
- 값에서 `&`와 `|`는 AND와 OR 비트 연산이다. 타입에선 인터섹션과 유니온이다.
- `const`는 새 변수를 선언하지만, `as const`는 리터럴 또는 리터럴 표현식의 추론된 타입을 바꾼다.
- `extends`는 서브클래스 또는 서브타입 또는 제네릭 타입의 한정자를 정의할 수 있다.
- `in`은 루프 또는 매핑된 타입에 등장한다.

JavaScript에선 객체 내의 각 속성을 로컬 변수로 만들어 주는 구조 분해 할당을 사용할 수 있다. 그런데 TypeScript에서 구조 분해 할당을 하면, 이상한 오류가 발생한다.

```typescript
interface Person {
  first: string
  last: string
}
function email({
  person: Person,
  // ~~~~~~ Binding element 'Person' implicitly has an 'any' type
  subject: string,
  // ~~~~~~ Duplicate identifier 'string'
  //        Binding element 'string' implicitly has an 'any' type
  body: string,
  // ~~~~~~ Duplicate identifier 'string'
  //        Binding element 'string' implicitly has an 'any' type
}) {
  /* ... */
}
```

값의 관점에서 `Person`과 `string`이 해석되었기 때문에 오류가 발생했다. 문제를 해결하려면 타입과 값을 구분해야 한다.

## 요약
- TypeScript 코드를 읽을 때 타입인지 값인지 구분하는 방법을 터득해야 한다.
- 모든 값은 타입을 가지지만, 타입은 값을 가지지 않는다. `type`과 `interface` 같은 키워드는 타입 공간에만 존재한다.
- `class`나 `enum` 같은 키워드는 타입과 값 두 가지로 사용될 수 있다.
- `"foo"`는 문자열 리터럴이거나, 문자열 리터럴 타입일 수 있다. 차이점을 알고 구별하는 방법을 터득해야 한다.
- `typeof`, `this` 그리고 많은 연산자들과 키워드들은 타입 공간과 값 공간에서 다른 목적으로 사용될 수 있다.