# 1장. 깨끗한 코드

## 코드가 존재하리라
코드에 관한 책은 구시대적이고, 곧 명세를 기준으로 코드가 자동생성될 것이라는 생각은 틀렸다.
- 언어의 추상화 레벨(고급/저급 언어의 관점에서)이 올라가고 domain specific 한 언어는 증가할지라도 코드는 사라지지 않는다.
- 기계가 실행할 정도로 자세한 명세는 그 자체로 코드이기 때문이다.

## 나쁜 코드
- Killer app 하나로 대박난 회사가 머지 않아 망한 일이 있었다. 그 원인은 나쁜 코드였다.
- 일정에 맞추기 위해 나쁜 코드들을 방치하고는 '나중에 고쳐야지' 라고 생각한 경험이 다들 있을 것이다. 하지만, 나중은 절대 오지 않는다.

## 나쁜 코드로 치르는 대가
프로젝트 초반에는 번개처럼 나가다가 1-2년만에 굼뱅이처럼 기어가는 팀도 많다. 나쁜 코드로 짠 프로그램에 가해지는 변경사항은 어느것 하나 사소하지 않다.

나쁜 코드가 쌓일수록 팀 생산성은 떨어진다. 그러다 마침내 0에 근접한다.
- 관리팀은 인력을 추가하려 한다.
- 하지만 새 팀원은 구조를 이해하지 못한다.
- 거기다 그 팀은 '새 인력을 투입했으므로 생산성이 늘겠지'라는 압박을 받는다.
- 결과적으로 나쁜 코드는 더 쌓인다.

### 원대한 재설계의 꿈
- 마침내 팀이 반기를 든다. 재설계를 요구한다.
- 관리층은 달갑진 않지만 생산성이 바닥이라는 사실을 부인할 도리가 없어 원대한 재설계를 허가한다.
- 새 tiger team이 구성된다. 기존의 프로덕트의 스펙 + 새로운 기능을 맡게 된다. 기존의 팀원들은 기존의 코드를 유지보수하게 된다.
- 두 팀은 오랫동안 경쟁한다.
- tiger team이 기존의 프로젝트를 거의 따라잡을 즈음, tiger team의 초기 멤버들은 대부분 새 멤버들로 교체되어 있다.
- 그리고 그들은 다시 재설계를 요구한다.

### 태도
몇 시간으로 예상한 업무가 몇 주로 늘어진 경험이나, 한 줄만 고치면 되리라 예상했다가 모듈을 수백 개 건드린 경험은 흔하다. 왜 좋은 코드는 그렇게도 빠르게 나쁜 코드로 바뀌는 것일까?

초기와 다른 스펙, 스케줄, 멍청한 매니저, 참을성 없는 고객, 쓸데없는 마케팅 인간들을 비난할지도 모른다. 하지만 그건 우리 잘못이다.
- 대부분은 매니저들은 우리 생각보다 더 진실을 원하고 있다.
- 그들 또한 좋은 코드를 원한다. 그와 동시에 스케줄 또한 지키고 싶어한다.
- 그와 마찬가지로, 좋은 코드를 지키는 것 또한 우리의 몫이다.

### 원초적 난제
- 더러운 코드는 생산성을 저하시킨다. 그와 동시에 개발자들은 기한을 맞추기 위해 더러운 코드를 짠다. 하지만, 더러운 코드를 만들어서는 절대 기한을 맞추지 못한다.
- 빨리 가기 위한 단 하나의 방법은 **최대한 깨끗한 코드를 항상 유지하는 것이다.**

### Clean Code라는 예술?
클린 코드를 구현하는 행위는 그림을 그리는 행위와 비슷하다. 그림을 보면 대부분의 사람은 잘 그려졌는지 엉망으로 그려졌는지 안다. 하지만 잘 그린 그림을 구분하는 능력이 그림을 잘 그리는 능력은 아니다. 다시 말해, 깨끗한 코드와 나쁜 코드를 구분할 줄 안다고 깨끗한 코드를 작성할 줄 안다는 뜻은 아니다.

클린 코드를 작성하려면 '청결'이라는 힘겹게 습득한 감각을 활용해 자잘한 기법들을 적용하는 절제와 규율이 필요하다.

