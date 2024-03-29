package ru.sooslick.bhop;

import org.jetbrains.annotations.NotNull;
import ru.sooslick.bhop.util.CommonUtil;

public class BhopRecord implements Comparable<Object> {
    private final String playerName;
    private int playerTime;

    public BhopRecord(String name, int time) {
        playerName = name;
        playerTime = time;
    }

    public String getName() {
        return playerName;
    }

    public int getTime() {
        return playerTime;
    }

    public String formatTime() {
        return CommonUtil.formatDuration(playerTime);
    }

    public void setTime(int newTime) {
        playerTime = newTime;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (!(o instanceof BhopRecord))
            return 0;
        return Integer.compare(playerTime, ((BhopRecord) o).getTime());
    }
}
