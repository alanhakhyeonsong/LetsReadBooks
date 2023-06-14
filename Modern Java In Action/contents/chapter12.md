# Chapter 12 - 새로운 날짜와 시간 API
Java 8 이전의 날짜와 시간 API의 문제들은 다음과 같다.

- `Date` 클래스는 특정 시점을 날짜가 아닌 밀리초 단위로 표현하기에 직관적이지 못하며 그렇다고 자체적으로 시간대 정보를 알고 있지 않다.
- `Date`를 deprecated 시키고 Java 1.1에서 등장한 `Calendar` 클래스 또한 쉽게 에러를 일으키는 설계 문제를 갖고 있다.
  - 1900년도에서 시작하는 오프셋은 없앴지만 여전히 달의 인덱스는 0부터 시작
- `Date`와 `Calendar` 두 가지 클래스가 등장하며 개발자들에게 혼란만 가중되었다.
- 날짜와 시간을 파싱하는데 등장한 `DateFormat`은 `Date`에만 작동하며, 스레드에 안전하지 않다.
- `Date`, `Calendar` 모두 가변 클래스이므로 유지보수가 아주 어렵다.

## LocalDate, LocalTime, Instant, Duration, Period 클래스
`java.time` 패키지는 `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `Duration`, `Period` 등 새로운 클래스를 제공한다.

### LocalDate와 LocalTime 사용
`LocalDate` 인스턴스는 시간을 제외한 날짜를 표현하는 불변 객체다. 특히 `LocalDate` 객체는 어떤 시간대 정보도 포함하지 않는다. 정적 팩토리 메서드 `of`로 `LocalDate` 인스턴스를 만들 수 있다.

```java
LocalDate date = LocalDate.of(2017, 9, 21);
int year = date.getYear();
Month month = date.getMonth(); // SEPTEMBER
int day = date.getDayOfMonth();
DayOfWeek dow = date.getDayOfWeek();
int len = date.lengthOfMonth(); // 31
boolean leap = date.isLeapYear(); // false (윤년이 아님)

