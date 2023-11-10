# Chapter 1. 자바스크립트에서 타입스크립트로
## 바닐라 자바스크립트의 함정
JavaScript는 **사실상 코드를 구성하는 방법에 제한이 없다.** 파일이 점점 늘어나면 그 자유가 얼마나 훼손될 수 있는지 명확해진다.

```javascript
function paintPainting(painter, painting) {
  return painter
      .prepare()
      .paint(painting, painter.ownMaterials)
      .finish();
}
```

위 코드를 어떠한 맥락도 없이 읽게 되면, `paintPainting` 함수를 호출하는 방법에 대해 막연한 생각만 들 것이다. `painter`가 어떤 `getPainter` 함수로 반환되어야 하는지 생각할 수도 있고, `painting`이 문자열이라고 운 좋게 추측할 수도 있다.

그러나 나중에 코드를 변경하면, 이 가정은 무효가 될 수 있다.
- 문자열이었던 `painting`이 다른 데이터 타입으로 변경되거나
- 하나 이상의 `painter` 메서드 이름이 변경되었을 수 있다.

다른 언어는 컴파일러가 충돌할 수 있다고 판단하면 코드 실행을 거부할 수 있다. 하지만 JavaScript처럼 충돌 가능성을 먼저 확인하지 않고 코드를 실행하는 dynamic type language는 그렇지 않다.

결국 코드의 자유는 JavaScript를 재미있게(?) 만들기도 하지만, 코드를 안전하게 실행하려고 할 때는 상당한 고통을 안겨준다.

### 부족한 문서
JavaScript 언어 사양에는 함수의 매개변수, 함수 반환, 변수 또는 다른 구성 요소의 의미를 설명하는 표준화된 내용이 없다. 따라서 많은 개발자가 블록 주석으로 함수와 변수를 설명하는 JSDoc 표준을 채택했다. 이는 표준으로 형식화된 함수와 변수 코드 바로 위에 문서 주석을 작성하는 방식이다.

```javascript
/**
 * Performs a painter painting a particular painting.
 *
 * @param {Painting} painter
 * @param {string} painting
 * @returns {boolean} Whether the painter painted the painting.
 */
function paintPainting(painter, painting) { /* ... */ }
```

JSDoc에는 다음과 같은 주요 문제로 인해 규모가 있는 코드베이스에서 사용하기 불편하다.
- JSDoc 설며잉 코드가 잘못되는 것을 막을 수 없다.
- JSDoc 설명이 이전에는 정확했더하더라도 코드 리팩터링 중에 생긴 변경 사항과 관련된 현재 유효하지 않은 JSDoc 주석을 모두 찾기란 어렵다.
- 복잡한 객체를 설명할 때는 다루기 어렵고 장황해서 타입과 그 관계를 정의하려면 다수의 독립형 주석이 필요하다.

### 부족한 개발자 도구
JavaScript는 타입을 식별하는 내장된 방법을 제공하지 않고, 코드가 JSDoc 주석에서 쉽게 분리되기 때문에 코드베이스에 대한 대규모 변경을 자동화하거나 통찰력을 얻기가 매우 어렵다. JavaScript 개발자는 C#이나 Java와 같은 타입이 지정된 언어에서 클래스 멤버 이름을 변경하거나 인수의 타입이 선언된 곳으로 바로 이동할 수 있는 기능을 보고 놀라곤 한다.

## 타입스크립트
- 프로그래밍 언어: JavaScript의 모든 구문과 타입을 정의하고 사용하기 위한 새로운 TypeScript 고유 구문이 포함된 언어
- 타입 검사기: JavaScript 및 TypeScript로 작성된 일련의 파일에서 생성된 모든 구성 요소(변수, 함수 등)을 이해하고, 잘못 구성된 부분을 알려주는 프로그램
- 컴파일러: 타입 검사기를 실행하고 문제를 보고한 후 이에 대응되는 JavaScript 코드를 생성하는 프로그램
- 언어 서비스: 타입 검사기를 사용해 VSCode와 같은 편집기에 개발자에게 유용한 유틸리티 제공법을 알려주는 프로그램

