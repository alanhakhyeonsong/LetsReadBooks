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

            @Override
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

            @Nested
            @DisplayName("상영 시작 시간이 할인 조건에 맞지 않는다면")
            class Context_with_invalid_period {
                final List<LocalDateTime> 할인_조건에_맞지_않는_상영_시작_시간들 = List.of(
                        // 월요일
                        givenMonday.withHour(9).withMinute(59),
                        givenMonday.withHour(12).withMinute(0),
                        // 목요일
                        givenThursday.withHour(9).withMinute(59),
                        givenThursday.withHour(21).withMinute(0),
                        // 그 외의 요일
                        givenTuesday.withHour(10).withMinute(0),
                        givenTuesday.withHour(10).withMinute(1),
                        givenTuesday.withHour(10).withMinute(30)
                );

                List<Screening> givenScreens() {
                    return 할인_조건에_맞지_않는_상영_시작_시간들.stream()
                            .map(상영시간 -> new Screening(givenMovie(), -1, 상영시간))
                            .collect(Collectors.toList());
                }

                @Test
                @DisplayName("할인되지 않은 금액을 리턴한다.")
                void it_returns_fee_does_not_discounted() {
                    for (Screening 할인되는_시간에_시작되는_상영 : givenScreens()) {
                        final Money 계산된_요금 = subject(할인되는_시간에_시작되는_상영);

                        assertThat(기본요금()).isEqualTo(계산된_요금);
                    }
                }
            }

            @Nested
            @DisplayName("상영 순번이 할인 조건에 맞는다면")
            class Context_with_valid_seq {
                final List<Integer> 지정된_상영_순번 = List.of(1, 10);

                List<Screening> givenScreening = 지정된_상영_순번.stream()
                        .map(seq -> new Screening(givenMovie(), seq, givenSundayAfternoon))
                        .collect(Collectors.toList());

                @Test
                @DisplayName("고정할인 금액만큼 할인된 금액을 리턴한다.")
                void it_returns_discounted_fee() {
                    for (Screening 할인되는_순번의_상영 : givenScreening) {
                        final Money 계산된_요금 = subject(할인되는_순번의_상영);

                        assertThat(기본요금().minus(givenFixedDiscountFee)).isEqualTo(계산된_요금);
                    }
                }
            }

            @Nested
            @DisplayName("상영 순번이 할인 조건에 맞지 않는다면")
            class Context_with_invalid_seq {
                final List<Integer> 지정되지_않은_상영_순번 = List.of(2, 9);

                List<Screening> givenScreening = 지정되지_않은_상영_순번.stream()
                        .map(seq -> new Screening(givenMovie(), seq, givenSundayAfternoon))
                        .collect(Collectors.toList());

                @Test
                @DisplayName("할인되지 않은 금액을 리턴한다.")
                void it_returns_fee_not_discounted() {
                    for (Screening 상영 : givenScreening) {
                        final Money 계산된_요금 = subject(상영);

                        assertThat(기본요금()).isEqualTo(계산된_요금);
                    }
                }
            }
        }

        @Nested
        @DisplayName("주어진 영화가 '타이타닉'일 때 (할인조건: 상영 시작 시간, 상영 순번 / 할인 금액: 퍼센트)")
        class Context_with_titanic extends TestCalculateMovieFee {

            @Override
            Movie givenMovie() {
                return given_타이타닉();
            }

            @Nested
            @DisplayName("상영 시작 시간이 할인 조건에 맞는다면")
            class Context_with_valid_period {
                final List<LocalDateTime> 지정된_기간_내의_시간들 = List.of(
                        // edge cases - 화요일
                        givenTuesday.withHour(14).withMinute(0),
                        givenTuesday.withHour(16).withMinute(59),
                        // inner cases - 화요일
                        givenTuesday.withHour(14).withMinute(1),
                        givenTuesday.withHour(16).withMinute(58),
                        // edge cases - 목요일
                        givenThursday.withHour(10).withMinute(0),
                        givenThursday.withHour(13).withMinute(59),
                        // inner cases - 목요일
                        givenThursday.withHour(10).withMinute(1),
                        givenThursday.withHour(13).withMinute(58)
                );

                List<Screening> givenScreens() {
                    return 지정된_기간_내의_시간들.stream()
                            .map(상영시간 -> new Screening(givenMovie(), 0, 상영시간))
                            .collect(Collectors.toList());
                }

                @Test
                @DisplayName("지정된 비율만큼 할인된 금액을 리턴한다")
                void it_returns_discounted_fee() {
                    for (Screening 할인되는_시간에_시작하는_상영 : givenScreens()) {
                        final Money 계산된_요금 = subject(할인되는_시간에_시작하는_상영);

                        assertThat(기본요금().times(1 - givenDiscountRate)).isEqualTo(계산된_요금);
                    }
                }
            }

            @Nested
            @DisplayName("상영 시작 시간이 할인 조건에 맞지 않는다면")
            class Context_with_invalid_period {
                final List<LocalDateTime> 할인_조건에_맞지_않는_상영_시작_시간들 = List.of(
                        // 월요일
                        givenMonday.withHour(9).withMinute(59),
                        givenMonday.withHour(12).withMinute(0),
                        // 목요일
                        givenThursday.withHour(9).withMinute(59),
                        givenThursday.withHour(21).withMinute(0),
                        // 그 외의 요일
                        givenTuesday.withHour(10).withMinute(0),
                        givenTuesday.withHour(10).withMinute(1),
                        givenTuesday.withHour(10).withMinute(30)
                );

                List<Screening> givenScreens() {
                    return 할인_조건에_맞지_않는_상영_시작_시간들.stream()
                            .map(상영시간 -> new Screening(givenMovie(), -1, 상영시간))
                            .collect(Collectors.toList());
                }

                @Test
                @DisplayName("할인되지 않는 금액을 리턴한다.")
                void it_returns_fee_not_discounted() {
                    for (Screening 할인_되는_시간에_시작하는_상영 : givenScreens()) {
                        final Money 계산된_금액 = subject(할인_되는_시간에_시작하는_상영);

                        assertThat(기본요금()).isEqualTo(계산된_금액);
                    }
                }
            }

            @Nested
            @DisplayName("상영 순번이 할인 조건에 맞는다면")
            class Context_with_valid_seq {
                final List<Integer> 지정된_상영_순번 = List.of(2);

                List<Screening> givenScreens() {
                    return 지정된_상영_순번.stream()
                            .map(seq -> new Screening(givenMovie(), seq, givenSundayAfternoon))
                            .collect(Collectors.toList());
                }

                @Test
                @DisplayName("지정된 비율만큼 할인된 금액을 리턴한다.")
                void it_returns_discounted_fee() {
                    for (Screening 할인되는_순번의_상영 : givenScreens()) {
                        final Money 계산된_금액 = subject(할인되는_순번의_상영);

                        assertThat(기본요금().times(1 - givenDiscountRate)).isEqualTo(계산된_금액);
                    }
                }
            }

            @Nested
            @DisplayName("상영 순번이 할인 조건에 맞지 않는다면")
            class Context_with_invalid_seq {
                final List<Integer> 지정된_상영_순번 = List.of(1, 3, 4, 5, 6, 7, 8, 9, 10);

                List<Screening> givenScreens() {
                    return 지정된_상영_순번.stream()
                            .map(seq -> new Screening(givenMovie(), seq, givenSundayAfternoon))
                            .collect(Collectors.toList());
                }

                @Test
                @DisplayName("할인되지 않은 금액을 리턴한다.")
                void it_returns_fee_not_discounted() {
                    for (Screening 상영 : givenScreens()) {
                        final Money 계산된_요금 = subject(상영);

                        assertThat(기본요금()).isEqualTo(계산된_요금);
                    }
                }
            }
        }

        @Nested
        @DisplayName("주어진 영화가 '스타워즈'일 때 (할인 조건 없음)")
        class Context_with_starwars extends TestCalculateMovieFee {

            @Override
            Movie givenMovie() {
                return given_스타워즈();
            }

            @Test
            @DisplayName("할인되지 않은 금액을 리턴한다.")
            void it_returns_fee_not_discounted() {
                final Screening 상영 = new Screening(givenMovie(), 0, givenSundayAfternoon);
                final Money 계산된_요금 = subject(상영);

                assertThat(기본요금()).isEqualTo(계산된_요금);
            }
        }
    }

    @Nested
    @DisplayName("changeDiscountPolicy 메소드는")
    class Describe_changeDiscountPolicy {

        @Nested
        @DisplayName("주어진 영화가 '스타워즈'일 때 (할인 조건 없음)")
        class Context_with_starwars {
            Movie givenMovie() {
                return given_스타워즈();
            }

            @Nested
            @DisplayName("'아바타'의 할인 정책이 주어지면")
            class Context_with_avatar_discount_policy {
                final DiscountPolicy givenDiscountRate = given_아바타_할인정책;

                @Test
                @DisplayName("주어진 할인 정책으로 할인 정책을 교체하고 void를 리턴한다.")
                void it_changes_discount_policy() {
                    final Movie 스타워즈 = givenMovie();
                    final Money 기본_요금 = 스타워즈.getFee();
                    스타워즈.changeDiscountPolicy(givenDiscountRate);

                    {
                        /* 변경된 할인 정책으로 할인이 되는지 확인한다. */
                        final int 아바타_할인_조건_순번 = 1;
                        final Screening 상영 = new Screening(스타워즈, 아바타_할인_조건_순번, givenSundayAfternoon);
                        final Money 계산된_요금 = 스타워즈.calculateMovieFee(상영);

                        assertThat(기본_요금.minus(givenFixedDiscountFee)).as("할인되지 않는 스타워즈의 요금이 아바타의 정책으로 할인된다")
                                .isEqualTo(계산된_요금);
                    }
                }
            }

            @Nested
            @DisplayName("'타이타닉'의 할인정책이 주어지면")
            class Context_with_starwars_policy {
                final DiscountPolicy givenDiscountPolicy = given_타이타닉_할인정책;

                @Test
                @DisplayName("주어진 할인 정책으로 할인정책을 교체하고 void를 리턴한다")
                void it_changes_the_discount_policy() {
                    final Movie movie = givenMovie();
                    final Money 기본_요금 = movie.getFee();
                    movie.changeDiscountPolicy(givenDiscountPolicy);

                    {
                        /* 변경된 할인 정책으로 할인이 되는지 확인한다. */
                        final int 타이타닉_할인_조건_순번 = 2;
                        final Screening 상영 = new Screening(movie, 타이타닉_할인_조건_순번, givenSundayAfternoon);
                        final Money 계산된_요금 = movie.calculateMovieFee(상영);

                        assertThat(기본_요금.times(1 - givenDiscountRate)).as("할인되지 않는 스타워즈의 요금이 타이타닉의 정책으로 할인된다")
                                .isEqualTo(계산된_요금);
                    }
                }
            }
        }
    }
}