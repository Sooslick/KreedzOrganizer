package ru.sooslick.bhop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Engine extends JavaPlugin {

    public static final String CFG_FILENAME = "plugin.yml";
    public static final String CFG_DEFAULT_LEVEL = "defaultLevel.yml";
    public static final String CFG_LEVELS = "levels";
    public static final String CFG_INVENTORY = "inventory";
    public static final String YAML_EXTENSION = ".yml";

    public static Logger LOG;

    private static String DATA_FOLDER_PATH;
    private static String LEVELS_DIR;
    private static String LEVELS_PATH;
    public static String INVENTORY_PATH;

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
        DATA_FOLDER_PATH = getDataFolder().getPath() + File.separator;
        LEVELS_DIR = DATA_FOLDER_PATH + CFG_LEVELS;
        LEVELS_PATH = LEVELS_DIR + File.separator;
        INVENTORY_PATH = DATA_FOLDER_PATH + CFG_INVENTORY + File.separator;
        reload();

        //todo: should me refactor listeners to reload()?
        //init listeners;
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getCommand("bhop").setExecutor(new CommandListener(this));        //todo plugin.yml

        //init other variables
        activePlayers = new ArrayList<>();
    }

    @Override
    public void onDisable() {
        saveAll();
    }

    public BhopLevel getBhopLevel(String name) {
        for (BhopLevel bhl : levels) {
            if (bhl.getName().equals(name)) return bhl;
        }
        return null;
    }

    public BhopCheckpoint getBhopCheckpoint(BhopPlayer bhpl, String cpName) {
        if (bhpl == null)
            return null;
        BhopLevel bhl = bhpl.getLevel();
        if (bhl == null) {
            //todo wtf? log message!
            return null;
        }
        return bhl.getCheckpoint(cpName);
    }

    public void playerStartEvent(Player p, BhopLevel bhl) {
        //check if player triggered event while playing
        BhopPlayer activeBhpl = getBhopPlayer(p);
        if (activeBhpl != null)
            activePlayers.remove(activeBhpl);

        //create new BhopPlayer and prepare him
        activePlayers.add(new BhopPlayer(p, bhl));
        //todo check result and cancel start if fail
        InventoryUtil.invToFile(p);
        p.teleport(bhl.getStartPosition());

        //launch timer if not exists
        if (bhopTimerId == 0) { //todo check can scheduler return 0 as ID
            bhopTimerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, bhopTimerProcessor, 1, 20);
        }
    }

    public void playerLoadEvent(BhopPlayer bhpl, BhopCheckpoint cp) {
        if (bhpl == null)
            return;
        //TODO CHECK IF CHECKPOINT IS AVAILABLE 4 PLAYER
        bhpl.getPlayer().teleport(cp.getLoadLocation());
        //todo message OH HELLO THERE
    }

    public void playerExitEvent(BhopPlayer bhpl) {
        if (bhpl == null)
            return;
        activePlayers.remove(bhpl);

        if (activePlayers.size() == 0) {
            Bukkit.getScheduler().cancelTask(bhopTimerId);
            bhopTimerId = 0;
        }

        //todo check result and ALARM if fail
        InventoryUtil.invFromFile(bhpl.getPlayer());
    }

    public void playerFinishEvent(BhopPlayer bhpl) {
        if (bhpl == null)
            return;
        //todo: save player's time and send MESSAGE.
        bhpl.getPlayer().sendMessage("MOLODEC.");
        playerExitEvent(bhpl);
    }

    public void playerCheckpointEvent(BhopPlayer bhpl, BhopCheckpoint cp) {
        if (bhpl == null)
            return;
        bhpl.addCheckpoint(cp);
    }

    public BhopPlayer getBhopPlayer(Player p) {
        for (BhopPlayer bhpl : activePlayers) {
            if (bhpl.getPlayer().equals(p)) return bhpl;
        }
        return null;
    }

    public int getActivePlayersCount() {
        return activePlayers.size();
    }

    private void reload() {
        LOG = Bukkit.getLogger();

        //check plugin directory. Create if not exists
        boolean folderCreated = false;
        if (!getDataFolder().exists()) {
            if (getDataFolder().mkdir()) {
                folderCreated = true;
                LOG.info("Created plugin folder");
            } else {
                LOG.warning("Cannot create plugin folder. Default config will be loaded!");
            }
        }
        saveDefaultConfig();

        //check levels directory, create and fill if not exists
        if (folderCreated) {
            File levelsDir = new File(LEVELS_DIR);
            if (!levelsDir.exists()) {
                if (levelsDir.mkdir()) {
                    //save defaultLevel.yml from resources
                    try {
                        File out = new File(LEVELS_PATH + CFG_DEFAULT_LEVEL);
                        if (!out.exists()) {
                            if (!out.createNewFile()) {
                                throw new IOException();
                            }
                        }
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(CFG_DEFAULT_LEVEL)));
                        PrintWriter pw = new PrintWriter(new File(LEVELS_PATH + CFG_DEFAULT_LEVEL));
                        String s;
                        while (!(s = br.readLine()).isEmpty()) {
                            pw.println(s);
                        }
                        pw.flush();
                        pw.close();
                        LOG.info("Saved defaultLevel.yml!");
                    } catch (IOException e) {
                        LOG.warning("Cannot create defaultLevel.yml!");
                    }
                } else {
                    LOG.warning("Cannot create levels folder!");
                }
            }
        }

        //load and read config
        //todo: refactor to Cfg class
        cfg = getConfig();
        List<?> csLevels = cfg.getList("levels");
        levels = new ArrayList<>();
        for (Object obj : csLevels) {
            String levelName = (String) obj;
            if (getBhopLevel(levelName) != null) {
                LOG.warning("Level " + levelName + " dublication in config.yml, skipping");
                continue;
            }
            try {
                //read level's data
                YamlConfiguration csParams = new YamlConfiguration();
                csParams.load(LEVELS_PATH + levelName + YAML_EXTENSION);
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
    }

    private void saveAll() {
        levels.forEach(this::saveLevel);
        saveCfg();
    }

    private void saveCfg() {
        List<String> levelNames = new LinkedList<>();
        levels.forEach(level -> levelNames.add(level.getName()));
        try {
            FileConfiguration mainCfg = new YamlConfiguration();
            mainCfg.set(CFG_LEVELS, levelNames);
            mainCfg.save(DATA_FOLDER_PATH + CFG_FILENAME);
        } catch (Exception e) {
            LOG.warning("Unable to save config");
        }
    }

    private void saveLevel(BhopLevel level) {
        try {
            YamlConfiguration levelCfg = new YamlConfiguration();
            levelCfg.set("world", level.getStartPosition().getWorld().getName());
//            levelCfg.set("region", level.getRegion());    //todo region
            levelCfg.set("bound1", BhopUtil.locationToString(level.getBound1()));
            levelCfg.set("bound2", BhopUtil.locationToString(level.getBound2()));
            levelCfg.set("start", BhopUtil.locationToString(level.getStartPosition()));
            levelCfg.set("finish", BhopUtil.locationToString(level.getFinish()));
            levelCfg.set("triggerType", level.getTriggerType().toString());
            ConfigurationSection csCps = new YamlConfiguration();
            for (BhopCheckpoint cp : level.getCheckpoints()) {
                ConfigurationSection csCurrentCp = new YamlConfiguration();
                csCurrentCp.set("triggerType", cp.getTriggerType().toString());
                csCurrentCp.set("triggerLocation", BhopUtil.locationToString(cp.getTriggerLocation()));
                csCurrentCp.set("loadLocation", BhopUtil.locationToString(cp.getLoadLocation()));
                csCps.set(cp.getName(), csCurrentCp);
            }
            levelCfg.set("checkpoints", csCps);
            ConfigurationSection csHs = new YamlConfiguration();
            for (BhopRecord rec : level.getRecords()) {
                csHs.set(rec.getName(), rec.getTime());
            }
            levelCfg.set("leaderboard", csHs);
            levelCfg.save(LEVELS_PATH + level.getName() + YAML_EXTENSION);
        } catch (Exception e) {
            LOG.warning("Unable to save level " + level.getName());
        }
    }

    //todo
    //  on move - detect triggers
    //  save records

    //todo:
    //  create arenas from game
    //  regions / boundings
    //  more player events

}
