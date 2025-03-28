# Story 11. JSP와 서블릿, Spring에서 발생할 수 있는 여러 문제점
> 들어가기 전에, 이 책이 나온지 벌써 10년이 지나서 오래된 내용일 수 있음에 주의하자. 그럼에도 불구하고 기술의 본질은 여전히 변하지 않았다. 책에 있는 내용을 그대로 요약할 것이니 알아서 해석하자.

자바 기반의 시스템 중 WAS에서 병목 현상이 발생할 수 있는 부분을 세밀하게 나눈다고 하면, UI 부분과 비즈니스 로직 부분으로 나눌 수 있다. 자바 기반의 서버단 UI를 구성하는 대부분의 기술은 JSP와 서블릿을 확장하는 기술이다.

## JSP와 Servlet의 기본적인 동작 원리는 꼭 알아야 한다
일반적으로 JSP와 같은 웹 화면단을 처리하는 부분에서 소요되는 시간은 많지 않다. JSP의 경우 가장 처음에 호출되는 경우에만 시간이 소요되고, 그 이후의 시간에는 컴파일된 서블릿 클래스가 수행되기 때문이다. JSP의 라이프 사이클은 다음의 단계를 거친다.

1. JSP URL 호출
2. 페이지 번역
3. JSP 페이지 컴파일
4. 클래스 로드
5. 인스턴스 생성
6. `jspInit` 메서드 호출
7. `_jspService` 메서드 호출
8. `jspDestroy` 메서드 호출

해당 JSP 페이지가 이미 컴파일되어 있고, 클래스가 로드되어 있고, JSP 파일이 변경되지 않았다면, 가장 많은 시간이 소요되는 2~4 프로세스는 생략된다.

서버의 종류에 따라 서버가 기동될 때 컴파일을 미리 수행하는 Precompile 옵션이 있다. 이 옵션을 선택하면 서버에 최신 버전을 반영한 이후에 처음 호출되었을 때 응답 시간이 느린 현상을 방지할 수 있다. 하지만 개발 시에 이 옵션을 켜 놓으면 서버를 기동할 때마다 컴파일을 수행하기 때문에 시간이 오래걸린다. 따라서 상황에 맞게 옵션을 지정하자.

이번에는 서블릿의 라이프 사이클을 살펴보자. WAS의 JVM이 시작한 후에는,

- Servlet 객체가 자동으로 생성되고 초기화 되거나,
- 사용자가 해당 Servlet을 처음으로 호출했을 때 생성되고 초기화 된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/d9c34319-ba95-4fb0-a038-2c4d5967f54b)

그 다음에는 계속 '사용 가능' 상태로 대기한다. 중간에 예외가 발생하면 '사용 불가능' 상태로 빠졌다가 다시 '사용 가능' 상태로 변환되기도 한다. 이후 해당 서블릿이 더 이상 필요 없을 때는 '파기' 상태로 넘어간 후 JVM에서 '제거' 된다.

반드시 기억해야 할 점은 **서블릿은 JVM에 여러 객체로 생성되지 않는다는 점이다.** WAS가 시작하고, '사용 가능' 상태가 된 이상 대부분의 서블릿은 JVM에 살아 있고, 여러 스레드에서 해당 서블릿의 `service()` 메서드를 호출하여 공유한다.

만약 서블릿 클래스의 메서드 내에 선언한 지역 변수가 아닌 멤버 변수(인스턴스 변수)를 선언하여 `service()` 메서드에서 사용하면 어떤 일이 발생할까?

```java
public class DontUserLikeThisServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    String successFlag = "N";

    public DontUserLikeThisServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        successFlag = request.getParameter("successFlag");
    }
}
```

`successFlag` 값은 여러 스레드에서 접근하면서 계속 값이 바뀔 것이다. 이처럼 여러 스레드로부터 뭇매를 맞으면, 데이터가 꼬여 원하지 않는 값들이 출력될 수도 있다. 따라서 **`service()` 메서드를 구현할 때는 멤버 변수나 `static`한 클래스 변수를 선언하여 지속적으로 변경하는 작업은 피하길 바란다.**

