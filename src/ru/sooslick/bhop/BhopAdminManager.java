package ru.sooslick.bhop;

import org.bukkit.command.CommandSender;
import ru.sooslick.bhop.command.BhopAction;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BhopAdminManager {
    private static List<PendingCommand> pendingCommands;
    private static List<BhopAdmin> activeAdmins;

    //disable constructor
    private BhopAdminManager() {}

    public static void init() {
        pendingCommands = new LinkedList<>();
        activeAdmins = new LinkedList<>();
    }

    public static void createLevel(CommandSender sender, String levelName) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            //no session found, just create level
            activeAdmins.add(new BhopAdmin(sender, new BhopLevel(levelName)));
            sender.sendMessage("§eCreated level §6" + levelName + " §e and started edit session");
            //todo: send status
            return;
        }
        sender.sendMessage("§cYou have an unfinished edit session, use §6/bhopmanage save §cor §6/bhopmanage discard");
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

    public static void editLevel(CommandSender sender, BhopLevel bhl) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            activeAdmins.add(new BhopAdmin(sender, bhl.clone()));
            sender.sendMessage("§eStarted editing §6" + bhl.getName());
            return;
        }
        sender.sendMessage("§cYou have an unfinished edit session, use §6/bhopmanage save §cor §6/bhopmanage discard");
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

    private static BhopAdmin getActiveAdmin(CommandSender sender) {
        return activeAdmins.stream()
                .filter(admin -> admin.getAdmin().equals(sender))
                .findFirst()
                .orElse(null);
    }
}
