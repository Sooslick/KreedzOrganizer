package ru.sooslick.bhop.util;

import java.time.Duration;

public class CommonUtil {
    private static final String DURATION_DEFAULT = "%d:%02d.%02d";
    private static final String DURATION_HOURS = "%d:%02d:%02d.%02d";

    public static String formatDuration(long ticksTotal) {
        long ticks = ticksTotal % 20;
        Duration duration = Duration.ofSeconds(ticksTotal / 20);
        long seconds = duration.getSeconds();
        long h = seconds / 3600;
        if (h > 0) {
            return String.format(
                    DURATION_HOURS,
                    seconds / 3600,
                    (seconds % 3600) / 60,
                    seconds % 60,
                    ticks);
        } else {
            return String.format(
                    DURATION_DEFAULT,
                    (seconds % 3600) / 60,
                    seconds % 60,
                    ticks);
        }
    }
}
