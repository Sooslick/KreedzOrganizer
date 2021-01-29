package ru.sooslick.bhop;

import org.jetbrains.annotations.NotNull;

public class BhopRecord implements Comparable<Object> {

    private static final String HTEMPLATE = "%d:%02d:%02d";
    private static final String MTEMPLATE = "%d:%02d";

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
        int h = playerTime / 3600;
        if (h > 0) {
            return String.format(
                    HTEMPLATE,
                    playerTime / 3600,
                    (playerTime % 3600) / 60,
                    playerTime % 60);
        } else {
            return String.format(
                    MTEMPLATE,
                    (playerTime % 3600) / 60,
                    playerTime % 60);
        }
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
