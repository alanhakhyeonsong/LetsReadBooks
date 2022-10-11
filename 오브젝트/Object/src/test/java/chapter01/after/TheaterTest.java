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
public class TheaterTest {

    @Mock
    TicketSeller ticketSeller;

    @InjectMocks
    Theater theater;

    @Test
    @DisplayName("관객 입장 테스트")
    public void testEnterAudience() throws Exception {
        //given
        Audience audience = new Audience(new Bag(10000L));

        //when
        theater.enter(audience);

        //then
        then(ticketSeller).should(times(1)).sellTo(audience);
    }
}
