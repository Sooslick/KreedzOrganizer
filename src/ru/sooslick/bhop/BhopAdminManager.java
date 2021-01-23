package ru.sooslick.bhop;

import org.bukkit.Location;
import org.bukkit.World;
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
            admin = new BhopAdmin(sender, new BhopLevel(levelName), true);
            activeAdmins.add(admin);
            sender.sendMessage("§eCreated level §6" + levelName + " §e and started edit session");
            admin.sendStatus();
            return;
        }
        sender.sendMessage("§cYou have an unfinished edit session, use §6/bhopmanage save §cor §6/bhopmanage discard");
    }

    public static void deleteLevel(CommandSender sender, BhopLevel bhl, boolean confirm) {
        PendingCommand pc = getPendingCommand(sender);
        if (pc == null) {
            pendingCommands.add(new PendingCommand(sender, BhopAction.DELETE));
            sender.sendMessage("§cYou cannot undo this operation. Type §e/bhopmanage delete " + bhl.getName() + " sure §cfor confirmation");
            return;
        }
        if (pc.getAction() != BhopAction.DELETE) {
            pc.deactivate();
            pendingCommands.add(new PendingCommand(sender, BhopAction.DELETE));
            sender.sendMessage("§cYou cannot undo this operation. Type §e/bhopmanage delete " + bhl.getName() + " sure §cfor confirmation");
            return;
        }
        if (confirm)
            Engine.getInstance().deleteLevel(bhl);
        else
            sender.sendMessage("§cYou cannot undo this operation. Type §e/bhopmanage delete " + bhl.getName() + " sure §cfor confirmation");
    }

    public static void editLevel(CommandSender sender, BhopLevel bhl) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            activeAdmins.add(new BhopAdmin(sender, bhl.clone(), false));
            sender.sendMessage("§eStarted editing §6" + bhl.getName());
            return;
        }
        sender.sendMessage("§cYou have an unfinished edit session, use §6/bhopmanage save §cor §6/bhopmanage discard");
    }

    public static void setRegion(CommandSender sender, World world, String rgName) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.setRegion(world, rgName);
    }

    public static void setBound(CommandSender sender, String cmd, Location loc) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.setBound(cmd, loc);
    }

    public static void setStart(CommandSender sender, Location loc) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.setStart(loc);
    }

    public static void setFinish(CommandSender sender, Location loc) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.setFinish(loc);
    }

    public static void setTriggerType(CommandSender sender, TriggerType triggerType) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.setTriggerType(triggerType);
    }

    public static void saveLevel(CommandSender sender) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        if (admin.saveLevel())
            activeAdmins.remove(admin);
    }

    public static void discardLevel(CommandSender sender, boolean confirm) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        PendingCommand pc = getPendingCommand(sender);
        if (pc == null) {
            pendingCommands.add(new PendingCommand(sender, BhopAction.DISCARD));
            sender.sendMessage("§cYou cannot undo this operation. Type §e/bhopmanage discard sure §cfor confirmation");
            return;
        }
        if (pc.getAction() != BhopAction.DELETE) {
            pc.deactivate();
            pendingCommands.add(new PendingCommand(sender, BhopAction.DISCARD));
            sender.sendMessage("§cYou cannot undo this operation. Type §e/bhopmanage discard sure §cfor confirmation");
            return;
        }
        if (confirm)
            activeAdmins.remove(admin);
        else
            sender.sendMessage("§cYou cannot undo this operation. Type §e/bhopmanage discard sure §cfor confirmation");
    }

    public static void resetScore(CommandSender sender, BhopLevel level, boolean confirm) {
        PendingCommand pc = getPendingCommand(sender);
        if (pc == null) {
            pendingCommands.add(new PendingCommand(sender, BhopAction.RESET));
            sender.sendMessage("§cYou cannot undo this operation. Type §e/bhopmanage reset " + level.getName() + " sure §cfor confirmation");
            return;
        }
        if (pc.getAction() != BhopAction.DELETE) {
            pc.deactivate();
            pendingCommands.add(new PendingCommand(sender, BhopAction.RESET));
            sender.sendMessage("§cYou cannot undo this operation. Type §e/bhopmanage reset " + level.getName() + " sure §cfor confirmation");
            return;
        }
        if (confirm)
            level.getRecords().clear();
        else
            sender.sendMessage("§cYou cannot undo this operation. Type §e/bhopmanage reset " + level.getName() + " sure §cfor confirmation");
    }

    public static void createCheckpoint(CommandSender sender, String name) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.createCheckpoint(name);
    }

    public static void editCheckpoint(CommandSender sender, String name) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.editCheckpoint(name);
    }

    public static void deleteCheckpoint(CommandSender sender, String name) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.deleteCheckpoint(name);
    }

    public static void setCheckpointLoad(CommandSender sender, Location location) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.setCheckpointLoad(location);
    }

    public static void setCheckpointTrigger(CommandSender sender, Location location) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.setCheckpointTrigger(location);
    }

    public static void setCheckpointTriggerType(CommandSender sender, TriggerType type) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.setCheckpointTriggerType(type);
    }

    public static void saveCheckpoint(CommandSender sender) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.saveCheckpoint();
    }

    public static void discardCheckpoint(CommandSender sender) {
        BhopAdmin admin = getActiveAdmin(sender);
        if (admin == null) {
            sender.sendMessage("§cYou are not in editing mode. Use Create or Edit command first.");
            return;
        }
        admin.discardCheckpoint();
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
