# Chapter 9. 도메인 모델과 바운디드 컨텍스트
## 도메인 모델과 경계
한 도메인은 여러 하위 도메인으로 구분되기 때문에 한 개의 모델로 여러 하위 도메인을 모두 표현하려고 시도하면 오히려 모든 하위 도메인에 맞지 않는 모델을 만들게 된다.

- 카탈로그에서의 상품: 상품 이미지, 상품명, 상품 가격, 옵션 목록, 상세 설명과 같은 상품 정보가 위주
- 재고 관리에서의 상품: 실존하는 개별 객체를 추적하기 위한 목적으로 상품을 사용.

논리적으로 같은 존재처럼 보이지만 하위 도메인에 따라 다른 용어를 사용하는 경우도 있다.

- 카탈로그 도메인에서의 상품이 검색 도메인에선 문서로 불리기도 한다.
- 시스템을 사용하는 사람을 회원 도메인에선 회원이라 부르지만, 주문 도메인에선 주문자, 배송 도메인에선 보내는 사람이라 부르기도 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/679d9ea3-47da-42e0-b7fe-f2dd99e94b1a)

하위 도메인마다 사용하는 용어가 다르기 때문에 올바른 도메인 모델을 개발하려면 하위 도메인마다 모델을 만들어야 한다. 각 모델은 명시적으로 구분되는 경계를 가져서 섞이지 않도록 해야 한다. 여러 하위 도메인의 모델이 섞이기 시작하면 모델의 의미가 약해질 뿐만 아니라 여러 도메인의 모델이 서로 얽히기 때문에 각 하위 도메인별로 다르게 발전하는 요구사항을 모델에 반영하기 어려워진다.

**모델은 특정한 컨텍스트 하에서 완전한 의미를 갖는다.** 이렇게 구분되는 경계를 갖는 컨텍스트를 DDD에선 **바운디드 컨텍스트**라고 부른다.

## 바운디드 컨텍스트
- **바운디드 컨텍스트는 모델의 경계를 결정하며 한 개의 바운디드 컨텍스트는 논리적으로 한 개의 모델을 갖는다.**
- 바운디드 컨텍스트는 용어를 기준으로 구분한다.
- 바운디드 컨텍스트는 실제로 사용자에게 기능을 제공하는 물리적 시스템으로 도메인 모델은 이 바운디드 컨텍스트 안에서 도메인을 구현한다.
- 이상적으로 하위 도메인과 바운디드 컨텍스트가 일대일 관계를 가지면 좋지만 현실은 그렇지 않을 때가 많다.
- 기업의 팀 조직 구조에 따라 결정되기도 한다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/e896fcfa-4b6a-40bc-bd59-42e3d520a120)

규모가 작은 기업은 전체 시스템을 한 개 팀에서 구현할 때도 있다. 여러 하위 도메인을 한 개의 바운디드 컨텍스트에서 구현한다.

주의할 점은, **하위 도메인 모델이 섞이지 않도록 하는 것**이다. 한 프로젝트에 각 하위 도메인의 모델이 위치하면 아무래도 전체 하위 도메인을 위한 단일 모델을 만들고 싶은 유혹에 빠지기 쉽다. 이렇게 되면 결과적으로 도메인 모델이 개별 하위 도메인을 제대로 반영하지 못해 하위 도메인별로 기능을 확장하기 어렵게 되고 이는 서비스 경쟁력을 떨어뜨리는 원인이 된다.

비록 한 개의 바운디드 컨텍스트가 여러 하위 도메인을 포함하더라도 하위 도메인마다 구분되는 패키지를 갖도록 구현해야 하며, 이렇게 함으로써 하위 도메인을 위한 모델이 서로 뒤섞이지 않고 하위 도메인마다 바운디드 컨텍스트를 갖는 효과를 낼 수 있다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/a8835a06-1b2b-4feb-a6be-c7651af5e678)

**바운디드 컨텍스트는 도메인 모델을 구분하는 경계가 되기 때문에 바운디드 컨텍스트는 구현하는 하위 도메인에 알맞은 모델을 포함한다.**

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/fe0891df-a5af-4c1f-bc0e-62396f8413b0)

## 바운디드 컨텍스트 구현
바운디드 컨텍스트가 도메인 모델만 포함하는 것은 아니다. 도메인 기능을 사용자에게 제공하는 데 필요한 표현 영역, 응용 서비스, 인프라스트럭처 영역을 모두 포함한다. 도메인 모델의 데이터 구조가 바뀌면 DB 테이블 스키마도 함께 변경해야 하므로 테이블도 바운디드 컨텍스트에 포함된다.

표현 영역은 HTML 페이지를 생성할 수도 있고, 다른 바운디드 컨텍스트를 위해 REST API를 제공할 수도 있다.

**모든 바운디드 컨텍스트를 반드시 도메인 주도로 개발할 필요는 없다.** 상품의 리뷰는 복잡한 도메인 로직을 갖지 않기 때문에 CRUD 방식으로 구현해도 된다. 즉 DAO와 데이터 중심의 밸류 객체를 이용해서 리뷰 기능을 구현해도 기능을 유지보수하는 데 큰 문제가 없다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/884eb974-38a6-4cd5-89b6-48bda9c92a83)