## 타입스크립트 실전
```typescript
const firstName = "Georgia";
const nameLength = firstName.length();
//
// Error: This expression is not callable.
```

일반 JavaScript 구문으로 작성된 코드다. 만일 이 코드에 TypeScript 타입 검사기를 실행하면, 문자열의 길이 속성이 함수가 아니라 숫자라는 지식을 활용해 주석으로 오류 사항을 알려준다.

코드를 입력할 때 편집기에서 간단한 오류를 미리 알려주면, 코드를 실행하고 오류가 발생할 때까지 기다리는 것보다 훨씬 유용하다. 만일 JavaScript에서 이 코드를 실행하면 오류가 발생한다.

## 제한을 통한 자유
TypeScript를 사용하면 매개변수와 변수에 제공되는 값의 타입을 지정할 수 있다. 일부 개발자는 처음엔 특정 영역이 제한적으로 작동하는 방법을 코드에 명시적으로 작성해야 한다고 생각한다. 코드를 지정한 방법으로만 사용하도록 제한한다면, TypeScript는 코드의 한 영역을 변경하더라도 이 코드를 사용하는 다른 코드 영역이 멈추지 않는다는 확신을 줄 수 있다.

함수의 매개변수 개수를 변경했을 경우, 변경된 함수를 호출하는 코드를 업데이트하지 않았다면 타입스크립트가 알려준다.

```typescript
function sayMyName(fullName) {
  console.log(`You acting kind of shady, ain\'t callin\' me ${fullName}`);
}

sayMyName("Ramos", "Gumo");
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/51a13d92-c789-474a-82e3-bfe2f87bc286)

이 코드는 JavaScript에서 오류 없이 실행되지만, 결과가 예상하는 것과 다르다. 두 번째 문자열로 전달한 "Gumo"는 제외된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/7f7d43fb-85c4-402c-8987-a40b59aac5ad)

잘못된 수의 인수를 사용해서 함수를 호출하는 것은 TypeScript가 제한하는 JavaScript가 가진 일종의 근시안적인 자유다.

## 정확한 문서화
앞서 다룬 `paintPainting` 함수의 TypeScript 버전을 살펴보자. 타입을 문서화하기 위한 TypeScript 구문을 살펴보진 않았지만, 다음 코드를 통해 문서화하는 TypeScript의 정밀함을 확인할 수 있다.

```typescript
interface Painter {
  finish(): boolean;
  ownMaterials: Meterial[];
  paint(painting: string, meterials: Meterial[]): boolean;
}

function paintPainting(painter: Painter, painting: string): boolean { /* ... */ }
```

이 코드를 처음 읽는 TypeScript 개발자라면 `Painter`에 적어도 세 가지 속성이 있고, 그중 두 가지는 메서드라는 것을 이해한다. TypeScript는 구문을 적용해 객체의 형태를 설명하고, 우수하고 강력한 시스템을 이용해 객체가 어떻게 보이는지 설명한다.

## 구문 컴파일하기
TypeScript 컴파일러에 TypeScript 구문을 입력하면 타입을 검사한 후 작성된 코드에 해당하는 JavaScript를 내보낸다. 편의상 컴파일러는 최신 JavaScript 구문이나 이전 ECMA Script에 상응하는 코드로 컴파일할 수도 있다.

## 로컬에서 시작하기
Node.js가 설치되어 있으면 TypeScript를 실행할 수 있다.

```bash
# 최신 버전을 전역으로 설치한다.
$ npm i -g typescript
```

