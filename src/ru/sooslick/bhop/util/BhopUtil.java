package ru.sooslick.bhop.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ru.sooslick.bhop.Engine;

import java.util.Arrays;

public class BhopUtil {

    public static final double MAXD = 100500d;
    private static final String COMMA = ",";

    public static Location stringToLocation(String s) {
        String[] coords = s.split(COMMA);
        //try to get world
        World world = Bukkit.getWorld(coords[0]);
        if (world != null)
            coords = Arrays.copyOfRange(coords, 1, coords.length);
        try {
            if (coords.length == 3)
                return new Location(world,
                        Double.parseDouble(coords[0]),
                        Double.parseDouble(coords[1]),
                        Double.parseDouble(coords[2]));
            else if (coords.length == 4)
                return new Location(world,
                        Double.parseDouble(coords[0]),
                        Double.parseDouble(coords[1]),
                        Double.parseDouble(coords[2]),
                        Float.parseFloat(coords[3]),
                        0);
            else if (coords.length == 5)
                return new Location(world,
                        Double.parseDouble(coords[0]),
                        Double.parseDouble(coords[1]),
                        Double.parseDouble(coords[2]),
                        Float.parseFloat(coords[3]),
                        Float.parseFloat(coords[4]));
            else {
                Engine.LOG.warning("stringToLocation: wrong Location format: " + s);
                return null;
            }
        } catch (Exception e) {
            Engine.LOG.warning("stringToLocation: wrong Location format: " + s);
            return null;
        }
    }

    public static Location stringToLocation(World w, String s) {
        Location l = stringToLocation(s);
        if (l == null)
            return null;
        l.setWorld(w);
        return l;
    }

    public static String locationToString(Location l) {
        return l.getX() + COMMA + l.getY() + COMMA + l.getZ() + COMMA
             + l.getYaw() + COMMA + l.getPitch();
    }

    public static String join(String delimiter, String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            sb.append(args[i]);
            if (i + 1 < args.length) sb.append(delimiter);
        }
        return sb.toString();
    }

    public static boolean isInside(Location l, Location bound1, Location bound2) {
        return (distanceBetween(l, bound1, bound2) == 0);
    }

    public static double distance(Location a, Location b) {
        if (a.getWorld() != b.getWorld())
            return MAXD;
        return diag(
                a.getX() - b.getX(),
                a.getY() - b.getY(),
                a.getZ() - b.getZ());
    }

    public static double distanceBetween(Location l, Location bound1, Location bound2) {
        if (l.getWorld() != bound1.getWorld())
            return MAXD;
        return diag(
                distanceBetween(l.getX(), bound1.getX(), bound2.getX()),
                distanceBetween(l.getY(), bound1.getY(), bound2.getY()),
                distanceBetween(l.getZ(), bound1.getZ(), bound2.getZ()));
    }

    public static double distanceBetween(double src, double x1, double x2) {
        if (x2 < x1) {
            double temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (src < x1)
            return x1 - src;
        else if (src > x2)
            return src - x2;
        else return 0;
    }

    public static double diag(double dx, double dy, double dz) {
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