서비스-DAO 구조를 사용하면 도메인 기능이 서비스에 흩어지게 되지만 도메인 기능 자체가 단순하면 서비스-DAO로 구성된 CRUD 방식을 사용해도 되므로 코드를 유지 보수하는 데 문제가 되지 않는다.

한 바운디드 컨텍스트에서 두 방식을 혼합해서 사용할 수도 있다. 대표적인 예가 CQRS 패턴이다.

각 바운디드 컨텍스트는 서로 다른 구현 기술을 사용할 수도 있다.

- 웹 MVC는 스프링 MVC를 사용하고 리포지터리 구현 기술은 JPA/하이버네이트를 사용하는 바운디드 컨텍스트가 존재할 수도 있다.
- Netty를 사용해서 REST API를 제공하고 마이바티스를 리포지터리 구현 기술로 사용하는 바운디드 컨텍스트가 존재할 수도 있다.
- RDBMS 대신 MongoDB와 같은 NoSQL을 사용할 수도 있다.

바운디드 컨텍스트가 반드시 사용자에게 보여지는 UI를 가지고 있어야 하는 것은 아니다.

## 바운디드 컨텍스트 간 통합
온라인 쇼핑 사이트에서 매출 증대를 위해 카탈로그 하위 도메인에 개인화 추천 기능을 도입한다면, 카탈로그 하위 도메인에는 기존 카탈로그를 위한 바운디드 컨텍스트와 추천 기능을 위한 바운디드 컨텍스트가 생긴다. 두 팀이 관련된 바운디드 컨텍스트를 개발하면 자연스럽게 두 바운디드 컨텍스트 간 통합이 발생한다.

카탈로그 시스템은 추천 시스템으로부터 추천 데이터를 받아오지만, 카탈로그 시스템에서는 추천의 도메인 모델을 사용하기보다는 카탈로그 도메인 모델을 사용해서 추천 상품을 표현해야 한다. 즉 다음과 같이 카탈로그의 모델을 기반으로 하는 도메인 서비스를 이용해서 상품 추천 기능을 표현해야 한다.

```java
/*
 * 상품 추천 기능을 표현하는 도메인 서비스
 */
public interface ProductRecommendationService {
  List<Product> getRecommendationsOf(ProductId id);
}
```

도메인 서비스를 구현한 클래스는 인프라스트럭처 영역에 위치한다. 이 클래스는 외부 시스템과의 연동을 처리하고 외부 시스템 모델과 현재 도메인 모델 간의 변환을 책임진다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/041d2214-a5aa-4b1f-9c76-1e3f8a69aba1)

`RecSystemClient`는 REST API로부터 데이터를 읽어와 카탈로그 도메인에 맞는 상품 모델로 변환한다.

```java
public class RecSystemClient implements ProductRecommendationService {
  private ProductRepository productRepository;

  @Override
  public List<Product> getRecommendationsOf(ProductId id) {
    List<RecommendationItem> items = getRecItem(id.getValue());
    return toProducts(items);
  }

  private List<RecommendationItem> getRecItem(String itemId) {
    // externalRecClient는 외부 추천 시스템을 위한 클라이언트라고 가정
    return externalRecClient.getRecs(itemId);
  }

  private List<Product> toProducts(List<RecommendationItem> items) {
    return items.stream()
            .map(item -> toProductId(item.getItemId()))
            .map(prodId -> productRepository.findById(prodId))
            .collect(toList());
  }

  private ProductId toProductId(String itemId) {
    return new ProductId(itemId);
  }
  // ...
}
```

두 모델 간의 변환 과정이 복잡하면 변환 처리르 위한 별도 클래스를 만들고 이 클래스에서 변환을 처리해도 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b9f0dae4-9c66-439d-9b6a-2915b5b94945)

REST API를 호출하는 것은 두 바운디드 컨텍스트를 직접 통합하는 방법이다. 간접적으로 통합하는 방법도 있고 대표적인 방식이 메시지 큐를 사용하는 것이다. 추천 시스템은 사용자가 조회한 상품 이력이나 구매 이력과 같은 사용자 활동 이력을 필요로 하는데 이 내역을 전달할 때 메시지 큐를 사용할 수 있다.

![](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3b066fbf-5516-4f59-97c7-67941c622c1a)

- 카탈로그 바운디드 컨텍스트는 추천 시스템이 필요로 하는 사용자 활동 이력을 메시지 큐에 추가한다.
- 메시지 큐는 비동기로 메시지를 처리하기 때문에 카탈로그 바운디드 컨텍스트는 메시지를 큐에 추가한 뒤에 추천 바운디드 컨텍스트가 메시지를 처리할 때까지 기다리지 않고 바로 이어서 자신의 처리를 계속한다.

