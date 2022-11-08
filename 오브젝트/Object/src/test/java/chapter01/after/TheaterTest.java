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
class TheaterTest {

    // SUT
    Theater theater;

    // DOC
    TicketSeller ticketSeller;

    @BeforeEach
    void setUp() {
        ticketSeller = mock(TicketSeller.class);
        theater = new Theater(ticketSeller);
    }

    @Test
    @DisplayName("관객 입장 테스트")
    void testEnterAudience() throws Exception {
        //given
        Audience audience = new Audience(new Bag(10000L));

        //when
        theater.enter(audience);

        //then
        then(ticketSeller).should(times(1)).sellTo(audience);
    }
}
