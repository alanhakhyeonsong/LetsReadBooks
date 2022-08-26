# 2장. 의미 있는 이름

## 의도를 분명히 밝혀라
변수나 함수 그리고 클래스 이름은 다음과 같은 굵직한 질문에 모두 답해야 한다.
- 변수(혹은 함수나 클래스)의 존재 이유는?
- 수행 기능은?
- 사용 방법은?

따로 주석이 필요하다면 의도를 분명히 드러내지 못했다는 말이다.

```java
// Bad Example
int d; // 경과 시간(단위: 날짜)

// Good Example
int elapsedTimeInDays;
int daysSinceCreation;
int daysSinceModification;
int fileAgeInDays;
```

```java
// Bad Example
public List<Int[]> getThem() {
    List<Int[]> list1 = new ArrayList<int[]>();
    for (int[] x : theList) {
        if (x[0] == 4)
            list1.add(x);
    }
    return list1;
}
```
코드의 단순성이 문제가 아니라 코드의 함축성이 문제이다. 코드 맥락이 코드 자체에 명시적으로 드러나지 않는다.
- `theList`에 무엇이 들었는가?
- `theList`에서 0번째 값이 어째서 중요한가?
- 값 4는 무슨 의미인가?
- 함수가 반환하는 리스트 `list1`을 어떻게 사용하는가?

```java
// Good Example
public List<int[]> getFlaggedCells() {
    List<int[]> flaggedCells = new ArrayList<int[]>();
    for (int[] cell : gameBoard)
        if (cell[STATUS_VALUE] == FLAGGED)
            flaggedCells.add(cell);
    return flaggedCells;
}
```

## 그릇된 정보를 피하라
- 프로그래머는 코드에 그릇된 단서를 남겨서는 안 된다. 이는 코드의 의미를 흐린다.
- 개발자에게 특수한 의미를 가지는 단어(`List` 등)는 실제 컨테이너가 `List`가 아닌 이상 `accountList`와 같이 변수명에 붙이지 말자. 차라리 `accountGroup`, `bunchOfAccounts`, `accounts` 등으로 명명하자.
- 비슷해 보이는 명명에 주의하자.

## 의미 있게 구분하라
- 말이 안되는 단어(한 글자만 바꾼다던지 한 단어), `a1, a2, a3`과 같이 숫자로 구분하는 경우 주의
- 클래스 이름에 `Info`, `Data`와 같은 불용어를 붙이지 말자. 정확한 개념 구분이 되지 않는다.
- `Name` vs `NameString`
- `getActiveAccount()` vs `getActiveAccounts()` vs `getActiveAccountInfo()`: 이들이 혼재할 경우 서로의 역할을 정확히 구분하기 어렵다.
- `money` vs `moneyAmount`
- `message` vs `theMessage`

## 발음하기 쉬운 이름을 사용하라
```java
// Bad Example
class DtaRcrd102 {
    private Date genymdhms;
    private Date modymdhms;
    private final String pszqint = "102";
    /* ... */
};
```

```java
// Good Example
class Customer {
    private Date generationTimestamp;
    private Date modificationTimestamp;
    private final String recordId = "102";
    /* ... */
};
```

## 검색하기 쉬운 이름을 사용하라
- 상수는 `static final`과 같이 정의해 쓰자.
- 변수 이름의 길이는 변수의 범위에 비례해서 길어진다.

## 인코딩을 피하라
- 헝가리식 표기법
  - 변수명에 해당 변수의 타입(`String`, `Int` 등)을 적지 말자
- 멤버 변수 접두어
  - `m_`이라는 접두어를 붙이지 말자
- 인터페이스와 구현 클래스
  - 인터페이스와 구현 클래스를 나눠야 한다면 구현 클래스의 이름에 정보를 인코딩하자.

|Do / Don't|Interface|Implementation class|
|--|--|--|
|Don't|IShapeFactory|ShapeFactory|
|Do|ShapeFactory|ShapeFactoryImpl|
|Do|ShapeFactory|CShapeFactory|

## 자신의 기억력을 자랑하지 마라
- 독자가 코드를 읽으면서 변수 이름을 자신이 아는 이름으로 변환해야 한다면 그 변수 이름은 바람직하지 못하다.  
(URL에서 호스트와 프로토콜을 제외한 소문자 주소를 r이라는 변수로 명명하는 일 등의 예시)
- 똑똑한 프로그래머와 전문가 프로그래머 사이에서 나타나는 차이점 하나만 들자면, 명료함이다.

