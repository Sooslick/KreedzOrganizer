package ru.sooslick.bhop.region;

import org.bukkit.Location;

public interface BhopRegion {
    Location getBound1();
    Location getBound2();
    boolean isInside(Location l);
}
