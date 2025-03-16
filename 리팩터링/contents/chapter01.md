# 1장. 리팩터링: 첫 번째 예시

```json
# plays.json
{
  "hamlet": {"name": "Hamlet", "type":  "tragedy"},
  "as-like": {"name": "As You Like It", "type":  "comedy"},
  "othello": {"name": "Othello", "type":  "tragedy"}
}

# invoices.json
[
  {
    "customer": "BigCo",
    "performances": [
      {
        "playID": "hamlet",
        "audience": 55
      },
      {
        "playID": "as-like",
        "audience": 35
      },
      {
        "playID": "othello",
        "audience": 40
      }
    ]
  }
]
```

```javascript
function statement(invoice, plays) {
  let totalAmount = 0;
  let volumeCredits = 0;
  let result = `청구 내역 (고객명: ${invoice.customer})\n`;
  const format = new Intl.NumberFormat("en-US",
                        { style: "currency", currency: "USD", minimumFractionDigits: 2 }).format;
  
  for (let perf of invoice.performances) {
    const play = plays[perf.playID];
    let thisAmount = 0;

    switch (play.type) {
      case "tragedy":
        thisAmount = 40000;
        if (perf.audience > 30) {
          thisAmount += 1000 * (perf.audience - 30);
        }
        break;
      case "comedy":
        thisAmount = 30000;
        if (perf.audience > 20) {
          thisAmount += 10000 + 500 * (perf.audience - 20);
        }
        thisAmount += 300 * perf.audience;
        break;
      default:
        throw new Error(`알 수 없는 장르: ${play.type}`);
    }

    // 포인트를 적립한다.
    volumeCredits += Math.max(perf.audience - 30, 0);
    // 희극 관객 5명마다 추가 포인트를 제공한다.
    if ("comedy" === play.type) volumeCredits += Math.floor(perf.audience / 5);

    // 청구 내역을 출력한다.
    result += `  ${play.name}: ${format(thisAmount/100)} (${perf.audience}석)\n`;
    totalAmount += thisAmount;
  }
  result += `총액: ${format(totalAmount/100)}\n`;
  result += `적립 포인트: ${volumeCredits}점\n`;
  return result;
}
```

## 예시 프로그램을 본 소감
- 코드를 수정하려면 사람이 개입되고, 사람은 코드의 미적 상태에 민감하다. 설계가 나쁜 시스템은 수정하기 어렵다. 원하는 동작을 수행하도록 하기 위해 수정해야 할 부분을 찾고, 기존 코드와 잘 맞물려 작동하게 할 방법을 강구하기 어렵기 때문이다. 무엇을 수정할지 찾기 어렵다면 실수를 저질러서 버그가 생길 가능성도 높아진다.
- 수백 줄짜리 코드를 수정할 때면 먼저 프로그램의 작동 방식을 더 쉽게 파악할 수 있도록 코드를 여러 함수와 프로그램 요소로 재구성한다. 프로그램의 구조가 빈약하다면 대체로 구조부터 바로잡은 뒤에 기능을 수정하는 편이 작업하기가 훨씬 수월하다.

> 프로그램이 새로운 기능을 추가하기에 편한 구조가 아니라면, 먼저 기능을 추가하기 쉬운 형태로 리팩토링하고 나서 원하는 기능을 추가한다.

- 리팩터링이 필요한 이유는 변경 때문이다. 잘 작동하고 나중에 변경할 일이 절대 없다면 코드를 현재 상태로 놔둬도 아무런 문제가 없다. 하지만 다른 사람이 읽고 이해해야 할 일이 생겼는데 로직을 파악하기 어렵다면 뭔가 대책을 마련해야 한다.

## 리팩터링의 첫 단계
- **리팩터링할 코드 영역을 꼼꼼하게 검사해줄 테스트 코드들부터 마련해야 한다. 리팩터링에서 테스트의 역할은 굉장히 중요하다.**
- 성공/실패를 스스로 판단하는 자가진단 테스트로 만든다.
- 리팩터링 시 테스트에 의지하는 이유는 내가 저지른 실수로부터 보호해주는 버그 검출기 역할을 해주기 때문이다. 원하는 내용을 소스 코드와 테스트 코드 양쪽에 적어두면, 두 번 다 똑같이 실수하지 않는 한 버그 검출기에 반드시 걸린다.
- 테스트를 작성하는 데 시간이 좀 걸리지만, 신경 써서 만들어두면 디버깅 시간이 줄어서 전체 작업 시간은 오히려 단축된다.

