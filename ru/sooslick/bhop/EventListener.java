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
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {

    private Engine engine;

    //todo: world
    private World w = Bukkit.getWorlds().get(0);

    public EventListener(Engine e) {
        engine = e;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (engine.getActivePlayersCount() == 0)
            return;

        Player p = e.getPlayer();
        //todo check gamemode, allow only survival and adventure
        BhopPlayer bhpl = engine.getActivePlayer(p);
        if (bhpl == null)
            return;

        if (!(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
            return;

        BhopLevel bhl = bhpl.getLevel();
        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock.equals(w.getBlockAt(bhl.getFinish()))) {
            engine.playerFinishEvent(p);
            return;
        }
        for (BhopCheckpoint bhcp : bhl.getCheckpoints()) {
            if (clickedBlock.equals(w.getBlockAt(bhcp.getLoadLocation()))) {
                engine.playerCheckpointEvent(p, bhcp);
                return;
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (engine.getActivePlayersCount() == 0)
            return;

        if (!(e.getEntity() instanceof Player))
            return;

        if (!(e.getCause().equals(EntityDamageEvent.DamageCause.FALL)))
            return;

        Player p = (Player) (e.getEntity());
        BhopPlayer bhpl = engine.getActivePlayer(p);
        if (bhpl == null)
            return;

        e.setCancelled(true);
    }

    public void onDisconnect(PlayerQuitEvent e) {
        if (engine.getActivePlayersCount() == 0)
            return;

        Player p = e.getPlayer();
        BhopPlayer bhpl = engine.getActivePlayer(p);
        if (bhpl == null)
            return;

        engine.playerExitEvent(p);
    }

}
