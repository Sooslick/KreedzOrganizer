package ru.sooslick.bhop;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EventListener implements Listener {

    private final Engine engine;

    public EventListener(Engine e) {
        engine = e;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        checkTrigger(e.getPlayer(), e.getClickedBlock(), TriggerType.INTERACT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        //todo: load test
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
        Player p = e.getPlayer();
        BhopPlayer bhpl = engine.getDcPlayer(p);
        if (bhpl == null)
            return;

        bhpl.setPlayer(p);
        p.sendMessage("You have an unfinished bhop level, type /bhop continue to return");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled())
            return;
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        //check is block inside any level
        for (BhopLevel level : engine.getBhopLevelList()) {
            if (level.isInside(e.getBlock().getLocation())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled())
            return;
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        //check is block inside any level
        for (BhopLevel level : engine.getBhopLevelList()) {
            if (level.isInside(e.getBlock().getLocation())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (e.isCancelled())
            return;
        //check is block inside any level
        for (BhopLevel level : engine.getBhopLevelList()) {
            if (level.isInside(e.getPlayer().getLocation())) {
                e.setCancelled(true);
                return;
            }
        }
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

    private void checkTrigger(Player p, Block b, TriggerType type) {
        if (engine.getActivePlayersCount() == 0)
            return;

        //todo check gamemode, allow only survival and adventure
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
            if (bhcp.getTriggerType() == type && b.equals(bhl.getWorld().getBlockAt(bhcp.getLoadLocation()))) {
                engine.playerCheckpointEvent(bhpl, bhcp);
                return;
            }
        }
    }

    //todo onJoin: restore inv from file (failover check)

}
