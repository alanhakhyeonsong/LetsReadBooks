# 아이템 14. 타입 연산과 제너릭 사용으로 반복 줄이기
```typescript
console.log('Cylinder 1 X 1',
  'Surface area:', 6.283185 * 1 * 1 + 6.283185 * 1 * 1,
  'Volume:', 3.14159 * 1 * 1 * 1);
console.log('Cylinder 1 X 2',
  'Surface area:', 6.283185 * 1 * 1 + 6.283185 * 2 * 1,
  'Volume:', 3.14159 * 1 * 2 * 1);
console.log('Cylinder 2 X 1',
  'Surface area:', 6.283185 * 2 * 1 + 6.283185 * 2 * 1,
  'Volume:', 3.14159 * 2 * 2 * 1);
```

비슷한 코드가 반복되어 보기 불편하다. 값과 상수가 반복되는 바람에 드러나지 않은 오류까지 가지고 있다. 이 코드에서 반복을 제거해보자.

```typescript
const surfaceArea = (r, h) => 2 * Math.PI * r * (r + h);
const volume = (r, h) => Math.PI * r * r * h;
for (const [r, h] of [[1, 1], [1, 2], [2, 1]]) {
  console.log(
    `Cylinder ${r} X ${h}`,
    `Surface area: ${surfaceArea(r, h)}`,
    `Volume: ${volume(r, h)}`
  );
}
```

반복된 코드를 제거하며 DRY 원칙을 지켜왔던 개발자라도 타입에 대해서는 간과했을 수 있다.

```typescript
interface Person {
  firstName: string;
  lastName: string;
}

interface PersonWithBirthDate {
  firstName: string;
  lastName: string;
  birth: Date;
}
```

타입 중복은 코드 중복만큼 많은 문제를 발생시킨다. 예를 들어 선택적 필드인 `middleName`을 `Person`에 추가한다 가정해보자. 그러면 `Person`과 `PersonWithBirthDate`는 다른 타입이 된다.

타입에서 증복이 더 흔한 이유 중 하나는 공유된 패턴을 제거하는 메커니즘이 기존 코드에서 하던 것과 비교해 덜 익숙하기 때문이다. 타입 간에 매핑하는 방법을 익히면, 타입 정의에서도 DRY의 장점을 적용할 수 있다.

**반복을 줄이는 가장 간단한 방법은 타입에 이름을 붙이는 것이다.**

```typescript
// AS-IS
function distance(a: {x: number, y: number}, b: {x: number, y: number}) {
  return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
}

// TO-BE
interface Point2D {
  x: number;
  y: number;
}
function distance(a: Point2D, b: Point2D) { /* ... */ }
```

이 코드는 상수를 사용해 반복을 줄이는 기법을 동일하게 타입 시스템에 적용한 것이다. 중복된 타입은 종종 문법에 의해 가려지기도 한다. 몇몇 함수가 같은 타입 시그니처를 공유하고 있다면 해당 시그니처를 명명된 타입으로 분리해 낼 수 있다.

```typescript
type HTTPFunction = (url: string, opts: Options) => Promise<Response>;
const get: HTTPFunction = (url, opts) => { /* ... */ };
const post: HTTPFunction = (url, opts) => { /* ... */ };
```

`Person/PersonWithBirthDate` 예제에선 한 인터페이스가 다른 인터페이스를 확장하게 해서 반복을 제거할 수 있다.

```typescript
interface Person {
  firstName: string;
  lastName: string;
}

interface PersonWithBirthDate extends Person {
  birth: Date;
}
```

만약 두 인터페이스가 필드의 부분 집합을 공유한다면, 공통 필드만 골라서 기반 클래스로 분리해 낼 수 있다. 이미 존재하는 타입을 확장하는 경우에, 일반적이진 않지만 인터섹션 연산자를 쓸 수도 있다.

```typescript
type PersonWithBirthDate = Person & { birth: Date };
```

이런 기법은 유니온 타입(확장할 수 없는)에 속성을 추가하려 할 때 특히 유용하다.

