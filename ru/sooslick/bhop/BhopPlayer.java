package ru.sooslick.bhop;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BhopPlayer {

    private Player player;
    private BhopLevel level;
    public int timer;
    private List<BhopCheckpoint> checkpoints;

    public BhopPlayer(Player p, BhopLevel bl) {
        player = p;
        level = bl;
        timer = 0;
        checkpoints = new ArrayList<>();
    }

    public Player getPlayer() {
        return player;
    }

    public BhopLevel getLevel() { return level; }

    public void addCheckpoint(BhopCheckpoint cp) {
        checkpoints.add(cp);
    }

}
