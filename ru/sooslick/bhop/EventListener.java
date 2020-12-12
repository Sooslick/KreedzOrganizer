package ru.sooslick.bhop;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
    public void onDamage(EntityDamageEvent e) {
        if (engine.getActivePlayersCount() == 0)
            return;

        if (!(e.getEntity() instanceof Player))
            return;

        if (e.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;

        BhopPlayer bhpl = engine.getBhopPlayer((Player) e.getEntity());
        if (bhpl == null)
            return;

        e.setCancelled(true);
    }

    public void onDisconnect(PlayerQuitEvent e) {
        if (engine.getActivePlayersCount() == 0)
            return;

        BhopPlayer bhpl = engine.getBhopPlayer(e.getPlayer());
        if (bhpl == null)
            return;

        engine.playerExitEvent(bhpl, true);
    }

    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BhopPlayer bhpl = engine.getDcPlayer(p);
        if (bhpl == null)
            return;

        bhpl.setPlayer(p);
        p.sendMessage("You have an unfinished bhop level, type /bhop continue to return");
    }

    public void onBlockChange(BlockEvent e) {
        if (!(e instanceof Cancellable))
            return;

        Cancellable ce = (Cancellable) e;
        if (ce.isCancelled())
            return;

        //check is block inside any level
        for (BhopLevel level : engine.getBhopLevelList()) {
            if (level.isInside(e.getBlock().getLocation())) {
                ce.setCancelled(true);
                return;
            }
        }
    }

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