### Clean Code란?
<details>
  <summary>
    <b>Bjarne Stroustrup, inventor of C++ and author of The C++ Programming Language</b>
  </summary>
  > 나는 우아하고 효율적인 코드를 좋아한다. 논리가 간단해야 버그가 숨어들지 못한다. 의존성을 최대한 줄여야 유지보수가 쉬워진다. 오류는 명백한 전략에 의거해 철저히 처리한다. 성능을 최적으로 유지해야 사람들이 원칙 없는 최적화로 코드를 망치려는 유혹에 빠지지 않는다. 깨끗한 코드는 한 가지를 제대로 한다.
</details>

- 코드는 즐겁게 읽혀야 한다.
- 효율적인 코드어야 한다. 이는 성능적 측면 뿐만 아니라 나쁜 코드는 난장판을 더 키우기 때문이다.(깨진 유리창 이론)
- 에러 핸들링, 메모리 누수, 경쟁상태, 일관되지 않은 명명법 등 디테일에 신경써라.
- 나쁜 코드는 여러가지 일을 하려고 한다. 나쁜 코드는 애매한 의도와 모호한 목적을 포함한다. 클린 코드는 한 가지에 집중한다. **클린 코드는 한 가지 일을 잘 한다.**

<details>
  <summary>
    <b>Grady Booch, author of Object Oriented Analysis and Design with Applications</b>
  </summary>
  > 깨끗한 코드는 단순하고 직접적이다. 깨끗한 코드는 잘 쓴 문장처럼 읽힌다. 깨끗한 코드는 결코 설계자의 의도를 숨기지 않는다. 오히려 명쾌한 추상화와 단순한 제어문으로 가득하다.
</details>

- 클린코드는 하나의 잘 쓰여진 산문처럼 읽혀야 한다. 소설의 기승전결처럼 문제를 제시하고 명쾌한 해답을 제시해야 한다.
- 명백한 추상: 코드는 추측 대신 실제를 중시, 필요한 것만 포함하며 독자로 하여금 결단을 내렸다고 생각하게 해야 한다.

<details>
  <summary>
    <b>"Big" Dave Thomas, founder of OTI, godfather of the Eclipse strategy</b>
  </summary>
  > 깨끗한 코드는 작성자가 아닌 사람도 읽기 쉽고 고치기 쉽다. 단위 테스트 케이스와 인수 테스트 케이스가 존재한다. 깨끗한 코드에는 의미 있는 이름이 붙는다. 특정 목적을 달성하는 방법은 (여러 가지가 아니라) 하나만 제공한다. 의존성은 최소이며 각 의존성을 명확히 정의한다. API는 명확하며 최소로 줄였다. 언어에 따라 필요한 모든 정보를 코드만으로 명확히 표현할 수 없기에 코드는 문학적으로 표현해야 마땅하다.
</details>

- 다른 이가 수정하기 쉬워야 한다.
- 테스트를 해야 한다.
- 코드는 간결할 수록 좋다.
- 코드는 세련되어야 한다.

<details>
  <summary>
    <b>Michael Feathers, author of Working Effectively with Legacy Code</b>
  </summary>
  > 깨끗한 코드의 특징은 많지만 그 중에서도 모두를 아우르는 특징이 하나 있다. 깨끗한 코드는 언제나 누군가 주의 깊게 짰다는 느낌을 준다. 고치려고 살펴봐도 딱히 손 댈 곳이 없다. 작성자가 이미 모든 사항을 고려했으므로. 고칠 궁리를 하다보면 언제나 제자리로 돌아온다. 그리고 누군가 남겨준 코드, 누군가 주의 깊게 짜놓은 작품에 감사를 느낀다.
</details>

- 코드를 care하라. (주의, 관심을 가지고 작성하라)

<details>
  <summary>
    <b> Ron Jeffries, author of Extreme Programming Installed and Extreme Programming Adventures in C# </b>
  </summary>

