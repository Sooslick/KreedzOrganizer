package ru.sooslick.bhop;

import org.bukkit.command.CommandSender;
import ru.sooslick.bhop.command.BhopAction;

public class PendingCommand {
    private static final long LIFETIME = 30000L;

    private final CommandSender sender;
    private final BhopAction action;
    private final long timestamp;
    private boolean active;

    public PendingCommand(CommandSender sender, BhopAction action) {
        this.sender = sender;
        this.action = action;
        timestamp = System.currentTimeMillis();
        active = true;
    }

    public CommandSender getCommandSender() {
        return sender;
    }

    public BhopAction getAction() {
        return action;
    }

    public void deactivate() {
        active = false;
    }

    public boolean isActive() {
        if (System.currentTimeMillis() - timestamp > LIFETIME)
            deactivate();
        return active;
    }
}
