# 2장. 시스템 특유의 값을 나타내기 위한 '값 객체'
## 값 객체란?
프로그래밍 언어에는 원시 데이터 타입(`int`, `String`)이 있다. 이 원시 데이터 타입만 사용해 시스템을 개발할 수도 있지만, 때로는 시스템 특유의 값을 정의해야 할 때가 있다.  
**이러한 시스템 특유의 값을 표현하기 위해 정의하는 객체를 값 객체라고 한다.**

```java
String fullName = "Sergio Ramos";
System.out.println(fullName); // Sergio Ramos 라는 값을 출력함
```

`fullName`은 문자열 타입의 값을 저장하는 변수로, '이름'을 나타낸다. 하지만, 원시 타입의 한계가 명확하다. 이름 값이 '성과 이름을 포함한 이름'인지, '한국인 이름만 포함하는 이름'인지와 같이 이름을 쓰는 관습에 따라 의도한 대로 코드가 동작하지 않을 수 있다.

객체지향 프로그래밍에서는 이런 문제를 해결하기 위해 클래스를 사용한다.
```java
@Getter
@Setter
public class FullName {
    private String firstName;
    private String lastName;

    public FullName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}

FullName fullName = new FullName("Sergio", "Ramos");
System.out.println(fullName.getLastName()); // Ramos가 출력됨
```

위 예시에서 변수 `fullName`은 성명을 나타내는 객체로, 값을 표현한다.

이처럼, 시스템에서 필요로 하는 값이 원시 데이터 타입이 아닐 수도 있다. 시스템의 요구 사항에 따라 값을 나타내는 적합한 표현이 정해진다.  
이 예시에서 `FullName` 클래스는 객체이기도 하고 동시에 값이기도 하다. 따라서 **값 객체**라고 부른다. **도메인 주도 설계에서 말하는 값 객체는 이렇게 시스템 특유의 값을 나타내는 객체다.**

> ENTITY의 식별성을 관리하는 일은 매우 중요하지만 그 밖의 객체에 식별성을 추가하면서 시스템의 성능이 저하되고, 분석 작업이 별도로 필요하며, 모든 객체를 동일한 것으로 보이게 해서 모델이 혼란스러워질 수 있다. 소프트웨어 설계는 복잡성과의 끊임없는 전투다. 그러므로 우리는 특별하게 다뤄야 할 부분과 그렇지 않은 부분을 구분해야 한다. 하지만 이러한 범주에 속하는 객체를 단순히 식별성이 없는 것으로만 생각한다면 우리의 도구상자나 어휘에 추가할 게 그리 많지 않을 것이다. 사실 이 같은 객체는 자체적인 특징을 비롯해 모델에 중요한 의미를 지닌다. 이것들이 사물을 서술하는 객체다. 개념적 식별성을 갖지 않으면서 도메인의 서술적 측면을 나타내는 객체를 VALUE OBJECT라 한다. - 도메인 주도 설계

## 값의 성질과 값 객체를 도입했을 때의 장점
값의 성질은 대표적으로 다음과 같다.
- 변하지 않는다.
- 주고받을 수 있다.
- 등가성을 비교할 수 있다.

당연한 일이지만 시스템 고유의 값을 객체로 나타내면 그만큼 정의하는 클래스의 수도 늘어난다. 원시 타입의 값을 잘 활용하는 방법으로 개발하는데 익숙하다면 많은 수의 클래스를 정의하는 것을 껄끄러워 하게 된다.

값 객체의 장점은 다음과 같다.
- 표현력이 증가한다.
- 무결성이 유지된다.
- 잘못된 대입을 방지한다.
- 로직이 코드 이곳저곳에 흩어지는 것을 방지한다. (유효성 검사 로직)

### 표현력의 증가
```java
@Getter
public class ModelNumber {
    private String productCode;
    private String branch;
    private String lot;

    public ModelNumber(String productCode, String branch, String lot) {
        if (productCode == null) throw new IllegalArgumentException("productCode 가 null 입니다.");
        if (branch == null) throw new IllegalArgumentException("branch 가 null 입니다.");
        if (lot == null) throw new IllegalArgumentException("lot 가 null 입니다.");

        this.productCode = productCode;
        this.branch = branch;
        this.lot = lot;
    }

    @Override
    public String toString() {
        return productCode + "-" + branch + "-" + lot;
    }
}
```
`ModelNumber` 클래스는 제품번호가 제품코드와 지점번호, 로트번호로 구성됨을 알 수 있다. 아무 정보를 제공하지 않는 문자열과 비교하면 큰 진보다.  
값 객체는 자기 정의를 통해 자신이 무엇인지에 대한 정보를 제공하는 자기 문서화를 돕는다.

### 무결성의 유지
사용자명은 간단히 말하면 문자열이다. 그러나 시스템에 따라 'n글자 이상 m글자 이하'와 같은 제한이나 '알파벳 문자만을 포함할 것'과 같은 규칙이 있을 수 있다.

원시타입(`String`)으로 이를 해결하고자 한다면, 매번 유효성 검사를 위한 코드를 작성해야 하고 한 곳이라도 잘못되면 시스템 오류로 이어질 수 있다.

```java
@Getter
public class UserName {
    private String value;

    public UserName(String value) {
        if (value == null) throw new IllegalArgumentException("value 가 null 입니다.");
        if (value.length() < 3) throw new IllegalArgumentException("사용자명은 3글자 이상이어야 합니다.");

        this.value = value;
    }
}
```
값 객체를 잘 이용하면 유효하지 않은 값을 처음부터 방지할 수 있다. `UserName` 클래스는 방어 코드를 통해 길이가 세 글자 미만인 사용자명을 허용하지 않는다. 시스템상 유효하지 않은 값은 이런 방식의 확인을 거쳐 허용하지 않는 것이다. 결과적으로 규칙을 위반하는 유효하지 않은 값을 걱정할 필요가 없다.

### 잘못된 대입 방지하기
원시 타입 대신 값 객체를 사용하게 되면, 컴파일러는 대입문에서 타입 불일치를 발견해 에러를 발생시킨다.

```java
// 원시 타입
User createUser(String name) { ... }

// 값 객체
User createUser(UserName name) { ... }
```

### 로직을 한곳에 모아두기
DRY 원칙(Do not Repeat Yourself)에서 밝혔듯이 코드 중복을 방지하는 일은 매우 중요하다. 중복된 코드가 많아지면 코드를 수정하는 난이도가 급상승한다.

앞서 `UserName` 클래스 내부에 검증 로직을 두었기 때문에 만약 사용자 생성, 수정 등의 일이 필요할 때 이 또한 매번 검증을 수행하는 코드를 개별 메서드에 직접 추가할 필요가 없다.

```java
public interface UserService {
    void CreateUser(UserName userName);
    void UpdateUser(UserName userName);
}

public class UserServiceImpl implements UserService {
    @Override
    public void CreateUser(UserName userName) {
        System.out.println(userName + "생성");
    }

    @Override
    public void UpdateUser(UserName userName) {
        System.out.println(userName + "수정");
    }
}
```
위 예제 코드에선 단순히 출력문을 수행했지만, 인자로 넘어오는 `UserName` 덕분에 검증하는 로직을 일일이 추가할 필요는 없었다.

정리하자면, 값 객체를 정의해 그 안에 규칙을 정리했기에 규칙이 변경 될 경우 수정할 범위는 `UserName` 클래스 안으로 국한된다. **규칙을 기술한 코드가 한 곳에 모여 있다면 수정할 곳도 한 곳 뿐이라는 의미다.**