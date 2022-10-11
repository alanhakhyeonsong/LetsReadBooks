package chapter01.after;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class BagTest {

    @Mock
    Invitation invitation;

    @InjectMocks
    Bag bag;

    @Test
    @DisplayName("초대장이 있는 경우 값을 지불하지 않는다.")
    public void testHasInvitation() throws Exception {
        //given
        Ticket ticket = new Ticket(8000L);

        //when
        Long fee = this.bag.hold(ticket);

        //then
        assertThat(this.bag.hasInvitation()).isTrue();
        assertThat(fee).isEqualTo(0L);
        assertThat(this.bag.hasTicket()).isTrue();
    }

    @Test
    @DisplayName("초대장이 없는 경우 값을 지불한다.")
    void testHasNotInvitation() throws Exception {
        //given
        Bag bag = new Bag(10000L);
        Ticket ticket = new Ticket(8000L);

        //when
        Long fee = bag.hold(ticket);

        //then
        assertThat(fee).isEqualTo(8000L);
    }
}
