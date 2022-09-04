# 4장. 주석
> 나쁜 코드에 주석을 달지 마라. 새로 짜라.  
> (브라이언 W. 커니핸, P.J. 플라우거)

주석은 필요악이다. 우리는 코드로 의도를 표현하지 못해, 실패를 만회하기 위해 주석을 사용한다. 주석은 언제나 실패를 의미한다. 주석 없이는 자신을 표현할 방법을 찾지 못해 할 수 없이 주석을 사용한다. 그래서 주석은 반겨 맞을 손님이 아니다.

주석을 무시하는 이유는 주석이 오래될수록 코드에서 멀어져서 거짓말을 하게 될 가능성이 커지기 때문이다. 코드는 유지보수를 해도, 주석은 계속 유지보수하기 현실적으로 불가능하기 때문이다.

부정확한 주석은 아예 없는 주석보다 훨씬 더 나쁘다. 이는 독자를 현혹하고 오도한다. 진실은 한 곳에만 존재한다. 바로 코드다. 코드만이 자기가 하는 일을 진실되게 말한다. 코드만이 정확한 정보를 제공하는 유일한 출처다. 그러므로 우리는 (간혹 필요할지라도) 주석을 가능한 줄이도록 꾸준히 노력해야 한다.

## 주석은 나쁜 코드를 보완하지 못한다.
코드에 주석을 추가하는 일반적인 이유는 코드 품질이 나쁘기 때문이다. 표현력이 풍부하고 깔끔하며 주석이 거의 없는 코드가, 복잡하고 어수선하며 주석이 많이 달린 코드보다 훨씬 좋다. 자신이 저지른 난장판을 주석으로 설명하려 애쓰는 대신 그 난장판을 깨끗이 치우는 데 시간을 보내라!

## 코드를 의도로 표현하라!
```java
if ((employee.flags & HOURLY_FLAG) && (employee.age > 65))
```

```java
if (employee.isEligibleForFullBenefits())
```
많은 경우 위 예제와 같이 주석으로 달려는 설명을 함수로 표현하여 충분히 깔끔하게 만들 수 있다.

## 좋은 주석
### 법적인 주석
각 소스 파일 첫머리에 들어가는 저작권 정보와 소유권 정보 등

```
// Copyright (C) 2003, 2004, 2005 by Object Montor, Inc. All right reserved.
// GNU General Public License
```

### 정보를 제공하는 주석
기본적인 정보를 주석으로 제공하면 편리하다. 다음은 추상 메서드가 반환할 값을 설명한다.
```java
// 테스트 중인 Responder 인스턴스를 반환한다.
protected abstract Responder responderInstance();
```
위 코드는 함수 이름에 정보를 담는 편이 더 좋다.  
`responderBeingTested`로 바꾸면 주석이 필요 없어진다.

```java
// kk:mm:ss EEE, MMM dd, yyyy 형식이다.
Pattern timeMatcher = Pattern.compile("\\d*:\\d*\\d* \\w*, \\w*, \\d*, \\d*");
```

### 의도를 설명하는 주석
```java
// 스레드를 대량 생성하는 방법으로 어떻게든 경쟁 조건을 만들려 시도한다.
for (int i = 0; i < 25000; i++) {
    WidgetBuilderThread widgetBuilderThread = new WidgetBuilderThread(widgetBuilder, text, parent, failFlag);
    Thread thread = new Thread(widgetBuilderThread);
    thread.start();
}
```

### 의미를 명료하게 밝히는 주석
때때로 모호한 인수나 반환값은 그 의미를 읽기 좋게 표현하면 이해하기 쉬워진다.
```java
assertTrue(a.compareTo(a) == 0); // a == a
assertTrue(a.compareTo(b) != 0); // a != b
assertTrue(ab.compareTo(ab) == 0); // ab == ab
assertTrue(a.compareTo(b) == -1); // a < b
assertTrue(aa.compareTo(ab) == -1); // aa < ab
```

### 결과를 경고하는 주석
```java
// 여유 시간이 충분하지 않다면 실행하지 마십시오.
public void_testWithReallyBigFile() {}
```

### TODO 주석
```java
// TODO-MdM 현재 필요하지 않다.
// 체크아웃 모델을 도입하면 함수가 필요 없다.
protected VersionInfo makeVersion() throws Exception {
    return null;
}
```

TODO 주석은 프로그래머가 필요하다 여기지만 당장 구현하기 어려운 업무를 기술한다. 더 이상 필요 없는 기능을 삭제하라는 알림, 누군가에게 문제를 봐달라는 요청, 더 좋은 이름을 떠올려달라는 부탁, 앞으로 발생할 이벤트에 맞춰 코드를 고치라는 주의 등에 유용하다. 요즘은 IDE를 통해 남은 TODO를 쉽게 볼 수 있으므로 편리하게 이용할 수 있다.

### 중요성을 강조하는 주석
```java
String listItemContent = match.group(3).trim();
// 여기서 trim은 정말 중요하다. trim 함수는 문자열에서 시작 공백을 제거한다.
// 문자열에 시작 공백이 있으면 다른 문자열로 인식되기 때문이다.
new ListItemWidget(this, listItemContent, this.level + 1);
return buildList(text.substring(match.end()));
```

