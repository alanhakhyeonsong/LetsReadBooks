# 아이템 9. 타입 단언보다는 타입 선언을 사용하기
TypeScript에서 변수에 값을 할당하고 타입을 부여하는 방법은 두 가지다.

```typescript
interface Person { name: string };

const alice: Person = { name: 'Alice' };
const bob = { name: 'Bob' } as Person;
```

위 두 가지 방법은 결과가 같아 보이지만 그렇지 않다.

- 첫 번째: 변수에 '타입 선언'을 붙여 그 값이 선언된 타입임을 명시한다.
- 두 번째: '타입 단언'을 수행한다. TypeScript가 추론한 타입이 있더라도 `Person` 타입으로 간주한다.

**타입 단언보다 타입 선언을 사용하는 게 낫다.**

```typescript
const alice: Person = {};
// 'Person' 유형에 필요한 'name' 속성이 '{}' 유형에 없습니다.
const bob: {} as Person; // 오류 없음
```

타입 선언은 할당되는 값이 해당 인터페이스를 만족하는지 검사한다. 앞 예제에선 그러지 못했기 때문에 TypeScript가 오류를 표시했다. 타입 단언은 강제로 타입을 지정했으니 타입 체커에게 오류를 무시하라고 하는 것이다.

타입 선언과 단언의 차이는 속성을 추가할 때도 마찬가지다.

```typescript
const alice: Person = {
  name: 'Alice',
  occupation: 'TypeScript developer'
  // 개체 리터럴은 알려진 속성만 지정할 수 있으며 'Person' 형식에 'occupation'이 없습니다.
};
const bob = {
  name: 'Bob',
  occupation: 'JavaScript developer'
} as Person; // 오류 없음
```

타입 선언문에선 잉여 속성 체크가 동작했지만, 단언문에선 적용되지 않는다. 타입 단언이 꼭 필요한 경우가 아니라면, 안전성 체크도 되는 타입 선언을 사용하는 것이 좋다.

화살표 함수의 타입 선언은 추론된 타입이 모호할 때가 있다.

```typescript
const people = ['alice', 'bob', 'jan'].map(name => ({name}));
// Person[]을 원했지만 결과는 { name: string; }[]...
```

`{name}`에 타입 단언을 쓰면 문제가 해결되는 것처럼 보인다.

```typescript
const people = ['alice', 'bob', 'jan'].map(
  name => ({name} as Person)
); // 타입은 Person[]
```

그러나 타입 단언을 사용하면 런타임에 문제가 발생하게 된다.

```typescript
const people = ['alice', 'bob', 'jan'].map(name => ({} as Person)); // 오류 없음
```

단언문을 쓰지 않고, 다음과 같이 화살표 함수 안에서 타입과 함께 변수를 선언하는 것이 가장 직관적이다.

```typescript
const people = ['alice', 'bob', 'jan'].map(name => {
  const person: Person = {name};
  return person;
}); // 타입은 Person[]
```

그러나 원래 코드에 비해 꽤 번잡하게 보인다. 코드를 좀 더 간결하게 보이기 위해 변수 대신 화살표 함수의 반환 타입을 선언해보자.

```typescript
const people = ['alice', 'bob', 'jan'].map(
  (name): Person => ({name})
); // 타입은 Person[]
```

소괄호는 매우 중요한 의미를 지닌다. `(name): Person`은 `name`의 타입이 없고, 반환 타입이 `Person`이라 명시한다. 그러나 `(name: Person)`은 `name`의 타입이 `Person`임을 명시하고 반환 타입은 없기 때문에 오류가 발생한다.

아래 코드는 최종적으로 원하는 타입을 직접 명시하고, TypeScript가 할당문의 유효성을 검사하게 한다.

```typescript
const people: Person[] = ['alice', 'bob', 'jan'].map(
  (name): Person => ({name})
);
```

그러나 함수 호출 체이닝이 연속되는 곳에선 체이닝 시작에서부터 명명된 타입을 가져야 한다. 그래야 정확한 곳에 오류가 표시된다.

타입 단언이 꼭 필요한 경우를 살펴보자. **타입 단언은 타입 체커가 추론한 타입보다 우리가 판단하는 타입이 더 정확할 때 의미가 있다.**

```typescript
document.querySelector('#myButton').addEventListener('click', e => {
  e.currentTarget // 타입은 EventTarget
  const button = e.currentTarget as HTMLButtonElement;
  button // 타입은 HTMLButtonElement
});
```

TypeScript는 DOM에 접근할 수 없기 때문에 `#myButton`이 버튼 앨리먼트인지 알지 못한다. 그리고 이벤트의 `currentTarget`이 같은 버튼이어야 하는 것도 알지 못한다. 우리는 TypeScript가 알지 못하는 정보를 가지고 있기 때문에 여기선 타입 단언문을 쓰는 것이 타당하다.

또한 자주 쓰이는 특별한 문법(`!`)을 사용해서 `null`이 아님을 단언하는 경우도 있다.

```typescript
const elNull = document.getElementById('foo'); // 타입은 HTMLElement | null
const el = document.getElementById('foo')!; // 타입은 HTMLElement
```

변수의 접두사로 쓰인 `!`는 `boolean`의 부정문이다. 그러나 접미사로 쓰인 `!`는 그 값이 `null`이 아니라는 단언문으로 해석된다. 우리는 `!`를 일반적인 단언문처럼 생각해야 한다. 단언문은 컴파일 과정 중에 제거되므로, 타입 체커는 알지 못하지만 그 값이 `null`이 아니라고 확신할 수 있을 때 사용해야 한다. 만약 그렇지 않다면 `null`인 경우를 체크하는 조건문을 사용해야 한다.

타입 단언문으로 임의의 타입 간에 변환을 할 수는 없다. `A`가 `B`의 부분 집합인 경우에 타입 단언문을 사용해 변환할 수 있다. `HTMLElement`는 `HTMLElement | null`의 서브타입이기 때문에 이러한 타입 단언은 동작한다. `HTMLButtonElement`는 `EventTarget`의 서브타입이기 때문에 역시 동작한다. 그리고 `Person`은 `{}`의 서브타입이므로 동작한다.

그러나 `Person`과 `HTMLElement`는 서로의 서브타입이 아니기 때문에 변환이 불가능하다.

```typescript
interface Person { name: string; }
const body = document.body;
const el = body as Person;
// 'HTMLElement' 형식을 'Person' 형식으로 변환하는 것은
// 형식이 다른 형식과 충분히 겹치지 않기 때문에
// 실수일 수 있습니다. 이것이 의도적인 경우에는 먼저 식을 'unknown'으로 변환하십시오.
```

이 오류를 해결하려면 `unknown` 타입을 사용해야 한다. 모든 타입은 `unknown` 타입의 서브타입이기 때문에 이 타입이 포함된 단언문은 항상 동작한다. `unknown` 단언은 임의의 타입 간에 변환을 가능케 하지만, `unknown`을 사용한 이상 적어도 무언가 위험한 동작을 하고 있다는 걸 알 수 있다.

## 요약
- 타입 단언(`as Type`)보다 타입 선언(`: Type`)을 사용해야 한다.
- 화살표 함수의 반환 타입을 명시하는 방법을 터득해야 한다.
- TypeScript보다 타입 정보를 더 잘 알고 있는 상황에선 타입 단언문과 `null` 아님 단언문을 사용하면 된다.