package ru.sooslick.bhop.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.sooslick.bhop.BhopAdminManager;
import ru.sooslick.bhop.BhopLevel;
import ru.sooslick.bhop.BhopPermissions;
import ru.sooslick.bhop.Engine;
import ru.sooslick.bhop.util.BhopUtil;

public class BhopEditCommandListener implements CommandExecutor {
    private static final String COMMAND_CREATE = "create";
    private static final String COMMAND_DELETE = "delete";
    private static final String COMMAND_EDIT = "edit";
    private static final String COMMAND_SET = "set";

    private static final String SET_REGION = "region";
    private static final String SET_BOUND1 = "bound1";
    private static final String SET_BOUND2 = "bound2";
    private static final String SET_START = "start";
    private static final String SET_FINISH = "finish";

    //todo: same messages in BhopCommandListener
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
                //todo check confirmation needed
                BhopAdminManager.deleteLevel(sender, bhl);
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
                        //todo: check wgEnabled
                        if (args.length == 2)
                            return sendMessageAndReturn(sender, "§c/bhop set region <world guard region> [world]");
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
                        return sendMessageAndReturn(sender, "§cPlease specify world of your BhopLevel: §6/bhop set region " + args[2] + " [world]");

                    case SET_BOUND1:
                    case SET_BOUND2:
                        if (args.length == 2)
                            return sendMessageAndReturn(sender, "§c/bhop set " + args[1] + " [world] [location]");
                        Location loc = parseLocation(sender, args);
                        if (loc != null) {
                            BhopAdminManager.setBound(sender, args[1], loc);
                            return true;
                        }
                        return sendMessageAndReturn(sender, "§cPlease specify location of your BhopLevel: §6/bhop set " + args[1] + " [world] [location]");

                    case SET_START:
                        if (args.length == 2)
                            return sendMessageAndReturn(sender, "§c/bhop set start [world] [location]");
                        loc = parseLocation(sender, args);
                        if (loc != null) {
                            BhopAdminManager.setStart(sender, loc);
                            return true;
                        }
                        return sendMessageAndReturn(sender, "§cPlease specify location of your BhopLevel: §6/bhop set start [world] [location]");

                    case SET_FINISH:
                        if (args.length == 2)
                            return sendMessageAndReturn(sender, "§c/bhop set finish [world] [location]");
                        loc = parseLocation(sender, args);
                        if (loc != null) {
                            BhopAdminManager.setFinish(sender, loc);
                            return true;
                        }
                        return sendMessageAndReturn(sender, "§cPlease specify location of your BhopLevel: §6/bhop set finish [world] [location]");

                    default:
                        return sendMessageAndReturn(sender, "§cParameters: region, bound1, bound2, start, finish, trigger, checkpoint");
                }

            default:
                return sendMessageAndReturn(sender, command.getUsage());
        }
    }

    private Location parseLocation(CommandSender sender, String[] args) {
        //try to get loc from args
        if (args.length > 3)
            return BhopUtil.stringToLocation(Bukkit.getWorld(args[2]), BhopUtil.join(", ", args, 3));
            //else try to get loc from playerpos
        else if (sender instanceof Player)
            return ((Player) sender).getLocation();
        return null;
    }

    private boolean sendMessageAndReturn(CommandSender sender, String message) {
        sender.sendMessage(message);
        return true;
    }
}