```bash
# tsc(TypeScript 컴파일러) 명령어로 TypeScript를 실행할 수 있다.
# 올바르게 설정되었는지 확인하자.
$ tsc --version
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/47ba4ef7-a064-492b-9ff4-621449046933)

## 로컬에서 실행하기
TypeScript가 설치되었으므로 코드에서 TypeScript를 실행할 로컬 폴더를 설정한다. 아무 곳에나 폴더를 만들고 다음 명령어를 실행해 신규 tsconfig.json 구성 파일을 생성한다.

```bash
tsc --init
```

`tsconfig.json` 파일은 TypeScript가 코드를 분석할 때 사용하는 설정을 선언한다.

일단 가장 중요한 특징은 `tsc`를 실행해 폴더의 모든 파일을 컴파일하도록 지시할 수 있고, TypeScript가 모든 구성 옵션에 대해 `tsconfig.json`을 참조할 수 있다는 것이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/6c992606-3efb-4ff7-8979-a7e2156974ef)

`tsc` 명령에 `helloworld.ts` 파일명을 알려준다. 만약 아래와 같은 내용을 추가한다면, 오류가 표시될 것이다.

```typescript
console.blub("Nothing is worth more than laughter.");
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/aab681af-26bd-4159-80b2-b078f9602f87)

TypeScript를 정상적으로 작동시키기 위해 코드를 수정하기 전에, `tsc`가 `console.blub`을 포함해 `helloworld.js`를 생성했다는 점에 주목하자.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/0b8fa378-7722-490f-8c67-45c05513c707)

## 편집기 기능
`tsconfig.json` 파일을 생성할 때의 또 다른 이점은 편집기에서 특정 폴더를 열었을 때, 편지빅가 이제 해당 폴더를 TypeScript 프로젝트로 인식한다는 것이다. VS Code에서 폴더를 열면 TypeScript 코드를 분석하는 데 사용하는 설정은 해당 폴더의 `tsconfig.json`을 따르게 된다.

## 타입스크립트에 대한 오해
### 잘못된 코드 해결책
TypeScript는 JavaScript 코드를 구조화하는 데 도움이 되지만, 타입 안정성 강화를 제외하곤 해당 구조가 어떻게 보여야 하는지에 대해선 어떤 것도 강요하지 않는다.

TypeScript는 특정 대상만을 위한 독단적인 프레임워크가 아닌 모든 개발자가 사용할 수 있는 프로그래밍 언어다. JavaScript에서 사용했던 아키텍처 패턴 중 무엇이든 사용해서 코드를 작성할 수 있고, TypeScript가 이를 지원한다.

### 자바스크립트로의 확장
TypeScript의 설계 목표는 다음과 같이 명시되어 있다.

- 현재와 미래의 ECMA Script 제안에 맞춘다.
- 모든 JavaScript 코드의 런타임 동작을 유지한다.

TypeScript는 JavaScript의 작동 방식을 전혀 변경하지 않는다. TypeScript 개발자들은 JavaScript에 추가되거나 JavaScript와 충돌할 수 있는 새로운 코드 기능을 TypeScript에 추가하지 않기 위해 열심히 노력했다.

### 자바스크립트보다 느림
런타임에서 TypeScript는 JavaScript보다 느리다고 불평하는 블로그 들이 많다. 하지만 이 주장은 부정확하고 오해의 소지가 있다. TypeScript가 코드에 적용하는 유일한 변경 사항은 인터넷 익스플로러 11과 같이 오래된 런타임 환경을 지원하기 위해 이전 버전의 JavaScript로 코드를 컴파일하도록 요청하는 경우다.

운영 프레임워크 대다수는 TypeScript의 컴파일러를 전혀 사용하지 않는다. 대신 transpile을 위한 별도의 도구를 사용하고 TypeScript는 타입 검사용으로만 사용한다.

그러나 TypeScript는 코드를 빌드하는 데 시간이 조금 더 걸린다. TypeScript 코드는 브라우저나 Node.js와 같은 환경에서 실행되기 전에 JavaScript로 컴파일되어야 한다. 빌드 파이프라인은 대부분 성능 저하를 무시하도록 설정된다. 코드에서 발생할 수 있는 오류를 분석하는 느린 TypeScript 기능은 실행 가능한 애플리케이션 코드 파일을 생성하는 것과는 분리된 채로 수행된다.