### 공개 API에서 Javadocs
설명이 잘 된 공개 API는 참으로 유용하고 만족스럽다. 공개 API를 구현한다면 반드시 훌륭한 Javadocs 작성을 추천한다. 하지만 여느 주석과 마찬가지로 Javadocs 역시 독자를 오도하거나, 잘못 위치하거나, 그릇된 정보를 전달할 가능성이 존재한다.

## 나쁜 주석
대다수의 주석이 이 범주에 속한다. 일반적으로 대다수 주석은 허술한 코드를 지탱하거나, 엉성한 코드를 변명하거나, 미숙한 결정을 합리화하는 등 프로그래머가 주절거리는 독백에서 크게 벗어나지 못했다.

### 주절거리는 주석
특별한 이유 없이 달리는 주석이다.
```java
public void loadProperties() {
    try {
        String propertiesPath = propertiesLocation + "/" + PROPERTIES_FILE;
        FileInputStream propertiesStream = new FileInputStream(propertiesPath);
        loadedProperties.load(propertiesStream);
    } catch (IOException e) {
        // 속성 파일이 없다면 기본값을 모두 메모리로 읽어 들였다는 의미다.
    }
}
```

catch 블록에 있는 주석은 저자에겡야 의미가 있겠지만 그 의미가 다른 사람들에게는 전해지지 않는다. 저 주석의 의미를 알아내려면 다른 코드를 뒤져보는 수밖에 없다. 이해가 인되어 다른 모듈까지 뒤져야 하는 주석은 제대로 된 주석이 아니다.

### 같은 이야기를 중복하는 주석
코드 내용을 그대로 중복하는 주석이 있다. 자칫하면 코드보다 주석을 읽는 시간이 더 오래 걸린다.

```java
// this.closed가 true일 때 반환되는 유틸리티 메서드다.
// 타임아웃에 도달하면 예외를 던진다.
public synchronized void waitForClose(final long timeoutMillis) throws Exception {
    if (!closed) {
        wait(timeoutMillis);
        if (!closed)
            throw new Exception("MockResponseSender could not be closed");
    }
}
```

### 오해할 여지가 있는 주석
위 코드를 다시 보자. 중복이 많으면서도 오해할 여지가 살짝 있다. `this.closed`가 `true`로 변하는 순간에 메서드는 반환되지 않는다. `this.closed`가 `true`여야 메서드는 반환된다. 아니면 무조건 타임아웃을 기다렸다 `this.closed`가 그래도 `true`가 아니면 예외를 던진다. 주석에 담긴 '살짝 잘못된 정보'로 인해 어느 프로그래머가 경솔하게 함수를 호출해 자기 코드가 아주 느려진 이유를 못찾게 되는 것이다.

### 의무적으로 다는 주석
모든 함수에 Javadocs를 달거나 모든 변수에 주석을 달아야 한다는 규칙은 어리석기 그지없다. 이런 주석은 코드를 복잡하게 만들며, 거짓말을 퍼트리고, 혼동과 무질서를 초래한다. 아래와 같은 주석은 아무 가치도 없다.

```java
/**
 *
 * @param title CD 제목
 * @param author CD 저자
 * @param tracks CD 트랙 숫자
 * @param durationInMinutes CD 길이(단위: 분)
 */
public void addCD(String title, String author, int tracks, int durationInMinutes) {
    CD cd = new CD();
    cd.title = title;
    cd.author = author;
    cd.tracks = tracks;
    cd.duration = durationInMinutes;
    cdList.add(cd);
}
```

### 이력을 기록하는 주석
지금은 소스 코드 관리 시스템이 있으니 전혀 필요 없다.

```
* 변경 이력 (11-Oct-2001부터)
* ------------------------------------------------
* 11-Oct-2001 : 클래스를 다시 정리하고 새로운 패키징
* 05-Nov-2001: getDescription() 메소드 추가
* 이하 생략
```

### 있으나 마나 한 주석
```java
/*
 * 기본 생성자
 */
protected AnnualDateRule() {}
```

### 무서운 잡음
때로는 Javadocs도 잡음이다. 다음은 잘 알려진 오픈 소스 라이브러리에서 가져온 코드다.

```java
/** The name. */
private String name;

/** The version. */
private String version;

/** The licenceName. */
private String licenceName;

/** The version. */
private String info;
```

### 함수나 변수로 표현할 수 있다면 주석을 달지 마라
```java
// 전역 목록 <smodule>에 속하는 모듈이 우리가 속한 하위 시스템에 의존하는가?
if (smodule.getDependSubsystems().contains(subSysMod.getSubSystem()))
```

이 코드에서 주석을 없애고 다시 표현하면 다음과 같다.

```java
ArrayList moduleDependencies = smodule.getDependSubSystems();
String ourSubSystem = sybSysMod.getSubSystem();
if (moduleDependees.contains(ourSubSystem))
```

### 위치를 표시하는 주석
### 닫는 괄호에 다는 주석
### 공로를 돌리거나 저자를 표시하는 주석
### 주석으로 처리한 코드
### HTML 주석
### 전역 정보
### 너무 많은 정보
### 모호한 관계
### 함수 헤더
### 비공개 코드에서 Javadocs