## 책의 JavaScript 예제를 Java로 구현 (Self)
### JSON 데이터 매핑
```java
package me.ramos.chapter01;

import java.util.ArrayList;
import java.util.List;

public class Invoice {

    private final String customer;
    private final List<Performance> performances = new ArrayList<>();

    public Invoice(String customer, List<Performance> performances) {
        this.customer = customer;
        this.performances.addAll(performances);
    }

    public String getCustomer() {
        return customer;
    }

    public List<Performance> getPerformances() {
        return performances;
    }
}
```

```java
package me.ramos.chapter01;

public class Performance {

    private final String playId;
    private final int audience;

    public Performance(String playId, int audience) {
        this.playId = playId;
        this.audience = audience;
    }

    public String getPlayId() {
        return playId;
    }

    public int getAudience() {
        return audience;
    }
}
```

```java
package me.ramos.chapter01;

public class Play {
    private final String name;
    private final PlayType type;

    public Play(String name, PlayType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public PlayType getType() {
        return type;
    }
}
```

```java
package me.ramos.chapter01;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Plays {
    private final Map<String, Play> plays = new ConcurrentHashMap<>();

    public Plays(Map<String, Play> plays) {
        this.plays.putAll(plays);
    }

    public Play getPlay(Performance performance) {
        return plays.get(performance.getPlayId());
    }
}
```

```java
package me.ramos.chapter01;

public enum PlayType {
    TRAGEDY, COMEDY
}
```

### Statement 출력

```java
package me.ramos.chapter01;

public class Statement {
    public String statement(Invoice invoice, Plays plays) throws Exception {
        StatementData statementData = new StatementData(invoice, plays);
        return renderPlainText(statementData);
    }

    private String renderPlainText(StatementData statementData) throws Exception {
        StringBuilder result = new StringBuilder(String.format("청구내역 (고객명: %s)\n", statementData.getCustomer()));
        for (Performance performance : statementData.getPerformances()) {
            result.append(String.format("%s: $%d %d석\n",statementData.playFor(performance).getName(), statementData.amountFor(performance) / 100, performance.getAudience()));
        }

        result.append(String.format("총액: $%d\n", statementData.totalAmount()));
        result.append(String.format("적립 포인트: %d점", statementData.totalVolumeCredits()));
        return result.toString();
    }

    private String renderHtml(StatementData statementData) throws Exception {
        StringBuilder result = new StringBuilder(String.format("<h1> 청구내역 (고객명: %s)\n </h1>", statementData.getCustomer()));
        result.append("<table> \n");
        result.append("<tr><th> 연극 </th> <th>좌석 수</th> <th>금액</th>");
        for (Performance performance : statementData.getPerformances()) {
            result.append(String.format("<tr><td> %s: </td> <td> $%d </td> <td> %d석 </td></tr>\n",statementData.playFor(performance).getName(), statementData.amountFor(performance) / 100, performance.getAudience()));
        }
        result.append("</table>\n");

        result.append(String.format("총액: $%d\n", statementData.totalAmount()));
        result.append(String.format("적립 포인트: %d점", statementData.totalVolumeCredits()));
        return result.toString();
    }
}
```

