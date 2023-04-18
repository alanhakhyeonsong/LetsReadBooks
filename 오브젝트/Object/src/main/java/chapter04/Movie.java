package chapter04;

import java.time.Duration;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Movie {

    private String title;
    private Duration runningTime;
    private Money fee;
    private List<DiscountCondition> discountConditions;

    private MovieType movieType;
    private Money discountAmount;
    private double discountPercent;

    @Builder
    public Movie(String title, Duration runningTime, Money fee, List<DiscountCondition> discountConditions,
            MovieType movieType, Money discountAmount, double discountPercent) {
        this.title = title;
        this.runningTime = runningTime;
        this.fee = fee;
        this.discountConditions = discountConditions;
        this.movieType = movieType;
        this.discountAmount = discountAmount;
        this.discountPercent = discountPercent;
    }
}
