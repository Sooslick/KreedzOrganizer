package ru.sooslick.bhop;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class BhopLevel {

    private final String name;
    private final List<BhopCheckpoint> checkpoints;
    private final List<BhopRecord> records;
    //private ... region
    private BhopRegion bounds;
    private Location start;
    private Location finish;
    private TriggerType triggerType;
    private boolean edit;

    //todo: worldguard region and other data

    public BhopLevel(String name) {
        this.name = name;
        checkpoints = new ArrayList<>();
        records = new ArrayList<>();
        edit = false;
    }

    public void setBounds(Location l1, Location l2) {
        bounds = new BhopRegion(l1, l2);
    }

    public BhopRegion getBhopRegion() {
        return bounds;
    }

    public World getWorld() {
        return bounds.getBound1().getWorld();
    }

    public void setStart(Location start) {
        this.start = start;
    }

    public Location getStartPosition() {
        return start;
    }

    public void setFinish(Location finish) {
        this.finish = finish;
    }

    public Location getFinish() {
        return finish;
    }

    public void setTriggerType(TriggerType type) {
        this.triggerType = type;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public String getName() {
        return name;
    }

    public void addCheckpoint(BhopCheckpoint bhopCheckpoint) {
        checkpoints.add(bhopCheckpoint);
    }

    public List<BhopCheckpoint> getCheckpoints() {
        return checkpoints;
    }

    public BhopCheckpoint getCheckpoint(String name) {
        return checkpoints.stream().filter(cp -> cp.getName().equals(name)).findFirst().orElse(null);
    }

    public void addRecord(BhopRecord rec) {
        records.add(rec);
    }

    public List<BhopRecord> getRecords() {
        return records;
    }

    public BhopRecord getPlayerRecord(String playerName) {
        return records.stream().filter(r -> r.getName().equals(playerName)).findFirst().orElse(null);
    }

    public BhopRecord getLevelRecord() {
        return records.stream().sorted().findFirst().orElse(null);
    }

    public boolean isChanged() {
        return edit;
    }

    public boolean isInside(Location l) {
        return BhopUtil.isInside(l, bounds.getBound1(), bounds.getBound2());
    }

    public double distanceToLevel(Location l) {
        return BhopUtil.distanceBetween(l, bounds.getBound1(), bounds.getBound2());
    }
}
