package ru.sooslick.bhop;

import org.bukkit.Location;
import org.bukkit.World;

public class BhopUtil {

    //todo - rm world from parameters
    public static Location stringToLocation(World w, String s) {
        String[] coords = s.split(",");
        return new Location(w, Double.parseDouble(coords[0]),
                               Double.parseDouble(coords[1]),
                               Double.parseDouble(coords[2]));
    }

}
