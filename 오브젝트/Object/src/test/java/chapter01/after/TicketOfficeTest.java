package chapter01.after;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class TicketOfficeTest {

    @Mock
    Audience audience;

    @Test
    @DisplayName("티켓 판매 mock 테스트")
    public void testSellTicketTo() throws Exception {
        //given
        Ticket ticket = new Ticket(8000L);
        TicketOffice ticketOffice = new TicketOffice(1L, ticket);

        //when
        ticketOffice.sellTicketTo(audience);

        //then
        then(audience).should().buy(ticket);
    }
}
