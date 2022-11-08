package chapter01.after;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TicketSellerTest {

    // SUT
    TicketSeller ticketSeller;

    // DOC
    TicketOffice ticketOffice;

    @BeforeEach
    void setUp() {
        ticketOffice = mock(TicketOffice.class);
        ticketSeller = new TicketSeller(ticketOffice);
    }

    @Test
    @DisplayName("티켓 판매 테스트")
    void testSellTo() throws Exception {
        //given
        Audience audience = new Audience(new Bag(10000L));

        //when
        ticketSeller.sellTo(audience);

        //then
        then(ticketOffice).should(times(1)).sellTicketTo(audience);
    }
}
