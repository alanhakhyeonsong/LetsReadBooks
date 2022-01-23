# 아이템 2. 생성자에 매개변수가 많다면 빌더를 고려하라

정적 팩터리와 생성자에는 똑같은 제약이 하나 있다. 선택적 매개변수가 많을 때 적절히 대응하기가 어렵다는 점이다. 클래스용 생성자 혹은 정적 팩터리에 대해 프로그래머들은 점층적 생성자 패턴(telescoping constructor pattern)을 즐겨 사용했다. 필수 매개변수만 받는 생성자, 필수 매개변수와 선택 매개변수 1개를 받는 생성자, 선택 매개변수를 2개까지 받는 생성자, ... 형태로 선택 매개변수를 전부 다 받는 생성자까지 늘려가는 방식이다.

## 1. 점층적 생성자 패턴(Telescoping Constructor Pattern)

```java
public class NutritionFacts {
    private final int servingSize; // (ml, 1회 제공량) 필수
    private final int servings; // (회, 총 n회 제공량) 필수
    private final int calories; // (1회 제공량당) 선택
    private final int fat; // (g/1회 제공량) 선택
    private final int sodium; // (mg/1회 제공량) 선택
    private final int carbohydrate; // (g/1회 제공량) 선택

    public NutritionFacts(int servingSize, int servings) {
        this(servingSize, servings, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories) {
        this(servingSize, servings, calories, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat) {
        this(servingSize, servings, calories, fat, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium) {
        this(servingSize, servings, calories, fat, sodium, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium, int carbohydrate) {
        this.servingSize = servingSize;
        this.servings = servings;
        this.calories = calories;
        this.fat = fat;
        this.sodium = sodium;
        this.carbohydrate = carbohydrate;
    }
}
```

이 클래스의 인스턴스를 만드려면 원하는 매개변수를 모두 포함한 생성자 중 가장 짧은 것을 골라 호출한다.
`NutritionFacts cocaCola = new NutritionFacts(240, 8, 100, 0, 35, 27);`

정확히는 **"필수 매개변수를 받는 생성자 1개, 그리고 선택 매개변수를 하나 씩 늘려가며 생성자를 만드는 패턴**이다.

다만, 사용자가 원치 않는 매개변수까지 포함하고 값을 지정해줘야 하는 문제가 있다. 또한 **매개변수가 개수가 많아지면 클라이언트 코드를 작성하거나 읽기가 어렵다.**

## 2. 자바빈즈 패턴(JavaBeans Pattern)

매개변수가 없는 생성자로 객체를 만든 후, 세터(setter) 메서드를 호출해 원하는 매개변수의 값을 설정하는 방식이다.

```java
// Lombok 라이브러리 적용
@Setter
@NoArgsConstructor
public class NutritionFacts {
    // 매개변수들은 (기본값이 있다면) 기본값으로 초기화 된다.
    private int servingSize = -1; // 필수. 기본값 없음
    private int servings = -1; // 필수. 기본값 없음
    private int calories = 0;
    private int fat = 0;
    private int sodium = 0;
    private int carbohydrate = 0;

}
```

Lombok에서도 볼 수 있는 `@Setter` 애노테이션. 코드는 길어지지만, 인스턴스를 만들기 쉽고 이전 점층적 생성자 패턴보다 읽기 쉬운 코드가 되었다.

**그러나, 객체 하나를 만드려면 메서드를 여러 개 호출해야 하고, 객체가 완전히 생성되기 전까지는 일관성(consistency)이 무너진 상태에 놓이게 된다.**

일관성이 무너지는 문제로 클래스를 불변으로 만들 수 없으며, 쓰레드 안전성을 얻으려면 프로그래머의 추가 작업이 필요하다.

// '프리징' 이라는 방법이 있긴 한데 다루기 어려워서 실전에서는 쓰지 않는다고 함.

## 3. 빌더 패턴(Builder Pattern)

클라이언트는 필요한 객체를 직접 만드는 대신, 필수 매개변수만으로 생성자(or 정적 팩터리)를 호출해 빌더 객체를 얻는다. 그 다음에 `setter`를 이용해 선택 매개변수들을 설정한다. 마지막으로 매개변수가 없는 `build` 메서드를 호출해 드디어 우리에게 필요한 불변 객체를 얻는다.

빌더는 생성할 클래스 안에 정적 멤버 클래스로 만들어두는게 정석적이다.

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

현재 `NutritionFacts` 클래스는 불변이며, 모든 매개변수의 기본값들을 한곳에 모아뒀다. `setter` 메서드들은 빌더 자신을 반환하기 때문에 연쇄적으로 호출할 수 있다. 이런 방식을 플루언트 API, 메서드 연쇄(Method Chaining)라 한다.

```java
NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
                            .calories(100)
                            .sodium(35)
                            .carbohydrate(27)
                            .build();
```

이 클라이언트 코드는 쓰기 쉽고, 무엇보다도 읽기가 쉽다.

**빌더 패턴은 (파이썬과 스칼라에 있는) 명명된 선택적 매개변수(named optional parameters)를 흉내 낸 것이다.**