다른 측면을 생각해보자. 전체 애플리케이션의 상태를 표현하는 `State` 타입과 단지 부분만 표현하는 `TopNavState`가 있는 경우를 살펴보자.

```typescript
interface State {
  userId: string;
  pageTitle: string;
  recentFiles: string[];
  pageContents: string;
}
interface TopNavState {
  userId: string;
  pageTitle: string;
  recentFiles: string[];
}
```

`TopNavState`를 확장하여 `State`를 구성하기보다, `State`의 부분 집합으로 `TopNavState`를 정의하는 것이 바람직해 보인다. 이 방법이 전체 앱의 상태를 하나의 인터페이스로 유지할 수 있게 해준다. `State`를 인덱싱하여 속성의 타입에서 중복을 제거할 수 있다.

```typescript
type TopNavState = {
  userId: State['userId'];
  pageTitle: State['pageTitle'];
  recentFiles: State['recentFiles'];
}
```

중복 제거가 아직 끝나지 않았다. `State` 내의 `pageTitle`의 타입이 바뀌면 `TopNavState`에도 반영된다. 그러나 여전히 반복되는 코드가 존재한다. 이 때, '매핑된 타입'을 사용하면 좀 더 나아진다.

```typescript
type TopNavState = {
  [k in 'userId' | 'pageTitle' | 'recentFiles']: State[k]
};
```

**매핑된 타입은 배열의 필드를 루프 도는 것과 같은 방식이다. 이 패턴은 표준 라이브러리에서도 일반적으로 찾을 수 있으며, `Pick`이라고 한다.**

```typescript
type Pick<T, K> = { [k in K]: T[k] };

// 다음과 같이 사용할 수 있다.
type TopNavState = Pick<State, 'userId' | 'pageTitle' | 'recentFiles'>;
```

여기서 `Pick`은 제네릭 타입이다. 중복된 코드를 없앤다는 관점으로, `Pick`을 사용하는 것은 함수를 호출하는 것에 비유할 수 있다. 마치 함수에서 두 개의 매개변수 값을 받아 결괏값을 반환하는 것처럼, `Pick`은 `T`, `K` 두 가지 타입을 받아 결과 타입을 반환한다.

태그된 유니온에서도 다른 형태의 중복이 발생할 수 있다.

```typescript
interface SaveAction {
  type: 'save';
  // ...
}
interface LoadAction {
  type: 'load';
  // ...
}

type Action = SaveAction | LoadAction;
type ActionType = 'save' | 'load'; // 타입의 반복
```

`Action` 유니온을 인덱싱하면 타입 반복 없이 `ActionType`을 정의할 수 있다.

```typescript
type ActionType = Action['type'];
```

`Action` 유니온에 타입을 더 추가하면 `ActionType`은 자동적으로 그 타입을 포함한다. `ActionType`은 `Pick`을 사용하여 얻게 되는, `type` 속성을 가지는 인터페이스와는 다르다.

```typescript
type ActionRec = Pick<Action, 'type'>; // {type: 'save' | 'load'}
```

한편 생성하고 난 다음에 업데이트가 되는 클래스를 정의한다면, `update` 메서드 매개변수의 타입은 생성자와 동일한 매개변수이면서, 타입 대부분이 선택적 필드가 된다.

```typescript
interface Options {
  width: number;
  height: number;
  color: string;
  label: string;
}
interface OptionsUpdate {
  width?: number;
  height?: number;
  color?: string;
  label?: string;
}
class UIWidget {
  constructor(init: Options) { /* ... */ }
  update(options: OptionsUpdate) { /* ... */ }
}
```

매핑된 타입과 `keyof`를 사용하면 `Options`로부터 `OptionsUpdate`를 만들 수 있다.

```typescript
type OptionsUpdate = {[k in keyof Options]?: Options[k]};
```

`keyof`는 타입을 받아 속성 타입의 유니온을 반환한다.

```typescript
type OptionsKeys = keyof Options;
// 'width' | 'height' | 'color' | 'label'
```

