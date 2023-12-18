# 48장. 모듈
## 모듈의 일반적 의미
- 모듈(module)이란 애플리케이션을 구성하는 개별적 요소로서 재사용 가능한 코드 조각을 말한다.
- 일반적으로 기능을 기준으로 파일 단위로 분리한다.
- 모듈이 성립하려면 모듈은 자신만의 **파일 스코프(모듈 스코프)** 를 가질 수 있어야 한다.

자신만의 파일 스코프를 갖는 모듈의 자산(모듈에 포함되어 있는 변수, 함수, 객체 등)은 기본적으로 비공개 상태다. 다시 말해, 자신만의 파일 스코프를 갖는 모듈의 모든 자산은 캡슐화되어 다른 모듈에서 접근할 수 없다. 즉, 모듈은 개별적 존재로서 애플리케이션과 분리되어 존재한다.

하지만 애플리케이션과 완전히 분리되어 개별적으로 존재하는 모듈은 재사용이 불가능하므로 존재의 의미가 없다. 모듈은 애플리케이션이나 다른 모듈에 의해 재사용되어야 의미가 있다. 따라서 **모듈은 공개가 필요한 자산에 한정하여 명시적으로 선택적 공개가 가능하다.** 이를 **`export`라 한다.**

공개된 모듈의 자산은 다른 모듈에서 재사용할 수 있다. 이때 공개된 모듈의 자산을 사용하는 모듈을 모듈 사용자라 한다.

**모듈 사용자는 모듈이 공개한 자산 중 일부 또는 전체를 선택해 자신의 스코프 내로 불러들여 재사용할 수 있다.** 이를 **`import`라 한다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/556ea47b-019d-4f4d-ba73-95ce5eaf6075)

이처럼 모듈은 애플리케이션과 분리되어 개별적으로 존재하다가 필요에 따라 다른 모듈에 의해 재사용된다. 모듈은 기능별로 분리되어 개별적인 파일로 작성된다. 따라서 코드의 단위를 명확히 분리하여 애플리케이션을 구성할 수 있고, 재사용성이 좋아서 개발 효율성과 유지보수성을 높일 수 있다.

## 자바스크립트와 모듈
JavaScript는 웹페이지의 단순한 보조 기능을 처리하기 위한 제한적인 용도를 목적으로 태어났다. 이러한 태생적 한계로 인해 다른 프로그래밍 언어와 비교할 때 부족한 부분이 있는 것이 사실이다. 대표적인 것이 모듈 시스템을 지원하지 않는다는 것이다. **다시 말해, JavaScript는 모듈이 성립하기 위해 필요한 파일 스코프와 `import`, `export`를 지원하지 않았다.**

클라이언트 사이드 JavaScript는 `script` 태그를 사용하여 외부의 JavaScript 파일을 로드할 순 있지만 파일마다 독립적인 파일 스코프를 갖지 않는다.

다시 말해, JavaScript 파일을 여러 개의 파일로 분리하여 `script` 태그로 로드해도 분리된 JavaScript 파일들은 결국 하나의 JavaScript 파일 내에 있는 것처럼 동작한다. 즉, 모든 JavaScript 파일은 하나의 전역을 공유한다.  
따라서 분리된 JavaScript 파일들의 전역 변수가 중복되는 등의 문제가 발생할 수 있다. 이것으로는 모듈을 구현할 수 없다.

JavaScript 런타임 환경인 Node.js는 모듈 시스템의 사실상 표준인 CommonJS를 채택했고 독자적인 진화를 거쳐, 현재는 CommonJS 사양과 100% 동일하진 않지만 기본적으로 이 사양을 따르고 있다. 즉, Node.js는 ECMAScript 표준 사양은 아니지만 모듈 시스템을 지원한다. 따라서 Node.js 환경에선 파일별로 독립적인 파일 스코프(모듈 스코프)를 갖는다.

## ES6 모듈(ESM)
ES6에서는 클라이언트 사이드 JavaScript에서도 동작하는 모듈 기능을 추가했다. IE를 제외한 대부분의 브라우저에서 ES6 모듈을 사용할 수 있다.

