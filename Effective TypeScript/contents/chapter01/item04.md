# 아이템 4. 구조적 타이핑에 익숙해지기
JavaScript는 본질적으로 duck typing 기반이다. 만약 어떤 함수의 매개변수 값이 모두 제대로 주어진다면, 그 값이 어떻게 만들어졌는지 신경 쓰지 않고 사용한다. TypeScript는 이런 동작, 즉 매개변수 값이 요구사항을 만족한다면 타입이 무엇인지 신경 쓰지 않는 동작을 그대로 모델링 한다.

```typescript
interface Vector2D {
  x: number;
  y: number;
}

function calculateLength(v: Vector2D) {
  return Math.sqrt(v.x * v.x + v.y * v.y);
}

interface NamedVector {
  name: string;
  x: number;
  y: number;
}

const v: NamedVector = { x: 3, y: 4, name: 'Zee' };
calculateLength(v); // 정상. 결과는 5
```
두 인터페이스의 관계를 전혀 선언하지 않았음에도 TypeScript는 위 코드를 이해할 수 있다. TypeScript 타입 시스템은 JavaScript의 런타임 동작을 모델링한다. `NamedVector`의 구조가 `Vector2D`와 호환되기 때문에 `calculateLength` 호출이 가능하다. 여기서 구조적 타이핑이라는 용어가 사용된다.

구조적 타이핑 때문에 문제가 발생하기도 한다.

```typescript
interface Vector3D {
  x: number;
  y: number;
  z: number;
}

function normalize(v: Vector3D) {
  const length = calculateLength(v);

  return {
    x: v.x / length,
    y: v.y / length,
    z: v.z / length,
  };
}
```

하지만 위 함수는 1보다 조금 더 긴 길이를 가진 결과를 출력한다. `calculateLength`는 2D 벡터를 기반으로 연산하는데, 버그로 인해 `normalize`가 3D 벡터로 연산되었다. `z`가 정규화 과정에서 무시된 것이다.

타입 체커는 이 문제를 잡아내지 못했다. 그 이유가 바로 구조적 타이핑이다. `Vector3D`와 호환되는 `{x, y, z}` 객체로 `calculateLength`를 호출하면, `x`, `y`가 있어서 `Vector2D`와 호횐된다.

함수를 작성할 때, 호출에 사용되는 매개변수의 속성들이 매개변수의 타입에 선언된 속성만을 가질 거라 생각하기 쉽다. 이러한 타입은 sealed 타입 또는 precise 타입이라 불리며, TypeScript 타입 시스템에선 표현할 수 없다. 좋든 싫든 타입은 열려있다.

```typescript
function calculateLengthL1(v: Vector3D) {
  let length = 0;
  for (const axis of Object.keys(v)) {
    const coord = v[axis];
    // 'string'은 'Vector3D'의 인덱스로 사용할 수 없기에 엘리먼트는 암시적으로 'any' 타입입니다.
    length += Math.abs(coord);
  }
  return length;
}
```

TypeScript가 오류를 정화깋 찾아낸 것이 맞다. 다음 코드처럼 작성할 수도 있다.

```typescript
const vec3D = {x: 3, y: 4, z: 1, address: '123 Broadway'};
calculateLengthL1(vec3D); // 정상, NaN을 반환한다.
```

`v`는 어떤 속성이든 가질 수 있기 때문에, `axis`의 타입은 `string`이 될 수도 있다. 따라서 `number` 타입이라 확정할 수 없다. 정확한 타입으로 객체를 순회하는 것은 까다로운 문제다.

```typescript
// 루프보단 모든 속성을 각각 더하는 구현이 더 낫다.
function calculateLengthL1(v: Vector3D) {
  return Math.abs(v.x) + Math.abs(v.y) + Math.abs(v.z);
}
```

**구조적 타이핑은 클래스와 관련된 할당문에서도 당황스러운 결과를 보여준다.**

```typescript
class C {
  foo: string;
  constructor(foo: string) {
    this.foo = foo;
  }
}

const c = new C('instance of C');
const d: C = { foo: 'object literal' }; // 정상
```

`d`가 `C` 타입에 할당되는 이유는 다음과 같다.

- `d`는 `string` 타입의 `foo` 속성을 가진다.
- 하나의 매개변수로 호출이 되는 생성자를 가진다.
- 구조적으로는 필요한 속성과 생성자가 존재하므로 문제가 없다.
- 만약 `C`의 생성자에 단순 할당이 아닌 연산 로직이 존재한다면, `d`의 경우는 생성자를 실행하지 않으므로 문제가 발생하게 된다.

**테스트를 작성할 때는 구조적 타이핑이 유리하다.**

```typescript
interface Author {
  first: string;
  last: string;
}
function getAuthors(database: PostgresDB): Author[] {
  const authorRows = database.runQuery(`SELECT FIRST, LAST FROM AUTHORS`);
  return authorRows.map(row => ({first: row[0], last: row[1]}));
}
```

`getAuthors` 함수를 테스트하기 위해선 모킹한 `PostgresDB`를 생성해야 한다. 그러나 구조적 타이핑을 활용하여 더 구체적인 인터페이스를 정의하는 것이 더 나은 방법이다.

```typescript
interface DB {
  runQuery: (sql: string) => any[];
}
function getAuthors(database: PostgresDB): Author[] {
  const authorRows = database.runQuery(`SELECT FIRST, LAST FROM AUTHORS`);
  return authorRows.map(row => ({first: row[0], last: row[1]}));
}
```

`runQuery` 메서드가 있기 때문에 실제 환경에서도 `getAuthors`에 `PostgresDB`를 사용할 수 있다. 구조적 타이핑 덕분에, `PostgresDB`가 `DB` 인터페이스를 구현하는지 명확히 선언할 필요가 없다. TypeScript는 그렇게 동작할 것이라는 것을 알아챈다.

```typescript
test('getAuthors', () => {
  const authors = getAuthors({
    runQuery(sql: string) {
      return [['Toni', 'Morrison'], ['Maya', 'Angelou']];
    }
  });
  expect(authors).toEqual([
    {first: 'Toni', last: 'Morrison'},
    {first: 'Maya', last: 'Angelou'}
  ]);
});
```

TypeScript는 테스트 DB가 해당 인터페이스를 충족하는지 확인한다. 그리고 테스트 코드에는 실제 환경의 데이터베이스에 대한 정보가 불필요하다. 심지어 모킹 라이브러리도 필요 없다. 추상화를 함으로써, 로직과 테스트를 특정한 구현으로부터 분리한 것이다.

## 정리
- JavaScript가 duck typing 기반이고 TypeScript가 이를 모델링하기 위해 구조적 타이핑을 사용함을 이해해야 한다. 어떤 인터페이스에 할당 가능한 값이라면 타입 선언에 명시적으로 나열된 속성들을 가지고 있을 것이다. 타입은 '봉인'되어 있지 않다.
- 클래스 역시 구조적 타이핑 규칙을 따른다는 것을 명심해야 한다. 클래스와 인스턴스가 예상과 다를 수 있다.
- 구조적 타이핑을 사용하면 유닛 테스팅을 손쉽게 할 수 있다.