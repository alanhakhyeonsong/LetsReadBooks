# 아이템 11. 잉여 속성 체크의 한계 인지하기
타입이 명시된 변수에 객체 리터럴을 할당할 때 TypeScript는 해당 타입의 속성이 있는지, 그리고 '그 외의 속성은 없는지' 확인한다.

```typescript
interface Room {
  numDoors: number;
  ceilingHeightFt: number;
}
const r: Room = {
  numDoors: 1,
  ceilingHeightFt: 10,
  elephant: 'present',
  // 개체 리터럴은 알려진 속성만 지정할 수 있으며
  // 'Room' 형식에 'elephant'이(가) 없습니다.
};
```

```typescript
const obj = {
  numDoors: 1,
  ceilingHeightFt: 10,
  elephant: 'present',
};
const r: Room = obj; // 정상
```

`obj` 타입은 `Room` 타입의 부분 집합을 포함하므로, `Room`에 할당 가능하며 타입 체커도 통과한다.

첫 번째 예제에선, 구조적 타입 시스템에서 발생할 수 있는 중요한 종류의 오류를 잡을 수 있도록 '잉여 속성 체크'라는 과정이 수행되었다. 그러나 잉여 속성 체크 역시 조건에 따라 동작하지 않는다는 한계가 있고, 통상적인 할당 가능 검사와 함께 쓰이면 구조적 타이핑이 무엇인지 혼란스러워질 수 있다.

잉여 속성 체크가 할당 가능 검사와는 별도의 과정이라는 것을 알아야 TypeScript 타입 시스템에 대한 개념을 정확히 잡을 수 있다.

TypeScript는 단순히 런타임에 예외를 던지는 코드에 오류를 표시하는 것뿐 아니라, 의도와 다르게 작성된 코드까지 찾으려고 한다.

```typescript
interface Options {
  title: string;
  darkMode?: boolean;
}
function createWindow(options: Options) {
  if (options.darkMode) {
    setDarkMode();
  }
  // ...
}
createWindow({
  title: 'Spider Solitaire',
  darkmode: true,
  // 개체 리터럴은 알려진 속성만 지정할 수 있지만
  // 'Options' 형식에 'darkmode'이(가) 없습니다.
  // 'darkMode'을(를) 쓰려고 했습니까?
});
```

위 코드를 실행하면 런타임에 어떠한 종류의 오류도 발생하지 않는다. 그러나 TypeScript가 알려 주는 오류 메시지처럼 의도한 대로 동작하지 않을 수 있다.

`Options` 타입은 범위가 매우 넓기 때문에, 순수한 구조적 타입 체커는 이런 종류의 오류를 찾아내지 못한다. `darkMode` 속성에 `boolean` 타입이 아닌 다른 타입의 값이 지정된 경우를 제외하면, `string` 타입인 `title` 속성과 '또 다른 어떤 속성'을 가지는 모든 객체는 `Options` 타입의 범위에 속한다.

TypeScript 타입은 범위가 아주 넓어질 수 있다.

```typescript
const o1: Options = document;
const o2: Options = new HTMLAnchorElement;
```

잉여 속성 체크를 이용하면 기본적으로 타입 시스템의 구조적 본질을 해치지 않으면서도 객체 리터럴에 알 수 없는 속성을 허용하지 않음으로써, 앞에서 다룬 `Room`이나 `Options` 예제 같은 문제점을 방지할 수 있다. (그래서 엄격한 객체 리터럴 체크라고도 불림)

```typescript
const o: Options = { darkmode: true, title: 'Ski Free' };
// 'Options' 형식에 'darkmode'이(가) 없습니다.

const intermediate = { darkmode: true, title: 'Ski Free' };
const o: Options = intermediate; // 정상

const o = { darkmode: true, title: 'Ski Free' } as Options; // 정상
// 단언문보다 선언문을 사용해야 하는 단적인 이유 중 하나
```

잉여 속성 체크를 원치 않는다면, 인덱스 시그니처를 사용해 TypeScript가 추가적인 속성을 예상하도록 할 수 있다.

```typescript
interface Options {
  darkMode?: boolean;
  [otherOptions: string]: unknown;
}
const o: Options = { darkmode: true }; // 정상
```

선택적 속성만 가지는 약한 타입에도 비슷한 체크가 동작한다.

```typescript
interface LineChartOptions {
  logscale?: boolean;
  invertedYAxis?: boolean;
  areaChart?: boolean;
}
const opts = { logScale: true };
const o: LineChartOptions = opts;
// ~ '{ logScale: boolean; }' 유형에
// 'LineChartOptions' 유형과 공통적인 속성이 없다.
```

약한 타입에 대해 TypeScript는 값 타입과 선언 타입에 공통된 속성이 있는지 확인하는 별도의 체크를 수행한다. 공통 속성 체크는 잉여 속성 체크와 마찬가지로 오타를 잡는 데 효과적이며 구조적으로 엄격하지 않는다. 그러나 잉여 속성 체크와 다르게, 약한 타입과 관련된 할당문마다 수행된다.

## 요약
- 객체 리터럴을 변수에 할당하거나 함수에 매개변수로 전달할 때 잉여 속성 체크가 수행된다.
- 잉여 속성 체크는 오류를 찾는 효과적인 방법이지만, TypeScript 타입 체커가 수행하는 일반적인 구조적 할당 가능성 체크와 역할이 다르다. 할당의 개념을 정확히 알아야 잉여 속성 체크와 일반적인 구조적 할당 가능성 체크를 구분할 수 있다.
- 잉여 속성 체크에는 한계가 있다. 임시 변수를 도입하면 잉여 속성 체크를 건너뛸 수 있다는 점을 기억해야 한다.