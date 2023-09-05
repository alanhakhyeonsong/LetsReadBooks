# Story 5. 지금까지 사용하던 for 루프를 더 빠르게 할 수 있다고?
어떤 프로그래밍을 하든 반복 구문 사용은 기본 주으이 기본이다. 이 반복 구문도 잘만 사용하면 성능 향상을 가져올 수 있다.

## 조건문에서의 속도는?
조건문은 성능에 얼마나 많이 영향을 줄까?

`if` 문 안에는 `boolean` 형태의 결과 값만 사용할 수 있다. `switch` 문은 JDK 6까지는 `byte`, `short`, `char`, `int` 네 가지 타입을 사용한 조건 분기만 가능했지만, JDK 7 부터는 `String`도 사용 가능하다. `if`문 조건 안에 들어가는 비교 구문에서 속도를 잡아먹지 않는 한, `if` 문장 자체에선 그리 많은 시간이 소요되지 않는다.

```java
package org.example;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
@BenchmarkMode({ Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ConditionIf {
    private static final int LOOP_COUNT = 1000;

    @Benchmark
    public void randomOnly() {
        Random random = new Random();
        int data = 1000 + random.nextInt();
        for (int i = 0; i < LOOP_COUNT; i++) {
            resultProcess("dummy");
        }
    }

    @Benchmark
    public void if10() {
        Random random = new Random();
        String result = null;
        int data = 1000 + random.nextInt();
        for (int i = 0; i < LOOP_COUNT; i++) {
            if (data < 50) {
                result = "50";
            } else if (data < 150) {
                result = "150";
            } else if (data < 250) {
                result = "250";
            } else if (data < 350) {
                result = "350";
            } else if (data < 450) {
                result = "450";
            } else if (data < 550) {
                result = "550";
            } else if (data < 650) {
                result = "650";
            } else if (data < 750) {
                result = "750";
            } else if (data < 850) {
                result = "850";
            } else if (data < 950) {
                result = "950";
            } else {
                result = "over";
            }
            resultProcess(result);
        }
    }

    @Benchmark
    public void if100() {
        Random random = new Random();
        String result = null;
        int data = 10000 + random.nextInt();
        for (int i = 0; i < LOOP_COUNT; i++) {
            if (data < 50) {
                result = "50";
            } else if (data < 150) {
                result = "150";
            } else if (data < 250) {
                result = "250";
            } else if (data < 350) {
                result = "350";
            } else if (data < 450) {
                result = "450";
            } else if (data < 550) {
                result = "550";
            } else if (data < 650) {
                result = "650";
            } else if (data < 750) {
                result = "750";
            } else if (data < 850) {
                result = "850";
            } else if (data < 950) {
                result = "950";
            } else if (data < 1050) {
                result = "1050";
            } else if (data < 1150) {
                result = "1150";
            } else if (data < 1250) {
                result = "1250";
            } else if (data < 1350) {
                result = "1350";
            } else if (data < 1450) {
                result = "1450";
            } // 중략
            
            else if (data < 9850) {
                result = "9850";
            } else if (data < 9950) {
                result = "9950";
            } else {
                result = "over";
            }
            resultProcess(result);
        }
    }

    String current;

    public void resultProcess(String result) {
        current = result;
    }
}
```

위 예제를 JMH로 측정한 결과는 다음과 같다.

<img width="1136" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9ce668f0-eb6b-4873-b121-b6e9092d8eeb">

<img width="724" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fa98cb06-9150-4ac5-9327-24603435b27d">

유의해야 할 점은 `if`문이 10개라 할지라도 `LOOP_COUNT` 때문에 총 10,000번의 `if`문을 거친 결과가 `if10()`의 값이다. 다만 아주 큰 성능 저하가 발생한다고 보긴 어렵다.

또한, JDK 7에는 `String` 문자열을 `switch` 문에 사용할 수 있다.

```java
public int getMonthNumber(String str) {
    int month = -1;
    switch (str) {
        case "January": month = 1;
          break;
        case "February": month = 2;
          break;
        case "March": month = 3;
          break;
        case "April": month = 4;
          break;
        case "May": month = 5;
          break;
        case "June": month = 6;
          break;
        case "July": month = 7;
          break;
        case "August": month = 8;
          break;
        case "September": month = 9;
          break;
        case "October": month = 10;
          break;
        case "November": month = 11;
          break;
        case "December": month = 12;
          break;
    }
    return month;
}
```

JDK 6까진 `switch-case` 문에선 주로 정수와 `enum`을 처리할 수 있었는데, 어떻게 JDK 7에선 `String`을 비교할까? 이는 `int` 정수를 리턴하는 `Object` 클래스에 선언되어 있는 `hashCode()` 메서드에 있다.

위 코드를 실제 컴파일을 해보면, `hashCode()` 메서드로 변환하여 숫자가 나오게 되고, `case` 문에 있는 각 값들을 작은 것부터 정렬한 다음 `String`의 `equals()` 메서드를 사용하여 실제 값과 동일한지 비교한다.

