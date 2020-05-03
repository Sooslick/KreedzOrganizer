package ru.sooslick.bhop;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Engine extends JavaPlugin {

    public static final String CFG_FILENAME = "plugin.yml";

    private FileConfiguration cfg;
    private List<BhopLevel> levels;
    private CommandListener commandListener;
    private EventListener eventListener;
    private List<BhopPlayer> activePlayers;
    private int bhopTimerId = 0;

    private Runnable bhopTimerProcessor = () -> {
        for (BhopPlayer bhpl : activePlayers) {
            bhpl.timer++;
        }
    };

    @Override
    public void onEnable() {

        //check plugin directory. Create if not exists
        try {
            if (!getDataFolder().exists()) {
                if (!getDataFolder().mkdir()) {
                    throw new Exception("cannot create datafolder");
                }
            }

            File f = new File(getDataFolder().toString() + File.separator + CFG_FILENAME);
            if (!f.exists()) {
                throw new Exception("cfg not exists");   //при перехвате эксепшна конфиг забивается дефолтными значениями
            }

            cfg = getConfig();              //todo по хорошему надо явно загрузить файл getDataFolder().toString() + File.separator + CFG_FILENAME

        } catch (Exception e) {
            System.out.println("get default data"); //todo

            saveDefaultConfig();
            cfg = getConfig();

        }

        ConfigurationSection csLevels = cfg.getConfigurationSection("levels");

        World w = Bukkit.getWorlds().get(0);
        levels = new ArrayList<>();
        for (String levelName : csLevels.getKeys(false)) {
            ConfigurationSection csParams = csLevels.getConfigurationSection(levelName);
            BhopLevel bhopLevel = new BhopLevel(levelName);


            bhopLevel.setBounds(BhopUtil.stringToLocation(w, csParams.getString("bound1")),
                                BhopUtil.stringToLocation(w, csParams.getString("bound2")));  //todo: world(0) - world by name
            bhopLevel.setStart(BhopUtil.stringToLocation(w, csParams.getString("start")));
            bhopLevel.setFinish(BhopUtil.stringToLocation(w, csParams.getString("finish")));

            ConfigurationSection csCheckpoints = csParams.getConfigurationSection("checkpoints");
            for (String cpName : csCheckpoints.getKeys(false)) {
                bhopLevel.addCheckpoint(new BhopCheckpoint(BhopUtil.stringToLocation(w, csCheckpoints.getString(cpName)), cpName));
            }
            //todo hi scores
            levels.add(bhopLevel);

        }

        //init listeners;
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        commandListener = new CommandListener(this);
        getCommand("bhop").setExecutor(commandListener);        //todo plugin.yml

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
        BhopLevel bhl = null;                       //todo refactor to method
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
        p.teleport(cp.getLocation());
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
