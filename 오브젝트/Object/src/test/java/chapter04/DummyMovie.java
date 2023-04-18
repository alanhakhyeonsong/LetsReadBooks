package chapter04;

import static chapter04.DiscountConditionType.PERIOD;
import static chapter04.DiscountConditionType.SEQUENCE;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;

import java.time.LocalTime;
import java.util.List;

public class DummyMovie {

    static final Money givenFixedDiscountFee = Money.wons(800);
    static final double givenDiscountRate = 0.1;

    static final List<DiscountCondition> avatarDiscountCondition = List.of(
            new DiscountCondition(SEQUENCE, 1),
            new DiscountCondition(SEQUENCE, 10),
            new DiscountCondition(PERIOD, MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 59)),
            new DiscountCondition(PERIOD, THURSDAY, LocalTime.of(10, 0), LocalTime.of(20, 59))
    );

    static final Movie AVATAR = Movie.builder()
            .title("아바타")
            .fee(Money.wons(10000))
            .movieType(MovieType.AMOUNT_DISCOUNT)
            .discountAmount(givenFixedDiscountFee)
            .discountConditions(avatarDiscountCondition)
            .build();

    static final List<DiscountCondition> titanicDiscountCondition = List.of(
            new DiscountCondition(PERIOD, TUESDAY, LocalTime.of(14, 0), LocalTime.of(16,59)),
            new DiscountCondition(SEQUENCE, 2),
            new DiscountCondition(PERIOD, THURSDAY, LocalTime.of(10, 0), LocalTime.of(13,59))
    );

    static final Movie TITANIC = Movie.builder()
            .title("타이타닉")
            .fee(Money.wons(11000))
            .movieType(MovieType.PERCENT_DISCOUNT)
            .discountPercent(givenDiscountRate)
            .discountConditions(titanicDiscountCondition)
            .build();

    static final Movie STARWARS = Movie.builder()
            .title("스타워즈")
            .fee(Money.wons(10000))
            .movieType(MovieType.NONE_DISCOUNT)
            .discountAmount(Money.ZERO)
            .discountConditions(List.of())
            .build();
}
