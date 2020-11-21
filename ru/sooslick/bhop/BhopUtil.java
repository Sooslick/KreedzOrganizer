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

}
