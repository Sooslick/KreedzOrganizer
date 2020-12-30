package ru.sooslick.bhop.region;

import org.bukkit.Location;
import ru.sooslick.bhop.util.BhopUtil;

public class DefaultBhopRegion implements BhopRegion {
    private final Location bound1;
    private final Location bound2;

    public DefaultBhopRegion(Location l1, Location l2) {
        bound1 = l1;
        bound2 = l2;
    }

    public Location getBound1() {
        return bound1;
    }

    public Location getBound2() {
        return bound2;
    }

    public boolean isInside(Location l) {
        return BhopUtil.isInside(l, bound1, bound2);
    }

    public BhopRegion getCopy() {
        return new DefaultBhopRegion(bound1.clone(), bound2.clone());
    }
}
