package chapter05;

import java.time.Duration;

public class PercentDiscountMovie extends Movie {

    private double percent;

    public PercentDiscountMovie(
            String title,
            Duration runningTime,
            Money fee,
            double percent,
            DiscountCondition... discountConditions
    ) {
        super(title, runningTime, fee, discountConditions);
        this.percent = percent;
    }

    @Override
    protected Money calculateDiscountAmount() {
        return getFee().times(percent);
    }
}
