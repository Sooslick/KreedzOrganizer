package ru.sooslick.bhop.command;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.sooslick.bhop.*;

import java.util.stream.Collectors;

public class BhopCommandListener implements CommandExecutor {

    private static final String COMMAND_START = "start";
    private static final String COMMAND_LOAD = "load";
    private static final String COMMAND_EXIT = "exit";
    private static final String COMMAND_CONTINUE = "continue";
    private static final String COMMAND_PRACTICE = "practice";
    private static final String COMMAND_SAVE = "save";
    private static final String COMMAND_LEVELS = "levels";
    private static final String COMMAND_CHECKPOINTS = "checkpoints";
    private static final String COMMAND_LEADERBOARDS = "leaderboard";
    private static final String COMMAND_STAT = "stat";
    private static final String COMMAND_HELP = "help";

    private static final String AVAILABLE_CHECKPOINTS = "§eAvailable points:";
    private static final String AVAILABLE_LEVELS = "§eAvailable levels:";
    private static final String CHECKPOINT_EXISTS = "§cCheckpoint %s exists.";
    private static final String CHECKPOINT_NOT_FOUND = "§cUnknown checkpoint.";
    private static final String CHECKPOINT_REQUIRED = "§cCheckpoint name required.";
    private static final String CHECKPOINT_SAVED = "§aSaved checkpoint %s";
    private static final String CONSOLE_CANNOT_BHOP = "Console is not allowed to play bhop";
    private static final String GAME_RESTORED = "§cLatest bhop state restored";
    private static final String LEVEL_NOT_FOUND = "§cLevel not found.";
    private static final String LEVEL_REQUIRED = "§cLevel name required.";
    private static final String LEVEL_STARTED = "§eLevel %s started";
    private static final String NOT_PLAYING = "§cYou are not in-game";
    private static final String NOT_PRACTICE = "§cYou are not in practice mode. Use /bhop practice <level name> to enable this feature";
    private static final String NO_PERMISSION = "§cYou have not permissions";
    private static final String USAGE_SAVE = "§c/bhop save <checkpoint name>";

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        //check common commands aka help, infos, stats...
        Engine engine = Engine.getInstance();
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case COMMAND_LEVELS:
                    sendMessageAndReturn(sender, engine.getBhopLevels());
                case COMMAND_CHECKPOINTS:
                    //format checkpoints list if level specified
                    if (args.length > 1) {
                        BhopLevel bhl = engine.getBhopLevel(args[1]);
                        if (bhl == null)
                            return sendMessageAndReturn(sender, "§cAvailable levels: " + engine.getBhopLevels());
                        else
                            return sendMessageAndReturn(sender, bhl.getCheckpoints().stream().map(BhopCheckpoint::getName).collect(Collectors.joining()));
                    }
                    //format checkpoints list for current player, otherwise prompt for level name
                    BhopPlayer bhpl = null;
                    if (sender instanceof Player)
                        bhpl = engine.getBhopPlayer((Player) sender);
                    if (bhpl != null)
                        return sendMessageAndReturn(sender, bhpl.getCheckpoints());
                    else
                        return sendMessageAndReturn(sender, "§c/bhop checkpoints <level name>");
                    
                case COMMAND_LEADERBOARDS:
                    if (args.length == 1)
                        return sendMessageAndReturn(sender, "§c/bhop leaderboards <level name>");
                    BhopLevel bhl = engine.getBhopLevel(args[1]);
                    if (bhl == null)
                        return sendMessageAndReturn(sender, "§cAvailable levels: " + engine.getBhopLevels());
                    bhl.printLeaderboard(sender);
                    return true;

                case COMMAND_STAT:
                    if (args.length == 1)
                        engine.printPlayerStat(sender, sender.getName());
                    else
                        engine.printPlayerStat(sender, args[1]);
                    return true;

