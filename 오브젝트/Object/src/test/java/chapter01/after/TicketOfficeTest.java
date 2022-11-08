package chapter01.after;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class TicketOfficeTest {

    // SUT
    TicketOffice ticketOffice;

    // DOC
    Ticket ticket;
    Audience audience;

    @BeforeEach
    void setUp() {
        audience = mock(Audience.class);
        ticket = new Ticket(8000L);
        ticketOffice = new TicketOffice(1L, ticket);
    }

    @Test
    @DisplayName("티켓 판매 mock 테스트")
    void testSellTicketTo() throws Exception {
        //when
        ticketOffice.sellTicketTo(audience);

        //then
        then(audience).should().buy(ticket);
    }
}
