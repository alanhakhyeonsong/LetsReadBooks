# 3장. 함수
길이가 길고, 중복된 코드에, 괴상한 문자열에, 낯설고 모호한 자료 유형과 API로 이루어진 함수는 이해하기에 많은 어려움이 따른다. 읽기 쉽고 이해하기 쉬운 함수는 어떻게 작성해야 할까?

## 작게 만들어라!
**함수를 만드는 첫째 규칙은 '작게!'다. 함수를 만드는 둘째 규칙은 '더 작게!'다.**

```java
public static String renderPageWithSetupsAndTeardowns(PageData pageData, boolean isSuite) throws Exception {
    boolean isTestPage = pageData.hasAttribute("Test");
    if (isTestPage) {
        WikiPage testPage = pageData.getWikiPage();
        StringBuffer newPageContent = new StringBuffer();
        includeSetupPages(testPage, newPageContent, isSuite);
        newPageContent.append(pageData.getContent());
        includeTeardownPages(testPage, newPageContent, isSuite);
        pageData.setContent(newPageContent.toString());
    }
    return pageData.getHtml();
}
```

위 코드도 길다. 한 함수당 되도록 3~5줄 이내로 줄이는 것을 권장한다.
```java
public static String renderpageWithSetupsAndTeardowns(PageData pageData, boolean isSuite) throws Exception {
    if (isTestPage(pageData))
        includeSetupAndTeardownPages(pageData, isSuite);
    return pageData.getHtml();
}
```

### 블록과 들여쓰기
중첩 구조(if/else/while 문 등)에 들어가는 블록은 한 줄이어야 한다. 대개 거기서 함수를 호출한다. 그러면 바깥을 감싸는 함수가 작아질 뿐 아니라, 블록 안에서 호출하는 함수 이름을 적절히 짓는다면 코드를 이해하기도 쉬워진다.

중첩 구조가 생길만큼 함수가 커져서는 안된다. 따라서 함수에서 들여쓰기 수준은 1단이나 2단을 넘어서면 안 된다.

## 한 가지만 해라!
**함수는 한 가지를 해야 한다. 그 한 가지를 잘 해야 한다. 그 한 가지만을 해야 한다.**

지정된 함수 이름 아래에서 추상화 수준이 하나인 단계만 수행한다면 그 함수는 한 가지 작업만 한다.

함수가 '한 가지만' 하는지 판단하는 방법이 있다. 단순히 다른 표현이 아니라 의미 있는 이름으로 다른 함수를 추출할 수 있다면 그 함수는 여러 작업을 하는 셈이다.

## 함수 당 추상화 수준은 하나로!
함수가 확실히 '한 가지' 작업만 하려면 함수 내 모든 문장의 추상화 수준이 동일해야 한다. 한 함수 내에 추상화 수준을 섞으면 코드를 읽는 사람이 헷갈린다.

### 위에서 아래로 코드 읽기: 내려가기 규칙
코드는 위에서 아래로 이야기처럼 읽혀야 좋다. 한 함수 다음에는 추상화 수준이 한 단계 낮은 함수가 온다. 즉, 위에서 아래로 프로그램을 읽으면 함수 추상화 수준이 한 번에 한 단계씩 낮아진다. 이를 내려가기 규칙이라 한다.

## Switch 문
switch 문은 작게 만들기 어렵지만, 다형성을 이용해 abstract factory에 숨겨 다형적 객체를 생성하는 코드 안에서만 switch 문을 사용하도록 한다.

```java
public Money calculatePay(Employee e) throws InvalidEmployeeType {
    switch (e.type) {
        case COMMISSIONED:
          return calculateCommissionedPay(e);
        case HOURLY:
          return calculateHourlyPay(e);
        case SALARIED:
          return calculateSalariedPay(e);
        default:
          throw new InvalidEmployeeType(e.type);
    }
}
```

위 함수에는 몇 가지 문제가 있다.
1. 함수가 길다.
2. '한 가지' 작업만 수행하지 않는다.
3. SRP를 위반한다.
4. OCP를 위반한다.
5. 위 함수와 구조가 동일한 함수가 무한정 존재한다.

```java
public abstract class Employee {
    public abstract boolean isPayday();
    public abstract Money calculatePay();
    public abstract void deliverPay(Money pay);
}
-----------------------------------------
public interface EmployeeFactory {
    public Employee makeEmployee(EmployeeRecord r) throws InvalidEmployeeType;
}
-----------------------------------------
public class EmployeeFactoryImpl implements EmployeeFactory {
    public Employee makeEmployee(EmployeeRecord r) throws InvalidEmployeeType {
        switch (r.type) {
            case COMMISSIONED:
              return new CommissionedEmployee(r);
            case HOURLY:
              return new HourlyEmployee(r);
            case SALARIED:
              return new SalariedEmployee(r);
            default:
              throw new InvalidEmployeeType(r.type);
        }
    }
}
```

## 서술적인 이름을 사용하라!
"코드를 읽으면서 짐작했던 기능을 각 루틴이 그대로 수행한다면 깨끗한 코드라 불러도 되겠다." - 워드

함수가 작고 단순할수록 서술적인 이름을 고르기도 쉬워진다. 일관성 있는 서술형 이름을 사용한다면 코드를 순차적으로 이해하기도 쉬워진다.

## 함수 인수
함수에서 이상적인 인수 개수는 0개(무항)다. 인수는 코드 이해에 방해가 되는 요소이므로 최선은 0개이고, 차선은 1개뿐인 경우이다. 출력 인수(함수의 반환 값이 아닌 입력 인수로 결과를 받는 경우)는 이해하기 어려우므로 왠만하면 쓰지 않는 것이 좋다.

### 많이 쓰는 단항 형식
- 인수에 질문을 던지는 경우  
`boolean fileExistes("MyFile");`
- 인수를 뭔가로 변환해 결과를 반환하는 경우  
`InputStream fileOpen("MyFile");`
- 이벤트 함수일 경우 (이벤트라는 사실이 코드에 명확하게 드러나야 한다.)

위 3가지가 아니라면 단항 함수는 가급적 피하는 것이 좋다.

### 플래그 인수
플래그 인수는 추하다. 함수로 bool 값을 넘기는 것 자체가 그 함수는 한꺼번에 여러 가지를 처리한다고 대놓고 공표하는 셈이다.

### 이항 함수
