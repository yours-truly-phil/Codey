package io.horrorshow.codey.parser;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.Locale;

public class TimeParser {
    public static OffsetDateTime toOffsetDateTime(String group) {
        var parts = group.split("\\s+");
        var hasDay = parts.length == 3;
        var HHmm = parts[hasDay ? 1 : 0].split(":");
        var region = parts[hasDay ? 2 : 1];

        var offsetDateTime = OffsetDateTime.now(ZoneId.of(region))
                .withHour(Integer.parseInt(HHmm[0]))
                .withMinute(Integer.parseInt(HHmm[1]));
        if (hasDay) {
            var dayOfTheWeek = DayOfWeek.valueOf(parts[0].toUpperCase(Locale.ROOT));
            offsetDateTime = offsetDateTime.with(ChronoField.DAY_OF_WEEK, dayOfTheWeek.getValue());
        }
        return offsetDateTime;
    }
}
