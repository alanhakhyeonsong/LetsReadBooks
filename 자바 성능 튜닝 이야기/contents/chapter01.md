# Story 1. 디자인 패턴, 꼭 써야 한다.
J2EE 패턴을 공부하려면, MVC 모델에 대해 먼저 이해해야 한다. J2EE 패턴에는 MVC 구조가 기본으로 깔려 있고, 요즘 많이 사용하는 Spring 프레임워크의 Spring MVC도 매우 인기 있는 부분이기 때문이다.

MVC는 Model, View, Controller의 약자다. 하나의 JSP나 Swing 처럼 화면에 모든 처리 로직을 모아 두는 것이 아니라 모델 역할, 뷰 역할, 컨트롤러 역할을 하는 클래스를 각각 만들어서 개발하는 모델이다.

- View: 사용자가 결과를 보거나 입력할 수 있는 화면. 이벤트를 발생시키고, 이벤트의 결과를 보여주는 역할을 함.
- Controller: 뷰와 모델의 연결자. 뷰에서 받은 이벤트를 모델로 연결하는 역할.
- Model: 뷰에서 입력된 내용을 저장, 관리, 수정하는 역할. 이벤트에 대한 실질적인 일을 하는 부분.

JSP 모델 1은 JSP에서 자바 빈을 호출하고 DB에서 정보를 조회, 등록, 수정, 삭제 업무를 한 뒤 결과를 브라우저로 보내 주는 방식이다. 간단하게 개발할 수 있다는 장점이 있지만, 개발 후 프로세스 변경이 생길 경우 수정이 어렵다는 단점이 있다. 더 큰 문제는, 화면과 비즈니스 모델의 분업화가 어려워 개발자의 역량에 따라 코드가 많이 달라질 수 있다는 점이다. 이 모델은 컨트롤러가 없기 때문에 MVC 모델이라 하기는 어렵다.

JSP 모델 2는 MVC 모델을 정확히 따른다. JSP로 요청을 직접 하는 JSP 모델 1과 가장 큰 차이점은 서블릿으로 요청을 한다는 것이다. 모델 2에서는 서블릿이 컨트롤러 역할을 수행한다.

## J2EE 디자인 패턴이란?
디자인 패턴은 시스템을 만들기 위해 전체 중 일부 의미 있는 클래스들을 묶은 각각의 집합이라 생각하면 된다. 반복되는 의미 있는 집합을 정의하고 이름을 지정해서, 누가 이야기하더라도 동일한 의미와 패턴이 되도록 만들어 놓은 것이다.

J2EE 디자인 패턴의 각 특징은 다음과 같다.

- Intercepting Filter 패턴: 요청 타입에 따라 다른 처리를 하기 위한 패턴
- Front Controller 패턴: 요청 전후에 처리하기 위한 컨트롤러를 지정하는 패턴
- View Helper 패턴: 프레젠테이션 로직과 상관 없는 비즈니스 로직을 헬퍼로 지정하는 패턴
- Composite View 패턴: 최소 단위의 하위 컴포넌트를 분리하여 화면을 구성하는 패턴
- Service to Worker 패턴: Front Controller와 View Helper 사이에 디스패처를 두어 조합하는 패턴
- Dispatcher View 패턴: Front Controller와 View Helper로 디스패처 컴포넌트를 형성한다. 뷰 처리가 종료될 때까지 다른 활동을 지연한다는 점이 Service to Worker 패턴과 다르다.
- **Business Delegate 패턴**: 비즈니스 서비스 접근을 캡슐화하는 패턴
- **Service Locator 패턴**: 서비스와 컴포넌트 검색을 쉽게 하는 패턴
- **Session Facade 패턴**: 비즈니스 티어 컴포넌트를 캡슐화하고, 원격 클라이언트에서 접근할 수 있는 서비스를 제공하는 패턴
- Composite Entity 패턴: 로컬 엔티티 빈과 POJO를 이용하여 큰 단위의 엔티티 객체를 구현
- **Transfer Object 패턴**: 일명 Value Object 패턴이라 많이 알려짐. 데이터를 전송하기 위한 객체에 대한 패턴
- Transfer Object Assembler 패턴: 하나의 Transfer Object로 모든 타입 데이터를 처리할 수 없으므로, 여러 Transfer Object를 조합하거나 변형한 객체를 생성하여 사용하는 패턴
- Value List Handler 패턴: 데이터 조회를 처리하고, 결과를 임시 저장하며, 결과 집합을 검색하여 필요한 항목을 선택하는 역할을 수행.
- **Data Access Object 패턴**: 일명 DAO라 많이 알려짐. DB에 접근을 전담하는 클래스를 추상화하고 캡슐화함.
- Service Activator 패턴: 비동기적 호출을 처리하기 위한 패턴

