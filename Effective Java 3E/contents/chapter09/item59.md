# 아이템 59. 라이브러리를 익히고 사용하라

## 라이브러리를 사용하여 얻는 이점들
- 표준 라이브러리를 사용하면 그 코드를 작성한 전문가의 지식과 우리보다 앞서 사용한 다른 프로그래머들의 경험을 활용할 수 있다.
- 핵심적인 일과 크게 관련 없는 문제를 해결하느라 시간을 허비하지 않아도 된다.
- 따로 노력하지 않아도 성능이 지속해서 개선된다.
- 기능이 점점 많아진다. 라이브러리에 부족한 부분이 있다면 개발자 커뮤니티에서 이야기가 나오고 논의된 후 다음 릴리스에 해당 기능이 추가되곤 한다.
- 우리가 작성한 코드가 많은 사람에게 낯익은 코드가 된다.

Java는 **메이저 릴리스마다 주목할 만한 수많은 기능이 라이브러리에 추가된다.** 메이저 릴리스마다 새로운 기능을 설명하는 웹페이지를 공시하는데, 한 번쯤 읽어보는 것을 추천한다.

## 꼭 익숙해져야 하는 라이브러리들
라이브러리가 너무 방대하여 모든 API 문서를 공부하기에는 벅차겠지만, **Java 프로그래머라면 적어도 `java.lang`, `java.util`, `java.io`와 그 하위 패키지들에는 익숙해져야 한다.** 다른 라이브러리들은 필요할 때마다 익히는 것이 좋다. 라이브러리는 매년 아주 빠르게 성장하고 있기에 모든 기능을 요약하는 것은 현실적으로 무리다.

**컬렉션 프레임워크와 스트림 라이브러리, `java.util.concurrent`의 동시성 기능은 잘 익혀두면 많은 도움이 된다.** 해당 패키지는 멀티스레드 프로그래밍 작업을 단순화해주는 고수준의 편의 기능은 물론, 능숙한 개발자가 자신만의 고수준 개념을 직접 구현할 수 있도록 도와주는 저수준 요소들을 제공한다.

Java 표준 라이브러리에서 원하는 기능을 찾지 못하면, 다음 선택지는 고품질의 서드파티 라이브러리가 될 것이다. 구글의 Guava 라이브러리가 대표적이다. 이마저도 찾지 못했다면, 다른 선택이 없으니 직접 구현하도록 하자.

## 핵심 정리
- 바퀴를 다시 발명하지 말자. 아주 특별한 나만의 기능이 아니라면 누군가 이미 라이브러리 형태로 구현해놓았을 가능성이 크다.
- 그런 라이브러리가 있다면, 쓰면 된다. 있는지 잘 모르겠다면 찾아보라.
- 일반적으로 라이브러리의 코드는 우리가 직접 작성한 것보다 품질이 좋고, 점차 개선될 가능성이 크다.
- 코드에도 규모의 경제가 적용된다.
- 즉, 라이브러리 코드는 개발자 각자가 작성하는 것보다 주목을 훨씬 많이 받으므로 코드 품질도 그만큼 높아진다.