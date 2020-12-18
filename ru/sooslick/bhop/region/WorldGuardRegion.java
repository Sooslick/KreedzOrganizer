package ru.sooslick.bhop.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;
import ru.sooslick.bhop.exception.WorldGuardException;

public class WorldGuardRegion implements BhopRegion {
    ProtectedCuboidRegion region;
    World world;

    //todo adequate Exceptions and messages + logging
    public WorldGuardRegion(World w, String rgName) throws Exception {
        world = w;
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(w));
            ProtectedRegion rg = manager.getRegion(rgName);
            if (rg instanceof ProtectedCuboidRegion) {
                region = (ProtectedCuboidRegion) rg;
            } else
                throw new Exception("rg not cuboid");
        } catch (NoClassDefFoundError e) {
            throw new WorldGuardException();
        }
    }

    public Location getBound1() {
        return convertVector(world, region.getMinimumPoint());
    }

    public Location getBound2() {
        return convertVector(world, region.getMaximumPoint());
    }

    //todo: move to util?
    private static Location convertVector(World w, BlockVector3 vec) {
        return new Location(w, vec.getX(), vec.getY(), vec.getZ());
    }
}