LocalDate today = LocalDate.now();
```

`LocalDate`가 제공하는 `get` 메서드에 `TemporalField`를 전달해서 정보를 얻는 방법도 있다. `TemporalField`는 시간 관련 객체에서 어떤필드의 값에 접근할지 정의하는 인터페이스다. `ChronoField`는 이 인터페이스의 구현체이며 `ChronoField`의 열거자 요소를 이용해서 원하는 정보를 쉽게 얻을 수 있다.

```java
// public int get(TemporalField field)
int year = date.get(ChronoField.YEAR);
int month = date.get(ChronoField.MONTH_OF_YEAR);
int day = date.get(ChronoField.DAY_OF_MONTH);
```

시간에 대한 정보는 `LocalTime` 클래스로 표현할 수 있다. `LocalTime` 역시 정적 메서드 `of`로 인스턴스를 만들 수 있다.

```java
LocalTime time = LocalTime.of(13, 45, 20); // 13:45:20
int hour = time.getHour();
int minute = time.getMinute();
int second = time.getSecond();
```

`parse` 정적 메서드를 사용하면 날짜와 시간 문자열로 `LocalDate`와 `LocalTime`의 인스턴스를 만들 수 있다.

```java
LocalDate date = LocalDate.parse("2020-12-22");
LocalTime time = LocalTime.parse("13:45:20");
```

`parse` 메서드에 `DateTimeFormatter`를 전달할 수도 있다. `DateTimeFormatter`의 인스턴스는 날짜, 시간 객체의 형식을 지정한다. `java.util.DateFormat` 클래스를 대체하는 클래스다. 문자열을 `LocalDate`나 `LocalTime`으로 파싱할 수 없을 때 `parse` 메서드는 `DateTimeParseException`을 일으킨다.

### 날짜와 시간 조합
`LocalDateTime`은 `LocalDate`와 `LocalTime`을 쌍으로 갖는 복합 클래스다. 날짜와 시간을 모두 표현할 수 있으며 정적 메서드 `of`로 인스턴스를 만들 수 있다.

```java
// 2017-09-21T13:45:20
LocalDateTime dt1 = LocalDateTime.of(2017, Month.SEPTEMBER, 21, 13, 45, 20);
LocalDateTime dt2 = LocalDateTime.of(date, time);
LocalDateTime dt3 = date.atTime(13, 45, 20);
LocalDateTime dt4 = date.atTime(time);
LocalDateTime dt5 = time.atDate(date);
```

### Instant 클래스 : 기계의 날짜와 시간
`java.time.Instant` 클래스는 기계적인 관점에서 시간을 표현한다. `Instant` 클래스는 유닉스 에포크 시간(Unix epoch time, 1970년 1월 1일 0시 0분 0초 UTC)을 기준으로 특정 지점까지의 시간을 초로 표현한다. 팩토리 메서드 `ofEpochSecond`에 초를 넘겨줘서 `Instant` 클래스 인스턴스를 만들 수 있다.

`Instant` 클래스는 나노초의 정밀도를 제공한다. 또한 오버로드된 `ofEpochSecond` 메서드 버전에선 두 번째 인수를 이용해서 나노초 단위로 시간을 보정할 수 있다. 두 번째 인수에는 0 ~ 999,999,999 사이의 값을 지정할 수 있다. 다음 네 가지 호출 코드는 같은 `Instant`를 반환한다.

```java
Instant.ofEpochSecond(3);
Instant.ofEpochSecond(3, 0);
Instant.ofEpochSecond(2, 1_000_000_000);
Instant.ofEpochSecond(4, -1_000_000_000);
```

`Instant` 클래스도 사람이 확인할 수 있도록 시간을 표시해주는 정적 팩토리 메서드 `now`를 제공한다. 하지만 기계 전용의 유틸리티이기에 사람이 읽을 수 있는 시간 정보를 제공하지 않는다.

### Duration과 Period 정의
지금까지 살펴본 모든 클래스는 `Temporal` 인터페이스를 구현하는데, `Temporal` 인터페이스는 특정 시간을 모델링하는 객체의 값을 어떻게 읽고 조작할지 정의한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/f8ec1c7b-16ef-40ab-ade3-15f84f8e7dd8)

`Duration` 클래스의 정적 팩토리 메서드 `between`으로 두 시간 객체 사이의 지속 시간을 만들 수 있다.

`Duration` 클래스는 초와 나노초로 시간 단위를 표현하므로 `between` 메서드에 `LocalDate`를 전달할 수 없다. 년, 월, 일로 시간을 표현할 때는 `Period` 클래스를 사용한다. `Period` 클래스의 팩토리 메서드 `between`을 이용하면 두 `LocalDate`의 차이를 확인할 수 있다.

지금까지 살펴본 모든 클래스는 불변이다. 불변 클래스는 함수형 프로그래밍 그리고 스레드 안전성과 도메인 모델의 일관성을 유지하는 데 좋은 특징이다.

## 날짜 조정, 파싱, 포매팅
`withAttribute` 메서드로 기존의 `LocalDate`를 바꾼 버전을 직접 간단하게 만들 수 있다. 다음 코드에선 바뀐 속성을 포함하는 새로운 객체를 반환하는 메서드를 보여준다. 모든 메서드는 기존 객체를 바꾸지 않는다.

```java
LocalDate date1 = LocalDate.of(2017, 9, 21);
LocalDate date2 = date1.withYear(2011);
LocalDate date3 = date2.withDayOfMonth(25);
LocalDate date4 = date3.with(ChronoField.MONTH_OF_YEAR, 2);
```

`get`과 `with` 메서드로 `Temporal` 객체의 필드값을 읽거나 고칠 수 있으며 `Temporal` 객체가 지정된 필드를 지원하지 않으면 `UnsupportedTemporalTypeException`이 발생한다.

### TemporalAdjusters 사용하기
간단한 날짜 기능이 아닌 더 복잡한 날짜 조정기능이 필요할 때 `with` 메서드에 `TemporalAdjuster`를 전달하는 방법으로 문제를 해결할 수 있다. 날짜와 시간 API는 다양한 상황에서 사용할 수 있도록 다양한 `TemporalAdjuster`를 제공한다.

```java
import static java.time.temporal.TemporalAdjusters.*;

