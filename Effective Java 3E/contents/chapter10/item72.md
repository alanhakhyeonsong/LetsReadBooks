# 아이템 72. 표준 예외를 사용하라
표준 예외를 재사용하면 얻는 이점이 많다.
- 우리가 만든 API가 다른 사람이 익히고 사용하기 쉬워진다.
- 우리의 API를 사용한 프로그램도 낯선 예외를 사용하지 않게 되어 읽기 쉽게 된다.
- 예외 클래스 수가 적을수록 메모리 사용량도 줄고 클래스를 적재하는 시간도 적게 걸린다.

## IllegalArgumentException
호출자가 인수로 부적절한 값을 넘길 때 던지는 예외  
ex. 반복 횟수를 지정하는 매개변수에 음수를 건넬 때 쓸 수 있다.

## IllegalStateException
대상 객체의 상태가 호출된 메서드를 수행하기에 적합하지 않을 때 주로 던진다.  
ex. 초기화되지 않은 객체를 사용하려 할 때

메서드가 던지는 모든 예외를 잘못된 인수나 상태라고 뭉뚱그릴 수도 있겠지만, 그 중 특수한 일부는 따로 구분해 쓰는게 보통이다.

`null` 값을 허용하지 않는 메서드에 `null`을 건네면 관례상 `IllegalArgumentException`이 아닌 `NullPointerException`을 던진다. 어떤 시퀀스의 허용 범위를 넘는 값을 건넬 때도 `IllegalArgumentException`보단 `IndexOutOfBoundsException`을 던진다.

## ConcurrentModificationException
단일 스레드에서 사용하려고 설계한 객체를 여러 스래드가 동시에 수정하려 할 때 던진다. (외부 동기화 방식으로 사용하려고 설계한 객체도 마찬가지)  
이 예외는 문제가 생길 가능성을 알려주는 정도의 역할로 쓰인다.

## UnsupportedOperationException
클라이언트가 요청한 동작을 대상 객체가 지원하지 않을 때 던진다. 대부분 객체는 자신이 정의한 메서드를 모두 지원하니 흔히 쓰이는 예외는 아니다. 보통은 구현하려는 인터페이스의 메서드 일부를 구현할 수 없을 때 쓰는데, 예를 들어 원소를 넣을 수만 있는 `List` 구현체에 대고 누군가 `remove` 메서드를 호출하면 이 예외를 던질 것이다.

## 상위 클래스의 예외는 직접 재사용하지 말자
**`Exception`, `RuntimeException`, `Throwable`, `Error`는 직접 재사용하지 말자. 이 클래스들은 추상 클래스라고 생각하길 바란다.** 이 예외들은 다른 예외들의 상위 클래스이므로 안정적으로 테스트할 수 없다.

## 예외 재사용
상황에 부합한다면 항상 표준 예외를 재사용하자. 이때 API 문서를 참고해 그 예외가 어떤 상황에서 던져지는지 꼭 확인해야 한다.

예외의 이름뿐 아니라 예외가 던져지는 맥락도 부합할 때만 재사용한다. 더 많은 정보를 제공하길 원한다면 표준 예외를 확장해도 좋다.  
→ 예외는 직렬화할 수 있다는 사실을 기억하자!

## IllegalStateException vs. IllegalArgumentException
**인수 값이 무엇이었든 어차피 실패했을 경우 `IllegalStateException`을, 그렇지 않으면 `IllegalArgumentException` 을 사용하자.**

https://www.baeldung.com/java-common-exceptions