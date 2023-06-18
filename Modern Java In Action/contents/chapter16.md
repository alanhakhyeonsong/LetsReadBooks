# Chapter 16 - CompletableFuture : 안정적 비동기 프로그래밍
## Future의 단순 활용
Java 5부터는 미래의 어느 시점에 결과를 얻는 모델에 활용할 수 있도록 `Future` 인터페이스를 제공하고 있다. 비동기 계산을 모델링하는 데 `Future`를 이용할 수 있으며, 계산이 끝났을 때 결과에 접근할 수 있는 참조를 제공한다. 다른 작업을 처리하다가 시간이 오래 걸리는 작업의 결과가 필요한 시점이 되었을 때 `Future`의 `get` 메서드로 결과를 가져올 수 있다. `get` 메서드를 호출했을 때 이미 계산이 완료되어 결과가 준비되었다면 즉시 결과를 반환하지만 결과가 준비되지 않았다면 작업이 완료될 때까지 우리 스레드를 블록시킨다.

`Future` 인터페이스로는 간결한 동시 실행 코드를 구현하기에 충분하지 않다. 또한 복잡한 의존을 갖는 동작을 구현하는 것은 쉽지 않다. 자바 8에서는 새로 제공하는 `CompletableFuture` 클래스를 통해 `Stream`과 비슷한 패턴, 즉 람다 표현식과 파이프라이닝을 활용하여 간편하게 비동기 동작을 구현할 수 있도록 한다. 따라서 `Future`와 `CompletableFuture`를 `Colletion`과 `Stream`의 관계에 비유할 수 있다.

## 비동기 API 구현
동기 메서드를 `CompletableFuture`를 통해 비동기 메서드로 변환할 수 있다. 비동기 계산과 완료 결과를 포함하는 `CompletableFuture` 인스턴스를 만들고 완료 결과를 `complete` 메서드로 전달하여 `CompletableFuture`를 종료할 수 있다.

```java
public Future<Integer> getPriceAsync(String product) {
	  CompletableFuture<Integer> futurePrice = new CompletableFuture<>();
	  new Thread(() -> {
			  int price = calculatePrice(product); // 다른 스레드에서 비동기적으로 계산 수행
		  	futurePrice.complete(price); // 오랜 시간이 걸리는 계산이 완료되면 Future에 값 설정
	  }).start();
	  return futurePrice; // 계산 결과가 완료되길 기다리지 않고 Future를 반환
} 
```

### 에러 처리 방법
비동기 작업을 하는 중 에러가 발생하면 해당 스레드에만 영향을 미친다. 에러가 발생해도 메인 스레드의 작업 흐름은 계속 진행되며 순서가 중요한 일이 있을 경우 일의 순서가 꼬인다.

클라이언트는 타임아웃 값을 받는 `get`메서드의 오버로드 버전을 만들어 이 문제를 해결할 수 있다. 이처럼 블록 문제가 발생할 수 있는 상황에서는 타임아웃을 활용하는 것이 좋다. 그래야 문제가 발생했을 때 클라이언트가 영원히 블록되지 않고 타임아웃 시간이 지나면 `TimeoutException`을 받을 수 있다.

```java
// Future.get시 반환할 value를 전달한다.
public boolean complete(T value);

// Future.get시 반환할 value와 Timeout 시간을 설정한다.
public T get(long timeout, TimeUnit unit);
```

`catch`한 `TimeoutException`에 대해 `catch` 블록 내 `completeExceptionally` 메서드를 이용해 `CompletableFuture` 내부에서 발생한 예외를 클라이언트로 전달할 수 있다.

```java
public boolean completeExceptionally(Throwable ex);
```

```java
public Future<Integer> getPriceAsync(String product) {
	  CompletableFuture<Integer> futurePrice = new CompletableFuture<>();
	  new Thread(() -> {
        try {
            int price = calculatePrice(product);
		  	    futurePrice.complete(price);
        } catch (Exception ex) {
            futurePrice.completeExceptionally(ex); // 도중에 문제가 발생하면 발생한 에러를 포함시켜 Future를 종료
        }
	  }).start();
	  return futurePrice;
} 
```

### 팩토리 메서드 supplyAsync로 CompletableFuture 만들기
```java
public Future<Double> getPriceAsync(String product) {
    return CompletableFuture.supplyAsync(() -> calculatePrice(product));
}
```

`supplyAsync` 메서드는 `Supplier`를 인수로 받아서 `CompletableFuture`를 반환한다. `CompletableFuture`는 `Supplier`를 실행해서 비동기적으로 결과를 생성한다. `ForkJoinPool`의 `Executor` 중 하나가 `Supplier`를 실행하며, 오버로드된 메서드를 이용하면 다른 `Executor`를 지정할 수 있다.

```java
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
    return asyncSupplyStage(asyncPool, supplier);
}

public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
    return asyncSupplyStage(screenExecutor(executor), supplier);
}
```

