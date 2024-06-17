# Chapter 6. 응용 서비스와 표현 영역
## 표현 영역과 응용 영역
도메인이 제 기능을 하려면 사용자와 도메인을 연결해주는 매개체가 필요하다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/5e444eab-3b05-400c-9db6-738a8b161be7)

- 표현 영역은 사용자의 요청을 해석한다.
  - 사용자가 웹 브라우저에서 폼에 ID와 암호를 입력한 뒤 전송 버튼을 클릭하면 요청 파라미터를 포함한 HTTP 요청을 표현 영역에 전달.
  - 요청을 받은 표현 영역은 URL, 요청 파라미터, 쿠키, 헤더 등을 이용해 사용자가 실행하고 싶은 기능을 판별하고 그 기능을 제공하는 응용 서비스를 실행.
- 실제 사용자가 원하는 기능을 제공하는 것은 응용 영역에 위치한 서비스다.
  - 기능을 실행하는 데 필요한 입력 값을 메서드 인자로 받고 실행 결과를 리턴한다.
- 응용 서비스의 메서드가 요구하는 파라미터와 표현 영역이 사용자로부터 전달받은 데이터는 형식이 일치하지 않기 때문에 표현 영역은 응용 서비스가 요구하는 형식으로 사용자 요청을 변환한다.

```java
@PostMapping("/member/join")
public ModelAndView join(HttpServletRequest request) {
  String email = request.getParameter("email");
  String password = request.getParameter("password");
  // 사용자 요청을 응용 서비스에 맞게 변환
  JoinRequest joinReq = new JoinRequest(email, password);
  // 변환한 객체(데이터)를 이용해 응용 서비스 실행
  joinService.join(joinReq);
  // ...
}
```

응용 서비스르 실행한 뒤 표현 영역은 실행 결과를 사용자에게 알맞은 형식으로 응답한다. (ex. HTML, JSON)

사용자와 상호작용은 표현 영역이 처리하기 때문에, 응용 서비스는 표현 영역에 의존하지 않는다. 응용 영역은 사용자가 웹 브라우저를 사용하는지 REST API를 호출하는지, TCP 소켓을 사용하는지를 알 필요가 없다. 단지 기능 실행에 필요한 입력 값을 받고 실행 결과만 리턴하면 될 뿐이다.

## 응용 서비스의 역할
응용 서비스는 클라이언트가 요청한 기능을 실행한다. 응용 서비스는 사용자의 요청을 처리하기 위해 리포지터리에서 도메인 객체를 가져와 사용한다.

응용 서비스는 주로 도메인 객체 간의 흐름을 제어하기 때문에 다음과 같이 단순한 형태를 갖는다.

```java
public Result doSomeFunc(SomeReq req) {
// 1. 리포지터리에서 애그리거트를 구한다.
  SomeAgg agg = someAggRepository.findById(req.getId());
  
  // 2. 애그리거트의 도메인 기능을 실행한다.
  agg.doFunc(req.getValue());
  
  // 3. 결과를 리턴한다.
  return createSuccessResult(agg);
}
```
응용 서비스가 복잡하다면 응용 서비스에서 도메인 로직의 일부를 구현하고 있을 가능성이 높다. 응용 서비스가 도메인 로직을 일부 구현하면 코드 중복, 로직 분산 등 코드 품질에 안 좋은 영향을 줄 수 있다.

응용 서비스는 트랜잭션 처리도 담당하는데, **데이터 일관성을 유지하려면 트랜잭션 범위에서 응용 서비스를 실행해야** 한다. 트랜잭션 외에 응용 서비스의 주요 역할로 접근 제어와 이벤트 처리도 있다.

### 도메인 로직 넣지 않기
도메인 로직은 도메인 영역에 위치하고 응용 서비스는 도메인 로직을 구현하지 않는다.