어떤 도메인 관점에서 모델을 사용하느냐에 따라 두 바운디드 컨텍스트의 구현 코드가 달라지게 된다. 카탈라고 도메인 관점에서 큐에 저장할 메시지를 생성하면 카탈로그 시스템의 연동 코드는 카탈로그 기준의 데이터를 그대로 메시지 큐에 저장한다.

```java
// 상품 조회 관련 로그 기록 코드
public class ViewLogService {
  private MessageClient messageClient;

  public void appendViewLog(String memberId, String productId, Date time) {
    messageClient.send(new ViewLog(memberId, productId, time));
  }
  // ...
}

// messageClient
public class RabbitMQClient implements MessageClient {
  private RabbitTemplate rabbitTemplate;

  @Override
  public void send(ViewLog viewLog) {
    // 카탈로그 기준으로 작성한 데이터를 큐에 그대로 보관
    rabbitTemplate.convertAndSend(logQueueName, viewLog);
  }
  // ...
}
```

두 바운디드 컨텍스트를 개발하는 팀은 메시징 큐에 담을 데이터의 구조를 협의하게 되는데 그 큐를 누가 제공하느냐에 따라 데이터 구조가 결정된다. 한쪽에서 메시지를 출판하고 다른 쪽에서 메시지를 구독하는 출판/구독 모델을 따른다.

## 바운디드 컨텍스트 간 관계
바운디드 컨텍스트는 어떤 식으로든 연결되기 때문에 두 바운디드 컨텍스트는 다양한 방식으로 관계를 맺는다. 바운디드 컨텍스트 간 가장 흔한 관계는 한쪽에서 API를 제공하고 다른 한쪽에서 그 API를 호출하는 관계이다. REST API가 대표적이다.

- 이 관계에서 API를 사용하는 바운디드 컨텍스트는 API를 제공하는 바운디드 컨텍스트에 의존하게 됨

상류 컴포넌트는 일종의 서비스 공급자 역할을 한다.
- 보통 하류 컴포넌트가 사용할 수 있는 통신 프로토콜을 정의하고 이를 공개한다.
- 하류 컴포넌트가 다수 존재할 때 여러 하류 컴포넌트의 요구사항을 수용할 수 있는 API를 만들고, 이를 서비스 형태로 하류 컴포넌트에게 제공을 하며 서비스의 일관성을 유지한다. (ex. **공개 호스트 서비스** - ex. 검색)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/47dfecb8-354c-491c-b003-db7d73ecc3db)

상류 컴포넌트의 서비스는 상류 바운디드 컨텍스트의 도메인 모델을 따른다. 따라서 하류 컴포넌트는 상류 서비스의 모델이 자신의 도메인 모델에 영향을 주지 않도록 보호해 주는 완충 지대를 만들어야 한다. (안티코럽션 계층)

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/09791b0d-26a0-4143-8fe4-f2bf9bb23492)

공유 커널은 두 바운디드 컨텍스트가 공유하는 모델이다.
- 모델을 두 번 개발하는 중복을 줄일 수 있지만 한 팀에서 임의로 모델 변경을 할 수 없다.

독립 방식은 서로 통합하지 않고 독립적인 모델로 발전시킨 방식이다.
- 수동(사람이 직접)으로 두 바운디드 컨텍스트를 통합
  - ex. ERP 솔루션
- 규모가 커질수록 수동 통합에는 한계가 있으므로 규모가 커지기 시작하면 두 바운디드 컨텍스트를 통합해야 한다.
- 외부에서 구매한 솔루션과 ERP를 완전히 대체할 수 없다면 두 바운디드 컨텍스트를 통합해주는 별도의 시스템을 만들어야 할 수도 있다.

## 컨텍스트 맵
개별 바운디드 컨텍스트에 매몰되면 전체를 보지 못할 때가 있다. 나무만 보고 숲을 보지 못하는 상황을 방지하려면 전체 비즈니스를 조망할 수 있는 지도가 필요한데 그것이 바로 컨텍스트 맵이다. 컨텍스트 맵은 바운디드 컨텍스트 간의 관계를 표시한 것이다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/4e57cad0-0dbe-4580-88a8-a6ba57096e14)

그림만 봐도 한 눈에 각 바운디드 컨텍스트의 경계가 명확하게 드러나고 서로 어떤 관계를 맺고 있는지 알 수 있다. 바운디드 컨텍스트 영역에 주요 애그리거트를 함께 표시하면 모델에 대한 관계가 더 명확히 드러난다. OHS는 공개 호스트 서비스, ACL은 안티코럽션 계층이다.

컨텍스트 맵은 시스템의 전체 구조를 보여준다. 이는 하위 도메인과 일치하지 않는 바운디드 컨텍스트를 찾아 도메인에 맞게 바운디드 컨텍스트를 조절하고 사업의 핵심 도메인을 위해 조직 역량을 어떤 바운디드 컨텍스트에 집중할지 파악하는 데 도움을 준다.

컨텍스트 맵을 그리는 규칙은 따로 없다. 간단한 도형과 선을 이용해서 각 컨텍스트의 관계를 이해할 수 있는 수준에서 그리면 된다.