> *In recent years I begin, and nearly end, with Beck’s rules of simple code. In priority order, simple code:*  
>   *- Runs all the tests;*  
>   *- Contains no duplication;*  
>   *- Expresses all the design ideas that are in the system;*  
>   *- Minimizes the number of entities such as classes, methods, functions, and the like.*  
> &nbsp;&nbsp;&nbsp;&nbsp;*Of these, I focus mostly on duplication. When the same thing is done over and over, it’s a sign that there is an idea in our mind that is not well represented in the code. I try to figure out what it is. Then I try to express that idea more clearly. Expressiveness to me includes meaningful names, and I am likely to change the names of things several times before I settle in. With modern coding tools such as Eclipse, renaming is quite inexpensive, so it doesn’t trouble me to change.*  
> &nbsp;&nbsp;&nbsp;&nbsp;*Expressiveness goes beyond names, however. I also look at whether an object or method is doing more than one thing. If it’s an object, it probably needs to be broken into two or more objects. If it’s a method, I will always use the Extract Method refactoring on it, resulting in one method that says more clearly what it does, and some submethods saying how it is done.*  
> &nbsp;&nbsp;&nbsp;&nbsp;*Duplication and expressiveness take me a very long way into what I consider clean code, and improving dirty code with just these two things in mind can make a huge difference. There is, however, one other thing that I’m aware of doing, which is a bit harder to explain.*  
> &nbsp;&nbsp;&nbsp;&nbsp;*After years of doing this work, it seems to me that all programs are made up of very similar elements. One example is “find things in a collection.” Whether we have a database of employee records, or a hash map of keys and values, or an array of items of some kind, we often find ourselves wanting a particular item from that collection. When I find that happening, I will often wrap the particular implementation in a more abstract method or class. That gives me a couple of interesting advantages.*  
> &nbsp;&nbsp;&nbsp;&nbsp;*I can implement the functionality now with something simple, say a hash map, but since now all the references to that search are covered by my little abstraction, I can change the implementation any time I want. I can go forward quickly while preserving my ability to change later.*  
> &nbsp;&nbsp;&nbsp;&nbsp;*In addition, the collection abstraction often calls my attention to what’s “really” going on, and keeps me from running down the path of implementing arbitrary collection behavior when all I really need is a few fairly simple ways of finding what I want.*  
> &nbsp;&nbsp;&nbsp;&nbsp;*Reduced duplication, high expressiveness, and early building of simple abstractions. That’s what makes clean code for me.*
</details>

- 중복을 없애라.
- 클래스/메서드는 한 가지 일만 하게 하라.
- 메서드의 이름 등으로 코드가 하는 일을 명시하라.
- (메서드 등을) 일찍 추상화해서 프로젝트를 빠르게 진행할 수 있게 하라.

<details>
  <summary>
    <b>Ward Cunningham, inventor of Wiki, inventor of Fit, coinventor of eXtreme Programming. Motive force behind Design Patterns. Smalltalk and OO thought leader. The godfather of all those who care about code.</b>
  </summary>
  > 코드를 읽으면서 짐작했던 기능을 각 루틴이 그대로 수행한다면 깨끗한 코드라 불러도 되겠다. 코드가 그 문제를 풀기 위한 언어처럼 보인다면 아름다운 코드라 불러도 되겠다.
</details>

- 읽고, 끄덕이고, 다음으로 넘어갈 수 있는 코드를 작성하라.
- 당신이 사용하는 언어를 탓하지 말라. 코드를 아름답게 만드는 것은 프로그래머이다.

## 우리들 생각
필자는 본 책의 내용(의견)을 절대적인 것으로써 전달할 것이다. 우리에겐, 적어도 우리 커리어의 현시점에서는, 그게 절대적이기 때문이다. **이것이 클린코드에 대한 우리의 학파이다.**

무술가들에게 최고 무술이나 한 무술 내에서 최고 기술을 꼽으라면 다들 다르게 답하리라. 흔히 무술 대가는 독자적인 문파를 만들고 제자를 모아 자신의 사상을 가르친다. 각 문파에 입문한 학생들은 창시자의 가르침을 익히고 연마한다. 그들은 까다로운 스승의 가르침을 배우기 위해 전념한다. 다른 대가의 가르침을 배제하기도 한다. 나중에 실력이 충분히 늘었다면 다른 대가를 찾아가 지식과 기술을 넓히기도 한다. 일부는 자신의 기술을 계속 연마하다 새로운 기술을 발견해 새로운 문파를 세운다.

이 책은 우리 **오브젝트 멘토 진영이 생각하는 Clean Code**를 설명한다. 여기서 가르치는 교훈과 기법은 우리 진영이 믿고 실천하는 교리다. 우리가 가르치는 교훈을 따른다면 우리가 만끽한 이점을 즐길 수 있을 것이며, 깨끗하고 프로페셔널한 코드 작성법을 배울 것이다. 하지만 우리가 절대적으로 '옳다'라고 여기진 말라. 우리들 못지않게 경험 많은 집단과 전문가가 존재한다. 마땅히 그들에게서도 배우라고 권한다.

