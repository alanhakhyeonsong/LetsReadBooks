package chapter02;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"InnerClassMayBeStatic", "NonAsciiCharacters"})
@DisplayName("Movie class")
class MovieTest {

    private final Money givenFixedDiscountFee = Money.wons(800);
    private final double givenDiscountRate = 0.1;
    private final LocalDateTime givenMonday = LocalDate.of(2020, Month.MARCH, 2).atStartOfDay();
    private final LocalDateTime givenTuesday = givenMonday.plusDays(1);
    private final LocalDateTime givenThursday = givenMonday.plusDays(3);
    private final LocalDateTime givenSunday = givenMonday.plusDays(6);
    private final LocalDateTime givenSundayAfternoon = givenSunday.withHour(13).withMinute(30);

    Movie given_아바타() {
        return new Movie(
                "아바타",
                Duration.ofMinutes(120),
                Money.wons(10000),
                given_아바타_할인정책
        );
    }

    private final DiscountPolicy given_아바타_할인정책 = new AmountDiscountPolicy(
            givenFixedDiscountFee,
            new SequenceCondition(1),
            new SequenceCondition(10),
            new PeriodCondition(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 59)),
            new PeriodCondition(DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(20, 59))
    );

    Movie given_타이타닉() {
        return new Movie(
                "타이타닉",
                Duration.ofMinutes(180),
                Money.wons(11000),
                given_타이타닉_할인정책
        );
    }

    private final DiscountPolicy given_타이타닉_할인정책 = new PercentDiscountPolicy(
            0.1,
            new PeriodCondition(DayOfWeek.TUESDAY, LocalTime.of(14, 0), LocalTime.of(16, 59)),
            new SequenceCondition(2),
            new PeriodCondition(DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(13, 59))
    );

    Movie given_스타워즈() {
        return new Movie(
                "스타워즈",
                Duration.ofMinutes(210),
                Money.wons(10000),
                given_스타워즈_할인정책
        );
    }

    private final DiscountPolicy given_스타워즈_할인정책 = new NonDiscountPolicy();

    abstract class TestCalculateMovieFee {
        abstract Movie givenMovie();

        Money 기본요금() {
            return givenMovie().getFee();
        }

        Money subject(Screening screening) {
            return givenMovie().calculateMovieFee(screening);
        }
    }

    @Nested
    @DisplayName("calculateMovieFee 메소드는")
    class Describe_calculateMovieFee {

        @Nested
        @DisplayName("주어진 영화가 '아바타'일 때 (할인 조건: 상영 시작 시간, 상영 순번 / 할인 금액: 고정 금액)")
        class Context_with_avatar extends TestCalculateMovieFee {
            Movie givenMovie() {
                return given_아바타();
            }

            @Nested
            @DisplayName("상영 시작 시간이 할인 조건에 맞는다면")
            class Context_with_valid_period {
                final List<LocalDateTime> 할인_조건에_맞는_상영_시작_시간들 = List.of(
                        // edge cases - 월요일
                        givenMonday.withHour(10).withMinute(0),
                        givenMonday.withHour(11).withMinute(59),
                        // inner cases - 월요일
                        givenMonday.withHour(10).withMinute(1),
                        givenMonday.withHour(11).withMinute(58),
                        // edge cases - 목요일
                        givenThursday.withHour(10).withMinute(0),
                        givenThursday.withHour(11).withMinute(59),
                        // inner cases - 목요일
                        givenThursday.withHour(10).withMinute(1),
                        givenThursday.withHour(11).withMinute(58)
                );

                List<Screening> givenScreens() {
                    return 할인_조건에_맞는_상영_시작_시간들.stream()
                            .map(상영시간 -> new Screening(givenMovie(), 0, 상영시간))
                            .collect(Collectors.toList());
                }

                @Test
                @DisplayName("고정할인 금액만큼 할인된 금액을 리턴한다.")
                void it_returns_discounted_fee() {
                    for (Screening 할인되는_시간에_시작하는_상영 : givenScreens()) {
                        final Money 계산된_요금 = subject(할인되는_시간에_시작하는_상영);

                        assertThat(기본요금().minus(givenFixedDiscountFee)).isEqualTo(계산된_요금);
                    }
                }
            }
        }
    }
}