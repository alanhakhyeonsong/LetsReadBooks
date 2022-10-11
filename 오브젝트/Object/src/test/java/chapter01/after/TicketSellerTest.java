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
public class TicketSellerTest {

    @Mock
    TicketOffice ticketOffice;

    @InjectMocks
    TicketSeller ticketSeller;

    @Test
    @DisplayName("티켓 판매 테스트")
    public void testSellTo() throws Exception {
        //given
        Audience audience = new Audience(new Bag(10000L));

        //when
        ticketSeller.sellTo(audience);

        //then
        then(ticketOffice).should(times(1)).sellTicketTo(audience);
    }
}