이 책에서 주장하는 기법 다수는 논쟁의 여지가 있다. 모든 내용에 대해 동의하지 않을 거니와 어떤 내용은 심하게 부정할지도 모른다. 그래도 좋다. 결정은 당신의 몫이다. 하지만, 이 책에서 추천하는 내용은 우리가 긴 시간 힘들게 고민한 내용이다. 이 내용은 우리가 수십년간의 경험과 시행착오의 반복으로 얻은 것이다. 당신이 동의하던 아니던 당신이 우리의 관점을 이해하고 존중해주길 바란다.

## 우리는 저자다
JavaDoc에서 `@author` 필드는 우리가 누구인지 이야기해준다. 우리는 저자다. 저자에게는 독자가 있다. 그리고 저자에게는 독자와 잘 소통할 책임도 있다. 다음에 코드를 짤 때는 자신이 저자라는 사실을, 여러분의 노력을 보고 판단을 내릴 독자가 있다는 사실을 기억하기 바란다.

혹자는 실제로 코드를 읽는 과정에 얼마나 많은 노력이 필요하겠냐고 생각할지도 모른다. 하지만 실상은 그 반대이다.
```
Bob이 모듈을 입력한다.
변경할 함수로 스크롤해 내려간다.
잠시 멈추고 생각한다.
이런! 모듈 상단으로 스크롤해 변수 초기화를 확인한다.
다시 내려와 입력하기 시작한다.
이런, 입력을 지운다!
다시 입력한다.
다시 지운다!
뭔가를 절반쯤 입력하다가 또 지운다!
지금 바꾸려는 함수를 호출하는 함수로 스크롤한 후 함수가 호출되는 방식을 살펴본다.
다시 돌아와 방금 지운 코드를 입력한다.
잠시 멈춘다.
코드를 다시 지운다!
다른 윈도를 열어 하위 클래스를 살핀다. 함수가 재정의되었는가?
...
```

실제로 읽기와 쓰기에 들이는 시간은 대략 10:1 정도이다. 새 코드를 작성하기 위해서는 옛 코드들을 읽어야 하기 때문이다. 그러므로, **빨리 가고 싶다면, 쉽게 코드를 작성하고 싶다면, "읽기 쉽게 작성하라".**

## 보이스카우트 규칙
잘 짠 코드가 전부는 아니다. 시간이 지나도 언제나 깨끗하게 유지해야 한다. 시간이 지나면서 엉망으로 전락하는 코드가 한둘이 아니다. 그러므로 우리는 적극적으로 코드의 퇴보를 막아야 한다.

> 캠프장은 처음 왔을 때보다 더 깨끗하게 해놓고 떠나라.

체크아웃할 때보다 좀 더 깨끗한 코드를 체크인한다면 코드는 절대 나빠지지 않는다. 한꺼번에 많은 시간과 노력을 투자해 코드를 정리할 필요가 없다. 변수 이름 하나를 개선하고, 조금 긴 함수 하나를 분할하고, 약간의 중복을 제거하고, 복잡한 if 문 하나를 정리하면 충분하다.

지속적인 개선이야말로 전문가 정신의 본질이다.

## 프리퀄과 원칙
여러모로 봐서 이 책은 2002년에 출판한 Agile Software Development: Principles, Patterns, and Practices(PPP)의 프리퀄이다. SRP, OCP, DIP 등 PPP에서 설명하는 객체지향 디자인의 원칙과 실제에 대한 설명이 종종 나오기 때문에 같이 읽어보면 좋을 것이다.

## 결론
예술에 대한 책을 읽는다고 예술가가 된다는 보장은 없다. 책은 단지 다른 예술가가 사용하는 도구와 기법, 그리고 생각하는 방식을 소개할 뿐이다. 이 책 역시 마찬가지다. 이 책을 읽는다고 뛰어난 프로그래머가 된다는 보장은 없다. '코드 감각'을 확실히 얻는다는 보장도 없다. 단지 뛰어난 프로그래머가 생각하는 방식과 그들이 사용하는 기술과 기교와 도구를 소개할 뿐이다.

이 책은 당신에게 많은 좋은 코드와 나쁜 코드를 소개한다. 나쁜 코드를 좋은 코드로 바꾸는 방법도 소개한다. 다양한 경험적 교훈과 체계와 절차와 기법도 열거한다. 나머지는 여러분의 몫이다.

"연습해 젊은이. 연습!"