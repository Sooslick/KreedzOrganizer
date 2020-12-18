package ru.sooslick.bhop;

public class BhopRecord implements Comparable {

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

    public void setTime(int newTime) {
        playerTime = newTime;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null)
            return 0;
        if (!(o instanceof BhopRecord))
            return 0;
        return Integer.compare(playerTime, ((BhopRecord) o).getTime());
    }
}
