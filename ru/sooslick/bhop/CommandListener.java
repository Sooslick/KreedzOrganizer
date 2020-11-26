package ru.sooslick.bhop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandListener implements CommandExecutor {

    private final Engine engine;

    private final String COMMAND_START = "start";
    private final String COMMAND_LOAD = "load";
    private final String COMMAND_EXIT = "exit";

    public CommandListener(Engine engine) {
        this.engine = engine;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //todo check another commands and permissions
        if (args.length == 0)
            return sendInfoMessage(sender, "infomessage"); //todo
        switch (args[0]) {
            case COMMAND_START:
                //console can't play bhop
                if (!(sender instanceof Player))
                    return sendInfoMessage(sender, "xaxa console cannot bhop )))"); //todo
                Player player = (Player) sender;
                //check level
                if (args.length == 1)
                    return sendInfoMessage(sender, "infomessage - level required"); //todo
                BhopLevel bhl = engine.getBhopLevel(args[1]);
                if (bhl == null)
                    return sendInfoMessage(sender, "infomessage"); //todo
                //trigger start event
                engine.playerStartEvent(player, bhl);
                return true;
            case COMMAND_LOAD:
                //console can't play bhop
                if (!(sender instanceof Player))
                    return sendInfoMessage(sender, "xaxa console cannot bhop )))"); //todo
                //check checkpoint
                if (args.length == 1)
                    return sendInfoMessage(sender, "infomessage"); //todo
                BhopPlayer bhpl = engine.getBhopPlayer((Player) sender);
                if (bhpl == null)
                    return sendInfoMessage(sender, "infomessage"); //todo
                BhopCheckpoint bhcp = engine.getBhopCheckpoint(bhpl, args[1]);
                if (bhcp == null)
                    return sendInfoMessage(sender, "infomessage"); //todo
                engine.playerLoadEvent(bhpl, bhcp);
                break;
            case COMMAND_EXIT:
                //console can't play bhop
                if (!(sender instanceof Player))
                    return sendInfoMessage(sender, "xaxa console cannot bhop )))"); //todo
                //check player
                bhpl = engine.getBhopPlayer((Player) sender);
                if (bhpl == null)
                    sendInfoMessage(sender, "гаф тяф"); //todo
                engine.playerExitEvent(bhpl);
                break;
            default:
                return sendInfoMessage(sender, "infomessage"); //todo
        }
        return false;
    }

    //todo weird method. Make void
    private boolean sendInfoMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
        return true;
    }

}
