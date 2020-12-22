package ru.sooslick.bhop.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.sooslick.bhop.BhopAdminManager;
import ru.sooslick.bhop.BhopLevel;
import ru.sooslick.bhop.BhopPermissions;
import ru.sooslick.bhop.Engine;

public class BhopEditCommandListener implements CommandExecutor {
    private static final String COMMAND_CREATE = "create";
    private static final String COMMAND_DELETE = "delete";
    private static final String COMMAND_EDIT = "edit";

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
            default:
                return sendMessageAndReturn(sender, command.getUsage());
        }
    }

    private boolean sendMessageAndReturn(CommandSender sender, String message) {
        sender.sendMessage(message);
        return true;
    }
}
