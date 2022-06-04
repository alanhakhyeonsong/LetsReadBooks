import org.junit.jupiter.api.Test;
import vo.ModelNumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ModelNumberTest {
    @Test
    public void exceptionTest() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            new ModelNumber(null, null, null);
        });
    }

    @Test
    public void initTest() throws Exception {
        //given
        ModelNumber modelNumber = new ModelNumber("a20421", "100", "1");

        //then
        assertEquals("a20421", modelNumber.getProductCode());
        assertEquals("100", modelNumber.getBranch());
        assertEquals("1", modelNumber.getLot());
        assertEquals("a20421-100-1", modelNumber.toString());
    }
}
