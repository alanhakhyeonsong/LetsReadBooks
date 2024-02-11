# 아이템 13. 타입과 인터페이스의 차이점 알기
TypeScript에서 명명된 타입(named type)을 정의하는 방법은 두 가지가 있다.

```typescript
type TState = {
  name: string;
  capital: string;
}

interface IState {
  name: string;
  capital: string;
}
```

명명된 타입을 정의할 때 인터페이스 대신 클래스를 사용할 수도 있지만, 클래스는 값으로도 쓰일 수 있는 JavaScript 런타임의 개념이다.

대부분의 경우엔 타입을 사용해도 되고 인터페이스를 사용해도 된다. 그러나 타입과 인터페이스 사이에 존재하는 차이를 분명하게 알고, 같은 상황에선 동일한 방법으로 명명된 타입을 정의해 일관성을 유지해야 한다. 그러려면 하나의 타입에 대해 두 가지 방법을 모두 사용해서 정의할 줄 알아야 한다.

## 타입과 인터페이스의 공통점
### 명명된 타입은 인터페이스로 정의하든 타입으로 정의하든 상태엔 차이가 없다.
`IState`, `TState`를 추가 속성과 함께 할당한다면 동일한 오류가 발생한다.

```typescript
const wyoming: TState = {
  name: 'Wyoming',
  capital: 'Cheyenne',
  population: 500_000
  // ... 형식은 'TState' 형식에 할당할 수 없습니다.
  // 개체 리터럴은 알려진 속성만 지정할 수 있으며
  // 'TState' 형식에 'population'이(가) 없습니다.
};
```

### 인덱스 시그니처는 인터페이스와 타입에서 모두 사용할 수 있다.
```typescript
type TDict: { [key: string]: string };
interface IDict {
  [key: string]: string;
}
```

### 함수 타입도 인터페이스나 타입으로 정의할 수 있다.
```typescript
type TFn = (x: number) => string;
interface IFn {
  (x: number): string;
}

const toStrT: TFn = x => '' + x; // 정상
const toStrI: IFn = x => '' + x; // 정상
```

```typescript
type TFnWithProperties = {
  (x: number): number;
  prop: string;
}
interface IFnWithProperties {
  (x: number): number;
  prop: string;
}
```

### 타입 별칭과 인터페이스는 모두 제너릭이 가능하다.
```typescript
type TPair<T> = {
  first: T;
  second: T;
}
interface IPair<T> {
  first: T;
  second: T;
}
```

### 인터페이스는 타입을 확장할 수 있으며, 타입은 인터페이스를 확장할 수 있다.
```typescript
interface IStateWithPop extends TState {
  population: number;
}
type TStateWithPop = IState & { population: number; };
```

`IStateWithPop`과 `TStateWithPop`은 동일하다. 여기서 주의할 점은 인터페이스는 유니온 타입 같은 복잡한 타입을 확장하진 못한다는 것이다. 복잡한 타입을 확장하고 싶다면 타입과 `&`를 사용해야 한다.

한편 클래스를 구현할 때는, 타입과 인터페이스 둘 다 사용할 수 있다.

```typescript
class StateT implements TState {
  name: string = '';
  capital: string = '';
}

class StateI implements IState {
  name: string = '';
  capital: string = '';
}
```

## 타입과 인터페이스의 차이점
### 유니온 타입은 있지만 유니온 인터페이스라는 개념은 없다.
```typescript
type AorB = 'a' | 'b';
```

### 인터페이스는 타입을 확장할 수 있지만, 유니온은 할 수 없다.
유니온 타입을 확장하는 게 필요할 때가 있다.

```typescript
type Input = { /** ... */ };
type Output = { /** ... */ };
interface VariableMap = {
  [name: string]: Input | Output;
}
```

```typescript
type NamedVariable = (Input | Output) & { name: string };
```

위 타입은 인터페이스로 표현할 수 없다. `type` 키워드는 일반적으로 `interface`보다 쓰임새가 많다. `type` 키워드는 유니온이 될 수도 있고, 매핑된 타입 또는 조건부 타입 같은 고급 기능에 활용되기도 한다.

### 튜플과 배열타입
튜플과 배열 타입도 `type` 키워드를 이용해 더 간결하게 표현할 수 있다.

```typescript
type Pair = [number, number];
type StringList = string[];
type NamedNums = [string, ...number[]];
```

```typescript
interface Tuple {
  0: number;
  1: number;
  length: 2;
}
const t: Tuple = [10, 20]; // 정상
```

그러나 인터페이스로 튜플과 비슷하게 구현하면 튜플에서 사용할 수 있는 `concat` 같은 메서드를 사용할 수 없다. 따라서 튜플은 `type`으로 구현하는 것이 낫다.

### 인터페이스에는 타입에 없는 몇 가지 기능이 있다.
보강이 가능하다.

```typescript
interface IState {
  name: string;
  capital: string;
}
interface IState {
  population: number;
}
const wyoming: IState = {
  name: 'Wyoming',
  capital: 'Cheyenne',
  population: 500_000
}; // 정상
```

위 예제처럼 속성을 확장하는 것을 **선언 병합**이라 한다. 이는 주로 타입 선언 파일에서 사용된다.

타입 선언 파일을 작성할 때는 선언 병합을 지원하기 위해 반드시 인터페이스를 사용해야 하며 표준을 따라야 한다. 타입 선언에는 사용자가 채워야 하는 빈틈이 있을 수 있는데, 바로 이 선언 병합이 그렇다.

TypeScript는 여러 버전의 JavaScript 표준 라이브러리에서 여러 타입을 모아 병합한다.

## 타입과 인터페이스 중 어느 것을 사용해야 하나?
- 복잡한 타입이라면 타입 별칭을 사용한다.
- 타입과 인터페이스, 두 가지 방법으로 모두 표현할 수 있는 간단한 객체 타입이라면 일관성과 보강의 관점에서 고려해 봐야 한다.
  - 일관되게 인터페이스를 사용하는 코드베이스에서 작업하고 있다면 인터페이스를 사용하고, 일관되게 타입을 사용 중이라면 타입을 사용하면 된다.
- 아직 스타일이 확립되지 않은 프로젝트라면, 향후에 보강의 가능성이 있을 지 생각해봐야 한다.
- 어떤 API에 대한 타입 선언을 작성해야 한다면 인터페이스를 사용하는 게 좋다.
- 프로젝트 내부적으로 사용되는 타입에 선언 병합이 발생하는 것은 잘못된 설계다. 이럴 때는 타입을 사용해야 한다.

## 정리
- 타입과 인터페이스의 차이점과 비슷한 점을 이해해야 한다.
- 한 타입을 `type`과 `interface` 두 가지 문법을 사용해서 작성하는 방법을 터득해야 한다.
- 프로젝트에서 어떤 문법을 사용할지 결정할 때 한 가지 일관된 스타일을 확립하고, 보강 기법이 필요한지 고려해야 한다.