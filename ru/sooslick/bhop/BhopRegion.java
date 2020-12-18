package ru.sooslick.bhop;

import org.bukkit.Location;

public class BhopRegion {
    private Location bound1;
    private Location bound2;

    public BhopRegion(Location l1, Location l2) {
        bound1 = l1;
        bound2 = l2;
    }

    public Location getBound1() {
        return bound1;
    }

    public Location getBound2() {
        return bound2;
    }
}
