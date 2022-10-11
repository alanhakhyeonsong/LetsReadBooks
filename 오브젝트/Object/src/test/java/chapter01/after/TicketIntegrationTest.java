package chapter01.after;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TicketIntegrationTest {

    @Test
    @DisplayName("초대장이 있는 관객 테스트")
    public void testHasInvitation() throws Exception {
        //given
        Ticket[] tickets = { new Ticket(8000L), new Ticket(8000L), new Ticket(8000L) };
        TicketOffice ticketOffice = new TicketOffice(3L, tickets);

        TicketSeller ticketSeller = new TicketSeller(ticketOffice);

        Theater theater = new Theater(ticketSeller);

        Invitation invitation = new Invitation(LocalDateTime.now());
        Bag bag = new Bag(invitation, 10000L);
        Audience audience = new Audience(bag);

        //when
        theater.enter(audience);

        //then
        assertThat(bag.hasTicket()).isTrue();
        assertThat(bag.hasInvitation()).isTrue();
    }

    @Test
    @DisplayName("초대장이 없는 관객 테스트")
    public void testHasNotInvitation() throws Exception {
        //given
        Ticket[] tickets = { new Ticket(8000L), new Ticket(8000L), new Ticket(8000L) };
        TicketOffice ticketOffice = new TicketOffice(3L, tickets);

        TicketSeller ticketSeller = new TicketSeller(ticketOffice);

        Theater theater = new Theater(ticketSeller);

        Bag bag = new Bag(10000L);
        Audience audience = new Audience(bag);

        //when
        theater.enter(audience);

        //then
        assertThat(bag.hasTicket()).isTrue();
        assertThat(bag.hasInvitation()).isFalse();
    }
}
