package ru.sooslick.bhop.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ru.sooslick.bhop.exception.WorldGuardException;

public class WorldGuardRegion implements BhopRegion {
    ProtectedRegion region;
    World world;

    //todo adequate Exceptions and messages + logging
    public WorldGuardRegion(World w, String rgName) throws Exception {
        world = w;
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(w));
            if (manager == null)
                throw new WorldGuardException();
            ProtectedRegion rg = manager.getRegion(rgName);
            if (rg == null)
                throw new Exception("Region not found");
            if (rg instanceof GlobalProtectedRegion)
                throw new Exception("Global region is not applicable");
            if (rg instanceof ProtectedPolygonalRegion)
                Bukkit.getLogger().warning("Polygonal region may affect performance");
            region = rg;
        } catch (NoClassDefFoundError e) {
            throw new WorldGuardException();
        }
    }

    public Location getBound1() {
        return BukkitAdapter.adapt(world, region.getMinimumPoint());
    }

    public Location getBound2() {
        return BukkitAdapter.adapt(world, region.getMaximumPoint());
    }

    public boolean isInside(Location l) {
        return region.contains(BukkitAdapter.asBlockVector(l));
    }

    public String getName() {
        return region.getId();
    }

    public BhopRegion getCopy() {
        try {
            return new WorldGuardRegion(world, region.getId());
        } catch (Exception e) {
            return new DefaultBhopRegion(getBound1(), getBound2());
        }
    }
}