```java
public class ChangePasswordService {

  public void changePassword(String memberId, String oldPw, String newPw) {
    Member member = memberRepository.findById(memberId);
    checkMemberExists(member);
    member.changePassword(oldPw, newPw);
  }
  // ...
}

public class Member {

  public void changePassword(String oldPw, String newPw) {
    if (!matchPassword(oldPw)) throw new BadPasswordException();
    setPassword();
  }

  // 현재 암호화 일치하는지 검사하는 도메인 로직
  public boolean matchPassword(String pwd) {
    return passwordEncoder.matches(pwd);
  }

  private void setPassword(String newPw) {
    if (isEmpty(newPw)) throw new IllegalArgumentException("no new password");
    this.password = newPw;
  }
}
```

기존 암호를 올바르게 입력했는지 확인하는 것은 도메인의 핵심 로직이기 때문에 다음 코드 처럼 응용 서비스에서 이 로직을 구현하면 안 된다.

```java
public class ChangePasswordService {
  public void changePassword(String memberId, String oldPw, String newPw) {
    Member member = memberRepository.findById(memberId);
    checkMemberExists(member);

    if (!passwordEncoder.matches(oldPw, member.getPassword())) {
      throw new BadPasswordException();
    }
    member.setPassword(newPw);
  }
  // ...
}
```

**도메인 로직을 도메인 영역과 응용 서비스에서 분산해서 구현하면 코드 품질에 문제가 발생한다.**

- 코드의 응집성이 떨어진다.
  - 도메인 데이터와 그 데이터를 조작하는 도메인 로직이 한 영역에 위치하지 않고 서로 다른 영역에 위치한다는 것은 도메인 로직을 파악하기 위해 여러 영역을 분석해야 한다는 것을 의미한다.
- 여러 응용 서비스에서 동일한 도메인 로직을 구현할 가능성이 높아진다.

소프트웨어의 가치를 높이려면 도메인 로직을 도메인 영역에 모아 코드 중복을 줄이고 응집도를 높여야 한다.

## 응용 서비스의 구현
응용 서비스는 표현 영역과 도메인 영역을 연결하는 매개체 역할을 하는데 이는 디자인 패턴에서 퍼사드(facade)와 같은 역할을 한다.

### 응용 서비스의 크기
- 한 응용 서비스 클래스에 회원 도메인의 모든 기능 구현하기
  - 중복 로직이 있을경우 private method를 사용하여 중복 로직을 제거할 수 있는 장점이 있다.
  - 코드 크기가 커진다는 것은 연관성이 적은 코드가 한 클래스에 함께 위치할 가능성이 높아짐을 의미하는데, 이는 결과적으로 관련 없는 코드가 뒤섞여서 코드를 이해하는 데 방해가 될 수 있다.
- 구분되는 기능별로 응용 서비스 클래스를 따로 구현하기
  - 클래스 내에서 한 개 내지 2~3개의 기능을 구현한다.
  - 클래스 개수는 많아지지만 이전과 비교해서 코드 품질을 일정 수준으로 유지하는 데 도움이 된다.
  - 클래스의 기능이 분산되어 중복해서 동일한 코드를 구현할 가능성이 있다.
    - 이 경우 별도 클래스에 로직을 구현해서 코드가 중복되는 것을 방지할 수 있다.

```java
// 각 응용 서비스에서 공통되는 로직을 별도 클래스로 구현
public final class MemberServiceHelper {
  public static Member findExistingMember(MemberRepository repo, String memberId) {
    Member member = repo.findById(memberId);
    if (member == null) {
      throw new NoMemberException(memberId);
    }
    return member;
  }
}

// 공통 로직을 제공하는 메서드를 응용 서비스에서 사용
import static com.myshop.member.application.MemberServiceHelper.*;

public class ChangeMemberService {
  private MemberRepository memberRepository;

  public void changePassword(String memberId, String curPw, String newPw) {
    Member member = findExistingMember(memberRepository, memberId);
    member.changePassword(curPw, newPw);
  }

  // ...
}
```

