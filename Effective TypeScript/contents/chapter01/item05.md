# 아이템 5. any 타입 지양하기
TypeScript의 타입 시스템은 점진적이고 선택적이다. 코드에 타입을 조금씩 추가할 수 있기 때문에 점진적이며, 언제든지 타입 체커를 해제할 수 있기 때문에 선택적이다. 이 기능들의 핵심은 `any` 타입이다.

```typescript
let age: number;
age = '12';
// '"12"' 형식은 'number' 형식에 할당할 수 없습니다.
age = '12' as any; // Ok
```

타입 체커를 통해 앞의 코드에서 오류를 찾아냈다. 오류는 `as any`를 추가해 해결할 수 있다. 이처럼 타입 선언을 추가하는 데에 시간을 쏟고 싶지 않아 `any` 타입이나 타입 단언문(`as any`)을 사용하고 싶기도 할 것이다. **그러나 일부 특별한 경우를 제외하고는 `any`를 사용하면 TypeScript의 수많은 장점을 누릴 수 없게 된다.**

## any 타입에는 타입 안전성이 없다
앞선 예제에서 `age`는 `number` 타입으로 선언되었다. `as any`를 사용하면 `string` 타입을 할당할 수 있게 된다. 타입 체커는 선언에 따라 `number` 타입으로 판단할 것이고 혼돈은 걷잡을 수 없게 된다.

```typescript
age += 1; // 런타임에 정상, age는 '121'
```

## any는 함수 시그니처를 무시해 버린다
함수를 작성할 때는 시그니처를 명시해야 한다. 호출하는 쪽은 약속된 타입의 입력을 제공하고, 함수는 약속된 타입의 출력을 반환한다. 그러나 `any` 타입을 사용하면 이런 약속을 어길 수 있다.

```typescript
function calculateAge(birthDate: Date): number {
  // ...
}

let birthDate: any = '1996-01-19';
calculateAge(birthDate); // 정상
```

`birthDate` 매개변수는 `string`이 아닌 `Date` 타입이어야 한다. `any` 타입을 사용하면 시그니처를 무시하게 된다. JavaScript에선 종종 암시적으로 타입이 변환되기 때문에 이런 경우 특히 문제가 될 수 있다. `string` 타입은 `number` 타입이 필요한 곳에서 오류 없이 실행될 때가 있고, 그럴 경우 다른 곳에서 문제를 일으키게 될 것이다.

## any 타입에는 언어 서비스가 적용되지 않는다
어떤 심벌에 타입이 있다면 TypeScript 언어 서비스는 자동완성 기능과 적절한 도움말을 제공한다.

그러나 `any` 타입인 심벌을 사용하면 아무런 도움을 받지 못한다.

```typescript
let person: any = { first: 'George', last: 'Washington' };
person.
```

이름 변경 기능은 또 다른 언어 서비스다. 편집기에서 Rename 단축키를 사용하면 `any` 타입의 심벌은 바뀌지 않는다.

TypeScript의 모토는 확장 가능한 JavaScript다. '확장'의 중요한 부분은 바로 TypeScript 경험의 핵심 요소인 언어 서비스다.

## any 타입은 코드 리팩터링 때 버그를 감춘다
```typescript
interface ComponentProps {
  onSelectItem: (item: any) => void;
}

function renderSelector(props: ComponentProps) { /** ... */ }

let selectedId: number = 0;

function handleSelectItem(item: any) {
  selectedId = item.id;
}

renderSelector({onSelectItem: handleSelectItem});
```

`onSelectItem`에 아이템 객체를 필요한 부분만 전달하도록 컴포넌트를 개선하면 다음과 같다.

```typescript
interface ComponentProps {
  onselectItem: (id: number) => void;
}
```

컴포넌트를 수정하고, 타입 체크를 모두 통과했다.

타입 체크를 통과했다고 끝난 것은 아니다. `handleSelectItem`은 `any` 매개변수를 받는다. 따라서 `id`를 전달받아도 문제가 없다고 나온다. `id`를 전달 받으면, 타입 체커를 통과함에도 불구하고 런타임에는 오류가 발생할 것이다. `any`가 아니라 구체적인 타입을 사용했다면, 타입 체커가 오류를 발견했을 것이다.

## any는 타입 설계를 감춰버린다
애플리케이션 상태 같은 객체를 정의하려면 꽤 복잡하다. 상태 객체 안에 있는 수많은 속성의 타입을 일일이 작성해야 하는데, `any` 타입을 사용하면 간단히 끝내버릴 수 있다.

물론 이때도 `any`를 사용하면 안 된다. 상태 객체의 설계를 감춰버리기 때문이다. **설계가 명확히 보이도록 타입을 일일이 작성하는 것이 좋다.**

## any는 타입시스템의 신뢰도를 떨어뜨린다
보통은 타입 체커가 실수를 잡아주고 코드의 신뢰도가 높아진다. 그러나 런타임에 타입 오류를 발견하게 된다면 타입 체커를 신뢰할 수 없을 것이다. 대규모 팀에 TypeScript를 도입하려는 상황에서, 타입 체커를 신뢰할 수 없다면 큰 문제가 될 것이다. `any` 타입을 쓰지 않으면 런타임에 발견될 오류를 미리 잡을 수 있고 신뢰도를 높일 수 있다.

TypeScript는 개발자의 삶을 편하게 하는 데 목적이 있지만, 코드 내에 존재하는 수많은 `any` 타입으로 인해 JavaScript보다 일을 더 어렵게 만들기도 한다. 타입 오류를 고쳐야 하고 여전히 머릿속에 실제 타입을 기억해야 하기 때문이다. **타입이 실제 값과 일치한다면 타입 정보를 기억해 둘 필요가 없다.** TypeScript가 타입 정보를 기억해 주기 때문이다.

## 요약
- `any` 타입을 사용하면 타입 체커와 TypeScript 언어 서비스를 무력화시켜버린다. `any` 타입은 진짜 문제점을 감추며, 개발 경험을 나쁘게 하고, 타입 시스템의 신뢰도를 떨어뜨린다. 최대한 사용을 피하도록 하자.