```java
package me.ramos.chapter01;

import java.util.List;
import me.ramos.chapter01.calculator.PerformanceCalculatorFactory;

public class StatementData {
    private final Invoice invoice;
    private final Plays plays;
    private final PerformanceCalculatorFactory performanceCalculatorFactory;

    public StatementData(Invoice invoice, Plays plays) {
        this.invoice = invoice;
        this.plays = plays;
        this.performanceCalculatorFactory = new PerformanceCalculatorFactory();
    }

    public String getCustomer() {
        return invoice.getCustomer();
    }

    public List<Performance> getPerformances() {
        return invoice.getPerformances();
    }

    public Play playFor(Performance performance) {
        return plays.getPlay(performance);
    }

    public int amountFor(Performance performance) throws Exception {
        return performanceCalculatorFactory
                .createPerformanceCalculator(performance, playFor(performance))
                .amountFor();
    }


    public int totalAmount() throws Exception {
        int totalAmount = 0;

        for (Performance performance : invoice.getPerformances()) {
            totalAmount += amountFor(performance);
        }
        return totalAmount / 100;
    }

    public int totalVolumeCredits() throws Exception {
        int volumeCredit = 0;
        for (Performance performance : invoice.getPerformances()) {
            volumeCredit += volumeCreditFor(performance);
        }
        return volumeCredit;
    }

    private int volumeCreditFor(Performance performance) throws Exception {
        return performanceCalculatorFactory
                .createPerformanceCalculator(performance, playFor(performance))
                .volumeCreditFor();
    }
}
```

### Performance Calculator

```java
package me.ramos.chapter01.calculator;

import me.ramos.chapter01.Performance;
import me.ramos.chapter01.Play;

public class PerformanceCalculator {
    protected Performance performance;
    protected Play play;

    public PerformanceCalculator(Performance performance, Play play) {
        this.performance = performance;
        this.play = play;
    }

    public int amountFor() throws Exception {
        throw new Exception("서브 클래스에서 이를 모두 구현했습니다.");
    }

    public int volumeCreditFor() throws Exception {
        throw new Exception("서브 클래스에서 이를 모두 구현했습니다.");
    }
}
```

```java
package me.ramos.chapter01.calculator;

import me.ramos.chapter01.Performance;
import me.ramos.chapter01.Play;

public class PerformanceCalculatorFactory {

    public PerformanceCalculator createPerformanceCalculator(Performance performance, Play play) throws Exception {
        switch (play.getType()) {
            case TRAGEDY -> {
                return new TragedyCalculator(performance, play);
            }
            case COMEDY -> {
                return new ComedyCalculator(performance, play);
            }
            default -> throw new Exception("알 수 없는 타입입니다." + play.getType());
        }
    }
}
```

```java
package me.ramos.chapter01.calculator;

import me.ramos.chapter01.Performance;
import me.ramos.chapter01.Play;

public class ComedyCalculator extends PerformanceCalculator {

    public ComedyCalculator(Performance performance, Play play) {
        super(performance, play);
    }

    @Override
    public int amountFor() throws Exception {
        int result = 30000;
        if (performance.getAudience() > 20) {
            result += 10000 + 500 * (performance.getAudience() - 20);
        }
        result += 300 * performance.getAudience();
        return result;
    }

    @Override
    public int volumeCreditFor() throws Exception {
        int result = 0;

        result += Math.max(performance.getAudience() - 30, 0);
        result += Math.floor(performance.getAudience() / 5);

        return result;
    }
}
```

```java
package me.ramos.chapter01.calculator;

import me.ramos.chapter01.Performance;
import me.ramos.chapter01.Play;

public class TragedyCalculator extends PerformanceCalculator {

    public TragedyCalculator(Performance performance, Play play) {
        super(performance, play);
    }

    @Override
    public int amountFor() throws Exception {
        int result = 40000;
        if (performance.getAudience() > 30) {
            result += 1000 * (performance.getAudience() - 30);
        }
        return result;
    }

    @Override
    public int volumeCreditFor() throws Exception {
        int result = 0;

        result += Math.max(performance.getAudience() - 30, 0);

        return result;
    }
}
```

## 마치며
- 함수 추출하기, 변수 인라인하기, 함수 옮기기, 조건부 로직을 다형성으로 바꾸기 등을 비롯한 다양한 리팩터링 기법들을 적용하자.
- 리팩터링은 보통 세 단계로 진행하곤 한다.
  - 원본 함수를 중첩 함수 여러 개로 나누기
  - 단계 쪼개기 : 계산 코드와 출력 코드 분리
  - 계산 로직을 다형성으로 표현
- 리팩터링은 대부분 코드가 하는 일을 파악하는 데서 시작한다.
- 좋은 코드를 가늠하는 확실한 방법은 '얼마나 수정하기 쉬운가'다.