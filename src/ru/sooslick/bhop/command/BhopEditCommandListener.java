package ru.sooslick.bhop.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.server.TabCompleteEvent;
import org.jetbrains.annotations.NotNull;
import ru.sooslick.bhop.BhopAdmin;
import ru.sooslick.bhop.BhopAdminManager;
import ru.sooslick.bhop.BhopCheckpoint;
import ru.sooslick.bhop.BhopLevel;
import ru.sooslick.bhop.BhopPermissions;
import ru.sooslick.bhop.Engine;
import ru.sooslick.bhop.TriggerType;
import ru.sooslick.bhop.util.BhopUtil;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BhopEditCommandListener implements CommandExecutor {
    public static final String COMMAND_MANAGE = "bhopmanage";
    public static final String COMMAND_MANAGE_ALIAS = "bhed";

    private static final String COMMAND_CHECKPOINT = "checkpoint";
    private static final String COMMAND_CREATE = "create";
    private static final String COMMAND_DELETE = "delete";
    private static final String COMMAND_DISCARD = "discard";
    private static final String COMMAND_EDIT = "edit";
    private static final String COMMAND_HELP = "help";
    private static final String COMMAND_RESET = "reset";
    private static final String COMMAND_SAVE = "save";
    private static final String COMMAND_SET = "set";
    private static final String CONFIRMATION = "sure";
    private static final String SET_BOUND1 = "bound1";
    private static final String SET_BOUND2 = "bound2";
    private static final String SET_FINISH = "finish";
    private static final String SET_LOAD = "load";
    private static final String SET_REGION = "region";
    private static final String SET_START = "start";
    private static final String SET_TRIGGER = "trigger";
    private static final String SET_TRIGGER_TYPE = "triggertype";

    private static final String NO_PERMISSION = "§cYou have not permissions";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission(BhopPermissions.EDIT))
            return sendMessageAndReturn(sender, NO_PERMISSION);
        if (args.length == 0)
            return sendMessageAndReturn(sender, command.getUsage());

        switch (args[0].toLowerCase()) {
            case COMMAND_CREATE:
                if (args.length == 1)
                    return sendMessageAndReturn(sender, "§c/bhopmanage create <level name>");
                BhopLevel bhl = Engine.getInstance().getBhopLevel(args[1]);
                if (bhl != null)
                    return sendMessageAndReturn(sender, "§cLevel " + args[1] + " exists, use §6/bhopmanage edit " + args[1] + " §cinstead");
                BhopAdminManager.createLevel(sender, args[1]);
                return true;

            case COMMAND_DELETE:
                if (args.length == 1)
                    return sendMessageAndReturn(sender, "§c/bhopmanage delete <level name>");
                bhl = Engine.getInstance().getBhopLevel(args[1]);
                if (bhl == null)
                    return sendMessageAndReturn(sender, "§cLevel §6" + args[1] + " §cnot found");
                boolean confirm = (args.length > 2 && args[2].equalsIgnoreCase("sure"));
                BhopAdminManager.deleteLevel(sender, bhl, confirm);
                return true;

            case COMMAND_EDIT:
                if (args.length == 1)
                    return sendMessageAndReturn(sender, "§c/bhopmanage edit <level name>");
                bhl = Engine.getInstance().getBhopLevel(args[1]);
                if (bhl == null)
                    return sendMessageAndReturn(sender, "§cLevel §6" + args[1] + " §cnot found, use §6/bhopmanage create " + args[1] + " §cinstead");
                BhopAdminManager.editLevel(sender, bhl);
                return true;

            case COMMAND_SET:
                if (args.length == 1) {
                    sender.sendMessage("§c/bhopmanage set <parameter> <value>");
                    return sendMessageAndReturn(sender, "§cParameters: region, bound1, bound2, start, finish, trigger, checkpoint");
                }
                switch (args[1].toLowerCase()) {
                    case SET_REGION:
                        if (!Engine.getInstance().isUseWg()) {
                            return sendMessageAndReturn(sender, "§cWorldGuard regions not available, use §6/bhopmanage set bound1/bound2");
                        }
                        if (args.length == 2)
                            return sendMessageAndReturn(sender, "§c/bhopmanage set region <world guard region> [world]");
                        World world = null;
                        //get world from args if specified
                        if (args.length > 3)
                            world = Bukkit.getWorld(args[3]);
                        //otherwise get world from current player's location
                        else if (sender instanceof Player)
                            world = ((Player) sender).getWorld();
                        //check is world specified
                        if (world != null) {
                            BhopAdminManager.setRegion(sender, world, args[2]);
                            return true;
                        }
                        return sendMessageAndReturn(sender, "§cPlease specify world of your BhopLevel: §6/bhopmanage set region " + args[2] + " [world]");

                    case SET_BOUND1:
                    case SET_BOUND2:
                        Location loc = parseLocation(sender, args);
                        if (loc != null) {
                            BhopAdminManager.setBound(sender, args[1], loc);
                            return true;
                        }
                        return sendMessageAndReturn(sender, "§cPlease specify location of your BhopLevel: §6/bhopmanage set " + args[1] + " [world] [location]");

                    case SET_START:
                        loc = parseLocation(sender, args);
                        if (loc != null) {
                            BhopAdminManager.setStart(sender, loc);
                            return true;
                        }
                        return sendMessageAndReturn(sender, "§cPlease specify location of your BhopLevel: §6/bhopmanage set start [world] [location]");

                    case SET_FINISH:
                        loc = parseLocation(sender, args);
                        if (loc != null) {
                            BhopAdminManager.setFinish(sender, loc);
                            return true;
                        }
                        return sendMessageAndReturn(sender, "§cPlease specify location of your BhopLevel: §6/bhopmanage set finish [world] [location]");

                    case SET_TRIGGER_TYPE:
                        if (args.length == 2)
                            return sendMessageAndReturn(sender, "§c/bhopmanage set triggertype [movement | interact]");
                        try {
                            TriggerType tt = TriggerType.valueOf(args[2].toUpperCase());
                            BhopAdminManager.setTriggerType(sender, tt);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage("§cUnknown trigger type: " + args[2] + ". Allowed types: movement, interact");
                        }
                        return true;

                    default:
                        return sendMessageAndReturn(sender, "§cParameters: region, bound1, bound2, start, finish, trigger, checkpoint");
                }

            case COMMAND_CHECKPOINT:
                if (args.length == 1)
                    return sendMessageAndReturn(sender, "§c/bhopmanage checkpoint <create | edit | delete | set | save | discard> <params>");

                switch (args[1].toLowerCase()) {
                    case COMMAND_CREATE:
                        if (args.length == 2)
                            return sendMessageAndReturn(sender, "§c/bhopmanage checkpoint create <name>");
                        BhopAdminManager.createCheckpoint(sender, args[2]);
                        return true;

                    case COMMAND_EDIT:
                        if (args.length == 2)
                            return sendMessageAndReturn(sender, "§c/bhopmanage checkpoint edit <name>");
                        BhopAdminManager.editCheckpoint(sender, args[2]);
                        return true;

                    case COMMAND_DELETE:
                        if (args.length == 2)
                            return sendMessageAndReturn(sender, "§c/bhopmanage checkpoint delete <name>");
                        BhopAdminManager.deleteCheckpoint(sender, args[2]);
                        return true;

                    case COMMAND_SET:
                        if (args.length == 2)
                            return sendMessageAndReturn(sender, "§c/bhopmanage checkpoint set <load | trigger | triggerType> <params>");

                        switch (args[2].toLowerCase()) {
                            case SET_LOAD:
                                Location loc = parseLocation(sender, args, true);
                                if (loc != null) {
                                    BhopAdminManager.setCheckpointLoad(sender, loc);
                                    return true;
                                }
                                return sendMessageAndReturn(sender, "§cPlease specify location of load position: §6/bhopmanage checkpoint set load [world] [location]");

                            case SET_TRIGGER:
                                loc = parseLocation(sender, args, true);
                                if (loc != null) {
                                    BhopAdminManager.setCheckpointTrigger(sender, loc);
                                    return true;
                                }
                                return sendMessageAndReturn(sender, "§cPlease specify location of trigger position: §6/bhopmanage checkpoint set trigger [world] [location]");

                            case SET_TRIGGER_TYPE:
                                if (args.length == 3)
                                    return sendMessageAndReturn(sender, "§c/bhopmanage checkpoint set triggertype [movement | interact]");
                                try {
                                    TriggerType tt = TriggerType.valueOf(args[3].toUpperCase());
                                    BhopAdminManager.setCheckpointTriggerType(sender, tt);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage("§cUnknown trigger type: " + args[2] + ". Allowed types: movement, interact");
                                }
                                return true;

                            default:
                                return sendMessageAndReturn(sender, "§cAvailable subcommands: load, trigger, triggerType");
                        }

                    case COMMAND_SAVE:
                        BhopAdminManager.saveCheckpoint(sender);
                        return true;

                    case COMMAND_DISCARD:
                        BhopAdminManager.discardCheckpoint(sender);
                        return true;

                    default:
                        return sendMessageAndReturn(sender, "Available checkpoint commands: create, edit, delete, set, save, discard");
                }

            case COMMAND_SAVE:
                BhopAdminManager.saveLevel(sender);
                return true;

            case COMMAND_DISCARD:
                confirm = (args.length > 1 && args[1].equals(CONFIRMATION));
                BhopAdminManager.discardLevel(sender, confirm);
                return true;

            case COMMAND_RESET:
                if (args.length == 1)
                    return sendMessageAndReturn(sender, "§c/bhopmanage reset <level name>");
                bhl = Engine.getInstance().getBhopLevel(args[1]);
                if (bhl == null)
                    return sendMessageAndReturn(sender, "§cLevel §6" + args[1] + " §cnot found.");
                confirm = (args.length > 2 && args[2].equals(CONFIRMATION));
                BhopAdminManager.resetScore(sender, bhl, confirm);
                return true;

            case COMMAND_HELP:
                if (args.length == 1) {
                    sender.sendMessage("§c/bhopmanage <command> <parameters>...");
                    sender.sendMessage("§6Available commands:");
                    sender.sendMessage("§eCreate §f- create a new level");
                    sender.sendMessage("§eEdit §f- edit an existing level");
                    sender.sendMessage("§eDelete §f- delete a level");
                    sender.sendMessage("§eSet §f- change one of level's parameter");
                    sender.sendMessage("§eCheckpoint §f- manage level's checkpoints");
                    sender.sendMessage("§eSave §f- save edited level");
                    sender.sendMessage("§eDiscard §f- discard any changes");
                    sender.sendMessage("§eReset §f- clear leaderboards for level");
                    return true;
                }
                switch (args[1]) {
                    case COMMAND_SET:
                        sender.sendMessage("§c/bhopmanage set <parameter> <values>");
                        sender.sendMessage("§6Available parameters:");
                        sender.sendMessage("§eRegion §f- assign WorldGuard region to the level");
                        sender.sendMessage("§eBound1 §f- set first bounding for level's area");
                        sender.sendMessage("§eBound2 §f- set second bounding for level's area");
                        sender.sendMessage("§eStart §f- set start position for level");
                        sender.sendMessage("§eFinish §f- set finish position for level");
                        sender.sendMessage("§eTriggerType §f- set action for finish triggering");
                        return true;
                    case COMMAND_CHECKPOINT:
                        sender.sendMessage("§c/bhopmanage checkpoint <command> <parameters>");
                        sender.sendMessage("§6Available commands:");
                        sender.sendMessage("§eCreate §f- create a new checkpoint");
                        sender.sendMessage("§eEdit §f- edit an existing checkpoint");
                        sender.sendMessage("§eDelete §f- delete a checkpoint");
                        sender.sendMessage("§eSet §f- change one of checkpoint's parameter");
                        sender.sendMessage("§eSet Load §f- set load position for checkpoint");
                        sender.sendMessage("§eSet Trigger §f- set trigger position that opens the checkpoint");
                        sender.sendMessage("§eSet TriggerType §f- set action required for opening this checkpoint");
                        sender.sendMessage("§eSave §f- save edited checkpoint");
                        sender.sendMessage("§eDiscard §f- discard any changes");
                        return true;
                    default:
                        sender.sendMessage("§c/bhopmanage help");
                }
                return true;

            default:
                return sendMessageAndReturn(sender, "§cUnknown subcommand, type §6/bhopmanage help §cfor info");
        }
    }

    private Location parseLocation(CommandSender sender, String[] args) {
        return parseLocation(sender, args, false);
    }

    private Location parseLocation(CommandSender sender, String[] args, boolean checkpoint) {
        int from = checkpoint ? 4 : 3;
        //try to get loc from args
        if (args.length > from)
            return BhopUtil.stringToLocation(Bukkit.getWorld(args[2]), BhopUtil.join(", ", args, from));
            //else try to get loc from playerpos
        else if (sender instanceof Player)
            return ((Player) sender).getLocation();
        return null;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean sendMessageAndReturn(CommandSender sender, String message) {
        sender.sendMessage(message);
        return true;
    }

    //////////////////
    // TAB COMPLETE //

    public static void tabComplete(TabCompleteEvent e, String[] args) {
        if (args.length == 1) {
            e.setCompletions(Arrays.asList(COMMAND_CREATE, COMMAND_EDIT, COMMAND_DELETE, COMMAND_SET, COMMAND_CHECKPOINT,
                    COMMAND_SAVE, COMMAND_DISCARD, COMMAND_RESET, COMMAND_HELP));
            return;
        }
        Engine engine = Engine.getInstance();
        if (args.length == 2) {
            switch (args[1].toLowerCase()) {
                case COMMAND_EDIT:
                case COMMAND_DELETE:
                case COMMAND_RESET:
                    e.setCompletions(engine.getBhopLevelList().stream().map(BhopLevel::getName).collect(Collectors.toList()));
                    return;
                case COMMAND_SET:
                    e.setCompletions(Arrays.asList(SET_BOUND1, SET_BOUND2, SET_START, SET_FINISH, SET_REGION, SET_TRIGGER_TYPE));
                    return;
                case COMMAND_CHECKPOINT:
                    e.setCompletions(Arrays.asList(COMMAND_CREATE, COMMAND_EDIT, COMMAND_DELETE, COMMAND_SET,
                            COMMAND_SAVE, COMMAND_DISCARD));
                    return;
                default:
                    tabComplete(e, Arrays.copyOf(args, args.length-1));
                    return;
            }
        }
        if (args.length == 3) {
            if (args[1].equalsIgnoreCase(COMMAND_SET) && args[2].equalsIgnoreCase(SET_TRIGGER_TYPE)) {
                e.setCompletions(Arrays.asList("movement", "interact"));
                return;
            }
            if (args[1].equalsIgnoreCase(COMMAND_CHECKPOINT)) {
                if (args[2].equalsIgnoreCase(COMMAND_EDIT) || args[2].equalsIgnoreCase(COMMAND_DELETE)) {
                    BhopAdmin admin = BhopAdminManager.getActiveAdmin(e.getSender());
                    e.setCompletions(admin.getLevel().getCheckpoints().stream().map(BhopCheckpoint::getName).collect(Collectors.toList()));
                    return;
                }
                if (args[2].equalsIgnoreCase(COMMAND_SET)) {
                    e.setCompletions(Arrays.asList(SET_TRIGGER_TYPE, SET_TRIGGER, SET_LOAD));
                    return;
                }
            }
            tabComplete(e, Arrays.copyOf(args, args.length-1));
            return;
        }
        if (args.length >= 4 && args[1].equalsIgnoreCase(COMMAND_CHECKPOINT) && args[2].equalsIgnoreCase(COMMAND_SET) && args[3].equalsIgnoreCase(SET_TRIGGER_TYPE)) {
            e.setCompletions(Arrays.asList("movement", "interact"));
        }
    }
}
