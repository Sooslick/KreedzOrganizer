package ru.sooslick.bhop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Engine extends JavaPlugin {

    public static final String CFG_FILENAME = "plugin.yml";

    public static Logger LOG;

    private FileConfiguration cfg;
    private List<BhopLevel> levels;
    private List<BhopPlayer> activePlayers;
    private int bhopTimerId = 0;

    private Runnable bhopTimerProcessor = () -> {
        for (BhopPlayer bhpl : activePlayers) {
            bhpl.timer++;
        }
    };

    @Override
    public void onEnable() {
        LOG = Bukkit.getLogger();
        //check plugin directory. Create if not exists
        if (!getDataFolder().exists()) {
            if (getDataFolder().mkdir()) {
                saveDefaultConfig();
            } else {
                LOG.warning("Cannot create data folder. Default config will be loaded!");
            }
        }

        //load and read config
        //todo: refactor to Cfg class
        cfg = getConfig();
        ConfigurationSection csLevels = cfg.getConfigurationSection("levels");
        levels = new ArrayList<>();
        for (String levelName : csLevels.getKeys(false)) {
            if (getBhopLevel(levelName) != null) {
                LOG.warning("Level " + levelName + " dublication in config.yml, skipping");
                continue;
            }
            try {
                //read level's data
                ConfigurationSection csParams = csLevels.getConfigurationSection(levelName);
                BhopLevel bhopLevel = new BhopLevel(levelName);
                World w = Bukkit.getWorld(csParams.getString("world"));
                bhopLevel.setBounds(
                        BhopUtil.stringToLocation(w, csParams.getString("bound1")),
                        BhopUtil.stringToLocation(w, csParams.getString("bound2")));
                bhopLevel.setStart(BhopUtil.stringToLocation(w, csParams.getString("start")));
                bhopLevel.setFinish(BhopUtil.stringToLocation(w, csParams.getString("finish")));
                bhopLevel.setTriggerType(TriggerType.valueOf(csParams.getString("triggerType")));

                //read level's checkpoints data
                ConfigurationSection csCheckpoints = csParams.getConfigurationSection("checkpoints");
                for (String cpName : csCheckpoints.getKeys(false)) {
                    ConfigurationSection cpData = csCheckpoints.getConfigurationSection(cpName);
                    Location load = BhopUtil.stringToLocation(w, cpData.getString("loadLocation"));
                    Location trigger;
                    TriggerType type;
                    try {
                        trigger = BhopUtil.stringToLocation(w, cpData.getString("triggerLocation"));
                    } catch (Exception e) {
                        trigger = load;
                    }
                    try {
                        type = TriggerType.valueOf(cpData.getString("triggerType"));
                    } catch (Exception e) {
                        type = TriggerType.MOVEMENT;
                    }
                    bhopLevel.addCheckpoint(new BhopCheckpoint(cpName, load, trigger, type));
                }

                //read level's leaderboard
                ConfigurationSection csRecords = csParams.getConfigurationSection("leaderboard");
                for (String recHolder : csRecords.getKeys(false)) {
                    bhopLevel.addRecord(new BhopRecord(recHolder, csRecords.getInt(recHolder)));
                }

                //save level
                levels.add(bhopLevel);
            } catch (Exception e) {
                LOG.warning("Error occured while reading level " + levelName);
                LOG.warning(e.getMessage());
                e.printStackTrace();
            }
        }

        //init listeners;
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getCommand("bhop").setExecutor(new CommandListener(this));        //todo plugin.yml

        //init other variables
        activePlayers = new ArrayList<>();
    }

    @Override
    public void onDisable() {
        //todo save config + leaderboards
    }

    public BhopLevel getBhopLevel(String name) {
        for (BhopLevel bhl : levels) {
            if (bhl.getName().equals(name)) return bhl;
        }
        return null;
    }

    public BhopCheckpoint getBhopCheckpoint(Player p, String cpName) {
        BhopLevel bhl = null;                       //todo refactor to method getPlayerBhopLevel
        for (BhopPlayer bhpl : activePlayers) {
            if (bhpl.getPlayer().equals(p)) {
                bhl = bhpl.getLevel();
                break;
            }
        }
        if (bhl == null) {
            //todo wtf?
            return null;
        }
        return bhl.getCheckpoint(cpName);
    }

    public void playerStartEvent(Player p, BhopLevel bhl) {
        activePlayers.add(new BhopPlayer(p, bhl));
        p.teleport(bhl.getStartPosition());
        //todo: store player's inventory to prevent cheating
        //check 4 timer processor
        if (bhopTimerId == 0) { //todo check can scheduler return 0 as ID
            bhopTimerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, bhopTimerProcessor, 1, 20);
        }
    }

    public void playerLoadEvent(Player p, BhopCheckpoint cp) {
        //TODO CHECK IF CHECKPOINT IS AVAILABLE 4 PLAYER
        p.teleport(cp.getLoadLocation());
        //todo message OH HELLO THERE
    }

    public void playerExitEvent(Player p) {
        BhopPlayer bhpl = getActivePlayer(p);
        if (bhpl == null) return;               //todo WTF.
        activePlayers.remove(bhpl);

        if (activePlayers.size() == 0) {
            Bukkit.getScheduler().cancelTask(bhopTimerId);
            bhopTimerId = 0;
        }
        //todo restore player's inventory
    }

    public void playerFinishEvent(Player p) {
        BhopPlayer bhpl = getActivePlayer(p);
        //todo: save player's time and send MESSAGE.
        p.sendMessage("MOLODEC.");
        playerExitEvent(p);
    }

    public void playerCheckpointEvent(Player p, BhopCheckpoint cp) {
        getActivePlayer(p).addCheckpoint(cp);
    }

    public boolean checkPlayerActive(Player p) {
        for (BhopPlayer bhpl : activePlayers) {
            if (bhpl.getPlayer().equals(p)) return true;
        }
        return false;
    }

    public int getActivePlayersCount() {
        return activePlayers.size();
    }

    public BhopPlayer getActivePlayer(Player p) {
        for (BhopPlayer bhpl : activePlayers) {
            if (bhpl.getPlayer().equals(p)) return bhpl;
        }
        return null;
    }

    //todo
    //  on move - detect triggers
    //  save records

    //todo:
    //  create arenas from game
    //  regions / boundings
    //  more player events

}