## 클래스 이름
- 명사 혹은 명사구가 적합하다. (`postPayment`, `deletePage`, `save` 등)
- 접근자, 변경자, 조건자는 `get`, `set`, `is`로 시작하자.
- 생성자를 오버로드할 경우 정적 팩토리 메서드를 사용하고 해당 생성자를 private으로 선언한다.

```java
Complex fulcrumPoint = new Complex(23.0);
Complex fulcrumPoint = Complex.FromRealNumber(23.0);
```

## 기발한 이름은 피하라
특정 문화에서만 사용하는 농담은 피하는 것이 좋다. 의도를 분명히 표현하는 이름을 사용하라.
- `HolyHandGrenade` -> `DeleteItems`
- `whack()` -> `kill()`

## 한 개념에 한 단어를 사용하라
추상적인 개념 하나에 단어 하나를 사용하자.
- fetch, retrieve, get
- controller, manager, driver

## 말장난을 하지 마라
한 단어를 두 가지 목적으로 사용하지 마라.

```java
public static String add(String message, String messageToAppend);
public List<Element> add(Element element); // append 혹은 insert로 바꾸는 것이 옳다.
```

## 해법 영역에서 가져온 이름을 사용하라
개발자라면 당연히 알고 있을 `JobQueue`, `AccountVisitor(Visitor pattern)` 등을 사용하지 않을 이유는 없다. 전산 용어, 알고리즘 이름, 패턴 이름, 수학 용어 등은 사용하자.

## 문제 영역에서 가져온 이름을 사용하라
적절한 프로그래머 용어가 없거나 문제 영역과 관련이 깊은 용어의 경우 문제 영역 용어를 사용하자.

## 의미 있는 맥락을 추가하라
- 클래스, 함수, namespace 등으로 감싸서 맥락(Context)을 표현하라.
- 그래도 불분명하다면 접두어를 사용하자.

```java
// Bad Example
private void printGuessStatistics(char candidate, int count) {
    String number;
    String verb;
    String pluralModifier;
    if (count == 0) {
        number = "no";
        verb = "are";
        pluralModifier = "s";
    } else if (count == 1) {
        number = "1";
        verb = "is";
        pluralModifier = "";
    } else {
        number = Integer.toString(count);
        verb = "are";
        pluralModifier = "s";
    }

    String guessMessage = String.format("There %s %s %s%s", verb, number, candidate, pluralModifier);
    print(guessMessage);
}
```

```java
// Good Example
public class GuessStatisticsMessage {
    private String number;
    private String verb;
    private String pluralModifier;

    public String make(char candidate, int count) {
        createPluralDependentMessageParts(count);
        return String.format("There %s %s %s%s", verb, number, candidate, pluralModifier);
    }

    private void createPluralDependentMessageParts(int count) {
        if (count == 0) {
            thereAreNoLetters();
        } else if (count == 1) {
            thereIsOneLetter();
        } else {
            thereAreManyLetters(count);
        }
    }

    private void thereAreManyLetters(int count) {
        number = Integer.toString(count);
        verb = "are";
        pluralModifier = "s";
    }

    private void thereIsOneLetter() {
        number = "1";
        verb = "is";
        pluralModifier = "";
    }

    private void thereAreNoLetters() {
        number = "no";
        verb = "are";
        pluralModifier = "s";
    }
}
```

## 불필요한 맥락을 없애라
`Gas Station Delux`라는 애플리케이션을 짠다고 해서 클래스 이름을 GSD로 시작하지 말자. IDE에서 G를 입력하고 자동 완성 키를 누르면 IDE는 모든 클래스를 열거하기에 효율적이지 못하다.  
일반적으로는 짧은 이름이 긴 이름보다 좋다. 단, 의미가 분명한 경우에 한해서다. 이름에 불필요한 맥락을 추가하지 않도록 주의한다.

## 마치면서
좋은 이름을 선택하려면 설명 능력이 뛰어나야 하고 문화적인 배경이 같아야 한다. 이것이 제일 어렵다. 좋은 이름을 선택하는 능력은 기술, 비즈니스, 관리 문제가 아니라 교육 문제다.  
사람들이 이름을 바꾸지 않으려는 이유 하나는 다른 개발자가 반대할까 두려워서다. 우리들 생각은 다르다. 오히려 (좋은 이름으로 바꿔주면) 반갑고 고맙다. 우리들 대다수는 자신이 짠 클래스 이름과 메서드 이름을 모두 암기하지 못한다. 암기는 요즘 나오는 도구에게 맡기고, 우리는 문장이나 문단처럼 읽히는 코드 아니면 적어도 표나 자료 구조처럼 읽히는 코드를 짜는 데만 집중해야 마땅하다.