### 응용 서비스의 인터페이스와 클래스
응용 서비스를 구현할 때 논쟁이 될 만한 것이 인터페이스가 필요한지 여부이다. 인터페이스가 필요한 상황은 어느 경우 일까?

- 구현 클래스가 다수 존재하거나 런타임에 구현 객체를 교체해야 할 경우이다.
- 표현 영역에서 단위 테스트를 위해 응용 서비스 클래스의 가짜 객체가 필요할 경우 (Mockito를 사용할 경우엔 필요없음)

보통 런타임에 이를 교체하는 경우가 거의 없을 뿐만 아니라 한 응용 서비스의 구현 클래스가 두 개인 경우도 매우 드물다. 이런 이유로 인터페이스와 클래스를 따로 구현하면 소스 파일만 많아지고 전체 구조만 복잡해지는 문제가 발생한다. **따라서, 인터페이스가 명확하게 필요하기 전까지는 응용 서비스에 대한 인터페이스를 작성하는 것이 좋은 설계라고는 볼 수 없다.**

### 표현 영역에 의존하지 않기
응용 서비스의 파라미터 타입을 결정할 때 주의할 점은 표현 영역과 관련된 타입을 사용하면 안 된다는 점이다.

- 표현 영역에 해당하는 `HttpServletRequest`, `HttpSession`을 응용 서비스에 파라미터로 전달하면 안 된다.

응용 서비스에서 표현 영역에 대한 의존이 발생하면 응용 서비스만 단독으로 테스트하기 어려워진다. 게다가 표현 영역의 구현이 변경되면 응용 서비스의 구현도 함께 변경해야 하는 문제도 발생한다.

더 심각한 것은 **응용 서비스가 표현 영역의 역할까지 대신하는 상황이 벌어질 수도 있다는 것이다.**

### 트랜잭션 처리
프레임워크가 제공하는 트랜잭션 기능을 적극 사용하는 것이 좋다. 간단한 설정만으로 트랜잭션을 시작하여 커밋하고 익셉션이 발생하면 롤백할 수 있다.

Spring은 `@Transactional`이 적용된 메서드가 `RuntimeException`을 발생시키면 트랜잭션을 롤백하고 그렇지 않으면 커밋하므로 이 규칙에 따라 코드를 작성하면 트랜잭션 처리 코드를 간결하게 유지할 수 있다.

## 표현 영역
표현 영역의 책임은 크게 다음과 같다.

- 사용자가 시스템을 사용할 수 있는 흐름(화면)을 제공하고 제어한다.
- 사용자의 요청을 알맞은 응용 서비스에 전달하고 결과를 사용자에게 제공한다.
- 사용자의 세션을 관리한다.

## 값 검증
값 검증은 표현 영역과 응용 서비스 두 곳에서 모두 수행할 수 있다. **원칙적으로 모든 값에 대한 검증은 응용 서비스에서 처리한다.**

응용 서비스에서 각 값이 존재하는지 형식이 올바른지 확인할 목적으로 익셉션을 사용할 때의 문제점은 사용자에게 좋지 않은 경험을 제공한다는 것이다. 순차적으로 필드를 검사하기 때문에 사용자 입력 값에 대한 전반적인 형식 오류 결과를 내어줄 수 없다. 이를 해결하기 위해 표현 영역에서 값을 검사하면 된다.

- 표현 영역: 필수 값, 값의 형식, 범위 등을 검증한다.
- 응용 서비스: 데이터의 존재 유무와 같은 논리적 오류를 검증한다.

## 권한 검사
개발하는 시스템마다 권한의 복잡도가 다르다. 단순한 시스템은 인증 여부만 검사하면 되는데 반해, 어떤 시스템은 관리자인지에 따라 사용할 수 있는 기능이 달라지기도 한다. 또 실행할 수 있는 기능이 역할마다 달라지는 경우도 있다.

