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
    private Location bound1;
    private Location bound2;
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
        bound1 = l1;
        bound2 = l2;
    }

    //todo mne ne nrav get bound 1 + 2...
    public Location getBound1() {
        return bound1;
    }

    public Location getBound2() {
        return bound2;
    }

    public World getWorld() {
        return bound1.getWorld();
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

    public boolean isChanged() {
        return edit;
    }

    public boolean isInside(Location l) {
        return BhopUtil.isInside(l, bound1, bound2);
    }
}
