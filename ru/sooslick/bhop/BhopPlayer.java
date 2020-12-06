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
    private int fleeTimer;

    public BhopPlayer(Player p, BhopLevel bl) {
        player = p;
        level = bl;
        timer = 0;
        fleeTimer = 0;
        checkpoints = new ArrayList<>();
        comeback = player.getLocation();
    }

    public Player getPlayer() {
        return player;
    }

    public BhopLevel getLevel() { return level; }

    public List<BhopCheckpoint> getCheckpoints() {
        return checkpoints;
    }

    public void addCheckpoint(BhopCheckpoint cp) {
        checkpoints.add(cp);
    }

    public Location getComebackLocation() {
        return comeback;
    }

    public void tick() {
        timer++;
        if (BhopUtil.isInside(player.getLocation(), level.getBound1(), level.getBound2())) {
            if (fleeTimer > 0) {
                fleeTimer = 0;
                player.sendMessage("welcome back"); //todo textovka
            }
        } else {
            if (fleeTimer++ == 1) {
                player.sendMessage("You are outside BhopLevel. Return back plz");   //todo norm textovka
            }
            if (fleeTimer > 5) {
                player.sendMessage(15 - fleeTimer + " seconds to exit");    //todo norm textovka
                if (fleeTimer > 15) {
                    Engine.getInstance().playerExitEvent(this);
                }
            }
        }
    }
}
