package vo;

import lombok.Getter;

@Getter
public class UserName {
    private String value;

    public UserName(String value) {
        if (value == null) throw new IllegalArgumentException("value 가 null 입니다.");
        if (value.length() < 3) throw new IllegalArgumentException("사용자명은 3글자 이상이어야 합니다.");

        this.value = value;
    }
}