## 적절한 include 사용하기
JSP의 `include` 기능을 사용하면, 하나의 JSP에서 다른 JSP를 호출하여 여러 JSP 파일을 혼합해서 하나의 JSP로 만들 수 있다. JSP에서 사용할 수 있는 `include` 방식은 정적인 방식과 동적인 방식이 있다.

- 정적 방식: JSP의 라이프 사이클 중 JSP 페이지 번역 및 컴파일 단계에서 필요한 JSP를 읽어 메인 JSP의 자바 소스 및 클래스에 포함 시키는 방식
  - `<%@include file="관련 URL"%>`
- 동적 방식: 페이지가 호출될 때마다 지정된 페이지를 불러들여서 수행하도록 되어 있다.
  - `<jsp:include page="relativeURL"/>`

당연한 이야기지만, 동적인 방식이 정적인 방식보다 느릴 수 밖에 없다. 응답 속도를 비교해보면 약 30배 차이가 난다고 한다.

즉, 성능을 빠르게 하려면 정적인 방식을 사용해야 한다는 의미가 되지만, 모든 화면을 정적인 방식으로 수행하면 잘 수행되던 화면에서 오류가 발생할 수 있다. 정적 방식을 사용하면 메인 JSP에 추가되는 JSP가 생긴다. 이 때 추가된 JSP와 메인 JSP에 동일한 이름의 변수가 있으면 심각한 오류가 발생할 수 있다.

## 자바 빈즈, 잘 쓰면 약 못 쓰면 독
Java Beans는 UI에서 서버 측 데이터를 담아 처리하기 위한 컴포넌트다. 간단히 자바 빈즈의 문제점만 알아보자.

**자바 빈즈를 통해 `useBean`을 하면 성능에 많은 영향을 미치진 않지만, 너무 많이 사용하면 JSP에서 소요되는 시간이 증가될 수 있다.**

책에 예제 코드를 살펴보면, 해당 화면의 경우 DB까지 전체 처리하는 데 소요된 시간은 97ms로, 그중 JSP에서 소요된 시간이 57ms다. JSP에서 자바 빈즈를 처리하기 위해 소요된 시간은 47ms로 전체 응답 시간의 48%에 해당하는 시간이다.

이 시간을 줄이기 위해선 TO(Transfer object) 패턴을 사용해야 한다. 하나의 TO 클래스를 만들고, 각 문자열 및 `HashMap`, `List`를 그 클래스의 변수로 지정하여 사용하면 화면을 수행하는 데 소요된 시간 중 48%가 절약된다.

한두 개의 자바 빈즈를 사용하는 것은 상관 없지만, 10~20개의 자바 빈즈를 사용하면 성능에 영향을 주게 된다.

## 태그 라이브러리도 잘 써야 한다
태그 라이브러리는 JSP에서 공통적으로 반복되는 코드를 클래스로 만들고, 그 클래스를 HTML 태그와 같이 정의된 태그로 사용할 수 있도록 하는 라이브러리다.

태그 라이브러리는 XML 기반의 tld 파일과 태그 클래스로 구성되어 있다. 이를 사용하기 위해선 `web.xml` 파일을 열어 `tld`의 URI와 파일 위치를 정의해야 한다. `tag`라는 태그의 하위 태그에 태그 라이브러리 이름과 클래스를 지정하여 사용한다.

태그 라이브러리에서 성능상 문제가 발생할 때는 언제일까? **태그 라이브러리 클래스를 잘못 작성하거나 태그 라이브러리 클래스로 전송되는 데이터가 많을 때 성능에 문제가 된다.**