LocalDate date1 = LocalDate.of(2014, 3, 18); // 2014-03-18 (화)
LocalDate date2 = date1.with(nextOrSame(DayOfWeek.SUNDAY)); // 2014-03-23
LocalDate date3 = date2.with(lastDayOfMonth()); // 2014-03-31
```

또한 필요한 기능이 정의되어 있지 않을 때는 비교적 쉽게 커스텀 `TemporalAdjuster` 구현을 만들 수 있다.

```java
@FunctionalInterface
public interface TemporalAdjuster {
    Temporal adjustInto(Temporal temporal);
}
```

### 날짜와 시간 객체 출력과 파싱
날짜와 시간 관련 작업에서 포매팅과 파싱은 서로 떨어질 수 없는 관계다. `java.time.format` 패키지가 전용 패키지로 추가되었다. 이 패키지에서 가장 중요한 클래스는 `DateTimeFormatter`다. 정적 팩토리 메서드와 상수를 이용해서 손쉽게 포매터를 만들 수 있다.

```java
LocalDate date = LocalDate.of(2014, 3, 18);
String s1 = date.format(DateTimeFormatter.BASIC_ISO_DATE); // 20140318
String s2 = date.format(DateTimeFormatter.ISO_LOCAL_DATE); // 2014-03-18
```

반대로 날짜나 시간을 표현하는 문자열을 파싱해서 날짜 객체를 다시 만들 수 있다.

```java
LocalDate date1 = LocalDate.parse("20140318", DateTimeFormatter.BASIC_ISO_DATE);
LocalDate date2 = LocalDate.parse("2014-03-18", DateTimeFormatter.ISO_LOCAL_DATE);
```

기존 `java.util.DateFormat` 클래스와 달리 모든 `DateTimeFormatter`는 스레드에서 안전하게 사용할 수 있는 클래스다. 또한 특정 패턴으로 포매터를 만들 수 있는 정적 팩토리 메서드도 제공한다.

```java
DateTimeFormatter formatter = DateTimeFormatter.ofPatterm("dd/MM/yyyy");
LocalDate date = LocalDate.of(2014, 3, 18);
String formattedDate = date1.format(formatter);
LocalDate date2 = LocalDate.parse(formattedDate, formatter);
```

`LocalDate`의 `format` 메서드는 요청 형식의 패턴에 해당하는 문자열을 생성한다. 그리고 정적 메서드 `parse`는 같은 포매터를 적용해서 생성된 문자열을 파싱함으로써 다시 날짜를 생성한다.

또한, `DateTimeFormatterBuilder` 클래스로 복합적인 포매터를 정의해서 좀 더 세부적으로 포매터를 제어할 수 있다.

## 다양한 시간대와 캘린더 활용 방법
지금까지 살펴본 모든 클래스에는 시간대와 관련한 정보가 없었다. 새로운 날짜와 시간 API의 큰 편리함 중 하나는 시간대를 간단하게 처리할 수 있다는 점이다. 기존의 `java.util.TimeZone`을 대체할 수 있는 `java.time.ZonedId` 클래스가 새롭게 등장했다. `ZoneId`를 이용하면 서머타임(DST) 같은 복잡한 사항이 자동으로 처리된다. 또한 `ZoneId`는 불변 클래스다.

### 시간대 사용하기
표준 시간이 같은 지역을 묶어서 시간대(time zone) 규칙 집합을 정의한다. `ZoneRules` 클래스에는 약 40개 정도의 시간대가 있다. `ZoneId`의 `getRules()`를 이용해서 해당 시간대의 규정을 획득할 수 있다.

```java
ZoneId romeZone = ZoneId.of("Europe/Rome");
```

지역 ID는 '{지역}/{도시}' 형식으로 이루어지며 IANA Time Zone Datebase에서 제공하는 지역 집합 정보를 사용한다. `toZoneId`로 기존의 `TimeZone` 객체를 `ZoneId` 객체로 변환할 수 있다.

```java
ZoneId zoneId = TimeZone.getDefault().toZoneId();
```

`ZoneId`는 `LocalDate`, `LocalDateTime`, `Instant`를 이용해서 `ZonedDateTime` 인스턴스로 변환할 수 있다. `ZonedDateTime`은 지정한 시간대에 상대적인 시점을 표현한다.

```java
LocalDate date = LocalDate.of(2014, Month.MARCH, 18);
ZonedDateTime zdt1 = date.atStartOfDay(romeZone);

