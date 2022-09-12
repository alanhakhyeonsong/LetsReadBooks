# 퍼사드(Facade) 패턴
의도: 한 서브시스템 내의 인터페이스 집합에 대한 획일화된 하나의 인터페이스를 제공하는 패턴으로, 서브 시스템을 사용하기 쉽도록 상위 수준의 인터페이스를 정의한다.

복잡한 서브 시스템 의존성을 최소화 하는 방법.

- 클라이언트가 사용해야 하는 복잡한 서브 시스템 의존성을 간단한 인터페이스로 추상화 할 수 있다.

![](https://velog.velcdn.com/images/songs4805/post/e2082248-3af8-4dcc-a172-995d7c893855/image.png)

- `Facade`: 단순하고 일관된 통합 인터페이스를 제공하며, 서브시스템을 구성하는 어떤 클래스가 어떤 요청을 처리해야 하는지 알고 있으며, 사용자의 요청을 해당 서브시스템 객체에 전달한다.
- `Subsystem`: 서브시스템의 기능을 구현하고, `Facade` 객체로 할당된 작업을 실제로 처리하지만 `Facade`에 대한 아무런 정보가 없다. 즉, 이들에 대한 어떤 참조자도 가지고 있지 않다.

## 활용성
퍼사드 패턴은 다음의 경우에 사용한다.
- 복잡한 서브시스템에 대한 단순한 인터페이스 제공이 필요할 때, 시스템 범위가 확장되면, 또한 구체적으로 설계되면 서브시스템은 계속 복잡해진다. 또한 패턴을 적용하면 확장성을 고려하여 설계하기 때문에, 작은 클래스가 만들어지게 된다. 이런 과정은 서브시스템을 재사용 가능한 것으로 만들어주고, 재정의할 수 있는 단위가 되도록 해주기도 하지만, 실제 이런 상세한 재설계나 정제의 내용까지 파악할 필요가 없는 개발자들에게는 복잡해진 각각의 클래스들을 다 이해하면서 서브시스템을 사용하기란 어려운 일이다. 이럴 때 퍼사드 패턴은 서브시스템에 대한 단순하면서도 기본적인 인터페이스를 제공함으로써 대부분의 개발자들에게 적합한 클래스 형태를 제공한다.
- 추상 개념에 대한 구현 클래스와 사용자 사이에 너무 많은 종속성이 존재할 때. 퍼사드의 사용을 통해 사용자와 다른 서브시스템 간의 결합도를 줄일 수 있다. 즉, 서브시스템에 정의된 모든 인터페이스가 공개되면 빈번한 메서드 호출이 있을 수 있으나, 이런 호출은 단순한 형태로 통합하여 제공하고 나머지 부분은 내부적으로 처리함으로써 사용자와 서브시스템 사이의 호출 횟수는 실질적으로 감소하게 되는 효과를 갖는다.
- 서브시스템을 계층화시킬 때. 퍼사드 패턴을 사용하여 각 서브시스템의 계층에 대한 접근점을 제공한다. 서브시스템이 다른 서브시스템에 종속적이라 하더라도, 각자가 제공하는 퍼사드를 통해서만 대화를 진행하게 함으로써 서브시스템 간의 종속성을 줄일 수 있다. 이로써 서브시스템 내부 설계의 변경이 다른 서브시스템에 독립적으로 자유롭게 될 수 있는 것이다.

## 구현
### 퍼사드 패턴 적용 전
```java
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Client {

    public static void main(String[] args) {
        String to = "keesun@whiteship.me";
        String from = "whiteship@whiteship.me";
        String host = "127.0.0.1";

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);

        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Test Mail from Java Program");
            message.setText("message");

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
```

### 퍼사드 패턴 적용 후
![](https://velog.velcdn.com/images/songs4805/post/70e1b9a9-4298-4229-ba87-267fc74b9d28/image.png)

```java
public class EmailSettings {

    private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
```

```java
public class EmailMessage {

    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String text;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }
}
```

```java
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {

    private EmailSettings emailSettings;

    public EmailSender(EmailSettings emailSettings) {
        this.emailSettings = emailSettings;
    }

    /**
     * 이메일 보내는 메소드
     * @param emailMessage
     */
    public void sendEmail(EmailMessage emailMessage) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", emailSettings.getHost());

        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailMessage.getFrom()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailMessage.getTo()));
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(emailMessage.getCc()));
            message.setSubject(emailMessage.getSubject());
            message.setText(emailMessage.getText());

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }    
}
```

결과적으로 `Client`의 코드는 다음과 같이 간결해진다.
```java
public class Client {

    public static void main(String[] args) {
        EmailSettings emailSettings = new EmailSettings();
        emailSettings.setHost("127.0.0.1");

        EmailSender emailSender = new EmailSender(emailSettings);

        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setFrom("keesun");
        emailMessage.setTo("whiteship");
        emailMessage.setCc("일남");
        emailMessage.setSubject("오징어게임");
        emailMessage.setText("밖은 더 지옥이더라고..");

        emailSender.sendEmail(emailMessage);
    }
}
```
또한 서브시스템의 구성 요소를 보호할 수 있어 사용자가 다루어야 할 객체의 수가 줄어들며 서브시스템을 쉽게 사용할 수 있게 되었다.

## 퍼사드 패턴의 장점과 단점
- 장점
  - 서브 시스템에 대한 의존성을 한 곳으로 모을 수 있다.
- 단점
  - 퍼사드 클래스가 서브 시스템에 대한 모든 의존성을 가지게 된다.

## Java와 Spring에서의 활용 예시
### Spring
- Spring MVC
- 스프링이 제공하는 대부분의 기술 독립적인 인터페이스와 그 구현체