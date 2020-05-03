package ru.sooslick.bhop;

import org.bukkit.Location;

public class BhopCheckpoint {

    private final Location location;
    private final String name;

    public BhopCheckpoint(Location location, String name) {
        this.location = location;
        this.name = name;
    }

    public Location getLocation() {
        return location ;
    }

    public String getName() {
        return name;
    }

}
