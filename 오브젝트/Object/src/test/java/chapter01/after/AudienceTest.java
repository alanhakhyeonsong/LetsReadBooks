package chapter01.after;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class AudienceTest {

    @Mock
    Bag bag;

    @InjectMocks
    Audience audience;

    @Test
    @DisplayName("티켓 구매 테스트")
    public void testBuyTicket() throws Exception {
        //given
        Ticket ticket = new Ticket(8000L);

        //when
        audience.buy(ticket);

        //then
        then(bag).should(times(1)).hold(ticket);
    }
}
