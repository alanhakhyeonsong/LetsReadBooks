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
class AudienceTest {

    // SUT
    Audience audience;

    // DOC
    Bag bag;

    @BeforeEach
    void setUp() {
        bag = mock(Bag.class);
        audience = new Audience(bag);
    }

    @Test
    @DisplayName("티켓 구매 테스트")
    void testBuyTicket() throws Exception {
        //given
        Ticket ticket = new Ticket(8000L);

        //when
        audience.buy(ticket);

        //then
        then(bag).should(times(1)).hold(ticket);
    }
}