이 중 성능과 관련된 패턴은 Service Locator 패턴이다. 성능에 큰 영향은 미치진 않지만 반드시 사용해야 하는 패턴은 Transfer Object 패턴이다.

## Transfer Object 패턴
Value Object라고도 불리는 Trnasfer Object는 데이터를 전송하기 위한 객체에 대한 패턴이다.

```java
public class EmployeeTO implements Serializable {
    private String empName;
    private String empId;
    private Strign empPhone;

    public EmployeTO() {
        super();
    }

    public EmployeeTo(String empName, String empId, String empPhone) {
        super();
        this.empName = empName;
        this.empId = empId;
        this.empPhone = empPhone;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        if (empName == null) return "";
        else return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getEmpPhone() {
        this.empPhone;
    }

    public void setEmpPhone(String empPhone) {
        this.empPhone = empPhone;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("empName=").append(empName).append("empId=")
            .append(empId).append("empPhone=").append(empPhone);
        return sb.toString();
    }
}
```

Transfer Object 패턴은 Transfer Object를 만들어 하나의 객체에 여러 타입의 값을 전달하는 일을 수행한다.

Transfer Object를 사용할 때 필드를 private으로 지정해서 getter/setter를 만들지, 아니면 public으로 지정해서 메서드들을 만들지 않을지에 대한 정답은 없지만, 성능상으로 따져볼 때 getter/setter를 만들지 않는 것이 더 빠르다. 하지만, 정보 은닉 및 불필요한 수정 방지를 위해 getter/setter를 열어두는 것이 일반적이다.

Transfer Object를 잘 만들어 놓으면 각 소스에서 일일이 null 체크를 할 필요가 없기 때문에 오히려 더 편해진다.

또한 `toString()` 메서드를 반드시 재정의해야 한다. 값 비교를 할 때나 데이터를 확인할 일들 때문이다.

Transfer Object를 사용한다고 성능이 드라마틱하게 개선되진 않는다. 하지만, 하나의 객체에 결과 값을 담아 올 수 있어 두 번, 세 번씩 요청을 하는 일이 발생하는 것을 줄여 주므로 이 패턴을 사용하기를 권장한다.

## Service Locator 패턴
```java
public class ServiceLocator {
    private InitialContext ic;
    private Map cache;
    private static ServiceLocator me;
    
    static {
        me = new ServiceLocator();
    }

    private ServiceLocator() {
        cache = Collections.synchronizedMap(new HashMap());
    }

    public InitialContext getInitialContext() throws Exception {
        try {
            if (ic == null) {
                ic = new InitialContext();
            }
        } catch (Exception e) {
            throw e;
        }
        return ic;
    }

    public static ServiceLocator getInstance() {
        return me;
    }

    // ...
}
```

**Service Locator 패턴은 예전에 많이 사용되었던 EJB의 EJB Home 객체나 DB의 DataSource를 찾을 때 소요되는 응답 속도를 감소시키기 위해 사용된다.** cache라는 Map 객체에 home 객체를 찾은 결과를 보관하고 있다가, 누군가 그 객체를 필요로 할 때 메모리에서 찾아 제공하도록 되어 있다. 만약 해당 객체가 cache에 없으면 메모리에서 찾는다.