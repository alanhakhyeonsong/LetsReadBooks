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