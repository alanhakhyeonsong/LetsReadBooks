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
단항 함수보다 이해하기 어렵다. Point 클래스의 경우 이항 함수가 적절하다. 2개의 인수간의 자연적인 순서가 있어야 한다. 인수가 2개이니 만큼 이해가 어렵고 위험이 따르므로 가능하면 단항 함수로 바꾸도록 애써야 한다.

### 삼항 함수
이항 함수보다 이해하기가 훨씬 어려우므로, 위험도 2배 이상 늘어난다. 삼항 함수를 만들 때는 신중히 고려하라.

### 인수 객체
인수가 많이 필요할 경우, 일부 인수를 독자적인 클래스 변수로 선언할 가능성을 살펴보자. x, y를 인자로 넘기는 것보다 Point를 넘기는 것이 더 낫다.

### 인수 목록
때로는 인수 개수가 가변적인 함수도 필요하다. `String.format` 메서드가 좋은 예시이다. 이 메서드의 인수는 List형 인수이기 때문에 이항 함수라고 할 수 있다.

### 동사와 키워드
함수의 의도나 인수의 순서와 의도를 제대로 표현하려면 좋은 함수 이름이 필수다.
- 단항 함수는 함수와 인수가 동사/명사 쌍을 이뤄야 한다.  
`writeField(name)`
- 함수 이름에 키워드를 추가하면 인수 순서를 기억할 필요가 없어진다.  
`assertExpectedEqualsActual(expected, actual)`

## 부수 효과를 일으키지 마라!
부수 효과는 거짓말이다. 함수에서 한 가지를 하겠다고 약속하고선 남몰래 다른 것도 하기 때문이다.

```java
public class UserValidator {
    private Cryptographer cryptographer;

    public boolean checkPassword(String userName, String password) {
        User user = UserGateway.findByName(userName);
        if (user != User.NULL) {
            String codedPhrase = user.getPhraseEncodedByPassword();
            String phrase = cryptographer.decrypt(codedPhrase, password);
            if ("Valid Password".equals(phrase)) {
                Session.initialize();
                return true;
            }
        }
        return false;
    }
}
```

`Session.initialize()` 호출은 함수가 일으키는 부수 효과이다. 이런 부수 효과가 시간적인 결합을 초래하여 혼란을 일으킨다.

### 출력 인수
일반적으로 출력 인수는 피해야 한다. 함수에서 상태를 변경해야 한다면 함수가 속한 객체 상태를 변경하는 방식을 택한다.

## 명령과 조회를 분리하라!
함수는 뭔가를 수행하거나 뭔가에 답하거나 둘 중 하나만 해야 한다. 둘 다 수행하면 안 된다. 객체 상태를 변경하거나 아니면 객체 정보를 반환하거나 둘 중 하나다.

`public boolean set(String attribute, String value);` 같은 경우에는 속성 값 설정 성공 시 true를 반환하므로 `if (set("username", "unclebob"))` 같은 괴상한 코드가 나온다.

명령과 조회를 분리해 혼란을 주지 않도록 하자.
```java
if (attributeExists("username")) {
    setAttribute("username", "unclebob");
    ...
}
```

## 오류 코드보다 예외를 사용하라!
명령 함수에서 오류 코드를 반환하는 형식은 명령/조회 분리 규칙을 미묘하게 위반한다. if 문에서 명령을 표현식으로 사용하기 쉬운 탓이다. 이는 곧 여러 단계로 중첩되는 코드를 야기한다.

```java
if (deletePage(page) == E_OK) {
	if (registry.deleteReference(page.name) == E_OK) {
		if (configKeys.deleteKey(page.name.makeKey()) == E_OK) {
			logger.log("page deleted");
		} else {
			logger.log("configKey not deleted");
		}
	} else {
		logger.log("deleteReference from registry failed"); 
	} 
} else {
	logger.log("delete failed"); return E_ERROR;
}
```

try/catch를 사용하면 오류 처리 코드가 원래 코드에서 분리되므로 코드가 깔끔해진다.

### Try/Catch 블록 뽑아내기
정상 작동과 오류 처리 동작을 뒤섞는 추한 구조이므로 if/else와 마찬가지로 블록을 별도 함수로 뽑아내는 편이 좋다.

```java
public void delete(Page page) {
    try {
        deletePageAndAllReferences(page);
    } catch (Exception e) {
        logError(e);
    }
}

private void deletePageAndAllReferences(Page page) throws Exception {
    deletePage(page);
    registry.deleteReference(page.name);
    configKeys.deleteKey(page.name.makeKey());
}

private void logError(Exception e) {
    logger.log(e.getMessage());
}
```

### 오류 처리도 한 가지 작업이다.
함수는 '한 가지' 작업만 해야 한다. 오류 처리도 '한 가지' 작업에 속한다. 그러므로 오류를 처리하는 함수는 오류만 처리해야 마땅하다. 함수에 키워드 try가 있다면 함수는 try로 시작해 catch/finally 문으로 끝나야 한다.

### Error.java 의존성 자석
오류 코드를 반환한다는 이야기는, 클래스든 열거형 변수든, 어디선가 오류 코드를 정의한다는 뜻이다.

```java
public enum Error {
    OK,
    INVALID,
    NO_SUCH,
    LOCKED,
    OUT_OF_RESOURCES,
    WAITING_FOR_EVENT;
}
```
위와 같은 클래스는 의존성 자석이다. 새 오류를 추가하거나 변경할 때 코스트가 많이 필요하다. 따라서 예외를 사용하는 것이 더 안전하다.

## 반복하지 마라!
중복은 소프트웨어에서 모든 악의 근원이다. 많은 원칙과 기법이 중복을 없애거나 제어할 목적으로 나왔다. 늘 중복을 없애도록 노력해야 한다.

## 구조적 프로그래밍
다익스트라의 구조적 프로그래밍의 원칙을 따르자면 모든 함수와 함수 내 모든 블록에 입구와 출구가 하나만 존재해야 한다. 즉, 함수는 return 문이 하나여야 되며, 루프 안에서 break나 continue를 사용해선 안 되며 goto는 절대로 안된다. 함수가 클 경우에만 상당 이익을 제공하므로, 함수를 작게 만든다면 오히려 여러차례 사용하는 것이 함수의 의도를 표현하기 쉬워진다.

구조적 프로그래밍의 목표와 규율은 공감하지만 함수가 작다면 위 규칙은 별 이익을 제공하지 못한다. 함수가 아주 클 때만 상당한 이익을 제공한다. 따라서 함수를 작게 만든다면 간혹 return, break, continue를 사용해도 괜찮다. 오히려 때로는 단일 입/출구 규칙보다 의도를 표현하기 쉬워진다.

## 함수를 어떻게 짜죠?
처음에는 길고 복잡하고, 들여쓰기 단계나 중복된 루프도 많다. 인수 목록도 아주 길다. 이 코드들을 빠짐없이 테스트하는 단위 테스트 케이스도 만들고, 코드를 다듬고, 함수를 만들고, 이름을 바꾸고, 중복을 제거한다. 처음부터 탁 짜내지 않는다. 그게 가능한 사람은 없다.