`script` 태그에 `type='module'` 어트리뷰트를 추가하면 로드된 JavaScript 파일은 모듈로서 동작한다. 일반적인 JavaScript 파일이 아닌 ESM임을 명확히 하기 위해 파일 확장자는 `mjs`를 사용할 것을 권장한다.

```html
<script type='module' src='app.mjs'></script>
```

ESM에는 클래스와 마찬가지로 기본적으로 strict mode가 적용된다.

### 모듈 스코프
ESM은 독자적인 모듈 스코프를 갖는다. ESM이 아닌 일반적인 JavaScript 파일은 `script` 태그로 분리해서 로드해도 독자적인 모듈 스코프를 갖지 않는다.

```javascript
// foo.js
// x 변수는 전역 변수
var x = 'foo';
console.log(window.x); // foo
```

```javascript
// bar.js
// x 변수는 전역 변수. foo.js에서 선언한 전역 변수 x와 중복된 선언임
var x = 'bar';

// foo.js에서 선언한 전역 변수 x의 값이 재할당됨.
console.log(window.x); // bar
```

```html
<!DOCTYPE html>
<html>
<body>
  <script src='foo.js'></script>
  <script src='bar.js'></script>
</body>
</html>
```

위 예제의 HTML에서 `script` 태그로 분리해서 로드된 2개의 JavaScript 파일은 하나의 JavaScript 파일 내에 있는 것처럼 동작한다. 즉, 하나의 전역을 공유한다.

ESM은 파일 자체의 독자적인 모듈 스코프를 제공한다. 따라서 모듈 내에서 `var` 키워드로 선언한 변수는 전역 변수가 아니며 `window` 객체의 프로퍼티도 아니다.

```javascript
// foo.mjs
var x = 'foo';
console.log(x); // foo
console.log(window.x); // undefined
```

```javascript
// bar.mjs
var x = 'bar';
console.log(x); // bar
console.log(window.x); // undefined
```

```html
<!DOCTYPE html>
<html>
<body>
  <script type='module' src='foo.js'></script>
  <script type='module' src='bar.js'></script>
</body>
</html>
```

모듈 내에서 선언한 식별자는 모듈 외부에서 참조할 수 없다. 모듈 스코프가 다르기 때문이다.

### export 키워드
모듈은 독자적인 모듈 스코프를 갖는다. 따라서 모듈 내부에서 선언한 모든 식별자는 기본적으로 해당 모듈 내부에서만 참조할 수 있다. 모듈 내부에서 선언한 식별자를 외부에 공개하여 다른 모듈들이 재사용할 수 있게 하려면 `export` 키워드를 사용한다.

`export` 키워드는 선언문 앞에 사용한다. 이로써 변수, 함수, 클래스 등 모든 식별자를 `export` 할 수 있다.

```javascript
// lib.mjs
// 변수의 공개
export const pi = Math.PI;

// 함수의 공개
export function square(x) {
  return x + x;
}

// 클래스의 공개
export class Person {
  constructor(name) {
    this.name = name;
  }
}
```

선언문 앞에 매번 `export` 키워드를 붙이는 것이 번거롭다면 `export`할 대상을 하나의 객체로 구성하여 한 번에 export 할 수도 있다.

```javascript
// lib.mjs
const pi = Math.PI;

 unction square(x) {
  return x + x;
}

class Person {
  constructor(name) {
    this.name = name;
  }
}

// 변수, 함수, 클래스를 하나의 객체로 구성하여 공개
export { pi, square, Person };
```

### import 키워드
다른 모듈에서 공개한 식별자를 자신의 모듈 스코프 내부로 로드하려면 `import` 키워드를 사용한다. 다른 모듈이 `export`한 식별자 이름으로 import 해야하며 ESM의 경우 파일 확장자를 생략할 수 있다.

```javascript
// app.mjs
// 같은 폴더 내의 lib.mjs 모듈이 export한 식별자 이름으로 import 한다.
// ESM의 경우 파일 확장자를 생략할 수 없다.
import { pi, square, Person } from './lib.mjs';

console.log(pi);
console.log(square(10));
console.log(new Person('Ramos'));
```