## 비블록 코드 만들기
`parallel` 메서드를 통한 병렬 스트림 API이나 `CompletableFuture`를 사용하면 비블록 코드를 만들 수 있다. 둘 다 내부적으로 `Runtime.getRuntime().availableProcess()` 가 반환하는 스레드 수를 사용하면 비슷한 성능을 낸다. 결과적으로는 비슷하지만 `CompletableFuture`는 병렬 스트림 버전에 비해 작업에 이용할 수 있는 다양한 `Executor`를 지정할 수 있다는 장점이 있다. 따라서 `Executor`로 스레드 풀의 크기를 조절하는 등 애플리케이션에 맞는 최적화된 설정을 만들 수 있다.

### 커스텀 Executor 사용하기
> 스레드 풀 크기 조절
> 
> 자바 병렬 프로그래밍(브라이언 게츠 공저)에서 스레드 풀의 최적값을 찾는 방법을 제안한다. 스레드 풀이 너무 크면 CPU와 메모리 자원을 서로 경쟁하느라 시간을 낭비할 수 있다. 반면 스레드 풀이 너무 작으면 CPU의 일부 코어는 활용되지 않을 수 있다.
> 
> ![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/ce8f6cfe-cc92-4484-8bd9-fe747fdb78bc)

애플리케이션의 특성에 맞는 `Executor`를 만들어 `CompletableFuture`를 활용하는 것이 바람직하다.

### 스트림 병렬화와 CompletableFuture 병렬화
- I/O가 포함되지 않은 계산 중심의 동작을 실행할 때는 스트림 인터페이스가 가장 구현하기 간단하며 효율적일 수 있다.
- 작업이 I/O를 기다리는 작업을 병렬로 실행할 때는 `CompletableFuture`가 더 많은 유연성을 제공하며 대기/계산(W/C)의 비율에 적합한 스레드 수를 설정할 수 있다.

## 비동기 작업 파이프라인 만들기
Stream API의 `map` 메서드와 `CompletableFuture`의 메서드들을 이용하여 비동기 작업 파이프라인을 만들 수 있다.

- `supplyAsync`: 전달받은 람다 표현식을 비동기적으로 수행한다.
- `thenApply`: `CompletableFuture`가 동작을 완전히 완료한 다음에 `thenApply`로 전달된 람다 표현식을 적용한다.
- `thenCompose`: 첫 번째 연산의 결과를 두 번째 연산으로 전달한다.
- `thenCombine`: 독립적으로 실행된 두 개의 `CompletableFuture` 결과를 이용하여 연산한다. 두 결과를 어떻게 합칠지 정의된 `BiFunction`을 두 번째 인수로 받는다.
- `thenCombineAsync`: 두 개의 `CompletableFuture` 결과를 반환하는 새로운 `Future`를 반환한다.

```java
Future<Double> futurePriceInUSD = 
    CompletableFuture.supplyAsync(() -> shop.getPrice(product))
                    .thenCombine(
                        CompletableFuture.supplyAsync(
                          () -> exchangeService.getRate(Money.EUR, Money.USD)),
                          (price, rate) -> price * rate
                    ));
```

Java 9에선 다음 메서드들이 추가되었다.

- `orTimeout`: 지정된 시간이 지난 후 `CompletableFuture`를 `TimeoutException`으로 완료하게 한다.
- `completeOnTimeout`: 지정된 시간이 지난 후 지정한 기본 값을 이용해 연산을 이어가게한다.

## CompletableFuture의 종료에 대응하는 방법
실제 원격 서비스들은 얼마나 지연될지 예측하기 어렵다.

- `thenAccept`: `CompletableFuture`가 생성한 결과를 어떻게 소비할 지 미리 지정한다.
- `allOf`: 전달받은 `CompletableFuture` 배열이 모두 완료될 때 `CompletableFuture`를 반환한다.
- `anyOf`: 전달받은 `CompletableFuture` 배열 중 하나라도 작업이 끝났을 때 완료한 `CompletableFuture`를 반환한다.

## 📌 정리
- 한 개 이상의 원격 외부 서비스를 사용하는 긴 동작을 실행할 때는 비동기 방식으로 애플리케이션의 성능과 반응성을 향상시킬 수 있다.
- 우리 고객에서 비동기 API를 제공하는 것을 고려해야 한다. `CompletableFuture`의 기능을 이용하면 쉽게 비동기 API를 구현할 수 있다.
- `CompletableFuture`를 이용할 때 비동기 태스크에서 발생한 에러를 관리하고 전달할 수 있다.
- 동기 API를 `CompletableFuture`로 감싸서 비동기적으로 소비할 수 있다.
- 서로 독립적인 비동기 동작이든 아니면 하나의 비동기 동작이 다른 비동기 동작의 결과에 의존하는 상황이든 여러 비동기 동작을 조립하고 조합할 수 있다.
- `CompletableFuture`에 콜백을 등록해서 `Future`가 동작을 끝내고 결과를 생산했을 때 어떤 코드를 실행하도록 지정할 수 있다.
- `CompletableFuture` 리스트의 모든 값이 완료될 때까지 기다릴지 아니면 첫 값만 완료되길 기다릴지 선택할 수 있다.
- Java 9에선 `orTimeout`, `completeOnTimeout` 메서드로 `CompletableFuture`에 비동기 타임아웃 기능을 추가했다.