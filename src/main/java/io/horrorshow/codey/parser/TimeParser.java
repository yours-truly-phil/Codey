package io.horrorshow.codey.parser;

import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TimeParser {
    public static OffsetDateTime toOffsetDateTime(String group) {
        if(!startsWithWeekDay(group.toUpperCase(Locale.ROOT))) {
            if(group.startsWith(" "))
                group = group.replaceFirst("\\s+", "");

            LocalTime time = stripRegion(group);
            String region = stripTime(group);

            return OffsetDateTime.now(ZoneId.of(region)).withHour(time.getHour()).withMinute(time.getMinute());
        } else
        {
            DayOfWeek weekOfDay = getWeekOfDay(group.toUpperCase(Locale.ROOT));
            LocalTime time = getTime(group);
            String region = getRegion(group);

            return OffsetDateTime.now(ZoneId.of(region)).withHour(time.getHour()).withMinute(time.getMinute()).with(ChronoField.DAY_OF_WEEK, weekOfDay.getValue());
        }
    }

    private static LocalTime getTime(String group) {
        String time = group.split("\\s+")[1];
        int[] timeParts = Arrays.stream(time.split(":")).mapToInt(Integer::parseInt).toArray();
        return LocalTime.of(timeParts[0], timeParts[1]);
    }

    private static String getRegion(String group) {
        return fixRegion(group.split("\\s+")[2]);
    }

    private static DayOfWeek getWeekOfDay(String group) {
        return DayOfWeek.valueOf(group.split("\\s+")[0]);
    }

    private static final List<String> weekDays = Arrays.stream(new DateFormatSymbols().getWeekdays()).map(s -> s.toUpperCase(Locale.ROOT)).toList();

    private static boolean startsWithWeekDay(String group)
    {
        return weekDays.contains(group.split("\\s+")[0]);
    }

    private static String stripTime(String group) {
        String[] data = group.split("\\s+");

        if(data.length == 1)
            return data[0];

        return fixRegion(String.join(" ", Arrays.copyOfRange(data, 1, data.length)));
    }


    private static LocalTime stripRegion(String group) {
        String time = group.split("\\s+")[0];
        int[] timeParts = Arrays.stream(time.split(":")).mapToInt(Integer::parseInt).toArray();
        return LocalTime.of(timeParts[0], timeParts[1]);
    }

    private static String fixRegion(String region)
    {
        String result = region;
        if(region.contains("/")) {
            String[] parts = result.split("/");
            result = capitalize(parts[0]) + "/" + capitalize(parts[1]);
        }
        else
            result = result.toUpperCase(Locale.ROOT);
        return result;
    }

    static String capitalize(String str) {
        if(str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