```html
<!DOCTYPE html>
<html>
<body>
  <script type='module' src='app.mjs'></script>
</body>
</html>
```

위 예제의 `app.mjs`는 애플리케이션의 진입점이므로 반드시 `script` 태그로 로드해야 한다. 하지만 `lib.mjs`는 `app.mjs`의 `import` 문에 의해 로드되는 의존성이다. 따라서 `lib.mjs`는 `script` 태그로 로드하지 않아도 된다.

모듈이 `export`한 식별자 이름을 일일이 지정하지 않고 하나의 이름에 한 번에 `import` 할 수도 있다. 이때 `import` 되는 식별자는 `as` 뒤에 지정한 이름의 객체에 프로퍼티로 할당된다.

```javascript
// app.mjs
// lib.mjs 모듈이 export한 모든 식별자를 lib 객체의 프로퍼티로 모아 import한다.
import * as lib from './lib.mjs';

console.log(lib.pi);
console.log(lib.square(10));
console.log(new lib.Person('Ramos'));
```

모듈이 `export`한 식별자 이름을 변경하여 `import`할 수도 있다.

```javascript
// app.mjs
// lib.mjs 모듈이 export한 식별자 이름을 변경하여 import한다.
import { pi as PI, square as sq, Person as P } from './lib.mjs';

console.log(PI);
console.log(sq(2));
console.log(new P('Ramos'));
```

모듈에서 하나의 값만 `export`한다면 `default` 키워드를 사용할 수 있다. 이 키워드를 사용하는 경우 기본적으로 이름 없이 하나의 값을 `export` 한다.

```javascript
// lib.mjs
export default x => x * x;
```

`default` 키워드를 사용하는 경우 `var`, `let`, `const` 키워드는 사용할 수 없다.

```javascript
// lib.mjs
export default const foo = () => {};
// SyntaxError: Unexpected token 'const'
// export default () => {};
```

`default` 키워드와 함께 `export`한 모듈은 `{}` 없이 임의의 이름으로 `import` 한다.

```javascript
// app.mjs
import square from './lib.mjs';

console.log(square(3));
```

## 실전 Tip
`index.ts`를 기준으로 필요한 util 기능을 사용한다. util 파일을 직접적으로 사용하지 않고 해당 모듈에 명시해둔 entrypoint인 `index.ts`를 거쳐 사용해야 한다.

```typescript
// index.ts
export * from './date';
export * from './eventEmitter';
export * from './reactQuery';
export * from './storage';
export * from './url';
```

```typescript
// reactQuery.ts
import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient();
```

```typescript
// date.ts
import 'dayjs/locale/ko';

import dayjs, { type Dayjs, locale } from 'dayjs';

import { YEAR_MONTH_DATE_FORMAT } from '../constants';

// 한국어로 설정 - `dayjs`가 제공하는 날짜와 시간 정보가 한국어로 표시됩니다.
locale('ko');

/**
 * 문자열을 입력받아 dayjs 객체를 반환합니다.
 *
 * @param inputData 변환 대상 입력 값
 * @see tokensList https://day.js.org/docs/en/parse/string-format#list-of-all-available-parsing-tokens
 * @returns dayjs 객체
 */
export const parseDayjs = (inputData: string | Date): Dayjs => dayjs(inputData);

/**
 * ISO 형식의 문자열 또는 dayjs을 입력 받아 지정된 포맷으로 날짜를 반환합니다.
 *
 * @function
 * @param inputData 변환 대상 입력 값
 * @param format 원하는 날짜 포맷
 * @returns 포맷팅된 날짜 문자열
 */
export const formatDate = (inputData: string | Dayjs | Date | undefined, format: string): string => {
  if (!inputData) {
    return '';
  }

  return dayjs(inputData).format(format);
};

/**
 * ISO 형식의 문자열 또는 dayjs을 입력 받아 년, 월, 일 기본 포멧으로 날짜를 반환합니다.
 * @param inputData 변환 대상 입력 값
 * @param format 원하는 날짜 포맷
 * @returns
 */
export const formatDateYMD = (inputData: Parameters<typeof formatDate>[0], format = YEAR_MONTH_DATE_FORMAT) => {
  return formatDate(inputData, format);
};
```