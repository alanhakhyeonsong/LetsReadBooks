package chapter04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"InnerClassMayBeStatic", "NonAsciiCharacters"})
@DisplayName("DiscountCondition class")
class DiscountConditionTest {

    private final LocalTime givenStartDiscountTime = LocalTime.of(9, 30);
    private final LocalTime givenEndDiscountTime = LocalTime.of(11, 30);
    private final DayOfWeek givenDiscountableDayOfWeek = DayOfWeek.TUESDAY;
    private final int givenDiscountableSequence = 42;
    private final int givenUndiscountableSequence = givenDiscountableSequence + 1;

    private final List<DayOfWeek> givenAllDayOfWeek = List.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
    );

    private final List<DayOfWeek> givenAllUndiscountableDayOfWeek = givenAllDayOfWeek.stream()
            .filter(dayOfWeek -> dayOfWeek != givenDiscountableDayOfWeek)
            .collect(Collectors.toList());

    abstract class TestDiscountCondition {
        abstract DiscountConditionType givenType();

        DiscountCondition givenDiscountCondition() {
            return new DiscountCondition(
                    givenType(),
                    givenDiscountableSequence,
                    givenDiscountableDayOfWeek,
                    givenStartDiscountTime,
                    givenEndDiscountTime
            );
        }

        boolean subject(DayOfWeek dayOfWeek, LocalTime time) {
            return givenDiscountCondition().isDiscountable(dayOfWeek, time);
        }

        boolean subject(int sequence) {
            return givenDiscountCondition().isDiscountable(sequence);
        }
    }

    @Nested
    @DisplayName("isDiscountable 메소드는")
    class Describe_isDiscountable {

        @Nested
        @DisplayName("DiscountCondition이 PERIOD일 때")
        class Context_with_period_type extends TestDiscountCondition {

            @Override
            DiscountConditionType givenType() {
                return DiscountConditionType.PERIOD;
            }

            @Nested
            @DisplayName("할인 조건에 맞는 시간이 주어지고, 할인 조건에 맞는 요일이 주어지면")
            class Context_with_valid_time_and_valid_dayOfWeek {
                final List<LocalTime> given_할인_조건에_맞는_시간 = List.of(
                        givenStartDiscountTime,
                        givenStartDiscountTime.plusSeconds(1),
                        givenEndDiscountTime.minusSeconds(1),
                        givenEndDiscountTime
                );

                final DayOfWeek given_할인_조건에_맞는_요일 = givenDiscountableDayOfWeek;

                @Test
                @DisplayName("true를 리턴한다.")
                void it_returns_true() {
                    for (LocalTime givenTime : given_할인_조건에_맞는_시간) {
                        assertThat(subject(given_할인_조건에_맞는_요일, givenTime)).isTrue();
                    }
                }
            }

            @Nested
            @DisplayName("할인 조건에 할인 조건에 맞지 않는 요일이 주어지면")
            class Context_with_invalid_dayOfWeek {
                final List<LocalTime> givenTime = List.of(
                        givenStartDiscountTime.minusMinutes(1),
                        givenStartDiscountTime,
                        givenStartDiscountTime.plusSeconds(1),
                        givenEndDiscountTime.minusSeconds(1),
                        givenEndDiscountTime,
                        givenEndDiscountTime.plusMinutes(1)
                );

                final List<DayOfWeek> givenDayOfWeek = givenAllUndiscountableDayOfWeek;

                @Test
                @DisplayName("false를 리턴한다.")
                void it_returns_false() {
                    for (LocalTime time : givenTime) {
                        for (DayOfWeek 할인_조건에_맞지_않는_요일 : givenDayOfWeek) {
                            assertThat(subject(할인_조건에_맞지_않는_요일,time))
                                    .as("요일이 맞지 않으면 시간에 관계 없이 false를 리턴한다.")
                                    .isFalse();
                        }
                    }
                }
            }

            @Nested
            @DisplayName("할인 조건에 맞지 않는 시간이 주어지면")
            class Context_with_invalid_time {
                List<LocalTime> givenTime = List.of(
                        givenStartDiscountTime.minusMinutes(1),
                        givenEndDiscountTime.plusSeconds(1)
                );

                @Test
                @DisplayName("false를 리턴한다.")
                void it_returns_false() {
                    for (LocalTime time : givenTime) {
                        for (DayOfWeek dayOfWeek : givenAllDayOfWeek) {
                            assertThat(subject(dayOfWeek, time))
                                    .as("시간이 맞지 않으면 요일에 관계 없이 false를 리턴한다.")
                                    .isFalse();
                        }
                    }
                }
            }

            @Nested
            @DisplayName("순번이 주어지면")
            class Context_with_sequence {
                List<Integer> givenSequence = List.of(
                        givenDiscountableSequence,
                        givenUndiscountableSequence
                );

                @Test
                @DisplayName("예외가 발생한다.")
                void it_throws_exception() {
                    for (Integer sequence : givenSequence) {
                        assertThatThrownBy(() -> subject(sequence))
                                .isInstanceOf(IllegalArgumentException.class);
                    }
                }
            }
        }

        @Nested
        @DisplayName("DiscountCondition이 SEQUENCE일 때")
        class Context_with_sequence extends TestDiscountCondition {

            @Override
            DiscountConditionType givenType() {
                return DiscountConditionType.SEQUENCE;
            }

            @Nested
            @DisplayName("할인 조건에 맞는 순번이 주어지면")
            class Context_with_valid_sequence {
                final int givenSequence = givenDiscountableSequence;

                @Test
                @DisplayName("true를 리턴한다.")
                void it_returns_true() {
                    assertThat(subject(givenSequence)).isTrue();
                }
            }

            @Nested
            @DisplayName("할인 조건에 맞지 않는 순번이 주어지면")
            class Context_with_invalid_sequence {
                final int givenSequence = givenUndiscountableSequence;

                @Test
                @DisplayName("false를 리턴한다.")
                void it_returns_false() {
                    assertThat(subject(givenSequence)).isFalse();
                }
            }

            @Nested
            @DisplayName("요일과 시간이 주어지면")
            class Context_with_DayOfWeek_and_LocalTime {
                final List<DayOfWeek> givenDayOfWeek = givenAllDayOfWeek;
                final List<LocalTime> givenTime = List.of(
                        givenStartDiscountTime.minusMinutes(1),
                        givenStartDiscountTime,
                        givenStartDiscountTime.plusMinutes(1),
                        givenEndDiscountTime.minusMinutes(1),
                        givenEndDiscountTime,
                        givenEndDiscountTime.plusMinutes(1)
                );

                @Test
                @DisplayName("예외가 발생한다.")
                void it_throws_exception() {
                    for (DayOfWeek dayOfWeek : givenDayOfWeek) {
                        for (LocalTime time : givenTime) {
                            assertThatThrownBy(() -> subject(dayOfWeek, time))
                                    .isInstanceOf(IllegalArgumentException.class);
                        }
                    }
                }
            }
        }
    }
}