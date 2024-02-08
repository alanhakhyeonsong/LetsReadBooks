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