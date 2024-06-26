package ru.sooslick.bhop;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.sooslick.bhop.command.BhopCommandListener;
import ru.sooslick.bhop.command.BhopEditCommandListener;
import ru.sooslick.bhop.exception.WorldGuardException;
import ru.sooslick.bhop.region.WorldGuardRegion;
import ru.sooslick.bhop.util.BhopUtil;
import ru.sooslick.bhop.util.CommonUtil;
import ru.sooslick.bhop.util.InventoryUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class Engine extends JavaPlugin {

    public static final String CFG_FILENAME = "config.yml";
    public static final String CFG_DEFAULT_LEVEL = "defaultLevel.yml";
    public static final String CFG_LEADERBOARDS = "leaderboards.yml";
    public static final String CFG_LEVELS = "levels";
    public static final String CFG_INVENTORY = "inventory";
    public static final String CFG_BACKUP = "Backup";
    public static final String CFG_USEWG = "useWorldGuardRegions";
    public static final String COMMAND_BHOP = "bhop";
    public static final String COMMAND_BHOPEDIT = "bhopmanage";
    public static final String YAML_EXTENSION = ".yml";

    public static Logger LOG;
    private static Engine instance;
    private static EventListener listener;

    private static String DATA_FOLDER_PATH;
    private static String LEVELS_DIR;
    public static String LEVELS_PATH;
    public static String INVENTORY_PATH;
    public static String INVENTORY_BACKUP_PATH;

    private List<BhopPlayer> activePlayers;
    private List<BhopPlayer> dcPlayers;
    private int bhopTimerId = 0;
    private boolean useWg = false;
    private boolean cfgChanged = false;

    private final Runnable bhopTimerProcessor = () -> activePlayers.stream()
           .filter(BhopPlayer::tickAndCheckFlee)
           .forEach(this::playerExitEvent);

    public static Engine getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        new Metrics(this, 10944);
        instance = this;
        DATA_FOLDER_PATH = getDataFolder().getPath() + File.separator;
        LEVELS_DIR = DATA_FOLDER_PATH + CFG_LEVELS;
        LEVELS_PATH = LEVELS_DIR + File.separator;
        INVENTORY_PATH = DATA_FOLDER_PATH + CFG_INVENTORY + File.separator;
        INVENTORY_BACKUP_PATH = DATA_FOLDER_PATH + CFG_INVENTORY + CFG_BACKUP + File.separator;
        reload();
    }

    @Override
    public void onDisable() {
        saveAll();
    }

    public boolean isUseWg() {
        return useWg;
    }

    public BhopCheckpoint getBhopPlayerCheckpoint(@NotNull BhopPlayer bhpl, String cpName) {
        return bhpl.getCheckpoint(cpName);
    }

    public BhopPlayer playerStartEvent(@NotNull Player p, @NotNull BhopLevel bhl) {
        LOG.info("Bhop player start event triggered");
        //remove player from DC list if presents
        dcPlayers.remove(getDcPlayer(p));
        //check if player triggered event while playing
        BhopPlayer activeBhpl = getBhopPlayer(p);
        if (activeBhpl != null) {
            LOG.info("Player restarted level");
            activeBhpl.restart(bhl);
        } else {
            //try to save inventory and cancel start in fail case
            if (!InventoryUtil.invToFile(p)) {
                LOG.warning("Cannot save player inventory, start event cancelled. Player: " + p.getName());
                p.sendMessage("§cOops, something went wrong. Can't start game");
                return null;
            }
            //create new BhopPlayer and prepare him
            activeBhpl = new BhopPlayer(p, bhl);
            activePlayers.add(activeBhpl);
        }
        p.teleport(bhl.getStartPosition());
        p.getActivePotionEffects().forEach(pe -> p.removePotionEffect(pe.getType()));

        //check gamemode
        // todo weird behavior, change plz
        if (p.getGameMode() == GameMode.SPECTATOR)
            p.setGameMode(GameMode.ADVENTURE);
        else if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE)
            activeBhpl.enableCheats();

        //launch timer if not exists
        if (bhopTimerId == 0) {
            bhopTimerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, bhopTimerProcessor, 1, 1);
        }

        //return player
        return activeBhpl;
    }

    public void playerLoadEvent(@NotNull BhopPlayer bhpl, @NotNull BhopCheckpoint cp) {
        LOG.info("Bhop player load event triggered");
        if (!bhpl.getCheckpointsSet().contains(cp))
            return;
        Player p = bhpl.getPlayer();
        p.teleport(cp.getLoadLocation());
        p.sendMessage("§eLoaded checkpoint " + cp.getName());
    }

    public void playerRejoinEvent(@NotNull BhopPlayer bhpl) {
        LOG.info("Bhop player rejoin event triggered");
        if (!dcPlayers.contains(bhpl))
            return;
        Player p = bhpl.getPlayer();
        if (!InventoryUtil.invToFile(p)) {
            LOG.warning("Cannot save player inventory, rejoin event cancelled. Player: " + p.getName());
            p.sendMessage("§cOops, something went wrong. Can't continue game");
            return;
        }
        dcPlayers.remove(bhpl);
        activePlayers.add(bhpl);
        p.teleport(bhpl.getDcLocation());
        p.getActivePotionEffects().forEach(pe -> p.removePotionEffect(pe.getType()));
        //check gamemode
        if (p.getGameMode() == GameMode.SPECTATOR)
            p.setGameMode(GameMode.ADVENTURE);
        else if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE)
            bhpl.enableCheats();
        //launch timer if not exists
        if (bhopTimerId == 0) {
            bhopTimerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, bhopTimerProcessor, 1, 1);
        }
    }

    public void playerExitEvent(@NotNull BhopPlayer bhpl) {
        playerExitEvent(bhpl, false);
    }

    public void playerExitEvent(@NotNull BhopPlayer bhpl, boolean dc) {
        LOG.info("Bhop player exit event triggered");
        activePlayers.remove(bhpl);

        if (dc) {
            bhpl.setDcLocation(bhpl.getPlayer().getLocation());
            dcPlayers.add(bhpl);
        }

        if (activePlayers.size() == 0) {
            Bukkit.getScheduler().cancelTask(bhopTimerId);
            bhopTimerId = 0;
        }

        Player p = bhpl.getPlayer();
        if (!InventoryUtil.invFromFile(p)) {
            LOG.warning("Cannot restore player's inventory, player: " + p.getName());
            p.sendMessage("§cOops, something went wrong. Can't restore your inventory, please contact server admin");
        }

        bhpl.exit();
    }

    public void playerFinishEvent(BhopPlayer bhpl) {
        LOG.info("Bhop player finish event triggered");
        if (bhpl == null)
            return;
        bhpl.getPlayer().sendMessage("§aLevel finished in " + CommonUtil.formatDuration(bhpl.getTimer()));
        if (!bhpl.isCheated()) {
            BhopRecord rec = bhpl.getLevel().getPlayerRecord(bhpl.getPlayer().getName());
            if (rec == null) {
                rec = new BhopRecord(bhpl.getPlayer().getName(), bhpl.getTimer());
                bhpl.getLevel().addRecord(rec);
                bhpl.getPlayer().sendMessage("§aNew personal best");
                saveLeaderboards();
            } else {
                if (bhpl.getTimer() < rec.getTime()) {
                    rec.setTime(bhpl.getTimer());
                    bhpl.getPlayer().sendMessage("§aNew personal best");
                    saveLeaderboards();
                }
            }
            BhopRecord wrec = bhpl.getLevel().getLevelRecord();
            if (wrec == rec && wrec.getTime() == bhpl.getTimer())
                Bukkit.broadcastMessage("§eNew record on " + bhpl.getLevel().getName() + ": " + rec.formatTime() + " by " + rec.getName());
        }
        playerExitEvent(bhpl);
    }

    public void playerCheckpointEvent(BhopPlayer bhpl, BhopCheckpoint cp) {
        //move event floods to the console
        //LOG.info("Bhop player checkpoint event triggered");
        if (bhpl == null || cp == null)
            return;
        if (bhpl.addCheckpoint(cp))
            bhpl.getPlayer().sendMessage("§aReached checkpoint " + cp.getName() +
                    ". Use §6/bhop load " + cp.getName() + " §ato teleport to this point");
    }

    public BhopPlayer getBhopPlayer(Player p) {
        if (getActivePlayersCount() == 0) return null;
        return activePlayers.stream().filter(bhpl -> bhpl.getPlayer().equals(p)).findFirst().orElse(null);
    }

    public BhopPlayer getDcPlayer(Player p) {
        // Player objects may be not equals, check by names
        return dcPlayers.stream().filter(dc -> dc.getPlayer().getName().equals(p.getName())).findFirst().orElse(null);
    }

    public int getActivePlayersCount() {
        return activePlayers.size();
    }

    public void printPlayerStat(CommandSender sender, String name) {
        AtomicInteger entries = new AtomicInteger();
        BhopLevelsHolder.getBhopLevelList()
                .forEach(bhl -> bhl.getRecords().stream()
                        .filter(bhr -> bhr.getName().equals(name))
                        .forEach(bhr -> {
                            sender.sendMessage("§e" + bhl.getName() + ": " + bhr.formatTime());
                            entries.getAndIncrement();
                        }));
        if (entries.get() == 0)
            sender.sendMessage("§cNo stats found for player " + name);
    }

    public void reload() {
        LOG = getLogger();
        LOG.info("Bhop reload");

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
//                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(CFG_DEFAULT_LEVEL)));
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/defaultLevel.yml")));
                        PrintWriter pw = new PrintWriter(LEVELS_PATH + CFG_DEFAULT_LEVEL);
                        String s;
                        while ((s = br.readLine()) != null) {
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
        reloadConfig();
        FileConfiguration cfg = getConfig();
        useWg = cfg.getBoolean(CFG_USEWG, false);
        List<?> csLevels = cfg.getList(CFG_LEVELS);
        if (csLevels == null) csLevels = Collections.emptyList();
        for (Object obj : csLevels) {
            String levelName = (String) obj;
            if (BhopLevelsHolder.getBhopLevel(levelName) != null) {
                LOG.warning("Level " + levelName + " duplication in config.yml, skipping");
                continue;
            }
            try {
                //read level's data
                YamlConfiguration csParams = new YamlConfiguration();
                csParams.load(LEVELS_PATH + levelName + YAML_EXTENSION);
                String author = csParams.getString("author", null);
                BhopLevel bhopLevel = new BhopLevel(levelName, author);
                World w = Bukkit.getWorld(csParams.getString("world"));
                boolean rgSuccess = false;
                if (useWg && csParams.contains("region")) {
                    try {
                        rgSuccess = bhopLevel.setRegion(w, csParams.getString("region"));
                    } catch (WorldGuardException e) {
                        LOG.warning("World Guard integration disabled");
                        useWg = false;
                    }
                }
                if (!rgSuccess) {
                    bhopLevel.setBounds(
                            BhopUtil.stringToLocation(w, csParams.getString("bound1")),
                            BhopUtil.stringToLocation(w, csParams.getString("bound2")));
                }
                bhopLevel.setStart(BhopUtil.stringToLocation(w, csParams.getString("start")));
                bhopLevel.setFinish(BhopUtil.stringToLocation(w, csParams.getString("finish")));
                bhopLevel.setTriggerType(TriggerType.valueOf(csParams.getString("triggerType").toUpperCase()));

                //read level's checkpoints data
                ConfigurationSection csCheckpoints = csParams.getConfigurationSection("checkpoints");
                if (csCheckpoints != null) {
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
                            type = TriggerType.valueOf(cpData.getString("triggerType").toUpperCase());
                        } catch (Exception e) {
                            type = TriggerType.MOVEMENT;
                        }
                        bhopLevel.addCheckpoint(new BhopCheckpoint(cpName, load, trigger, type));
                    }
                }

                //save level
                BhopLevelsHolder.updateLevel(bhopLevel);
            } catch (Exception e) {
                LOG.warning("Error occurred while reading level " + levelName);
                LOG.warning(e.getMessage());
            }
        }
        int levelsSize = BhopLevelsHolder.getLevelsNumber();
        LOG.info("Loaded " + levelsSize + " Bhop levels");
        int files = new File(LEVELS_DIR).listFiles().length;
        if (files > levelsSize)
            LOG.warning("Level folder contains more files than actual level count. Did you configure levels properly?");

        //load bhop admin
        BhopAdminManager.init();

        //read leaderboards
        YamlConfiguration csRecords = new YamlConfiguration();
        File f = new File(DATA_FOLDER_PATH + CFG_LEADERBOARDS);
        if (f.exists()) {
            try {
                csRecords.load(DATA_FOLDER_PATH + CFG_LEADERBOARDS);
                for (BhopLevel bhl : BhopLevelsHolder.getBhopLevelList()) {
                    ConfigurationSection csLevelRecs = csRecords.getConfigurationSection(bhl.getName());
                    for (String name : csLevelRecs.getKeys(false)) {
                        bhl.addRecord(new BhopRecord(name, csLevelRecs.getInt(name)));
                    }
                }
            } catch (Exception e) {
                LOG.warning("Unable to read leaderboards");
            }
        } else {
            LOG.info("Leaderboards.yml is not exists, skipped");
        }

        //init listeners;
        if (listener != null)
            HandlerList.unregisterAll(listener);
        listener = new EventListener(this);
        getServer().getPluginManager().registerEvents(listener, this);

        PluginCommand cmd = getCommand(COMMAND_BHOP);
        assert cmd != null;
        cmd.setExecutor(new BhopCommandListener());
        cmd = getCommand(COMMAND_BHOPEDIT);
        assert cmd != null;
        cmd.setExecutor(new BhopEditCommandListener());

        //check active players
        if (activePlayers != null && activePlayers.size() > 0) {
            List<BhopPlayer> copy = new LinkedList<BhopPlayer>(activePlayers);
            copy.forEach(bhpl -> {
                this.playerExitEvent(bhpl);
                bhpl.getPlayer().sendMessage("§4Oops, Bhop plugin was reloaded causing interrupting your session.");
            });
        }

        //init other variables
        activePlayers = new ArrayList<>();
        dcPlayers = new ArrayList<>();
    }

    private void saveAll() {
        saveLeaderboards();
        BhopLevelsHolder.getBhopLevelList().forEach(this::writeLevel);
        saveCfg();
    }

    public void saveCfgOnlyLevels() {
        reloadConfig();
        FileConfiguration cfg = getConfig();
        cfg.set(CFG_LEVELS, BhopLevelsHolder.getBhopLevelNamesList());
        try {
            cfg.save(DATA_FOLDER_PATH + CFG_FILENAME);
            LOG.info("Saved config");
        } catch (IOException e) {
            LOG.warning("Unable to save config");
        }
    }

    private void saveCfg() {
        if (!cfgChanged) {
            LOG.info("No changes in main config");
            return;
        }
        try {
            FileConfiguration mainCfg = new YamlConfiguration();
            mainCfg.set(CFG_USEWG, useWg);
            mainCfg.set(CFG_LEVELS, BhopLevelsHolder.getBhopLevelNamesList());
            mainCfg.save(DATA_FOLDER_PATH + CFG_FILENAME);
            LOG.info("Saved config");
        } catch (Exception e) {
            LOG.warning("Unable to save config");
        }
    }

    public void writeLevel(BhopLevel level) {
        if (!level.isChanged())
            return;
        LOG.info("Saving changes in level " + level.getName());
        cfgChanged = true;
        try {
            YamlConfiguration levelCfg = new YamlConfiguration();
            levelCfg.set("world", level.getStartPosition().getWorld().getName());
            levelCfg.set("author", level.getAuthor());
            if (level.getBhopRegion() instanceof WorldGuardRegion)
                levelCfg.set("region", ((WorldGuardRegion) level.getBhopRegion()).getName());
            levelCfg.set("bound1", BhopUtil.locationToString(level.getBhopRegion().getBound1()));
            levelCfg.set("bound2", BhopUtil.locationToString(level.getBhopRegion().getBound2()));
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

    public void saveLeaderboards() {
        YamlConfiguration csLeaders = new YamlConfiguration();
        for (BhopLevel bhl : BhopLevelsHolder.getBhopLevelList()) {
            ConfigurationSection csLevel = new YamlConfiguration();
            for (BhopRecord rec : bhl.getRecords()) {
                csLevel.set(rec.getName(), rec.getTime());
            }
            csLeaders.set(bhl.getName(), csLevel);
        }
        try {
            csLeaders.save(DATA_FOLDER_PATH + CFG_LEADERBOARDS);
            LOG.info("Saved leaderboards");
        } catch (Exception e) {
            LOG.warning("Unable to save leaderboards");
        }
    }

    // ADMIN METHODS

    public void deleteLevel(BhopLevel bhl) {
        if (BhopLevelsHolder.removeLevel(bhl)) {
            cfgChanged = true;
            if (bhl.getFile().delete())
                LOG.info("Removed level " + bhl.getName());
            else
                LOG.warning("Level " + bhl.getName() + " removed, but file was not deleted");
        }
    }

    //todo future features:
    //  separate listeners: gameplay / antigriefing
    //  cfg field: enableDefaultAntigriefing
    //  refactor cfg to class
    //  more antigriefing checks
    //  more "cheated" checks
    //  region changes detection
    //  sign records
    //  level owner
    //  test level
    //  code refactoring
    //  new timer: realtime / ticks + scoreboard
    //  triggerzones
    //  race mode
    //  vault economy enable
    //  paid entry
    //  win reward
    //  jumpto
    //  gui inventories
}