이런 다양한 상황을 충족하기 위해 Spring Security와 같은 프레임워크는 유연하고 확장 가능한 구조를 갖고 있다. 보안 프레임워크에 대한 이해가 부족하면 프레임워크를 무턱대고 도입하는 것보다 개발할 시스템에 맞는 권한 검사 기능을 구현하는 것이 시스템 유지 보수에 유리할 수 있다.

보안 프레임워크의 복잡도를 떠나 보통 다음 세 곳에서 권한 검사를 수행할 수 있다.
- 표현 영역
- 응용 서비스
- 도메인

표현 영역에서 할 수 있는 기본적인 검사는 인증된 사용자인지 아닌지 검사하는 것이다. 대표적인 예가 회원 정보 변경 기능이다. 관련된 URL은 인증된 사용자만 접근해야 한다.

- URL을 처리하는 컨트롤러에 웹 요청을 전달하기 전에 인증 여부를 검사해서 인증된 사용자의 웹 요청만 컨트롤러에 전달한다.
- 인증된 사용자가 아닐 경우 로그인 화면으로 리다이렉트 시킨다.

이런 접근 제어를 하기 좋은 위치가 서블릿 필터다.

URL 만으로 접근 제어를 할 수 없는 경우 응용 서비스의 메서드 단위로 권한 검사를 수행해야 한다. 이는 필수는 아니다. 예를 들어 Spring Security는 AOP를 활용해서 애너테이션으로 서비스 메서드에 대한 권한 검사를 할 수 있는 기능을 제공한다.

```java
public class BlockMemberService {
  private MemberRepository memberRepository;

  @PreAuthorize("hasRole('ADMIN')")
  public void block(String memberId) {
    Member member = memberRepository.findById(memberId);
    if (member == null) throw new NoMemberException();
    member.block();
  }

  // ...
}
```

개별 도메인 객체 단위로 권한 검사를 해야 하는 경우는 구현이 복잡해진다. 예를 들어 게시글 삭제는 본인 또는 관리자 역할을 가진 사용자만 할 수 있다 가정하면, 게시글 작성자가 본인인지 확인하려면 게시글 애그리거트를 먼저 로딩해야 한다. 즉 응용 서비스의 메서드 수준에서 권한 검사를 할 수 없기 때문에 다음과 같이 직접 권한 검사 로직을 구현해야 한다.

```java
public class DeleteArticleService {

  public void delete(String userId, Long articleId) {
    Article article = articleRepository.findById(articleId);
    checkArticleExistence(article);
    permissionService.checkDeletePermission(userId, article);
    article.markDeleted();
  }

  // ...
}
```

Spring Security와 같은 보안 프레임워크를 확장해서 개별 도메인 객체 수준의 권한 검사 기능을 프레임워크에 통합할 수도 있지만 프레임워크에 대한 높은 이해가 필요하다. 그렇지 않다면 도메인에 맞는 권한 검사 기능을 직접 구현하는 것이 코드 유지 보수에 유리하다.

## 조회 전용 기능과 응용 서비스
서비스에서 조회 전용 기능을 사용하면 서비스 코드가 다음과 같이 단순히 조회 전용 기능을 호출하는 형태로 끝날 수 있다.

```java
public class OrderListService {

  public List<OrderView> getOrderList(String ordererId) {
    return orderViewDao.selectByOrderer(ordererId);
  }

  // ...
}
```

서비스에서 수행하는 추가적인 로직이 없을뿐더러 단일 쿼리만 실행하는 조회 전용 기능이어서 트랜잭션이 필요하지도 않다. 이 경우라면 굳이 서비스를 만들 필요 없이 표현 영역에서 바로 조회 전용 기능을 사용해도 문제가 없다.

응용 서비스가 사용자 요청 기능을 실행하는 데 별다른 기여를 하지 못한다면 굳이 서비스를 만들지 않아도 된다.