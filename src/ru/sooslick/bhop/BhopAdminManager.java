package ru.sooslick.bhop;

import org.bukkit.command.CommandSender;
import ru.sooslick.bhop.command.BhopAction;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BhopAdminManager {
    private static List<PendingCommand> pendingCommands;

    //disable constructor
    private BhopAdminManager() {}

    public static void init() {
        pendingCommands = new LinkedList<>();
    }

    public static void deleteLevel(CommandSender sender, BhopLevel bhl) {
        PendingCommand pc = getPendingCommand(sender);
        if (pc == null) {
            pendingCommands.add(new PendingCommand(sender, BhopAction.DELETE));
            sender.sendMessage("§cYou cannot undo this operation. Type §e/bhop delete " + bhl.getName() + " sure §cfor confirmation");
            return;
        }
        if (pc.getAction() != BhopAction.DELETE) {
            pc.deactivate();
            pendingCommands.add(new PendingCommand(sender, BhopAction.DELETE));
            sender.sendMessage("§cYou cannot undo this operation. Type §e/bhop delete " + bhl.getName() + " sure §cfor confirmation");
            return;
        }
        Engine.getInstance().deleteLevel(bhl);
    }

    private static List<PendingCommand> activePendingCommands() {
        //cleanup
        pendingCommands.removeAll(pendingCommands.stream()
                .filter(PendingCommand::isActive)
                .collect(Collectors.toList()));
        return pendingCommands;
    }

    private static PendingCommand getPendingCommand(CommandSender sender) {
        return activePendingCommands().stream()
                .filter(pc -> pc.getCommandSender().equals(sender))
                .findFirst()
                .orElse(null);
    }
}