LocalDateTime dateTime = LocalDateTime.of(2014, Month.MARCH, 18, 13, 45);
ZonedDateTime zdt2 = dateTime.atZone(romeZone);

Instant instant = Instant.now();
ZonedDateTime zdt3 = instant.atZone(romeZone);
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/9f49ca6f-7421-40c1-8f80-dbab48eb0253)

위 그림에서 보여주는 `ZonedDateTime`의 컴포넌트를 보면 `LocalDate`, `LocalTime`, `LocalDateTime`, `ZoneId`의 차이를 쉽게 이해할 수 있다.

### UTC/Greenwich 기준의 고정 오프셋
때로는 UTC(Universal Time Coordinated, 협정 세계시)/GMT(Greenwich Mean Time, 그리니치 표준시)를 기준으로 시간대를 표현하기도 한다. 예를 들어, '뉴욕은 런던보다 5시간 느리다'라고 표현할 수 있다. `ZoneId`의 서브클래스인 `ZoneOffset` 클래스로 런던의 그리니치 0도 자오선과 시간값의 차이를 표현할 수 있다.

```java
ZoneOffset newYorkOffset = ZoneOffset.of("-05:00");
```

위 예제에서 정의한 `ZoneOffset`으로는 서머타임을 제대로 처리할 수 없으므로 권장하지 않는 방식이다. `ZoneOffset`은 `ZoneId`이므로 `ZoneOffset`을 사용할 수 있다. 또한 ISO-8601 캘린더 시스템에서 정의하는 UTC/GMT와 오프셋으로 날짜와 시간을 표현하는 `OffsetDateTime`을 만드는 방법도 있다.

### 대안 캘린더 시스템 사용하기
ISO-8601 캘린더 시스템은 실질적으로 전 세계에 통용된다. 하지만 Java 8에서는 추가로 4개의 캘린더 시스템을 제공한다. `ThaiBuddhistDate`, `MinguoDate`, `JapaneseDate`, `HijrahDate`가 각각의 캘린더 시스템을 대표한다. 이 4개의 클래스와 `LocalDate` 클래스는 `ChronoLocalDate` 인터페이스를 구현하는데, `ChronoLocalDate`는 임의의 연대기에서 특정 날짜를 표현할 수 있는 기능을 제공하는 인터페이스다.

날짜와 시간 API 설계자는 `ChronoLocalDate`보단 `LocalDate`를 사용하라고 권고한다. 프로그램의 입출력을 지역화하는 상황을 제외하고는 모든 데이터 저장, 조작, 비즈니스 규칙 해석 등의 작업에서 `LocalDate`를 사용해야 한다.

## 📌 정리
- Java 8 이전 버전에서 제공하는 기존의 `java.util.Date` 클래스와 관련 클래스에서는 여러 불일치점들과 가변성, 어설픈 오프셋, 기본값, 잘못된 이름 결정 등의 설계 결함이 존재했다.
- 새로운 날짜와 시간 API에서 날짜와 시간 객체는 모두 불변이다.
- 새로운 API는 각각 사람과 기계가 편리하게 날짜와 시간 정보를 관리할 수 있도록 두 가지 표현 방식을 제공한다.
- 날짜와 시간 객체를 절대적인 방법과 상대적인 방법으로 처리할 수 있으며 기존 인스턴스를 변환하지 않도록 처리 결과로 새로운 인스턴스가 생성된다.
- `TemporalAdjuster`를 이용하면 단순히 값을 바꾸는 것 이상의 복잡한 동작을 수행할 수 있으며 자신만의 커스텀 날짜 변환 기능을 정의할 수 있다.
- 날짜와 시간 객체를 특정 포맷으로 출력하고 파싱하는 포매터를 정의할 수 있다. 패턴을 이용하거나 프로그램으로 포매터를 만들 수 있으며 포매터는 스레드 안정성을 보장한다.
- 특정 지역/장소에 상대적인 시간대 또는 UTC/GMT 기준의 오프셋을 이용해서 시간대를 정의할 수 있으며 이 시간대를 날짜와 시간 객체에 적용해서 지역화할 수 있다.
- ISO-8601 표준 시스템을 준수하지 않는 캘린더 시스템도 사용할 수 있다.