# 아이템 2. 타입스크립트 설정 이해하기
```javascript
function add(a, b) {
  return a + b;
}
add(10, null);
```

위 코드가 오류 없이 타입 체커를 통과할 수 있을까?

TypeScript 컴파일러는 매우 많은 설정을 가지고 있다. 이 설정들은 커맨드 라인에서 사용할 수 있다.

```bash
$ tsc --noImplicitAny program.ts
```

`tsconfig.json` 설정 파일을 통해서도 가능하다.

```json
{
  "compilerOptions": {
    "noImplicitAny": true
  }
}
```

가급적 설정 파일을 사용하는 것이 좋다. 그래야만 TypeScript를 어떻게 사용할 계획인지 동료들이나 다른 도구들이 알 수 있다. 설정 파일은 `tsc --init`만 실행하면 간단히 생성된다.

- TypeScript의 설정들은 어디서 소스 파일을 찾을지, 어떤 종류의 출력을 생성할지 제어하는 내용이 대부분이다.
- 언어 자체의 핵심 요소를 제어하는 설정도 있다.

### noImplicitAny
**`noImplicitAny`는 변수들이 미리 정의된 타입을 가져야 하는지 여부를 제어한다.**

```typescript
/** noImplicitAny가 해제되어 있을 때는 유효 */
function add(a, b) {
  return a + b;
}

/** TypeScript가 추론한 위 함수의 타입은 아래와 같다. */
function add(a: any, b: any): any
```

`any` 타입을 매개변수에 사용하면 타입 체커는 속절없이 무력해진다. `any`는 유용하지만 매우 주의해서 사용해야 한다.

**TypeScript는 타입 정보를 가질 때 가장 효과적이기 때문에, 되도록이면 `noImplicitAny`를 설정해야 한다.**

- TypeScript가 문제를 발견하기 수월해진다.
- 코드의 가독성이 좋아진다.
- 개발자의 생산성이 향상된다.

`noImplicitAny` 설정 해제는, JavaScript로 되어 있는 기존 프로젝트를 TypeScript로 전환하는 상황에만 필요하다.

### strictNullChecks
**`strictNullChecks`는 `null`과 `undefined`가 모든 타입에서 허용되는지 확인하는 설정이다.**

```typescript
/** strictNullChecks가 해제되었을 때 유효 */
const x: number = null; // 정상, null은 유효한 값이다.

/** 설정 시 오류 */
const x: number = null;
// ~ 'null' 형식은 'number' 형식에 할당할 수 없습니다.
```

`null` 대신 `undefined`를 써도 같은 오류가 난다. 만약 `null`을 허용하려 한다면, 의도를 명시적으로 드러냄으로써 오류를 고칠 수 있다.

```typescript
const x: number | null = null;
```

만약 `null`을 허용하지 않으려면, 이 값이 어디서부터 왔는지 찾아야 하고, `null`을 체크하는 코드나 단언문을 추가해야 한다.

- `strictNullChecks`는 `null`과 `undefined` 관련된 오류를 잡아 내는 데 많은 도움이 되지만, 코드 작성을 어렵게 한다.
- `strictNullChecks`를 설정하려면 `noImplicitAny`를 먼저 설정해야 한다.
- `strictNullChecks`와 `noImplicitAny`만큼 중요한 설정이 없다. 프로젝트의 가능한 한 초반에 설정하는게 좋다.

## 요약
- TypeScript 컴파일러는 언어의 핵심 요소에 영향을 미치는 몇 가지 설정을 포함하고 있다.
- TypeScript 설정은 커맨드 라인을 이용하기 보단 `tsconfig.json`을 사용하는 것이 좋다.
- JavaScript 프로젝트를 TypeScript로 전환하는 게 아니라면 `noImplicitAny`를 설정하는 것이 좋다.
- '`undefined`는 객체가 아닙니다' 같은 런타임 오류를 방지하기 위해 `strictNullChecks`를 설정하는 것이 좋다.
- TypeScript에서 엄격한 체클르 하고 싶다면 `strict` 설정을 고려해야 한다.