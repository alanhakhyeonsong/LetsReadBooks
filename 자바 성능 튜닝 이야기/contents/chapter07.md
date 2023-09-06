# Story 7. 클래스 정보, 어떻게 알아낼 수 있나?
Java에는 클래스와 메서드의 정보를 확인할 수 있는 API가 있다. `Class` 클래스와 `Method` 클래스다. 이 두 클래스가 성능에 얼마나 영향을 주는지 확인해보자.

## reflection 관련 클래스들
Java API에는 `reflection`이라는 패키지가 있다. 이 패키지에 있는 클래스들을 사용하면 JVM에 로딩되어 있는 클래스와 메서드 정보를 읽어 올 수 있다.

### Class 클래스
`Class` 클래스는 클래스에 대한 정보를 얻을 때 사용하기 좋고, 생성자는 따로 없다. `ClassLoader` 클래스의 `defineClass()` 메서드를 이용해 클래스 객체를 만들 수도 있지만, 좋은 방법은 아니다. 차라리 `Object` 클래스에 있는 `getClass()` 메서드를 이용하는 것이 일반적이다.

- `String getName()`: 클래스의 이름을 리턴
- `Package getPackage()`: 클래스의 패키지 정보를 패키지 클래스 타입으로 리턴
- `Field[] getFields()`: `public`으로 선언된 변수 목록을 `Field` 클래스 배열 타입으로 리턴
- `Field getField(String name)`: `public`으로 선언된 변수를 `Field` 클래스 타입으로 리턴
- `Field[] getDeclaredFields()`: 해당 클래스에서 정의된 변수 목록을 리턴
- `Field getDeclaredField(String name)`: name과 동일한 이름으로 정의된 변수 리턴
- `Method[] getMethods()`: `public`으로 선언된 모든 메서드 목록을 `Method` 클래스 배열 타입으로 리턴. 해당 클래스에서 사용 가능한 상속받은 메서드도 포함.
- `Method getMethod(String name, Class... parameterTypes)`: 지정된 이름과 매개변수 타입을 갖는 메서드를 리턴
- `Method[] getDeclaredMethods()`: 해당 클래스에서 선언된 모든 메서드 정보를 리턴
- `Method getDeclaredMethod(String name, Class... parameterTypes)`: 지정된 이름과 매개변수 타입을 갖는 해당 클래스에서 선언된 메서드를 리턴
- `Constructor[] getConstructors()`: 해당 클래스에 선언된 모든 `public` 생성자의 정보를 리턴
- `Constructor[] getDeclaredConstructors()`: 해당 클래스에서 선언된 모든 생성자의 정보 리턴
- `int getModifiers()`: 해당 클래스의 접근자 정보를 `int` 타입으로 리턴
- `String toString()`: 해당 클래스 객체를 문자열로 리턴

`String currentClassName = this.getClass().getName()`을 사용하면 현재 클래스의 이름을 알 수 있다. `getName()`은 패키지 정보까지 리턴해준다. 클래스 이름만 필요할 경우엔 `getSimpleName()`을 사용해도 된다.

### Method 클래스
`Method` 클래스를 이용하여 메서드에 대한 정보를 얻을 수 있다. 하지만, `Method` 클래스엔 생성자가 없으므로 `Method` 클래스의 정보를 얻기 위해선 `Class` 클래스의 `getMethods()`를 사용하거나 `getDeclaredMethod()`를 사용해야 한다.

- `Class<?> getDeclaringClass()`: 해당 메서드가 선언된 클래스 정보를 리턴
- `Class<?> getReturnType()`: 해당 메서드의 리턴 타입을 리턴
- `Class<?>[] getParameterTypes()`: 해당 메서드를 사용하기 위한 매개변수의 타입들을 리턴
- `String getName()`: 해당 메서드의 이름을 리턴
- `int getModifiers()`: 해당 메서드의 접근자 정보를 리턴
- `Class<?>[] getExceptionTypes()`: 해당 메서드에 정의되어 있는 예외 타입들을 리턴
- `Object invoke(Object obj, Object... args)`: 해당 메서드를 수행한다.
- `String toGenericString()`: 타입 매개변수를 포함한 해당 메서드의 정보를 리턴
- `String toString()`: 해당 메서드의 정보를 리턴

### Field 클래스
`Field` 클래스는 클래스에 있는 변수들의 정보를 제공하기 위해 사용한다. 생성자가 존재하지 않으므로 `Class` 클래스의 `getField()` 메서드나 `getDeclaredFields()` 메서드를 써야 한다.

- `int getModifiers()`: 해당 변수의 접근자 정보를 리턴
- `String getName()`: 해당 변수의 이름을 리턴
- `String toString()`: 해당 변수의 정보를 리턴

## reflection 클래스를 잘못 사용한 사례
일반적으로 로그를 프린트할 때 클래스 이름을 알아내기 위해서는 `Class` 클래스를 많이 사용한다.

`this.getClass().getName()`

이 방법을 사용한다 해서 성능에 많은 영향을 미치지는 않는다. 다만 `getClass()` 메서드를 호출할 때 `Class` 객체를 만들고, 그 객체의 이름을 가져오는 메서드를 수행하는 시간과 메모리를 사용할 뿐이다.

```java
public String checkClass(Object src) {
    if (src.getClass().getName().equals("java.math.BigDecimal")) {
        // ...
    }
    // ...
}
```

해당 클래스 이름을 알아내기 위해 `getClass().getName()`을 호출해서 사용했는데, 약간 비효율적이다.

```java
public String checkClass(Object src) {
    if (src instanceof java.math.BigDecimal) {
        //...
    }
    // ...
}
```

`instanceof`를 사용하면 훨씬 간단해졌다.

책의 예제를 통해 JMH로 성능 비교를 해보면 다음과 같다.

|대상|응답 시간(ms)|
|--|--|
|`instanceof` 사용|0.167|
|`Reflection` 사용|1.022|

가끔은 복잡한 것보다 기본으로 돌아가자.

## 정리하며
`reflection` 관련 클래스를 사용하면 클래스의 정보 및 여러 가지 세부 정보를 알 수 있어 매우 편리하다. 클래스의 메타 데이터 정보는 JVM의 Perm 영역에 저장된다는 사실을 기억하자. 만약 `Class` 클래스를 사용하여 엄청나게 많은 클래스를 동적으로 생성하는 일이 벌어지면 Perm 영역이 더 이상 사용할 수 없게 되어 `OutOfMemoryError`가 발생할 수도 있으니, 조심해서 사용하자.