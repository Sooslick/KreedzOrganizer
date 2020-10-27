package ru.sooslick.bhop;

import org.bukkit.Location;

public class BhopCheckpoint {

    private final String name;
    private final Location loadLocation;
    private final Location triggerLocation;
    private final TriggerType triggerType;

    public BhopCheckpoint(String name, Location loadLocation, Location triggerLocation, TriggerType triggerType) {
        this.name = name;
        this.loadLocation = loadLocation;
        this.triggerLocation = triggerLocation;
        this.triggerType = triggerType;
    }

    public Location getLoadLocation() {
        return loadLocation;
    }

    public String getName() {
        return name;
    }

}
