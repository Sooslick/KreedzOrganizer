package ru.sooslick.bhop.command;

import org.bukkit.Bukkit;
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

public class BhopEditCommandListener implements CommandExecutor {
    private static final String COMMAND_CREATE = "create";
    private static final String COMMAND_DELETE = "delete";
    private static final String COMMAND_EDIT = "edit";
    private static final String COMMAND_SET = "set";

    private static final String SET_REGION = "region";

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
                        if (args.length > 3)
                            world = Bukkit.getWorld(args[3]);
                        else if (sender instanceof Player)
                            world = ((Player) sender).getWorld();
                        if (world != null) {
                            BhopAdminManager.setRegion(sender, world, args[2]);
                            return true;
                        }
                        sendMessageAndReturn(sender, "§cPlease specify world of your BhopLevel: §6/bhop set region " + args[2] + " [world]");
                    default:
                        return sendMessageAndReturn(sender, "§cParameters: region, bound1, bound2, start, finish, trigger, checkpoint");
                }

            default:
                return sendMessageAndReturn(sender, command.getUsage());
        }
    }

    private boolean sendMessageAndReturn(CommandSender sender, String message) {
        sender.sendMessage(message);
        return true;
    }
}
