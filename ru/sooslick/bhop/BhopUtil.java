package ru.sooslick.bhop;

import org.bukkit.Location;
import org.bukkit.World;

public class BhopUtil {

    private static final String COMMA = ",";

    //todo - rm world from parameters
    public static Location stringToLocation(World w, String s) {
        String[] coords = s.split(COMMA);
        return new Location(w, Double.parseDouble(coords[0]),
                               Double.parseDouble(coords[1]),
                               Double.parseDouble(coords[2]));
    }

    public static String locationToString(Location l) {
        return l.getBlockX() + COMMA + l.getBlockY() + COMMA + l.getBlockZ();
    }

    public static boolean isInside(Location l, Location bound1, Location bound2) {
        double x1 = bound1.getX();
        double x2 = bound2.getX();
        double y1 = bound1.getY();
        double y2 = bound2.getY();
        double z1 = bound1.getZ();
        double z2 = bound2.getZ();
        if (x2 < x1) swap(x1, x2);
        if (y2 < y1) swap(y1, y2);
        if (z2 < z1) swap(z1, z2);
        return (l.getX() >= x1 && l.getX() <= x2 &&
                l.getY() >= y1 && l.getY() <= y2 &&
                l.getZ() >= z1 && l.getZ() <= z2);
    }

    public static void swap(Object a, Object b) {
        Object temp = a;
        a = b;
        b = temp;
    }
}
