# 아이템 51. 메서드 시그니처를 신중히 설계하라
이번 아이템에는 개별 아이템으로 두기 애매한 API 설계 요령들을 모아 놓았다. 이 요령들을 잘 활용하면 배우기 쉽고, 쓰기 쉬우며, 오류 가능성이 적은 API를 만들 수 있을 것이다.

## 메서드 이름을 신중히 짓자
항상 표준 명명 규칙을 따라야 한다.(item 68)  
- 이해할 수 있고, 같은 패키지에 속한 다른 이름들과 일관되게 짓는 게 최우선 목표다.
- 그 다음 목표는 개발자 커뮤니티에서 널리 받아들여지는 이름을 사용하는 것이다.
- 긴 이름은 피하자. 애매하면 자바 라이브러리의 API 가이드를 참조하라.

## 편의 메서드를 너무 많이 만들지 말자
모든 메서드는 각각 자신의 소임을 다해야 한다. 메서드가 너무 많은 클래스는 익히고, 사용하고, 문서화하고, 테스트하고, 유지보수하기 어렵다.  
인터페이스도 마찬가지다. 메서드가 너무 많으면 이를 구현하는 사람과 사용하는 사람 모두를 고통스럽게 한다. 클래스나 인터페이스는 자신의 각 기능을 완벽히 수행하는 메서드로 제공해야 한다. 아주 자주 쓰일 경우에만 별도의 약칭 메서드를 두기 바란다.

**확신이 서지 않으면 만들지 말자.**

## 매개변수 목록은 짧게 유지하자
4개 이하가 좋다. 이 이상은 전부 기억하기가 쉽지 않다.

**같은 타입의 매개변수 여러 개가 연달아 나오는 경우가 특히 해롭다.** 사용자가 매개변수 순서를 기억하기 어려울뿐더러, 실수로 순서를 바꿔 입력해도 그대로 컴파일되고 실행된다. 단지 의도와 다르게 동작할 뿐이다.

### 과하게 긴 매개변수 목록을 짧게 줄여주는 기술 3가지
1. 여러 매서드로 쪼갠다.  
**쪼개진 메서드 각각은 원래 매개변수 목록의 부분집합을 받는다.** 잘못하면 메서드가 너무 많아질 수 있지만, 직교성(orthogonality)을 높여 오히려 메서드 수를 줄여주는 효과도 있다.  
ex) `java.util.List` 인터페이스

2. 매개변수 여러 개를 묶어주는 도우미 클래스를 만든다.  
일반적으로 이런 도우미 클래스는 **정적 멤버 클래스(item 24)** 로 둔다. 특히 잇따른 매개변수 몇 개를 독립된 하나의 개념으로 볼 수 있을 때 추천하는 기법이다.  

3. 객체 생성에 사용한 빌더 패턴을 메서드 호출에 응용한다.  
이 기법은 매개변수가 많을 때, 특히 그중 일부는 생략해도 괜찮을 때 도움이 된다. 먼저, 모든 매개 변수를 하나로 추상화한 객체를 정의하고, 클라이언트에서 이 객체의 setter 메서드를 호출해 필요한 값을 설정하게 하는 것이다. 각 setter 메서드는 매개변수 하나 혹은 서로 연관된 몇 개만 설정하게 한다. 클라이언트는 필요한 매개변수를 다 설정한 다음, `execute` 메서드를 호출해 앞서 설정한 매개변수들의 유효성을 검사한다. 이후, 설정이 완료된 객체를 넘겨 원하는 계산을 수행한다.

```java
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private int carbohydrate;

    public static class Builder {
        // 필수 매개변수
        private final int servingSize;
        private final int servings;

        // 선택 매개변수 - 기본값으로 초기화한다.
        private int calories = 0;
        private int fat = 0;
        private int sodium = 0;
        private int carbohydrate = 0;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int val) {
            calories = val;
            return this;
        }

        public Builder fat(int val) {
            fat = val;
            return this;
        }

        public Builder sodium(int val) {
            sodium = val;
            return this;
        }

        public Builder carbohydrate(int val) {
            carbohydrate = val;
            return this;
        }
    }

    private NutritionFacts(Builder builder) {
        servingSize = builder.servingSize;
        servings = builder.servings;
        calories = builder.calories;
        fat = builder.fat;
        sodium = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }
}
```

## 매개 변수의 타입으로는 클래스보다는 인터페이스가 더 낫다
매개 변수로 적합한 인터페이스가 있다면 (이를 구현한 클래스가 아닌) 그 인터페이스를 직접 사용하자.

예를 들어, 메서드에 `HashMap`을 넘길 일은 전혀 없다. 대신 `Map`을 사용하자. 그 결과 `HashMap` 뿐만 아니라 `TreeMap`, `ConcurrentHashMap`, `TreeMap`의 부분맵 등 어떤 `Map` 구현체도 인수로 건넬 수 있다. 심지어 아직 존재하지 않는 `Map`도 가능하다.

이는 Spring에서 추구하는 다형성 + OCP, DIP를 가능하게 하는 DI와 같은 맥락이다.

## boolean보다는 원소 2개짜리 열거 타입이 낫다
열거 타입을 사용하면 코드를 읽고 쓰기가 더 쉬워진다. 게다가 나중에 선택지를 추가하기도 쉽다. 

```java
public enum TemperatureScale { FAHRENHEIT, CELSIUS }
```
화씨온도와 섭씨온도를 원소로 정의한 열거 타입의 예시이다. 나중에 캘빈온도도 지원해야 한다면, `Thermometor`에 또다른 정적 메서드를 추가할 필요 없이 위 열거타입에 캘빈온도(KELVIN)를 추가하면 된다.  
또한, 온도 단위에 대한 의존성을 개별 열거 타입 상수의 메서드 안으로 리팩터링해 넣을 수도 있다.
