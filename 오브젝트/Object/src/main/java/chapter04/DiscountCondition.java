package chapter04;

import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Getter;

public class DiscountCondition {

    @Getter
    private DiscountConditionType type;

    private int sequence;

    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public DiscountCondition(DiscountConditionType type, int sequence) {
        this.type = type;
        this.sequence = sequence;
    }

    public DiscountCondition(
            DiscountConditionType type,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        this.type = type;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DiscountCondition(
            DiscountConditionType type,
            int sequence,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        this.type = type;
        this.sequence = sequence;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isDiscountable(DayOfWeek dayOfWeek, LocalTime time) {
        if (type != DiscountConditionType.PERIOD) {
            throw new IllegalArgumentException();
        }
        return this.dayOfWeek.equals(dayOfWeek)
                && !this.startTime.isAfter(time)
                && !this.endTime.isBefore(time);
    }

    public boolean isDiscountable(int sequence) {
        if (type != DiscountConditionType.SEQUENCE) {
            throw new IllegalArgumentException();
        }
        return this.sequence == sequence;
    }
}
