package ru.sooslick.bhop;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {

    private Engine engine;

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
        p.sendMessage("слыш прыгать /bhop continue"); //todo msg
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
