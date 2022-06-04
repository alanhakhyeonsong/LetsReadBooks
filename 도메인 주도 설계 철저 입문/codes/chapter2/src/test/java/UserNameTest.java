import org.junit.jupiter.api.Test;
import vo.UserName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserNameTest {
    @Test
    public void exceptionTest() throws Exception {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new UserName("sr");
        });
        assertEquals("사용자명은 3글자 이상이어야 합니다.", exception.getMessage());
    }
}
