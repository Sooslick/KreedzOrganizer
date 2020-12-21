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
    private static final String COMMAND_DELETE = "delete";

    //todo: same messages in BhopCommandListener
    private static final String NO_PERMISSION = "§cYou have not permissions";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission(BhopPermissions.EDIT))
            return sendMessageAndReturn(sender, NO_PERMISSION);
        if (args.length == 0)
            return sendMessageAndReturn(sender, command.getUsage());
        switch (args[0].toLowerCase()) {
            case COMMAND_DELETE:
                if (args.length == 1)
                    return sendMessageAndReturn(sender, "/bhopedit delete <level name>");
                BhopLevel bhl = Engine.getInstance().getBhopLevel(args[1]);
                if (bhl == null)
                    return sendMessageAndReturn(sender, "Level " + args[1] + " not found");
                BhopAdminManager.deleteLevel(sender, bhl);
                return true;
            default:
                return sendMessageAndReturn(sender, command.getUsage());
        }
    }

    private boolean sendMessageAndReturn(CommandSender sender, String message) {
        sender.sendMessage(message);
        return true;
    }
}
