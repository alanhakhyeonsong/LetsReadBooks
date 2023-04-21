package chapter05;

import java.time.LocalDateTime;
import lombok.Getter;

public class Screening {

    private Movie movie;

    @Getter
    private int sequence;

    @Getter
    private LocalDateTime whenScreened;

    public Reservation reserve(Customer customer, int audienceCount) {
        return new Reservation(customer, this, calculateFee(audienceCount), audienceCount);
    }

    private Money calculateFee(int audienceCount) {
        return movie.calculateMovieFee(this).times(audienceCount);
    }
}