매핑된 타입(`[k in keyof Options]`)은 순회하며 `Options` 내 `k` 값에 해당하는 속성이 있는지 찾는다. `?`는 각 속성을 선택적으로 만든다. 이 패턴 역시 아주 일반적이며 표준 라이브러리에 `Partial`이라는 이름으로 포함되어 있다.

```typescript
class UIWidget {
  constructor(init: Options) { /* ... */ }
  update(options: Partial<Options>) { /* ... */ } 
}
```

**값으로부터 타입을 만들어 낼 때는 선언의 순서에 주의해야 한다. 타입 정의를 먼저 하고 값이 그 타입에 할당 가능하다고 선언하는 것이 좋다.** 그렇게 해야 타입이 더 명확해지고, 예상하기 어려운 타입 변동을 방지할 수 있다.

함수나 메서드의 반환 값에 명명된 타입을 만들고 싶을 수도 있다.

```typescript
function getUserInfo(userId: string) {
  // ...
  return {
    userId,
    name,
    age,
    height,
    weight,
    favoriteColor,
  };
}
// 추론된 반환 타입은 { userId: string; name: string; age: number, ... }
```

이때는 조건부 타입이 필요하다. 그러나 표준 라이브러리에는 이러한 일반적 패턴의 제너릭 타입이 정의되어 있다. 이런 경우 `ReturnType` 제네릭이 정확히 들어맞다.

```typescript
type UserInfo = ReturnType<typeof getUserInfo>;
```

`ReturnType`은 함수의 '값'인 `getUserInfo`가 아니라 함수의 '타입'인 `typeof getUserInfo`에 적용되었다. `typeof`와 마찬가지로 이런 기법은 신중하게 사용해야 한다. 적용 대상이 값인지 타입인지 정확히 알고, 구분해서 처리해야 한다.

**제너릭 타입은 타입을 위한 함수와 같다. 그리고 함수는 코드에 대한 DRY 원칙을 지킬 때 유용하게 사용된다.** 따라서 타입에 대한 DRY 원칙의 핵심이 제너릭이라는 것은 어쩌면 당연하지만, 간과한 부분이 있다.  
함수에서 매개변수로 매핑할 수 있는 값을 제한하기 위해 타입 시스템을 사용하는 것처럼 제너릭 타입에서 매개변수를 제한할 수 있는 방법이 필요하다. `extends`를 사용하는 것이다.

```typescript
interface Name {
  first: string;
  last: string;
}
type DancingDuo<T extends Name> = [T, T];

const couple1: DancingDuo<Name> = [
  {first: 'Fred', last: 'Astaire'},
  {first: 'Ginger', last: 'Rogers'},
]; // Ok
const couple2: DancingDuo<first: string> = [
  // 'Name' 타입에 필요한 'last' 속성이 '{ first: string }' 타입에 없습니다.
  {first: 'Sonny'},
  {first: 'Cher'},
]
```

앞에 나온 `Pick`의 정의는 `extends`를 사용해서 완성할 수 있다. 타입 체커를 통해 기존 예제를 실행해 보면 오류가 발생한다.

```typescript
type Pick<T, K extends keyof T> = {
  [k in K]: T[k]
}; // 정상
```

타입이 값의 집합이라는 관점에서 생각하면 `extends`를 '확장'이 아니라 '부분 집합'이라는 걸 이해하는 데 도움이 될 것이다.

## 요약
- DRY 원칙을 타입에도 최대한 적용해야 한다.
- 타입에 이름을 붙여 반복을 피해야 한다. `extends`를 사용해서 인터페이스 필드의 반복을 피해야 한다.
- 타입들 간의 매핑을 위해 TypeScript가 제공한 도구를 공부하면 좋다. 여기엔 `keyof`, `typeof`, 인덱싱, 매핑된 타입들이 포함된다.
- 제너릭 타입은 타입을 위한 함수와 같다. 타입을 반복하는 대신 제너릭 타입을 사용하여 타입들 간에 매핑을 하는 것이 좋다. 제너릭 타입을 제한하려면 `extends`를 사용하면 된다.
- 표준 라이브러리에 정의된 `Pick`, `Partial`, `ReturnType` 같은 제너릭 타입에 익숙해져야 한다.