잘못된 매개변수를 일찍 발견하려면, 빌더의 생성자와 메서드에서 입력 매개변수를 검사하고, build 메서드가 호출하는 생성자에서 여러 매개변수에 걸친 불변식을 검사하자.

### 빌더 패턴과 계층적 클래스

**빌더 패턴은 계층적으로 설계된 클래스와 함께 쓰기에 좋다.**

추상 클래스는 추상 빌더를, 구체 클래스는 구체 빌더를 갖게 한다.

Pizza.java

```java
public abstract class Pizza {
    public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE }
    final Set<Topping> toppings;

    // 재귀적인 타입 변수
    abstract static class Builder<T extends Builder<T>> {
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);

        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            return self();
        }

        abstract Pizza build();

        // 하위 클래스는 이 메서드를 재정의(overriding) 하여 "this"를 반환하도록 해야 한다.
        protected abstract T self();
    }

    Pizza(Builder<?> builder) {
        toppings = builder.toppings.clone(); // item 50 참조
    }
}
```

`Pizza.Builder` 클래스는 재귀적 타입 한정(item 30)을 이용하는 제네릭 타입이다. 여기에 추상 메서드인 self를 더해 하위 클래스에서는 형변환하지 않고도 메서드 연쇄가 가능하다.

self 타입이 없는 자바를 위한 이 우회방법을 시뮬레이트한 셀프 타입 관용구라 한다.

### Pizza, NyPizza, CalzonePizza

`Pizza`의 하위 클래스 2개가 있다. 뉴욕 피자는 크기(size) 매개변수를 필수로 받고, 칼초네 피자는 소스를 안에 넣을지 선택(sauceInside)하는 매개변수를 필수로 받는다.

NyPizza.java

```java
public class NyPizza extends Pizza {
    public enum Size { SMALL, MEDIUM, LARGE }
    private final Size size;

    public static class Builder extends Pizza.Builder<Builder> {
        private final Size size;

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        @Override
        public NyPizza build() {
            return new NyPizza(this);
        }

        @Override
        protected Builder self() { return this; }
    }

    private NyPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }
}
```

Calzone.java

```java
public class Calzone extends Pizza {
    private final boolean sauceInside;

    public static class Builder extends Pizza.Builder<Builder> {
        private boolean sauceInside = false; // 기본값

        public Builder sauceInside() {
            sauceInside = true;
            return this;
        }

        @Override
        public Calzone build() {
            return new Calzone(this);
        }

        @Override
        protected Builder self() { return this; }
    }

    private Calzone(Builder builder) {
        super(builder);
        sauceInside = builder.sauceInside;
    }
}
```

각 하위 클래스의 빌더가 정의한 build 메서드는 해당하는 구체 하위 메서드를 반환하도록 선언한다.

`NyPizza.Builder`는 NyPizza를 반환, `Calzone.Builder`는 Calzone를 반환한다는 뜻이다.

하위 클래스의 메서드가 상위 클래스의 메서드가 정의한 반환 타입이 아닌, 그 하위 타입을 반환하는 기능을 공변반환 타이핑(covariant return typing)이라 한다. 이 기능을 이용하면 클라이언트가 형변환에 신경 쓰지 않고도 빌더를 사용할 수 있다.

```java
NyPizza pizza = new NyPizza.Builder(SMALL)
        .addTopping(SAUSAGE)
        .addTopping(ONION)
        .build();
Calzone calzone = new Calzone.Builder()
        .addTopping(HAM)
        .sauceInside()
        .build();
```

생성자로는 누릴 수 없는 사소한 이점으로, 빌더를 이용하면 가변인수 매개변수 여러 개 사용할 수 있다. 각각을 적절한 메서드로 나눠 선언하면 된다. 메서드를 여러 번 호출하도록 하고 각 호출 때 넘겨진 매개변수들을 하나의 필드로 모을 수도 있다.

빌더 패턴은 상당히 유연하다. 빌더 하나로 여러 객체를 순회하면서 만들 수 있고, 빌더에 넘기는 매개변수에 따라 다른 객체를 만들 수도 있다. 객체마다 부여되는 일련번호와 같은 특정 필드는 빌더가 알아서 채우도록 할 수도 있다.

단점은, 객체를 만드려면 그에 앞서 빌더부터 만들어야 한다는 점이다. 점층적 생성자 패턴보다는 코드가 장황해서 매개변수가 4개 이상은 되어야 값어치를 한다.  
// API는 시간이 지날수록 매개변수가 많아지는 경향이 있긴 함.

## 핵심 정리

**생성자나 정적 팩터리가 처리해야 할 매개변수가 많다면 빌더 패턴을 선택하는 게 더 낫다.**

## 📌 참고

`<E extends Comparable<E>>` : 모든 타입 E는 자신과 비교할 수 있다.  
`Builder<T extends Builder<T>>` : 모든 타입 T는 자신과 빌더를 사용할 수 있다.

// Q&A는 다음 [링크](https://github.com/java-squid/effective-java/tree/master/chapter02/item02#qa)를 참고하자.
