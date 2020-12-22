package ru.sooslick.bhop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ru.sooslick.bhop.exception.WorldGuardException;
import ru.sooslick.bhop.region.BhopRegion;
import ru.sooslick.bhop.region.DefaultBhopRegion;
import ru.sooslick.bhop.region.WorldGuardRegion;
import ru.sooslick.bhop.util.BhopUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BhopLevel {

    private final String name;
    private final List<BhopCheckpoint> checkpoints;
    private final List<BhopRecord> records;
    private BhopRegion bounds;
    private Location start;
    private Location finish;
    private TriggerType triggerType;
    private boolean edit;

    public BhopLevel(String name) {
        this.name = name;
        checkpoints = new ArrayList<>();
        records = new ArrayList<>();
        edit = false;
    }

    public File getFile() {
        return new File(Engine.LEVELS_PATH + name + Engine.YAML_EXTENSION);
    }

    // todo awful method. Just rework
    public boolean setRegion(World world, String rgName) throws WorldGuardException {
        //try to assign WG region
        try {
            bounds = new WorldGuardRegion(world, rgName);
            return true;
        } catch (WorldGuardException e) {
            // class def error - world guard not exists or not loaded
            throw e;
        } catch (Exception e) {
            // another exceptions, especially rg not found
            Bukkit.getLogger().warning("Cannot assign region to bhop level " + name + ", region - " + rgName
            + "\n" + e.getMessage());
            return false;
        }
    }

    public void setBounds(Location l1, Location l2) {
        bounds = new DefaultBhopRegion(l1, l2);
    }

    private void setBhopRegion(BhopRegion rg) {
        bounds = rg;
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

    public void setChanged() {
        edit = true;
    }

    public boolean isChanged() {
        return edit;
    }

    public boolean isInside(Location l) {
        return bounds.isInside(l);
    }

    public double distanceToLevel(Location l) {
        return BhopUtil.distanceBetween(l, bounds.getBound1(), bounds.getBound2());
    }

    @Override
    public BhopLevel clone() {
        BhopLevel copy = new BhopLevel(name);
        copy.setBhopRegion(bounds.getCopy());   //weird wg.
        copy.setStart(start.clone());
        copy.setFinish(finish.clone());
        copy.setTriggerType(triggerType);
        checkpoints.forEach(cp -> copy.addCheckpoint(cp.clone()));
        records.forEach(copy::addRecord);   //immutable?
        return copy;
    }
}
