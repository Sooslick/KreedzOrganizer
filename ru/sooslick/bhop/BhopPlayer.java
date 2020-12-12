package ru.sooslick.bhop;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BhopPlayer {

    private Player player;
    private BhopLevel level;
    private int timer;
    private List<BhopCheckpoint> checkpoints;
    private Location comeback;
    private Location dcLocation;
    private int fleeTimer;
    private boolean cheats;

    public BhopPlayer(Player p, BhopLevel bl) {
        player = p;
        level = bl;
        timer = 0;
        fleeTimer = 0;
        checkpoints = new ArrayList<>();
        comeback = player.getLocation();
        dcLocation = level.getStartPosition();
        cheats = false;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player p) {
        player = p;
    }

    public BhopLevel getLevel() { return level; }

    public List<BhopCheckpoint> getCheckpointsList() {
        return checkpoints;
    }

    public void addCheckpoint(BhopCheckpoint cp) {
        checkpoints.add(cp);
    }

    public String getCheckpoints() {
        StringBuilder sb = new StringBuilder();
        checkpoints.forEach(cp -> sb.append(cp.getName()).append(", "));
        return sb.toString();
    }

    public Location getComebackLocation() {
        return comeback;
    }

    public Location getDcLocation() {
        return dcLocation;
    }

    public void setDcLocation(Location l) {
        dcLocation = l;
    }

    public int getTimer() {
        return timer;
    }

    public void enableCheats() {
        cheats = true;
        player.sendMessage("Bhop cheats enabled");
    }

    public boolean isCheated() {
        return cheats;
    }

    public void tick() {
        timer++;
        if (level.isInside(player.getLocation())) {
            if (fleeTimer > 0) {
                fleeTimer = 0;
                player.sendMessage("Welcome back");
            }
        } else {
            if (fleeTimer++ == 1) {
                player.sendMessage("You are outside of level. Let's come back!");
            }
            if (fleeTimer > 5) {
                player.sendMessage(15 - fleeTimer + " seconds to exit");
                if (fleeTimer > 15) {
                    Engine.getInstance().playerExitEvent(this);
                }
            }
        }
    }
}