화면에서 태그 라이브러리를 통해 100~500건을 처리할 때 소요되는 시간을 놓고 WAS:DB 소요 시간을 비교해보면 1:9로 DB에서 소요된 시간이 월등히 높았다. 하지만, 이 사이트의 경우 한 번에 검색 가능한 목록에 대한 제한이 없었다. 그리고 일반적으로 조회되는 목록의 건수가 4000건이 넘었다.

4000건을 조회 할 때의 WAS:DB에서의 응답 시간 비율은 5:5를 보였다. 물론 4000건의 데이터를 한 번에 조회하는 것 자체가 문제가 있다. 하지만 기존 CS 시스템의 틀을 벗어나지 못하거나, 벗어나려 하지 않는 고객들이 있는 사이트는 어쩔 수 없이 그만큼의 데이터를 처리해 주어야 한다.

또한 태그 라이브러리는 태그 사이에 있는 데이터를 넘겨주어야 하는데, 이 때 넘겨주는 데이터 형태는 대부분 문자열 타입이다. **따라서 데이터가 많으면 많을수록 처리를 해야 하는 내용이 많아지고, 자연히 태그 라이브러리 클래스에서 처리되는 시간이 많아질 수밖에 없다. 목록을 처리하면서 대용량의 데이터를 처리할 경우에는 태그 라이브러리의 사용을 자제하자.**

## 스프링 프레임워크를 사용하면서 발생할 수 있는 문제점들
**스프링 프레임워크를 사용할 때 성능 문제가 가장 많이 발생하는 부분은 Proxy와 관련되어 있다.** 스프링 프록시는 기본적으로 실행 시에 생성된다. 따라서, 개발할 때 적은 요청을 할 때는 이상이 없다가, 요청량이 많은 운영 상황으로 넘어가면 문제가 나타날 수 있다. 스프링이 프록시를 사용하게 되는 주요 기능은 바로 **트랜잭션**이다. `@Transactional`을 사용하면 해당 어노테이션을 사용한 클래스의 인스턴스를 처음 만들 때 프록시 객체를 만든다.

이밖에도, 개발자가 직접 스프링 AOP를 사용해서 별도의 기능을 추가하는 경우에도 프록시를 사용하는데, 이 부분에서 문제가 많이 발생한다. 스프링이 자체적으로 제공하는 기능은 검증된 기능이지만, 개발자가 직접 작성한 AOP 코드는 예상하지 못한 성능 문제를 보일 가능성이 매우 높다.

추가로, **스프링이 내부 매커니즘에서 사용하는 캐시도 조심해서 써야 한다.** 예를 들어 스프링 MVC에서 작성하는 메서드의 리턴 타입으로 다음과 같은 문자열을 사용할 수 있다.

```java
public class SampleController {
    
    @RequestMapping("/member/{id}")
    public String hello(@PathVariable int id) {
        return "redirect:/member/" + id;
    }
}
```

이렇게 문자열 자체를 리턴하면 스프링은 해당 문자열에 해당하는 실제 뷰 객체를 찾는 매커니즘을 사용하는데, 이 때 매번 동일한 문자열에 대한 뷰 객체를 새로 찾기 보다는 이미 찾아본 뷰 객체를 캐싱해두면 다음에도 동일한 문자열이 반환됐을 때 훨씬 빠르게 뷰 객체를 찾을 수 있다.

**스프링에서 제공하는 `ViewResolver` 중에 자주 사용되는 `InternalResourceViewResolver`에는 그러한 캐싱 기능이 내장되어 있다.**

만약 매번 다른 문자열이 생성될 가능성이 높고, 상당히 많은 수의 키 값으로 캐시 값이 생성될 여지가 있는 상황에서는 문자열을 반환하는 게 메모리에 치명적일 수 있다. 따라서 이런 상황에선 **뷰 이름을 문자열로 반환하기 보단 뷰 객체 자체를 반환하는 방법이 메모리 릭을 방지하는 데 도움이 된다.**

```java
public class SampleController {
    
    @RequestMapping("/member/{id}")
    public String hello(@PathVariable int id) {
        return new RedirectView("/member/" + id;)
    }
}
```