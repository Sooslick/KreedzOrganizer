package ru.sooslick.bhop;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.TabCompleteEvent;
import ru.sooslick.bhop.command.BhopCommandListener;
import ru.sooslick.bhop.command.BhopEditCommandListener;
import ru.sooslick.bhop.util.BhopUtil;
import ru.sooslick.bhop.util.InventoryUtil;

import java.io.File;
import java.util.List;

public class EventListener implements Listener {

    private final Engine engine;

    public EventListener(Engine e) {
        engine = e;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK)
            checkTrigger(e.getPlayer(), e.getClickedBlock(), TriggerType.INTERACT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null)
            return;
        checkTrigger(e.getPlayer(), e.getTo().getBlock(), TriggerType.MOVEMENT);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled())
            return;
        if (engine.getActivePlayersCount() == 0)
            return;
        if (engine.getBhopPlayer(e.getPlayer()) == null)
            return;
        PlayerTeleportEvent.TeleportCause tc = e.getCause();
        if (tc == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT ||
            tc == PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.isCancelled())
            return;
        if (engine.getActivePlayersCount() == 0)
            return;
        if (!(e.getEntity() instanceof Player))
            return;
        if (engine.getBhopPlayer((Player) e.getEntity()) == null)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.isCancelled())
            return;
        if (engine.getActivePlayersCount() == 0)
            return;
        if (!(e.getEntity() instanceof Player))
            return;
        if (engine.getBhopPlayer((Player) e.getEntity()) == null)
            return;
        if (((Player) e.getEntity()).getFoodLevel() > e.getFoodLevel())
            e.setCancelled(true);
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if (engine.getActivePlayersCount() == 0)
            return;

        BhopPlayer bhpl = engine.getBhopPlayer(e.getPlayer());
        if (bhpl == null)
            return;

        engine.playerExitEvent(bhpl, true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        //restore inv if inv file exists
        Player p = e.getPlayer();
        File f = new File(Engine.INVENTORY_PATH + e.getPlayer().getName() + Engine.YAML_EXTENSION);
        if (f.exists()) {
            if (!InventoryUtil.invFromFile(p)) {
                Engine.LOG.warning("Cannot restore player's inventory, player: " + p.getName());
                p.sendMessage("§cOops, something went wrong. Can't restore your inventory, please contact server admin");
            }
            Engine.LOG.info("Restored inventory of player " + e.getPlayer().getName());
        }

        //check dc
        BhopPlayer bhpl = engine.getDcPlayer(p);
        if (bhpl == null)
            return;
        bhpl.setPlayer(p);
        p.sendMessage("§eYou have an unfinished bhop level, type §6/bhop continue §eto return");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled())
            return;
        if (e.getPlayer().hasPermission(BhopPermissions.BYPASS))
            return;
        //check is block inside any level
        for (BhopLevel level : engine.getBhopLevelList()) {
            if (level.isInside(e.getBlock().getLocation())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("Don't build here");
                return;
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled())
            return;
        if (e.getPlayer().hasPermission(BhopPermissions.BYPASS))
            return;
        //check is block inside any level
        for (BhopLevel level : engine.getBhopLevelList()) {
            if (level.isInside(e.getBlock().getLocation())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("Don't build here");
                return;
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (e.isCancelled())
            return;
        BhopPlayer bhpl = engine.getBhopPlayer(e.getPlayer());
        if (bhpl == null)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.isCancelled())
            return;
        if (!(e.getEntity() instanceof Player))
            return;
        BhopPlayer bhpl = engine.getBhopPlayer((Player) e.getEntity());
        if (bhpl == null)
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onCreeper(EntityExplodeEvent e) {
        if (e.isCancelled())
            return;
        double r = calcExplosionRadius(e.getLocation(), e.blockList());
        double d = engine.distanceToNearestLevel(e.getLocation());
        if (d < r)
            e.blockList().clear();
    }

    @EventHandler
    public void onExplosion(BlockExplodeEvent e) {
        if (e.isCancelled())
            return;
        double r = calcExplosionRadius(e.getBlock().getLocation(), e.blockList());
        double d = engine.distanceToNearestLevel(e.getBlock().getLocation());
        if (d < r)
            e.blockList().clear();
    }

    private void checkTrigger(Player p, Block b, TriggerType type) {
        if (engine.getActivePlayersCount() == 0)
            return;
        BhopPlayer bhpl = engine.getBhopPlayer(p);
        if (bhpl == null)
            return;

        //check finish
        BhopLevel bhl = bhpl.getLevel();
        if (bhl.getTriggerType() == type && b.equals(bhl.getWorld().getBlockAt(bhl.getFinish()))) {
            engine.playerFinishEvent(bhpl);
            return;
        }
        //check cpoint
        for (BhopCheckpoint bhcp : bhl.getCheckpoints()) {
            if (bhcp.getTriggerType() == type && b.equals(bhl.getWorld().getBlockAt(bhcp.getTriggerLocation()))) {
                engine.playerCheckpointEvent(bhpl, bhcp);
                return;
            }
        }
    }

    private double calcExplosionRadius(Location center, List<Block> bs) {
        return bs.stream().map(b -> BhopUtil.distance(center, b.getLocation())).max(Double::compareTo).orElse(0d);
    }

    ////////////////////
    // TAB COMPLETION //

    @EventHandler
    public void onTabComplete(TabCompleteEvent e) {
        String[] args = e.getBuffer().replaceFirst("/", "").replaceAll("[ ]+", " ").trim().split(" ");
        if (args.length == 0)
            return;
        if (args[0].equalsIgnoreCase(BhopCommandListener.COMMAND_BHOP) || args[0].equalsIgnoreCase(BhopCommandListener.COMMAND_BHOP_ALIAS)) {
            BhopCommandListener.tabComplete(e, args);
            return;
        }
        if (args[0].equalsIgnoreCase(BhopEditCommandListener.COMMAND_MANAGE) || args[0].equalsIgnoreCase(BhopEditCommandListener.COMMAND_MANAGE_ALIAS)) {
            BhopEditCommandListener.tabComplete(e, args);
        }
    }
}