한가지 꼭 기억해 두어야 하는 점은 숫자들이 정렬되어 있다는 점이다. `switch-case` 문은 작은 숫자부터 큰 숫자를 비교하는 게 가장 빠르다. 대상이 되는 `case`의 수가 적으면 상관 없지만, 많으면 많을수록 `switch-case`에서 소요되는 시간이 오래 걸린다. 따라서, 간단한 `switch-case`라도 성능을 고려하면서 사용해야 한다.

## 반복 구문에서의 속도는?
일반적으로 `for`문을 많이 사용한다. 가끔 `while`문도 사용하는데, `while`문은 잘못하면 무한 루프에 빠질 수 있으므로 되도록이면 `for`문을 사용하자.

JDK 5.0 이전에는 `for` 구문을 다음과 같이 사용하였다.

```java
for (int i = 0; i < list.size(); i++)
```

매번 반복하면서 `list.size()`를 호출하기 때문에 이는 좋지 않다. 다음과 같이 수정하자.

```java
int size = list.size();
for (int i = 0; i < size; i++)
```

불필요한 `size()` 메서드 반복 호출이 없어지므로 더 빠르게 처리된다.

JDK 5.0 부터는 다음과 같이 for-each라 불리는 루프를 사용할 수 있다.

```java
ArrayList<String> list = new ArrayList<>();
// ...
for (String str : list)
```

for-each를 사용하면 별도로 형변환하거나 `get()` 메서드 또는 `elementAt()` 메서드를 호출할 필요 없이 순서에 따라 `String` 객체를 `for` 문장 안에서 사용할 수 있으므로 매우 편리하다. 단, 이 방식은 데이터의 첫 번째 값부터 마지막까지 처리해야 할 경우에만 유용하다. 만약 순서를 거꾸로 돌리거나 특정 값부터 데이터를 탐색하는 경우에는 적절하지 않다.

```java
package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
@BenchmarkMode({ Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ForLoop {
    private static final int LOOP_COUNT = 1000;
    List<Integer> list;

    @Setup
    public void setUp() {
        list = new ArrayList<>(LOOP_COUNT);
        for (int i = 0; i < LOOP_COUNT; i++) {
            list.add(i);
        }
    }

    @Benchmark
    public void traditionalForLoop() {
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            resultProcess(list.get(i));
        }
    }

    @Benchmark
    public void traditionalSizeForLoop() {
        for (int i = 0; i < list.size(); i++) {
            resultProcess(list.get(i));
        }
    }

    @Benchmark
    public void timeForEachLoop() {
        for (Integer i : list) {
            resultProcess(i);
        }
    }

    int current;

    public void resultProcess(int result) {
        current = result;
    }
}
```

위 코드의 JMH 결과를 비교해보자.

이론적으로는 반복할 때마다 `list.size()`를 호출하는 부분이 가장 느려야 하고 for-each는 속도가 빨라야 한다.

<img width="642" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e5de345e-fa55-4bee-94e6-0e98bf296f1d">

## 반복 구문에서의 필요 없는 반복
가장 많은 실수 중 하나는 반복 구문에서 계속 필요 없는 메서드 호출을 하는 것이다.

```java
public void sample(DataVO data, String key) {
    TreeSet treeSet2 = null;
    treeSet2 = (TreeSet) data.get(key);
    if (treeSet2 != null) {
        for (int i = 0; i < treeSet2.size(); i++) {
            DataVO2 data2 = (DataVO2) treeSet2.toArray()[i];
            // ...
        }
    }
}
```

위 소스의 문제는 `toArray()`를 반복해서 수행한다는 것이다.

`sample()`은 애플리케이션이 한 번 호출되면 40번씩 수행된다고 가정한다. 또한 `treeSet2` 객체에 256개의 데이터들이 들어가 있다 가정하면, 결과적으로 `toArray()`는 한 번 호출될 때마다 10,600번 씩 반복 호출된다.

다음과 같이 수정하도록 하자.

```java
public void sample(DataVO data, String key) {
    TreeSet treeSet2 = null;
    treeSet2 = (TreeSet) data.get(key);
    if (treeSet2 != null) {
      DataVO2[] dataVO2 = (DataVO2) treeSet2.toArray();
      int treeSet2Size = treeSet2.size();
        for (int i = 0; i < treeSet2Size; i++) {
            DataVO2 data2 = dataVO2[i];
            // ...
        }
    }
}
```

정리하자면, 반복 구문은 조금이라도 잘못 생각하면 무한 루프를 수행하여 애플리케이션을 재시작하거나 스레드를 찾아 중단시켜야 하는 경우가 발생하므로 성능상 문제가 되기도 한다. 하지만 반대로 생각하면, 반복 구문의 문제점을 찾으면 성능상 문제가 되는 부분을 더 쉽게 해결할 수 있다는 말이 된다.