                case COMMAND_HELP:
                    sender.sendMessage("§6/bhop start <level name>\n" +
                                    "/bhop load <checkpoint name>\n" +
                                    "/bhop exit\n" +
                                    "/bhop practice <level name>\n" +
                                    "/bhop save <checkpoint name>\n" +
                                    "/bhop levels\n" +
                                    "/bhop checkpoints\n" +
                                    "/bhop leaderboard <level name>\n" +
                                    "/bhop stat [player]\n"
                    );
                    return true;
            }
        }

        //console can't play bhop
        if (!(sender instanceof Player))
            return sendMessageAndReturn(sender, CONSOLE_CANNOT_BHOP);
        if (!sender.hasPermission(BhopPermissions.GAMEPLAY))
            return sendMessageAndReturn(sender, NO_PERMISSION);

        if (args.length == 0)
            return sendMessageAndReturn(sender, command.getUsage());
        switch (args[0].toLowerCase()) {

            case COMMAND_START:
            case COMMAND_PRACTICE:
                Player player = (Player) sender;
                //check level
                if (args.length == 1) {
                    sender.sendMessage(LEVEL_REQUIRED);
                    sender.sendMessage(AVAILABLE_LEVELS);
                    return sendMessageAndReturn(sender, engine.getBhopLevels());
                }
                BhopLevel bhl = engine.getBhopLevel(args[1]);
                if (bhl == null) {
                    sender.sendMessage(LEVEL_NOT_FOUND);
                    sender.sendMessage(AVAILABLE_LEVELS);
                    return sendMessageAndReturn(sender, engine.getBhopLevels());
                }
                //trigger start event
                BhopPlayer bhpl = engine.playerStartEvent(player, bhl);
                if (bhpl != null && args[0].equalsIgnoreCase(COMMAND_PRACTICE))
                    bhpl.enableCheats();
                return sendMessageAndReturn(sender, String.format(LEVEL_STARTED, bhl.getName()));

            case COMMAND_SAVE:
                //check player
                bhpl = engine.getBhopPlayer((Player) sender);
                if (bhpl == null)
                    return sendMessageAndReturn(sender, NOT_PLAYING);
                if (!bhpl.isCheated())
                    return sendMessageAndReturn(sender, NOT_PRACTICE);
                if (args.length == 1)
                    return sendMessageAndReturn(sender, USAGE_SAVE);
                for (BhopCheckpoint cp : bhpl.getCheckpointsSet())
                    if (cp.getName().equalsIgnoreCase(args[1]))
                        sendMessageAndReturn(sender, String.format(CHECKPOINT_EXISTS, args[1]));
                Location loc = bhpl.getPlayer().getLocation();
                bhpl.addCheckpoint(new BhopCheckpoint(args[1], loc, loc, TriggerType.INTERACT));
                return sendMessageAndReturn(sender, String.format(CHECKPOINT_SAVED, args[1]));

            case COMMAND_LOAD:
                //check player
                bhpl = engine.getBhopPlayer((Player) sender);
                if (bhpl == null)
                    return sendMessageAndReturn(sender, NOT_PLAYING);
                //check checkpoint
                if (args.length == 1) {
                    sender.sendMessage(CHECKPOINT_REQUIRED);
                    sender.sendMessage(AVAILABLE_CHECKPOINTS);
                    return sendMessageAndReturn(sender, bhpl.getCheckpoints());
                }
                BhopCheckpoint bhcp = engine.getBhopCheckpoint(bhpl, args[1]);
                if (bhcp == null) {
                    sender.sendMessage(CHECKPOINT_NOT_FOUND);
                    sender.sendMessage(AVAILABLE_CHECKPOINTS);
                    return sendMessageAndReturn(sender, bhpl.getCheckpoints());
                }
                engine.playerLoadEvent(bhpl, bhcp);
                return true;

            case COMMAND_EXIT:
                //check player
                bhpl = engine.getBhopPlayer((Player) sender);
                if (bhpl == null)
                    return sendMessageAndReturn(sender, NOT_PLAYING);
                engine.playerExitEvent(bhpl);
                return true;

            case COMMAND_CONTINUE:
                //get bhop player from dc
                bhpl = engine.getDcPlayer((Player) sender);
                if (bhpl == null)
                    return sendMessageAndReturn(sender, NOT_PLAYING);
                //then trigger rejoin event
                engine.playerRejoinEvent(bhpl);
                return sendMessageAndReturn(sender, GAME_RESTORED);

            default:
                return sendMessageAndReturn(sender, command.getUsage());
        }
    }

    private boolean sendMessageAndReturn(CommandSender sender, String message) {
        sender.sendMessage(message);
        return true;
    }
}
