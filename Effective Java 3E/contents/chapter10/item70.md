# 아이템 70. 복구할 수 있는 상황에는 검사 예외를, 프로그래밍 오류에는 런타임 예외를 사용하라
Java는 문제 상황을 알리는 타입(`throwable`)으로 **검사 예외, 런타임 예외, 에러** 이렇게 세 가지를 제공한다.

![](https://velog.velcdn.com/images/songs4805/post/18f1795d-5f4f-43fc-b29b-5ee492d1089b/image.png)

![](https://velog.velcdn.com/images/songs4805/post/2d7b40be-3991-4d6b-95cf-24f61868ae72/image.png)

## 검사 예외(Checked Exception)
**호출하는 쪽에서 복구하리라 여겨지는 상황이라면 검사 예외를 사용하라.**
- 호출하는 쪽에서 복구해야만 하는 (catch) Exception
- 호출하는 쪽이 catch 하거나, throws를 통해 더 바깥으로 전파하도록 강제

## 비검사 예외(Unchecked Exception, Error & Runtime Exception)
- 프로그램 오류. 복구가 불가능하거나 더 실행해봐야 득보단 실이 많다.
- 클라이언트가 API 명세를 제대로 지키지 않았을 경우 발생하는 예외
- Error 클래스를 상속하지 말고, **`RuntimeException`의 하위 클래스를 활용하라**
- 가능한 예외 상황을 벗어나는 데 필요한 정보를 알려주는 메서드를 함께 제공하라

**Exception, RuntimeException, Error 를 상속하지 않는 throwable 은 만들지 말라!**

## 핵심 정리
- 복구할 수 있는 상황이면 검사 예외를, 프로그래밍 오류라면 비검사 예외를 던지자.
- 확실하지 않다면 비검사 예외를 던지자.
- 검사 예외도 아니고 런타임 예외도 아닌 `throwable`은 정의하지도 말자.
- 검사 예외라면 복구에 필요한 정보를 알려주는 메서드도 제공하자.