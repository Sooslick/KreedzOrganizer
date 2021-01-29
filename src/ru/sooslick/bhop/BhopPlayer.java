package ru.sooslick.bhop;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BhopPlayer {

    private Player player;
    private BhopLevel level;
    private int timer;
    private final Set<BhopCheckpoint> checkpoints;
    private final Location comeback;
    private Location dcLocation;
    private int fleeTimer;
    private boolean cheats;

    public BhopPlayer(Player p, BhopLevel bl) {
        player = p;
        level = bl;
        timer = 0;
        fleeTimer = 0;
        checkpoints = new LinkedHashSet<>();
        comeback = player.getLocation();
        dcLocation = level.getStartPosition();
        cheats = false;
    }

    public void restart(BhopLevel bl) {
        level = bl;
        timer = 0;
        fleeTimer = 0;
        checkpoints.clear();
        dcLocation = level.getStartPosition();
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player p) {
        player = p;
    }

    public BhopLevel getLevel() { return level; }

    public BhopCheckpoint getCheckpoint(String name) {
        return checkpoints.stream().filter(cp -> cp.getName().equals(name)).findFirst().orElse(null);
    }

    public Set<BhopCheckpoint> getCheckpointsSet() {
        return checkpoints;
    }

    public boolean addCheckpoint(BhopCheckpoint cp) {
        return checkpoints.add(cp);
    }

    public String formatCheckpoints() {
        return checkpoints.stream().map(BhopCheckpoint::getName).collect(Collectors.joining());
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
        player.sendMessage("§cBhop cheats enabled");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isCheated() {
        return cheats;
    }

    public void tick() {
        timer++;
        if (level.isInside(player.getLocation())) {
            if (fleeTimer > 0) {
                if (fleeTimer > 5)
                    player.sendMessage("§eWelcome back");
                fleeTimer = 0;
            }
        } else {
            if (fleeTimer++ == 1) {
                player.sendMessage("§cYou are outside of level. Let's come back!");
            }
            if (fleeTimer % 5 == 0) {
                player.sendMessage("§c" + (15 - fleeTimer) + " seconds to exit");
                if (fleeTimer >= 15) {
                    Engine.getInstance().playerExitEvent(this);
                }
            }
        }
    }
}
