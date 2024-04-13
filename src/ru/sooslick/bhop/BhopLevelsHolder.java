package ru.sooslick.bhop;

import org.bukkit.Location;
import ru.sooslick.bhop.util.BhopUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BhopLevelsHolder {

    private static final List<BhopLevel> levels = new LinkedList<>();

    public static BhopLevel getBhopLevel(String name) {
        return levels.stream()
                .filter(level -> level.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static String getBhopLevelNames() {
        return levels.stream()
                .map(BhopLevel::getName)
                .collect(Collectors.joining(", "));
    }

    public static List<String> getBhopLevelNamesList() {
        return levels.stream()
                .map(BhopLevel::getName)
                .collect(Collectors.toList());
    }

    public static List<BhopLevel> getBhopLevelList() {
        return new LinkedList<>(levels);
    }

    public static void updateLevel(BhopLevel updatedLevel) {
        BhopLevel old = getBhopLevel(updatedLevel.getName());
        if (old != null)
            levels.remove(old);
        levels.add(updatedLevel);
    }

    public static boolean removeLevel(BhopLevel rem) {
        return levels.remove(rem);
    }

    public static double calcDistanceToNearestLevel(Location l) {
        return levels.stream()
                .map(level -> level.distanceToLevel(l))
                .min(Double::compareTo)
                .orElse(BhopUtil.MAXD);
    }

    public static int getLevelsNumber() {
        return levels.size();
    }
}
