package ru.sooslick.bhop;

public class BhopRecord {

    private final String playerName;
    private final int playerTime;

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
}
